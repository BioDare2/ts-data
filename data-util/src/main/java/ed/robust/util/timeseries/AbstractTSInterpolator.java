/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of core methods for TimeSeriesInterpolator. 
 * The class handles timeseries with multiple entries for same time, or with entries that can become multiple after rounding.
 * @author tzielins
 */
public abstract class AbstractTSInterpolator implements TimeSeriesInterpolator, DataSource {

    protected Timepoint first;
    protected Timepoint last;
    //protected double avgStep;
    protected double optimalStep;
    
    @Override
    public List<Timepoint> makeInterpolation(double step) {
       return makeInterpolation(step, ROUNDING_TYPE.NO_ROUNDING);
    }

    @Override
    public List<Timepoint> makeInterpolation(double step, ROUNDING_TYPE xOutputRound) {
        return makeInterpolation(step,first.getTime(),last.getTime(),xOutputRound);
    }
    
    @Override
    public List<Timepoint> makeInterpolation(double step,double first,double last, ROUNDING_TYPE xOutputRound) {
       
       if (step == 0) throw new IllegalArgumentException("Step must be > 0, got: "+step);
       if (DataRounder.round(step,xOutputRound) != step) throw new IllegalArgumentException("Output roudning must be wider than presission of the step");
       
       List<Timepoint> interpol = new ArrayList<>();
       
       double fin = last;
       
       double beg = first;

       int i = 0;
       while (true) {
           double time = DataRounder.round(beg+(step*i++),xOutputRound);
           if (time > fin) break;
           
           double value = interpolate(time);
           
           interpol.add(new Timepoint(time,value));
       }
       
       return interpol;
        
    }

    @Override
    public double getValue(double time) {
        return interpolate(time);
    }

    @Override
    public Timepoint getTimePoint(double time) {
        return new Timepoint(time,getValue(time));
    }
    
    

    @Override
    public Timepoint getFirst() {
        return first;
    }

    @Override
    public Timepoint getLast() {
        return last;
    }
    
    @Override
    public double getFirstTime() {
        return getFirst().getTime();
    }
    
    @Override
    public double getLastTime() {
        return getLast().getTime();
    }
    
    
    @Override
    public List<Timepoint> getTimepoints(double start,double end,double step,ROUNDING_TYPE x_rounding) {        
        return makeInterpolation(step, start, end, x_rounding);
    }
        
    @Override
    public List<Timepoint> getTimepoints(double step,ROUNDING_TYPE x_rounding) {        
        return getTimepoints(getFirstTime(),getLastTime(),step, x_rounding);
    }
    
    @Override
    public Pair<double[],double[]> getTimesAndValues(double step,ROUNDING_TYPE x_rounding) {
        return getTimesAndValues(getFirstTime(),getLastTime(),step, x_rounding);
    }
    
    @Override
    public Pair<double[],double[]> getTimesAndValues(double start,double end,double step,ROUNDING_TYPE x_rounding) {
        
        List<Timepoint> timepoints = getTimepoints(start, end, step, x_rounding);
        double[] times = new double[timepoints.size()];
        double[] values = new double[timepoints.size()];
        
        for (int i =0;i<timepoints.size();i++) {
            times[i] = timepoints.get(i).getTime();
            values[i] = timepoints.get(i).getValue();
        }
        
        return new Pair<>(times,values);
    }
    

    /**
     * Performs actual interpolation, should be implemented in contract subclass.
     * @param time for which to interpolate the value
     * @return value of interpolation
     */
    protected abstract double interpolate(double time);
    
