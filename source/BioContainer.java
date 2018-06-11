/*
 * BioContainer.java
 *
 * Created on December 3, 2004, 6:26 PM
 */

import java.beans.*;
import java.io.Serializable;

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.*; // For swing events
import javax.swing.*; // For graphical interface tools
import javax.swing.border.*; // For window borders

import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
//import java.awt.geom.AffineTransform; // For resizing containers
import java.math.*;
import java.util.Vector; // for sorting vectors

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For action events
import javax.swing.*; // For graphical interface tools

import java.io.*;

import org.openscience.cdk.ChemModel;
import org.openscience.cdk.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.io.*;

import javax.vecmath.Point2d;

import java.awt.image.BufferedImage;

/**
 * @author Matthew Fricke
 */
public class BioContainer extends Widget implements Serializable, Connectable, Mappable 
{
    
    // Serialization explicit version
   private static final long serialVersionUID = 1;
    
 
    public Vector<BioContainer> containers = new Vector<BioContainer>();
    public Vector<BioComponent> components = new Vector<BioComponent>();
    //private Vector edges = new Vector();
    transient static private JFileChooser open_fc = new JFileChooser();
    public ImageIcon icon;
    String image_url;
    public Image image;
    public Point start_resize_drag;
    public int original_height;
    public int original_width;
    public int original_y;
    public int original_x;
    transient private Rectangle last_pressed_anchor;
    
    private BioGraph biograph = null;
    
    // Remember the last mouse position so we know how far to move when dragged
    int last_mouse_x_location;
    int last_mouse_y_location;    
    
    private String current_default;
    
    private MoleculeType type;
    
    public boolean check_species_flag = false;
    
    private int version_test;
    
    private AtomMap atom_map;
   
    private boolean show_cdk = false;
    
    protected CDKCapsule cdk_capsule;
     
    transient private Rectangle resize_lower_right = new Rectangle();
    transient private Rectangle resize_upper_right = new Rectangle();
    transient private Rectangle resize_upper_left = new Rectangle();
    transient private Rectangle resize_lower_left = new Rectangle();
    
    public BioContainer() {
        assignID();
        //placeResizeHandles(); // no containing_panel
    }
    
    /**
     *
     * @param x
     * @param y
     * @param label
     * @param containing_panel
     */    
    public BioContainer(int x, int y, String label, WidgetPanel containing_panel)
    {
	this.containing_panel = containing_panel;
	this.template = false;
	this.x = x;
	this.y = y;
        
        width = 75;
	height = 50;
	setLabel( label );
        //this.label = new FlickrLabel( label, x - 2 ,y+height+10, containing_panel );

	//image_url = icon_url;

	//	URL url = this.getClass().getResource(icon_url);
	//icon = new ImageIcon(url);
	//width = 2*icon.getIconWidth()/3;
	//height = 2*icon.getIconHeight()/3;
	//image = icon.getImage();

    
        cdk_capsule = new CDKCapsule( x, y, width, height, containing_panel );

        placeResizeHandles();
        
    }
    
