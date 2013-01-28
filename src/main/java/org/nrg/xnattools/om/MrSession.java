/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.xnattools.om;

import org.nrg.xnattools.xml.XMLSearch;

public class MrSession {
    
    public MrSession() {
        
    }
    
    public Object[] getAllMrSessionIds(String host, String username, String password) throws Exception {
        XMLSearch search = new XMLSearch(host,username,password);
        Object[] sessionIds = search.getIdentifiers("xnat:mrSessionData.ID","xnat:mrSessionData");
        if (sessionIds != null) {
            System.out.println("Total sesions are " + sessionIds.length);
        }else {
            System.out.println("Couldnt get the sessions");
        }
        return sessionIds;
    }
    
}
