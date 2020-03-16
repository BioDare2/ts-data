/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import ed.robust.dom.util.Pair;
import ed.robust.util.rnd.WalkingRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialFitter;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.stat.regression.SimpleRegression;

/**
 * Utility class that copes with some of the operations on timeseries
 * @author Zielu
 */
public class TimeSeriesOperations {
    
    /**
     * Converts time series into arrays of times and values.
     * @param data input timeseries
     * @return pair of tables, left (1st) is time, right (2nd) are the values
     * @deprecated use the timeseries method instead
     */
    public static Pair<double[],double[]> extractTimeAndValueTables(TimeSeries data) {
        
        return data.getTimesAndValues();
    }
    
    
    
    /**
     * Creates time series using its array representation as table of times and values
     * @param times table with values of time for each time point
     * @param values table with values of each time point
     * @return timeseries that holds given times and values
     * @deprecated  use the timeseries constructor instead
     */
    public static TimeSeries makeTimeSeries(double[] times,double[] values) {
        return new TimeSeries(times, values);
    }
    
    /**
     * Creates list of tiempoints using its array representation as table of times and values
     * @param times table with values of time for each time point
     * @param values table with values of each time point
     * @return list of timepoints that holds given times and values
     */
    public static List<Timepoint> makeTimepoints(double[] times,double[] values) {
        if (times.length != values.length) throw new IllegalArgumentException("Times and values size must match");
        List<Timepoint> list = new ArrayList<>();
        
        for (int i = 0;i<times.length;i++) {
            list.add(new Timepoint(times[i],values[i]));
        }
        return list;
    }
    
    
    /**
     * Makes new timeseries that is sum of the two arguments == f(x) = a(x)+b(x). The arguments have to have the same time values
     * otherwise IllegalArgumentException is thrown
     * @param a 1st timeseries to add
     * @param b 2nd timeseries to add
     * @return timeseries which is f(x) = a(x)+b(x)
     */
    public static TimeSeries sum(TimeSeries a,TimeSeries b) {

        if (!haveSameTimes(a,b)) throw new IllegalArgumentException("Timesiers have to have the same time values");

        TimeSeries result = new TimeSeries();
        List<Timepoint> pointsA = a.getTimepoints();
        List<Timepoint> pointsB = b.getTimepoints();

        for (int i = 0;i<pointsA.size();i++) {
            result.add(pointsA.get(i).getTime(),pointsA.get(i).getValue()+pointsB.get(i).getValue());
        }

        return result;
    }

    /**
     * Makes new timeseries that is difference of the two arguments == f(x) = a(x)-b(x). The arguments have to have the same time values
     * otherwise IllegalArgumentException is thrown
     * @param a 1st timeseries to substract from
     * @param b 2nd timeseries to substract with
     * @return timeseries which is f(x) = a(x)-b(x)
     */
    public static TimeSeries substract(TimeSeries a,TimeSeries b) {

        if (!haveSameTimes(a,b)) throw new IllegalArgumentException("Timesiers have to have the same time values");

        TimeSeries result = new TimeSeries();
        List<Timepoint> pointsA = a.getTimepoints();
        List<Timepoint> pointsB = b.getTimepoints();

        for (int i = 0;i<pointsA.size();i++) {
            result.add(pointsA.get(i).changeValue(pointsA.get(i).getValue()-pointsB.get(i).getValue()));
        }

        return result;
    }
    
    /**
     * Makes new timeseries that is convolution of the two argumenst == f(x) = a(x)*b(x). The arguments have to have the same time values
     * otherwise IllegalArgumentException is thrown
     * @param a 1st timeseries to be multiply
     * @param b 2nd timeseries to multiply by
     * @return timeseries which is f(x) = a(x)*b(x)
     */
    public static TimeSeries convolute(TimeSeries a,TimeSeries b) {

        if (!haveSameTimes(a,b)) throw new IllegalArgumentException("Timesiers have to have the same time values");

        TimeSeries result = new TimeSeries();
        List<Timepoint> pointsA = a.getTimepoints();
        List<Timepoint> pointsB = b.getTimepoints();

        for (int i = 0;i<pointsA.size();i++) {
            result.add(pointsA.get(i).getTime(),pointsA.get(i).getValue()*pointsB.get(i).getValue());
        }

        return result;
    }
        

