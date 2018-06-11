import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable

import javax.swing.text.*; // for input dialog

public class RulePanel extends WidgetPanel implements ActionListener
{
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    private int vertical_offset;
    private int padding;

    public RulePanel()
    {


    }

    

    // Custom popup menu handlers shadow methods in WidgetHander
// For the popup menu
    /**
     *
     * @param e
     */    
    public void actionPerformed( ActionEvent e )
    {
	// Add action handling code here
	if (debug_statements) System.out.println( e );
	if (debug_statements) System.out.println( e.getActionCommand() );
	if (selected_component == null && selected_container == null )
	    {
		if (debug_statements) System.out.println("Fatal Error: Nothing selected yet context menu displayed. Exiting..." );
		System.exit(1);
	    }

	if (selected_component != null && selected_container != null )
	    {
		if (debug_statements) System.out.println("Fatal Error: A container and a component are both selected. This shouldn't be able to happen. Exiting..." );
		System.exit(1);
	    }
	
	// From state menu
	//if ( e.getActionCommand().equals("Phosphorylated") )
	//    {
	//	selected_component.setState("P");
	//    }
	//else if ( e.getActionCommand().equals("UnPhosphorylated") )
	//    {
	//	if (debug_statements) System.out.println("Change state to UP:");
	//	selected_component.setState("UP");
	//    }
	//	else if ( e.getActionCommand().equals("Unspecified") )
	//    {
	//	selected_component.setState("?");
	//    }

	// From options menu
	
	else if ( e.getActionCommand().equals("Rename") )
	    {
	        // Display dialog box
		Widget to_be_renamed;

		if ( selected_container != null )
		    {
			to_be_renamed = selected_container;
		    }
		else 
		    {
			to_be_renamed = selected_component;
		    }
			

		String new_label = (String)JOptionPane.showInputDialog( 
								       this,
								       "Enter new label",
								       null,
								       JOptionPane.PLAIN_MESSAGE,
								       null,
								       null,
								       to_be_renamed.getLabel());
			
		
		if ( new_label != null )
		    {
			to_be_renamed.setLabel( new_label );
		    }
		else 
		    {
			if (debug_statements) System.out.println( "Rename cancelled by user after editing the input text box" );
		    }

		repaint();
	    }
	else if ( e.getActionCommand().equals("Delete") )
	    {
		if ( selected_component != null )
		    {
			if (debug_statements) System.out.println("Deleting Component");
			// remove the incident edges first
			selected_component.removeAllEdges();
			
			// remove from parent container if it has one
			if ( selected_component.getContainer() != null )
			    {
				selected_component.getContainer().removeComponent(selected_component);
			    }

			components.remove(selected_component);
			selected_component = null; // display will use this otherwise
			repaint();
		    }
		else if ( selected_container != null )
		    {
			if (debug_statements) System.out.println("Deleting Container");

			for ( int i = 0; i < selected_container.getComponents().size(); i++ )
			    {
				((BioComponent)selected_container.getComponents().get(i)).removeAllEdges();
			    }
			
			containers.remove(selected_container);
			selected_container = null; // display will use this otherwise
			repaint();
		    }
	    }
	// From context menu change state submenu
	else if ( e.getActionCommand().equals("Phosphorylated") )
	    {
		selected_component.setState( "P" );
	    }
	else if ( e.getActionCommand().equals("UnPhosphorylated") )
	    {
		selected_component.setState( "UP" );
	    }
	else if ( e.getActionCommand().equals("Unspecified") )
	    {
		selected_component.setState( null );
	    }
	// Unknown action: display an error and quit
	else 
	    {
		if (debug_statements) System.out.println("Fatal Error: Unknown item selected from context menu! Exiting..." );
		System.exit(1);
	    }

      

	// Action completed. Undo selection.
	
	clearSelections();
    }
    
