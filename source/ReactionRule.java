import java.util.*; //For vector data structure
import java.awt.*; // For JComponent
import javax.swing.*; // For graphical interface tools
import java.awt.event.*; // For mouse interactions
import java.io.*; // For file IO in write and read object methods
import java.net.*; // For URL so we can load images from .jar files
import javax.swing.border.*;

public class ReactionRule extends Operation
    {
            
    
    private class ParameterChooserListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            JComboBox cb = (JComboBox)e.getSource();
            String key = (String)cb.getSelectedItem();
            String value = model_parameters.getValue( key );
            
            // If the key does not exist then do not replace the value
            if ( cb == forward_rate_parameter_chooser )    
            {
                if ( value != null )
                {
                    forward_rate_textfield.setText( value );
                }
            }
            else if ( cb == reverse_rate_parameter_chooser )
            {
                if ( value != null )
                {
                    reverse_rate_textfield.setText( value );
                }
            }
                
             
        }
    }
    
    private class PropertiesDialogDone extends WindowAdapter implements ActionListener 
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
        
            String forward_rate_key = (String)forward_rate_parameter_chooser.getSelectedItem();
            String forward_rate_value = forward_rate_textfield.getText();
            
            
            if ( label_textfield.getText().length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Rule Label",
                        "The rule label field cannot be left blank\n");
                        return;
            }
            
            if ( forward_rate_key == null )
            {
                getContainingPanel().displayError("Error Setting the Forward Rate",
                        "The forward rate must have a name\n");
                        return;
            }
            
            if ( forward_rate_key.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Forward Rate",
                        "The forward rate must have a name\n");
                        return;
            }
            
            if ( forward_rate_value.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Forward Rate",
                        "The forward rate value field was left blank\n");
                        return;
            }
            
            
            
            Float forward_rate;
            try {
                    forward_rate = new Float( forward_rate_value );
                    
                    if ( forward_rate.floatValue() < 0.0 ) 
                    {
                        getContainingPanel().displayError("Error Setting the Forward Rate",
                        "The forward rate must be a positive number;\n" +
                        forward_rate_value + " is not in that range.");
                        return;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate", forward_rate_value + " is not a valid number" );
                    return;
                }
            
            if ( !model_parameters.setValue( forward_rate_key, forward_rate_value ) ) // false if key not found
            {
                model_parameters.addParameter( forward_rate_key, forward_rate_value );
            }
            
            setForwardRateParameter( model_parameters.getParameter(forward_rate_key) ); //rate_value.floatValue() );
            
            // now do the same for the reverse reaction rate
            if (isReversable())
            {
            String reverse_rate_key = (String)reverse_rate_parameter_chooser.getSelectedItem();
            String reverse_rate_value = reverse_rate_textfield.getText();
            
            if ( reverse_rate_key == null )
            {
                getContainingPanel().displayError("Error Setting the Reverse Rate",
                        "The reverse rate must have a name\n");
                        return;
            }
            
            if ( reverse_rate_key.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Reverse Rate",
                        "The reverse rate must have a name\n");
                        return;
            }
            
            if ( reverse_rate_value.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Reverse Rate",
                        "The reverse rate value field was left blank\n");
                        return;
            }
            
            Float reverse_rate;
            try {
                    reverse_rate = new Float( reverse_rate_value );
                    
                    if ( reverse_rate.floatValue() < 0.0 ) 
                    {
                        getContainingPanel().displayError("Error Setting the reverse Rate",
                        "The reverse rate must be a positive number;\n" +
                        reverse_rate_value + " is not in that range.");
                        return;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate", reverse_rate_value + " is not a valid number" );
                    return;
                }
            
            if ( !model_parameters.setValue( reverse_rate_key, reverse_rate_value ) ) // false if key not found
            {
                model_parameters.addParameter( reverse_rate_key, reverse_rate_value );
            }
            
            setReverseRateParameter( model_parameters.getParameter(reverse_rate_key) ); //rate_value.floatValue() );
            }
            
              
        String index_str = index_textfield.getText();
        Integer index = new Integer( index_str );
     
        WidgetPanel wp = getContainingPanel();
         
        if ( wp instanceof ReactionRulePalette )
        {
         ReactionRulePalette sp = (ReactionRulePalette)wp;
        
        if ( index < 0 )
        {
            sp.displayError( "Index Error", "The index " + index + " less than 0" );
            return;
        }
        else if ( index > sp.getAllReactionRules().size() )
        {
            sp.getAllReactionRules().remove(ReactionRule.this);
            sp.getAllReactionRules().add( ReactionRule.this );
            sp.compressDisplay();
        }
        else
        {
            sp.getAllReactionRules().remove(ReactionRule.this);
            sp.getAllReactionRules().insertElementAt( ReactionRule.this, index.intValue() );
            sp.compressDisplay();
        }
        
        }
            
        properties_dialog.setVisible( false );
        properties_dialog.dispose();
        
        setLabel( label_textfield.getText() );
        setAnnotation( annotation_textfield.getText() );
        
        user_canceled = false;
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
  private class PropertiesDialogCancel extends WindowAdapter implements ActionListener 
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
            
        properties_dialog.setVisible( false );
        properties_dialog.dispose();
        
        user_canceled = true;
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
    // Serialization explicit version
    private static final long serialVersionUID = 1;
        
        // for progress bar
        int progress = 0;
        private javax.swing.Timer timer;
        private JProgressBar progress_bar;

        private transient JDialog properties_dialog;
        
        //private Domain domain;
        
        transient JTextField index_textfield;
        transient JTextField label_textfield;
        transient JTextField forward_rate_textfield;
        transient JTextField reverse_rate_textfield;
        transient JTextField annotation_textfield;
        transient protected JComboBox forward_rate_parameter_chooser;
        transient protected JComboBox reverse_rate_parameter_chooser;
        transient protected Vector<Parameter> parameters;
        transient protected ModelParameters model_parameters;
        private boolean user_canceled = false; 
        
        protected float reverse_rate;
        protected float forward_rate;
        
        // For backwards compatability - point to the same place as operands and results
        //protected Vector<Pattern> products;
        //protected Vector<Pattern> reactants;
        //protected Vector operators; // this is dangerous because it shadows operators in
        // Operation - but older versions of RuleBuilder expect to find Operators here
        
        // For subclasses
        ReactionRule()
        {
            
        }
        
	ReactionRule( Vector<Pattern> reactants, Vector<Operator> operators, Vector<Pattern> products, String forward_rate, String reverse_rate, boolean reversable, WidgetPanel containing_panel )
	{
            //this.forward_rate = forward_rate;
            //this.reverse_rate = reverse_rate;
            setProducts( products );
	    setReactants( reactants );
            this.operators = operators;
            this.containing_panel = containing_panel;
	}
	