    /**
     * Randomly removes in average N/every points from the data. The resulting series is hence shorter but the first and last points are preserved
     * @param data to be hollowed
     * @param every how many points to be removed in average (cause it is random process), 2 means that every 2nd, 3 every 3rd... 
     * @return new timeseries that has N/every points less
     */
    public static TimeSeries randomHollow(TimeSeries data, int every) {

            if (every <= 1) throw new IllegalArgumentException("Cannot remove every "+every+" element");
            TimeSeries result = new TimeSeries();

            double p = 1.0/every;
            for (Timepoint tp  : data) {
                    if (Math.random() > p) {
                       result.add(tp.getTime(),tp.getValue());
                    }
            }
            if (!result.getFirst().equals(data.getFirst())) result.add(data.getFirst().getTime(),data.getFirst().getValue());
            if (!result.getLast().equals(data.getLast())) result.add(data.getLast().getTime(),data.getLast().getValue());
            
            return result;

    }
    
    /**
     * Removes every 'every' point from the data. The resulting series is hence shorter, but the first and last points are preserved
     * @param data to be hollowed
     * @param every every which point to be removed
     * @return new timeseries that has N/every points less
     */
    public static TimeSeries hollow(TimeSeries data,int every) {
            if (every <= 1) throw new IllegalArgumentException("Cannot remove every "+every+" element");
            TimeSeries result = new TimeSeries();
            
            List<Timepoint> points = data.getTimepoints();
            for (int i=0;i<points.size();i++) {
                if (((i+1) % every)==0) continue;
                result.add(points.get(i));
            }
        
            if (!result.getFirst().equals(data.getFirst())) result.add(data.getFirst());
            if (!result.getLast().equals(data.getLast())) result.add(data.getLast());
            
            return result;
    }
    

    /**
     * Gets the global amplitude of the data (max-min)/2
     * @param data
     * @return 
     * @deprecated  use the data method instead
     */
    public static double getAmplitute(TimeSeries data) {
        
        return data.getAmplitude();
        /*double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        
        for (Timepoint tp : data) {
            double val = tp.getValue();
            if (val < min) min = val;
            if (val > max) max = val;
        }
        return (max-min)/2.0;*/
    }
    
    /**
     * Adds random noise to the data. The noise is added in as the random number from -0.5*level*amplitude to 0.5*level*amplitude
     * @param data input data
     * @param level level as maximum fraction of amplitude to be added
     * @return new noise timeseries
     */
    public static TimeSeries addNoise(TimeSeries data, double level) {
        return addNoise(data,level,null);
    }    
    
    /**
     * Adds random noise to the data. The noise is added in as the random number from -0.5*level*amplitude to 0.5*level*amplitude
     * @param data input data
     * @param level level as maximum fraction of amplitude to be added
     * @param seed seed value to be used to initialized random numbers or null if the default system one should be used instead
     * @return new noise timeseries
     */
    public static TimeSeries addNoise(TimeSeries data, double level,Long seed) {
        
        Random random = (seed != null ? new Random(seed) : new Random());
        double range = getAmplitute(data)*level;
        TimeSeries result = new TimeSeries();
        
        
        for (Timepoint tp : data) {
            result.add(tp.getTime(),tp.getValue()+(random.nextDouble()-0.5)*range);
        }
        
        return result;
    }

    /**
     * Adds walking noise to the data. The noise is added in as the 'walking' random number from -0.5*level*amplitude to 0.5*level*amplitude.
     * Walking random number is generated using WalkingRandom, which is generator which rembers previous value and can return next one
     * only in the 'step' range from it, it let the noise to change more smoothly from 0 to 1 and back staying for longer around 'current mean'
     * @param data input data
     * @param level level as maximum fraction of amplitude to be added
     * @return new noise timeseries
     */
    public static TimeSeries addWalkingNoise(TimeSeries data, double level) {
        return addWalkingNoise(data, level, (long)(Math.random()*1000000));
    }  
    