    // Create pure BioContainer from MoleculeType
    public BioContainer(int x, int y, MoleculeType type, WidgetPanel containing_panel)
    {
	this.containing_panel = containing_panel;
	this.template = false;
	this.x = x;
	this.y = y;
	
        width = 75;
	height = 50;
  
        try
        {
            MoleculeType type_copy = (MoleculeType)WidgetCloner.clone(type);
            setLabel( type_copy.getLabel() );
            setComponents( type_copy.getComponents() );

            Iterator<BioComponent> comp_itr = getComponents().iterator();
            while ( comp_itr.hasNext() )
            {
                comp_itr.next().setContainer(this);
            }
            
            setX( type_copy.getX() );
            setY( type_copy.getY() );
            setWidth( type_copy.getWidth() );
            setHeight( type_copy.getHeight() );

            setAtomMap( type_copy.getAtomMap() );
            
            cdk_capsule = type.cdk_capsule;
            
            //if ( type_copy.getCDKCapsule() != null )
            //{
            //    setCDKCapsule( type_copy.getCDKCapsule() );
            //}
            //else
            //{
            //    cdk_capsule = new CDKCapsule( x, y, width, height, containing_panel );
            //}
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
            
        placeResizeHandles();
        
    }
        
    /**
     *
     * @param v
     */    
    public void setComponents( Vector<BioComponent> v )
    {
        components = v;
    }
    
     boolean addComponent(BioComponent component)
     {
	
	// Make sure the component is not already in the container
	if ( components.indexOf(component) != -1 ) 
	    {
		return false;
	    }
        
        // Remove from previous container
        if ( component.getContainer() != null )
        {
            if ( component.getContainer() != this )
            {
                component.getContainer().removeComponent( component );
            }
        }
        
	component.setContainer( this );
	component.calculatePointerOffset( getX(), getY() );

	if (debug_statements) System.out.println("Add Component to Container called");

	//component.setXOffset(0);
	//component.setYOffset(0);
        component.setBioGraph( biograph );
        
	components.add( component );
                
        // add incident edges
        //for ( int i = 0; i < component.getEdges().size(); i++ )
        //{
        //    edges.add( component.getEdges().get(i) );
        //}

        
	return true;
    }

     // DOES NOT REMOVE EDGES = TO DO WITH CONCURRENT MODIFICATION
     // EXPLAIN THAT HERE
    void removeComponent(BioComponent component)
    {
			if (debug_statements) System.out.println("Successfully removed component from container");
			component.setContainer(null);
			component.setXOffset(0);
			component.setYOffset(0);
			components.remove(component);
     
                        // remove incident edges
                        //for ( int i = 0; i < component.getEdges().size(); i++ )
                        //{
                        //    edges.remove( component.getEdges().get(i) );
                        //}
                        
                        
                        
    }

    void removeAllComponents()
    {
	for ( int i = 0; i < components.size(); i++ )
	    {
		((BioComponent)components.get(i)).setContainer(null);
		((BioComponent)components.get(i)).setXOffset(0);
		((BioComponent)components.get(i)).setYOffset(0);
	    }
	
	components.removeAllElements();
        //edges.removeAllElements();
    }

    String resizeAnchor(int mouse_x, int mouse_y)
    {
        Rectangle anchor = getPressedResizeAnchor( mouse_x, mouse_y );
        
        if ( anchor == null ) return null;
        
        if ( anchor == resize_lower_left )
        {
            return "SW";
        }
        else if ( anchor == resize_lower_right )
        {
            return "SE";
        }
        else if ( anchor == resize_upper_left )
        {
            return "NW";
        }
        else if ( anchor == resize_upper_right )
        {
            return "NE";
        }
            
        return null;
    }
    
    Rectangle getPressedResizeAnchor(int mouse_x, int mouse_y)
    {
	last_pressed_anchor = null;
	//boolean on_anchor = mouse_x > x + width - 7 && mouse_x < x + width && mouse_y < y + height && mouse_y > y + height - 7;
	
        if ( resize_lower_right.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_lower_right;
        else if ( resize_upper_right.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_upper_right;
        else if ( resize_upper_left.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_upper_left;
        else if ( resize_lower_left.contains( mouse_x, mouse_y ) ) last_pressed_anchor = resize_lower_left;
        
	// Remember the start location so we can determine how mich to change the image size by in resize().
	if ( last_pressed_anchor != null )
            {
                start_resize_drag = new Point( mouse_x, mouse_y );
                original_height = height;
                original_width = width;
                original_y = y;
                original_x = x;
            }
	
	return last_pressed_anchor;
    }
    
    void resize( Dimension d )
    {
        int new_height = 0;
	int new_width = 0;
	
	// Find out how large the containing panel is right now 
	int panel_width = (int) containing_panel.panel_dimension.getWidth();
	int panel_height = (int) containing_panel.panel_dimension.getHeight();
	
	if (debug_statements) System.out.println( "Resizing " + label );
	new_height = (int)d.getHeight();
	new_width = (int)d.getWidth();
	
                
	// Don't let the user make the container too small or too big
	if (new_height < 50) new_height = 50;
	if ( new_width < 50 ) new_width = 50;
	//if ( new_height > panel_height-y ) new_height = panel_height-y - 10;
	//if ( new_width > panel_width-x ) new_width = panel_width-x - 5;
	
	if (debug_statements) System.out.println(new_height + " " + new_width );
	
	height = new_height;
	width = new_width;
	
        label.setXOffset(getWidth());
        label.setYOffset(-label.getFont().getSize());
        refreshLocation();
        //label.setY( y+height+13 );
        //label.setX( x );
        
	if ( containing_panel != null ) containing_panel.revalidate();
    }
    
    void resize( int mouse_x, int mouse_y )
    {
        //Rectangle last_pressed_anchor = getPressedResizeAngle( mouse_x, mouse_y );
        
	int new_height = original_height;
	int new_width = original_width;
	int new_x = original_x;
        int new_y = original_y;
        
	// Find out how large the containing panel is right now 
	int panel_width = (int) containing_panel.panel_dimension.getWidth();
	int panel_height = (int) containing_panel.panel_dimension.getHeight();
	
	if (debug_statements) System.out.println( "Resizing " + label.getString() );
	
        if ( debug_statements ) System.out.println( "Anchor: " + last_pressed_anchor );
        if ( debug_statements ) System.out.println( "Upper Left Anchor: " + resize_upper_left );
        if ( debug_statements ) System.out.println( "Upper Right Anchor: " + resize_upper_right );
        if ( debug_statements ) System.out.println( "Lower Left Anchor: " + resize_lower_left );
        if ( debug_statements ) System.out.println( "Lower Right Anchor: " + resize_lower_right );
        
        // Determine where the mouse pointer is so we can grow or shrink the container in the right direction
        if ( last_pressed_anchor == resize_lower_right ) 
        {
            new_height = original_height + ( mouse_y - (int)start_resize_drag.getY() );
            new_width = original_width + ( mouse_x - (int)start_resize_drag.getX() );
        }
        else if ( last_pressed_anchor == resize_upper_left ) 
        {
            new_height = original_height - ( mouse_y - (int)start_resize_drag.getY() );
            new_width = original_width - ( mouse_x - (int)start_resize_drag.getX() );
            
            new_x = mouse_x;
            new_y = mouse_y;
        }
        else if ( last_pressed_anchor == resize_upper_right ) 
        {
            new_height = original_height - ( mouse_y - (int)start_resize_drag.getY() );
            new_width = original_width + ( mouse_x - (int)start_resize_drag.getX() );
        
            new_x = getX();
            new_y = mouse_y;
        }
        else if ( last_pressed_anchor == resize_lower_left ) 
        {
            new_height = original_height + ( mouse_y - (int)start_resize_drag.getY() );
            new_width = original_width - ( mouse_x - (int)start_resize_drag.getX() );
            
            new_x = mouse_x;
            new_y = getY();
        }
        else
        {
            return;
        }
        
        
	// Don't let the user make the container too small or too big
	
        /*
        if (new_height < 50) 
        {
            new_x = getX();
            new_y = getY();
            new_height = 50;
        }
	if ( new_width < 50 ) 
        {
            new_x = getX();
            new_y = getY();
            new_width = 50;
        }
	//if ( new_height > panel_height-y ) new_height = panel_height-y - 10;
	//if ( new_width > panel_width-x ) new_width = panel_width-x - 5;
	
	if (debug_statements) System.out.println(new_height + " " + new_width );
	
         
         */
        
        if ( new_height > 50 ) 
        {
            setHeight( new_height );
            setY(new_y);
        }
        
        if ( new_width > 50 )
        {
            setWidth( new_width );
            setX(new_x);
        }

        label.setLabelXOffset(getWidth());
        label.setLabelYOffset(-label.getFont().getSize());
        refreshLocation();
        
        if ( containing_panel != null ) containing_panel.revalidate();
    }

    // Paint a basic picture of this Container
    public void paint(int origin_x, int origin_y, Graphics2D g2d)
    {
        if (debug_statements) System.out.println("Painting Container");
        Stroke old_stroke = ((Graphics2D)g2d).getStroke();
	
        g2d.setColor(Color.BLACK);
        
        // Place the container in the upper left hand corner by default
        int x = getX()-origin_x;
        int y = getY()-origin_y;
        
        // If this container is inside a biograph then place the container
        // in relation to the biograph
        
        
        g2d.drawRoundRect( x, y, getWidth(), getHeight(), 50, 50 );
	((Graphics2D)g2d).setStroke( old_stroke );
	
	//label.display( c, g2d );
        
        // Paint components
        Iterator i = getComponents().iterator();
	while ( i.hasNext() )
	    {
		BioComponent next_bc = (BioComponent)i.next();
                next_bc.paint( origin_x, origin_y, g2d ); 
	    }

    }
    
    private void displayResizeHandles( Component c, Graphics2D g2d )
    {
        
        if (debug_statements) System.out.println("Displaying Resize Handles");
        g2d.setColor(Color.gray);
        placeResizeHandles();
        //g2d.drawLine(x+width-7, y+height-7, x+width-5, y+height-5 );
        g2d.drawRect( (int)resize_upper_left.getX(), (int)resize_upper_left.getY(), (int)resize_upper_left.getWidth(), (int)resize_upper_left.getHeight() );
        g2d.drawRect( (int)resize_lower_left.getX(), (int)resize_lower_left.getY(), (int)resize_lower_left.getWidth(), (int)resize_lower_left.getHeight() );
        g2d.drawRect( (int)resize_upper_right.getX(), (int)resize_upper_right.getY(), (int)resize_upper_right.getWidth(), (int)resize_upper_right.getHeight() );
        g2d.drawRect( (int)resize_lower_right.getX(), (int)resize_lower_right.getY(), (int)resize_lower_right.getWidth(), (int)resize_lower_right.getHeight() );
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        Stroke old_stroke = ((Graphics2D)g2d).getStroke();
	//g.drawImage(image, x, y, width, height, containing_panel);

        //if (debug_statements) System.out.println(getLabel() + " has " + getEdges().size() + " edges.");
        if ( debug_statements ) 
        {
            System.out.print(this + " contains ");
            if ( getComponents().isEmpty() ) System.out.print(" no components.\n");
            else System.out.println();    
            Iterator itr = getComponents().iterator();
            while ( itr.hasNext() )
            {
                System.out.println("- "+itr.next());
            }
        }
        
        
        
        label.display(c,g2d);
        
        //setColor(getColor());
        // Determine color to use
        if ( isSelected() )
        {
            setColor( Color.BLUE );
            if ( this.getBioGraph() == null && !(getContainingPanel() instanceof WidgetPalette) )
            {
                // Zoom used to undo zoom effects on resize handle so its easy to see
                double zf = getContainingPanel().getZoom();
         	
                // Draw resize handle
                displayResizeHandles( c, g2d );
                
            }
        
        }
        else if ( !(getContainingPanel() instanceof MoleculePalette) )
        {
        
            //if ( !getEdges().isEmpty() && isPartOfValidSpecies() )
            //{
            //    setColor( new Color( 0.8f, 0.4f, 0.0f, 0.95f ) );
            //}
            //else if ( isValidType() )
            //{
            //    setColor( new Color( 1.0f, 0.0f, 1.0f, 0.95f ) );
            //}
            //else 
            if( isValidType() )
            {
                //setColor( Color.GREEN );
                setColor( new Color( 0, 102, 0, 255 ) );
            }
            else if ( isValidPattern() )
            {   
                //setColor( Color.GREEN );
                setColor( new Color( 0, 102, 0, 255 ) );
                ((Graphics2D)g2d).setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
            }
            else
            {
                setColor( Color.RED );
            }
        
        }
        
        g2d.setColor( getColor() );
        
        if ( detectCollision( getContainingPanel().getAllContainers() ) )
        {
            g2d.setColor( Color.RED );
        }
        
        if ( this instanceof MoleculeType )
        {
            setColor( getUnselectedColor() );
        }
        g2d.drawRoundRect( x, y, width, height, 50, 50 );
	((Graphics2D)g2d).setStroke( old_stroke );
	
        g2d.setColor( g2d.getBackground() );
	
	//g.drawString( label+"{"+ID+"}", x - 2 ,y+height+10 );
        //g2d.drawString( label+ ":(" + getX() + "," + getY()+"),(" + getZoomedX() + "," + getZoomedY()+")", x - 2 ,y+height+10 );
        //g2d.drawString( label, x - 2 ,y+height+10 );
        //+ ":(" + getX() + "," + getY()+"),(" + getZoomedX() + "," + getZoomedY()+")"
        // Draw components
        
 
            
            cdk_capsule.display( c, g2d );
        
            Iterator i = getComponents().iterator();
            while ( i.hasNext() )
	    {
		BioComponent next_bc = (BioComponent)i.next();
                //next_bc.setUnselectedColor( getAtomColor( next_bc.getLabel() ) ); // horrible place to do this - fix
                next_bc.display( c, g2d ); 
            }
            
            

    }
    
    // Return deep copy of this Container's components
	BioContainer make_new_container()
	{
	    BioContainer new_container = new BioContainer( x, y, "New Container", containing_panel );
	    
	    // Copy components individually;
	    new_container.components = new Vector<BioComponent>();
	    
	    for ( int i = 0; i < components.size(); i++ )
		{
		    new_container.components.add( components.get(i) );
		    
		}

	    return new_container;
	    
	}
        
    // this function calculates the pointer offset so that the container moves RELATIVE to the pointer's
    // (x,y) when dragged - not TO the pointers (x,y)
    void calculatePointerOffset( int mouse_x, int mouse_y )
    {
	x_offset = mouse_x - x;
	y_offset = mouse_y - y;

	// Components need to calculate their own offsets in order to display properly
       for ( int i = 0; i < components.size(); i++ )
	    {
		((BioComponent)components.get(i)).calculatePointerOffset( getX(), getY() ); 
	    }

    }

    // Overrides Widget::updateLocation( x, y )
    void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
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
            
            if (debug_statements) System.out.println("Container: Panel width: " + panel_width );
            if (debug_statements) System.out.println("Container: width: " + getWidth() );
            if (debug_statements) System.out.println("Container: mouse_x: " + mouse_x );
            if (debug_statements) System.out.println("Container: x_offset: " + x_offset );
            if (debug_statements) System.out.println("Container: new_x: " + new_x );
            if (debug_statements) System.out.println("Container: Proposed x+width: " + (new_x+getWidth()) );
            
	    boolean x_bound = false;
	    boolean y_bound = false;
	                
            // Check for window boundary collision
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
            
            //if ( detectContainerCollision( new_x, new_y ) 
            //    || detectContainerCollision( getX(), getY() ) )
            //{
            //    return;
            //}
            //if ( detectContainerCollision( new_x, new_y ) )
            //{
            //    return;
            //}
              
             */
            
            super.updateLocation( mouse_x, mouse_y, confine_to_boundaries );
            
	    int new_x = getX(); 
	    int new_y = getY();
	    
            if (debug_statements) System.out.println("++new_x and new_y in BioContainer:updateLocation(): " + new_x + "," + new_y);
            
            if (cdk_capsule != null ) cdk_capsule.updateLocation( x, y, confine_to_boundaries );
            
	    Iterator i = components.iterator();
	    while( i.hasNext() )
		{		    
		    BioComponent c = (BioComponent)i.next();
		    
		    if (debug_statements) System.out.println("Offsets: " + c.getXOffset() + " " + c.getYOffset() );
		    c.updateLocation( x, y, confine_to_boundaries );
		}

            containing_panel.repaint();
	}

    /**
     *
     * @param containing_panel
     */    
    public void setContainingPanel( WidgetPanel containing_panel )
    {
        if (debug_statements) System.out.println("Called BioContainer's setContainingPanel(...)");
	this.containing_panel = containing_panel;

        cdk_capsule.setContainingPanel( containing_panel );
        
        label.setContainingPanel( containing_panel );
        
	for ( int i = 0; i < components.size(); i++ )
	    {
		((BioComponent) getComponents().get(i)).setContainingPanel( containing_panel );
	    }
    }

    /**
     *
     * @return
     */    
    public Vector<BioComponent> getComponents()
    {
        return components;
    }
    
    // Get components by label
    public Vector<BioComponent> getComponentsByLabel( String label )
    {
        Vector<BioComponent> matches = new Vector<BioComponent>();
        Iterator itr = getComponents().iterator();
        while ( itr.hasNext() )
        {
            BioComponent comp = (BioComponent)itr.next();
            if ( (comp).getLabel().equals(label) )
            {
                matches.add( comp );
            }
        }
        
        return matches;
    }
    
    // Sorts components according to BioComponent::compareTo comparator 
    public void sortComponents()
    {
        Collections.sort( getComponents() );
    }
 
    // This has a lot of wierd consequences lets leave it alone for a while
    void addContainer( BioContainer container )
    {
	//component.setContainer( this );
	
	// Make sure the container is not already in this container
	//if ( components.indexOf(container) != -1 ) 
	//    {
	//	return false;
	//    }

	//containers.add( container );
	//return true;
    }
    
    Vector<BioContainer> getContainers()
    {
	return containers;
    }

    public Vector<Edge> getEdges()
    {
    	Vector<Edge> edges = new Vector<Edge>();

	// Gather edges from all components in this container
	for ( int i = 0; i < components.size(); i++ )
	    {
		BioComponent c = (BioComponent)components.get(i);
		
		for ( int j = 0; j < c.getEdges().size(); j++ )
		    {
			Edge e = (Edge)c.getEdges().get(j);
			if ( edges.indexOf(e) == -1 )
                        {
                            edges.add(e);
                        }
		    }
	    }

	return edges;
    }

    public Vector<AtomMap> getAtomMaps()
    {
    	Vector<AtomMap> maps = new Vector<AtomMap>();

	// Gather maps from all components in this container
	for ( int i = 0; i < components.size(); i++ )
	    {
		BioComponent c = (BioComponent)components.get(i);
		
                if ( c.getAtomMap() != null )  maps.add( c.getAtomMap() );
	    }

        if ( getAtomMap() != null )  maps.add( getAtomMap() );
        
	return maps;
    }
    
    // Overrides Widget setselected so that selection propagates to the elements
    // inside the BioContainer
    /**
     *
     * @param new_selection_state
     */    
    public void setSelected( boolean new_selection_state )
    {
        // Set the BioContainers selected state using the Widget setSelected method
        super.setSelected(new_selection_state);
        
        // Tell components in this container about the selection state
        Iterator i = components.iterator();
	while ( i.hasNext() )
	    {
		((BioComponent)i.next()).setSelected(selected);
	    }
    }
    
    public void setSelectedColor( Color new_color )
    {
        // Set the BioContainers selected state using the Widget setSelected method
        super.setSelectedColor( new_color );
        
        // Components should set their own colors
        
        // Tell components in this container about the selection color
        ///Iterator i = components.iterator();
	//while ( i.hasNext() )
	//    {
	//	((BioComponent)i.next()).setSelectedColor( new_color );
	//    }
    }
    
    public void setUnselectedColor( Color new_color )
    {
        // Set the BioContainers selected state using the Widget setSelected method
        super.setUnselectedColor( new_color );
        
        // Tell components in this container about the selection color
        Iterator i = components.iterator();
	while ( i.hasNext() )
	    {
		((BioComponent)i.next()).setUnselectedColor( new_color );
	    }
    }
    
    public boolean removeAtomMap()
    {
            if ( atom_map == null ) return false;
            Connectable other_end = atom_map.getOtherEnd(this);
            if ( other_end != null )
            { 
                if ( other_end instanceof BioContainer )    
                {
                    ((BioContainer)other_end).setAtomMap(null);
                }
            }
            
            if ( getContainingPanel() != null ) getContainingPanel().removeEdge(atom_map);
            setAtomMap(null);
            
            return true;
    }
    
    /**
     *
     * @param e
     */    
    public boolean removeEdge(Edge e)
    {
        if ( e instanceof AtomMap )
        {
            return removeAtomMap();
        }
        
        // First find which components have the edge
        
        Iterator i = components.iterator();
        while( i.hasNext() )
        {
            BioComponent c = (BioComponent) i.next();
            if (c.hasEdge( e ) )
            {
                // Second tell the component to delete it
                c.removeEdge(e);
                return true;
            }
        }
        
        /*
        if (debug_statements) System.out.println("+++++++++++++++++++++\nBioContainer::removeEdge called.\n++++++++++++++++");
        edges.remove(e);
        e.disconnectOtherEnd( e.getStart() );
        e.disconnectOtherEnd( e.getEnd() );
        
        if (e.getStart().hasEdge(e) ) e.getStart().removeEdge(e);
        if ( e.getEnd().hasEdge(e) ) e.getEnd().removeEdge(e);
         */
        return false;
    }
    
        /**
     *
     * @param e
     */    
    public void removeAllEdges()
    {
        
        Iterator i = components.iterator();
        while( i.hasNext() )
        {
            BioComponent c = (BioComponent) i.next();
            c.removeAllEdges();
        }
        
    }
    
    /**
     *
     * @param e
     */    
    //public void addEdge(Edge e) 
    //{
    //    if ( edges.indexOf(e) == -1 )
    //    {
    //        edges.add(e);
    //    }
    //}
    
    /**
     *
     * @param e
     * @return
     */    
    public boolean hasEdge(Edge e) 
    {
        // First find which components have the edge (if any)
        
        Iterator i = components.iterator();
        while( i.hasNext() )
        {
            BioComponent c = (BioComponent) i.next();
            if (c.hasEdge( e ) )
            {
                return true;
            }
        }
        
        
        return false;
    }
    
    public boolean detectCollision( Vector<BioContainer> containers ) 
    {
            if ( containing_panel == null )
            {
                return false;
            }
        
            // Check for collision with other containers
            Iterator j = containers.iterator();
            
            while( j.hasNext() )
            {
                BioContainer c = (BioContainer)j.next();
                
                
                if ( c != this )
                {
                    Rectangle r1 = c.getBoundingBox();
                    Rectangle r2 = getBoundingBox();
                
                    if ( r1.intersects(r2) )
                    {
                       if (debug_statements) System.out.println("Container collision detected.");
                       return true;
                    }   
                }
            }
            
            return false;
    }
    
    // under construction...
    public boolean detectWindowBoundaryCollision() 
    {
        return true;
    }
    
    // Returns an adjacency matrix containing adjacency information about
    // the components in this container. The matrix is indexed by the components'
    // position in the components vector
    public boolean[][] getComponentAdjacencyMatrix()
    {
        int size = components.size();
        boolean[][] matrix = new boolean[size][size]; // Initializes all elements to false
        
        Iterator current_node_itr = components.iterator();
        while( current_node_itr.hasNext() )
        {
            BioComponent current_node = (BioComponent)current_node_itr.next();
            
            // Print labels of components for debugging
            if (debug_statements) System.out.println( "Current node: " + current_node.getLabel() );
            
            int current_node_index = components.indexOf( current_node );
            
            Vector<Connectable> adjacent_nodes = current_node.getNeighbors();
            Iterator neighbor_itr = adjacent_nodes.iterator();
            
            // Iterate over neighbors, find their indicies and add them to
            // and set the corresponding position in the matrix to true
            while( neighbor_itr.hasNext() )
            {
                BioComponent neighbor = (BioComponent) neighbor_itr.next();
                if (debug_statements) System.out.println( "Neighboring node: " + neighbor.getLabel() );
                
                int adjacent_node_index = components.indexOf( neighbor );
                
                if ( adjacent_node_index == -1 )
                {
                    if ( getContainingPanel() != null )
                    {
                        getContainingPanel().displayWarning("Warning: Unhandled Condition",
                                   "A component was processed that has a neighbor that\n" +
                                   "is not in the same container\n." +
                                   "The component label is " + current_node.getLabel() + "\n" +
                                   "and the offending neighbor's label is " + neighbor.getLabel() +".\n" +
                                   "This neighbor will not be included in the adjacency matrix\n" +
                                   "used to calculate graph isomorphisms.");
                    }
                    else
                    {
                        if (debug_statements) System.out.println("Warning: Unhandled Condition,"+
                                   "A component was processed that has a neighbor that\n" +
                                   "is not in the same container\n." +
                                   "The component label is " + current_node.getLabel() + "\n" +
                                   "and the offending neighbor's label is " + neighbor.getLabel() +".\n" +
                                   "This neighbor will not be included in the adjacency matrix\n" +
                                   "used to calculate graph isomorphisms.");
                    }
                    
                    continue;
                }
                
                matrix[current_node_index][adjacent_node_index] = true;
            }
        }
        
        return matrix;
    }    
    
    // Returns an "adjecancy list" for the components in this container
    // An adjacency list is normally an array of linked lists here I just
    // use an IdentityHashMap of vectors
    // IdentityHashMaps intentionally violate the map interface description
    // by using reference equality instead of a user defined "comparable"
    // interface (x.equals(y))
    public IdentityHashMap getComponentAdjacencyList() 
    {
        // List of adjacency lists
        IdentityHashMap<BioComponent,Vector<Connectable>> adjacency_lists = new IdentityHashMap<BioComponent,Vector<Connectable>>();
        
        Iterator comp_i = components.iterator();
        BioComponent current_component = null;
        while( comp_i.hasNext() )
        {
            // Create a mapping between the current component and the associated
            // adjacency list
            current_component = (BioComponent)comp_i.next();
            adjacency_lists.put( current_component, current_component.getNeighbors() );
        }
        
        return adjacency_lists;
    }
    
    public void displayAdjacencyMatrix() 
    {
        if (debug_statements) System.out.println("Adjacency Matrix for Container ");
        if (debug_statements) System.out.print( getLabel() + " ");
        
        int size = components.size();
        
        for ( int i = 0; i < size; i++ )
        {
            if (debug_statements) System.out.print(((BioComponent)components.get(i)).getLabel() + " ");
        }
        if (debug_statements) System.out.println();
        
        boolean[][] matrix = getComponentAdjacencyMatrix();
        
        for ( int i = 0; i < size; i++ )
        {
            if (debug_statements) System.out.print(((BioComponent)components.get(i)).getLabel() + " ");
            for ( int j = 0; j < size; j++ )
            {
                if (debug_statements) System.out.print( matrix[i][j] );
                if (debug_statements) System.out.print( " " );
            }
            if (debug_statements) System.out.println();
        }
        if (debug_statements) System.out.println();
    }
   
    
    BioGraph getBioGraph()
    {        
        return biograph;
    }
    
    void setBioGraph( BioGraph s )
    {
        biograph = s;

        Iterator bcom_itr = components.iterator();
        while( bcom_itr.hasNext() )
        {
            ((BioComponent)bcom_itr.next()).setBioGraph( s );
        }
    
    }
    
    public MoleculeType createMoleculeType() 
    {   
       
        if ( getComponents().isEmpty() )
         {
             getContainingPanel().displayError("Error Creating Molecule Type", "Molecule Types must contain at least one Component");
             return null;
        }
        
       return new MoleculeType( this );
       
    }
    
    public boolean isType(BioContainer type) 
    {
        if ( type.getComponents().size() != getComponents().size() )
        {
            return false;
        }
        
        if ( !type.getLabel().equals( getLabel() ) )
        {
            return false;
        }
        
        Vector<BioComponent> these_components = new Vector<BioComponent>();
        these_components.addAll( getComponents() );
        
        // Match components
        Iterator type_components_itr = type.getComponents().iterator();
        while ( type_components_itr.hasNext() )
        {
            BioComponent type_current_component = (BioComponent)type_components_itr.next();
            
            BioComponent match = isTypeHelper( type_current_component, these_components );
            if ( match == null )
            {
                if (debug_statements) System.out.println("isTypeHelper returned false on " + type_current_component.getLabel() );
                return false;
            }
            else
            {
                these_components.removeElement( match );
            }
        }
            
        return true;
    }
    
    private BioComponent isTypeHelper( BioComponent current, Vector<BioComponent> these_components )
    {
        Iterator this_components_itr = these_components.iterator();
            while ( this_components_itr.hasNext() )
            {
                BioComponent this_current_component = (BioComponent)this_components_itr.next();
                if ( this_current_component.getLabel().equals( current.getLabel() ) )
                {
                    return this_current_component;       
                }
            }
        
        return null;
    }
    
    public BioComponent getComponent(String label) 
    {
        Iterator itr = getComponents().iterator();
        while ( itr.hasNext() )
        {
            BioComponent current = (BioComponent)itr.next();
            if ( current.getLabel().equals( label ) )
            {
              return current;
            }
        }
        return null;
    }
    
    public void setMoleculeType(MoleculeType type) 
    {
        this.type = type;
    }    
    
    
    public MoleculeType getMoleculeType() 
    {
        //if (debug_statements) System.out.println("getMoleculeType:Getting panel");
        WidgetPanel panel = getContainingPanel();
        if ( panel == null ) return null;
        
        //if (debug_statements) System.out.println("getMoleculeType:Getting gui");
        GUI gui = panel.getTheGUI();
        
        //if (debug_statements) System.out.println("getMoleculeType:Getting model");
        Model model = gui.getModel();
        
        //if (debug_statements) System.out.println("getMoleculeType:Getting molecule type from model");
        return model.getMoleculeType( getLabel() );
    }    
    
    public boolean matchesPattern( BioContainer pattern ) 
    {
        if (debug_statements) System.out.println("matchesPattern() is considering " + pattern.getLabel() + " and " + getLabel() );
        if ( !pattern.getLabel().equals( getLabel() ) )
        {
            if ( !pattern.getLabel().equals("*") )
            {
                return false;
            }
        }
        
        Vector<BioComponent> these_components = new Vector<BioComponent>();
        these_components.addAll( getComponents() );
        
        // Match components
        Iterator pattern_components_itr = pattern.getComponents().iterator();
        while ( pattern_components_itr.hasNext() )
        {
            BioComponent pattern_current_component = (BioComponent)pattern_components_itr.next();
            
            BioComponent match = isTypeHelper( pattern_current_component, these_components );
            if ( match == null )
            {
                if (debug_statements) System.out.println("isTypeHelper returned false on " + pattern_current_component.getLabel() );
                return false;
            }
            else
            {
                these_components.removeElement( match );
            }
        }
            
        if (debug_statements) System.out.println("...match!");
        return true;
    }
    
    public boolean isValidType() 
    {
        Model model = getContainingPanel().getTheGUI().getModel();
        return model.isValidType( this );
    }
   
    synchronized public boolean isValidPattern() 
    {
        return getContainingPanel().getTheGUI().getModel().isValidPattern( this ); 
    }
   
    public boolean isPartOfValidPattern() 
    {
        //clearCheckSpeciesFlags( new Vector() );
        return checkPattern( new Vector<BioContainer>() );
    }
    
    public Pattern getPattern()
    {
        Vector<BioContainer> contents = new Vector<BioContainer>();
        Pattern pattern = new Pattern( "Pattern", 0, 0, getContainingPanel() );
        
        checkPattern( contents );
        
        Iterator c_itr = contents.iterator();
        while( c_itr.hasNext() )
        {
            BioContainer current = (BioContainer)c_itr.next();
            pattern.addContainer( current );
        }
        
        return pattern;
    }
    
    public boolean isPartOfValidSpecies() 
    {
        //clearCheckSpeciesFlags( new Vector() );
        return checkSpecies( new Vector<BioContainer>() );
    }
    
    public boolean checkSpecies( Vector<BioContainer> visited_list ) 
    {
        //if (debug_statements) System.out.println( visited_list.size() );
        visited_list.add( this );
        
        Vector<BioContainer> neighboring_containers = new Vector<BioContainer>();
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent current_component = (BioComponent)comp_itr.next();
            Iterator neighbor_itr = current_component.getNeighbors().iterator();
            while ( neighbor_itr.hasNext() )
            {
                BioComponent neighbor = (BioComponent)neighbor_itr.next();
                BioContainer neighbor_container = neighbor.getContainer();
                if ( neighbor_container == null )
                {
                    return false;
                }
                
                if ( neighbor_container != this )
                {
                    neighboring_containers.add( neighbor_container );
                }
            }
        }
        

         if ( isValidType() == false )
         {
             return false;
         }

        
        Iterator neighboring_containers_itr = neighboring_containers.iterator();
        while ( neighboring_containers_itr.hasNext() )
        {
            BioContainer neighboring_container = (BioContainer)neighboring_containers_itr.next();
            if ( visited_list.indexOf( neighboring_container ) == -1 )
            {
                if ( !neighboring_container.checkSpecies( visited_list ) )
                {
                    return false;
                }
                
            }
        
        }
        
        return true;
    }
    
    public boolean checkPattern( Vector<BioContainer> visited_list ) 
    {
        if (debug_statements) System.out.println( visited_list.size() );
        if (debug_statements) System.out.println("Adding container \"" + getLabel() + "\" to pattern.");
        
        visited_list.add( this );
        
        Vector<BioContainer> neighboring_containers = new Vector<BioContainer>();
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent current_component = (BioComponent)comp_itr.next();
            Iterator neighbor_itr = current_component.getNeighbors().iterator();
            while ( neighbor_itr.hasNext() )
            {
                BioComponent neighbor = (BioComponent)neighbor_itr.next();
                BioContainer neighbor_container = neighbor.getContainer();
                if ( neighbor_container == null )
                {
                    return false;
                }
                
                if ( neighbor_container != this )
                {
                    neighboring_containers.add( neighbor_container );
                }
            }
        }
        

         if ( isValidPattern() == false )
         {
             return false;
         }

        
        Iterator neighboring_containers_itr = neighboring_containers.iterator();
        while ( neighboring_containers_itr.hasNext() )
        {
            BioContainer neighboring_container = (BioContainer)neighboring_containers_itr.next();
            if ( visited_list.indexOf( neighboring_container ) == -1 )
            {
                if ( !neighboring_container.checkPattern( visited_list ) )
                {
                    return false;
                }
                
            }
        
        }
        
        return true;
    }
    
    public void clearCheckSpeciesFlags( Vector<BioContainer> visited_list ) 
    {
        check_species_flag = false;
        
        Vector<BioContainer> neighboring_containers = new Vector<BioContainer>();
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent current_component = (BioComponent)comp_itr.next();
            Iterator neighbor_itr = current_component.getNeighbors().iterator();
            while ( neighbor_itr.hasNext() )
            {
                BioComponent neighbor = (BioComponent)neighbor_itr.next();
                BioContainer neighbor_container = neighbor.getContainer();
                if ( neighbor_container == null )
                {
                    return;
                }
                
                if ( neighbor_container != this )
                {
                    neighboring_containers.add( neighbor_container );
                }
            }
        }
        
        
        Iterator neighboring_containers_itr = neighboring_containers.iterator();
        while ( neighboring_containers_itr.hasNext() )
        {
            BioContainer neighboring_container = (BioContainer)neighboring_containers_itr.next();
            
            if ( visited_list.indexOf( neighboring_container ) == -1 )
            {
                visited_list.add( neighboring_container );
                neighboring_container.clearCheckSpeciesFlags( visited_list );
            }
        }
        
        
    }
 