public void displayPopupMenu( MouseEvent e ) 
{
    getContainingPanel().getTheGUI().setSaveNeeded( true );
    displayPopupMenu( e.getX(), e.getY() );
}
        
public void displayPopupMenu( int x, int y ) 
{
    getContainingPanel().getTheGUI().setSaveNeeded( true );
    try
    {
    /*
    JMenuItem delete_rule = new JMenuItem( "delete" );
   
    // Context menu for changing attributes of the selected component
    JPopupMenu popup = new JPopupMenu();
    popup.add("Actions");
    popup.addSeparator();
    popup.add(delete_rule);
    		
    delete_rule.addActionListener( this );
    
    popup.show( containing_panel, e.getX(), e.getY() );
    */
    
    JMenuItem disband_menu_item = new JMenuItem( "Disband" );
    JMenuItem add_menu_item = new JMenuItem( "Add to Reaction Rule List" );
    JMenuItem forward_menu_item = new JMenuItem( "Set Forward Rate" );
    JMenuItem reverse_menu_item = new JMenuItem( "Set Reverse Rate" );
    JMenuItem del_menu_item = new JMenuItem( "Delete" );
    JMenuItem create_mapping_menu_item = new JMenuItem( "Create Mapping" );
    JMenuItem layout_menu_item = new JMenuItem( "Auto-Layout" );
        JMenuItem properties_menu_item = new JMenuItem( "Properties" );
    
   
    
        //unlock_menu_item.addActionListener( this );
        //lock_menu_item.addActionListener( this );
        disband_menu_item.addActionListener( this );
        add_menu_item.addActionListener( this );
        forward_menu_item.addActionListener( this );
        reverse_menu_item.addActionListener( this );
        del_menu_item.addActionListener( this );
        create_mapping_menu_item.addActionListener( this );
        layout_menu_item.addActionListener( this );
        properties_menu_item.addActionListener( this );
        
        // Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
        popup.addSeparator();
        popup.add( forward_menu_item );
        if ( isReversable() )
        {
            popup.add( reverse_menu_item );
        }

        popup.add( create_mapping_menu_item );
        
        //popup.add( layout_menu_item );
	popup.addSeparator();
        
        if ( getContainingPanel() != getContainingPanel().getTheGUI().getReactionRulePalette() )
        {
            
            popup.add( add_menu_item );
            popup.addSeparator();
            popup.add( disband_menu_item );
        }
        
        popup.add( del_menu_item );
        popup.add(properties_menu_item);
        
        popup.show( containing_panel, x, y );
        
    }
    catch (Exception exp)
    {
        exp.printStackTrace();
    }
}

