// This class used serialization to perform a deep copy of a widget
// Widgets are serializable

import java.io.*;
import java.util.*;
import java.awt.*;

public class SelectionBoxCloner
{
   // so that nobody can accidentally create an ObjectCloner object
    private SelectionBoxCloner(){}
   // returns a deep copy of any widget
   /**
    *
    * @param existing_selection_box
    * @throws Exception
    * @return
    */   
   
   static public SelectionBox clone(SelectionBox existing_selection_box) throws Exception
   {
    return deepCopy( existing_selection_box );
   }
   
   static public SelectionBox deepCopy(SelectionBox existing_selection_box) throws Exception
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      SelectionBox rval = null;
      try
      {
         ByteArrayOutputStream bos =
               new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_selection_box);   
         oos.flush();               
         ByteArrayInputStream bin =
               new ByteArrayInputStream(bos.toByteArray()); 
         ois = new ObjectInputStream(bin);                 
         // return the new widget
          rval = (SelectionBox)ois.readObject();
      }
      catch(Exception e)
      {
         System.out.println("Exception in ObjectCloner = " + e);
         throw(e);
      }
      finally
      {
         oos.close();
         ois.close();
      }
      
      
      rval.assignID();
      return rval;
   }
  
}

