/*
 * Or.java
 *
 * Created on Oct 29, 2005, 11:13 PM
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
public class Or extends LogicalOperator implements Serializable {

    private static final long serialVersionUID = 1;
    
    
    Or(int x, int y, WidgetPanel containing_panel)  
    {
                //this.label = "or";
		this.x = x;
		this.y = y;
		this.containing_panel = containing_panel;

		image_url = "images/or_op.gif";
                
                setLabel("Or Operator");

    }
    
    
}
