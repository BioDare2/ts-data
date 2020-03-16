/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.robust.error;

/**
 *
 * @author tzielins
 */
public class RobustDBConcurrentException extends RobustDBException {

    public RobustDBConcurrentException(String msg) {
        super(msg);
    }

    public RobustDBConcurrentException(String string, Throwable e) {
        super(string, e);
    }

    public RobustDBConcurrentException(String string, String text, Throwable e) {
        super(string, text, e);
    }

    public RobustDBConcurrentException(String string, String text) {
        super(string, text);
    }
    
    
}