    /**
     * Adds walking noise to the data. The noise is added in as the 'walking' random number from -0.5*level*amplitude to 0.5*level*amplitude.
     * Walking random number is generated using WalkingRandom, which is generator which rembers previous value and can return next one
     * only in the 'step' range from it, it let the noise to change more smoothly from 0 to 1 and back staying for longer around 'current mean'
     * @param data input data
     * @param level level as maximum fraction of amplitude to be added
     * @param seed seed for noise generator
     * @return new noise timeseries
     */
    public static TimeSeries addWalkingNoise(TimeSeries data, double level,long seed) {
        
        double range = getAmplitute(data)*level;
        TimeSeries result = new TimeSeries();
        WalkingRandom random = new WalkingRandom(seed,0.25);
        
        for (Timepoint tp : data) {
            result.add(tp.getTime(),tp.getValue()+(random.random()-0.5)*range);
        }
        
        return result;
    }
    
    /**
     * Checks if two timeseries have exactly the same time values (and length)
     * @param a one timesries to compare
     * @param b the other
     * @return true only if all time values are sampe in both timeseries
     */
    public static boolean haveSameTimes(TimeSeries a, TimeSeries b) {
        
        if (a.size() != b.size()) return false;
        
        List<Timepoint> pointsA = a.getTimepoints();
        List<Timepoint> pointsB = b.getTimepoints();

        for (int i = 0;i<pointsA.size();i++) {
            if (pointsA.get(i).getTime() != pointsB.get(i).getTime()) return false;
        }
        return true;
    }

    /**
     * Dampens the input data by adding expotensial decay is such a way that last point will have
     * its value equals to endFraction of original one. So the rate is determined by the length of the data and
     * endFraction number (the lower the fraction the faster the dampening)
     * @param data to be dampen
     * @param endFraction number between 0 and 1 which is equal the ration between new value of y point and original y value
     * @return 
     */
    public static TimeSeries dampen(TimeSeries data,double endFraction) {
        
        if (endFraction >= 1 || endFraction <= 0) throw new IllegalArgumentException("EndFraction must be between [(0,1)");
        
        double lastX = -Math.log(endFraction);
        
        double offset = data.getFirst().getTime();
        double range = data.getLast().getTime()-offset;
        double scale = -lastX / range;
        
        TimeSeries dump = new TimeSeries();
        
        for (Timepoint tp : data) {
            dump.add(tp.getTime(),tp.getValue()*Math.exp(scale*(tp.getTime()-offset)));
        }
        return dump;
    }
    
    
    /**
     * Adds linear trend to the data, new data will follow formula newY = y + ax +b
     * @param data to which linear tend will be added
     * @param a slope of the trend
     * @param b interception of the trend
     * @return 
     */
    public static TimeSeries addTrend(TimeSeries data,double a,double b) {
        return data.addTrend(a, b);
        /*
        TimeSeries out = new TimeSeries();
        for (Timepoint tp:data) {
            out.add(new Timepoint(tp.getTime(),tp.getValue()+tp.getTime()*a+b,tp.getStdError(),tp.getStdDev()));
        }
        return out;
        */
    }
    
    /**
     * Fits the linear trend to the data an returns its parameters
     * @param data inpute data
     * @return pair of trend parameters, first is the slope second is interception
     */
    public static Pair<Double,Double> getLinTrendParams(TimeSeries data) {
        SimpleRegression reg = new SimpleRegression();
        for (Timepoint tp: data) {
            reg.addData(tp.getTime(), tp.getValue());            
        }
        return new Pair<>(reg.getSlope(),reg.getIntercept());
    }
    
