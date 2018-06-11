/*
 * Reaction.java
 *
 * Created on April 11, 2005, 10:08 PM
 */

import java.beans.*;
import java.io.Serializable;
import java.util.*;
import javax.swing.*;
import java.awt.*;

/**
 * @author matthew
 */
public class Reaction extends Operation implements Serializable {
    
    
    private float rate;
    //private Vector<Species> reactants;
    //private Vector<Species> products;
    
    protected float reverse_rate;
    protected float forward_rate;
    
    Reaction( Vector<Species> reactants, Vector<Operator> operators, Vector<Species> products, float forward_rate, float reverse_rate, boolean reversable, WidgetPanel containing_panel )
    {
        try
        {
            this.forward_rate = forward_rate;
            this.reverse_rate = reverse_rate; 
            //this.result = result;
	    setReactants( reactants );
            setProducts( products );
            this.operators = operators;
            //this.containing_panel = containing_panel;
            setContainingPanel( containing_panel );
        }
        catch (NullPointerException e )
        {
            e.printStackTrace();
        }
    }
    
    
    public void display( Component c, Graphics2D g2d )
	{
            if ( !isVisible() ) return;
        
	    if (debug_statements) System.out.println("Displaying reaction constituents");
            
	    Iterator<Species> p_itr = getReactants().iterator();
            Iterator q_itr = getProducts().iterator();
            Iterator o_itr = operators.iterator();
  
            g2d.setColor(Color.BLACK);
            
            while ( p_itr.hasNext() )
            {
                if (debug_statements) System.out.println("Displaying Reactant");
                p_itr.next().display( c, g2d );
            }
            
            while ( q_itr.hasNext() )
            {
                if (debug_statements) System.out.println("Displaying Product");
                ((Species) q_itr.next()).display( c, g2d );
            }
            
            while ( o_itr.hasNext() )
            {
                ((Operator) o_itr.next()).display( c, g2d );
            }
            
            g2d.setColor( g2d.getBackground() );
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
       
        if (debug_statements) System.out.println( "**********X OFFSET: " + x_offset + "**************");
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
            
            
            if (debug_statements) System.out.println("Reaction: Panel width: " + panel_width );
            if (debug_statements) System.out.println("Reaction: width: " + getWidth() );
            if (debug_statements) System.out.println("Reaction: new_x: " + new_x );
            if (debug_statements) System.out.println("Reaction: x_offset: " + x_offset );
            if (debug_statements) System.out.println("Reaction: Proposed x+width: " + (new_x+getWidth()) );
            
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
   
        
            Iterator<Species> itr = getReactants().iterator();
            while( itr.hasNext() )  
	    {
		itr.next().updateLocation( new_x, new_y, confine_to_boundaries ); 
	    }
            
            itr = getProducts().iterator();
            while( itr.hasNext() )  
	    {
		itr.next().updateLocation( new_x, new_y, confine_to_boundaries ); 
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
    
    public void calculatePointerOffset( int mouse_x, int mouse_y )
    {
	x_offset = mouse_x - getX();
	y_offset = mouse_y - getY();
        
	
        Iterator<Species> itr = getReactants().iterator();
            while( itr.hasNext() )  
	    {
		itr.next().calculatePointerOffset( getX(), getY() ); 
	    }
            
            itr = getProducts().iterator();
            while( itr.hasNext() )  
	    {
		itr.next().calculatePointerOffset( getX(), getY() ); 
	    }
           
         for ( int i = 0; i < operators.size(); i++ )
	    {
		// Calculate offset from the upper left corner of the bounding box (getX, getY)
		// to preserve relative offsets but not absolute positioning from the 
		// Species creation window
		operators.get(i).calculatePointerOffset( getX(), getY() ); 
	    }
         
    }

    /*
    public void setContainingPanel( WidgetPanel cp )
    {
        
	this.containing_panel = cp;

          Iterator<Species> itr = getReactants().iterator();
            while( itr.hasNext() )  
	    {
		itr.next().setContainingPanel( containing_panel ); 
	    }
            
            itr = getProducts().iterator();
            while( itr.hasNext() )  
	    {
                itr.next().setContainingPanel( containing_panel ); 
	    }
        
	for ( int i = 0; i < operators.size(); i++ )
	    {
		operators.get(i).setContainingPanel( cp );
	    }
    }
    */
    
    synchronized public void setSelected(boolean selected) 
    {
        if (debug_statements) System.out.println("Group setSelected"+selected+" called");
        this.selected = selected;
    
        Vector<Species> species = new Vector<Species>();
        species.addAll( getReactants() );
        species.addAll( getProducts() );
       
        Iterator<Species> p_itr = species.iterator();
        while ( p_itr.hasNext() )
        {
            p_itr.next().setSelected( selected );
        }
    
        Iterator<Operator> o_itr = operators.iterator();
        while ( o_itr.hasNext() )
        {
            o_itr.next().setSelected( selected );
        }
    }

    public void setProducts( Vector<Species> p )
    {
        Vector<BioGraph> b = new Vector();
        b.addAll(p);
        setResults( b );
    }
    
    public void setReactants( Vector<Species> p )
    {
        Vector<BioGraph> b = new Vector();
        b.addAll(p);
        setOperands( b );
    }
    
    public Vector<Species> getProducts()
{
    Vector<Species> species = new Vector();
    
    Iterator<BioGraph> itr = getResults().iterator();
    while ( itr.hasNext() )
    {
        species.add( (Species)itr.next() );
    }
    
    return species;
}

public Vector<Species> getReactants()
{
    Vector<Species> species = new Vector();
    
    Iterator<BioGraph> itr = getOperands().iterator();
    while ( itr.hasNext() )
    {
        species.add( (Species)itr.next() );
    }
    
    return species;
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
}
