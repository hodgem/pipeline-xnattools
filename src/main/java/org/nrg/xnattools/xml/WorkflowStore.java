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
import org.apache.http.auth.params.AuthPNames;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.AuthPolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
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

    public void store(String xmlContents) throws Exception {
        try {
            final URI uri = StringUtils.isBlank(hostUri.getPath()) ? hostUri.resolve("/data/workflows?req_format=xml") : hostUri.resolve("data/workflows?req_format=xml");

            String sessionId = SessionManager.GetInstance().getJSESSION();

            HttpPut request = new HttpPut(uri);
            request.setEntity(new StringEntity(xmlContents, ContentType.APPLICATION_XML));

            DefaultHttpClient client = new DefaultHttpClient();
            HttpContext context = configureAuthentication(client, sessionId, uri);

            // Only worry about stuff here if we got a 400 or worse. 200 and 300s are OK.
            _log.trace("Sending Request...");
            long startTime = Calendar.getInstance().getTimeInMillis();
            HttpResponse response = context == null ? client.execute(request) : client.execute(request, context);
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

    private HttpContext configureAuthentication(final DefaultHttpClient client, final String sessionId, final URI uri) {
        client.getParams().setParameter(AuthPNames.PROXY_AUTH_PREF, AUTH_PREFERENCES);

        if (StringUtils.isBlank(sessionId)) {
            client.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(username, password));
            return new BasicHttpContext() {{
                setAttribute(ClientContext.AUTH_CACHE, new BasicAuthCache() {{
                    put(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()), new BasicScheme());
                }});
            }};
        } else {
            // If we already have a JSESSIONID cookie, then bug out
            for (Cookie cookie : client.getCookieStore().getCookies()) {
                if (cookie.getName().equals("JSESSIONID")) {
                    return null;
                }
            }
            // If we don't have a JSESSIONID cookie, add it.
            final BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", sessionId);
            cookie.setDomain(uri.getHost());
            client.getCookieStore().addCookie(cookie);
            return null;
        }
    }

    private static final Logger _log = LoggerFactory.getLogger(XMLStore.class);
    private static final List<String> AUTH_PREFERENCES = new ArrayList<String>() {{
        add(AuthPolicy.BASIC);
        add(AuthPolicy.DIGEST);
    }};

    XmlObject xml;
}
