/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ripple.topology.io;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

public class UrlContent extends AbstractFileResolvingContent {

	private final URI uri;

	private final URL url;

	private final URL cleanedUrl;

	public UrlContent(URL url) {
		Preconditions.checkNotNull(url, "url");
		this.url = url;
		this.cleanedUrl = getCleanedUrl(this.url, url.toString());
		this.uri = null;
	}

	private URL getCleanedUrl(URL originalUrl, String originalPath) {
		try {
			return new URL(StringUtils.cleanPath(originalPath));
		}
		catch (MalformedURLException ex) {
			// Cleaned URL path cannot be converted to URL
			// -> take original URL.
			return originalUrl;
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		URLConnection con = this.url.openConnection();
		ContentUtils.useCachesIfNecessary(con);
		try {
			return con.getInputStream();
		}
		catch (IOException ex) {
			// Close the HTTP connection (if applicable).
			if (con instanceof HttpURLConnection) {
				((HttpURLConnection) con).disconnect();
			}
			throw ex;
		}
	}

	@Override
	public URL getURL() throws IOException {
		return this.url;
	}

	@Override
	public URI getURI() throws IOException {
		if (this.uri != null) {
			return this.uri;
		}
		else {
			return super.getURI();
		}
	}

	@Override
	public File getFile() throws IOException {
		if (this.uri != null) {
			return super.getFile(this.uri);
		}
		else {
			return super.getFile();
		}
	}

	@Override
	public String getDescription() {
		return "URL [" + this.url + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this ||
			(obj instanceof UrlContent && this.cleanedUrl.equals(((UrlContent) obj).cleanedUrl)));
	}

	@Override
	public int hashCode() {
		return this.cleanedUrl.hashCode();
	}

}
