/*
 * ModelParameters.java
 *
 * Created on November 1, 2005, 12:52 AM
 */

import java.beans.*;
import java.io.Serializable;
import java.awt.*; // Windowing
import javax.swing.*; // Swing tools
import java.awt.event.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;

/**
 * @author matthew
 */
public class ModelParameters extends Object implements Serializable 
{    
    private class ItemRemover implements ActionListener 
    {
    
    public void actionPerformed(ActionEvent event) 
    {
        int[] indicies = table_view.getSelectedRows();
        
       Vector<Parameter> data_list = table_model.getParameters();
       Vector<Parameter> delete_list = new Vector();

        String in_use_names = new String();
        boolean is_in_use = false;
        for ( int i = 0; i < indicies.length; i++ )
        {        
            Parameter p = data_list.get(indicies[i]);
           // Check the item is not in use
           Iterator<Species> species_itr = gui.getModel().getSpecies().iterator();
           while ( species_itr.hasNext() )
           {
            Species s = species_itr.next();
            if ( s.getConcentrationParameter() == p )
            {
                in_use_names += p.getKey() + " is used by Species " + s.getLabel() + " in the Species Palette\n";
                is_in_use = true;
            }
           }
           
           Iterator<ReactionRule> rule_itr = gui.getModel().getRules().iterator();
           while ( rule_itr.hasNext() )
           {
            ReactionRule r = rule_itr.next();
            if ( r.getForwardRateParameter() == p )
            {
                in_use_names += p.getKey() + " is used by Rule " + r.getLabel() + " in the Rule Palette\n";
                is_in_use = true;
            }
            else if ( r.isReversable() )
                {
                    if ( r.getReverseRateParameter() == p )
                    {
                        in_use_names += p.getKey() + " is used by Rule " + r.getLabel() + " in the Rule Palette";
                        is_in_use = true;
                    }
                }
            
           }
           
           // Check rules and species on the Drawing Board
           Iterator<Species> db_species_itr = gui.getEditorPanel().getAllSpecies().iterator();
           while ( db_species_itr.hasNext() )
           {
            Species s = db_species_itr.next();
            if ( s.getConcentrationParameter() == p )
            {
                in_use_names += p.getKey() + " is used by Species " + s.getLabel() + " on the Drawing Board\n";
                is_in_use = true;
            }
           }
           
           Iterator<ReactionRule> db_rule_itr = gui.getEditorPanel().getAllReactionRules().iterator();
           while ( db_rule_itr.hasNext() )
           {
            ReactionRule r = db_rule_itr.next();
            if ( r.getForwardRateParameter() == p )
            {
                in_use_names += p.getKey() + " is used by Rule " + r.getLabel() + " on the Drawing Board\n";
                is_in_use = true;
            }
            else if ( r.isReversable() )
                {
                    if ( r.getReverseRateParameter() == p )
                    {
                        in_use_names += p.getKey() + " is used by Rule " + r.getLabel() + " on the Drawing Board";
                        is_in_use = true;
                    }
                }
            
           }
           
           
            if ( !is_in_use )
            {
                delete_list.add( data_list.get(indicies[i]) );
            }
           
           is_in_use = false;
        }
        
        data_list.removeAll( delete_list );
        
        int start_new_rows_index = indicies[0];
        int end_new_rows_index = indicies[indicies.length-1];
        
        // Cause the table view to update
        table_model.fireTableRowsDeleted( start_new_rows_index, end_new_rows_index );  
    
        if ( in_use_names.length() != 0 )
        {
            gui.getEditorPanel().displayWarning("Not All Selected Parameters Were Deleted", "The following parameters were not deleted because they are in use:\n\n" + in_use_names );
        }
    }
    
    }
    
    private class ItemAdder implements ActionListener 
    {
    
    public void actionPerformed(ActionEvent event) 
    {
        addParameter("K"+unique_param_index++ ,"0");
        
        // Make sure the newly added row is visible to the user
        int column = 0;
        int row = table_model.getRowCount();
        table_view.scrollRectToVisible(table_view.getCellRect(row,column,true));
    }
  }
    
