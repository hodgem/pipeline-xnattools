/* 
 *  Copyright Washington University in St Louis 2006
 *  All rights reserved
 *  
 *  @author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools.xml;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.xmlbeans.XmlObject;
import org.nrg.pipeline.xmlbeans.xnat.MRSessionDocument;
import org.nrg.pipeline.xmlreader.XmlReader;
import org.nrg.xdat.bean.XnatMrsessiondataBean;
import org.nrg.xnattools.SessionManager;
import org.nrg.xnattools.exception.SessionManagerNotInitedException;
import org.xml.sax.SAXException;

public class MRXMLSearch extends AbsService {
    
    boolean quiet = true;
    
    public MRXMLSearch(String host, String username, String password) throws MalformedURLException {
    	super(host, username,password);
    }
    

    public String getXML(String id, String dir) throws Exception {
        String createdFile = null;
        String service_session = null;
        try {
            service_session = SessionManager.GetInstance().getJSESSION();
            if (service_session != null) {
	            try {
	                        createdFile = execute(id, dir);
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
        }
        return createdFile;
    }
   
    
    private void executeToStream(String value, OutputStream bos)throws FileNotFoundException, MalformedURLException, IOException, SAXException, ParserConfigurationException, SessionManagerNotInitedException{
    	executeToStream(value, true,bos, quiet);
    }
    
    
    
	 public void streamXML(String id,OutputStream bos ) throws Exception {
		 streamXML(id, true,  bos,quiet );
	 }
	 
   

    
    /**
     * @param value
     * @param dir
     * @return Path to created XML File
     * @throws FileNotFoundException
     * @throws MalformedURLException
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     * 
     */
    private String execute(String value, String dir)throws FileNotFoundException, MalformedURLException, IOException, SAXException, ParserConfigurationException, SessionManagerNotInitedException{
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
        URL url = new URL(host + "app/template/MRXMLSearch.vm/id/" + value + "/adjustPath/fullpath");

        String response = getResponseBody(url);
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile));
        byte[] bytes = response.getBytes();
        bos.write(bytes, 0, bytes.length);
        bos.flush();
        bos.close();

        if (!quiet) {
            System.out.println("Response Received (" + (Calendar.getInstance().getTimeInMillis() - startTime) + " ms)");
        }

        return outFile.getAbsolutePath();
    }
    
    public MRSessionDocument getMrSessionFromHost(String id) throws Exception {
        return getMrSessionFromHost(id, true);
   }
    
    public MRSessionDocument getMrSessionFromHost(String id, boolean fullPath) throws Exception {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	streamXML(id, fullPath, out, quiet);
    	ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
    	MRSessionDocument mrSession = (MRSessionDocument)new XmlReader().read(in);
        return mrSession;
   }
    
    public XnatMrsessiondataBean getMrSessionAsBeanFromHost(String id) throws Exception {
    	return getMrSessionAsBeanFromHost(id, true);
    }
    
    public XnatMrsessiondataBean getMrSessionAsBeanFromHost(String id, boolean fullPath) throws Exception {
    	ByteArrayOutputStream out = new ByteArrayOutputStream();
    	streamXML(id, fullPath, out, quiet);
    	ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        XnatMrsessiondataBean mrSession = (XnatMrsessiondataBean) getBeanFromStream(in);
        out.close(); in.close();
        return mrSession;
   }
    
  
    
    

    
    public MRSessionDocument getMrSessionFromHost(String id, String dir) throws Exception {
        String createdFile = getXML(id,dir);
        MRSessionDocument mrSession = (MRSessionDocument)new XmlReader().read(createdFile, true);
        return mrSession;
   }

    public XnatMrsessiondataBean getMrSessionFromAsBeanHost(String id, String dir) throws Exception {
        String createdFile = getXML(id,dir);
        XnatMrsessiondataBean mrSession = (XnatMrsessiondataBean) getBeanFromXml(createdFile, true);
        return mrSession;
   }

    
   public void setQuiet(boolean q) {
       quiet = q;
   }
    
    
    public static void main(String args[]) throws MalformedURLException {
        MRXMLSearch search = new MRXMLSearch("https://cndabeta.wustl.edu","mohanar","***");
                //String createdFile = search.getXML("OAS1_0001_MR1", ".");
        try {
        	MRSessionDocument mrSession =search.getMrSessionFromHost("25770", false);
        	//MRSessionDocument mr =  search.getMrSessionFromHost("070914_TC25419");
 //       	MRSessionDocument mr =  search.getMrSessionFromHost("070914_TC25419", System.getProperty("user.home"));
           System.out.println(mrSession.toString());
        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println("All done");
        
    }
    
    XmlObject xml;
}
