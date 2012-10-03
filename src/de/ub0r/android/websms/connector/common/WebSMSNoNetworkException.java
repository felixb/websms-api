/*
 * Copyright (C) 2010 Felix Bechstein
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

import android.content.Context;

/**
 * No network available.
 */
public class WebSMSNoNetworkException extends WebSMSException {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8849042093428089066L;

	/**
	 * Create a new WebSMSException.
	 * 
	 * @param c
	 *            Context to resolve resource
	 */
	public WebSMSNoNetworkException(final Context c) {
		super(c.getString(R.string.no_network));
	}

}
