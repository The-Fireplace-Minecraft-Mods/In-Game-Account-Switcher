/*
 * In-Game Account Switcher is a mod for Minecraft that allows you to change your logged in account in-game, without restarting Minecraft.
 * Copyright (C) 2015-2022 The_Fireplace
 * Copyright (C) 2021-2025 VidTu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package ru.vidtu.ias.utils;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Simple math expression parser. Used for configuration.
 * A modified version of the <a href="https://stackoverflow.com/a/26227947">this public domain answer from StackOverflow</a>.
 *
 * @author Boann
 * @author VidTu
 * @see <a href="https://stackoverflow.com/a/26227947">stackoverflow.com/a/26227947</a>
 */
public final class Expression {
    /**
     * Space pattern for removing all spaces.
     */
    @NotNull
    public static final Pattern SPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * Current expression.
     */
    @NotNull
    private final String expression;

    /**
     * Current position.
     */
    private int pos = -1;

    /**
     * Current character.
     */
    private int ch;

    /**
     * Creates a new expression.
     *
     * @param expression Expression string
     */
    @Contract(pure = true)
    public Expression(@NotNull String expression) {
        this.expression = SPACE_PATTERN.matcher(expression).replaceAll("");
    }

    /**
     * Parses the expression.
     *
     * @return Parsed expression
     * @throws IllegalStateException If expression is not valid
     */
    @CheckReturnValue
    public double parse() {
        try {
            // Begin reading.
            this.next();

            // Parse and return.
            double val = this.parseExpression();
            if (this.pos >= this.expression.length()) return val;
            throw new IllegalStateException("Read not fully: " + Character.toString(this.ch));
        } catch (Throwable t) {
            // Rethrow.
            throw new IllegalStateException("Unable to parse: " + this, t);
        }
    }

    // Grammar:
    // expression = term | expression `+` term | expression `-` term
    // term = factor | term `*` factor | term `/` factor
    // factor = `+` factor | `-` factor | `(` expression `)` | number
    //        | functionName `(` expression `)` | functionName factor
    //        | factor `^` factor

    /**
     * Parsed the expression.
     *
     * @return Parsed expression
     */
    @CheckReturnValue
    private double parseExpression() {
        double x = this.parseTerm();
        for (int i = 0; i < 64; i++) {
            if (this.skipIf('+')) x += this.parseTerm(); // addition
            else if (this.skipIf('-')) x -= this.parseTerm(); // subtraction
            else return x;
        }
        throw new RuntimeException("Out of tries.");
    }

    /**
     * Parses the term.
     *
     * @return Parsed term
     */
    @CheckReturnValue
    private double parseTerm() {
        double x = this.parseFactor();
        for (int i = 0; i < 64; i++) {
            if (this.skipIf('*')) x *= this.parseFactor(); // multiplication
            else if (this.skipIf('/')) x /= this.parseFactor(); // division
            else return x;
        }
        throw new RuntimeException("Out of tries.");
    }

    /**
     * Parses the factor.
     *
     * @return Parsed factor
     */
    @CheckReturnValue
    private double parseFactor() {
        if (this.skipIf('+')) return +this.parseFactor(); // unary plus
        if (this.skipIf('-')) return -this.parseFactor(); // unary minus
        double x;
        int startPos = this.pos;
        if (this.skipIf('(')) { // parentheses
            x = this.parseExpression();
            if (!this.skipIf(')')) throw new RuntimeException("Missing ')'");
        } else if ((this.ch >= '0' && this.ch <= '9') || this.ch == '.') { // numbers
            for (int i = 0; i < 64 && ((this.ch >= '0' && this.ch <= '9') || this.ch == '.'); i++) {
                this.next();
            }
            x = Double.parseDouble(this.expression.substring(startPos, this.pos));
        } else if (this.ch >= 'a' && this.ch <= 'z') { // functions
            for (int i = 0; i < 64 && this.ch >= 'a' && this.ch <= 'z'; i++) {
                this.next();
            }
            String func = this.expression.substring(startPos, this.pos);
            if (this.skipIf('(')) {
                x = this.parseExpression();
                if (!this.skipIf(')')) throw new RuntimeException("Missing ')' after argument to " + func);
            } else {
                x = this.parseFactor();
            }
            x = switch (func) {
                case "sqrt" -> Math.sqrt(x);
                case "sin" -> Math.sin(Math.toRadians(x));
                case "cos" -> Math.cos(Math.toRadians(x));
                case "tan" -> Math.tan(Math.toRadians(x));
                default -> throw new RuntimeException("Unknown function: " + func);
            };
        } else {
            throw new RuntimeException("Unexpected: " + Character.toString(this.ch));
        }
        if (this.skipIf('^')) x = Math.pow(x, this.parseFactor()); // exponentiation
        return x;
    }

