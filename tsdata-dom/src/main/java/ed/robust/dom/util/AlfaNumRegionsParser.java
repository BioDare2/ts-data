/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author tzielins
 */
public class AlfaNumRegionsParser {
    
    
    final Character LAST_ROW;
    final int LAST_COL;
    final String WELL_SEP = ":";
    final String REG_SEP = ";";
    
    
    public AlfaNumRegionsParser(Character lastRow,int lastCol) {
        super();
        if (lastRow == null) throw new IllegalArgumentException("Last row cannot be null");
        this.LAST_ROW = lastRow;
        this.LAST_COL = lastCol;
    }
    
    public List<String> expandWells(String regions) {
        
        List<Pair<Character, Integer>> wells = decodeRegions(regions);
        Collections.sort(wells,new FullPairComparator<Character,Integer>());
        List<String> ids = new ArrayList<>(wells.size());
        for (Pair<Character,Integer> well : wells)
            ids.add(well.getLeft()+well.getRight().toString());
        return ids;
    }
    
    
    
    public List<Pair<Character, Integer>> decodeRegions(String regions) {
        List<Pair<Character, Integer>> list = new ArrayList<>();
        Set<Pair<Character, Integer>> present = new HashSet<>();
        
        String[] regs = regions.split(REG_SEP);
        for (String code : regs) {
            List<Pair<Character, Integer>> subReg = decodeRegion(code);
            int prevS = present.size();
            present.addAll(subReg);
            if (present.size() != (prevS+subReg.size())) throw new IllegalArgumentException("Duplicated regions: "+code+" in: "+regions);
            list.addAll(subReg);
        }
        return list;
    }
    
    protected List<Pair<Character, Integer>> decodeRegion(String region) {
        Pair<Character, Integer> firstCell;
        Pair<Character, Integer> lastCell;
        
        if (!region.contains(WELL_SEP)) {
            firstCell = decodeWell(region);
            lastCell = firstCell;
        } else {
            String[] tokens = region.split(WELL_SEP);
            if (tokens.length != 2) 
                throw new IllegalArgumentException("Expected START_CELL"+WELL_SEP+"END_CELL instead of:"+region);
        
            firstCell = decodeWell(tokens[0]);
            lastCell = decodeWell(tokens[1]);
        }
        
        return spanCells(firstCell,lastCell);
    }

    

    protected Pair<Character, Integer> decodeWell(String wellT) {
        if (wellT == null) throw new IllegalArgumentException("Well cannto be null");
        wellT = wellT.trim().toUpperCase();
        if (wellT.length() < 2) 
            throw new IllegalArgumentException("Wrong well: "+wellT+", expected for example B3");
        Character row = wellT.charAt(0);
        if (row.compareTo('A') < 0 || row.compareTo(LAST_ROW) > 0)
            throw new IllegalArgumentException("Row nr: "+row+", outside bounds [A,"+LAST_ROW+"]");
        
       
        int col = 0;
        try {
            col = Integer.parseInt(wellT.substring(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Col: "+wellT.substring(1)+" is not a number");
        }
        if (col < 1 || col > LAST_COL)
            throw new IllegalArgumentException("Col nr: "+col+", outside bounds [1,"+LAST_COL+"]");
        return new Pair<>(row,col);
    }

    protected List<Pair<Character, Integer>> spanCells(Pair<Character, Integer> firstCell, Pair<Character, Integer> lastCell) {
        List<Pair<Character, Integer>> cells = new ArrayList<>();
        
        char firstRow = firstCell.getLeft();
        char lastRow = lastCell.getLeft();
        int firstCol = firstCell.getRight();
        int lastCol = lastCell.getRight();
        
        if (lastRow < firstRow) throw new IllegalArgumentException("Wrong row order:"+firstCell+"-"+lastCell);
        if (lastCol < firstCol) throw new IllegalArgumentException("Wrong column order:"+firstCell+"-"+lastCell);
        
        for (char row = firstRow;row<=lastRow;row++) {
            for (int col = firstCol;col<=lastCol;col++) {
                cells.add(new Pair<>(row,col));
            }
        }
        
        return cells;
    }
    
    public List<Integer> enumerateWells(List<Pair<Character, Integer>> wellsCordinates) {
        List<Integer> ixs = new ArrayList<>();
        
        for (Pair<Character,Integer> cell : wellsCordinates) {
            int col = cell.getRight();
            int row = cell.getLeft()-'A';
            ixs.add(row*LAST_COL+col);
        }
        return ixs;
    }
    
}
