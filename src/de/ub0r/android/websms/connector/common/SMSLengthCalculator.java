package de.ub0r.android.websms.connector.common;

import java.io.Serializable;

import android.os.Parcelable;

/**
 * A simple interface for computing the length of a message based on the message
 * body. This is used for informing users how many characters they have
 * remaining before an additional message is required, as well as how many
 * messages they are currently using.
 * 
 * @author Fintan Fairmichael
 * 
 */
public interface SMSLengthCalculator extends Serializable, Parcelable {
	int[] calculateLength(final String messageBody, final boolean use7bitOnly);
}
