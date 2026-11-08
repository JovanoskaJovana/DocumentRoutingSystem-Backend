package mk.ukim.finki.routingsystem.model.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.routingsystem.model.documentEntities.Document;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

/**
 * Global exception handler for all controllers in the application.
 * Intercepts exceptions thrown across all layers and maps them to
 * appropriate HTTP responses with a consistent {@link ErrorResponse} body.
 */

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handles all resource-not-found exceptions.
   *
   * @param ex the thrown exception
   * @return 404 Not Found with the exception message in the response body
   */
  @ExceptionHandler({
          DocumentNotFoundException.class,
          CompanyNotFoundException.class,
          DepartmentNotFoundException.class,
          DocumentVersionNotFoundException.class,
          EmployeeNotFoundException.class,
          RoutingRulesForTenantNotFoundException.class
  })
  public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
  }

  /**
   * Handles invalid request exceptions caused by bad input or illegal document state transitions.
   *
   * @param ex the thrown exception
   * @return 400 Bad Request with the exception message in the response bod
   */
  @ExceptionHandler({
          InvalidDocumentStateException.class,
          RequiredChangeNoteException.class,
          RequiredEmployeeIdException.class
  })
  public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
  }

  /**
   * Handles conflict exceptions caused by duplicate resource creation.
   *
   * @param ex the thrown exception
   * @return 409 Conflict with the exception message in the response body
   */
  @ExceptionHandler(CompanyAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleConflict(CompanyAlreadyExistsException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
  }

  /**
   * Handles approving or rejecting a document that was already approved or rejected.
   *
   * @param ex the thrown exception
   * @return 409 Conflict with the exception message in the response body
   */
  @ExceptionHandler(DocumentAlreadyDecidedException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyDecided(DocumentAlreadyDecidedException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
  }

  /**
   * Handles documents whose file cannot be read: missing, password-protected or damaged.
   *
   * @param ex the thrown exception
   * @return 422 Unprocessable Entity with the exception message in the response body
   */
  @ExceptionHandler(UnreadableDocumentException.class)
  public ResponseEntity<ErrorResponse> handleUnreadableDocument(UnreadableDocumentException ex) {
    return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
  }

  /**
   * Handles uploading or renaming a document to a title that already exists in the same company.
   *
   * @param ex the thrown exception
   * @return 409 Conflict with the exception message in the response body
   */
  @ExceptionHandler(DuplicateDocumentTitleException.class)
  public ResponseEntity<ErrorResponse> handleDuplicateTitle(DuplicateDocumentTitleException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage()));
  }

  /**
   * Handles database constraint violations. Only the per-company title constraint becomes 409:
   * it is hit when two requests save the same title at the same time, after both passed the duplicate check.
   * Every other violation is an unexpected error and goes to {@link #handleGeneric}.
   *
   * @param ex      the thrown exception
   * @param request the request that caused the exception
   * @return 409 Conflict for a duplicate title, otherwise 500 Internal Server Error
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, HttpServletRequest request) {
    if (violatesConstraint(ex, Document.UNIQUE_TITLE_PER_COMPANY)) {
      return handleDuplicateTitle(new DuplicateDocumentTitleException());
    }
    return handleGeneric(ex, request);
  }

  private static boolean violatesConstraint(Throwable ex, String constraintName) {
    for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
      if (cause instanceof ConstraintViolationException violation) {
        return constraintName.equalsIgnoreCase(violation.getConstraintName());
      }
    }
    return false;
  }

  /**
   * Handles concurrent modification of the same resource, e.g. two signatories
   * approving and rejecting the same document at the same time.
   *
   * @param ex the thrown exception
   * @return 409 Conflict with a message asking the client to reload and retry
   */
  @ExceptionHandler(OptimisticLockingFailureException.class)
  public ResponseEntity<ErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex) {
    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(HttpStatus.CONFLICT.value(),
                    "This resource was modified by another user. Please reload and try again."));
  }

  /**
   * Handles forbidden access exceptions.
   *
   * @param ex the thrown message
   * @return 403 Forbidden with the exception message in the response body
   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
    return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage()));
  }

  /**
   * Fallback handler for all unhandled exceptions.
   * Logs the request and the full stack trace, but returns only a generic message to the client.
   *
   * @param ex      the thrown exception
   * @param request the request that caused the exception
   * @return 500 Internal Server Error with a generic error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
  }

}
