/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.util;

/**
 *
 * @author tzielins
 */
public class OptionalValueConverter {
    
    
    public static int optionalInt(String value, int defVal) {
        return optionalInt(value, defVal, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }
    
    public static int optionalInt(String value, int defVal,int min,int max) {
        if (value == null || value.trim().isEmpty()) return defVal;
        try {
            int v = Integer.parseInt(value);
            if (v < min || v > max) v = defVal;
            return v;
        } catch (NumberFormatException e) {
            return defVal;
        }
    }
    
    public static long optionalLong(String value,long defVal) {
        return optionalLong(value, defVal, Long.MIN_VALUE, Long.MAX_VALUE);
    }
    
    public static long optionalLong(String value,long defVal,long min,long max) {
        if (value == null || value.trim().isEmpty()) return defVal;
        try {
            long v = Long.parseLong(value);
            if (v < min || v > max) v = defVal;
            return v;
        } catch (NumberFormatException e) {
            return defVal;
        }        
    }
    
}
