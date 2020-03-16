/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.util.timeseries;

import ed.robust.dom.data.Timepoint;
import java.util.Comparator;

/**
 *
 * @author tzielins
 */
public class TimepointValueComparator implements Comparator<Timepoint> {

    public TimepointValueComparator() {
    }

    @Override
    public int compare(Timepoint o1, Timepoint o2) {
        
        double v1 = o1.getValue();
        double v2 = o2.getValue();
        if (v1 > v2) return 1;
        if (v1 < v2) return -1;
        return 0;
    }
    
}
