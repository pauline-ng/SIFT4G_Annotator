package org.siftdna.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
//import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OutputFileManager {
	FileOutputStream Result_fout = null; 
	BufferedWriter Result_bfwriter = null;
	FileWriter Result_fwriter= null;
	BufferedWriter Result_bfwriter1 = null;
	FileWriter Result_fwriter1= null;

	FileInputStream temp_fin = null; 
	InputStreamReader temp_isr = null; 
	BufferedReader temp_br = null;
	
	//Create the TSV output file
	public void CreateTSVOutPutFile(String Directory_Path,String FinalResult, List<String> Headers) throws IOException{
		//OutPut File
		//Result_fwriter = new FileWriter(Directory_Path + System.getProperty("file.separator") + FinalResult + ".tsv");
		//Result_bfwriter = new BufferedWriter(Result_fwriter);

		//only annotated OutPut File
		String file = FinalResult.substring(0, FinalResult.lastIndexOf("_SIFT"));
		Result_fwriter1 = new FileWriter(Directory_Path + System.getProperty("file.separator") + file + "_SIFTannotations.xls");
		Result_bfwriter1 = new BufferedWriter(Result_fwriter1);
		for(int i =0; i <Headers.size();i++){
			//Result_bfwriter.write(Headers.get(i));
			Result_bfwriter1.write(Headers.get(i));
		}

	}
	
	public void CreateVCFOutPutFile(String Directory_Path,String FinalResult, List<String> Headers) throws IOException{
		//Output file
		Result_fwriter = new FileWriter(Directory_Path + System.getProperty("file.separator") + FinalResult + ".vcf");
		Result_bfwriter = new BufferedWriter(Result_fwriter);
		for(int i =0; i <Headers.size();i++){
			Result_bfwriter.write(Headers.get(i));
		}

	}
	
	public void CloseTSVOutPutFile(){
		try {
			Result_bfwriter.close();
			Result_bfwriter1.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void CloseVCFOutPutFile(){
		try {
			Result_bfwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// write each of the temp files in results folder to a final result file
	public void WriteFile(String Directory_Path, String tempFile,String FinalResult) throws IOException {
		temp_fin = new FileInputStream(Directory_Path + System.getProperty("file.separator") + tempFile);
		temp_isr = new InputStreamReader(temp_fin);
		temp_br = new BufferedReader(temp_isr);

		String line;
		while ((line = temp_br.readLine()) != null) {
			if (FinalResult.contains(".tsv")) {
				String[] components = line.split("\t");
				// Checking for Transcripts available or not(to get only annotated).
				if (components.length >= 5 && components[5].length() != 0 && !components[5].equals("NA")) {
					Result_bfwriter1.append(line + "\r\n");
				}
			}
			else{
				Result_bfwriter.append(line + "\r\n");
			}
		}
		temp_br.close();
	}
	
	public List<String> searchFiles(String Directory_Path,String Filter){
		List<String> Files_list = new ArrayList<String>();
		File folder = new File(Directory_Path);
		File files[]= folder.listFiles();
		for(int i=0; i<files.length; i++){
			if(files[i].getName().contains("FinalOutput"))
				continue;
			if(files[i].getName().endsWith(Filter)){
				Files_list.add(files[i].getName());
			}
		}
		return Files_list;
	}
	
	public int DeleteFile(String Directory_Path, String FileName){
		File folder = new File(Directory_Path + System.getProperty("file.separator") + FileName);
		folder.delete();
		return 0;
	}
}//end of class
	