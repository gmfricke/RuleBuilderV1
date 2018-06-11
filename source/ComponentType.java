/*
 * ComponentType.java
 *
 * Created on May 4, 2005, 6:28 PM
 */

// These objects hold information about the allowed and default states for components
// in the same container with the same label
// ComponentTypes exist in and are managed by MoleculeTypes. Only one ComponentType
// exists in a MoleculeType for each component label though there may be multiple components
// with that label.

import java.beans.*;
import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.*; // For file IO in objectWrite and Read
import java.io.Serializable; // DropHandler needs to be Serializable
import java.awt.geom.*; // for shapes (circle)
import java.math.*;

import javax.swing.event.*; // For swing events
import javax.swing.*; // For graphical interface tools
import javax.swing.border.*; // For window borders

/**
 * @author matthew
 */
public class ComponentType extends Object implements Serializable 
{
            
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    transient protected boolean debug_statements = true;
    
    private String label;
    private String default_state = new String();
    
    private Vector<String> allowed_states = new Vector<String>();
        
    transient private JTextField textfield;
    transient private JLabel default_state_label;
    transient private JDialog allowed_states_dialog;
    transient private JList allowed_list;
    transient private DefaultListModel model;
    private boolean stateless = false;
    
    transient private WidgetPanel containing_panel;
    
    private class ItemRemover implements ActionListener 
    {
    
    public void actionPerformed(ActionEvent event) 
    {
     // Get the current selection
                        int selection = allowed_list.getSelectedIndex();
                        if( selection >= 0 )
                        {
                                // Add this item to the list and refresh
                                model.removeElementAt( selection );
                                allowed_list.revalidate();
                                allowed_list.repaint();

                                // As a nice touch, select the next item
                                if( selection >= model.size() )
                                {
                                        selection = model.size() - 1;
                                }
                                allowed_list.setSelectedIndex( selection );
                        }
    }
    
    }
    
    private class ItemAdder implements ActionListener 
    {
    
    public void actionPerformed(ActionEvent event) 
    {
        
      String new_state = textfield.getText();
      Object[] current_states = model.toArray();
      
      
      if ( new_state == null )
      {
            getContainingPanel().displayError("Reserved State Name", "\"?\" is reserved for BioNetGen's internal use.");
            return;
      }
      
      if ( new_state.equals("*") )
      {
            getContainingPanel().displayError("Explicit Specification of Wildcard", "\"*\" is reserved by BioNetGen to indicate any state in the allowed state list.\n" +
            "The wildcard state is implicitly available and does not need to be added here.");
            return;
      }
      
      if ( new_state.matches("^\\s+$") || new_state.equals("") )
      {
            getContainingPanel().displayError("Blank State", "Allowed States must contain at least one character.");
            return;
      }
    
        // replace multiple spaces with a single space
      new_state = new_state.replaceAll( "\\s+", " " );
      // remove trailing and leading white space
      new_state = new_state.trim();
    
      
      for ( int i = 0; i < current_states.length; i++ )
      {
          if ( new_state.equals( (String)current_states[i] ) )
          {
                getContainingPanel().displayError("Duplicate State", "Allowed states must be unique.");
                return;
          }
      }
       
     
      model.addElement(new_state);
      allowed_states_dialog.getContentPane().invalidate();
      allowed_states_dialog.getContentPane().validate();
     
      if ( allowed_list.getSelectedValue() == null )
      {
          allowed_list.setSelectedIndex(0);
      }
      
      allowed_list.ensureIndexIsVisible( model.getSize() );
     }
  }
    
    private class AllowedStatesDialogCloser extends WindowAdapter implements ActionListener 
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
        
	if ( model.size() == 0 )
        {
            /*
            if ( !getContainingPanel().displayQuestion("No Allowed States", "Are you sure you wish to make this component \"stateless?\"") )
            {
                return;
            }
            else
            {
             */
                setStateless( true );
            //}
        }
        else
        {
            setStateless( false );
        }
        
