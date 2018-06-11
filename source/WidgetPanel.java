import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.event.ChangeListener;
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure
import java.awt.geom.AffineTransform; // For transforms
import java.awt.geom.*; // For Line2D
import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable,
// and WidgetPanel needs to be serializable for writeObject
import javax.swing.JViewport.*;
import javax.swing.text.*; // for input dialog
 
import java.io.*; // For file io

import java.awt.image.BufferedImage;

public class WidgetPanel extends JPanel implements ActionListener, ChangeListener, Serializable, DropTargetListener //Scrollable
{
    
    
    protected class KeyboardControl implements KeyListener 
    {
        KeyboardControl(){}
        
        public void keyPressed(KeyEvent evt) 
        {
            if (debug_statements) if (debug_statements) System.out.println("Key Press Detected");
            
            int key = evt.getKeyCode();  // keyboard code for the key that was pressed
            
            if (key == KeyEvent.VK_DELETE || key == KeyEvent.VK_BACK_SPACE ) 
            {
                if ( getSelectedWidget() instanceof BioComponent )
                {
                    if ( !((BioComponent)getSelectedWidget()).isMovableByUser() ) return;
                }
                
                Widget w = getSelectedWidget();
                getTheGUI().getEditsManager().addEdit( new RemoveEdit( w, w.getX(), w.getY() ) );
                removeSelectedWidget();
                clearSelections();
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_C ) {
                copySelectedWidget();
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_V ) {
                pasteCopiedWidget();
            }
            else if (evt.isControlDown() && key == KeyEvent.VK_X ) {
                cutSelectedWidget();
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
    
    private class MouseControl extends MouseInputAdapter
    {
        transient protected boolean mouse_over = false;
             
        private WidgetPanel panel;
        
        private int current_x;
        
        private int current_y;
        
        private int num_clicks = 0;
        
        Widget previously_clicked = null;
        
        public MouseControl(WidgetPanel panel) {
            this.panel = panel;
        }
        
        public void mousePressed(MouseEvent e) 
        {
             
            // Compensate for zoom and translation
            int mouse_x = (int)((e.getX()-getZoomedXTranslation())*(1.0/getZoom()));
            int mouse_y = (int)((e.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
            if (debug_statements) System.out.println("Mouse press at device" + e.getX() + "," + e.getX() );
            if (debug_statements) System.out.println("Mouse press at user" + mouse_x + "," + mouse_y );
            
            if (debug_statements) System.out.println("Requesting input focus for this widgetpanel so it can handle keyboard events.");
            requestFocus();
            
            if (debug_statements) System.out.println("Number of components in this panel: " + components.size() );
            if (debug_statements) System.out.println("Number of containers in this panel: " + containers.size() );
            
            // Check for a leftover copy_the_selection_box action generated from a click outside the widget panel
            // ie a paste action from the toolbar that calls setSelectionBoxLocation
            // Since the button release was not in the widgetpanel with copy_the_selection_box may not have completed
            if ( copy_the_selection_box )
            {
                the_selection_box.releaseContents();
                copy_the_selection_box = false;
            }
            
            clearSelections();
            
            if ( mode.equals( "manipulate" ) ) {
            /*
            if (  e.isPopupTrigger() )
                    {
                        if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                        selected_species.displayPopupMenu( e );
                        selected_species.setSelected(false);
                        clearSelections();
                        return;
                    }
             */
                
                if (debug_statements) System.out.println("mouse button pressed at " + mouse_x + ", " + mouse_y );
                
                // Perhaps a selection box was pressed
                if ( the_selection_box.isInUse() == true ) 
                {
                    
                    
                    if (debug_statements) System.out.println( "the_selection_box.getX(): " + the_selection_box.getX() );
                    if (debug_statements) System.out.println( "the_selection_box.getY(): " + the_selection_box.getY() );
                    if (debug_statements) System.out.println( "the_selection_box.getHeight(): " + the_selection_box.getHeight() );
                    if (debug_statements) System.out.println( "the_selection_box.getWidth(): " + the_selection_box.getWidth() );
                    if ( the_selection_box.contains( mouse_x, mouse_y) ) {
                        if (debug_statements) System.out.println("the Selection box selected");
                        the_selection_box.calculatePointerOffset( mouse_x,mouse_y );
                        
                        if ( e.isPopupTrigger() ) {
                            the_selection_box.displayPopupMenu( e );
                            return;
                        }
                        
                        // Copy if shift key pressed
                        if ( ( e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK )
                        == InputEvent.SHIFT_DOWN_MASK ) 
                        
                        {
                            SelectionBox copy = the_selection_box.makeCopy();
                            the_selection_box.setSelected(false);
                            //
                            the_selection_box = copy;
                            
                            
                            if (debug_statements) System.out.println("New Selection Box has " + the_selection_box.getContents().size() + " widgets");
                            
                            // Release the copies contents back into the panel - now treat the
                            // original contents as the copy
                              
                            copy_the_selection_box = true;
                        }
                        
                        move_the_selection_box = true;
                        Point start = new Point( the_selection_box.getX(), the_selection_box.getY() );
                        the_selection_box.setDragStart( start );
                    }
                    else {
                        
                        the_selection_box.inUse(false);
                        move_the_selection_box = false;
                        copy_the_selection_box = false;
                        
                    }
                    
                    // No other actions should occur related to this mouse event
                    // when a selection box is selected
                    return;
                }
                
                // figure out which widget was pressed - if any. 
                
                // FlickrLabels first
                selected_label = getPressedFlickrLabel( mouse_x, mouse_y, flickr_labels );
                if ( selected_label != null )
                {
                    selected_label.setSelected( true );
                    selected_label.setEditable(true);
                    
                    // hand keyboard control to the label for now
                    FlickrLabel.KeyboardControl kl = selected_label.getKeyboardListener();
                    if (debug_statements) System.out.println("Handing keyboard input control to FlickerLabel");
                    
                    return;
                }
                
                // Operators next
                
                {
                    Iterator i = operators.iterator();
                    while ( i.hasNext() ) {
                        Operator o = (Operator)i.next();
                        if ( o.contains( mouse_x, mouse_y ) ) {
                            if (debug_statements) System.out.println("Operator pressed");
                            
                            selected_operator = o;
                            
                            if ( selected_operator != null ) {
                                if ( e.isPopupTrigger() ) {
                                    if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                    selected_operator.displayPopupMenu( e.getX(), e.getY() );
                                    return; // Don't even look at the possibility of resizing with the popup gesture
                                }
                                
                                selected_operator.setSelected(false);
                            }
                            selected_operator = o;
                            selected_operator.setSelected(true);
                            selected_operator.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_operator = true;
                            Point start_drag = new Point( selected_operator.getX() ,selected_operator.getY() );
                            selected_operator.setDragStart( start_drag );
                            
                            return;
                        }
                    }
                }
                
                // CDK Capsules 
                {
                    CDKCapsule cdk_capsule = getPressedCDKCapsule( mouse_x, mouse_y, cdk_capsules );
                    if ( cdk_capsule != null )
                    {
                        if (  e.isPopupTrigger() ) 
                        {
                            cdk_capsule.displayPopupMenu(mouse_x, mouse_y );
                        }
                        return;
                    }
                    
                }
                
                // Patterns next
                {
                    selected_pattern = getPressedPattern( mouse_x, mouse_y, patterns );
                    
                    if ( selected_pattern != null ) {
                        if (debug_statements) System.out.println("Pattern pressed");
                        if ( selected_pattern.isLocked() ) {
                            
                            if (  e.isPopupTrigger() ) {
                                if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                selected_pattern.displayPopupMenu( e.getX(), e.getY() );
                                selected_pattern.setSelected(true);
                                return;
                            }
                            
                            selected_pattern.setSelected(true);
                            selected_pattern.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_pattern = true;
                            return;
                        }
                    }
                }
                // Species next
                {
                    selected_species = getPressedSpecies( mouse_x, mouse_y, species );
                    
                    if ( selected_species != null ) {
                        if (debug_statements) System.out.println("Species pressed");
                        if ( selected_species.isLocked() ) {
                            
                            if (  e.isPopupTrigger() ) {
                                if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                selected_species.displayPopupMenu( e.getX(), e.getY() );
                                selected_species.setSelected(true);
                                return;
                            }
                            
                            selected_species.setSelected(true);
                            selected_species.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_species = true;
                            return;
                        }
                 
                        
                    }
                // Groups next
                {
                    selected_group = getPressedGroup( mouse_x, mouse_y, groups );
                    
                    if ( selected_group != null ) {
                        if (debug_statements) System.out.println("Group pressed");
                        
                            if (  e.isPopupTrigger() ) {
                                if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                selected_group.displayPopupMenu( e.getX(), e.getY() );
                                selected_group.setSelected(true);
                                return;
                            }
                            
                            selected_group.setSelected(true);
                            selected_group.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_group = true;
                            return;
                        }
                 
                        
                    }
                }
                        
                /*
                else // Species not locked so we need to find the
                    // widget inside the species that matches and select that instead
                {
                    //Components first
                    selected_component = getPressedComponent( e, selected_species.getComponents() );
                 
                    if ( selected_component != null )
                    {
                        if (  e.isPopupTrigger() )
                        {
                            if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                            displayComponentOptionsMenu( e );
                            selected_component.setSelected(false);
                            clearSelections();
                            return;
                        }
                 
                        selected_component.setSelected( true );
                        selected_component.calculatePointerOffset( mouse_x, mouse_y);
                        move_selected_component = true;
                        return;
                    }
                 
                    // Now containers
                    selected_container = getPressedContainer(e, selected_species.getContainers() );
                 
                    if ( selected_container != null )
                    {
                 
                        if (  e.isPopupTrigger() )
                        {
                            if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                            displayContainerOptionsMenu( e );
                            selected_container.setSelected(false);
                            clearSelections();
                            return;
                        }
                 
                            // Implementing container resize inside
                            // species will resuire more work - can only
                            // move containers for now
                                resize_selected_container = false;
                                move_selected_container = true;
                                selected_container.calculatePointerOffset( mouse_x,mouse_y );
                                selected_container.setSelected( true );
                    }
                 
                    return;
                }
                 */
                 //   }
               // }
            /*
            Iterator i = species.iterator();
            while ( i.hasNext() )
                {
                    Species s = (Species)i.next();
                    if ( s.contains(e) )
                        {
                            if (debug_statements) System.out.println("Species pressed");
             
                            if ( selected_species != null )
                            {
                                selected_species.setSelected(false);
                            }
                            selected_species = s;
             
                            //
                            if (  e.isPopupTrigger() )
                            {
                                if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                s.displayPopupMenu( e );
                                clearSelections();
                                return;
                            }
             
                            selected_species.setSelected(true);
                            selected_species.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_species = true;
                            return;
                        }
            }
             */
                
                
                
                
                
                // ReactionRules next
                {
                    ReactionRule r = getPressedReactionRule( mouse_x, mouse_y, getAllReactionRules() );
                    if ( r != null ) {
                        if (debug_statements) System.out.println("ReactionRule pressed");
                        if ( selected_reactionrule != null ) {
                            selected_reactionrule.setSelected(false);
                        }
                        selected_reactionrule = r;
                        selected_reactionrule.setSelected(true);
                        selected_reactionrule.calculatePointerOffset( mouse_x, mouse_y);
                        
                        if (  e.isPopupTrigger() ) {
                            if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                            selected_reactionrule.displayPopupMenu( e );
                            selected_reactionrule.setSelected(false);
                            clearSelections();
                            return;
                        }
                        
                        move_selected_reactionrule = true;
                        return;
                    }
                }
                
                // figure out which widget was pressed - if any. Components next
                {
                    Iterator i = components.iterator();
                    while ( i.hasNext() ) {
                        BioComponent c = (BioComponent)i.next();
                        if ( c.contains( mouse_x, mouse_y ) ) {
                            if (debug_statements) System.out.println("Component pressed");
                            
                            //If they are holding down the shift key remember this the component pressed
                            // Macintosh OS X uses the control key for popup triggers
                            if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK ) == InputEvent.SHIFT_DOWN_MASK ) {
                                if ( start_selected == false ) {
                                    if (debug_statements) System.out.println("Start of link successfully selected");
                                    start_component = c;
                                    start_component.setSelected(true);
                                    start_selected = true;
                                    return;
                                }
                                
                            }
                            
                            
                            
                            selected_component = c;
                            selected_component.setSelected( true );
                            
                            
                            // isPopupTrigger() happens on button _pressed_ in Linux and Mac
                            if ( e.isPopupTrigger() ) {
                                if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                                selected_component.displayPopupMenu( e.getX(), e.getY() );
                                move_selected_component = false;
                                return;
                            }
                            
                            //if ( selected_component.template == true )
                            //    {
                            //	selected_component = selected_component.make_new_component();
                            //	components.add( selected_component );
                            //    }
                            
                            // if we arn't doing something special with the component just move it
                            
                            
                            // Remove from any containers it might be in - will be added
                            // back in if the user didn't move it outside the container
                            // on button release
                            BioContainer temp = selected_component.getContainer();
                            
                            if ( temp != null ) {
                                temp.removeComponent(selected_component);
                            }
                            
                            selected_component.calculatePointerOffset( mouse_x, mouse_y);
                            move_selected_component = true;
                            Point start = new Point( selected_component.getX(), selected_component.getY() );
                            selected_component.setDragStart( start );
                            
                            //selected_component.setSelected(true);
                            return;
                        }
                    }
                    
                }
                
                // Edges next
                {
                    Iterator i = edges.iterator();
                    while ( i.hasNext() ) {
                        Edge edge = (Edge)i.next();
                        
                        if ( edge.contains(mouse_x, mouse_y ) ) {
                            if (debug_statements) System.out.println("Edge pressed");
                        
                            selected_edge = edge;
                            selected_edge.setSelected(true);
                            
                            if ( edge instanceof AtomMap && e.isPopupTrigger() )
                            {
                                ((AtomMap)edge).displayPopupMenu(e.getX(), e.getY() );
                                ///selected_edge.setSelected(false);
                                //selected_edge = null;
                                return;
                            }
                            
                            return;
                        }
                    }
                }
                
                
                // figure out which widget was pressed - if any. Containers next
                {
                selected_container = getPressedContainer( mouse_x, mouse_y, containers );
                
                if ( selected_container != null ) 
                {
                      if (debug_statements) System.out.println("Container pressed");
                    
                    if ( e.isPopupTrigger() ) {
                        if (debug_statements) System.out.println("Platform dependent popup trigger detected");
                        //displayContainerOptionsMenu( e.getX(), e.getY() );
                        selected_container.displayPopupMenu( e.getX(), e.getY() );
                        return; // Don't even look at the possibility of resizing with the popup gesture
                    }
                    
                    // See if a resize anchor was pressed
                    String  anchor = selected_container.resizeAnchor(mouse_x, mouse_y);
                    if ( anchor != null ) 
                    {
                        if ( anchor.equals("NE") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("NW") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("SE") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("SW") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        
                        
                        // Empty container - components will be added
                        // back in (if the user didn't rezize the container
                        // so they are no longer included) on mouse button
                        // release
                        
                        selected_container.removeAllComponents();
                        selected_container.setSelected(true);
                        
                        move_selected_container = false;
                    }
                    else {
                        resize_selected_container = false;
                        move_selected_container = true;
                        Point start = new Point( selected_container.getX(), selected_container.getY() );
                        selected_container.setDragStart( start );
                        selected_container.calculatePointerOffset( mouse_x,mouse_y );
                        selected_container.setSelected( true );
                    }
                    
                    return;
                }
                }
                
                  if (debug_statements) System.out.println("Nothing pressed");
                // nothing selected so they clicked on white space
                // perhaps they want to make a selection by dragging
                // a selection box. Tell the_selection_box where the
                // user clicked unless the selection box is already in use
                
                if ( the_selection_box.isInUse() == false ) {
                    the_selection_box.start_drag_x = mouse_x;
                    the_selection_box.start_drag_y = mouse_y;
                    the_selection_box.setX(mouse_x);
                    the_selection_box.setY(mouse_y);
                    the_selection_box.inUse(true);
                }
            }
            
            else if ( mode.equals("add_unbound_components")
                      || mode.equals("add_unspecified_components")
                      || mode.equals("add_bound_components")) 
            {
                selected_component = getPressedComponent( mouse_x, mouse_y, components );
                
                if ( selected_component != null ) 
                {
                    
                    move_selected_component = true;
                    selected_component.setSelected( true );
                    selected_component.calculatePointerOffset( mouse_x, mouse_y);
                    
                    
                }
                else {
                    
                    BioComponent new_component = new BioComponent( mouse_x, mouse_y, "c", null, false, panel );
                    
                    if ( mode.equals( "add_unbound_components" ) )
                    {
                        new_component.setBindingState( "No Additional Bonds" );
                    }
                    else if ( mode.equals( "add_unspecified_components" ) )
                    {
                        new_component.setBindingState( "Don't Care" );
                    }
                    else if ( mode.equals( "add_bound_components" ) )
                    {
                        new_component.setBindingState( "Additional Bonds" );
                    }
                    
                    int new_x = mouse_x - new_component.getWidth()/2;
                    int new_y = mouse_y - new_component.getHeight()/2;
                    new_component.updateLocation( new_x, new_y, true );
                    
                    addComponent( new_component );
                    selected_component = new_component;
                    selected_component.setSelected(true);
                    
                    AddEdit edit = new AddEdit( selected_component, selected_component.getX(), selected_component.getY() );
                    getTheGUI().getEditsManager().addEdit( edit );
                    
                    component_added = true;
                }
            }
            else if ( mode.equals("add_containers") ) {
                selected_container = getPressedContainer( mouse_x, mouse_y, containers );
                
                if ( selected_container != null ) {
                    
                    
                    String  anchor = selected_container.resizeAnchor(mouse_x, mouse_y);
                    if ( anchor != null ) 
                    {
                        if ( anchor.equals("NE") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("NW") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("SE") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        else if ( anchor.equals("SW") )
                        {
                            panel.setCursor( Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR ) );
                            resize_selected_container = true;
                        }
                        
                        selected_container.removeAllComponents();
                        selected_container.setSelected(true);
                        
                        move_selected_container = false;
                    }
                    else // move it
                    {
                        resize_selected_container = false;
                        move_selected_container = true;
                        selected_container.calculatePointerOffset( mouse_x,mouse_y );
                        selected_container.setSelected( true );
                    }
                }
                else {
                    
                    BioContainer new_container = new BioContainer( mouse_x, mouse_y, "C", panel );
                    
                    int new_x = mouse_x - new_container.getWidth()/2;
                    int new_y = mouse_y - new_container.getHeight()/2;
                    new_container.updateLocation( new_x, new_y, true );
                    addContainer( new_container );
                    selected_container = new_container;
                    new_container.setSelected(true);
                    
                    AddEdit edit = new AddEdit( selected_container, selected_container.getX(), selected_container.getY() );
                    getTheGUI().getEditsManager().addEdit( edit );
                    
                    container_added = true;
                    
                }
            }
           /*
            else if ( mode.equals("add_operators") ) {
                selected_operator = getPressedOperator( mouse_x, mouse_y, operators );
                
                if ( selected_operator != null ) {
                    
                    move_selected_operator = true;
                    selected_operator.calculatePointerOffset( mouse_x,mouse_y );
                    selected_operator.setSelected( true );
                }
                else {
                    
                    Operator new_operator = new Operator( mouse_x, mouse_y, "new", "none", false, panel );
                    new_operator.setX( mouse_x - new_operator.getWidth()/2);
                    new_operator.setY( mouse_y - new_operator.getHeight()/2);
                    addOperator( new_operator );
                    new_operator.setSelected(true);
                }
            }
            **/
            else if ( mode.equals("add_edges") ) {
                
                //clearSelections();
                // Determine which component was pressed - if any
                Iterator i = components.iterator();
                while ( i.hasNext() ) {
                    
                    BioComponent c = (BioComponent)i.next();
                    if ( c.contains( mouse_x, mouse_y ) ) {
                        if (debug_statements) System.out.println("Component pressed");
                        
                        if ( start_component == null ) {
                            if (debug_statements) System.out.println("Starting Component Selected");
                            start_component = c;
                            start_component.setSelected( true );
                        }
                        // check that the starting component is still exists in this panel
                        
                        else {
                            if ( components.indexOf( start_component ) != -1 ) {
                                // Attempt to link to itself - do nothing
                                if ( start_component == c ) {
                                    return;
                                }
                                
                                // Check that both components are contained - avoids some messy special cases.
                                //if ( containers.indexOf( start_component.getContainer() ) == -1
                                //    || containers.indexOf( c.getContainer() ) == -1 )
                                //{
                                //    displayError( "Edge Creation Error","Edges may only be created between components that are inside containers.");
                                //    return;
                                //}
                                
                                if (debug_statements) System.out.println("Creating Link");
                                linkComponents(start_component, c);
                                
                                // Record the add edit with the edits_manager inside
                                // the link components function
                                
                                start_component.setSelected( false );
                                start_component = null;
                            }
                            else {
                                // Something happened to the start_component while we wern't looking
                                // so set the start component to the newly selected component - presumably
                                // the user isnt trying to create a link starting with a component not
                                // in the widget panel. It is possible they are trying to link to a component
                                // internal to a species? Should that be permitted?
                                start_component = c;
                                start_component.setSelected( true );
                                
                            }
                        }
                        
                        return;
                    }
                }
            }
            else if ( mode.equals("cdk_model") )
            {
                if (debug_statements) System.out.println("Adding a new CDK Model");
                CDKCapsule capsule = new CDKCapsule(mouse_x, mouse_y, 50, 50, panel);
                panel.addCDKCapsuleToLocation(capsule, mouse_x, mouse_y );
            }
            else if ( mode.equals("atom_map") )
            {
                Mappable m = null; 
                m = getPressedComponent( mouse_x, mouse_y, getAllComponents() );
                if ( m == null ) m = getPressedContainer( mouse_x, mouse_y, getAllContainers() );
                
                if ( m != null )
                {
                    if ( start_map == null ) {
                            if (debug_statements) System.out.println("Start of Map Selected");
                            start_map = m;
                            start_map.setSelected( true );
                        }
                        
                    // check that the start of the map still exists in this panel
                        else {
                            if ( components.indexOf( start_map ) != -1 
                                || containers.indexOf( start_map ) != -1 ) 
                            {
                                // Attempt to link to itself - do nothing
                                if ( start_map == m ) {
                                    return;
                                }
                                
                                
                                if (debug_statements) System.out.println("Creating Map");
                                map(start_map, m);
                                
                                // Record the add edit with the edits_manager inside
                                // the link components function
                                
                                start_map.setSelected( false );
                                
                                if ( start_map instanceof BioComponent )
                                {
                                    if ( selected_component != null )
                                    {
                                        selected_component.setSelected( false );
                                    }
                                    selected_component = (BioComponent)start_map;
                                    selected_component.setSelected( true );
                                }
                                else if ( start_map instanceof BioContainer )
                                {
                                    if ( selected_container != null )
                                    {
                                        selected_container.setSelected( false );
                                    }
                                    selected_container = (BioContainer)start_map;
                                    selected_container.setSelected( true );
                                }
                                
                                start_map = null;
                            }
                            else {
                                // Something happened to the start_component while we wern't looking
                                // so set the start component to the newly selected component - presumably
                                // the user isnt trying to create a link starting with a component not
                                // in the widget panel. It is possible they are trying to link to a component
                                // internal to a species? Should that be permitted?
                                start_map = m;
                                start_map.setSelected( true );
                                
                            }
                        }
                }
            }
            else if ( mode.equals("add_plus_operators")
            || mode.equals("add_forward_operators")
            || mode.equals("add_forward_and_reverse_operators")
            || mode.equals("add_and_operators")
            || mode.equals("add_or_operators")
            || mode.equals("add_union_operators")  ) 
            {
                
                
                selected_operator = getPressedOperator( mouse_x, mouse_y, operators );
                    
                if ( selected_operator != null ) {
                
                    if ( e.isPopupTrigger() )
                    {
                        selected_operator.displayPopupMenu( e.getX(), e.getY() );
                        clearSelections();
                        return;
                    }

                    move_selected_operator = true;
                    selected_operator.calculatePointerOffset( mouse_x,mouse_y );
                    selected_operator.setSelected( true );
                                  
                }
                else {
                   
                                    
                    if (debug_statements) System.out.println("MousePressed:Add Operator");
                    
                    
                    Operator op = null;
                    
                    if ( mode.equals("add_plus_operators") ) {
                        
                        op = new Plus(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                       
                    }
                    else if ( mode.equals("add_forward_operators") ) {
                        op = new Forward(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                    }
                    else if ( mode.equals("add_forward_and_reverse_operators") ) {
                        op = new ForwardAndReverse(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                    }
                    else if ( mode.equals("add_and_operators") ) {
                        op = new And(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                    }
                    else if ( mode.equals("add_or_operators") ) {
                        op = new Or(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                    }
                    else if ( mode.equals("add_union_operators") ) {
                        op = new Union(mouse_x, mouse_y, panel );
                        
                        AddEdit edit = new AddEdit( op, op.getX(), op.getY() );
                        getTheGUI().getEditsManager().addEdit( edit );
                    }
                        
                    addSelectedOperator( op );   
                    op.updateLocation( mouse_x-op.getWidth()/2, mouse_y-op.getHeight()/2, true);
                }
            }
            else {
                
                displayError("Internal Error","Unknown mode. Contact support at support@bionetgen.com.");
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
            
           
           if (debug_statements) System.out.println("mouseDragged");
            
            // Compensate for zoom and translation
            int mouse_x = (int)((e.getX()-getZoomedXTranslation())*(1.0/getZoom()));
            int mouse_y = (int)((e.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
            //int size_limit = 6000;
            //    if ( mouse_x > size_limit || mouse_y > size_limit )
            //    {
            //        if (debug_statements) System.out.println("Size limit reached");
            //        return;
            //    }
        
            if (debug_statements) System.out.println( "getZoomedXTranslation():" + getZoomedXTranslation() );
            if (debug_statements) System.out.println( "getZoomedYTranslation():" + getZoomedYTranslation() );
            if (debug_statements) System.out.println("1/zoom:"+1.0/getZoom());
            
            int padding = 50;
            int new_left_x_loc = 0;
            int new_up_y_loc = 0;
            int new_right_x_loc = 0;
            int new_down_y_loc = 0;
            Widget sw = getSelectedWidget();
            
            if ( sw == null ) return;
            
            new_left_x_loc = sw.getZoomedX();
            new_up_y_loc = sw.getZoomedY();
            new_right_x_loc = sw.getZoomedX()+sw.getZoomedWidth();
            new_down_y_loc = sw.getZoomedY()+sw.getZoomedHeight();
            
            /*
            if ( sw instanceof SelectionBox )
            {
                new_left_x_loc = ((SelectionBox)sw).getX();
                new_up_y_loc = ((SelectionBox)sw).getY();
                new_right_x_loc = ((SelectionBox)sw).getX()+((SelectionBox)sw).getWidth();
                new_down_y_loc = ((SelectionBox)sw).getY()+((SelectionBox)sw).getHeight();
            }
            */
            
            int pref_width = getPreferredSize().width;
            int pref_height = getPreferredSize().height;
            
            if (debug_statements) System.out.println( "new_left_x_loc: "+ new_left_x_loc );    
            if (debug_statements) System.out.println( "new_up_y_loc: "+ new_up_y_loc ); 
            if (debug_statements) System.out.println( "new_right_x_loc: "+ new_right_x_loc ); 
            if (debug_statements) System.out.println( "new_down_x_loc: "+ new_down_y_loc ); 
            if (debug_statements) System.out.println( "preferred width: " + pref_width ); 
            if (debug_statements) System.out.println( "preferred height: "+ pref_height ); 
            //int new_x_loc = mouse_x;//+getSelectedWidget().getWidth()+padding; 
            //int new_y_loc = mouse_y;//+getSelectedWidget().getHeight()+padding;
            //if (!isInsideBoundaries( new_left_x_loc, new_up_y_loc ) 
            //|| !isInsideBoundaries( new_right_x_loc, new_down_y_loc ) )
            //{
                
 /*
                if ( pref_width < new_right_x_loc ) 
                {
                    pref_width = new_right_x_loc;
                }
                if ( pref_height < new_down_y_loc ) 
                {
                    pref_height = new_down_y_loc;
                }
 
                    if ( new_left_x_loc < 0 )
                    {
                        pref_width += -new_left_x_loc;
                        moveAllRight(getSelectedWidget(),-new_left_x_loc);
                    }

                     if ( new_up_y_loc < 0 )
                    {
                        pref_height += -new_up_y_loc;
                        moveAllDown(getSelectedWidget(),-new_up_y_loc);
                    }
                
                
                
                
                    setPreferredSize( new Dimension( pref_width, pref_height ) );
                    
                
                    //The user is dragging something, so scroll if needed
                    Rectangle r = new Rectangle(sw.getX(), sw.getY(), sw.getX()+sw.getWidth(), sw.getY()+sw.getHeight() );
                    scrollRectToVisible(r);
                    revalidate();
                    repaint();
            
             // }
            
   */         
            
            // Handle selection box
            
            
            // Move the selection box - only after creation
            
            
            
            // Resize the selection box - only during creation
            if ( the_selection_box.isInUse() == true && move_the_selection_box == false && copy_the_selection_box == false) {
                int height = mouse_y-the_selection_box.start_drag_y;
                int width = mouse_x-the_selection_box.start_drag_x;
                
                if (debug_statements) System.out.println("Selection Box Start Y = " + the_selection_box.start_drag_y);
                if (debug_statements) System.out.println("Selection Box Start X = " + the_selection_box.start_drag_x);
                if (debug_statements) System.out.println("Selection Box Height = " + height);
                if (debug_statements) System.out.println("Selection Box Width = " + width);
                
                if ( width > 0 ) 
                {
                    the_selection_box.setX(the_selection_box.start_drag_x);
                    the_selection_box.setWidth( width );
                }
                else {
                    the_selection_box.setX( mouse_x );
                    the_selection_box.setWidth( -1*width );
                }
                
                if ( height > 0 ) 
                {
                    the_selection_box.setY(the_selection_box.start_drag_y);
                    the_selection_box.setHeight( height );
                }
                else {
                    the_selection_box.setY( mouse_y );
                    the_selection_box.setHeight( -1*height );
                }
                
                //the_selection_box.setHeight( mouse_y-the_selection_box.getY() );
                //r = new Rectangle(the_selection_box.getX(), the_selection_box.getY(), the_selection_box.getWidth(), the_selection_box.getHeight());
                
                //scrollRectToVisible(r);
                
                repaint();
            }
            
            if (selected_container != null) 
            {
                if (move_selected_container) {
                    boolean confine_to_boundaries = true;
                    selected_container.updateLocation(mouse_x,mouse_y,confine_to_boundaries );
                }
                else if (resize_selected_container) {
                    selected_container.resize(mouse_x, mouse_y);
                }
                else {
                    if (debug_statements) System.out.println("Error condition in MouseDragged: not moving or resizing selected container!");
                }
               
            }
            
            else if (selected_component != null && move_selected_component) 
            {
                boolean confine_to_boundaries = true;
                if ( selected_component.isMovableByUser() )
                {
                    selected_component.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
                }
                //return;
            }
            
            else if (selected_operator != null && move_selected_operator) {
                boolean confine_to_boundaries = true;
                selected_operator.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
            }
            
            else if (selected_species != null && move_selected_species) {
                boolean confine_to_boundaries = true;
                if (debug_statements) System.out.println("Species x, y:" + mouse_x + "," + mouse_y);
                selected_species.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
            }
            else if (selected_reactionrule != null && move_selected_reactionrule) {
                boolean confine_to_boundaries = true;
                selected_reactionrule.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
            }
            else if (selected_pattern != null && move_selected_pattern) {
                boolean confine_to_boundaries = true;
                selected_pattern.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
            }
            else if (selected_group != null && move_selected_group) {
                boolean confine_to_boundaries = true;
                selected_group.updateLocation(mouse_x,mouse_y, confine_to_boundaries);
            }
            
            if ( move_the_selection_box == true ) //|| the_selection_box.isInUse() ) 
            {
                the_selection_box.updateLocation( mouse_x, mouse_y, true );
            }
            
            
            
            
            
        }
        
        // Handles the event of a user releasing the mouse button.
        synchronized public void mouseReleased(MouseEvent e) 
        {
            
            panel.setCursorAccordingToMode();
            
            // Compensate for zoom and translation
            int mouse_x = (int)((e.getX()-getZoomedXTranslation())*(1.0/getZoom()));
            int mouse_y = (int)((e.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
                
            
            //**************
            //  THIS CODE SHOULD BE REWRITTEN FROM SCRATCH
            //  IT HAS BEEN REWRITTEN IN PLACE TOO MANY TIMES
            //**************
            
            // Handle the selection box
            //Vector edge_remove_list = new Vector();
            //Vector component_remove_list = new Vector();
            //Vector container_remove_list = new Vector();
            //Vector operator_remove_list = new Vector();
            //Vector species_remove_list = new Vector();
                
            
            if ( the_selection_box.isInUse() == true ) {
                // find contained widgets
            
                Iterator widget_itr = getAllWidgets().iterator();
                while ( widget_itr.hasNext() )
                {
                    Widget current = (Widget)widget_itr.next();
                    if ( the_selection_box.contains( current ) )
                    {
                        the_selection_box.addWidget( current );
                        
                        // Handle component edges. Since they are managed by 
                        // components edges and maps have to be added like this...
                        
                        /*if ( current instanceof BioComponent )
                        {
                            BioComponent current_component = (BioComponent)current;
                            Iterator edge_itr = current_component.getEdges().iterator();
                            while( edge_itr.hasNext() ) 
                            {
                                Edge current_edge = (Edge)edge_itr.next();
                                the_selection_box.addWidget( current_edge );
                            }
                            
                            AtomMap map = current_component.getAtomMap();
                            if ( map != null )
                            {
                                the_selection_box.addWidget( map );
                            }
                        } 
                         */   
                    } 
                }// end outer while
                    
              
                /*
                Iterator i = components.iterator();
                while( i.hasNext() ) {
                    
                    Widget widget = (Widget)i.next();
                    Iterator l = ((BioComponent)widget).getEdges().iterator();
                    
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                            exp.printStackTrace();
                        
                        }
                        
                        the_selection_box.addWidget( widget );
                        //component_remove_list.add( widget );
                        
                        
                        while( l.hasNext() ) 
                        {
                            Widget edge_copy = null;
                            Widget e_widget = (Widget)l.next();
                            
                            try
                            {
                                edge_copy = (Edge)WidgetCloner.clone( e_widget );
                            }
                            catch( Exception exp )
                            {
                                exp.printStackTrace();
                            }
                            
                            //edge_remove_list.add( e_widget );
                            the_selection_box.addWidget( e_widget );
                        }
                        
                        
                        
                    }
                }
                
                
                Iterator j = containers.iterator();
                while( j.hasNext() ) {
                    Widget widget = (Widget)j.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        the_selection_box.addWidget( widget );
                        //container_remove_list.add( widget );
                    }
                }
                
                Iterator v = rules.iterator();
                while( v.hasNext() ) {
                    Widget widget = (Widget)v.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        the_selection_box.addWidget( widget );
                        //container_remove_list.add( widget );
                    }
                }
                
                Iterator k = operators.iterator();
                while( k.hasNext() ) {
                    Widget widget = (Widget)k.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                        }
                        
                        the_selection_box.addWidget( widget );
                        //operator_remove_list.add( widget );
                        }
                }
                
                Iterator l = species.iterator();
                while( l.hasNext() ) {
                    Widget widget = (Widget)l.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                        }
                        
                        the_selection_box.addWidget( widget );
                        //species_remove_list.add( widget );
                    }
                
                }
                
                
            }
            
            
                Iterator l = patterns.iterator();
                while( l.hasNext() ) {
                    Widget widget = (Widget)l.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                        }
                        
                        the_selection_box.addWidget( widget );
                        //species_remove_list.add( widget );
                    }
                
                }
                
                Iterator k = groups.iterator();
                while( k.hasNext() ) {
                    Widget widget = (Widget)k.next();
                    int x = widget.getX();
                    int y = widget.getY();
                    int width = widget.getWidth();
                    int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                        }
                        
                        the_selection_box.addWidget( widget );
                        //species_remove_list.add( widget );
                    }
                
                }
                
                Iterator label_itr = flickr_labels.iterator();
                while( label_itr.hasNext() ) 
                {
                    Widget widget = (Widget)label_itr.next();
                    //int x = widget.getX();
                    //int y = widget.getY();
                    //int width = widget.getWidth();
                    //int height = widget.getHeight();
                    if ( the_selection_box.contains( widget ) )
                        //|| the_selection_box.contains(x+width,y+height)
                        //|| the_selection_box.contains(x,y+height)
                        //|| the_selection_box.contains(x+width,y))
                    {
                        Widget copy = null;
                        try
                        {
                            //copy = WidgetCloner.clone( widget );
                        }
                        catch( Exception exp )
                        {
                            displayError("Widget Cloning Error","Exception cloning widget in MouseControl:MouseReleased().\nContact support at support@bionetgen.com.");
                        }
                        
                        the_selection_box.addWidget( widget );
                        //species_remove_list.add( widget );
                    }
                
                }
            
            if ( the_selection_box.getContents().size() > 0 ) {
                // do something with the widgets
                // example: change the widgets label
                for ( int i = 0; i < the_selection_box.getContents().size(); i++ ) {
                    ((Widget)the_selection_box.getContents().get(i)).setSelected(true);
                }
                
                // remove the items added to the selection_box from the panel
                //containers.removeAll( container_remove_list );
                //components.removeAll( component_remove_list );
                //edges.removeAll( edge_remove_list );
                //operators.removeAll( operator_remove_list );
                //species.removeAll( species_remove_list );
                */
                
                // Windows checks for popup trigger on release...
                if ( e.isPopupTrigger() ) {
                    the_selection_box.displayPopupMenu(e.getX(), e.getY());
                    return;
                }
            }
            else {
                the_selection_box.inUse(false);
            }
            
            
            // Find contained components so they can be added to the appropriate container
            // two cases: container was moved or component was moved
            // Clean up after a move or resize (deselect moved component or moved or resized container)
            
            // Operator moved
            if ( selected_operator != null && move_selected_operator ) {
                // Do not deselect widgets after a move
                
                //if (debug_statements) System.out.println("Operator deselected");
                //selected_operator.setSelected(false);
                //selected_operator = null;
                //move_selected_operator = false;
                
                // Create a move edit and pass it to the edits manager
                Point start = selected_operator.getDragStart();
                MoveEdit op_move_edit = new MoveEdit( selected_operator, (int)start.getX(), (int)start.getY() );
                getTheGUI().getEditsManager().addEdit( op_move_edit );
                
                //return;
            }
            
            // Container moved, resized, or added case
            else if ( selected_container != null && (move_selected_container || resize_selected_container || container_added) ) 
            {
                if ( move_selected_container )
                {
                    // Register the edit for undo/redo
                    Point start = selected_container.getDragStart();
                    MoveEdit move_edit = new MoveEdit( selected_container, (int)start.getX(), (int)start.getY() );
                    getTheGUI().getEditsManager().addEdit( move_edit );
                }
                    
                //Vector delete_me = new Vector(); // temp storage so the components vector isn't disturbed until after the for loop
                
                for ( int i = 0; i < components.size(); i++ ) {
                    BioComponent component = ((BioComponent)(components.get(i)));
                    //if ( selected_container.contains( component.getX(), component.getY() ) )
                    if ( selected_container.contains( component ) ) {
                        //if (debug_statements) System.out.println("Free component added to container and removed from free components");
                        
                        // Prevent containers from taking ownership of components if
                        // the component already has an owning container and the container
                        // moved not the component
                        
                        if ( component.getContainer() == null ) {
                            selected_container.addComponent( component );
                            
                            // yes, it should be obvious that selected container
                            // is selected - but this is more robust
                            component.setSelected( selected_container.isSelected() );
                        }
                        else {
                            if ( component.getContainer() != selected_container )
                                if (debug_statements) System.out.println( "This component is already contained" );
                        }
                        
                        // ... and remove from the old container
                        //if ( component.getContainer() != selected_container )
                        //    {
                        //	component.getContainer().removeComponent( component );
                        //	component.setContainer( selected_container );
                        //    }
                        
                        //delete_me.add( component );
                    }
                    
                }
                
                // now we can delete the components added to the container from the free_component vector
                //for ( int j = 0; j < delete_me.size(); j++ )
                //    {
                //	components.remove( delete_me.get(j) );
                //    }
                
                //delete_me.removeAllElements();
                
                // reset information needed for the last action
                //move_selected_container = false;
                //resize_selected_container = false;
                //clearSelections();
                //selected_component.setSelected(false);
                //selected_component = null;
                
                
            
            }
            // Component moved or added case
            else if ( selected_component != null && (move_selected_component || component_added ) ) {
                
                if ( move_selected_component )
                {
                    // Register the edit for undo/redo
                    Point start = selected_component.getDragStart();
                    MoveEdit move_edit = new MoveEdit( selected_component, (int)start.getX(), (int)start.getY() );
                    getTheGUI().getEditsManager().addEdit( move_edit );
                }
                
                for ( int i = 0; i < containers.size(); i++ ) {
                    BioContainer container = ((BioContainer)(containers.get(i)));
                    if ( container.contains( selected_component ) ) {
                        //if (debug_statements) System.out.println("Free component added to container and removed from free components");
                        
                        if ( selected_component.getContainer() != container
                        && selected_component.getContainer() != null ) {
                            selected_component.getContainer().removeComponent( selected_component );
                            selected_component.setContainer( container );
                        }
                        
                        container.addComponent( selected_component );
                        
                        //components.remove( selected_component );
                    }
                }
                
                // reset information needed for the last action
                move_selected_component = false;
                
                //selected_component.setSelected( false );
                //selected_component = null;
                //clearSelections();
            }
            
            // Components and Containers may have moved so check all
            
            // Assumption: there is no way for the selection box to affect a container without
            // also moving the components inside the container
            
            // Selection boc moved or copied
            else if ( move_the_selection_box == true ) 
            {
                
                if ( copy_the_selection_box == true )
                {
                    the_selection_box.releaseContents();
                    copy_the_selection_box = false;
                }
                
                    // Register the edit for undo/redo
                    Point start = the_selection_box.getDragStart();
                    MoveEdit move_edit = new MoveEdit( the_selection_box, (int)start.getX(), (int)start.getY() );
                    getTheGUI().getEditsManager().addEdit( move_edit );
                
                // decouple components from BioContainer
                Vector<BioComponent> v = the_selection_box.getComponents();
                Iterator itr = v.iterator();
                while( itr.hasNext() ) 
                {
                    BioComponent bcomp = (BioComponent)itr.next();
                    if ( bcomp.getContainer() != null ) 
                    {
                        BioContainer container = bcomp.getContainer();
                        container.removeComponent( bcomp );
                        bcomp.setContainer( null );
                    }
                }
                
                
                for ( int j = 0; j < components.size(); j++ ) 
                 {
                    
                    BioComponent component = (BioComponent)components.get(j);
                    for ( int i = 0; i < containers.size(); i++ ) 
                    {
                        BioContainer container = ((BioContainer)(containers.get(i)));
                        if ( container.contains( component  ) ) 
                        {
                            if (debug_statements) System.out.println("Component added to container");
                            container.addComponent( component );
                            component.setSelected( container.isSelected() );
                        }  
                    }
                }
                 
            }
            
            // The popup menu trigger happens when the mouse is released in MS Windows
                
                if ( e.isPopupTrigger() ) 
                {
                    if ( selected_component != null ) {
                        selected_component.displayPopupMenu( e.getX(), e.getY() );
                        //return;
                    }
                    else if ( selected_container != null ) {
                        //displayContainerOptionsMenu( e.getX(), e.getY() );
                        selected_container.displayPopupMenu( e.getX(), e.getY() );
                        //return;
                    }
                    else if ( selected_species != null ) {
                        selected_species.displayPopupMenu( e.getX(), e.getY() );
                        //return;
                    }
                    else if ( selected_operator != null ) {
                        if (debug_statements) System.out.println("Popup menu triggered.");
                        
                        if ( selected_operator instanceof ForwardAndReverse )
                        {
                            ((ForwardAndReverse)selected_operator).displayPopupMenu( e.getX(), e.getY() );
                        }
                        else if ( selected_operator instanceof Forward )
                        {
                            ((Forward)selected_operator).displayPopupMenu( e.getX(), e.getY() );
                        }
                        else
                        {
                            selected_operator.displayPopupMenu( e.getX(), e.getY() );
                        }
                            
                        //return;
                    }
                    else if ( selected_reactionrule != null ) {
                        if (debug_statements) System.out.println("Rule popup menu triggered.");
                        selected_reactionrule.displayPopupMenu( e.getX(), e.getY() );
                        //return;
                    }
                    else if ( the_selection_box.isInUse() ) {
                        if (debug_statements) System.out.println("Popup menu triggered (the_selection_box).");
                        the_selection_box.displayPopupMenu( e.getX(), e.getY() );
                        //return;
                    }
                }
            
            if (debug_statements) System.out.println("Check whether anything is in the selection box");
            if ( the_selection_box.getContents().isEmpty() )
            {
                the_selection_box.inUse(false);
            }
            
            //}
        }
        
        public BioContainer getPressedContainer(int mouse_x, int mouse_y, Vector<BioContainer> containers ) {
            Iterator i = containers.iterator();
            while ( i.hasNext() ) {
                BioContainer c = (BioContainer)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public Species getPressedSpecies(int mouse_x, int mouse_y, Vector<Species> species ) {
            Iterator i = species.iterator();
            while ( i.hasNext() ) {
                Species c = (Species)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public Edge getPressedEdge(int mouse_x, int mouse_y, Vector<Edge> edges ) {
            Iterator i = edges.iterator();
            while ( i.hasNext() ) {
                Edge c = (Edge)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public AtomMap getPressedAtomMap(int mouse_x, int mouse_y, Vector<Edge> edges ) {
            Iterator i = edges.iterator();
            while ( i.hasNext() ) 
            {
                Widget w = (Widget)i.next();
                if( w instanceof AtomMap )
                {
                    AtomMap m = (AtomMap)w;
                    if ( m.contains( mouse_x, mouse_y ) ) 
                    {
                        return m;
                    }
                }
            }
            
            return null;
        }
        
        public Widget getPressedWidget(int mouse_x, int mouse_y ) 
        {
            Widget pressed = null;
            if (( pressed = getPressedComponent( mouse_x, mouse_y, components ))!=null ) return pressed;
            if (( pressed = getPressedContainer( mouse_x, mouse_y, containers ))!=null ) return pressed;
            if (( pressed = getPressedEdge( mouse_x, mouse_y, edges ))!=null ) return pressed;
            if (( pressed = getPressedPattern( mouse_x, mouse_y, patterns ))!=null ) return pressed;
            if (( pressed = getPressedReactionRule( mouse_x, mouse_y, rules ))!=null ) return pressed;
            if (( pressed = getPressedOperator( mouse_x, mouse_y, operators ))!=null ) return pressed;
            if (( pressed = getPressedGroup( mouse_x, mouse_y, groups ))!=null ) return pressed;
            if (( pressed = getPressedFlickrLabel( mouse_x, mouse_y, flickr_labels ))!=null ) return pressed;
            
            return pressed;
        }
        
        public FlickrLabel getPressedFlickrLabel(int mouse_x, int mouse_y, Vector flickr_labels ) 
        {
            Iterator i = flickr_labels.iterator();
            while ( i.hasNext() ) {
                FlickrLabel c = (FlickrLabel)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) 
                {
                    return c;
                }
            }
            
            return null;
        }
        
        public CDKCapsule getPressedCDKCapsule(int mouse_x, int mouse_y, Vector<CDKCapsule> cdk_capsules ) {
            Iterator i = cdk_capsules.iterator();
            while ( i.hasNext() ) {
                CDKCapsule c = (CDKCapsule)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public Group getPressedGroup(int mouse_x, int mouse_y, Vector<Group> group ) {
            Iterator i = group.iterator();
            while ( i.hasNext() ) {
                Group c = (Group)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public Pattern getPressedPattern(int mouse_x, int mouse_y, Vector<Pattern> patterns ) {
            Iterator i = patterns.iterator();
            while ( i.hasNext() ) {
                Pattern c = (Pattern)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public ReactionRule getPressedReactionRule(int mouse_x, int mouse_y, Vector<ReactionRule> rules ) {
            Iterator i = rules.iterator();
            while ( i.hasNext() ) {
                ReactionRule c = (ReactionRule)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public Operator getPressedOperator(int mouse_x, int mouse_y, Vector<Operator> operators ) {
            Iterator i = operators.iterator();
            while ( i.hasNext() ) {
                Operator c = (Operator)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        public BioComponent getPressedComponent(int mouse_x, int mouse_y, Vector<BioComponent> components ) {
            Iterator i = components.iterator();
            while ( i.hasNext() ) {
                BioComponent c = (BioComponent)i.next();
                if ( c.contains( mouse_x, mouse_y ) ) {
                    return c;
                }
            }
            
            return null;
        }
        
        // This method is required by MouseListener.
        public void mouseMoved(MouseEvent e) 
        {
            // Compensate for zoom and translation
            int mouse_x = (int)((e.getX()-getZoomedXTranslation())*(1.0/getZoom()));
            int mouse_y = (int)((e.getY()-getZoomedYTranslation())*(1.0/getZoom())); 
            //if (debug_statements) System.out.println("Mouse move at device" + e.getX() + "," + e.getX() );
            //if (debug_statements) System.out.println("Mouse move at user" + mouse_x + "," + mouse_y );
            
            //this.current_x = e.getX();
            //this.current_y = e.getY();
            this.current_x = mouse_x;
            this.current_y = mouse_y;
            
            if ( getMode() != null )
            if ( getMode().equals("add_edges") || getMode().equals("atom_map") )
            {
                if ( start_component != null || start_map != null )
                {
                    repaint();
                }
            }
            
            //if (debug_statements) System.out.println("Mouse moved to " + getX() + "," + getY() );
            
        }
        
        // These methods are required by MouseMotionListener.
        public void mouseClicked(MouseEvent e)
        {
             
            Widget w = getPressedWidget(e.getX(), e.getY());
            
            if (debug_statements) System.out.println("Last clicked " + previously_clicked + " Just clicked: " + w );
            num_clicks++;
          
            //if ( e.getClickCount() == 1 )
            //{
                // Treat this as a mouse button press
                //mousePressed( e );
                //e.consume();
            //}
            //if ( previously_clicked == w )
            //else 
            if ( e.getClickCount() == 2 )
            {
                // Compensate for zoom and translation
                int mouse_x = (int)((e.getX()-getZoomedXTranslation())*(1.0/getZoom()));
                int mouse_y = (int)((e.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
                
                if ( w instanceof BioComponent )
                {
                   ((BioComponent)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof BioContainer )
                {
                   //displayContainerOptionsMenu( e.getX(), e.getY() );
                    ((BioContainer)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof ReactionRule )
                {
                   ((ReactionRule)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof Species )
                {
                   ((Species)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof SelectionBox )
                {
                   ((SelectionBox)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof Group )
                {
                   ((Group)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof Edge )
                {
                   ((Edge)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof Operator )
                {
                   ((Operator)w).displayPopupMenu( e.getX(), e.getY() );
                }
                else if ( w instanceof Pattern )
                {
                   ((Pattern)w).displayPopupMenu( e.getX(), e.getY() );
                }
                
                num_clicks = 0;
            }
            
            previously_clicked = w;
        }
        
        public void mouseExited(MouseEvent e)
        {
            mouse_over = false;
        }
        
        public void mouseEntered(MouseEvent e)
        {
            mouse_over = true;
            // get input focus for the keyboard
            //requestFocus();
        }
        
        public int getX() {
            return current_x;
        }
        
        public int getY() {
            return current_y;
        }
                
      
        
    }
    

    
    transient protected boolean debug_statements = true;
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    private boolean recognized;
    protected Point pressedPoint;
    protected boolean resize_selected_container = false;
    protected boolean move_selected_container = false;
    protected boolean move_selected_component = false;
    protected boolean move_the_selection_box = false;
    protected boolean copy_the_selection_box = false;
    protected boolean move_selected_operator = false;
     protected boolean move_selected_group = false;
    protected boolean move_selected_species = false;
    protected boolean move_selected_pattern = false;
    protected boolean move_selected_reactionrule = false;
    
    
    protected SelectionBox the_selection_box;
    protected Vector<BioComponent> components = new Vector<BioComponent>();
    protected Vector<FlickrLabel> flickr_labels = new Vector<FlickrLabel>();
    protected Vector<BioContainer> containers = new Vector<BioContainer>();
    protected Vector<CDKCapsule> cdk_capsules = new Vector<CDKCapsule>();
    
    protected Vector<Edge> edges = new Vector<Edge>();
    protected Vector<Operator> operators = new Vector<Operator>();
    protected Vector<Species> species = new Vector<Species>();
    protected Vector<ReactionRule> rules = new Vector<ReactionRule>();
    protected Vector<Reaction> reactions = new Vector<Reaction>();
    protected Vector<Pattern> patterns = new Vector<Pattern>();
    protected Vector<Group> groups = new Vector<Group>();
    
    protected Edge selected_edge;
    protected BioContainer selected_container;
    protected BioComponent selected_component;
    protected Operator selected_operator;
    protected Species selected_species;
    protected ReactionRule selected_reactionrule;
    protected ReactionRule selected_reaction;
    protected Pattern selected_pattern;
    protected Group selected_group;
    protected FlickrLabel selected_label;
    
    //protected Widget copied_widget;
    //protected SelectionBox copied_selection_box; 
    
    protected BioComponent start_component;
    protected Mappable start_map;
    
    protected BioComponent end_link;
    protected boolean start_selected = false;
    
    protected Dimension panel_dimension;
    //protected WidgetTransferHandler widget_transfer_handler = new WidgetTransferHandler();
    protected MouseControl mouse_control = new MouseControl( this );
    protected KeyboardControl keyboard_control = new KeyboardControl();
    
    
    protected Model the_model;
    
    private String mode = "manipulate";
    
    private boolean container_added = false;
    
    private boolean component_added = false;
    
    protected transient GUI the_gui;
    
    static long biograph_id;
    
    protected double zoom_factor = 1.0;
    
    protected int x_trans = 0;
    protected int y_trans = 0;
    
    protected int zoomed_x_translation = 0;
    protected int zoomed_y_translation = 0;
    
    // Extra room around the edge of what should be painted as background
    // on refresh - in case of labels that extend beyond the panel edge and which
    // might be later exposed during a zoom action.
    private int edge_buffer = 100;
    
    private boolean is_selected = false;
     
    protected Dimension base_dimension;
    protected Dimension area;

    protected boolean area_changed = false;
    
    transient private Rectangle drag_ghost_rect = new Rectangle();
    
    transient protected DragSource dragSource;
    transient protected DragGestureRecognizer dgr;
    
    //private String mode = "add_containers";
     
    public WidgetPanel() 
    {
        
    }
    
    /**
     *
     * @param the_gui
     */
    public WidgetPanel( GUI the_gui ) 
    {
        this.the_gui = the_gui;
        
        the_selection_box = new SelectionBox(this);
        
        //setDropTarget(new DropTarget(this, widget_transfer_handler.getDropHandler()));
        addMouseMotionListener(mouse_control);
        addMouseListener(mouse_control);
        addKeyListener( keyboard_control );
        
        setAutoscrolls(true); //enable synthetic drag events

        // Setup Drop listener
        new DropTarget(this, // component
        DnDConstants.ACTION_COPY_OR_MOVE, // actions
         this); // DropTargetListener
        
        
        
        setBackground(Color.white);
        
        area = new Dimension(10000,10000);
        this.setSize( area );
        base_dimension = getSize();
        //setPreferredSize(area);
        
        
    }
    
    private String getNextBioGraphLabel() 
    {
        String label = "BioGraph"+biograph_id++;
        if (debug_statements) System.out.println("Assigned new biograph the label "+label);
        return label;
    }
    
    synchronized void addModel( Model the_model ) {
        this.the_model = the_model;
    }
    
    /*
    protected void paintComponent(Graphics g) 
    {
        Graphics2D g2d = (Graphics2D)g;
        
        base_dimension = getSize();
         
        //Graphics2D g2d = (Graphics2D) ((Graphics2D)g).create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        
        if (isOpaque()) { //paint background
            g2d.setColor(getBackground());
            //g.fillRect( getX(), getY(), getWidth(), getHeight() );
            g2d.fillRect( getX(), getY(), (int)(base_dimension.getWidth()), (int)(base_dimension.getHeight()) );
                
            // This isnt good - WidgetPanel and WidgetPalette size accessors should be
            // made consistent. - when editor has scroll bars this will be a must
            if ( this instanceof WidgetPalette )
            {
                Rectangle r = new Rectangle(0,0,(int)getArea().getWidth(),(int)getArea().getHeight());
                JViewport vp = getTheGUI().getMoleculeScrollPane().getViewport();
                //int h = vp.getHeight();
                //int w = vp.getWidth();
                int w = (int)getTheGUI().getMoleculePaletteFrame().getWidth();
                int h = (int)getTheGUI().getMoleculePaletteFrame().getHeight();
                int spx = 0;
                int spy = 0;
                
                g2d.setColor( Color.RED );
                g2d.drawString(w+","+h, getX(), h);
                g2d.drawRect( spx, spy, w, h );
                g2d.drawString(getX()+","+h, getX(), h);
                
                
                g2d.setColor( getBackground() );
                if (debug_statements) System.out.println("Base Height: " + base_dimension.getHeight() );
            }
            else
            {
                g2d.fillRect( getX(), getY(), getWidth(), getHeight() );
            }
        }
        
            //
            // Now scale the Graphics context
            //
        if (debug_statements) System.out.println( "Zoom Factor is " + zoom_factor );
        
        // Only keep the items zoomed centered in the editor panel
        // allow them to move up and left in palettes
        if ( !(this instanceof WidgetPalette) )
        {
            double x_zoom_compensate = (base_dimension.getWidth() - base_dimension.getWidth()*zoom_factor)/2;
            double y_zoom_compensate = (base_dimension.getHeight() - base_dimension.getHeight()*zoom_factor)/2;
            zoomed_x_translation = (int)x_zoom_compensate+x_trans;
            zoomed_y_translation = (int)y_zoom_compensate+y_trans;
            if (debug_statements) System.out.println("New Origin: (" + zoomed_x_translation + "," + zoomed_y_translation + ")");
            if (debug_statements) System.out.println("Old Origin: (" + -zoomed_x_translation*(1.0/zoom_factor) + "," + -zoomed_y_translation*(1.0/zoom_factor) + ")");
            g2d.translate( zoomed_x_translation, zoomed_y_translation );
        }
            g2d.scale(zoom_factor, zoom_factor);
            
            //g2d.setColor(Color.BLACK);
            //g2d.drawString("NewO", 0, 0 );
            //g2d.drawString("OldO", (int)(-zoomed_x_translation*(1.0/zoom_factor)), (int)(-zoomed_y_translation*(1.0/zoom_factor)) );
            //g2d.translate( old_dimension.getWidth()*zoom_factor, old_dimension.getHeight()*zoom_factor );
            
        // Get panel dimension for setLocation to use in bounds checking
        panel_dimension = getSize();
        
        
        
        // Display the containers first, then selected container,
        // then components, then operators and finally selected
        // widgets and the selection box
        
        Iterator i = edges.iterator();
        while( i.hasNext() ) {
            ((Edge)i.next()).display(this, g2d);
        }
        
        Iterator j = containers.iterator();
        while ( j.hasNext() ) {
            ((BioContainer)j.next()).display(this, g2d);
        }
        
        if ( selected_container != null ) {
            selected_container.display( this, g2d );
        }
        
        Iterator k = components.iterator();
        while( k.hasNext() ) {
            ((BioComponent)k.next()).display(this, g2d);
        }
        
        Iterator l = operators.iterator();
        while( l.hasNext() ) {
            ((Operator)l.next()).display(this, g2d);
        }
        
        Iterator m = patterns.iterator();
        while( m.hasNext() ) {
            ((Pattern)l.next()).display(this, g2d);
        }
        
        if ( selected_component != null ) {
            selected_component.display( this, g2d );
        }
        else if ( selected_operator != null ) 
        {
            selected_operator.display( this, g2d );
        }
        
        // Display species
        Iterator n = species.iterator();
        while( n.hasNext() ) {
            ((Species)n.next()).display( this, g2d );
        }
        
        // Display rules
        Iterator o = rules.iterator();
        while( o.hasNext() ) {
            ((ReactionRule)o.next()).display( this, g2d );
        }
        
        // display selection box
        if ( the_selection_box != null ) {
            the_selection_box.display( this, g2d );
        }
        
         
        
        // we dont want the scaled copy hanging around in memory
        g2d.dispose();
    }
    */
    
    protected void paintComponent(Graphics gr) 
    {    
        //base_dimension = getSize();
        //Graphics2D g = (Graphics2D)gr;
        
        Graphics2D g = (Graphics2D) ((Graphics2D)gr).create();
        
        //setPreferredSize( getSize() );
        
        //if ( getZoom() != 1.0 )
        {
            RenderingHints renderHints =
            new RenderingHints(RenderingHints.KEY_ANTIALIASING,
                     RenderingHints.VALUE_ANTIALIAS_ON);
            renderHints.put(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);

            g.setRenderingHints(renderHints);
        }
        
	// Get panel dimension for setLocation to use in bounds checking
        
        if (isOpaque()) 
	    { //paint background
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight() );
	    }
        
        /*
        if ( !(this instanceof WidgetPalette) )// i.e. this is the editor panel
        {
            double x_zoom_compensate = (base_dimension.getWidth() - base_dimension.getWidth()*zoom_factor)/2;
            double y_zoom_compensate = (base_dimension.getHeight() - base_dimension.getHeight()*zoom_factor)/2;
            zoomed_x_translation = (int)x_zoom_compensate+x_trans;
            zoomed_y_translation = (int)y_zoom_compensate+y_trans;
            //if (debug_statements) System.out.println("New Origin: (" + zoomed_x_translation + "," + zoomed_y_translation + ")");
            //if (debug_statements) System.out.println("Old Origin: (" + -zoomed_x_translation*(1.0/zoom_factor) + "," + -zoomed_y_translation*(1.0/zoom_factor) + ")");
            g.translate( zoomed_x_translation, zoomed_y_translation );
        }
        */
         
        g.scale( getZoom(), getZoom() );
        
	panel_dimension = getSize();

	int panel_width = (int)((getBaseDimension().getWidth()-getZoomedXTranslation())/getZoom());
        int        panel_height = (int)((getBaseDimension().getHeight()-getZoomedYTranslation())/getZoom());
        int        panel_x = (int)(-getZoomedXTranslation()/getZoom());
        int        panel_y = (int)(-getZoomedYTranslation()/getZoom());
        
        
        
        // Draw bounding box for debugging
        /*
	g.setColor(Color.red);
	BasicStroke default_stroke = new BasicStroke(2.0f);
	BasicStroke dashed_stroke = new BasicStroke(1f,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND,1f,new float[] {2f},0f);
	
	Stroke def = g.getStroke();
	g.setStroke ( dashed_stroke );
	g.drawRect( panel_x, panel_y, panel_width-10, panel_height-10);
	g.setStroke( def );
	*/
        
    
        
	Iterator i = edges.iterator();
	while( i.hasNext() )
	    {
		((Edge)i.next()).display(this, g);
	    }          

        Iterator k = components.iterator();
	while( k.hasNext() )
	    {
		((BioComponent)k.next()).display(this, g);
	    }
        
        // Display the potential edge
            if ( getMode() != null )
            {
                if (getMode().equals("add_edges") || getMode().equals("atom_map") )
                {
                        int start_x = 0;
                        int start_y = 0;
                        int start_width = 0;
                        int start_height = 0;
                        int end_x = 0;
                        int end_y = 0;
                        
                    // This is inefficient - we know the location of
                        // the start won't change until the drag stops
                    if ( start_component != null )
                    {
                        start_x = start_component.getX();
                        start_x += start_component.getWidth()/2;
                        start_y = start_component.getY();
                        start_y += start_component.getHeight()/2;
                    }
                    else if ( start_map != null )
                    {
                        start_x = (int)start_map.getEdgeAttachPoint().getX();
                        start_y = (int)start_map.getEdgeAttachPoint().getY();
                    }
                    
                        if ( start_map != null || start_component != null )
                        {
                        end_x = mouse_control.current_x;
                        end_y = mouse_control.current_y;
                  
                        g.setColor(Color.DARK_GRAY);
                        

                        
                        Line2D line = new Line2D.Float( start_x, start_y,
					   end_x, 
					   end_y);
                        //g2d.setStroke(new BasicStroke(8));
                        //Graphics2D g2d = (Graphics2D)g;
                        Stroke default_stroke = ((Graphics2D)g).getStroke();
                        
                        	((Graphics2D)g).setStroke(new BasicStroke(
				     1f, 
				     BasicStroke.CAP_ROUND, 
				     BasicStroke.JOIN_ROUND, 
				     1f, 
				     new float[] {2f}, 
				     0f));
                        ((Graphics2D)g).draw(line);
                        
                        ((Graphics2D)g).setStroke( default_stroke );
                        
                        g.setColor(getBackground());
                        
                    }
                }
            }
        
        // Display CDk Capsules
        
        /*
        Iterator h = cdk_capsules.iterator();
	while( h.hasNext() )
	    {
		((CDKCapsule)h.next()).display(this, g);
	    }
        */
        
	Iterator j = containers.iterator();
	while ( j.hasNext() )
	    {
		((BioContainer)j.next()).display(this, g);
	    }             

        // Drag icon testing
        /*
        if ( getSelectedWidget() != null ) 
        {
            BufferedImage image = getSelectedWidget().createImage();
            int width = getSelectedWidget().getWidth();
            int height = getSelectedWidget().getHeight();
            g.drawImage( image, 0, 0, width, height, this );
        }
        */
	
	
	Iterator l = operators.iterator();
	while( l.hasNext() )
	    {
		((Operator)l.next()).display(this, g);
	    }
        
        Iterator m = patterns.iterator();
	while( m.hasNext() )
	    {
		((Pattern)m.next()).display(this, g);
	    }
        
	if ( selected_component != null ) 
	    {
		selected_component.display( this, g );
	    }
	else if ( selected_operator != null ) 
	    {
		selected_operator.display( this, g );
	    }

	// Display species
	Iterator n = species.iterator();
	while( n.hasNext() )
	    {
		((Species)n.next()).display( this, (Graphics2D)g );
	    }
        
        // Display rules
	Iterator o = rules.iterator();
	while( o.hasNext() )
	    {
		((ReactionRule)o.next()).display( this, (Graphics2D)g );
	    }
        
        
        //if (this instanceof ReactionPalette) if (debug_statements) System.out.println("Displaying " + reactions.size() + " Reactions");
        
        // Display reactions
	// Limit the number of reactions displayed to 100 or less
        //if ( reactions.size() <= 100 )
        //{
        
        Iterator p = reactions.iterator();
	while( p.hasNext() )
	    {
                if (debug_statements) System.out.println("Displaying Reaction in Widget Panel");
		((Reaction)p.next()).display( this, (Graphics2D)g );
	    }
        
        
        // Display groups
        if ( groups != null ) // For bacwards compatibility with previous versions that had no groups
        {
            Iterator q = groups.iterator();
            while( q.hasNext() )
	    {
                if (debug_statements) System.out.println("Displaying Group");
		((Group)q.next()).display( this, (Graphics2D)g );
	    }
        }
        
        Iterator f = flickr_labels.iterator();
	while( f.hasNext() )
	    {
		((FlickrLabel)f.next()).display(this, g);
	    }  
        
	// display selection box
        if ( the_selection_box != null )
        {
            the_selection_box.display( this, (Graphics2D)g );
        }
   
        g.dispose();
    }
   
    
    BioComponent getSelectedComponent() {
        return selected_component;
    }
    
    BioContainer getSelectedContainer() {
        return selected_container;
    }
    
    Species getSelectedSpecies() {
        return selected_species;
    }
        
    Pattern getSelectedPattern() {
        return selected_pattern;
    }
    
    Group getSelectedGroup() 
    {
        return selected_group;
    }
    
    SelectionBox getTheSelectionBox() 
    {
        return the_selection_box;
    }
    
    ReactionRule getSelectedReactionRule() {
        return selected_reactionrule;
    }
    
    Operator getSelectedOperator() {
        return selected_operator;
    }
    
    FlickrLabel getSelectedLabel() 
    {
        return selected_label;
    }
    
    Widget getSelectedWidget() {
        if ( getSelectedEdge() != null ) return getSelectedEdge();
        else if ( getSelectedContainer() != null ) return getSelectedContainer();
        else if ( getSelectedOperator() != null ) return getSelectedOperator();
        else if ( getSelectedReactionRule() != null ) return getSelectedReactionRule();
        else if ( getSelectedComponent() != null ) return getSelectedComponent();
        else if ( getSelectedSpecies() != null ) return getSelectedSpecies();
        else if ( getTheSelectionBox().isInUse() ) return getTheSelectionBox();
        else if ( getSelectedGroup() != null ) return getSelectedGroup();
        else if ( the_selection_box.isInUse() ) return the_selection_box;
        return null;
    }
    
    // For the popup menu
    public void actionPerformed( ActionEvent e ) {
        // Add action handling code here
        if (debug_statements) System.out.println( e );
        if (debug_statements) System.out.println( e.getActionCommand() );
        //if (selected_component == null && selected_container == null )
        //    {
        //	displayError("Fatal Error", "Nothing selected yet context menu displayed."
        //                       + "Contact Support at support@bionetgen.com");
        //    }
        
        if (selected_component != null && selected_container != null ) 
        {
            displayError("Fatal Error", "A container and a component are both selected. This shouldn't be able to happen. Contact support at support@bionetgen.com" );
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
        else if ( e.getActionCommand().equals("print") ) 
        {
               print();
        }
        else if ( e.getActionCommand().equals("cut") ) 
        {
                if ( !cutSelectedWidget() )
                {
                    displayError("Error Cutting Widget", "There was an error cutting the widget. Contact support at support@bionetgen.com.");
                }
        }
        else if ( e.getActionCommand().equals("copy") ) 
        {
                if ( !copySelectedWidget() )
                {
                    displayError("Error Copying Widget", "There was an error copying the widget. Contact support at support@bionetgen.com.");
                }
        }
        else if ( e.getActionCommand().equals("paste") ) 
        {
            int width = 0;
            int height = 0;
            if ( getCopiedWidget() != null )
            {
             width = getCopiedWidget().getWidth();
             height = getCopiedWidget().getHeight();
            
                if ( !pasteCopiedWidget() )
                {
                    displayError("Error Pasting Widget", "There was an error pasting the widget. Contact support at support@bionetgen.com.");
                }
             }
            else
            {
                displayWarning("Could not Paste", "Nothing has been copied or cut.");
            }
                
        }
        else if ( e.getActionCommand().equals("Zoom In") ) 
        {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*1.05, false);
        }
        else if ( e.getActionCommand().equals("Zoom Out") ) 
        {
                double current_zoom_factor = getZoom();
                setZoom( current_zoom_factor*0.95, false);
        }
        else if ( e.getActionCommand().equals("Autozoom") ) 
        {
                autoZoom( false );
        }
        else if ( e.getActionCommand().equals("Create Molecule Type") ) 
        {
            if ( selected_container == null )   
            {
                displayError("Molecule Type Creation Error", "The selected container was NULL in WidgetPanel:actionPerformed()");
            }
            
            getTheGUI().getMoleculePalette().addMoleculeType( selected_container );
            
        }
        else if ( e.getActionCommand().equals("Make into Species") ) 
        {
            Species s = null;
            float concentration;
            
            if ( selected_component != null ) 
            {
                // Make the species using the selected component as the starting point
                s = makeSpecies( selected_component, "0" );
            }
            else if ( selected_container != null ) 
            {
                if ( selected_container.getComponents().size() == 0 ) 
                {
                    displayError( "Species Creation Error","Species must contain at least one component");
                }
                else {
                    // Make the species using the first component in the container
                    // as the starting point
                    
                    // Make the species using the selected component as the starting point
                    BioComponent biocomp = (BioComponent)selected_container.getComponents().get(0);
                    if (debug_statements) System.out.println( biocomp );
                    
                    s = makeSpecies( biocomp, "0" );
                    
                }
            }
            
            // Get the user to set the initial concentration - don't add the
            // species if the user cancels
                if ( !s.setPropertiesFromUser() )
                    {
                        return;
                    }
                
            
            // Error check = if null then do not add the species - its makeSpecies
            // job to handle the error and inform the user
            if ( s == null ) {
                //displayError("Species Creation Error","Species could not be created because of an internal error.\nContact support at support@bionetgen.com");
                return;
            }
            
            if (debug_statements) System.out.println("Making Species");
            
            //the_model.addSpecies(s);
            
            Species copy = null;
            
            
            try {
                copy = (Species)WidgetCloner.clone(s);
            }
            catch ( Exception exp ) {
                displayError( "Species Clone Error", "There was an exception while cloning the species." +
                "The exception was: " + exp.getMessage() +"\nContact support at support@bionetgen.com" );
                return;
            }
            
            if ( copy == null ) {
                displayError( "Species Clone Error", "There was an error while cloning the species." +
                "The resulting species was null. Contact support at support@bionetgen.com" );
                return;
            }
            
            getTheGUI().getSpeciesPalette().addSpecies( copy );
            
            // does it need to be a copy anymore??
            //removeSpecies(s);
            //the_gui.getSpeciesPalette().addSpecies(copy);
            
            
            // Clean up the Editor Panel
            /*
            for ( int i = 0; i < s.getComponents().size(); i++ ) {
                components.remove( s.getComponents().get(i) );
            }
            
            for ( int i = 0; i < s.getContainers().size(); i++ ) {
                containers.remove( s.getContainers().get(i) );
            }
            
            for ( int i = 0; i < s.getEdges().size(); i++ ) {
                edges.remove( s.getEdges().get(i) );
            }
             */
            
            //the_model.processRules();
            
            
            clearSelections();
            //start_selected = false;
            //start_link = null;
        }
        else if ( e.getActionCommand().equals("Lock Species") ) {
            Species s = null;
            float concentration;
            
            // Get concentration from user
            Float initial_conc_value = null;
            boolean initial_conc_set = false;
            
            String initial_conc = null;
            
            while ( initial_conc_set == false ) {
                initial_conc = (String)displayInputQuestion( "New Species", "Enter the initial concentration for this species.");
                
                if ( initial_conc == null ) {
                    return;
                }
                
                try {
                    initial_conc_value = new Float( initial_conc );
                    initial_conc_set = true;
                    
                    if ( initial_conc_value.floatValue() < 0.0 ) {
                        displayError("Error Setting Intitial Concentration",
                        "The initial concentration must be greater than 0.0;\n" +
                        initial_conc_value + " is not in that range.");
                        initial_conc_set = false;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    displayError( "Error Setting Intitial Concentration",initial_conc + " is not a valid number" );
                    initial_conc_set = false;
                }
            }
            
            concentration = initial_conc_value.floatValue();
            
            if ( selected_component != null ) {
                
                // Make the species using the selected component as the starting point
                s = makeSpecies( selected_component, initial_conc );
                
            }
            else if ( selected_container != null ) {
                if ( selected_container.getComponents().size() == 0 ) {
                    displayError( "Species Creation Error","Species must contain at least one component");
                }
                else {
                    // Make the species using the first component in the container
                    // as the starting point
                    
                    // Make the species using the selected component as the starting point
                    BioComponent biocomp = (BioComponent)selected_container.getComponents().get(0);
                    if (debug_statements) System.out.println( biocomp );
                    
                    s = makeSpecies( biocomp, initial_conc );
                    
                }
            }
            
            // Error check = if null then do not add the species - its makeSpecies
            // job to handle the error and inform the user
            if ( s == null ) {
                //displayError("Species Creation Error","Species could not be created because of an internal error.\nContact support at support@bionetgen.com");
                return;
            }
            
            if (debug_statements) System.out.println("Making Species");
            
            //the_model.addSpecies(s);
            
            Species copy = null;
            
            
            try {
                copy = (Species)WidgetCloner.clone(s);
            }
            catch ( Exception exp ) {
                displayError( "Species Clone Error", "There was an exception while cloning the species." +
                "The exception was: " + exp.getMessage() +"\nContact support at support@bionetgen.com" );
                return;
            }
            
            if ( copy == null ) {
                displayError( "Species Clone Error", "There was an error while cloning the species." +
                "The resulting species was null. Contact support at support@bionetgen.com" );
                return;
            }
            
            //getTheGUI().getSpeciesPalette().addSpecies( copy );
            
            
            addSpecies( s);
            
            
            // Clean up the Editor Panel
            for ( int i = 0; i < s.getComponents().size(); i++ ) {
                components.remove( s.getComponents().get(i) );
            }
            
            for ( int i = 0; i < s.getContainers().size(); i++ ) {
                containers.remove( s.getContainers().get(i) );
            }
            
            for ( int i = 0; i < s.getEdges().size(); i++ ) {
                edges.remove( s.getEdges().get(i) );
            }
            
            //the_model.processRules();
            
            
            clearSelections();
            //start_selected = false;
            //start_link = null;
        }
        else if ( e.getActionCommand().equals("Rename") ) {
            // Display dialog box
            Widget to_be_renamed;
            
            if ( selected_container != null ) {
                to_be_renamed = selected_container;
            }
            else {
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
            
            
            if ( new_label != null ) {
                to_be_renamed.setLabel( new_label );
            }
            else {
                if (debug_statements) System.out.println( "Rename cancelled by user after editing the input text box" );
            }
            
            repaint();
        }
        else if ( e.getActionCommand().equals("Delete") ) 
        {
            Widget w = getSelectedWidget();
            if ( w == null ) 
            {
                //displayError("Delete","Nothing Selected.");
                return;
            }
            
            if ( w instanceof BioComponent )
            {
                if ( !((BioComponent)w).isMovableByUser() ) return;
            }
            

                RemoveEdit edit = new RemoveEdit( w, w.getX(), w.getY() );
                getTheGUI().getEditsManager().addEdit( edit );
            
            removeSelectedWidget();
            clearSelections();
            repaint();
        }
        // From context menu change state submenu
        //else if ( e.getActionCommand().equals("Phosphorylated") )
        //    {
        //	selected_component.setState( "P" );
        //    }
        //else if ( e.getActionCommand().equals("UnPhosphorylated") )
        //    {
        //	selected_component.setState( "UP" );
        //    }
        //else if ( e.getActionCommand().equals("Unspecified") )
        //    {
        //	selected_component.setState( "?" );
        //    }
        // Unknown action: display an error and quit
        
        // This actionPerformed call might be part of a calling chain so dont error
        // out here:
        //else {
        //    displayError("Unknown Action","The action \"" + e.getActionCommand() + "\" is unknown.\nContact support at support@bionetgen.com");
        //    return;
        //}
        
        
        // Action completed. Undo selection.
        
        //clearSelections();
    }
    
    
    synchronized void clearSelections() {
        if (debug_statements) System.out.println("Clearing Selections");
        
        if ( selected_component != null ) {
            if (debug_statements) System.out.println("Selected Component cleared");
            selected_component.setSelected(false);
            selected_component = null;
        }
        if ( selected_container != null ) {
            selected_container.setSelected(false);
            selected_container = null;
        }
        if ( selected_edge != null ) {
            selected_edge.setSelected(false);
            selected_edge = null;
        }
        if ( selected_operator != null ) {
            selected_operator.setSelected(false);
            selected_operator = null;
        }
        if ( selected_species != null ) {
            selected_species.setSelected(false);
            selected_species = null;
        }
        if ( selected_reactionrule != null ) {
            selected_reactionrule.setSelected(false);
            selected_reactionrule = null;
        }
        if ( selected_pattern != null ) {
            selected_pattern.setSelected(false);
            selected_pattern = null;
        }
        if ( selected_group != null ) {
            selected_group.setSelected(false);
            selected_group = null;
        }
        if ( selected_label != null ) {
            selected_label.setSelected(false);
            selected_label.setEditable(false);
            selected_label = null;
        }
        
        
        //if ( start_component != null )
        //    {
        //        start_component.setSelected( false);
        //        start_component = null;
        //    }
        
        
        container_added = false;
        component_added = false;
        move_selected_reactionrule = false;
        move_selected_species = false;
        move_selected_component = false;
        move_selected_container = false;
        move_selected_pattern = false;
        move_selected_group = false;
        resize_selected_container = false;
        //the_selection_box.inUse(false);
        move_the_selection_box = false;
        
        repaint();
    }
    
    public void addWidgetToLocation( Widget widget, int x, int y )
    {
        addWidgetToLocation( widget, x, y, false );
    }
    
    synchronized public void addWidgetToLocation( Widget widget, int x, int y, boolean center ) 
    {
        
        if (debug_statements) System.out.println("addWidgetToLocation run");
        if (debug_statements) System.out.println("++x and y in addWidgetToLocation(): " + x + "," + y);
        //clearSelections();
        
        
            
        if ( widget instanceof BioComponent ) 
        {
            BioComponent bc = (BioComponent)widget;
            if ( center == true )
            {
                    x -= bc.getWidth()/2;
                    y -= bc.getHeight()/2;
            }
            addComponentToLocation( bc, x, y );    
        }
        else if ( widget instanceof BioContainer ) 
        {
            BioContainer bc = (BioContainer)widget;
            if ( center == true )
            {
                    x -= bc.getWidth()/2;
                    y -= bc.getHeight()/2;
            }
            addContainerToLocation( bc, x, y );
        }
        else if ( widget instanceof Operator ) 
        {
            Operator o = (Operator)widget;
            if ( center == true )
            {
                    x -= o.getWidth()/2;
                    y -= o.getHeight()/2;
            }
            addOperatorToLocation( o, x, y );
        }
        else if ( widget instanceof Species ) 
        {
            Species s = (Species)widget;
            if ( center == true )
            {
                    x -= s.getWidth()/2;
                    y -= s.getHeight()/2;
            }
            addSpeciesToLocation( s, x, y );
            
        }
        else if ( widget instanceof Pattern ) 
        {
            Pattern s = (Pattern)widget;
            if ( center == true )
            {
                    x -= s.getWidth()/2;
                    y -= s.getHeight()/2;
            }
            addPatternToLocation( s, x, y );
        }
        else if ( widget instanceof ReactionRule ) 
        {
            ReactionRule rr = (ReactionRule)widget;
            if ( center == true )
            {
                    x -= rr.getWidth()/2;
                    y -= rr.getHeight()/2;
            }
            addReactionRuleToLocation( rr, x, y );
        }
        else if ( widget instanceof Group ) 
        {
            Group g = (Group)widget;
            if ( center == true )
            {
                    x -= g.getWidth()/2;
                    y -= g.getHeight()/2;
            }
            addGroupToLocation( g, x, y );
        }
        else if ( widget instanceof SelectionBox ) 
        {
            SelectionBox sb = (SelectionBox)widget;
            if ( center == true )
            {
                    x -= sb.getWidth()/2;
                    y -= sb.getHeight()/2;
            }
            setTheSelectionBoxToLocation( sb, x, y );
            //setTheSelectionBoxToLocation( (SelectionBox)widget, x, y );
        }
        else 
        {
            displayError("Add Widget to Location Error", "Widgets of type \"" + widget.getClass().getName() + "\" are unknown to WidgetPanel:addWidgetToLocation().\n" +
            "Contact support at support@bionetgen.com.");
        }
        
        repaint();
        
        if (debug_statements) System.out.println( "Number of containers in this panel: " + containers.size() );
        if (debug_statements) System.out.println( "Number of free components in this panel: " + components.size() );
    }
    
    synchronized public void addWidget( Widget widget ) 
    {
        
        if (debug_statements) System.out.println("WidgetPanel:addWidget run");
        //clearSelections();
        
        if ( widget instanceof BioComponent ) 
        {
            BioComponent bc = (BioComponent)widget;
            
            addComponent( bc );    
        }
        else if ( widget instanceof BioContainer ) 
        {
            BioContainer bc = (BioContainer)widget;
            
            addContainer( bc );
        }
        else if ( widget instanceof Operator ) 
        {
            Operator o = (Operator)widget;
            
            addOperator( o );
        }
        else if ( widget instanceof Species ) 
        {
            Species s = (Species)widget;
            
            addSpecies( s );
            
        }
        else if ( widget instanceof Pattern ) 
        {
            Pattern s = (Pattern)widget;
            
            addPattern( s );
        }
        else if ( widget instanceof ReactionRule ) 
        {
            ReactionRule rr = (ReactionRule)widget;
            
            addReactionRule( rr );
        }
        else if ( widget instanceof Group ) 
        {
            Group g = (Group)widget;
            
            addGroup( g );
        }
        else if ( widget instanceof SelectionBox ) 
        {
            SelectionBox sb = (SelectionBox)widget;
            
            setTheSelectionBox( sb );
            //setTheSelectionBoxToLocation( (SelectionBox)widget, x, y );
        }
       
        repaint();
        
        if (debug_statements) System.out.println( "Number of containers in this panel: " + containers.size() );
        if (debug_statements) System.out.println( "Number of free components in this panel: " + components.size() );
    }
    
    boolean linkComponents( Connectable start, Connectable end ) {
        // Adjacent components manage edges
        
        // Don't allow components to be linked to themselves
        if ( start == end ) {
            return false;
        } 
        
        // The edge constructor will add e to start and end components if possible
        Edge e = new Edge(start,end, this);
        
        // Check to see if edge was added to start and end, if so then add edge to the
        // edge vector in WidgetPanel
        if ( e.getStart() == start && e.getEnd() == end ) 
        {
            addEdge( e );
            getTheGUI().getEditsManager().addEdit( new AddEdit(e, e.getX(), e.getY() ) );
            //AddEdit edit = new AddEdit( e, e );
            //getTheGUI().getEditsManager().addEdit( edit );
                    
        }
        else {
            if (debug_statements) System.out.println( "Adding edge to one of the components failed\n" +
            "so not adding edge to WidgetPanel");
            return false;
        }
        
        
        
        
        start_selected = false;
        repaint();
        return true;
    }
    
    // Maps two components using an AtomMap
    boolean map( Mappable start, Mappable end ) 
    {
        if (debug_statements) System.out.println("Map() called");
        
        // Adjacent components manage edges
        
        // Don't allow components to be linked to themselves
        if ( start == end ) {
            return false;
        }
        
        if ( start.getClass() != end.getClass() )
        {
            displayError("Map Error","Attempt to map two widgets of different classes");
            return false;
        }
        
        // The edge constructor will add e to start and end components if possible
        AtomMap e = new AtomMap(start,end, this);
        
        // Check to see if edge was added to start and end, if so then add edge to the
        // edge vector in WidgetPanel
        if ( e.getStart() == start && e.getEnd() == end ) 
        {
            addEdge( e );
            
            //AddEdit edit = new AddEdit( e, e );
            //getTheGUI().getEditsManager().addEdit( edit );
                    
        }
        else {
            if (debug_statements) System.out.println( "Adding AtomMap to one of the components failed\n" +
            "so not adding map to WidgetPanel");
            return false;
        }
        
        start_selected = false;
        repaint();
        return true;
    }
   
    void displayContainerOptionsMenu( int mouse_x, int mouse_y ) {
        
        JMenuItem menu_rename = new JMenuItem( "Rename" );
        JMenuItem menu_delete = new JMenuItem( "Delete" );
        JMenuItem menu_make_species = new JMenuItem( "Make into Species" );
        JMenuItem menu_make_observable = new JMenuItem( "Make into Observable" );
        JMenuItem menu_make_molecule_type = new JMenuItem( "Create Molecule Type" );
        JMenuItem menu_make_molecule = new JMenuItem( "Create Molecule" );
        JMenuItem menu_lock_species = new JMenuItem( "Lock Species" );
        
        // Context menu for changing attributes of the selected component
        JPopupMenu popup = new JPopupMenu();
        popup.add("Options");
        popup.addSeparator();
        popup.add(menu_rename);
        popup.add(menu_delete);
        popup.add(menu_make_molecule_type);
        //popup.add(menu_make_molecule);
        
        
        boolean vs = selected_container.isPartOfValidSpecies();
        boolean vp = selected_container.isPartOfValidPattern();
        
        if ( vs || vp )
        {
            popup.addSeparator();    
            
            if ( vs ) popup.add( menu_make_species );
            //if ( vp ) popup.add( menu_make_observable );
        }
        
        menu_rename.addActionListener( this );
        menu_delete.addActionListener( this );
        //menu_make_molecule.addActionListener( this );
        menu_make_molecule_type.addActionListener( this );
        menu_make_species.addActionListener( this );
        menu_make_observable.addActionListener( this );
        menu_lock_species.addActionListener(this);
        
        popup.show( this, mouse_x, mouse_y );
    }
    
    /*
    void displayOperatorOptionsMenu( int mouse_x, int mouse_y ) {
        JMenuItem menu_set_forward_rate = new JMenuItem( "Set Forward Rate" );
        JMenuItem menu_set_reverse_rate = new JMenuItem( "Set Reverse Rate" );
        JMenuItem menu_delete = new JMenuItem( "Delete" );
        
        menu_set_reverse_rate.addActionListener( this );
        menu_set_forward_rate.addActionListener( this );
        menu_delete.addActionListener( this );
        
        // Context menu for changing attributes of the selected component
        JPopupMenu popup = new JPopupMenu();
        popup.add("Options");
        popup.addSeparator();
        
        if ( selected_operator.getLabel().equals( "forward" ) ) {
            popup.add(menu_set_forward_rate);
        }
        else if ( selected_operator.getLabel().equals( "forward_and_reverse" ) ) {
            popup.add(menu_set_forward_rate);
            
            popup.add(menu_set_reverse_rate);
        }
        
        popup.add(menu_delete);
        
        popup.show( this, mouse_x, mouse_y );
    }
     */
    
    Vector<Species> findAllSpecies( Vector components ) {
        // Finds all species starting from a list of components
        
        if (debug_statements) System.out.println("Number of components: " + components.size() );
        
        for ( int i = 0; i < components.size(); i++ ) {
            BioComponent c = (BioComponent)components.get(i);
            if ( c.getBioGraph() == null ) {
                Species s = makeSpecies(c, "0.0" );
                if (s != null) {
                    species.add(s);
                }
            }
            else {
                if (debug_statements) System.out.println("Non-null species in Component");
            }
        }
        if (debug_statements) System.out.println("Found " + species.size() + " species");
        
        return species;
        
    }
    
    // If the user provides a vector as the third argument makeSpecies will populate the vector with 
    // all the components, edges, and containers that were used to make the species. This can 
    // be useful information for avoiding duplicate species membership when creating multiple
    // species at once.
    Species makeSpecies( BioComponent start, String concentration )
    {
        return makeSpecies( start, concentration, new Vector() );
    }
    
    Species makeSpecies( BioComponent start, String concentration, Vector<Widget> used ) 
    {
        if (debug_statements) System.out.println("Building Species Object");
        Species s = new Species( "temp_species_name", 0, 0, this );
        
        if ( findSpecies( start, s ) == false ) {
            
            // Check that there are no components that are uncontained in the species
            //for ( int i = 0; i < s.getComponents().size(); i++ )
            //    {
            //	if ( ((BioComponent) s.getComponents().get(i)).getContainer() == null )
            //	    {
            displayError("Species Creation Error", "All components in the species must be contained");
            
            // remove the offending species
            //species = null;
            Iterator<BioComponent> comp_itr = s.getComponents().iterator();
            while ( comp_itr.hasNext() ) 
            {
                BioComponent current = comp_itr.next();
                current.setBioGraph(null);
            }
            
            for ( int j = 0; j < s.getContainers().size(); j++ ) 
            {
                ((BioContainer) s.getContainers().get(j)).setBioGraph(null);
            }
            
            return null;
        }
        
        // Turn the BioContainers in the new Species into Molecules
        //Iterator cont_itr = s.getContainers().iterator();
        //Vector molecules = new Vector();
        //Vector remove_list = new Vector();
        //Vector remember_edges = new Vector();
        
        //while ( cont_itr.hasNext() )
        //{
        //    BioContainer current = (BioContainer)cont_itr.next();
        //    Molecule m = current.createMolecule();
        //    molecules.add( m );
        //    remove_list.add( current );
        //    remember_edges.add( current.getEdges() );
        //}
        
        // Remove the old containers from this panel
        //Iterator rem_itr = remove_list.iterator();
        //while ( rem_itr.hasNext() )
        //{
        //    removeContainer( (BioContainer)rem_itr.next() );
        //}
        
        
        // Replace the old containers with the molecule versions
        //s.setContainers( molecules );
        
        
        if ( !s.validate() ) 
        {
            repaint();
            return null;
        }
        
        if (debug_statements) System.out.println("Species created successfully.");
        
        s.setLabel( getNextBioGraphLabel() );
        
        s.setConcentration( concentration );
                
        Species s_copy = null;
        
        // Populate the used vector with the widgets the species was made from before the copy
        used.addAll( s.getContainers() );
        used.addAll( s.getComponents() );
        used.addAll( s.getEdges() );
        
        try
        {
            s_copy = (Species)WidgetCloner.clone(s);
        
            // remove the species links from the original widgets still in the Drawing Board
            
            Iterator<BioComponent> comp_itr = s.getComponents().iterator();
            while ( comp_itr.hasNext() ) 
            {
                BioComponent current = comp_itr.next();
                current.setBioGraph(null);
                current.setConnectedByContainer( false );
                current.setConnectedByEdge( false );

            }
            
            Iterator<BioContainer> cont_itr = s.getContainers().iterator();
            while ( cont_itr.hasNext() ) 
            {
                BioContainer current = cont_itr.next();
                current.setBioGraph(null);
            }
            
            
            
        // reset the components and containers used to create the species
        // This method needs to be rewritten from scratch - has been 
        // rewritten in place too many times for different purposes.  
            
            /*
            for ( int j = 0; j < s.getComponents().size(); j++ ) 
            {
                ((BioComponent) s.getComponents().get(j)).setBioGraph(null);
            }
            
            for ( int j = 0; j < s.getContainers().size(); j++ ) 
            {
                ((BioContainer) s.getContainers().get(j)).setBioGraph(null);
            }
             */
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        
        return s_copy;
    }
    
    boolean findSpecies(BioComponent component, Species s) {
        //1) add to the current species
        
        if ( component.getContainer() == null ) {
            return false;
        }
        
        component.setBioGraph(s);
        
        //2) send connected components to findSpecies
        // Find components linked through edges
        for ( int i = 0; i < component.getEdges().size(); i++ ) {
            //s.addEdge( ((Edge)component.getEdges().get(i)) );
            //edges.remove( (Edge)component.getEdges().get(i) );
            
            BioComponent next_component = (BioComponent)((Edge)component.getEdges().get(i)).getOtherEnd( component );
            if ( next_component != null ) {
                
                // Follow edges to find other components in the species
                if ( !next_component.isConnectedByEdge() ) 
                {
                    next_component.setConnectedByEdge( true );
                    if ( findSpecies( next_component, s ) == false ) {
                        return false;
                    }
                }
            }
            else {
                displayError("Error Creating Species","Null component at the end of an edge.");
                return false;
            }
        }
        
        // Now find components linked through containers
        if ( component.getContainer() != null ) {
            s.addContainer( component.getContainer() );
            //containers.remove( component.getContainer() );
            
            Vector contents = component.getContainer().getComponents();
            for ( int i = 0; i < contents.size(); i++ ) {
                BioComponent next_component = (BioComponent)contents.get(i);
                           
                if ( !next_component.isConnectedByContainer() ) 
                {
                    next_component.setConnectedByContainer( true );
                    findSpecies( next_component, s );
                }
            }
        }
        
        //components.remove(component);
        return true;
        
    }
    
    boolean findBioGraph(BioComponent component, BioGraph s) 
    {    
        if ( component.getContainer() == null ) return false;
        component.setBioGraph(s);
        
        Iterator edge_itr = component.getEdges().iterator();
        while ( edge_itr.hasNext() ) 
        {
            BioComponent next_component = (BioComponent)((Edge)edge_itr.next()).getOtherEnd( component );
            
            if ( next_component.getBioGraph() != s ) 
                if ( findBioGraph( next_component, s ) == false ) return false;
        }
        
        if ( component.getContainer() != null ) 
        {
            s.addContainer( component.getContainer() );
            
            Iterator comp_itr = component.getContainer().getComponents().iterator();
            while ( comp_itr.hasNext() )
            {
                BioComponent next_component = (BioComponent)comp_itr.next();
                
                if ( next_component.getBioGraph() != s ) 
                    if ( findBioGraph( next_component, s ) == false ) return false;
            }
        }
        
        return true;
        
    }
    
    
    void displayError( String error_title, String error_message ) {
        JOptionPane.showMessageDialog(this,
        error_message,
        error_title,
        JOptionPane.ERROR_MESSAGE);
        
    }
    
    void displayWarning( String warning_title, String warning_message ) {
        JOptionPane.showMessageDialog(this,
        warning_message,
        warning_title,
        JOptionPane.WARNING_MESSAGE);
        
    }
    
    void displayInformation( String info_title, String info_message ) {
        JOptionPane.showMessageDialog(this,
        info_message,
        info_title,
        JOptionPane.INFORMATION_MESSAGE);
        
    }
    
    public boolean displayQuestion( String q_title, String q_message ) {
        int result = displayQuestion( q_title, q_message, false );
        
        if ( result == JOptionPane.YES_OPTION ) {
            return true;
        }
        
        return false;
    }
    
    public boolean displayConfirm( String q_message ) {
        int result = JOptionPane.showConfirmDialog( this, q_message );
        
        if ( result == JOptionPane.YES_OPTION ) {
            return true;
        }
        
        return false;
    }
    
    public int displayQuestion( String q_title, String q_message, boolean allow_cancel ) 
    {
        int format = 0;
        if ( allow_cancel ) {
            format = JOptionPane.YES_NO_CANCEL_OPTION;
        }
        else {
            format = JOptionPane.YES_NO_OPTION;
        }
        
    
        return JOptionPane.showConfirmDialog(
            this,
            q_message,
            q_title,
            format);
        
    }
    
    public String displayInputQuestion( String title, String question, boolean allow_cancel ) {
        int format = 0;
        if ( allow_cancel ) format = JOptionPane.YES_NO_CANCEL_OPTION; 
        else format = JOptionPane.YES_NO_OPTION;
        return displayInputQuestion( title, question, format, null, null );
    }
    
    public String displayInputQuestion( String title, String question, Object[] options ) {
        return displayInputQuestion( title, question, JOptionPane.QUESTION_MESSAGE, options, null );
    }
    
    public String displayInputQuestion( String title, String question, String[] options ) {
        return displayInputQuestion( title, question, JOptionPane.QUESTION_MESSAGE, options, null );
    }
    
    public String displayInputQuestion( String title, String question ) {
        return displayInputQuestion( title, question, JOptionPane.QUESTION_MESSAGE, null, null );
    }
    
    public String displayInputQuestion( String title, String question, int format, Object[] possible_values, String default_value )
        {
            String[] possible_vals = new String[possible_values.length];
            for ( int i = 0; i < possible_values.length; i++ )
            {
                possible_vals[i] = (String)possible_values[i];
            }
            
          return displayInputQuestion( title, question, format, possible_vals, default_value );
        }
    
    public String displayInputQuestion( String title, String question, int format, String[] possible_values, String default_value ) {
        
        //Object[] possibilities = {"phosphorylated", "unphosphorylated", "unspecified"};
        String s = (String)JOptionPane.showInputDialog(
        this,
        question,
        title,
        format,
        null,
        possible_values, // possibilities
        default_value); // default
        
        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            return s;
        }
        
        //If you're here, the return value was null/empty.
        return null;
        
    }
    
    public int displayConfirm( String title, String question, String[] possible_values, String default_value ) {
        
        //Object[] possibilities = {"phosphorylated", "unphosphorylated", "unspecified"};
        int s = JOptionPane.showOptionDialog(
        this,
        question,
        title,
        JOptionPane.YES_NO_CANCEL_OPTION,
        JOptionPane.WARNING_MESSAGE,
        null,
        possible_values, // possibilities
        default_value); // default
        

            return s;
                
    }
    
    Vector<Species> getAllSpecies() {
        return species;
    }
    
    Vector<FlickrLabel> getAllFlickrLabels() {
        return flickr_labels;
    }
    
    Vector<Pattern> getAllPatterns() {
        return patterns;
    }
    
    Vector<ReactionRule> getAllReactionRules() {
        return rules;
    }
    
    Vector<Reaction> getAllReactions() {
        return reactions;
    }
    
    Vector<BioComponent> getAllComponents() {
        return components;
    }
    
    public void setAllContainers( Vector<BioContainer> v ) {
        containers = v;
    }
    
    public Vector<BioContainer> getAllContainers() {
        return containers;
    }
    
    Vector<Edge> getAllEdges() {
        return edges;
    }
    
    Vector<Operator> getAllOperators() {
        return operators;
    }
    
    Vector<Group> getAllGroups() {
        return groups;
    }
    
    private Widget getCopiedWidget()
    {
        return getTheGUI().getCopiedWidget();
    }
    
    Vector<Widget> getAllWidgets() 
    {
        Vector<Widget> widgets = new Vector<Widget>();
        widgets.addAll( getAllSpecies() );
        widgets.addAll( getAllContainers() );
        widgets.addAll( getAllComponents() );
        widgets.addAll( getAllOperators() );
        widgets.addAll( getAllPatterns() );
        widgets.addAll( getAllReactionRules() );
        widgets.addAll( getAllGroups() );
        widgets.addAll( getAllFlickrLabels() );
        widgets.addAll( getAllEdges() );
        return widgets;
    }
    
    synchronized boolean addEdge( Edge edge ) {
        // Make sure the edge is not already in the panel
        if ( edges.indexOf(edge) != -1 ) {
            return false;
        }
        
        edges.add( edge );
        
         // Add attendent label
         if ( edge.getFlickrLabel() != null )
         {
            addFlickrLabel( edge.getFlickrLabel() );
         }
        
        repaint();
        return true;
    }
    
    synchronized boolean addComponent( BioComponent component ) {
        // Make sure the component is not already in the panel
        if ( components.indexOf( component ) != -1 ) {
            return false; 
        }
        
        components.add( component );
        
        // Add attendent label
        addFlickrLabel( component.getFlickrLabel() );
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    synchronized boolean addFlickrLabel( FlickrLabel fl ) 
    {
        if (debug_statements) System.out.println("Added FlickrLabel " + fl.getString() ); 
        if ( flickr_labels.indexOf( fl ) != -1 ) {
            return false;
        } 
     
        //add( fl ); // Add to panel so it can recieve keyboard and mouse events
        
        flickr_labels.add( fl );
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    synchronized boolean addGroup( Group group ) {
        // Make sure the edge is not already in the panel
        if ( groups.indexOf( group ) != -1 ) {
            return false;
        }
        
        groups.add( group );
        this.addFlickrLabel( group.getFlickrLabel() );
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException 
    {
        stream.writeObject( (Vector<Species>)species );
        stream.writeObject( (Vector<BioContainer>)containers );
        stream.writeObject( (Vector<BioComponent>)components );
        stream.writeObject( (Vector<Edge>)edges );
        stream.writeObject( (Vector<Operator>)operators );
        stream.writeObject( (Vector<ReactionRule>)rules );
        stream.writeObject( (Vector<Pattern>)patterns );
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        
        //Read object state from stream
        
        try
        {
        
        this.species = (Vector<Species>)stream.readObject();
        this.containers = (Vector<BioContainer>)stream.readObject();
        this.components = (Vector<BioComponent>)stream.readObject();
        this.edges = (Vector<Edge>)stream.readObject();
        this.operators = (Vector<Operator>)stream.readObject();
        this.rules = (Vector<ReactionRule>)stream.readObject();
        this.patterns = (Vector<Pattern>)stream.readObject();
        
        }
        catch ( ClassCastException cce )
        {
            cce.printStackTrace();
        }
        
        // Do the stuff would have happened in the constructor
        the_selection_box = new SelectionBox(this);
        
        //setDropTarget(new DropTarget(this, widget_transfer_handler.getDropHandler()));
        addMouseMotionListener(mouse_control);
        addMouseListener(mouse_control);
        
        setBackground(Color.white);
    }
    
    void setContainers( Vector<BioContainer> containers ) {
        this.containers = containers;
    }
    
    //void setTheSelectionBox( SelectionBox s ) {
    //    this.the_selection_box = s;
    //}
    
    void setComponents( Vector<BioComponent> components ) {
        this.components = components;
    }
    
    void setEdges( Vector<Edge> edges ) {
        this.edges = edges;
    }
    
    void setSpecies( Vector<Species> species ) {
        this.species = species;
    }
    
    void setPatterns( Vector<Pattern> patterns ) {
        this.patterns = patterns;
    }
    
    void setGroups( Vector<Group> groups ) {
        this.groups = groups;
    }
    
    void setReactions( Vector<Reaction> reactions ) {
        this.reactions = reactions;
    }
    
    void setReactionRules( Vector<ReactionRule> rr ) {
        this.rules = rr;
    }
    
    void setOperators( Vector<Operator> operators ) {
        this.operators = operators;
    }
    
    void initialize() {
        species.removeAllElements();
        edges.removeAllElements();
        components.removeAllElements();
        containers.removeAllElements();
        operators.removeAllElements();
        rules.removeAllElements();
        patterns.removeAllElements();
        groups.removeAllElements();
        flickr_labels.removeAllElements();
        reactions.removeAllElements();
        clearSelections();
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
    }
    
    Edge getSelectedEdge() {
        return selected_edge;
    }
    
    void removeComponent( BioComponent selection ) {
        
        // Remove from the panel
        getAllComponents().remove( selection );
        //remove( selection.getPanel() );
        
        // Remove from containers
        if ( selection.getContainer() != null ) {
            selection.getContainer().removeComponent( selection );
        }
        
        // Remove from species
        if ( selection.getBioGraph() != null ) {
            selection.getBioGraph().removeComponent( selection );
        }
        
        // Remove components edges from panel and from adjacent nodes
        if (debug_statements) System.out.println("*** Will delete " + edges.size() + "edges" );
        Iterator edge_itr = selection.getEdges().iterator();
        while( edge_itr.hasNext() ) 
        {
            Edge e = (Edge)edge_itr.next();
            
            
            if ( e.getStart() == selection ) {
                e.getEnd().getEdges().remove( e );
            }
            else {
                e.getStart().getEdges().remove( e );
            }
            
            
            if (debug_statements) System.out.println("*** Deleting edge " + e.getID() );
            
            edges.remove( e ); 
             
            // Remove attendent label
            removeFlickrLabel( e.getFlickrLabel() );
        }
        
        // Erase attendant AtomMaps
        AtomMap map = selection.getAtomMap();
        if ( map != null )
        {
            BioComponent other_end = (BioComponent)map.getOtherEnd( selection );
            other_end.setAtomMap( null );
            edges.remove( map );
            removeFlickrLabel( map.getFlickrLabel() );
            selection.setAtomMap( null );
        }
        
        // remove flickrlabel
        removeFlickrLabel( selection.getFlickrLabel() );
        
        if ( selected_component == selection ) selected_component = null;
    }
    
    
    
    public void removeContainerOnly( BioContainer c ) 
    {
        // Remove from the panel
        getAllContainers().remove( c );
        removeFlickrLabel( c.getFlickrLabel() );
        
        // Remove all the maps from the Panel
        Iterator map_itr = c.getAtomMaps().iterator();
        while( map_itr.hasNext() ) 
        {
            // Maps and Edges are both stored in the edges vector
            AtomMap map = (AtomMap)map_itr.next();
            removeFlickrLabel( map.getFlickrLabel() );
            edges.remove( map );
        }
        
        if ( c == selected_container ) selected_container = null;
    }
    
    void removeContainer( BioContainer selection ) {
        
        // Remove from the panel
        getAllContainers().remove( selection );
        removeFlickrLabel( selection.getFlickrLabel() );
        
        // Remove all the edges from the Panel
        Iterator edge_itr = selection.getEdges().iterator();
        while( edge_itr.hasNext() ) 
        {
            Edge edge = (Edge)edge_itr.next();
            removeFlickrLabel( edge.getFlickrLabel() );
            edges.remove( edge );
        }
        
        // Remove all the maps from the Panel
        Iterator map_itr = selection.getAtomMaps().iterator();
        while( map_itr.hasNext() ) 
        {
            // Maps and Edges are both stored in the edges vector
            AtomMap map = (AtomMap)map_itr.next();
            removeFlickrLabel( map.getFlickrLabel() );
            edges.remove( map );
        }
        
        // Remove all the components from the Panel
        Iterator comp_itr = selection.getComponents().iterator();
        while( comp_itr.hasNext() ) 
        {
            BioComponent comp = (BioComponent)comp_itr.next();
            components.remove( comp );
            
             // Remove attendent label
            removeFlickrLabel( comp.getFlickrLabel() );
        }
        
        // Remove all the edges so there wont be any external components connected to this container
        // This effects the internal structure of the container being removed so 
        // make a copy of the container before removing if it is going to be used again.
        selection.removeAllEdges();
        
        // Remove all the Components from the panel
        
        // Remove from species
        //if ( selection.getSpecies() != null )
        //   {
        //!!!!!
        //selection.removeContainer( selection );
        //  }
        
        if ( selected_container == selection ) selected_container = null;
    }
    
    void removeEdge( Edge edge ) {
        edge.disconnectOtherEnd( edge.getStart() );
        edge.disconnectOtherEnd( edge.getEnd() );
        edges.remove( edge );
        removeFlickrLabel( edge.getFlickrLabel() );
        if ( selected_edge == edge ) selected_edge = null; 
    }
     
    void removeOperator( Operator o ) {
        operators.remove( o );
        this.removeFlickrLabel( o.getFlickrLabel() );
        if ( selected_operator == o ) selected_operator = null;
    }
    
    void removeFlickrLabel( FlickrLabel fl ) {
        if (debug_statements) System.out.println("Removing FlickrLabel: " + fl.getString() );
        flickr_labels.remove( fl );
        
        if (debug_statements) System.out.println("List of FlickrLabels in the Panel:");
        Iterator<FlickrLabel> fl_itr = flickr_labels.iterator();
        while ( fl_itr.hasNext() )
        {
            if (debug_statements) System.out.println("  " + fl_itr.next().getString() );
        }
        if (debug_statements) System.out.println("end FlickrLabels");
        
        if ( selected_label == fl ) selected_label = null;
    }
    
    void removeGroup( Group g ) 
    {
        groups.remove( g );
        this.removeFlickrLabel( g.getFlickrLabel() );
        if ( selected_group == g ) selected_group = null;
    }
    
    void removeSpecies( Species s ) {
        Iterator i = s.getEdges().iterator();
        
        // Have to remove edges explicitly since there is a local copy of the edge
        // in the widget panel
        while( i.hasNext() ) {
            edges.remove( i.next() );
        }
        
        species.remove( s );
        this.removeFlickrLabel( s.getFlickrLabel() );
        if ( selected_species == s ) selected_species = null;
        
    }
    
    void removePattern( Pattern p ) {
        Iterator i = p.getEdges().iterator();
        
        // Have to remove edges explicitly since there is a local copy of the edge
        // in the widget panel
        while( i.hasNext() ) {
            edges.remove( i.next() );
        }
        
        patterns.remove( p );
        this.removeFlickrLabel( p.getFlickrLabel() );
        
        if ( selected_pattern == p ) selected_pattern = null;
    }
    
    void removeReactionRule( ReactionRule r ) 
    {
        rules.remove( r );
        this.removeFlickrLabel( r.getFlickrLabel() );
        if ( selected_reactionrule == r ) selected_reactionrule = null;

    }
    
    synchronized void addSelectedOperator( Operator o ) {
        if (selected_operator != null ) {
            selected_operator.setSelected( false );
        }
        
        
        selected_operator = o;
        selected_operator.setSelected( true );
        addOperator(o);
    }
    
    synchronized void addSelectedComponent( BioComponent bc ) {
        if ( selected_component != null) {
            selected_component.setSelected(false);
        }
        
        selected_component = bc;
        selected_component.setSelected( true );
        addComponent(bc);
        //components.add(bc);
    }
    
    synchronized void addSelectedGroup( Group g ) {
        if ( selected_group != null) {
            selected_group.setSelected(false);
        }
        
        selected_group = g;
        selected_group.setSelected( true );
        addGroup(g);
        //components.add(bc);
    }
    
    synchronized void addSelectedContainer( BioContainer bc ) {
        // Deselect previously selected container
        if ( selected_container != null) {
            selected_container.setSelected(false);
        }
        
        selected_container = bc;
        selected_container.setSelected( true );
        addContainer(bc);
    }
    
    synchronized void addSelectedReactionRule( ReactionRule rule ) {
        // Deselect previously selected rule
        if ( selected_reactionrule != null) {
            selected_reactionrule.setSelected(false);
        }
        
        selected_reactionrule = rule;
        selected_reactionrule.setSelected( true );
        addReactionRule( rule );
    }
    
    synchronized void addSelectedSpecies( Species species ) {
        // Deselect previously selected rule
        if ( selected_species != null) {
            selected_species.setSelected(false);
        }
        
        selected_species = species;
        selected_species.setSelected( true );
        addSpecies( species );
    }
    
    synchronized void addSelectedPattern( Pattern pattern ) 
    {
        // Deselect previously selected pattern
        if ( selected_pattern != null) {
            selected_pattern.setSelected(false);
        }
        
        selected_pattern = pattern;
        selected_pattern.setSelected( true );
        addPattern( pattern );
    }
    
    synchronized boolean addOperator(Operator operator) {
        // Make sure the operator is not already in the panel
        if ( operators.indexOf(operator) != -1 ) {
            return false;
        }
       
        if (debug_statements) System.out.println("Added Operator");
        
        operators.add( operator );
        this.addFlickrLabel( operator.getFlickrLabel() );
        
        revalidate();
        repaint();
        
        //setPreferredSize( getZoomedUsedArea() );
        return true;
    }
    
    synchronized boolean addSpecies(Species s) {
        // Make sure the species is not already in the panel
        if ( species.indexOf( s ) != -1 ) {
            return false;
        }
        
        
        this.species.add( s );
        s.relinkParameter(); // To make sure the concentration parameter points to the right place 
        this.addFlickrLabel( s.getFlickrLabel() );
        
        //setPreferredSize( getZoomedUsedArea() );
        // Now we have to add edges to the WidgetPanel explicitly due to the
        // quasi independent nature of edges??????
        revalidate();        
        repaint();
        
        return true;
    }
    
    synchronized boolean addPattern(Pattern s) throws NullPointerException
    {
        // Make sure the species is not already in the panel
        if ( patterns.indexOf( s ) != -1 ) {
            return false;
        }
        
        if ( s == null )
        {
            throw new NullPointerException();
        }
        
        patterns.add( s );
        this.addFlickrLabel( s.getFlickrLabel() );
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        
        return true;
    }
    
    boolean setTheSelectionBoxToLocation(SelectionBox sb, int x, int y ) 
    {
        if (debug_statements) System.out.println("Adding SelectionBox with " + sb.getContents().size() + " widgets." );
        sb.setContainingPanel( this );
        sb.resetOffsets();
        //sb.updateLocation(0,0,true );
        //sb.calculatePointerOffset(sb.getX(),sb.getY());
      
        if ( debug_statements )
        {
            System.out.println("Before move: SB x="+sb.getX() + ", SB y=" + sb.getY() );
        }
        sb.updateLocation(x,y,true);
        //sb.calculatePointerOffset(x,y);
        if ( debug_statements )
        {
            System.out.println("After move: SB x="+sb.getX() + ", SB y=" + sb.getY() );
        }
        
        return setTheSelectionBox( sb );
    }
    
    boolean setTheSelectionBox(SelectionBox sb) 
    {
        if ( debug_statements ) System.out.println("WidgetPanel:setTheSelectionBox() called.");
        
        if ( the_selection_box.isSelected() || the_selection_box.isInUse() )
        {
            the_selection_box.setSelected( false );
            the_selection_box.inUse( false );
        }
        
        the_selection_box = sb;
        
        the_selection_box.inUse( true );
        the_selection_box.setSelected( true );
        //the_selection_box.releaseContents();
        
        // So mouse control knows what to do when the user clicks away from the new selection box (see MouseControl:mouseReleased)
        this.copy_the_selection_box = true;
        this.move_the_selection_box = true;
        
        repaint();
        
        return true;
    }
    
    synchronized boolean addReactionRule(ReactionRule rr) {
        // Make sure the reaction rule is not already in the panel
        if ( rules.indexOf( rr ) != -1 ) {
            return false;
        } 
        
        
        rules.add( rr );
        rr.relinkParameters();
        this.addFlickrLabel( rr.getFlickrLabel() );
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    synchronized boolean addReaction(Reaction r) {
        // Make sure the reaction rule is not already in the panel
        if ( rules.indexOf( r ) != -1 ) {
            return false;
        }
        
        
        reactions.add( r );
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    synchronized boolean addContainer(BioContainer bc) {
        // Make sure the container is not already in the panel
        if ( containers.indexOf( bc ) != -1 ) {
            return false;
        }
        
        if (debug_statements) System.out.println("Remap Components to CDK called in addConainer()");
        bc.reMapComponentsToCDK();
        
        containers.add( bc );
        
         // Add attendent label
        addFlickrLabel( bc.getFlickrLabel() );
        
        // Add the components in the container
        for ( int i = 0; i < bc.getComponents().size(); i++ ) {
            addComponent( (BioComponent) bc.getComponents().get(i) );
        }
        
        // Add the edges in the container
        for ( int i = 0; i < bc.getEdges().size(); i++ ) {
            addEdge( (Edge) bc.getEdges().get(i) );
        }
        
        
        //setPreferredSize( getZoomedUsedArea() );
        revalidate();
        repaint();
        return true;
    }
    
    // Very primative first version.
    ReactionRule makeReactionRule( Vector<Pattern> patterns, Vector<Operator> operators ) {
        //Vector species = findAllSpecies( components );
        
        //if ( species.size() != 3 )
        //{
        //    displayError("Rule Creation Error","Rules consist of three species and\n" +
        //    " two operators. Your rule has " + species.size() + " species and\n" +
        //    operators.size() + " operators");
        //    return null;
        //}
        
        if ( patterns.isEmpty() && operators.isEmpty()) {
            displayError("Error in WidgetPanel::makeReactionRule()","No patterns or operators in argument");
            return null;
        }
        
        if ( patterns.isEmpty() ) {
            displayError("Error in WidgetPanel::makeReactionRule()","No patterns in argument");
            return null;
        }
        
        if ( operators.isEmpty() ) {
            displayError("Error in WidgetPanel::makeReactionRule()","No operators in argument");
            return null;
        }
        
        // Find the arrow operator and make sure there is only one
        
        Operator arrow = null;
        Iterator operator_itr = operators.iterator();
        while ( operator_itr.hasNext() ) {
            Operator current_op = (Operator)operator_itr.next();
            if ( current_op instanceof Forward ) //Forward and Reverse is a subclass of Forward
            {
                if ( arrow != null ) {
                    displayError("Error Creating Reaction Rule","A Reaction Rule may not have more than one arrow operator");
                    return null;
                }
                
                arrow = current_op;
            }
        }
        
        if ( arrow == null ) {
            displayError("Error Creating Reaction Rule","Each Reaction Rule must contain an arrow operator");
            return null;
        }
        
        Vector<Pattern> reactants = new Vector<Pattern>();
        Vector<Pattern> products = new Vector<Pattern>();
        //Pattern result;
        
        // Sort the patterns into reactants and products based on the their position
        // relative to the arrow operator
        Iterator pattern_itr = patterns.iterator();
        while ( pattern_itr.hasNext() ) {
            Pattern current_pattern = (Pattern)pattern_itr.next();
            
            if (debug_statements) System.out.println("Arrow X: " + arrow.getX() );
            if (debug_statements) System.out.println("Pattern X: " + current_pattern.getX() );
            
            if ( current_pattern.getX() > arrow.getX() ) 
            {
                products.add( current_pattern );
            }
            else 
            {
                reactants.add( current_pattern );
            }
        }
        
        /*
        int operand_comp_count = 0;
         
        int i = 0;
        for ( ; i < patterns.size() - 1; i++ )
        {
            operand_comp_count += ((Pattern)patterns.get(i)).getComponents().size();
            operands.add( patterns.get(i) );
        }
         
        result = (Pattern)patterns.get(i);
         
        // Check that there are the same number of components in result as there
        // are in the operands
        if ( result.getComponents().size() != operand_comp_count )
        {
                 displayError("Rule Creation Error","The total number of components in the reactants\n" +
                 "and in the product must be the same. Your rule has " + operand_comp_count + " components in the reactants\n" +
                 " and " + result.getComponents().size() + " in the product.");
                return null;
        }
         */
        
        
        // Build rule object
        ReactionRule r = null;
        boolean reversable = false;
        if ( arrow instanceof ForwardAndReverse ) 
        {
            ForwardAndReverse fra = ((ForwardAndReverse)arrow);
            reversable = true;
            r = new ReactionRule( reactants, operators, products, fra.getForwardRate(), fra.getReverseRate(), reversable, this );
        }
        else if ( arrow instanceof Forward )
        {
            Forward fa = ((Forward)arrow);
            reversable = false;
            r = new ReactionRule( reactants, operators, products, fa.getForwardRate(), "", reversable, this );
        }
        
        IDGenerator idgen = new IDGenerator();
        long id = idgen.getCurrentID()+1; // replace with reaction_rule specific count
        
        r.setLabel("Rule"+id);
        
        //displayInformation("Created a New Rule", "The rule contains " + products.size() + " products and " + reactants.size() + " reactants." );  
        
        
        
        return r;
    }
    
    
    public void setMode(java.lang.String mode) 
    {
       // Since the mode is changing clean up 
        start_component = null;
        start_map = null;
        
        this.mode = mode;
    
        
        
        setCursorAccordingToMode();
    }
    
    public String getMode()
    {
        return mode;
    }
    
    public void dragEnter(DropTargetDragEvent dtde) 
    {
        // This is a hack. There seems to be no way to access the drop target
        // object from the DragSourceHandler to determine which component the 
        // mouse is over. However "Supported Action" messages can be sent from the drop
        // target to the drag handler. I have hijacked these messages 
        // in order to send the drop target type to the drag handler so it can 
        // set the correct icon.
        //if ( this instanceof WidgetPalette )
        //{
        //    dtde.acceptDrag(1);
        //}
        //else
        //{
        //    dtde.acceptDrag(2);
        //}
        
        // I have to use numbers > 0 since 0 blocks the DragSourceHandler from getting
        // the event. There is some bit masking going on I think.
        
    }
    
    public void dragExit(DropTargetEvent dte) {
    }
    
    public void dragOver(DropTargetDragEvent dtde) 
    {
    // This is a hack. There seems to be no way to access the drop target
        // object from the DragSourceHandler to determine which component the 
        // mouse is over. However "Supported Action" messages can be sent from the drop
        // target to the drag handler. I have hijacked these messages 
        // in order to send the drop target type to the drag handler so it can 
        // set the correct icon.
        if ( this instanceof WidgetPalette )
        {
            dtde.acceptDrag(1);
        }
        else
        {
            dtde.acceptDrag(2);
        }
        
      /*
     if (!DragSource.isDragImageSupported())
     {
         
         setCursor( Cursor.getPredefinedCursor(Cursor.HAND_CURSOR ) );
         
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
        
        Image image = icon.getImage();
        
         
         // Erase the last ghost image and cue line
         paintImmediately(drag_ghost_rect.getBounds());    

	int height = icon.getIconHeight();
	int width = icon.getIconWidth();
 

         // Remember where you are about to draw the new ghost image
         drag_ghost_rect.setRect(dtde.getLocation().getX(),dtde.getLocation().getY(), width, height );
 
         // Draw the ghost image
         g2.drawImage(image, 
             AffineTransform.getTranslateInstance(drag_ghost_rect.getX(),
                                                  drag_ghost_rect.getY()), 
                     null);                
     }
          */
    }
    
    public void drop(DropTargetDropEvent dtde) 
    {
        if ( this instanceof WidgetPalette )
        {
            displayError("Drag and Drop Error", "Objects may not be dropped into Palettes in this version.");
            return;
        }
        
        Transferable t = (Transferable)dtde.getTransferable();
        Widget w = new Widget();
        try
        {
            if (debug_statements) System.out.println("Getting Transfered Data from TransferHandler");
            w = (Widget)t.getTransferData( new DataFlavor( w.getClass(), "Widget") );
            w = WidgetCloner.clone(w);
        }
        catch ( UnsupportedFlavorException ude )
        {
            displayError("Error \"Dropping\" Object", "The class " + w.getClass().getName() + " is unsupported.");
            return;
        }
        catch ( Exception e )
        {
            displayError("Error \"Dropping\" Object", "The exception was due to " + e.getMessage() );
            e.printStackTrace();
            return;
        }
        
        Point p = dtde.getLocation();
        
        // Translate into the zoomed coordinate system.
        int x = (int)(p.getX()/getZoom());
        int y = (int)(p.getY()/getZoom());
        
        w.setContainingPanel(this);
        
        if ( w instanceof ReactionRule )
        {
            ((ReactionRule)w).setContainingPanel( this );
        }
            
        if ( w instanceof MoleculeType )
        {
            try
            {
                if (debug_statements) System.out.println("Dropping BioContainer from MoleculeType into WidgetPanel: setting component states to defaults."); 

                BioContainer new_bc = new BioContainer(x,y,((MoleculeType)w),this);
                new_bc.setComponentsToDefault();
                new_bc.reMapComponentsToCDK();
                
                w = new_bc;
                w.setLabel( w.getLabel() );
                //addContainerToLocation( new_bc, x, y );
                //return;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
            }
            
        }
        
        addWidgetToLocation( w, x, y, true );
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde) 
    {
  
    }
    
    /*
    public void addSpeciesToLocation(Species s, int x, int y) {
        if (!( x >= 0 && x < panel_dimension.width
        && y >= 0 && y < panel_dimension.height )) {
            displayError("Drag & Drop Warning", "Species may only be dropped into the BioNetGen Editor Window");
            return;
        }
        
        if ( s == null ) {
            displayError("Internal Error", "Attempt to drop null species into target panel. Contact support at support@bionetgen.com");
            return;
        }
        
        clearSelections();
        
        // It is important that this happenes before any GUI stuff
        s.setContainingPanel(this);
        
        s.setSelected( true );
        selected_species = s;
        s.updateLocation( x-s.getWidth()/2, y-s.getHeight()/2, true );
        addSpecies( s );
     }
     */
    
    /*
    public boolean addContainerToLocation(BioContainer c, int x, int y) {
        if (debug_statements) System.out.println("Called addContainerToLocation()");
        
        if (!( x >= 0 && x < panel_dimension.width
        && y >= 0 && y < panel_dimension.height )) {
            displayError("Drag & Drop Warning", "Molecules, Species, and ReactionRules may only be dropped into the BioNetGen Editor Window");
            return false;
        }
        
        if ( c == null ) {
            displayError("Internal Error", "Attempt to drop null container into target panel. Contact support at support@bionetgen.com");
            return false;
        }
        
        clearSelections();
        
        // It is important that this happenes before any GUI stuff
        c.setContainingPanel(this);
        
        c.setSelected( true );
        
        c.updateLocation( x-c.getWidth()/2, y-c.getHeight()/2, true );
        
        selected_container = c;
        
        addContainer( c );
        if (debug_statements) System.out.println("Container added to "+x+" "+y+" has "+c.getComponents().size()+" components");
        
        return true;
    }
     */
    
    /*
    public void addReactionRuleToLocation(ReactionRule c, int x, int y) {
        if (debug_statements) System.out.println("Called addReactionRuleToLocation()");
        
        if (!( x >= 0 && x < panel_dimension.width
        && y >= 0 && y < panel_dimension.height )) {
            displayError("Drag & Drop Warning", "Molecules, Species, and ReactionRules may only be dropped into the BioNetGen Editor Window");
            return;
        }
        
        if ( c == null ) {
            displayError("Internal Error", "Attempt to drop null ReactionRule into target panel. Contact support at support@bionetgen.com");
            return;
        }
        
        clearSelections();
        
        // It is important that this happenes before any GUI stuff
        c.setContainingPanel(this);
        
        c.setSelected( true );
        
        c.updateLocation( x-c.getWidth()/2, y-c.getHeight()/2, true );
        
        selected_reactionrule = c;
        
        addReactionRule( c );
        if (debug_statements) System.out.println("ReactionRule added to "+x+" "+y+" has "+c.getComponents().size()+" components");
        
    }
     */
    
    public GUI getTheGUI() {
        return the_gui;
    }
    
    public boolean isValidMoleculeType(BioContainer mol) {
        BioContainer match = getTheGUI().getMoleculePalette().getMoleculeType( mol );
        
        if ( match == null ) {
            return false;
        }
        
        return true;
    }
    
    public void refreshAll() {
        getTheGUI().refreshAll();
    }
    
    public boolean removeBioGraph(BioGraph bg) {
        // Have to remove edges explicitly since there is a local copy of the edge
        // in the widget panel
        Iterator i = bg.getEdges().iterator();
        while( i.hasNext() ) {
            edges.remove( i.next() );
        }
        
        // All biographs are in species and patterns
        this.species.remove( bg );
        this.patterns.remove( bg );
        this.removeFlickrLabel( bg.getFlickrLabel() );
        
        if ( selected_species == bg ) selected_species = null;
        else if ( selected_pattern == bg ) selected_pattern = null;
        
        
        return true;
        
    }
    
    public void removeWidget( Widget w )
    {
        if (debug_statements) System.out.println("removeComponent() called");
        if ( w instanceof BioGraph ) removeBioGraph( (BioGraph)w );
        else if ( w instanceof BioComponent ) removeComponent( (BioComponent)w );
        else if ( w instanceof BioContainer ) removeContainer( (BioContainer)w );
        else if ( w instanceof Edge ) removeEdge( (Edge)w );
        else if ( w instanceof Group ) removeGroup( (Group)w );
        else if ( w instanceof Operator ) removeOperator( (Operator)w );
        else if ( w instanceof Pattern ) removePattern( (Pattern)w );
        else if ( w instanceof ReactionRule ) removeReactionRule( (ReactionRule)w );
        else if ( w instanceof Species ) removeSpecies( (Species)w );
        else if ( w instanceof FlickrLabel ) removeFlickrLabel( (FlickrLabel)w );
        else displayError("Unknown Widget Type","An unknown widget type was encountered in WidgetPanel:removeWidget().\nContact support at support@bionetgen.com.");
        repaint();
    }
    
    public boolean removeSelectedWidget() {
        // Erase any selected edge
        if ( getSelectedEdge() != null ) {
            if (debug_statements) System.out.println("Erasing Edge");
            
            Edge edge = getSelectedEdge();
            removeEdge( edge );
        }
        else if ( getSelectedComponent() != null ) {
            if (debug_statements) System.out.println("Erasing Component");
            
            BioComponent selection = getSelectedComponent();
            removeComponent( selection );
            clearSelections();
            
        }
        else if ( getSelectedContainer() != null ) {
            if (debug_statements) System.out.println("Erasing Container");
            
            BioContainer selection = getSelectedContainer();
            removeContainer( selection );
            clearSelections();
            
        }
        else if ( getSelectedOperator() != null ) {
            if (debug_statements) System.out.println("Erasing Operator");
            
            Operator selection = getSelectedOperator();
            removeOperator( selection );
            clearSelections();
            
        }
        else if ( getSelectedSpecies() != null ) {
            if (debug_statements) System.out.println("Erasing Species");
            
            Species selection = getSelectedSpecies();
            removeSpecies( selection );
            clearSelections();
            
        }
        else if ( getSelectedPattern() != null ) {
            if (debug_statements) System.out.println("Erasing Pattern");
            
            Pattern selection = getSelectedPattern();
            removePattern( selection );
            clearSelections();
            
        }
        else if ( getSelectedReactionRule() != null ) {
            if (debug_statements) System.out.println("Erasing ReactionRule");
            
            ReactionRule selection = getSelectedReactionRule();
            removeReactionRule( selection );
            clearSelections();
            
        }
        else if ( getSelectedGroup() != null ) {
            if (debug_statements) System.out.println("Erasing Group");
            
            Group selection = getSelectedGroup();
            removeGroup( selection );
            clearSelections();
            
        }
        else if ( the_selection_box.isInUse() == true ) 
        {
            //the_selection_box.inUse( false );
            // New implementation of selection box removes the contents from the
            // widgetpanel on creation so dont need all this any more
            if (debug_statements) System.out.println("Erasing SelectionBox");
            the_selection_box.deleteContents();
            the_selection_box.inUse(false);
            clearSelections();
        }
        return true;
    }
    
    public boolean cutSelectedWidget() 
    {
        copySelectedWidget();
        
        if ( getCopiedWidget() instanceof SelectionBox )
        {
            //the_selection_box.inUse( false );
            Iterator<Widget> itr = the_selection_box.getContents().iterator();
            while ( itr.hasNext() )
            {
                removeWidget(itr.next());
            }
            
            //containers.removeAll( the_selection_box.getContents() );
            //components.removeAll( the_selection_box.getContents() );
            //edges.removeAll( the_selection_box.getContents() );
            //operators.removeAll( the_selection_box.getContents() );
            //species.removeAll( the_selection_box.getContents() );
            //rules.removeAll( the_selection_box.getContents() );
            
            the_selection_box.inUse( false );
            repaint();
            return true;
        }
        
        if ( getSelectedWidget() instanceof BioComponent )
                {
                    if ( !((BioComponent)getSelectedWidget()).isMovableByUser() ) return false;
                }
        
        removeSelectedWidget();
        return true;
    }
    
    public boolean copySelectedWidget() {
        if (debug_statements) System.out.println("Copied the selected widget");
        
        /*
        if ( the_selection_box.isInUse() )
        {
            if ( the_selection_box.getContents().isEmpty() )
            {
                displayError( "Selection Box Copy Error","Nothing selected." );
                return false;
            }
            copied_selection_box = the_selection_box.makeCopy();
            copied_selection_box.calculatePointerOffset( the_selection_box.getX(), the_selection_box.getY() );
            if (debug_statements) System.out.println("Copied Selection Box has " + copied_selection_box.getContents().size() + " items.");
            getTheGUI().copied_widget = null;
            return true;
        }
        */
         
        Widget s_widget = getSelectedWidget();
        
        if ( s_widget == null )
        {
            displayError( "Copy Error","Nothing selected." );
            return false;
        }
        
        try 
        {
            //getTheGUI().copied_widget = s_widget;
            
            getTheGUI().copied_widget = WidgetCloner.clone( s_widget );
            
            //copied_selection_box = null;
        }
        catch ( Exception e ) {
            displayError("Error Cloning Widget","Unhandled exception in WidgetPanel::copySelectedWidget(). Contact support at support@bionetgen.com");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    
    
    public boolean pasteCopiedWidget() 
    {
        int mouse_x = mouse_control.getX();
        int mouse_y = mouse_control.getY();
       
        if ( !mouse_control.mouse_over )
        {
            mouse_x = 0;
            mouse_y = 0;
        }
        
        return pasteCopiedWidget( mouse_x, mouse_y );
    }
        
    public boolean pasteCopiedWidget( int x, int y ) 
    {
        int mouse_x = x;
        int mouse_y = y;
        
       if ( debug_statements ) System.out.println("WidgetPanel:pasteCopiedWidget() called");
       
        Widget wid = getCopiedWidget();
        if ( getCopiedWidget() == null ) return false;
        
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
        
        /*
        if ( wid instanceof SelectionBox )
        {
            if (debug_statements) System.out.println("Pasting Selection Box with " + ((SelectionBox)wid).getContents().size() );
            clearSelections();
            the_selection_box.setSelected( false );
            the_selection_box.inUse( false );
            
            ((SelectionBox)wid).setContainingPanel(this);
            the_selection_box.inUse( true );
            the_selection_box = ((SelectionBox)wid).makeCopy();
            //the_selection_box = copied_selection_box;
            the_selection_box.updateLocation(mouse_x-the_selection_box.getWidth()/2, mouse_y-the_selection_box.getWidth()/2, true);
            the_selection_box.releaseContents();
            //setTheSelectionBoxToLocation( copied_selection_box, mouse_x, mouse_y );   
            //copied_selection_box = null;
            return true;
        }
         */
        
        try
        {
            Widget copy = WidgetCloner.clone( wid );
        
            
        
        
        if (debug_statements) System.out.println("++x and y in pasteCopiedWidget(): " + mouse_x + "," + mouse_y);
        
	if ( isInsideBoundaries( mouse_x, mouse_y ) )
        {
            //int x_center = mouse_x-copy.getWidth()/2;
            //int y_center = mouse_y-copy.getHeight()/2;
            copy.resetOffsets();
            //copy.setXOffset( 0 );
            //copy.setYOffset( 0 );
            //copy.updateLocation(0,0,true);
            //copy.calculatePointerOffset( 0, 0 );
             
            boolean try_to_center = true;
            
            addWidgetToLocation( copy, mouse_x, mouse_y, try_to_center );
     
        }
        else
        {
            displayError("Paste Error","Attempted to paste an item to the current mouse location\n" +
            "but the mouse pointer is not over the editor window.");
        }
        
        }
        catch ( Exception e )
        {
            if (debug_statements) System.out.println("Widget Cloning Error: An unhandled exception was encountered in WidgetPanel:PasteCopiedWidget(). The message was \"" + e.getMessage() + ".\"");
            displayError("Widget Cloning Error","An unhandled exception was encountered in WidgetPanel:PasteCopiedWidget(). Contact support at support@bionetgen.com.");
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    public boolean isInsideBoundaries( int x, int y ) 
    {
        return isInsideBoundaries( x, y, 0, 0 ); 
    }
    
    public boolean isInsideBoundaries( int x, int y, int width, int height ) 
    {
            int panel_width = (int)(getBaseDimension().getWidth()*(1.0/getZoom())-getZoomedXTranslation()*(1.0/getZoom()));
            int panel_height = (int)(getBaseDimension().getHeight()*(1.0/getZoom())-getZoomedYTranslation()*(1.0/getZoom()));
            int panel_x = (int)(-getZoomedXTranslation()*(1.0/getZoom()));
            int panel_y = (int)(-getZoomedYTranslation()*(1.0/getZoom()));
           
            if (debug_statements) System.out.println( "panel_width = " + panel_width + ", panel_height = " + panel_height );
            if (debug_statements) System.out.println( "panel_x = " + panel_x + ", panel_y = " + panel_y );
            if (debug_statements) System.out.println("Zoomed X,Y: " + getZoomedXTranslation() + ", " + getZoomedYTranslation() );
                 
	    	    
		    if((x)>panel_width)
			{
			    return false;
			}
		    if(x < panel_x)
			{
			    return false;  
			}
		    if((y)>panel_height)
                    {
			return false;
		    }
		    if(y < panel_y)
                    {
			return false;
		    }
            
        return true;
    }
    
    synchronized public boolean addOperatorToLocation(Operator operator, int x, int y) 
    {
        if ( selected_operator != null ) {
                selected_operator.setSelected(false);
            }
            
            selected_operator = operator;
            x -= operator.getWidth()/2;
            y -= operator.getHeight()/2;
            selected_operator.updateLocation(x,y,true);
            
            
            addOperator(selected_operator);
            
            return true;
    }
    
    synchronized public boolean addGroupToLocation(Group g, int x, int y) 
    {
            g.setContainingPanel( this );
            g.updateLocation(x,y,true);
            
            addSelectedGroup(g);
     
            return true;
    }
    
    synchronized public boolean addSpeciesToLocation(Species species, int x, int y) 
    {
            species.setContainingPanel( this );
            species.updateLocation(x,y,true);
 
            addSelectedSpecies(species);
     
            return true;
    }
    
    synchronized public boolean addPatternToLocation(Pattern pattern, int x, int y) 
    {
            pattern.setContainingPanel( this );
            pattern.updateLocation(x,y,true);
 
            addSelectedPattern( pattern );
     
            return true;
    }
    
    synchronized public boolean addReactionRuleToLocation(ReactionRule rule, int x, int y) 
    {
            
            rule.setContainingPanel( this );
            rule.updateLocation(x,y,true);
            
            addSelectedReactionRule(rule);
     
            return true;
    }
    
    synchronized public boolean addComponentToLocation(BioComponent component, int x, int y) 
    {
            component.setContainingPanel( this );
            
            component.updateLocation( x, y, true );
            
            addSelectedComponent( component );
            return true;
    }
    
    // Wrappers for drag and drop use
    synchronized public boolean addContainerToLocation(BioContainer container, Point drop_point) 
    {
        if (debug_statements) System.out.println("Drop Point: " + drop_point.getX() + ", " + drop_point.getY() );
        // Compensate for zoom and translation
        int x = (int)((drop_point.getX()-getZoomedXTranslation())*(1.0/getZoom()));
        int y = (int)((drop_point.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
        if (debug_statements) System.out.println("Drop Coords Translated to: " + x + ", " + y );    
        

        
        return addContainerToLocation( container, x, y );
    }
    
    synchronized public boolean addSpeciesToLocation(Species species, Point drop_point) 
    {
        // Compensate for zoom and translation
        int x = (int)((drop_point.getX()-getZoomedXTranslation())*(1.0/getZoom()));
        int y = (int)((drop_point.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
        return addSpeciesToLocation( species, x, y );
    }
    
    synchronized public boolean addPatternToLocation(Pattern pattern, Point drop_point) 
    {
        // Compensate for zoom and translation
        int x = (int)((drop_point.getX()-getZoomedXTranslation())*(1.0/getZoom()));
        int y = (int)((drop_point.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
        return addPatternToLocation( pattern, x, y );
    }
    
    public boolean addReactionRuleToLocation(ReactionRule rule, Point drop_point) 
    {
        // Compensate for zoom and translation
        int x = (int)((drop_point.getX()-getZoomedXTranslation())*(1.0/getZoom()));
        int y = (int)((drop_point.getY()-getZoomedYTranslation())*(1.0/getZoom()));    
            
        return addReactionRuleToLocation( rule, x, y );
    }
    
    public boolean addCDKCapsuleToLocation( CDKCapsule capsule, int x, int y )
    {
        capsule.updateLocation(x, y, true );
        cdk_capsules.add( capsule );
        repaint();
        return true;
    }
    
    synchronized public boolean addContainerToLocation(BioContainer container, int x, int y) 
    {
            if ( container.getContainingPanel() instanceof MoleculePalette ) 
             {
                container.setComponentsToDefault();
             }
        
        container.setContainingPanel( this );
        
        // Reset offsets
        //container.setXOffset( 0 );
        //container.setYOffset( 0 );
            // Center the widget
        if (debug_statements) System.out.println("++x and y in addContainerToLocation(): " + x + "," + y);
        container.updateLocation( x, y, true );
                        
        addSelectedContainer(container);
        
        return true;
    }
    
        public void setZoom(double zoom_factor, boolean silent) 
        {
            // better do some sanity check on the
            // zoom factor, too
            double max_zoom = 2.0;  
            double min_zoom = 0.1;
            
           
            if ( zoom_factor > max_zoom )
            {
                 if (!silent) displayWarning("Zoom Warning","Maximum zoom ("+max_zoom+"x) reached");
                zoom_factor = max_zoom;
            }
            else if ( zoom_factor < min_zoom )
            {
                 if (!silent) displayWarning("Zoom Warning","Minimum zoom ("+min_zoom+") reached");
                zoom_factor = min_zoom;
            }
            
            
            if (debug_statements) System.out.println("Zoom set to " + zoom_factor );

            this.zoom_factor = zoom_factor;
      
            
            setPreferredSize( getZoomedUsedArea() );
            
            Widget sel_w = getSelectedWidget();
            if ( sel_w != null )
            {
                        Rectangle r = new Rectangle(sel_w.getZoomedX(), sel_w.getZoomedY(), sel_w.getZoomedWidth(), sel_w.getZoomedHeight() );
                    
                        if (debug_statements) System.out.println("Scrolling to Visible: " + sel_w.getZoomedX() +","+ sel_w.getZoomedY() +","+ sel_w.getZoomedWidth() +","+ sel_w.getZoomedHeight() );
                    
                        scrollRectToVisible(r);
            }
            
            revalidate();
            repaint();
            
        }
        
        public double getZoom() 
        {
            return zoom_factor;
        }        

        public void setXTranslation( int x )
        {
            if (debug_statements) System.out.println("Setting X Translation to " + x );
            x_trans = x;
           
            invalidate();
            revalidate();
            repaint();
        }
        
        public void setYTranslation( int y )
        {
            if (debug_statements) System.out.println("Setting Y Translation to " + y );
            y_trans = y;
            invalidate();
            revalidate();
            repaint();
        }
        
        public int getXTranslation()
        {
            return x_trans;
        }
        
        public int getYTranslation()
        {
            return y_trans;
        }
        
        public int getZoomedXTranslation()
        {
            return zoomed_x_translation;
        }
        
        public int getZoomedYTranslation()
        {
            return zoomed_y_translation;
        }
        
        public Dimension getBaseDimension() 
        {
            return base_dimension;
        }
        
        public void stateChanged(javax.swing.event.ChangeEvent e) 
        {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) 
            {
                int zoom = (int)source.getValue();
                setZoom( zoom/10.0, false );
            }
        }
    
    public Dimension getArea() 
    {
        return area;
    }
    
    /**
     *
     * @param new_area
     */    
    
    public void setArea(Dimension new_area) 
    {
        area = new_area;
    }
    
    synchronized public Pattern makePattern( Vector<BioContainer> containers ) 
    {
        if (debug_statements) System.out.println("MakePattern(containers) called with " + containers.size() + " containers.");
        
            Pattern p = new Pattern( "Pattern", 0, 0, this );

            
            
            Iterator scont_itr = containers.iterator();
            while ( scont_itr.hasNext() )
            {
                if (debug_statements) System.out.println("MakePattern(containers): considering adding container");
                BioContainer bc = (BioContainer)scont_itr.next();
                
                try
                {
                p.addContainer( bc );
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                        
                // cant do it here because of a threading problem
                /*
                if ( bc.isValidPattern() )
                {
                    if (debug_statements) System.out.println("MakePattern(containers): adding container");
                    p.addContainer( bc  );
                }
                else
                {
                     displayError("Pattern Creation Error", "Invalid operand found. One of the containers is not a valid pattern.");
                     return null;
                }
                 */
                
            }
            
        Pattern p_copy = null;
        try
        {
            p_copy = (Pattern)WidgetCloner.clone( p ); 
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
            
        p.disband();
            
        if (debug_statements) System.out.println("MakePattern(containers): setting pattern label");
        p_copy.setLabel( getNextBioGraphLabel() );
          
        if (debug_statements) System.out.println("MakePattern(containers) finished");
        return p_copy;
    }
    

    public Image loadImage(java.lang.String path) 
    {
        URL url = this.getClass().getResource(path);
        ImageIcon icon = null;
        
        try 
        {
            icon = new ImageIcon(url);
        }
        catch ( Exception e )
        {  
            if (debug_statements) System.out.println( "Error Opening Operator Icon URL: The exception was " + e.getMessage() );
            if (debug_statements) System.out.println("This operator is an instance of " + this.getClass().getName() );
            return null;
        }
        
        Image  image = icon.getImage();
        return image;
    }
    
    public void setCursorAccordingToMode() 
    {
        if ( mode.equals("manipulate") ) 
        {
            /*
            Image image = loadImage("images/manip_cursor.png");
            
            Point hotspot = new Point( 5,5 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            */
            
           Cursor c = Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ); 
           setCursor( c );
        }
        else if ( mode.equals("add_unbound_components") ) 
        {
            
            String os_name = System.getProperty("os.name");
            if ( os_name.matches(".*Windows.*") )
            {
                Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
                setCursor( c );
            }
            else
            {
                Image image = loadImage("images/component.png");
           
                Point hotspot = new Point( 8,8 );
                String name = mode;
                Toolkit tk = Toolkit.getDefaultToolkit();
                Cursor c = tk.createCustomCursor( image, hotspot, name );
            
                setCursor( c );
            }
            

            
        }
        else if ( mode.equals("add_containers") ) 
        {
            /*
            Image image = loadImage("images/container.png");
            
            Point hotspot = new Point( 12,12 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            setCursor( c );
            */
            
            Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
            setCursor( c );
            
        }
        else if ( mode.equals("add_edges") || mode.equals("atom_map") ) 
        {
            /*
            Image image = loadImage("images/edge.png");
            
            Point hotspot = new Point( 0,0 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            */
            
            Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
            setCursor( c );
             
        }
        else if ( mode.equals("add_forward_operators") ) 
        {
            String os_name = System.getProperty("os.name");
            if ( os_name.matches(".*Windows.*") )
            {
                Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
                setCursor( c );
            }
            else
            {
            Image image = loadImage("images/forward_op.png");
            
            Point hotspot = new Point( 12,12 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            setCursor( c );
            }
        }
        else if ( mode.equals("add_plus_operators") ) 
        {
            String os_name = System.getProperty("os.name");
            if ( os_name.matches(".*Windows.*") )
            {
                Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
                setCursor( c );
            }
            else
            {
            Image image = loadImage("images/plus_op.png");
            
            Point hotspot = new Point( 12,12 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            setCursor( c );
            }
        }
        else if ( mode.equals("add_forward_and_reverse_operators") ) 
        {
            String os_name = System.getProperty("os.name");
            if ( os_name.matches(".*Windows.*") )
            {
                Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
                setCursor( c );
            }
            else
            {
            Image image = loadImage("images/forward_and_reverse_op.png");
            
            Point hotspot = new Point( 12,12 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            setCursor( c );
            }
        }
        else if ( mode.equals("add_union_operators") ) 
        {
            String os_name = System.getProperty("os.name");
            if ( os_name.matches(".*Windows.*") )
            {
                Cursor c = Cursor.getPredefinedCursor( Cursor.CROSSHAIR_CURSOR ); 
                setCursor( c );
            }
            else
            {
            Image image = loadImage("images/union_op.png");
            
            Point hotspot = new Point( 12,12 );
            String name = mode;
            Toolkit tk = Toolkit.getDefaultToolkit();
            Cursor c = tk.createCustomCursor( image, hotspot, name );
            setCursor( c );
            }
        }
    }
    
    public Dimension getZoomedUsedArea()
    {
        Dimension area = getUsedArea();
            int width = area.width;
            int height = area.height;
        
            int used_width = (int)((width+getZoomedXTranslation())*(getZoom()));
            int used_height = (int)((height+getZoomedYTranslation())*(getZoom()));
        
            int visible_width = (int)(getVisibleRect().width);
            
            int visible_height = (int)(getVisibleRect().height);
            
            if (debug_statements) System.out.println( "used width: " + used_width + " visible width: " + visible_width );
            if (debug_statements) System.out.println( "used height: " + used_height + " visible height: " + visible_height );
            
            
            int preferred_width = used_width > visible_width ? used_width : visible_width;  
            int preferred_height = used_height > visible_height ? used_height : visible_height;
            
            area.width = preferred_width;
            area.height = preferred_height;

            return area;
    }
    
    public Dimension getUsedArea() 
    {
        // Find right most widget's rightmost coord
        int rightmost = 0;
        int lowest = 0;
        int padding = 50;
        
        Iterator widget_itr = getAllWidgets().iterator();
        while ( widget_itr.hasNext() )
        {
            Widget current = (Widget)widget_itr.next();
            int x = current.getX()+current.getWidth()+padding; //modify for width after this test
            int y = current.getY()+current.getHeight()+padding;
            if ( x > rightmost ) rightmost = x;
            if ( y > lowest ) lowest = y;
        }
        
        Dimension used_area = new Dimension();
        used_area.width = rightmost;
        //used_area.width*=getZoom();
        //used_area.width+=getZoomedXTranslation();
        used_area.height = lowest;
        //used_area.height*=getZoom();
        //used_area.height+=getZoomedYTranslation();
        
        return used_area;
                
    }
    
    public void autoZoom( boolean silent ) 
    {
        //double current_zoom_factor = getZoom();
                
            // Get the size of the visible area
            double current_aperture_width = getVisibleRect().getWidth();
            double current_aperture_height = getVisibleRect().getHeight();
                
            // Get the size of the used area
            Dimension used_area = getUsedArea();
            
            double used_area_width = used_area.width;
            //used_area_width/=getZoom();
            //used_area_width-=getZoomedXTranslation();
          
            double used_area_height = used_area.height;
            //used_area_height/=getZoom();
            //used_area_height-=getZoomedYTranslation();
                       
            if (debug_statements) System.out.println( "Ap Width: " + current_aperture_width + " Ap Height: " + current_aperture_height + " Used width: " + used_area_width + " Used height: " + used_area_height );
            
            if ( !silent )
            if ( current_aperture_height >= used_area_height && current_aperture_width >= used_area_width )
            {
                displayWarning("Autozoom Warning", "The entire canvas is already visible.");
                return;
            }
            
            double vertical_zoom_needed = current_aperture_height/used_area_height;
            double horizontal_zoom_needed = current_aperture_width/used_area_width;
            
            // Use the smallest zoom factor of the two dimensions
            double zoom_needed = vertical_zoom_needed > horizontal_zoom_needed ? horizontal_zoom_needed : vertical_zoom_needed;
            //zoom_needed *= getZoom();
            
            if ( !silent )
            if ( zoom_needed < 0.1 )
            {
                zoom_needed = 0.1;
                displayWarning("Autozoom Warning", "Could not zoom below 0.1, enlarge this window and try autozoom again.");
            }
            
            
                
            setZoom( zoom_needed, silent );
    }    
    
    public Dimension getPreferredScrollableViewportSize() 
    {
        // I have no idea why 0,0 allows the viewport to resize with the frame...
        Dimension size = getSize();
        System.out.println( "getPreferredScrollableViewportSize() returned: " + size.getWidth() + ", " + size.getHeight() );
        return size;
        //getZoomedUsedArea();
        // ... but it seems to work :(
    }
    
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) 
    {
        int tick_size = (int)(75/getZoom());
        return tick_size;
    }
    
    public boolean getScrollableTracksViewportHeight() 
    {
        return false;
    }
    
    public boolean getScrollableTracksViewportWidth() 
    {
        return false;
    }
    
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) 
    {
       int tick_size = 100;
       if (debug_statements) System.out.println("Returning ScrollableUnitIncrement: " + tick_size);
       return tick_size;
    }
    
    public void moveAllDown(Widget source, int offset) 
    {
        Iterator w_itr = getAllWidgets().iterator();
        
        // Check for duplicate entries
        Vector<Widget> seen = new Vector<Widget>();
        while ( w_itr.hasNext() )
        {
            Widget o = (Widget)w_itr.next();
            
            
            if ( seen.indexOf(o) != -1 )
            {
                displayError("Move all Widgets Error","Duplicate Widgets in getAllWdigets()");
            }
            else
            {
                seen.add( o );
            }
        }
       
        // Protect the contents of the selection box from being moved twice
        //seen.addAll( the_selection_box.getContents() );
        
        Iterator s_itr = seen.iterator();
        
        while ( s_itr.hasNext() )
        {
            Widget w = (Widget)s_itr.next();
          
            // Protect the contents of the selection box from moving
            if ( the_selection_box == source )
            if ( the_selection_box.getContents().indexOf(w) != -1 )
            {
                continue;
            }
            // Don't move contained components since the container will do that
            // already
            if ( w instanceof BioComponent )
            {
                if ( ((BioComponent)w).isContained() )
                {
                    continue;
                }
            }
            
            if ( w instanceof Pattern )
            {
                continue;
            }
            
            if ( w == source )
            {
                continue;
            }
            
            
            
            w.resetOffsets();
            
            w.updateLocation( w.getX(), w.getY()+offset, true );
        }
    }
    
    public void moveAllRight(Widget source, int offset) 
    {
        Iterator w_itr = getAllWidgets().iterator();
        
        // Check for duplicate entries
        Vector<Widget> seen = new Vector<Widget>();
        while ( w_itr.hasNext() )
        {
            Widget o = (Widget)w_itr.next();
            
            if ( seen.indexOf(o) != -1 )
            {
                displayError("Move all Widgets Error","Duplicate Widgets in getAllWdigets()");
            }
            else
            {
                seen.add( o );
            }
        }
        
        if (debug_statements) System.out.println("moveAllRight(): There are " + seen.size() + " widgets in the seen list.");
        
        // Protect the contents of the selection box from being moved twice
        seen.addAll( the_selection_box.getContents() );
        
        Iterator s_itr = seen.iterator();
        
        while ( s_itr.hasNext() )
        {
            Widget w = (Widget)s_itr.next();
            
            // Protect the contents of the selection box from moving
            if ( the_selection_box == source )
            if ( the_selection_box.getContents().indexOf(w) != -1 )
            {
                continue;
            }
            // Don't move contained components since the container will do that
            // already
            if ( w instanceof BioComponent )
            {
                if ( ((BioComponent)w).isContained() )
                {
                    continue;
                }
            }
            
            if ( w instanceof Pattern )
            {
                continue;
            }
            
            if ( w == source )
            {
                continue;
            }
            
            if (debug_statements) System.out.println("moveAllRight(): moving widget right by " + offset );
            w.resetOffsets();
            w.updateLocation( w.getX()+offset, w.getY(), true );
        }
    }
    
        public KeyboardControl getKeyboardListener() 
        {
            return keyboard_control;
        }   
    
        public void setFlickrLabels(Vector<FlickrLabel> labels) 
        {
            flickr_labels = labels;
        }

 /*
    public void repaint() 
    {
        // We need to be sure that the repaint happens in the event dispatch thread
        
        Runnable repaint_panel = new Runnable() 
        {
            public void run() 
            { 
                repaint_helper();
            }
        };
        SwingUtilities.invokeLater(repaint_panel);
    }
        
    // Helper function called by invokeLater in repaint
    private void repaint_helper()
    {
        super.repaint();
    }
  
  */

    public boolean print() 
    {
        PrintUtilities.printComponent( this );
        return true;
    }

   

    public boolean isSelected() 
    {
        
        return is_selected;
    }

    public void setSelected(boolean is_selected) 
    {
        this.is_selected = is_selected;
        
        if ( is_selected == false )
        {
            clearSelections();
            
            the_selection_box.inUse(false);
            
            the_selection_box.setSelected(false);
        }
    }

    public void sort(Vector widgets) 
    {
        
    }

    


}
