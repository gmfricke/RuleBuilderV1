/*
 * Operation.java
 *
 * Created on October 30, 2005, 1:34 AM
 */

// An operation is any legal combination of operators and operands (as opposed to 
// an expression which may not contain operators).

// Operation is the base class for Groups and ReactionRules

import java.beans.*;
import java.io.Serializable;
import java.util.*;
import java.awt.*; // For JComponent
import javax.swing.*; // For graphical interface tools
import java.awt.event.*; // For mouse interactions
import java.io.*; // For file IO in write and read object methods

import java.awt.image.BufferedImage;

/**
 * @author matthew
 */
public class Operation extends Widget implements Serializable 
{
        // Serialization explicit version
        private static final long serialVersionUID = 1;
    
        protected Vector<Operator> operators;
        
        // These names are used instead of results and operands for backwards
        // compatability with serialized ReactionRules that expect these variable
        // names
        protected Vector<BioGraph> results; 
        protected Vector<BioGraph> operands;
    
        // For subclasses
        Operation()
        {
            
        }
        
        Operation( Vector<BioGraph> operands, Vector<BioGraph> results, Vector<Operator> operators )
        {
            setOperands(operands);
            setResults(results);
            setOperators(operators);
        }
        
    public void display( Component c, Graphics2D g2d )
	{
            if ( !isVisible() ) return;
        
           if ( isSelected() ) 
           {
                g2d.setColor( getSelectedColor() );
           }
           else
           {
                g2d.setColor( getUnselectedColor() );
           }
            
	    if (debug_statements) System.out.println("Displaying constituents");
            
	    Iterator p_itr = getResults().iterator();
            Iterator o_itr = operators.iterator();
            Iterator q_itr = getOperands().iterator();
            
            getFlickrLabel().display( c, g2d );
            
            while ( p_itr.hasNext() )
            {
                ((BioGraph) p_itr.next()).display( c, g2d );
            }
            
            
            while ( q_itr.hasNext() )
            {
                ((BioGraph) q_itr.next()).display( c, g2d );
            }
            
            if (debug_statements) System.out.println("Displaying " + operators.size() + " operators");
            while ( o_itr.hasNext() )
            {
                ((Operator) o_itr.next()).display( c, g2d );
            }
	}



public int calculateX()
    {
	// calculate x
	// x is the left-most x and y is the up-most y of all components and containers
	
        int left_most_x = 1000000;
        //if ( containing_panel != null )
         //   if ( containing_panel.panel_dimension != null )
        //    {
        //        left_most_x = (int)containing_panel.panel_dimension.getWidth();
        //    }
        
	for ( int i = 0; i < getOperands().size(); i++ )
	    {
		if ( ((BioGraph)getOperands().get(i)).getX() < left_most_x )
		    {
			left_most_x = ((BioGraph)getOperands().get(i)).getX();
		    }
	    }
        
        for ( int i = 0; i < getResults().size(); i++ )
	    {
		if ( ((BioGraph)getResults().get(i)).getX() < left_most_x )
		    {
			left_most_x = ((BioGraph)getResults().get(i)).getX();
		    }
	    }
	
	for ( int i = 0; i < operators.size(); i++ )
	    {
		if ( ((Operator)operators.get(i)).getX() < left_most_x )
		    {
			left_most_x = ((Operator)operators.get(i)).getX();
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
	
        //if ( containing_panel != null )
        //     if ( containing_panel.panel_dimension != null )
        //        {
        //            up_most_y = (int)containing_panel.panel_dimension.getHeight();
        //        }
        
	for ( int i = 0; i < getOperands().size(); i++ )
	    {
		if ( ((BioGraph)getOperands().get(i)).getY() < up_most_y )
		    {
			up_most_y = ((BioGraph)getOperands().get(i)).getY();
		    }
	    }
	
        for ( int i = 0; i < getResults().size(); i++ )
	    {
		if ( ((BioGraph)getResults().get(i)).getY() < up_most_y )
		    {
			up_most_y = ((BioGraph)getResults().get(i)).getY();
		    }
	    }
        
	for ( int i = 0; i < operators.size(); i++ )
	    {
		if ( ((Operator)operators.get(i)).getY() < up_most_y )
		    {
			up_most_y = ((Operator)operators.get(i)).getY();
		    }
	    }

	return up_most_y;
    }

/**
 *
 * @return
 */

/**
 *
 * @return
 */
public	int calculateWidth()
	{
            // calculate width
	// width is the right-most x - getX() and y is the down-most y - getY() of all components and containers
	
	int right_most_x = 0;
	
	for ( int i = 0; i < getOperands().size(); i++ )
	    {
		BioGraph p = (BioGraph)getOperands().get(i);
		if ( p.getX()+p.getWidth() > right_most_x )
		    {
			right_most_x = p.getX()+p.getWidth();
		    }
	    }
	
        for ( int i = 0; i < getResults().size(); i++ )
	    {
		BioGraph p = (BioGraph)getResults().get(i);
		if ( p.getX()+p.getWidth() > right_most_x )
		    {
			right_most_x = p.getX()+p.getWidth();
		    }
	    }
        
	for ( int i = 0; i < operators.size(); i++ )
	    {
		Operator o = (Operator)operators.get(i);
		if ( o.getX()+o.getWidth() > right_most_x )
		    {
			right_most_x = o.getX()+o.getWidth();
		    }
	    }
	
	return right_most_x - getX();
            
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
	
	for ( int i = 0; i < getOperands().size(); i++ )
	    {
		BioGraph s = (BioGraph)getOperands().get(i);
		if ( s.getY()+s.getHeight() > down_most_y )
		    {
			down_most_y = s.getY()+s.getHeight();
		    }
	    }
        
        for ( int i = 0; i < getResults().size(); i++ )
	    {
		BioGraph s = (BioGraph)getResults().get(i);
		if ( s.getY()+s.getHeight() > down_most_y )
		    {
			down_most_y = s.getY()+s.getHeight();
		    }
	    }
	
	for ( int i = 0; i < operators.size(); i++ )
	    {
		Operator o = (Operator)operators.get(i);
		if ( o.getY()+o.getHeight() > down_most_y )
		    {
			down_most_y = o.getY()+o.getHeight();
		    }
	    }
	
	return down_most_y - getY();
    }
    
    public int getHeight()
    {
        setHeight( calculateHeight() ); 
        // The idea is to find a better place to update Height, Width, x and y
        // so it doesnt always happen on "get"
        return height;
    }
    
    public int getWidth()
    {
        setWidth( calculateWidth() );
        return width;
    }
   
    public int getX()
    {   
        setX( calculateX() );
        return x;
    }
    
    public int getY()
    {
        setY( calculateY() );
        return y;
    }
    
    public Vector<BioGraph> getOperands()
    {
        return operands;
    }
    
    public Vector<BioGraph> getResults()
    {
        return results;
    }
    
    public void resetOffsets()
    {
        calculatePointerOffset( getX(), getY() );
    }
    
    public void calculatePointerOffset( int mouse_x, int mouse_y )
    {
	x_offset = mouse_x - getX();
	y_offset = mouse_y - getY();
        
	for ( int i = 0; i < getResults().size(); i++ )
	    {
		// Calculate offset from the upper left corner of the bounding box (getX, getY)
		// to preserve relative offsets but not absolute positioning from the 
		// Species creation window
		((BioGraph)getResults().get(i)).calculatePointerOffset( getX(), getY() ); 
	    }
        
        for ( int i = 0; i < getOperands().size(); i++ )
	    {
		// Calculate offset from the upper left corner of the bounding box (getX, getY)
		// to preserve relative offsets but not absolute positioning from the 
		// Species creation window
		((BioGraph)getOperands().get(i)).calculatePointerOffset( getX(), getY() ); 
	    }
        
         for ( int i = 0; i < operators.size(); i++ )
	    {
		// Calculate offset from the upper left corner of the bounding box (getX, getY)
		// to preserve relative offsets but not absolute positioning from the 
		// Species creation window
		((Operator)operators.get(i)).calculatePointerOffset( getX(), getY() ); 
	    }
         
    }
    
    // Overrides Widget::updateLocation( x, y )
    /**
     *
     * @param mouse_x
     * @param mouse_y
     * @param confine_to_boundaries
     */    
    synchronized public void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
    {
       
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
                double ct_height = ct.getBaseDimension().getHeight();
                double ct_width = ct.getBaseDimension().getWidth();
                double ct_zoom = ct.getZoom();
                double ct_xzoom_trans = ct.getZoomedXTranslation();
                double ct_yzoom_trans = ct.getZoomedYTranslation();
                        
                panel_width = (int)((ct_width-ct_xzoom_trans)/ct_zoom);
                panel_height = (int)((ct_height-ct_yzoom_trans)/ct_zoom);
                panel_x = (int)(-ct_xzoom_trans/ct_zoom);
                panel_y = (int)(-ct_yzoom_trans/ct_zoom);
            }
            catch ( NullPointerException e )
            {
                if ( getContainingPanel() == null ) if (debug_statements) System.out.println("Containing Panel is a NULL pointer.");
                if ( getContainingPanel().panel_dimension == null ) if (debug_statements) System.out.println("Containing Panel's dimension is a NULL pointer.");
                
                e.printStackTrace();
            }
            
            
            if (debug_statements) System.out.println("ReactionRule: Panel width: " + panel_width );
            if (debug_statements) System.out.println("ReactionRule: width: " + getWidth() );
            if (debug_statements) System.out.println("ReactionRule: new_x: " + new_x );
            if (debug_statements) System.out.println("ReactionRule: Proposed x+width: " + (new_x+getWidth()) );
            
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
            if (debug_statements) System.out.println("ReactionRule collided with panel boundary.");
        }
   
       
        super.updateLocation(mouse_x, mouse_y, confine_to_boundaries );
        //int new_x = this.x;
        //int new_y = this.y;
        
        if (debug_statements) System.out.println("Operation:updateLocation(): new_x = " + new_x + ", new_y = " + new_y );
        
	for ( int i = 0; i < getResults().size(); i++ )
	    {
		((BioGraph)getResults().get(i)).updateLocation( new_x, new_y, confine_to_boundaries ); 
	    }

        for ( int i = 0; i < getOperands().size(); i++ )
	    {
		((BioGraph)getOperands().get(i)).updateLocation( new_x, new_y, confine_to_boundaries ); 
	    }

            
	for ( int i = 0; i < operators.size(); i++ )
	    {
		((Operator)operators.get(i)).updateLocation( new_x, new_y, confine_to_boundaries ); 
	    }
	
            setX( calculateX() );
            setY( calculateY() );
            setWidth(calculateWidth());
            setHeight(calculateHeight());
            
	//containing_panel.repaint();
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
	    return mouse_x > getX() && mouse_x < getX() + getWidth() && mouse_y > getY() && mouse_y < getY() + getHeight();
	}

// Does this widgets bounding_box entirely contain the bounding box of the target widget?
boolean contains( Widget w )
	{
	    return getBoundingBox().contains( w.getBoundingBox() );
	}

public void setContainingPanel( WidgetPanel cp )
    {
	this.containing_panel = cp;
        
        if ( label != null )
        {
            label.setContainingPanel(cp);
        }
        
	for ( int i = 0; i < getResults().size(); i++ )
	    {
		((BioGraph)getResults().get(i)).setContainingPanel( cp );
	    }
        
        for ( int i = 0; i < getOperands().size(); i++ )
	    {
		((BioGraph)getOperands().get(i)).setContainingPanel( cp );
	    }

	for ( int i = 0; i < getOperators().size(); i++ )
	    {
		((Operator)getOperators().get(i)).setContainingPanel( cp );
	    }
    }

