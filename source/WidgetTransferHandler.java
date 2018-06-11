import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.io.Serializable;
import java.util.List;
import java.util.*;
import java.net.*; //For URL image loading from Jar files

import javax.swing.*;


public class WidgetTransferHandler extends TransferHandler implements Transferable
{
    transient protected boolean debug_statements = true;
    protected Widget widget;
    protected BioContainer container;
    protected Species species;
    protected Pattern pattern;
    protected ReactionRule reactionrule;
    protected Point dragPoint;
    protected Point dropPoint;
    protected Component dragComponent;
    protected Component dropComponent;

    /**
     *
     * @return
     */    
    public Point getDropPoint() {
        return dropPoint;
    }
    
    public JComponent getSourceComponent()
    {
        return source_comp;
    }

    public void setDropPoint(Point dropPoint) {
        this.dropPoint = dropPoint;
    }

    public Component getDragComponent() {
        return dragComponent;
    }

    public void setDragComponent(Component dragComponent) {
        this.dragComponent = dragComponent;
    }

    public Point getDragPoint() {
        return dragPoint;
    }

    public void setDragPoint(Point dragPoint) {
        this.dragPoint = dragPoint;
    }

    public Component getDropComponent() {
        return dropComponent;
    }

    public void setDropComponent(Component dropComponent) {
        this.dropComponent = dropComponent;
    }

