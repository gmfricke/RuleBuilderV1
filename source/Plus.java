/*
 * Plus.java
 *
 * Created on September 19, 2005, 11:20 PM
 */

import java.net.*; //For URL image loading from Jar files
import javax.swing.*; // For graphical interface tools
import java.awt.*; // For graphical windowing tools

import java.beans.*;
import java.io.Serializable;

import java.io.*; // For file IO in write and read object methods

/**
 * @author matthew
 */
public class Plus extends Operator implements Serializable {
    
    private static final long serialVersionUID = 1;
    
    
    Plus( int x, int y, WidgetPanel containing_panel )  
    {
        //this.label = "plus";
		this.x = x;
		this.y = y;
		this.containing_panel = containing_panel;

		image_url = "images/plus_op.png";
                image = loadImage( image_url );
                setLabel(" ");
                
        
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
    
        image = loadImage( image_url );
    }
}
