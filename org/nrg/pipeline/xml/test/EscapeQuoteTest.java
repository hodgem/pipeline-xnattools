/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.pipeline.xml.test;

public class EscapeQuoteTest {
	public static void main(String args[]) {
		String[] input = {null, "mohana", "'mohana", "mohana'", "moh'an'a", "mo'hana"};
		for (int i = 0; i < input.length; i++) {
			String rtn = input[i];
			if (rtn == null) continue;
        	String[] pieces = rtn.split("'");
        	rtn = "";
           for (int j=0; j < pieces.length; j++) {
        	   rtn += "'" + pieces[j] + "'" + "\\'"; 
           }
           if (rtn.endsWith("\\'") && !input[i].endsWith("'") ) {
        	   int indexOfLastQuote = rtn.lastIndexOf("\\'");
        	   if (indexOfLastQuote != -1)
        	   rtn = rtn.substring(0,indexOfLastQuote);
           }
           System.out.println("Escaped " + input[i] + " :" + rtn +": ");
        }
	}
}
