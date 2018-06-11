/*
 * BioGraphCloner.java
 *
 * Created on June 13, 2005, 11:55 AM
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

class BioGraphCloner1
{
   // so that nobody can accidentally create an ObjectCloner object
    private BioGraphCloner1(){}
   
    static public BioGraph clone( BioGraph existing_bg ) throws Exception
   {
        return deepCopy( existing_bg );
   }
   
   // returns a deep copy of any widget
   static public BioGraph deepCopy(BioGraph existing_bg) throws Exception
   {
      ObjectOutputStream oos = null;
      ObjectInputStream ois = null;
      
      BioGraph rval = null;
      
         ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
         oos = new ObjectOutputStream(bos);
         // serialize and pass the object
         oos.writeObject(existing_bg);   
         oos.flush();               
         ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); 
      
         
      try
      {
         ois = new ObjectInputStream(bin);                 
         rval = (BioGraph)ois.readObject();
         
         if (rval == null )
         {
            System.out.println("Fault reading species object from stream in BioGraphCloner");
         }
         
      }
      catch (EOFException eof)
        {
            System.out.println("eof encountered" + eof.getMessage());
        } 
        catch (OptionalDataException ode)
        {
            System.out.println("OptionalDataException" + ode.getMessage());
        }
        catch (IOException ioe)
        {                  
              System.out.println("IOException on read object");
              System.out.println(ioe.getMessage());
              System.out.println(ioe.toString());
        }
        catch (ClassNotFoundException cnf)
        {
            System.out.println("ClassNotFoundException");
        }
        catch ( ClassCastException cce )
        {
        System.out.println( "Class Cast Exception:" + cce.getMessage() );
        }
      catch(Exception e)
      {
         System.out.println("Exception in SpeciesCloner = " + e);
         throw(e);
      }
      finally
      {
         oos.close();
         ois.close();
      }
      
            // Set the containing panel here since it is declared to be transitory
      rval.setContainingPanel( existing_bg.getContainingPanel() );
      return rval;
   }
  
}

