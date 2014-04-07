package bridlensis.env;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import bridlensis.InputReader;

public class UserFunction implements Callable {

	private final String name;
	private List<Variable> args = new ArrayList<>();
	private boolean hasReturn = false;

	UserFunction(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addArgument(Variable arg) {
		args.add(arg);
	}

	@Override
	public int getMandatoryArgsCount() {
		// All arguments are mandatory
		return args.size();
	}

	@Override
	public int getArgsCount() {
		return args.size();
	}

	@Override
	public ReturnType getReturnType() {
		return hasReturn ? ReturnType.REQUIRED : ReturnType.VOID;
	}

	public void setHasReturn(boolean hasReturn) {
		this.hasReturn = hasReturn;
	}

	public Iterator<Variable> argumentsIterator() {
		return args.iterator();
	}

	@Override
	public String statementFor(String indent, List<String> args,
			Variable returnVar) {
		StringBuilder sb = new StringBuilder(indent);
		for (int i = args.size() - 1; i >= 0; i--) {
			sb.append("Push ");
			sb.append(args.get(i));
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
		}
		sb.append("Call ");
		sb.append(getName());
		if (hasReturn) {
			sb.append(InputReader.NEW_LINE);
			sb.append(indent);
			sb.append("Pop ");
			sb.append(returnVar.getNSISExpression());
		}
		return sb.toString();
	}

}