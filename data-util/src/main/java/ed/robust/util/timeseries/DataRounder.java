/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

/**
 * Utility class that performs data rounding of given precision. Can be acess statically or via instance whith set rounding type.
 * @author tzielins
 */
public class DataRounder {
    
    protected ROUNDING_TYPE rounding;
    
    /**
     * Rounds given number using requested precission. Rounding is implemented by first shifting decimal place to the right of so many
     * places as the precission dictatates (1 for DECY, 2 for CENTY ...) rounding the number to int using the Math.rint method and shifting
     * the decimal place back. 
     * <p> For example for 1.2345 and rounding CENTY, 1.2345 -&gt; 123.45 -&gt; 123 / 100 -&gt; 1.23
     * @param val value to be rounded
     * @param rounding rounding type
     * @return rounded value
     */
    public static double round(double val,ROUNDING_TYPE rounding) {
        switch(rounding) {
            case NO_ROUNDING: return val;
            case DECY: return Math.rint(val*10)/10.0;
            case CENTY: return Math.rint(val*100)/100.0;
            case MIL: return Math.rint(val*1000)/1000.0;
            case MIKRO: return Math.rint(val*1000000)/1000000.0;
            case INTEGER: return Math.rint(val);
            case HALF_INT: {
                double v = Math.floor(val);
                double r = val - v;
                if (r >= 0.25 && r< 0.75) v+=0.5;
                else if (r >=0.75) v+=1;
                return v;
            }
            default: throw new IllegalArgumentException("Unsuported rounding type: "+rounding);                
        }
        
    }
    
    
    /**
     * Creates rounder instance that will always round with the given precision
     * @param rounding  to be used when round is called
     */
    public DataRounder(ROUNDING_TYPE rounding) {
        this.rounding = rounding;
    }
    
    /**
     * Rounds number using the precision set during rounder construction
     * @param val value to be rounded
     * @return  rounded value
     */
    public double round(double val) {
        return round(val,rounding);
    }
    
}
