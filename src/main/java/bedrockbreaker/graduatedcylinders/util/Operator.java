package bedrockbreaker.graduatedcylinders.util;

import java.util.HashMap;

public enum Operator {

	GROUP('(', 0),
	UNGROUP(')', 7),

	PERCENT('%', 6, (a) -> a, true),
	TRILLION('t', 6, (a) -> a * 1_000_000_000_000d, true),
	GIGA('g', 6, (a) -> a * 1_000_000_000d, true),
	BILLION('b', 6, (a) -> a * 1_000_000_000d, true),
	MILLION('m', 6, (a) -> a * 1_000_000d, true),
	THOUSAND('k', 6, (a) -> a * 1000d, true),

	// Yeah, 1.1e1.2e.3 is a valid expression (= 1.1e(1.2e.3) approx. 272.714). Deal with it.
	// Generally not usable though, since 'e' is the most common keybind for open/closing inventory. Could still be copy/pasted though.
	MAGNITUDE('e', 5, (a, b) -> a * Math.pow(10, b), false),
	POWER('^', 4, (a, b) -> Math.pow(a, b), false),
	NEGATE('~', 3, (a) -> -a, false),
	IDENTITY('#', 3, (a) -> a, false),
	MULTIPLY('*', 2, (a, b) -> a * b, true),
	DIVIDE('/', 2, (a, b) -> a / b, true),
	SUBTRACT('-', 1, (a, b) -> a - b, true),
	ADD('+', 1, (a, b) -> a + b, true);

	private final static HashMap<Character, Operator> OPERATORS = new HashMap<Character, Operator>();
	
	public final int priority;
	private final char operator;
	private final BinaryOperator binary;
	private final UnaryOperator unary;
	private final boolean isLeftAssociative;

	static {
		for (Operator operator : Operator.values()) {
			Operator.OPERATORS.put(operator.getChar(), operator);
		}
	}

	public static boolean isOperator(char c) {
		return OPERATORS.containsKey(c);
	}

	public static Operator getOperator(char c) {
		return OPERATORS.get(c);
	}

	private Operator(char operator, int priority, BinaryOperator executor, boolean isLeftAssociative) {
		this.operator = operator;
		this.priority = priority;
		this.binary = executor;
		this.unary = null;
		this.isLeftAssociative = isLeftAssociative;
	}

	private Operator(char operator, int priority, UnaryOperator modifier, boolean isLeftAssociative) {
		this.operator = operator;
		this.priority = priority;
		this.binary = null;
		this.unary = modifier;
		this.isLeftAssociative = isLeftAssociative;
	}

	private Operator(char operator, int priority) {
		this.operator = operator;
		this.priority = priority;
		this.binary = null;
		this.unary = null;
		this.isLeftAssociative = false;
	}

	public char getChar() {
		return this.operator;
	}

	public double execute(double left, double right) {
		return this.binary.execute(left, right);
	}

	public double execute(double value) {
		return this.unary.execute(value);
	}

	public boolean isUnary() {
		return this.unary != null;
	}

	public boolean isLeftAssociative() {
		return this.isLeftAssociative;
	}

	public boolean isPrefix() {
		return this.isUnary() && !this.isLeftAssociative();
	}

	public boolean isPostfix() {
		return this.isUnary() && this.isLeftAssociative();
	}

	@FunctionalInterface
	private interface BinaryOperator {
		public double execute(double a, double b);
	}

	@FunctionalInterface
	private interface UnaryOperator {
		public double execute(double a);
	}
}
