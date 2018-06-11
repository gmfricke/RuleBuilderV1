/*
 * IDGenerator.java
 *
 * Created on February 10, 2005, 3:19 PM
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author matthew
 */
    public class IDGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = -2220061743188752697L;
        
        transient protected boolean debug_statements = true;
        private static long next_id = 0;
        
        IDGenerator()
        {
            
        }
        
        public long getNextID()
        {
            if (debug_statements) System.out.println( "Generated ID: " + next_id );
            return next_id++;
        }
        
        public long getCurrentID()
        {
            return next_id;
        }
        
        
        public void setCurrentID( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current ID set to: " + next_id );
        }
    }
