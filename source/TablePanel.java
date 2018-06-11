/*
 * TablePanel.java
 *
 * @author mlf
 * copied and modified from ModelParameters, as created by matthew on February 21, 2005, 11:22 AM
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
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.List;

public class TablePanel extends JPanel implements Serializable {    

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
  		}

  		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			if (value instanceof Boolean) { // Boolean
     				 flg = BOOLEAN;
      				return cellEditors[BOOLEAN].getTableCellEditorComponent(table, value, isSelected, row, column);
    			} else if (value instanceof String) { // String
      				flg = STRING;
      				return cellEditors[STRING].getTableCellEditorComponent(table, value, isSelected, row, column);
    			}
    			return null;
  		}

  		public Object getCellEditorValue() {
    		switch (flg) {
    			case BOOLEAN:
    			case STRING:
      				return cellEditors[flg].getCellEditorValue();
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
    			return cellEditors[flg].isCellEditable(anEvent);
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

    	 private class TableModel extends AbstractTableModel {
        
        	// Serialization explicit version
        	private static final long serialVersionUID = 1;
    
        	private Vector<Parameter> parameters = new Vector<Parameter>();

		private String [] columnNames;
		private Vector<Vector<Object>> vdata = new Vector<Vector<Object>>();
        
		// two column table; preload all cells via 2D vector
		TableModel(String [] cNames, Vector<Vector<Object>> idata) {
			columnNames = cNames;
			vdata = idata;
			if (debug_statements) System.out.println("importing into TableModel");
			Enumeration e = vdata.elements();
			while (e.hasMoreElements()) {
				if (debug_statements) System.out.println(e.nextElement());
			}
		}

		// one column table; no initial cell values
		TableModel(String [] cNames) {
			columnNames = cNames;
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

		//public void setValueAt(Object value, int row, int col) {
            	//	if ((columnNames.length > 1 && col > 0) || (columnNames.length < 2)) 
		//		data[row][col] = value;
		//}
        
        	public String getColumnName(int column) {
                	return columnNames[column];
        	}
        
        	public boolean isCellEditable(int row, int col) {
            		if (columnNames.length > 1 && col < 1) 
				return false;
			else 
				return true;
        	}
        
        	public boolean addNameValuePair( java.lang.String name, java.lang.String value ) {
            		Parameter p = new Parameter( name, value );
            		parameters.add( p );
            
            		if (debug_statements) System.out.println("Added <" + name + "," + value + "> to TableModel.");
            
            		// since only one row was added these numbers are the same
            		int start_new_rows_index = getRowCount();
            		int end_new_rows_index = start_new_rows_index;
            
            		// Cause the table view to update
            		fireTableRowsInserted( start_new_rows_index, end_new_rows_index );
            
            		return true;
        	}
        
        	public boolean deleteNameValuePair( int index ) {
            		if (debug_statements) System.out.println("Attempting to delete row " + index );
            
            		if ( index < 0 ) return false;
            
            		if ( index >= parameters.size() ) {
                		return false;
            		}
                
            		try {
                		parameters.remove( index );
            		}
            		catch ( ArrayIndexOutOfBoundsException aiobe ) {
                		return false;
            		}
            
            
            		return true;
        	}
        
        	public boolean deleteNameValuePair( java.lang.String name ) {
          
            		if (debug_statements) System.out.println("Deleting <" + name + "> from TableModel.");
            
            		int index = keyIndex( name );
            
            		if ( index == -1 ) return false;
            
            		return deleteNameValuePair( index );
        	}
        
        	public void setValueAt( Object value, int row, int column ) {
            		if ( column == 0 ) {
                		if ( keyExists( (String)value ) ) {
                    			gui.getEditorPanel().displayError("Error in Parameter","Parameter names must be unique" );
                    			return;
                		}
                
                		((Parameter)parameters.get(row)).setKey( (String)value );
            		}
            		else if ( column == 1 ) {
                		Float numeric_value = null;
                		try {
                    			numeric_value = new Float( (String)value );
                		}
                		catch( NumberFormatException exp ) {
                    			gui.getEditorPanel().displayError( "Error Setting the Parameter Value", value + " is not a valid number" );
                    		return;
                		}
                
                
                		((Parameter)parameters.get(row)).setValue( (String)value );
            			}
            		else {
               		 	gui.getEditorPanel().displayError("Error in Model Parameters","Attempt to set value at (" + row + "," + column + ") is out of bounds. Contact support at support@bionetgen.com");
            		}
        	}
        
        	private boolean keyExists( String key ) {
            		int index = keyIndex( key );
            
            		if ( index == -1) {
                		return false;
            		}
            
            		return true;
        	}
        
        	private int keyIndex( String search_key ) {
            		Iterator param_itr = parameters.iterator();
            		while ( param_itr.hasNext() ) {
                		Parameter param = (Parameter)param_itr.next();
                		String key = (param).getKey();
                		if ( search_key.equals( key ) ) {
                    			return parameters.indexOf( param );
                		}
            		}
            
            		return -1;
        	}
        
        	public Parameter getParameter( String key ) {
           		return getParameter( keyIndex( key ) );
        	}
        
        	public Parameter getParameter( int index ) {
            		if ( index >= parameters.size() ) return null;
            		return (Parameter)parameters.get( index );
        	}
        
        	public Vector<Parameter> getParameters() {
            		return parameters;
        	}
        
        	public void setParameters( Vector<Parameter> p) {
            		parameters = p;
        	}
        
        	public void initialize() {
            		parameters.removeAllElements();
        	}
        
        	public boolean addParameter(Parameter p) {
            		if ( keyExists(p.getKey() ) ) {
                		return false;
            		}
                
            		parameters.add(p);
            		return true;
        	}
        
        	public Vector<String> getParameterNames() {
            		Vector<String> parameter_names = new Vector();
            
            		Iterator param_itr = parameters.iterator();
            		while ( param_itr.hasNext() ) {
                		Parameter p = (Parameter)param_itr.next();
                		parameter_names.add( p.getKey() );
            		}
            
            		return parameter_names;
        	}
        
        	public boolean setValue(java.lang.String key, java.lang.String value) {
            		int index = keyIndex( key );
            		if ( index == -1 ) return false;
            		setValueAt( value, index, 1 );
            		return true;
        	}
        
	}
    
    	private class ItemRemover implements ActionListener {
    
    		public void actionPerformed(ActionEvent event) {
        		int[] indicies = table_view.getSelectedRows();
        
        		for ( int i = 0; i < indicies.length; i++ ) {
            			table_model.deleteNameValuePair( indicies[i] );
        		}
        
        	// We have to wait until all the deletions are complete before updating the view
            	int start_new_rows_index = indicies[0];
            	int end_new_rows_index = indicies[indicies.length-1];
            
            	// Cause the table view to update
            	table_model.fireTableRowsDeleted( start_new_rows_index, end_new_rows_index );
            
    		}
    	}
    
    	private class ItemAdder implements ActionListener {
    
    		public void actionPerformed(ActionEvent event) {
        		addParameter("K"+unique_param_index++ ,"0");
        
        		// Make sure the newly added row is visible to the user
        		int column = 0;
        		int row = table_model.getRowCount();
        		table_view.scrollRectToVisible(table_view.getCellRect(row,column,true));
    		}
  	}
    
    	private class DialogCloser extends WindowAdapter implements ActionListener {
        	public void actionPerformed(ActionEvent event) {
            		handleClose();
        	}
    
        	public void windowClosing(WindowEvent e) {
            		handleClose();
		}
    
        	private void handleClose() {
            	// Save cell edits in progress when window closes.
            		CellEditor current_editor = table_view.getCellEditor();
            
            		if ( current_editor != null ) {
                		current_editor.stopCellEditing();
            		}
            
            		dialog.setVisible( false );
            		dialog.dispose();

            		gui.refreshAll();
        	}
    	}
    
	// Serialization explicit version
	private static final long serialVersionUID = 1;
    
        transient protected boolean debug_statements = true;
    
	transient private JDialog dialog;
	transient private JTable table_view;
	transient private TableModel table_model;
	transient private GUI gui;
	transient private Model model;
	static int unique_param_index = 0;

	private class CellUpdater implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			int index = table_view.getSelectedRow();
			table_model.fireTableCellUpdated(index,1);		
		}
	}
   
	public TablePanel( GUI gui, Model model, String[] inputcNames, Vector<Vector<Object>> inputdata, int xdim, int ydim, String title) {

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

		table_model = new TableModel(inputcNames, inputdata);

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
   
	public TablePanel( GUI gui, Model model, String[] inputcNames, int xdim, int ydim, String title) {

		this.gui = gui;
		this.model = model;

		int txdim = xdim - 55;
		int tydim = ydim - 100;
		Dimension sizepanel = new Dimension(xdim,ydim);
		Dimension sizetable = new Dimension(txdim,tydim);

		JButton add_button = new JButton("Add Time");
		add_button.setFont(new Font("sanssserif",Font.BOLD,8));
		add_button.addActionListener(new ItemAdder());
		JButton delete_button = new JButton("Delete Time");
		delete_button.setFont(new Font("sanssserif",Font.BOLD,8));
		delete_button.addActionListener(new ItemRemover());
		JPanel button_panel = new JPanel();
		button_panel.add(add_button);
		button_panel.add(delete_button);

        	JPanel table_panel = new JPanel();
		table_panel.setBorder(BorderFactory.createTitledBorder(new EmptyBorder(10,10,0,0),title,2,2));
		table_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		table_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		table_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		table_panel.setPreferredSize (sizepanel);
		table_panel.setMaximumSize (sizepanel);
		table_panel.setMinimumSize (sizepanel);

		table_model = new TableModel(inputcNames);

		table_view = new JTable(table_model);
		table_view.setPreferredScrollableViewportSize(sizetable);
		table_view.setShowHorizontalLines(true);
		table_view.setShowVerticalLines(true);
		table_view.setGridColor(Color.black);
		table_view.getTableHeader().setResizingAllowed(false);
		table_view.getTableHeader().setReorderingAllowed(false);

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
        	table_panel.add(new JScrollPane(table_view),BorderLayout.CENTER);
		table_panel.add(button_panel,BorderLayout.CENTER);
            
        	this.add(table_panel,BorderLayout.WEST);
	}
   
	public boolean addParameter(java.lang.String name, java.lang.String value) {
        	table_model.addNameValuePair( name, value );
        	return true;
	}
    
	public boolean addParameter( Parameter p ) {
        	return table_model.addParameter( p );
	}
    
	public boolean deleteParameter(java.lang.String name ) {
        	table_model.deleteNameValuePair( name );
        	return true;
	}
    
	public Parameter getSelectedParameter() {
        	int index = table_view.getSelectedRow();
        	if ( index == -1 ) return null;
       		return (Parameter)table_model.getParameter(index);
	}
    
	public String getParameterValue(java.lang.String name) {
      		int index = table_model.keyIndex( name );
      		return (String)table_model.getValueAt( index, 1 );
	}
    
	public Vector<Parameter> getParameters() {
        	return table_model.getParameters();
	}
    
	public void setParameters( Vector<Parameter> p ) {
        	table_model.setParameters( p );
	}
    
	public String getValue( String key ) {
        	int index = table_model.keyIndex( key );
        	if ( index == -1 ) return null;
        	return (String)table_model.getValueAt( index, 1 );
	}
    
	public void setGUI( GUI gui ) {
        	this.gui = gui;
	}
    
	public void setModel( Model model ) {
        	this.model = model;
	}
    
	public void initialize() {
        	table_model.initialize();
	}
    
	public Vector<String> getParameterNames() {
        	return table_model.getParameterNames();
	}
    
	public boolean setValue(java.lang.String key, java.lang.String value) {
        	return table_model.setValue( key, value );
	}

	public Parameter getParameter(java.lang.String key) {
        	return table_model.getParameter(key);
	}
    
}
