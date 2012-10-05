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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import de.ub0r.android.websms.connector.common.Utils.HttpOptions;

/**
 * AsyncTask to manage IO to a basic WebAPI.
 * 
 * @author flx
 */
public abstract class BasicConnector extends Connector {
	/** Tag for output. */
	private static final String TAG = "bcon";
	/** User Agent. */
	private static final String USERAGENT = // .
	"curl/7.21.3 (x86_64-pc-linux-gnu) "
			+ "libcurl/7.21.3 OpenSSL/0.9.8o zlib/1.2.3.4 libidn/1.18";

	/**
	 * Get the URL used for sending messages.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param d
	 *            {@link ArrayList} of arguments
	 * @return gateway URL for sending
	 */
	protected String getUrlSend(final Context context,
			final ArrayList<BasicNameValuePair> d) {
		return this.getUrlSend(d);
	}

	/**
	 * Get the URL used for sending messages.
	 * 
	 * @param d
	 *            {@link ArrayList} of arguments
	 * @return gateway URL for sending
	 */
	@Deprecated
	protected String getUrlSend(final ArrayList<BasicNameValuePair> d) {
		return null;
	}

	/**
	 * Get the URL used for getting balance.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param d
	 *            {@link ArrayList} of arguments
	 * @return gateway URL for balance update
	 */
	protected String getUrlBalance(final Context context,
			final ArrayList<BasicNameValuePair> d) {
		return this.getUrlBalance(d);
	}

	/**
	 * Get the URL used for getting balance.
	 * 
	 * @param d
	 *            {@link ArrayList} of arguments
	 * @return gateway URL for balance update
	 */
	@Deprecated
	protected String getUrlBalance(final ArrayList<BasicNameValuePair> d) {
		return null;
	}

	/**
	 * Get HTTP method used for transmitting data to the Service.
	 * 
	 * @return true to use POST, false for GET; default implementation returns
	 *         true
	 */
	protected boolean usePost(final ConnectorCommand command) {
		return true;
	}

	/**
	 * Do HTTP basic auth.
	 * 
	 * @return true to do HTTP basic auth
	 */
	protected boolean useBasicAuth() {
		return false;
	}

	/** @return user agent */
	protected String getUserAgent() {
		return USERAGENT;
	}

	/**
	 * Trust any SSL certificates.
	 * 
	 * @return true to trust any SSL certificate, default implementation returns
	 *         false
	 */
	protected boolean trustAllSLLCerts() {
		return false;
	}

	/**
	 * Array of SHA-1 fingerprint hashes of trusted SSL certificates. Only this
	 * certificates will be accepted as trusted.
	 * 
	 * @return array of trusted SSL certificates
	 */
	protected String[] trustedSSLCerts() {
		return null;
	}

	/**
	 * Set encoding used for HTTP GET requests.
	 * 
	 * @return encoding used for HTTP GET requests; default implementation uses
	 *         ISO-8859-15
	 */
	protected String getEncoding() {
		return "ISO-8859-15";
	}

	/**
	 * Set timeout for establishing a connection and waiting for a response from
	 * the server.
	 * 
	 * @return timeout; defaults to no timeout
	 */
	protected int getTimeout() {
		return 0;
	}

	/**
	 * Set maximum number of HTTP connections.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @return number of connections; defaults to system default
	 */
	protected int getMaxHttpConnections(final Context context,
			final ConnectorSpec cs) {
		return 0;
	}

	/**
	 * Set parameter name for username.
	 * 
	 * @return API param for username
	 */
	protected abstract String getParamUsername();

	/**
	 * Set parameter name for password.
	 * 
	 * @return API param for password
	 */
	protected abstract String getParamPassword();

	/**
	 * Set parameter name for recipients.
	 * 
	 * @return API param for recipients
	 */
	protected abstract String getParamRecipients();

	/**
	 * Set paramteter name for text.
	 * 
	 * @return API param for text
	 */
	protected abstract String getParamText();

	/**
	 * Set paramter name for sender id.
	 * 
	 * @return API param for sender
	 */
	protected abstract String getParamSender();

