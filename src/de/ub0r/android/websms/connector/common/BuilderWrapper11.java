package de.ub0r.android.websms.connector.common;

import android.app.AlertDialog.Builder;
import android.content.Context;

/**
 * Get a {@link Builder} with theme support.
 * 
 * @author flx
 */
final class BuilderWrapper11 {
	/**
	 * Hide constructor.
	 */
	private BuilderWrapper11() {
		// nothing to do
	}

	/**
	 * Get a {@link Builder}.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param theme
	 *            theme
	 * @return {@link Builder}
	 */
	static Builder getBuilder(final Context context, final int theme) {
		return new Builder(context, theme);
	}
}
