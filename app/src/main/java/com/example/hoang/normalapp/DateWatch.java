package com.example.hoang.normalapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Calendar;

/**
 * Created by Hoang on 11/20/2016.
 */

public class DateWatch implements TextWatcher {
    private String current = "";
    private String ddmmyyyy = "DDMMYYYY";
    private Calendar cal = Calendar.getInstance();
    private EditText date;

    public DateWatch(EditText date) {
        this.date = date;
    }

    public DateWatch() {
    }
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

            String enteredVal = s.toString();
            boolean isValid = true;
            boolean isValidMonth = true;
            boolean isValidYear = true;
            boolean isValidDay = true;
            String enteredMonth;
            String enteredDate;
            String enteredYear;

            if (enteredVal.length()==2 && before ==0) {
                enteredMonth = enteredVal.substring(0,1);
                if (Integer.parseInt(enteredVal) < 1 || Integer.parseInt(enteredVal)>12) {
                    isValidMonth = false;
                    //isValid = false;
                } else {
                    enteredVal+="/";
                    date.setText(enteredVal);
                    date.setSelection(enteredVal.length());
                }
           } else if (enteredVal.length() == 5 && before == 0) {
                enteredDate = enteredVal.substring(3);
                if (Integer.parseInt(enteredDate) > 31) {
                    isValidDay = false;
                    date.setError("Enter a valid date: DD/YYYY");
                } else {
                    enteredVal+="/";
                    date.setText(enteredVal);
                    date.setSelection(enteredVal.length());
                }
            }
            else if (enteredVal.length()==10 && before ==0) {
                //enteredVal.sub
                enteredYear = enteredVal.substring(6);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (Integer.parseInt(enteredYear) > 2016) {
                    isValidYear = false;
                } else {
                    date.setText(enteredVal);
                    date.setSelection(enteredVal.length());
                }
            } else if (enteredVal.length()> 10) {
                isValid = false;
            }

            if (!isValid) {
                date.setError("Enter a valid date: MM/DD/YYYY");
            } else if (isValidMonth == false) {
                date.setError("Enter a valid date: MM/DD/YYYY");
                date.setText("");
            } else if (isValidDay == false) {

                date.setText(enteredVal.substring(0,3));
                date.setSelection(3);
            } else if (isValidYear == false)  {
                date.setError("Enter a valid date: YYYY");
                date.setText(enteredVal.substring(0,6));
                date.setSelection(6);
//            } else {
//                date.setError(null);
            }
        }
    public void enterSpeechDate(String enteredDate) {
        date.setText(enteredDate);
    }
    @Override
    public void afterTextChanged(Editable s) {

    }
}
