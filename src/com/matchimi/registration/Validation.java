package com.matchimi.registration;

import static com.matchimi.CommonUtilities.REGISTERFORM_INVALID_EMAIL;
import static com.matchimi.CommonUtilities.REGISTERFORM_INVALID_PASSWORD;
import static com.matchimi.CommonUtilities.REGISTERFORM_REQUIRED_MSG;
import android.widget.EditText;

public class Validation {

	public final static boolean isValidEmail(EditText editText) {
		boolean required = true;

		if (editText == null) {
			return false;
		} else {
			String text = editText.getText().toString().trim();
			// clearing the error, if it was previously set by some other values
			editText.setError(null);

			// text required and editText is blank, so return false
			if (required && !hasText(editText))
				return false;

			if (required
					&& !android.util.Patterns.EMAIL_ADDRESS.matcher(text)
							.matches()) {
				editText.setError(REGISTERFORM_INVALID_EMAIL);
				return false;
			}
			;
		}

		return required;
	}

	public final static boolean isValidPassword(EditText editText) {
		boolean required = true;

		if (editText == null) {
			return false;
		} else {
			String text = editText.getText().toString().trim();
			// clearing the error, if it was previously set by some other values
			editText.setError(null);

			// text required and editText is blank, so return false
			if (required && !hasText(editText))
				return false;

			if (required && text.length() < 4) {
				editText.setError(REGISTERFORM_INVALID_PASSWORD);
				return false;
			}
			;
		}

		return required;
	}

	// check the input field has any text or not
	// return true if it contains text otherwise false
	public static boolean hasText(EditText editText) {

		String text = editText.getText().toString().trim();
		editText.setError(null);

		// length 0 means there is no text
		if (text.length() == 0) {
			editText.setError(REGISTERFORM_REQUIRED_MSG);
			return false;
		}

		return true;
	}

}