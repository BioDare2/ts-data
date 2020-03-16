package ed.robust.error;

/**
 * <p>Representation of the exceptions in the robust system.</p>
 * <p>It has additional feature that apart from system message it stores also information
 * to be presented to the final user by the front end</p
 * @author tzielins
 *
 */
public class RobustException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7346256721730577524L;
	
	protected String UIMessage;

	/**
	 * 
	 * @return message designated for the final user
	 */
	public String getUIMessage() {
		return UIMessage;
	}

	/**
	 * Sets the message for the final user
	 * @param message to be presented to the final user
	 */
	public void setUIMessage(String message) {
		UIMessage = message;
	}

	public RobustException()
	{
		super();
	}


	
	public RobustException(String message,String uitext)
	{
		super(message);
                if (uitext == null) throw new IllegalArgumentException("UI message cannot be null");
		this.UIMessage = uitext;
	}
	
	public RobustException(String message,String uitext,Throwable cause)
	{
		super(message,cause);
                if (uitext == null) throw new IllegalArgumentException("UI message cannot be null");
		this.UIMessage = uitext;
	}

	public RobustException(RobustException e)
	{
	    this(e.getMessage(),e.getUIMessage(),e);
	}

	public RobustException(Exception e) {
	    this(e.getMessage(),e.getMessage(),e);
	}
}
