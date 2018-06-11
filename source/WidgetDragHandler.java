/*
 * WidgetDragListener.java
 *
 * Created on November 21, 2005, 6:28 AM
 */

import java.beans.*;
import java.io.Serializable;

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import javax.swing.*; // For swing components

import java.net.*; //For URL image loading from Jar files
import java.awt.*; // For graphical windowing tools

/**
 * @author matthew
 */
public class WidgetDragHandler extends Object implements Serializable, DragSourceListener
{
    transient protected boolean debug_statements = true;
    transient protected Cursor no_drop;
    transient protected Cursor drop_ok;
    
    WidgetDragHandler()
    {
        Image image1 = loadImage("images/no_drop.png");
            
            Point hotspot = new Point( 5,5 );
            String name1 = "no drop";
            String name2 = "drop ok";
            
            Toolkit tk = Toolkit.getDefaultToolkit();
            no_drop = tk.createCustomCursor( image1, hotspot, name1 );
            
            Image image2 = loadImage("images/drop_ok.png");
            
            drop_ok = tk.createCustomCursor( image2, hotspot, name2 ); 
    }
    
        public void dragDropEnd(DragSourceDropEvent dsde) {
    }
    
    public void dragEnter(DragSourceDragEvent dsde) 
    {
        int valid = dsde.getTargetActions();
        
        if (debug_statements) System.out.println("Target drop action: " + valid );
        
        if ( valid == 1 )
        {
            dsde.getDragSourceContext().setCursor( no_drop );
        }
        else
        {
            dsde.getDragSourceContext().setCursor( drop_ok );
        }
    }
    
    public void dragExit(DragSourceEvent dse) {
    }
    
    public void dragOver(DragSourceDragEvent dsde) 
    {
        int valid = dsde.getTargetActions();
        
        if (debug_statements) System.out.println("Target drop action: " + valid );
        
        if ( valid == 1 )
        {
            dsde.getDragSourceContext().setCursor( no_drop );
        }
        else
        {
            dsde.getDragSourceContext().setCursor( drop_ok );
        }
        
        /*
        if (debug_statements) System.out.println("Drag Over Fired");
        
        try
        {
            if ( !((WidgetPanel)dsde.getDragSourceContext().getComponent()).isInsideBoundaries( dsde.getX(), dsde.getY() ) )
            {
                return;
            }
        }
        catch( ClassCastException e )
        {
            displayError("Sanity Check Failed","The source for this drag operation is a \""+dsde.getSource().getClass().getName()+"\" not a WidgetPanel. Contact support@bionetgen.com");
            return;
        }
        
        Transferable t = (Transferable)dsde.getDragSourceContext().getTransferable();
        Widget w = null;
        try
        {
            w = (Widget)t.getTransferData( new DataFlavor( w.getClass(), "Widget") );
        }
        catch ( UnsupportedFlavorException ude )
        {
            displayError("Error Displaying Drag Image", "The class " + w.getClass().getName() + " is unsupported.");
            return;
        }
        catch ( Exception e )
        {
            displayError("Error Displaying Drag Image", "The exception was due to " + e.getMessage() );
            return;
        }
        
        if ( w != null )
        {
            if (debug_statements) System.out.println("Container dragged over panel.");
            
            Graphics2D g2 = (Graphics2D) getGraphics();
 
            // Erase the last ghost image and cue line
            //paintImmediately(drag_ghost_rect.getBounds());    
            
            Image image = null;
            if ( w instanceof SelectionBox )
            {
                image = ((SelectionBox)w).createImage();
            }
            else if ( w instanceof BioGraph )
            {
                image = ((BioGraph)w).createImage();
            }
            else
            {
                displayError("Sanity Check Failed","The source for this drag operation is a \""+dsde.getSource().getClass().getName()+"\" not a WidgetPanel. Contact support@bionetgen.com");
                return;
            }
            
            int height = w.getHeight();
            int width = w.getWidth();
            double x = dsde.getLocation().getX();
            double y = dsde.getLocation().getY();
            
            // Remember where you are about to draw the new ghost image
            drag_ghost_rect.setRect(x, y, width, height );
 
            // Draw the ghost image
            g2.drawImage(image, 
                         AffineTransform.getTranslateInstance(drag_ghost_rect.getX(),
                                                  drag_ghost_rect.getY()), 
                         null);                
     }
        else
        {
            displayError("Error Displaying Drag Image", "The data object was null. Contact support@bionetgen.com" );
            return;
        }
         */
    }
    
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }
    
    // Utility method
    private Image loadImage(java.lang.String path) 
    {
        URL url = this.getClass().getResource(path);
        ImageIcon icon = null;
        
        try 
        {
            icon = new ImageIcon(url);
        }
        catch ( Exception e )
        {  
            if (debug_statements) System.out.println( "Error Opening Operator Icon URL: The exception was " + e.getMessage() );
            if (debug_statements) System.out.println("This operator is an instance of " + this.getClass().getName() );
            return null;
        }
        
        Image  image = icon.getImage();
        return image;
    }
    
}
