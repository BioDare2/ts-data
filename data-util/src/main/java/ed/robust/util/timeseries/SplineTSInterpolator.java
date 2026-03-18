/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.data.Timepoint;
import java.util.List;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;
import org.apache.commons.math3.exception.OutOfRangeException;

/**
 * Smooth interpolator that is based on cubic splines implemented in apache.org math package.
 * Outside original data, there are are two brackets inserted which values same as outside points, that way
 * there will be smooth transitions between outside points and the constant (linear) values returned for data outside the original
 * data set. So for data outside the data range, the value of original data ends will be returned for very far times, for closer some
 * smoothed intermediated values will be given.
 * @author tzielins
 */
public class SplineTSInterpolator extends AbstractTSInterpolator {

    /**
     * Distance from last and first point at which a 'bracket' which be inserted to anchor interpolator at same values as outside points
     * and preserve the smoothness.
     */
    protected static double BRACKET = 100;
    UnivariateFunction function;
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each data point will become an anchor, unless
     * there are multiple values for same time in that case their average value will be used. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points as long as their are further than BRACKET from
     * the boundaries, if not, some smooth spline intermediate will be used.
     * @param data to initiate the interpolator with
     */
    public SplineTSInterpolator(TimeSeries data) {
        this(data,ROUNDING_TYPE.NO_ROUNDING);
    }

    /**
     * Creates new interpolator using the data to initialised with anchor points. Each time of timepoint will be rounded according
     * to the rounding parameter and its value averaged if necessary. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points as long as their are further than BRACKET from
     * the boundaries, if not, some smooth spline intermediate will be used.
     * @param data to initiate the interpolator with
     * @param rounding which should be applied to times before inserting data in interpolator. For example if rounding is DECY and data are
     * (0.1,1) (0.14,2) (0.18,3) (0.22,4) the interpolator will be initiated with values (0.1,1.5) (0.2, 3.5)
     */
    public SplineTSInterpolator(TimeSeries data,ROUNDING_TYPE rounding) {
        
        setData(data,rounding);
    }
    
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each time of timepoint will be rounded according
     * to the rounding parameter and its value averaged if necessary. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points as long as their are further than BRACKET from
     * the boundaries, if not, some smooth spline intermediate will be used.
     * @param data to initiate the interpolator with, has to be sorted by time
     * @param rounding which should be applied to times before inserting data in interpolator. For example if rounding is DECY and data are
     * (0.1,1) (0.14,2) (0.18,3) (0.22,4) the interpolator will be initiated with values (0.1,1.5) (0.2, 3.5)
     */
    protected SplineTSInterpolator(List<Timepoint> data,ROUNDING_TYPE rounding) {
        
        setData(data,rounding);
    }
    
    
    
    @Override
    protected void setAvgData(List<Timepoint> points) {
    
        
        double[] X = new double[points.size()+2];
        double[] Y = new double[points.size()+2];
        
        //we add extra point before beginning for smoth transition to linear regression
        X[0] = X[1]-BRACKET;
        Y[0] = Y[1];
        for (int i = 0;i<points.size();i++) {
            Timepoint tp = points.get(i);
            X[i+1] = tp.getTime();
            Y[i+1] = tp.getValue();
        }
        //we add extra point add the end for smooth transition to linear regression
        X[X.length-1] = X[X.length-2]+BRACKET;
        Y[Y.length-1] = Y[Y.length-2];
        
        UnivariateInterpolator inter = makeInterpolator();
        function = inter.interpolate(X, Y);
        
        first = new Timepoint(first.getTime(),function.value(first.getTime()));
        last = new Timepoint(last.getTime(),function.value(last.getTime()));
        
    }
    

    protected UnivariateInterpolator makeInterpolator() {
        return new SplineInterpolator();
    }
    
    @Override
    protected double interpolate(double time) {
        try {
            return function.value(time);
        } catch (OutOfRangeException e) {
            if (time < first.getTime()) return first.getValue();
            return last.getValue();
        }
    }

    
    
    
}
