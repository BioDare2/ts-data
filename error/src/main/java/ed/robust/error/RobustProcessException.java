package ed.robust.error;

/**
 * Representation of errors during processing of data or files.
 * @author tzielins
 *
 */
public class RobustProcessException extends RobustException {

    protected RobustProcessException() {
        super();
    }
    
    public RobustProcessException(String msg) {
	this(msg,msg);
    }

    public RobustProcessException(String string, 
                    Throwable e) {
            this(string,string,e);
    }
    
	public RobustProcessException(String string, String text,
			Throwable e) {
		super(string,text,e);
	}

	public RobustProcessException(String string, String text) {
		super(string,text);
	}

	public RobustProcessException(RobustException e)
	{
	   super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 6264850696920794991L;

}
