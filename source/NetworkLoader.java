/*
 * NetworkLoader.java
 *
 * Created on September 10, 2005, 4:56 AM
 */

import java.beans.*;
import java.io.Serializable;
import java.io.*;
import java.awt.*; 
import javax.swing.*;
import javax.swing.SwingUtilities;
  
import java.util.*; // For vector and parameter treemap
import java.io.Serializable; // So the model can be serialized
import java.io.*; // For file manipulation and string writing

import java.util.regex.*;
import java.awt.event.*;

/**
 * @author matthew
 */
public class NetworkLoader //extends LongTask
{
    int progress = 0;
    BufferedReader br;
    boolean done = false;
    boolean canceled = false;
    String status = "test";
                
    
    public NetworkLoader() {
        
    }
    
    public int getProgress()
    {
        return progress;
    }
   
    
    public void run() {
    }    
    
    public void load( final BufferedReader b, Model m ) 
    {
        the_model = m;
        
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                progress = 0;
                done = false;
                canceled = false;
                status = "test1";
                br = b;
                return new NetworkParser();
            }
        };
        worker.start();
    }

    /**
     * Called from ProgressBarDemo to find out how much work needs
     * to be done.
     */
    public int getLengthOfTask() {
        return 10000;
    }

    public void stop() {
        //canceled = true;
        //stayus = null;
    }

    /**
     * Called from ProgressBarDemo to find out if the task has completed.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns the most recent status message, or null
     * if there is no current status message.
     */
    public String getMessage() {
        return status;
    }

    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    class NetworkParser 
    {
            NetworkParser()
            {
                
                
    }
        
         
}

    private Model the_model;
}
    

