package com.naived.lexResolver.dfa;

import com.naived.lexResolver.nfa.NfaEdge;
import com.naived.lexResolver.nfa.NfaNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class DfaNode {

    private ArrayList<DfaEdge> outers;

    private HashSet<NfaNode> nodeSet;

    private int endFor = -1;

    public DfaNode(){
        outers = new ArrayList<>();
        nodeSet = new HashSet<>();
    }

    public ArrayList<DfaEdge> getOuters() {
        return outers;
    }

    public HashSet<NfaNode> getNodeSet() {
        return nodeSet;
    }

    public int getEndFor() {
        return endFor;
    }

    public void addOuter(DfaEdge edge){
        outers.add(edge);
    }

    public void addNode(NfaNode node){
        if (!nodeSet.contains(node)){
            nodeSet.add(node);
            if (endFor == -1 || (endFor > node.getEndFor() && node.getEndFor() != -1)){
                endFor = node.getEndFor();
            }
        }
    }

    public void calculateEndFor(){
        for (NfaNode node : nodeSet){
            if (endFor == -1 || (node.getEndFor() != -1 && node.getEndFor() < endFor)){
                endFor = node.getEndFor();
            }
        }
    }

    public boolean same(DfaNode node){
        boolean result = true;
        for (NfaNode nfaNode : nodeSet){
            if(!node.nodeSet.contains(nfaNode)){
                result = false;
                break;
            }
        }
        if (!result) return false;
        for (NfaNode nfaNode : node.nodeSet){
            if(!nodeSet.contains(nfaNode)){
                result = false;
                break;
            }
        }
        return result;
    }

    public void expandToDfa(ArrayList<DfaNode> allNodes){
        HashSet<Character> trans = new HashSet<>();
        for (NfaNode node : nodeSet){
            for (Iterator<NfaEdge> it = node.getOuters(); it.hasNext(); ) {
                NfaEdge edge = it.next();
                trans.add(edge.getKey());
            }
        }
        for (char key : trans){
            if (key == '\0') continue;
            DfaNode targetNode = new DfaNode();
            ArrayList<NfaNode> directNodes = new ArrayList<>();
            for (NfaNode node : nodeSet){
                for (Iterator<NfaEdge> it = node.getOuters(); it.hasNext(); ) {
                    NfaEdge edge = it.next();
                    if (edge.getKey() == key) {
                        directNodes.add(edge.getTo());
                    }
                }
            }
            for (NfaNode node : directNodes){
                node.closure(targetNode.nodeSet);
            }
            boolean same = false;
            for (DfaNode dfaNode : allNodes){
                if (dfaNode.same(targetNode)) {
                    same = true;
                    targetNode = dfaNode;
                    break;
                }
            }
            new DfaEdge(this, key, targetNode);
            if (!same){
                targetNode.calculateEndFor();
                allNodes.add(targetNode);
                targetNode.expandToDfa(allNodes);
            }
        }
    }

    public static DfaNode determineNfa(NfaNode nfa){
        DfaNode result = new DfaNode();
        result.nodeSet = nfa.closure();
        ArrayList<DfaNode> allNodes = new ArrayList<>();
        allNodes.add(result);
        result.expandToDfa(allNodes);
        return result;
    }

}
