package org.siftdna.gui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.siftdna.gui.utils.SIFTResultTableModel;
import org.siftdna.main.SIFTConstants;
import org.siftdna.siftobjects.SingleSIFTResult;

public class MO_ResultsPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	private JTable table = null;
	private SIFTResultTableModel tablemodel  = null;

	
	/*****************************************************************
	 * MO_ResultsPanel is empty until
	 * the entire thing completes.
	 * Then it prints out number of queries with SIFT annotations
	 * and number without SIFT annotations
	 * 
	 *****************************************************************/
		
	public MO_ResultsPanel() {
		super();
		setLayout(new BorderLayout());
		
		new Thread(this).start();
	}//end constructor
	
	@Override
	public void run() {		
		prepareGUI();
	}
	
	private void prepareGUI() {
		
		String [] colHeadings = SIFTConstants.RESULTS_COL_HEADINGS;	
		tablemodel = new SIFTResultTableModel();
		tablemodel.setColumnIdentifiers(colHeadings);
		table = new JTable(tablemodel);			
		
		JScrollPane scrollPane = new JScrollPane(table);		
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);
		
	}//end prepareGUI
	
	public void clearTable() {
		tablemodel.getDataVector().removeAllElements();
		tablemodel.setRowCount(0);
		tablemodel.fireTableDataChanged();
	}

	public void addRow(String [] result) {
		Vector<String> vector = new Vector<String>();
		for (String r : result) {
			vector.add(r);
		}
		//vector = sort(vector);
		tablemodel.addRow(vector);
		
		if (table.getRowCount() > 0) { 
			setVisible(true);
			tablemodel.fireTableDataChanged();
		}

	}
	
	
}//end MO_ResultsPanel
