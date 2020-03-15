/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.dom.util;

import java.io.Serializable;

/**
 * Container for different values types that can be serialized and then converted to requested type
 * @author tzielins
 */
public class ParamValue implements Serializable {

    Serializable value;

    public ParamValue(String val)
    {
	this.value = val;
    }


    public ParamValue(int val)
    {
	this.value = val;
    }

    public ParamValue(long val)
    {
	this.value = val;
    }

    public ParamValue(boolean val)
    {
	this.value = val;
    }

    public ParamValue(double val)
    {
	this.value = val;
    }

    @Override
    /**
     * Returns this value represented as String. Which is same as the toString method
     * of the underlying value object
     */
    public String toString()
    {
	if (value != null) return value.toString();
	return null;
    }

    /**
     * Gives value of this parameters as int.
     * @return int value of this parameter
     * @throws IllegalArgumentException if the value was null or cannot be converted to int
     */
    public int toInt() throws IllegalArgumentException
    {
	if (value == null) throw new IllegalArgumentException("Null value of the parameter");

	if (value instanceof Integer) return ((Integer)value);

	try {
	    return Integer.parseInt(value.toString());
	} catch(NumberFormatException e) {
	    throw new IllegalArgumentException(value.toString()+" is not an int");
	}
    }

    /**
     * Gives value of this parameter as long
     * @return long value of this parameter
     * @throws IllegalArgumentException if the value is null or cannot be converted to long
     */
    public long toLong() throws IllegalArgumentException
    {
	if (value == null) throw new IllegalArgumentException("Null value of the parameter");

	if (value instanceof Long) return ((Long)value);

	try {
	    return Long.parseLong(value.toString());
	} catch(NumberFormatException e) {
	    throw new IllegalArgumentException(value.toString()+" is not long");
	}
    }

    /**
     * Gives value of this parameter as double
     * @return double value of this parameter
     * @throws IllegalArgumentException if the value is null or cannot be converted to double
     */
    public double toDouble() throws IllegalArgumentException
    {
	if (value == null) throw new IllegalArgumentException("Null value of the parameter");

	if (value instanceof Double) return ((Double)value);

	try {
	    return Double.parseDouble(value.toString());
	} catch(NumberFormatException e) {
	    throw new IllegalArgumentException(value.toString()+" is not double");
	}
    }

    /**
     * Gives value of this parameter as boolean
     * @return boolean value of this parameter
     * @throws IllegalArgumentException if the value is null or cannot be converted to boolean
     */
    public boolean toBool() throws IllegalArgumentException
    {
	if (value == null) throw new IllegalArgumentException("Null value of the parameter");

	if (value instanceof Boolean) return ((Boolean)value);

	return Boolean.parseBoolean(value.toString());

    }

}
