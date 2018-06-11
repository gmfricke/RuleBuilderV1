/*
 * ConfigurationSaver.java
 *
 * Created on June 2, 2005, 1:52 PM
 */

import java.beans.*;
import java.io.Serializable;
import java.io.*; // For file manipulation and object writing

/**
 * @author matthew
 */
public class ConfigurationSaver extends Object implements Serializable 
{
    transient protected boolean debug_statements = true;
    File config_file;
    GUI the_gui;
    
    ConfigurationSaver( String path, GUI the_gui  )
    { 
        this.the_gui = the_gui;
        
        config_file = new File(path);
    }
    
    public boolean save( Configuration config )
    {
	ObjectOutput output = null;
        	   
        if (debug_statements) System.out.println("Saving configuration to " + config_file.getAbsolutePath() );
        
        
        try{
	    //use buffering
	    OutputStream file = new FileOutputStream( config_file );
	    OutputStream buffer = new BufferedOutputStream( file );
	    output = new ObjectOutputStream( buffer );
            output.writeObject( config );
        }
        catch( NotSerializableException e )
	    {
		the_gui.getEditorPanel().displayError("Save Error",e.getMessage() + " in the Java object graph is not serializable. \nContact Support at support@bionetgen.com");
	    }
	catch(IOException ex)
	    {
		the_gui.getEditorPanel().displayError("State Save Exception Caught",
						   "Cannot output file. OutputStream.WriteObject Failed");
	    //fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
	    }
        catch ( Exception e )
        {
            e.printStackTrace();
            the_gui.getEditorPanel().displayError("Error Loading Configuration: ", e.getMessage() );
        }
        
	finally
	    {
	    try 
		{
		    if (output != null) 
			{
			    //flush and close "output" and its underlying streams
			    output.close();
			}
		}
	    catch (IOException ex )
		{
		    // ++++++++++++++++++++++++++++++++++++++++++++
		    // Should write error method that displays an error box and writes the logger
		    the_gui.getEditorPanel().displayError("Configuration Save Error","Cannot close output stream.");
		    //	fLogger.log(Level.SEVERE, "Cannot close output stream.", ex);
	    }
	    
	}
	
        return true;
    }
    
}