package org.commonprovenance.framework.nro.rest.exceptionhandling;

import java.time.Clock;
import java.time.LocalDateTime;

import org.commonprovenance.framework.nro.exceptions.CertificateNotFoundException;
import org.commonprovenance.framework.nro.exceptions.CertificateVerificationException;
import org.commonprovenance.framework.nro.exceptions.DocumentNotFoundException;
import org.commonprovenance.framework.nro.exceptions.InvalidTimestampException;
import org.commonprovenance.framework.nro.exceptions.MissingSignatureException;
import org.commonprovenance.framework.nro.exceptions.OrganizationAlreadyExistsException;
import org.commonprovenance.framework.nro.exceptions.OrganizationIdMismatchException;
import org.commonprovenance.framework.nro.exceptions.OrganizationNotFoundException;
import org.commonprovenance.framework.nro.exceptions.SignatureVerificationException;
import org.commonprovenance.framework.nro.exceptions.TokenAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UrlPathHelper;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class CustomRestGlobalExceptionHandling {

  private static UrlPathHelper URL_PATH_HELPER = new UrlPathHelper();

  @ExceptionHandler({
      CertificateNotFoundException.class,
      DocumentNotFoundException.class,
      OrganizationNotFoundException.class
  })
  public ResponseEntity<ApiError> handleResoursceNotFound(
      final Exception ex,
      @NonNull final HttpServletRequest request) {
    final HttpStatus status = HttpStatus.NOT_FOUND;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        ex.getLocalizedMessage(),
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }

  @ExceptionHandler({
      InvalidTimestampException.class,
      MissingSignatureException.class,
      OrganizationIdMismatchException.class,
      IllegalArgumentException.class
  })
  public ResponseEntity<ApiError> handleBadRequest(
      final Exception ex,
      @NonNull final HttpServletRequest request) {
    final HttpStatus status = HttpStatus.BAD_REQUEST;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        ex.getLocalizedMessage(),
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }

  @ExceptionHandler({
      OrganizationAlreadyExistsException.class,
      TokenAlreadyExistsException.class
  })
  public ResponseEntity<ApiError> handleConflict(
      final Exception ex,
      @NonNull final HttpServletRequest request) {
    final HttpStatus status = HttpStatus.CONFLICT;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        ex.getLocalizedMessage(),
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }

  @ExceptionHandler({
      CertificateVerificationException.class,
      SignatureVerificationException.class
  })
  public ResponseEntity<ApiError> handleUnauthorized(
      final Exception ex,
      @NonNull final HttpServletRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    final ApiError apiError = new ApiError(
        LocalDateTime.now(Clock.systemUTC()),
        status,
        ex.getLocalizedMessage(),
        URL_PATH_HELPER.getRequestUri(request));
    return new ResponseEntity<>(apiError, new HttpHeaders(), status);
  }
}
