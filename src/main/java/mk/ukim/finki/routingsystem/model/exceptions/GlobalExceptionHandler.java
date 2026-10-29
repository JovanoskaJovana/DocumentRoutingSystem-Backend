package mk.ukim.finki.routingsystem.model.exceptions;

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
   *
   * @param ex the thrown exception
   * @return 500 Internal Server Error with a generic error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred"));
  }

}
