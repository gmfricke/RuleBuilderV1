/*
 * Mappable.java
 *
 * Created on December 8, 2005, 11:43 AM
 */

import java.beans.*;
import java.io.Serializable;

/**
 * @author matthew
 */
public interface Mappable extends Connectable
{
    AtomMap getAtomMap();
    boolean setAtomMap( AtomMap a );
}