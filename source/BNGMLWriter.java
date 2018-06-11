/*
 * BNGMLWriter.java
 *
 * Created on December 16, 2005, 12:45 PM by Matthew Fricke
 */

/**
 *
 * @author  matthew
 */

import java.io.*; // for file
import java.util.*;

public interface BNGMLWriter 
{
    void writeModelToBNGML( File file );
    void writeSpeciesToBNGML( Species species, OutputStream xml_stream );
    void writeComponentToBNGML( BioComponent component, OutputStream xml_stream );
    void writeEdgeToBNGML( Edge edge, OutputStream xml_stream );
    void writeObservableToBNGML( Group group, OutputStream xml_stream );
    void writeBioGraphToBNGML( BioGraph graph, OutputStream xml_stream );
    void writeRuleToBNGML( ReactionRule rule, OutputStream xml_stream );
    void writeContainerToBNGML( BioContainer container, OutputStream xml_stream );
    void writePatternToBNGML( Pattern pattern, OutputStream xml_stream );
    void writeParametersToBNGML( Map parameters, OutputStream xml_stream );
}
