package com.forbesdigital.junit.validation;

import com.forbesdigital.jee.validation.IErrorCode;
import com.forbesdigital.jee.validation.IErrorLocationType;
import com.forbesdigital.jee.validation.IValidationContext;
import com.forbesdigital.jee.validation.IValidator;
import com.forbesdigital.jee.validation.SingleObjectOrList;
import com.forbesdigital.jee.validation.preprocessing.IPreprocessingConfig;
import com.forbesdigital.jee.validation.preprocessing.IPreprocessor;
import java.util.List;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * Utility class for tests with Validations.
 *
 * @author Guy Tomoki <guy.tomoki@lotaris.com>
 */
public class ValidationUtils {

	/**
	 * Performs an action on a validation state object in the specified preprocessing chain.
	 *
	 * @param <T> the type of state object
	 * @param preprocessingChain the preprocessing chain
	 * @param stateClass the class of state object
	 * @param stateAction the action to perform
	 */
	public static <T> void useValidationState(IPreprocessor preprocessingChain, final Class<? extends T> stateClass, final IValidationStateAction<T> stateAction) {
		when(preprocessingChain.process(anyObject(), any(IPreprocessingConfig.class))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {

				IPreprocessingConfig config = (IPreprocessingConfig) invocation.getArguments()[1];
				IValidationContext context = config.getValidationContext();

				stateAction.performAction(context.getState(stateClass));

				return true;
			}
		});
	}

	/**
	 * An action on a validation state object. Can be used to retrieve data from or modify a state
	 * object.
	 *
	 * @param <T> the type of state object
	 * @see #useValidationState(com.forbesdigital.jee.validation.preprocessing.IPreprocessor, java.lang.Class, com.forbesdigital.junit.validation.ValidationUtils.IValidationStateAction)
	 */
	public static interface IValidationStateAction<T> {

		/**
		 * Performs an action with the specified state object.
		 *
		 * @param state the state object
		 */
		public void performAction(T state);
	}

	/**
	 * Run all the validators of the preprocessing chain.
	 *
	 * @param preprocessingChain whose validators will be run.
	 * @deprecated use a validation state object and {@link #modifyValidationState(com.forbesdigital.jee.validation.preprocessing.IPreprocessor, java.lang.Class)}
	 */
	@Deprecated
	public static void runValidatorsInPreprocessingChain(IPreprocessor preprocessingChain) {
		when(preprocessingChain.process(anyObject(), any(IPreprocessingConfig.class))).thenAnswer(new Answer<Boolean>() {
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable {
				IPreprocessingConfig config = (IPreprocessingConfig) invocation.getArguments()[1];
				for (IValidator validator : config.getValidators()) {
					validator.collectErrors(invocation.getArguments()[0], config.getValidationContext());
				}
				return true;
			}
		});
	}

	/**
	 * Asserts that validation in the specified preprocessing chain is configured as expected.
	 *
	 * @param preprocessingChain the preprocessing chain
	 * @param expectedPatchValidation whether patch validation should be enabled
	 * @param expectedValidationGroups the expected validation groups (or null if none are expected)
	 * @param expectedValidators the validators that are expected to be applied (or null if none are
	 * expected)
	 */
	public static void assertValidationConfigurationInPreprocessingChain(IPreprocessor preprocessingChain, final boolean expectedPatchValidation, final Class[] expectedValidationGroups, final IValidator... expectedValidators) {
		verify(preprocessingChain, times(1)).process(anyObject(), argThat(new BaseMatcher<IPreprocessingConfig>() {
			private boolean patchValidationMatches;
			private boolean validationGroupsMatch;
			private boolean validatorsMatch;

			@Override
			public boolean matches(Object item) {

				final IPreprocessingConfig config = (IPreprocessingConfig) item;

				// check patch validation
				patchValidationMatches = config.isPatchValidationEnabled() == expectedPatchValidation;

				// check that the number of validation groups is correct
				validationGroupsMatch = config.getValidationGroups().length == (expectedValidationGroups != null ? expectedValidationGroups.length : 0);
				if (validationGroupsMatch && expectedValidationGroups != null) {

					// check each validation group in order
					final int n = expectedValidationGroups.length;
					for (int i = 0; i < n; i++) {
						if (config.getValidationGroups()[i] != expectedValidationGroups[i]) {
							validationGroupsMatch = false;
							break;
						}
					}
				}

				// check that the number of validators is correct
				validatorsMatch = config.getValidators().size() == (expectedValidators != null ? expectedValidators.length : 0);
				if (validatorsMatch && expectedValidators != null) {

					// check each validator in order
					final int n = expectedValidators.length;
					for (int i = 0; i < n; i++) {
						if (config.getValidators().get(i) != expectedValidators[i]) {
							validatorsMatch = false;
						}
					}
				}

				return patchValidationMatches && validationGroupsMatch && validatorsMatch;
			}

			@Override
			public void describeTo(Description description) {

				description.appendText("Preprocessing configuration with");
				description.appendText(" patch validation " + (expectedPatchValidation ? "enabled" : "not enabled"));

				// describe expected validation groups
				if (expectedValidationGroups != null && expectedValidationGroups.length >= 1) {
					description.appendValueList(" and validation groups ", ", ", "", expectedValidationGroups);
				} else {
					description.appendText(" and no validation groups");
				}

				// describe expected validators (their class)
				if (expectedValidators != null && expectedValidators.length >= 1) {
					description.appendValueList(" and validators ", ", ", "", expectedValidators);
				} else {
					description.appendText(" and no validators");
				}
			}
		}));
	}

	/**
	 * Asserts that a preprocessing chain contains the given list of validators.
	 *
	 * <p><strong>This method is deprecated in favor of
	 * <tt>assertValidationConfigurationInPreprocessingChain</tt>. Use it only for legacy resources
	 * that have not yet been refactored to inject validators.</strong></p>
	 *
	 * @param preprocessingChain the preprocessing chain to check
	 * @param validators the list of validators class.
	 * @deprecated use
	 * {@link #assertValidationInPreprocessingChain(com.forbesdigital.jee.validation.preprocessing.IPreprocessor, boolean, java.lang.Class[], com.forbesdigital.jee.validation.IValidator[])}
	 */
	@Deprecated
	public static void assertValidatorsInPreprocessingChain(IPreprocessor preprocessingChain, final Class<? extends IValidator>... validators) {
		verify(preprocessingChain, times(1)).process(anyObject(), argThat(new BaseMatcher<IPreprocessingConfig>() {
			@Override
			public boolean matches(Object item) {
				final IPreprocessingConfig config = (IPreprocessingConfig) item;
				if (config.getValidators().size() != validators.length) {
					return false;
				}
				for (int i = 0; i < validators.length; i++) {
					if (!validators[i].isAssignableFrom(config.getValidators().get(i).getClass())) {
						return false;
					}
				}
				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("ApiPreprocessingContext with the following validators:");
				for (Class className : validators) {
					description.appendText(" " + className.getName());
				}
			}
		}));
	}

	/**
	 * Make a preprocessing chain fail.
	 *
	 * @param preprocessingChain the preprocessing chail to make fail.
	 */
	public static void failPreprocessingChainValidations(IPreprocessor preprocessingChain) {
		when(preprocessingChain.process(anyObject(), any(IPreprocessingConfig.class))).thenAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				IPreprocessingConfig config = (IPreprocessingConfig) args[1];
				IValidationContext context = config.getValidationContext();
				context.addError("bug", null, errorCode(123), "a bug message");
				return true;
			}
		});
	}

	public static IErrorLocationType errorLocationType(final String type) {
		return new IErrorLocationType() {
			@Override
			public String getLocationType() {
				return type;
			}
		};
	}

	public static IErrorCode errorCode(final int code) {
		return new IErrorCode() {
			@Override
			public int getCode() {
				return code;
			}

			@Override
			public int getDefaultHttpStatusCode() {
				return 422;
			}
		};
	}

	public static Matcher<IErrorCode> isErrorCode(final int code) {
		return new BaseMatcher<IErrorCode>() {
			@Override
			public boolean matches(Object item) {
				return item instanceof IErrorCode && ((IErrorCode) item).getCode() == code;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Error code " + code);
			}
		};
	}

	public static Matcher<IErrorLocationType> isErrorLocationType(final String locationType) {
		return new BaseMatcher<IErrorLocationType>() {
			@Override
			public boolean matches(Object item) {
				return item instanceof IErrorLocationType && locationType.equals(((IErrorLocationType) item).getLocationType());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Error locationType " + locationType);
			}
		};
	}

	public static Matcher<IErrorLocationType> isJsonErrorLocationType() {
		return isErrorLocationType("json");
	}

	/**
	 * Checks that no error message has been added to the context.
	 */
	public static void verifyNoErrorAdded(IValidationContext context) {
		verify(context, never()).addError(anyString(), any(IErrorLocationType.class), any(IErrorCode.class), anyString());
		verify(context, never()).addErrorAtCurrentLocation(any(IErrorCode.class), anyString());
	}

	/**
	 * Checks that no validator was used on any object with any of the <tt>validateObject*</tt>
	 * methods.
	 *
	 * @param context the validation context
	 */
	public static void verifyNoValidatorUsed(IValidationContext context) {
		verify(context, never()).validateObject(anyObject(), anyString(), any(IValidator.class));
		verify(context, never()).validateObjectOrList(any(SingleObjectOrList.class), anyString(), any(IValidator.class));
		verify(context, never()).validateObjects(any(List.class), anyString(), any(IValidator.class));
	}
}