    synchronized public void setOperators( Vector<Operator> ops )
    {
        operators = ops;
    }
    
    synchronized public void setResults( Vector<BioGraph> results )
    {
        this.results = results;
    }
        
    synchronized public void setOperands( Vector<BioGraph> operands )
    {
        this.operands = operands;
    }
    
    public Vector<Operator> getOperators() 
    {
        return operators;
    }

    synchronized public void setSelected(boolean selected) 
{
    
    if (debug_statements) System.out.println("Group setSelected"+selected+" called");
    this.selected = selected;
    
    Vector<BioGraph> biographs = new Vector<BioGraph>();
    biographs.addAll( getResults() );
    biographs.addAll( getOperands() );
    
    Iterator p_itr = biographs.iterator();
    while ( p_itr.hasNext() )
    {
        ((BioGraph)p_itr.next()).setSelected( selected );
    }
    
    Iterator o_itr = operators.iterator();
    while ( o_itr.hasNext() )
    {
        ((Operator)o_itr.next()).setSelected( selected );
    }
}
    
    
    public void paint(int x_origin, int y_origin, Graphics2D g2d) 
    {
            Iterator p_itr = getResults().iterator();
            Iterator o_itr = operators.iterator();
            Iterator q_itr = getOperands().iterator();
            
            while ( p_itr.hasNext() )
            {
                ((BioGraph) p_itr.next()).paint( x_origin, y_origin, g2d );
            }
            
            while ( q_itr.hasNext() )
            {
                ((BioGraph) q_itr.next()).paint( x_origin, y_origin, g2d );
            }
            
            if (debug_statements) System.out.println("Displaying " + operators.size() + " operators");
            while ( o_itr.hasNext() )
            {
                ((Operator) o_itr.next()).paint( x_origin, y_origin, g2d );
            }
    }

    

