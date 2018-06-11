import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
//import java.awt.geom.AffineTransform; // For resizing containers
import java.math.*; 
import java.io.*; // For file IO in write and read object methods
 
import java.awt.image.BufferedImage; 

//public abstract make abstract after everyone converts
public class Operator extends Widget
{
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    private float forward_rate = 0;
    private float reverse_rate = 0;
    
    transient JPopupMenu popup = new JPopupMenu();
    
    // Needed by subclasses
    Operator()
    {
        
    }
   
    public void displayPopupMenu(int x, int y)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem delete = new JMenuItem("Delete");
        delete.addActionListener( getContainingPanel() );
        popup.addSeparator();
        popup.add( delete ); 
        
        popup.show( getContainingPanel(), x, y );
        popup = new JPopupMenu(); // reset the menu after display
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
	// Draw line to make it obvious to the user how the space
	// of species is partitioned by the operators
	double panel_height = containing_panel.panel_dimension.getHeight();

	if ( isSelected() )
	    {
		g2d.setColor( getColor() );
		
          	
                int s_y = getY()-1;
                int s_height = getHeight()+1;
                
                if ( this instanceof Forward )
                {
                    s_y = getY()-8;
                    s_height = getHeight()+8;
                }
                
                if ( this instanceof ForwardAndReverse )
                {
                    s_height = getHeight()+16;
                }
                
                
		g2d.drawRect( getX()-1, s_y, getWidth()+1, s_height );
	    }


	g2d.setColor(Color.red);

        /*
	Stroke def = g2d.getStroke();
	g2d.setStroke (new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
	
        
        g.drawLine( x+width/2, 0, x+width/2, (int)panel_height );
	*/
         
	// Draw the operator icon
        // THis has to be done dynamically because otherwise serialization
        // accross platforms screws up - it tries to save the image in the
        // save file even though its marked transient if you make image
        // a class variable
        
	g2d.drawImage(image, x, y, width, height, containing_panel);

	//g2d.setStroke ( def );
    }


    // Process takes the existing species list and adds n new species as
    // defined below.
    void process( Vector existing_species )
    {
	// 1) Determine the operands. Include the first species to the right
	// and the first species to the left
	

	// 2) Make two vectors one of all the existing species that
	// match the left hand species and one for all those that 
	// match the right hand species

	// 3) // Assume plus operator for now
	// AND all combinations of the left hand side vector and the
	// right hand side vector
	
	// 4) Make a new species for each combination of the 
	// right hand vector and the left hand vector using the
	// result_species as the combinor
    }

    
    public void paint(int x_origin, int y_origin, Graphics2D g2d) 
    {
        int x = getX()-x_origin;
        int y = getY()-y_origin;
        
        URL url = this.getClass().getResource(image_url);
        ImageIcon icon = null;
        try 
        {
            icon = new ImageIcon(url);
        }
        catch ( Exception e )
        {  
            if (debug_statements) System.out.println( "Error Opening Operator Icon URL: The exception was " + e.getMessage() );
            if (debug_statements) System.out.println("This operator is an instance of " + this.getClass().getName() );
            return;
        }
         
        int width = icon.getIconWidth();
        int height = icon.getIconHeight();
		  
        Image  image = icon.getImage();
	g2d.drawImage(image, x, y, width, height, containing_panel);
    }
    
   /*
    public void setReverseRate(float r) 
    {
        reverse_rate = r;
    }
   
    public float getReverseRate() 
    {
        return reverse_rate;
    }
    
    public void setForwardRate(float r) 
    {
        forward_rate = r;
    }
   
    public float getForwardRate() 
    {
        return forward_rate;
    }
     */  
    
   private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
   {
    stream.defaultReadObject();
    
    // setup JPopupMenu
    popup = new JPopupMenu();
   }

    public Image loadImage( String image_url ) 
    {
        URL url = this.getClass().getResource(image_url);
        ImageIcon icon = null;
        Image image = null;
        
        try 
        {
            icon = new ImageIcon(url);
            image = icon.getImage();
        }
        catch ( Exception e )
        {  
            if (debug_statements) if (debug_statements) System.out.println( "Error Opening Operator Icon URL: The exception was " + e.getMessage() );
            if (debug_statements) System.out.println("This operator is an instance of " + this.getClass().getName() );
            return null;
        }
         
        width = icon.getIconWidth();
        height = icon.getIconHeight();
	
        return image;
    }

    protected transient Image image;
}
