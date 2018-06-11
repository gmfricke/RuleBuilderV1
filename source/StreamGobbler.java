/*
 * StreamGobbler.java
 *
 * Created on June 14, 2005, 2:06 PM
 */

import java.io.*; // For file manipulation and string writing
import javax.swing.*;
import java.awt.Color;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.undo.*;

 class StreamGobbler extends Thread
{
     transient protected boolean debug_statements = true;
    InputStream is;
    String type;
    JTextPane pane;
    
    StreamGobbler(InputStream is, String type, JTextPane p )
    {
        this.is = is;
        this.type = type;
        pane = p;
        
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String new_line=null;
                
            while ( (new_line = br.readLine()) != null)
            {
                final String line = new_line;
                Runnable AddLine = new Runnable() {
                public void run() 
                {
                    
                try
                {
                    
                if (debug_statements) System.out.println(type + "> " + line );
                
                String complete_line = line + "\n";
                    int length = line.length();
                    SimpleAttributeSet sas = new SimpleAttributeSet();
                    StyleConstants.setFontFamily(sas, "SansSerif");
                    StyleConstants.setFontSize(sas, 12);
                    
                
                if ( type.equals("ERROR") )
                {
                    StyleConstants.setForeground(sas, Color.red);
                    StyledDocument styled_doc = pane.getStyledDocument();
                    Position position = styled_doc.getEndPosition();
                    int offset = position.getOffset();
                    styled_doc.insertString(offset, complete_line, sas );
                }
                else
                {
                    StyleConstants.setForeground(sas, Color.black);
                    StyledDocument styled_doc = pane.getStyledDocument();
                    Position position = styled_doc.getEndPosition();
                    int offset = position.getOffset();
                    styled_doc.insertString(offset, complete_line, sas );
                }
                   
                int height = (int) pane.getBounds().getHeight();
                pane.scrollRectToVisible(new Rectangle(new Point(0, height+StyleConstants.getFontSize(sas))));
                if (debug_statements) System.out.println("Scrolling Log to " + (height+StyleConstants.getFontSize(sas)));
                //pane.repaint();
                } 
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                }
                };
                SwingUtilities.invokeLater(AddLine);
            }
                
                  
            } 
            catch (Exception e)
              {
                e.printStackTrace();  
              }
    }
}