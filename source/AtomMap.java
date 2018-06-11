/*
 * AtomMap.java
 *
 * Created on December 7, 2005, 2:24 PM
 */

import java.beans.*; // For java bean object support methods
import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
//import java.awt.geom.AffineTransform; // For resizing containers
import java.awt.geom.*;
import java.awt.*;

/**
 * @author matthew
 *
 * AtomMap inherits from Edge in order to leverage the functions already defined there
 * for handling widget to widget connections. 
 */
public class AtomMap extends Edge
{
    public AtomMap( Mappable start, Mappable end, WidgetPanel containing_panel ) 
    {
    
        this.start = start;
        this.end = end;
            
        setContainingPanel( containing_panel );
	selected = false;
        
        if ( start.getAtomMap() != null )
            {
                AtomMap existing_map = start.getAtomMap();
                Mappable existing_map_start = (Mappable)existing_map.getStart();
                Mappable existing_map_end = (Mappable)existing_map.getEnd();
                
                // Erase the existing map
                existing_map_end.setAtomMap(null);
                existing_map_start.setAtomMap(null);
                if (debug_statements) System.out.println("AtomMap(): there are " + containing_panel.getAllEdges().size() + " edges." );
                containing_panel.removeEdge(existing_map);
                if (debug_statements) System.out.println("AtomMap(): there are " + containing_panel.getAllEdges().size() + " edges." );
                
                if (debug_statements) System.out.println("Disconnecting Existing Map ("+existing_map.getLabel()+")");
                
            }
            
            
            if ( end.getAtomMap() != null )
            {
                
                AtomMap existing_map = end.getAtomMap();
                Mappable existing_map_start = (Mappable)existing_map.getStart();
                Mappable existing_map_end = (Mappable)existing_map.getEnd();
                
                // Erase the existing map
                existing_map_end.setAtomMap(null);
                existing_map_start.setAtomMap(null);
                
                if (debug_statements) System.out.println("AtomMap(): there are " + containing_panel.getAllEdges().size() + " edges." );
                containing_panel.removeEdge(existing_map);
                if (debug_statements) System.out.println("AtomMap(): there are " + containing_panel.getAllEdges().size() + " edges." );
                
                
                if (debug_statements) System.out.println("Disconnecting Existing Map ("+existing_map.getLabel()+")");
            }
            

	    start.setAtomMap(this);
            end.setAtomMap(this);
            
            long id = getID();
            
            setLabel( Long.toString(id) );
            updateLocation(); // to place flickrlable properly
            //label.setContainingPanel( containing_panel );
            
    }
    
    public void actionPerformed( ActionEvent e ) 
    {
        if ( e.getActionCommand().equals("Rename") ) 
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
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        Composite current_comp = g2d.getComposite();
        // Set the selected appearance
        if ( start.isSelected() || end.isSelected() || isSelected() )
        {
            g2d.setColor(Color.BLUE);
        }
        else // the unselected appearence
        {
            //line = new Line2D.Float();
            //if(true) return;  // Don't show at all
            //Color col = new Color(10,10,10);
            Color col = Color.BLACK;
            g2d.setColor(col);
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
        }
        
        
        
        // We don't want to highlight this map if the container of a start or
        // end component is highlighted because the user probably just wants to see
        // the container map
        // Check the start
        /*
        if ( start instanceof BioComponent )
        {
            BioContainer start_cont = ((BioComponent)start).getContainer();
            if ( start_cont != null )
            {
                if ( start_cont.isSelected() )
                {
                    //line = new Line2D.Float();
                    //if(true) return; // Don't show at all
                    Color col = Color.LIGHT_GRAY;
                    g2d.setColor(col);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.0f));
                }
            }
        }
        
        // Check the end
        if ( end instanceof BioComponent )
        {
            BioContainer end_cont = ((BioComponent)end).getContainer();
            if ( end_cont != null )
            {
                if ( end_cont.isSelected() )
                {
                    //line = new Line2D.Float();
                    //if(true) return; // Don't show at all
                    Color col = Color.LIGHT_GRAY;
                    g2d.setColor(col);
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.0f));
                }
            }
        }
        
         */
        
            Point start_point = start.getEdgeAttachPoint();
            Point end_point = end.getEdgeAttachPoint();
            
            int start_x = (int)start_point.getX();
            int start_y = (int)start_point.getY();
            int end_x = (int)end_point.getX();
            int end_y = (int)end_point.getY();
            
	    line = new Line2D.Float( start_x, start_y, end_x, end_y );
        
            //label.updateLocation( start_x + end_x/2, start_y + end_y/2, false );
            
            Stroke default_stroke = g2d.getStroke();
                        
            g2d.setStroke(new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
            
            g2d.draw(line);
            
            // Reset the "ghosting" effect
            //g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
            
            g2d.setColor(Color.BLUE);
            g2d.drawString( getLabel(), start_x+10, start_y+10 );
            g2d.drawString( getLabel(), end_x-10, end_y-10 );
            
            g2d.setStroke( default_stroke );
            
            // Reset the "ghosting" effect
            g2d.setComposite( current_comp );            
            g2d.setColor(c.getBackground());
    }
   
    public void displayPopupMenu( int mouse_x, int mouse_y ) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem menu_rename = new JMenuItem( "Rename" );
        JMenuItem menu_delete = new JMenuItem( "Delete" );
     
        menu_rename.addActionListener( this );
        menu_delete.addActionListener( getContainingPanel() );
        
        JPopupMenu popup = new JPopupMenu();
        popup.add("Options");
        popup.addSeparator();
        popup.add( menu_rename );
        popup.add( menu_delete );
        
        popup.show( getContainingPanel(), mouse_x, mouse_y );
    }
    
}
