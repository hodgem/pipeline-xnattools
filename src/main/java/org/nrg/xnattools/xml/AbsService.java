/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.nrg.xnattools.SessionManager;
import org.nrg.xnattools.exception.SessionManagerNotInitedException;
import org.xml.sax.SAXException;

public abstract class AbsService {
	
	protected String userSessionID;
	protected boolean externalSessionID = false;
    protected String host, username, password;
    protected final HttpHost _targetHost;
    protected final HttpContext _context;

    public AbsService(String host, String username, String password) throws MalformedURLException {
        this.host = host;
        if (!this.host.endsWith("/")) this.host += "/";
        this.username = username;
        this.password = password;
        String trustStore = System.getProperty("ts");
        this.userSessionID = null;
        String trustStorePwd = System.getProperty("tspwd");
        if (trustStore != null && trustStorePwd != null) {
       	 System.setProperty("javax.net.ssl.trustStore", trustStore);
    	 System.setProperty("javax.net.ssl.trustStorePassword", trustStorePwd);
    	 System.out.println("Set the TRUST CERTIFICATES");
        }

        URL url = new URL(host);
        AuthCache authCache = new BasicAuthCache();
        _targetHost = new HttpHost(url.getHost(), url.getPort(), url.getProtocol());
        authCache.put(_targetHost, new BasicScheme());
        _context = new BasicHttpContext();
        _context.setAttribute(ClientContext.AUTH_CACHE, authCache);
    }
    
    

    protected String getResponseBody(URL url) throws IOException, SessionManagerNotInitedException {
        StringBuilder buffer = new StringBuilder();
        DefaultHttpClient httpclient = new DefaultHttpClient();
    	HttpGet httpget = new HttpGet(url.getPath());

        try {
        	String _userSessionId = SessionManager.GetInstance().getJSESSION();
            httpget.setHeader("Cookie","JSESSIONID="+ _userSessionId);

            HttpResponse response = httpclient.execute(_targetHost, httpget, _context);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                buffer.append(EntityUtils.toString(entity));
                EntityUtils.consume(entity);
            }
        } finally {
            httpget.releaseConnection();
        }
        return buffer.toString();
    }
	
    protected BaseElement getBeanFromXml(String xmlFilePath, boolean deleteFile) throws IOException, SAXException{
        File f = new File(xmlFilePath);
        InputStream fis = new FileInputStream(f);
        BaseElement base = getBeanFromStream(fis); 
        if (deleteFile) {
            f.delete();
        }
        return base;
    }
    
    protected BaseElement getBeanFromStream(InputStream in) throws IOException, SAXException{
        XDATXMLReader reader = new XDATXMLReader();
        return reader.parse(in);
    }
    
    protected void executeToStream(String value, boolean fullPath, OutputStream bos, boolean quiet)throws FileNotFoundException, MalformedURLException, IOException, SAXException, ParserConfigurationException, SessionManagerNotInitedException{
        if (!quiet)System.out.println("Requesting xml for " + value + "");
        long startTime = Calendar.getInstance().getTimeInMillis();
        String urlStr = host + "app/template/MRXMLSearch.vm/id/" + value;
        if (fullPath)
        	 urlStr += "/adjustPath/fullpath";
        URL url = new URL(urlStr);
        String response = getResponseBody(url);
        final byte[] bytes = response.getBytes();
        bos.write(bytes, 0, bytes.length);
        bos.flush();
        if (!quiet)System.out.println("Response Received (" + (Calendar.getInstance().getTimeInMillis() - startTime) + " ms)");
}
    
 
    public void streamXML(String id, boolean fullPath, OutputStream bos, boolean quiet ) throws Exception {
	    try {
            executeToStream(id, fullPath, bos, quiet);
        } catch (SAXException e) {
            System.out.println("ERROR CODE 30: Invalid XML Received.");
            System.out.println("This could be do to network instability. Please re-try your request.  If the problem persists contact your IT Management.");
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Couldnt connect to host " + host );
            throw e;
        }
    }
    
    protected Call createCall(String session) throws ServiceException{
    	Service service = new Service();
        Call call= (Call)service.createCall();
        call.setMaintainSession(true);
        if(session!=null)call.setProperty("Cookie","JSESSIONID=" + session);
        return call;
    }
}
