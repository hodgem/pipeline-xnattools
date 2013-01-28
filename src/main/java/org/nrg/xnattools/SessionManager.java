package org.nrg.xnattools;

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.nrg.xnattools.exception.SessionManagerNotInitedException;

/*
 * This class will be responsible for maintaining session with XNAT. 
 * Each object wanting to communicate with XNAT should request a session from
 * the manager.
 */
public class SessionManager {

	private static SessionManager self;
	private String _host;
	private String _username;
	private String _password;
	private String _userSessionId;
    long startTime;
    long sessionTimeout;
    long DEFUALT_SESSION_TIMEOUT = 900000;
    long timeSinceLastRequest;
	private final String SESSION_EXPIRATION_TIME = "SESSION_EXPIRATION_TIME";
	
    private SessionManager(String host, String username, String password) {
        _host = host;
        if (!_host.endsWith("/")) _host += "/";
        _username = username;
        _password = password;
    }
    
    public static SessionManager GetInstance(String host, String username, String password) {
        if (self == null) {
           self = new SessionManager(host,username,password); 
        }
        return self;
    }

    public static SessionManager GetInstance() throws SessionManagerNotInitedException {
    	if (self == null) throw new SessionManagerNotInitedException();
    	return self;
    }

    
    private synchronized void createJSESSION() throws ClientProtocolException, IOException, SessionManagerNotInitedException{
        DefaultHttpClient httpclient = new DefaultHttpClient();
    	HttpPost httpPost = new HttpPost(_host+"data/JSESSION");
    	httpclient.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM, AuthScope.ANY_SCHEME), new UsernamePasswordCredentials(_username, _password));
    	
        HttpResponse response = httpclient.execute(httpPost);
        
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
    	for (int j=0; j< headerElements.length;j++) {
    		if (SESSION_EXPIRATION_TIME.equals(headerElements[j].getName())) {
    			rtn = headerElements[j].getValue();
    			break;
    		}
    	}
    	return rtn;
    }
    
    private synchronized void setSessionExpirationTime(HttpResponse response) {
    	Header[] cookie = getCookie(response);
    	for (int i=0; i< cookie.length;i++) {
    		String headerValue = getSessionExpirationValue(cookie[i]);
    		if (headerValue != null) {
    			String[] headerValues = headerValue.split(",");
    			sessionTimeout = System.currentTimeMillis() + Long.parseLong(headerValues[1]);
    			break;
    		}
    	}
    	System.out.println(sessionTimeout);
    }

   
    public synchronized String getJSESSION() throws ClientProtocolException, IOException, SessionManagerNotInitedException{
    	if (_userSessionId == null || !isAliveJSESSION()) {
    		createJSESSION();
    		return _userSessionId;
    	}else 
    		return _userSessionId;
    }

    
    
    private boolean isAliveJSESSION(){
        boolean isAlive = true;
        if (_userSessionId == null) return !isAlive;
        long now = System.currentTimeMillis();
        if (now < sessionTimeout) return isAlive;
        else return !isAlive;
    }
    
    
    public void deleteJSESSION() throws ClientProtocolException, IOException, SessionManagerNotInitedException{
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpDelete httpDelete = new HttpDelete(_host+"data/JSESSION");
        httpDelete.setHeader("Cookie","JSESSIONID="+ _userSessionId);
        HttpResponse response2 = httpclient.execute(httpDelete);
        try {
            if (response2.getStatusLine().getStatusCode()==200) {
                _userSessionId=null;
            }else 
            	throw new SessionManagerNotInitedException();
        } finally {
            httpDelete.releaseConnection();
        }
    }


    public static void main(String[] args) throws Exception {
    	SessionManager sessionManager = SessionManager.GetInstance("http://localhost:8080", "mohanar", "Admin999");
    	String _jsession = sessionManager.getJSESSION();
    	System.out.println("Is Session Alive " + sessionManager.isAliveJSESSION());
//    	try {
//    		Thread.sleep(900005);
//    		} catch(InterruptedException e) {
//    		} 
    	//sessionManager.deleteJSESSION();
 //   	System.out.println("After 5 minutes Is Session Alive " + sessionManager.isAliveJSESSION());
    	System.exit(0);
    }
    



    

    
}
