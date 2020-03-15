
package ed.robust.dom.data;

import static ed.robust.dom.data.Timepoint.STD_DEV;
import static ed.robust.dom.data.Timepoint.STD_ERR;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Representation of a timepoint that can be serialized to xml in a compact way. 
 * The time and value are mandatory but the error does not have to be present, apart from error value
 * its type is also stored.
 * @author tzielins
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Timepoint implements Comparable<Timepoint>, Serializable {

    /**
    * 
    */
    private static final long serialVersionUID = -6951939249615174546L;

	/** 
     * Denotes error type as Starndard Error 
     */
    public static final byte STD_ERR = 1;
    
    /**
     * Denotes error type as Standard Devaiation
     */
    public static final byte STD_DEV = 2;
	
    @XmlAttribute(name="t", required = true)
    private double time;
    @XmlAttribute(name="v", required = true)
    private double value;
    @XmlAttribute(name="ste", required = false)
    private Double stdError;
    @XmlAttribute(name="std", required = false)
    private Double stdDev;

    private Timepoint() {

    }

    public Timepoint(double time, double value) {
	this();
        if (Double.isInfinite(time) || Double.isNaN(time)) throw new IllegalArgumentException("time cannot be infinitive nor Nan: "+time);
        if (Double.isInfinite(value) || Double.isNaN(value)) throw new IllegalArgumentException("value cannot be infinitive nor Nan: "+value);
	this.time = time;
	this.value = value;
    }

    /**
     * Creates new timepoint with given error value
     * @param time time in hours
     * @param value value
     * @param error error value (must be >= 0)
     * @param errorType marks if the error value is for standard deviation or standard error, as defined by constants STD_DEV, STD_ERR
     */
    public Timepoint(double time, double value,double error,byte errorType) {
	this(time,value);
        if (error < 0) throw new IllegalArgumentException("Error must be >= 0");
        if (errorType != STD_ERR && errorType != STD_DEV) throw new IllegalArgumentException("Unknown error type");
        switch(errorType) {
            case STD_ERR: this.stdError = error; break;
            case STD_DEV: this.stdDev = error; break;
        }
    }
    
    /**
     * Creates new timepoint with std error and std dev values
     * @param time time in hours
     * @param value value
     * @param stdError std error value (must be >= 0)
     * @param stdDev  std dev value (must be >=0)
     */
    public Timepoint(double time,double value,Double stdError,Double stdDev) {
        this(time,value);
        if ((stdError != null && stdError < 0) || (stdDev != null && stdDev < 0)) throw new IllegalArgumentException("Error must be >= 0");
        this.stdDev = stdDev;
        this.stdError = stdError;
    }
    
    /**
     * Checks if timepoint represents value with error
     * @return true if it has std err or std dev
     */
    public boolean hasError() {
        return (stdDev != null || stdError != null);
    }
    
    /**
     * Checks if timepoint represents value with asigned std error
     * @return true if there is std error
     */
    public boolean hasStdError() {
        return (stdError != null);
    }
    
    /**
     * Checks if timepoint represents value with asigned std deviation
     * @return true if there is std deviation
     */
    public boolean hasStdDev() {
        return (stdDev != null);
    }
    
    /**
     * Gets the value of the time property.
     * 
     */
    public double getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     */
    /*public void setTime(double value) {
        this.time = value;
    }*/

    /**
     * Gets the value of the value property.
     * 
     */
    public double getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     */
    /*public void setValue(double value) {
        this.value = value;
    }*/

    public Double getStdDev() {
        return stdDev;
    }

    protected void setStdDev(double stdDev) {
        if (stdDev < 0) throw new IllegalArgumentException("Error must be >= 0");
        this.stdDev = stdDev;
    }

    public Double getStdError() {
        return stdError;
    }

    protected void setStdError(double stdError) {
        if (stdError < 0) throw new IllegalArgumentException("Error must be >= 0");
        this.stdError = stdError;
    }



    @Override
    public int compareTo(Timepoint o) {
        if (time > o.time) return 1;
        if (time == o.time) return 0;
        return -1;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.time) ^ (Double.doubleToLongBits(this.time) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        hash = 29 * hash + (this.stdError != null ? this.stdError.hashCode() : 1);
        hash = 29 * hash + (this.stdDev != null ? this.stdDev.hashCode() : 3);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (! (obj instanceof Timepoint)) return false;
        Timepoint t = (Timepoint)obj;
        
        if (time == t.time && value == t.value) {

            if (stdError == null)
                if (t.stdError != null) return false;
            
            if (stdDev == null)
                if (t.stdDev != null) return false;
            
            if (stdError != null)
                if (!stdError.equals(t.stdError)) return false;
            if (stdDev != null)
                if (!stdDev.equals(t.stdDev)) return false;
            
            return true;
        }
        return false;
    }
    
    /**
     * Sets the time for the otherwise immutable timepoint, by creating new timepoint
     * which has the same value and errors but new time value. Original object remains the same.
     * @param time value to be set as time for the new point
     * @return new timepoint that preserves original values but have the requested time set.
     */
    public Timepoint changeTime(double time) {
        Timepoint out = new Timepoint(time, this.value);
        out.stdDev = this.stdDev;
        out.stdError = this.stdError;
        return out;
    }
    
    /**
     * Sets the value for the otherwise immutable timepoint, by creating new timepoint
     * which has the same time and errors but the new value. Original object remains the same.
     * @param value to be set as time for the new point
     * @return new timepoint that preserves original time and errors but have the requested value set.
     */
    public Timepoint changeValue(double value) {
        Timepoint out = new Timepoint(this.time, value);
        out.stdDev = this.stdDev;
        out.stdError = this.stdError;
        return out;
    }
    
    /**
     * Scale timepoint values with the given factor (divides with it). Since the timepoint
     * is immutable creates new object having the same time but scaled value and errors.
     * @param factor to be scaled with (divided)
     * @return new point of same time, but scaled values and errors if they were present
     */
    public Timepoint factorise(double factor) {
        Timepoint out = new Timepoint(this.time, value/factor);
        if (this.stdDev != null) out.stdDev = this.stdDev/factor;
        if (this.stdError != null) out.stdError = this.stdError/factor;
        return out;
    }
    
    public Timepoint offset(double offset) {
        Timepoint out = new Timepoint(this.time, value+offset);
        out.stdDev = this.stdDev;
        out.stdError = this.stdError;
        return out;
    }
    
    
    public Timepoint scale(double factor, double offset) {
        Timepoint out = new Timepoint(this.time, value/factor+offset);
        if (this.stdDev != null) out.stdDev = this.stdDev/factor;
        if (this.stdError != null) out.stdError = this.stdError/factor;
        return out;
    }

    
    /**
     * Check if timepoitns are equals given the precision. That way timepoints can be treated as equals even
     * if they have small differences due to some rounding errors.
     * @param obj other timepoint to compare with
     * @param precision to which data must match, for example 0.1 means that the diffrence between values must be smaller than 0.1
     * @return true if both timepoints have same time, value and error values in the requested precission range
     */
    public boolean almostEquals(Timepoint t,double precision) {
        //if (! (obj instanceof Timepoint)) return false;
        //Timepoint t = (Timepoint)obj;
        
        double dif = Math.abs(time-t.time);
        if (dif > precision) return false;
        
        dif = Math.abs(value-t.value);
        if (dif > precision) return false;
        
        if (stdError == null) {
            if (t.stdError != null) return false;
        } else {
            if (t.stdError == null) return false;
            dif = Math.abs(stdError -t.stdError);
            if (dif > precision) return false;
        }
        
        if (stdDev == null) {
            if (t.stdDev != null) return false;
        } else {
            if (t.stdDev == null) return false;
            dif = Math.abs(stdDev -t.stdDev);
            if (dif > precision) return false;
        }
        
        return true;
        
        
    }
    


    @Override
    public String toString() {
        String s = time+":"+value;
        
        if (hasStdError())
            return s+"("+stdError+")";
        
        if (hasStdDev())
            return s+"("+stdDev+")";
        
        return s;
        
    }

    /*
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeDouble(time);
        out.writeDouble(value);
        out.writeDouble(stdError != null ? stdError : -1);
        out.writeDouble(stdDev != null ? stdDev : -1);  
        throw new IOException("Am I called");
    }
    
    @Override
    public void readExternal(ObjectInput in) throws IOException {
        time = in.readDouble();
        value = in.readDouble();
        stdError = in.readDouble();
        stdDev = in.readDouble();
        if (stdError < 0) stdError = null;
        if (stdDev < 0) stdDev = null;
    }*/



}