public void actionPerformed( ActionEvent action )
{
    if (debug_statements) System.out.println(action);
    
     if ( action.getActionCommand().equals("Create Mapping") )
    {
        createMapping();
        return;
    }
    else if ( action.getActionCommand().equals("Auto-Layout") )
    {
        layout();
    }
    else if ( action.getActionCommand().equals("Properties") )
    {
        setPropertiesFromUser();
    }
    else if ( action.getActionCommand().equals("Delete") )
    {
        if (debug_statements) System.out.println("Deleting Reaction Rule");
        getContainingPanel().removeReactionRule( this );
        getContainingPanel().refreshAll();
    }
    else if ( action.getActionCommand().equals( "Disband" ) )
    {
           disband();
    }
    else if ( action.getActionCommand().equals( "Add to Reaction Rule List" ) )
    {
        if ( isReversable() )
        {
            if ( getReverseRate().equals("") )
            {
                getContainingPanel().displayError("Error Creating Reaction Rule", "Please set the reverse reaction rate.");
                return;
            }
        }
        
        if ( getForwardRate().equals("") )
        {
                getContainingPanel().displayError("Error Creating Reaction Rule", "Please set the forward reaction rate.");
                return;
        }
        
        if (debug_statements) System.out.println( "The Forward Reaction Rate is " + getForwardRate() );
           
        
        ReactionRule copy = null;
        try
        {
            copy = (ReactionRule)WidgetCloner.clone( this );
        }
        catch ( Exception e )
        {
            getContainingPanel().displayError("ReactionRule Error", "There was an error cloning the reaction rule. Contact support at support@bionetgen.com");
        }
        getContainingPanel().getTheGUI().getReactionRulePalette().addReactionRule( copy );
    }
    else if ( action.getActionCommand().equals("Set Forward Rate") )
        {
            ((Forward)getProductionOperator()).setForwardRateFromUser();
            getContainingPanel().repaint();
            
            /*
            Float rate_value = null;
            boolean rate_set = false;
            
            while ( rate_set == false )
            {
            String rate = (String)getContainingPanel().displayInputQuestion( "Set Forward Reaction Rate", "Enter the forward reaction rate.");
            
            if ( rate == null )
            {
                return;
            }
            
            try 
            {
                rate_value = new Float( rate );
                rate_set = true;
                
                if ( rate_value.floatValue() < 0.0 )
                {
                    getContainingPanel().displayError("Error Setting the Reaction Rate", 
                    "The reaction rate must be positive;\n" +
                    rate + " is not in that range.");
                    rate_set = false;
                }
                
            }
            catch( NumberFormatException exp )
            {
                getContainingPanel().displayError( "Error Setting the Reaction Rate",rate + " is not a valid number" );
                rate_set = false;
            }
            }
            
            setForwardRate( rate_value.floatValue() );
            getContainingPanel().repaint();
             */
        }
        else if ( action.getActionCommand().equals("Set Reverse Rate") )
        {
            ((ForwardAndReverse)getProductionOperator()).setReverseRateFromUser();
            getContainingPanel().repaint();
            
            /*
            Float rate_value = null;
            boolean rate_set = false;
            
            while ( rate_set == false )
            {
            String rate = (String)getContainingPanel().displayInputQuestion( "Set Reverse Reaction Rule", "Enter the reverse reaction rate.");
            
            if ( rate == null )
            {
                return;
            }
            
            try 
            {
                rate_value = new Float( rate );
                rate_set = true;
                
                if ( rate_value.floatValue() < 0.0 )
                {
                    getContainingPanel().displayError("Error Setting the Reaction Rate", 
                    "The reaction rate must be positive;\n" +
                    rate + " is not in that range.");
                    rate_set = false;
                }
                
            }
            catch( NumberFormatException exp )
            {
                getContainingPanel().displayError( "Error Setting the Reaction Rate",rate + " is not a valid number" );
                rate_set = false;
            }
            }
            
            setReverseRate( rate_value.floatValue() );
            
             */
        }
    else
    {
        getContainingPanel().displayError("Internal Error","Unknown action command \"" + action.getActionCommand() + "\" in ReactionRule::actionPerformed(). Contact support at support@bionetgen.com." );
    }
    
    /*
    else if ( action.getActionCommand().equals("Apply this rule") )
        {
            // 1 Get all matching components in existing species that match the operands
            // 2) Copy the species they belong to
            // 3) Replace the components that match the operands with the components from
            // the resultant.
            // 4) Add the modified species copies to the SpeciesPalette
                   
          // One reactant case
          if ( containing_panel != null )
                {
                    Vector product_parts = new Vector();
                    
                    Iterator oper_itr = getOperands().iterator();
                   
                    while ( oper_itr.hasNext() )
                    {
                        containing_panel.getTheGUI().getSpeciesPalette().clearSelections();
                        Vector matching_graphs = containing_panel.getTheGUI().getSpeciesPalette().getMatchingPortions( (Species) oper_itr.next() );
                        
                        Iterator matching_graph_i = matching_graphs.iterator();
                        while ( matching_graph_i.hasNext() )
                        {
            
                            Vector matching_components = (Vector) matching_graph_i.next();
                            
                        // Replace matching components with result
                        Species matching_species = null;
                        Species matching_species_clone = null;
                            
                        Iterator match_itr = matching_components.iterator();
                        while ( match_itr.hasNext() )
                        {
                            Vector components = (Vector)match_itr.next();
                            
                            if ( !components.isEmpty() ) 
                            {
                                matching_species = ((BioComponent)components.get(0)).getSpecies();
                                
                                if (debug_statements) System.out.println("Matching Species contains " + matching_species.getComponents().size() + " components");
                                
                                try
                                {
                                    matching_species_clone = SpeciesCloner.clone( matching_species );
                                }
                                catch( Exception e )
                                {
                                    getContainingPanel().displayError("Error Cloning Species",
                                    "Contact support at support@bionetgen.com");
                                }
                                
                                // Ugh! Have to map the original components that were matched with 
                                // their counterparts in the cloned species
                                // There must be a better way - need to redesign this code from top down
                                for ( int i = 0; i < components.size(); i++ )
                                {
                                    BioComponent cbc = (BioComponent)components.get(i);
                                    BioComponent counterpart = matching_species_clone.getComponent( cbc.getID() );
                                    components.set( i, counterpart );
                                }
                                
                            }
                            else
                            {
                                continue;
                            }
                            
                            
                            // -- The following problem is handled by treating each match
                            //    individually
                            
                            // quick sanity check
                            // this is wrong because you can have multiple subgraph matches
                            // and so return too many components
                            // each seperate subgraph match ought to return separate node vector
                            // +++++++ FIX THIS!!! +++++++
                            //if ( components.size() != result.getComponents().size() )
                            //{
                            //    getContainingPanel().displayError("Rule Application Error","The total number of component in the result (and operands)\n" +
                            //    "and in the component matches must be the same.\n There were  " + result.getComponents().size() + " components in the operands/result" +
                            //    " and " + matching_components.size() + " matching components.\n Contact support at support@bionetgen.com");
                            //    return;
                            //}
                            
                            Iterator i = components.iterator();
                            while ( i.hasNext() )
                            {
                                BioComponent matching_component = ((BioComponent)i.next() );
                                BioComponent corresponding_component = (BioComponent)result.getComponent( matching_component.getID() );
                                
                                if (corresponding_component == null ) continue;
                                
                                BioContainer container = matching_component.getContainer();
                                
                                if ( container == null )
                                {
                                    getContainingPanel().displayError("Error Applying Reaction Rule",
                                    "Container is null in Component " + matching_component.getLabel() + "{"+matching_component.getID()+"}"+". Contact support at support@bionetgen.com");
                                    continue;
                                }
                                
                                BioComponent corresponding_clone = null;
                                
                                try
                                {
                                    corresponding_clone = BioComponentCloner.clone( corresponding_component );
                                }
                                catch( Exception e )
                                {
                                    getContainingPanel().displayError("Error Cloning BioComponent",
                                    "Contact support at support@bionetgen.com");
                                }
                                
                                container.removeComponent( matching_component );
                                container.addComponent( corresponding_clone );
                                
                                // Fix the location
                                corresponding_clone.setXOffset( matching_component.getXOffset() );
                                corresponding_clone.setYOffset( matching_component.getYOffset() );
                                corresponding_clone.setX( matching_component.getX() );
                                corresponding_clone.setY( matching_component.getY() );
                                
                                // Preserve all edges to external components - edges to components
                                // not included in the matching subgraph (and so should be left alone)
                                
                                Iterator matching_component_edge_itr = matching_component.getEdges().iterator();
                                while ( matching_component_edge_itr.hasNext() )
                                {
                                    Edge current_edge = (Edge) matching_component_edge_itr.next();
                                    BioComponent other_end = current_edge.getOtherComponent( matching_component );
                                    
                                    // Check that the node at the other end of this edge is not a member
                                    // of the set of nodes in the matching subgraph
                                    if ( -1 == components.indexOf( other_end ) )
                                    {
                                        corresponding_clone.addEdge( current_edge );
                                        
                                        // Doesn't matter if start and end endpoint in edge switch
                                        current_edge.setStart( corresponding_clone );
                                        current_edge.setEnd( other_end );
                                    }
                                }
                                
                            
                            }
                            
                            // Fix edge adjacencies for all components (some may not need fixing
                            // because of the edge work done above in edge preservation - 
                            // we don't worry about that here.
                            // Iterate through all the edges in the target species and match
                            // the pointers to nodes with the actual nodes in the new species
                            // using the Widget IDs (which are preserved through cloning)
                            
                            
                            Iterator comp_itr = matching_species_clone.getComponents().iterator();
                            while ( comp_itr.hasNext() )
                            {
                                BioComponent current_component = (BioComponent)comp_itr.next();
                                
                                Iterator edge_itr = current_component.getEdges().iterator();
                                while ( edge_itr.hasNext() )
                                {
                                    Edge current_edge = (Edge)edge_itr.next();
                                    
                                    long start_id = current_edge.getStart().getID();
                                    long end_id = current_edge.getEnd().getID();
                                   
                                    BioComponent start_comp = matching_species_clone.getComponent( start_id );
                                    BioComponent end_comp = matching_species_clone.getComponent( end_id );
                                    
                                    if ( start_comp == null || end_comp == null )
                                    {
                                        getContainingPanel().displayError("Error Applying Rule",
                                                                          "Edge endpoint could not be found in matching species. Internal error: contact support at support@bionetgen.com");
                                    }
                                    
                                    current_edge.setStart( start_comp );
                                    current_edge.setEnd( end_comp );
                                }
                            }
                            
                            // Yuck!
                            product_parts.add( matching_species_clone );
                            
                        }
                 
                        }
                        
                    }
                    
                    // Take the product parts and make them into one species
                    
                    // Check that there was something to work with
                    if ( product_parts.isEmpty() )
                    {
                        return;
                    }    
                    
                    Iterator product_parts_itr = product_parts.iterator();
                    
                    Species product = (Species)product_parts_itr.next();
                    
                    while ( product_parts_itr.hasNext() )
                    {
                        Species current_part = (Species)product_parts_itr.next();
                        
                        // Take all the components and containers from the part and add to the
                        // product
                        
                        product.getContainers().addAll( current_part.getContainers() );
                    }
          
                    getContainingPanel().getTheGUI().getSpeciesPalette().addSpecies( product );
           
          }
     
          }
     */
}

