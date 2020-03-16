/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ed.robust.error;

/**
 *
 * @author tzielins
 */
public class RobustDBException extends RobustInnerError {
    private static final long serialVersionUID = 1L;

    public RobustDBException(String msg)
    {
	this(msg,msg);
    }

    	public RobustDBException(String string, 
			Throwable e) {
		super(string,string,e);
	}
    
    	public RobustDBException(String string, String text,
			Throwable e) {
		super(string,text,e);
	}

	public RobustDBException(String string, String text) {
		super(string,text);
	}

}
