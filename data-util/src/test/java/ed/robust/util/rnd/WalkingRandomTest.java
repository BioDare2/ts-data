/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.rnd;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Zielu
 */
public class WalkingRandomTest {
    
    public WalkingRandomTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of random method, of class WalkingRandom.
     */
    @Test
    public void testRandom() {
        System.out.println("random");
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
