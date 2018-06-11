// SelectionBox class is a collection of widgets

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
//import java.awt.geom.AffineTransform; // For resizing containers
import java.math.*;
import java.awt.event.*; // For mouse interactions

import java.awt.image.BufferedImage;

public class SelectionBox extends Widget implements ActionListener
{
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    boolean debug_statements = true;
    
    private Vector<Widget> contents = new Vector<Widget>();

    SelectionBox( WidgetPanel containing_panel )
    {
        this.containing_panel = containing_panel;
	//super(0, 0, "selection box", containing_panel);
    }
    
    boolean addWidget(Widget widget)
    {
        // Check for duplication
        if ( contents.indexOf(widget) != -1 )
        {
            // This widget is already in the selection box
            return false;
        }
        
	contents.add( widget );
	widget.resetOffsets();
        
        if (debug_statements) System.out.println( "The selection box now contains " +
        getComponents().size() + " components, " +
        getEdges().size() + " edges, " +
        getMaps().size() + " maps, " +
        getContainers().size() + " containers, " +
        getSpecies().size() + " species, and " +
        getOperators().size() + " operators" +
        getPatterns().size() + " patterns" +
        getGroups().size() + " groups" +
        getLabels().size() + " labels"
        );
        
        return true;
    }

    public void display( Component c, Graphics2D g2d )
    {
        //if ( !isVisible() ) return;
        
	//g.drawImage(image, x, y, width, height, containing_panel);


	g2d.setColor(Color.blue);
	//Graphics2D g2d = (Graphics2D)g;
	((Graphics2D)g2d).setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	
        g2d.drawRect( x, y, width, height );
	
        Iterator widget_itr = getContents().iterator();
        while ( widget_itr.hasNext() )
        {
            Widget wid = (Widget)widget_itr.next();
            
            if ( wid instanceof Edge )
            {
                Edge edge = (Edge)wid;
                if ( getContents().indexOf( edge.getStart() ) != -1
                                    && getContents().indexOf( edge.getEnd() ) != -1 ) {
                                        edge.display( c, g2d );
                                    }
                                    
            }
            else
            {
                wid.display( c, g2d );
            }
            
        }
         
        // Display pattern zones
         /*                            
        Iterator zone_itr = findOperandZones().iterator();
        while ( zone_itr.hasNext() )
        {
            Rectangle zone = (Rectangle)zone_itr.next();
            
            g2d.setColor(Color.red);
        ((Graphics2D)g2d).setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
            
            g2d.drawRect( (int)zone.getX(), (int)zone.getY(), (int)zone.getWidth(), (int)zone.getHeight() );
            
        }
       */
    }
    
