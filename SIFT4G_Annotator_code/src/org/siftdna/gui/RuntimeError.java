package org.siftdna.gui;

import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class RuntimeError extends JPanel implements Runnable{
	  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public RuntimeError(){
		super();
	}
	
	public void createChromosomeMismatchMessage (String unlistedChr) {
		final JPanel panel = new JPanel();
		JOptionPane.showMessageDialog(panel, "<html><body><p style='width: 250px;'>The following chromosomes (or scaffolds/contigs) are not found in the SIFT 4G database and will not be annotated:</p><p>" + unlistedChr + "<p></p><p>Click OK to continue with annotation.</p><p></p><p><i>Please contact <A HREF=\"http://sift-dna.org/sift-bin/contact.pl\">us</A> if you have any questions</i>.</p></body></html>", 
				null, JOptionPane.PLAIN_MESSAGE);
		
	
	}
	
	public void createColumnError() {
		    final JPanel panel = new JPanel();

		    JOptionPane.showMessageDialog(panel, "Input VCF file should contain at least 8 columns", "Error", JOptionPane.ERROR_MESSAGE);
		    System.exit(0);
		  }

	public void createColumnError(String errorline) {
	    final JPanel panel = new JPanel();

	    JOptionPane.showMessageDialog(panel, "Input VCF file should contain at least 8 columns.\nLine with error:\n" + errorline, "Error", JOptionPane.ERROR_MESSAGE);
	    System.exit(0);
	  }

	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		createColumnError();
	}
	
    public void createErrorForUnorderedFile (String badinputline) {
        final JPanel panel = new JPanel();

        JOptionPane.showMessageDialog(panel, "Chromosome positions should be sorted in ascending order.\nThe lines around:\n"
                    + badinputline.replace("\t", "   ") + "\n seem unordered.", "Error", JOptionPane.ERROR_MESSAGE);
        System.exit(0);
    }

    
		}


