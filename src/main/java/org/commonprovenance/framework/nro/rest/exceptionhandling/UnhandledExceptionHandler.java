package org.commonprovenance.framework.nro.rest.exceptionhandling;

import java.time.Clock;
import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.exceptions.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;

@Order(3)
@RestControllerAdvice
public class UnhandledExceptionHandler {
  private static UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();
  private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledExceptionHandler.class);

  @ExceptionHandler({
      Exception.class })
  public ResponseEntity<ApiError> handleRest(
      final Exception ex,
      @NonNull final HttpServletRequest request) {

    LOGGER.error(ApplicationException.buildMessage(ex, "\"*** Unhandled Exception ***\""), ex);

    final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        "Unhandled Exception",
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }
}
