package org.commonprovenance.framework.nro.exceptions;

public class InvalidSignatureException extends RuntimeException {

  public InvalidSignatureException() {
  }

  public InvalidSignatureException(String message) {
    super(message);
  }

  public InvalidSignatureException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidSignatureException(Throwable cause) {
    super(cause);
  }

  public InvalidSignatureException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
