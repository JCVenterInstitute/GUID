package org.jtc.common.ejb.api.exception;

import org.jtc.common.ejb.api.intf.Error;

public class ApplicationErrorException extends Exception {
     static private final String NEWLINE = System.getProperty("line.separator");

     Error[] errors;

     public ApplicationErrorException(Throwable cause) {
         super(cause);
         if (ApplicationErrorException.class.isInstance(cause)){
             ApplicationErrorException e = (ApplicationErrorException)cause;
             if (e.getErrors() != null ){
                 int length = e.getErrors().length;
                 errors = new Error[e.getErrors().length];
                 System.arraycopy(e.getErrors(),0,errors,0,length);
             }
         }
     }

     public ApplicationErrorException(String msg) {
         super(msg);
     }

     public ApplicationErrorException(String msg, Throwable cause) {
         super(msg, cause);
     }

     public ApplicationErrorException(Error[] errors) {
         this.errors = errors;
     }

     public ApplicationErrorException(String msg, Error[] errors) {
         super(msg);
         this.errors = errors;
     }

     public ApplicationErrorException(String msg, Throwable cause, Error[] errors) {
         super(msg, cause);
         this.errors = errors;
     }

     public ApplicationErrorException(Throwable cause,Error[] errors) {
         super(cause);
         this.errors = errors;
     }

     public Error[] getErrors() {
         return errors;
     }

     public Error searchException(String exception) {
         if (errors == null ){
             return null;
         }
         for( int i=0; i< errors.length; i++) {
             if (exception.equalsIgnoreCase(errors[i].getErrorCodeName())) {
                 return errors[i];
             }
         }
         return null;
     }

     /**
      * Returns a error String in ApplicationErrorException
      * The result is the concatenation of three strings:
      * <ul>
      * <li>The name of the actual class of this object
      * <li>": " (a colon and a space)
      * <li>The result of the {@link #getMessage} method for this object
      * <li>": "(a colon and a space)
      * <li>All the error messages in errors object delimited by ": "
      * </ul>
      *
      * @return a string representation of this ApplicationErrorException.
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

     public String getErrorMessage() {
         StringBuffer sb = new StringBuffer();
         if(getCause() == null && this.getMessage() != null && this.getMessage().trim().length() > 0) {
             sb.append(this.getMessage());
         }
         if (errors != null) {
             if(sb.length() > 0) {
                 sb.append( NEWLINE);
             }
             for(int i =0 ; i < errors.length; i++) {
                 sb.append( errors[i].getErrorDate());
                 sb.append(": ");
                 sb.append( errors[i].getErrorCodeMessage());
             }
         }
         return sb.toString();
     }

 }
