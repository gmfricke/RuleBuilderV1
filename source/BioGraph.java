import java.util.*; //For vector data structure
import java.awt.*; // For JComponent
import javax.swing.*; // For graphical interface tools

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows

import java.awt.event.*;

import java.awt.image.BufferedImage;

public class BioGraph extends Widget implements ActionListener
{
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    Vector<BioContainer> containers = new Vector<BioContainer>();
    
    
    private boolean locked = true;
    
    BioGraph(){}
    
    BioGraph(int x, int y, String label, String image_url, boolean template, WidgetPanel containing_panel)
    {
	this.containing_panel = containing_panel;
        
        int s_red = 0;
        int us_red = 15;
        int s_blue = 255;
        int us_blue = 0;
        int s_green = 0;
        int us_green = 0;
        int s_alpha = 150;
        int us_alpha = 150;
        
        Color sel_color = new Color( s_red, s_green, s_blue, s_alpha );
        Color unsel_color = new Color( us_red, us_green, us_blue, us_alpha );
        
        setLabel( label );
        
        setSelectedColor( sel_color );
        setUnselectedColor( unsel_color );
    }

    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Disband") )
	{
            if ( getContainingPanel().displayQuestion("Disband Species","Disbanding this BioGraph will" +
            " replace it with its constituent containers, edges, and components. " +
            "If this is a species the concentration data will be lost.") )
            {
                // Edges must be added to the panel after the biograph is
                // removed because removing the biograph removes all edges
                // it contains from the panel
                getContainingPanel().removeBioGraph( this );
                getContainingPanel().getAllComponents().addAll( getComponents() );
                getContainingPanel().getAllContainers().addAll( getContainers() );
                getContainingPanel().getAllEdges().addAll( getEdges() );
                getContainingPanel().repaint();
            }
            
        }
        else if ( action.getActionCommand().equals("Lock") )
	{
            setLocked( true );
        }
        else if ( action.getActionCommand().equals("Unlock") )
        {
            // Edges must be added to the panel after the biograph is
                // removed because removing the biograph removes all edges
                // it contains from the panel
            getContainingPanel().removeBioGraph( this );
            getContainingPanel().getAllComponents().addAll( getComponents() );
            getContainingPanel().getAllContainers().addAll( getContainers() );
            getContainingPanel().getAllEdges().addAll( getEdges() );
            getContainingPanel().repaint();
            setLocked( false );
        }
         
