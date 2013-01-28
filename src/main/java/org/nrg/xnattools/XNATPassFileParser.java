/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import org.nrg.xnattools.exception.UserNameNotFoundException;

//import com.sun.xml.internal.ws.util.StringUtils;

public class XNATPassFileParser {
    Properties xnatPassFile;
    boolean passFileExists = false;
    String passFilePath ;
        
    public XNATPassFileParser() {
        passFilePath = System.getProperty("user.home")+File.separator + ".xnatPass";
        if (exists(passFilePath)) {
            passFileExists = true;
            setup();
        }
    }
    
    private void setup() {
        System.out.println("Parsing " + passFilePath);
        try {
            xnatPassFile = new Properties();
            FileInputStream filein = new FileInputStream (passFilePath);
            DataInputStream input = new DataInputStream (filein);
            xnatPassFile.load(input);
        }catch(FileNotFoundException fne) {
            passFileExists = false;
        }catch(IOException fne) {
            passFileExists = false;
        }
    }
    
    public String getUserName(String host) throws UserNameNotFoundException {
        String rtn = null;
        host = fix(host);
        rtn = getProperty(host,true);
        if (rtn == null) throw new UserNameNotFoundException();
        return rtn;
    }
    
    private String getProperty(String pattern, boolean returnKey) {
        String rtn = null;
        Enumeration keys = xnatPassFile.propertyNames();
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (key.indexOf(pattern) != -1) {
                if (returnKey)
//                    rtn = StringUtils.replace(key,pattern,"");
//                else
//                    rtn = xnatPassFile.getProperty(key);
                break;
            }
        }
        return rtn;
        
    }
    
    private String fix(String host) {
//        if (host.startsWith("http://")) {
//            host = StringUtils.replace(host,"http://","@");
 //       }
        if (!host.startsWith("@")) host = "@" + host;
        if (!host.endsWith("/")) {
            host += "/";
        }
        return host;
    }
    
    public String getUsersPassword(String username,String host) throws UserNameNotFoundException{
        host = fix(host);
        return xnatPassFile.getProperty(username + host);
    }
    
    public boolean passFileExists() {
        return passFileExists;
    }
    
    public static void main(String args[]) {
        try {
            XNATPassFileParser p = new XNATPassFileParser();
            String u = p.getUserName("http://192.168.37.195/cnda");
            String pwd = p.getUsersPassword(u,"http://192.168.37.195/cnda");
            System.out.println("Username = " + u + " pwd " + pwd);
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public  boolean exists(String fileName) {
        boolean rtn = false;
        File dir = new File(fileName);
        if (dir.exists()) rtn = true;
        return rtn;
    }
}
