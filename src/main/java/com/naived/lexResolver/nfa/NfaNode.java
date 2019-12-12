package com.naived.lexResolver.nfa;

import com.naived.lexResolver.exception.ReParseException;
import com.naived.lexResolver.reParser.ReParser;

import java.util.*;

public class NfaNode {
    private ArrayList<NfaEdge> outers;
    private int endFor = -1;

    public NfaNode(){
        outers = new ArrayList<>();
    }

    public Iterator<NfaEdge> getOuters() {
        return outers.iterator();
    }

    public void addOuter(NfaEdge outer) {
        this.outers.add(outer);
    }

    public int getEndFor() {
        return endFor;
    }

    public void setEndFor(int endFor){
        this.endFor = endFor;
    }

    public HashSet<NfaNode> closure(){
        HashSet<NfaNode> set = new HashSet<>();
        this.closure(set);
        return set;
    }

    public void closure(HashSet<NfaNode> set){
        if (set.add(this)){
            for (NfaEdge edge : outers){
                if (edge.getKey() == '\0'){
                    edge.getTo().closure(set);
                }
            }
        }
    }

    /**
     * 通过后缀正规表达式re建立nfa
     * @param re 后缀正规表达式
     * @return nfa入口节点
     */
    public static NfaNode buildSuffixReNfa(String re, int endFor){
        Stack<ArrayList<NfaNode>> faStack = new Stack<>();
        for (int i = 0; i < re.length(); i++){
            if (re.charAt(i) == '\\'){
                i++;
                if (re.charAt(i) == 'n') faStack.push(buildSimpleNfa('\n'));
                else if(re.charAt(i) == 't') faStack.push(buildSimpleNfa('\t'));
                else if(re.charAt(i) == 'f') faStack.push(buildSimpleNfa('\f'));
                else faStack.push(buildSimpleNfa(re.charAt(i)));
            }
            else if (re.charAt(i) == '*'){
                faStack.push(buildRepeatNfa(faStack.pop()));
            }
            else if (re.charAt(i) == '|'){
                faStack.push(buildOrNfa(faStack.pop(), faStack.pop()));
            }
            else if (re.charAt(i) == '&'){
                faStack.push(buildJoinNfa(faStack.pop(), faStack.pop()));
            }
            else if (re.charAt(i) == '+'){
                faStack.push(buildPlusNfa(faStack.pop()));
            }
            else if (re.charAt(i) == '?'){
                faStack.push(buildOneLessNfa(faStack.pop()));
            }
            else {
                faStack.push(buildSimpleNfa(re.charAt(i)));
            }
        }
        ArrayList<NfaNode> result = faStack.pop();
        result.get(1).endFor = endFor;
        return result.get(0);
    }

    private static ArrayList<NfaNode> buildSimpleNfa(char key){
        NfaNode from = new NfaNode();
        NfaNode to = new NfaNode();
        NfaEdge edge = new NfaEdge(from, key, to);
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(from);
        result.add(to);
        return result;
    }

    private static ArrayList<NfaNode> buildRepeatNfa(ArrayList<NfaNode> key){
        NfaNode from = new NfaNode();
        NfaNode to = new NfaNode();
        NfaEdge a = new NfaEdge(from, '\0', key.get(0));
        NfaEdge b = new NfaEdge(key.get(1), '\0', to);
        NfaEdge c = new NfaEdge(from, '\0', to);
        NfaEdge d = new NfaEdge(key.get(1), '\0', key.get(0));
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(from);
        result.add(to);
        return result;
    }

    private static ArrayList<NfaNode> buildPlusNfa(ArrayList<NfaNode> key){
        ArrayList<NfaNode> copy = copyNfa(key);
        ArrayList<NfaNode> head = buildRepeatNfa(key);
        return buildJoinNfa(head, copy);
    }

    private static ArrayList<NfaNode> copyNfa(ArrayList<NfaNode> key){
        ArrayList<NfaNode> nodes = new ArrayList<>();
        addChildren(key.get(0), nodes);
        ArrayList<NfaNode> copyNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++){
            copyNodes.add(new NfaNode());
        }
        for (NfaNode node : nodes) {
            for (Iterator<NfaEdge> it = node.getOuters(); it.hasNext(); ) {
                NfaEdge edge = it.next();
                NfaEdge newEdge = new NfaEdge(copyNodes.get(nodes.indexOf(edge.getFrom())), edge.getKey(), copyNodes.get(nodes.indexOf(edge.getTo())));
            }
        }
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(copyNodes.get(nodes.indexOf(key.get(0))));
        result.add(copyNodes.get(nodes.indexOf(key.get(1))));
        return result;
    }

    private static void addChildren(NfaNode node, ArrayList<NfaNode> set){
        if (set.indexOf(node) == -1) set.add(node);
        else return;
        for (Iterator<NfaEdge> it = node.getOuters(); it.hasNext(); ) {
            NfaEdge edge = it.next();
            addChildren(edge.getTo(), set);
        }
    }

    private static ArrayList<NfaNode> buildOneLessNfa(ArrayList<NfaNode> key){
        NfaNode from = new NfaNode();
        NfaNode to = new NfaNode();
        NfaEdge a = new NfaEdge(from, '\0', key.get(0));
        NfaEdge b = new NfaEdge(key.get(1), '\0', to);
        NfaEdge c = new NfaEdge(from, '\0', to);
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(from);
        result.add(to);
        return result;
    }

    private static ArrayList<NfaNode> buildOrNfa(ArrayList<NfaNode> key1, ArrayList<NfaNode> key2){
        NfaNode from = new NfaNode();
        NfaNode to = new NfaNode();
        NfaEdge a = new NfaEdge(from, '\0', key1.get(0));
        NfaEdge b = new NfaEdge(key1.get(1), '\0', to);
        NfaEdge c = new NfaEdge(from, '\0', key2.get(0));
        NfaEdge d = new NfaEdge(key2.get(1), '\0', to);
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(from);
        result.add(to);
        return result;
    }

    private static ArrayList<NfaNode> buildJoinNfa(ArrayList<NfaNode> key2, ArrayList<NfaNode> key1){
        NfaEdge a = new NfaEdge(key1.get(1), '\0', key2.get(0));
        ArrayList<NfaNode> result = new ArrayList<>();
        result.add(key1.get(0));
        result.add(key2.get(1));
        return result;
    }

    public static void main(String[] args) {
        ReParser reParser = new ReParser();
        try {
            NfaNode node = buildSuffixReNfa(reParser.reSuffix("(u|U)+"), 0);
            System.out.println();
        }catch (ReParseException e){
            e.printStackTrace();
        }
    }
}
