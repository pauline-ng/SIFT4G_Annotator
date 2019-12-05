package org.siftdna.gui;

public class SIFTSummary {

	
	public static String [] getSummaryLine(String chromosome, long numberWithSIFTScores, long numberWithoutSIFTScores) {
		if (chromosome != null) {
			String [] summary = {chromosome, Long.toString(numberWithSIFTScores), Long.toString(numberWithoutSIFTScores) };
			return summary;			
		} else {
			return null;
		}
	}
}
