/*
 * Domain.java
 *
 * Created on December 31, 2005, 11:30 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import java.awt.*; // For Component
import javax.swing.*; // For GUI tools
import java.net.*; // For URLs (needed to read images from .jar)

/**
 *
 * @author matthew
 */
public class Domain extends Widget
{
    
    private transient Image image;
    
    /** Creates a new instance of Domain */
    public Domain( int x, int y, String label, String image_path, WidgetPanel containing_panel ) 
    {
        this.x = x;
        this.y = y;
        // Don't use the lable
        image_url = image_path;
        
        URL url = this.getClass().getResource(image_url);
        ImageIcon icon = null;
        
        try 
        {
            icon = new ImageIcon(url);
        }
        catch ( Exception e )
        {  
            if (debug_statements) System.out.println( "Error Opening Domain Icon URL: The exception was " + e.getMessage() );
            e.printStackTrace();
            return;
        }
         
        width = icon.getIconWidth();
        height = icon.getIconHeight();
		  
        image = icon.getImage();
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        
	g2d.drawImage(image, x, y, width, height, containing_panel);

    }
}
