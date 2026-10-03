package com.example.androidcalculator;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class Calculation {

    /* Takes a String as an input and converts to an Array List compatible with the
        infixToPostfix function.
         */
    public ArrayList<String> tokenize(String s){
        ArrayList<String> list = new ArrayList<>();
        StringBuilder temp = new StringBuilder();


        for (char ch: s.toCharArray()){
            if (isOperator(ch)){
                //checking if the operator is minus and if it can be used there (in front of the num)
                if (temp.length() == 0){
                    boolean canUseMinus = list.isEmpty() || isOperator(list.get(list.size() - 1))
                            || list.get(list.size() - 1).equals("(");
                    if (ch == '-' && canUseMinus) temp.append(ch);
                    else if (canUseMinus) return null;
                    else list.add(String.valueOf(ch));
                }
                // adds the number to the array list and then the operator.
                else {
                    list.add(temp.toString());
                    list.add(String.valueOf(ch));
                    temp = new StringBuilder();
                }
            }
            else if (ch == '(' || ch == ')') {
                //adds adds the number to the array list and then the parenthesis.
                if (!(temp.length() == 0)){
                    list.add(String.valueOf(temp));
                    temp = new StringBuilder();
                }
                list.add(String.valueOf(ch));
            }
            // if numeric appends the number string.
            else {
                temp.append(ch);

            }
        }
        if (!(temp.length() == 0)) list.add(temp.toString()); // puts any number left on string into the array.
        return list;
    }


    /* takes an ArayList as a parameter.
        Creates a new ArrayList by converting it from infix to postfix format,
        using a stack.
        Returns the new ArrayList in postfix format
     */
    public ArrayList<String> infixToPostfix(ArrayList<String> s){
        if (s == null) return null;

        ArrayDeque<String> stack = new ArrayDeque<>();
        ArrayList<String> list = new ArrayList<>();

        for (String c : s) {
            if (isOperator(c)) {
                while (!stack.isEmpty() && isOfLowerOrEqualOrder(c, stack.peek())) {
                    list.add(stack.pop());
                }
                stack.push(c);
            } else if (c.equals("(")) {
                stack.push(c);
            } else if (c.equals(")")) {
                while (!stack.isEmpty()) {
                    if (stack.peek().equals("(")) {
                        stack.pop();
                        break;
                    } else list.add(stack.pop());
                }
            } else {
                list.add(c);
            }
        }
        while (!stack.isEmpty()){
            list.add(stack.pop());
        }
        return list;
    }


    /* Parameter: an ArrayList using the postfix format
        Calculates the result of the expression using a stack.
        Returns: The result as a Double (or null if the format is not right)
     */

    public Double calculatePostfix(ArrayList<String> list){
        if (list == null) return null;

        ArrayDeque<Double> numStack = new ArrayDeque<>();
        double exp1;
        double exp2;
        double result; //calculate result

        for (String s : list) {
            if (isOperator(s)) {
                try {
                    exp2 = numStack.pop();
                    exp1 = numStack.pop();

                } catch (Exception e) {
                    return null;
                }
                if (s.equals("+"))
                    result = exp1 + exp2;
                else if (s.equals("-"))
                    result = exp1 - exp2;
                else if (s.equals("*"))
                    result = exp1 * exp2;
                else if (exp2 != 0)
                    result = exp1 / exp2;
                else return null;

                numStack.push(result);
            } else {
                if (isNumeric(s)) {
                    Double num = Double.valueOf(s);
                    numStack.push(num);
                } else return null;
            }
        }
        if (numStack.size() != 1) return null;
        else return numStack.pop();
    }


    public boolean isOfLowerOrEqualOrder(String current, String onStack){
        switch (current){
            case "*":
                return onStack.equals("/") || onStack.equals("*");

            case "/":
                return onStack.equals("*") || onStack.equals("/");

            case "+":

            case "-":
                return isOperator(onStack);

            default: return false;
        }

    }


    public boolean isOperator(String s) {
        return s.equals("+") || s.equals("*") || s.equals("/") || s.equals("-");
    }

    public boolean isOperator(char ch){
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    public boolean isNumeric(String s){
        if (s == null){
            return false;
        }
        try{
            Double.parseDouble(s);
        } catch (NumberFormatException nfe){
            return false;
        }
        return true;
    }


    public String printResult(Double num){
        if (num == null || num.isNaN() || num.isInfinite()) return "Error";
        return new BigDecimal(num).round(new MathContext(12))
                .stripTrailingZeros().toPlainString();
    }
}
