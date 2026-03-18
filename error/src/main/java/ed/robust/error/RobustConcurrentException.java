package ed.robust.error;

/**
 * Representation of errors during processing of data or files due to the concurrent access.
 * @author tzielins
 *
 */
public class RobustConcurrentException extends RobustException {

    /**
     * 
     */
    private static final long serialVersionUID = 6264850696920794991L;
    
    protected RobustConcurrentException() {
        super();
    }
    
    public RobustConcurrentException(String msg) {
	this(msg,msg);
    }

    public RobustConcurrentException(String string, 
                    Throwable e) {
            this(string,string,e);
    }
    
	public RobustConcurrentException(String string, String text,
			Throwable e) {
		super(string,text,e);
	}

	public RobustConcurrentException(String string, String text) {
		super(string,text);
	}

	public RobustConcurrentException(RobustException e)
	{
	   super(e);
	}


}
