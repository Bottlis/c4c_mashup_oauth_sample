package com.c4e.mymashup.odata;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import com.c4e.oauth.OAuthSAMLClient;

public class C4CTicketODataClient {
	public static final String HTTP_HEADER_ACCEPT = "Accept";
	public static final String APPLICATION_JSON = "application/json";
	public static final String SEPARATOR = "/";
	public static final String AUTHORIZATION_HEADER = "Authorization";

	private HttpClient m_httpClient = null;
	private String mODataServiceUrl = null;
	private String mOAuthToken = null;
	private String host = null;
	private String user = null;

	public C4CTicketODataClient(String host, String user) {
		this.host = host;
		this.user = user;
		this.mODataServiceUrl = host + "/sap/byd/odata/v1/servicerequest";
	}

	public class SystemQueryOptions {
		private String queryCondition;

		public String getQueryCondition() {
			return queryCondition;
		}

		public void setQueryCondition(String queryCondition) {
			this.queryCondition = queryCondition;
		}
	}

	public String readTicketsAsJSON() throws Exception {
		String serviceUrl = getODataServiceUrl();
		String entitySetName = "ServiceRequestCollection";
		SystemQueryOptions queryOptions = this.new SystemQueryOptions();
		String queryString = "?$format=json&$orderby=LastChangeDateTime%20desc&$top=5";
		queryOptions.setQueryCondition(queryString);

		String absolutUri = createUri(serviceUrl, entitySetName, null, queryOptions);
		InputStream content = executeGet(absolutUri, APPLICATION_JSON);

		StringWriter writer = new StringWriter();
		IOUtils.copy(content, writer, "UTF-8");
		String jsonString = writer.toString();
		return jsonString;
	}

	private String createUri(String serviceUri, String entitySetName, String id, SystemQueryOptions options) {

		final StringBuilder absolauteUri = new StringBuilder(serviceUri).append(SEPARATOR).append(entitySetName);
		if (id != null) {
			absolauteUri.append("('").append(id).append("')");
		}

		if (options != null) {
			if (options.getQueryCondition() != null) {
				absolauteUri.append(options.getQueryCondition());
			}
		}

		return absolauteUri.toString();
	}

	private HttpClient getHttpClient() throws IOException {
		this.m_httpClient = HttpClients.createDefault();
		return this.m_httpClient;
	}

	private InputStream executeGet(String absoluteUrl, String contentType) throws IllegalStateException, IOException {
		final HttpGet get = new HttpGet(absoluteUrl);
		get.setHeader(AUTHORIZATION_HEADER, getAuthorizationHeader());
		get.setHeader(HTTP_HEADER_ACCEPT, contentType);

		HttpHost target = new HttpHost("proxy", 8080, "http");
		RequestConfig config = RequestConfig.custom().setProxy(target).build();
		get.setConfig(config);

		HttpResponse response = getHttpClient().execute(get);
		return response.getEntity().getContent();
	}

	private String getODataServiceUrl() {
		return this.mODataServiceUrl;
	}

	private String getAuthorizationHeader() {
		OAuthSAMLClient oauthClient = new OAuthSAMLClient(this.host, this.user);
		String token = null;
		try {
			token = oauthClient.getAccessToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.mOAuthToken = token;

		String result = "Bearer " + this.mOAuthToken;
		return result;
	}

}
