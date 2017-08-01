package org.jtc.common.ejb.api.exception;

import org.jtc.common.ejb.api.intf.Error;

public class SystemErrorException extends Exception {
    static private final String NEWLINE = System.getProperty("line.separator");

    private Error[] errors;

    public SystemErrorException(Throwable cause) {
        super(cause);
        if (SystemErrorException.class.isInstance(cause)){
            SystemErrorException e = (SystemErrorException)cause;
            if (e.getErrors() != null){
                int length = e.getErrors().length;
                errors = new Error[e.getErrors().length];
                System.arraycopy(e.getErrors(),0,errors,0,length);
            }
        }
    }

    public SystemErrorException(String msg) {
        super(msg);
    }

    public SystemErrorException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SystemErrorException(Error [] errors) {
        this.errors = errors;
    }

    public SystemErrorException(String msg,Error [] errors) {
        super(msg);
        this.errors = errors;
    }

    public SystemErrorException(String msg, Throwable cause,Error [] errors) {
        super(msg, cause);
        this.errors = errors;
    }

    public SystemErrorException(Throwable cause,Error [] errors) {
        super( cause);
        this.errors = errors;
    }
    public Error [] getErrors(){
        return errors;
    }

    /**
     * Returns a error String in SystemErrorException
     * the result is the concatenation of three strings:
     * <ul>
     * <li>The name of the actual class of this object
     * <li>": " (a colon and a space)
     * <li>The result of the {@link #getMessage} method for this object
     * <li>": "(a colon and a space)
     * <li>All the error messages in errors object delimited by ": "
     * </ul>
     *
     * @return a string representation of this SystemErrorException.
     */
    public String toString() {
        StringBuffer s=new StringBuffer();
        String sName = getClass().getName();
        String message = getLocalizedMessage();
        s.append(sName);
        if (getCause() == null && message != null) {
            s.append(": ");
            s.append(message);
        }
        if (errors != null){
            for(int i=0;i<errors.length;i++){
                s.append(i+": ");
                s.append(errors[i].getErrorCodeMessage());
            }
        }
        return s.toString();
    }
    public String getErrorMessage(){
        StringBuffer sb = new StringBuffer();
        if(getCause() == null && this.getMessage() != null && this.getMessage().trim().length() > 0) {
            sb.append(this.getMessage());
        }
        if (errors != null) {
            if(sb.length() > 0) {
                sb.append( NEWLINE);
            }
            for(int i =0 ; i < errors.length; i++) {
                sb.append( "date:");
                sb.append( errors[i].getErrorDate());
                sb.append( NEWLINE);
                sb.append( "code:");
                sb.append( errors[i].getErrorCode());
                sb.append( NEWLINE);
                sb.append( "group id:");
                sb.append( errors[i].getErrorGroupId());
                sb.append( NEWLINE);
                sb.append( "name:");
                sb.append( errors[i].getErrorCodeName());
                sb.append( NEWLINE);
                sb.append( "message:");
                sb.append( errors[i].getErrorCodeMessage());
                sb.append( NEWLINE);
                sb.append( NEWLINE);
            }
        }
        return sb.toString();
    }
}
