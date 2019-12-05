package org.siftdna.genomes;

/*
 * Copyright (c) 2007-2012 The Broad Institute, Inc.
 * SOFTWARE COPYRIGHT NOTICE
 * This software and its documentation are the copyright of the Broad Institute, Inc. All rights are reserved.
 *
 * This software is supplied without any warranty or guaranteed support whatsoever. The Broad Institute is not responsible for its use, misuse, or functionality.
 *
 * This software is licensed under the terms of the GNU Lesser General Public License (LGPL),
 * Version 2.1 which is available at http://www.opensource.org/licenses/lgpl-2.1.php.
 */

//package org.broad.igv.feature.genome;

//import org.broad.igv.util.HttpUtils;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.siftdna.main.SIFTConstants;


public class GenomeListItem {

    private String displayableName;
    private String location;
    private String fileName;
    private ArrayList<GenomeListItem> serverGenomeArchiveList;
    private HashMap<String, String>downloaded_genomes;
    boolean serverGenomeListUnreachable = false;
    public static final GenomeListItem ITEM_MORE;
    public static Map<String, GenomeListItem> genomeItemMap = new CI.CILinkedHashMap<GenomeListItem>();
    
    static{
        ITEM_MORE = new GenomeListItem("More...", "","");
    }


 
    public GenomeListItem(String displayableName, String location, String filename) {
        this.displayableName = displayableName;
        this.location = location;
        this.fileName = filename;
    }
    
    private static GenomeListItem theInstance;


    public String getDisplayableName() {
        return displayableName;
    }

    public ArrayList<GenomeListItem> getServerGenomeList() {

        if (serverGenomeListUnreachable) {
            return null;
        }

        if (serverGenomeArchiveList == null) {
            serverGenomeArchiveList = new ArrayList<GenomeListItem>();
            BufferedReader dataReader = null;
            InputStream inputStream = null;
            
            try {
            	//inputStream = GenomeListItem.class.getResource("http://sift-db.bii.a-star.edu.sg/public/Databases.txt");
                URL dbUrl = new URL(SIFTConstants.SIFTDB_LINK);
            	inputStream = dbUrl.openStream();
                dataReader = new BufferedReader(new InputStreamReader(inputStream));
                String genomeRecord;

                while ((genomeRecord = dataReader.readLine()) != null) {

                    if (genomeRecord.startsWith("<") || genomeRecord.startsWith("(#")) {
                        continue;
                    }

                    if (genomeRecord != null) {
                        genomeRecord = genomeRecord.trim();

                        String[] fields = genomeRecord.split("\t");
                        if ((fields != null) && (fields.length >= 2)) {

                            String name = fields[0];
                            String url = fields[1];
                            String filename = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".zip"));
                            GenomeListItem item = new GenomeListItem(name, url, filename);
                            serverGenomeArchiveList.add(item);
                            genomeItemMap.put(filename, item);

                        } else {
                            //log.error("Found invalid server genome list record: " + genomeRecord);
                        }
                    }
                }
            } catch (Exception e) {
                serverGenomeListUnreachable = true;
                serverGenomeArchiveList = null;

            } finally {
                if (dataReader != null) {
                    try {
                        dataReader.close();
                    } catch (IOException e) {
                        //log.error(e);
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                       // log.error(e);
                    }
                }
            }
        }

        return serverGenomeArchiveList;
    }
    /*public String getId() {
        return id;
    }*/

    public String getLocation() {
        if(location == null){
            GenomeListItem newItem = GenomeListItem.searchGenomeList(this.displayableName, GenomeListItem.getInstance().getServerGenomeList());
            if(newItem != null){
                this.displayableName = newItem.displayableName;
                this.location = newItem.location;
                this.fileName = newItem.fileName;
            }
        }
        return location;
    }
    
    public String getFileName(){
    	if(fileName == null){
    		GenomeListItem newItem = GenomeListItem.searchGenomeList(this.displayableName, GenomeListItem.getInstance().getServerGenomeList());
    		if(newItem != null){
    			this.displayableName = newItem.displayableName;
    			this.location = newItem.location;
    			this.fileName = newItem.fileName;
    		}
    	}
		return fileName;
    }

    public static GenomeListItem searchGenomeList(String genomeName, Iterable<GenomeListItem> genomeList) {
        if (genomeList == null) return null;
        for (GenomeListItem item : genomeList) {
            if (item.getDisplayableName().equals(genomeName)) {
                return item;
            }
        }
        return null;
    }

	private synchronized static GenomeListItem getInstance() {
		// TODO Auto-generated method stub
        if (theInstance == null) {
            theInstance = new GenomeListItem();
        }
        return theInstance;
	}
    
    public  GenomeListItem() {
		// TODO Auto-generated constructor stub
	}

	@Override
    public String toString() {
        return getDisplayableName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GenomeListItem that = (GenomeListItem) o;

        if (displayableName != null ? !displayableName.equals(that.displayableName) : that.displayableName != null)
            return false;
        
        if (location != null ? !location.equals(that.location) : that.location != null) return false;
        
        if(fileName != null ? !fileName.equals(that.fileName) : that.fileName !=null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayableName != null ? displayableName.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        return result;
    }

    /**
     * Check if the genome being referred to points to a local (on this machine)
     * sequence, which was downloaded from a server. So a user-created genome
     * which points to a local fasta file will return false, but one created
     * by {@link GenomeManager#downloadWholeGenome(String, java.io.File, java.awt.Frame)}
     * will return true
     * @return
     */
    @SuppressWarnings("null")
	public Object[] list_downloaded(String dirname) {
		
		downloaded_genomes = new HashMap<String, String>();
		if(dirname != null){
			File dir = new File(dirname);
			for (final File fileEntry : dir.listFiles()) {
				if (fileEntry.isDirectory()) {
					if(genomeItemMap.get(fileEntry.getName()) != null){
						String displayName = genomeItemMap.get(fileEntry.getName()).getDisplayableName();
						downloaded_genomes.put(displayName, fileEntry.getAbsolutePath());
					}
				}
			}
		}	
		return downloaded_genomes.keySet().toArray();
	}
    
    public String get_local_path(String species){
		return downloaded_genomes.get(species);
		
	}
    
    public String get_local_fileName(String name){
    	GenomeListItem newItem = GenomeListItem.searchGenomeList(name, GenomeListItem.getInstance().getServerGenomeList());
    	if(newItem != null){
    		return newItem.fileName;
    	}
    	else{
    		return null;
    	}
    }
    

   public boolean hasDownloaded(String SpeciesName){
	   return (downloaded_genomes.containsKey(SpeciesName))? true : false;
    }
   
   
   public static ArrayList<GenomeListItem> getGenomes() {
       return new ArrayList<GenomeListItem>(genomeItemMap.values());
   }

    
}
