/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Own implementation of linear interpolator. It stores all the anchors used to initiate it and during interpolation
 * finds the lower and higher neighbours and calculate the right value for the data between them using linear regression.
 * Outside original data values of the boundaries are returned. 
 * <p>Interpolation for time values which matches times in original data are guaranteed to return the original value for that point as long
 * as no averaging was performed because of rounding of times duplicates. For that reason this class is better than SplineLinearTSInteroplator
 * cause the original values are preserved, it is also a bit faster when creating whole timeseries interpolation as it access the points
 * in consecutive way and no insertion point must be searched.
 * @author Zielu
 */
public class BinningLinearTSInterpolator extends AbstractTSInterpolator {

     
    
    /**
     * Interpolator anchors.
     */
    protected List<Timepoint> data;
    /**
     * Index of the last right anchor, in case of consecutive interpolate request this index or the next one should be hit without need
     * for searching of the insertion point.
     */
    protected int currentIndex = 1;
    
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each data point will become an anchor, unless
     * there are multiple values for same time in that case their average value will be used. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points.
     * @param data to initiate the interpolator with
     */    
    public BinningLinearTSInterpolator(TimeSeries data)
    {
        this(data,ROUNDING_TYPE.NO_ROUNDING);
    }
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each time of timepoint will be rounded according
     * to the rounding parameter and its value averaged if necessary. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points.
     * @param data to initiate the interpolator with
     * @param rounding which should be applied to times before inserting data in interpolator. For example if rounding is DECY and data are
     * (0.1,1) (0.14,2) (0.18,3) (0.22,4) the interpolator will be initiated with values (0.1,1.5) (0.2, 3.5)
     */
    public BinningLinearTSInterpolator(TimeSeries data,ROUNDING_TYPE rounding) {
        setData(data,rounding);
        
    }
    
    
    
    @Override
   protected void setAvgData(List<Timepoint> dataPoints) {
       
        
        
        
        Timepoint minInf = new Timepoint(-Double.MAX_VALUE,first.getValue());
        Timepoint maxInf = new Timepoint(Double.MAX_VALUE,last.getValue());
        
        data = new ArrayList<>(dataPoints.size()+2);
        data.add(minInf);
        data.addAll(dataPoints);
        data.add(maxInf);
        
        
    }
   
   
   
    @Override
    protected double interpolate(double time) {
        
        int index = findIndex(time);
        Timepoint tp = data.get(index);
        //System.out.println("Index: "+index+" time: "+time+" tp: "+tp.getTime());
        if (tp.getTime() == time) return tp.getValue();
        
        return interpolate(time,data.get(index-1),tp);
    }
    
    
    /**
     * Linear interpolation using the lower(left) and higher(right) boundaries and their values.
     * @param t time for which new value should be calculated
     * @param left values for the left point
     * @param right values for the right point
     * @return  value for the line going through left and right
     */
    public static double interpolate(double t, Timepoint left, Timepoint right) {
        
        double val = left.getValue()+(t-left.getTime())*(right.getValue()-left.getValue())/(right.getTime()-left.getTime());
        return val;
    }
    
    /**
     * Finds index of the anchor which is &gt;= requested time. Implementation cashes the previous hit, so if the 
     * request are monotonious it checks that value and the next one which should be enough to not perform a search.
     * @param t time for which find a right boundary
     * @return index of such element that data.get(i).time() &gt=t and data.get(i-1).time() &lt; t
     */
    protected int findIndex(double t) {
        
        //try current index most likely we are here or at the next one
        Timepoint cT = data.get(currentIndex);
        if (cT.getTime() >= t && data.get(currentIndex -1).getTime() < t) return currentIndex;
        
        //try next
        if (cT.getTime() < t) {
            currentIndex++;
        
            cT = data.get(currentIndex);
            if (cT.getTime() >= t && data.get(currentIndex -1).getTime() < t) return currentIndex;        
        }
        
        //System.out.println("Binnary search");
        Timepoint tp = new Timepoint(t, 0);
        
        int index = Collections.binarySearch(data, tp);
        if (index < 0) index = -index-1;
        
        currentIndex = index;
        return index;
    }
    
    
    
}
