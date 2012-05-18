/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools.xml;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.Calendar;

public abstract class AbsService {
	
	protected String userSessionID;
	protected boolean externalSessionID = false;
    protected String host, username, password;
    protected Service service;
    protected final HttpHost _targetHost;
    protected final HttpContext _context;

    public AbsService(String host, String username, String password) throws MalformedURLException {
        this.host = host;
        if (!this.host.endsWith("/")) this.host += "/";
        this.username = username;
        this.password = password;
        service = new Service();
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
    
    
    protected Service getService(){
        service.setMaintainSession(true);
        return service;
    }

    protected String getResponseBody(URL url) throws IOException {
        StringBuilder buffer = new StringBuilder();
        DefaultHttpClient httpclient = new DefaultHttpClient();
        try {
            httpclient.getCredentialsProvider().setCredentials(
                    new AuthScope(_targetHost.getHostName(), _targetHost.getPort()),
                    new UsernamePasswordCredentials(username, password));

            HttpGet httpget = new HttpGet(url.getPath());

            HttpResponse response = httpclient.execute(_targetHost, httpget, _context);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                buffer.append(EntityUtils.toString(entity));
                EntityUtils.consume(entity);
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return buffer.toString();
    }

    protected Call createCall(String session) throws ServiceException{
        
        Call call= (Call)getService().createCall();
        call.setMaintainSession(true);
        
        if (username!=null){
            call.setUsername(this.username);
        }
        if (password!=null){
            call.setPassword(this.password);
        }
        if(session!=null)call.setProperty("Cookie","JSESSIONID=" + session);
        return call;
    }
    
    
    /**
	 * @return Session id for use in subsequent requests.
	 * @throws ServiceException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 */
	public String createServiceSession() throws ServiceException,
			MalformedURLException, RemoteException {
        if (userSessionID==null){
        	 //System.setProperty("javax.net.ssl.keyStore", "c:\\keystore1.jks");
        	 //System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        	 //System.setProperty("javax.net.ssl.keyStoreType", "JKS");
        	// System.setProperty("javax.net.ssl.trustStore", "c:\\cacerts1.jks");
        	// System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

            Call call = createCall(null);
            //  REQUEST SESSION ID
            URL requestSessionURL = new URL(host + "axis/CreateServiceSession.jws");
            call.setTargetEndpointAddress(requestSessionURL);
            call.setOperationName("execute");
            Object[] params = {};
            userSessionID =(String) call.invoke(params);
            
            //userSessionID=getJSessionID(call.getResponseMessage());
            return userSessionID;
        }else{
            externalSessionID = true;
            String service_session = userSessionID;
            userSessionID= refreshServiceSession(service_session);
            if (!service_session.equals(userSessionID)){
                //System.out.println(userSessionID)
            }
            return userSessionID;
        }
	}

 
	
    /**
     * @return Session id for use in subsequent requests.
     * @throws ServiceException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public String refreshServiceSession(String service_session)
            throws ServiceException, MalformedURLException, RemoteException {
            Call call = createCall(this.userSessionID);
            call.setMaintainSession(true);
            URL requestSessionURL = new URL(host + "axis/RefreshServiceSession.jws");
            call.setTargetEndpointAddress(requestSessionURL);
            call.setOperationName("execute");
            Object[] params = new Object[] { service_session };
            userSessionID= (String)call.invoke(params);
            //userSessionID=getJSessionID(call.getResponseMessage());
            return userSessionID;
    }

	/**
	 * @throws ServiceException
	 * @throws MalformedURLException
	 * @throws RemoteException
	 */
	public void closeServiceSession(String service_session)
			throws ServiceException, MalformedURLException, RemoteException {
		if (service_session != null && !externalSessionID) {
			Call call = createCall(userSessionID);
			URL requestSessionURL = new URL(host+ "axis/CloseServiceSession.jws");
			call.setTargetEndpointAddress(requestSessionURL);
			call.setOperationName("execute");
			Object[] params = new Object[] { service_session };
			call.invoke(params);
		}
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
    
    protected void executeToStream(String service_session, String value, boolean fullPath, OutputStream bos, boolean quiet)throws FileNotFoundException, MalformedURLException, IOException, SAXException, ParserConfigurationException{
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
        String service_session = null;
        try {
            service_session = createServiceSession();
            if (service_session != null) {
	            try {
	                        executeToStream(service_session,id, fullPath, bos, quiet);
	                    } catch (SAXException e) {
	                        System.out.println("ERROR CODE 30: Invalid XML Received.");
	                        System.out.println("This could be do to network instability. Please re-try your request.  If the problem persists contact your IT Management.");
                } catch (ParserConfigurationException e) {
                    
                }
        }
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Couldnt connect to host " + host );
            throw e;
        }finally {
            try {
                if (service_session != null) closeServiceSession(service_session);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Couldnt close connection to host " + host );
                throw e;
            }
        }
    }
}