	/**
	 * Set parameter name for send later.
	 * 
	 * @return API param for send later; default implementation returns null
	 */
	protected String getParamSendLater() {
		return null;
	}

	/**
	 * Set paramter name for selecting the sub connector.
	 * 
	 * @return API param name for subconnector; default implementation returns
	 *         null
	 */
	protected String getParamSubconnector() {
		return null;
	}

	/**
	 * Set paramter name for sending message as flash sms.
	 * 
	 * @return API param for flash type; default implementation returns null
	 */
	protected String getParamFlash() {
		return null;
	}

	/**
	 * Set username value.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @return the username for authorization at the WebAPI.
	 */
	protected abstract String getUsername(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs);

	/**
	 * Set password value.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @return the password for authorization at the WebAPI.
	 */
	protected abstract String getPassword(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs);

	/**
	 * Set selected sub connector. Get used subconnector. Default implementation
	 * returns just the param subcon.
	 * 
	 * @param subcon
	 *            id of selected subconnector
	 * @return value for subconnector
	 */
	protected String getSubconnector(final String subcon) {
		return subcon;
	}

	/**
	 * Set message's text. Default implementation returns just the given text.
	 * 
	 * @param text
	 *            message body
	 * @return value for text
	 */
	protected String getText(final String text) {
		return text;
	}

	/**
	 * Set sender id value.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @return value for sender.
	 */
	protected abstract String getSender(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs);

	/**
	 * Set recipients value.
	 * 
	 * @param command
	 *            {@link ConnectorCommand}
	 * @return value for recipients.
	 */
	protected abstract String getRecipients(final ConnectorCommand command);

	/**
	 * Set send later value. Default implementation just returns the plain
	 * value.
	 * 
	 * @param sendLater
	 *            send later
	 * @return value for send later
	 */
	protected String getSendLater(final long sendLater) {
		return String.valueOf(sendLater);
	}

	/**
	 * Parse response. Implement this message to parse the returned text from
	 * HTTP server.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @param htmlText
	 *            HTTP response body
	 */
	protected abstract void parseResponse(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs,
			final String htmlText);

	/**
	 * Parse HTTP response code. Default implementation throws
	 * {@link WebSMSException} if response != HTTP_OK.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param response
	 *            {@link HttpResponse}
	 */
	protected void parseResponseCode(final Context context,
			final HttpResponse response) {
		final int resp = response.getStatusLine().getStatusCode();
		if (resp != HttpURLConnection.HTTP_OK) {
			Log.e(TAG, "HTTP Status Line: "
					+ response.getStatusLine().toString());
			Log.e(TAG, "HTTP Headers:");
			for (Header h : response.getAllHeaders()) {
				Log.e(TAG, h.getName() + ": " + h.getValue());
			}
			try {
				final String htmlText = Utils.stream2str(
						response.getEntity().getContent()).trim();
				Log.e(TAG, "HTTP Body:");
				for (String l : htmlText.split("\n")) {
					Log.e(TAG, l);
				}
			} catch (Exception e) {
				Log.w(TAG, "error getting content", e);
			}
			throw new WebSMSException(context, R.string.error_http,
					String.valueOf(resp));
		}
	}

	/**
	 * Add some {@link BasicNameValuePair}s to arguments before calling the web
	 * service.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @param d
	 *            {@link ArrayList} of arguments
	 */
	protected void addExtraArgs(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs,
			final ArrayList<BasicNameValuePair> d) {
		// default implementation does nothing
	}

	/**
	 * Add a HTTP entity and push it with HTTP POST.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param command
	 *            {@link ConnectorCommand}
	 * @param cs
	 *            {@link ConnectorSpec}
	 * @return {@link HttpEntity}
	 */
	protected HttpEntity addHttpEntity(final Context context,
			final ConnectorCommand command, final ConnectorSpec cs) {
		return null;
	}

