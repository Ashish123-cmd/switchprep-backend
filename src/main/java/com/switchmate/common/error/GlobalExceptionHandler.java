package com.switchmate.common.error;

import java.time.Instant;
import java.util.List;

import jakarta.validation.ConstraintViolationException;
import com.switchmate.common.exception.BadRequestException;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.InvalidAdminApiKeyException;
import com.switchmate.common.exception.InvalidGoogleTokenException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.common.exception.RateLimitExceededException;
import com.switchmate.common.exception.ResumeParsingException;
import com.switchmate.common.exception.SignupDraftExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		List<FieldErrorResponse> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldErrorResponse)
				.toList();

		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException exception) {
		List<FieldErrorResponse> fieldErrors = null;
		Throwable cause = exception.getMostSpecificCause();
		if (cause instanceof com.fasterxml.jackson.databind.JsonMappingException mappingException
				&& !mappingException.getPath().isEmpty()) {
			String field = mappingException.getPath().get(mappingException.getPath().size() - 1).getFieldName();
			if (field != null && !field.isBlank()) {
				fieldErrors = List.of(new FieldErrorResponse(field, "Invalid value"));
			}
		}
		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request body is missing or malformed", fieldErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException exception) {
		List<FieldErrorResponse> fieldErrors = exception.getConstraintViolations().stream()
				.map(violation -> new FieldErrorResponse(
						violation.getPropertyPath().toString(), violation.getMessage()))
				.toList();

		return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", fieldErrors);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(NotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), null);
	}

	@ExceptionHandler(BadRequestException.class)
	public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException exception) {
		return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", exception.getMessage(), null);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException exception) {
		return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Resume file must not exceed 5 MB", null);
	}

	@ExceptionHandler(ResumeParsingException.class)
	public ResponseEntity<ApiErrorResponse> handleResumeParsing(ResumeParsingException exception) {
		return error(HttpStatus.UNPROCESSABLE_ENTITY, "RESUME_PARSING_ERROR", exception.getMessage(), null);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(ConflictException exception) {
		return error(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), null);
	}

	@ExceptionHandler(SignupDraftExpiredException.class)
	public ResponseEntity<ApiErrorResponse> handleSignupDraftExpired(SignupDraftExpiredException exception) {
		return error(HttpStatus.GONE, "SIGNUP_DRAFT_EXPIRED", exception.getMessage(), null);
	}

	@ExceptionHandler(RateLimitExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleRateLimitExceeded(RateLimitExceededException exception) {
		return error(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", exception.getMessage(), null);
	}

	@ExceptionHandler(InvalidAdminApiKeyException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidAdminApiKey(InvalidAdminApiKeyException exception) {
		return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid admin API key", null);
	}

	@ExceptionHandler(InvalidGoogleTokenException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidGoogleToken(InvalidGoogleTokenException exception) {
		return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", exception.getMessage(), null);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException exception) {
		return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid email or password", null);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
		logger.error("Unexpected application error", exception);
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", null);
	}

	private FieldErrorResponse toFieldErrorResponse(FieldError fieldError) {
		return new FieldErrorResponse(fieldError.getField(), fieldError.getDefaultMessage());
	}

	private ResponseEntity<ApiErrorResponse> error(
			HttpStatus status,
			String code,
			String message,
			List<FieldErrorResponse> fieldErrors) {
		ApiErrorResponse response = new ApiErrorResponse(Instant.now(), status.value(), code, message, fieldErrors);
		return ResponseEntity.status(status).body(response);
	}
}