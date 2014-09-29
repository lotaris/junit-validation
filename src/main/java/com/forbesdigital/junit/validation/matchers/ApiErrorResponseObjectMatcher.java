package com.forbesdigital.junit.validation.matchers;

import com.forbesdigital.jee.validation.ApiError;
import com.forbesdigital.jee.validation.ApiErrorResponse;
import com.forbesdigital.jee.validation.IErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * Ensures that an API error response object has the expected HTTP status code and errors.
 *
 * @author Laurent Prevost <laurent.prevost@forbes-digital.com>
 */
public class ApiErrorResponseObjectMatcher extends BaseMatcher<ApiErrorResponse> {

	//<editor-fold defaultstate="collapsed" desc="Static Imports">
	public static ApiErrorResponseObjectMatcher isApiErrorResponseObject() {
		return new ApiErrorResponseObjectMatcher();
	}
	
	public static ApiErrorResponseObjectMatcher isApiErrorResponseObject(int expectedHttpStatusCode) {
		return new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
	}
	//</editor-fold>
	private Integer expectedHttpStatusCode;
	private List<ErrorExpectation> expectedErrors;
	private boolean isNonNullResponse;
	private Integer actualHttpStatusCode;
	private boolean httpStatusCodeMatches;
	private List<Object> invalidErrors;
	private List<Error> actualErrors;
	private List<ErrorExpectation> unmetErrorExpectations;

	public ApiErrorResponseObjectMatcher() {
		expectedErrors = new ArrayList<>();
	}

	public ApiErrorResponseObjectMatcher(int expectedHttpStatusCode) {
		this();
		this.expectedHttpStatusCode = expectedHttpStatusCode;
	}

//	public ApiErrorResponseObjectMatcher(EApiHttpStatusCodes expectedHttpStatusCode) {
//		this();
//		this.expectedHttpStatusCode = expectedHttpStatusCode.getCode();
//	}

	public ApiErrorResponseObjectMatcher withError(int code) {
		return withError(code, null, null, (String) null);
	}

	public ApiErrorResponseObjectMatcher withError(IErrorCode code) {
		return withError(code.getCode(), null, null, (String) null);
	}

	public ApiErrorResponseObjectMatcher withError(int code, String locationType, String location) {
		return withError(code, locationType, location, (String) null);
	}

	public ApiErrorResponseObjectMatcher withError(IErrorCode code, String locationType, String location) {
		return withError(code.getCode(), locationType, location, (String) null);
	}

	public ApiErrorResponseObjectMatcher withError(int code, String locationType, String location, String message) {
		expectedErrors.add(new ErrorExpectation(code, locationType, location, message));
		return this;
	}

	public ApiErrorResponseObjectMatcher withError(IErrorCode code, String locationType, String location, String message) {
		return withError(code.getCode(), locationType, location, message);
	}

	public ApiErrorResponseObjectMatcher withError(int code, String locationType, String location, Pattern messagePattern) {
		expectedErrors.add(new ErrorExpectation(code, locationType, location, messagePattern));
		return this;
	}

	public ApiErrorResponseObjectMatcher withError(IErrorCode code, String locationType, String location, Pattern messagePattern) {
		return withError(code.getCode(), locationType, location, messagePattern);
	}

	@Override
	public boolean matches(Object item) {

		// reset state
		isNonNullResponse = true;
		actualHttpStatusCode = null;
		httpStatusCodeMatches = true;
		invalidErrors = new ArrayList<>();
		actualErrors = new ArrayList<>();
		unmetErrorExpectations = new ArrayList<>();

		// ensure response is not null
		if (item == null) {
			isNonNullResponse = false;
			return false;
		}

		final ApiErrorResponse response = (ApiErrorResponse) item;

		// ensure the HTTP status code is the correct one (if set)
		actualHttpStatusCode = response.getHttpStatusCode();
		httpStatusCodeMatches = expectedHttpStatusCode == null || expectedHttpStatusCode.equals(actualHttpStatusCode);

		for (ApiError error : response.getErrors()) {
			actualErrors.add(new ApiErrorResponseObjectMatcher.Error(error));
		}

		// ensure all expected errors are there
		for (final ApiErrorResponseObjectMatcher.ErrorExpectation expectedError : expectedErrors) {

			boolean found = false;
			for (final ApiErrorResponseObjectMatcher.Error actualError : actualErrors) {
				if (expectedError.matches(actualError)) {
					actualErrors.remove(actualError);
					found = true;
					break;
				}
			}

			if (!found) {
				unmetErrorExpectations.add(expectedError);
			}
		}

		return httpStatusCodeMatches && invalidErrors.isEmpty()
				&& unmetErrorExpectations.isEmpty() && actualErrors.isEmpty();
	}