        else
        {
            getContainingPanel().displayError("Internal Error","Unknown action command \"" + action.getActionCommand() + "\" in Species::actionPerformed(). Contact support at support@bionetgen.com." );
        }
    }
    
    public void removeComponent( BioComponent com )
    {
        // First find which container the component is in
        Iterator con_itr = containers.iterator();
        while( con_itr.hasNext() )
        {
            BioContainer current = (BioContainer)con_itr.next();
            
            int index = current.getComponents().indexOf( com );
            if ( index != -1 )
            {
                current.removeComponent( com );
            }
   
            // don't stop looking when we remove one - just in case there
            // are other instances due to bugs
            // SHOULD AT LEAST DISPLAY THAT THIS CONDITION WAS DETECTED
        }
    }
    
    public void removeEdge( Edge edge )
    {
        // First find which container the edge is in
        Iterator con_itr = containers.iterator();
        while( con_itr.hasNext() )
        {
            BioContainer current = (BioContainer)con_itr.next();
            
            int index = current.getEdges().indexOf( edge );
            if ( index != -1 )
            {
                current.removeEdge( edge );
            }
   
            // don't stop looking when we remove one - just in case there
            // are other instances due to bugs
            // SHOULD AT LEAST DISPLAY THAT THIS CONDITION WAS DETECTED
        }
    }
    
    boolean addContainer( BioContainer c )
    {
        
        if ( containers.indexOf( c ) != -1 ) 
	    {
		return false;
	    }

        //c.setSpecies( this );
        c.setSelectedColor( getSelectedColor() );
        c.setUnselectedColor( getUnselectedColor() );
        c.setBioGraph( this );
        c.resetOffsets();
	containers.add( c );
        
        setX( calculateX() );
        setY( calculateY() );
        setWidth( calculateWidth() );
        setHeight( calculateHeight() );

        return true;
    }

    void removeContainer(BioContainer container)
    {
	containers.remove( container );
        setX( calculateX() );
        setY( calculateY() );
        setWidth( calculateWidth() );
        setHeight( calculateHeight() );
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
	
        getFlickrLabel().display( c, g2d );
        
        Iterator e_itr = getEdges().iterator();
        while( e_itr.hasNext() )
	    {
		Edge next_edge = ((Edge)e_itr.next());
                next_edge.display(c, g2d);
	    } 
          
        Iterator c_itr = containers.iterator();
	
        while( c_itr.hasNext() )
	    {
		//	((Container)(containers.get(i))).setColor(Color.red);
		BioContainer next_container = ((BioContainer)c_itr.next());
                next_container.display(c, g2d);
	    }     
        
	// Draw bounding box for debugging
	/*
        g2d.setColor(Color.red);
	BasicStroke default_stroke = new BasicStroke(2.0f);
	BasicStroke dashed_stroke = new BasicStroke(1f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1f,new float[] {2f},0f);
	
	Stroke def = g2d.getStroke();
	g2d.setStroke ( dashed_stroke );
	g2d.drawRect( getX(), getY(), getWidth(), getHeight() );
	g2d.setStroke( def );
        g2d.drawString(getX()+","+getY(), getX(), getY());
        g2d.drawString((getX()+getWidth())+","+(getY()+getHeight()), getX()+getWidth(), getY()+getHeight());
         */
    }

    /**
     *
     * @return
     */    
    public int calculateX()
    {
	// calculate x
	// x is the left-most x and y is the up-most y of all components and containers
	
        int left_most_x = 1000000;
        
        // This doesn't always work because the panel can be smaller than the 
        //  species - causing bugs:
        
        //if ( containing_panel != null )
        //    if ( containing_panel.panel_dimension != null )
        //    {
        //        left_most_x = (int)containing_panel.panel_dimension.getWidth();
        //    }
        
	
	for ( int i = 0; i < containers.size(); i++ )
	    {
                int current_x = ((BioContainer)containers.get(i)).getX();
                //if (debug_statements) System.out.println("Calculating Container x coord: looking at container with x coord: " + current_x );
                
		if ( current_x < left_most_x )
		    {
             
			left_most_x = current_x;
		    }
	    }

	return left_most_x;
    }
    
    /**
     *
     * @return
     */    
    public int calculateY()
    {
	// calculate Y
	// x is the left-most x and y is the up-most y of all components and containers
	
        int up_most_y = 100000;
	
        // This doesn't always work because the panel can be smaller than the 
        //  species - causing bugs:
       
        //if ( containing_panel != null )
        //     if ( containing_panel.panel_dimension != null )
        //        {
        //            up_most_y = (int)containing_panel.panel_dimension.getHeight();
        //        }
        
	
	for ( int i = 0; i < containers.size(); i++ )
	    {
		if ( ((BioContainer)containers.get(i)).getY() < up_most_y )
		    {
			up_most_y = ((BioContainer)containers.get(i)).getY();
		    }
	    }

	return up_most_y;
    }

    /**
     *
     * @return
     */    
    public int calculateHeight()
    {
	// calculate Height
	// width is the right-most x - getX() and y is the down-most y - getY() of all components and containers
	
	int down_most_y = 0;
	
	for ( int i = 0; i < containers.size(); i++ )
	    {
		BioContainer cc = (BioContainer)containers.get(i);
		if ( cc.getY()+cc.getHeight() > down_most_y )
		    {
			down_most_y = cc.getY()+cc.getHeight();
		    }
	    }
	
	return down_most_y - getY();
    }
    
    
    /**
     *
     * @return
     */    
    public int calculateWidth()
    {
	// calculate Height
	// width is the right-most x - getX() and y is the down-most y - getY() of all components and containers
	
	int right_most_x = 0;
	
	
	for ( int i = 0; i < containers.size(); i++ )
	    {
		BioContainer cc = (BioContainer)containers.get(i);
		if ( cc.getX()+cc.getWidth() > right_most_x )
		    {
                        
			right_most_x = cc.getX()+cc.getWidth();
		    }
	    }
	
	return right_most_x - getX();
    }

    public int getX()
    {
        calculateX();
        return x;
    }
    
    public int getY()
    {
        calculateY();
        return y;
    }
    
    public int getWidth()
    {
        calculateWidth();
        return width;
    }
    
    public int getHeight()
    {
        calculateHeight();
        return height;
    }

