/*
 * ReactionRulePanel.java
 *
 * Created on January 9, 2005, 12:48 PM
 */
 
/**
 *
 * @author  Matthew Fricke
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;

import java.net.*; // For URLs 

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class ReactionRulePalette extends WidgetPalette 
{
    protected class KeyboardControl implements KeyListener 
    {
        KeyboardControl(){}
        
        public void keyPressed(KeyEvent evt) {
            if (debug_statements) System.out.println("Key Press Detected");
            
            int key = evt.getKeyCode();  // keyboard code for the key that was pressed
            
            if (evt.isControlDown() && key == KeyEvent.VK_C ) {
                copySelectedWidget();
            }
            else if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE ) 
            {
                if (debug_statements) System.out.println("Erase Key Press Detected");
                
                    if ( selected_reactionrule != null )
                    {
                        removeReactionRule( selected_reactionrule );
                        compressDisplay();
                
                        clearSelections();
                    }
            } 
            else if ( key == KeyEvent.VK_MINUS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*0.95);
            }
            else if ( key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*1.05);
            }
            else if ( key == KeyEvent.VK_UP ) 
            {
                int current_ytrans = getYTranslation();
                setYTranslation( current_ytrans - 10 );
            }
            else if ( key == KeyEvent.VK_DOWN ) 
            {
                int current_ytrans = getYTranslation();
                setYTranslation( current_ytrans + 10 );
            }            
	    else if ( key == KeyEvent.VK_LEFT ) 
            {
                int current_xtrans = getXTranslation();
                setXTranslation( current_xtrans - 10 );
            }
            else if ( key == KeyEvent.VK_RIGHT ) 
            {
                int current_xtrans = getXTranslation();
                setXTranslation( current_xtrans + 10 );
            }         
            else 
	    {
                if (debug_statements) System.out.println("Unhandled keyboard event.");
            }
            
            
        }
        
        public void keyReleased(KeyEvent evt) {
            
        }
        
        public void keyTyped(KeyEvent evt) {
            
        }
    }
 
    private class MouseControl extends MouseInputAdapter implements DragGestureListener
    {
	
        private WidgetPalette palette;
        
        public MouseControl(WidgetPalette palette) 
        {
           this.palette = palette;
        }
    
    // Handles the event of the user pressing down the mouse button.
    // Here the program determines which widget the mouse pointer was over a
    // when the button was pressed. If the widget pressed is a template then
    // a new widget is created and assigned to be the selected widget. If the
    // widget pressed is not a template then it is the selected widget.
    
	public void mousePressed(MouseEvent e)
	{
            
            
            // Compensate for zoom and translation
            // getZoomedX(Y)Translation should always be 0 for WidgetPalettes
            int mouse_x = (int)(((e.getX()-getZoomedXTranslation())/getZoom()));
            int mouse_y = (int)(((e.getY()-getZoomedYTranslation())/getZoom()));    
            
            clearSelections();
            
            requestFocus(); // Needed so that if the user presses a key the
            // JComponent will get the key press
	    
            // Determine which reaction rule was selected.
            
            Iterator i = rules.iterator();
	    while ( i.hasNext() )
		{
		    ReactionRule rr = (ReactionRule)i.next();
		    if ( rr.contains( mouse_x, mouse_y ) ) 
		    {
			if (debug_statements) System.out.println("Reaction Rule Pressed");
                       
			selected_reactionrule = rr;
                        selected_reactionrule.setSelected(true);
                        
                        if ( e.isPopupTrigger() )
			    {
				selected_reactionrule.displayPopupMenu( e );
				return;
			    }
			
		    }
                }
        }

    

    // Handles the event of a user dragging the mouse while holding
    // down the mouse button.
    public void mouseDragged(MouseEvent e)
	{    
	   if (debug_statements) System.out.println("Mouse drag detected in ReactionRule Palette");
         
           /*
            JComponent c = (JComponent) e.getSource();
            TransferHandler th = c.getTransferHandler();
            if (th != null) 
            {
                th.exportAsDrag(c, e, DnDConstants.ACTION_MOVE );
            }
            else
            {
                displayError("Error in Drag 'n Drop of ReactionRule","ReactionRule Handler was NULL. Contact support at support@bionetgen.com");
            }
            */
   
	}

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e)
    {
        
        if ( selected_reactionrule != null && e.isPopupTrigger() )
        {
            selected_reactionrule.displayPopupMenu( e );
            return;
	}
	
	
    }
       
     // This method is required by MouseListener.
     public void mouseMoved(MouseEvent e){}

     // These methods are required by MouseMotionListener.
     public void mouseClicked(MouseEvent e){}
     public void mouseExited(MouseEvent e){}
     public void mouseEntered(MouseEvent e){}

     public void dragGestureRecognized(DragGestureEvent dge) 
     {
            Widget w = getSelectedWidget();
            Image image = w.createImage();
            int height = w.getHeight();
            int width = w.getWidth();
        
            WidgetDragHandler listener = getTheGUI().getWidgetDragHandler();
            
            dge.startDrag(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR ),// cursor
                        image,
                        new Point(-width/2,-height/2),
			palette.getSelectedWidget(), // transferable
			listener);  // drag source listener
     }
     

     
}

        
    // Serialization explicit version
    private static final long serialVersionUID = 1;

    // Transient so StateSaver wont try to serialize it
    transient private MouseControl local_mouse_control = new MouseControl( this );
    transient private KeyboardControl local_keyboard_control = new KeyboardControl();
    
    //ScrollControl scroll_control = new ScrollControl();
    
    /** Creates a new instance of SpeciesPanel */
    public ReactionRulePalette( GUI the_gui ) 
    {
        super( the_gui );
        removeMouseMotionListener( mouse_control );
        removeMouseListener(mouse_control);
        addMouseMotionListener(local_mouse_control);
	addMouseListener(local_mouse_control);

        removeKeyListener( keyboard_control );
        
        addKeyListener( local_keyboard_control );
        
        // Setup Drag Listener
        DragSource dragSource = DragSource.getDefaultDragSource();
        // creating the recognizer is all that's necessary - it
        // does not need to be manipulated after creation
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(
        this, // component where drag originates
        DnDConstants.ACTION_COPY_OR_MOVE, // actions
        local_mouse_control); // drag gesture listener
        
        area = new Dimension(0,0);
        
     	setBackground(Color.white);
    }
   
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Delete") )
	{
            if ( selected_reactionrule != null )
            {
                removeReactionRule( selected_reactionrule );
                return;
            }
        }
        
        super.actionPerformed( action );
    }
    
    // Template for addRule
    synchronized boolean addReactionRule( ReactionRule rr )
    {
	
	if ( rules.indexOf( rr ) != -1 || rr == null ) 
	    {
		return false;
	    }

        while ( null != getTheGUI().getModel().getRule( rr.getLabel() ) )
        {
            String new_name = displayInputQuestion("Rule Exists", "A rule with the label \""+rr.getLabel()+"\" already exists.\n" +
                "Please enter a unique label.");
                
                if (new_name == null)
                {
                    //user_cancelled
                    return false;
                }
                
                rr.setLabel(new_name);    
        }
        
	rr.setContainingPanel(this);

        rr.setSelected( false );
        
        /*
        
	boolean confine_to_boundaries = false;

	// s.getX&Y so that the proper relative locations of the various 
	// constituents of the species are preserved
	rr.calculatePointerOffset(rr.getX(),rr.getY()); 

	// Calculate how far down to place the species
            rr.setContainingPanel(this);
            rr.setSelected( false );
            rr.calculatePointerOffset(rr.getX(),rr.getY()); 
            rr.updateLocation( padding, vertical_offset, false );
            
 	//s.updateLocation(0,vertical_offset, confine_to_boundaries);
	vertical_offset+=rr.getHeight()+padding;
         
         
         */ 
        
        //positionElement( rr );
	super.addReactionRule( rr );
        compressDisplay();

        /*
        // Update the area occupied by reaction rules
            area.height = vertical_offset;
                         
            if ( rr.getWidth() > area.width ) 
            {
                area.width = rr.getWidth() + 2*padding; //2* because we have to account for
                // the lefthand padding as well.
            }
                
            // Change the preferred size to reflect the new area
            setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            revalidate();
            
            repaint();
	*/
        
            // Add to model
            the_gui.getModel().addReactionRule( rr );
            
	return true;
    }
      
  /*
    public void compressDisplay()
    {
        vertical_offset = padding;
        
        if ( getAllReactionRules().isEmpty() ) 
        {
            area.width = 0;
            area.height = 0;
            return;
        }
        
        
        // Set width to a valid initial value
        ReactionRule first_rule = (ReactionRule)getAllReactionRules().get(0);
        
        area.width = first_rule.getWidth();
        
        Iterator rr_itr = this.getAllReactionRules().iterator();
        while ( rr_itr.hasNext() )
        {
            positionReactionRule((ReactionRule)rr_itr.next());
        }
    }
    
    public void positionReactionRule( ReactionRule rr )
    {
        if (debug_statements) System.out.println("Positioning Reaction Rule");
        // s.getX&Y so that the proper relative locations of the various 
	// constituents of the reaction rule are preserved
	rr.calculatePointerOffset(rr.getX(),rr.getY()); 
        rr.updateLocation( padding, vertical_offset, false );
            
	vertical_offset+=rr.getHeight()+padding;
        if (debug_statements) System.out.println("Vertical Offset is now: " + vertical_offset);
        
        //vertical_offset*=getZoom();
        // Update the area occupied by containers
            //area.height = vertical_offset;
            area.height = (int)(vertical_offset*getZoom());             
            
            if ( rr.getWidth() > area.width ) 
            {
                area.width = rr.getWidth() + 2*padding; //2* because we have to account for
                // the lefthand padding as well.
            }
            
            area.width*=getZoom();
            
            // Change the preferred size to reflect the new area
            setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            revalidate();
            
            repaint();
    }
    */
   
    synchronized public void removeReactionRule(ReactionRule rr) 
    {
        if (debug_statements) System.out.println("Removing Reaction Rule in the Reaction Rule Palette");
        super.removeReactionRule( rr );
        
        compressDisplay();
        clearSelections();
        refreshAll();
        
        // Remove from model too
        getTheGUI().getModel().removeReactionRule( rr );
    }    
    
    synchronized public boolean pasteCopiedWidget( int x, int y ) 
    {
        Widget copied_widget = getTheGUI().copied_widget;
        
        if ( !(copied_widget instanceof ReactionRule) )
        {
            displayError("Error Pasting " + copied_widget.getClass().getName(),
            "Only Reaction Rules may be pasted into the Reaction Rule palette." );
            return false;
        }
        
        ReactionRule rr = null;
        try
        {
            rr = (ReactionRule)WidgetCloner.clone(copied_widget);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        addReactionRule( rr );
        return true;
    }
   
    public boolean cutSelectedWidget()
    {
                        copySelectedWidget();
                        removeReactionRule( selected_reactionrule );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                        
                        return true;
    }
}
