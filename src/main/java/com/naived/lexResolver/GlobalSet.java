package com.naived.lexResolver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GlobalSet {

    public static GlobalSet getInstance(){
        if (instance == null){
            instance = new GlobalSet();
        }
        return instance;
    }

    private GlobalSet(){
    }

    public String allSet = "abcdefghijklmnopqrstuvwxyz ()[]{}:;\"',.<>?/|\\!~%^&*-_+=\n\t\f";

    public String grammarChar = "\\()[]{}.*+|?&";

    private static GlobalSet instance;

}
