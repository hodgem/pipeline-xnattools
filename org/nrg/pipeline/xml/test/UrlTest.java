/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

*/

package org.nrg.pipeline.xml.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UrlTest {
	 private String toString(InputStream inputStream) throws IOException {
		    String string;
		    StringBuilder outputBuilder = new StringBuilder();
		    if (inputStream != null) {
		      BufferedReader reader =
		          new BufferedReader(new InputStreamReader(inputStream));
		      while (null != (string = reader.readLine())) {
		        outputBuilder.append(string).append('\n');
		      }
		    }
		    return outputBuilder.toString();
		  }

	 public void test() {
			try {
				URL httpsu = new URL("https://www.bankofamerica.com");
				URLConnection uc = httpsu.openConnection();
				InputStream is = uc.getInputStream();
				System.out.println(toString(is));
			}catch(Exception e) {
				e.printStackTrace();
			}
	 }
	 
	public static void main(String args[]) {
		UrlTest u = new UrlTest();
		u.test();
	}
}
