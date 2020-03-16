package ed.robust.error;

/**
 * Representation of any IO error
 * @author tzielins
 *
 */
public class RobustIOException extends RobustInnerError {

    public RobustIOException(String msg) {
	this(msg,msg);
    }

	public RobustIOException(String text, Throwable e) {
		super(text,text,e);
	}
        
	public RobustIOException(String string, String text, Throwable e) {
		super(string,text,e);
	}

	public RobustIOException(String string, String string2) {
		super(string,string2);
	}

	public RobustIOException(RobustException e)
	{
	    super(e);
	}

	public RobustIOException(Exception e) {
	    super(e.getMessage(),e);
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 924208461340068486L;

}
