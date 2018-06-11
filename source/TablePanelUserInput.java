/*
 * TablePanelUserInput.java
 *
 * @author mlf
 *
 */

import java.beans.*;
import java.awt.*; 		// For graphical windowing tools
import java.awt.event.*; 	// For mouse interactions
import javax.swing.*;	 	// For graphical interface tools
import java.net.*; 		//For URL image loading from Jar files
import java.util.*; 		//For vector data structure

import java.awt.dnd.*; 		// For drag 'n drop between windows
import java.io.*; 		// For file IO in objectWrite and Read
import java.io.Serializable; 	// DropHandler needs to be Serializable

import javax.swing.event.*; 	// For swing events
import javax.swing.*; 		// For graphical interface tools
import javax.swing.border.*; 	// For window borders

import java.io.Serializable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.*;
import javax.swing.JComponent.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import java.util.Arrays;
import java.util.List;

import java.lang.Object;

class TablePanelUserInput extends JPanel implements Serializable {    

	class TableModelUserInput extends AbstractTableModel {
        
       		// Serialization explicit version
       		private static final long serialVersionUID = 1;

		private String [] columnNames;

		// one column table; no initial cell values
		TableModelUserInput(String [] cNames) {
			columnNames = cNames;
		}
        
       		public int getRowCount() {
         		return parameters.size();
        	}

        	public int getColumnCount() {
         		return columnNames.length;
        	}

        	public Object getValueAt(int row, int col) {
			if (debug_statements) System.out.println("getValueAt: ");
			if (debug_statements) System.out.println(row);
			if (debug_statements) System.out.println(col);
			if (debug_statements) System.out.println("parameters size is");
			if (debug_statements) System.out.println(parameters.size());
                	String key = ((Parameter)parameters.get( row )).getKey();
                	if (debug_statements) System.out.println("getValueAt: returning key " + key );

                	return key;
        	}

        	public String getColumnName(int column) {
               		return columnNames[column];
        	}
        
