package ed.robust.error;

/**
 * Representation of errors raised by the security subsystem in the case of not sufficient rights
 * @author tzielins
 *
 */
public class RobustSecurityException extends RobustException {

    protected RobustSecurityException() {
        super();
    }
    
    public RobustSecurityException(String msg) {
	this(msg,msg);
    }

	public RobustSecurityException(String string, String text, Throwable e) {
		super(string,text,e);
}

	public RobustSecurityException(String string, String text) {
		super(string,text);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3479310618168123732L;

}
