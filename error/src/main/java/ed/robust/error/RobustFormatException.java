package ed.robust.error;

/**
 * Representation of the error connected with mis formating of the data or files
 * @author tzielins
 *
 */
public class RobustFormatException extends RobustException {


    public RobustFormatException(String msg) {
	this(msg,msg);
    }
    
    public RobustFormatException(String msg,Throwable e) {
	this(msg,msg,e);
    }
    

	public RobustFormatException(String string, String text, Throwable e) {
		super(string,text,e);
	}

	public RobustFormatException(String string, String text) {
		super(string,text);
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -667622585528331631L;

}
