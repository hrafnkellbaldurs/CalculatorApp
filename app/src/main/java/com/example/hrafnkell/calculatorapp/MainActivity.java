package com.example.hrafnkell.calculatorapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private TextView m_display;
    private Vibrator m_vibrator;
    private String m_color;
    private Boolean m_use_vibrator = false;
    private List operands;
    private List numbers;
    SharedPreferences m_sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        operands = new ArrayList();
        numbers = new ArrayList();

        m_display = (TextView)findViewById(R.id.display);
        m_vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        m_sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Getting the values from the preferences
        m_use_vibrator = m_sp.getBoolean("vibrate", false);
        m_color = m_sp.getString("colorType", "black");


        // Getting the custom font from the assets folder
        Typeface digitalFont = Typeface.createFromAsset(getAssets(), "Fonts/digital-7.ttf");
        m_display.setTypeface(digitalFont);

        // Setting the color of the font
        m_display.setTextColor(Color.parseColor(m_color));

    }

    @Override
    protected void onStart(){
        super.onStart();
        m_use_vibrator = m_sp.getBoolean("vibrate",false);
        m_color = m_sp.getString("colorType", "black");

        m_display.setTextColor(Color.parseColor(m_color));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,CalcPreferenceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void buttonPressed(View view){

        //If the user wants the device to vibrate when pressing a button,
        //the device vibrates
        if(m_use_vibrator){
            m_vibrator.vibrate(50);
            Toast.makeText(getApplicationContext(),"Vibrate...",Toast.LENGTH_SHORT).show();
        }

        Button buttonView = (Button) view;

        //The string that the user creates by pressing the buttons
        String text = m_display.getText().toString();

        switch(view.getId()){
            case R.id.button1:
            case R.id.button2:
            case R.id.button3:
            case R.id.button4:
            case R.id.button5:
            case R.id.button6:
            case R.id.button7:
            case R.id.button8:
            case R.id.button9:
            case R.id.button0:
                // If the user presses a number, and the only token is "0", we replace the "0"
                // with the user input
                if(text.equals("0")) clearText(m_display);
                m_display.append(buttonView.getText());
                break;

            case R.id.buttonP:
                // If the user is trying to type consecutive operands, ignore it
                if(!(text.endsWith("+") || text.endsWith("-"))){

                    // If the first button a user presses is the "+" operand, ignore it
                    if(!text.isEmpty()) m_display.append(buttonView.getText());
                }
                break;

            case R.id.buttonM:
                // If the user is trying to type consecutive operands, ignore it
                if(!(text.endsWith("+") || text.endsWith("-"))){

                    //Here we let the user enter a "-" operand in case he wants to enter a number
                    //below zero
                    m_display.append(buttonView.getText());
                }
                break;

            case R.id.buttonC:
                clearText(m_display);
                break;

            case R.id.buttonB:
                // Removes the last character from the TextView if the text isn't empty
                if(text.length() != 0){
                    String res = text;
                    res = res.substring(0, res.length() - 1);
                    m_display.setText(res);
                }
                break;

            case R.id.buttonE:
                String result = "";

                // If the user is trying to equal a single operator or equal an empty string, ignore it
                if((text.length() == 1 && text.charAt(0) == '-' ) || text.length() == 0){
                    clearText(m_display);
                }
                // If all requirements are met, evaluate the text input from the user
                else result = evaluateExpression(text);
                m_display.setText(result);
                break;
        }
    }

    String evaluateExpression(String expr){

        // In case of Integer overflow we use BigInteger
        BigInteger result = BigInteger.valueOf(0);

        List tokens = new ArrayList();

        // Split the expression into tokens so we can work with the input
        StringTokenizer st = new StringTokenizer( expr, "[+\\-]", true );
        while(st.hasMoreElements()){
            String token = st.nextToken();
            tokens.add(token);
        }

        // Basecase 1: If the user only entered a single positive number, return it
        if(tokens.size() == 1) return tokens.get(0).toString();

        // If the first number is a minus number, combine the first two tokens into a single number
        if(tokens.get(0).equals("-")) numbers = combineFirstTwoTokens(tokens);
        else numbers = tokens;

        // Extracts the operands from the numbers and splits them into two lists
        extractOperands();

        // Basecase 2: If the user only entered a single negative number, return it
        if(numbers.size() == 1) return numbers.get(0).toString();

        // Make the first number of the sequence the result to be able to add/subtract from it
        result = result.add(BigInteger.valueOf(Long.parseLong(numbers.get(0).toString())));

        // Calculate the rest of the sequence
        for (int i = 1; i < numbers.size() ; i++) {
            if(operands.get(i-1).toString().equals("-")) result = result.subtract(BigInteger.valueOf(Long.parseLong(numbers.get(i).toString())));
            else if(operands.get(i-1).toString().equals("+")) result = result.add(BigInteger.valueOf(Long.parseLong(numbers.get(i).toString())));
        }

        // Resetting lists for the next evaluation
        clearText(m_display);

        return result.toString();
    }

    // Combines the first two tokens of a list into a single token
    private List combineFirstTwoTokens(List tokens){

        List result = new ArrayList();

        // Constructs a new list with the first two tokens as a new single one
        result.add(tokens.get(0).toString()+tokens.get(1).toString());

        // Removes the two tokens since they've been put together in a new list
        tokens.remove(0);
        tokens.remove(0);

        // Add the remaining tokens to the new list, resulting in a list one size smaller
        for (int i = 0; i < tokens.size() ; i++) {
            result.add(tokens.get(i));
        }

        return result;
    }

    // Separates the expression into two lists
    private void extractOperands(){

        for (int i = 0; i < numbers.size() ; i++) {
            String tempNumber = numbers.get(i).toString();
            if(tempNumber.equals("-") || tempNumber.equals("+")){
                operands.add(tempNumber);
                numbers.remove(i);
                i = 0;
            }
        }
    }

    private void clearText(TextView text){
        text.setText("");
        operands.clear();
        numbers.clear();
    }



}
