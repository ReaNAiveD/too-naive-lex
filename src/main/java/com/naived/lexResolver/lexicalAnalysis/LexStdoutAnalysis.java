package com.naived.lexResolver.lexicalAnalysis;

import com.naived.lexResolver.dfa.DfaEdge;
import com.naived.lexResolver.dfa.DfaNode;
import com.naived.lexResolver.exception.UnmatchException;

import java.io.*;
import java.util.ArrayList;

public class LexStdoutAnalysis implements LexAnalysis {

    DfaNode dfa;
    ArrayList<ArrayList<String>> outputTable;

    public LexStdoutAnalysis(DfaNode dfa, ArrayList<ArrayList<String>> outputTable){
        this.dfa = dfa;
        this.outputTable = outputTable;
    }

    @Override
    public void analyze(InputStream input) {
        Reader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder content = new StringBuilder();
        DfaNode currentState = dfa;
        try {
            int i;
            while ((i = reader.read()) != -1){
                boolean match = false;
                while (!match){
                    for (DfaEdge trans : currentState.getOuters()){
                        if (trans.getKey() == (char)i){
                            match = true;
                            currentState = trans.getTo();
                            content.append((char)i);
                            break;
                        }
                    }
                    if (!match) {
                        if (currentState != null && currentState.getEndFor() >= 0) {
                            System.out.println(content.toString());
                            for (int j = 1; j < outputTable.get(currentState.getEndFor()).size(); j++) {
                                System.out.println(outputTable.get(currentState.getEndFor()).get(j));
                            }
                            System.out.println("------------------------------------");
                            currentState = dfa;
                            content = new StringBuilder();
                        } else {
                            System.out.print("cannot recognize for ");
                            System.out.print((char)i);
                            System.out.println(" after " + content);
                            System.out.println("------------------------------------");
                            currentState = dfa;
                            content = new StringBuilder();
                        }
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
