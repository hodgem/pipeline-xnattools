/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools.xml;

import java.net.MalformedURLException;
import java.util.Calendar;

import org.apache.axis.client.Call;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.xnattools.SessionManager;

public class XMLStore extends AbsService {
    
    public XMLStore(XmlObject xmlObject, String host, String user, String pwd) throws MalformedURLException {
    	super(host, user,pwd);
        xml = xmlObject;
    }

    public XMLStore(String host, String user, String pwd) throws MalformedURLException {
    	super(host, user,pwd);
    	this.xml = null;
    }
    
    
    
    public void store(String xmlContents)  throws Exception{
        try {
        	String service_session = SessionManager.GetInstance().getJSESSION();
            Call call = createCall(service_session);
            call.setTargetEndpointAddress(host + "axis/StoreXML.jws");
            
            call.setOperationName("store");
            Object[] params = {service_session,xmlContents,new Boolean(false),new Boolean(true)};
            
            System.out.println("Sending Request...");
            long startTime = Calendar.getInstance().getTimeInMillis();
            String o = (String)call.invoke(params);
            long duration = Calendar.getInstance().getTimeInMillis() - startTime;
            System.out.println("Response Received (" + duration + " ms)");
            call = null;
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("Unable to store workflow with " + host);
            throw e;
            //System.exit(1);
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
    
    XmlObject xml;
}
