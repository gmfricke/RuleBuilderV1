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

import org.openscience.cdk.Atom;

import java.awt.image.BufferedImage; // For Buffered Image

//import java.awt.geom.AffineTransform; // For resizing containers

/**
 * BioComponents are attributes of molecules. They consist of
 * a label, a state, and adjacent edges. BioComponents usually
 * exist inside BioContainers. BioContainers manage the information
 * needed to display themselves in the BioNetGen GUI.
 */
     public class BioComponent extends Widget implements Connectable, Mappable
    {
        // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        // Transient data members are not serialized (for persistant storage)
        private BioContainer container;
        private BioGraph biograph;
	private Vector<Edge> edges = new Vector<Edge>(); 
	private String state = null;
	private String binding_state;
        private String default_state;
        private ComponentType type;
        
        // BioGraph builder helper variables (form seen lists for the recursion)
        private boolean connected_by_edge;
        private boolean connected_by_container;
        
        private Vector<String> BNGLEdges = new Vector<String>();        
        private String bngl_map;
        
        private AtomMap atom_map;        
        
        transient private Font font = new Font("Arial", Font.PLAIN, 12);
        /**
         * Default BioComponent constructor. Required in order to
         * meet Java Bean specifications.
         */        
        public BioComponent()
        {
            assignID();
        }
        
        /**
         * BioComponent constructor which initializes the BioComponent'
         * state according to the parameters passed in.
         * @param x x coordinate in the coordinate space of the
         * WidgetPanel displaying the BioComponent.
         * @param y y coordinate of the BioComponent in the
         * coordinate space of the WidgetPanel displaying
         * it.
         * @param label A string entered by the user as a means of
         * consistently referencing the BioComponent.
         * @param state A string defined by the user encoding the state
         * of the BioComponent.
         * @param template A boolean value which indicates whether the
         * BioComponent is intended to be used as a template
         * for the creation of other BioComponents or is
         * a finished BioComponent. Deprecated.
         * @param containing_panel A pointer to the WidgetPanel responsible for
         * displaying this BioComponent.
         */        
	public BioComponent( int x, int y, String label, String state, boolean template, WidgetPanel containing_panel )
        { 
                 setWidth(16);
		setHeight(16);
                setX(x);
		setY(y);
            
                this.containing_panel = containing_panel;
		this.template = template;
 
		setLabel( label );
		
		
		setBindingState( "No Additional Bonds" );
		setState( state );  
               
        }

        /**
         * Accessor function for setting the BioComtainer pointer (container)
         * to the BioContainer that contains this BioComponent. If
         * no BioContainer contains this BioContainer then container
         * should be null. Required for Java Bean compliance.
         * @param container A pointer to a BioContainer that contains this
         * BioComponent.
         */        
	public void setContainer( BioContainer container )
	{
	    this.container = container;
	}

	// Overload setContainingPanel()
        /**
         * Sets the "containing_panel" to be the WidgetPanel (or
         * subclass) that is responsible for displaying this
         * BioComponent.
         * @param panel The WidgetPanel (or subclass) that containing_panel
         * will be set to.
         */        
	public void setContainingPanel( WidgetPanel panel )
	{
            if (debug_statements) System.out.println("Called BioComponent's setContainingPanel(...)");
	
	    super.setContainingPanel(panel);
	    
            label.setContainingPanel( panel );
            
	    for ( int i = 0; i < edges.size(); i++ )
		{
		    // Use an iterator!!!
		    ((Edge)edges.get(i)).setContainingPanel( panel );
		}
	}

        /**
         *
         * @param new_binding_state
         */        
	public void setBindingState( String new_binding_state )
	{
	    /*
	    if ( new_binding_state.equals("bound") 
		 || new_binding_state.equals("unbound") 
		 || new_binding_state.equals("unspecified"))
		{
		    binding_state = new_binding_state;
                    
                    
		}
	    else
		{
		    if (debug_statements) System.out.println("Fatal Error in BioComponent::setState(): Unknown Component State! Exiting...");
		    System.exit(1);
		}
            */
            
            binding_state = new_binding_state;
            
            getContainingPanel().repaint();
	} 
	
	String getBindingState()
	{
	    return binding_state;
	}
	
        /**
         *
         * @return
         */        
        public String getState()
        {
            //if ( getAllowedStates().isEmpty() )
            //{
            //    return null;
            //}
             
            return state;
        }
        
        public String getLabelState()
        {
            return getLabel() + "-" + getState();
        }
        
        /**
         *
         * @param new_state
         */        
	public void setState( String new_state )
	{
	    /*
	    if ( new_state == null )
		{
		    if (debug_statements) System.out.println( "Error in BioComponent::setState(): null state symbol " );
		}
	    
	    if ( new_state.equals("P") || new_state.equals("UP") ||  new_state.equals("?") )
		{
		    if (debug_statements) System.out.println("Component state changed from " + state +" to " + new_state );
		    state = new_state;
		}
	    else 
		{
		    if (debug_statements) System.out.println( "Error in BioComponent::setState(): Unknown state symbol " + new_state );
		}
	    */
            
	    state = new_state;
	    if (containing_panel != null) containing_panel.repaint();
	}

        // Paint a basic picture of the component
        public void paint(int x_origin, int y_origin, Graphics2D g2d)
        {
            // Safety check
           /*
            if ( getContainer() == null )
            {
                getContainingPanel().displayError("Sanity Check Failed","A component is being dragged that is not inside a container. Contact support@bionetgen.com");
                return;
            }
            */
            
            int x = getX() - x_origin;
            int y = getY() - y_origin;
            
            if (debug_statements) System.out.println("relative_x = " + x + ", relative_y = " + y);
            
            g2d.setColor(Color.BLACK);
                
                if ( getState() == null )
                {
                    //g2d.drawString( label, (x-2)+(width/2), y+height+12 );
                }
                else
                {
                    //g2d.drawString( label + "~" + getState(), 2+(width/2), height+12 );
                }
               
		if ( binding_state.equals("Additional Bonds") )
		    {	
			
			g2d.setColor( getColor() );//c.getBackground() );
			g2d.fillOval(x,y,width,height);
			//
			g2d.drawOval( x, y, width, height );
			
		    }	
		else if ( binding_state.equals("Don't Care") )
		    {
                        g2d.setColor( Color.WHITE );
			g2d.fillOval(x, y, width, height);
			g2d.setColor(getColor());
                        g2d.fillArc(x, y, width, height, -90,180);
			g2d.drawOval( x, y, width, height );
		    }
		else  if ( binding_state.equals("No Additional Bonds") )
		    {
                        g2d.setColor( Color.WHITE );//c.getBackground() );
                        g2d.fillOval(x,y,width,height);
                        g2d.setColor( Color.BLACK );
                        g2d.drawOval( x, y, width, height );
		    }
        }
                
        /**
         *
         * @param c
         * @param g
         */        
	public void display(Component c, Graphics2D g2d)
	    {
                if ( !isVisible() ) return;
            
                if ( isSelected() )
                {
                    setColor(getSelectedColor());
                }
                else
                {
                    setColor(getUnselectedColor());
                }
                
                if ( debug_statements ) 
                {
                    if ( getContainer() != null ) 
                        System.out.println(this + " is contained by " + getContainer() );
                    else
                        System.out.println(this + " is not contained" ); 
                    
                }
        
            
                //if (debug_statements)
                {
                    System.out.print(this + " has");
                    if ( getEdges().isEmpty() ) System.out.print(" no edges.\n");
                    else System.out.println();
                    Iterator<Edge> itr = getEdges().iterator();
                    while ( itr.hasNext() )
                    {
                        Edge e = itr.next();
                        if (debug_statements) System.out.println(e + "- start="+e.getStart() + " end=" + e.getEnd() );
                    }
                }
		          
                //if (debug_statements) System.out.println( "This component has " + getEdges().size() + " edges.");
            
		g2d.setColor(getColor());
               
		label.display( c, g2d );
                
                if ( getState() == null || getContainingPanel() instanceof MoleculePalette )
                {
                    // don't draw the state string
                }
                else if ( getState().equals("*") ) //|| getContainingPanel() instanceof MoleculePalette )
                {
                    // don't draw the state string
                }
                else
                {
                    int right_of_label = label.getX()+label.getWidth()+1;
                    int in_line_with_label = label.getY()+label.getHeight();///2;
                    g2d.setFont( font );
                    g2d.setColor(Color.black);
                    g2d.drawString( "~" + getState(), right_of_label, in_line_with_label );
                    g2d.setColor( getColor() );
                }
            
                if ( image_url != null )
                {
                    URL url = this.getClass().getResource(image_url);
                    ImageIcon icon = null;
        
                    try 
                    {
                        icon = new ImageIcon(url);
                    }
                    catch ( Exception e )
                    {     
                        if (debug_statements) System.out.println( "Error Opening Domain Icon URL: The exception was " + e.getMessage() );
                        return;
                    }
         
                    setWidth( icon.getIconWidth() );
                    setHeight( icon.getIconHeight() );
		    
                    Image  image = icon.getImage();
                    g2d.drawImage(image, x, y, width, height, containing_panel);
        
		    

                    //g2d.setColor(getColor());
                    //g2d.drawRoundRect( x, y, width, height, 5, 5 );
                    //g2d.setColor(g2d.getBackground());
                    
                    // Annotate with a small version of the original icon
                    int annotation_width = 10;
                    int annotation_height = 10;
                    if ( binding_state.equals("Additional Bonds") )
		    {	
			
			g2d.setColor( getColor() );//c.getBackground() );
			g2d.fillOval(x,y,annotation_width, annotation_height);
			//
			g2d.drawOval( x, y, annotation_width, annotation_height );
			
		    }	
                    else if ( binding_state.equals("Don't Care") )
		    {
                        g2d.setColor( c.getBackground() );
			g2d.fillOval(x, y, annotation_width, annotation_height);
			g2d.setColor(getColor());
                        g2d.fillArc(x, y, annotation_width, annotation_height, -90,180);
			g2d.drawOval( x, y, annotation_width, annotation_height );
		    }
		else  if ( binding_state.equals("No Additional Bonds") )
		    {
                        g2d.setColor( Color.WHITE );//c.getBackground() );
                        g2d.fillOval(x,y,annotation_width,annotation_height);
                        g2d.setColor(getColor());
                        g2d.drawOval( x, y, annotation_width, annotation_height );
		    }

		
                    return;
                }
                
		if ( binding_state.equals("Additional Bonds") )
		    {	
			
			g2d.setColor( getColor() );//c.getBackground() );
			g2d.fillOval(x,y,width,height);
			//
			g2d.drawOval( x, y, width, height );
			
		    }	
		else if ( binding_state.equals("Don't Care") )
		    {
                        g2d.setColor( c.getBackground() );
			g2d.fillOval(x, y, width, height);
			g2d.setColor(getColor());
                        g2d.fillArc(x, y, width, height, -90,180);
			g2d.drawOval( x, y, width, height );
		    }
		else  if ( binding_state.equals("No Additional Bonds") )
		    {
                        g2d.setColor( Color.WHITE );//c.getBackground() );
                        g2d.fillOval(x,y,width,height);
                        g2d.setColor(getColor());
                        g2d.drawOval( x, y, width, height );
		    }

		
	    }
       
        /**
         *
         * @param edge
         * @return
         */        
	public boolean addEdge( Edge edge )
	{
	    // check for edge duplication
            if ( -1 != edges.indexOf( edge ) )
            {
                getContainingPanel().displayError("Internal Error","Attempt to add the same edge to a component more than once.\nContact support at support@bionetgen.com");
                return false;
            }
            
	    for ( int i = 0; i < edges.size(); i++ )
		{
		    if (((Edge)edges.get(i)).getOtherEnd( this ) == this )
			{
			    getContainingPanel().displayError("Edge Creation Error","Attempt to link a component to itself.");
			    return false;
  
			}
		}
            
            
            
            

             
            
            // check the edge doesnt already exist between these two components
            /*
            Iterator neighbors = getNeighbors().iterator();
            while( neighbors.hasNext() )
            {
                BioComponent neighbor = (BioComponent)neighbors.next();
                if ( neighbor == edge.getOtherComponent( this ) )
                {
                    if (debug_statements) System.out.println("An edge already exists between these two components!");
                    return false;
                }
            }
            */
            
	    edges.add( edge );
            //if ( container != null ) container.addEdge(edge);
            
	    //setBindingState("bound");
	    return true;
	}
	
        /**
         *
         * @param e
         */        
	public boolean removeEdge( Edge e )
	{
            if ( e instanceof AtomMap )
            {
                return removeAtomMap();
            }
            
            if (debug_statements) System.out.println("BioComponent::removeEdge called.");
	    if ( e.getStart() == this )
                {
                    e.getEnd().getEdges().remove( e );
                }
                else
                {
                    e.getStart().getEdges().remove( e );
                }
            
           edges.remove( e );
           if ( getContainingPanel() != null ) getContainingPanel().removeEdge(e);
           return true;
	}
  
        public boolean removeAtomMap()
	{
            if ( atom_map == null ) return false;
            
            atom_map.disconnectOtherEnd(this);
              
            if ( getContainingPanel() != null ) getContainingPanel().removeEdge(atom_map);
            setAtomMap(null);
            return true;
        }
            
	public void removeAllEdges()
	{
            Iterator edge_itr = edges.iterator();
            while ( edge_itr.hasNext() )
            {
                Edge e = (Edge)edge_itr.next();
                if ( e.getStart() == this )
                {
                    e.getEnd().getEdges().remove( e );
                }
                else
                {
                    e.getStart().getEdges().remove( e );
                }
            }
	    
	    edges.removeAllElements();
	}

        /**
         *
         * @return
         */        
	public BioComponent make_new_component()
	{
	    return new BioComponent(x,y,label.getString(),state,false, containing_panel); 
	}


	public BioContainer getContainer()
	{
	    return container;
	}

        /**
         *
         * @param selected
         */        
	public void setSelected( boolean selected )
	{
	    //if (debug_statements) System.out.println("SetSelected called...");

            super.setSelected( selected );

            Iterator i = edges.iterator();
    		    while ( i.hasNext() )
			{
			    ((Edge)i.next()).setSelected( selected );
			}

            //flickr_text.setSelected( selected );
            
	    if (containing_panel != null) containing_panel.repaint();

            
	}

        /**
         *
         * @param species
         */        
	public void setBioGraph( BioGraph bg )
	{
	    this.biograph = bg;
	}
        
        /**
         *
         * @return
         */        
	public BioGraph getBioGraph()
	{
	    return biograph;
	}
	
        /**
         *
         * @param v
         */        
        public boolean setEdges( Vector<Edge> v )
        {
            edges = v;
            return true;
        }
        
        /**
         *
         * @return
         */        
	public Vector<Edge> getEdges()
	{
	    return edges;
	}

        // Returns true if edge is present in this component
        /**
         *
         * @param e
         * @return
         */        
        public boolean hasEdge(Edge e) 
        {
            return edges.indexOf(e) != -1; 
        }
        
        // Returns a vector containing all the components adjacent to this one
        public Vector<Connectable> getNeighbors() 
        {
            Vector neighbors = new Vector();
            Iterator edge_i = edges.iterator();
            while( edge_i.hasNext() )
            {
                neighbors.add( (Connectable)((Edge)edge_i.next()).getOtherEnd(this) );
            }
            
            return neighbors;
        }
           
        public Vector<String> getAllowedStates() 
        {
            Vector<String> allowed = new Vector<String>();
            
            BioContainer container = getContainer();
            
            if ( container == null ) return allowed;
            
            // Wildcard container so the allowed states list for this component has to 
            // be drawn from all molecule types
            if ( container.getLabel().equals("*") )
            {
                Iterator ct_itr = getContainingPanel().getTheGUI().getModel().getComponentTypes(getLabel()).iterator();
                while ( ct_itr.hasNext() )
                {
                    ComponentType ct = (ComponentType)ct_itr.next();
                    allowed.addAll( ct.getAllowedStates() );
                }
             
                return removeDuplicates( allowed );
            }
            
            // If the component is a wildcard it can take on the state of any componenttype
            // in its MoleculeType
            if ( getLabel().equals("*") )
            {
                MoleculeType mt = getContainer().getMoleculeType();
                
                if ( mt == null ) return allowed;
                
                Vector<ComponentType> cts = mt.getComponentTypes();
                
                if ( cts.isEmpty() ) return allowed;
                
                Iterator ct_itr = cts.iterator();
                while ( ct_itr.hasNext() )
                {
                    ComponentType ct = (ComponentType)ct_itr.next();
                    allowed.addAll( ct.getAllowedStates() );
                }
            }
            
            ComponentType ct = getType();
            if ( ct == null ) return allowed;
            allowed.addAll( ct.getAllowedStates() );
            
            
            return removeDuplicates( allowed );
        }
        
        
	//private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	//{
	    // Compensate for missing constructor.
	    //reset();

	    // have to set containing_panel manually outside this function
	    //this.template = template;
	    //  stream.defaultReadObject();

	    //this.x = stream.readInt();
	    //	this.y = stream.readInt();
	    //	this.width = stream.readInt();
	    //	this.height = stream.readInt();

	    //		this.label = (String)stream.readObject();
		
		
	    //	setBindingState( (String)stream.readObject() );
	    //	setState( (String)stream.readObject() );                  

	    //}
	
	
	//private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	//{
	//   stream.defaultWriteObject();
	    
	    //stream.writeInt(this.x);
	    //stream.writeInt(this.y);
	    //stream.writeInt( this.width );
	    //stream.writeInt( this.height );
	    
	    //stream.writeObject(label);
	    
	    
	    //stream.writeObject( getBindingState() );
	    //stream.writeObject( getState() );                  
	    

	//}

       
        
   public String getDefaultState() 
   {
       // Dynamicaly update state
       
            if (debug_statements) System.out.println("getDefaultState:Getting container");
       
            BioContainer container = getContainer();
            
            if ( container == null )
            {
                return null;
            }
       
            if (debug_statements) System.out.println("getDefaultState:Getting MoleculeType");
       
            MoleculeType mt = container.getMoleculeType();
            
            if ( mt == null )
            {
                if (debug_statements) System.out.println("getDefaultState: MoleculeType was null. Returning.");
                return null;
            }
       
            
            if (debug_statements) System.out.println("getDefaultState:Getting ComponentType");
       
            ComponentType type = mt.getComponentType( getLabel() );
            
            if ( type == null )
            {
                getContainingPanel().displayError("No Such Component Type", "The component type " + getLabel() + " does not exist in any Molecule Type.");
                return null; 
            }
 
             if (debug_statements) System.out.println("getDefaultState:Getting Setting Type");
       
            setType( type );
            
            return getType().getDefaultState();
   }
   
   
   
   public void setType( ComponentType type )
   {
       this.type = type;
   }
   
   public ComponentType getType()
   {   
       if ( getContainer() == null ) return null;
       
       String l = container.getLabel();
       
       WidgetPanel cp = getContainingPanel();
       GUI the_gui = cp.getTheGUI();
       Model the_model = the_gui.getModel();
       MoleculeType mt = the_model.getMoleculeType( l );
       
       
       
       if ( mt == null ) return null;
       
       return mt.getComponentType( getLabel() );
   }
   
   public boolean isValidState(String state) 
   {
       if ( getType() == null ) return false; 
       return getType().isValidState( getState() );
   }
   
   public void setLabel(String new_label) 
   {

            if ( this.label == null )
            {
                this.label = new FlickrLabel( new_label, this, getX(), getY()+getHeight()+12, containing_panel, true );
               
            }
            else
            {
                this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
            }            

            label.setLabelXOffset(getWidth());
            label.setLabelYOffset(-label.getFont().getSize());
            
            //this.label.setFont( this.label.getFont().deriveFont(Font.BOLD));
            //containing_panel.addFlickrLabel( this.label );
            
            // Schedule canvas update
	    //containing_panel.repaint();

        //super.setLabel(new_label);
        //containing_panel.removeFlickrLabel( label );
        //label = new FlickrLabel( new_label, x, y+height+15, containing_panel );
        //containing_panel.addFlickrLabel( label ); //Widget side
        //label.updateLocation(x, y, false);
        
        BioContainer container = getContainer();
        
        if ( container == null ) return;
        if ( container.getContainingPanel() == null ) return;
        
        MoleculeType mt = container.getMoleculeType();
        
        if ( mt == null ) return;
        
        ComponentType ct = mt.getComponentType( new_label );
        
        if ( ct != null )
        {
            setType( ct );
        }
   }
   
   public void assignID() 
   {
       super.assignID();
       
       Iterator edge_itr = getEdges().iterator();
       while( edge_itr.hasNext() )
       {
           ((Edge)edge_itr.next()).assignID();
       }
   }
   
   
   
   public boolean addBNGLEdge( String BNGLEdge) 
   {
       BNGLEdges.add( BNGLEdge );
       return true;
   }
   
   public boolean addBNGLMap( String bngl_map) 
   {
       this.bngl_map = bngl_map;
       return true;
   }
   
   public Vector getBNGLEdges() 
   {
       return BNGLEdges;
   }
   
   public String getBNGLMap() 
   {
       return this.bngl_map;
   }
   
   public boolean isStateless()
   {
       if ( getLabel().equals("*") || getContainer().getLabel().equals("*") ) return false; // Need to redo stateless completely
       
       ComponentType ct = getType();
       
       if ( ct == null )
       {
            getContainingPanel().displayError("Error in Stateless check", "The Component " + label + "'s Container " + getContainer().getLabel() + " is invalid.");
       }
       
       return ct.isStateless();
   }
   
   public void displayStateDialog() 
   {
            Vector<String> allowed = new Vector<String>();
            
            if ( isStateless() )
            {
            //   getContainingPanel().displayError("Change State Error", "Cannot set the state of a component with no allowed states." );
            //    return;
            allowed.add("No State");
            
            }
            else
            {
                allowed.addAll( getAllowedStates() );
                allowed.add("*");
            }
            
            Object[] allowed_states = allowed.toArray();
            String new_state = getContainingPanel().displayInputQuestion("Change State","Change state to:", allowed_states );
            if (debug_statements) System.out.println("displayInputQuestion() returned " + new_state);
            
            if ( new_state.equals("No State") ) new_state = null;
            
            setState( new_state );
   }
   
   // Need this for backwards compatibility with versions that used null as the 
   // default state instead of "*"
   private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
    stream.defaultReadObject();
    
    // For backwards compatibility
    //if ( state == null ) state = "*";
    
            String b_state = binding_state;
            if ( b_state.equals("bound") )
            {
                binding_state = "Additional Bonds";
            }
            else if ( b_state.equals("unbound") )
            {
                binding_state = "No Additional Bonds";
            }
            else if ( b_state.equals("unspecified binding") )
            {
                binding_state = "Don't Care";
            }
            
            this.label.setOn();
   }
   
    public void displayPopupMenu( int mouse_x, int mouse_y ) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        
        JMenuItem menu_rename = new JMenuItem( "Rename" );
        JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem menu_change_state = new JMenuItem( "Set State" );
        JMenuItem menu_make_species = new JMenuItem( "Make into Species" );
        JMenuItem menu_lock_species = new JMenuItem( "Lock Species" );
        
        // Context menu for changing attributes of the selected component
        JPopupMenu popup = new JPopupMenu();
        popup.add("Options");
        popup.addSeparator();
        popup.add(menu_rename);
        popup.add(menu_delete);
        
        JMenu binding_menu = new JMenu("Binding"); 
        JMenu state_menu = new JMenu("State");
        
        
        ButtonGroup binding_radio_group = new ButtonGroup();
        JRadioButtonMenuItem bound_rb = new JRadioButtonMenuItem("Additional Bonds");
        JRadioButtonMenuItem unbound_rb = new JRadioButtonMenuItem("No Additional Bonds");
        JRadioButtonMenuItem unspecified_rb = new JRadioButtonMenuItem("Don't Care");
	        
                binding_radio_group.add( bound_rb );
                binding_radio_group.add( unbound_rb );
                
                binding_radio_group.add( unspecified_rb );
                
                bound_rb.addActionListener( this );
                unbound_rb.addActionListener( this );
                unspecified_rb.addActionListener( this );
                
  
       if ( binding_state.equals("No Additional Bonds" ) )
        {
            bound_rb.setSelected( false );
            unbound_rb.setSelected( true );
            unspecified_rb.setSelected( false );
        }
         else if ( binding_state.equals("Additional Bonds" ) )
        {
            bound_rb.setSelected( true );
            unbound_rb.setSelected( false );
            unspecified_rb.setSelected( false );
        }
        else if ( binding_state.equals( "Don't Care" ) )
        {
            bound_rb.setSelected( false );
            unbound_rb.setSelected( false );
            unspecified_rb.setSelected( true );
        }
                
        binding_menu.add( unbound_rb );
        binding_menu.add( bound_rb );
        binding_menu.add( unspecified_rb );
        
        
        BioContainer container = getContainer();
        
        if ( container != null )
        {
            
        MoleculeType mtype = container.getMoleculeType();
        ComponentType ctype = null;
        
        Model model = getContainingPanel().getTheGUI().getModel();
        
        
        
        if ( mtype != null || container.getLabel().equals("*") )
        {  
            state_menu = addStatesToMenu( getAllowedStates(), state_menu );
            popup.add(state_menu);
            //popup.add(menu_change_state);
        }
        
        }
          
        /*
        //if ( mtype != null )
        //{
        //    ctype = mtype.getComponentType( getLabel() );
        //}
        
        
        //    if ( ctype != null || container.isPartOfValidSpecies() ) {
                
                popup.addSeparator();
               
                
                if ( container.isPartOfValidSpecies() ) {
                    //if ( selected_component.getBioGraph() != null )
                    //{
                    //    popup.add( menu_lock_species );
                    //}
                    //else
                    //{
                    popup.add( menu_make_species );
                    //}
                }
                
            }
            else if ( ctype != null ) {
                popup.addSeparator();
                
                popup.add(menu_change_state);
            }
        }
        else {
            if (debug_statements) System.out.println("Container was NULL in displayComponentOptionsMenu()");
        }
        */
        
        menu_change_state.addActionListener( this );
        menu_rename.addActionListener( this );
        menu_delete.addActionListener( getContainingPanel() );
        menu_make_species.addActionListener( getContainingPanel() );
        
        // Create state change submenu
        // Create a submenu with items
        
        //JMenuItem change_state_submenu_phosphorylated = new JMenuItem( "Phosphorylated" );
        //JMenuItem change_state_submenu_unphosphorylated = new JMenuItem( "UnPhosphorylated" );
        //JMenuItem change_state_submenu_unspecified = new JMenuItem( "Unspecified" );
        
        
        //change_state_submenu_phosphorylated.addActionListener( this );
        //change_state_submenu_unphosphorylated.addActionListener( this );
        //change_state_submenu_unspecified.addActionListener( this );
        
        //JMenu change_state_submenu = new JMenu("Change State");
        //change_state_submenu.add(change_state_submenu_phosphorylated);
        //change_state_submenu.add(change_state_submenu_unphosphorylated);
        //change_state_submenu.add(change_state_submenu_unspecified);
        
        // Add submenu to popup menu
        //popup.add(change_state_submenu);
        
        // Add the Make Species item to the bottom of the menu
        //popup.addSeparator();
        
        popup.add( binding_menu );
        
       // Define the domains submenu
    JMenu domain_menu = new JMenu("Domain");

    // This menu will have a lot of entries so use a layout manager
    GridLayout menu_grid = new GridLayout(9,6);   
    domain_menu.getPopupMenu().setLayout(menu_grid);
    
    //domain_menu.getPopupMenu().setForeground( Color.WHITE );
    domain_menu.getPopupMenu().getComponent().setBackground( Color.WHITE );
    
    addDomainToMenu( "images/domains/ank.png", domain_menu);
    addDomainToMenu( "images/domains/arm.gif", domain_menu);
    addDomainToMenu( "images/domains/bromo.gif", domain_menu);
    addDomainToMenu( "images/domains/c1.gif", domain_menu);
    addDomainToMenu( "images/domains/c2.gif", domain_menu);
    addDomainToMenu( "images/domains/card.gif", domain_menu );
    addDomainToMenu( "images/domains/ch.gif", domain_menu );
    addDomainToMenu( "images/domains/chromo.gif", domain_menu );
    addDomainToMenu( "images/domains/csd.gif", domain_menu );
    addDomainToMenu( "images/domains/dd.gif", domain_menu );
    addDomainToMenu( "images/domains/ded.gif", domain_menu );
    addDomainToMenu( "images/domains/dh.gif", domain_menu );
    addDomainToMenu( "images/domains/eFh.gif", domain_menu );
    addDomainToMenu( "images/domains/enth.gif", domain_menu );
    addDomainToMenu( "images/domains/evh1.gif", domain_menu );
    addDomainToMenu( "images/domains/fbox.gif", domain_menu );
    addDomainToMenu( "images/domains/fha.gif", domain_menu );
    addDomainToMenu( "images/domains/fyve.gif", domain_menu );
    addDomainToMenu( "images/domains/gel.gif", domain_menu );
    addDomainToMenu( "images/domains/gyf.gif", domain_menu );
    addDomainToMenu( "images/domains/hect.gif", domain_menu );
    addDomainToMenu( "images/domains/lim.gif", domain_menu );
    addDomainToMenu( "images/domains/mh2.gif", domain_menu );
    addDomainToMenu( "images/domains/pb1.gif", domain_menu );
    addDomainToMenu( "images/domains/pdz.gif", domain_menu );
    addDomainToMenu( "images/domains/ph.gif", domain_menu );
    addDomainToMenu( "images/domains/ptb.gif", domain_menu );
    addDomainToMenu( "images/domains/px.gif", domain_menu );
    addDomainToMenu( "images/domains/rgs.gif", domain_menu );
    addDomainToMenu( "images/domains/ring.gif", domain_menu );
    addDomainToMenu( "images/domains/sam.gif", domain_menu );
    addDomainToMenu( "images/domains/sh2.gif", domain_menu );
    addDomainToMenu( "images/domains/sh3.gif", domain_menu );
    addDomainToMenu( "images/domains/snare.gif", domain_menu );
    addDomainToMenu( "images/domains/socs.gif", domain_menu );
    addDomainToMenu( "images/domains/start.gif", domain_menu );
    addDomainToMenu( "images/domains/tir.gif", domain_menu );
    addDomainToMenu( "images/domains/tpr.gif", domain_menu );
    addDomainToMenu( "images/domains/traf.gif", domain_menu );
    addDomainToMenu( "images/domains/tubby.gif", domain_menu );
    addDomainToMenu( "images/domains/uba.gif", domain_menu );
    addDomainToMenu( "images/domains/vhs.gif", domain_menu );
    addDomainToMenu( "images/domains/wd40.gif", domain_menu );
    addDomainToMenu( "images/domains/ww.gif", domain_menu );
    addDomainToMenu( "images/domains/bh1-4.gif", domain_menu );
    addDomainToMenu( "none", domain_menu );
    
            popup.add( domain_menu );
 
        popup.show( getContainingPanel(), mouse_x, mouse_y );
    }
    
    public void actionPerformed( ActionEvent e ) 
    {
        if (debug_statements) System.out.println(e);
        if ( e.getActionCommand().startsWith("state&") )
        {
            String new_state = e.getActionCommand().replaceFirst("state&", "");
            if ( new_state.equals("No State")) new_state = null; 
            setState(new_state);
        }
        else if ( e.getActionCommand().startsWith("domain&") )
        {
            String icon_path = e.getActionCommand().replaceFirst("domain&", "");
            if ( icon_path.equals("none") )
            {
                icon_path = null;
                setWidth(16);
		setHeight(16);
            }
            image_url = icon_path;
           
            getContainingPanel().repaint();
            // to place the flickrlabel properly 
            //label.setX(getX());
	    // label.setY(getY()+getHeight()+13);

	    // Open image just to get new height for label placement
	    URL url = this.getClass().getResource(image_url);
                    ImageIcon icon = null;
        
                    try 
                    {
                        icon = new ImageIcon(url);
                    }
                    catch ( Exception exp )
                    {     
                        if (debug_statements) System.out.println( "Error Opening Domain Icon URL: The exception was " + exp.getMessage() );
                        return;
                    }

		    if (debug_statements) System.out.println("Domain added:\nOld height: " + getHeight() );

                    setWidth( icon.getIconWidth() );
                    setHeight( icon.getIconHeight() );

		    if (debug_statements) System.out.println("New height: " + getHeight() );

		    label.setY(y+getHeight()+13);
		    getContainingPanel().repaint();
		    return;
        }
        else if ( e.getActionCommand().equals("Set State") ) 
        {
            displayStateDialog();
        }
        else if ( e.getActionCommand().equals("Additional Bonds") ) 
        {
            setBindingState("Additional Bonds");
        }
        else if ( e.getActionCommand().equals("No Additional Bonds") ) 
        {
            setBindingState("No Additional Bonds");
        }
        else if ( e.getActionCommand().equals("Don't Care") ) 
        {
            setBindingState("Don't Care");
        }
       else if ( e.getActionCommand().equals("Rename") ) 
       {
            
            
            String new_label = (String)JOptionPane.showInputDialog(
            getContainingPanel(),
            "Enter new label",
            null,
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            getLabel());
            
            
            if ( new_label != null ) {
                setLabel( new_label );
            }
            else {
                if (debug_statements) System.out.println( "Rename cancelled by user after editing the input text box" );
            }
            
            getContainingPanel().repaint();
          }
        
        super.actionPerformed( e );
    }
    
    // removed duplicate strings from a vector
    // This is not the fastest way to do this...
    // THis method looks like O(n^2) but can prob be
    // done in O(nlogn) time if we sort the vector
    // The vector size should be very small though < a few tens of entries
    private Vector removeDuplicates( Vector v ) 
    {
        Vector<String> unique_strings = new Vector<String>();
        Vector<String> delete_these = new Vector<String>();
        
        Iterator v_itr = v.iterator();
        while ( v_itr.hasNext() )
        {
            String str = (String)v_itr.next();
            
            Iterator u_itr = unique_strings.iterator();
            
            boolean seen = false;
            while ( u_itr.hasNext() )
            {
                String str2 = (String)u_itr.next();
                
                if ( str.equals(str2) )
                {
                    seen = true;
                    break;
                }
            }
            
            if ( !seen )
            {
                unique_strings.add( str );
            }
        }
        
        return unique_strings;
    }
    
    public boolean isContained() 
    {
        return getContainer() != null;
    }    
    
    public AtomMap getAtomMap() 
    {
        return atom_map;
    }    
    
    public boolean setAtomMap( AtomMap map ) 
    {            
        atom_map = map;
        
        return true;
    }    
    
    public Point getEdgeAttachPoint() 
    {
        return new Point(getX() + getWidth()/2, getY() + getHeight()/2);
    }    

         private Atom cdk_atom;

         public Atom getCDKAtom() {
             return cdk_atom;
         }

         public void setCdk_atom(Atom cdk_atom) {
             this.cdk_atom = cdk_atom;
         }

         public void updateLocation(int x, int y, boolean confine_to_boundaries) 
         {
             super.updateLocation(x, y, confine_to_boundaries );
             
             Iterator<Edge> edges_iterator = getEdges().iterator();
             while ( edges_iterator.hasNext() )
             {
                 Edge edge = edges_iterator.next();
                 edge.updateLocation();
                 
                 // edge.updateLocation updates the x, y, height, width
                 // of the edge. This is needed since edges derive their values
                 // from the components that form their endpoints.
             }
             
             if ( this.atom_map != null )
             {
                 atom_map.updateLocation();
             }
         }
    
    private JMenu addStatesToMenu( Vector<String>states, JMenu menu ) 
    {
        
        if ( isStateless() )
            {
            //   getContainingPanel().displayError("Change State Error", "Cannot set the state of a component with no allowed states." );
            //    return;
            JMenuItem mi = new JMenuItem( "No State" );
            mi.setActionCommand( "state&"+"No State" );
            mi.addActionListener(this);
            menu.add(mi);
            }
        else
        {
            Iterator<String>state_itr = states.iterator();
            while ( state_itr.hasNext() )
            {
            String current_state = state_itr.next();
            JMenuItem mi = new JMenuItem( current_state );
            mi.setActionCommand( "state&"+current_state );
            mi.addActionListener(this);
            menu.add(mi);
            }
        
            JMenuItem mi = new JMenuItem( "*" );
            mi.setActionCommand( "state&"+"*" );
            mi.addActionListener(this);
            menu.add(mi);
        }
        
        return menu;
    }
         
    private JMenuItem addDomainToMenu( String image_path, JMenu menu ) 
    {
        if (image_path.equals("none"))
        {
            JMenuItem mi = new JMenuItem( "none" );
            mi.setActionCommand("domain&"+image_path);
            mi.addActionListener(this);
            mi.setBackground(Color.WHITE);
            menu.add(mi);
            return mi;
        }
        
        ImageIcon icon = new ImageIcon( getClass().getResource(image_path) );
        Image image = icon.getImage();
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
        Image scaled_image = image.getScaledInstance( width*2/3, height*2/3, Image.SCALE_SMOOTH );
        JMenuItem mi = new JMenuItem( new ImageIcon( scaled_image ) );
        mi.setActionCommand("domain&"+image_path);
        mi.addActionListener(this);
        mi.setBackground(Color.WHITE);
        menu.add(mi);
        return mi;
    }

    public void setMovableByUser( boolean movable_by_user )
    {
        this.movable_by_user = movable_by_user;
    }
    
    public boolean isMovableByUser() 
    {
        return movable_by_user;
    }

    private boolean movable_by_user = true;

    public boolean isConnectedByEdge() {
        return connected_by_edge;
    }

    public void setConnectedByEdge(boolean connected_by_edge) {
        this.connected_by_edge = connected_by_edge;
    }

    public boolean isConnectedByContainer() {
        return connected_by_container;
    }

    public void setConnectedByContainer(boolean connected_by_container) {
        this.connected_by_container = connected_by_container;
    }

    
    
}
     