    // Isomorphic graphs will have the same unlabeled adjacency matrix
    // with the columns and rows permuted
    public boolean isIsomorphicTo(BioContainer target, String adjacency_type ) 
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
            if (debug_statements) System.out.println( "isIso: " + this_label_states[i] + " and " + target_label_states[i] );
            
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
    private boolean[][] getComponentAdjacencyMatrix( String adjacency_type )
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

    public boolean matches(BioContainer target) 
    {
	return 
        this.isIsomorphicTo( target, "CommonEdge" )
        && this.isIsomorphicTo( target, "CommonContainer" );
    }
    
    public void assignID() 
    {
        super.assignID();
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            ((BioComponent)comp_itr.next()).assignID();
        }
        
    }    
    
    public void resetOffsets() 
    {
        setXOffset(0);
        setYOffset(0);
    }    

    public void setComponentsToDefault() 
    {
        if (debug_statements) System.out.println("**Set Components to Default Called");
        if (debug_statements) System.out.println("**This container has " + getComponents().size() + " components");
        
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent current = (BioComponent)comp_itr.next();
            if (debug_statements) System.out.println("Getting default state");
            String default_state = current.getDefaultState();
            if (debug_statements) System.out.println("Setting State");
            current.setState( default_state );
            if (debug_statements) System.out.println("Set component " + current.getLabel() + " to have state " + current.getState() );
        }
    }
    
    public boolean contains(int x, int y) 
    {
        return super.contains( x, y ) || (resizeAnchor( x, y ) != null );
    }
    
    public AtomMap getAtomMap() 
    {
        return atom_map;
    }    
    
    public boolean setAtomMap(AtomMap map) 
    {
        atom_map = map;
        return true;
    }    
  
    // Over ride superclasses getHeight so the label height can be added
    //public int getHeight() 
    //{
        //int label_height = 10;
        //return height+label.getHeight();// + label_height;
    //}    
    
    public boolean setEdges(Vector<Edge> v) 
    {
        return false;
    }
    
    public boolean addEdge(Edge e) 
    {
        return false;
    }
    
    public Vector<Connectable> getNeighbors() 
    {
        return null;
    }
    
    public Point getEdgeAttachPoint() 
    {
        if ( atom_map != null )
        {
            int x = 0;
            int y = 0;
            
            // Since this is a BioContainer the other end should also be 
            // a BioContainer
            BioContainer other_end = (BioContainer)atom_map.getOtherEnd(this);
            
            // Calculate which point on the container is closest to the other end
            int x_delta = (other_end.getX() + other_end.getWidth()/2) - (getX()+getWidth()/2);
            int y_delta = (other_end.getY() + other_end.getHeight()/2) - (getY()+getHeight()/2);
            
            if ( Math.abs( x_delta ) > Math.abs( y_delta ) )
            {
                if ( x_delta < 0 )
                {
                    x = getX();
                }
                else
                {
                    x = getX()+getWidth();
                }
                
                y = getY()+getHeight()/2;
            }
            else
            {
                if ( y_delta < 0 )
                {
                    y = getY();
                }
                else
                {
                    // this.height because this.getHeight() adds the text height
                    y = getY()+height;
                }
                
                x = getX()+getWidth()/2;
            }
             
            return new Point( x, y );
        }
        
        // Failsafe
        return new Point(getX() + getWidth()/2, getY() + getHeight()/2);
    }
    
    
    
    public void displayPopupMenu(int mouse_x, int mouse_y) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JPopupMenu popup = new JPopupMenu();
        JMenuItem read_mdl = new JMenuItem("Read MDL");
        read_mdl.addActionListener( this );
        
        JMenuItem menu_rename = new JMenuItem( "Rename" );
        JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem menu_make_species = new JMenuItem( "Make into Species" );
        JMenuItem menu_make_observable = new JMenuItem( "Make into Observable" );
        JMenuItem menu_make_molecule_type = new JMenuItem( "Create Molecule Type" );
        JMenuItem menu_make_molecule = new JMenuItem( "Create Molecule" );
        JMenuItem menu_lock_species = new JMenuItem( "Lock Species" );
        JMenuItem menu_show_cdk = new JMenuItem( "Display Chemical Structure" );
        JMenuItem menu_show_components = new JMenuItem( "Display Components" );
        
        
        // Context menu for changing attributes of the selected component
        popup.add("Options");
        popup.addSeparator();
        popup.add(menu_rename);
        popup.add(menu_delete);
        popup.add(menu_make_molecule_type);
        popup.add( read_mdl ); 
        
        /*
        if ( isShowCDK() )
        {
            popup.add( menu_show_components );
        }
        else
        {
            popup.add( menu_show_cdk );
        }
        */
         
        //popup.add(menu_make_molecule);
        
        
        boolean vs = isPartOfValidSpecies();
        boolean vp = isPartOfValidPattern();
        
        menu_make_species.setEnabled(false);
        
        if ( vs || vp )
        {
            popup.addSeparator();    
            
            if ( vs ) menu_make_species.setEnabled(true);
            //if ( vp ) popup.add( menu_make_observable );
        }
        
        popup.add( menu_make_species );
        
        menu_rename.addActionListener( getContainingPanel() );
        menu_delete.addActionListener( getContainingPanel() );
        //menu_make_molecule.addActionListener( this );
        menu_make_molecule_type.addActionListener(getContainingPanel());
        menu_make_species.addActionListener( getContainingPanel() );
        menu_make_observable.addActionListener( getContainingPanel() );
        menu_lock_species.addActionListener(getContainingPanel());
        menu_show_cdk.addActionListener(this);
        menu_show_components.addActionListener(this);
        
        popup.show( getContainingPanel(), mouse_x, mouse_y );
    
    }
    
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Display Chemical Structure") ) 
        {
            //setShowCDK( true );
        }
        else if ( action.getActionCommand().equals("Display Components") ) 
        {
            //mapComponentsToCDK();
            //setShowCDK( false );
        }
        else if ( action.getActionCommand().equals("Read MDL") ) 
        {
                    
                    //Show it.
                    if ( open_fc == null ) open_fc = new JFileChooser();
                    open_fc.setFileFilter( new MDLFilter() );
                    
		    int returnVal = open_fc.showDialog(getContainingPanel(),"Open");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            String path = open_fc.getSelectedFile().getAbsolutePath();
                            
                            
                                //append .bng if not already the extension
                               if (!path.matches(".*\\.mol$"))
                               {
                                   path = path+".mol";
                               }
                                
                               File load_file = new File( path );
                              
                              if ( cdk_capsule == null ) cdk_capsule = new CDKCapsule(getX(), getY(), getWidth(), getHeight(), getContainingPanel() );
                              cdk_capsule.readCDKMoleculeFromMDLFile( load_file );
                              setCDKCapsule( cdk_capsule );
                    }
        }
    }
    
    /*
    public void setShowCDK(boolean value) 
    {
        show_cdk = value;
        
        if ( show_cdk != true )
        {
            mapComponentsToCDK();
        }
        
     
        getContainingPanel().repaint();
    }
     */
    
    public boolean isShowCDK() 
    {
        return show_cdk;
    }

    public void setCDKMolecule( Molecule mol )
    {
        cdk_capsule.setCDKMolecule( mol );
        mapComponentsToCDK();
    }
    
    public void reMapComponentsToCDK() 
    {
        if ( cdk_capsule == null )
        {
            cdk_capsule.reReadCDKMoleculeFromMDLFile();
            mapComponentsToCDK();
        }
    }
    
    // In order to support atom maps we need to map the CDK atoms in
    // BioComponents (Support of Fangping project)
    private void mapComponentsToCDK() 
    {
        if (debug_statements) System.out.println( "mapComponentsToCDK() called" );
        
        // Need to remember the mapping from Components to Atoms so 
        // we can make edges based on atomic bonds after the components are made.
        HashMap mapping = new HashMap();
    
        Molecule mol = getCDKCapsule().getCDKMolecule();
        Vector<Point2d> rel_coords = getCDKCapsule().getRelativeAtomLocations();
        
        Vector<BioComponent> existing_components = new Vector<BioComponent>();
        existing_components.addAll( getComponents() );
        
        for ( int i = 0; i < mol.getAtomCount(); i++ )
            {
                Atom a = mol.getAtomAt(i);
                
                // Skip non-carbons
                //if ( !a.getSymbol().equals("C") ) continue;
                
                String label = a.getID();
                if ( label == null ) label = a.getSymbol();
                String state = null;
                
                // Create a new matching component unless one already exists with that name
                // in which case use the existing component
                int x = (int)a.getX2d();
                int y = (int)a.getY2d();
                  
                int cdk_height = (int)getCDKCapsule().getCDKDisplaySize().getHeight();
                
                y = (cdk_height - y);
                
                
                BioComponent new_comp = null;
                
                boolean comp_exists = false;
                
                
                Iterator itr = existing_components.iterator();
                while ( itr.hasNext() )
                {
                    BioComponent current = (BioComponent)itr.next();
                    
                    if ( current.getLabel().equals( label )  )
                    {
                        comp_exists = true;
                        current.setUnselectedColor( getAtomColor(a.getSymbol()) );
                        new_comp = current;
                        
                        break;
                    }
                }
                
                if ( !comp_exists )
                {
                    new_comp = new BioComponent(x, y,label,state,false,getContainingPanel());
                    
                                   //new_comp.calculatePointerOffset( getX(), getY() );
                    //new_comp.updateLocation(x-new_comp.getWidth()/2, y-new_comp.getHeight()/2, false);
                    new_comp.updateLocation(x-new_comp.getWidth()/2, y-new_comp.getHeight()/2, false);
                    
                    addComponent( new_comp );
                    getContainingPanel().addComponent(new_comp);
                    new_comp.setUnselectedColor( getAtomColor(a.getSymbol()) );
                    new_comp.setMovableByUser(false);
                    getContainingPanel().repaint();
                }
                else
                {
                    // remove the existing component from the existing_components vector 
                    // so it doesnt get matched twice
                    existing_components.remove( new_comp );
                                   
                    //new_comp.calculatePointerOffset( getX(), getY() );
               //new_comp.updateLocation(x-new_comp.getWidth()/2, y-new_comp.getHeight()/2, false);
                new_comp.updateLocation(x-new_comp.getWidth()/2, y-new_comp.getHeight()/2, false);
                new_comp.setUnselectedColor( getAtomColor(a.getSymbol()) );
                new_comp.setMovableByUser(false);
                getContainingPanel().repaint();
                }
                    
                if (debug_statements) System.out.println( "mapComponentsToCDK(): component now at " + new_comp.getX() + "," + new_comp.getY() );
                //
                mapping.put( a, new_comp );
                
                // Set the correct base color
                
                
               
               
               if ( debug_statements ) System.out.println("Moving component to {" + x + "," + y + "}");
               

            }
        
            // Create edges to match the atomic bonds
        /*
            for ( int i = 0; i < mol.getAtomCount(); i++ )
            {
                Atom start_atom = mol.getAtomAt(i);
                
                // We don't include non-carbons for Fangping Mu's project
                //if ( !start_atom.getSymbol().equals("C") ) continue;
                
                Atom[] connected_atoms = mol.getConnectedAtoms(start_atom);
                
                BioComponent start = (BioComponent)mapping.get( start_atom );
                
                for ( int j = 0; j < connected_atoms.length; j++ )
                {
                    //if ( !connected_atoms[j].getSymbol().equals("C") ) continue;
                    BioComponent end = (BioComponent)mapping.get( connected_atoms[j] );
                    
                    //getContainingPanel().linkComponents( start, end );
                    new Edge( start, end, getContainingPanel() );
                }
            }
        */
     }
    
    public CDKCapsule getCDKCapsule() 
    {
        return cdk_capsule;
    }

    public void setCDKCapsule(CDKCapsule cdk_capsule) 
    {    
        if ( cdk_capsule == null )
        {
            if (debug_statements) System.out.println("Rejected attempt to set BioContainer:cdk_capsule to null");
            return;
        }
        this.cdk_capsule = cdk_capsule;
        this.cdk_capsule.updateLocation( getX(), getY(), false );
        
        if (debug_statements) System.out.println("Adding capsule with " + this.cdk_capsule.getCDKMolecule().getAtomCount() + " atoms." );
        mapComponentsToCDK();
        
         resize( new Dimension( cdk_capsule.getWidth()+40, cdk_capsule.getHeight()+40 ) );

    }

    public Color getAtomColor( String atomic_symbol ) 
    {
            //H white
            Color turq = Color.CYAN;
            Color light_red = Color.ORANGE;
            Color grey_blue = Color.GRAY;
            Color grey = Color.DARK_GRAY;
            Color magenta = Color.MAGENTA;
            Color blue = Color.BLUE;
            Color red = Color.RED;
            Color green = Color.GREEN;
            Color purple = new Color(80, 0, 100);
            Color light_green = Color.GREEN;
            Color red_brown = new Color(80, 80, 0);
            Color brown = new Color(100, 50, 0);
            Color yellow = Color.YELLOW;

            if (atomic_symbol.equals("Li")) return turq;
            else if (atomic_symbol.equals("Na")) return turq;
            else if (atomic_symbol.equals("K")) return turq;
            else if (atomic_symbol.equals("Rb")) return turq;
            else if (atomic_symbol.equals("Cs")) return turq;
            else if (atomic_symbol.equals("Fr")) return turq;
            else if (atomic_symbol.equals("Ce")) return turq;
            else if (atomic_symbol.equals("Th")) return turq;
            else if (atomic_symbol.equals("Pr")) return light_red;
            else if (atomic_symbol.equals("Pa")) return light_red;
            else if (atomic_symbol.equals("Nd")) return light_red;
            else if (atomic_symbol.equals("U")) return light_red;
            else if (atomic_symbol.equals("Pm")) return light_red;
            else if (atomic_symbol.equals("Np")) return light_red;
            else if (atomic_symbol.equals("Sm")) return light_red;
            else if (atomic_symbol.equals("Pu")) return light_red;
            else if (atomic_symbol.equals("Eu")) return light_red;
            else if (atomic_symbol.equals("Gd")) return light_red;
            else if (atomic_symbol.equals("Am")) return light_red;
            else if (atomic_symbol.equals("Cm")) return light_red;
            else if (atomic_symbol.equals("Tb")) return light_red;
            else if (atomic_symbol.equals("Bk")) return light_red;
            else if (atomic_symbol.equals("Dy")) return light_red;
            else if (atomic_symbol.equals("Cd")) return light_red;
            else if (atomic_symbol.equals("Ho")) return light_red;
            else if (atomic_symbol.equals("Es")) return light_red;
            else if (atomic_symbol.equals("Er")) return light_red;
            else if (atomic_symbol.equals("Fm")) return light_red;
            else if (atomic_symbol.equals("Tm")) return light_red;
            else if (atomic_symbol.equals("Md")) return light_red;
            else if (atomic_symbol.equals("Yb")) return light_red;
            else if (atomic_symbol.equals("No")) return light_red;
            else if (atomic_symbol.equals("Lu")) return light_red;
            else if (atomic_symbol.equals("Lr")) return light_red;
            else if (atomic_symbol.equals("La")) return light_red;
            else if (atomic_symbol.equals("Ac")) return light_red;
            else if (atomic_symbol.equals("A")) return light_red;
            else if (atomic_symbol.equals("Al")) return light_red;
            else if (atomic_symbol.equals("Ga")) return light_red;
            else if (atomic_symbol.equals("In")) return light_red;
            else if (atomic_symbol.equals("Tl")) return light_red;
            else if (atomic_symbol.equals("Si")) return light_red;
            else if (atomic_symbol.equals("Ge")) return light_red;
            else if (atomic_symbol.equals("Sn")) return light_red;
            else if (atomic_symbol.equals("Pb")) return light_red;
            else if (atomic_symbol.equals("As")) return light_red;
            else if (atomic_symbol.equals("Sb")) return light_red;
            else if (atomic_symbol.equals("Bi")) return light_red;
            else if (atomic_symbol.equals("Se")) return light_red;
            else if (atomic_symbol.equals("Te")) return light_red;
            else if (atomic_symbol.equals("Po")) return light_red;
            else if (atomic_symbol.equals("At")) return light_red;
            else if (atomic_symbol.equals("Rn")) return light_red;
            else if (atomic_symbol.equals("Xe")) return light_red;
            else if (atomic_symbol.equals("Kr")) return light_red;
            else if (atomic_symbol.equals("Ar")) return light_red;
            else if (atomic_symbol.equals("Ne")) return light_red;
            else if (atomic_symbol.equals("He")) return light_red;
            else if (atomic_symbol.equals("113")) return light_red;
            else if (atomic_symbol.equals("114")) return light_red;
            else if (atomic_symbol.equals("115")) return light_red;
            else if (atomic_symbol.equals("116")) return light_red;
            else if (atomic_symbol.equals("117")) return light_red;
            else if (atomic_symbol.equals("118")) return light_red;
            else if (atomic_symbol.equals("Be")) return grey_blue;
            else if (atomic_symbol.equals("Mg")) return grey_blue;
            else if (atomic_symbol.equals("ca")) return grey_blue;
            else if (atomic_symbol.equals("Sr")) return grey_blue;
            else if (atomic_symbol.equals("Ba")) return grey_blue;
            else if (atomic_symbol.equals("Ra")) return grey_blue;
            else if (atomic_symbol.equals("C")) return grey;
            else if (atomic_symbol.equals("N")) return blue;
            else if (atomic_symbol.equals("O")) return red;
            else if (atomic_symbol.equals("F")) return green;
            else if (atomic_symbol.equals("P")) return purple;
            else if (atomic_symbol.equals("S")) return yellow;
            else if (atomic_symbol.equals("Cl")) return light_green;
            else if (atomic_symbol.equals("Br")) return red_brown;
            else if (atomic_symbol.equals("I")) return brown;
            else if (atomic_symbol.equals("Sc")) return magenta;
            else if (atomic_symbol.equals("Ti")) return magenta; 
            else if (atomic_symbol.equals("V")) return magenta;
            else if (atomic_symbol.equals("Cr")) return magenta;
            else if (atomic_symbol.equals("Mn")) return magenta;
            else if (atomic_symbol.equals("Fe")) return magenta;
            else if (atomic_symbol.equals("Co")) return magenta;
            else if (atomic_symbol.equals("Ni")) return magenta;
            else if (atomic_symbol.equals("Cu")) return magenta;
            else if (atomic_symbol.equals("Zn")) return magenta;
            else if (atomic_symbol.equals("Y")) return magenta;
            else if (atomic_symbol.equals("Zr")) return magenta;
            else if (atomic_symbol.equals("Nb")) return magenta;
            else if (atomic_symbol.equals("Mo")) return magenta;
            else if (atomic_symbol.equals("Tc")) return magenta;
            else if (atomic_symbol.equals("Ru")) return magenta;
            else if (atomic_symbol.equals("Rh")) return magenta;
            else if (atomic_symbol.equals("Pd")) return magenta;
            else if (atomic_symbol.equals("Ag")) return magenta;
            else if (atomic_symbol.equals("Cd")) return magenta;
            else if (atomic_symbol.equals("Hf")) return magenta;
            else if (atomic_symbol.equals("Ta")) return magenta;
            else if (atomic_symbol.equals("W")) return magenta;
            else if (atomic_symbol.equals("Re")) return magenta;
            else if (atomic_symbol.equals("Os")) return magenta;
            else if (atomic_symbol.equals("Ir")) return magenta;
            else if (atomic_symbol.equals("Pt")) return magenta;
            else if (atomic_symbol.equals("Au")) return magenta;
            else if (atomic_symbol.equals("Hg")) return magenta;
            else if (atomic_symbol.equals("Rf")) return magenta;
            else if (atomic_symbol.equals("Db")) return magenta;
            else if (atomic_symbol.equals("Sg")) return magenta;
            else if (atomic_symbol.equals("Bh")) return magenta;
            else if (atomic_symbol.equals("Hs")) return magenta;
            else if (atomic_symbol.equals("Mf")) return magenta;
            else if (atomic_symbol.equals("110")) return magenta;
            else if (atomic_symbol.equals("111")) return magenta;
            else if (atomic_symbol.equals("112")) return magenta;

            //getContainingPanel().displayError( "Error Mapping CDK Molecule","Unknown Atomic Symbol " + atomic_symbol );
            return Color.BLACK;
    }  

    public void setLabel( String new_label )
        {
            //containing_panel.removeFlickrLabel( this.label );
            if ( this.label == null )
            {
                this.label = new FlickrLabel( new_label, this, getX(), getY()+getHeight()+12, containing_panel, true );
               
            }
            else
            {
                this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
            }            

            this.label.setFont( this.label.getFont().deriveFont(Font.BOLD));
            this.label.setOn();
            
            label.setLabelXOffset(getWidth());
            label.setLabelYOffset(-label.getFont().getSize());
            //containing_panel.addFlickrLabel( this.label );
            
            // Schedule canvas update
	    //containing_panel.repaint();
	}
    
    // To restore label's font information 
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
    stream.defaultReadObject();
    
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setOn();
    
    //cdk_capsule = new CDKCapsule( x, y, width, height, null );
    
    reMapComponentsToCDK();
    
    resize_lower_right = new Rectangle();
    resize_upper_right = new Rectangle();
    resize_upper_left = new Rectangle();
    resize_lower_left = new Rectangle();
    //placeResizeHandles(); // cant do this here because containing panel is not available
   
   }

    private void placeResizeHandles() 
    {
        double zf = getContainingPanel().getZoom();
        
        resize_lower_right.setRect(x+getWidthWithLabel()-5, y+getHeightWithLabel()-5, (int)(12/zf), (int)(12/zf));
        resize_upper_right.setRect(x+getWidthWithLabel()-5, y-5, (int)(12/zf), (int)(12/zf));
        resize_upper_left.setRect(x-5, y-5, (int)(12/zf), (int)(12/zf));
        resize_lower_left.setRect(x-5, y+getHeightWithLabel()-5, (int)(12/zf), (int)(12/zf));
    }
    
    
    
}
