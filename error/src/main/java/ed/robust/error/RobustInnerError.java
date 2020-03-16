package ed.robust.error;

/**
 * Representation of the general internal errors, which should not happen during normal use.
 * @author tzielins
 *
 */
public class RobustInnerError extends RobustException {

    public RobustInnerError(String msg) {
	this(msg,msg);
    }

	public RobustInnerError(String string, Throwable e) {
            this(string,string,e);
        }    
	public RobustInnerError(String string, String text, Throwable e) {
		super(string,text,e);
}

	public RobustInnerError(String string, String text) {
		super(string,text);
	}

	public RobustInnerError(RobustException e)
	{
	    super(e);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3479310618168123711L;

}