	@Override
	public void describeTo(Description description) {

		description.appendText("JSON API error response");

		if (expectedHttpStatusCode != null) {
			description.appendText(" with HTTP status code " + expectedHttpStatusCode);
		}

		if (!expectedErrors.isEmpty()) {
			description.appendValueList(" with " + expectedErrors.size() + " errors: ", ", ", "", expectedErrors);
		}
	}

	@Override
	public void describeMismatch(Object item, Description description) {

		if (!isNonNullResponse) {
			description.appendText("response is null");
			return;
		}

		description.appendText("response doesn't match");

		if (!httpStatusCodeMatches) {
			description.appendText(", has HTTP status code " + actualHttpStatusCode);
		}

		if (!invalidErrors.isEmpty()) {
			description.appendValueList(", has " + invalidErrors.size() + " invalid errors (", ", ", ")", invalidErrors);
		}

		if (!unmetErrorExpectations.isEmpty()) {
			description.appendValueList(", is missing " + unmetErrorExpectations.size() + " expected errors (", ", ", ")", unmetErrorExpectations);
		}

		if (!actualErrors.isEmpty()) {
			description.appendValueList(", has " + actualErrors.size() + " additional unexpected errors (", ", ", ")", actualErrors);
		}
	}

	protected static class Error {

		private Integer code;
		private String locationType;
		private String location;
		private String message;

		public Error(ApiError error) {
			this.code = error.getNumericCode();
			this.locationType = error.getLocationTypeAsString();
			this.location = error.getLocation();
			this.message = error.getMessage();
		}

		public Integer getCode() {
			return code;
		}

		public String getLocationType() {
			return locationType;
		}
		
		public String getLocation() {
			return location;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String toString() {

			final StringBuilder builder = new StringBuilder();
			builder.append("code=").append(code);

			if (locationType != null) {
				builder.append(", locationType=").append(locationType);
			}

			if (location != null) {
				builder.append(", location=").append(location);
			}

			builder.append(", message=").append(message);

			return builder.toString();
		}
	}

	private static class ErrorExpectation {

		private int code;
		private String locationType;
		private String location;
		private String message;
		private Pattern messagePattern;

		public ErrorExpectation(int code, String locationType, String location, String message) {
			this.code = code;
			this.locationType = locationType;
			this.location = location;
			this.message = message;
		}

		public ErrorExpectation(int code, String locationType, String location, Pattern messagePattern) {
			this.code = code;
			this.locationType = locationType;
			this.location = location;
			this.messagePattern = messagePattern;
		}

		@Override
		public String toString() {

			final StringBuilder builder = new StringBuilder();
			builder.append("code=").append(code);

			if (locationType != null) {
				builder.append(", locationType=").append(locationType);
			} else {
				builder.append(", no locationType");
			}

			if (location != null) {
				builder.append(", location=").append(location);
			} else {
				builder.append(", no location");
			}

			if (message != null) {
				builder.append(", message=").append(message);
			} else if (messagePattern != null) {
				builder.append(", message~=").append(messagePattern.toString());
			} else {
				builder.append(", non-blank message");
			}

			return builder.toString();
		}

		public boolean matches(ApiErrorResponseObjectMatcher.Error error) {

			if (error.getCode() != null && code != error.getCode()) {
				return false;
			} 
			
			if (locationType != null ? !locationType.equals(error.getLocationType()) : error.getLocationType() != null) {
				return false;
			}
			
			if (location != null ? !location.equals(error.getLocation()) : error.getLocation() != null) {
				return false;
			}

			if (message != null) {
				return message.equals(error.getMessage());
			} else if (messagePattern != null) {
				return error.getMessage() != null && messagePattern.matcher(error.getMessage()).matches();
			} else {
				return error.getMessage() != null && !error.getMessage().isEmpty();
			}
		}
	}
}