// this function calculates the pointer offset so that the container moves RELATIVE to the pointer's
    // (x,y) when dragged - not TO the pointers (x,y)
    /**
     *
     * @param mouse_x
     * @param mouse_y
     */    
    public void calculatePointerOffset( int mouse_x, int mouse_y )
    {
        x_offset = mouse_x - getX();
	y_offset = mouse_y - getY();
        
	// Containers need to calculate their own offsets in order to display properly
	for ( int i = 0; i < containers.size(); i++ )
	    {
		//((BioContainer)containers.get(i)).calculatePointerOffset( mouse_x, mouse_y ); 
                ((BioContainer)containers.get(i)).calculatePointerOffset( getX(), getY() ); 
        }
    }

    // Overrides Widget::updateLocation( x, y )
    /**
     *
     * @param mouse_x
     * @param mouse_y
     * @param confine_to_boundaries
     */    
    public void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
    {
        
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
                // Need to figure out the new panel boundaries after translation and zooming by undoing the zoom
                // and translation transformations
                // The formula is basically (old_panel_width(or height) - translation)/zoom_factor
                panel_width = (int)((ct.getBaseDimension().getWidth()-ct.getZoomedXTranslation())/ct.getZoom());
                panel_height = (int)((ct.getBaseDimension().getHeight()-ct.getZoomedYTranslation())/ct.getZoom());
                panel_x = (int)(-ct.getZoomedXTranslation()/ct.getZoom());
                panel_y = (int)(-ct.getZoomedYTranslation()/ct.getZoom());
            }
            catch ( NullPointerException e )
            {
                if ( getContainingPanel() == null ) if (debug_statements) System.out.println("Containing Panel is a NULL pointer.");
                if ( getContainingPanel().panel_dimension == null ) if (debug_statements) System.out.println("Containing Panel's dimension is a NULL pointer.");
                
                e.printStackTrace();
            }
            
            
            if (debug_statements) System.out.println("BioGraph: Panel width: " + panel_width );
            if (debug_statements) System.out.println("BioGraph: width: " + getWidth() );
            if (debug_statements) System.out.println("BioGraph: mouse_x: " + mouse_x );
            if (debug_statements) System.out.println("BioGraph: x_offset: " + x_offset );
            if (debug_statements) System.out.println("BioGraph: new_x: " + new_x );
            if (debug_statements) System.out.println("BioGraph: Proposed x+width: " + (new_x+getWidth()) );
            
	    boolean x_bound = false;
	    boolean y_bound = false;
	                
            // Check for window boundary collision
	    if ( confine_to_boundaries )
		{
		    if(new_x+getWidth()>panel_width)
			{
			    new_x = (int)panel_width-getWidth()-5;
			    x_bound = true;
			}
		    if(new_x < panel_x)
			{
			    new_x = panel_x;
			    x_bound = true;
			}
		    if((new_y+getHeight())>panel_height)
			{
			    new_y = (int)panel_height-getHeight()-10;
			    y_bound = true;
			}
		    if(new_y < panel_y)
			{
			    y_bound = true;
			    new_y = panel_y;
			}
		}
            
            if (debug_statements) System.out.println("Settled x+width: " + (new_x+getWidth()) );
            
            //if ( detectContainerCollision( new_x, new_y ) 
            //    || detectContainerCollision( getX(), getY() ) )
            //{
            //    return;
            //}
            //if ( detectContainerCollision( new_x, new_y ) )
            //{
            //    return;
            //}
         
	    
        if ( x_bound || y_bound )
        {
            if (debug_statements) System.out.println("BioGraph collided with panel boundary.");
        }
          
         */
        
        super.updateLocation( mouse_x, mouse_y, confine_to_boundaries );
        
        // x and y set by super.updateLocation
        int new_x = x;
        int new_y = y;
        
        Iterator itr = containers.iterator();
        while( itr.hasNext() )
        {
            BioContainer current_bc = ((BioContainer)itr.next());
            current_bc.updateLocation( new_x, new_y, confine_to_boundaries ); 
        }
        
        if (debug_statements) System.out.println("Species now at " + getX() + "," + getY() );
        setX( calculateX() );
        setY( calculateY() );
        setWidth( calculateWidth() );
        setHeight( calculateHeight() );
    }
    

    /**
     *
     * @return
     */    
    public Vector<BioContainer> getContainers()
    {
	return containers;
    }

    /**
     *
     * @return
     */    
    public Vector<BioComponent> getComponents()
    {
        Vector<BioComponent> components = new Vector<BioComponent>();
        
	Iterator c_itr = containers.iterator();
        while( c_itr.hasNext() )
        {
           components.addAll(((BioContainer) c_itr.next()).getComponents());
        }
        
        return components;
    }
    
    /**
     *
     * @return
     */    
    Vector<Edge> getEdges()
    {
    	Vector<Edge> edges = new Vector<Edge>();

	// Gather edges from all containers in this species
        Iterator c_itr = containers.iterator();
        while ( c_itr.hasNext() )
	    {
		BioContainer bc = (BioContainer)c_itr.next();
		
                Iterator e_itr = bc.getEdges().iterator();
		while ( e_itr.hasNext() )
                {
			Edge e = (Edge)e_itr.next();
                        
                        // Avoid adding duplicate edges
			if ( edges.indexOf(e) == -1 )
                        {
                            edges.add(e);
                        }
		    }
	    }

	return edges;
    }

    /**
     *
     * @param cp
     */    
    public void setContainingPanel( WidgetPanel cp )
    {
	this.containing_panel = cp;
        this.label.setContainingPanel( cp );
        
	for ( int i = 0; i < containers.size(); i++ )
	    {
		((BioContainer)containers.get(i)).setContainingPanel( cp );
	    }
    }

    /*
    // Does this species "match" the targer species s. Matching means that some component or container
    // is the same in this species as in s. Components are defined by their label(????), state, and binding_state.
    // Containers are defined by the number of components contained and their (complete) states
    // Returns a vector of matching components
    Vector matches( Species s )
    {
	Vector matches = new Vector();

	for ( int i = 0; i < components.size(); i++ )
	    {
		for ( int j = 0; j < s.getComponents().size(); j++ )
		    {
			BioComponent this_component = (BioComponent)components.get(i);
			BioComponent target_component = (BioComponent)s.getComponents().get(i);
			
			if ( this_component.getLabel() == target_component.getLabel() //?????
			     && (this_component.getBindingState() == target_component.getBindingState()
				 || this_component.getBindingState() == "unspecified"
				 || target_component.getBindingState() == "unspecified")
			     && (this_component.getState() == target_component.getState()
				 || this_component.getState() == "unspecified"
				 || target_component.getState() == "unspecified"))
			    {
				matches.add(this_component);
			    }
		    }
	    }
	
	return matches;
    }
*/
    
    // Overwrites contains functions inherited from Widget
    /**
     *
     * @param e
     * @return
     */    
    //public boolean contains( MouseEvent e )
 //   {
