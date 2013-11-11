package de.ub0r.android.websms.connector.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A simple implementation of an SMSLengthCalculator. Provide an array of
 * message lengths representing the number of characters that can be added each
 * time before an additional SMS will be used.
 * 
 * For example: [160, 149] - here the first 160 characters is just one SMS, the
 * next 149 (for a total of 309) would constitute 2 messages.
 * 
 * Note that the final value implicitly repeats, so the above example is
 * actually [160, 149, 149, ...].
 * 
 * If the provider has a maximum message length and this is set correctly the
 * repeating message length will not be an issue.
 * 
 * @author Fintan Fairmichael
 * 
 */
public class BasicSMSLengthCalculator implements SMSLengthCalculator {
	private static final long serialVersionUID = -2841752540084364776L;

	private final int[] messageLengths;

	/**
	 * 
	 * @param messageLengths
	 *            the message lengths representing the number of characters that
	 *            can be added at each point before an additional message is
	 *            required
	 */
	public BasicSMSLengthCalculator(final int[] messageLengths) {
		this.messageLengths = messageLengths;
	}

	private final int messageLength(final int index) {
		if (index >= this.messageLengths.length) {
			return this.messageLengths[this.messageLengths.length - 1];
		} else {
			// Last message length repeats, if necessary
			return this.messageLengths[index];
		}
	}

	@Override
	public int[] calculateLength(final String messageBody,
			final boolean use7bitOnly) {
		// Currently ignoring use7bitOnly param
		final int length = messageBody.length();

		int numberSMSRequired = 0;
		int charsRemaining = length;
		int remainingTilNextSMS = 0;

		while (true) {
			final int messageLength = this.messageLength(numberSMSRequired);
			numberSMSRequired++;
			if (messageLength >= charsRemaining) {
				remainingTilNextSMS = messageLength - charsRemaining;
				break;
			}
			charsRemaining -= messageLength;
		}
		return new int[] { numberSMSRequired, length, remainingTilNextSMS, 0 };
	}

	// Parcel stuff
	private BasicSMSLengthCalculator(final Parcel parcel) {
		this(parcel.createIntArray());
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel out, final int flags) {
		out.writeIntArray(this.messageLengths);
	}

	public static final Parcelable.Creator<BasicSMSLengthCalculator> CREATOR = new Parcelable.Creator<BasicSMSLengthCalculator>() {
		public BasicSMSLengthCalculator createFromParcel(final Parcel in) {
			return new BasicSMSLengthCalculator(in);
		}

		public BasicSMSLengthCalculator[] newArray(final int size) {
			return new BasicSMSLengthCalculator[size];
		}
	};
}
