package org.nrg.xnattools.xml;


import org.apache.axis.client.Call;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.apache.http.util.EntityUtils;
import org.apache.xmlbeans.XmlObject;
import org.nrg.xdat.bean.XnatImagesessiondataBean;
import org.nrg.xdat.bean.base.BaseElement;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;

public class XMLSearch extends AbsService {
    
    public XMLSearch(String host, String username, String password) throws MalformedURLException {
    	super(host, username, password);
    }
    
    public String getReadMe(String session_id) throws Exception {
    	String service_session = null;
    	try {
            service_session = createServiceSession();
            URL url = new URL(host + "axis/VelocitySearch.jws");
            Call call = createCall(service_session);
            call.setTargetEndpointAddress(url);
            call.setOperationName("search");
            Object[] params = new Object[]{service_session,"xnat:mrSessionData.ID","=",session_id,"xnat:mrSessionData"};
            return (String)call.invoke(params);
    	} finally {
             try {
                 if (service_session != null)closeServiceSession(service_session);
             } catch (Exception e) {
                 e.printStackTrace();
                 System.out.println("Couldnt close connection to host " + host );
                 throw e;
             }
         }
    }
    
    public String searchFirst(String field, String value, String comparison, String dataType, String dir) throws Exception {
        String createdFile = null;
        String service_session = null;
        try {
             service_session = createServiceSession();
            Object[] sessions = getIdentifiers(service_session, field, comparison, value, dataType);
            if (sessions != null && sessions.length > 0) {
                    String id = "" +sessions[0];
                    System.out.println("Recd " + id);
                    try {
                        createdFile = execute(service_session,id, dataType, dir, true);
                        System.out.println("Created File: " + createdFile);
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
                if (service_session != null)closeServiceSession(service_session);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Couldnt close connection to host " + host );
                throw e;
            }
        }
        return createdFile;
    }
    
    
    public ArrayList<String>  searchAll(String field, String value, String comparison, String dataType, String dir) throws Exception {
        ArrayList rtn = new ArrayList();
        String createdFile = null;
        String service_session  = null;
        try {
            service_session = createServiceSession();
            Object[] sessions = getIdentifiers(service_session, field, comparison, value, dataType);
            if (sessions != null ) {
                for (int i =0;i<sessions.length;i++)
                {
                    String id = ""+sessions[i];
                    System.out.println("Recd " + id);
                    try {
                        createdFile = execute(service_session,id, dataType, dir, true);
                        System.out.println("Created File: " + createdFile);
                        rtn.add(createdFile);
                    } catch (SAXException e) {
                        System.out.println("ERROR CODE 30: Invalid XML Received.");
                        System.out.println("This could be do to network instability. Please re-try your request.  If the problem persists contact your IT Management.");
                    } catch (ParserConfigurationException e) {
                    }
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Couldnt connect to host " + host );
            throw e;
        }finally {
            try {
                if (service_session !=null) closeServiceSession(service_session);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Couldnt close connection to host " + host );
                throw e;
            }
        }
        return rtn;
    }
    
    
    public Object[]  searchAll(String field, String value, String comparison, String dataType) throws Exception {
        String service_session  = null;
        try {
            service_session = createServiceSession();
            Object[] sessions = getIdentifiers(service_session, field, comparison, value, dataType);
            return sessions;
        }catch(Exception e) {
            e.printStackTrace();
            System.out.println("Couldnt connect to host " + host );
            throw e;
        }finally {
            try {
                if (service_session !=null) closeServiceSession(service_session);
            }catch(Exception e) {
                e.printStackTrace();
                System.out.println("Couldnt close connection to host " + host );
                throw e;
            }
        }
    }
    
    /**
     * @param service_session
     * @param field
     * @param comparison
     * @param value
     * @param dataType
     * @return Object [] of Identifiers 
     * @throws ServiceException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public Object[] getIdentifiers(String service_session,String field, String comparison, String value, String dataType) throws ServiceException,MalformedURLException,RemoteException{
        URL url = new URL(host + "axis/GetIdentifiers.jws");
        Call call = createCall(service_session);
        call.setTargetEndpointAddress(url);
        
        call.setOperationName("search");
        
        Object[] params=new Object[]{service_session,field,comparison,value,dataType};

        System.out.println("Requesting matching IDs...");
        long startTime = Calendar.getInstance().getTimeInMillis();
        Object[] o = (Object[])call.invoke(params);
        long duration = Calendar.getInstance().getTimeInMillis() - startTime;
        System.out.println("Response Received (" + duration + " ms)");
        return o;
    }
    
    /**
     * @param service_session
     * @param field
     * @param dataType
     * @return Object [] of Identifiers 
     * @throws ServiceException
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public Object[] getIdentifiers(String service_session,String field, String dataType) throws ServiceException,MalformedURLException,RemoteException{
        URL url = new URL(host + "axis/GetIdentifiers.jws");
        Call call = createCall(service_session);
        call.setTargetEndpointAddress(url);
        
        call.setOperationName("search");
        String comparison = " IS NOT ";
        String value = "NULL";
        Object[] params=new Object[]{service_session,field,comparison,value,dataType};
        System.out.println("Requesting matching IDs...");
        long startTime = Calendar.getInstance().getTimeInMillis();
        Object[] o = (Object[])call.invoke(params);
        long duration = Calendar.getInstance().getTimeInMillis() - startTime;
        System.out.println("Response Received (" + duration + " ms)");
        return o;
    }
    
    
    /**
     * @param service_session
     * @param dataType
     * @param dir
     * @param quiet
     * @return Path to created XML File
     * @throws FileNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * 
     */
    public String execute(String service_session,String value, String dataType, String dir, boolean quiet)throws FileNotFoundException, MalformedURLException, IOException, SAXException, ParserConfigurationException{
        int counter = 0;
        String finalName = value + ".xml";
        if (!dir.endsWith(File.separator)) dir += File.separator;
        File outFile = new File(dir,finalName);
        while(outFile.exists())
        {
            finalName = value + "_v" + (counter++) + ".xml";
            outFile = new File(dir,finalName);
        }
        
        if (!quiet) {
            System.out.println("Requesting xml for " + value + "");
        }
        long startTime = Calendar.getInstance().getTimeInMillis();

        URL url = new URL(host + "app/template/XMLSearch.vm/id/" + value + "/data_type/" + dataType);

        String response = getResponseBody(url);

        if (response.length() > 0) {
            BufferedOutputStream bos = null;
            try {
                bos = new BufferedOutputStream(new FileOutputStream(outFile));
                final byte[] bytes = response.getBytes();
                bos.write(bytes, 0, bytes.length);
            } finally {
                if (bos != null) {
                    bos.flush();
                    bos.close();
                }
            }
        }

        if (!quiet) {
            _log.debug("Response Received (" + (Calendar.getInstance().getTimeInMillis() - startTime) + " ms)");
        }

        return outFile.getAbsolutePath();
    }
    
    public BaseElement getBeanFromHost(String id, boolean fullPath) throws Exception {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	streamXML(id, fullPath, out, false);
    	ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    //	int length = in.available();
    //	byte [] buff = new byte[length];
    //	in.read(buff);

    	//System.out.println(new String(buff));
        BaseElement bean =  getBeanFromStream(in);
        out.close(); in.close();
        return bean;
   }
    
    public static void main(String args[]) throws MalformedURLException {
        //XMLSearch search = new XMLSearch("http://cnda.neuroimage.wustl.edu:80","mohanar","BLA");
        //String createdFile = search.searchFirst("xnat:mrSessionData.ID","633-BS", "=","xnat:mrSessionData",".");
        XMLSearch search = new XMLSearch("https://cnda.wustl.edu","mohanar","admin");
        try {
           	XnatImagesessiondataBean imageSession  = (XnatImagesessiondataBean) search.getBeanFromHost("CNDA_E16035", true);
            System.out.println(imageSession.getId() + " " + imageSession.getAcquisitionSite());
        	//String createdFile = search.searchFirst("xnat:MRSession.ID","CNDA_E16035","=","xnat:mrSessionData",".");
            //ArrayList<String> files = search.searchAll("wrk:workflowData.ID","070425_TC24273","=","wrk:workflowData",".");
        }catch(Exception e) {e.printStackTrace();}
        System.exit(0);

        
    }
    
    XmlObject xml;
    private static final Log _log = LogFactory.getLog(XMLSearch.class);
}