//	return contains( e.getX(), e.getY() );
    //}
    
    
    // The species contains the x,y coordinate if any of its constituents do
    /**
     *
     * @param mouse_x
     * @param mouse_y
     * @return
     */    
    
    // Base class contains is better
    /*
    boolean contains(int mouse_x, int mouse_y)
    {
        
	Iterator i = containers.iterator();
	Iterator j = components.iterator();
	Iterator k = edges.iterator();

	while ( i.hasNext() )
	    {
		if ( ((BioContainer)i.next()).contains( mouse_x, mouse_y ))
		    {
			return true;
		    }
	    }

	while ( j.hasNext() )
	    {
		if ( ((BioComponent)j.next()).contains( mouse_x, mouse_y ))
		    {
			return true;
		    }
	    }

	while ( k.hasNext() )
	    {
		if ( ((Edge)k.next()).contains( mouse_x, mouse_y ))
		    {
			return true;
		    }
	    }

	return false;
        
    }
     */

    // Handle actions for which this object is a registered listener
    /**
     *
     * @param action
     */    
    // Override default widget action performed method
    

    public void setSelected( boolean new_selection_state )
    {
        // Set the Species selected state using the Widget setSelected method
        super.setSelected(new_selection_state);
        
        // Tell elements of this species about the selection state
        Iterator i = containers.iterator();
	while ( i.hasNext() )
	    {
		((BioContainer)i.next()).setSelected(selected);
	    }
    }
    
    public void setSelectedColor( Color new_color )
    {
        // Set the Species selected state using the Widget setSelected method
        super.setSelectedColor( new_color );
        
        // Tell elements of this species about the selection state
        Iterator i = containers.iterator();
	while ( i.hasNext() )
	    {
		((BioContainer)i.next()).setSelectedColor( new_color );
	    }
    }
    
    public void setUnselectedColor( Color new_color )
    {
        // Set the Species selected state using the Widget setSelected method
        super.setUnselectedColor( new_color );
        
        // Tell elements of this species about the selection state
        Iterator i = containers.iterator();
	while ( i.hasNext() )
	    {
		((BioContainer)i.next()).setUnselectedColor( new_color );
	    }
    }
    
    public Rectangle getBoundingBox() 
    {
        return new Rectangle( getX(), getY(), getWidth(), getHeight() );
    }
    
    public boolean contains(MouseEvent e)
	{
	    return contains( e.getX(), e.getY() );
	}

 public boolean contains(int mouse_x, int mouse_y )
	{
            //return mouse_x > getX() && mouse_x < getX() + getWidth() && mouse_y > getY() && mouse_y < getY() + getHeight();
            
            Iterator edge_itr = getEdges().iterator();
            while ( edge_itr.hasNext() )
            {
                if ( ((Edge)edge_itr.next()).contains( mouse_x, mouse_y ) )
                {
                    return true;
                }
            }
            
            Iterator container_itr = getContainers().iterator();
            
            while ( container_itr.hasNext() )
            {
                if ( ((BioContainer)container_itr.next()).contains( mouse_x, mouse_y ) )
                {
                    return true;
                }
            }
            
            return false;
 	}

// Does this widgets bounding_box entirely contain the bounding box of the target widget?
boolean contains( Widget w )
	{
	    return getBoundingBox().contains( w.getBoundingBox() );
	}

void setContainers( Vector<BioContainer> c )
{
    containers = c;
}


