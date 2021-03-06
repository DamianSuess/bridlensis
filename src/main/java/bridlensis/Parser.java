package bridlensis;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;

import bridlensis.env.EnvironmentException;

public class Parser {

	public static final char UTF16BE_BOM = '\uFFFE';
	public static final char UTF16LE_BOM = '\uFEFF';

	private static final Logger logger = Logger.getInstance();

	private File baseDir;
	private File outDir;
	private String encoding;
	private Collection<String> excludeFiles;
	private int fileCount = 0;
	private int inputLines = 0;
	private StatementParser statementParser;
	private boolean insideMacro;

	public Parser(StatementParser statementParser, File baseDir, File outDir,
			String encoding, Collection<String> excludeFiles) {
		this.baseDir = baseDir;
		this.outDir = outDir;
		this.encoding = encoding;
		this.statementParser = statementParser;
		this.excludeFiles = new ArrayList<String>();
		if (excludeFiles != null) {
			this.excludeFiles.addAll(excludeFiles);
		}
	}

	public int getInputLines() {
		return inputLines;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void parse(String inputFileName, String outputFileName)
			throws IOException, ParserException {
		insideMacro = false;
		File inputFile = new File(baseDir, inputFileName);
		logger.debug("Begin parse file: " + inputFile.getAbsolutePath());
		try (BufferedWriter writer = getOutputWriter(outputFileName)) {
			writer.write(NSISStatements.nullDefine());
			parseFile(inputFile, writer);
		}
	}

	private BufferedWriter getOutputWriter(String outputFileName)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		File outputFile = new File(outDir, outputFileName);
		File outDirPath = outputFile.getParentFile();
		if (!outDirPath.exists() || !outDirPath.isDirectory()) {
			if (!outDirPath.mkdirs()) {
				throw new IOException("Unable to create directory "
						+ outDirPath.getAbsolutePath());
			}
		}
		logger.debug("Output file: " + outputFile.getAbsolutePath());
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(outputFile), encoding));
		if (encoding.equalsIgnoreCase("UTF-16LE")) {
			writer.write(UTF16LE_BOM);
		} else if (encoding.equalsIgnoreCase("UTF-16BE")) {
			writer.write(UTF16BE_BOM);
		}
		return writer;
	}

	private void parseFile(File inputFile, BufferedWriter writer)
			throws IOException, ParserException {
		InputReader reader = new InputReader(inputFile, encoding);
		fileCount++;
		try {
			while (reader.goToNextStatement()) {
				writer.write(parseStatement(reader));
				writer.write(NSISStatements.NEWLINE_MARKER);
			}
			logger.debug(String.format("End parsing %d lines in file %s.",
					reader.getLinesRead(), inputFile.getAbsolutePath()));
			inputLines += reader.getLinesRead();
		} catch (InvalidSyntaxException | EnvironmentException e) {
			throw new ParserException(inputFile.getAbsolutePath(),
					reader.getLinesRead(), e);
		} finally {
			reader.close();
		}
	}

	protected String parseStatement(InputReader reader)
			throws InvalidSyntaxException, EnvironmentException,
			ParserException {
		if (!reader.hasNextWord()) {
			return reader.getCurrentStatement();
		}

		Word word = reader.nextWord();
		String keyword = word.asName();
		WordTail tail = reader.getWordTail();

		if (tail.isCompilerCommand()) {
			String command = reader.nextWord().asName();
			if (command.equals("include")) {
				return parseInclude(reader);
			} else if (command.equals("macro")) {
				insideMacro = true;
				return reader.getCurrentStatement();
			} else if (command.equals("macroend")) {
				insideMacro = false;
				return reader.getCurrentStatement();
			}
		}

		if (insideMacro) {
			return reader.getCurrentStatement();
		} else if (tail.isAssignment()) {
			return statementParser.parseVarAssign(word, reader);
		} else if (tail.isFunctionArgsOpen()) {
			return statementParser.parseCall(word, null, reader);
		} else if (keyword.equals("var")) {
			return statementParser.parseVarDeclare(reader);
		} else if (keyword.equals("function")) {
			return statementParser.parseFunctionBegin(reader);
		} else if (keyword.equals("return")) {
			return statementParser.parseFunctionReturn(reader);
		} else if (keyword.equals("functionend")) {
			return statementParser.parseFunctionEnd(reader);
		} else if (keyword.equals("if") || keyword.equals("elseif")) {
			return statementParser.parseIf(word, reader);
		} else if (keyword.equals("else")) {
			return NSISStatements.logicLibDefine(reader.getIndent(), "Else");
		} else if (keyword.equals("endif")) {
			return NSISStatements.logicLibDefine(reader.getIndent(), "EndIf");
		} else if (keyword.equals("do")) {
			return statementParser.parseDoLoop("Do", reader);
		} else if (keyword.equals("continue")) {
			return NSISStatements
					.logicLibDefine(reader.getIndent(), "Continue");
		} else if (keyword.equals("break")) {
			return NSISStatements.logicLibDefine(reader.getIndent(), "Break");
		} else if (keyword.equals("loop")) {
			return statementParser.parseDoLoop("Loop", reader);
		}

		return reader.getCurrentStatement();
	}

	private String parseInclude(InputReader reader)
			throws InvalidSyntaxException, ParserException {
		String inputFileName = reader.nextWord().asBareString();
		File inputFile = new File(baseDir, inputFileName);
		String statement;
		if (excludeFiles.contains(inputFileName)
				|| excludeFiles.contains(inputFile.getAbsolutePath())) {
			// Handle excluded file
			logger.info(reader, "Include file '" + inputFileName
					+ "' omitted being marked as excluded.");
			String outputFileName = MakeBridleNSIS
					.convertToBridleFilename(inputFileName);
			File outputFile = new File(outDir, outputFileName);
			copyFile(inputFile, outputFile, reader.getLinesRead());
			statement = NSISStatements.include(reader.getIndent(),
					outputFileName);
		} else if (!inputFile.exists()) {
			// Include file not found
			logger.debug(reader, "Include file '" + inputFileName
					+ "' not found, assuming it's found by NSIS.");
			statement = reader.getCurrentStatement();
		} else {
			// Parse include file
			logger.debug(reader,
					"Follow include: " + inputFile.getAbsolutePath());
			String outputFileName = MakeBridleNSIS
					.convertToBridleFilename(inputFileName);
			try (BufferedWriter writer = getOutputWriter(outputFileName)) {
				parseFile(inputFile, writer);
			} catch (IOException e) {
				throw new InvalidSyntaxException(e.getMessage(), e);
			}
			statement = NSISStatements.include(reader.getIndent(),
					outputFileName);
		}
		return statement;
	}

	private void copyFile(File sourceFile, File destFile, int lineNumber)
			throws ParserException {
		logger.debug(String.format("Copy file '%s' to directory '%s'",
				sourceFile.getAbsolutePath(), outDir.getAbsolutePath()));
		try (FileInputStream input = new FileInputStream(sourceFile);
				FileOutputStream output = new FileOutputStream(destFile)) {
			if (!destFile.exists() && !destFile.createNewFile()) {
				throw new ParserException(sourceFile.getAbsolutePath(),
						lineNumber, new IOException(
								"Unable to create output file "
										+ destFile.getAbsolutePath()));
			}
			FileChannel destination = output.getChannel();
			FileChannel source = input.getChannel();
			destination.transferFrom(source, 0, source.size());
		} catch (IOException e) {
			throw new ParserException(sourceFile.getAbsolutePath(), lineNumber,
					e);
		}
	}
}