    // this function calculates the pointer offset so that the container moves RELATIVE to the pointer's
    // (x,y) when dragged - not TO the pointers (x,y)
    void calculatePointerOffset( int mouse_x, int mouse_y )
    {
	//x_offset = mouse_x - x;
	//y_offset = mouse_y - y;
        super.calculatePointerOffset(mouse_x, mouse_y);
        
	// Components need to calculate their own offsets in order to display properly
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		((Widget)i.next()).calculatePointerOffset( x, y ); 
	    }

    }

    // Overrides Widget::updateLocation( x, y )
    public void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
    { 
      if (debug_statements)  System.out.println("SelectionBox:updateLocation() called.");
        
      super.updateLocation( mouse_x, mouse_y, confine_to_boundaries );
        
        // x and y set by super.updateLocation
        int new_x = x;
        int new_y = y;
        
        Iterator<Widget> itr = getContents().iterator();
        while( itr.hasNext() )
        {
            Widget w = itr.next();
            if (debug_statements) System.out.println("SelectionBox: Widget offset: (" +w.getXOffset()+ "," +w.getYOffset()+")" );
            w.updateLocation( new_x, new_y, confine_to_boundaries ); 
        }
        
        
      /*
      
	    // Subtract width/2 and height/2 so pointer tip is centered on icon
	    int new_x = mouse_x - x_offset;  //+ Math.abs(mouse_x - x); //- width/2;
	    int new_y = mouse_y - y_offset;  //Math.abs(mouse_y - y); ;// //- height/2;

            int panel_width = 0;
            int panel_height = 0;
            int panel_x = 0;
            int panel_y = 0;

            WidgetPanel ct = getContainingPanel();
            
            try
            {
                panel_width = (int)(ct.getBaseDimension().getWidth()*(1.0/ct.getZoom()))-ct.getZoomedXTranslation()*2;
                panel_height = (int)(ct.getBaseDimension().getHeight()*(1.0/ct.getZoom()))-ct.getZoomedYTranslation()*2;
                panel_x = -ct.getZoomedXTranslation()*2;
                panel_y = -ct.getZoomedYTranslation()*2;
            }
            catch ( NullPointerException e )
            {
                if ( getContainingPanel() == null ) if (debug_statements) System.out.println("Containing Panel is a NULL pointer.");
                if ( getContainingPanel().panel_dimension == null ) if (debug_statements) System.out.println("Containing Panel's dimension is a NULL pointer.");
                
                e.printStackTrace();
            }
            
	    boolean x_bound = false;
	    boolean y_bound = false;
	                
            // Check for window boundary collision
            // always true for selection boxes
	    if ( confine_to_boundaries )
		{
		    if((new_x+width+10)>panel_width)
			{
			    new_x = (int)panel_width-width-5;
			    x_bound = true;
			}
		    if(new_x < panel_x)
			{
			    new_x = panel_x;
			    x_bound = true;
			}
		    if((new_y+height)>panel_height)
			{
			    new_y = (int)panel_height-height-10;
			    y_bound = true;
			}
		    if(new_y < panel_y)
			{
			    y_bound = true;
			    new_y = panel_y;
			}
		}
            
            
	    x = new_x; 
	    y = new_y;
	    
	    Iterator i = contents.iterator();
	    while ( i.hasNext() )
		{
                    Widget cw = ((Widget)i.next());
		    cw.updateLocation( x, y, false );
                    if (debug_statements) System.out.println("SelectionBox: Widget offset: (" +cw.getXOffset()+ "," +cw.getYOffset()+")" );
		}
	    
	    containing_panel.repaint();
       
       */
	}



    Vector<Widget> getContents()
    {
	return contents;
    }

    void inUse( boolean state )
    {
	in_use = state;
	containing_panel.repaint();

	if (state == false)
	    {
		width = 0;
		height = 0;
		x = 0;
		y = 0;
		

	    Iterator i = contents.iterator();
	    while ( i.hasNext() )
		{
		    ((Widget)i.next()).setSelected(selected);
		}

		contents.removeAllElements();
	    }
    }

    SelectionBox makeCopy()
    {
	// create a new selection box that contains deep copies of this selection
	// box's contents - uses serialization
	SelectionBox new_selection_box = null;

	try 
	    {
		new_selection_box = (SelectionBox)WidgetCloner.clone(this);//SelectionBoxCloner.deepCopy( this );
		//new_selection_box.setContainingPanel( containing_panel );
	    }
	catch( Exception e )
	    {
		containing_panel.displayError("Widget Cloning Error",e.getMessage());
                e.printStackTrace();
	    }

	return new_selection_box;
    }

    // Overloads widget method
    /**
     *
     * @param containing_panel
     */    
    public void setContainingPanel( WidgetPanel containing_panel )
    {
	this.containing_panel = containing_panel;

	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		((Widget)i.next()).setContainingPanel( containing_panel );
	    }
	
    }

    /**
     *
     * @param selected
     */    
    public void setSelected( boolean selected )
    {
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		((Widget)i.next()).setSelected(selected);
	    }
       
    }

    public void displayPopupMenu(MouseEvent e)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        displayPopupMenu( e.getX(), e.getY() );
    }
    
    public void displayPopupMenu(int x, int y)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem make_species = new JMenuItem( "Make Species" );
        make_species.setEnabled(false);
	JMenuItem make_rule = new JMenuItem( "Make Rule" );
        make_rule.setEnabled(false);
	JMenuItem make_pattern_group = new JMenuItem( "Make Observable");
        make_pattern_group.setEnabled(false);
		
        make_species.addActionListener( this );
        make_rule.addActionListener( this );
	make_pattern_group.addActionListener( this );
        
	// Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Actions");
	popup.addSeparator();
	
        //if ( getOperators().isEmpty() ) return;
        
        if ( getContainers().isEmpty() ) 
        {
            // leave all menu items disabled
        }    
        else if ( !getReactionRules().isEmpty() )
        {
            // leave all menu items disabled    
        }
        else
        {    
        
        
        boolean invalid_container = false;
        Iterator container_itr = getContainers().iterator();
        while ( container_itr.hasNext() )
        {
            if ( !((BioContainer)container_itr.next()).isValidPattern() )
            {
                invalid_container = true;
            }
        }
        
        if ( !invalid_container )
        {
        if ( !getOperators().isEmpty() )
        {
            boolean is_reaction_rule = true;
            boolean arrow_found = false;
            Iterator op_itr = getOperators().iterator();
            while ( op_itr.hasNext() )
            {
                Operator op = (Operator)op_itr.next();
                
                if ( op instanceof Forward )
                {
                    if ( arrow_found == true)
                    {
                        // don't want more than one arrow operator per rule
                        is_reaction_rule = false;
                    }
                    else
                    {
                        arrow_found = true;
                    }
                }
                
                if ( op instanceof LogicalOperator )
                {
                    if (debug_statements) System.out.println("Operator of type \"" + op.getClass().getName() + "\" is not a Logical Operator.");
                    is_reaction_rule = false;
                    break;
                }
                else
                {
                    continue;
                }
            }
            
            if ( !arrow_found ) is_reaction_rule = false;
            
            if ( is_reaction_rule )
            {
                make_rule.setEnabled(true);
            }
        }
            
            boolean is_pattern_group = true;
            Iterator op_itr2 = getOperators().iterator();
            while ( op_itr2.hasNext() )
            {
                Operator op = (Operator)op_itr2.next();
                               
                if ( op instanceof LogicalOperator )
                {
                    
                    continue;
                }
                else
                {
                    if (debug_statements) System.out.println("Operator of type \"" + op.getClass().getName() + "\" is not a Logical Operator.");
                    is_pattern_group = false;
                    break;
                }
            }
            
            if ( is_pattern_group )
            {
                make_pattern_group.setEnabled(true);
            }
            
        }
        
        boolean valid_species = true;
        
        if (!getOperators().isEmpty() || !getReactionRules().isEmpty() || !getSpecies().isEmpty() || !getGroups().isEmpty() )
        {
            valid_species = false;
        }
        // Check to see if all the containers are part biographs with all valid types
        Iterator<BioContainer> itr = getContainers().iterator();
        while ( itr.hasNext() )
        {
           if ( !itr.next().isValidType() )
           {
               valid_species = false;
               break;
           }
        }

        if ( valid_species )
        {
            make_species.setEnabled(true);
        }
        
        }
        
        
        popup.add( make_rule );
        popup.add( make_species );
        popup.add( make_pattern_group );
       popup.show( containing_panel, x, y );
        
        
	
    }

    // Handle actions for which this object is a registered listener
    /**
     *
     * @param action
     */    
    public void actionPerformed( ActionEvent action )
    {
        // Assumed to be triggered from the selection box
        if ( action.getActionCommand().equals("Make Species") )
        {
            Iterator<BioContainer> bc_itr = getContainers().iterator();
            
            Vector<Species> species_found = new Vector();
            Vector<Widget> used = new Vector(); // the original widgets used to make the species from
            
            while ( bc_itr.hasNext() )
            {
                BioContainer bc = bc_itr.next();
                
                // Check whether this container has already been used to make a species here.
                if ( used.indexOf(bc) != -1 ) continue;
                
                Species s = getContainingPanel().makeSpecies( bc.getComponents().get(0), "1", used );
                if ( s == null )
                {
                    getContainingPanel().displayError("Error Creating Species","Could not create species with " + bc.getLabel() + " as the start node.");
                }
                else
                {
                // Get the user to set the initial properties - don't add the
                // species if the user cancels
                if ( !s.setPropertiesFromUser() )
                    {
                        return;
                    }
                    species_found.add(s);
                }
            }
            
            Iterator<Species> species_itr = species_found.iterator();
            while ( species_itr.hasNext() )
            {
                getContainingPanel().getTheGUI().getSpeciesPalette().addSpecies( species_itr.next() ); 
            }
        }
        else if ( action.getActionCommand().equals("Make Observable") )
	{   
                Vector<Rectangle> zones = findOperandZones();
		Vector<Pattern> patterns = makePatterns( zones );
                Vector<Operator> operators = getOperators();
                
                Group g = new Group( "", "", patterns, operators, getContainingPanel() );
               
                Group copy = null;
                
                try
                {
                    // Copy so the original patterns and operators arnt modified later
                    copy = (Group)WidgetCloner.clone( g );
                }
                catch ( Exception e )
                {
                    getContainingPanel().displayError("Error Cloning Group","The exception message was: " + e.getMessage() );
                    e.printStackTrace();
                    return;
                }
                
                getContainingPanel().getTheGUI().getObservablesPalette().addGroup( copy );
        }
        else if ( action.getActionCommand().equals("Make Rule") )
	    {
                if (debug_statements) System.out.println("Make Rule Called");
                
                
                
                Vector<Rectangle> zones = findOperandZones();
		Vector<Pattern> patterns = makePatterns( zones );
                Vector<Operator> operators = getOperators();
                
                
                if (debug_statements) System.out.println("There were " + zones.size() + " operands in this rule.");
                if (debug_statements) System.out.println("There were " + patterns.size() + " patterns in this rule.");
                if (debug_statements) System.out.println("There were " + operators.size() + " operators in this rule.");
                
                
                ReactionRule r = containing_panel.makeReactionRule( patterns, operators );
                if ( r == null )
                {
                    if (debug_statements) System.out.println("Reaction Rule creation was aborted");
                    return;
                }
                
                ReactionRule copy = null;
                
                if (debug_statements) System.out.println("ReactionRule has " + r.getProducts().size() + " products.");
                if (debug_statements) System.out.println("ReactionRule has " + r.getReactants().size() + " reactants.");
                if (debug_statements) System.out.println("ReactionRule has " + r.getResults().size() + " results.");
                if (debug_statements) System.out.println("ReactionRule has " + r.getOperators().size() + " operators.");
                
                try
                {
                    copy = (ReactionRule)WidgetCloner.clone( r );
                }
                catch ( Exception e )
                {
                    getContainingPanel().displayError("Error Cloning ReactionRule", "The exception error was: " + e.getMessage() );
                    e.printStackTrace();
                    return;
                }
                
                if ( copy.setPropertiesFromUser() == false )
                {
                    return;
                }
                
                /*
                if ( copy.isReversable() )
                {
                    if ( copy.getReverseRate().equals("") )
                    { 
                        copy.setPropertiesFromUser();
                    }
                }
                else if ( copy.getForwardRate().equals("") )
                {
                    copy.setPropertiesFromUser();
                 }
                 */
                
                if (debug_statements) System.out.println( "The Forward Reaction Rate is " + copy.getForwardRate() );
           
                ReactionRulePalette reactionrule_palette = containing_panel.getTheGUI().getReactionRulePalette();
                    
                
                //if ( 
               
                reactionrule_palette.addReactionRule( copy );
                
                // clean up
                
                //);
                
                /*
                
                Iterator c_itr = getPatterns().iterator();
                Iterator o_itr = operators.iterator();    
                
                    
                            while( c_itr.hasNext() )
                            {
                                containing_panel.removePattern( (Pattern) c_itr.next() );
                            }
                            
                    
                        while( o_itr.hasNext() )
                        {
                            containing_panel.removeOperator( (Operator)o_itr.next() );
                        }
                    
                  */  
                        // this selection boxes work is done so get rid of it
                        inUse( false ); 
                        reactionrule_palette.repaint();
                        containing_panel.repaint();
                    
                    
                }
                  
	    }

    public Vector<BioComponent> getComponents()
    {
	Vector<BioComponent> components = new Vector<BioComponent>();

	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();

		if ( widget instanceof BioComponent )
		    {
			components.add( (BioComponent)widget );
		    }
	    }
	
	return components;
    }

    Vector<Edge> getEdges()
    {
	Vector<Edge> edges = new Vector<Edge>();

	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();

		if ( widget instanceof Edge && !(widget instanceof AtomMap) )
		    {
			edges.add( (Edge)widget );
		    }
	    }
	
	return edges;
    }

    Vector<AtomMap> getMaps()
    {
	Vector<AtomMap> maps = new Vector<AtomMap>();

	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();

		if ( widget instanceof AtomMap )
		    {
			maps.add( (AtomMap)widget );
		    }
	    }
	
	return maps;
    }
    
    Vector<BioContainer> getContainers()
    {
	Vector<BioContainer> containers = new Vector<BioContainer>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof BioContainer )
		    {
			containers.add( (BioContainer)widget );
		    }
	    }
	
	return containers;
    }
    
    Vector<Pattern> getPatterns()
    {
	Vector<Pattern> patterns = new Vector<Pattern>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof Pattern )
		    {
			patterns.add( (Pattern)widget );
		    }
	    }
	
	return patterns;
    }

    Vector<Species> getSpecies()
    {
	Vector<Species> species = new Vector<Species>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof Species )
		    {
			species.add( (Species)widget );
		    }
	    }
	
	return species;
    }
    
    Vector<FlickrLabel> getLabels()
    {
	Vector<FlickrLabel> labels = new Vector<FlickrLabel>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof FlickrLabel )
		    {
			labels.add( (FlickrLabel)widget );
		    }
	    }
	
	return labels;
    }
    
    Vector<Group> getGroups()
    {
	Vector<Group> groups = new Vector<Group>();
	
	Iterator i = this.contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof Group )
		    {
			groups.add( (Group)widget );
		    }
	    }
	
	return groups;
    }
    
    /* from before patterns had to be explicitly created before rules
    Vector getPatterns()
    {
	Vector patterns = new Vector();
	Vector containers = new Vector();
        
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof BioContainer )
		    {
                        boolean processed_container = false;
                        BioContainer bc = (BioContainer)widget;
                        
                        Iterator p_itr = patterns.iterator();
                            while( p_itr.hasNext() )
                            {
                                Pattern p = (Pattern)p_itr.next();
                                
                                if ( p.getContainers().indexOf( bc ) != -1 )
                                {
                                    processed_container = true;
                                }
                                
                            }

                        if ( !processed_container )
                        {
                            if (bc.isValidPattern())
                            {
                                Pattern new_pattern = bc.getPattern();
                                patterns.add( new_pattern );
                            }
                            else
                            {
                                getContainingPanel().displayError( "Error Building Rule","The container \"" + bc.getLabel() + "\" is not part of a valid pattern.");
                                return null;
                            }
                        }
                }
        }
        
        
	return patterns;
    }
     */
    
    Vector<Operator> getOperators()
    {
	Vector<Operator> operators = new Vector<Operator>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof Operator )
		    {
			operators.add( (Operator)widget );
		    }
	    }
	
	return operators;
    }

    Vector<ReactionRule> getReactionRules()
    {
	Vector<ReactionRule> rules = new Vector<ReactionRule>();
	
	Iterator i = contents.iterator();
	while ( i.hasNext() )
	    {
		Widget widget = (Widget)i.next();
		
		if ( widget instanceof ReactionRule )
		    {
			rules.add( (ReactionRule)widget );
		    }
	    }
	
	return rules;
    }

    
    public void assignID() 
    {
        super.assignID();
        
        Iterator widget_itr = getContents().iterator();
        while ( widget_itr.hasNext() )
        {
            Widget current = (Widget)widget_itr.next();
            if ( current instanceof BioComponent )
            {
                ((BioComponent)current).assignID();
            }
            else if ( current instanceof BioContainer )
            {
                ((BioContainer)current).assignID();
            }
            else if ( current instanceof Operator )
            {
                ((Operator)current).assignID();
            }   
            else if ( current instanceof BioGraph )
            {
                ((BioGraph)current).assignID();
            }   
            else if ( current instanceof Edge )
            {
                ((Edge)current).assignID();
            }   
            else
            {
                if (debug_statements) System.out.println("Error while assigning IDs,"+"Unhandled widget type: " + current.getClass().getName() 
                + "Contact support at support@bionetgen.com");
            }
                
        }
        
    }    

    public boolean releaseContents() 
    {
        WidgetPanel panel = getContainingPanel();
        
        Vector<Widget> c_vec = getContents();
        Iterator<Widget> itr = c_vec.iterator();
        while ( itr.hasNext() )
        {
            Widget w = itr.next();
            
            if ( w instanceof BioComponent )
            {
                BioComponent bc = (BioComponent)w;
                Vector<Edge> remove_list = new Vector();
                Iterator<Edge> edge_itr = bc.getEdges().iterator();
                while ( edge_itr.hasNext() )
                {
                    Edge e = edge_itr.next();
                    if ( c_vec.indexOf( e.getStart() ) == -1 || c_vec.indexOf( e.getEnd() ) == -1 )
                    {
                       remove_list.add( e );
                    }
                }
                
                Iterator<Edge> remove_itr = remove_list.iterator();
                while ( remove_itr.hasNext() )
                {
                    bc.removeEdge( remove_itr.next() );
                }
            }
            
            if ( w instanceof Edge )
            {
                Edge e = (Edge)w;
                if ( c_vec.indexOf( e.getStart() ) == -1 || c_vec.indexOf( e.getEnd() ) == -1 )
                {
                    e.getStart().removeEdge( e );
                    e.getEnd().removeEdge( e );
                    continue;
                }
            }
            
            panel.addWidget( w );
        }
        
        //Iterator comp_itr = getComponents().iterator();
        //while( comp_itr.hasNext() )
        //{
        //    panel.addComponent( (BioComponent)comp_itr.next() );
        //}
        
        /*
        Iterator i = getContents().iterator();
                            while ( i.hasNext() ) {
                                Widget widget = (Widget)i.next();
                                
                                if ( widget instanceof BioComponent ) 
                                {
                                    BioComponent bcomp = (BioComponent)widget;
                                    // Check for dangling edges
                                    if (debug_statements) System.out.println("Selection Box Release: considering component with " + bcomp.getEdges().size() + " edges.");
                                    
                                    Vector<Edge> edge_remove_list = new Vector<Edge>();
                                    
                                    Iterator<Edge> edge_itr = bcomp.getEdges().iterator();
                                    while( edge_itr.hasNext() )
                                    {
                                        Edge edge = edge_itr.next();
                                        
                                        if (debug_statements) System.out.println("Considering Edge with label " + edge.getLabel() );
                                     
                                        if ( getContents().indexOf( edge ) == -1 )
                                        {
                                            if (debug_statements) System.out.println("Disconnecting edge from component because the edge is not in the selection box.");
                                            edge_remove_list.add( edge );
                                        }
                                        
                                        if ( getContents().indexOf( edge.getStart() ) != -1 || getContents().indexOf( edge.getEnd() ) != -1 )
                                        {
                                            if (debug_statements) System.out.println("Disconnecting edge from component because both end points were not in the selection box.");
                                            edge_remove_list.add( edge );
                                        }
                                    }
                                    
                                    // Remove edges here to avoid concurrent access problems
                                    
                                    // Removing the edges causes all the biocomponents to lose their edge pointers
                                    // and does not seem to be necessary since the dangling edges are not added to 
                                    // the selection box anyway.
                                    //if (debug_statements) System.out.println("Removing " + edge_remove_list.size() + " edges.");
                                    //Iterator<Edge> remove_itr = edge_remove_list.iterator();
                                    //while ( remove_itr.hasNext() )
                                    //{
                                        //bcomp.removeEdge(remove_itr.next());
                                    //}
                                    
                                    // Disconnect from BioContainers that were not also picked up by the selection box
                                    boolean container_included = false;
                                    
                                    if ( bcomp.getContainer() != null )
                                    {
                                        Iterator itr = getContainers().iterator();
                                        while ( itr.hasNext() )
                                        {
                                            if ( itr.next() == bcomp.getContainer() )
                                            {
                                                container_included = true;
                                                break;
                                            }
                                        }
                                        
                                        if ( !container_included )
                                        {
                                            
                                            BioContainer con = bcomp.getContainer();
                                            con.removeComponent( bcomp );
                                            bcomp.setContainer( null );
                                            
                                        }
                                    }
                                    
                                    if (debug_statements) System.out.println("SelectionBox:releasing component with " + bcomp.getEdges().size() + " edges.");
                                    panel.getAllComponents().add( bcomp );
                                }
                                else if ( widget instanceof BioContainer ) {
                                    panel.getAllContainers().add( (BioContainer)widget );
                                }
                                // The partial interchangability of Maps and Edges is 
                                // going to cause confusion later
                                else if ( widget instanceof AtomMap ) 
                                {
                                    AtomMap map = (AtomMap)widget;
                                    
                                    // Only add the map to the panel if both its components
                                    // were selected, otherwise remove this map from its
                                    // adjacent components so there are no invisible, invalid maps
                                    // hanging arround
                                    if ( getContents().indexOf( map.getStart() ) != -1
                                    && getContents().indexOf( map.getEnd() ) != -1 ) 
                                    {
                                        panel.addEdge( map );
                                    }
                                    else 
                                    {
                                        map.getStart().removeEdge(map);
                                        map.getEnd().removeEdge(map);
                                    }
                                }
                                else if ( widget instanceof Edge && !(widget instanceof AtomMap) ) 
                                {
                                    Edge edge = (Edge)widget;
                                    
                                    // Only add the edge to the panel if both its components
                                    // were selected, otherwise remove this edge from its
                                    // adjacent components so there are no invisible, invalid edges
                                    // hanging arround
                                    if ( getContents().indexOf( edge.getStart() ) != -1
                                    && getContents().indexOf( edge.getEnd() ) != -1 ) 
                                    {
                                        panel.addEdge( edge );
                                    }
                                    else 
                                    {
                                         if (debug_statements) System.out.println("Not releasing edge from selection box because both end points were not in included.");
                                       
                                        edge.getStart().removeEdge(edge);
                                        edge.getEnd().removeEdge(edge);
                                    }
                                }
                                else if ( widget instanceof Species ) {
                                    panel.getAllSpecies().add( (Species)widget );
                                }
                                else if ( widget instanceof Operator ) {
                                    panel.getAllOperators().add( (Operator)widget );
                                }
                                else if ( widget instanceof ReactionRule ) {
                                    panel.getAllReactionRules().add( (ReactionRule)widget );
                                }
                                else if ( widget instanceof Pattern ) {
                                    panel.getAllPatterns().add( (Pattern)widget );
                                }
                                else if ( widget instanceof Group ) {
                                    panel.getAllGroups().add( (Group)widget );
                                }
                                else if ( widget instanceof FlickrLabel ) {
                                    panel.getAllFlickrLabels().add( (FlickrLabel)widget );
                                }
                                else {
                                    panel.displayError("Selection Box Error",
                                    "Unknown widget subtype encountered in SelectionBox:releaseContents. Contact support at support@bionetgen.com");
                                    return false;
                                }
                            }
         
         */
        
        return true;
    }
    
    public boolean isInUse() 
    {
        return in_use;
    }
    
    public Vector<Pattern> makePatterns( Vector<Rectangle> zones ) 
    {
        // Create pattern zones (areas between operators and the selection box edges)
        
        if (debug_statements) System.out.println("++++++++++++++++++++++++");
        if (debug_statements) System.out.println("+ Make Patterns Called +");
        if (debug_statements) System.out.println("++++++++++++++++++++++++");
        if (debug_statements) System.out.println("Looking at " + zones.size() + " zones" );
        
        Vector<Pattern> patterns = new Vector<Pattern>(); // the end result
        
        
        Iterator zone_itr = zones.iterator();
        while( zone_itr.hasNext() )
        {
            if (debug_statements) System.out.println("******** zone ************");
        Rectangle current_zone = (Rectangle)zone_itr.next();
        
        // Find all the containers in this zone
        
        // Data structure to hold all the containers that are in this zone
        Vector<BioContainer> zone_conts = new Vector<BioContainer>();
        
        Vector<BioContainer> conts = getContainers();
        
        Iterator species_itr = getSpecies().iterator();
        while( species_itr.hasNext() )
        {
            Species s = (Species)species_itr.next();
            conts.addAll( s.getContainers() );
        }
        
        Iterator container_itr = conts.iterator();
        while ( container_itr.hasNext() )
        {
            BioContainer current_container = (BioContainer)container_itr.next();
            int x = current_container.getX();
            int y = current_container.getY();
            int width = current_container.getWidth();
            int height = current_container.getHeight();
            
            if (debug_statements) System.out.println( "--------Contains check---------");
            if (debug_statements) System.out.println( "current_container.x: " + current_container.getX() );
            if (debug_statements) System.out.println( "current_container.y: " + current_container.getY() );
            if (debug_statements) System.out.println( "current_container.width: " + current_container.getWidth() );
            if (debug_statements) System.out.println( "current_container.height: " + current_container.getHeight() );
            if (debug_statements) System.out.println( "current_zone.x: " + current_zone.getX() );
            if (debug_statements) System.out.println( "current_zone.y: " + current_zone.getY() );
            if (debug_statements) System.out.println( "current_zone.width: " + current_zone.getWidth() );
            if (debug_statements) System.out.println( "current_zone.height: " + current_zone.getHeight() );
            
            
            if ( current_zone.contains(x, y, width, height) )
            {
                if (debug_statements) System.out.println( "+current_container added to current_zone" );
                
                try
                {
                    zone_conts.add( current_container );
            
                }
                catch ( Exception e )
                {
                    getContainingPanel().displayError("Rule Creation Error", "Error cloning Container.");
                    return new Vector<Pattern>();
                }
            }
            else
            {
                if (debug_statements) System.out.println( "+current_container not added to current_zone" );
            }
        }
        // Now we have a vector of containers that fall into this operand zone
        
        if ( zone_conts.isEmpty() )
        {
        
            getContainingPanel().displayError("Rule Creation Error", "Not enough operands could be found.");
            return new Vector<Pattern>();
        }
        
        try
        {
            Pattern p = getContainingPanel().makePattern( zone_conts );
            
            Iterator p_itr = p.getContainers().iterator();
            while( p_itr.hasNext() )
            {
                BioContainer bc = (BioContainer)p_itr.next();
                if ( !bc.isValidPattern() )
                {
                    getContainingPanel().displayError("Error Creating Rule","Not all the containers are valid patterns.");
                    return new Vector<Pattern>();
                }
            }
            //p = (Pattern)BioGraphCloner.clone( p );
            
            patterns.add(p);
            
        }
        catch (Exception e)
        {
            getContainingPanel().displayError("Exception in SelectionBox:ActionPerformed()","The message was: " + e.getMessage() );
            e.printStackTrace();
        }
        
        }
        
        return patterns;
    }
  
    
    private Vector<Rectangle> findOperandZones() 
    {
        Vector<Rectangle> zones = new Vector<Rectangle>();
        
        if ( getOperators() == null )
        {
            return zones; 
        }
        
        
        // If there are no operators use the whole selection box
        if ( getOperators().isEmpty() )
        {
            zones.add( new Rectangle( getX(), getY(), getWidth(), getHeight() ) );
            return zones; 
        }
   
        
        int left_edge = getX();
        int right_edge = getX()+getWidth();
        int previous_edge = left_edge;
        int next_edge = 0;
        int top_edge = getY();
        int bottom_edge = top_edge+getHeight();
        
        
        Rectangle current_rectangle = null;
        
        // Use a simple sort algorithm to sort operatators by x coord
        // there should never be enough operators to make us worry about speed
        
        Vector<Operator> sorted_operators = new Vector<Operator>();
        Vector<Operator> unsorted_operators = new Vector<Operator>();
        unsorted_operators.addAll( getOperators() ); 
        
        // Find the smallest element and put it in the first postion, find the next smallest
        // and put it in the next position, repeat (smallest meaning least x coord)
        
        int num_items_to_sort = unsorted_operators.size();
        if (debug_statements) System.out.println("Sorting " + num_items_to_sort + " operators.");
        
        for ( int i = 0; i < num_items_to_sort; i++ )
        {
            Operator smallest = (Operator)unsorted_operators.get(0);
            Iterator opers_itr = unsorted_operators.iterator();
            while ( opers_itr.hasNext() )
            {
                Operator current_operator = (Operator)opers_itr.next();
                if ( current_operator.getX() < smallest.getX() )
                 {
                    smallest = current_operator;
                }
            }
        
            unsorted_operators.remove( smallest );
            sorted_operators.add( smallest );
            
            if (debug_statements) System.out.println("Sort: added operator with x = " + smallest.getX() + " to sorted list");
        }
        
        Iterator ops_itr = sorted_operators.iterator();
        while ( ops_itr.hasNext() )
        {
            Operator current_op = ((Operator)ops_itr.next());
            next_edge = current_op.getX()+current_op.getWidth()/2;
            current_rectangle = new Rectangle(previous_edge, top_edge, next_edge-previous_edge, getHeight() );
            zones.add( current_rectangle );
            previous_edge = next_edge;
        
        }
        
            next_edge = right_edge;
            current_rectangle = new Rectangle(previous_edge, top_edge, next_edge-previous_edge, getHeight() );
            zones.add( current_rectangle );
            previous_edge = next_edge;
 
            return zones;
    }
    
   
    public void paint(int x_origin, int y_origin, Graphics2D g2d) 
    {
        if (debug_statements) System.out.println("Selection Box Painted");
        
	g2d.setColor(Color.blue);
	
        //Graphics2D g2d = (Graphics2D)g;
	((Graphics2D)g2d).setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	
                                     g2d.drawRect( x, y, width, height );
	
        Iterator widget_itr = getContents().iterator();
        while ( widget_itr.hasNext() )
        {
            Widget wid = (Widget)widget_itr.next();
            
            if ( wid instanceof Edge )
            {
                Edge edge = (Edge)wid;
                if ( getContents().indexOf( edge.getStart() ) != -1
                                    && getContents().indexOf( edge.getEnd() ) != -1 ) {
                                        edge.paint( x_origin, y_origin, g2d );
                                    }
                                    
            }
            else
            {
                wid.paint( x_origin, y_origin, g2d );
            }
            
        }
         
    }
    
    public boolean in_use = false;
    public Point start_resize_drag;
    public int original_height;
    public int original_width;
    //private int x_offset;
    //private int y_offset;

    // Remember the last mouse position so we know how far to move when dragged
    public int start_drag_x;
    public int start_drag_y;

    // Parameter values need to point to the parameter in the model parameters table
    // so changes there show up everywhere. Cloning breaks this so we need a relink method
    // that the cloner calls.
    public void relinkParameters() 
    {
        Iterator<Operator> op_itr = this.getOperators().iterator();
        while ( op_itr.hasNext() )
        {
            Operator op = op_itr.next();
            if ( op instanceof Forward )
            {
               ((Forward)op).relinkParameter();
            }
            if ( op instanceof ForwardAndReverse )
            {
               ((ForwardAndReverse)op).relinkParameter();
            }
        }
        
        Iterator<Species> s_itr = this.getSpecies().iterator();
        while ( s_itr.hasNext() )
        {
            Species s = s_itr.next();
            if ( s instanceof Species )
            {
                ((Species)s).relinkParameter();
            }
        }
        
        Iterator<ReactionRule> r_itr = getReactionRules().iterator();
        while ( r_itr.hasNext() )
        {
            Operator op = ((ReactionRule)r_itr.next()).getProductionOperator();
            if ( op instanceof ForwardAndReverse )
            {
              ((ForwardAndReverse)op).relinkParameter();
            }
            else if ( op instanceof Forward )
            {
              ((Forward)op).relinkParameter();
            }
        }
    }

    public void deleteContents() 
    {
        WidgetPanel wp = getContainingPanel();
        Iterator<Widget> itr = getContents().iterator();
        while ( itr.hasNext() ) 
        {
            
            Widget w = itr.next();
            wp.removeWidget( w );
        
            if ( debug_statements ) System.out.println("Deleting widget " + w);
        /*
                if ( the_selection_box.getContents().get(i) instanceof BioContainer ) {
                    if (debug_statements) System.out.println(" a container");
                    containers.remove( the_selection_box.getContents().get(i) );
                }
                // Now that Components are managed by the containers and the WidgetPanel
                // be careful not to remove the same Component twice
                else if ( the_selection_box.getContents().get(i) instanceof BioComponent ) {
                    if (debug_statements) System.out.println(" a component");
                    BioComponent temp = (BioComponent) the_selection_box.getContents().get(i);
                    components.remove(temp);
                    if ( temp.getContainer() != null ) {
                        temp.getContainer().removeComponent(temp);
                    }
                    temp.removeAllEdges();
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof Operator ) {
                    if (debug_statements) System.out.println(" an operator");
                    Operator temp = (Operator) the_selection_box.getContents().get(i);
                    removeOperator(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof Species ) {
                    if (debug_statements) System.out.println(" a species");
                    Species temp = (Species) the_selection_box.getContents().get(i);
                    removeSpecies(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof Edge ) {
                    if (debug_statements) System.out.println(" an edge");
                    Edge temp = (Edge) the_selection_box.getContents().get(i);
                    removeEdge(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof Pattern ) {
                    if (debug_statements) System.out.println(" a pattern");
                    Pattern temp = (Pattern) the_selection_box.getContents().get(i);
                    removePattern(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof ReactionRule ) {
                    if (debug_statements) System.out.println(" a reactionrule");
                    ReactionRule temp = (ReactionRule) the_selection_box.getContents().get(i);
                    removeReactionRule(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof Group ) {
                    if (debug_statements) System.out.println(" a group");
                    Group temp = (Group) the_selection_box.getContents().get(i);
                    removeGroup(temp);
                    
                }
                else if ( the_selection_box.getContents().get(i) instanceof FlickrLabel ) {
                    if (debug_statements) System.out.println(" a label");
                    FlickrLabel temp = (FlickrLabel) the_selection_box.getContents().get(i);
                    removeFlickrLabel(temp);
                }
                the_selection_box.deleteContents();
                }
                else {
                    displayError("Fatal Error", "Unknown Widget subclass \"" 
                    + the_selection_box.getContents().get(i).getClass().getName()
                    + "\"encountered in WidgetPanel:actionPerformed(). Contact support at support@bionetgen.com");
                    return false;
                }
                
             
            }
               the_selection_box.inUse( false );
               repaint();
          */ 
        }
        
        contents.removeAllElements();
    }

    public void resetOffsets() 
    {
        calculatePointerOffset( getX(), getY() );
    }
	

}
