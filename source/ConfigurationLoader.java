/*
 * ConfigurationLoader.java
 *
 * Created on June 2, 2005, 1:50 PM
 */

import java.beans.*;
import java.io.Serializable;
import java.io.*; // For file manipulation and object writing

/**
 * @author matthew
 */
public class ConfigurationLoader extends Object implements Serializable 
{
    transient protected boolean debug_statements = true;
    private File config_file;
    private GUI the_gui;

    ConfigurationLoader( String path, GUI the_gui )
    { 
        this.the_gui = the_gui;
        
        config_file = new File(path);
        
    }
    
    public Configuration load()
    {
        Configuration config = null;
        ObjectInput input = null;
	
        
        if (debug_statements) System.out.println("Loading configuration from " + config_file.getAbsolutePath() );
        
        try
        {
	    //use buffering
	    
	    InputStream file = new FileInputStream( config_file );
	    InputStream buffer = new BufferedInputStream( file );
	    input = new ObjectInputStream ( buffer );
            config = (Configuration)input.readObject();
        }
        catch (EOFException eof)
        {
            if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
        } 
        catch (OptionalDataException ode)
        {
            if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
        }
        catch (InvalidClassException ice)
        {                  
              if (debug_statements) System.out.println("Invalid Class Exception");
              if (debug_statements) System.out.println(ice.getMessage());
              if (debug_statements) System.out.println(ice.toString());
              
              //the_gui.getEditorPanel().displayError( "Load Failure","The data file (" +config_file+ ") you attempted to load appears to"
              //+ "\nbe from an incompatible version of BioNetGen.\nResetting configuration." );
              return new Configuration();
        }
        catch ( NullPointerException npe )
        {
            return new Configuration();
        }
        catch (IOException ioe)
        {                  
              if (debug_statements) System.out.println("IOException on read object");
              if (debug_statements) System.out.println(ioe.getMessage());
              if (debug_statements) System.out.println(ioe.toString());
        }
        catch (ClassNotFoundException cnf)
        {
            if (debug_statements) System.out.println("ClassNotFoundException");
        }
        catch ( ClassCastException cce )
        {
            System.out.println("Class Cast Exception:" + cce.getMessage());
        //the_gui.getEditorPanel().displayError( "Error Loading Settings",
        //                                        "Class Cast Exception:"
        //                                        + cce.getMessage() );
        }

        catch (Exception e)
        {
            e.printStackTrace();
            the_gui.getEditorPanel().displayError("Error Loading Configuration: ", "The exception was: " + e.getMessage() );
            
            
        }
        finally
        {
            
                if ( input != null ) 
                {
                    //close "input" and its underlying streams
                    
                    try
                    {
                        input.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        the_gui.getEditorPanel().displayError("Error Closing Configuration File: ", "The exception was: " + e.getMessage() );
                    }
                }
	
        }
        
        return config;
    }
    
}
