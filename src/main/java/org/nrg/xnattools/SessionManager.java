package org.nrg.xnattools;

import org.apache.commons.lang.StringUtils;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.nrg.xnattools.exception.SessionManagerNotInitedException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/*
 * This class will be responsible for maintaining session with XNAT. 
 * Each object wanting to communicate with XNAT should request a session from
 * the manager.
 */
public class SessionManager {

    public static final String SESSION_EXPIRATION_TIME = "SESSION_EXPIRATION_TIME";

    private static SessionManager self;

    private URI _service;
    private String _username;
    private String _password;
    private String _userSessionId;
    long sessionTimeout;
	int i=0;
	long requestTime;
	
    private SessionManager(String host, String username, String password) throws URISyntaxException {
        URI server = new URI(host);
        _service = StringUtils.isBlank(server.getPath()) ? server.resolve("/data/JSESSION") : server.resolve("data/JSESSION");
        _username = username;
        _password = password;
    }
    
    public static SessionManager GetInstance(String host, String username, String password) {
        if (self == null) {
            try {
                self = new SessionManager(host,username,password);
            } catch (URISyntaxException e) {
                throw new RuntimeException("Bad URI format found: " + host, e);
            }
        }
        return self;
    }

    public static SessionManager GetInstance() throws SessionManagerNotInitedException {
    	if (self == null) {
            throw new SessionManagerNotInitedException();
        }
    	return self;
    }

    
    private synchronized void createJSESSION() throws IOException, SessionManagerNotInitedException {
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(_service);
    	client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME), new UsernamePasswordCredentials(_username, _password));
        HttpContext context = new BasicHttpContext() {{
            setAttribute(ClientContext.AUTH_CACHE, new BasicAuthCache() {{
                put(new HttpHost(_service.getHost(), _service.getPort(), _service.getScheme()), new BasicScheme());
            }});
        }};

        //local machine time
    	requestTime = System.currentTimeMillis();
        if (_userSessionId != null ) {
            _userSessionId = null;
        }
    	
        HttpResponse response = client.execute(httpPost, context);
        
        try {
            if (response.getStatusLine().getStatusCode()==200) {
            	HttpEntity entity2 = response.getEntity();
                _userSessionId=EntityUtils.toString(entity2);
                setSessionExpirationTime(response);
            }else 
            	throw new SessionManagerNotInitedException();
        } finally {
            httpPost.releaseConnection();
        }
    }

    private Header[] getCookie(HttpResponse response) {
    	return response.getHeaders("Set-Cookie");
    }
    
    private String getSessionExpirationValue(Header cookie) {
    	String rtn = null;
   		HeaderElement[] headerElements = cookie.getElements();
        for (final HeaderElement headerElement : headerElements) {
            if (SESSION_EXPIRATION_TIME.equals(headerElement.getName())) {
                rtn = headerElement.getValue();
                break;
            }
        }
    	return rtn;
    }
    
    private synchronized void setSessionExpirationTime(HttpResponse response) {
    	Header[] cookie = getCookie(response);
        for (final Header aCookie : cookie) {
            String headerValue = getSessionExpirationValue(aCookie);
            if (headerValue != null) {
                String[] headerValues = headerValue.split(",");
                long hostRequestTime = Long.parseLong(headerValues[0]);
                long timeDifference = hostRequestTime - requestTime;
                sessionTimeout = (System.currentTimeMillis() - timeDifference) + Long.parseLong(headerValues[1]);
                //sessionTimeout = Long.parseLong(headerValues[0]) + Long.parseLong(headerValues[1]);

                break;
            }
        }
    	System.out.println("Session Timeout () " + sessionTimeout);
    }

   
    public synchronized String getJSESSION() throws IOException, SessionManagerNotInitedException{
    	i++;
    	String rtn ;
    	if (_userSessionId == null || !isAliveJSESSION()) {
            createJSESSION();
    	}
        rtn = _userSessionId;
    	System.out.println(i + " GETJSESSION CALLED " + rtn + "  "+ _userSessionId);
    	return rtn;
    }

    private synchronized boolean isAliveJSESSION(){
        long now = System.currentTimeMillis();
        boolean rtn = now < sessionTimeout;
        System.out.println("JSESSION " + _userSessionId + " isAlive " + rtn + " now - sessionTimeout = " + (now - sessionTimeout));
        return rtn;
    }
    
    
    public void deleteJSESSION() throws IOException, SessionManagerNotInitedException, URISyntaxException {
    	DefaultHttpClient client = new DefaultHttpClient();
        final BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", _userSessionId);
        cookie.setDomain(_service.getHost());
        client.getCookieStore().addCookie(cookie);
        HttpDelete httpDelete = new HttpDelete(_service);
        HttpResponse response = client.execute(httpDelete);
        try {
            if (response.getStatusLine().getStatusCode()==200) {
                _userSessionId=null;
            }else 
            	throw new SessionManagerNotInitedException();
        } finally {
            httpDelete.releaseConnection();
        }
    }


    public static void main(String[] args) throws Exception {
    	SessionManager sessionManager = SessionManager.GetInstance("https://cnda-dev-16a.nrg.mir", "mohanar", "admin");
    	String jsession = sessionManager.getJSESSION();
    	System.out.println("Got session: " + jsession);
    	System.out.println("Is alive:    " + sessionManager.isAliveJSESSION());
    	try {
    		Thread.sleep(900005);
        } catch(InterruptedException ignored) {
        }
     	System.out.println("After 15+ minutes Is Session Alive " + sessionManager.isAliveJSESSION());
     	System.exit(0);
    }
}
