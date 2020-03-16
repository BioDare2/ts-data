/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.TimeSeries;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.interpolation.UnivariateInterpolator;

/**
 * Linear interpolator (missing points are given by regression between neighbouring anchors) which is implemented using
 * apache.org LinearInterpolator. 
 * Outside original data, there are are two brackets inserted which values same as outside points, that way
 * there will be smooth transitions between outside points and the constant (linear) values returned for data outside the original
 * data set. So for data outside the data range the value of original data ends will be returned.
 * @author tzielins
 */
public class SplineLinearTSInterpolator extends SplineTSInterpolator {
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each data point will become an anchor, unless
     * there are multiple values for same time in that case their average value will be used. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points.
     * @param data to initiate the interpolator with
     */
    public SplineLinearTSInterpolator(TimeSeries data) {
        super(data);
    }
    
    /**
     * Creates new interpolator using the data to initialised with anchor points. Each time of timepoint will be rounded according
     * to the rounding parameter and its value averaged if necessary. 
     * <p>Times outside the data range, will be interpolated as the same as data boundary points.
     * @param data to initiate the interpolator with
     * @param rounding which should be applied to times before inserting data in interpolator. For example if rounding is DECY and data are
     * (0.1,1) (0.14,2) (0.18,3) (0.22,4) the interpolator will be initiated with values (0.1,1.5) (0.2, 3.5)
     */
    public SplineLinearTSInterpolator(TimeSeries data,ROUNDING_TYPE rounding) {
        super(data,rounding);
    }
    
    @Override
    protected UnivariateInterpolator makeInterpolator() {
        return new LinearInterpolator();
    }
    
    
}
