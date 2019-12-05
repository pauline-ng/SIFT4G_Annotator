package org.siftdna.gui;

import java.awt.BorderLayout;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.siftdna.gui.utils.SIFTResultTableModel;
import org.siftdna.main.SIFTConstants;
import org.siftdna.siftobjects.SingleSIFTResult;

public class ResultsPanel extends JPanel implements Runnable {

	private static final long serialVersionUID = 1L;

	private JTable table = null;
	private SIFTResultTableModel tablemodel  = null;
	
	public ResultsPanel() {
		super();
		
		this.setLayout(new BorderLayout());
		new Thread(this).start();
		
		
	}//end ResultsPanel
	
	
	private void initiateTable() {
		//vector = new Vector<String>();
		
		String [] colHeadings = SIFTConstants.COL_HEADINGS;	
		
		tablemodel = new SIFTResultTableModel();
		tablemodel.setColumnIdentifiers(colHeadings);
		table = new JTable(tablemodel);
		
		JScrollPane scrollPane = new JScrollPane(table);		
		table.setFillsViewportHeight(true);
		add(scrollPane, BorderLayout.CENTER);
		
	}//end initiateTable
	
	public void clearTable() {
		tablemodel.getDataVector().removeAllElements();
		tablemodel.setRowCount(0);
		tablemodel.fireTableDataChanged();
	}
	
	public void addRow(SingleSIFTResult result) {
		Vector<String> vector = new Vector<String>();
		vector.add(result.getValue(SIFTConstants.SR_CHR));
		vector.add(result.getValue(SIFTConstants.SR_COORD));
		vector.add(result.getValue(SIFTConstants.SR_REF));
		vector.add(result.getValue(SIFTConstants.SR_ALT));
		vector.add(result.getValue(SIFTConstants.SR_TRN));
		vector.add(result.getValue(SIFTConstants.SR_GID));

		vector.add(result.getValue(SIFTConstants.SR_AA1));
		vector.add(result.getValue(SIFTConstants.SR_AA2));
		vector.add(result.getValue(SIFTConstants.SR_APOS));
		vector.add(result.getValue(SIFTConstants.SR_SIFTSCORE));
		vector.add(result.getValue(SIFTConstants.SR_MEDIAN));
		vector.add(result.getValue(SIFTConstants.SR_NUMSEQ));
		vector.add(result.getValue(SIFTConstants.SR_ANNO));		
		vector.add(result.getValue(SIFTConstants.SR_PRED));
		tablemodel.addRow(vector);
		
		if (table.getRowCount() > 0) { 
			setVisible(true);
			tablemodel.fireTableDataChanged();
		}

	}


	@Override
	public void run() {
		initiateTable();
	}
	


	
	
	
	
	
	
}//end ResultsPanel class
