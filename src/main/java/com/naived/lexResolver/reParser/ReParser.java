package com.naived.lexResolver.reParser;

import com.naived.lexResolver.GlobalSet;
import com.naived.lexResolver.exception.ReParseException;

import java.util.ArrayList;
import java.util.Stack;

/**
 * re运算符有\转义运算符()优先级运算符[]范围运算符{}标签替代运算符.全集*任意个运算符+至少一个运算符|或运算符?至多一个运算符&连接运算符
 */
public class ReParser {

    enum RangeState {FIRST, FROM, LINE, TO}

    enum OperatorType {SINGLE, DOUBLE, BRACE}

    public String rangeParse(String re) throws ReParseException{
        boolean inRange = false;
        StringBuilder result = new StringBuilder();
        char lastChar = 0;
        char currentChar;
        boolean withTrans = false;
        StringBuilder rangeMake = new StringBuilder();
        RangeState state = RangeState.FIRST;
        char from = 0;
        boolean reversed = false;
        for (int i = 0; i < re.length(); i++){
            if (re.charAt(i) == '\\'){
                i++;
                if (i >= re.length()){
                    throw new ReParseException();
                }
                if (re.charAt(i) == 'n') currentChar = '\n';
                else if (re.charAt(i) == 't') currentChar = '\t';
                else if (re.charAt(i) == 'f') currentChar = '\f';
                else currentChar = re.charAt(i);
                withTrans = true;
            }
            else{
                currentChar = re.charAt(i);
                withTrans = false;
            }
            if (!inRange){
                if (currentChar == '[' && !withTrans){
                    inRange = true;
                }
                else{
                    reAppend(result, currentChar, withTrans);
                }
            }else{
                if (currentChar == ']' && !withTrans){
                    if (state == RangeState.LINE) {
                        if (!reversed && rangeMake.indexOf(from + "") == -1) rangeMake.append(from);
                        else if (reversed && rangeMake.indexOf(from + "") != -1) rangeMake.deleteCharAt(rangeMake.indexOf(from + ""));
                    }
                    if (rangeMake.length() >= 1){
                        result.append('(');
                        reAppend(result, rangeMake.charAt(0), true);
                        for (int j= 1; j < rangeMake.length(); j++){
                            result.append('|');
                            reAppend(result, rangeMake.charAt(j), true);
                        }
                        result.append(')');
                    }
                    inRange = false;
                    rangeMake = new StringBuilder();
                    from = 0;
                    state = RangeState.FIRST;
                    reversed = false;
                }
                else{
                    switch (state){
                        case FIRST:
                            if (currentChar == '^' && !withTrans){
                                rangeMake.append(GlobalSet.getInstance().allSet);
                                reversed = true;
                                state = RangeState.FROM;
                                break;
                            }
                        case FROM:
                            from = currentChar;
                            state = RangeState.LINE;
                            break;
                        case LINE:
                            if (currentChar == '-' && !withTrans){
                                state = RangeState.TO;
                            }
                            else{
                                if (!reversed && rangeMake.indexOf(from + "") == -1) rangeMake.append(from);
                                else if (reversed && rangeMake.indexOf(from + "") != -1) rangeMake.deleteCharAt(rangeMake.indexOf(from + ""));
                                from = currentChar;
                                state = RangeState.LINE;
                            }
                            break;
                        case TO:
                            if (from <= currentChar){
                                for (char ch = from; ch <= currentChar; ch++){
                                    if (!reversed && rangeMake.indexOf(ch + "") == -1) rangeMake.append(ch);
                                    else if (reversed && rangeMake.indexOf(ch + "") != -1) rangeMake.deleteCharAt(rangeMake.indexOf(ch + ""));
                                }
                            }
                            state = RangeState.FROM;
                            break;
                    }
                }
            }
            lastChar = currentChar;
        }
        return result.toString();
    }

    public String dotParse(String re) throws ReParseException{
        StringBuilder result = new StringBuilder();
        char currentChar = 0;
        boolean withTrans = false;
        for (int i = 0; i < re.length(); i++){
            if (re.charAt(i) == '\\'){
                i++;
                if (i >= re.length()) throw new ReParseException();
                if (re.charAt(i) == 'n') currentChar = '\n';
                else if (re.charAt(i) == 't') currentChar = '\t';
                else if (re.charAt(i) == 'f') currentChar = '\f';
                else currentChar = re.charAt(i);
                withTrans = true;
            }
            else{
                currentChar = re.charAt(i);
                withTrans = false;
            }
            if (currentChar == '.' && !withTrans){
                result.append('(');
                reAppend(result, GlobalSet.getInstance().allSet.charAt(0), true);
                for (int j = 1; j < GlobalSet.getInstance().allSet.length(); j++){
                    result.append('|');
                    reAppend(result, GlobalSet.getInstance().allSet.charAt(j), true);
                }
                result.append(')');
            }
            else {
                reAppend(result, currentChar, withTrans);
            }
        }
        return result.toString();
    }