    /**
     * Performs linear detrending of the data. Finds the linear trend and substracts it from the data.
     * It does not preserve the mean value.
     * @param data input data
     * @return detrended data
     */
    public static TimeSeries lineDetrended(TimeSeries data) {
        
        Pair<Double,Double> trend = getLinTrendParams(data);
        return addTrend(data,-trend.getLeft(),-trend.getRight());

    }
    
    /**
     * Finds the mean value of the timeseries
     * @param data input data
     * @return mean value of the timeseries
     * @deprecated  use the data method instead
     */
    public static double getMeanValue(TimeSeries data) {
        return data.getMeanValue();
     }
    
    
    /**
     * Fits line to the data and returns it as the trend
     * @param data to which trend should be found
     * @return timeseries that represends the trend in the data, will have same times as original data, and values being from the
     * fitted line
     */
    public static TimeSeries getLinTrend(TimeSeries data) {
        
        Pair<Double,Double> ab = getLinTrendParams(data);
        TimeSeries trend = new TimeSeries();
        
        double a = ab.getLeft();
        double b = ab.getRight();
        
        for (Timepoint tp : data) {
            trend.add(tp.getTime(),a*tp.getTime()+b);
        }
        
        return trend;
    }
    /**
     * Finds polynomial function that best fits into the data. 
     * Implemented using apache math polynomial fitter. 
     * @param data for which polynomila function should be fitted
     * @param degree degree of polynomial, so highest power in the polynomial, for example 1 will give function y =ax+b
     * @return representation of polynomial function that best fits to the data
     */
    public static PolynomialFunction getPolyTrendFunction(TimeSeries data,int degree) {

        PolynomialFitter fitter = new PolynomialFitter(new LevenbergMarquardtOptimizer());
        
        /*
        double gueses[] = new double[degree+1];
        //for (int i = 0;i<gueses.length;i++) gueses[i] = 1;
        Pair<Double,Double> ab = getTrend(data);
        
        gueses[0] = ab.getRight();
        if (degree > 0 ) gueses[1] = ab.getLeft();
        
        CurveFitter fitter = new CurveFitter(new LevenbergMarquardtOptimizer());
        */
        
        for (Timepoint tp : data) fitter.addObservedPoint(tp.getTime(), tp.getValue());
        
        double[] coeficients = new double[degree+1];
        Arrays.fill(coeficients, 1.0);
        
        try {
            coeficients = fitter.fit(coeficients);
        } catch (Exception e) {
            //something went wrong lest do linear
            Pair<Double,Double> slopInter = getLinTrendParams(data);
            coeficients = new double[]{slopInter.getRight(),slopInter.getLeft()};
        }
        //double[] coeficients = fitter.fit(new PolynomialFunction.Parametric(),gueses);
        //System.out.println(""+Arrays.toString(coeficients));
        
        PolynomialFunction fit = new PolynomialFunction(coeficients);

        return fit;
    }
    
    /**
     * Finds trend in the data using polynomial representation of the trend with given degree.
     * @param data for which polynomial trend should be found
     * @param degree degree of polynomial, highest power in the polynomial, for example 1 will give linear trend y =ax+b
     * @return Timeseries that represents the trend in data, have the same time coordinates as original data but the values are taken
     * from the trend
     */
    public static TimeSeries getPolyTrend(TimeSeries data, int degree) {
        
        
        PolynomialFunction fit = getPolyTrendFunction(data, degree);
        
        TimeSeries trend = new TimeSeries();
        for (Timepoint tp : data) {
            double t = tp.getTime();
            trend.add(t,fit.value(t));
        }
        return trend;
    }
    
    /**
     * Creates new timeseries by adding function values at each timepoint.
     * @param data to which function values should be added
     * @param function function (of time) to be applied at each timepoint
     * @return new TiemSeries with times matching the input and values incremented by the function value
     */
    public static TimeSeries addFunction(TimeSeries data,UnivariateFunction function) {
        TimeSeries res = new TimeSeries();
        for (Timepoint tp : data) {
            double t = tp.getTime();
            res.add(tp.offset(function.value(t)));
        }
        return res;        
    }
    
