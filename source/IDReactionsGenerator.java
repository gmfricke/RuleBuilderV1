/*
 * IDReactionsGenerator.java
 *
 * Created on 1 May, 2006
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author mlf; extension of IDGenerator
 */
    public class IDReactionsGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        transient protected boolean debug_statements = true;
        private static long next_id;
        
        IDReactionsGenerator()
        {
		next_id = 0;
        }
        
        public long getNextIDReactions()
        {
            if (debug_statements) System.out.println( "Generated IDReactions: " + next_id );
            return next_id++;
        }
        
        public long getCurrentIDReactions()
        {
            return next_id;
        }
        
        
        public void setCurrentIDReactions( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current IDReactions set to: " + next_id );
        }
    }
