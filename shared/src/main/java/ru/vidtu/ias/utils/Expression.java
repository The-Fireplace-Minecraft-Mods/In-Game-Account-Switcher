package ru.vidtu.ias.utils;

import org.jetbrains.annotations.Contract;

import java.util.Locale;

/**
 * Simple math expression parser. Used for configuration.
 * <small>Actually made from 5-7 math parsing methods found on the internet.</small>
 *
 * @author VidTu
 */
public class Expression {
    private final String expression;
    private int position;

    /**
     * Create new expression.
     *
     * @param expression Expression string
     */
    public Expression(String expression) {
        this.expression = expression.replaceAll("\\s+",""); //Remove all white spaces
    }

    /**
     * Get the current expression char.
     *
     * @return Current expression char
     */
    @Contract(pure = true)
    private char current() {
        return position >= expression.length() ? '?' : expression.charAt(position);
    }

    /**
     * Parse expression.
     *
     * @return Parsed expression
     * @throws IllegalArgumentException If we can't calculate this
     */
    public double parse() {
        return parse(0);
    }

    /**
     * Parse expression.
     *
     * @param returnFlag Flag for return value. 0 = return at end, 1 = return at multiply, 2 = return at factor
     * @return Parsed expression
     * @throws IllegalArgumentException If we can't calculate this
     * @implNote This is the worst code I've seen in my life
     */
    private double parse(int returnFlag) {
        try {
            if (returnFlag == 2) {
                if (current() == '-') {
                    position++;
                    return -parse(2);
                }
                double x;
                if (current() == '(') {
                    position++;
                    x = parse(0);
                    if (current() == ')') position++;
                    if (current() == '^') {
                        position++;
                        x = Math.pow(x, parse(2));
                    }
                    return x;
                }
                int begin = position;
                while (current() == '.' || Character.isDigit(current())) position++;
                x = Double.parseDouble(expression.substring(begin, position));
                if (current() == '^') {
                    position++;
                    x = Math.pow(x, parse(2));
                }
                return x;
            }
            double x = parse(2);
            while (true) {
                if (current() == '*') {
                    position++;
                    x *= parse(2);
                    continue;
                }
                if (current() == '/') {
                    position++;
                    x /= parse(2);
                    continue;
                }
                if (returnFlag == 1) return x;
                break;
            }
            while (true) {
                if (current() == '+') {
                    position++;
                    x += parse(1);
                    continue;
                }
                if (current() == '-') {
                    position++;
                    x -= parse(1);
                    continue;
                }
                return x;
            }
        } catch (Throwable t) {
            throw new IllegalArgumentException("Sorry, but we're unable to calculate " + expression + " at pos " + position, t);
        }
    }

    /**
     * Parse expression, replacing <code>w</code> with width, <code>h</code> with height.
     *
     * @param expression Expression string
     * @param width      Screen width
     * @param height     Screen height
     * @return Expression result
     * @throws IllegalArgumentException If we can't calculate this
     */
    @Contract(pure = true)
    public static double parseWidthHeight(String expression, int width, int height) throws IllegalArgumentException {
        return new Expression(expression.toLowerCase(Locale.ROOT).replace("w", String.valueOf(width)).replace("h", String.valueOf(height))).parse();
    }
}