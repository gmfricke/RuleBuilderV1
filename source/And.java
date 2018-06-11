/*
 * And.java
 *
 * Created on October 29, 2005, 11:14 PM
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
public class And extends LogicalOperator implements Serializable {
    
    
    private static final long serialVersionUID = 1;
    
    
    And(int x, int y, WidgetPanel containing_panel)  
    {
        //this.label = "and";
		this.x = x;
		this.y = y;
		this.containing_panel = containing_panel;

		image_url = "images/and_op.gif";

                setLabel("And Operator");
        
    }
    
    
    
}
