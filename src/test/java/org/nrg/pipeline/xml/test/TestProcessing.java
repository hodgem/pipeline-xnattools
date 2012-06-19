/* 
 *	Copyright Washington University in St Louis 2006
 *	All rights reserved
 * 	
 * 	@author Mohana Ramaratnam (Email: mramarat@wustl.edu)

 */

package org.nrg.pipeline.xml.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Test;

public class TestProcessing {
	@Test
	public void testEscapeQuotes() {
		String[] input = { null, "mohana", "'mohana", "mohana'", "moh'an'a", "mo'hana" };
		String[] output = { null, "'mohana'", "''\\''mohana'", "'mohana'\\'", "'moh'\\''an'\\''a'", "'mo'\\''hana'" }; 
		for (int i = 0; i < input.length; i++) {
			String rtn = input[i];
			if (rtn == null)
				continue;
			String[] pieces = rtn.split("'");
			rtn = "";
			for (int j = 0; j < pieces.length; j++) {
				rtn += "'" + pieces[j] + "'" + "\\'";
			}
			if (rtn.endsWith("\\'") && !input[i].endsWith("'")) {
				int indexOfLastQuote = rtn.lastIndexOf("\\'");
				if (indexOfLastQuote != -1)
					rtn = rtn.substring(0, indexOfLastQuote);
			}
			assertNotNull(input[i]);
			assertNotNull(rtn);
			assertTrue(output[i].equals(rtn));
		}
	}

	@Test
	public void testToString() {
		try {
			URL httpsu = new URL("https://www.bankofamerica.com");
			URLConnection uc = httpsu.openConnection();
			InputStream is = uc.getInputStream();
			String results = toString(is);
			assertNotNull(results);
			assertTrue(results.length() > 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String toString(InputStream inputStream) throws IOException {
		String string;
		StringBuilder outputBuilder = new StringBuilder();
		if (inputStream != null) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					inputStream));
			while (null != (string = reader.readLine())) {
				outputBuilder.append(string).append('\n');
			}
		}
		return outputBuilder.toString();
	}
}
