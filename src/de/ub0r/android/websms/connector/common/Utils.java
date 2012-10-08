/*
 * Copyright (C) 2010-2012 Felix Bechstein
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * General Utils calls.
 * 
 * @author flx
 */
public final class Utils {
	/** Tag for output. */
	private static final String TAG = "utl";

	/** Standard buffer size. */
	public static final int BUFSIZE = 32768;

	/** Gzip. */
	private static final String GZIP = "gzip";
	/** Accept-Encoding. */
	private static final String ACCEPT_ENCODING = "Accept-Encoding";

	/** Default port for HTTP. */
	private static final int PORT_HTTP = 80;
	/** Default port for HTTPS. */
	private static final int PORT_HTTPS = 443;

	/** Preference's name: use default sender. */
	public static final String PREFS_USE_DEFAULT_SENDER = "use_default_sender";
	/** Preference's name: custom sender. */
	public static final String PREFS_CUSTOM_SENDER = "custom_sender";

	/** Resturn only matching line in stream2str(). */
	public static final int ONLY_MATCHING_LINE = -2;

	/** Common {@link DefaultHttpClient}. */
	private static DefaultHttpClient httpClient = null;

	/** Log level. */
	private static boolean verboseLog = false;

	/**
	 * Options passed to getHttpClient().
	 * 
	 * @author flx
	 */
	public static class HttpOptions {
		/** URL to open. */
		public String url = null;
		/** Cookies to transmit. */
		public ArrayList<Cookie> cookies = null;
		/** HTTP POST data. */
		public HttpEntity postData = null;
		/** additional HTTP headers. */
		public ArrayList<Header> headers = null;
		/** Useragent. */
		public String userAgent = null;
		/** Referer. */
		public String referer = null;
		/** Encoding. Default is "ISO-8859-15". */
		public final String encoding;
		/** Trust all SSL certificates; only used on first call! */
		public boolean trustAll = false;
		/**
		 * Finger prints that are known to be valid; only used on first call!
		 * Only used if {@code trustAll == false}
		 */
		public String[] knownFingerprints = null;
		/** Connection and socket timeout. */
		public int timeout = 0;
		/** Max number of connections. */
		public int maxConnections = 0;

		/** Default Constructor. */
		public HttpOptions() {
			this(null);
		}

		/**
		 * Default Constructor.
		 * 
		 * @param e
		 *            encoding
		 */
		public HttpOptions(final String e) {
			if (TextUtils.isEmpty(e)) {
				this.encoding = "ISO-8859-15";
			} else {
				this.encoding = e;
			}
		}

		/** Clear {@link HttpOptions} to reuse it. */
		public void clear() {
			this.url = null;
			this.postData = null;
		}

		/**
		 * Add HTTP basic auth header to list of headers.
		 * 
		 * @param user
		 *            user
		 * @param password
		 *            password
		 * @return HTTP basic auth header
		 */
		public final Header addBasicAuthHeader(final String user,
				final String password) {
			Header h = new BasicHeader("Authorization", "Basic "
					+ Base64Coder.encodeString(user + ":" + password));
			if (this.headers == null) {
				this.headers = new ArrayList<Header>();
			}
			this.headers.add(h);
			return h;
		}

		/**
		 * Add form data as {@link HttpEntity}.
		 * 
		 * @param formData
		 *            form data
		 * @return {@link HttpEntity}
		 * @throws UnsupportedEncodingException
		 *             UnsupportedEncodingException
		 */
		public final HttpEntity addFormParameter(
				final List<BasicNameValuePair> formData)
				throws UnsupportedEncodingException {
			HttpEntity he = null;
			if (formData != null) {
				he = new UrlEncodedFormEntity(formData, this.encoding);
				this.postData = he;
			}
			return he;
		}

		/**
		 * Add {@link JSONObject} as {@link HttpEntity}.
		 * 
		 * @param json
		 *            {@link JSONObject} form data
		 * @return {@link HttpEntity}
		 * @throws UnsupportedEncodingException
		 *             UnsupportedEncodingException
		 */
		public final HttpEntity addJson(final JSONObject json)
				throws UnsupportedEncodingException {
			StringEntity he = new StringEntity(json.toString(), this.encoding);
			he.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));

