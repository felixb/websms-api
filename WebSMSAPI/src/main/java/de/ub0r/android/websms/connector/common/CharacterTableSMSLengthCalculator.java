/*
 * Copyright (C) 2009-2012 Felix Bechstein
 * 
 * This file is part of WebSMS.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.websms.connector.common;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.telephony.SmsMessage;

/**
 * This implementation of an SMSLengthCalculator replaces bad characters with a
 * {@link CharacterTable} before calculating the message length. This class does
 * need at least an Android API 4.
 * 
 * @author Felix Bechstein <f@ub0r.de>
 */
public class CharacterTableSMSLengthCalculator implements SMSLengthCalculator {
	/** Automatic generated serial version UID. */
	private static final long serialVersionUID = 750570280586096152L;

	/** {@link CharacterTable} used for replacing bad chars. */
	private final CharacterTable mCT;

	/**
	 * @param table
	 *            {@link CharacterTable} used for replacing bad chars
	 */
	public CharacterTableSMSLengthCalculator(final CharacterTable table) {
		this.mCT = table;
	}

	/**
	 * @param b
	 *            {@link Bundle} for creating a {@link CharacterTable}
	 */
	public CharacterTableSMSLengthCalculator(final Bundle b) {
		this.mCT = new CharacterTable(b);
	}

	@Override
	public int[] calculateLength(final String messageBody,
			final boolean use7bitOnly) {
		return SmsMessage.calculateLength(this.mCT.encodeString(messageBody),
				use7bitOnly);
	}

	// Parcel stuff
	private CharacterTableSMSLengthCalculator(final Parcel parcel) {
		this(parcel.readBundle());
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(final Parcel out, final int flags) {
		out.writeBundle(this.mCT.getBundle());
	}

	public static final Parcelable.Creator<CharacterTableSMSLengthCalculator> CREATOR = new Parcelable.Creator<CharacterTableSMSLengthCalculator>() {
		public CharacterTableSMSLengthCalculator createFromParcel(
				final Parcel in) {
			return new CharacterTableSMSLengthCalculator(in);
		}

		public CharacterTableSMSLengthCalculator[] newArray(final int size) {
			return new CharacterTableSMSLengthCalculator[size];
		}
	};
}
