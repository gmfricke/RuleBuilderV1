/*
 * EquilibrationParameters.java
 *
 * Created on February 21, 2005, 11:22 AM
 */

import java.beans.*;
import java.io.Serializable;
import java.awt.*; // Windowing
import java.awt.event.*;
import java.util.*;
import javax.swing.*; // Swing tools
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * @author matthew, mlf
 *    copied and modified from ModelParameters class 
 */
public class EquilibrationPanel extends JPanel implements Serializable {    

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
    
    	class TableModel extends AbstractTableModel {
        
        	// Serialization explicit version
        	private static final long serialVersionUID = 1;
    
        	private Vector<Parameter> parameters = new Vector<Parameter>();
        
        	TableModel() { }
        
        	public int getRowCount() {
            		return parameters.size();
        	}

        	public int getColumnCount() {
            		return 2;
        	}

        	public Object getValueAt(int row, int col) {
            		if ( col == 0 ) {
                		String key = ((Parameter)parameters.get( row )).getKey();
                		if (debug_statements) System.out.println("getValueAt: returning key " + key );
                 
                		return key;
            		}
            		else if ( col == 1 ) {
                 		String value = ((Parameter)parameters.get( row )).getValue();
                 		if (debug_statements) System.out.println("getValueAt: returning value " + value );
                 		return value;
            		}
            		else {
                		gui.getEditorPanel().displayError("Error in Model Parameters","Requested cell ("+row + "," + col + ") is out of bounds. Contact support at support@bionetgen.com");
            		}
            
            		return null;
        	}
        
        	public String getColumnName( int column) {
            		if ( column == 0 ) return "Molecule";
            		else if ( column == 1 ) return "Initial Concentration";
            		else {
                		gui.getEditorPanel().displayError("Error in Model Parameters","Requested column name (" + column + ") is out of bounds. Contact support at support@bionetgen.com");
            		}
            
            		return null;
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
        
        	public boolean isCellEditable( int row, int col ) {
            		return true;
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
    
	// Serialization explicit version
	private static final long serialVersionUID = 1;
    
        transient protected boolean debug_statements = true;
        
	transient private JDialog dialog;
	transient private JTable table_view;
	transient private TableModel table_model = new TableModel();
	transient private GUI gui;
	transient private Model model;
	static int unique_param_index = 0;
   
	public EquilibrationPanel( GUI gui, Model model, Vector headings, Vector rowdata ) {
		this.gui = gui;
		this.model = model;

        	JPanel table_panel = new JPanel();
		int xdim = 260;
		int ydim = 98;
		Dimension size = new Dimension(xdim,ydim);
		table_panel.setPreferredSize (size);
		table_panel.setMaximumSize (size);
		table_panel.setMinimumSize (size);
            
        	table_view = new JTable(rowdata,headings);
		table_view.setPreferredScrollableViewportSize(size);
		table_view.setShowHorizontalLines(true);
		table_view.setShowVerticalLines(true);
		table_view.setGridColor(Color.black);
		table_view.getTableHeader().setResizingAllowed(false);
		table_view.getTableHeader().setReorderingAllowed(false);
		table_view.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		int total_columns = table_view.getColumnCount();
		int width = (int)xdim/total_columns;
		int j = 0;
		for (; j < total_columns;) {
    			TableColumn col = table_view.getColumnModel().getColumn(j);
    			col.setPreferredWidth(width);
			++j;
		}
        	table_panel.add(new JScrollPane(table_view));
            
        	this.add(table_panel, BorderLayout.EAST);
	}
   
	public EquilibrationPanel( GUI gui, Vector headings ) {
		this.gui = gui;

        	JPanel table_panel = new JPanel();
		int xdim = 260;
		//int ydim = 70;
		int ydim = 98;
		Dimension size = new Dimension(xdim,ydim);
		table_panel.setPreferredSize (size);
		table_panel.setMaximumSize (size);
		table_panel.setMinimumSize (size);
            
		Vector<String> data = new Vector();

        	table_view = new JTable(data,headings);
		table_view.setPreferredScrollableViewportSize(size);
		table_view.setShowHorizontalLines(true);
		table_view.setShowVerticalLines(true);
		table_view.setGridColor(Color.black);
		table_view.getTableHeader().setResizingAllowed(false);
		table_view.getTableHeader().setReorderingAllowed(false);
		table_view.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		int total_columns = table_view.getColumnCount();
		int width = (int)xdim/total_columns;
		int j = 0;
		for (; j < total_columns;) {
    			TableColumn col = table_view.getColumnModel().getColumn(j);
    			col.setPreferredWidth(width);
			++j;
		}
        	table_panel.add(new JScrollPane(table_view));
            
        	this.add(table_panel, BorderLayout.EAST);
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
