// This class used serialization to perform a deep copy of a widget
// Widgets are serializable

import java.io.*;
import java.util.*;
import java.awt.*;

public class WidgetCloner
{
    // so that nobody can accidentally create an ObjectCloner object
   private WidgetCloner(){}
   // returns a deep copy of any widget
   static public Widget clone( Widget existing_widget ) throws Exception
   {
        return deepCopy( existing_widget );
   }
   
   /**
    *
    * @param existing_widget
    * @throws Exception
    * @return
    */   
   static public Widget deepCopy(Widget existing_widget) throws Exception
   {
      //if (debug_statements) System.out.println("Cloning Widget"); 
       
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      Widget rval = null;
      
      try
      {
         ByteArrayOutputStream bos =
               new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_widget);   
         oos.flush();               
         ByteArrayInputStream bin =
               new ByteArrayInputStream(bos.toByteArray()); 
         ois = new ObjectInputStream(bin);                 
         // return the new widget
         rval = (Widget)ois.readObject();
      }
      catch(Exception e)
      {
         System.out.println("Exception in ObjectCloner = " + e);
         e.printStackTrace();
         throw(e);
      }
      finally
      {
         oos.close();
         ois.close();
      }
      
      // Process non-serializable member-variables
      WidgetPanel cp = existing_widget.getContainingPanel();
      rval.setContainingPanel( cp );
      rval.setFont( existing_widget.getFont() );
     
      
      
      // We want pointers to the original parameter not a deep copy
      if (cp != null)
      {
        if ( existing_widget instanceof SelectionBox )
        {
            ((SelectionBox)rval).relinkParameters();
        }
          
        if ( existing_widget instanceof ForwardAndReverse ) 
        {              
          ((ForwardAndReverse)rval).relinkParameter();
        }
        else if ( existing_widget instanceof Forward ) 
        {
            ((Forward)rval).relinkParameter();
        }
        
        if ( existing_widget instanceof Species )
        {
            ((Species)rval).relinkParameter();
        }
         
        if ( existing_widget instanceof ReactionRule )
        {
          ((ReactionRule)rval).relinkParameters();
        }
        
      }
      
      
      // Remap unclonable cdk molecules
      if ( existing_widget instanceof BioContainer )
      {
          ((BioContainer)rval).reMapComponentsToCDK();
      }
      
      // Assign a unique identifier
      rval.setVisible( true );
      rval.assignID();
       
      rval.setUnselectedColor( existing_widget.getUnselectedColor() );
      rval.setSelectedColor( existing_widget.getSelectedColor() );
      
      return rval;
   }
  
}

