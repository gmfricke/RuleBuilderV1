/*
 * IDParametersGenerator.java
 *
 * Created on 1 May, 2006
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author mlf; extension of IDGenerator
 */
    public class IDParametersGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        transient protected boolean debug_statements = true;
        private static long next_id;
        
        IDParametersGenerator()
        {
		next_id = 0;
        }
        
        public long getNextIDParameters()
        {
            if (debug_statements) System.out.println( "Generated IDParameters: " + next_id );
            return next_id++;
        }
        
        public long getCurrentIDParameters()
        {
            return next_id;
        }
        
        
        public void setCurrentIDParameters( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current IDParameters set to: " + next_id );
        }
    }
