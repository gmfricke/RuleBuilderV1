/*
 * ObservablesPalette.java
 *
 * Created on August 29, 2005, 12:47 AM
 */

/**
 *
 * @author  matthew
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class ObservablesPalette extends WidgetPalette
{
    
    
    protected class KeyboardControl implements KeyListener 
    {
        KeyboardControl(){}
        
        public void keyPressed(KeyEvent evt) {
            if (debug_statements) System.out.println("Key Press Detected");
            
            int key = evt.getKeyCode();  // keyboard code for the key that was pressed
            if (evt.isControlDown() && key == KeyEvent.VK_C ) {
                copySelectedWidget();
            }
            else if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE ) {
                
                    if ( selected_pattern != null )
                    {
                        removeGroup( selected_group );
                        compressDisplay();
                
                        clearSelections();
                    }
            }
            else if ( key == KeyEvent.VK_MINUS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*0.95, false);
            }
            else if ( key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*1.05, false);
            }
            else if ( key == KeyEvent.VK_UP ) 
            {
                int current_ytrans = getYTranslation();
                setYTranslation( current_ytrans - 10 );
            }
            else if ( key == KeyEvent.VK_DOWN ) 
            {
                int current_ytrans = getYTranslation();
                setYTranslation( current_ytrans + 10 );
            }            
	    else if ( key == KeyEvent.VK_LEFT ) 
            {
                int current_xtrans = getXTranslation();
                setXTranslation( current_xtrans - 10 );
            }
            else if ( key == KeyEvent.VK_RIGHT ) 
            {
                int current_xtrans = getXTranslation();
                setXTranslation( current_xtrans + 10 );
            }         
            else 
	    {
                if (debug_statements) System.out.println("Unhandled keyboard event.");
            }
            
            
        }
        
        public void keyReleased(KeyEvent evt) {
            
        }
        
        public void keyTyped(KeyEvent evt) {
            
        }
    }
  
    private class MouseControl extends MouseInputAdapter implements DragGestureListener
    {
	
        private WidgetPalette panel;
        
        public MouseControl(WidgetPalette panel) 
        {
           this.panel = panel;
        }
        
	public void mousePressed(MouseEvent e)
	{
            
            
            clearSelections();
            requestFocus();
	    // Determine which pattern was selected.
            // Compensate for zoom and translation
            // getZoomedX(Y)Translation should always be 0 for WidgetPalettes
            int mouse_x = (int)(((e.getX()-getZoomedXTranslation())/getZoom()));
            int mouse_y = (int)(((e.getY()-getZoomedYTranslation())/getZoom()));    
            
            
            
            Iterator i = groups.iterator();
	    while ( i.hasNext() )
		{
		    Group s = (Group)i.next();
		    if ( s.contains(mouse_x, mouse_y) ) 
		    {
			if (debug_statements) System.out.println("Group pressed");
                       
			selected_group = s;
                        selected_group.setSelected( true );
                        
                        if ( e.isPopupTrigger() )
			    {
				displayGroupOptionsMenu( e );
				return;
			    }
			//selected_container.setSelected(true);
		    }
                }
        }

    // Handles the event of the user pressing down the mouse button.
    // Here the program determines which widget the mouse pointer was over a
    // when the button was pressed. If the widget pressed is a template then
    // a new widget is created and assigned to be the selected widget. If the
    // widget pressed is not a template then it is the selected widget.
    

    // Handles the event of a user dragging the mouse while holding
    // down the mouse button.
    public void mouseDragged(MouseEvent e)
	{    
	   if (debug_statements) System.out.println("Mouse drag detected");
         
           /*
            JComponent c = (JComponent) e.getSource();
            TransferHandler th = c.getTransferHandler();
            if (th != null) 
            {
                th.exportAsDrag(c, e, DnDConstants.ACTION_MOVE );
            }
            else
            {
                displayError("Error in Drag 'n Drop of ReactionRule","ReactionRule Handler was NULL. Contact support at support@bionetgen.com");
            }
   */
	}

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e)
    {
	if ( getSelectedGroup() != null && e.isPopupTrigger() )
        {
            displayGroupOptionsMenu( e );
            return;
        }
	
	
    }
       
     // This method is required by MouseListener.
     public void mouseMoved(MouseEvent e){}

     // These methods are required by MouseMotionListener.
     public void mouseClicked(MouseEvent e){}
     public void mouseExited(MouseEvent e)
     {
         //clearSelections();
     }
     
     public void mouseEntered(MouseEvent e){}

     // Specific to DnD drags
     public void dragGestureRecognized(DragGestureEvent dge) 
     {
            Widget w = getSelectedWidget();
            Image image = w.createImage();
            int height = w.getHeight();
            int width = w.getWidth();
        
            WidgetDragHandler listener = getTheGUI().getWidgetDragHandler();
            
            dge.startDrag(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR ),// cursor
                        image,
                        new Point(-width/2,-height/2),
			panel.getSelectedWidget(), // transferable
			listener);  // drag source listener
     }
}

        
    // Serialization explicit version
    private static final long serialVersionUID = 1;

    // Transient so StateSaver wont try to serialize it
    transient private MouseControl local_mouse_control = new MouseControl( this );
    transient private KeyboardControl local_keyboard_control = new KeyboardControl();
    
    //ScrollControl scroll_control = new ScrollControl();
   
    
    /** Creates a new instance of GroupPanel */
    public ObservablesPalette(GUI the_gui) 
    {
        super( the_gui );
        removeMouseMotionListener( mouse_control );
        removeMouseListener(mouse_control);
        addMouseMotionListener(local_mouse_control);
	addMouseListener(local_mouse_control);
        removeKeyListener( keyboard_control );
        addKeyListener( local_keyboard_control );
            
        // Setup Drag Listener
        DragSource dragSource = DragSource.getDefaultDragSource();
        // creating the recognizer is all that's necessary - it
        // does not need to be manipulated after creation
        DragGestureRecognizer dgr = dragSource.createDefaultDragGestureRecognizer(
        this, // component where drag originates
        DnDConstants.ACTION_COPY_OR_MOVE, // actions
        local_mouse_control); // drag gesture listener
        
        area = new Dimension(0,0);
        
     	setBackground(Color.white);
    }
    
        public void actionPerformed( ActionEvent e )
        {
            super.actionPerformed( e );
            
            if ( e.getActionCommand().equals("Delete") )
            {
		 if ( selected_pattern != null )
		    {
			if (debug_statements) System.out.println("Deleting Group");
                        getTheGUI().getModel().removeObservable( selected_group );
                        
                        removeGroup( selected_group );
			compressDisplay();
                        clearSelections();
			repaint();
		    }
            }
        }
        
   /* 
   public boolean addGroup( BioContainer bc ) throws GroupCreationException
    {
        Vector containers = new Vector();
        containers.add( bc );
        
         
        return addGroup( containers );
    }
    
    */
        
   /* 
   public boolean addGroup( SelectionBox sb ) throws GroupCreationException
    {
        if (debug_statements) System.out.println("Adding Selection Box contents to Observables");
        
        Vector containers = sb.getContainers();
        
        Iterator species_itr = sb.getSpecies().iterator();
        while( species_itr.hasNext() )
        {
            Species s = (Species)species_itr.next();
            containers.addAll( s.getContainers() );
        }
        
        return addGroup( containers );
    }
    
   public boolean addGroup( Vector containers )
   {
       if (debug_statements) System.out.println("addGroup( containers ) called");
       
       Group p = makeGroup(containers);
       
       if ( p.getContainers().isEmpty() )
       {
           displayError("Error Adding Observable","Group is empty.");
           return false;
       }
       else
       {
           displayError("Adding Observable","Created Group with " + p.getContainers().size() + " containers.");
       }
       
       if (debug_statements) System.out.println("adding pattern");
       return addGroup( p );
   }
    
    */
   
   /*
   public boolean addGroup( Group spec )
    {
        if (debug_statements) System.out.println("Group added to observables");
        
	if ( spec == null ) 
	    {
		return false;
           }
        
        Group s = null;
        try
        {
            s = (Group)WidgetCloner.clone(spec);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        
        // check for redundency
        //Iterator s_itr = pattern.iterator();
        //while( s_itr.hasNext() )
        //{
        //    if ( s.isIsomorphicTo( (Group) s_itr.next(), "CommonEdge" ) )
       //     {
       //         return false;
       //     }
       // }

        s.setContainingPanel(this);
        //s.updateLocation(0,0,false);
        
        if (debug_statements) System.out.println("*+++*" +
        "\nX=" + s.getX() +
        "\nY=" + s.getY() +
        "\nHeight=" + s.getHeight() +
        "\nWidth=" + s.getWidth() +
        "\n*+++*");
        
        super.addGroup( s );
 
        //Iterator i = s.getEdges().iterator();
        
        //while( i.hasNext() )
        //{
        //    addEdge( (Edge)i.next() );
        //}
        
        positionGroup( s );
        
	// Add pattern to the model
        //the_gui.getModel().addGroup(s);
    
        // Add to the model
        the_gui.getModel().addObservable( s );
        
        displayInformation("Observable Added","");
        
	return true;
    }
    
    */ 
    
   void displayGroupOptionsMenu( MouseEvent e )
    {
			
	
	JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem prop_menu_item = new JMenuItem( "Properties" );

   
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
	popup.addSeparator();	
	popup.add(menu_delete);
        popup.add(prop_menu_item);
	
	prop_menu_item.addActionListener(getSelectedGroup());
	menu_delete.addActionListener( this );

        
	popup.show( this, e.getX(), e.getY() );	
    }
    
    public Vector<Group> getGroups() 
    {
        return groups;
    }
    
    public void clearSelections()
    {
        Iterator s_itr = getGroups().iterator();
                    while ( s_itr.hasNext() )
                    {
                        ((Group)s_itr.next()).setSelected(false);
                    }
    }
    
    /*
    public void compressDisplay()
    {
        vertical_offset = padding;
        
        if ( getGroups().isEmpty() ) 
        {
            area.width = 0;
            area.height = 0;
            return;
        }
        
        
        // Set width to a valid initial value
        Group first_group = (Group)getGroups().get(0);
        
        area.width = first_group.getWidth();
        
        Iterator s_itr = getGroups().iterator();
        while ( s_itr.hasNext() )
        {
            positionGroup((Group)s_itr.next());
        }
    }
    
    public void positionGroup( Group s )
    {
        // s.getX&Y so that the proper relative locations of the various 
	// constituents of the pattern are preserved
	//s.calculatePointerOffset(s.getX(),s.getY()); 

	if (debug_statements) System.out.println("GroupDisplayPanel: Vertical offset="+vertical_offset);
	// Calculate how far down to place the pattern
            s.setContainingPanel(this);
            s.setSelected( false );
            
            // Calculate offset from the upper left corner of the bounding box (getX, getY)
            // to preserve relative offsets but not absolute positioning from the 
            // Group creation window
            s.calculatePointerOffset(s.getX(),s.getY());
            s.updateLocation( padding, vertical_offset, false );
            
            
        if (debug_statements) System.out.println("*****" +
        "\nX=" + s.getX() +
        "\nY=" + s.getY() +
        "\nHeight=" + s.getHeight() +
        "\nWidth=" + s.getWidth() +
        "\nX Offset=" + s.getXOffset() +
        "\nY Offset=" + s.getYOffset() +
        "\n*****");
            
 	//s.updateLocation(0,vertical_offset, confine_to_boundaries);
	vertical_offset+=s.getHeight()+padding;
        
        //account for zoom
        //vertical_offset*=getZoom(); //<- confusing device space and user space!
        
        // Update the area occupied by containers
            area.height = vertical_offset;
            area.height = (int)(vertical_offset*getZoom());             
            
            if ( s.getWidth() > area.width ) 
            {
                area.width = s.getWidth() + 2*padding; //2* because we have to account for

                // the lefthand padding as well.
            }
            
            area.width*=getZoom();
            
            // Change the preferred size to reflect the new area
            setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            revalidate();
            
            repaint();
    }
    
     */
     
    public void removeGroup(Group s) 
    {
        super.removeGroup( s );
        getTheGUI().getModel().removeObservable( s );
        
        compressDisplay();
    }
    
    public boolean pasteCopiedWidget( int x, int y ) 
    {
        Widget copied_widget = getTheGUI().copied_widget;
        
        //try
        //{
            
        /*
        if ( copied_widget instanceof BioContainer )
        {
            addGroup( (BioContainer)copied_widget );
        }
        else if ( copied_widget instanceof SelectionBox )
        {
            addGroup( (SelectionBox)copied_widget );
        }
         */
        //else 
        if ( copied_widget instanceof Group )
        {
            addGroup( (Group)copied_widget );
        }
        else
        {
            displayError("Error Pasting " + copied_widget.getClass().getName(),
            "Only Groups can be pasted into the Observables Palette." );
            return true;
        }
        
        //}
        //catch (GroupCreationException e )
        //{
        //    displayError("Group Creation Error", e.getMessage() );
        //}
        
        Group s = null;
        try
        {
            s = (Group)WidgetCloner.clone(copied_widget);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        addGroup( s );
        return true;
    }
 
   
    public boolean cutSelectedWidget()
    {
                        copySelectedWidget();
                        removeGroup( selected_group );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                        
                        return true;
    }
    
    public boolean addGroup(Group group)
    {
       if ( groups.indexOf( group ) != -1 || group == null ) 
	    {
		return false;
	    }

       if ( group.getLabel() == null || group.getType() == null )
       {
           group.setPropertiesFromUser();
       }
       else if ( group.getLabel().equals("") || !(group.getType().equals("Species") || group.getType().equals("Molecules")) )
       {
        group.setPropertiesFromUser();
       }
       
        if ( !preventDuplicates( group ) )
        {
            return false;
        }
        
	group.setContainingPanel(this);
        group.setSelected( false );
        
        super.addGroup( group );
        //positionElement( group );
	compressDisplay();
        
        the_gui.getModel().addObservable( group );
            
	return true;
    }

    public boolean preventDuplicates( Group o ) 
    {
        while ( null != getTheGUI().getModel().getObservable( o.getLabel() ) )
        {
            String new_name = displayInputQuestion("Observable Exists", "An observable with that label already exists.\n" +
                "Please enter a unique label.");
                
                if (new_name == null)
                {
                    //user_cancelled
                    return false;
                }
                
                o.setLabel(new_name);    
        }
        
        return true;
    }
    
    
}
