/*
 * Pattern.java
 *
 * Created on June 7, 2005, 4:00 PM
 */

import java.util.*; //For vector data structure
import java.awt.*; // For JComponent
import javax.swing.*; // For graphical interface tools

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public class Pattern extends BioGraph implements Serializable 
{
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    Pattern( String label, int x, int y, WidgetPanel containing_panel)
    {
	this.containing_panel = containing_panel;
        
        int s_red = 0;
        int us_red = 0;
        int s_blue = 255;
        int us_blue = 0;
        int s_green = 0;
        int us_green = 0;
        int s_alpha = 150;
        int us_alpha = 150;
        
        Color sel_color = new Color( s_red, s_green, s_blue, s_alpha );
        Color unsel_color = new Color( us_red, us_green, us_blue, us_alpha );
        
        setSelectedColor( sel_color );
        setUnselectedColor( unsel_color );
        
        setLabel( label );
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        g2d.setColor(getColor());
	
        /*
        Stroke current_stroke = g2d.getStroke();
        
	g2d.setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	g2d.drawRect( x-5, y-5, width+10, height+10 );
        
        g2d.setColor( c.getBackground() );
        
        g2d.setStroke( current_stroke );
        */
        
        super.display( c, g2d );
    }
}
