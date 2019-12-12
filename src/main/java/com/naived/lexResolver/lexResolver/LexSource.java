package com.naived.lexResolver.lexResolver;

import com.naived.lexResolver.nfa.NfaEdge;
import com.naived.lexResolver.nfa.NfaNode;

import java.util.ArrayList;

public class LexSource {
    ArrayList<String> head;
    ArrayList<ArrayList<String>> define;
    ArrayList<ArrayList<String>> rules;
    ArrayList<String> sup;

    public ArrayList<String> getHead() {
        return head;
    }

    public ArrayList<ArrayList<String>> getDefine() {
        return define;
    }

    public ArrayList<ArrayList<String>> getRules() {
        return rules;
    }

    public ArrayList<String> getSup() {
        return sup;
    }

    public NfaNode suffixBuildNfa(){
        NfaNode result = new NfaNode();
        for (int i = 0; i < rules.size(); i++){
            NfaNode node = NfaNode.buildSuffixReNfa(rules.get(i).get(0), i);
            new NfaEdge(result, '\0', node);
        }
        return result;
    }

    public LexSource() {
        head = new ArrayList<String>();
        define = new ArrayList<ArrayList<String>>();
        rules = new ArrayList<ArrayList<String>>();
        sup = new ArrayList<String>();
    }
}