        	public boolean isCellEditable(int row, int col) {
			return false;
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
            
            		if ( index < 0 ) {
         			if (debug_statements) System.out.println("I failed with index < 0 in Attempting to delete row " + index );
				return false;
			}
            
            		if ( index >= parameters.size() ) {
         			if (debug_statements) System.out.println("I failed with index >= parameters.size in Attempting to delete row " + index );
               			return false;
            		}
                
            		try {
         			if (debug_statements) System.out.println("Attempting to delete row " + index );
               			parameters.remove( index );
            		}
            		catch ( ArrayIndexOutOfBoundsException aiobe ) {
         			if (debug_statements) System.out.println("I failed with array index out of bounds  in Attempting to delete row " + index );
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

		public boolean deleteNameValuePair (java.lang.Object name) {
			if (debug_statements) System.out.println("Deleting <" + (String)name + "> from TableModel via object kill");
			return parameters.removeElement(name);
		}
        
        	public void setValueAt( Object value, int row, int column ) {

               		if ( keyExists( (String)value ) ) {
                 		gui.getEditorPanel().displayError("Error in Parameter","Parameter names must be unique" );
                    		return;
                	}

			if (column > 0) {
               	 		gui.getEditorPanel().displayError("Error in Model Parameters","Attempt to set value at (" + row + "," + column + ") is out of bounds. Contact support at support@bionetgen.com");
				return;
            		}

               		Float numeric_value = null;
               		try {
                  		numeric_value = new Float( (String)value );
                	}
                	catch( NumberFormatException exp ) {
                    		gui.getEditorPanel().displayError( "Error Setting the Parameter Value", value + " is not a valid number" );
                    		return;
                	}
                	((Parameter)parameters.get(row)).setValue( (String)value );
			return;
        	}
        
        	public boolean keyExists( String key ) {
           		int index = keyIndex( key );
            
            		if ( index == -1) {
               			return false;
            		}
            
            		return true;
        	}
        
        	public int keyIndex( String search_key ) {
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

    	private class GetSampleTimeDialogCloser extends WindowAdapter implements ActionListener {
    
    		public void actionPerformed(ActionEvent event) {
        		handleClose();
    		}
    
    		public void windowClosing(WindowEvent e) {
			handleClose();
		}
    
    		private void handleClose() {
        
        		get_sample_time_dialog.setVisible(false);
        		get_sample_time_dialog.dispose();
    		}
  	}
    
    	public void displayGetSampleTime() {
            
		Dimension size0 = new Dimension(150,40);
		Dimension size1 = new Dimension(230,70);
        
  	        JFrame owner = gui.getMainFrame();
          	get_sample_time_dialog = new JDialog(owner, true);
            	get_sample_time_dialog.setTitle("Enter next time");
            	get_sample_time_dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE );
            	get_sample_time_dialog.addWindowListener(new GetSampleTimeDialogCloser());
		get_sample_time_dialog.setMinimumSize(size1);
		get_sample_time_dialog.setMaximumSize(size1);
		get_sample_time_dialog.setPreferredSize(size1);
            
            	Container content = get_sample_time_dialog.getContentPane();

		time_entry_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		time_entry_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		time_entry_textfield.setEditable(true);
		time_entry_textfield.setFocusable(true);
		time_entry_textfield.setForeground(Color.black);

            	JButton addButton = new JButton("Add");
            	addButton.addActionListener(new GetSampleTimeDialogCloser());

            	JPanel time_entry_Panel = new JPanel();
		time_entry_Panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		time_entry_Panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		time_entry_Panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		time_entry_Panel.setMinimumSize(size0);
		time_entry_Panel.setMaximumSize(size0);
		time_entry_Panel.setPreferredSize(size0);
            	time_entry_Panel.add(time_entry_textfield, BorderLayout.WEST);
            	time_entry_Panel.add(addButton, BorderLayout.EAST);
            
            	content.add(time_entry_Panel, BorderLayout.CENTER);
            
            	get_sample_time_dialog.pack();
            	get_sample_time_dialog.setLocation(410,380);  
            	get_sample_time_dialog.setVisible(true);
            
            	this.gui.refreshAll();
    	}
    
    	private class ItemAdder implements ActionListener {
    
    		public void actionPerformed(ActionEvent event) {

			displayGetSampleTime();

			Object userinput = time_entry_textfield.getText();
               		Float numeric_value = null;
               		try {
                  		numeric_value = new Float((String)userinput);
                	}
                	catch( NumberFormatException exp ) {
                    		gui.getEditorPanel().displayError( "Error Setting the Time Value", (String)userinput + " is not a valid number" );
                    		return;
                	}
			if (numeric_value < 0) {
				gui.getEditorPanel().displayError( "Error Setting the Time Value", "values must be positive" );
				return;
			}
			String lastvaluestring = lastvalue.toString();
			Float lastvaluefloat = new Float(lastvaluestring);
			if (numeric_value < lastvaluefloat) {
				gui.getEditorPanel().displayError( "Error Setting the Time Value", "values must be monotonically increasing" );
				return;
			}
			lastvalue = userinput;
			if (debug_statements) System.out.println("item adder is going to add " + (String)userinput);
        		addParameter((String)userinput,"0");
			newsampletimes.addElement((String)userinput);

			// verify changes to newsampletimes
			Enumeration k = newsampletimes.elements();
			while (k.hasMoreElements()) {
				if (debug_statements) System.out.println("newsampletimes elt " + (String)k.nextElement());
			}
        
        		// Make sure the newly added row is visible to the user
        		int column = 0;
        		int row = table_model.getRowCount();
        		table_view.scrollRectToVisible(table_view.getCellRect(row,column,true));
    		}
  	}

    	private class ItemRemover implements ActionListener {
    
    		public void actionPerformed(ActionEvent event) {
        		int[] indicies = table_view.getSelectedRows();
			Vector<String> killkeys = new Vector<String>();
        
        		for ( int i = 0; i < indicies.length; i++ ) {
				int myindex = indicies[i];
				String currentkey = table_model.getParameter(myindex).getKey();
				killkeys.add(currentkey);
				if (debug_statements) System.out.println("item remover is working on index ");
				if (debug_statements) System.out.println(myindex);
				if (debug_statements) System.out.println("item name is " + currentkey);
        		}

			Vector<Parameter> revisedparameters = new Vector<Parameter>();
			revisedparameters.addAll(table_model.getParameters());
			Enumeration e = killkeys.elements();
			Enumeration f = revisedparameters.elements();

			// what we start with
			while (f.hasMoreElements()) {
				Parameter tempobject = (Parameter)f.nextElement();
				if (debug_statements) System.out.println("I have initial revised key " + tempobject.getKey());
			}

			// what we delete
			while (e.hasMoreElements()) {
				String tempstring = (String)e.nextElement();
				Parameter p = table_model.getParameter(tempstring);
				if (debug_statements) System.out.println("deleting key " + tempstring + " in parameters vector and usersampletimes vector");
				newsampletimes.removeElement(tempstring);
				revisedparameters.removeElement((Object)p);
			}

			// verify changes to revisedparameters
			Enumeration g = revisedparameters.elements();
			while (g.hasMoreElements()) {
				Parameter tempobject = (Parameter)g.nextElement();
				if (debug_statements) System.out.println("I have modified revised key " + tempobject.getKey());
			}

			// verify changes to newsampletimes
			Enumeration k = newsampletimes.elements();
			while (k.hasMoreElements()) {
				if (debug_statements) System.out.println("newsampletimes elt " + (String)k.nextElement());
			}

			// create a new table_model parameters vector with the changes
			table_model.initialize();
			Enumeration h = revisedparameters.elements();
			while (h.hasMoreElements()) {
				Parameter tempparameter = (Parameter)h.nextElement();
				String tempstring = tempparameter.getKey();
				if (debug_statements) System.out.println("adding obj " + tempstring);
          			Parameter p = new Parameter(tempstring,"0");
        			table_model.addParameter(p);
			}
        
        		// We have to wait until all the deletions are complete before updating the view
            		int start_new_rows_index = 0;
            		int end_new_rows_index = table_model.getRowCount();
            
            		// Cause the table view to update
            		table_model.fireTableRowsDeleted( start_new_rows_index, end_new_rows_index );
            
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
        
        private JTextField time_entry_textfield = new JTextField("0",10);
       	private Vector<Parameter> parameters = new Vector<Parameter>();
	static int unique_param_index = 0;
	private Object lastvalue = 0;
	public Vector<String> newsampletimes = new Vector<String>();
    
    	transient private JDialog get_sample_time_dialog;
    	transient private WidgetPanel containing_panel;
	transient private JDialog dialog;
	transient private JTable table_view;
	transient private TableModelUserInput table_model;
	transient private GUI gui;
	transient private Model model;
	transient private State state;
	transient private SimulationConfig sc;
	transient private Vector<String> usersampletimes;
   
	public TablePanelUserInput(GUI gui, Model model, State state, SimulationConfig sc, String[] inputcNames, int xdim, int ydim, String title) {

		this.gui = gui;
		this.model = model;

		int txdim = xdim - 45;
		int tydim = ydim - 95;
		//int txdim = xdim - 55;
		//int tydim = ydim - 100;
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

		table_model = new TableModelUserInput(inputcNames);

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

		// let's look and see if we already have some data to populate the table with
		Vector<String> usersampletimes = sc.getTimecourseSampletimesvalues();
		if (usersampletimes.size() == 0) {
			if (debug_statements) System.out.println("no preexisting sample times data available");
		}
		else {
			Iterator <String> usersampletimesiter = usersampletimes.iterator();
			while (usersampletimesiter.hasNext()) {
				String currenttime = usersampletimesiter.next();
				if (debug_statements) System.out.println("found sample time " + currenttime);
				table_model.addNameValuePair(currenttime, "0");
				newsampletimes.addElement(currenttime);
			}
		}
            
        	this.add(table_panel,BorderLayout.WEST);
	}

	public JTable getJTable() {
		return table_view;
	}

	public Vector<String> getNewSampleTimes() {
		return newsampletimes;
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
                if (debug_statements) System.out.println("getParameterValue is calling getValueAt");
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
                if (debug_statements) System.out.println("getValue is calling getValueAt");
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
