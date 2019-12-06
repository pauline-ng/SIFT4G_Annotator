package org.siftdna.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.siftdna.main.SIFTConstants;


public class UrlFileReader {
	private static ArrayList<String> contentList = null;
	private String content = null;
	public String FileReader(String link){
		try{
			URL url = new URL(link);
			URLConnection urlc = url.openConnection();
		    urlc.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; "
		            + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
	//	    InputStream in = urlc.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
			String line;
			contentList = new ArrayList<String>();
			while((line = in.readLine()) != null){
				contentList.add(line);
			}
			in.close();
			content = StringUtils.join(contentList, " ");
		}
		catch(MalformedURLException e){
			content = "NA";
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println (e);
			content = "NoInternet";
		}
		return content;
	}
}
