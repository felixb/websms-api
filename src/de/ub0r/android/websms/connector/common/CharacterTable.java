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

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

/**
 * Character table is replacing unwanted characters with well behaving ones.
 * 
 * @author Thomas Pilarski <Thomas.Pilarski@gmail.com>, Felix Bechstein
 *         <f@ub0r.de>
 */
public final class CharacterTable {

	/** Mapping. */
	private final Map<String, String> mMap;

	/**
	 * Default constructor.
	 * 
	 * @param map
	 *            {@link Map} holding bad and good characters
	 */
	public CharacterTable(final Map<String, String> map) {
		// this.mMap = new HashMap<String, String>(map.size());
		// this.mMap.putAll(map);
		this.mMap = map;
	}

	/**
	 * Import from {@link Bundle} constructor.
	 * 
	 * @param b
	 *            {@link Bundle} holding bad and good characters
	 */
	public CharacterTable(final Bundle b) {
		this.mMap = new HashMap<String, String>(b.size());
		for (String k : b.keySet()) {
			this.mMap.put(k, b.getString(k));
		}
	}

	/**
	 * Encode {@link String}.
	 * 
	 * @param str
	 *            {@link String}
	 * @return encoded {@link String}
	 */
	public String encodeString(final String str) {
		final int l = str.length();
		final StringBuffer strb = new StringBuffer(l);
		for (int i = 0; i < l; i++) {
			String s = str.substring(i, i + 1);
			String chr = this.mMap.get(s);
			if (chr == null) {
				strb.append(s);
			} else {
				strb.append(chr);
			}
		}
		return strb.toString();
	}

	/**
	 * @return inner {@link Map}
	 */
	public Map<String, String> getMap() {
		return this.mMap;
	}

	/**
	 * @return inner {@link Map} as {@link Bundle}
	 */
	public Bundle getBundle() {
		final Bundle b = new Bundle(this.mMap.size());
		for (String k : this.mMap.keySet()) {
			b.putString(k, this.mMap.get(k));
		}
		return b;
	}

}