			if (this.headers == null) {
				this.headers = new ArrayList<Header>();
			}
			this.headers
					.add(new BasicHeader("Content-Type", "application/json"));
			this.postData = he;
			return he;
		}
	}

	/**
	 * {@link HttpEntityWrapper} to wrap giziped content.
	 * 
	 * @author flx
	 */
	public static final class GzipDecompressingEntity // .
			extends HttpEntityWrapper {
		/**
		 * Default Constructor.
		 * 
		 * @param entity
		 *            {@link HttpEntity}
		 */
		public GzipDecompressingEntity(final HttpEntity entity) {
			super(entity);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public InputStream getContent() throws IOException {
			Log.d(TAG, "unzip content");
			InputStream wrappedin = this.wrappedEntity.getContent();
			return new GZIPInputStream(wrappedin);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getContentLength() {
			return -1;
		}
	}

	/**
	 * No Constructor needed here.
	 */
	private Utils() {
		return;
	}

	/**
	 * Set log level.
	 * 
	 * @param enable
	 *            true to show potentially privacy relevant traffic
	 */
	public static void setVerboseLog(final boolean enable) {
		verboseLog = enable;
	}

	/**
	 * Get custom sender from preferences by users choice. Else: default sender
	 * is selected.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param defSender
	 *            default Sender
	 * @return selected Sender
	 */
	public static String getSender(final Context context, // .
			final String defSender) {
		if (context == null) {
			return defSender;
		}
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (p.getBoolean(PREFS_USE_DEFAULT_SENDER, true)) {
			return defSender;
		}
		final String s = p.getString(PREFS_CUSTOM_SENDER, "");
		if (s == null || s.length() == 0) {
			return defSender;
		}
		return s;
	}

	/**
	 * Get custom sender number from preferences by users choice. Else: default
	 * sender is selected.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param defSender
	 *            default Sender
	 * @return selected Sender
	 */
	public static String getSenderNumber(final Context context, // .
			final String defSender) {
		if (context == null) {
			return defSender;
		}
		final SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		if (p.getBoolean(PREFS_USE_DEFAULT_SENDER, true)) {
			return defSender;
		}
		final String s = p.getString(PREFS_CUSTOM_SENDER, "");
		if (s == null || s.length() == 0) {
			return defSender;
		}
		final String sn = s.replaceAll("(\\+|[0-9])", "");
		if (sn.length() > 0) {
			Log.d(TAG, "fall back to default numer: " + sn);
			return defSender;
		}
		return s;
	}

	/**
	 * Parse a String of "name <number>, name <number>, number, ..." to an array
	 * of "name <number>".
	 * 
	 * @param recipients
	 *            recipients
	 * @return array of recipients
	 */
	public static String[] parseRecipients(final String recipients) {
		String s = recipients.trim();
		if (s.endsWith(",")) {
			s = s.substring(0, s.length() - 1);
		}
		ArrayList<String> ret = new ArrayList<String>();
		String[] ss = s.split(",");
		String r = null;
		for (String rr : ss) {
			if (r == null) {
				r = rr;
			} else {
				r += "," + rr;
			}
			if (rr.contains("0") || rr.contains("1") || rr.contains("2")
					|| rr.contains("3") || rr.contains("4") || rr.contains("5")
					|| rr.contains("6") || rr.contains("7") || rr.contains("8")
					|| rr.contains("9")) {
				r = r.trim();
				final String na = getRecipientsName(r);
				final String nu = cleanRecipient(getRecipientsNumber(r));
				if (na != null && na.trim().length() > 0) {
					r = na + " <" + nu + ">";
				} else {
					r = nu;
				}
				ret.add(r);
				r = null;
			}
		}
		return ret.toArray(new String[0]);
	}

	/**
	 * Join an array of recipients separated with separator.
	 * 
	 * @param recipients
	 *            recipients
	 * @param separator
	 *            separator
	 * @return joined recipients
	 */
	public static String joinRecipients(final String[] recipients,
			final String separator) {
		if (recipients == null) {
			return null;
		}
		final int e = recipients.length;
		if (e == 0) {
			return null;
		}
		final StringBuilder buf = new StringBuilder(recipients[0]);
		for (int i = 1; i < e; i++) {
			buf.append(separator);
			buf.append(recipients[i]);
		}
		return buf.toString();
	}

	/**
	 * Join an array of recipients separated with separator, stripped to only
	 * contain numbers.
	 * 
	 * @param recipients
	 *            recipients
	 * @param separator
	 *            separator
	 * @param oldFormat
	 *            Use old international format. E.g. 0049, not +49.
	 * @return joined recipients
	 */
	public static String joinRecipientsNumbers(final String[] recipients,
			final String separator, final boolean oldFormat) {
		if (recipients == null) {
			return null;
		}
		final int e = recipients.length;
		if (e == 0) {
			return null;
		}
		final StringBuilder buf = new StringBuilder();
		if (oldFormat) {
			buf.append(international2oldformat(// .
			getRecipientsNumber(recipients[0])));
		} else {
			buf.append(getRecipientsNumber(recipients[0]));
		}
		for (int i = 1; i < e; i++) {
			buf.append(separator);
			if (oldFormat) {
				buf.append(international2oldformat(// .
				getRecipientsNumber(recipients[i])));
			} else {
				buf.append(getRecipientsNumber(recipients[i]));
			}
		}
		return buf.toString();
	}

	/**
	 * Get a recipient's number.
	 * 
	 * @param recipient
	 *            recipient
	 * @return recipient's number
	 */
	public static String getRecipientsNumber(final String recipient) {
		final int i = recipient.lastIndexOf('<');
		if (i != -1) {
			final int j = recipient.indexOf('>', i);
			if (j > 0) {
				return recipient.substring(i + 1, j);
			}
		}
		return recipient;
	}

	/**
	 * Get a recipient's name.
	 * 
	 * @param recipient
	 *            recipient
	 * @return recipient's name
	 */
	public static String getRecipientsName(final String recipient) {
		final int i = recipient.lastIndexOf('<');
		if (i > 0) {
			return recipient.substring(0, i - 1).trim();
		}
		return recipient;
	}

	/**
	 * Clean recipient's phone number from [ -.()<>].
	 * 
	 * @param recipient
	 *            recipient's mobile number
	 * @return clean number
	 */
	public static String cleanRecipient(final String recipient) {
		if (TextUtils.isEmpty(recipient)) {
			return "";
		}
		String n;
		int i = recipient.indexOf("<");
		int j = recipient.indexOf(">");
		if (i != -1 && i < j) {
			n = recipient.substring(i, j);
		} else {
			n = recipient;
		}
		return n.replaceAll("[^*#+0-9]", "") // .
				.replaceAll("^[*#][0-9]*#", "");
	}

	/**
	 * Convert international number to national.
	 * 
	 * @param defPrefix
	 *            default prefix
	 * @param number
	 *            international number
	 * @return national number
	 */
	public static String international2national(final String defPrefix,
			final String number) {
		if (number.startsWith(defPrefix)) {
			return '0' + number.substring(defPrefix.length());
		} else if (number.startsWith("00" + defPrefix.substring(1))) {
			return '0' + number.substring(defPrefix.length() + 1);
		}
		return number;
	}

	/**
	 * Convert national number to international. Old format internationals were
	 * converted to new format.
	 * 
	 * @param defPrefix
	 *            default prefix
	 * @param number
	 *            national number
	 * @return international number
	 */
	public static String national2international(final String defPrefix,
			final String number) {
		if (number.startsWith("+")) {
			return number;
		} else if (number.startsWith("00")) {
			return "+" + number.substring(2);
		} else if (number.startsWith("0")) {
			return defPrefix + number.substring(1);
		} else if (defPrefix.length() > 1
				&& number.startsWith(defPrefix.substring(1))) {
			return "+" + number;
		}
		return defPrefix + number;
	}

	/**
	 * Convert national number to international.
	 * 
	 * @param defPrefix
	 *            default prefix
	 * @param number
	 *            national numbers
	 * @return international numbers
	 */
	public static String[] national2international(final String defPrefix,
			final String[] number) {
		if (number == null || number.length == 0) {
			return null;
		}
		final int l = number.length;
		String[] n = new String[l];
		for (int i = 0; i < l; i++) {
			if (!TextUtils.isEmpty(number[i])) {
				n[i] = national2international(defPrefix,
						getRecipientsNumber(number[i]));
			}
		}
		return n;
	}

	/**
	 * Convert international number to old format. Eg. +49123 to 0049123
	 * 
	 * @param number
	 *            international number starting with +
	 * @return international number in old format starting with 00
	 */
	public static String international2oldformat(final String number) {
		if (number.startsWith("+")) {
			return "00" + number.substring(1);
		}
		return number;
	}

	/**
	 * Print all cookies from {@link CookieStore} to {@link String}.
	 * 
	 * @param client
	 *            {@link DefaultHttpClient}
	 * @return {@link Cookie}s formated for debug out
	 */
	private static String getCookies(final DefaultHttpClient client) {
		String ret = "cookies:";
		for (Cookie cookie : httpClient.getCookieStore().getCookies()) {
			ret += "\n" + cookie.getName() + ": " + cookie.getValue();
		}
		ret += "\nend of cookies";
		return ret;
	}

	/**
	 * Print all {@link Header}s from {@link HttpRequest} to {@link String}.
	 * 
	 * @param request
	 *            {@link HttpRequest}
	 * @return {@link Header}s formated for debug out
	 */
	private static String getHeaders(final HttpRequest request) {
		String ret = "headers:";
		for (Header h : request.getAllHeaders()) {
			ret += "\n" + h.getName() + ": " + h.getValue();
		}
		ret += "\nend of headers";
		return ret;
	}

	/**
	 * Get {@link Cookie}s stored in static {@link CookieStore}.
	 * 
	 * @return {@link ArrayList} of {@link Cookie}s
	 */
	public static ArrayList<Cookie> getCookies() {
		if (httpClient == null) {
			return null;
		}
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		if (cookies == null || cookies.size() == 0) {
			return null;
		}
		ArrayList<Cookie> ret = new ArrayList<Cookie>(cookies.size());
		ret.addAll(cookies);
		return ret;
	}

	/**
	 * Get the number of {@link Cookie}s stored in static {@link CookieStore}.
	 * 
	 * @return number of {@link Cookie}s
	 */
	public static int getCookieCount() {
		if (httpClient == null) {
			return 0;
		}
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		if (cookies == null) {
			return 0;
		}
		return cookies.size();
	}

	/**
	 * Get cookies as {@link String}.
	 * 
	 * @return cookies
	 */
	public static String getCookiesAsString() {
		if (httpClient == null) {
			return null;
		}
		return getCookies(httpClient);
	}

	/**
	 * Clear internal cookie cache.
	 */
	public static void clearCookies() {
		if (httpClient != null) {
			final CookieStore cs = httpClient.getCookieStore();
			if (cs != null) {
				cs.clear();
			}
		}
	}

	/**
	 * Get a fresh HTTP-Connection.
	 * 
	 * @param o
	 *            {@link HttpOptions}
	 * @return the connection
	 * @throws IOException
	 *             IOException
	 */
	public static HttpResponse getHttpClient(final HttpOptions o)
			throws IOException {
		if (verboseLog) {
			Log.d(TAG, "HTTPClient URL: " + o.url);
		} else {
			Log.d(TAG, "HTTPClient URL: " + o.url.replaceFirst("\\?.*", ""));
		}

		if (httpClient == null) {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", new PlainSocketFactory(),
					PORT_HTTP));
			SocketFactory httpsSocketFactory;
			if (o.trustAll) {
				httpsSocketFactory = new FakeSocketFactory();
			} else if (o.knownFingerprints != null
					&& o.knownFingerprints.length > 0) {
				httpsSocketFactory = new FakeSocketFactory(o.knownFingerprints);
			} else {
				httpsSocketFactory = SSLSocketFactory.getSocketFactory();
			}
			registry.register(new Scheme("https", httpsSocketFactory,
					PORT_HTTPS));
			HttpParams params = new BasicHttpParams();

			HttpConnectionParams.setConnectionTimeout(params, o.timeout);
			HttpConnectionParams.setSoTimeout(params, o.timeout);

			if (o.maxConnections > 0) {
				ConnManagerParams.setMaxConnectionsPerRoute(params,
						new ConnPerRoute() {
							public int getMaxForRoute(final HttpRoute httproute) {
								return o.maxConnections;
							}
						});
			}

			httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(
					params, registry), params);

			httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
				public void process(final HttpResponse response,
						final HttpContext context) throws HttpException,
						IOException {
					HttpEntity entity = response.getEntity();
					Header contentEncodingHeader = entity.getContentEncoding();
					if (contentEncodingHeader != null) {
						HeaderElement[] codecs = contentEncodingHeader
								.getElements();
						for (HeaderElement codec : codecs) {
							if (codec.getName().equalsIgnoreCase(GZIP)) {
								response.setEntity(new GzipDecompressingEntity(
										response.getEntity()));
								return;
							}
						}
					}
				}
			});
		}
		if (o.cookies != null && o.cookies.size() > 0) {
			final int l = o.cookies.size();
			CookieStore cs = httpClient.getCookieStore();
			for (int i = 0; i < l; i++) {
				cs.addCookie(o.cookies.get(i));
			}
		}
		// . Log.d(TAG, getCookies(httpClient));

		HttpRequestBase request;
		if (o.postData == null) {
			request = new HttpGet(o.url);
		} else {
			HttpPost pr = new HttpPost(o.url);
			pr.setEntity(o.postData);
			// . Log.d(TAG, "HTTPClient POST: " + postData);
			request = pr;
		}
		request.addHeader("Accept", "*/*");
		request.addHeader(ACCEPT_ENCODING, GZIP);

		if (o.referer != null) {
			request.setHeader("Referer", o.referer);
			if (verboseLog) {
				Log.d(TAG, "HTTPClient REF: " + o.referer);
			}
		}

		if (o.userAgent != null) {
			request.setHeader("User-Agent", o.userAgent);
			if (verboseLog) {
				Log.d(TAG, "HTTPClient AGENT: " + o.userAgent);
			}
		}

		addHeaders(request, o.headers);

		if (verboseLog) {
			Log.d(TAG, "HTTP " + request.getMethod() + " " + request.getURI());
			Log.d(TAG, getHeaders(request));
			if (request instanceof HttpPost) {
				Log.d(TAG, "");
				Log.d(TAG, ((HttpPost) request).getEntity().getContent());
			}
		}
		return httpClient.execute(request);
	}

	/**
	 * Add headers to the Request.
	 * 
	 * @param request
	 *            Request to be added headers to
	 * @param headers
	 *            Headers to add
	 */
	private static void addHeaders(final HttpRequestBase request,
			final ArrayList<Header> headers) {
		if (headers == null) {
			return;
		}

		for (Header h : headers) {
			request.addHeader(h);
		}
	}

	/**
	 * Shutdown and forget the cached HttpClient. This causes the client to be
	 * re-created with the used settings on the next call to the
	 * getHttpClient()-family of functions. The caller has to use this method
	 * before calling a getHttpClient() with new trustAll or fingerprints.
	 * Calling this function while no cached Client exits does nothing.
	 */
	public static void resetHttpClient() {
		if (httpClient == null) {
			return;
		}

		httpClient.getConnectionManager().shutdown();
		httpClient = null;
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is) throws IOException {
		return stream2str(is, 0, -1, null);
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from param charset to read the
	 *            {@link InputStream}. Can be null.
	 * @param charset
	 *            charset to be used to read {@link InputStream}. Can be null.
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is, final String charset)
			throws IOException {
		return stream2str(is, 0, -1, null);
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from
	 * @param start
	 *            first characters of stream that should be fetched. Set to 0,
	 *            if nothing should be skipped.
	 * @param end
	 *            last characters of stream that should be fetched. This method
	 *            might read some more characters. Set to -1 if all characters
	 *            should be read.
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is, final int start,
			final int end) throws IOException {
		return stream2str(is, null, start, end);
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from param charset to read the
	 *            {@link InputStream}. Can be null.
	 * @param charset
	 *            charset to be used to read {@link InputStream}. Can be null.
	 * @param start
	 *            first characters of stream that should be fetched. Set to 0,
	 *            if nothing should be skipped.
	 * @param end
	 *            last characters of stream that should be fetched. This method
	 *            might read some more characters. Set to -1 if all characters
	 *            should be read.
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is, final String charset,
			final int start, final int end) throws IOException {
		return stream2str(is, charset, start, end, null);
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from
	 * @param start
	 *            first characters of stream that should be fetched. Set to 0,
	 *            if nothing should be skipped.
	 * @param end
	 *            last characters of stream that should be fetched. This method
	 *            might read some more characters. Set to -1 if all characters
	 *            should be read.
	 * @param pattern
	 *            start reading at this pattern, set end = -2 to return only the
	 *            line, matching this pattern
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is, final int start,
			final int end, final String pattern) throws IOException {
		return stream2str(is, null, start, end, pattern);
	}

	/**
	 * Read {@link InputStream} and convert it into {@link String}.
	 * 
	 * @param is
	 *            {@link InputStream} to read from
	 * @param charset
	 *            charset to be used to read {@link InputStream}. Can be null.
	 * @param start
	 *            first characters of stream that should be fetched. Set to 0,
	 *            if nothing should be skipped.
	 * @param end
	 *            last characters of stream that should be fetched. This method
	 *            might read some more characters. Set to -1 if all characters
	 *            should be read.
	 * @param pattern
	 *            start reading at this pattern, set end = -2 to return only the
	 *            line, matching this pattern
	 * @return {@link String} holding all the bytes from the {@link InputStream}
	 * @throws IOException
	 *             IOException
	 */
	public static String stream2str(final InputStream is, final String charset,
			final int start, final int end, final String pattern)
			throws IOException {
		boolean foundPattern = false;
		if (pattern == null) {
			foundPattern = true;
		}
		InputStreamReader r;
		if (charset == null) {
			r = new InputStreamReader(is);
		} else {
			r = new InputStreamReader(is, charset);
		}
		final BufferedReader bufferedReader = new BufferedReader(r, BUFSIZE);
		final StringBuilder data = new StringBuilder();
		String line = null;
		long totalSkipped = 0;
		long skipped = 0;
		while (start > totalSkipped) {
			skipped = bufferedReader.skip(start - totalSkipped);
			if (skipped == 0) {
				break;
			}
			totalSkipped += skipped;
		}
		skipped = 0;
		while ((line = bufferedReader.readLine()) != null) {
			skipped += line.length() + 1;
			if (!foundPattern) {
				if (line.indexOf(pattern) >= 0) {
					if (end == ONLY_MATCHING_LINE) {
						return line;
					}
					foundPattern = true;
					Log.d(TAG, "skipped: " + skipped);
				}
			}
			if (foundPattern) {
				data.append(line + "\n");

			}
			if (end >= 0 && skipped > (end - start)) {
				break;
			}
		}
		bufferedReader.close();
		if (!foundPattern) {
			return null;
		}
		return data.toString();
	}

	/**
	 * Generate MD5 Hash from String.
	 * 
	 * @param s
	 *            input
	 * @return hash
	 */
	public static String md5(final String s) {
		try {
			// Create MD5 Hash
			final MessageDigest digest = java.security.MessageDigest
					.getInstance("MD5");
			digest.update(s.getBytes());
			final byte[] messageDigest = digest.digest();
			// Create Hex String
			final StringBuilder hexString = new StringBuilder(32);
			int b;
			for (byte bt : messageDigest) {
				b = 0xFF & bt;
				if (b < 0x10) {
					hexString.append('0' + Integer.toHexString(b));
				} else {
					hexString.append(Integer.toHexString(b));
				}
			}
			return hexString.toString();
		} catch (final NoSuchAlgorithmException e) {
			Log.e(TAG, null, e);
		}
		return "";
	}

	/**
	 * Get HTTP GET parameters.
	 * 
	 * @param url
	 *            base URL
	 * @param params
	 *            parameters as {@link BasicNameValuePair}
	 * @param encoding
	 *            encoding
	 * @return URL with parameters added
	 * @throws UnsupportedEncodingException
	 *             UnsupportedEncodingException
	 */
	public static String httpGetParams(final String url,
			final List<BasicNameValuePair> params, final String encoding)
			throws UnsupportedEncodingException {
		if (verboseLog) {
			Log.d(TAG, "httpGetParams(" + url + "," + params + ")");
		}
		final StringBuilder u = new StringBuilder(url);
		u.append("?");
		final int l = params.size();
		for (int i = 0; i < l; i++) {
			final BasicNameValuePair nv = params.get(i);
			if (!TextUtils.isEmpty(nv.getName())
					&& !TextUtils.isEmpty(nv.getValue())) {
				u.append(nv.getName());
				u.append("=");
				u.append(URLEncoder.encode(nv.getValue(), encoding));
				u.append("&");
			}
		}
		String ret = u.toString();
		if (ret.endsWith("?") || ret.endsWith("&")) {
			ret = ret.substring(0, ret.length() - 1);
		}
		if (verboseLog) {
			Log.d(TAG, "new url: " + ret);
		}
		return ret;
	}

	/**
	 * Show update notification.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param pkg
	 *            package
	 */
	public static void showUpdateNotification(final Context context,
			final String pkg) {
		Notification n = new Notification(android.R.drawable.stat_sys_warning,
				context.getString(R.string.update_title), 0);
		n.flags = Notification.FLAG_AUTO_CANCEL;
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(
				Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkg)),
				PendingIntent.FLAG_UPDATE_CURRENT);
		n.setLatestEventInfo(context, context.getString(R.string.update_title),
				context.getString(R.string.update_message), pi);

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(0, n);
	}

	/**
	 * Checks if there is a data network available. Requires
	 * ACCESS_NETWORK_STATE permission.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return true if a data network is available
	 */
	public static boolean isNetworkAvailable(final Context context) {
		try {
			ConnectivityManager mgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo net = mgr.getActiveNetworkInfo();
			return net != null && net.isConnected();
		} catch (SecurityException ex) {
			// for backwards compatibility, if the app has no
			// ACCESS_NETWORK_STATE permission then carry on
			return true;
		}
	}

}
