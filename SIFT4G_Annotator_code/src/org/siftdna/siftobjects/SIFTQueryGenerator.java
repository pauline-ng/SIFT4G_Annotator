package org.siftdna.siftobjects;

import org.siftdna.main.SIFTConstants;

public class SIFTQueryGenerator {
	
	public SIFTQueryGenerator() {
		super();		
	}//end constructor
	
	
	public static ISIFTQuery generateQuery(String line, String fileFormat) {
		ISIFTQuery query = null;
		if (fileFormat.equals(SIFTConstants.VCF_FORMAT)) {
			query = new SIFTVCFQuery(line);
		} else if (fileFormat.equals(SIFTConstants.SIFT_RESIDUE_FORMAT)) {
			query = new SIFTResidueQuery(line);
		} else if (fileFormat.equals(SIFTConstants.SIFT_SPACE_FORMAT)) {
			query = new SIFTSpaceQuery(line);
		}
		return query;
	}
	
	
}