// Legacy
public String getReverseRate() 
{
    return ((ForwardAndReverse)getProductionOperator()).getReverseRate();
}

public Parameter getReverseRateParameter() 
{
    return ((ForwardAndReverse)getProductionOperator()).getReverseRateParameter();
}


public String getForwardRateName() 
{   
    return ((Forward)getProductionOperator()).getForwardRateName();
}

public String getReverseRateName() 
{   
    return ((ForwardAndReverse)getProductionOperator()).getReverseRateName();
}

// Legacy
public String getForwardRate() 
{   
    return ((Forward)getProductionOperator()).getForwardRate();
}

public Parameter getForwardRateParameter() 
{   
    Forward rate = ((Forward)getProductionOperator());
    return rate.getForwardRateParameter();
}

public boolean setForwardRateParameter( Parameter p ) 
{   
    ((Forward)getProductionOperator()).setForwardRateParameter( p );
    return true;
}

public boolean setReverseRateParameter( Parameter p ) 
{   
    if ( !(getProductionOperator() instanceof ForwardAndReverse) ) return false;
    ((ForwardAndReverse)getProductionOperator()).setReverseRateParameter( p );
    return true;
}

// Legacy
synchronized public void setReverseRate(String rate) 
{
    ((ForwardAndReverse)getProductionOperator()).setReverseRate( rate );
    //this.reverse_rate = rate;
}

