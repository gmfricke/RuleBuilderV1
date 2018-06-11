/*
 * IDSpeciesGenerator.java
 *
 * Created on 21 April, 2006
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author mlf; extension of IDGenerator
 */
    public class IDSpeciesGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        transient protected boolean debug_statements = true;
        private static long next_id;
        
        IDSpeciesGenerator()
        {
		next_id = 0;
        }
        
        public long getNextIDSpecies()
        {
            if (debug_statements) System.out.println( "Generated IDSpecies: " + next_id );
            return next_id++;
        }
        
        public long getCurrentIDSpecies()
        {
            return next_id;
        }
        
        
        public void setCurrentIDSpecies( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current IDSpecies set to: " + next_id );
        }
    }
