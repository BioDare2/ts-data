package ed.robust.error;

/**
 * Represepresentation of errors caused by wrong data coming from front end, either not complited
 * or not consistent.
 * @author tzielins
 *
 */
public class RobustUIException extends RobustException {

    public RobustUIException(String msg) {
	this(msg,msg);
    }

	public RobustUIException(String string, String text,
			Throwable e) {
		super(string,text,e);
	}

	
	public RobustUIException(String string, String text) {
		super(string,text);
	}
        
        public RobustUIException(RobustException e) {
            super(e);
        }

	/**
	 * 
	 */
	private static final long serialVersionUID = 6264850696920794991L;

}
