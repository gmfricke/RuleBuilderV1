/*
 * BioContainerCloner.java
 *
 * Created on December 15, 2004, 4:36 PM
 */

/**
 *
 * @author  Matthew Fricke
 */
// This class used serialization to perform a deep copy of a widget
// Widgets are serializable

import java.io.*;
import java.util.*;
import java.awt.*;

class BioContainerCloner
{
   // so that nobody can accidentally create an ObjectCloner object
   private BioContainerCloner(){}
   
    static public BioContainer clone( BioContainer existing_biocontainer ) throws Exception
   {
       System.out.println("Cloning BioContainer named " + existing_biocontainer.getLabel() );
        return deepCopy( existing_biocontainer );
   }
   
   // returns a deep copy of any widget
   static public BioContainer deepCopy(BioContainer existing_biocontainer)
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      BioContainer rval = null;
      
      try
      {
         ByteArrayOutputStream bos =
               new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_biocontainer);   
         oos.flush();               
         ByteArrayInputStream bin =
               new ByteArrayInputStream(bos.toByteArray()); 
         ois = new ObjectInputStream(bin);                 
         // return the new widget
         rval =  (BioContainer)ois.readObject();
         
         //Vector components_clone = new Vector();
         //Iterator comp_itr = rval.getComponents().iterator();
         //while ( comp_itr.hasNext() )
         //{
         //    components_clone.add( BioComponentCloner.clone( (BioComponent)comp_itr.next() ) ); 
         //}
         
         //rval.setComponents( components_clone );
         
      }
      catch(Exception e)
      {
         System.out.println("Exception in BioContainerCloner = " + e);
         existing_biocontainer.getContainingPanel().displayError("Error Cloning BioContainer","Contact Support at support@bionetgen.com.\n" +
         "The exception message was: " + e.getMessage());
         e.printStackTrace();
      }
      finally
      {
          try
          {
            oos.close();
            ois.close();
          }
          catch( Exception exp ) 
          {
              System.out.println("Exception in BioContainerCloner = " + exp);
              existing_biocontainer.getContainingPanel().displayError("Error closing streams during BioContainer Cloning","Contact Support at support@bionetgen.com.\n" +
              "The exception message was: " + exp.getMessage());
              exp.printStackTrace();
          }
      }
      
      // Set the containing panel here since it is declared to be transitory
      rval.setContainingPanel( existing_biocontainer.getContainingPanel() );
      //rval.assignID();
      
      return rval;
   }
  
}

