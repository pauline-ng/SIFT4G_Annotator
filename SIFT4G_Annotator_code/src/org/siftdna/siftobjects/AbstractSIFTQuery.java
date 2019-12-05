package org.siftdna.siftobjects;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSIFTQuery implements ISIFTQuery, Comparable<ISIFTQuery> {

	
	public abstract void parseQuery(String query);

	public String query = null;
	
	private String originalChr = null; // for outputting
	private String chromosome = null;
	private long position = -1;
	private int orn = 0;
	private String refAllele = null;
	private String originalAltAllele = null;
	private List<String> altAlleles = null;
	private List<String> headers = null; // If VCF has more than 1 sample, we need to store them
	
	public AbstractSIFTQuery(String query) {
		super();
		this.query = query;
		this.headers = new ArrayList<String>();
		parseQuery(query);
	}
	
	@Override
	public void addHeader(String header) {
		this.headers.add(header);
	}
	
	@Override
	public List<String> getHeaders() {
		return this.headers;
	}
	

	@Override
	public String getOriginalChr() {
		return this.originalChr;
	}
	
	
	
	@Override
	public void setOriginalChr(String chromosome) {
		this.originalChr = chromosome;
	}
	
	@Override
	public String getChromosome() {
		return this.chromosome;
	}
	
	@Override
	public void setChromosome(String chromosome) {
		
		// Different input files may or may not have "chr" appended
		// Even VCF files are not consistent.
		// For the sake of consistency, we remove this chr
		this.originalChr = chromosome;

		this.chromosome = chromosome;
		if (chromosome.startsWith("chr")) {
			this.chromosome = chromosome.substring(3);
		}
		

	}
	
	@Override
	public String getQuery() {
		return this.query;
	}
	
	@Override 
	public void setPosition(long position) {
		this.position = position;
	}
	
	@Override
	public long getPosition() {
		return position;
	}

	@Override 
	public void setOrn(int orn) {
		this.orn = orn;
	}
	
	@Override
	public int getOrn() {
		return orn;
	}
	
	
	@Override
	public String getRefAllele() {
		return refAllele;
	}

	@Override
	public void setRefAllele(String refAllele) {
		this.refAllele = refAllele;
	}
	
	@Override
	public List<String> getAltAlleles() {
		return altAlleles;
	}
	
	@Override
	public void setAltAlleles(String altAlleles) {
		originalAltAllele = altAlleles; // VCF can have more than 1 allele (eg. A,C)
		String [] alts = altAlleles.split(",");		
		this.altAlleles = new ArrayList<String>();
		for(String alt : alts) {
			this.altAlleles.add(alt);			
		}
	}

	@Override
	public String getOriginalAltAlleles() {
		return this.originalAltAllele;
	}
	
	@Override
	public int compareTo(ISIFTQuery q) {
		// Compare genomic co-ordinate first
		Long thisPos = Long.valueOf(this.getPosition());
		Long thatPos = Long.valueOf(q.getPosition());
		int comparisonResult = thisPos.compareTo(thatPos);
//		int comparisonResult = Long.compareTo(this.getPosition(), q.getPosition());
		if (comparisonResult == 0) {
			// co-ordinates are the same, then we compare alternate allele
			// so that it is sorted by A,C,G,T
			List<String> myAlternates = this.getAltAlleles();
			String myAlternate = myAlternates.get(0); // Do this for now.
			List<String> othAlternates = q.getAltAlleles();
			String othAlternate = othAlternates.get(0);
			comparisonResult = myAlternate.compareTo(othAlternate);
		}
		return comparisonResult;
	}
	
}