    public String braceParse(String re, ArrayList<ArrayList<String>> define) throws ReParseException{
        StringBuilder result = new StringBuilder();
        char currentChar = 0;
        boolean withTrans = false;
        boolean withinBrace = false;
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < re.length(); i++){
            if (re.charAt(i) == '\\'){
                i++;
                if (i >= re.length()) throw new ReParseException();
                if (re.charAt(i) == 'n') currentChar = '\n';
                else if (re.charAt(i) == 't') currentChar = '\t';
                else if (re.charAt(i) == 'f') currentChar = '\f';
                else currentChar = re.charAt(i);
                withTrans = true;
            }
            else{
                currentChar = re.charAt(i);
                withTrans = false;
            }
            if (currentChar == '{' && !withTrans && !withinBrace){
                withinBrace = true;
            }
            else if (withinBrace && !(currentChar == '}' && !withTrans)){
                key.append(currentChar);
            }
            else if (currentChar == '}' && !withTrans){
                boolean found = false;
                for (ArrayList<String> keyValue : define){
                    if (keyValue.get(0).equals(key.toString())){
                        found = true;
                        result.append('(');
                        result.append(keyValue.get(1));
                        result.append(')');
                    }
                }
                if (!found) System.out.println("{" + key.toString() + "} not found");
                withinBrace = false;
                key = new StringBuilder();
            }
            else {
                reAppend(result, currentChar, withTrans);
            }
        }
        return result.toString();
    }

    public String reSuffix(String re) throws ReParseException {
        StringBuilder result = new StringBuilder();
        Stack<Character> operatorStack = new Stack<>();
        char currentChar = 0;
        char lastChar = '(';
        boolean withTrans = false;
        boolean lastTrans = false;
        for (int i = 0; i < re.length(); i++) {
            if (re.charAt(i) == '\\') {
                i++;
                if (i >= re.length()) throw new ReParseException();
                if (re.charAt(i) == 'n') currentChar = '\n';
                else if (re.charAt(i) == 't') currentChar = '\t';
                else if (re.charAt(i) == 'f') currentChar = '\f';
                else currentChar = re.charAt(i);
                withTrans = true;
            }
            else{
                currentChar = re.charAt(i);
                withTrans = false;
            }
            if (currentChar == '(' && !withTrans){
                if (lastChar == ')' || lastTrans || GlobalSet.getInstance().grammarChar.indexOf(lastChar) == -1 || isUnaryOperator(lastChar)){
                    while (!operatorStack.empty() && isUnaryOperator(operatorStack.peek())){
                        result.append(operatorStack.pop());
                    }
                    operatorStack.push('&');
                }
                operatorStack.push('(');
            }
            else if (isUnaryOperator(currentChar) && !withTrans){
                operatorStack.push(currentChar);
            }
            else if (currentChar == '|' && !withTrans){
                while (!operatorStack.empty() && operatorStack.peek() != '|' && operatorStack.peek() != '('){
                    result.append(operatorStack.pop());
                }
                operatorStack.push('|');
            }
            else if (currentChar == ')' && !withTrans){
                while (!operatorStack.empty() && operatorStack.peek() != '('){
                    result.append(operatorStack.pop());
                }
                if (!operatorStack.empty()) operatorStack.pop();
            }
            else{
                if (lastChar == ')' || lastTrans || GlobalSet.getInstance().grammarChar.indexOf(lastChar) == -1 || isUnaryOperator(lastChar)){
                    while (!operatorStack.empty() && isUnaryOperator(operatorStack.peek())){
                        result.append(operatorStack.pop());
                    }
                    operatorStack.push('&');
                }
                reAppend(result, currentChar, true);
            }
            lastChar = currentChar;
            lastTrans = withTrans;
        }
        while (!operatorStack.empty()){
            result.append(operatorStack.pop());
        }
        return result.toString();
    }

    private boolean isUnaryOperator(char c){
        return c == '*' || c == '+' || c=='?';
    }

    private boolean isBinaryOperator(char c){
        return c == '|' || c == '&';
    }

    private boolean isBraceOperator(char c){
        return c == '(' || c == ')';
    }

    private void reAppend(StringBuilder builder, char c, boolean matchingChar){
        if (matchingChar){
            if (GlobalSet.getInstance().grammarChar.indexOf(c) != -1) {
                builder.append('\\');
                builder.append(c);
            }
            else if (c == '\n'){
                builder.append("\\n");
            }
            else if (c == '\t'){
                builder.append("\\t");
            }
            else if (c == '\f'){
                builder.append("\\f");
            }
            else {
                builder.append(c);
            }
        }
        else{
            if (c == '\n'){
                builder.append("\\n");
            }
            else if (c == '\t'){
                builder.append("\\t");
            }
            else if (c == '\f'){
                builder.append("\\f");
            }
            else {
                builder.append(c);
            }
        }
    }

    public static void main(String[] args) {
        ReParser reParser = new ReParser();
        try {
            System.out.println(reParser.reSuffix("((u|U)|(u|U)?(l|L|ll|LL)|(l|L|ll|LL)(u|U))"));
        }catch (ReParseException e){
            e.printStackTrace();
        }
    }

}
