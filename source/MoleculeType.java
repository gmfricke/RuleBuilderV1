/*
 * MoleculeType.java
 *
 * Created on April 26, 2005, 10:32 AM
 */

import java.beans.*;
import java.io.Serializable;
import java.util.*;

/**
 * @author matthew
 */
public class MoleculeType extends BioContainer implements Serializable 
{
        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    transient protected boolean debug_statements = true;
    private Vector<ComponentType> component_types = new Vector<ComponentType>();
    
    public MoleculeType(BioContainer bc ) 
    {
        try
        {
        BioContainer bc_copy = (BioContainer)WidgetCloner.clone( bc );
        this.containing_panel = bc.getContainingPanel();
        setComponents( (Vector<BioComponent>)bc_copy.getComponents() );
        
        setX( bc.getX() );
        setY( bc.getY() );
        setWidth( bc.getWidth() );
        setHeight( bc.getHeight() );
        setLabel( bc.getLabel() );
        
         //setCDKCapsule( (CDKCapsule)WidgetCloner.clone(bc.getCDKCapsule()) );
         cdk_capsule = bc.getCDKCapsule();
        
        //reMapComponentsToCDK();
        
        Iterator comp_itr = getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent c = (BioComponent)comp_itr.next();
            c.setContainer( this );
            //c.setBindingState( "unbound" );
            
            ComponentType ct = getComponentType( c.getLabel() ); 
            
            if ( ct == null )
            {
                ct = new ComponentType();
                ct.setLabel( c.getLabel() );
            
                if ( c.getState() != null )
                {
                    if ( !c.getState().equals("*") )
                    {
                        ct.addAllowedState( c.getState() );
                        ct.setDefaultState( c.getState() );
                    }
                }
            
                ct.setContainingPanel(c.getContainingPanel());
            
                addComponentType( ct );
            }
            else
            {
                if ( c.getState() != null )
                {
                    if ( !c.getState().equals("*" ) )
                    {
                        ct.addAllowedState( c.getState() );
                    }
                }
            }
            
           
            
            //c.setState(c.getDefaultState());
        }
        

       
       
        
         setContainingPanel( bc.getContainingPanel() );
       }
       catch (Exception e)
       {
        getContainingPanel().displayError("Error Making MoleculeType",
        "The exception reported was: " + e.getMessage() );
        e.printStackTrace();
       }
       
    }
    
    public void setComponentTypes( Vector<ComponentType> v )
    {
        component_types = v;
    }
   
    public Vector<ComponentType> getComponentTypes()
    {
        return component_types;
    }
    
    // Get components by label
    public Vector<ComponentType> getComponentTypesByLabel( String label )
    {
        Vector<ComponentType> matches = new Vector<ComponentType>();
        Iterator itr = getComponentTypes().iterator();
        while ( itr.hasNext() )
        {
            ComponentType comp = (ComponentType)itr.next();
            if ( (comp).getLabel().equals(label) )
            {
                matches.add( comp );
            }
        }
        
        return matches;
    }
    
    public ComponentType getComponentType( String label )
    {
        Iterator ct_itr = getComponentTypes().iterator();
        
        while ( ct_itr.hasNext() )
        {
            ComponentType current_ct = (ComponentType)ct_itr.next();
            
            if ( current_ct.getLabel().equals( label ) )
            {
                return current_ct;
            }
        }
        
        return null;
    }
    
    public void addComponentType( ComponentType ct )
    {
        Vector<ComponentType> ctypes = getComponentTypes();
        
        // Check for duplication
        if ( ctypes.indexOf( ct ) != -1 )
        {
            return;
        }
        ctypes.add( ct );
    }
    
    public ComponentType removeComponentType( String label )
    {
        Iterator ct_itr = getComponentTypes().iterator();
        ComponentType item_to_remove = null;
        
        while ( ct_itr.hasNext() )
        {
            ComponentType current_ct = (ComponentType)ct_itr.next();
            
            if ( current_ct.getLabel().equals( label ) )
            {
                item_to_remove = current_ct;
            }
        }
        
        getComponentTypes().remove( item_to_remove );
        return item_to_remove;
    }
    
    public void setAllowedStatesFromUser() 
    {
        if (debug_statements) System.out.println("setAllowedStates shows " + getComponentTypes().size() + " component types");
        
        Iterator c_itr = getComponentTypes().iterator();
         while ( c_itr.hasNext() )
         {
             
            ComponentType current = (ComponentType)c_itr.next();
            
            // Check if a component type with this name already exists
            // if not then create it
           
            Vector<ComponentType> seen_list = new Vector<ComponentType>();
                     
            //ComponentType type = getComponentType( current.getLabel() );
            //if ( !seenBefore( type, seen_list ) )
            if ( !seenBefore( current, seen_list ) )
            {
                seen_list.add( current );
                
                current.displayAllowedStates();
                addComponentType( current );
            }
          
            if ( current == null )
            {
                getContainingPanel().displayError("Type creation error","Type was null in BioContainer:createMoleculeType()");
                return;
            }
                
            //current.setType( type );
         }
         
    }
    
    public void setContainingPanel(WidgetPanel container) 
    {
        super.setContainingPanel( container );
        
        Iterator itr = component_types.iterator();
        while ( itr.hasNext() )
        {
            ((ComponentType)itr.next()).setContainingPanel( container );
        }
    }
    
    private boolean seenBefore(ComponentType ct, Vector<ComponentType> seen_list) 
    {
        Iterator itr = seen_list.iterator();
        while ( itr.hasNext() )
        {
            ComponentType seen = (ComponentType)itr.next();
            if ( seen.getLabel().equals( ct.getLabel() ) )
            {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isType(BioContainer bc) 
    {
        if (debug_statements) System.out.println("MoleculeType:isType(): comparing " + bc.getLabel() + " and " + getLabel() );
        // Check that there are the same number of Components as ComponentTypes
        if ( bc.getComponents().size() != getComponents().size() )
        {
            if (debug_statements) System.out.println("MoleculeType:isType(): number of components not equal. " + getComponents().size() + "," + bc.getComponents().size() );
            if (debug_statements) System.out.println("MoleculeType:isType(): " + getComponents() );
            return false;
        }
        
        // Check the label
        if ( !bc.getLabel().equals( getLabel() ) || bc.getLabel().equals( "*" ) )
        {
            if (debug_statements) System.out.println("MoleculeType:isType(): labels not equal or is wildcard." + bc.getLabel() + "," + getLabel() );
            return false;
        }
        
        Vector<BioComponent> unmatched = new Vector<BioComponent>();
        BioComponent matched = null;
        
        Iterator tcomps_itr = getComponents().iterator();
        while( tcomps_itr.hasNext() )
        {
            unmatched.add( (BioComponent)tcomps_itr.next() );
        }
        
        
        Iterator comp_itr = bc.getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            BioComponent comp = (BioComponent)comp_itr.next();
            
            if ( comp.getState() != null )
            {
                if ( comp.getState().equals("*") )
                {
                    return false;
                }
            }
            
            Iterator tcomp_itr = unmatched.iterator();
            while ( tcomp_itr.hasNext() )
            {
                BioComponent tcomp = (BioComponent)tcomp_itr.next();
                
                // Check label
                if ( tcomp.getLabel().equals( comp.getLabel() ) )
                {
                    // Check state
                    if ( getComponentType( tcomp.getLabel() ).isValidState( comp.getState() ) )
                    {
                        if (debug_statements) System.out.println("Matched " + tcomp.getLabel() + " and " + comp.getLabel() );
                        matched = tcomp;
                        break; // break the loop and process the next component
                    }
                }
            }
            
            unmatched.remove( matched );
        }
        
        if ( unmatched.isEmpty() )
        {
            return true;
        }
        
        if (debug_statements) System.out.println("MoleculeType:isType(): " + unmatched.size() + " ComponentTypes were unmatched.");
        return false;
    }
    
    public boolean isMatchingPattern(BioContainer pattern) 
    {
        if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): comparing " + pattern.getLabel() + " and " + getLabel() );
        // Check that there are not more Components than ComponentTypes
        if ( pattern.getComponents().size() > getComponents().size() )
        {
            if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): more Components than ComponentTypes. " + getComponentTypes().size() + "," + pattern.getComponents().size() );
            if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): " + getComponents() );
            return false;
        }
        
        // Check the label. The pattern label must match or be a wildcard
        if ( !pattern.getLabel().equals( getLabel() ) && !pattern.getLabel().equals( "*" ) )
        {
            if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): labels not equal and pattern label is not a wildcard." + pattern.getLabel() + "," + getLabel() );
            return false;
        }
        
        // Create a copy of the Components vector to work with
        Vector<BioComponent> pattern_unmatched = new Vector<BioComponent>();
        Vector<BioComponent> type_unmatched = new Vector<BioComponent>();
        
        BioComponent pattern_matched = null;
        BioComponent type_matched = null;
        
        Iterator comp_itr = pattern.getComponents().iterator();
        while( comp_itr.hasNext() )
        {
            pattern_unmatched.add( (BioComponent)comp_itr.next() );
        }
        
        Iterator tcomp_itr = getComponents().iterator();
        while( tcomp_itr.hasNext() )
        {
            type_unmatched.add( (BioComponent)tcomp_itr.next() );
        }
        
        Vector<BioComponent> wildcards = new Vector<BioComponent>();
        
        // Process exact matches first and then try to apply wildcard matches after
        Iterator tcomps_itr = getComponents().iterator();
        while ( tcomps_itr.hasNext() )
        {
            BioComponent tcomp = (BioComponent)tcomps_itr.next();
            
            if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): Comparing componenttype " + tcomp.getLabel() + " and... " );
            
            Iterator bc_itr = pattern_unmatched.iterator();
            while ( bc_itr.hasNext() )
            {
                BioComponent bc = (BioComponent)bc_itr.next();
             
                if (debug_statements) System.out.println( "component: " + bc.getLabel() ); 
                
                // Check and skip wildcard components and process later
                if ( bc.getLabel().equals("*") )
                {
                    wildcards.add( bc );
                    continue;
                }
                
                // Check label
                if ( bc.getLabel().equals( tcomp.getLabel() ) )
                {
                    // Check state
                    if ( getComponentType( tcomp.getLabel() ).isValidState( bc.getState() ) )
                    {
                        if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): Matched " + tcomp.getLabel() + " and " + bc.getLabel() );
                        pattern_matched = bc;
                        type_matched = tcomp;
                        break; // break the loop and process the next component
                    }
                }
            }
            
            pattern_unmatched.remove( pattern_matched );
            type_unmatched.remove( type_matched );
        }
        
        // Repeat the process for wildcards using the unmatched componenttypes
        Iterator unmatched_itr = type_unmatched.iterator();
        while ( unmatched_itr.hasNext() )
        {
            BioComponent tcomp = (BioComponent) unmatched_itr.next();
            
            Iterator wildcard_itr = wildcards.iterator();
            while ( wildcard_itr.hasNext() )
            {
                BioComponent wildcard = (BioComponent) wildcard_itr.next();
               
                // Check states
                if ( getComponentType( tcomp.getLabel() ).isValidState( wildcard.getState() ) )
                {
                    pattern_matched = wildcard;
                    break;
                }
            }
            
            // Vector:remove(Object) only removes the first occurance - which 
            // is what we want
            wildcards.remove( pattern_matched );
            pattern_unmatched.remove( pattern_matched );
        }
            
        
        if ( pattern_unmatched.isEmpty() )
        {
            return true;
        }
        
        if (debug_statements) System.out.println("MoleculeType:isMatchingPattern(): " + pattern_unmatched.size() + " ComponentTypes were unmatched.");
        return false;
        
    }
    
}
