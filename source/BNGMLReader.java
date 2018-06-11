/*
 * BNGMLReader.java
 *
 * Created on December 16, 2005, 12:45 PM by Matthew Fricke
 */

/**
 *
 * @author  matthew
 */
import java.util.*; // For Map
import java.io.*;

public interface BNGMLReader
{
    boolean readModelFromBNGML( File file );
    Species readSpeciesFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    BioComponent readComponentFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    Edge readEdgeFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    Group readObservableFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    BioGraph readBioGraphFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    ReactionRule readRuleFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    BioContainer readContainerFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    Pattern readPatternFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
    Map readParametersFromBNGML( InputStream xml_stream ) throws BNGMLFormatException;
}
