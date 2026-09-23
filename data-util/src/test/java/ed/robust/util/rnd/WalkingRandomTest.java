/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.rnd;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Zielu
 */
public class WalkingRandomTest {
    
    public WalkingRandomTest() {
    }

    /**
     * Test of random method, of class WalkingRandom.
     */
    @Test
    public void testRandom() {
        double step = 0.25;
        long seed = 1;
        WalkingRandom instance = new WalkingRandom(seed,step);
        
        double prev = instance.random();
        
        for (int i =0;i<500;i++) {
            
            double val = instance.random();
            assertTrue(Math.abs(val-prev)<=step);
            
            System.out.println(i+"\t"+val);
            prev = val;
        }
    }
}
