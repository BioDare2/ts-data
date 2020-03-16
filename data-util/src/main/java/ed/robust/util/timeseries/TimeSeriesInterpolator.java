/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.Timepoint;
import java.util.List;

/**
 * Interface for TimeSeries data interpolator. Once instance is initialised it can produce whole time series (in the range of the original timeseries)
 * or gives values for particular time points. Interpolator can produce values in whole time range (-infinity,+infinity) despide the data
 * which were used to initialise them.
 * @author tzielins
 */
public interface TimeSeriesInterpolator {
   
    
   /**
    * Creates new time series which starts at original timeseries 'first' point and last up to original 'last' point 
    * (exclusive if step size so dictates) with data points generated every 'step'. 
    * @param step distance between consecutive points in hours
    * @return interpolated timeseries as list of timepoints in monotonic order of time values
    */ 
   public List<Timepoint> makeInterpolation(double step);
   
   /**
    * Creates new time series which starts at original timeseries 'first' point and last up to no more than original 'last' point 
    * (exclusive if step size so dictates) with data points generated every 'step' hours. Times values in output timeseries
    * can be rounded according to xOutputRound to keep timepoint in 'crisp' accuracy despite double precession and rounding. For example
    * step 0.1 it can happen that time values will be 0, 0.1, 0.19999999999, 0.3 rounding to DECY will make sure that times are 0,0.1,0.2,0.3.
    * @param step distance between consecutive points in hours
    * last point fulfils t&lt;last and (t+step)&gt;last
    * @param xOutputRound rounding which will be applied to the output time values to assure they crispness (exact multiplication of step values)
    * This rounding must be wider than precession of step (so that the output data points will not converge after rounding and will not produce duplicates 
    * in time domain. If not an IllegalArgumenException is thrown.
    * @return interpolated timeseries as list of timepoints in monotonic order of time values
    * @throws IllegalArgumentException if xOutputRounding affects the step (step has higher precession than rounding)
    */
   public List<Timepoint> makeInterpolation(double step,ROUNDING_TYPE xOutputRound);
   
   /**
    * Creates new time series which starts at 'first' point and last up to no more than 'last' point 
    * (exclusive if step size so dictates) with data points generated every 'step' hours. Times values in output timeseries
    * can be rounded according to xOutputRound to keep timepoint in 'crisp' accuracy despite double precession and rounding. For example
    * step 0.1 it can happen that time values will be 0, 0.1, 0.19999999999, 0.3 rounding to DECY will make sure that times are 0,0.1,0.2,0.3.
    * @param step distance between consecutive points in hours
    * @param first time value of first data point in hours
    * @param last upper boundary for timeseries in hours, this boundary typically is exclusive depending on the step value, time value t of
    * last point fulfils t&lt;=last and (t+step)&gt;last
    * @param xOutputRound rounding which will be applied to the output time values to assure they crispness (exact multiplication of step values)
    * This rounding must be wider than precession of step (so that the output data points will not converge after rounding and will not produce duplicates 
    * in time domain. If not an IllegalArgumenException is thrown.
    * @return interpolated timeseries as list of timepoints in monotonic order of time values
    * @throws IllegalArgumentException if xOutputRounding affects the step (step has higher precession than rounding)
    */
   public List<Timepoint> makeInterpolation(double step,double first,double last,ROUNDING_TYPE xOutputRound);
   
   /**
    * Interpolate value at the given time.
    * @param time to interpolate value for
    * @return interpolated value
    */
   public double getValue(double time); 
   
   /**
    * Returns interpolated time point for the given time.
    * @param time to interpolate value for in hours
    * @return interpolated timepoint 
    */
   public Timepoint getTimePoint(double time);
   
   /**
    * Gives first timepoint that matches the beginning of underlying original timeseries which was used to create interpolator. 
    * Method is used to find the range of original data as the interpolator covers the whole double range. 
    * The time value of this timepoint marks the beginning of the real data, the value will match the interpolated value for that time 
    * (it may be different than original depending on implementation). 
    * @return left boundary of original timeseries, which value is interpolated
    */
   public Timepoint getFirst();
   
   /**
    * Gives last timepoint that matches the end of underlying original timeseries which was used to create interpolator. 
    * Method is used to find the range of original data as the interpolator covers the whole double range. 
    * The time value of this timepoint marks the end of the real data (last data point), 
    * the value will match the interpolated value for that time (which may be different than original depending on implementation). 
    * @return right boundary of original timeseries, which value is interpolated
    */
   public Timepoint getLast();
   
   /**
    * Finds the step in the given options which matches the best average step in underlying anchores used to create the interpolator. 
    * Idea of this method is to get such interpolation step which will require the least interpolation, ie will
    * be as close as to real data as possible given available step options. 
    * @param options list of potential step (distance between points) to choose from
    * @return selected value from option list which is the closest to original data step
    */
   public double chooseBestStep(List<Double> options);
    
   /**
    * Gives averaged distance between data points which forms the 'core' of this interpolation.
    * The step can be affected by rounding during interpolator initiation (for example some data points may be averaged before
    * used in interpolation, so the average step in interpolator can be different than in original data, 
    * @return average distance between the 'anchors' of this interpolator
    */
   //public double getAverageStep();
   
   /**
    * Gives optimal distance between data points that forms the 'core' of this interpolation.
    * The step is chosen to match closely the original data steps, so for evenly spaced data it will be the exact step, for not
    * equally spaced if there is one dominant data step (for example only few data points are missing from originally equally spaced
    * data) this dominant step will be returned, otherwise the average step is given.
    * <P>The step is only with CENTY resolution!!!!!
    * @return optimal step with CENTY resolution.
    */
   public double getOptimalStep();
}
