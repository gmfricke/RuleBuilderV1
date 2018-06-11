/*
 * SearchPalette.java
 *
 * Created on April 28, 2006, 6:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author matthew
 */
import java.awt.event.*; 

import javax.swing.JTextField;
import javax.swing.JButton;
import java.util.*;

public class SearchPalette extends WidgetPalette
{
    private class TextFieldListener implements ActionListener
    {
        public void actionPerformed(ActionEvent evt) 
        {
            update();
        }
    }
    
    private class ButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent evt) 
        {
            
        }
    }
    
    transient JTextField key = new JTextField(25);
    transient JButton save = new JButton("Apply Edits");
    transient TreeMap<Widget,Widget> map = new TreeMap();
    
    public SearchPalette( GUI the_gui ) 
    {
        super( the_gui );
        
        key.addActionListener( new TextFieldListener() );
        save.addActionListener( new ButtonListener() );
        add( key );
    }
    
    void update()
    {
        initialize();
        
        try
        {
        int position = 30;
        int padding = 10;
        
        String key_string = key.getText();
        
        Vector<Widget> search_vector = new Vector();
        search_vector.addAll( the_gui.getSpeciesPalette().getAllWidgets() );
        search_vector.addAll( the_gui.getReactionRulePalette().getAllWidgets() );
        search_vector.addAll( the_gui.getObservablesPalette().getAllWidgets() );
        //search_vector.addAll( the_gui.getMoleculePalette().getAllWidgets() );
        
        Iterator<Widget> w_itr = search_vector.iterator();
        
        
        while ( w_itr.hasNext() )
        {
            Widget w = w_itr.next();
            
            if ( !(w instanceof FlickrLabel) )
            if ( w.getLabel().contains(key_string) )
            {
                Widget clone = WidgetCloner.clone( w );
                
                clone.setContainingPanel( this );
                clone.resetOffsets();
                clone.updateLocation(20,position,true);
                addWidget( clone );
                position += clone.getHeightWithLabel()+padding;
                
                // Associate the clone with its originator
                map.put( clone, w );
                
                // So selected calls to clone also go to peer
                clone.addPeer(w);
            }
        }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void initialize() 
    {
        Iterator<Widget> w_itr = getAllWidgets().iterator();
        
        while ( w_itr.hasNext() )
        {
            Widget w = w_itr.next();
            
            w.removePeer( map.get(w) );
        }
        
        super.initialize();
    }
    
}
