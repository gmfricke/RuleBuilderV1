/*
 * Species.java
 *
 * Created on June 7, 2005, 3:28 PM
 */

import java.beans.*;
import java.io.Serializable;

import java.util.*; //For vector data structure
import java.awt.*; // For JComponent
import javax.swing.*; // For graphical interface tools
import java.io.*;
import java.awt.event.*;
import javax.swing.border.*;

class Species extends BioGraph
{
        
    private class ParameterChooserListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            JComboBox cb = (JComboBox)e.getSource();
            String key = (String)cb.getSelectedItem();
            String value = model_parameters.getValue( key );
            
            // If the key does not exist then do not replace the value
            if ( value != null )
            {
                concentration_textfield.setText( value );
            }
             
        }
    }
    
    private class ConcentrationDialogDone extends WindowAdapter implements ActionListener 
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
        
            String key = (String)parameter_chooser.getSelectedItem();
            String value = concentration_textfield.getText();
            
            if ( label_textfield.getText().length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Species Label",
                        "The species label field cannot be left blank\n");
                        return;
            }
            
            if ( key == null )
            {
                getContainingPanel().displayError("Error Setting the Concentration",
                        "The parameter must have a name\n");
                        return;
            }
            
            if ( key.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Concentration",
                        "The parameter must have a name\n");
                        return;
            }
            
            if ( value.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Concentration",
                        "The parameter value field was left blank\n");
                        return;
            }
            
            Float rate;
            try {
                    rate = new Float( value );
                    
                    if ( rate.floatValue() < 0.0 ) 
                    {
                        getContainingPanel().displayError("Error Setting the Initial Concentration",
                        "The initial concentration must be a positive number;\n" +
                        value + " is not in that range.");
                        return;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate",value + " is not a valid number" );
                    return;
                }
            
            if ( !model_parameters.setValue( key, value ) ) // false if key not found
            {
                model_parameters.addParameter( key, value );
            }
            
            setConcentrationParameter( model_parameters.getParameter(key) ); //rate_value.floatValue() );
            
        String index_str = index_textfield.getText();
        Integer index = new Integer( index_str );
        
         WidgetPanel wp = getContainingPanel();
         
        if ( wp instanceof SpeciesPalette )
        {
         SpeciesPalette sp = (SpeciesPalette)wp;
        
        if ( index < 0 )
        {
            sp.displayError( "Index Error", "The index " + index + " less than 0" );
            return;
        }
        else if ( index > sp.getAllSpecies().size() )
        {
            sp.getAllSpecies().remove(Species.this);
            sp.getAllSpecies().add( Species.this );
            sp.compressDisplay();
        }
        else
        {
            sp.getAllSpecies().remove(Species.this);
            sp.getAllSpecies().insertElementAt( Species.this, index.intValue() );
            sp.compressDisplay();
        }
        
        }
        
        concentration_dialog.setVisible( false );
        concentration_dialog.dispose();
        
        setLabel( label_textfield.getText() );
        
        
        
        user_canceled = false;
        
        getContainingPanel().getTheGUI().refreshAll();
    }
  }
    
  private class ConcentrationDialogCancel extends WindowAdapter implements ActionListener 
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
            
        concentration_dialog.setVisible( false );
        concentration_dialog.dispose();
        
        user_canceled = true;
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
    
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    private float concentration;
    private String concentration_name = "";
    private Parameter concentration_parameter = new Parameter("","");
    
    transient protected JComboBox parameter_chooser;
    transient protected Vector<Parameter> parameters;
    transient protected ModelParameters model_parameters;
        
    transient protected JTextField concentration_textfield;
    transient protected JTextField label_textfield;
    transient protected JTextField index_textfield;
    transient protected JDialog concentration_dialog;

    private boolean derived = false;
    
    private boolean user_canceled = false;   
    
    //Species(){}
    
    Species(String label, int x, int y, WidgetPanel containing_panel)
    {
        this.containing_panel = containing_panel;
        
        int s_red = 0;
        int us_red = 0;
        int s_blue = 255;
        int us_blue = 0;
        int s_green = 0;
        int us_green = 0;
        int s_alpha = 150;
        int us_alpha = 150;
        
        Color sel_color = new Color( s_red, s_green, s_blue, s_alpha );
        Color unsel_color = new Color( us_red, us_green, us_blue, us_alpha );
        
        setLabel( label );
        //setSelectedColor( sel_color );
        //setUnselectedColor( unsel_color );
    }
    
    
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Add this Derived Species to the Model") )
        {
            setDerived(false);
            
        }
        else if ( action.getActionCommand().equals("Disband") )
	{
            /*
            if ( getContainingPanel().displayQuestion("Disband Species","Disbanding this BioGraph will" +
            " replace it with its constituent containers, edges, and components. " +
            "If this is a species the concentration data will be lost.") )
            {
             */
                // Edges must be added to the panel after the biograph is
                // removed because removing the biograph removes all edges
                // it contains from the panel
                disband();
            //}
            
        }
        else if ( action.getActionCommand().equals("Auto-Layout") )
	{
            layout();
        }
        else if ( action.getActionCommand().equals("Properties") )
	{
            setPropertiesFromUser();
            
            // Get concentration from user
            /*
                Float initial_conc_value = null;
            boolean initial_conc_set = false;
            
            while ( initial_conc_set == false )
            {
            String initial_conc = (String)getContainingPanel().displayInputQuestion( "New Species", "Enter the initial concentration for this species.");
            
            if ( initial_conc == null )
            {
                return;
            }
            
            try 
            {
                initial_conc_value = new Float( initial_conc );
                initial_conc_set = true;
                
                if ( initial_conc_value.floatValue() < 0.0 )
                {
                    getContainingPanel().displayError("Error Setting Intitial Concentration", 
                    "The initial concentration must be greater than 0.0;\n" +
                    initial_conc_value + " is not in that range.");
                    initial_conc_set = false;
                }
                
            }
            catch( NumberFormatException exp )
            {
                getContainingPanel().displayError( "Error Setting Intitial Concentration",initial_conc + " is not a valid number" );
                initial_conc_set = false;
            }
            }
                
            setConcentration( initial_conc_value.floatValue() );
        */
       
            
        }
        else if ( action.getActionCommand().equals("Add to Species List") )
        {
            Species copy = null;
            try
            {
                 copy = (Species)WidgetCloner.clone( this );
            }
            catch ( Exception e )
            {
                getContainingPanel().displayError("Internal Error while Cloning Species","The exception message was " + e.getMessage() + "\n" +
                "Contact support at support@bionetgen.com");
            }
            
            getContainingPanel().getTheGUI().getSpeciesPalette().addSpecies( copy );
        }
        
         
        else
        {
            getContainingPanel().displayError("Internal Error","Unknown action command \"" + action.getActionCommand() + "\" in Species::actionPerformed(). Contact support at support@bionetgen.com." );
        }
    }
    
    // Legacy
    public void setConcentration(String concentration) 
    {
        setConcentration( concentration, false );
    }
    
    public void setConcentration(String concentration, boolean peer) 
    {
        this.concentration_parameter.setValue( concentration );
        
        if ( !peer )
        {
            Iterator<Widget> w_itr = getPeers().iterator();
            while ( w_itr.hasNext() )
            {
                Species s = (Species)w_itr.next();
                s.setConcentration( concentration, true );
            }
        }
    }
    
    
    public void setConcentrationParameter(Parameter param  ) 
    {
        setConcentrationParameter( param, false );
    }
    
    public void setConcentrationParameter(Parameter param, boolean peer ) 
    {
        this.concentration_parameter = param;
        
        if ( !peer )
        {
            Iterator<Widget> w_itr = getPeers().iterator();
            while ( w_itr.hasNext() )
            {
                Species s = (Species)w_itr.next();
                s.setConcentrationParameter( param, true );
            }
        }
    }
    
    // Legacy
    public String getConcentration() 
    {
        return concentration_parameter.getValue();
    }
    
    
    public void setConcentrationName(String concentration_name ) 
    {
        setConcentrationName( concentration_name, false );
    }
    
    public void setConcentrationName(String concentration_name, boolean peer ) 
    {
        this.concentration_parameter.setKey( concentration_name );
        
        if ( !peer )
        {
            Iterator<Widget> w_itr = getPeers().iterator();
            while ( w_itr.hasNext() )
            {
                Species s = (Species)w_itr.next();
                s.setConcentrationName( concentration_name, true );
            }
        }
    }
    
    public String getConcentrationName() 
    {
        return getConcentrationParameter().getKey();
    }
    
    public String getConcentrationValue() 
    {
        if ( concentration_parameter == null )
        {
            return "";
        }
        return concentration_parameter.getValue();
    }
    
    public Parameter getConcentrationParameter() 
    {
        if ( concentration_parameter == null ) return new Parameter("","");
        return concentration_parameter;
    }
    
    
    /**
     *
     * @param c
     * @param g
     */
    public void display(Component c, Graphics2D g2d) 
    {
        
        if ( !isVisible() ) return;
        
        // Display the containers first, then selected container,
        // then components, then operators and finally selected
        // widgets and the selection box
        
        if (debug_statements) System.out.println("Displaying Species in " + getContainingPanel().getClass().getName() );
        
        //Color current = g2d.getColor();
        //g2d.setColor( Color.BLACK );
        
        // Concentration Name will be null if an old version of Species was deserialized
        //if ( getConcentrationName() != null )
        //{
        //    g2d.drawString( getConcentrationName()+" = "+getConcentrationValue(), getX(), getY() );
        //}
        //else
        //{
            // Legacy
        //    g2d.drawString( new Float( concentration ).toString(), getX(), getY() );
        //}
        
        
        //g2d.setColor( current );
        
        if ( derived )
        {
            Stroke current_stroke = g2d.getStroke();
        
	g2d.setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	//g2d.drawRect( x-5, y-5, width+10, height+10 );
        
        g2d.setColor( c.getBackground() );
        
        super.display( c, g2d );
        
        g2d.setStroke( current_stroke );
        }
        else
        {
            super.display( c, g2d );
        }
    }
    
    public void displayPopupMenu(int mouse_x, int mouse_y ) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
               //JMenuItem unlock_menu_item = new JMenuItem( "Unlock" );
	//JMenuItem lock_menu_item = new JMenuItem( "Lock" );
        JMenuItem disband_menu_item = new JMenuItem( "Disband" );
	JMenuItem add_menu_item = new JMenuItem( "Add to Species List" );
        JMenuItem prop_menu_item = new JMenuItem( "Properties" );
        JMenuItem del_menu_item = new JMenuItem( "Delete" );
        
        //unlock_menu_item.addActionListener( this );
        //lock_menu_item.addActionListener( this );
        disband_menu_item.addActionListener( this );
        add_menu_item.addActionListener( this );
        prop_menu_item.addActionListener( this );
        del_menu_item.addActionListener( getContainingPanel() );
        
        // Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
        popup.addSeparator();
        
	popup.addSeparator();
        
        JMenuItem layout_menu_item = new JMenuItem( "Auto-Layout" );
        layout_menu_item.addActionListener( this );
        popup.add( layout_menu_item );
          
        
        /*
        if ( isLocked() )
        {
            popup.add(unlock_menu_item);
        }
        else
        {
            popup.add(lock_menu_item);
        }
	*/
         
        
        
        popup.add( add_menu_item );
        popup.addSeparator();
        popup.add( disband_menu_item );
        popup.add( del_menu_item );
        popup.add( prop_menu_item );
        popup.show( containing_panel, mouse_x, mouse_y );
    }
    
    public boolean validate() 
    {
        Vector<BioContainer> molecule_types = getContainingPanel().getTheGUI().getMoleculePalette().getMoleculeTypes();
    
        
        Iterator mol_itr = getContainers().iterator(); 
        while ( mol_itr.hasNext() )
        {
            BioContainer mol = (BioContainer)mol_itr.next();
       
            BioContainer match = getContainingPanel().getTheGUI().getModel().getMoleculeType( mol );
            
            if ( match == null )
            {
                getContainingPanel().displayError("Species Validation Error", "\"" + mol.getLabel() + "\" could not be matched with any existing Molecule Types");
                return false;
            }
        }
        
        return true;
    }
    
    public boolean setPropertiesFromUser() 
    {
        JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            concentration_dialog = new JDialog( owner, true );
            concentration_dialog.setLocation( 200, 100 );  
            concentration_dialog.setSize( 250, 200 );  
            concentration_dialog.setTitle( "Species Properties" );
            concentration_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            concentration_dialog.addWindowListener( new ConcentrationDialogCancel() );
            
            Container content = concentration_dialog.getContentPane();
            JPanel label_panel = new JPanel();
            JPanel concentration_panel = new JPanel();
            JPanel button_panel = new JPanel();
            JPanel names_panel = new JPanel();
            JPanel values_panel = new JPanel();
            
            label_panel.setLayout(new BorderLayout());
            concentration_panel.setLayout(new BorderLayout());
            
            names_panel.setLayout(new BorderLayout());
            values_panel.setLayout(new BorderLayout());
            
            label_textfield = new JTextField(10);
	    String existinglabel = getLabel();
	    System.out.println("Existing label on this species is " + existinglabel);

	    //if (existinglabel.startsWith("BioGraph")) {
	    if (! (getContainingPanel() instanceof SpeciesPalette)) {
	    	long nextspeciesid = getContainingPanel().getTheGUI().getModel().getIDSpeciesGeneratorNext();
            	String numstring = new Long(nextspeciesid).toString();
            	label_textfield.setText( "Species" + numstring);
	    }
	    else {
            	label_textfield.setText( existinglabel );
	    }

            concentration_textfield = new JTextField(10);
            model_parameters = getContainingPanel().getTheGUI().getModelParameters();
            Vector<Parameter> parameters = model_parameters.getParameters();
            
            Vector<String> parameter_names = model_parameters.getParameterNames();
            
            parameter_chooser = new JComboBox( parameter_names );
            parameter_chooser.setEditable( true );
            parameter_chooser.addActionListener( new ParameterChooserListener() );
            
            if ( getConcentrationParameter() != null )
            {
                String default_selection = getConcentrationParameter().getKey();
                parameter_chooser.setSelectedItem( default_selection ); 
            }
            
            Integer index = getContainingPanel().getAllSpecies().indexOf(this);
            index_textfield = new JTextField(4);
            index_textfield.setText( index.toString() );
            
            JButton done = new JButton("Done");
            JButton cancel = new JButton("Cancel");
            done.addActionListener( new ConcentrationDialogDone() );
            cancel.addActionListener( new ConcentrationDialogCancel() );
            
            names_panel.add( new JLabel("Parameter Name: "),BorderLayout.NORTH );
            values_panel.add( parameter_chooser, BorderLayout.NORTH );
           
             
            names_panel.add( new JLabel("Concentration: "), BorderLayout.CENTER );
            values_panel.add( concentration_textfield, BorderLayout.CENTER );
            
            JPanel north_panel = new JPanel();
            JPanel index_panel = new JPanel();
            north_panel.setLayout(new BorderLayout());
            index_panel.setLayout(new BorderLayout());
            
            if ( getContainingPanel() instanceof SpeciesPalette )
            {
                index_panel.add(new JLabel("Index: "), BorderLayout.WEST );
                index_panel.add( index_textfield, BorderLayout.CENTER );
                index_panel.add(new JLabel(" "), BorderLayout.EAST);
            }
            
            label_panel.add( new JLabel("Label: "), BorderLayout.WEST );
            label_panel.add( label_textfield, BorderLayout.CENTER );
            
            north_panel.add(label_panel, BorderLayout.NORTH);
            north_panel.add(index_panel, BorderLayout.CENTER);
            
            button_panel.add( done, BorderLayout.WEST );
            button_panel.add( cancel, BorderLayout.CENTER );
            
            Border label_eb = BorderFactory.createEtchedBorder();
            Border concentration_eb = BorderFactory.createEtchedBorder();
            
            Border titled_label_eb = BorderFactory.createTitledBorder( label_eb, "Species Name" );
            Border titled_concentration_eb = BorderFactory.createTitledBorder( concentration_eb, "Initial Concentration" );
            concentration_panel.setLayout( new BorderLayout() );
            label_panel.setBorder( titled_label_eb );
            concentration_panel.setBorder( titled_concentration_eb );
            concentration_panel.add( names_panel, BorderLayout.WEST );
            concentration_panel.add( values_panel, BorderLayout.CENTER );
            

            

            
            content.add(north_panel, BorderLayout.NORTH);
            content.add(concentration_panel, BorderLayout.CENTER);
            content.add( button_panel, BorderLayout.SOUTH );
            
            
           // concentration_dialog.pack();
            
            
           //concentration_dialog.setResizable(false);
            concentration_dialog.pack();
           concentration_dialog.setVisible(true);
            concentration_dialog.setResizable(false);
           
        
        /*
            Float rate_value = null;
            boolean rate_set = false;
            
            ModelParameters params = getContainingPanel().getTheGUI().getModelParameters();
            params.displayDialog();
            Parameter param = params.getSelectedParameter();
            if ( param == null )
            {
                getContainingPanel().displayError("Error Setting Rate", "No parameter was selected from the parameter table.");
                return;
            }
            
          */
        
            /*
            while ( rate_set == false ) {
                String rate = (String)getContainingPanel().displayInputQuestion( "Reaction Rate", "Enter the forward reaction rate.");
                
                if ( rate == null ) {
                    return;
                }
                
                try {
                    rate_value = new Float( rate );
                    rate_set = true;
                    
                    if ( rate_value.floatValue() < 0.0 ) {
                        getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate must be positive;\n" +
                        rate + " is not in that range.");
                        rate_set = false;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate",rate + " is not a valid number" );
                    rate_set = false;
                }
            }
            */

            
            getContainingPanel().repaint();
            
            return !user_canceled; // Return false if the user cancelled, user_canceled flag set by the ConcentrationDialogCanceled/Done member classes 
    }
    
    /*
    public boolean setConcentrationFromUser() 
    {
            ModelParameters params = getContainingPanel().getTheGUI().getModelParameters();
            params.displayDialog();
            Parameter param = params.getSelectedParameter();
            if ( param == null )
            {
                getContainingPanel().displayError("Error Setting Rate", "No parameter was selected from the parameter table.");
                return false;
            }
        
            setConcentrationParameter( param );
            
        /*
        WidgetPanel wp = getContainingPanel();
        
        // Get concentration from user
            Float initial_conc_value = null;
            boolean initial_conc_set = false;
            
            while ( initial_conc_set == false ) {
                String initial_conc = (String)wp.displayInputQuestion( "New Species", "Enter the initial concentration for this species.");
                
                if ( initial_conc == null ) {
                    return false;
                }
                
                try {
                    initial_conc_value = new Float( initial_conc );
                    initial_conc_set = true;
                    
                    if ( initial_conc_value.floatValue() < 0.0 ) {
                        wp.displayError("Error Setting Intitial Concentration",
                        "The initial concentration must be greater or equal to 0.0;\n" +
                        initial_conc_value + " is not in that range.");
                        initial_conc_set = false;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    wp.displayError( "Error Setting Intitial Concentration",initial_conc + " is not a valid number" );
                    initial_conc_set = false;
                }
            }
          
         
            concentration = initial_conc_value.floatValue();
         */   
         