    public int getSourceActions(JComponent c) {
        return DnDConstants.ACTION_COPY_OR_MOVE;
    }

    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) 
    {
            if ( comp.equals( getSourceComponent() ) )
            {
                return false;
            }
        
            if (comp instanceof ObservablesPalette) 
            {
                if (debug_statements) System.out.println("Passing over ObservablesPalette in DropHandler");
                if ( reactionrule != null ) return false;
                return true;
            }
            else if (comp instanceof SpeciesPalette) 
            {
                if (debug_statements) System.out.println("Passing over SpeciesPalette in DropHandler");
                if ( reactionrule != null || pattern != null  ) return false;
                return true;
            }
          
            else if (comp instanceof ReactionRulePalette) 
            {
                if (debug_statements) System.out.println("Passing over ReationRulePalette in DropHandler");
                if ( reactionrule == null ) return false;
                return true;
            }
            else if (comp instanceof MoleculePalette) 
            {
                if (debug_statements) System.out.println("Passing over MoleculePalette in DropHandler");
                if ( container == null ) return false;
                return true;
            }
            else if (comp instanceof WidgetPanel)
            {
                if (debug_statements) System.out.println("WidgetPanel seen by DropHandler");
                return true;
            }
            return false;
    }

    public void exportAsDrag(JComponent comp, InputEvent e, int action) 
    {
        if (debug_statements) System.out.println("exportAsDrag run");
        setDragComponent(comp);
        setDragPoint(((MouseEvent) e).getPoint());
        //comp.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.exportAsDrag(comp, e, action );
        source_comp = comp;
    }

    protected Transferable createTransferable(JComponent jc ) {
	
      if (debug_statements) System.out.println("createTranferable run");

      WidgetPanel comp = null;
      
      if ( jc instanceof WidgetPanel )
      {
          comp = (WidgetPanel)jc;
        

                if ( comp.getSelectedContainer() != null )
                {
              
                        BioContainer source_container = comp.getSelectedContainer();
              
                        try
                        {
                            container = (BioContainer)WidgetCloner.clone( source_container );
                        }
                        catch( Exception e )
                        {
                            comp.displayError("Error Cloning Molecule", e.getMessage()+"\n" +
                            "Internal error: contact support at support@bionetgen.com");
                        }
                }
                else if ( comp.getSelectedSpecies() != null )
                {
                       Species source_species = comp.getSelectedSpecies();
              
                       try
                       {
                       species = (Species)WidgetCloner.clone( source_species );
                       //species = source_species;
                           
                        }
                        catch( Exception e )
                        {
                            comp.displayError("Error Cloning Species", e.getMessage()+"\n" +
                            "Internal error: contact support at support@bionetgen.com");
                        }
                }
          else if ( comp.getSelectedPattern() != null )
                {
                       Pattern source_pattern = comp.getSelectedPattern();
              
                       try
                       {
                        pattern = (Pattern)WidgetCloner.clone( (Pattern)source_pattern );
                           
                        }
                        catch( Exception e )
                        {
                            comp.displayError("Error Cloning Pattern", e.getMessage()+"\n" +
                            "Internal error: contact support at support@bionetgen.com");
                        }
                }
            else if ( comp.getSelectedReactionRule() != null )
                {
                       ReactionRule source_reactionrule = comp.getSelectedReactionRule();
              
                       try
                       {
                       reactionrule = (ReactionRule)WidgetCloner.clone( source_reactionrule );
                       //species = source_species;
                           
                        }
                        catch( Exception e )
                        {
                            comp.displayError("Error Cloning ReactionRule", e.getMessage()+"\n" +
                            "Internal error: contact support at support@bionetgen.com");
                        }
                }
          else if ( comp.getTheSelectionBox() != null )
                {
                       SelectionBox source_selectionbox = comp.getTheSelectionBox();
              
                       try
                       {
                       widget = (SelectionBox)WidgetCloner.clone( source_selectionbox );
                       //species = source_species;
                           
                        }
                        catch( Exception e )
                        {
                            comp.displayError("Error Cloning SelectionBox", e.getMessage()+"\n" +
                            "Internal error: contact support at support@bionetgen.com");
                        }
                }
          
           if ( species != null || container != null || reactionrule != null || pattern != null )
          {
                if (debug_statements) System.out.println("Transferable object created.");
          }
          else
          {
                if (debug_statements) System.out.println("Transferable object creation failed.");
          }
        
          comp.clearSelections();
      }
        else
          {
            if (debug_statements) System.out.println("Invalid JComponent source for Drag 'n Drop.");
          }
        
      return this; 
      }

    public boolean importData(JComponent comp, Transferable t) 
    {
        if ( comp instanceof ObservablesPalette )
        {
            return false;
        }
        
        if (debug_statements) System.out.println("importData run");
	
        if ( widget != null )
        {
            if (debug_statements) System.out.println( "TransferHandler: Widget class: " + widget.getClass().getName() );
        }
        else
        {
            if (debug_statements) System.out.println( "TransferHandler: Widget is null." );
        }
        
        //if ( comp instanceof WidgetPalette ) 
        //{
        //    return false;
        //}
        
        Point p = getDropPoint();
	
	if (p == null ) 
        {
            if (debug_statements) System.out.println("p = null!");
            return false;
        }
        
        try
        {
        
        if ( comp instanceof ObservablesPalette && pattern != null )
        {
            ((ObservablesPalette)comp).addPattern( pattern );
        }
        else if ( comp instanceof SpeciesPalette && species != null )
        {
            ((SpeciesPalette)comp).addSpecies( species );
        }
        else if ( comp instanceof ObservablesPalette && widget instanceof SelectionBox )
        {
            SelectionBox sb = (SelectionBox)widget;
            
            
           // if ( !((ObservablesPalette)comp).addGroup( sb ) )
           // {
           //     ((WidgetPanel)comp).displayError("Error Adding Observable","Pattern creation failed.");
           //     return false;
           // }
        }
        else if ( comp instanceof ObservablesPalette && container != null )
        {
            //((ObservablesPalette)comp).addGroup( container );
        }
        else if ( comp instanceof ObservablesPalette && species != null )
        {
            //((ObservablesPalette)comp).addGroup( species.getContainers() );
        }
        else if ( comp instanceof SpeciesPalette && widget instanceof SelectionBox )
        {
            ((SpeciesPalette)comp).addSpecies( (SelectionBox)widget );     
        }
        else if ( comp instanceof SpeciesPalette && container != null )
        {
            ((SpeciesPalette)comp).addSpecies( container );
        }
        else if ( comp instanceof ReactionRulePalette && reactionrule != null )
        {
            ((ReactionRulePalette)comp).addReactionRule( reactionrule );
        }
        else if ( comp instanceof MoleculePalette && container != null )
        {             
            ((MoleculePalette)comp).addMoleculeType( container );
        }
        else if ( comp instanceof WidgetPanel )
        {
         if ( container != null )
         {
             if (debug_statements) System.out.println("TransferHandler: Adding container to WidgetPanel");

             
              ((WidgetPanel) comp).addContainerToLocation( container, p );
         } 
         else if ( species != null )
         {
            ((WidgetPanel) comp).addSpeciesToLocation( species, p );
         }
         else if ( reactionrule != null )
         {
            ((WidgetPanel) comp).addReactionRuleToLocation( reactionrule, p );
         }
        }
        
        }
         catch( SpeciesCreationException e )
            {
                ((WidgetPanel)comp).displayError("Species Creation Error", e.getMessage() );
                return false;
            }
        
         //((WidgetPanel) comp).addContainer( container );
	if (debug_statements) System.out.println("importData successful");
	
        //reset the pointers to objects being transfered
        species = null;
        container = null;
        widget = null;
        reactionrule = null;
        pattern = null;
        
	return true;
        
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        
            try {
		if (debug_statements) System.out.println("exportDone run");
            } catch (Exception e) {
                e.printStackTrace();
            }
    }
    

    protected static DropHandler dropHandler = new DropHandler();
    
    private JComponent source_comp;
    
    public static DropHandler getDropHandler() {
        return dropHandler;
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException 
    {
        return widget;
    }
    
    public DataFlavor[] getTransferDataFlavors() 
    {
        return null;
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) 
    {
        return true;
    }
   
    // Only supported by Mac OSX at the time this was written 
    public Icon getVisualRepresentation(Transferable t) 
    {
       if (debug_statements) System.out.println("WidgetTransferHandler::getVisualRepresentation called");
        
       URL url = this.getClass().getResource("images/plus_op.png");
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
        
        return icon;
    }
    
    protected static class DropHandler implements DropTargetListener, Serializable {

        private boolean canImport;

        private boolean actionSupported(int action) {
            return (action & (DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_LINK)) !=
                   DnDConstants.ACTION_NONE;
        }

        // --- DropTargetListener methods -----------------------------------

        public void dragEnter(DropTargetDragEvent e) {
            DataFlavor[] flavors = e.getCurrentDataFlavors();

            JComponent c = (JComponent) e.getDropTargetContext().getComponent();            
            TransferHandler importer = c.getTransferHandler();

            if (importer != null && importer.canImport(c, flavors)) {
                canImport = true;
            } else {
                canImport = false;
            }

            int dropAction = e.getDropAction();

            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
        }

        public void dragOver(DropTargetDragEvent e) 
        {
            
        }

        public void dragExit(DropTargetEvent e) 
        {
        }

        public void drop(DropTargetDropEvent e) {

	    //if (debug_statements) System.out.println("Drop Event");
            int dropAction = e.getDropAction();

            JComponent c = (JComponent) e.getDropTargetContext().getComponent();
            WidgetTransferHandler importer = (WidgetTransferHandler) c.getTransferHandler();
            
            if (canImport && importer != null && actionSupported(dropAction)) {
                e.acceptDrop(dropAction);

                try {
        
                    if ( c instanceof WidgetPanel )
                    {
                        
                        Transferable t = e.getTransferable();
                         importer.setDropPoint(e.getLocation());
                         importer.setDropComponent(c);
                         e.dropComplete(importer.importData(c, t));
                         
         
                    }
		    else
                    {
                        //if (debug_statements) System.out.println("Drop Rejected");
                        e.rejectDrop();
		    }
                } catch (RuntimeException re) {
                    e.dropComplete(false);
                }
            } else {
                e.rejectDrop();
            }
        }

        public void dropActionChanged(DropTargetDragEvent e) {
            int dropAction = e.getDropAction();

            if (canImport && actionSupported(dropAction)) {
                e.acceptDrag(dropAction);
            } else {
                e.rejectDrag();
            }
        }
    }
}
