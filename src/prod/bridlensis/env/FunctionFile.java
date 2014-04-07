package bridlensis.env;

import java.util.List;

import bridlensis.InputReader;
import bridlensis.InvalidSyntaxException;
import bridlensis.StatementFactory;

public class FunctionFile implements Callable {

	private static final int FILE_INDEX = 0;
	private static final int OPTIONS_INDEX = 1;
	private static final int OUTPATH_INDEX = 2;

	public FunctionFile() {
	}

	@Override
	public int getMandatoryArgsCount() {
		return 1;
	}

	@Override
	public int getArgsCount() {
		return 3;
	}

	@Override
	public ReturnType getReturnType() {
		return ReturnType.VOID;
	}

	@Override
	public String statementFor(String indent, List<String> args,
			Variable returnVar) throws InvalidSyntaxException {
		StringBuilder sb = new StringBuilder(indent);
		if (!args.get(OUTPATH_INDEX).equals(StatementFactory.NULL)) {
			sb.append("SetOutPath ");
			sb.append(args.get(OUTPATH_INDEX));
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}
		sb.append("File ");
		if (!args.get(OPTIONS_INDEX).equals(StatementFactory.NULL)) {
			String options = StatementFactory.deString(args.get(OPTIONS_INDEX));
			if (!options.isEmpty()) {
				sb.append(options);
				sb.append(' ');
			}
		}
		sb.append(args.get(FILE_INDEX));
		return sb.toString();
	}

}