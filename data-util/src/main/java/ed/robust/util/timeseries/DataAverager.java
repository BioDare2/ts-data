/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

/**
 * Simple utility class that averages values
 * @author tzielins
 */
public class DataAverager {
    
    protected double sum = 0;
    protected int N = 0;
        
    /** 
     * Adds new input value to the averager
     * @param val new value to be added
     */
    public void add(double val) {
        sum+=val;
        N++;
    }
        
    /**
     * Gets averaged value based on the added numbers
     * @return 
     */
    public double getMean() {
        if (N == 0) throw new IllegalArgumentException("Cannot average empty data");
        return sum/N;
    }
    
    /**
     * Gets number of values which have been added to the averager
     * @return 
     */
    public int getN() {
        return N;        
    }
    
    /**
     * Checks if averager had some values inserted
     * @return true if no value was added to averager hence no mean can be calculated
     */
    public boolean isEmpty() {
        return N<= 0;
    }
    
}
