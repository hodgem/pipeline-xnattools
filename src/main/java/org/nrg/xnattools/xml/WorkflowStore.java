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

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.xnattools.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.http.converter.*;
import org.springframework.web.client.RestTemplate;

public class WorkflowStore extends AbsService {

    public WorkflowStore(XmlObject xmlObject, String host, String user, String pwd) throws MalformedURLException {
        super(host, user,pwd);
        xml = xmlObject;
    }

    public void store(String xmlContents)  throws Exception{
        try {
            final URI uri = new URI(host).resolve("/data/workflows?req_format=xml");

            String jsessionid = SessionManager.GetInstance().getJSESSION();

            HttpClient client = new HttpClient();
            client.getParams().setAuthenticationPreemptive(true);

            HttpHeaders requestHeaders = new HttpHeaders();

            if (StringUtils.isBlank(jsessionid)) {
                Credentials credentials = new UsernamePasswordCredentials(username, password);
                client.getState().setCredentials(new AuthScope(uri.getHost(), uri.getPort(), AuthScope.ANY_REALM), credentials);
            } else {
                requestHeaders.add("Cookie", "JSESSIONID=" + jsessionid);
            }

            CommonsClientHttpRequestFactory factory = new CommonsClientHttpRequestFactory(client);
            RestTemplate template = new RestTemplate(factory);
            template.setMessageConverters(Arrays.asList(messageConverters));

            HttpEntity<String> requestEntity = new HttpEntity<String>(xmlContents, requestHeaders);

            _log.trace("Sending Request...");
            long startTime = Calendar.getInstance().getTimeInMillis();
            ResponseEntity response = template.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
            long duration = Calendar.getInstance().getTimeInMillis() - startTime;
            _log.trace("Response received in {} ms: {}", duration, response.getStatusCode());
        }catch (Exception e) {
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

    private static final Logger _log = LoggerFactory.getLogger(XMLStore.class);
    private final HttpMessageConverter<?>[] messageConverters = new HttpMessageConverter<?>[] { new FormHttpMessageConverter(), new StringHttpMessageConverter(), new ResourceHttpMessageConverter(), new ByteArrayHttpMessageConverter() };
    XmlObject xml;
}