    void addWidget( Widget widget, int x, int y )
    {
		
        if (debug_statements) System.out.println("Add widget run");
        clearSelections();

	if ( widget instanceof BioComponent )
	    {
		
		selected_component = (BioComponent)widget;
		//selected_component.setSelected(true);
		
		if (selected_container != null) 
		    {
			selected_container.setSelected(false);
			selected_container = null;
		    }


		if ( selected_component == null )
		    {
			if (debug_statements) System.out.println("Selected Widget null!");
		    }
		
		selected_component.x = x;
		
		selected_component.y = y;
		
		selected_component.setContainingPanel( this );
		
		components.add(selected_component);

		// Perhaps this code should be in the BioComponent constructor?
		
		JMenuItem menu_phosphorylated = new JMenuItem( "Phosphorylated" );
                JMenuItem menu_unphosphorylated = new JMenuItem( "UnPhosphorylated" );
                JMenuItem menu_unspecified = new JMenuItem( "Unspecified" );
                
		// Context menu for setting the state of the new component
		JPopupMenu popup = new JPopupMenu();
		popup.add("State");
		popup.addSeparator();
		popup.add(menu_phosphorylated);
		popup.add(menu_unphosphorylated);
		popup.add(menu_unspecified);
		popup.show( this, x, y);
		
		menu_phosphorylated.addActionListener( this );
                menu_unphosphorylated.addActionListener( this );
                menu_unspecified.addActionListener( this );
                
	    }
	else if ( widget instanceof BioContainer )
	    {
		
		selected_container = (BioContainer)widget;
	
		if ( selected_container == null )
		    {
			if (debug_statements) System.out.println("Selected Widget null!");
			System.exit(1);
		    }

		//selected_container.setSelected(true);
		
		if (selected_component != null) 
		    {
			selected_component.setSelected(false);
			selected_component = null;
		    }
		
		selected_container.x = x;
		
		selected_container.y = y;
		
		selected_container.setContainingPanel( this );
		
		containers.add(selected_container);
	    }
	else if ( widget instanceof Operator )
	    {
		if (selected_component != null) 
		    {
			selected_component.setSelected(false);
			selected_component = null;
		    }

		if (selected_container != null) 
		    {
			selected_container.setSelected(false);
			selected_container = null;
		    }

		selected_operator = (Operator)widget; 
		
		//selected_component.setSelected(false);
		selected_component = null;
		//selected_container.setSelected(false);
		selected_container = null;
		
		if ( selected_operator == null )
		    {
			if (debug_statements) System.out.println("Selected Widget null!");
		    }
		
		selected_operator.x = x;
		
		selected_operator.y = y;
		
		selected_operator.setContainingPanel( this );
		
		operators.add(selected_operator);
	    }
	else if ( widget instanceof Species )
	    {
		clearSelections();

		if ( selected_operator == null )
		    {
			if (debug_statements) System.out.println("Selected Widget null!");
		    }
		
		widget.setX(x);
		
		widget.setY(y);
		
		widget.setContainingPanel( this );

		Species s = (Species)widget;

		boolean confine_to_boundaries = true;
		s.calculatePointerOffset(y,x);
		s.updateLocation(x,y, confine_to_boundaries);
		

		species.add(s);
	    }
	else
	    {
		if (debug_statements) System.out.println("Error: Unknown type imported");
	    }
	
        repaint();
	
        if (debug_statements) System.out.println( "Number of containers in this panel: " + containers.size() );
	if (debug_statements) System.out.println( "Number of free components in this panel: " + components.size() );
    }
    
    boolean linkComponents(BioComponent start, BioComponent end)
    {
	// Adjacent components manage edges
	edges.add( new Edge(start,end, this) );
	start_selected = false;
	repaint();
        return true;
    }

    void displayComponentOptionsMenu( MouseEvent e )
    {
			
	JMenuItem menu_rename = new JMenuItem( "Rename" );
	JMenuItem menu_delete = new JMenuItem( "Delete" );
	JMenuItem menu_change_state = new JMenuItem( "Change State" );
	
	

	// Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
	popup.addSeparator();
	popup.add(menu_rename);
	popup.add(menu_delete);
	
	menu_rename.addActionListener( this );
	menu_delete.addActionListener( this );
	

	// Create state change submenu
	// Create a submenu with items

	JMenuItem change_state_submenu_phosphorylated = new JMenuItem( "Phosphorylated" );
	JMenuItem change_state_submenu_unphosphorylated = new JMenuItem( "UnPhosphorylated" );
	JMenuItem change_state_submenu_unspecified = new JMenuItem( "Unspecified" );

	
	change_state_submenu_phosphorylated.addActionListener( this );
	change_state_submenu_unphosphorylated.addActionListener( this ); 
	change_state_submenu_unspecified.addActionListener( this );
	
	JMenu change_state_submenu = new JMenu("Change State");
	change_state_submenu.add(change_state_submenu_phosphorylated);
	change_state_submenu.add(change_state_submenu_unphosphorylated);
	change_state_submenu.add(change_state_submenu_unspecified);

	// Add submenu to popup menu
	popup.add(change_state_submenu);

	// Add the Make Species item to the bottom of the menu
	
	

	popup.show( this, e.getX(), e.getY() );
    }


void displayContainerOptionsMenu( MouseEvent e )
    {
			
	JMenuItem menu_rename = new JMenuItem( "Rename" );
	JMenuItem menu_delete = new JMenuItem( "Delete" );
	
	
	// Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
	popup.addSeparator();
	popup.add(menu_rename);
	popup.add(menu_delete);
	

	menu_rename.addActionListener( this );
	menu_delete.addActionListener( this );
	
	popup.show( this, e.getX(), e.getY() );	
    }
}