    /**
     * Performs rounding using the DataRounder implementation. Convenience access method that hides presence of DataRounder class.
     * @param val value to be rounded
     * @param rounding rounding type
     * @return rounded value
     * @see DataRounder
     */
    public static double round(double val,ROUNDING_TYPE rounding) {
        
        return DataRounder.round(val, rounding);
       
    }
    
    
    /**
     * Used to initiate the interpolator with given data and potential rounding. 
     * The input data have their times rounded according to rounding parameter, and if necessary the values for same 'rounded' times
     * are averaged and used to initiate the interpolators, ie the resulting interpolator may be anchored on less data points than original data
     * if the rounding was narrower than step between points. It calls abstract setData(list) which takes care of actual interpolation
     * @param source timeseries with input data, repetitions of times are allowed
     * @param rounding rounding which should be applied to time values before inserting into interpolator
     */
    protected void setData(TimeSeries source,ROUNDING_TYPE rounding) {
       
        if (source == null || source.isEmpty()) throw new IllegalArgumentException("Source data cannot be empty");
        setData(source.getTimepoints(),rounding);
    }
    
    /**
     * Used to initiate the interpolator with given data and potential rounding. 
     * The input data have their times rounded according to rounding parameter, and if necessary the values for same 'rounded' times
     * are averaged and used to initiate the interpolators, ie the resulting interpolator may be anchored on less data points than original data
     * if the rounding was narrower than step between points. It calls abstract setData(list) which takes care of actual interpolation
     * @param source timepoints with input data, repetitions of times are allowed but timepoints must be sorted acording to time
     * @param rounding rounding which should be applied to time values before inserting into interpolator
     */
    protected void setData(List<Timepoint> source,ROUNDING_TYPE rounding) {
    
        LinkedHashMap<Double,DataAverager> wells = new LinkedHashMap<>();
        
        for (Timepoint tp: source) {
            double t = round(tp.getTime(),rounding);
            DataAverager avg = wells.get(t);
            if (avg == null) {
                avg = new DataAverager();
                wells.put(t, avg);
            }
            avg.add(tp.getValue());
        }
         //if (Thread.interrupted())
         //   throw new InterruptedException(Thread.currentThread().hashCode() + " have been interupted during building interpolator, setData");
        
        List<Timepoint> dataPoints = new ArrayList<>();
        double[] times = new double[wells.size()];
        int tIx = 0;
        for (Map.Entry<Double,DataAverager> entry : wells.entrySet()) {
            dataPoints.add(new Timepoint(entry.getKey(),entry.getValue().getMean()));
            times[tIx++]=(entry.getKey());
        }
        
        first = dataPoints.get(0);
        last = dataPoints.get(dataPoints.size()-1);

        //System.out.println(-Double.MAX_VALUE+100);
        //dataPoints.add(0,new Timepoint(-Double.MAX_VALUE/2,first.getValue()));
        //dataPoints.add(new Timepoint(Double.MAX_VALUE/2,last.getValue()));
        
        //avgStep = round((last.getTime()-first.getTime())/(dataPoints.size()-1),rounding);
        optimalStep = new TimeStepFinder().findTimeStep(times);
        setAvgData(dataPoints);
    }

    /**
     * Performs actual interpolation using provided dataPoints. This method is called by setData, so times rounding and potential
     * averaging should be already done, and actuall implementation should only make sure that underlying interpolation objects are initiated
     * properly. 
     * @param dataPoints monotonic list timepoints which will be anchors for the interpolator, no repetitions of times are allowed
     */
    protected abstract void setAvgData(List<Timepoint> dataPoints);
    
    @Override
    public double chooseBestStep(List<Double> options) {
        if (options == null || options.isEmpty()) throw new IllegalArgumentException("Step options cannot be empty");
        
        double min = Double.MAX_VALUE;
        double best = Double.NaN;
        
        for (Double option : options) {
            //double dif = Math.abs(option-avgStep);
            double dif = Math.abs(option-optimalStep);
            if (dif < min) {
                best = option;
                min = dif;
            }
        }
        return best;
    }
    
    /*@Override
    public double getAverageStep() {
    return avgStep;
    }*/
    
    @Override
    public double getOptimalStep() {
        return optimalStep;
    }
    
    
}