    private class DialogCloser extends WindowAdapter implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            handleClose();
        }
    
        public void windowClosing(WindowEvent e)
        {
            handleClose();
	}
    
        private void handleClose()
        {
            // Save cell edits in progress when window closes.
            CellEditor current_editor = table_view.getCellEditor();
            
            if ( current_editor != null )
            {
                current_editor.stopCellEditing();
            }
            
            dialog.setVisible( false );
            dialog.dispose();
        
            gui.setSaveNeeded( true );
            gui.refreshAll();
        }
    }
    
    class TableModel extends AbstractTableModel 
    {
        //private TreeMap map = new TreeMap();
        //Vector keys = new Vector();
        //Vector values = new Vector();
        
        // Serialization explicit version
        private static final long serialVersionUID = 1;
    
        private Vector<Parameter> parameters = new Vector<Parameter>();
        
        TableModel()
        {
        }
        
        public int getRowCount() 
        {
            //Vector keys = new Vector();
            //Vector values = new Vector();
            //convertToVectors( keys, values );
            
            return parameters.size();
            //return keys.size();
        }

        public int getColumnCount() 
        {
            return 2;
        }

        private void convertToVectors( Vector<String> keys, Vector<String> values )
        {
            /*
            Iterator keyValuePairs = map.entrySet().iterator();
            while ( keyValuePairs.hasNext() )
            {
                Map.Entry entry = (Map.Entry) keyValuePairs.next();
                Object key = entry.getKey();
                Object value = entry.getValue();
                
                keys.add( key );
                values.add( value );
            }
             */
        }
        
        public Object getValueAt(int row, int col) 
        {
            if ( col == 0 )
            {
                String key = ((Parameter)parameters.get( row )).getKey();
                if (debug_statements) System.out.println("getValueAt: returning key " + key );
                 
                return key;
            }
            else if ( col == 1 )
            {
                 String value = ((Parameter)parameters.get( row )).getValue();
                 if (debug_statements) System.out.println("getValueAt: returning value " + value );
                 return value;
            }
            else
            {
                gui.getEditorPanel().displayError("Error in Model Parameters","Requested cell ("+row + "," + col + ") is out of bounds. Contact support at support@bionetgen.com");
            }
            
            /*
            if ( col == 0 ) return keys.get( row );
            else if ( col == 1 ) return values.get( row );
            else
            {
                gui.getEditorPanel().displayError("Error in Model Parameters","Requested cell ("+row + "," + col + ") is out of bounds. Contact support at support@bionetgen.com");
            }
             */
            
            return null;
        }
        
        public String getColumnName( int column)
        {
            if ( column == 0 ) return "Name";
            else if ( column == 1 ) return "Value";
            else
            {
                gui.getEditorPanel().displayError("Error in Model Parameters","Requested column name (" + column + ") is out of bounds. Contact support at support@bionetgen.com");
            }
            
            return null;
        }
        
        public boolean addNameValuePair( java.lang.String name, java.lang.String value ) 
        {
            Parameter p = new Parameter( name, value );
            parameters.add( p );
            //keys.add( name );
            //values.add( value );
            
            if (debug_statements) System.out.println("Added <" + name + "," + value + "> to TableModel.");
            
            // since only one row was added these numbers are the same
            int start_new_rows_index = getRowCount();
            int end_new_rows_index = start_new_rows_index;
            
            // Cause the table view to update
            fireTableRowsInserted( start_new_rows_index, end_new_rows_index );
            
            return true;
        }
        
        public boolean deleteNameValuePair( int index ) 
        {
            if (debug_statements) System.out.println("Attempting to delete row " + index );
            
            if ( index < 0 ) return false;
            
            if ( index >= parameters.size() )
            {
                return false;
            }
                
            try
            {
                parameters.remove( index );
            }
            catch ( ArrayIndexOutOfBoundsException aiobe )
            {
                return false;
            }
            
            
            return true;
        }
        
        public boolean deleteNameValuePair( java.lang.String name ) 
        {
          
            if (debug_statements) System.out.println("Deleting <" + name + "> from TableModel.");
            
            int index = keyIndex( name );
            
            if ( index == -1 ) return false;
            
            return deleteNameValuePair( index );
        }
        
        public boolean isCellEditable( int row, int col )
        {
            return true;
        }
        
        public void setValueAt( Object value, int row, int column )
        {
            if ( column == 0 )
            {
                if ( keyExists( (String)value ) )
                {
                    gui.getEditorPanel().displayError("Error in Parameter","Parameter names must be unique" );
                    return;
                }
                
                ((Parameter)parameters.get(row)).setKey( (String)value );
            }
            else if ( column == 1 )
            {
                Float numeric_value = null;
                try 
                {
                    numeric_value = new Float( (String)value );
                }
                catch( NumberFormatException exp ) 
                {
                    gui.getEditorPanel().displayError( "Error Setting the Parameter Value", value + " is not a valid number" );
                    return;
                }
                
                
                ((Parameter)parameters.get(row)).setValue( (String)value );
            }
            else
            {
                gui.getEditorPanel().displayError("Error in Model Parameters","Attempt to set value at (" + row + "," + column + ") is out of bounds. Contact support at support@bionetgen.com");
            }
             
        }
        
        private boolean keyExists( String key )
        {
            int index = keyIndex( key );
            
            if ( index == -1)
            {
                return false;
            }
            
            return true;
        }
        
        private int keyIndex( String search_key )
        {
            Iterator param_itr = parameters.iterator();
            while ( param_itr.hasNext() )
            {
                Parameter param = (Parameter)param_itr.next();
                String key = (param).getKey();
                if ( search_key.equals( key ) )
                {
                    return parameters.indexOf( param );
                }
            }
            
            return -1;
        }
        
        //public Vector getKeys() 
        //{
        //    return keys;
        //}
        
        public Parameter getParameter( String key )
        {
           int index =  keyIndex( key );
           
           if ( index == -1 )
           {
               return null;
           }
           
           return getParameter( index );
        }
        
        public Parameter getParameter( int index )
        {
            if ( index >= parameters.size() ) return null;
            return (Parameter)parameters.get( index );
        }
        
        public Vector<Parameter> getParameters() 
        {
            return parameters;
        }
        
        public void setParameters( Vector<Parameter> p) 
        {
            parameters = p;
        }
        
        public void initialize() 
        {
            parameters.removeAllElements();
        }
        
        public boolean addParameter(Parameter p) 
        {
            if ( keyExists(p.getKey() ) ) 
            {
                return false;
            }
                
            parameters.add(p);
            return true;
        }
        
        public Vector<String> getParameterNames() 
        {
            Vector<String> parameter_names = new Vector();
            
            Iterator param_itr = parameters.iterator();
            while ( param_itr.hasNext() )
            {
                Parameter p = (Parameter)param_itr.next();
                parameter_names.add( p.getKey() );
            }
            
            return parameter_names;
        }
        
        public boolean setValue(java.lang.String key, java.lang.String value) 
        {
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
    private Model model;
    static int unique_param_index = 0;
   
    public ModelParameters( GUI gui, Model model ) 
    {
        this.gui = gui;
        this.model = model;
        
        /*
        for ( int i = 0; i < 10; i++ )
        {
            Integer integer = new Integer( i );
            addParameter( "Value"+i, integer.toString() );
        }
         */
    }
      
    public void displayDialog()
    {
            JFrame owner = gui.getMainFrame();
            dialog = new JDialog( owner, true );
            dialog.setTitle( "Model Parameters" );
            dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            
            DialogCloser dc = new DialogCloser();
            
            dialog.addWindowListener( dc );
            
            JButton done_button = new JButton("Done");
            done_button.addActionListener( dc );
            
            JButton add_button = new JButton("Add Parameter");
            add_button.addActionListener( new ItemAdder() );
            
            JButton delete_button = new JButton("Delete Parameter(s)");
            delete_button.addActionListener( new ItemRemover() );
            
            JPanel button_panel = new JPanel();
            JPanel table_panel = new JPanel();
            
            button_panel.add( done_button );
            button_panel.add( add_button );
            button_panel.add( delete_button );
            
            //String[] headings = {"Name","Value"};
            Vector<String> headings = new Vector();
            headings.add( "Name" );
            headings.add( "Value" );
           
            Container content = dialog.getContentPane();
            
            table_view = new JTable(table_model);
            table_panel.add(new JScrollPane(table_view));
            
            content.add( table_panel, BorderLayout.CENTER );
            content.add( button_panel, BorderLayout.SOUTH );
            
            dialog.setLocation( 200, 100 );  
            dialog.setSize( 200, 200 );  
            
            dialog.pack();
            
            dialog.setVisible(true);
    }
    
    public boolean addParameter(java.lang.String name, java.lang.String value) 
    {
        table_model.addNameValuePair( name, value );
        return true;
    }
    
    public boolean addParameter( Parameter p ) 
    {
        return table_model.addParameter( p );
    }
    
    public boolean deleteParameter(java.lang.String name ) 
    {
        table_model.deleteNameValuePair( name );
        return true;
    }
    
    public Parameter getSelectedParameter() 
    {
        int index = table_view.getSelectedRow();
        if ( index == -1 ) return null;
        return (Parameter)table_model.getParameter(index);
    }
    
    public String getParameterValue(java.lang.String name) 
    {
      int index = table_model.keyIndex( name );
      return (String)table_model.getValueAt( index, 1 );
    }
    
    //public Vector getNames() 
    //{
    //    return table_model.getKeys();
    //}
    
    public Vector<Parameter> getParameters()
    {
        return table_model.getParameters();
    }
    
    public void setParameters( Vector<Parameter> p )
    {
        table_model.setParameters( p );
    }
    
    public String getValue( String key ) 
    {
        int index = table_model.keyIndex( key );
        if ( index == -1 ) return null;
        return (String)table_model.getValueAt( index, 1 );
    }
    
    public void setGUI( GUI gui )
    {
        this.gui = gui;
    }
    
    public void setModel( Model model )
    {
        this.model = model;
    }
    
    public void initialize() 
    {
        table_model.initialize();
    }
    
    public Vector<String> getParameterNames() 
    {
        return table_model.getParameterNames();
    }
    
    public boolean setValue(java.lang.String key, java.lang.String value) 
    {
        return table_model.setValue( key, value );
        
    }
    
    public Parameter getParameter(java.lang.String key) 
    {
        return table_model.getParameter(key);
    }
    
}
