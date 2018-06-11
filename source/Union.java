/*
 * Union.java
 *
 * Created on Oct 31, 2005, 3:14 PM
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
public class Union extends LogicalOperator implements Serializable {

    private static final long serialVersionUID = 1;
    
    
    Union(int x, int y, WidgetPanel containing_panel)  
    {
                this.x = x;
		this.y = y; 
		this.containing_panel = containing_panel;

		image_url = "images/union_op.png";
                image = loadImage( image_url );
                setLabel("Union Operator");
                
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
    
        image = loadImage( image_url );
    }
}
