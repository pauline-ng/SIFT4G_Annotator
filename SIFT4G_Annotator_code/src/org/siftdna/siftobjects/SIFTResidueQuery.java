package org.siftdna.siftobjects;

import org.siftdna.main.SIFTConstants;

/*******************************************************
 * 
 * Class to represent a user query in SIFT residue format 
 * 
 * @author simnl
 * @date: 2013.12.26
 * 
 *******************************************************/


public class SIFTResidueQuery extends AbstractSIFTQuery {
	
	public SIFTResidueQuery(String query) {
		super(query);
		parseQuery(query);
	}//end constructor
	
	@Override
	public void parseQuery(String query) {
		
		String [] components = this.query.split(SIFTConstants.SIFT_DELIMITER);
		
		setChromosome(components[SIFTConstants.SIFT_CHR_COL]);
		
		setPosition(Long.valueOf(components[SIFTConstants.SIFT_RESIDUE_POS_COL]).longValue());
		setRefAllele(components[SIFTConstants.SIFT_RESIDUE_ALLELE_COL]);
		setAltAlleles(components[SIFTConstants.SIFT_RESIDUE_COMMENTS_COL]);
		setOrn(Integer.valueOf(components[SIFTConstants.SIFT_RESIDUE_ORN_COL]).intValue());
		
		String alleles = components[SIFTConstants.SIFT_RESIDUE_ALLELE_COL];
		String [] a = alleles.split("/");
		setRefAllele(a[0]);
		setAltAlleles(a[1]);
	}	
}