        allowed_states_dialog.setVisible( false );
        allowed_states_dialog.dispose();
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
    
    
    private class ValueReporter implements ListSelectionListener 
    {
        public void valueChanged(ListSelectionEvent event) {
        if (!event.getValueIsAdjusting()) 
        {    
            if ( allowed_list.getSelectedValue() != null )
            {
                default_state_label.setText(allowed_list.getSelectedValue().toString());
            }
        }
    }
  }
        
    
    private PropertyChangeSupport propertySupport;
    
    private String user_selected_default = null;
    
    public ComponentType() {
        propertySupport = new PropertyChangeSupport(this);
    }
    
    public String getDefaultState()
    {
        if ( isStateless() ) return null;
        return default_state;
    }
   
    public void setDefaultState( String state )
    {
        default_state = state;
    }

    public Vector<String> getAllowedStates()
    {
        return allowed_states;
    }
    
    public void setAllowedStates( Vector<String> allowed_states )
    {
        this.allowed_states = allowed_states;
    }
    
    public void displayAllowedStates() 
    {        
            JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            allowed_states_dialog = new JDialog( owner, true );
            allowed_states_dialog.setTitle( getLabel() );
            allowed_states_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            allowed_states_dialog.addWindowListener( new AllowedStatesDialogCloser() );
            
            Container content = allowed_states_dialog.getContentPane();
            
            model = new DefaultListModel();
            Object[] entries = getAllowedStates().toArray();
                
            for(int i=0; i<entries.length; i++)
            {
                model.addElement(entries[i]);
            }
            
            allowed_list = new JList(model);
            
            allowed_list.setVisibleRowCount(5);
            allowed_list.setFixedCellWidth(150);
            allowed_list.setFixedCellHeight(20);
            allowed_list.addListSelectionListener(new ValueReporter());
            //Font displayFont = new Font("Serif", Font.BOLD, 18);
            //allowed_list.setFont(displayFont);
            JScrollPane listPane = new JScrollPane(allowed_list);
            JPanel listPanel = new JPanel();
            JPanel default_state_panel = new JPanel();
            //listPanel.setBackground(Color.white);
            Border listPanelBorder = BorderFactory.createTitledBorder("Allowed States for " + getLabel() );

            
            JButton addButton = new JButton("Add");
            //addButton.setFont(displayFont);
            addButton.addActionListener(new ItemAdder());
            JPanel buttonPanel = new JPanel();
            //buttonPanel.setBackground(Color.white);
            Border buttonPanelBorder = BorderFactory.createTitledBorder("Add Allowed State");
            buttonPanel.setBorder(buttonPanelBorder);
            textfield = new JTextField(15);
            default_state_label = new JLabel(getDefaultState());
            buttonPanel.add( textfield );
            buttonPanel.add(addButton);
            JButton delButton = new JButton("Remove");
            delButton.addActionListener( new ItemRemover() );
            buttonPanel.add( delButton );
            default_state_panel.setBorder( BorderFactory.createEmptyBorder() );
            default_state_panel.add( default_state_label, BorderLayout.LINE_END );
            
            JPanel list_default_wrapper_panel = new JPanel(); 
            listPanel.setBorder(listPanelBorder);
            listPanel.setLayout(new BorderLayout());
            listPanel.add(listPane, BorderLayout.NORTH);
            listPanel.add( new JLabel("Default: "), BorderLayout.WEST );
            listPanel.add( default_state_label, BorderLayout.CENTER );
  
            
            JButton doneButton = new JButton("Done");
            doneButton.addActionListener( new AllowedStatesDialogCloser() );
            buttonPanel.add( doneButton );
            
            content.add(listPanel, BorderLayout.CENTER);
            content.add(buttonPanel, BorderLayout.SOUTH);
            
            allowed_states_dialog.pack();
            
            
            int dialog_height = allowed_states_dialog.getHeight();
            int dialog_width = allowed_states_dialog.getWidth();
            
            allowed_states_dialog.setLocation( 200, 100 );  
            
            allowed_states_dialog.setVisible(true);
            
            // allowed_states_dialog is model so code will wait for input there to 
            // finish before doing other things.
            
            
            // initialize the current allowed states array
            getAllowedStates().removeAllElements();
            Object[] allowed_array = model.toArray();
            for ( int i = 0; i < allowed_array.length; i++ )
            {   
                if (debug_statements) System.out.println("Adding " + (String)allowed_array[i] );
               
                addAllowedState(  (String)allowed_array[i] );
            }
            
            setDefaultState( default_state_label.getText() );
            
            // Validate and update the Species - they cannot have wildcard
            // states so set all the components to their defaults if they
            // have the invalid state "*"
            Iterator<BioComponent> bcomp_itr = this.getContainingPanel().getTheGUI().getSpeciesPalette().getAllComponents().iterator();
            while ( bcomp_itr.hasNext() )
            {
                BioComponent bcomp = bcomp_itr.next();
                if ( bcomp.getState().equals("*") )
                {
                    bcomp.setState( bcomp.getDefaultState() );
                }
            }
            
            // now do the same for the model
            Iterator<Species> species_itr = this.getContainingPanel().getTheGUI().getModel().getSpecies().iterator();
            while ( species_itr.hasNext() )
            {
                Species s = species_itr.next();
                bcomp_itr = s.getComponents().iterator();
                while ( bcomp_itr.hasNext() )
                {
                    BioComponent bcomp = bcomp_itr.next();
                    if ( bcomp.getState() == null )
                    {
                        // Do nothing
                    }
                    else if ( bcomp.getState().equals("*") )
                    {
                        bcomp.setState( bcomp.getDefaultState() );
                    }
                }
            }
            
            this.getContainingPanel().getTheGUI().refreshAll();
    }
    
