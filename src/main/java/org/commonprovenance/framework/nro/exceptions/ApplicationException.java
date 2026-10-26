package org.commonprovenance.framework.nro.exceptions;

import java.util.function.BiFunction;

public class ApplicationException extends RuntimeException {
  public static String buildMessage(Throwable throwable) {
    return buildMessage(throwable, "");
  }

  public static String buildMessage(Throwable throwable, String acc) {
    BiFunction<Throwable, String, String> build = (exception, message) -> message
        + "\n- [" + exception.getClass().getSimpleName() + "]: "
        + exception.getMessage()
        + " (" + throwable.getStackTrace()[0].getFileName() + "/" + throwable.getStackTrace()[0].getMethodName() + ":" + throwable.getStackTrace()[0].getLineNumber() + ")";

    if (throwable == null)
      return acc;

    if (throwable instanceof ApplicationException appException)
      return buildMessage(appException.getCause(), build.apply(appException, acc));

    return build.apply(throwable, acc);
  }

  public ApplicationException() {
  }

  public ApplicationException(String message) {
    super(message);
  }

  public ApplicationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ApplicationException(Throwable cause) {
    super(cause);
  }

  public ApplicationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
