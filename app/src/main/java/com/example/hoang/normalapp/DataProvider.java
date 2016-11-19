package com.example.hoang.normalapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Hoang on 11/19/2016.
 */

public class DataProvider {
    public static HashMap<String, List<String>> getInfo() {
        HashMap<String, List<String>> details = new HashMap<String, List<String>>();
        List<String> action = new ArrayList<>();
        action.add("What hell");
        action.add("Ok, then");
        List<String> comedy = new ArrayList<>();
        action.add("Good morning");
        action.add("Goodbye");
        List<String> notImportant = new ArrayList<>();
        action.add("Hello");
        action.add("Hi");

        details.put("Action", action);
        details.put("Comedy", comedy);
        details.put("Don't know", notImportant);

    return details;
    }
}
