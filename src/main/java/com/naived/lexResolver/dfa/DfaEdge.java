package com.naived.lexResolver.dfa;

public class DfaEdge {

    private DfaNode from;
    private char key;
    private DfaNode to;

    public DfaEdge(DfaNode from, char key, DfaNode to){
        this.from = from;
        this.key = key;
        this.to = to;
        from.addOuter(this);
    }

    public DfaNode getFrom() {
        return from;
    }

    public char getKey() {
        return key;
    }

    public DfaNode getTo() {
        return to;
    }
}
