/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author tzielins
 */
public class LinesParser {
    
    final String LINE_SEP = ";";
    final String LINE_MUL = "*";
    
    public List<String> parseLine(String line,int expSize) {
        if (expSize == 0) return new ArrayList<>();
        
        List<String> lines = parseLine(line);
        if (lines.size() == 1) {
            while (lines.size() < expSize) lines.add(lines.get(0));
        }
        if (lines.size() == expSize) return lines;
        throw new IllegalArgumentException("Mismatch between parsed line size: "+lines.size()+" and expected size: "+expSize+" in line code: "+line);
    }
    
    public List<String> parseLine(String line) {
        if (line == null || line.trim().equals("")) return new ArrayList<>(Arrays.asList(""));
        if (!line.contains(LINE_SEP)) return parseOneLineCode(line);
        
        List<String> codes = new ArrayList<>();
        String[] tokens = line.split(LINE_SEP);
        for (String token : tokens) codes.addAll(parseOneLineCode(token));
        return codes;
    }
    
    public List<String> parseOneLineCode(String code) {
        code = code.trim();        
        if (!code.contains(LINE_MUL)) return new ArrayList<>(Arrays.asList(code));
        
        try {
            String numP = code.substring(0,code.indexOf(LINE_MUL));
            String line = code.substring(code.indexOf(LINE_MUL)+1);
            int num = Integer.parseInt(numP);
            List<String> codes = new ArrayList<>(num);
            for (int i =0;i<num;i++) codes.add(line);
        return codes;
        } catch (Exception e) {
            throw new IllegalArgumentException("Wrong format of line code: "+code);
        }
    }
    
    
}