	/**
	 * Send data. This method actually calls the web service with all set
	 * parameters. The response is parsed in custom methods implemented by the
	 * actual connector.
	 * 
	 * @param context
	 *            Context
	 * @param command
	 *            ConnectorCommand
	 * @throws IOException
	 *             IOException
	 */
	private void sendData(final Context context, final ConnectorCommand command)
			throws IOException {
		// check network availability
		if (!Utils.isNetworkAvailable(context)) {
			throw new WebSMSNoNetworkException(context);
		}
		HttpOptions o = new HttpOptions(this.getEncoding());
		o.userAgent = this.getUserAgent();
		// get Connection
		final ConnectorSpec cs = this.getSpec(context);
		ArrayList<BasicNameValuePair> d = new ArrayList<BasicNameValuePair>();
		final String text = command.getText();
		if (text != null && text.length() > 0) {
			o.url = this.getUrlSend(context, d);
			final String subCon = command.getSelectedSubConnector();
			addParam(d, this.getParamText(), this.getText(text));
			addParam(d, this.getParamRecipients(), this.getRecipients(command));

			if (command.getFlashSMS()) {
				addParam(d, this.getParamSubconnector(), this.getParamFlash());
			} else {
				addParam(d, this.getParamSubconnector(),
						this.getSubconnector(subCon));
			}

			final String customSender = command.getCustomSender();
			if (customSender == null) {
				addParam(d, this.getParamSender(),
						this.getSender(context, command, cs));
			} else {
				addParam(d, this.getParamSender(), customSender);
			}
			final long sendLater = command.getSendLater();
			final String pSendLater = this.getParamSendLater();
			if (sendLater > 0 && pSendLater != null) {
				d.add(new BasicNameValuePair(pSendLater, this
						.getSendLater(sendLater)));
			}
		} else {
			o.url = this.getUrlBalance(context, d);
		}

		if (!this.useBasicAuth()) {
			addParam(d, this.getParamUsername(),
					this.getUsername(context, command, cs));
			addParam(d, this.getParamPassword(),
					this.getPassword(context, command, cs));
		}

		this.addExtraArgs(context, command, cs, d);

		final String encoding = this.getEncoding();
		if (!this.usePost(command)) {
			o.url = Utils.httpGetParams(o.url, d, encoding);
			d = null;
		} else {
			o.postData = this.addHttpEntity(context, command, cs);
			if (o.postData == null) {
				o.addFormParameter(d);
			} else {
				o.url = Utils.httpGetParams(o.url, d, encoding);
				d = null;
			}
		}
		if (this.useBasicAuth()) {
			o.addBasicAuthHeader(this.getUsername(context, command, cs),
					this.getPassword(context, command, cs));
		}

		o.trustAll = this.trustAllSLLCerts();
		o.knownFingerprints = this.trustedSSLCerts();

		o.timeout = this.getTimeout();
		o.maxConnections = this.getMaxHttpConnections(context, cs);

		Log.d(TAG, "HTTP REQUEST: " + o.url);
		HttpResponse response = Utils.getHttpClient(o);

		this.parseResponseCode(context, response);
		final String htmlText = Utils.stream2str(
				response.getEntity().getContent()).trim();
		Log.d(TAG, "HTTP RESPONSE: " + htmlText);
		this.parseResponse(context, command, cs, htmlText);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doUpdate(final Context context, final Intent intent)
			throws IOException {
		this.sendData(context, new ConnectorCommand(intent));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doSend(final Context context, final Intent intent)
			throws IOException {
		this.sendData(context, new ConnectorCommand(intent));
	}

	/**
	 * Add a {@link BasicNameValuePair} to a {@link ArrayList}.
	 * 
	 * @param d
	 *            {@link ArrayList} of {@link BasicNameValuePair}
	 * @param n
	 *            name
	 * @param v
	 *            value
	 * @return the list
	 */
	protected static ArrayList<BasicNameValuePair> addParam(
			final ArrayList<BasicNameValuePair> d, final String n,
			final String v) {
		if (!TextUtils.isEmpty(n)) {
			d.add(new BasicNameValuePair(n, v));
		}
		return d;
	}
}
