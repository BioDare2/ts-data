/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

/**
 *
 * @author tzielins
 */
public class SmartDataRounder {
    
    static final DataRounder mik = new DataRounder(ROUNDING_TYPE.MIKRO);
    static final DataRounder mil = new DataRounder(ROUNDING_TYPE.MIL);
    static final DataRounder centy = new DataRounder(ROUNDING_TYPE.CENTY);
    static final DataRounder decy = new DataRounder(ROUNDING_TYPE.DECY);
    static final DataRounder integer = new DataRounder(ROUNDING_TYPE.INTEGER);
    
    public static double round(double value) {
        return round(value,value);
    }

    public static double round(double value, double reference) {
        
        reference = Math.abs(reference);
        if (reference > 500) return integer.round(value);
        if (reference > 100) return decy.round(value);
        if (reference > 10) return centy.round(value);
        if (reference > 1) return mil.round(value);
        if (reference > 0.001) return mik.round(value);
        return value;
    }
}
