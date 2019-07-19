/*
 * org.nrg.xnattools.xml.WorkflowStore
 * XNAT http://www.xnat.org
 * Copyright (c) 2014, Washington University School of Medicine
 * All Rights Reserved
 *
 * Released under the Simplified BSD.
 *
 * Last modified 2/17/14 4:58 PM
 */
package org.nrg.xnattools.xml;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.xnattools.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class WorkflowStore extends AbsService {

    private final URI hostUri;

    public WorkflowStore(XmlObject xmlObject, String host, String user, String pwd) throws MalformedURLException, URISyntaxException {
        super(host, user, pwd);
        hostUri = new URI(host);
        xml = xmlObject;
    }
    
    public class ClientContext {
    	
    	CloseableHttpClient client = null;
    	HttpContext context = null;
    	
		public HttpContext getContext() {
			return context;
		}
		public void setContext(HttpContext context) {
			this.context = context;
		}
		public CloseableHttpClient getClient() {
			return client;
		}
		public void setClient(CloseableHttpClient client) {
			this.client = client;
		}
    	
    }

    public void store(String xmlContents) throws Exception {
		//HttpResponse response;
        try {
            final URI uri = StringUtils.isBlank(hostUri.getPath()) ? hostUri.resolve("/data/workflows?req_format=xml") : hostUri.resolve("data/workflows?req_format=xml");

            final String sessionId = SessionManager.GetInstance().getJSESSION();

            final HttpPut request = new HttpPut(uri);
            request.setEntity(new StringEntity(xmlContents, ContentType.APPLICATION_XML));

            final HttpClientBuilder builder = HttpClientBuilder.create();
            final ClientContext clientContext = configureAuthentication(builder, sessionId, uri);
            final CloseableHttpClient client = clientContext.getClient();
            final HttpContext context = clientContext.getContext();

            // Only worry about stuff here if we got a 400 or worse. 200 and 300s are OK.
            _log.trace("Sending Request...");
            long startTime = Calendar.getInstance().getTimeInMillis();
            @SuppressWarnings({ "resource" })
			final HttpResponse response = context == null ? client.execute(request) : client.execute(request, context);
            long duration = Calendar.getInstance().getTimeInMillis() - startTime;
            if (response.getStatusLine().getStatusCode() >= 400) {
                _log.error("Error encountered storing the workflow document: " + response.getStatusLine());
            } else {
                HttpEntity entity = null;
                try {
                    entity = response.getEntity();
                    _log.info("Got response with {} bytes of content type {} in {} ms", new Object[]{entity.getContentLength(), entity.getContentType() != null ? entity.getContentType().getValue() : "Unspecified", duration});
                } finally {
                    if (entity != null) {
                        EntityUtils.consume(entity);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to store workflow with " + host);
            throw e;
        } finally {
        	//response.close();
        }
    }

    public void store() throws Exception{
        try {
            String contents = xml.xmlText(new XmlOptions().setSavePrettyPrint().setSaveAggressiveNamespaces());
            store(contents);
            System.out.println("Item Stored");
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Unable to store workflow with " + host);
            throw e;
        }
    }

    private ClientContext configureAuthentication(final HttpClientBuilder builder, final String sessionId, final URI uri) {

    	final ClientContext clientContext = new ClientContext();
    	final CloseableHttpClient client;
    	final RequestConfig.Builder requestConfig =   
    	RequestConfig.custom().setTargetPreferredAuthSchemes(AUTH_PREFERENCES);
        if (StringUtils.isBlank(sessionId)) {
            final CredentialsProvider credsProvider = new BasicCredentialsProvider();
        	credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));
        	client = builder.setDefaultRequestConfig(requestConfig.build()).setDefaultCredentialsProvider(credsProvider).build();
        	clientContext.setClient(client);
            BasicHttpContext httpContext = new BasicHttpContext() {{
                setAttribute(HttpClientContext.AUTH_CACHE, new BasicAuthCache() {{
                    put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), new BasicScheme());
                }});
            }};
        	clientContext.setContext(httpContext);
        } else {
            final BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", sessionId);
            final CookieStore cookieStore = new BasicCookieStore();
    		cookieStore.addCookie(cookie);
            cookie.setDomain(uri.getHost());
            client = builder.setDefaultRequestConfig(requestConfig.build()).setDefaultCookieStore(cookieStore).build();
        	clientContext.setClient(client);
        }
        return clientContext;
    }

    private static final Logger _log = LoggerFactory.getLogger(XMLStore.class);
    @SuppressWarnings("serial")
	private static final List<String> AUTH_PREFERENCES = new ArrayList<String>() {{
        add(AuthSchemes.BASIC);
        add(AuthSchemes.DIGEST);
    }};

    XmlObject xml;
}
