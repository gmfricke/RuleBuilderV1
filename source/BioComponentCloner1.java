/*
 * BioComponentCloner.java
 *
 * Created on February 24, 2005, 10:13 PM
 *
 * Author: matthew
 */

// This class used serialization to perform a deep copy of a BioComponent
// BioComponents (like all widgets) are serializable

import java.io.*;
import java.util.*;
import java.awt.*;

class BioComponentCloner1
{
  

    
   // so that nobody can accidentally create a BioComponentCloner object
    private BioComponentCloner1(){}
   
    static public BioComponent clone( BioComponent existing_biocomponent ) throws Exception
   {
       System.out.println("Cloning BioComponent named " + existing_biocomponent.getLabel() );
        return deepCopy( existing_biocomponent );
   }
   
   // returns a deep copy of any widget
   static public BioComponent deepCopy(BioComponent existing_biocomponent) throws Exception
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      BioComponent rval = null;
      
      try
      {
         ByteArrayOutputStream bos =
               new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_biocomponent);   
         oos.flush();               
         ByteArrayInputStream bin =
               new ByteArrayInputStream(bos.toByteArray()); 
         ois = new ObjectInputStream(bin);                 
         // return the new widget
         rval =  (BioComponent)ois.readObject();
      }
      catch(Exception e)
      {
         System.out.println("Exception in BioComponentCloner = " + e);
         throw(e);
      }
      finally
      {
         oos.close();
         ois.close();
      }
      
            // Set the containing panel here since it is declared to be transitory
      rval.setContainingPanel( existing_biocomponent.getContainingPanel() );
      return rval;
   }
  
}

