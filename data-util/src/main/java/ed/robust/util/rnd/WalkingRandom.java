/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.rnd;

import java.util.Random;

/**
 *
 * @author Zielu
 */
public class WalkingRandom {
    
    
    protected double current;
    protected double maxStep;
    protected double bounceTreshold;
    protected Random random;
    
    public WalkingRandom() {
        this(System.currentTimeMillis(),0.25);
    }
    
    public WalkingRandom(long seed,double maxStep) {
        
        if (maxStep <= 0 || maxStep >= 1) throw new IllegalArgumentException("Step must be between 0 and 1");
        
        this.maxStep = maxStep*2;
        this.random = new Random(seed);
        this.current = random.nextDouble();
        
        this.bounceTreshold = 0.3;
    }
    
    public double random() {
        
        double step = (random.nextDouble()-0.5)*maxStep;
        current+=step;
        if (current >= 0 && current < 1) return current;
        
        boolean bounce = (random.nextDouble() > bounceTreshold);
        
        if (bounce) {
            if (current < 0) current = Math.abs(current);
            else current = 1 - (current % 1.0);
        } else {
            if (current < 0) current = 0;
            else current = 1-1e-12;
        }
        
        return current;
    }
}