// Isomorphic graphs will have the same unlabeled adjacency matrix
    // with the columns and rows permuted
    public boolean isIsomorphicTo(BioGraph target, String adjacency_type ) 
    {
        Vector<BioComponent> components = getComponents();
        Vector<BioComponent> target_components = target.getComponents();
        
        // Get the easy tests out of the way
        
        int n = components.size();
        
        // Must have the same number of components to be isomorphic
        if ( target_components.size() != n )
        {
            return false;
        }
        
	// Get adjacency matrices
        boolean[][] this_adj = getComponentAdjacencyMatrix(adjacency_type);
        boolean[][] target_adj = target.getComponentAdjacencyMatrix(adjacency_type);

        String[] this_label_states = new String[ n ];
        String[] target_label_states = new String[ n ];
        
        
        int[] iso_map =  new int[n];
	
        // iso_map[i] is the node number in the other graph 
	// corresponding to the node i in this graph
        // Initialize to be in total correspondence
        // the "labels" arrays contain the node labels corresponding to the index
        // used to match labels in isIso()
	for (int i = 0; i < n; i++)
        {
            iso_map[i] = i;
            this_label_states[i] = ((BioComponent)components.get(i)).getLabelState();
            target_label_states[i] = ((BioComponent)target_components.get(i)).getLabelState();
        }

        
        
        // generate and test all permutations
        return isoPermuteAndTest (iso_map, n, 0, this_adj, target_adj, this_label_states, target_label_states );
    }
	

    private boolean isoPermuteAndTest ( int[] iso_map, int size, int depth, boolean[][] this_adj, boolean[][] target_adj,
                                        String[] this_label_states, String[] target_label_states ) 
    {
        
    if (depth == size-1)
    {
        return isIso(iso_map, size, this_adj, target_adj, this_label_states, target_label_states );
    }
    
    for (int i = depth; i < size; i++) 
    {
        // swap iso_map[i] and iso_map[depth]
        int temp = iso_map[i]; 
        iso_map[i] = iso_map[depth]; 
        iso_map[depth] = temp;
    	
        // Recursive step
        if (isoPermuteAndTest(iso_map, size, depth+1, this_adj, target_adj, this_label_states, target_label_states ) )
        {
            return true;
        }
	    
        // otherwise swap back
        iso_map[depth] = iso_map[i];  
        iso_map[i] = temp;
    }
	
    return false;    
    }
    
    private boolean isIso (int[] iso_map, int size, boolean[][] this_adj, boolean[][] target_adj, String[] this_label_states, String[] target_label_states ) 
    {      
        // Check whether the labels and conectivity of these two nodes are the same
	for (int i = 0; i < size; i++)
        {
            if (debug_statements) System.out.println( "isIso:" + this_label_states[i] + " and " + target_label_states[i] );
            
            // Check labels
            if ( !this_label_states[i].equals( target_label_states[iso_map[i]]) )
            {
                return false;
            }
                
            // Check adjacency
                
            for (int j = 0; j < size; j++)   
            {
                if (this_adj[i][j] != target_adj[iso_map[i]][iso_map[j]] ) 
                {
                    return false;
                }
            }
        }
	 
	return true;
    }
    
    
    
    
    // Returns an adjacency matrix containing adjacency information about
    // the components in this species. The matrix is indexed by the components'
    // position in the components vector
    public boolean[][] getComponentAdjacencyMatrix( String adjacency_type )
    {
        Vector<BioComponent> components = getComponents();
        int size = components.size();
        boolean[][] matrix = new boolean[size][size]; // Initializes all elements to false
        
        Iterator current_node_itr = components.iterator();
        while( current_node_itr.hasNext() )
        {
            BioComponent current_node = (BioComponent)current_node_itr.next();
            
            // Print labels of components for debugging
            //if (debug_statements) System.out.println( "Current node: " + current_node.getLabel() );
            
            int current_node_index = components.indexOf( current_node );
            
            
            Vector<Connectable> adjacent_nodes = new Vector<Connectable>(); 
            
            if ( adjacency_type.equals("CommonEdge") )
            {
                adjacent_nodes.addAll( current_node.getNeighbors() );
            }
            else if ( adjacency_type.equals("CommonContainer") )
            {
                adjacent_nodes.addAll( current_node.getContainer().getComponents() );
                adjacent_nodes.remove( current_node );
            }
            else
            {
                String title = "Error in Species::getComponentAdjacencyMatrix(...)";
                String error_msg = "Unknown adjacency type \""+adjacency_type+"\" specified. Cannot continue.\n" +
                "Please contact support at support@bionetgen.com";
                
                if ( getContainingPanel() != null )
                {
                    getContainingPanel().displayError(title, error_msg);
                }
                else
                {
                    if (debug_statements) System.out.println(title+", "+error_msg);
                }
                
                return null;
              }
            
            Iterator neighbor_itr = adjacent_nodes.iterator();
            
            // Iterate over neighbors, find their indicies and add them to
            // and set the corresponding position in the matrix to true
            while( neighbor_itr.hasNext() )
            {
                BioComponent neighbor = (BioComponent) neighbor_itr.next();
                //if (debug_statements) System.out.println( "Neighboring node: " + neighbor.getLabel() );
                
                int adjacent_node_index = components.indexOf( neighbor );
                
                if ( adjacent_node_index == -1 )
                {
                    String error_msg = "A component was processed that has a neighbor that\n" +
                                   "is not in this species\n." +
                                   "The component label is " + current_node.getLabel() + "\n" +
                                   "and the offending neighbor's label is " + neighbor.getLabel() +".\n" +
                                   "This neighbor will not be included in the adjacency matrix\n" +
                                   "used to calculate graph isomorphisms.";
                    
                    if ( getContainingPanel() != null )
                    {
                        getContainingPanel().displayWarning("Warning: Unhandled Condition",
                                                            error_msg );
                    }
                    else
                    {
                        if (debug_statements) System.out.println("Warning: Unhandled Condition,"+error_msg);
                    }
                    
                    continue;
                }
                
                matrix[current_node_index][adjacent_node_index] = true;
            }
        }
        
        return matrix;
    }    

    public void displayPopupMenu(MouseEvent e)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        //JMenuItem unlock_menu_item = new JMenuItem( "Unlock" );
	//JMenuItem lock_menu_item = new JMenuItem( "Lock" );
        JMenuItem disband_menu_item = new JMenuItem( "Disband" );
	
        //unlock_menu_item.addActionListener( this );
        //lock_menu_item.addActionListener( this );
        disband_menu_item.addActionListener( this );
        
        
        // Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Actions");
	popup.addSeparator();
        
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
         
        popup.add( disband_menu_item );
      
        
        popup.show( containing_panel, e.getX(), e.getY() );
         
    }
   
    public Vector<BioGraph> getSubgraphsIsomorphicTo( BioGraph target, String adjacency_type ) 
    {
        Vector<BioComponent> matching_components = new Vector<BioComponent>();
        Vector<BioGraph> matches = new Vector<BioGraph>();
        Vector<BioGraph> subgraphs = new Vector<BioGraph>();
        Vector<BioComponent> components = getComponents();
        Vector<BioComponent> target_components = target.getComponents();
        
        
        
        // Use a separate deletion vector so that we can avoid deleting (and so changing indices)
        // in the component vector while still in the for loop that depends on those
        // indicies
        Vector<BioComponent> deletion_vect = new Vector<BioComponent>();
        
        // Decompose the target into all possible subgraphs
        // 1) Generate a 2d array containing all permutations of a boolean array
        //    removing components that correspond to true values in the permutaion array
        //    results in 2^(graph_size) new permutaions of the target graph
        // 2) Test each subgraph permutation for Isomorphism with "this" container, add
        //    isomorphic subgraphs to the matches vector
        // 3) return matches2 vector.
        
        
        // Get all permutations of a boolean array to guide
        // removal of nodes from the parent graph in order to 
        // generate all subgraphs. Array returned by permuteBooleanArray
        // has "size" columns and 2^size rows
        int n_components = target_components.size();
               
        boolean[][] vertex_permutations = permuteBooleanArray( n_components );
        int n_vertex_permutations = (int)Math.pow(2,n_components);
            
        for ( int i = 0; i < n_vertex_permutations; i++ )
        {
                //progress_bar.setValue( i );
                
                // Make a copy of the Container (and its components)
                BioGraph component_subgraph = null;
                Vector<BioComponent> subgraph_components = null;
            
                try
                {
                    component_subgraph = (BioGraph)WidgetCloner.clone( target );
                    //component_subgraph = target; 
                    subgraph_components = component_subgraph.getComponents();
                }
                catch( Exception e )
                {
                    getContainingPanel().displayError("Exception in Species::getSubGraphIsoMorphisms()",
                                                  "Contact support at support@bionetgen.com."
                                                  + " Exception message was: " 
                                                  + e.getMessage() );
                    return null;
                }
        
                
                for( int k = 0; k < n_components; k++ )
                {
                    // if true add the kth node to the deletion vector
                    if ( vertex_permutations[i][k] == true )
                    {
                        deletion_vect.add( subgraph_components.get(k) );
                    }
                }
            
                // remove nodes from copy and generate edge subgraphs
                Iterator itr = deletion_vect.iterator();
                while( itr.hasNext() )
                {
                    BioComponent bc = ( (BioComponent)itr.next() );
                    bc.removeAllEdges();
                    component_subgraph.removeComponent(bc);
                }
            
                // Now we have a subgraph where V(G) is a subset or equal to
                // V(target) but we still need to generate all the possible
                // permutations where E(permutations) are subsets of E(G).
            
                // Use permuteBinaryArray to generate the adjacencies for each
                // node pair in the subgraph
            
                int n_edges = component_subgraph.getEdges().size();
                boolean[][] edge_permutations = permuteBooleanArray( n_edges );
                int n_edge_permutations = (int)Math.pow(2,n_edges);
                
                for ( int j = 0; j < n_edge_permutations; j++ )
                {
                    // Make a copy of the Container (and its components)
                    BioGraph edge_subgraph = null;
                    Vector<Edge> subgraph_edges = null;
            
                    try
                    {
                        //edge_subgraph = component_subgraph; 
                        edge_subgraph = (BioGraph)WidgetCloner.clone( component_subgraph );
                        subgraph_edges = edge_subgraph.getEdges();
                    }
                    catch( Exception e )
                    {
                        getContainingPanel().displayError("Exception in BioContainer::getSubGraphIsoMorphisms()",
                                                  "Contact support at support@bionetgen.com."
                                                  + " Exception message was: " 
                                                  + e.getMessage() );
                        return null;
                    }
                    
                Vector<Edge> edge_deletion_vect = new Vector<Edge>();
                for( int k = 0; k < n_edges; k++ )
                {
                    // if true add the kth edge to the deletion vector
                    if ( edge_permutations[j][k] == true )
                    {
                        edge_deletion_vect.add( subgraph_edges.get(k) );
                    }
                }
            
                // remove nodes from copy and generate edge subgraphs
                Iterator edge_itr = edge_deletion_vect.iterator();
                while( edge_itr.hasNext() )
                {
                    Edge e = ( (Edge)edge_itr.next() );
                    edge_subgraph.removeEdge(e);
                }
                
                if ( this.isIsomorphicTo( edge_subgraph, adjacency_type ) )
                {
                    matches.add( edge_subgraph );
                }
                
                }
          
        }
       
                        
        if (debug_statements) System.out.println("Found " + matches.size() + " matching subgraphs.");
        return matches;
    }
    
    // This function takes an empty boolean array and populates each row  
    // with a unique true-false permutation. 2^cols rows contain
    // all possible permutations. This is equivilent to generating a truth
    // table for "cols" number of expressions. The function polulates half of the array 
    // it is given with true values and the other half with false values.
    // The number of elements is then split and each half given to
    // permuteBooleanArray which repeats the process on the next row until the
    // the array is of size two and the call tree is "rows" deep.
    // "End" must be 2^max_depth when permuteBooleanArray is initially called
    // since there are alway 2^n ways to permute a boolean array of length n.
    
    // Wrapper function for permuteBooleanArray
    private boolean[][] permuteBooleanArray( int size )
    {
        int cols = size;
        int rows = (int)Math.pow(2,cols);
        
        boolean[][] perm_array = new boolean[rows][cols];
        
        permuteBooleanArray(perm_array, 0, 0, rows, cols);
        
        return perm_array;
    }
    
    private void permuteBooleanArray( boolean[][] perm_array, int depth, int start, int end, int max_depth )
    {
	if ( depth == max_depth ) return; 

	int middle = start+((end-start)/2);

	for ( int i = start; i < middle; i++ )
	    {
		perm_array[i][depth] = true;
	    } 

	for ( int i = middle; i < end; i++ )
	    {
		perm_array[i][depth] = false;
	    }

	permuteBooleanArray( perm_array, depth+1, start, middle, max_depth );
	permuteBooleanArray( perm_array, depth+1, middle, end, max_depth );
    }
    
    public BioComponent getComponent(long ID) 
    {
        Iterator i = getComponents().iterator();
        while ( i.hasNext() )
        {
            BioComponent current = (BioComponent)i.next();
            if ( current.getID() == ID )
            {
                return current;
            }
        }
        
        return null;
    }
    
    public boolean matches(Species target) 
    {
	return 
        this.isIsomorphicTo( target, "CommonEdge" )
        && this.isIsomorphicTo( target, "CommonContainer" );
    }
    
    // Returns matching components
    public Vector<BioComponent> getMatchingPortions(Species target ) 
    {        
        Vector<BioComponent> matching_graphs = new Vector<BioComponent>();
        Vector<BioGraph> matches = new Vector<BioGraph>();
        
        Vector<BioGraph> subgraph_isomorphisms = getSubgraphsIsomorphicTo( target, "CommonEdge" );
                
        Iterator subgraph_itr = subgraph_isomorphisms.iterator();
        while ( subgraph_itr.hasNext() )
        { 
           Species subgraph = (Species)subgraph_itr.next();
           if ( isIsomorphicTo( subgraph, "CommonContainer" ) )
            {
                    matches.add( subgraph );
            }
        }
        
        // Match the components in the subgraphs with the components in the
        // original graph, add the original components to a vector and return them
        // to the caller
        Iterator match_i = matches.iterator();
        while( match_i.hasNext())
        {
            Vector<BioComponent> matching_components = new Vector<BioComponent>();
            Iterator sgc_itr = ((Species)match_i.next()).getComponents().iterator();
            while ( sgc_itr.hasNext() )
            {
                BioComponent subgraph_component = (BioComponent)sgc_itr.next();
                matching_components.add( target.getComponent( subgraph_component.getID() ) );
            }
            
           matching_graphs.addAll( matching_components );
            
        }
        
        return matching_graphs;
	//return matches;
    }
    
    
    
    public void setLocked(boolean locked) 
    {
        this.locked = locked;
        
        if ( this.locked )
        {
            if (debug_statements) System.out.println("Species Locked");
        }
        else
        {
            if (debug_statements) System.out.println("Species UnLocked");
        }
    }
    
    public boolean isLocked() 
    {
        return locked;
    }
    
    public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] retValue;
        
        retValue = super.getTransferDataFlavors();
        return retValue;
    }
    
    public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor flavor) {
        boolean retValue;
        
        retValue = super.isDataFlavorSupported(flavor);
        return retValue;
    }
    
    /*
    public BufferedImage createImage()
    {
        BufferedImage img = new BufferedImage
        (
         getWidth()+5, 
         getHeight()+5, 
         BufferedImage.TYPE_INT_ARGB_PRE
        );
        
        Graphics2D g2 = img.createGraphics();
 
     // Make the image ghostlike
     g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
 
     // Draw the Container onto the img
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
     paint( getX(), getY(), g2 );
     //g2.setColor( Color.BLACK );
     //g2.drawOval( 5, 5, 10, 10 );
     //g2.dispose();
     
     return img;
    }
    */
    
    public void paint( int x_origin, int y_origin, Graphics2D g2d) 
    {
        if (debug_statements) System.out.println("Painting Species");
        
        Iterator e_itr = getEdges().iterator();
        while( e_itr.hasNext() )
	    {
		Edge next_edge = ((Edge)e_itr.next());
                next_edge.paint(x_origin, y_origin, g2d);
	    } 
        
        Iterator c_itr = containers.iterator();
	
        while( c_itr.hasNext() )
	    {
		//	((Container)(containers.get(i))).setColor(Color.red);
		BioContainer next_container = ((BioContainer)c_itr.next());
                next_container.paint(x_origin, y_origin, g2d);
	    }    
    }

    // Resolve layout problems
    public void layout() 
    {
        // Resolve container collisions within the subgraph
        Iterator container_itr = getContainers().iterator();
        while( container_itr.hasNext() )
        {
            int direction = 1;
            BioContainer container = (BioContainer)container_itr.next();
            while ( container.detectCollision( getContainers() ) )
            {
                direction++;
                container.resetOffsets();
                int new_x = container.getX();
                int new_y = container.getY();
                
                if (direction % 2 == 0)
                {
                     new_x+=container.getWidth();
                }
                else
                {
                    new_y+=container.getHeight();
                }
                    
                
                container.updateLocation( new_x, new_y, false );
            }
        }
    }
    
    public boolean detectCollision( Vector<BioGraph> graphs ) 
    {
        Vector<BioContainer> graph_containers = new Vector<BioContainer>();
        
        Iterator<BioGraph> graph_itr = graphs.iterator();
        while ( graph_itr.hasNext() )
        {
            BioGraph graph = graph_itr.next();
            
            if ( graph != this )
            {
                graph_containers.addAll( graph.getContainers() );
            }
        }
        
        Iterator<BioContainer> container_itr = getContainers().iterator();
        while( container_itr.hasNext() )
        {
            BioContainer container = container_itr.next();
            
            if ( container.detectCollision(graph_containers) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void disband() 
    {
                WidgetPanel wp = getContainingPanel();
                wp.removeBioGraph( this );
                
                Iterator<BioComponent> component_itr = getComponents().iterator();
                while ( component_itr.hasNext() )
                {
                    wp.addComponent( component_itr.next() );
                }
                
                Iterator<BioContainer> container_itr = getContainers().iterator();
                while ( container_itr.hasNext() )
                {
                    wp.addContainer( container_itr.next() );
                }
               
                Iterator<Edge> edge_itr = getEdges().iterator();
                while ( edge_itr.hasNext() )
                {
                    wp.addEdge( edge_itr.next() );    
                }
               
                
                
                // Afterwards this species is invalid
                Iterator<BioComponent> comp_itr = getComponents().iterator();
                while ( comp_itr.hasNext() ) 
                {
                    BioComponent current = comp_itr.next();
                    current.setBioGraph(null);
                    current.setConnectedByContainer(false);
                    current.setConnectedByEdge(false);
                }
            
                for ( int j = 0; j < getContainers().size(); j++ ) 
                {
                    getContainers().get(j).setBioGraph(null);
                }
                
                setSelected(false);
                wp.repaint();
    }

}


