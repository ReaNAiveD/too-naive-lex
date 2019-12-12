package com.naived.lexResolver.nfa;

public class NfaEdge {

    private NfaNode from;
    private char key;
    private NfaNode to;

    public NfaEdge(NfaNode from, char key, NfaNode to){
        this.from = from;
        this.key = key;
        this.to = to;
        from.addOuter(this);
    }

    public NfaNode getFrom() {
        return from;
    }

    public char getKey() {
        return key;
    }

    public NfaNode getTo() {
        return to;
    }

}