    public Vector<BioGraph> getBioGraphs() 
    {
        Vector<BioGraph> biographs = new Vector<BioGraph>();
        biographs.addAll( getOperands() );
        biographs.addAll( getResults() );
        return biographs;
    }
    
    public void setLabel(String new_label) 
   {
            
            if ( this.label == null )
            {
                this.label = new FlickrLabel( new_label, this, getX(), getY()+getHeight()+20, containing_panel, true );
               
            }
            else
            {
                this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
            }  
            
            this.label.setFont( new Font("Arial", Font.ITALIC, 14) );
            this.label.setOn();
            
            label.setLabelXOffset(getWidth());
            label.setLabelYOffset(2*-label.getFont().getSize()+2);
            
            
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
{
    stream.defaultReadObject();
       
    label.setFont(new Font("Ariel", Font.ITALIC, 14));
    label.setOn();
   
}

    private String annotation;

    public String getAnnotation() 
    {
        return annotation;
    }

    public void setAnnotation(String annotation) 
    {
        this.annotation = annotation;
    }
    
    public void disband()
    {
     // Delink all the containers from their biographs so they can display their resize handles
                Iterator<BioGraph> prod_itr = getOperands().iterator();
                while ( prod_itr.hasNext() )
                {
                    BioGraph bg = prod_itr.next();
                    Iterator<BioContainer> container_itr = bg.getContainers().iterator();
                    while ( container_itr.hasNext() )
                    {
                        container_itr.next().setBioGraph(null);
                    }                    
                }
                
                
                
                setSelected(false);
                getContainingPanel().removeWidget( this );
                getContainingPanel().getAllComponents().addAll( getComponents() );
                getContainingPanel().getAllContainers().addAll( getContainers() );
                getContainingPanel().getAllEdges().addAll( getEdges() );
                getContainingPanel().getAllOperators().addAll( getOperators() );
                getContainingPanel().getAllEdges().addAll( getAtomMaps() );
                getContainingPanel().repaint();
    }
    
    public Vector<BioComponent> getComponents() 
{
    Vector<BioComponent> components = new Vector<BioComponent>();
    
    Vector <BioGraph> patterns = new Vector();
    patterns.addAll( getOperands() );
    patterns.addAll( getResults() );
    
    Iterator pattern_itr = patterns.iterator();
    while ( pattern_itr.hasNext() )
    {
        BioGraph curr_pattern = (BioGraph)pattern_itr.next();
        components.addAll( curr_pattern.getComponents() );
    }
    
    return components;
}

public Vector<BioContainer> getContainers() 
{
    Vector<BioContainer> containers = new Vector<BioContainer>();
    
    Vector <BioGraph> patterns = new Vector();
    patterns.addAll( getOperands() );
    patterns.addAll( getResults() );
    
    Iterator<BioGraph> pattern_itr = patterns.iterator();
    while ( pattern_itr.hasNext() )
    {
        BioGraph curr_pattern = (BioGraph)pattern_itr.next();
        containers.addAll( curr_pattern.getContainers() );
    }
    
    return containers;
}

public Vector<Edge> getEdges() 
{
    Vector<Edge> edges = new Vector<Edge>();
    
    Vector <BioGraph> patterns = new Vector();
    patterns.addAll( getOperands() );
    patterns.addAll( getResults() );
    
    Iterator<BioGraph> pattern_itr = patterns.iterator();
    while ( pattern_itr.hasNext() )
    {
        BioGraph curr_pattern = (BioGraph)pattern_itr.next();
        Iterator edge_itr = curr_pattern.getEdges().iterator();
        while ( edge_itr.hasNext() )
        {
            Edge e = (Edge)edge_itr.next();
            
            // Check for duplicates
            if ( edges.indexOf( e ) == -1 )
            {
                edges.add( e );
            }
        }
    }
    
    return edges;
}

public Vector<AtomMap> getAtomMaps() 
{
    Vector<AtomMap> maps = new Vector<AtomMap>();
    
    
    Iterator component_itr = getComponents().iterator();
    while ( component_itr.hasNext() )
    {
        BioComponent component = (BioComponent)component_itr.next();
        AtomMap am = component.getAtomMap();
        if ( am == null ) continue;
        
            if ( maps.indexOf( am ) == -1 )
            {
                maps.add( am );
            }
        
    }
    
    Iterator container_itr = getContainers().iterator();
    while ( container_itr.hasNext() )
    {
        BioContainer container = (BioContainer)container_itr.next();
        AtomMap am = container.getAtomMap();
        if ( am == null ) continue;
        
        
            if ( maps.indexOf( am ) == -1 )
            {
                maps.add( am );
            }
    }
    
    return maps;
}

}
