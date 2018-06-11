/*
 * SpeciesPalette.java
 *
 * Created on January 9, 2005, 12:47 PM
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

import java.net.*; // For URLs

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;

import java.util.*; //For vector data structure

public class SpeciesPalette extends WidgetPalette
{
    protected class KeyboardControl implements KeyListener 
    {
        KeyboardControl(){}
        
        public void keyPressed(KeyEvent evt) {
            if (debug_statements) if (debug_statements) System.out.println("Key Press Detected");
            
            int key = evt.getKeyCode();  // keyboard code for the key that was pressed
            if (evt.isControlDown() && key == KeyEvent.VK_C ) {
                copySelectedWidget();
            }
            else if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE ) {
                
                    if ( selected_species != null )
                    {
                        removeSpecies( selected_species );
                        compressDisplay();
                
                        clearSelections();
                        repaint();
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
            else if (evt.isControlDown() && key == KeyEvent.VK_X ) {
                cutSelectedWidget();
                repaint();
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
                if (debug_statements) if (debug_statements) System.out.println("Unhandled keyboard event.");
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
	    // Determine which species was selected.
            // Compensate for zoom and translation
            // getZoomedX(Y)Translation should always be 0 for WidgetPalettes
            int mouse_x = (int)(((e.getX()-getZoomedXTranslation())/getZoom()));
            int mouse_y = (int)(((e.getY()-getZoomedYTranslation())/getZoom()));    
            
            clearSelections();
            
            Iterator i = species.iterator();
	    while ( i.hasNext() )
		{
		    Species s = (Species)i.next();
		    if ( s.contains(mouse_x, mouse_y) ) 
		    {
			if (debug_statements) if (debug_statements) System.out.println("Species pressed");
                       
			selected_species = s;
                        selected_species.setSelected( true );
                        
                        if ( e.isPopupTrigger() )
			    {
				displaySpeciesOptionsMenu( e );
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
	   if (debug_statements) if (debug_statements) System.out.println("Mouse drag detected");
         
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
            **/
   
	}

    // Handles the event of a user releasing the mouse button.
    public void mouseReleased(MouseEvent e)
    {
	if ( selected_species != null && e.isPopupTrigger() )
        {
            displaySpeciesOptionsMenu( e );
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
            if (w == null ) return;
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
   
    
    /** Creates a new instance of SpeciesPanel */
    public SpeciesPalette( GUI the_gui ) 
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
		 if ( selected_species != null )
		    {
			if (debug_statements) if (debug_statements) System.out.println("Deleting Species");
                        getTheGUI().getModel().removeSpecies( selected_species );
                        
                        removeSpecies( selected_species );
			compressDisplay();
                        clearSelections();
			repaint();
		    }
            }
        }
        
   synchronized public boolean addSpecies( BioComponent bc ) throws SpeciesCreationException
    {
        Species s = makeSpecies( bc, "0" );
        
        if ( s == null )
        {
            return true;
        }
        
        boolean successful = s.setPropertiesFromUser();
        
        if ( !successful ) return false;
        
        return addSpecies( s );
    }
    
    
   synchronized public boolean addSpecies( BioContainer bc ) throws SpeciesCreationException
    {
        if (debug_statements) if (debug_statements) System.out.println("addSpecies(BioContainer)");
        
        bc.setContainingPanel(this);
        
        if( bc.getComponents().isEmpty() )
        {
            throw new SpeciesCreationException("All Components must be contained.");
        }
        
        BioComponent component = (BioComponent)bc.getComponents().get(0);
         
        return addSpecies( component );
    }
    
   synchronized public boolean addSpecies( SelectionBox sb ) throws SpeciesCreationException
    {
        Vector<BioComponent> c = sb.getComponents();
        
        if ( c.isEmpty() )
        {
            throw new SpeciesCreationException("A Species must contain at least one Component.");
        }
        
        // Check that the graph is connected
        
        BioComponent component = (BioComponent)c.get(0);
        
        Species s = makeSpecies( component, "0" );
        
        boolean successful = s.setPropertiesFromUser();
        
        if ( !successful ) return false;
        
        Iterator comp_itr = c.iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent curr_comp = (BioComponent)comp_itr.next();
            if ( curr_comp.getBioGraph() != s )
            {
                throw new SpeciesCreationException("The Species Graph contained in the SelectionBox was not connected.");
            }
        }
        
        return addSpecies( s );
    }
    
    
   synchronized public boolean addSpecies( Species spec )
    {
	if ( spec == null ) 
	    {
		return false;
           }
        
        Species s = null;
        try
        {
            s = (Species)WidgetCloner.clone(spec);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        while ( null != getTheGUI().getModel().getSpecies( s.getLabel() ) )
        {
            String new_name = displayInputQuestion("Species Exists", "A species with that label already exists.\n" +
                "Please enter a unique label.");
                
                if (new_name == null)
                {
                    //user_cancelled
                    return false;
                }
                
                
                s.setLabel(new_name);    
        }
    
        
        // check for redundency
        //Iterator s_itr = species.iterator();
        //while( s_itr.hasNext() )
        //{
        //    if ( s.isIsomorphicTo( (Species) s_itr.next(), "CommonEdge" ) )
       //     {
       //         return false;
       //     }
       // }
            
        
        //s.updateLocation(0,0,false);
        
        if (debug_statements) if (debug_statements) System.out.println("*+++*" +
        "\nX=" + s.getX() +
        "\nY=" + s.getY() +
        "\nHeight=" + s.getHeight() +
        "\nWidth=" + s.getWidth() +
        "\n*+++*");
        
        
        
        super.addSpecies( s );
 
        //Iterator i = s.getEdges().iterator();
        
        //while( i.hasNext() )
        //{
        //    addEdge( (Edge)i.next() );
        //}
        
        //positionSpecies( s );
        //positionElement( s );
        compressDisplay();
        
        //this.displayInformation("Added Species","There are now " + getContents().size() + " species in the palette");
        
	// Add species to the model
        //the_gui.getModel().addSpecies(s);
    
        // Add to the model
        the_gui.getModel().addSpecies( s );
        
        
	return true;
    }
      
    void displaySpeciesOptionsMenu( MouseEvent e )
    {
			
	
	JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem menu_conc = new JMenuItem( "Properties" );
        JMenuItem add_to_model = new JMenuItem( "Add this Derived Species to the Model" );
        
   
	JPopupMenu popup = new JPopupMenu();
	popup.add("Options");
        popup.addSeparator();
        
	popup.addSeparator();	
	popup.add(menu_delete);
        
	
        popup.addSeparator();	
	if ( getSelectedSpecies().isDerived() ) popup.add(add_to_model);
        
	add_to_model.addActionListener( getSelectedSpecies() );
	menu_delete.addActionListener( this );
        menu_conc.addActionListener( getSelectedSpecies() );

        popup.add(menu_conc);
        
	popup.show( this, e.getX(), e.getY() );	
    }
    
    Vector<Species> getMatchingSpecies( Species s )
    {
        Vector<Species> matches = new Vector<Species>();
        
        Iterator itr = species.iterator();
        while ( itr.hasNext() )
        {
           Species target = (Species)itr.next();
            
           if ( s.matches( target ) )
           {
               matches.add( target );
           }
        }
        
        return matches;
    }
    
    // Returns a vector of vectors. The first dimension of vectors
    // divides the results by species.
    // The second dimension contains the matching components for each species
    Vector<Vector<BioComponent>> getMatchingPortions( Species s )
    {
        Vector<Vector<BioComponent>> matches = new Vector<Vector<BioComponent>>();
        
        Iterator itr = species.iterator();
        while ( itr.hasNext() )
        {
           Species target = (Species)itr.next();
           Vector<BioComponent> current_matches = s.getMatchingPortions( target );
           matches.add( current_matches );
        }
        
        return matches;
    }
    
    public Vector<Species> getSpecies() 
    {
        return species;
    }
    
    synchronized public void clearSelections()
    {
        Iterator s_itr = getSpecies().iterator();
                    while ( s_itr.hasNext() )
                    {
                        ((Species)s_itr.next()).setSelected(false);
                    }
    }
    
    /*
    
    public void compressDisplay()
    {
        vertical_offset = padding;
        
        if ( getAllSpecies().isEmpty() ) 
        {
            area.width = 0;
            area.height = 0;
            return;
        }
        
        
        // Set width to a valid initial value
        Species first_species = (Species)getAllSpecies().get(0);
        
        area.width = first_species.getWidth();
        
        Iterator s_itr = getSpecies().iterator();
        while ( s_itr.hasNext() )
        {
            positionSpecies((Species)s_itr.next());
        }
    }
    
    public void positionSpecies( Species s )
    {
        // s.getX&Y so that the proper relative locations of the various 
	// constituents of the species are preserved
	//s.calculatePointerOffset(s.getX(),s.getY()); 

	if (debug_statements) if (debug_statements) System.out.println("SpeciesDisplayPanel: Vertical offset="+vertical_offset);
	// Calculate how far down to place the species
            s.setContainingPanel(this);
            s.setSelected( false );
            
            // Calculate offset from the upper left corner of the bounding box (getX, getY)
            // to preserve relative offsets but not absolute positioning from the 
            // Species creation window
            s.calculatePointerOffset(s.getX(),s.getY());
            s.updateLocation( padding, vertical_offset, false );
            
            
        if (debug_statements) if (debug_statements) System.out.println("*****" +
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
     
    synchronized public void removeSpecies(Species s) 
    {
        super.removeSpecies( s );
        getTheGUI().getModel().removeSpecies( s );
        
        compressDisplay();
    }
    
    synchronized public boolean pasteCopiedWidget( int x, int y ) 
    {
        Widget copied_widget = getTheGUI().copied_widget;
        
        try
        {
            
        
        if ( copied_widget instanceof BioContainer )
        {
            addSpecies( (BioContainer)copied_widget );
        }
        //else if ( copied_widget instanceof BioComponent )
        //{
        //    addSpecies( (BioComponent)copied_widget );
        //}
        else if ( copied_widget instanceof Species )
        {
            addSpecies( (Species)copied_widget );
        }
        else
        {
            displayError("Error Pasting " + copied_widget.getClass().getName(),
            "Only Species and Containers may be pasted into the Species palette." );
            return true;
        }
        
        }
        catch (SpeciesCreationException e )
        {
            displayError("Species Creation Error", e.getMessage() );
        }
        
        /*
        Species s = null;
        try
        {
            s = (Species)WidgetCloner.clone(copied_widget);
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
        
        addSpecies( s );
         
         */
        
        return true;
    }

    
    synchronized public boolean cutSelectedWidget()
    {
                        copySelectedWidget();
                        removeSpecies( selected_species );
                        compressDisplay();
                        clearSelections();
			refreshAll();
                        
                        return true;
    }
    
    /*
    protected void paintComponent(Graphics gr) 
    {    
        base_dimension = getSize();
        //Graphics2D g = (Graphics2D)gr;
        Graphics2D g = (Graphics2D) ((Graphics2D)gr).create();
        
        
        //if ( getZoom() != 1.0 )
        {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        
	// Get panel dimension for setLocation to use in bounds checking
        
        if (isOpaque()) 
	    { //paint background
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
	    }
        
        if ( !(this instanceof WidgetPalette) )
        {
            double x_zoom_compensate = (base_dimension.getWidth() - base_dimension.getWidth()*zoom_factor)/2;
            double y_zoom_compensate = (base_dimension.getHeight() - base_dimension.getHeight()*zoom_factor)/2;
            zoomed_x_translation = (int)x_zoom_compensate+x_trans;
            zoomed_y_translation = (int)y_zoom_compensate+y_trans;
            //if (debug_statements) if (debug_statements) System.out.println("New Origin: (" + zoomed_x_translation + "," + zoomed_y_translation + ")");
            //if (debug_statements) if (debug_statements) System.out.println("Old Origin: (" + -zoomed_x_translation*(1.0/zoom_factor) + "," + -zoomed_y_translation*(1.0/zoom_factor) + ")");
            g.translate( zoomed_x_translation, zoomed_y_translation );
        }
        
        g.scale( getZoom(), getZoom() );
        
	panel_dimension = getSize();

	int panel_width = (int)((getBaseDimension().getWidth()-getZoomedXTranslation())/getZoom());
        int        panel_height = (int)((getBaseDimension().getHeight()-getZoomedYTranslation())/getZoom());
        int        panel_x = (int)(-getZoomedXTranslation()/getZoom());
        int        panel_y = (int)(-getZoomedYTranslation()/getZoom());

        
	// Display species
	Iterator n = the_gui.getModel().getSpecies().iterator();
	while( n.hasNext() )
	    {
		((Species)n.next()).display( this, (Graphics2D)g );
	    }

        g.dispose();
    }
   */

    public void removeDerivedSpecies() 
    {
        Vector<Species> delete_list = new Vector();
        Iterator<Species> itr = getAllSpecies().iterator();
        while ( itr.hasNext() )
        {
            Species current = itr.next();
            if ( current.isDerived() )
            {
                delete_list.add(current);
            }
        }
        
        Iterator<Species> delete_itr = delete_list.iterator();
        while ( delete_itr.hasNext() )
        {
            Species current = delete_itr.next();
            removeSpecies( current );
        }
        
        
    }
}
