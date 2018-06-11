/*
 * WidgetPalette.java
 *
 * Created on January 9, 2005, 12:40 PM
 */

/**
 *
 * @author  matthew
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
 
import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class WidgetPalette extends WidgetPanel
{
     
    // Serialization explicit version
    private static final long serialVersionUID = 1;
 
    protected int padding = 50;
    protected int vertical_offset = padding;
    
    protected int maxUnitIncrement = 10;    
    
    
    /**
     * Creates a new instance of WidgetPalette
     * @param the_gui
     */
    public WidgetPalette( GUI the_gui ) 
    {
        super( the_gui );
 
        area = new Dimension( 0,0 );
        
     	setBackground(Color.white);
        
        
    }
       
    
    /**
     *
     * @param new_offset
     */    
    public void setVerticalOffset(int new_offset) 
    {
        vertical_offset = new_offset;
    }    
    
    /**
     *
     * @return
     */    
    public int getVerticalOffset() 
    {
        return vertical_offset;
    }
    
    /**
     *
     * @return
     */    
    public Dimension getArea() 
    {
        return area;
    }
    
    /**
     *
     * @param new_area
     */    
    public void setArea(Dimension new_area) 
    {
        area = new_area;
    }
    
    void initialize()
    {
            super.initialize();
        
            vertical_offset = padding;
            area = new Dimension(0,0);
            revalidate();
            repaint();
    }
    
    public Vector<Widget> getContents()
    {
        Vector<Widget> widgets = new Vector<Widget>();
        
        if ( this instanceof ReactionPalette )
        {
            Iterator<Reaction> target_itr = getAllReactions().iterator();
            while ( target_itr.hasNext() )
            {
                widgets.add( (Widget)target_itr.next() );
            }
        }
        else if ( this instanceof SpeciesPalette )
        {
            Iterator<Species> target_itr = getAllSpecies().iterator();
            while ( target_itr.hasNext() )
            {
                widgets.add( (Widget)target_itr.next() );
            }
        }
        else if ( this instanceof ReactionRulePalette )
        {
            Iterator<ReactionRule> target_itr = getAllReactionRules().iterator();
            while ( target_itr.hasNext() )
            {
                widgets.add( (Widget)target_itr.next() );
            }
        }
        else if ( this instanceof MoleculePalette )
        {
            Iterator<BioContainer> target_itr = getAllContainers().iterator();
            while ( target_itr.hasNext() )
            {
                widgets.add( (Widget)target_itr.next() );
            }
        }
        else if ( this instanceof ObservablesPalette )
        {
            Iterator<Group> target_itr = getAllGroups().iterator();
            while ( target_itr.hasNext() )
            {
                widgets.add( (Widget)target_itr.next() );
            }
        }

        return widgets;
    }
    
    public void compressDisplay()
    {
        vertical_offset = padding;
        
        if ( getContents().isEmpty() ) 
        {
            area.width = 0;
            area.height = 0;
            return;
        }
        
        // Set width to a valid initial value
        
        Vector<Widget> widgets = getContents();
        //Collections.sort( widgets );
        Widget first_element = widgets.get(0);
        
        if (debug_statements) System.out.println( "Elements in Palette: " );
        
        area.width = first_element.getWidth();
        
        Iterator<Widget> p_itr = widgets.iterator();
        while ( p_itr.hasNext() )
        {
            Widget w = p_itr.next();
            if (debug_statements) System.out.println("-"+w.getLabel());
        }
        
        Iterator<Widget> s_itr = widgets.iterator();
        while ( s_itr.hasNext() )
        {
            Widget w = s_itr.next();
            positionElement(w);
        }
    }
    
    public void positionElement( Widget s )
    {
        // s.getX&Y so that the proper relative locations of the various 
	// constituents of the species are preserved
	//s.calculatePointerOffset(s.getX(),s.getY()); 

	if (debug_statements) System.out.println("WidgetPalette: Vertical offset="+vertical_offset);
	// Calculate how far down to place the species
            s.setContainingPanel(this);
            s.setSelected( false );
            
            // Calculate offset from the upper left corner of the bounding box (getX, getY)
            // to preserve relative offsets but not absolute positioning from the 
            // Species creation window
            s.calculatePointerOffset(s.getX(),s.getY());
            s.updateLocation( padding, vertical_offset, false );
            
            
        if (debug_statements) System.out.println("*****" +
        "\nX=" + s.getX() +
        "\nY=" + s.getY() +
        "\nHeight=" + s.getHeight() +
        "\nWidth=" + s.getWidth() +
        "\nX Offset=" + s.getXOffset() +
        "\nY Offset=" + s.getYOffset() +
        "\n*****");
            
 	//s.updateLocation(0,vertical_offset, confine_to_boundaries);
	vertical_offset+=s.getHeight()+padding;
        
        //account for zoom
        //vertical_offset*=getZoom(); //<- confusing device space and user space!
        
        // Update the area occupied by containers
            //area.height = vertical_offset;
            area.height = (int)(vertical_offset*getZoom());             
            
            if ( s.getWidth() > area.width ) 
            {
                area.width = s.getWidth() + 2*padding; //2* because we have to account for

                // the lefthand padding as well.
            }
            
            area.width*=getZoom();
            
            // Change the preferred size to reflect the new area
            setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            revalidate();
            
            repaint();
    }    
    
    synchronized public void setZoom(double zoom) 
    {
        setZoom( zoom, true );
    }
    
    synchronized public void setZoom(double zoom, boolean silent) 
    {
        super.setZoom( zoom, silent );
        compressDisplay();
    }
    
}

