package ru.vidtu.ias.utils;

/**
 * Simple math expression parser.<br>
 * Used for configuration.<br>
 * <small>Actually made from 5-7 math parsing methods found on the web.</small><br>
 * @author VidTu
 */
public class Expression {
	public String expr; //Expression
	public int i; //Position
	
	/**
	 * Creates new expression.
	 * @param s Expression string
	 */
	public Expression(String s) {
		this.expr = s.replaceAll("\\s+",""); //Remove all white spaces
	}
	
	/**
	 * Get current expression char.
	 * @return Current expression char ^_^
	 */
	public char current() {
		return i >= expr.length()?'?':expr.charAt(i);
	}

	/**
	 * Parse expression.<br>
	 * In normal circumstances you want to use <code>0</code> as <code>returnFlag</code> as:<br>
	 * <code>parse(0);</code>
	 * @param returnFlag Flag for return value. 0 = return at end, 1 = return at multiply, 2 = return at factor
	 * @return Parsed expression
	 * @throws IllegalArgumentException If our calculator is too stupid to calculate your expression
	 * @apiNote This is the worst code I've seen in my life
	 */
	public double parse(int returnFlag) {
		try {
			if (returnFlag == 2) {
				if (current() == '-') {
					i++;
					return -parse(2);
				}
				double x;
				if (current() == '(') {
					i++;
					x = parse(0);
					if (current() == ')') i++;
					if (current() == '^') {
						i++;
						x = Math.pow(x, parse(2));
					}
					return x;
				}
				int begin = i;
				while (current() == '.' || Character.isDigit(current())) i++;
				x = Double.parseDouble(expr.substring(begin, i));
				if (current() == '^') {
					i++;
					x = Math.pow(x, parse(2));
				}
				return x;
			}
			double x = parse(2);
			while (true) {
				if (current() == '*') {
					i++;
					x *= parse(2);
					continue;
				}
				if (current() == '/') {
					i++;
					x /= parse(2);
					continue;
				}
				if (returnFlag == 1) return x;
				break;
			}
			while (true) {
				if (current() == '+') {
					i++;
					x += parse(1);
					continue;
				}
				if (current() == '-') {
					i++;
					x -= parse(1);
					continue;
				}
				return x;
			}
		} catch (Throwable t) {
			throw new IllegalArgumentException("Sowwy, but we're unable to calculate something: '" + expr + "' at pos " + i, t);
		}
	}
}
