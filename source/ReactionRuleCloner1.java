/*
 * SpeciesCloner.java
 *
 * Created on December 22, 2004, 3:21 PM
 */

/**
 *
 * @author  matthew
 */
// This class used serialization to perform a deep copy of a Species
// All widget subclasses are serializable

import java.io.*;
import java.util.*;
import java.awt.*;

class ReactionRuleCloner1
{
   // so that nobody can accidentally create an ObjectCloner object
    private ReactionRuleCloner1(){}
   
    static public ReactionRule clone( ReactionRule existing_reaction_rule ) throws Exception
   {
        return deepCopy( existing_reaction_rule );
   }
   
   // returns a deep copy of any widget
   static public ReactionRule deepCopy(ReactionRule existing_reaction_rule) throws Exception
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      ReactionRule rval = null;
      
      try
      {
         ByteArrayOutputStream bos =
               new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_reaction_rule);   
         oos.flush();               
         ByteArrayInputStream bin =
               new ByteArrayInputStream(bos.toByteArray()); 
         ois = new ObjectInputStream(bin);                 
         // return the new widget
         rval = (ReactionRule)ois.readObject();
      }
      catch(Exception e)
      {
         System.out.println("Exception in ReactionRuleCloner = " + e);
         e.printStackTrace();
         throw(e);
      }
      finally
      {
         oos.close();
         ois.close();
      }
      
            // Set the containing panel here since it is declared to be transitory
      rval.setContainingPanel( existing_reaction_rule.getContainingPanel() );
      return rval;
   }
  
}

