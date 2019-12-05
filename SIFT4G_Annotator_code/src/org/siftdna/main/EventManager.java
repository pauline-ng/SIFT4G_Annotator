package org.siftdna.main;

import java.io.BufferedWriter;
import java.io.FileWriter;


public class EventManager {
	static BufferedWriter Result_bfwriter1 = null;
	static FileWriter Result_fwriter1= null;
	
	static public EventManager instance = null;
	
	public static EventManager GetInstance(){
		if(instance == null){
			instance = new EventManager();
			try{
				Result_fwriter1 = new FileWriter("Events.log");
				Result_bfwriter1 = new BufferedWriter(Result_fwriter1);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return instance;
	}
	
	public static void Demote(){
		try{
			Result_bfwriter1.close();
			Result_fwriter1.close();
			Result_fwriter1 = null;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static synchronized void writeLogEvent(String EventDescription){
		if(Result_bfwriter1 != null){
			try{
			Result_bfwriter1.append("Debug:\t" +EventDescription);
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
	
	public static synchronized  void writeErrorEvent(String ErrorDescrption){
		try{
		Result_bfwriter1.append("Error:\t" +ErrorDescrption);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