// Legacy
synchronized public void setForwardRate(String rate) 
{
    ((Forward)getProductionOperator()).setForwardRate( rate );
    //this.forward_rate = rate;
}

public boolean isReversable() 
{
    Operator po = getProductionOperator();
    if ( po != null )
    {
        return po instanceof ForwardAndReverse;
    }
    return false;
}



public Operator getProductionOperator() 
{
    Iterator operator_i = getOperators().iterator();
    while ( operator_i.hasNext() )
    {
        Operator current = (Operator)operator_i.next();
        if ( current instanceof ForwardAndReverse
             || current instanceof Forward )
        {
            return current;
        }
    }
    
    return null;
}

public Vector<Pattern> getProducts()
{
    Vector<Pattern> patterns = new Vector();
    
    Iterator<BioGraph> itr = getResults().iterator();
    while ( itr.hasNext() )
    {
        patterns.add( (Pattern)itr.next() );
    }
    
    return patterns;
}

public Vector<Pattern> getReactants()
{
    Vector<Pattern> patterns = new Vector();
    
    Iterator<BioGraph> itr = getOperands().iterator();
    while ( itr.hasNext() )
    {
        patterns.add( (Pattern)itr.next() );
    }
    
    return patterns;
}
	
public void setProducts( Vector<Pattern> p )
{
   Vector<BioGraph> b = new Vector();
   b.addAll(p);
   setResults( b );
}

