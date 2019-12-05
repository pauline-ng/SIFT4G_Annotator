package org.siftdna.siftobjects;

import java.util.List;

public interface ISIFTQuery extends Comparable<ISIFTQuery> {
	
	String getQuery();
	
	String getOriginalChr();
	void setOriginalChr(String chr);
	
	
	String getChromosome();
	void setChromosome(String chromosome);
	long getPosition();
	void setPosition(long position);
	void setOrn(int orn);
	int getOrn();
	String getRefAllele();
	List<String> getAltAlleles();
	void setRefAllele(String refAllele);
	void setAltAlleles(String altAlleles);
	String getOriginalAltAlleles();
	
	
	void addHeader(String header);
	List<String> getHeaders();
	
}//end ISIFTQuery interface
