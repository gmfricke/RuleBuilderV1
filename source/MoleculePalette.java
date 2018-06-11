/*
 * MoleculePanel.java
 *
 * Created on January 9, 2005, 12:20 AM
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

import java.net.*; //For URL image loading from Jar files

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class MoleculePalette extends WidgetPalette 
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
                
                if ( selected_component != null )
                {
                    BioContainer container = selected_component.getContainer();
                    if ( container.getComponents().size() == 1 )
                    {
			displayError( "Component Deletion Error","MoleculeTypes must have at least one component." );
                        return;
                    }
                    else
                    {
                        if ( displayQuestion( "Deletion Warning","Deleting this component will invalidate all molecules of this type. Continue?" ) )
                        {
                            removeComponent( selected_component );
                            clearSelections();
                        }
                    }
                }
                else if ( selected_container != null )
                {
                    if ( displayQuestion( "Deletion Warning","Deleting this MoleculeType will invalidate all molecules of that type. Continue?" ) )
                    {
                        removeMoleculeType( selected_container );
                        compressDisplay();
                        clearSelections();
                    }
                }
                
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_C ) {
                copySelectedWidget();
                repaint();
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_V ) {
                pasteCopiedWidget();
                repaint();
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_X ) 
            {
                if ( selected_component != null )
                {
                    BioContainer container = selected_component.getContainer();
                    if ( container.getComponents().size() == 1 )
                    {
			displayError( "Component Cut Error","MoleculeTypes must have at least one component." );
                        return;
                    }
                    else
                    {
                        if ( displayQuestion( "Cut Warning","Cutting this component will invalidate all molecules of this type. Continue?" ) )
                        {
                            removeComponent( selected_component );
                            cutSelectedWidget();
                            clearSelections();
                            repaint();
                            return;
                        }
                    }
                }
                else if ( selected_container != null )
                {
                        cutSelectedWidget();
                        clearSelections();
                        repaint();
                }
            }
            else if ( key == KeyEvent.VK_MINUS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*0.95);
            }
            else if ( key == KeyEvent.VK_PLUS || key == KeyEvent.VK_EQUALS ) 
            {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*1.05);
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
            
            
            requestFocus();
            
            // Compensate for zoom and translation
            // getZoomedX(Y)Translation should always be 0 for WidgetPalettes
            int mouse_x = (int)(((e.getX()-getZoomedXTranslation())/getZoom()));
            int mouse_y = (int)(((e.getY()-getZoomedYTranslation())/getZoom()));    
            
            if (debug_statements) System.out.println("Zoom Factor: " + getZoom());
            if (debug_statements) System.out.println("Mouse press at: " + e.getX() + ", " + e.getY() );
            if (debug_statements) System.out.println("Translated to: " + mouse_x + ", " + mouse_y );
            
	   // Determine which component was pressed
            clearSelections();
            
            Iterator i = components.iterator();
	    while ( i.hasNext() )
		{
		    BioComponent c = (BioComponent)i.next();
		    if ( c.contains( mouse_x, mouse_y) ) 
		    {
			if (debug_statements) System.out.println("Component pressed");
                       
			selected_component = c;
                        selected_component.setSelected( true );
                        
                        if ( e.isPopupTrigger() )
			    {
				displayComponentOptionsMenu( e );
				return;
			    }
			//selected_container.setSelected(true);
                        
                        // Select the parent container
                        // selected_container = selected_component.getContainer();
                        return;
		    }
                    
                }
            
             // Determine which container was selected.
           
            Iterator ci = containers.iterator();
	    while ( ci.hasNext() )
		{
		    BioContainer c = (BioContainer)ci.next();
		    if ( c.contains( mouse_x, mouse_y ) ) 
		    {
			if (debug_statements) System.out.println("Container pressed");
                       
			selected_container = c;
                        selected_container.setSelected( true );
                        
                        if ( e.isPopupTrigger() )
			    {
				displayContainerOptionsMenu( e );
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
         
           // so that clicking and dragging a component in a molecule type
           // will DnD the whole molectule type
           if ( selected_component != null )
           {
               selected_container = selected_component.getContainer();
               selected_component = null;
           }
           
            //JComponent c = (JComponent) e.getSource();
            //TransferHandler th = c.getTransferHandler();
            //if (th != null) 
            //{
            //    th.exportAsDrag(c, e, DnDConstants.ACTION_MOVE );
            //}
   
	}

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e)
    {
	if ( selected_container != null && e.isPopupTrigger() )
        {
            displayContainerOptionsMenu( e );
            return;
        }
        else if ( selected_component != null && e.isPopupTrigger() )
        {
            displayComponentOptionsMenu( e );
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

     public void dragGestureRecognized(DragGestureEvent dge) 
     {
         Widget w = getSelectedWidget();
            Image image = w.createImage();
            int height = w.getHeight();
            int width = w.getWidth();
        
            WidgetDragHandler listener = getTheGUI().getWidgetDragHandler();
            
            dge.startDrag(Cursor.getPredefinedCursor( Cursor.MOVE_CURSOR ) ,// cursor
                        image,
                        new Point(-width/2,-height/2),
			panel.getSelectedWidget(), // transferable
			listener);  // drag source listener
            
        
            if (debug_statements) System.out.println("dragGesture");
            
        if (debug_statements) System.out.println("dragGestureRecognized() fired");
            
        /*
        URL url = this.getClass().getResource("images/plus_op.gif");
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
        
         */
         
        
     }
     
}

        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    

    // Transient so StateSaver wont try to serialize it
    transient private MouseControl local_mouse_control = new MouseControl( this );
    transient private KeyboardControl local_keyboard_control = new KeyboardControl();
    //ScrollControl scroll_control = new ScrollControl();
    
    transient protected boolean debug_statements = true;
   
    /**
     * Creates a new instance of MoleculePanel
     * @param the_gui
     */
    public MoleculePalette( GUI the_gui ) 
    {
        super( the_gui );
        removeMouseMotionListener( mouse_control );
        removeMouseListener(mouse_control);
       
        addMouseMotionListener(local_mouse_control);
	addMouseListener(local_mouse_control);
        
        local_keyboard_control = new KeyboardControl();
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
            
            if ( e.getActionCommand().equals("Delete") )
            {
                
                 if ( selected_component != null )
                 {
                     selected_container = selected_component.getContainer();
                 }
		 if ( selected_container != null )
		    {
			if (debug_statements) System.out.println("Deleting Molecule Type");
                        
                        if ( !displayQuestion( "Deletion Warning","Deleting this component will invalidate all molecules of this type. Continue?" ) )
                        {
                            return;
                        }
                        
                        removeMoleculeType( selected_container );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                 }
            }
            else if ( e.getActionCommand().equals("Edit Allowed States") )
            {
                selected_component.getType().displayAllowedStates();
            }
            
            super.actionPerformed( e );
        }
    
    synchronized public boolean addMoleculeType( BioContainer bc_orig )
    {
        if ( bc_orig == null ) return true;
        
        try
        {
        BioContainer bc_copy = (BioContainer)WidgetCloner.clone( bc_orig );//BioContainerCloner.clone( bc_orig );
        
        
        // Don't allow templates to be created from containers with no components
        //if ( bc_copy.getComponents().isEmpty() )
        //{
        //    displayError("Molecule Template Creation Error",
        //    "Templates cannot be created from empty Containers.");
        //    return false;
        //}
        
        // Don't allow templates to be created from patterns
        if ( bc_copy.getLabel().equals("*") )
        {
            displayError("Molecule Template Creation Error",
            "Templates cannot be created from \"wildcard\" Containers.");
            return false;
        }
        
        
        Iterator comp_itr = bc_copy.getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent curr_comp = ((BioComponent)comp_itr.next());
            
            if ( curr_comp.getLabel().equals("*") )
            {
                displayError("Molecule Template Creation Error",
                "Templates cannot be created from Containers with \"wildcard\" Components.");
                return false;
            }
            
            if ( !curr_comp.getBindingState().equals("No Additional Bonds") )
            {
                displayError("Molecule Template Creation Error",
                "Templates cannot be created that contain Components with \"wildcard\" binding states.");
                return false;
            }
            
        }
        
        
        
        while( getMoleculeType( bc_copy.getLabel() ) != null ) 
        {
                String new_name = displayInputQuestion("MoleculeType Exists", "A Molecule Type with the label \""+bc_copy.getLabel()+"\" already exists.\n" +
                "Please enter a unique type name.");
                
                if (new_name == null)
                {
                    //user_cancelled
                    return false;
                }
                
                
                bc_copy.setLabel(new_name);    
            }
            
            bc_copy.setContainingPanel( this );
        
            MoleculeType mt = new MoleculeType( bc_copy );
            mt.setAllowedStatesFromUser();
            addMoleculeType( mt );
        }
        catch( Exception e )
        {
            displayError("Unexpected exception",e.getMessage());
            e.printStackTrace();
        }
        
        return true;
    }
    
    // Shadow WidgetPanels addContainer() so that the containers can be added in a grid pattern
    synchronized boolean addMoleculeType( MoleculeType mt )
    {
            
        // Make sure the container is not already in the panel
        
        // By object address
	//if ( containers.indexOf( mt ) != -1 ) 
	//    {
	//	return false;
	//    }
        // ... duplicate ComponentTypes are needed sometimes
         
        
            
            mt.setContainingPanel(this);
            
            mt.setSelected( false );
            
            //mt.calculatePointerOffset(mt.getX(),mt.getY()); 
            //mt.updateLocation( padding, vertical_offset, false );
		
            //vertical_offset+=mt.getHeight()+padding;
            //positionElement( mt );
              
              
            containers.add(mt);
            
            
            // Add the components in the container, rely on addComponent to take care of any edges
	for ( int i = 0; i < mt.getComponents().size(); i++ )
	    {
                BioComponent comp = (BioComponent) mt.getComponents().get(i);
		addComponent( comp );
            }
            
            Vector<Edge> delete_me = new Vector<Edge>();
            
        // Add the edges in the container (if fully contained)
	for ( int i = 0; i < mt.getEdges().size(); i++ )
	    {
		Edge e =  (Edge) mt.getEdges().get(i);
                
                if ( components.indexOf( e.getStart() ) == -1
                     || components.indexOf( e.getEnd() ) == -1 )
                {
                    // add to remove pile
                    delete_me.add( e );
                }
                else
                {
                    addEdge(e);
                }

            }
         
            Iterator v = delete_me.iterator();
            while( v.hasNext() )
            {
                Edge e = (Edge)v.next();
                if ( mt.hasEdge(e) ) mt.removeEdge( e );
            }
            
            
            
            // Update the area occupied by containers
            //area.height = vertical_offset;
                         
            //if ( mt.getWidth() > area.width ) 
            //{
            //    area.width = mt.getWidth() + 2*padding; //2* because we have to account for
                // the lefthand padding as well.
            //}
                
            // Change the preferred size to reflect the new area
            //setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            //revalidate();
            
            //repaint();
            
        
            compressDisplay();
           getTheGUI().getModel().addMoleculeType( mt );
            
	return true;
    }
    
    
    void displayContainerOptionsMenu( MouseEvent e )
    {
			
	
	JMenuItem menu_delete = new JMenuItem( "Delete" );


        
	// Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
	popup.addSeparator();	
	popup.add(menu_delete);
	

	
	menu_delete.addActionListener( this );

        
	popup.show( this, e.getX(), e.getY() );	
    }
    
    void displayComponentOptionsMenu( MouseEvent e )
    {
			
	
	JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem menu_set_allowed_states = new JMenuItem( "Edit Allowed States" );


        
	// Context menu for changing attributes of the selected component
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
	popup.addSeparator();
	popup.add(menu_set_allowed_states);
	popup.addSeparator();	
        popup.add(menu_delete);

	
	menu_delete.addActionListener( this );
        menu_set_allowed_states.addActionListener( this );

        
	popup.show( this, e.getX(), e.getY() );	
    }
    
    /*
    public void compressDisplay()
    {
        vertical_offset = padding;
        
        if ( getAllContainers().isEmpty() ) 
        {
            area.width = 0;
            area.height = 0;
            return;
        }
        
        Iterator s_itr = this.getAllContainers().iterator();
        
        // Set width to a valid initial value
        BioContainer first_container = (BioContainer)getAllContainers().get(0);
         
        area.width = first_container.getWidth();
        
        // Position all the containers
        while ( s_itr.hasNext() )
        {
            positionMoleculeType((BioContainer)s_itr.next());
        }
        
        if (debug_statements) System.out.println("+++Vertical Offset: " + vertical_offset );
        if (debug_statements) System.out.println("+++Area Height: " + area.height );
    }
    
    public void positionMoleculeType(  BioContainer s )
    {
        if (debug_statements) System.out.println("Positioning Molecule Type");
        // s.getX&Y so that the proper relative locations of the various 
	// constituents of the species are preserved
	s.calculatePointerOffset(s.getX(),s.getY()); 
        s.updateLocation( padding, vertical_offset, false );
            
	vertical_offset+=s.getHeight()+padding;
            
        // Update the area occupied by containers
            area.height = vertical_offset;
            area.height*=getZoom();
            
         
            
            if (debug_statements) System.out.println( "Mol width: " + s.getWidth() + ", Area width:" + area.width );
            
            if ( s.getWidth() > area.width ) 
            {
                area.width = s.getWidth() + 2*padding; //2* because we have to account for    
                // the lefthand padding as well.
            }
                
            area.width*=getZoom();
            
            // Change the preferred size to reflect the new area
            setPreferredSize( area );
                
            // Let the scrollpane know it needs to update
            invalidate();
            revalidate();
            
            if (debug_statements) System.out.println("+++Vertical Offset: " + vertical_offset );
            if (debug_statements) System.out.println("+++Area Height: " + area.height );
            //repaint();
    }
    
     */
    
    public Vector<BioContainer> getMoleculeTypes() 
    {
        return containers;
    }
    
    public BioContainer getMoleculeType( BioContainer mol ) 
    {
        Iterator types = getAllContainers().iterator();
        while ( types.hasNext() )
        {
            BioContainer mt = (BioContainer)types.next();
            if ( mol.isType( mt ) )
            {
                return mt;
            }
       }
        
        return null;
    }
    
    public Vector<MoleculeType> getMoleculeTypes( BioContainer pattern ) 
    {
        Vector<MoleculeType> matches = new Vector<MoleculeType>();
        Iterator types = getAllContainers().iterator();
        while ( types.hasNext() )
        {
            MoleculeType mt = (MoleculeType)types.next();
            if ( mt.matchesPattern( pattern ) )
            {
                matches.add(mt);
            }
       }
        
        return matches;
    }
    
    public BioContainer getMoleculeType( String molname ) 
    {
        Iterator types = getAllContainers().iterator();
        while ( types.hasNext() )
        {
            BioContainer mt = (BioContainer)types.next();
            if ( molname.equals( mt.getLabel() ) )
            {
                return mt;
            }
       }
        
        return null;
    }
    
    /*
    public void removeComponent(BioComponent ct) 
    {
        super.removeComponent( ct );
        
        // Only remove from the model if it was the last instance in the palette of that type
        
        if ( !this.hasComponentType( ct.getLabel() ) ) 
        {
            Model model = getTheGUI().getModel();
            model.removeComponentType( model.getComponentType( ct.getLabel() ) );
            this.displayInformation("Removing Component Type","You have erased the last instance of the Component Type \"" +
            ct.getLabel() + "\" from the molecule type definitions.\nYou will be asked to redefine that Component Type if you use it again.");
        }
            
        refreshAll();
    }    
    
     */
    
    synchronized public void removeMoleculeType(BioContainer mt) 
    {
        super.removeContainer( mt );
  
        getTheGUI().getModel().removeMoleculeType( mt );
  
        /*
        // Remove all the components from the Panel
        Iterator comp_itr = mt.getComponents().iterator();
        while( comp_itr.hasNext() ) 
        {
            BioComponent ct = (BioComponent)comp_itr.next();
            if ( !this.hasComponentType( ct.getLabel() ) )
            {
                Model model = getTheGUI().getModel();
                model.removeComponentType( model.getComponentType( ct.getLabel() ) );
                this.displayInformation("Removing Component Type","You have erased the last instance of the Component Type \"" +
                ct.getLabel() + "\" from the molecule type definitions.\nYou will be asked to redefine that Component Type if you use it again.");
            }
        }
        */
         
        refreshAll();
    }  
    
    public boolean hasComponentType(java.lang.String type_name) 
    {
        Iterator mt_itr = getAllContainers().iterator();
        while( mt_itr.hasNext() )
        {
            BioContainer curr_mt = (BioContainer)mt_itr.next();
            
            Iterator ct_itr = curr_mt.getComponents().iterator();
            while( ct_itr.hasNext() )
            {
                BioComponent curr_ct = (BioComponent)ct_itr.next();
                if ( curr_ct.getLabel().equals( type_name ) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
 
    public boolean pasteCopiedWidget( int x, int y ) 
    {
        if ( debug_statements ) System.out.println("MoleculePalette:pasteCopiedWidget() called");
        
        Widget wid = getTheGUI().copied_widget;
        
        if ( !(wid instanceof BioContainer) )
        {
            displayError("Error Pasting " + wid.getClass().getName(), 
            "Only Containers may be pasted into the Molecule Type palette." );
            return true;
        }
        
        if ( wid instanceof BioComponent )
        {
            BioComponent b = (BioComponent)wid;
            b.removeAllEdges();
            b.removeAtomMap();
        }
        
        if ( wid instanceof BioContainer )
        {
            BioContainer bc = (BioContainer)wid;
            bc.removeAtomMap();
            
            Vector components = bc.getComponents();
            Iterator<BioComponent> comp_itr = components.iterator();
            while ( comp_itr.hasNext() )
            {
                BioComponent bcomp = comp_itr.next();
                Vector<Edge> remove_list = new Vector();
                Iterator<Edge> edge_itr = bcomp.getEdges().iterator();
                while ( edge_itr.hasNext() )
                {
                    Edge e = edge_itr.next();
                    if ( components.indexOf( e.getStart() ) == -1 || components.indexOf( e.getEnd() ) == - 1 )
                    {
                        remove_list.add( e );
                    }
                }
                
                Iterator<Edge>remove_itr = remove_list.iterator();
                while ( remove_itr.hasNext() )
                {
                    bcomp.removeEdge( remove_itr.next() );
                }
            }
        }
        
        addMoleculeType( (BioContainer)wid );
        
        /*
        BioContainer c = (BioContainer)wid;
        MoleculeType mt = c.createMoleculeType();
        addMoleculeType( mt );
        */
        
        return true;
    }
    
    
    
    public boolean cutSelectedWidget()
    {
        if ( !displayQuestion( "Cut Warning","Cutting this MoleculeType will invalidate all molecules of that type. Continue?" ) )
                        {
                            return false;
                        }
                        copySelectedWidget();
                        removeMoleculeType( selected_container );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                        
                        return true;
    }
    
    public boolean pasteCopiedWidget() 
    {
        return pasteCopiedWidget( 0, 0 );
    }
}