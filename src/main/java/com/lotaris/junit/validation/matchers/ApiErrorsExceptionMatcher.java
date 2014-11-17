package com.lotaris.junit.validation.matchers;

import com.lotaris.jee.validation.ApiErrorsException;
import com.lotaris.jee.validation.IErrorCode;
import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Ensures that an API errors exception contains an API error response with the expected HTTP status
 * code and errors.
 *
 * @author Laurent Prevost <laurent.prevost@forbes-digital.com>
 */
public class ApiErrorsExceptionMatcher extends BaseMatcher<ApiErrorsException> {

	//<editor-fold defaultstate="collapsed" desc="Static Imports">
	public static ApiErrorsExceptionMatcher isApiErrorsException(int expectedHttpStatusCode) {
		return new ApiErrorsExceptionMatcher(expectedHttpStatusCode);
	}
	
//	public static ApiErrorsExceptionMatcher isApiErrorsException(EApiHttpStatusCodes expectedHttpStatusCode) {
//		return new ApiErrorsExceptionMatcher(expectedHttpStatusCode);
//	}
	
	//</editor-fold>
	private boolean hasErrorResponse;
	private ApiErrorResponseObjectMatcher errorResponseMatcher;

	public ApiErrorsExceptionMatcher(int expectedHttpStatusCode) {
		errorResponseMatcher = new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
	}

//	public ApiErrorsExceptionMatcher(EApiHttpStatusCodes expectedHttpStatusCode) {
//		errorResponseMatcher = new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
//	}

	public ApiErrorsExceptionMatcher withError(int code) {
		errorResponseMatcher.withError(code, null, null, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(IErrorCode code) {
		errorResponseMatcher.withError(code.getCode(), null, null, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String locationType, String location) {
		errorResponseMatcher.withError(code, locationType, location, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(IErrorCode code, String locationType, String location) {
		errorResponseMatcher.withError(code.getCode(), locationType, location, (String) null);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String locationType, String location, String message) {
		errorResponseMatcher.withError(code, locationType, location, message);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(IErrorCode code, String locationType, String location, String message) {
		errorResponseMatcher.withError(code.getCode(), locationType, location, message);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(int code, String locationType, String location, Pattern messagePattern) {
		errorResponseMatcher.withError(code, locationType, location, messagePattern);
		return this;
	}

	public ApiErrorsExceptionMatcher withError(IErrorCode code, String locationType, String location, Pattern messagePattern) {
		errorResponseMatcher.withError(code.getCode(), locationType, location, messagePattern);
		return this;
	}

	@Override
	public boolean matches(Object item) {

		hasErrorResponse = true;

		if (item == null) {
			return false;
		}

		final ApiErrorsException exception = (ApiErrorsException) item;
		if (exception.getErrorResponse() == null) {
			hasErrorResponse = false;
			return false;
		}

		return errorResponseMatcher.matches(exception.getErrorResponse());
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("API errors exception with ");
		errorResponseMatcher.describeTo(description);
	}

	@Override
	public void describeMismatch(Object item, Description description) {
		if (!hasErrorResponse) {
			description.appendText("exception has no error response");
			return;
		}
		errorResponseMatcher.describeMismatch(((ApiErrorsException) item).getErrorResponse(), description);
	}
}
