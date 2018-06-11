/*
 * IDRulesGenerator.java
 *
 * Created on 21 April, 2006
 */

import java.beans.*;
import java.io.Serializable;

// THIS HAS TO BE SAVED AS PART OF STATE OR WILL CAUSE BUGS
 
/**
 * @author mlf; extension of IDGenerator
 */
    public class IDRulesGenerator implements Serializable 
    {
            // Serialization explicit version
        private static final long serialVersionUID = 1;
        
        transient protected boolean debug_statements = true;
        private static long next_id;
        
        IDRulesGenerator()
        {
		next_id = 0;
        }
        
        public long getNextIDRules()
        {
            if (debug_statements) System.out.println( "Generated IDRules: " + next_id );
            return next_id++;
        }
        
        public long getCurrentIDRules()
        {
            return next_id;
        }
        
        
        public void setCurrentIDRules( long id )
        {
            next_id = id;
            if (debug_statements) System.out.println( "Current IDRules set to: " + next_id );
        }
    }
