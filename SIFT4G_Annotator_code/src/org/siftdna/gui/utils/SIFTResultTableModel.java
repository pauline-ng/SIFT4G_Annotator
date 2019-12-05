package org.siftdna.gui.utils;

import javax.swing.table.DefaultTableModel;

public class SIFTResultTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	
	public SIFTResultTableModel() {
		super();
	}
		
	@Override
	public boolean isCellEditable (int row, int col) {
		return false;
	}


}//end 
