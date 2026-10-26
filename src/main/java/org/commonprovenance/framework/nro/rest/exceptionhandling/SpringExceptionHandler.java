package org.commonprovenance.framework.nro.rest.exceptionhandling;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.commonprovenance.framework.nro.exceptions.ApplicationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;

@Order(2)
@RestControllerAdvice
public class SpringExceptionHandler {
  private static UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  @ExceptionHandler({
      MethodArgumentNotValidException.class })
  public ResponseEntity<ApiError> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex,
      @NonNull final HttpServletRequest request) {

    final String message = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .collect(Collectors.joining("; "));
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        message,
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }

}