//         return true;
//    }    


    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
    
        setIndex(0);
        
        // Convert legacy concentration to concentration parameter
        if ( concentration_parameter == null )
        {
            concentration_parameter = new Parameter( "c"+getID(), new Float( concentration ).toString() );
        }
        
        label.setFont(new Font("Ariel", Font.ITALIC, 14));
        label.setOn();
    }

    public void setDerived(boolean derived) 
    {
        if ( derived == true )
        {
                // for derived species remove their concentration from the parameter list
                // really need to refactor this is starting to turn into a Rube Goldberg Machine
                getContainingPanel().getTheGUI().getModelParameters().deleteParameter( getConcentrationName() );
        }
        else
        {
            getContainingPanel().repaint();
            setPropertiesFromUser();
            getContainingPanel().getTheGUI().getModel().addSpecies( this );
            
            // If the species was added to the model add its concentration parameter as well.
            // 
            
        }
        
        this.derived = derived;
    }

    // A derived species was read from a .net file but has not been added to the model yet
    public boolean isDerived() 
    {
        return derived;
    }

    public void relinkParameter() 
    {
        if (debug_statements) System.out.println("Relinking Concentration Parameter in Species");
        
           String cname = getConcentrationName();
          Parameter cparam = getContainingPanel().getTheGUI().getModelParameters().getParameter(cname);
          
          setConcentrationParameter( cparam );

    }
    
    public void setLabel(String new_label) 
   {

        /*
            if ( this.label == null )
            {
                this.label = new FlickrLabel( new_label, getX(), getY()+getHeight()+20, containing_panel, true );
               
            }
            else
            {
                this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
            }  
          */
        
            if ( debug_statements ) System.out.println("Setting Species Label to " + new_label );
        
            super.setLabel( new_label );
        
            this.label.setFont( new Font("Arial", Font.ITALIC, 14) );
            this.label.setOn();
            
            label.setLabelXOffset(getWidth());
            label.setLabelYOffset(2*-label.getFont().getSize()+2);
            
            
    }

    
    
    

    

    

    
}