    /**
     * Creates new timeseries by removing function values at each timepoint.
     * @param data to which function values should be added
     * @param function function (of time) to be applied at each timepoint
     * @return new TiemSeries with times matching the input and values decremented by the function value
     */
    public static TimeSeries substractFunction(TimeSeries data,UnivariateFunction function) {
        TimeSeries res = new TimeSeries();
        for (Timepoint tp : data) {
            double t = tp.getTime();
            res.add(tp.offset(-function.value(t)));
        }
        return res;        
    }
    
    
    /**
     * Finds the timepoints corresponding to min and max values in the timeseries.
     * If multiple points have same min or max value, there is no guarantee which one is returned,
     * but always a 'real' point is returned, so it is guaranteed that it exists in time series and its value
     * is either min or max.
     * @param data in which to find min and max
     * @return pair of timepoints, left one has value matching min value in timeseries, right holds the max value 
     */
    public static Pair<Timepoint,Timepoint> getMinMax(TimeSeries data) {
        
        return data.getMinMaxTimePoint();
        //return new Pair(data.getMinTimePoint(),data.getMaxTimePoint());

        /*
        if (data == null || data.isEmpty()) throw new IllegalArgumentException("Cannot find min and max in empty data");
        
        
        Timepoint min = new Timepoint(Double.NaN,Double.MAX_VALUE);
        Timepoint max = new Timepoint(Double.NaN,-Double.MAX_VALUE);
        

        for (Timepoint tp : data) {
            if (tp.getValue() < min.getValue()) min = tp;
            if (tp.getValue() > max.getValue()) max = tp;
        }
        
        return new Pair<Timepoint,Timepoint>(min,max);
        */
    }
    
    
    /**
     * Finds the timepoints corresponding to min and max values in the timeseries.
     * If multiple points have same min or max value, there is no guarantee which one is returned,
     * but always a 'real' point is returned, so it is guaranteed that it exists in time series and its value
     * is either min or max.
     * @param data in which to find min and max
     * @return pair of timepoints, left one has value matching min value in timeseries, right holds the max value 
     */
    public static Pair<Timepoint,Timepoint> getMinMax(List<Timepoint> data) {
        
        TimeSeries tmp = new TimeSeries(data);
        return getMinMax(tmp);
        /*
        if (data == null || data.size() < 1) throw new IllegalArgumentException("Cannot find min and max in empty data");
        
        
        Timepoint min = new Timepoint(Double.NaN,Double.MAX_VALUE);
        Timepoint max = new Timepoint(Double.NaN,-Double.MAX_VALUE);
        

        for (Timepoint tp : data) {
            if (tp.getValue() < min.getValue()) min = tp;
            if (tp.getValue() > max.getValue()) max = tp;
        }
        
        return new Pair<Timepoint,Timepoint>(min,max);
        */
    }
    
    
    
    /**
     * Looks for highest and lowest peaks (valleys) in the data. Peak (Valley) is a point which has value
     * larger (smaller for valley) than both of its neighbours. So peaks and valleys are not necessary max and min points, as min/max
     * may be at the end of time series or belong to a plateo so are not literary speaking peaks
     * @param data input data to be analysed (must have at least 3 points)
     * @return pair of values left is the highest peak right is the lowest valley found, if no peak/valley is found the corresponding
     * value is set to 0 (for example straight line will not give any peaks or valleys).
     */
    public static Pair<Timepoint,Timepoint> findHighestLowestPeaks(TimeSeries data) {
        return findHighestLowestPeaks(data.getTimepoints());
    }   
    
    
    /**
     * Looks for highest and lowest peaks (valleys) in the data. Peak (Valley) is a point which has value
     * larger (smaller for valley) than both of its neighbours. So peaks and valleys are not necessary max and min points, as min/max
     * may be at the end of time series or belong to a plateo so are not literary speaking peaks
     * @param data input data to be analysed (must have at least 3 points), must be sorted by time
     * @return pair of values left is the highest peak right is the lowest valley found, if no peak/valley is found the corresponding
     * value is set to 0 (for example straight line will not give any peaks or valleys).
     */
    static Pair<Timepoint,Timepoint> findHighestLowestPeaks(List<Timepoint> points) {
        
        Pair<List<Timepoint>,List<Timepoint>> peaksAndValleys = findPeaksAndValleys(points);
        
        List<Timepoint> peaks = peaksAndValleys.getLeft();        
        List<Timepoint> valleys = peaksAndValleys.getRight();
        
        Comparator<Timepoint> valueComparator = new TimepointValueComparator();
        Collections.sort(peaks,valueComparator);
        Collections.sort(valleys,valueComparator);
        
        
        
        Timepoint highest = peaks.isEmpty() ? null :peaks.get(peaks.size()-1);
        Timepoint lowest = valleys.isEmpty() ? null :valleys.get(0);
        
        return new Pair<>(highest,lowest);
        
    }

