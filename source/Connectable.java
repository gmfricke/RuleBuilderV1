/*
 * Connectable.java
 *
 * Created on December 8, 2005, 10:46 AM
 */

import java.beans.*;
import java.io.Serializable;

import java.util.*; // For Vector
import java.awt.Point; // For Point

/**
 * @author matthew
 */
public interface Connectable
{
    boolean addEdge( Edge e );
    Vector<Edge> getEdges();
    Vector<Connectable> getNeighbors();
    boolean removeEdge( Edge e );
    boolean setEdges( Vector<Edge> v );
    Point getEdgeAttachPoint();
    int getHeight();
    int getWidth();
    boolean isSelected();
    void setSelected( boolean s );
    String getLabel();
    
}
