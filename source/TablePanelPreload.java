/*
 * TablePanelPreload.java
 *
 * @author mlf
 *
 */

import java.beans.*;
import java.io.Serializable;
import java.awt.*; // Windowing
import java.awt.event.*;
import java.util.*;
import javax.swing.*; // Swing tools
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.*;
import javax.swing.JComponent.*;
import javax.swing.border.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.List;

        
class TablePanelPreload extends JPanel implements Serializable {    

	class MultiEditor implements TableCellEditor {

		private final static int BOOLEAN = 1;
  		private final static int STRING = 2;
  		private final static int NUM_EDITOR = 3;
  		DefaultCellEditor[] cellEditors;
  		int flg;

  		public MultiEditor() {
    			cellEditors = new DefaultCellEditor[NUM_EDITOR];
    			JCheckBox checkBox = new JCheckBox();
    			checkBox.setHorizontalAlignment(JLabel.CENTER);
    			cellEditors[BOOLEAN] = new DefaultCellEditor(checkBox);
    			JTextField textField = new JTextField();
    			cellEditors[STRING] = new DefaultCellEditor(textField);
    			flg = NUM_EDITOR; // nobody
    			//flg = STRING; // nobody
  		}

  		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value instanceof Boolean) { // Boolean
     				 flg = BOOLEAN;
      				return cellEditors[BOOLEAN].getTableCellEditorComponent(table, value, isSelected, row, column);
    			} else if (value instanceof String || value instanceof Integer || value instanceof Number) { // String
      				flg = STRING;
      				return cellEditors[STRING].getTableCellEditorComponent(table, value, isSelected, row, column);
    			}
    			return null;
  		}

		// method now sporting an ugly hack - we need a better way to interrogate for editor type
  		public Object getCellEditorValue() {
    			switch (flg) {
    				case STRING: 
					return "STRING" + cellEditors[flg].getCellEditorValue();
    				case BOOLEAN:
					return "BOOLEN" + cellEditors[flg].getCellEditorValue(); 
      				//return cellEditors[flg].getCellEditorValue();
    			default:
      				return null;
    			}
  		}

  		public Component getComponent() {
    			return cellEditors[flg].getComponent();
  		}

		public boolean stopCellEditing() {
    			return cellEditors[flg].stopCellEditing();
  		}

  		public void cancelCellEditing() {
    			cellEditors[flg].cancelCellEditing();
  		}

  		public boolean isCellEditable(EventObject anEvent) {
    			//return cellEditors[flg].isCellEditable(anEvent);  // in conflict with other isCellEditable method, must provide this method though
			return true;
  		}

  		public boolean shouldSelectCell(EventObject anEvent) {
    			return cellEditors[flg].shouldSelectCell(anEvent);
  		}

  		public void addCellEditorListener(CellEditorListener l) {
    			cellEditors[flg].addCellEditorListener(l);
  		}

  		public void removeCellEditorListener(CellEditorListener l) {
    			cellEditors[flg].removeCellEditorListener(l);
  		}

  		public void setClickCountToStart(int n) {
    			cellEditors[flg].setClickCountToStart(n);
  		}

  		public int getClickCountToStart() {
    			return cellEditors[flg].getClickCountToStart();
  		}
 	}

 	class TableModelPreload extends AbstractTableModel {
        
       		// Serialization explicit version
       		private static final long serialVersionUID = 1;
    
		private String [] columnNames;
		private Vector<Vector<Object>> vdata = new Vector<Vector<Object>>();
        
		// two column table; preload all cells via 2D vector
		TableModelPreload(String [] cNames, Vector<Vector<Object>> idata) {
			columnNames = cNames;
			vdata = idata;
			if (debug_statements) System.out.println("importing into TableModelPreload");
			Enumeration e = vdata.elements();
			while (e.hasMoreElements()) {
				if (debug_statements) System.out.println(e.nextElement());
			}
		}

       		public int getRowCount() {
         		return vdata.size();
        	}

        	public int getColumnCount() {
         		return columnNames.length;
        	}

        	public Object getValueAt(int row, int col) {
			return vdata.elementAt(row).elementAt(col);
       		}

		public void setValueAt(Object value, int row, int col) {
           		if (col > 0) {
  				Integer rrow = new Integer(row);
  				Integer ccol = new Integer(col);

				// determine what sort of input is expected to come into this cell
				// and pull out the flag and the value (terrible hack)
				TableCellEditor tce = table_view.getCellEditor(row,col);
				Object cev = tce.getCellEditorValue();
				String cevstring = cev.toString();
				int cevstringlength = cevstring.length();
				String valuestring = cevstring.substring(6,cevstringlength);
				String flagstring = cevstring.substring(0,6);
				int flagstringlength = flagstring.length();

				// debug
				if (debug_statements) System.out.println("editor for this cell is " + cevstring);
				if (debug_statements) System.out.println("valuestring is " + valuestring);
				if (debug_statements) System.out.println("flagstring is " + flagstring);

				if (flagstring.equals("STRING")) {
					if (debug_statements) System.out.println("string input flag " + flagstring);
					if (debug_statements) System.out.println("string input flag value " + valuestring);
					//Float numeric_value = null;
					Integer numeric_value;
					if (!(valuestring.equals("unlimited"))) {
			        		try {   
                                			//numeric_value = new Float(valuestring);
                                			numeric_value = Integer.valueOf(valuestring);
							value = numeric_value;
                        			}       
                        			catch( NumberFormatException exp ) { 
                                			gui.getEditorPanel().displayError( "Error Setting the Count Value", valuestring + " is not a valid integer" );
                                			value = "unlimited";
                        			} 
					}
					else {
						value = "unlimited";
					}
  					if (debug_statements) System.out.println("TableModelPreload setValueAt: row is " + (String)rrow.toString() + " col is " + (String)ccol.toString() + " val is " + valuestring);
				}
				else {
					if (debug_statements) System.out.println("boolean input flag " + flagstring);
					if (debug_statements) System.out.println("boolean input flag value " + valuestring);
					if (valuestring.equals("true")) {
						value = true;
					}
					else {
						value = false;
					}
				}

  				Vector<Object> temp = vdata.elementAt(row);
  				temp.removeElementAt(col);
  				temp.insertElementAt(value,col);
  				vdata.removeElementAt(row);
  				vdata.insertElementAt(temp,row);
  				newtabledata.removeElementAt(row);
  				newtabledata.insertElementAt(temp,row);
  			}
  			else
  				if (debug_statements) System.out.println("incorrect row, col indexing into current vector");
  		}
        
        	public String getColumnName(int column) {
               		return columnNames[column];
        	}
        
        	public boolean isCellEditable(int row, int col) {
          		if (col < 1) 
				return false;
			else 
				return true;
       		}
	}

	class MultiRenderer extends DefaultTableCellRenderer {

  		JCheckBox checkBox = new JCheckBox();

  		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    			if (value instanceof Boolean) { // Boolean
      				checkBox.setSelected(((Boolean) value).booleanValue());
      				checkBox.setHorizontalAlignment(JLabel.CENTER);
				checkBox.addActionListener(new CellUpdater());
      				return checkBox;
			}
    			String str = (value == null) ? "" : value.toString();
    			return super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
  		}
	}

	public class CellUpdater implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			int index = table_view.getSelectedRow();
			table_model.fireTableCellUpdated(index,1);		
		}
	}
    
	// Serialization explicit version
	private static final long serialVersionUID = 1;

        transient protected boolean debug_statements = true;
        
        public Vector<Vector<Object>> newtabledata = new Vector<Vector<Object>>();
    
	transient private JDialog dialog;
	transient private JTable table_view;
	transient private TableModelPreload table_model;
	transient private GUI gui;
	transient private Model model;
   
	public TablePanelPreload(Boolean b, GUI gui, SimulationConfig sc, String[] inputcNames, Vector<Vector<Object>> inputdata, int xdim, int ydim, String title) {

		this.gui = gui;
		this.model = model;

		int txdim = xdim - 40;
		int tydim = ydim - 50;
		Dimension sizepanel = new Dimension(xdim,ydim);
		Dimension sizetable = new Dimension(txdim,tydim);

        	JPanel table_panel = new JPanel();
		table_panel.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0),title,2,2));
		table_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		table_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		table_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		table_panel.setPreferredSize (sizepanel);
		table_panel.setMaximumSize (sizepanel);
		table_panel.setMinimumSize (sizepanel);

		// let's look and see if we already have some data to populate the table with
		Vector<Vector<Object>> equilbooleans = sc.getEquilibrationBooleanValues();
		if (equilbooleans.size() == 0) {
			if (debug_statements) System.out.println("no preexisting table data available");
			newtabledata = inputdata;
			sc.setEquilibrationBooleanValues(newtabledata);
		}
		else {
                	Iterator<Vector<Object>> equilbooleansrowiter = equilbooleans.iterator();
                	Vector<Object> equilbooleansthisrow;
                	Iterator<Object> equilbooleanscoliter;
                	while (equilbooleansrowiter.hasNext()) {
                        	equilbooleansthisrow = equilbooleansrowiter.next();
                        	equilbooleanscoliter = equilbooleansthisrow.iterator();
                        	while (equilbooleanscoliter.hasNext()) {
                                	Object current = equilbooleanscoliter.next();
                                	String currentstring = current.toString();
                                	if (debug_statements) System.out.println("TablePanelPreload ctor equilbooleans: " + currentstring);
                        	}  
			}
			newtabledata = equilbooleans;     
                }    

		table_model = new TableModelPreload(inputcNames, newtabledata);

		table_view = new JTable(table_model);
		table_view.setPreferredScrollableViewportSize(sizetable);
		table_view.setShowHorizontalLines(true);
		table_view.setShowVerticalLines(true);
		table_view.setGridColor(Color.black);
		table_view.getTableHeader().setResizingAllowed(false);
		table_view.getTableHeader().setReorderingAllowed(false);
		table_view.setCellSelectionEnabled(true);

		table_view.getColumn(inputcNames[1]).setCellRenderer(new MultiRenderer());
		table_view.getColumn(inputcNames[1]).setCellEditor(new MultiEditor());

		int total_columns = table_view.getColumnCount();
		int width = (int)txdim/total_columns;
		int j = 0;
		for (; j < total_columns;) {
    			TableColumn col = table_view.getColumnModel().getColumn(j);
    			col.setMinWidth(width);
    			col.setMaxWidth(width);
    			col.setPreferredWidth(width);
			++j;
		}
        	table_panel.add(new JScrollPane(table_view),BorderLayout.WEST);
            
        	this.add(table_panel,BorderLayout.WEST);
	}
   
	public TablePanelPreload(String s, GUI gui, SimulationConfig sc, String[] inputcNames, Vector<Vector<Object>> inputdata, int xdim, int ydim, String title) {

		this.gui = gui;
		this.model = model;

		int txdim = xdim - 40;
		int tydim = ydim - 50;
		Dimension sizepanel = new Dimension(xdim,ydim);
		Dimension sizetable = new Dimension(txdim,tydim);

        	JPanel table_panel = new JPanel();
		table_panel.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(0,0,0,0),title,2,2));
		table_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		table_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		table_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		table_panel.setPreferredSize (sizepanel);
		table_panel.setMaximumSize (sizepanel);
		table_panel.setMinimumSize (sizepanel);

		// let's look and see if we already have some data to populate the table with
		Vector<Vector<Object>> maxstoichlimits = sc.getNetworkStoichLimitValues();
		if (maxstoichlimits.size() == 0) {
			if (debug_statements) System.out.println("no preexisting table data available");
			newtabledata = inputdata;
		}
		else {
                	Iterator<Vector<Object>> maxstoichrowiter = maxstoichlimits.iterator();
                	Vector<Object> maxstoichthisrow;
                	Iterator<Object> maxstoichcoliter;
                	while (maxstoichrowiter.hasNext()) {
                        	maxstoichthisrow = maxstoichrowiter.next();
                        	maxstoichcoliter = maxstoichthisrow.iterator();
                        	while (maxstoichcoliter.hasNext()) {
                                	Object current = maxstoichcoliter.next();
                                	String currentstring = current.toString();
                                	if (debug_statements) System.out.println("TablePanelPreload ctor maxstoich: " + currentstring);
                        	}  
			}
			newtabledata = maxstoichlimits;     
                }    

		table_model = new TableModelPreload(inputcNames, newtabledata);

		table_view = new JTable(table_model);
		table_view.setPreferredScrollableViewportSize(sizetable);
		table_view.setShowHorizontalLines(true);
		table_view.setShowVerticalLines(true);
		table_view.setGridColor(Color.black);
		table_view.getTableHeader().setResizingAllowed(false);
		table_view.getTableHeader().setReorderingAllowed(false);
		table_view.setCellSelectionEnabled(true);

		table_view.getColumn(inputcNames[1]).setCellRenderer(new MultiRenderer());
		table_view.getColumn(inputcNames[1]).setCellEditor(new MultiEditor());

		int total_columns = table_view.getColumnCount();
		int width = (int)txdim/total_columns;
		int j = 0;
		for (; j < total_columns;) {
    			TableColumn col = table_view.getColumnModel().getColumn(j);
    			col.setMinWidth(width);
    			col.setMaxWidth(width);
    			col.setPreferredWidth(width);
			++j;
		}
        	table_panel.add(new JScrollPane(table_view),BorderLayout.WEST);
            
        	this.add(table_panel,BorderLayout.WEST);
	}

	public TablePanelPreload() {
	}

	public void TableDisable() {
		table_view.setGridColor(Color.red);
		table_view.setCellSelectionEnabled(false);
                if (debug_statements) System.out.println("current tabledisable font is " + table_view.getFont().getName());
		table_view.setFont(new Font("Lucida Grande", Font.ITALIC, 12));
	}

	public TableModelPreload getTableModelPreload() {
		return table_model;
	}

	public JTable getJTable() {
		return table_view;
	}

	public Vector<Vector<Object>> getNewTableData() {
		return newtabledata;
	}
   
	public void setGUI( GUI gui ) {
        	this.gui = gui;
	}
    
	public void setModel( Model model ) {
        	this.model = model;
	}
}
