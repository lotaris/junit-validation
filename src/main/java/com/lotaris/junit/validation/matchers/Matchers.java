package com.lotaris.junit.validation.matchers;

/**
 * This helper class offers additional, custom Hamcrest matchers for our test
 * assertions.
 *
 * @author Laurent Prevost <laurent.prevost@forbes-digital.com>
 */
public final class Matchers {

	public static ApiErrorResponseObjectMatcher isApiErrorResponseObject() {
		return new ApiErrorResponseObjectMatcher();
	}

	public static ApiErrorResponseObjectMatcher isApiErrorResponseObject(int expectedHttpStatusCode) {
		return new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
	}

//	public static ApiErrorResponseObjectMatcher isApiErrorResponseObject(EApiHttpStatusCodes expectedHttpStatusCode) {
//		return new ApiErrorResponseObjectMatcher(expectedHttpStatusCode);
//	}

	public static ApiErrorsExceptionMatcher isApiErrorsException(int expectedHttpStatusCode) {
		return new ApiErrorsExceptionMatcher(expectedHttpStatusCode);
	}

//	public static ApiErrorsExceptionMatcher isApiErrorsException(EApiHttpStatusCodes expectedHttpStatusCode) {
//		return new ApiErrorsExceptionMatcher(expectedHttpStatusCode);
//	}

	//<editor-fold defaultstate="collapsed" desc="Hidden Constructor">
	private Matchers() {
	}
	//</editor-fold>
}
