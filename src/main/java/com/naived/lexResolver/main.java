package com.naived.lexResolver;

import com.naived.lexResolver.dfa.DfaNode;
import com.naived.lexResolver.exception.LexFileParseException;
import com.naived.lexResolver.exception.ReParseException;
import com.naived.lexResolver.lexResolver.FileResolver;
import com.naived.lexResolver.lexResolver.LexSource;
import com.naived.lexResolver.lexResolver.Resolver;
import com.naived.lexResolver.lexicalAnalysis.LexAnalysis;
import com.naived.lexResolver.lexicalAnalysis.LexStdoutAnalysis;
import com.naived.lexResolver.nfa.NfaNode;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class main {
    public static void main(String[] args) {
        try {
            InputStream inputStream = new FileInputStream("cLex.l");
            Resolver resolver = new FileResolver();
            LexSource lexSource = resolver.resolve(inputStream);
            NfaNode nfa = lexSource.suffixBuildNfa();
            DfaNode dfa = DfaNode.determineNfa(nfa);
            LexAnalysis lexAnalysis = new LexStdoutAnalysis(dfa, lexSource.getRules());
            lexAnalysis.analyze(new FileInputStream("main.c"));
            System.out.println();
        }catch (FileNotFoundException | LexFileParseException | ReParseException e){
            e.printStackTrace();
        }
    }
}
