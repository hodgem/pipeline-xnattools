/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 */

package org.nrg.pipeline.xmlreader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.nrg.xdat.bean.base.BaseElement;
import org.nrg.xdat.bean.reader.XDATXMLReader;
import org.xml.sax.SAXException;

//////////////////////////////////////////////////////////////////////////
////ClassName: XmlReader 
/**

XmlReader reads an xml instance document and validates the same.

@author mohanar
@version $Id: XmlReader.java,v 1.1 2008/07/28 20:55:17 mohanar Exp $
@since Pipeline 1.0
*/

public class XmlReader {

    public XmlObject read(ByteArrayInputStream in) throws XmlException, IOException {
        ArrayList errors = new ArrayList();
        XmlOptions xopt = new XmlOptions();
        xopt.setErrorListener(errors);
         XmlObject xo = XmlObject.Factory.parse(in, xopt);
         if (errors.size() != 0) {
             throw new XmlException(errors.toArray().toString());
         }
         return xo;
    }
    
    public BaseElement getBeanFromXml(String xmlfilePath, boolean deleteFile) throws IOException, SAXException{
        XDATXMLReader reader = new XDATXMLReader();
        File f = new File(xmlfilePath);
        InputStream fis = new FileInputStream(f);
        BaseElement base = reader.parse(fis);
        if (deleteFile) f.delete();
        return base;
    }

	
    public XmlObject read(String xmlFileName) throws XmlException, IOException {
        return read(xmlFileName,false);
    }
    
    /* Read an xml file and return its java representation.
     * After parsing the document, validate the document.
     * 
     */
    public XmlObject read(String xmlFileName, boolean delete) throws XmlException, IOException {
        File xmlFile = new File(xmlFileName); 
        //Bind the instance to the generated XMLBeans types.
        ArrayList errors = new ArrayList();
        XmlOptions xopt = new XmlOptions();
        xopt.setErrorListener(errors);
         XmlObject xo = XmlObject.Factory.parse(xmlFile, xopt);
         if (errors.size() != 0) {
             throw new XmlException(errors.toArray().toString());
         }
         //String err = XMLBeansUtils.validateAndGetErrors(xo);
         //if (err != null) {
         //    throw new XmlException("Invalid XML " + xmlFile + "\n" + errors);
        //}
         if (delete) xmlFile.delete();
         return xo;
    }

   
    
    public static void main(String args[]) {
        try {
            
           ArrayList errors = new ArrayList();
           XmlOptions xopt = new XmlOptions();
           xopt.setErrorListener(errors);
           XmlObject xo  =  XmlObject.Factory.parse(new File("C:\\eclipse\\workspace\\FIV1.2\\000107_vc1543.xml"), xopt);
           if (errors.size() != 0) {
               System.out.println(errors.toArray().toString());
           }
           System.out.println("Doc is " + xo.type);
           
           //MRSessionDocument mrSession = (MRSessionDocument) xo;
           
        }catch(Exception e){e.printStackTrace();}
        
    }
    
}
