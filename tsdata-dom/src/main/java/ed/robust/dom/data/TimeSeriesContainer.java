/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author tzielins
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class TimeSeriesContainer implements Iterable<TimeSeries> {
    
    @XmlElement(name="ser")
    private List<TimeSeries> series;
    
    public TimeSeriesContainer() {
        series = new ArrayList<>();
    }
    
    public TimeSeriesContainer(List<TimeSeries> ser) {
        series = ser;        
    }
    
    public List<TimeSeries> getSeries() {
        return series;
    }

    @Override
    public Iterator<TimeSeries> iterator() {
        return series.iterator();
    }
    
}
