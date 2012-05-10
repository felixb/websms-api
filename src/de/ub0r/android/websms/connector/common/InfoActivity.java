/*
 * Copyright (C) 2010 Felix Bechstein
 * 
 * This file is part of ConnectorTest.
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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;

/**
 * This is the default Activity launched from android market after install of
 * any plugin. This activity simply says: "i am a plugin for WebSMS".
 * 
 * @author flx
 */
public final class InfoActivity extends Activity {
	/** Tag for debug output. */
	private static final String TAG = "Info";

	/** WebSMS package name */
	private static final String PACKAGENAME_WEBSMS = "de.ub0r.android.websms";

	/** mysms package name */
	private static final String PACKAGENAME_MYSMS = "com.mysms.android.sms";

	/** Info text shown to user. */
	private static final String INFO_TEXT = "This is a WebSMS Connector."
			+ "\nThe only way to use it, is lauching it with WebSMS or mysms.";

	/** Button label: websms. */
	private static final String BTN_WEBSMS = "WebSMS";

	/** Button label: mysms. */
	private static final String BTN_MYSMS = "mysms";

	/** Link to WebSMS in android market. */
	private Intent intentWebsms = new Intent(Intent.ACTION_VIEW, Uri.parse(// .
			"market://search?q=pname:" + PACKAGENAME_WEBSMS));

	/** Link to mysms in android market. */
	private Intent intentMysms = new Intent(Intent.ACTION_VIEW, Uri.parse(// .
			"market://search?q=pname:" + PACKAGENAME_MYSMS));

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onResume() {
		super.onResume();
		Builder b;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			b = BuilderWrapper11.getBuilder(this,
					android.R.style.Theme_Holo_Light_Dialog);
		} else {
			b = new Builder(this);
		}
		b.setTitle(this.getTitle());
		final String pkg = this.getPackageName();
		final int info = this.getResources().getIdentifier("info_text",
				"string", pkg);
		int icon = this.getResources().getIdentifier("app_icon", "drawable",
				pkg);
		if (icon == 0) {
			icon = this.getResources().getIdentifier("icon", "drawable", pkg);
		}
		Log.d(TAG, "resID.icon=" + icon);
		Log.d(TAG, "resID.info=" + info);
		if (icon > 0) {
			b.setIcon(icon);
		}
		if (info > 0) {
			b.setMessage(info);
		} else {
			b.setMessage(INFO_TEXT);
		}
		b.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						InfoActivity.this.finish();
					}
				});

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		List<ResolveInfo> ris = this.getPackageManager().queryIntentActivities(
				intent, 0);
		for (ResolveInfo ri : ris) {
			if (ri.activityInfo == null) {
				continue;
			}
			if (ri.activityInfo.packageName.equals(PACKAGENAME_WEBSMS)) {
				this.intentWebsms = new Intent(Intent.ACTION_MAIN);
				this.intentWebsms.addCategory(Intent.CATEGORY_LAUNCHER);
				this.intentWebsms.setClassName(ri.activityInfo.packageName,
						ri.activityInfo.name);
			} else if (ri.activityInfo.packageName.equals(PACKAGENAME_MYSMS)) {
				this.intentMysms = new Intent(Intent.ACTION_MAIN);
				this.intentMysms.addCategory(Intent.CATEGORY_LAUNCHER);
				this.intentMysms.setClassName(ri.activityInfo.packageName,
						ri.activityInfo.name);
			}
		}
		b.setPositiveButton(BTN_WEBSMS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					InfoActivity.this.intentWebsms
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					InfoActivity.this
							.startActivity(InfoActivity.this.intentWebsms);
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "no market", e);
				}
				InfoActivity.this.finish();
			}
		});
		b.setNeutralButton(BTN_MYSMS, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				try {
					InfoActivity.this.intentMysms
							.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					InfoActivity.this
							.startActivity(InfoActivity.this.intentMysms);
				} catch (ActivityNotFoundException e) {
					Log.e(TAG, "no market", e);
				}
				InfoActivity.this.finish();
			}
		});
		b.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(final DialogInterface dialog) {
				InfoActivity.this.finish();
			}
		});
		b.setCancelable(true);
		b.show();
	}
}
