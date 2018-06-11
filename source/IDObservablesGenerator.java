/*
 * IDObservablesGenerator.java
 *
 * Created on 1 May, 2006
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author mlf; extension of IDGenerator
 */
    public class IDObservablesGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        transient protected boolean debug_statements = true;
        private static long next_id;
        
        IDObservablesGenerator()
        {
		next_id = 0;
        }
        
        public long getNextIDObservables()
        {
            if (debug_statements) System.out.println( "Generated IDObservables: " + next_id );
            return next_id++;
        }
        
        public long getCurrentIDObservables()
        {
            return next_id;
        }
        
        
        public void setCurrentIDObservables( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current IDObservables set to: " + next_id );
        }
    }
