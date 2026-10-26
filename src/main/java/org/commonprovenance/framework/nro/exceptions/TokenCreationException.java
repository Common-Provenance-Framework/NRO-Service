package org.commonprovenance.framework.nro.exceptions;

public class TokenCreationException extends ApplicationException {

  public TokenCreationException() {
  }

  public TokenCreationException(String message) {
    super(message);
  }

  public TokenCreationException(String message, Throwable cause) {
    super(message, cause);
  }

  public TokenCreationException(Throwable cause) {
    super(cause);
  }

  public TokenCreationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
