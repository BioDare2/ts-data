/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.dom.data;

import ed.robust.dom.util.Pair;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.*;
import java.util.stream.Stream;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * TimeSeries representation that can be serialized to xml in a compact way (for WS interactions)
 * and has some convenience methods.
 * <p>The contained timepoints are sorted according to their time values, so iteration over the timeseries or accessing
 * all the time points always gives that right order of points.  Duplicates of times are allowed, order of duplicates is undefined.
 * <p>For increased performance, the timeseries contain sorted flag which marks if the underlying timepoints have been sorted or not.
 * Any read-like operations tries to sort the points first, but sorting is only performed if the flag is not set. Any write-like operation
 * resets the sort flag to false.
 * <p>This is implementation IS NOT thread safe.
 * @author tzielins
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class TimeSeries implements Iterable<Timepoint>, Externalizable {
    
    private static final long serialVersionUID = -6018167069621000247L;
    

    @XmlElement(name="t")
    private List<Timepoint> elements;
	
    /**
     * Flags that marks if the current content of the timeseries has been sorted. All the write operations must set this flag to false
     */
    @XmlTransient
    private boolean sorted;
    
    @XmlTransient
    private Double sumValue = null;
    
    //@XmlTransient
    //private Double maxValue;
    
    @XmlTransient
    private Timepoint maxTimePoint = null;
    
    //@XmlTransient
    //private Double minValue;
    
    @XmlTransient
    private Timepoint minTimePoint = null;
    
    /**
     * Creates empty timeseries
     */
    public TimeSeries() {
        elements = new ArrayList<>();                
        sorted = true;
    }
    
    /**
     * Creates timeseries that contains all the data points from the given list.
     * @param points list of points that should be added (does not have to be sorted)
     */
    public TimeSeries(Collection<? extends Timepoint> points) {
    	this();
    	addAll(points);
    }
    
    
    
    /**
     * Creates timeseries that contains exatly the same time points as the template. 
     * The timepoints are not duplicated.
     * @param template timeseries which values will be added to the new object
     */
    public TimeSeries(TimeSeries template) {
    	this(template.elements);
        this.sorted = template.sorted;
    }
    
    public TimeSeries(double[] times,double values[]) {
        this();
        if (times.length != values.length) throw new IllegalArgumentException("Times and values must have the same length");
        for (int i = 0;i<times.length;i++) {
            add(new Timepoint(times[i],values[i]));
        }
    }

    /**
     * Sorted value is set to false after unmarshalling as we cannot "trust" the sended order.
     * @param umar
     * @param parent 
     */
    void afterUnmarshal(Unmarshaller umar, Object parent) {
        if (!elements.isEmpty()) sorted = false; 
    }
    
    
    /**
     * Returns all the timepoints (sorted) that belongs to this timeseries. 
     * The list is read only.
     * @return 
     */
    public List<Timepoint> getTimepoints() {
        sort();
        return Collections.unmodifiableList(elements);
    }
    
    
    /**
     * Gets first time point in the series
     * @return
     * @throws IllegalArgumentException if list is empty
     */
    public Timepoint getFirst() {
    	if (elements.isEmpty() ) throw new IllegalArgumentException("Cannot get element from empty timeseries");
    	sort();
    	return elements.get(0);
    }
    
    /**
     * Gets last time point in the series
     * @return
     * @throws IllegalArgumentException if list is empty
     */
    public Timepoint getLast() {
    	if (elements.isEmpty() ) throw new IllegalArgumentException("Cannot get element from empty timeseries");
    	sort();
    	return elements.get(elements.size()-1);
    }
    
    public double getMeanValue() {
        if (isEmpty()) throw new IllegalArgumentException("No mean value for empty TS");
        if (sumValue == null) findMMM();
        return sumValue / elements.size();
    }
    
    public Timepoint getMaxTimePoint() {
        if (isEmpty()) throw new IllegalArgumentException("No max value for empty TS");
        if (maxTimePoint == null) findMMM();
        return maxTimePoint;
    }
    
    public Pair<Timepoint,Timepoint> getMinMaxTimePoint() {
        return new Pair<>(getMinTimePoint(),getMaxTimePoint());
    }
    
    public double getMaxValue() {
        /*if (isEmpty()) throw new IllegalArgumentException("No max value for empty TS");
        if (maxValue == null) findMMM();
        return maxValue;
        */
        return getMaxTimePoint().getValue();
    }
    
    public double getAmplitude() {
        return (getMaxValue()-getMinValue())/2;
    }
    
    
    public Timepoint getMinTimePoint() {
        if (isEmpty()) throw new IllegalArgumentException("No max value for empty TS");
        if (minTimePoint == null) findMMM();
        return minTimePoint;
    }
    
    public double getMinValue() {
        /*if (isEmpty()) throw new IllegalArgumentException("No min value for empty TS");
        if (minValue == null) findMMM();
        return minValue;*/
        return getMinTimePoint().getValue();
    }
    
    protected void findMMM() {
        if (isEmpty()) return;
        double sum = 0;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        
        Timepoint maxT = null;
        Timepoint minT = null;
        for (Timepoint tp : elements) {
            double v = tp.getValue();
            sum+=v;
            if (v < min) {
                min = v;
                minT = tp;
            }
            if (v > max) {
                max = v;
                maxT = tp;
            }
        }
        sumValue = sum;
        //maxValue = max;
        //minValue = min;
        maxTimePoint = maxT;
        minTimePoint = minT;
    }
    

    /**
     * Creates new time series which will contain only those points of original one which times are inside the given boundaries (inclusive)
     * @param min lowest boundary for time in result timeseries (inclusive)
     * @param max highest boundary for time in result timeseries (inclusive)
     * @return new TimeSeries with data points with time in [min,max]
     */
    public TimeSeries subSeries(double min,double max) {
    	if (min > max) throw new IllegalArgumentException("min must be <= max");
    	
    	List<Timepoint> out = new ArrayList<>();
    	for (Timepoint tp : elements) {
    		if (tp.getTime() >= min && tp.getTime() <= max) out.add(tp);
    	}
        TimeSeries res = new TimeSeries();
        res.elements = out;
        res.sorted = this.sorted;
    	return res;
    }

    /**
     * Sets the content of the timeseris with the given timepoints. The existing points (if any) are discarded.
     * The timepoints are not duplicate but the list is.  
     * @param timepoints list of points that the timeseries will contain. Do not have to be sorted. Cannot contain nulls
     */
    public void setTimepoints(List<Timepoint> timepoints) {
    	if (timepoints == null) throw new IllegalArgumentException("List of tiempoints cannot be null");
    	if (timepoints.contains(null)) throw new IllegalArgumentException("List of tiempoints cannot contain null timepoints");
        this.elements = new ArrayList<>(timepoints);
        sorted = false;
        sumValue = null;
        minTimePoint = null;
        maxTimePoint = null;
        
    }
    
    /**
     * Adds the timepoint to the series. It will be inserted in the 'right location'
     * @param t timepoint to be added
     */
    public void add(Timepoint t) {
    	if (t == null) throw new IllegalArgumentException("Null timepoint cannot be added");
        if (sorted && !elements.isEmpty()) {
            sorted = elements.get(elements.size()-1).getTime() <= t.getTime();
        }
        elements.add(t);
        updateMMM(t);
    }
    
    protected void updateMMM(Timepoint t) {
        if (sumValue == null || minTimePoint == null || maxTimePoint == null) findMMM();
        else {
            double minValue = minTimePoint.getValue();
            double maxValue = maxTimePoint.getValue();
            
            double v = t.getValue();
            sumValue+=v;
            if (v <minValue) {
                //minValue = v;
                minTimePoint = t;
            }
            if (v > maxValue) {
                //maxValue = v;
                maxTimePoint = t;
            }
        }
        
    }
    
    /**
     * Adds new time point of the given time and value to the series. It will be inserted in the 'right location'
     * @param time time of the time point
     * @param value value of the timepoint
     */
    public void add(double time,double value) {
        add(new Timepoint(time,value));
    }
    
    /**
     * Adds all the timepoints int the given collection to the series. They will be inserted in the 'right location'.
     * @param all collection of timepoints, do not have to be sorted, can contain duplicates of times, cannot contains null
     */
    public void addAll(Collection<? extends Timepoint> all) {
    	if (all.contains(null)) throw new IllegalArgumentException("Collection of tiempoints cannot contain null timepoints");
        elements.addAll(all);
        sorted = false;
        findMMM();
    }
    
    /**
     * Sorts the underlying list of timepoints according to the tiem value. For increased performance it marks timeseries as sorted
     * so next call do not do unnecessary sorting. All the methods that modifies content must reset sroted flag to false.
     */
    protected void sort() {
        if (sorted) return;
        Collections.sort(this.elements);
        sorted = true;
    }
    
    /**
     * Returs size of the timeseries (number of time points)
     * @return number of time points in the series
     */
    public int size() {
        return elements.size();
    }
    
    
    /**
     * Returns true if timeseries does not contain any points.
     * @return
     */
    public boolean isEmpty() {
    	return elements.isEmpty();
    }

    public Stream<Timepoint> stream() {
        sort();
        return elements.stream();
    }
    
    @Override
    public Iterator<Timepoint> iterator() {
        sort();
        return elements.iterator();
    }
    
    /**
     * Calculate the duration of the whole timeseries, ie difference between last and first time point.
     * @return time span between last and first time points or 0 if timeseries is empty.
     */
    public double getDuration() {
    	if (elements.isEmpty()) return 0;
    	return (getLast().getTime()-getFirst().getTime());
    }
    
    /**
     * Calculates the average time step between the time points in this series. 
     * It is calculated using duration and number of points belonging to the time series. 
     * @return average time difference between the subsequent time points.
     */
    public double getAverageStep() {
        if (size() < 2) return Double.NaN;
    	return getDuration()/(elements.size()-1);
    }
    
    
    public TimeSeries scale(double factor, double offset) {
        
        //if (start > end) throw new IllegalArgumentException("star must be <= end, got "+start+":"+end);
        if (factor == 0) throw new IllegalArgumentException("factor must be != 0");
        
        List<Timepoint> scaled = new ArrayList<>();
        for (Timepoint tp : elements) {
            //if (tp.getTime() < start || tp.getTime() > end) continue;
            scaled.add(tp.scale(factor,offset));
        }
        
        TimeSeries out = new TimeSeries();
        out.elements = scaled;
        out.sorted = this.sorted;
        if (this.sumValue != null) out.sumValue = this.sumValue/factor+offset;
        if (this.minTimePoint != null) out.minTimePoint = this.minTimePoint.scale(factor, offset);
        if (this.maxTimePoint != null) out.maxTimePoint = this.maxTimePoint.scale(factor, offset);
        
        return out;
    }
    
    public TimeSeries offsetTime(double offset) {
        
        List<Timepoint> changed = new ArrayList<>();
        for (Timepoint tp : elements) {
            //if (tp.getTime() < start || tp.getTime() > end) continue;
            changed.add(tp.changeTime(tp.getTime()+offset));
        }  
        
        TimeSeries out = new TimeSeries();
        out.elements = changed;
        out.sorted = this.sorted;
        return out;        
    }
    
    public TimeSeries addTrend(double slope,double intercept) {

        List<Timepoint> scaled = new ArrayList<>();
        for (Timepoint tp : elements) {
            double offset = slope*tp.getTime()+intercept;
            scaled.add(tp.offset(offset));
        }
        
        TimeSeries out = new TimeSeries();
        out.elements = scaled;
        out.sorted = this.sorted;
        return out;
    }
    
    
    public TimeSeries factorise(double factor) {
        return scale(factor,0);

        /*
        if (factor == 0) throw new IllegalArgumentException("Dividing factor must be != 0");
        
        List<Timepoint> list = new ArrayList<>();
        for (Timepoint tp : elements) list.add(tp.factorise(factor));
        
        TimeSeries out = new TimeSeries(list);
        out.sorted = this.sorted;
        
        return out;
        */ 
    }
    
    
    
    public TimeSeries offset(double offset) {
        return scale(1,offset);
        
        /*
        List<Timepoint> list = new ArrayList<>();
        for (Timepoint tp : elements) list.add(tp.offset(offset));
        
        TimeSeries out = new TimeSeries(list);
        out.sorted = this.sorted;
        
        return out;
        */ 
    }
    
    
    
    /**
     * Checks if two time series contains same timepoint within given precission. That way two time series may be treated equals
     * even if time points differ because of for example rounding errors. 
     * To be almostEquals the other time series must have the same length, and all the time points must have
     * matching time and values withing given precission.
     * @param other timeseries to compare with
     * @param precision precision to which compare the time and value fields, for example times 0.13 and 0.17 are equal for the precission 0.1
     * and not equal for precision 0.01
     * @return true if all the timepoints in both series matches themselve withing given precission
     */
    public boolean almostEquals(TimeSeries other,double precision) {
    	if (other == null) return false;
    	if (size() != other.size()) return false;
    	
    	sort();
    	other.sort();
    	
    	for (int i = 0; i<elements.size();i++) {
    		if (!elements.get(i).almostEquals(other.elements.get(i), precision)) return false;
    	}
    	
    	return true;
    	
    }
    
    /**
     * Checks if two timeseries have same time values in their points
     * @param other timeseries to compare with
     * @return true if timepoints in both series have exactly same times
     */
    public boolean hasSameTimes(TimeSeries other) {
        if (other == null) return false;
        if (this.size() != other.size()) return false;
        
    	sort();
    	other.sort();

    	for (int i = 0; i<elements.size();i++) {
    		if (elements.get(i).getTime() != other.elements.get(i).getTime()) return false;
    	}
    	
    	return true;        
    }
    
    /**
     * Checks if two timeseries have same time values in their points
     * @param other timeseries to compare with
     * @param precision precision to which compare the time and value fields, 
     * for example times 0.13 and 0.17 are equal for the precission 0.1
     * and not equal for precision 0.01
     * @return true if timepoints in both series have same times within given precission
     */
    public boolean hasSameTimes(TimeSeries other, double precision) {
        if (precision < 0) throw new IllegalArgumentException("Precission must be >=0 ");
        
        if (other == null) return false;
        if (this.size() != other.size()) return false;
        
    	sort();
    	other.sort();

    	for (int i = 0; i<elements.size();i++) {
            final double diff = elements.get(i).getTime() - other.elements.get(i).getTime();
            if (Math.abs(diff) > precision) return false;
    	}
    	
    	return true;        
    }    
    
    public Pair<double[],double[]> getTimesAndValues() {
        return getTimesAndValuesArr(true,true);
    }

    public Pair<List<Double>,List<Double>> getTimesAndValuesLists() {
        return getTimesAndValuesLists(true,true);
    }
    
    public double[] getTimes() {
        return getTimesAndValuesArr(true,false).getLeft();
    }
    
    public double[] getValues() {
        return getTimesAndValuesArr(false,true).getRight();
    }
    
    public List<Double> getTimesList() {
        return getTimesAndValuesLists(true,false).getLeft();
    }
    
    public List<Double> getValuesList() {
        return getTimesAndValuesLists(false,true).getRight();
    }
    
    private Pair<double[], double[]> getTimesAndValuesArr(boolean getTimes, boolean getValues) {
        if (!getTimes && !getValues) throw new IllegalArgumentException("You need either times or values to extracted");
        
        sort();
        double[] times = (getTimes ? new double[elements.size()] : new double[0]);
        double[] values = (getValues ? new double[elements.size()] : new double[0]);
        
        for (int i = 0;i<elements.size();i++) {
            Timepoint tp = elements.get(i);
            if (getTimes) times[i] = tp.getTime();
            if (getValues) values[i] = tp.getValue();
        }
        
        return new Pair<>(times,values);
    }
    
    @SuppressWarnings("unchecked")
    private Pair<List<Double>, List<Double>> getTimesAndValuesLists(boolean getTimes, boolean getValues) {
        if (!getTimes && !getValues) throw new IllegalArgumentException("You need either times or values to extracted");
        
        sort();
        List<Double> times = (getTimes ? new ArrayList<>(elements.size()) : Collections.EMPTY_LIST);
        List<Double> values = (getValues ? new ArrayList<>(elements.size()) : Collections.EMPTY_LIST);
        
        for (int i = 0;i<elements.size();i++) {
            Timepoint tp = elements.get(i);
            if (getTimes) times.add(tp.getTime());
            if (getValues) values.add(tp.getValue());
        }
        
        return new Pair<>(times,values);
    }
    
    

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TimeSeries)) return false;
        TimeSeries t = (TimeSeries)obj;
        
        if (size() != t.size()) return false;
        this.sort();
        t.sort();
        return this.elements.equals(t.elements);
    }

    @Override
    public int hashCode() {
        sort();
        int hash = 3;
        hash = 89 * hash + (this.elements != null ? this.elements.hashCode() : 0);
        return hash;
    }

    protected double printRound(double val) {
        return Math.rint(val*10)/10.0;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("TS[");
        int LIMIT = 20;
        int entries = 0;
        for (Timepoint tp : elements) {
            sb.append(printRound(tp.getTime())).append(":").append(printRound(tp.getValue())).append(",");
            entries++;
            if (entries >= LIMIT) {
                sb.append(" ...").append(elements.size()-entries).append("more");
                break;
            }
        }
        sb.append("]");
        return sb.toString();
    }
    
    
    
    /*
    protected Object writeReplace()
        throws java.io.ObjectStreamException
    {
        //System.out.println("Write resolve called");
        
        return new TimeSeriesSerProxy(this);
    }*/

    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        sort();
        out.writeInt(elements.size());
        out.writeBoolean(sorted);
        
        
        boolean hasStdErrors = false;
        boolean hasStdDevs = false;
        for (Timepoint tp : elements) {
            out.writeDouble(tp.getTime());
            out.writeDouble(tp.getValue());
            if (tp.hasStdError()) hasStdErrors = true;
            if (tp.hasStdDev()) hasStdDevs = true;
        }
        out.writeBoolean(hasStdErrors);
        if (hasStdErrors) {
            for (Timepoint tp : elements) {
                out.writeDouble(tp.hasStdError() ? tp.getStdError() : -1);
            }
        }
        
        out.writeBoolean(hasStdDevs);
        if (hasStdDevs) {
            for (Timepoint tp : elements) {
                out.writeDouble(tp.hasStdDev() ? tp.getStdDev() : -1);
            }
        }
        
        if (!isEmpty()) {
            if (sumValue == null || maxTimePoint == null || minTimePoint == null) findMMM();
            out.writeDouble(sumValue);
            //out.writeDouble(maxValue);
            //out.writeDouble(minValue);
            int ix = elements.indexOf(maxTimePoint);
            if (ix < 0) throw new IllegalStateException("TS in inconsistent state, the stored maxTimepoint: "+(maxTimePoint != null ? maxTimePoint.toString():"null")+" is not element of the timeseries");
            out.writeInt(ix);
            ix = elements.indexOf(minTimePoint);
            if (ix < 0) throw new IllegalStateException("TS in inconsistent state, the stored minTimepoint: "+(minTimePoint != null ? minTimePoint.toString():"null")+" is not element of the timeseries");
            out.writeInt(ix);     
            
        }
        
       
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        int size = in.readInt();
        sorted = in.readBoolean();
         
        elements = new ArrayList<>(size);
        for (int i = 0;i<size;i++) {
            double t = in.readDouble();
            double v = in.readDouble();
            elements.add(new Timepoint(t, v));
        }
        
        boolean hasStdErrors = in.readBoolean();
        if (hasStdErrors) {
            for (int i = 0;i<size;i++) {
                double e = in.readDouble();
                if (e < 0) continue;
                Timepoint tp = elements.get(i);
                tp.setStdError(e);
            }
        }
        boolean hasStdDevs = in.readBoolean();
        if (hasStdDevs) {
            for (int i = 0;i<size;i++) {
                double e = in.readDouble();
                if (e < 0) continue;
                Timepoint tp = elements.get(i);
                tp.setStdDev(e);
            }
        }
        
        if (size > 0) {
            sumValue = in.readDouble();
            //maxValue = in.readDouble();
            //minValue = in.readDouble();
            maxTimePoint = elements.get(in.readInt());
            minTimePoint = elements.get(in.readInt());            
        }
        
    }//*/
    
    
    
    /*
     //implementation thad uses arrays
   @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //out.writeInt(timepoints.size());
        
        int size = elements.size();
        double[] times = new double[size];
        double[] values = new double[size];
        double[] stdErrors = new double[size];
        double[] stdDevs = new double[size];
        
        boolean hasStdErrors = false;
        boolean hasStdDevs = false;
        
        for (int i = 0;i<elements.size();i++) {
            Timepoint tp = elements.get(i);
            times[i] = tp.getTime();
            values[i] = tp.getValue();
            if (tp.hasStdError()) {
                hasStdErrors = true;
                stdErrors[i] = tp.getStdError();
            } else {
                stdErrors[i] = -1;
            }
            if (tp.hasStdDev()) {
                hasStdDevs = true;
                stdDevs[i] = tp.getStdDev();
            } else {
                stdDevs[i] = -1;
            }
        }
        
        out.writeBoolean(sorted);
        
        out.writeObject(times);
        out.writeObject(values);
        
        out.writeBoolean(hasStdErrors);
        if (hasStdErrors) out.writeObject(stdErrors);
        
        out.writeBoolean(hasStdDevs);
        if (hasStdDevs) out.writeObject(stdDevs);
        
        //long l = Double.stdDevs[i].
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
        sorted = in.readBoolean();
        double[] times = (double[])in.readObject();
        double[] values = (double[])in.readObject();
        
        double[] stdErrors = null;
        double[] stdDevs = null;
        
        
        elements = new ArrayList<>(times.length);
        for (int i = 0;i<times.length;i++) {
            elements.add(new Timepoint(times[i], values[i]));
        }
        
        boolean hasStdErrors = in.readBoolean();
        if (hasStdErrors) {
            stdErrors = (double[])in.readObject();
        }
        boolean hasStdDevs = in.readBoolean();
        if (hasStdDevs) {
            stdDevs = (double[])in.readObject();
        }

        if (hasStdErrors) {
            for (int i =0;i<times.length;i++) {
                if (stdErrors[i] >= 0)
                    elements.get(i).setStdError(stdErrors[i]);
            }
        }
        
        if (hasStdDevs) {
            for (int i =0;i<times.length;i++) {
                if (stdDevs[i] >= 0)
                    elements.get(i).setStdDev(stdDevs[i]);
            }
        }
        
       
        
        
    }
    //*/

    /*
    //implementation thad uses byte buffers
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        //out.writeInt(timepoints.size());
        
        int size = elements.size();
        ByteBuffer times = ByteBuffer.allocate(size*8);
        ByteBuffer values = ByteBuffer.allocate(size*8);
        double[] stdErrors = new double[size];
        double[] stdDevs = new double[size];
        
        boolean hasStdErrors = false;
        boolean hasStdDevs = false;
        
        for (int i = 0;i<elements.size();i++) {
            Timepoint tp = elements.get(i);
            times.putDouble(tp.getTime());
            values.putDouble(tp.getValue());
            if (tp.hasStdError()) {
                hasStdErrors = true;
                stdErrors[i] = tp.getStdError();
            } else {
                stdErrors[i] = -1;
            }
            if (tp.hasStdDev()) {
                hasStdDevs = true;
                stdDevs[i] = tp.getStdDev();
            } else {
                stdDevs[i] = -1;
            }
        }
        
        out.writeInt(size);
        out.writeBoolean(sorted);
        
        out.write(times.array());
        out.write(values.array());
        
        out.writeBoolean(hasStdErrors);
        if (hasStdErrors) out.writeObject(stdErrors);
        
        out.writeBoolean(hasStdDevs);
        if (hasStdDevs) out.writeObject(stdDevs);
        
        //long l = Double.stdDevs[i].
        
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
        int size = in.readInt();
        sorted = in.readBoolean();
        
        byte[] timesB = new byte[size*8];
        byte[] valuesB = new byte[size*8];
        
        int offset = 0;
        int last =timesB.length;
        while (offset < last) {
            int read = in.read(timesB, offset, timesB.length-offset);
            if (read <0) break;
            offset+=read;
        }
        if (offset != timesB.length) throw new IOException("Less bytes read "+offset+" than expected considering data size:"+size);
        
        offset = 0;
        last =timesB.length;
        while (offset < last) {
            int read = in.read(valuesB, offset, valuesB.length-offset);
            if (read <0) break;
            offset+=read;
        }
        if (offset != timesB.length) throw new IOException("Less bytes read "+offset+" than expected considering data size:"+size);
        
        ByteBuffer times = ByteBuffer.wrap(timesB);
        ByteBuffer values = ByteBuffer.wrap(valuesB);
        
        double[] stdErrors = null;
        double[] stdDevs = null;
        
        
        
        elements = new ArrayList<>(size);
        for (int i = 0;i<size;i++) {
            elements.add(new Timepoint(times.getDouble(), values.getDouble()));
        }
        
        boolean hasStdErrors = in.readBoolean();
        if (hasStdErrors) {
            stdErrors = (double[])in.readObject();
        }
        boolean hasStdDevs = in.readBoolean();
        if (hasStdDevs) {
            stdDevs = (double[])in.readObject();
        }

        if (hasStdErrors) {
            for (int i =0;i<size;i++) {
                if (stdErrors[i] >= 0)
                    elements.get(i).setStdError(stdErrors[i]);
            }
        }
        
        if (hasStdDevs) {
            for (int i =0;i<size;i++) {
                if (stdDevs[i] >= 0)
                    elements.get(i).setStdDev(stdDevs[i]);
            }
        }
        
       
        
        
    }
    //*/

    final boolean isSorted() {
        return sorted;
    }


    
}
