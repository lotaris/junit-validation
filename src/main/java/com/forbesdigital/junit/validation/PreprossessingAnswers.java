package com.forbesdigital.junit.validation;

import com.forbesdigital.jee.validation.IErrorCode;
import com.forbesdigital.jee.validation.preprocessing.IPreprocessingConfig;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Provides an easy way to build answers for the Preprocessing action in the
 * API unit tests.
 * 
 * @author Laurent Prevost <laurent.prevost@forbes-digital.com>
 */
public class PreprossessingAnswers  {

	public static class PreprossessingWithErrorAnswer implements Answer {
		
		private final IErrorCode errorCode;
		private final String message;
		
		//<editor-fold defaultstate="collapsed" desc="Constructors">
		public PreprossessingWithErrorAnswer(IErrorCode errorCode, String message) {
			this.errorCode = errorCode;
			this.message = message;
		}
		//</editor-fold>
		
		@Override
		public Boolean answer(InvocationOnMock invocation) throws Throwable {
			((IPreprocessingConfig) invocation.getArguments()[1]).getValidationContext().addError(null, null, errorCode, message);
			return true;
		}
	}
}