public void setReactants( Vector<Pattern> p )
{
   Vector<BioGraph> b = new Vector();
   b.addAll(p);
   setOperands( b );
}


	
// This method maps all components and containers on the products side
// to components and containers on the reactants side. To do this the
// method calls WidgetPanel map on all pairs in a predefined order
// The order was provided by Jim Faeder.
public void createMapping() 
{
    if (debug_statements) System.out.println("createMapping() Called");
    
    // Build a list of Containers on the Reactant and Product sides.
    Vector<BioContainer> product_containers = new Vector<BioContainer>();
    Vector<BioContainer> reactant_containers = new Vector<BioContainer>();
    
    // Iterate over all Reactants.
    Iterator reactant_itr = getReactants().iterator();
    while ( reactant_itr.hasNext() )
    {
        Pattern reactant = (Pattern)reactant_itr.next();
        reactant_containers.addAll( reactant.getContainers() );
    }
    
    // Iterate over all Products.
    Iterator product_itr = getProducts().iterator();
    while ( product_itr.hasNext() )
    {
        Pattern product = (Pattern)product_itr.next();
        product_containers.addAll( product.getContainers() );
    }
    
    if (debug_statements) System.out.println("createMapping(): Found " + product_containers.size() + " product containers");
    if (debug_statements) System.out.println("createMapping(): Found " + reactant_containers.size() + " reactant containers");
    
    int map_label_count = 0; // Map Label 
    Iterator rc_itr = reactant_containers.iterator();
    while ( rc_itr.hasNext() )
    {
        BioContainer current = (BioContainer)rc_itr.next();
        
        BioContainer match = findFirstMatchingContainer( current, product_containers );
        
        if ( match != null ) 
        {
            product_containers.remove( match );
            
            Integer map_label = new Integer(map_label_count++);
            AtomMap map = new AtomMap( current, match, getContainingPanel() );
            map.setLabel( map_label.toString() );
            
            getContainingPanel().addEdge( map );
            
            if (debug_statements) System.out.println("createMapping(): Mapped " + map.getStart().getLabel() + " to " + map.getEnd().getLabel() );
            
            // Map all the components in the matching containers
            // Since current and match are a match they must have the same number
            // of components
            Iterator current_comp_itr = current.getComponents().iterator();
            Iterator match_comp_itr = match.getComponents().iterator();
            
            while ( current_comp_itr.hasNext() && match_comp_itr.hasNext() )
            {
                BioComponent comp_map_start = (BioComponent)current_comp_itr.next();
                BioComponent comp_map_end = (BioComponent)match_comp_itr.next();
                
                map_label = new Integer( map_label_count++ );
                map = new AtomMap( comp_map_start, comp_map_end, getContainingPanel() );
                map.setLabel( map_label.toString() );
                getContainingPanel().addEdge(map);
                
                if (debug_statements) System.out.println("createMapping(): Mapped " + map.getStart().getLabel() + " to " + map.getEnd().getLabel() );
            
            }
            
        }
        
    }
}
    // Helper function for createMapping()
    private BioContainer findFirstMatchingContainer( BioContainer target, Vector<BioContainer> containers )
    {
        Iterator itr = containers.iterator();
        while ( itr.hasNext() )
        {
            BioContainer current = (BioContainer)itr.next();
            if ( isContainerMatch( target, current ) )
            {
                return current;
            }
        }
        
        return null;
    }

    // Helper function for findFirstMatchingContainer()
    private boolean isContainerMatch( BioContainer a, BioContainer b )
    {
        // Check that the labels are the same
        if ( !a.getLabel().equals( b.getLabel() ) ) return false;
        
        // Check that the number of components is the same
        if ( a.getComponents().size() != b.getComponents().size() ) return false;
        
        // Sort the component lists so they can be compared side by side
        a.sortComponents();
        Vector<BioComponent> a_comps = a.getComponents();
        
        b.sortComponents();
        Vector<BioComponent> b_comps = b.getComponents();
        
        Iterator a_itr = a_comps.iterator();
        Iterator b_itr = b_comps.iterator();
        
        while( a_itr.hasNext() && b_itr.hasNext() ) // That these two vectors have the same number of elements has already been checked 
        {
            BioComponent a_current = (BioComponent)a_itr.next();
            BioComponent b_current = (BioComponent)b_itr.next();
            
            // Check that the labels are the same
            if ( !a_current.getLabel().equals( b_current.getLabel() ) )
            {
                return false;
            }
        }
        
        // All the component labels were the same, the container label was the same
        // and there were the same number of components so these two Containers match
        return true;
    }

    /* mlf
    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }
    */

    public void display(Component c, Graphics2D g2d) 
    {
        if ( !isVisible() ) return;
        
	/* mlf
        if ( domain != null )
        {
            domain.display(c, g2d);
        }
        */
        
        /*
        g2d.setColor(Color.lightGray);
        
        Stroke current_stroke = g2d.getStroke();
        
	g2d.setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	g2d.drawRect( x-5, y-5, width+10, height+10 );
        
        g2d.setColor( c.getBackground() );
        
        g2d.setStroke( current_stroke );
         */
        
        // Display maps
        Iterator maps = getAtomMaps().iterator();
        while ( maps.hasNext() )
        {
            AtomMap map = (AtomMap)maps.next();
            map.display(c, g2d);
        }
        
        super.display( c, g2d );
    }

    public void updateLocation(int x, int y, boolean confine_to_boundaries) 
    {
        super.updateLocation( x, y, confine_to_boundaries );
        
        /*
        if ( domain != null )
        {
            domain.updateLocation( x, y, confine_to_boundaries );
        }
        */
    }

    

    public void setContainingPanel(WidgetPanel wp) 
    {
        label.setContainingPanel(wp);
        
        super.setContainingPanel( wp );
        
        Iterator map_itr = getAtomMaps().iterator();
        while( map_itr.hasNext() )
        {
            ((AtomMap)map_itr.next()).setContainingPanel( wp );
        }
    }
    
    public void layout() 
    {
        // Do this to recalculate coords
            setContainingPanel( getContainingPanel() );
        
            resetOffsets();
            updateLocation(getX(), getY(), false);
            
            Iterator<BioGraph> p_itr = getBioGraphs().iterator();
            Iterator<Operator> o_itr = operators.iterator();
            
            while ( p_itr.hasNext() )
            {
                BioGraph pattern = (BioGraph)p_itr.next();
                
                pattern.layout();
                
                // Recalculate x, y, width, height since layout may have changed them
               //pattern.resetOffsets();
               // pattern.updateLocation(getX(), getY(), false);
                
                boolean direction = true;
                
                Operator op = getProductionOperator();
                int y = op.getY()-pattern.getHeight()/2;
                int x = pattern.getX();
                if ( getOperands().indexOf( pattern ) != -1 )
                    {
                        if ( pattern.getX()+pattern.getWidth() > op.getX() )
                        {
                            x = op.getX()-(pattern.getWidth()+op.getWidth());
                        }
                    }
                else
                    {
                        if ( pattern.getX() < op.getX() )
                        {
                            x = op.getX()+op.getWidth()*2;
                        }
                    }
                
               pattern.resetOffsets();
               pattern.updateLocation( x, y, false );
                
                while ( pattern.detectCollision( getBioGraphs() ) )
                {
                    //direction = !direction;
                    
                    // if its an operand make sure it is left of the arrow
                    
                    
                    int new_x = pattern.getX();
                    int new_y = pattern.getY();
                
                    if (direction)
                    {
                        new_x-=20;
                    }
                    else
                    {
                        new_y-=pattern.getHeight();
                    }
                    
                    pattern.resetOffsets();
                    pattern.updateLocation( new_x, new_y, false );
                }
            }
            
            
            // Put the operators in the right place
    }

    /*
    // This function is needed to make sure geometry changes caused
    // my using a CDK Molecules layout are propagated properly so that
    // the Rule looks OK afterwards
    public void mapToCDKMolecules(Vector<CDKCapsule>capsules) 
    {
        Iterator <BioGraph> pattern_itr = getPatterns().iterator();
        
        while ( pattern_itr.hasNext() )
        {
        
        Vector<BioContainer> containers = this.getContainers();
           
            Iterator container_itr = containers.iterator();
            while ( container_itr.hasNext() )
            {
                BioContainer container = (BioContainer)container_itr.next();
                
                CDKCapsule mol = null;

                    // Find the matching molecule
                    Iterator mol_itr = mols.iterator();
                    while ( mol_itr.hasNext() )
                    {
                        mol = (CDKCapsule)mol_itr.next();
                        
                        if ( container.getLabel().equals( mol.getLabel() ) )
                        {
                            CDKCapsule copy = new CDKCapsule( 0,0,0,0, containing_panel );
                            copy.setLabel( mol.getLabel() );
                            copy.setCDKMolecule( (org.openscience.cdk.Molecule)mol.getCDKMolecule().clone() );
                            
                            container.setCDKCapsule( copy );
                            break; // We only want to match one molecule to each container
                            //container.setShowCDK( true );
                        }
                    }
                }
            
        }
    }
     */ 

    public void relinkParameters() 
    {
        Operator op = getProductionOperator();
          if ( op instanceof ForwardAndReverse )
          {
              ((ForwardAndReverse)op).relinkParameter();
          }
          else if ( op instanceof Forward )
          {
              ((Forward)op).relinkParameter();
          }
    }
   
    
    
    public boolean setPropertiesFromUser() 
    {
        JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            properties_dialog = new JDialog( owner, true );
            properties_dialog.setTitle( "Reaction Properties" );
            properties_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            properties_dialog.addWindowListener( new PropertiesDialogCancel() );
            
            Container content = properties_dialog.getContentPane();
            JPanel label_panel = new JPanel();
            JPanel button_panel = new JPanel();
            JPanel forward_rate_panel = new JPanel();
            JPanel reverse_rate_panel = new JPanel();
            JPanel forward_rate_names_panel = new JPanel();
            JPanel forward_rate_values_panel = new JPanel();
            JPanel reverse_rate_names_panel = new JPanel();
            JPanel reverse_rate_values_panel = new JPanel();
            JPanel rates_panel = new JPanel();
            JPanel annotation_panel = new JPanel();
            
            label_panel.setLayout(new BorderLayout());
            rates_panel.setLayout( new BorderLayout() );
            reverse_rate_panel.setLayout( new BorderLayout() );
            forward_rate_panel.setLayout( new BorderLayout() );
            
            //forward_rate_names_panel.setLayout(new BoxLayout(forward_rate_names_panel, BoxLayout.PAGE_AXIS));
            //forward_rate_values_panel.setLayout(new BoxLayout(forward_rate_values_panel, BoxLayout.PAGE_AXIS));
            
            forward_rate_names_panel.setLayout(new BorderLayout());
            forward_rate_values_panel.setLayout(new BorderLayout());
            
            //reverse_rate_names_panel.setLayout(new BoxLayout(reverse_rate_names_panel, BoxLayout.PAGE_AXIS));
            //reverse_rate_values_panel.setLayout(new BoxLayout(reverse_rate_values_panel, BoxLayout.PAGE_AXIS));
            
            reverse_rate_names_panel.setLayout(new BorderLayout());
            reverse_rate_values_panel.setLayout(new BorderLayout());
            
            annotation_panel.setLayout( new BorderLayout() );
            
            label_textfield = new JTextField(10);
	    if (! (getContainingPanel() instanceof ReactionRulePalette)) {
            	long nextrulesid = getContainingPanel().getTheGUI().getModel().getIDRulesGeneratorNext();
            	String numstring = new Long(nextrulesid).toString();
            	label_textfield.setText( "Rule" + numstring );
            }
            else {
            	label_textfield.setText( getLabel() );
            }
            forward_rate_textfield = new JTextField(10);
            reverse_rate_textfield = new JTextField(10);
            annotation_textfield = new JTextField(10);
            
            index_textfield = new JTextField(4);
            index_textfield.setText( getIndex().toString() );
            
            Integer index = getContainingPanel().getAllReactionRules().indexOf(this);
            index_textfield.setText( index.toString() );
            
            if ( getAnnotation() != null )
            {
                annotation_textfield.setText( getAnnotation() );
            }
            
            if (getForwardRateParameter() != null ) forward_rate_textfield.setText( getForwardRate() );
            
           
            
            if (isReversable())
            {
                if (getReverseRateParameter() != null )  reverse_rate_textfield.setText( getReverseRate() );
                reverse_rate_textfield.setEnabled(true);
            }
            else
            {
                reverse_rate_textfield.setEnabled(false);
            }
            
            
            model_parameters = getContainingPanel().getTheGUI().getModelParameters();
            Vector<Parameter> parameters = model_parameters.getParameters();
            
            Vector<String> parameter_names = model_parameters.getParameterNames();
            
            forward_rate_parameter_chooser = new JComboBox( parameter_names );
            forward_rate_parameter_chooser.setEditable( true );
            forward_rate_parameter_chooser.addActionListener( new ParameterChooserListener() );
            
            reverse_rate_parameter_chooser = new JComboBox( parameter_names );
            reverse_rate_parameter_chooser.setEditable( true );
            reverse_rate_parameter_chooser.addActionListener( new ParameterChooserListener() );
            if ( !isReversable() ) reverse_rate_parameter_chooser.setEnabled( false );
            
            if ( getForwardRateParameter() != null )
            {
               String default_selection = getForwardRateParameter().getKey();
                forward_rate_parameter_chooser.setSelectedItem( default_selection ); 
            }
            
            if ( isReversable() )
            if ( getReverseRateParameter() != null )
            {
                String default_selection = getReverseRateParameter().getKey();
                reverse_rate_parameter_chooser.setSelectedItem( default_selection ); 
            }
            

            JButton done = new JButton("Done");
            JButton cancel = new JButton("Cancel");
            done.addActionListener( new PropertiesDialogDone() );
            cancel.addActionListener( new PropertiesDialogCancel() );
            
            forward_rate_names_panel.add( new JLabel("Name: "), BorderLayout.NORTH );
            forward_rate_values_panel.add( forward_rate_parameter_chooser, BorderLayout.NORTH );
            
            forward_rate_names_panel.add( new JLabel("Rate: "), BorderLayout.CENTER );
            forward_rate_values_panel.add( forward_rate_textfield, BorderLayout.CENTER );
            
            reverse_rate_names_panel.add( new JLabel("Name: "), BorderLayout.NORTH );
            reverse_rate_values_panel.add( reverse_rate_parameter_chooser, BorderLayout.NORTH );
            
            reverse_rate_names_panel.add( new JLabel("Rate: "), BorderLayout.CENTER );
            reverse_rate_values_panel.add( reverse_rate_textfield, BorderLayout.CENTER );
            
            
            annotation_panel.add( annotation_textfield );
            
            
            button_panel.add( done, BorderLayout.WEST );
            button_panel.add( cancel, BorderLayout.CENTER );
            
            Border label_eb = BorderFactory.createEtchedBorder();
            Border properties_eb = BorderFactory.createEtchedBorder();
            
            Border titled_label_eb = BorderFactory.createTitledBorder( label_eb, "Rule Name" );
            Border titled_forward_rate_eb = BorderFactory.createTitledBorder( properties_eb, "Forward Rate" );
            Border titled_reverse_rate_eb = BorderFactory.createTitledBorder( properties_eb, "Reverse Rate" );
            Border titled_annotation_eb = BorderFactory.createTitledBorder( properties_eb, "BNGL Annotation" );
            
            label_panel.setBorder( titled_label_eb );
            
            forward_rate_panel.setBorder( titled_forward_rate_eb );
            forward_rate_panel.add( forward_rate_names_panel, BorderLayout.WEST );
            forward_rate_panel.add( forward_rate_values_panel, BorderLayout.CENTER );
            
            
            reverse_rate_panel.setBorder( titled_reverse_rate_eb );
            reverse_rate_panel.add( reverse_rate_names_panel, BorderLayout.WEST );
            reverse_rate_panel.add( reverse_rate_values_panel, BorderLayout.CENTER );
         
            annotation_panel.setBorder( titled_annotation_eb );
            
            label_panel.add( new JLabel("Label: "), BorderLayout.WEST );
            label_panel.add( label_textfield, BorderLayout.CENTER );
            
            content.setLayout(new BorderLayout());
            content.add(label_panel, BorderLayout.NORTH );
            
            JPanel north_panel = new JPanel();
            north_panel.setLayout( new BorderLayout() );
            
            
            north_panel.add(label_panel, BorderLayout.NORTH);
            
            JPanel index_panel = new JPanel();
            index_panel.setLayout( new BorderLayout() );
            index_panel.add( new JLabel("Index: "), BorderLayout.WEST );
            index_panel.add( index_textfield, BorderLayout.CENTER );
            index_panel.add( new JLabel(" "), BorderLayout.EAST );
            
            
            if ( getContainingPanel() instanceof ReactionRulePalette )
            {
                north_panel.add(index_panel, BorderLayout.CENTER);
            }
            
            rates_panel.add(forward_rate_panel, BorderLayout.NORTH );
            
            content.add( north_panel, BorderLayout.NORTH );
            
            if ( isReversable() )
            {
                rates_panel.add(reverse_rate_panel, BorderLayout.CENTER );
            }
            
            content.add( rates_panel, BorderLayout.CENTER );
            
            JPanel south_panel = new JPanel();
            south_panel.setLayout( new BorderLayout() );
            
            south_panel.add( annotation_panel, BorderLayout.NORTH );
            south_panel.add( button_panel, BorderLayout.CENTER );
            content.add( south_panel, BorderLayout.SOUTH );
                        
            properties_dialog.setLocation( 200, 100 );  
            properties_dialog.setSize( 250, 310 );  
            if ( !isReversable() ) properties_dialog.setSize( 250, 240 );  
            properties_dialog.setResizable(true);
            
            
           properties_dialog.pack();
        
            properties_dialog.setVisible(true);
           
           
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
    
}