    /**
     * Skips the whitespaces and character from the string if it's the next character, otherwise does nothing.
     *
     * @param ch Character to skip
     * @return Whether the character was skipped
     */
    @CheckReturnValue
    private boolean skipIf(int ch) {
        // Skip all whitespaces.
        for (int i = 0; i < 1024 && Character.isWhitespace(ch); i++) {
            this.next();
        }

        // Do nothing if not current.
        if (this.ch != ch) return false;

        // Eat otherwise.
        this.next();
        return true;
    }

    /**
     * Sets the {@link #ch} to the next string char. (or to -1 if EOF)
     */
    private void next() {
        this.pos++;
        this.ch = this.pos < this.expression.length() ? this.expression.codePointAt(this.pos) : -1;
    }

    @Contract(pure = true)
    @Override
    @NotNull
    public String toString() {
        return "Expression{" +
                "expression='" + this.expression + '\'' +
                ", pos=" + this.pos +
                ", ch=" + this.ch +
                '}';
    }

    /**
     * Parses the expression.
     *
     * @param expression Expression string
     * @return Parsed expression
     * @throws IllegalStateException If expression is not valid
     */
    @Contract(pure = true)
    public static double parse(@NotNull String expression) {
        Expression expr = new Expression(expression);
        return expr.parse();
    }

    /**
     * Parses (and rounds) the expression replacing "{@code %width%}" with width
     * and "{@code %height%}" with height or returns null if not able to parse.
     *
     * @param expression Target expression
     * @param width      Target width
     * @param height     Target height
     * @return Parsed (rounded) expression or null
     */
    @Contract(pure = true)
    @Nullable
    public static Integer parsePosition(@Nullable String expression, int width, int height) {
        try {
            // Null/empty shortcut.
            if (expression == null || expression.isBlank()) return null;

            // Calculate.
            return (int) parse(expression.toLowerCase(Locale.ROOT)
                    .replace("%width%", Integer.toString(width))
                    .replace("%height%", Integer.toString(height)));
        } catch (Throwable ignored) {
            // Invalid.
            return null;
        }
    }

    /**
     * Gets the position validity color.
     * <ul>
     *     <li>Empty and null expressions will return GRAY.</li>
     *     <li>Valid expressions will return almost-WHITE. (one current vanilla uses with EditBoxes)</li>
     *     <li>Out-of-bounds (for current window) expressions will return YELLOW.</li>
     *     <li>Invalid expressions (infinite, NaN, or too big to be an int) will return ORANGE.</li>
     *     <li>Unparseable expressions will return RED.</li>
     * </ul>
     *
     * @param expression Target expression
     * @param width      Target width
     * @param height     Target height
     * @param x          Whether this is X and not Y
     * @return Position expression validity color
     */
    @Contract(pure = true)
    public static int positionValidityColor(@Nullable String expression, int width, int height, boolean x) {
        try {
            // Null/empty are GRAY.
            if (expression == null || expression.isBlank()) return 0xFF_80_80_80;

            // Calculate.
            double value = parse(expression.toLowerCase(Locale.ROOT)
                    .replace("%width%", Integer.toString(width))
                    .replace("%height%", Integer.toString(height)));

            // Check for invalid. (ORANGE)
            if (!Double.isFinite(value) || value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) return 0xFF_FF_80_00;
            int rounded = (int) value;

            // Check for OOB. (YELLOW)
            if (rounded < 0) return 0xFF_FF_FF_00;
            int bound = x ? width : height;
            if (value > bound) return 0xFF_FF_FF_00;

            // Valid.
            return 0xFF_E0_E0_E0;
        } catch (Throwable ignored) {
            // Invalid.
            return 0xFF_FF_00_00;
        }
    }
}
