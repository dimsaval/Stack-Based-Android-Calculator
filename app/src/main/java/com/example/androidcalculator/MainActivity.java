package com.example.androidcalculator;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{


    Button num1; Button num2; Button num3; Button num4; Button num5; Button num6; Button num7; Button num8; Button num9; Button num0;

    Button plus; Button minus; Button multiply; Button divide; Button percent;

    Button equal; Button comma; Button ac; Button back; Button parenthesis;

    TextView result;

    Calculation calculator = new Calculation();


    StringBuilder expression = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DynamicColors.applyToActivityIfAvailable(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        num1 = findViewById(R.id.numone);
        num2 = findViewById(R.id.numtwo);
        num3 = findViewById(R.id.numthree);
        num4 = findViewById(R.id.numfour);
        num5 = findViewById(R.id.numfive);
        num6 = findViewById(R.id.numsix);
        num7 = findViewById(R.id.numseven);
        num8 = findViewById(R.id.numeight);
        num9 = findViewById(R.id.numnine);
        num0 = findViewById(R.id.numzero);

        plus = findViewById(R.id.plus);
        minus = findViewById(R.id.minus);
        multiply = findViewById(R.id.multi);
        divide = findViewById(R.id.divide);
        percent = findViewById(R.id.percent);


        comma = findViewById(R.id.coma);
        equal = findViewById(R.id.equals);
        ac = findViewById(R.id.AC);
        back = findViewById(R.id.backSpace);

        parenthesis = findViewById(R.id.parenthesis);
        result = findViewById(R.id.result);


        num1.setOnClickListener(this);
        num2.setOnClickListener(this);
        num3.setOnClickListener(this);
        num4.setOnClickListener(this);
        num5.setOnClickListener(this);
        num6.setOnClickListener(this);
        num7.setOnClickListener(this);
        num8.setOnClickListener(this);
        num9.setOnClickListener(this);
        num0.setOnClickListener(this);
        plus.setOnClickListener(this);
        minus.setOnClickListener(this);
        multiply.setOnClickListener(this);
        divide.setOnClickListener(this);
        comma.setOnClickListener(this);


        equal.setOnClickListener(v -> {

            //checking if expression is empty.
            if (expression.length() == 0) return;

            // checking if there are any open parenthesis.
            int balance = 0;
            String s = expression.toString();
            for (Character ch: s.toCharArray()){
                if (ch == '(') balance++;
                else if (ch == ')') balance--;
            }
            if (balance != 0) {
                Toast.makeText(MainActivity.this, "Close parenthesis, please!", Toast.LENGTH_SHORT).show();
                return;
            }

            //Calculation pipeline
            ArrayList<String> list = calculator.tokenize(s);
            ArrayList<String> postfix = calculator.infixToPostfix(list);
            Double numResult = calculator.calculatePostfix(postfix);

            //Showing Result
            result.setText(calculator.printResult(numResult));
            if (numResult != null)
                expression = new StringBuilder(String.valueOf(calculator.printResult(numResult)));
            else expression = new StringBuilder();
        });

        ac.setOnClickListener(v -> {
            expression.delete(0, expression.length());
            result.setText(expression); //update screen
        });

        back.setOnClickListener(v -> {
            if (expression.length() == 0) return;
            expression.delete(expression.length() -1, expression.length());
            result.setText(expression); //update screen
        });

        parenthesis.setOnClickListener(v -> {
            String s = expression.toString();
            int balance = 0;

            for (Character ch: s.toCharArray()){
                if (ch == '(') balance++;
                else if (ch == ')') balance--;
            }

            if (expression.length() != 0) {

                String lastDigit = s.substring(s.length() - 1);
                if (lastDigit.equals(".")) return;

                if (balance != 0 && (calculator.isNumeric(lastDigit) || lastDigit.equals(")"))) {
                    expression.append(")");
                }
                else expression.append("(");
            }
            else expression.append("(");

            result.setText(expression.toString());
        });


        percent.setOnClickListener(v -> {
            if (expression.length() == 0) return;

            // Scan backward from the end to find the start of the number currently being typed.
            int i = expression.length() - 1;
            char currentChar = expression.charAt(i);
            while (i >= 0 && !(calculator.isOperator(currentChar) || currentChar == '(' || currentChar == ')')) {
                i -= 1;
                currentChar = (i >= 0) ? expression.charAt(i) : '\0';   // guard against i == -1
            }

            String secondStr = expression.substring(i + 1);
            if (!calculator.isNumeric(secondStr)) return;
            double secondNum = Double.parseDouble(secondStr);

            boolean isCompound = i >= 0 && (currentChar == '+' || currentChar == '-');



            if (!isCompound) {
                double res = secondNum /= 100.0;
                expression = new StringBuilder(expression.substring(0, i + 1));
                expression.append(calculator.printResult(res));
            }
            else {
                int j = i - 1;

                while (j >= 0 && (Character.isDigit(expression.charAt(j)) || expression.charAt(j) == '.')) {
                    j--;
                }

                //checking if minus is Unary (in front of a negative)
                boolean firstIsUnaryMinus = j >= 0 && expression.charAt(j) == '-'
                        && (j == 0 || calculator.isOperator(expression.charAt(j - 1))
                        || expression.charAt(j - 1) == '(');
                if (firstIsUnaryMinus) j--;

                String firstStr = expression.substring(j + 1, i);

                // if left side of expression isn't number, just use the right side.
                if (!calculator.isNumeric(firstStr)) {
                    double result = secondNum / 100.0;
                    expression = new StringBuilder(expression.substring(0, i + 1));
                    expression.append(calculator.printResult(result));
                } else {
                    double firstNum = Double.parseDouble(firstStr);
                    double scaledRight = firstNum * (secondNum / 100.0);
                    expression = new StringBuilder(expression.substring(0, i + 1));
                    expression.append(calculator.printResult(scaledRight));
                }

            }
            result.setText(expression.toString()); // update screen
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }




    public void onClick(View v){
        Button button = (Button) v;
        String text = button.getText().toString(); // get text from button.

        /* checking if previous input was an operator, in order to change the operator if two op.
         are typed together, the latter stays on. */
        if (expression.length() != 0){
            char lastCh = expression.charAt(expression.length() - 1);
            if (calculator.isOperator(lastCh) && calculator.isOperator(text)){
                //if typing minus after multi. or divide then is used to show a negative number.
                if ((lastCh == '*' || lastCh == '/') && text.equals("-")) {
                    expression.append("(");
                } else // change the operator.
                    expression.delete(expression.length() -1, expression.length());
            }
            result.setText(expression); //update screen
        }
        // if expression is empty, only '-' is allowed to be typed.
        else if (calculator.isOperator(text) && !text.equals("-")) return;

        // two dots are not allowed to be typed in the same number.
        if (text.equals(".") && hasDot(expression.toString())) return;

        expression.append(text);
        result.setText(expression.toString()); // update screen.
    }







    /* Function that takes an Expression as String and checks if the last number on the expression
        has a decimal point.
        Returns True if it has and False if it doesn't.
     */
    public boolean hasDot(String s){
        boolean temp = false;
        for (char ch: s.toCharArray()){
            if (ch == '.') temp = true;
            if (calculator.isOperator(ch)) temp = false;
        }
        return temp;
    }







}