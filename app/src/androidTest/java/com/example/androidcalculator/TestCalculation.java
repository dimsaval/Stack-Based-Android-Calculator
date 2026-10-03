package com.example.androidcalculator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
  Full coverage test suite for Calculation.java.
  Grouped by method under test. Each group starts with happy-path cases,
  then edge cases, then malformed/invalid input.
 */
public class TestCalculation {

    Calculation calc;

    @Before
    public void Before() {
        calc = new Calculation();
    }

    private Double evaluate(String expr) {
        return calc.calculatePostfix(calc.infixToPostfix(calc.tokenize(expr)));
    }

    // tokenize(String)
    // -------------------------------------------------------------------------

    @Test
    public void tokenize_Addition() {
        ArrayList<String> tokens = calc.tokenize("2+3");
        Assert.assertEquals(Arrays.asList("2", "+", "3"), tokens);
    }

    @Test
    public void tokenize_multiDigitNumbers() {
        ArrayList<String> tokens = calc.tokenize("123+456");
        Assert.assertEquals(Arrays.asList("123", "+", "456"), tokens);
    }

    @Test
    public void tokenize_decimalNumber() {
        ArrayList<String> tokens = calc.tokenize("1.5*2");
        Assert.assertEquals(Arrays.asList("1.5", "*", "2"), tokens);
    }

    @Test
    public void tokenize_withParentheses() {
        ArrayList<String> tokens = calc.tokenize("(2+3)*4");
        Assert.assertEquals(Arrays.asList("(", "2", "+", "3", ")", "*", "4"), tokens);
    }

    @Test
    public void tokenize_leadingUnaryMinus() {
        // "-" at the very start of the expression is part of the number.
        ArrayList<String> tokens = calc.tokenize("-5+3");
        Assert.assertEquals(Arrays.asList("-5", "+", "3"), tokens);
    }

    @Test
    public void tokenize_unaryMinusAfterOperator() {
        // "-" right after another operator is unary, not binary.
        ArrayList<String> tokens = calc.tokenize("5*-2");
        Assert.assertEquals(Arrays.asList("5", "*", "-2"), tokens);
    }

    @Test
    public void tokenize_unaryMinusAfterOpenParen() {
        ArrayList<String> tokens = calc.tokenize("(-5+3)");
        Assert.assertEquals(Arrays.asList("(", "-5", "+", "3", ")"), tokens);
    }

    @Test
    public void tokenize_binaryMinusAfterCloseParen() {
        // "-" right after ")" must be binary, not swallowed into a unary number.
        ArrayList<String> tokens = calc.tokenize("(2+3)-4");
        Assert.assertEquals(Arrays.asList("(", "2", "+", "3", ")", "-", "4"), tokens);
    }

    @Test
    public void tokenize_consecutiveParens() {
        ArrayList<String> tokens = calc.tokenize("((2))");
        Assert.assertEquals(Arrays.asList("(", "(", "2", ")", ")"), tokens);
    }

    @Test
    public void tokenize_leadingNonMinusOperatorReturnsNull() {
        // "*" (or "+" "/") can never legally start an expression.
        Assert.assertNull(calc.tokenize("*5"));
        Assert.assertNull(calc.tokenize("+5"));
        Assert.assertNull(calc.tokenize("/5"));
    }

    @Test
    public void tokenize_emptyStringReturnsEmptyList() {
        ArrayList<String> tokens = calc.tokenize("");
        Assert.assertTrue(tokens.isEmpty());
    }

    @Test
    public void tokenize_malformedNumberIsKeptAsSingleToken() {
        // tokenize() doesn't validate number shape; it just groups digits/dots.
        // "10.." becomes one bad token, which is rejected later in calculatePostfix.
        ArrayList<String> tokens = calc.tokenize("10..");
        Assert.assertEquals(Arrays.asList("10.."), tokens);
    }


    // infixToPostfix(ArrayList<String>)
    // ---------------------------------------------------------------

    @Test
    public void infixToPostfix_simpleAddition() {
        ArrayList<String> infix = new ArrayList<>(Arrays.asList("2", "+", "3"));
        ArrayList<String> postfix = calc.infixToPostfix(infix);
        Assert.assertEquals(Arrays.asList("2", "3", "+"), postfix);
    }

    @Test
    public void infixToPostfix_respectsPrecedence() {
        // 3 + 4 * 2 -> 3 4 2 * +
        ArrayList<String> infix = new ArrayList<>(Arrays.asList("3", "+", "4", "*", "2"));
        ArrayList<String> postfix = calc.infixToPostfix(infix);
        Assert.assertEquals(Arrays.asList("3", "4", "2", "*", "+"), postfix);
    }

    @Test
    public void infixToPostfix_leftAssociativeSamePrecedence() {
        // 8 / 2 / 2 -> 8 2 / 2 / (left-to-right, not 8 2 2 / /)
        ArrayList<String> infix = new ArrayList<>(Arrays.asList("8", "/", "2", "/", "2"));
        ArrayList<String> postfix = calc.infixToPostfix(infix);
        Assert.assertEquals(Arrays.asList("8", "2", "/", "2", "/"), postfix);
    }

