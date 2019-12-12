package com.naived.lexResolver.lexResolver;

import com.naived.lexResolver.exception.LexFileParseException;
import com.naived.lexResolver.exception.ReParseException;
import com.naived.lexResolver.reParser.ReParser;

import java.io.*;
import java.util.ArrayList;

public class FileResolver implements Resolver {
    enum State {DEFAULT, HEAD, DEFINE, RULE, SUP}
    State state = State.DEFAULT;

    public LexSource resolve(InputStream source) throws LexFileParseException, ReParseException{
        LexSource lexSource = parseFile(source);
        normalizeRe(lexSource);
        return lexSource;
    }

    private void handleQuote(LexSource lex){
        for (ArrayList<String> rule : lex.rules){
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < rule.get(0).length(); i++){
                if (rule.get(0).charAt(i) != '\"') {
                    if(rule.get(0).charAt(i) == '\\'){
                        i++;
                        if (rule.get(0).charAt(i) >= '0' && rule.get(0).charAt(i) <= '9'){
                            builder.append(rule.get(0).charAt(i) - '0');
                        }
                        else if (rule.get(0).charAt(i) == 'n'){
                            builder.append('\n');
                        }
                        else if (rule.get(0).charAt(i) == 't'){
                            builder.append('\t');
                        }
                        else if (rule.get(0).charAt(i) == 'f'){
                            builder.append('\f');
                        }
                        else if (rule.get(0).charAt(i) == '\\'){
                            builder.append('\\');
                        }
                        else if (rule.get(0).charAt(i) == '\"'){
                            builder.append('\"');
                        }
                        else{
                            builder.append(rule.get(0).charAt(i));
                        }
                    }else{
                        builder.append(rule.get(0).charAt(i));
                    }
                }
            }
            rule.set(0, builder.toString());
        }
    }

    private void normalizeRe(LexSource lex) throws ReParseException{
        ReParser reParser = new ReParser();
        for (ArrayList<String> define : lex.define){
            define.set(1, reParser.braceParse(reParser.dotParse(reParser.rangeParse(define.get(1))), lex.define));
        }
        for (ArrayList<String> rule : lex.rules){
            rule.set(0, reParser.reSuffix(reParser.braceParse(reParser.dotParse(reParser.rangeParse(rule.get(0))), lex.define)));
        }
    }

    public LexSource parseFile(InputStream source) throws LexFileParseException {
        LexSource lex = new LexSource();
        BufferedReader reader = new BufferedReader(new InputStreamReader(source));
        boolean err = false;
        String errDescription;
        try {
            String line;
            int ruleBraces = 0;
            boolean betweenRule = false;
            while ((line = reader.readLine()) != null){
                switch (state){
                    case DEFAULT:
                        if (line.equals("%{")){
                            state = State.HEAD;
                        }
                        else {
                            err = true;
                            errDescription = "cannot find lex head";
                            throw new LexFileParseException();
                        }
                        break;
                    case HEAD:
                        if (line.equals("%}")){
                            state = State.DEFINE;
                        }
                        else{
                            lex.head.add(line);
                        }
                        break;
                    case DEFINE:
                        if (line.equals("%%")){
                            state = State.RULE;
                        }
                        else if (line.matches("(\t| )*")){
                            continue;
                        }
                        else{
                            String[] results = line.split("(\t| )+");
                            ArrayList<String> defineLine = new ArrayList<String>();
                            defineLine.add(0, results[0]);
                            defineLine.add(1, results[1]);
                            lex.define.add(defineLine);
                        }
                        break;
                    case RULE:
                        if (line.equals("%%")){
                            state = State.SUP;
                        }
                        else if (line.matches("(\t| )*")){
                            continue;
                        }
                        else{
                            /*
                            String[] results;
                            if (line.charAt(0) == '['){
                                results = line.split("]");
                                results[0] += "]";
                                results[1] = results[1].replaceFirst("(\t| )+", "");
                            }
                            else {
                                results = line.split("(\t| )+");
                            }
                            ArrayList<String> ruleLine = new ArrayList<String>(1);
                            ruleLine.add(0, results[0]);
                            String testUp = results[1];
                             */
                            ArrayList<String> ruleLine = new ArrayList<String>();
                            StringBuilder reRaw = new StringBuilder();
                            int braceDeep = 0;
                            int breakPoint = line.length();
                            for (int i = 0; i < line.length(); i++){
                                if (line.charAt(i) == '\\'){
                                    reRaw.append('\\');
                                    i++;
                                    reRaw.append(line.charAt(i));
                                }
                                else if (line.charAt(i) == '(' || line.charAt(i) == '[' || line.charAt(i) == '{'){
                                    braceDeep++;
                                    reRaw.append(line.charAt(i));
                                }
                                else if (line.charAt(i) == ')' || line.charAt(i) == ']' || line.charAt(i) == '}'){
                                    braceDeep--;
                                    reRaw.append(line.charAt(i));
                                }
                                else if (line.charAt(i) == ' ' && braceDeep > 0){
                                    reRaw.append(line.charAt(i));
                                }
                                else if (line.charAt(i) == ' ' && braceDeep == 0){
                                    breakPoint = i;
                                    break;
                                }
                                else if (line.charAt(i) == '\t'){
                                    breakPoint = i;
                                    break;
                                }
                                else{
                                    reRaw.append(line.charAt(i));
                                }
                            }
                            ruleLine.add(0, reRaw.toString());
                            String testUp = line.substring(breakPoint);
                            testUp = testUp.replaceFirst("(\t| )+", "");
                            while (testUp.length() == 0 || testUp.charAt(0)!= '{'){
                                testUp = reader.readLine();
                                if (testUp == null){
                                    err = true;
                                    errDescription = "rule braces match err";
                                    throw new LexFileParseException();
                                    //break;
                                }
                            }
                            if (err) {
                                throw new LexFileParseException();
                                //break;
                            }
                            while (true){
                                ruleLine.add(testUp);
                                if (testUp.charAt(testUp.length() - 1) == '}') break;
                                testUp = reader.readLine();
                                if (testUp == null){
                                    err = true;
                                    errDescription = "rule braces match err";
                                    throw new LexFileParseException();
                                    //break;
                                }
                            }
                            if (err) break;
                            lex.rules.add(ruleLine);
                        }
                        break;
                    case SUP:
                        lex.sup.add(line);
                        break;
                }
                if (err) throw new LexFileParseException();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
//        handleQuote(lex);
        return lex;
    }

    public static void main(String[] args) {
        try {
            InputStream inputStream = new FileInputStream("cLex.l");
            Resolver resolver = new FileResolver();
            LexSource lexSource = resolver.resolve(inputStream);
            System.out.println(lexSource);
        }catch (FileNotFoundException | LexFileParseException | ReParseException e){
            e.printStackTrace();
        }
    }
}
