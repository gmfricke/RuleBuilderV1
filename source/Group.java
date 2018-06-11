/*
 * Group.java
 *
 * Created on October 30, 2005, 1:44 AM
 */

// A group is a collection of patterns and logical operators

import java.beans.*;
import java.io.Serializable;
import java.util.*; // For vector
import java.awt.*; // For JComponent
import java.awt.event.*; // For window adapter
import javax.swing.*; // For graphical interface tools

/**
 * @author Matthew Fricke
 */
public class Group extends Operation implements Serializable, ActionListener 
{   
    private class PropertiesDialogCancel extends WindowAdapter implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            handleClose();
        }
    
        public void windowClosing(WindowEvent e)
        {
            handleClose();
	}
    
        private void handleClose()
        {
            
        nametype_dialog.setVisible( false );
        nametype_dialog.dispose();
        
        getContainingPanel().getTheGUI().getObservablesPalette().repaint();
    }
  }
    
    private class PropertiesDialogCloser extends WindowAdapter implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            handleClose();
        }
    
        public void windowClosing(WindowEvent e)
        {
            handleClose();
	} 
    
        private void handleClose()
        {
        
        type = (String)types.getSelectedItem();
        String label = textfield.getText();
        
        if ( label.length() == 0 )
        {
            getContainingPanel().displayError("Error Setting Observables Properties","The observable's name was left blank.");
            return;
        }
        
        setLabel( label );
        
        nametype_dialog.setVisible( false );
        nametype_dialog.dispose();
        
        getContainingPanel().getTheGUI().getObservablesPalette().repaint();
    }
  }
     // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    protected String type = null;
    
    transient private JDialog nametype_dialog;
    transient private JComboBox types;
    transient private JTextField textfield;
    
    // For subclasses
    //Group()
    //{
        
    //}
    
    Group( String label, String type, Vector<Pattern> patterns, Vector<Operator> operators, WidgetPanel containing_panel )
    {
        this.containing_panel = containing_panel; 
        
        setPatterns( patterns );
        this.operators = operators;
        setResults( new Vector<BioGraph>() ); // Results are empty for groups
        
        setType( type );
        setLabel( label );
        
        if (debug_statements) System.out.println("Added " + this.operators.size() + " operators.");
    }
    
    //public void display( Component c, Graphics2D g2d )
    //{
        //Color current = g2d.getColor();
        //g2d.setColor( Color.BLACK );
        //g2d.drawString( getLabel() + " {" + type + "}", getX(), getY()-2 );
        //g2d.setColor( current );
        
        
        //super.display( c, g2d);
    //}
    
    public Vector<Operator> getOperators()
    {
        return operators;
    }
   
    public void setOperators()
    {
        this.operators = operators;
    }
    
    public String getType()
    {
        return type;
    }
    
    public void setType( String type )
    {
        this.type = type;
    }
    
    public void setPropertiesFromUser()
    {
        JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            nametype_dialog = new JDialog( owner, true );
            nametype_dialog.setTitle( "Properties" );
            nametype_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            nametype_dialog.addWindowListener( new PropertiesDialogCloser() );
            
            Container content = nametype_dialog.getContentPane();
            
            JPanel button_panel = new JPanel();
            JPanel name_panel = new JPanel();
            JPanel type_panel = new JPanel();
            
            textfield = new JTextField(getLabel(), 10);
            
            types = new JComboBox();
            types.addItem("Molecules");
            types.addItem("Species");
            
            JButton done = new JButton("Done");
            JButton cancel = new JButton("Cancel");
            done.addActionListener( new PropertiesDialogCloser() );
            
            name_panel.add( new JLabel("Name: "), BorderLayout.WEST );
            
            name_panel.add( textfield, BorderLayout.EAST );
            
            type_panel.add( new JLabel("Type: "), BorderLayout.WEST );
            type_panel.add( types, BorderLayout.EAST );
            
            button_panel.add( done, BorderLayout.CENTER );
            //button_panel.add( cancel, BorderLayout.EAST );
            
            content.add( name_panel, BorderLayout.NORTH );
            content.add( type_panel, BorderLayout.CENTER );
            content.add( button_panel, BorderLayout.SOUTH );
            
            
            nametype_dialog.pack();
            
            nametype_dialog.setLocation( 200, 100 );  
            nametype_dialog.setSize( 200, 200 );  
            
            
            nametype_dialog.setVisible(true);
            
    }

    public Vector<Pattern> getPatterns() 
    {
        Vector<Pattern> patterns = new Vector();
        Iterator<BioGraph> itr = getBioGraphs().iterator();
        while ( itr.hasNext() )
        {
            patterns.add( (Pattern)itr.next() );
        }
        
        return patterns;
    }

    public void setPatterns(Vector<Pattern> p) 
    {
        Vector<BioGraph> b = new Vector();
        b.addAll(p);
        setOperands( b );
    }
    
    public void displayPopupMenu(int mouse_x, int mouse_y ) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem disband_menu_item = new JMenuItem( "Disband" );
	JMenuItem add_menu_item = new JMenuItem( "Add to Observables List" );
        JMenuItem prop_menu_item = new JMenuItem( "Properties" );
        JMenuItem del_menu_item = new JMenuItem( "Delete" );
        
        disband_menu_item.addActionListener( this );
        add_menu_item.addActionListener( this );
        prop_menu_item.addActionListener( this );
        del_menu_item.addActionListener( getContainingPanel() );
        
        // Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
        popup.addSeparator();  
        popup.add( add_menu_item );
        popup.addSeparator();
        popup.add( disband_menu_item );
        popup.add( del_menu_item );
        popup.add( prop_menu_item );
        popup.show( containing_panel, mouse_x, mouse_y );
    }
    
    public void actionPerformed( ActionEvent action )
    {  /*
        if ( action.getActionCommand().equals("Disband") )
	{
            
            // needs implementing
            
                getContainingPanel().removeGroup( this );
                getContainingPanel().getAllComponents().addAll( getComponents() );
                getContainingPanel().getAllContainers().addAll( getContainers() );
                getContainingPanel().getAllEdges().addAll( getEdges() );
                
                // Afterwards this species is invalid
                for ( int j = 0; j < getComponents().size(); j++ ) 
                {
                    getComponents().get(j).setBioGraph(null);
                }
            
                for ( int j = 0; j < getContainers().size(); j++ ) 
                {
                    getContainers().get(j).setBioGraph(null);
                }
                
                setSelected(false);
                getContainingPanel().repaint();
            //}
            
        }
        else if ( action.getActionCommand().equals("Auto-Layout") )
	{
            layout();
        }
             */
        if ( action.getActionCommand().equals("Properties") )
	{
            setPropertiesFromUser();    
        }
        else if ( action.getActionCommand().equals("Add to Observables List") )
        {
            Group copy = null;
            try
            {
                 copy = (Group)WidgetCloner.clone( this );
            }
            catch ( Exception e )
            {
                getContainingPanel().displayError("Internal Error while Cloning Group","The exception message was " + e.getMessage() + "\n" +
                "\nContact support at support@bionetgen.com");
            }
            
            getContainingPanel().getTheGUI().getObservablesPalette().addGroup( copy );
        }
        else if ( action.getActionCommand().equals("Disband") )
	{
            /*
            if ( getContainingPanel().displayQuestion("Disband Species","Disbanding this BioGraph will" +
            " replace it with its constituent containers, edges, and components. " +
            "If this is a species the concentration data will be lost.") )
            {
             */
                // Edges must be added to the panel after the biograph is
                // removed because removing the biograph removes all edges
                // it contains from the panel
                disband();
            //}
            
        }
         
        else
        {
            getContainingPanel().displayError("Internal Error","Unknown action command \"" + action.getActionCommand() + "\" in Group::actionPerformed(). Contact support at support@bionetgen.com." );
        }
    }
    
}
