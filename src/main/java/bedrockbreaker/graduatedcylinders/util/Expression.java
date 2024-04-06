package bedrockbreaker.graduatedcylinders.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class Expression {
	
	public static double evaluate(String expression) {
		return Expression.evaluate(expression, new Context());
	}

	public static double evaluate(String expression, Context context) {
		if (expression.length() == 0) return Double.NaN;
		expression = expression.toLowerCase().replaceAll("[ _]", "");

		ParsePosition parsePosition = new ParsePosition(0);
		TokenStack tokens = new TokenStack();

		try {
			for (int i = 0; i < expression.length(); i++) {
				char currentChar = expression.charAt(i);
				if (Operator.isOperator(currentChar)) {
					Token prevToken = tokens.peek();
					Operator operator = Operator.getOperator(currentChar);
					if (((operator == Operator.ADD || operator == Operator.SUBTRACT) && (prevToken == null || prevToken.isOperator())) || operator.isPrefix()) {
						tokens.push(Double.NaN); // Push dummy number to keep stack alternating between numbers and operators
						tokens.push(operator == Operator.ADD ? Operator.IDENTITY : (operator == Operator.SUBTRACT ? Operator.NEGATE : operator));
					} else if (operator.isPostfix()) {
						Expression.partialEvaluate(tokens, operator.priority, context);
						tokens.push(operator);
						tokens.push(Double.NaN);
					} else if (operator == Operator.GROUP) {
						// This catches both "a(" and ")(", since closing parentheses are followed by NaN, which isn't an operator
						if (prevToken != null && !prevToken.isOperator()) Expression.pushBinaryOperator(tokens, Operator.MULTIPLY, context);
						tokens.push(Double.NaN);
						tokens.push(operator);
					} else if (operator == Operator.UNGROUP) {
						tokens.push(operator);
						tokens.push(Double.NaN);
					} else {
						Expression.pushBinaryOperator(tokens, operator, context);
					}
				} else {
					parsePosition.setIndex(i);
					double value = context.numberFormat.parse(expression, parsePosition).doubleValue();
					if (parsePosition.getIndex() == i) return Double.NaN;
					if (tokens.peek(1) != null && tokens.peek(1).operator == Operator.UNGROUP) Expression.pushBinaryOperator(tokens, Operator.MULTIPLY, context);
					tokens.push(value);
					i = parsePosition.getIndex() - 1;
				}
			}
	
			Expression.partialEvaluate(tokens, 0, context);
		} catch (Exception ignored) {
			return Double.NaN;
		}
		
		return tokens.size() > 1 ? Double.NaN : tokens.pop().value;
	}

	private static void pushBinaryOperator(TokenStack tokens, Operator operator, Context context) {
		Expression.partialEvaluate(tokens, !operator.isLeftAssociative() ? operator.priority + 1 : operator.priority, context);
		tokens.push(operator);
	}

	private static void partialEvaluate(TokenStack tokens, int minPriority, Context context) {
		final int initialMinPriority = minPriority;
		int groupLevel = 0;
		while (tokens.size() >= 3) {
			Operator operator = tokens.peek(1).operator;
			if (operator.priority < minPriority) break;

			if (operator == Operator.UNGROUP) {
				tokens.pop();
				tokens.pop();
				minPriority = Operator.GROUP.priority;
				groupLevel++;
				continue;
			} else if (operator == Operator.GROUP) {
				tokens.remove(1);
				tokens.remove(1);
				groupLevel--;
				if (groupLevel == 0) minPriority = initialMinPriority;
				continue;
			}

			double right = tokens.pop().value;
			tokens.pop(); // Remove operator
			double left = tokens.pop().value;
			if (operator == Operator.PERCENT) tokens.push(left * context.percent * .01d);
			else if (operator.isUnary()) tokens.push(operator.execute(operator.isLeftAssociative() ? left : right));
			else tokens.push(operator.execute(left, right));
		}
		if (groupLevel != 0) throw new IllegalStateException("Encountered unbalanced parenthesis");
	}

	public static class Context {

		public double percent = 1;
		public NumberFormat numberFormat = DecimalFormat.getNumberInstance(Locale.getDefault());

		public Context setPercent(double percent) {
			this.percent = percent;
			return this;
		}

		public Context setNumberFormat(NumberFormat numberFormat) {
			this.numberFormat = numberFormat;
			return this;
		}
	}

	private static class TokenStack {

		public List<Token> tokens = new ArrayList<Token>();

		public int size() {
			return this.tokens.size();
		}

		public void push(Operator operator) {
			this.tokens.add(new Token(operator));
		}
		
		public void push(double value) {
			this.tokens.add(new Token(value));
		}
		
		public Token peek() {
			return this.peek(0);
		}
		
		public Token peek(int depth) {
			if (this.tokens.size() <= depth) return null;
			return this.tokens.get(this.tokens.size() - depth - 1);
		}
		
		public Token pop() {
			return this.remove(0);
		}
		
		public Token remove(int depth) {
			return this.tokens.remove(this.tokens.size() - depth - 1);
		}

		public String toString() {
			return Arrays.toString(this.tokens.toArray());
		}
	}

	private static class Token {

		public Operator operator;
		public double value = Double.NaN;

		public Token(Operator operator) {
			this.operator = operator;
		}

		public Token(double value) {
			this.value = value;
		}

		public boolean isOperator() {
			return operator != null;
		}

		public String toString() {
			return this.isOperator() ? Character.toString(this.operator.getChar()) : Double.toString(this.value);
		}
	}
}
