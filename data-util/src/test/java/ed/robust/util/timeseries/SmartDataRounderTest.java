/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class SmartDataRounderTest {
    
    public SmartDataRounderTest() {
    }

    /**
     * Test of round method, of class SmartDataRounder.
     */
    @Test
    public void testRound() {
        System.out.println("round");
        
        double EPS = 1E-9;
        double value = 0.000123232331;
        double expResult = 0.000123232331;
        double result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);

        value = 0.0123232331;
        expResult = 0.012323;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);
        
        value = 0.123232331;
        expResult = 0.123232;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);
        
        value = 1.266363232331;
        expResult = 1.266;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);
        
        value = 12.66363232331;
        expResult = 12.66;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);
        
        value = 52.66863232331;
        expResult = 52.67;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);

        value = 126.6363232331;
        expResult = 126.6;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);

        value = 526.6863232331;
        expResult = 527;
        result = SmartDataRounder.round(value);
        assertEquals(expResult, result, EPS);
    }
    
    @Test
    public void testRoundWithRef() {
        System.out.println("round with ref");
        
        double EPS = 1E-9;
        double value = 1234.5671123232331;
        double ref = 0.0001;
        double expResult = 1234.5671123232331;
        double result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);
        
        ref = 0.1;
        expResult = 1234.567112;
        result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);
        
        ref = 1.3;
        expResult = 1234.567;
        result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);

        ref = 15;
        expResult = 1234.57;
        result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);
        
        ref = 150;
        expResult = 1234.6;
        result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);
        
        ref = 600;
        expResult = 1235;
        result = SmartDataRounder.round(value,ref);
        assertEquals(expResult, result, EPS);
        
    }
    
}