    /**
     * Finds all the peaks and valleys in the given timeseries. Peak (Valley) is a point which has value
     * larger (smaller for valley) than both of its neighbours, hence peak cannot be at the border of timeseries or belong to a plato
     * 
     * @param data input data to be analysed, must have at least 3 points
     * @return pair of peaks and valleys, left argument are the list of peaks, right list of valleys. It can return empty list if no peaks/valleys
     * are found
     */
    public static Pair<List<Timepoint>, List<Timepoint>> findPeaksAndValleys(TimeSeries data) {
        
        return findPeaksAndValleys(data.getTimepoints());
    }
    
    /**
     * Finds all the peaks and valleys in the given timeseries. Peak (Valley) is a point which has value
     * larger (smaller for valley) than both of its neighbours, hence peak cannot be at the border of timeseries or belong to a plato
     * 
     * @param data input data to be analysed, must have at least 3 points, must be sorted by time
     * @return pair of peaks and valleys, left argument are the list of peaks, right list of valleys. It can return empty list if no peaks/valleys
     * are found
     */
    static Pair<List<Timepoint>, List<Timepoint>> findPeaksAndValleys(List<Timepoint> points) {
        
        if (points.size() < 3) throw new IllegalArgumentException("There must be at leas 3 points in the data");
        
        List<Timepoint> peaks = new ArrayList<>();
        List<Timepoint> valleys = new ArrayList<>();
        
        
        Timepoint prev = points.get(0);
        for (int i=1;i<points.size()-1;i++) {
            
            Timepoint current = points.get(i);
            Timepoint next = points.get(i+1);
            
            double val = current.getValue();
            if (val > prev.getValue() && val > next.getValue())  {
                peaks.add(current);
            }
            if (val < prev.getValue() && val < next.getValue()) {
                valleys.add(current);
            }
            prev = current;
        }
        
        return new Pair<>(peaks,valleys);
        
    }
    
    /**
     * Cast the time of data points of the source timeseries that they will match the times of the destination timeseries by interpolating
     * values at the destinations timepoints.
     * The casting is done using spline interpolator to achieve smoothness.
     * @param source time series which values should be interpolated to match the times of the destination
     * @param destination timeseries which times should be used as the template
     * @return new timeseries which times matched the destination and values are interpolated from the source
     */
    public static TimeSeries castTime(TimeSeries source,TimeSeries destination) {
        
        if (destination.isEmpty()) return new TimeSeries();
        if (source.isEmpty()) throw new IllegalArgumentException("Cannot cast empty source time series");
        if (source.size() == 1) {
            double[] vals = new double[destination.size()];
            Arrays.fill(vals, source.getFirst().getValue());
            return new TimeSeries(destination.getTimes(),vals);
        }
        TimeSeriesInterpolator inter = new SplineTSInterpolator(source);
        TimeSeries result = new TimeSeries();
        for (Timepoint tp : destination)
            result.add(tp.getTime(), inter.getValue(tp.getTime()));
        
        return result;        
    }
    
    
}
