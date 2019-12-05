package org.siftdna.siftobjects;

import java.util.ArrayList;
import java.util.List;

import org.siftdna.main.SIFTConstants;

/*************************************************
 * 
 * Class to represent SIFT query in VCF format
 * 
 * @author simnl
 * @date 2013.12.27
 *************************************************/


public class SIFTVCFQuery extends AbstractSIFTQuery {
	
	private String identity = null;
	private String quality = null;
	private String filter = null;
	private String info = null;
	private String format = null;
	

	
	public SIFTVCFQuery(String query) {
		super(query);		
	}

	@Override
	public void parseQuery(String query) {
		// Depending ont which VCF version, we may or may not have FORMAT and others
		// #CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT	VSample	9
		// chr1	115256514	.	G	A	100	PASS	AO=72;DP=235;FAO=72;FDP=235;FR=.;FRO=163;FSAF=37;FSAR=35;FSRF=95;FSRR=68;FWDB=-0.0531628;HRUN=2;LEN=1;MLLD=68.4832;RBI=0.0556319;REFB=-0.0559517;REVB=0.0163899;RO=162;SAF=37;SAR=35;SRF=95;SRR=67;SSEN=0;SSEP=0;STB=0.547965;SXB=0.96443;TYPE=snp;VARB=0.0488615;OID=.;OPOS=115256514;OREF=G;OALT=A;OMAPALT=A	GT:GQ:DP:FDP:RO:FRO:AO:FAO:SAR:SAF:SRF:SRR:FSAR:FSAF:FSRF:FSRR	0/1:99:235:235:162:163:72:72:35:37:95:67:35:37:95:68	./.:.:.:.:.:.:.:.:.:.:.:.:.:.:.:.		
		String [] components = query.split("\t");		
		
		setChromosome(components[SIFTConstants.VCF_CHR_COL]);
		setPosition(Long.valueOf(components[SIFTConstants.VCF_POS_COL]).longValue());
		setRefAllele(components[SIFTConstants.VCF_REF_COL]);
		setAltAlleles(components[SIFTConstants.VCF_ALT_COL]);
		
		this.identity = components[SIFTConstants.VCF_ID_COL];
		this.quality = components[SIFTConstants.VCF_QUAL_COL];
		this.filter = components[SIFTConstants.VCF_FILTER_COL];
		this.info = components[SIFTConstants.VCF_INFO_COL];
		if (components.length < (SIFTConstants.VCF_FORMAT_COL + 1)) {
			this.format = "";
		} else {
			this.format = components[SIFTConstants.VCF_FORMAT_COL];
		}
		for(int j = SIFTConstants.VCF_FIRST_SAMPLE_COL; j < components.length; j++) {			
			String comp = components[j];
			if (comp != null) {				
				addHeader(comp);
			}
		}
	}
	
}//end SIFTVCFQuery class
