package org.siftdna.siftobjects;

import java.util.List;

/*******************************************************
 * 
 * Class to represent a user query in SIFT format 
 * 
 * 
 * @author simnl
 * @date: 2013.12.26
 * 
 *******************************************************/


public class SIFTQuery extends AbstractSIFTQuery {
	
	public SIFTQuery(String query) {
		super(query);
		parseQuery(query);
	}//end constructor
	
	@Override
	public int compareTo(ISIFTQuery q) {
		// Compare genomic co-ordinate first
		Long thisPos = Long.valueOf(this.getPosition());
		Long thatPos = Long.valueOf(q.getPosition());
		int comparisonResult = thisPos.compareTo(thatPos);
//		int comparisonResult = Long.compare(this.getPosition(), q.getPosition());
		if (comparisonResult == 0) {
			// co-ordinates are the same, then we compare alternate allele
			// so that it is sorted by A,C,G,T
			List<String> myAltAlleles = this.getAltAlleles();
			String myAltAllele = myAltAlleles.get(0);
			List<String> othAltAlleles = q.getAltAlleles();
			String othAltAllele = othAltAlleles.get(0);
			comparisonResult = myAltAllele.compareTo(othAltAllele);
		}
		return comparisonResult;
	}


	@Override
	public void parseQuery(String query) {
		String [] components = this.query.split(",");
		//this.chromosome = components[0];
		setPosition(Long.valueOf(components[1]).longValue()); 
		//this.position =		 
		for (String c : components) {
			
			if (c.contains("/")) {
				String [] a = c.split("/");
				setRefAllele(a[0]);
				this.setAltAlleles(a[1]);
				break;				
			}
		}		
	}	
}