    public boolean addAllowedState(String allowed_state) 
        {
            Iterator i = allowed_states.iterator();
            while ( i.hasNext() )
            {
                if ( allowed_state.equals( i.next() ) )
                {
                    return false;
                }
            }
            
            if ( allowed_states.indexOf( allowed_states) != -1 )
            {
                return false;
            }
            
            allowed_states.add( allowed_state );
            return true;
        }
        
        public boolean removeAllowedState(String allowed_state) 
        {
            if ( allowed_states.indexOf( allowed_states) != -1 )
            {
                allowed_states.remove( allowed_state );
                return true;
            }
            return false;
        }
        
        public void emptyAllowedStates() 
        {
            allowed_states.removeAllElements();
        }
        
    public WidgetPanel getContainingPanel()
    {
        return containing_panel;
    }
    
    public void setContainingPanel( WidgetPanel panel )
    {
        containing_panel = panel;
    }
    
    public String getLabel()
    {
        return label;
    }
    
    
    public void setLabel( String label )
    {
        this.label = label;
    }
    
    public boolean isStateless() 
    {
        return getAllowedStates().isEmpty();
    }
    
    public void setStateless(boolean stateless) 
    {
        this.stateless = stateless;
    }
 
    public boolean isValidState(String state) 
   {
       if (debug_statements) System.out.println("\nisValidState is considering " + state);
       
       if ( state == null )
       {
           if ( getAllowedStates().isEmpty() )
           {
               return true;
           }
           else
           {
               return false;
           }
       }
       
       if ( state.equals("*") )
       {
           return true;
       }
       
       
       Iterator state_itr = getAllowedStates().iterator();
       while( state_itr.hasNext() )
       {
           String current_state = (String)state_itr.next();
           if ( current_state.equals( state ) )
           {
               if (debug_statements) System.out.println( state + " is a valid state.");
               return true;
           }
       }
       
       if (debug_statements) System.out.println( state + " is not a valid state.");
       return false;
   }
    
}
