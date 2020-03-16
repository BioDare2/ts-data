/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

/**
 * Enumeration for different rounding precisions. 
 * @author tzielins
 */
public enum ROUNDING_TYPE {
   
        /**
         * original value no rounding
         */
        NO_ROUNDING, 
        /**
         * rounding to 0.1
         */
        DECY, 
        /**
        * rounding to 0.01
        */CENTY, 
        /**
         * rounding to closest int
         */
        INTEGER,
        /**
         * rounding to 0.001
         */
        MIL,
        /**
         * rounding to 0.000001
         */
        MIKRO,
        /**
         * rounding to 0.5 so possible ends are 0, 0.5
         */
        HALF_INT
    
    
}
