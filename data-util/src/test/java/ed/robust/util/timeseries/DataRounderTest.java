/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author tzielins
 */
public class DataRounderTest {
    
    public DataRounderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test 
    public void testRoundNaNs() {
        
        for (ROUNDING_TYPE type : ROUNDING_TYPE.values()) {

            Double val = Double.NaN;
            Double exp = Double.NaN;           
            double res = DataRounder.round(val, type);
            assertEquals(exp, res,1E-6);
            
            val = Double.POSITIVE_INFINITY;
            exp = Double.POSITIVE_INFINITY;            
            res = DataRounder.round(val, type);
            assertEquals(exp, res,1E-6);
            
            val = Double.NEGATIVE_INFINITY;
            exp = Double.NEGATIVE_INFINITY;            
            res = DataRounder.round(val, type);
            assertEquals(exp, res,1E-6);
            
        }
    }
    
    @Test 
    public void testRoundOverflow() {
        
        for (ROUNDING_TYPE type : ROUNDING_TYPE.values()) {

            int intval = Integer.MAX_VALUE-2;
            Double val = (double)intval;
            
            Double exp = (double)intval;           
            double res = DataRounder.round(val, type);
            assertEquals(exp, res,1E-6);
            
        }
    }
    
    
    
    @Test
    public void testRound_double_ROUNDING_TYPE() {
        System.out.println("round");
        
        DataRounder rounder = new DataRounder(ROUNDING_TYPE.HALF_INT);
        double val = Math.random()*1000;
        for (double i = 0;i<1.1;i+=0.01) {
            System.out.println(rounder.round(val+i)+" ("+(val+i)+")");
            
        }
        
        double base = 40;
        int f40 = 0;
        int f405 = 0;
        int f41 = 0;
        int err = 0;
        
        for (int i = 0;i<3000;i++) {
            double value = base+Math.random();
            value = rounder.round(value);
            if (value == 40) f40++;
            else if (value == 40.5) f405++;
            else if (value == 41) f41++;
            else err++;
        }
        
        System.out.println("40: "+f40+" 40.5: "+f405+" f41: "+f41+" e: "+err);
    }

}