    @Test
    public void infixToPostfix_parenthesesOverridePrecedence() {
        // (2+3)*4 -> 2 3 + 4 *
        ArrayList<String> infix = new ArrayList<>(
                Arrays.asList("(", "2", "+", "3", ")", "*", "4"));
        ArrayList<String> postfix = calc.infixToPostfix(infix);
        Assert.assertEquals(Arrays.asList("2", "3", "+", "4", "*"), postfix);
    }

    @Test
    public void infixToPostfix_nullInputReturnsNull() {
        Assert.assertNull(calc.infixToPostfix(null));
    }

    @Test
    public void infixToPostfix_emptyInputReturnsEmptyList() {
        Assert.assertTrue(calc.infixToPostfix(new ArrayList<>()).isEmpty());
    }

    // ---------------------------------------------------------------
    // isOfLowerOrEqualOrder(String current, String onStack)
    // ---------------------------------------------------------------

    @Test
    public void precedence_samePrecedencePopsStackOperator() {
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("*", "*"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("*", "/"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("/", "*"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("/", "/"));
    }

    @Test
    public void precedence_additiveNeverOutranksAnyOperator() {
        // "+" and "-" should pop *any* operator ahead of them (never higher precedence on the stack).
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("+", "+"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("+", "-"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("+", "*"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("+", "/"));
        Assert.assertTrue(calc.isOfLowerOrEqualOrder("-", "*"));
    }

    @Test
    public void precedence_multiplicativeDoesNotPopAdditive() {
        // "*" or "/" should NOT pop a lower-precedence "+"/"-" sitting on the stack.
        Assert.assertFalse(calc.isOfLowerOrEqualOrder("*", "+"));
        Assert.assertFalse(calc.isOfLowerOrEqualOrder("*", "-"));
        Assert.assertFalse(calc.isOfLowerOrEqualOrder("/", "+"));
        Assert.assertFalse(calc.isOfLowerOrEqualOrder("/", "-"));
    }

    @Test
    public void precedence_unrecognizedCurrentOperatorReturnsFalse() {
        // Only reachable with a malformed "current" argument, but the default branch should hold.
        Assert.assertFalse(calc.isOfLowerOrEqualOrder("(", "+"));
    }

    // ---------------------------------------------------------------
    // calculatePostfix(ArrayList<String>)
    // ---------------------------------------------------------------

    @Test
    public void calculatePostfix_addition() {
        Assert.assertEquals(5.0, evaluate("2+3"), 1e-9);
    }

    @Test
    public void calculatePostfix_subtraction() {
        Assert.assertEquals(-1.0, evaluate("2-3"), 1e-9);
    }

    @Test
    public void calculatePostfix_multiplication() {
        Assert.assertEquals(6.0, evaluate("2*3"), 1e-9);
    }

    @Test
    public void calculatePostfix_division() {
        Assert.assertEquals(2.5, evaluate("5/2"), 1e-9);
    }

    @Test
    public void calculatePostfix_divisionByZeroReturnsNull() {
        Assert.assertNull(evaluate("10/0"));
    }

    @Test
    public void calculatePostfix_precedenceAndParentheses() {
        Assert.assertEquals(20.0, evaluate("(2+3)*4"), 1e-9);
        Assert.assertEquals(11.0, evaluate("3+4*2"), 1e-9);
    }

    @Test
    public void calculatePostfix_unaryMinusAcrossOperators() {
        Assert.assertEquals(-2.0, evaluate("-5+3"), 1e-9);
        Assert.assertEquals(-10.0, evaluate("5*-2"), 1e-9);
        Assert.assertEquals(1.0, evaluate("(2+3)-4"), 1e-9);
    }

    @Test
    public void calculatePostfix_tooFewOperandsReturnsNull() {
        // "+3" with nothing before it: infixToPostfix doesn't catch this, calculatePostfix must.
        ArrayList<String> postfix = new ArrayList<>(Arrays.asList("3", "+"));
        Assert.assertNull(calc.calculatePostfix(postfix));
    }

    @Test
    public void calculatePostfix_tooManyOperandsReturnsNull() {
        // Two numbers with no operator between them: stack ends with size 2, not 1.
        ArrayList<String> postfix = new ArrayList<>(Arrays.asList("2", "3"));
        Assert.assertNull(calc.calculatePostfix(postfix));
    }

    @Test
    public void calculatePostfix_nonNumericTokenReturnsNull() {
        Assert.assertNull(evaluate("10.."));
    }

    @Test
    public void calculatePostfix_leadingInvalidOperatorReturnsNull() {
        Assert.assertNull(evaluate("*5"));
    }

    @Test
    public void calculatePostfix_nullInputReturnsNull() {
        Assert.assertNull(calc.calculatePostfix(null));
    }

    @Test
    public void calculatePostfix_emptyInputReturnsNull() {
        // Empty list leaves the stack at size 0, which fails the size == 1 check.
        Assert.assertNull(calc.calculatePostfix(new ArrayList<>()));
    }


    // isOperator(String) / isOperator(char)
    // ---------------------------------------------------------------

    @Test
    public void isOperator_string_recognizesAllFourOperators() {
        Assert.assertTrue(calc.isOperator("+"));
        Assert.assertTrue(calc.isOperator("-"));
        Assert.assertTrue(calc.isOperator("*"));
        Assert.assertTrue(calc.isOperator("/"));
    }

    @Test
    public void isOperator_string_rejectsNonOperators() {
        Assert.assertFalse(calc.isOperator("5"));
        Assert.assertFalse(calc.isOperator("("));
        Assert.assertFalse(calc.isOperator(")"));
        Assert.assertFalse(calc.isOperator("."));
        Assert.assertFalse(calc.isOperator(""));
        Assert.assertFalse(calc.isOperator("++"));
    }

    @Test
    public void isOperator_char_recognizesAllFourOperators() {
        Assert.assertTrue(calc.isOperator('+'));
        Assert.assertTrue(calc.isOperator('-'));
        Assert.assertTrue(calc.isOperator('*'));
        Assert.assertTrue(calc.isOperator('/'));
    }

    @Test
    public void isOperator_char_rejectsNonOperators() {
        Assert.assertFalse(calc.isOperator('5'));
        Assert.assertFalse(calc.isOperator('('));
        Assert.assertFalse(calc.isOperator(')'));
        Assert.assertFalse(calc.isOperator('.'));
    }


    // isNumeric(String)
    // ---------------------------------------------------------------

    @Test
    public void isNumeric_acceptsValidNumbers() {
        Assert.assertTrue(calc.isNumeric("5"));
        Assert.assertTrue(calc.isNumeric("5.5"));
        Assert.assertTrue(calc.isNumeric("-5"));
        Assert.assertTrue(calc.isNumeric("-5.5"));
        Assert.assertTrue(calc.isNumeric("0"));
    }

    @Test
    public void isNumeric_rejectsNull() {
        Assert.assertFalse(calc.isNumeric(null));
    }

    @Test
    public void isNumeric_rejectsEmptyString() {
        Assert.assertFalse(calc.isNumeric(""));
    }

    @Test
    public void isNumeric_rejectsMalformedNumbers() {
        Assert.assertFalse(calc.isNumeric("1.2.3"));
        Assert.assertFalse(calc.isNumeric("abc"));
        Assert.assertFalse(calc.isNumeric("5+"));
        Assert.assertFalse(calc.isNumeric("."));
    }


    // printResult(Double)
    // ---------------------------------------------------------------

    @Test
    public void printResult_nullReturnsError() {
        Assert.assertEquals("Error", calc.printResult(null));
    }

    @Test
    public void printResult_wholeNumberHasNoDecimalPoint() {
        Assert.assertEquals("5", calc.printResult(5.0));
        Assert.assertEquals("0", calc.printResult(0.0));
    }

    @Test
    public void printResult_negativeWholeNumber() {
        Assert.assertEquals("-5", calc.printResult(-5.0));
    }

    @Test
    public void printResult_decimalResultKeepsDecimalPoint() {
        Assert.assertEquals("2.5", calc.printResult(2.5));
    }

    @Test
    public void printResult_floatingPointNoise() {
        // Documents current behavior rather than ideal behavior: 0.1 + 0.2 does NOT
        // print as a clean "0.3" today. If printResult is switched to a BigDecimal/
        // MathContext rounding approach, this test's expected value should change to "0.3".
        Assert.assertEquals("0.3", calc.printResult(0.1 + 0.2));
    }

    @Test
    public void printResult_scientificNotationForTinyNumbers() {
        // Same caveat as above: this is today's actual behavior, not the desired one.
        // A value this small round-trips to Java's scientific notation, which tokenize()
        // cannot parse back in if the user keeps typing after this result is shown.
        Assert.assertEquals("0.00001", calc.printResult(0.00001));
    }



    // End-to-end integration checks (tokenize -> infixToPostfix -> calculatePostfix)
    // ---------------------------------------------------------------

    @Test
    public void integration_orderOfOperations() {
        Assert.assertEquals(14.0, evaluate("2+3*4"), 1e-9);
    }

    @Test
    public void integration_parenthesesChangeOrderOfOperations() {
        Assert.assertEquals(20.0, evaluate("(2+3)*4"), 1e-9);
    }

    @Test
    public void integration_nestedParentheses() {
        Assert.assertEquals(9.0, evaluate("((2+1)*3)"), 1e-9);
    }

    @Test
    public void integration_chainedSubtractionIsLeftAssociative() {
        Assert.assertEquals(-5.0, evaluate("2-3-4"), 1e-9);
    }

    @Test
    public void integration_malformedExpressionsReturnNull() {
        Assert.assertNull(evaluate("5+"));
        Assert.assertNull(evaluate("*5"));
        Assert.assertNull(evaluate("10.."));
        Assert.assertNull(evaluate("10/0"));
        Assert.assertNull(evaluate(""));
    }
}

