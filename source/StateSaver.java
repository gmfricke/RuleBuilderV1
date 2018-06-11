// This class is responsible for saving BioNetGen's state

import java.io.*; // For file manipulation and object writing
import java.util.*; // For vectors and arraylists
import java.util.logging.*; // For error logs
import javax.swing.*;
import java.awt.*; // For Dimension

import java.beans.XMLEncoder; // For XML Output

public class StateSaver
{
    transient protected boolean debug_statements = true;
    private GUI the_gui;
    private Model the_model;

    // Use Java's error log system
    //private static final Logger fLogger = Logger.getLogger(StateSaver.class.getPackage().getName());

    StateSaver( GUI the_gui, Model the_model )
    {
	this.the_gui = the_gui;
	this.the_model = the_model;
    }

    boolean save( File save_file )
    {
	
	ObjectOutput output = null;
	
	try{
            OutputStream file = new FileOutputStream( save_file );
	    OutputStream buffer = new BufferedOutputStream( file );
	    output = new ObjectOutputStream( buffer );
	    
            State state = packageState();
            saveState( output, state );
            the_gui.setSaveNeeded( false );
	}
	catch( NotSerializableException e )
	    {
		the_gui.getEditorPanel().displayError("Save Error",e.getMessage() + " in the Java object graph is not serializable. \nContact Support at support@bionetgen.com");
	    }
	catch(IOException ex)
	    {
		the_gui.getEditorPanel().displayError("State Save Exception Caught",
						   "Cannot output file. OutputStream.WriteObject Failed");
	    //fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
	    }
	finally
	    {
	    try 
		{
		    if (output != null) 
			{
			    //flush and close "output" and its underlying streams
			    output.close();
			}
		}
	    catch (IOException ex )
		{
		    // ++++++++++++++++++++++++++++++++++++++++++++
		    // Should write error method that displays an error box and writes the logger
		    the_gui.getEditorPanel().displayError("State Save Error","Cannot close output stream.");
		    //	fLogger.log(Level.SEVERE, "Cannot close output stream.", ex);
	    }
	    
	}
	
	return false;
    }

    boolean saveWidgetPanel( ObjectOutput output, WidgetPanel panel ) throws IOException
    {

        output.writeObject( (Vector<Species>)panel.getAllSpecies() );
        output.writeObject( (Vector<BioContainer>)panel.getAllContainers() );
        output.writeObject( (Vector<BioComponent>)panel.getAllComponents() );
        output.writeObject( (Vector<Edge>)panel.getAllEdges() );
        output.writeObject( (Vector<Operator>)panel.getAllOperators() );
        output.writeObject( (Vector<ReactionRule>)panel.getAllReactionRules() );
        output.writeObject( (Vector<Pattern>)panel.getAllPatterns() );
        
        output.writeInt( panel.getHeight() );
        output.writeInt( panel.getWidth() );
        output.writeInt( panel.getX() );
        output.writeInt( panel.getY() );
        Dimension dim = new Dimension( (int)panel.getWidth(), (int)panel.getHeight() );
        output.writeObject( dim );
        
        return true;
        
    }
    
    boolean saveMoleculePalette( ObjectOutput output, MoleculePalette palette ) throws IOException
    {
	saveWidgetPalette( output, palette );
        
	return true;
    }
    
    boolean saveSpeciesPalette( ObjectOutput output, SpeciesPalette p ) throws IOException
    {
	saveWidgetPalette( output, p );

	return true;
    }
    
    boolean saveReactionRulePalette( ObjectOutput output, ReactionRulePalette p ) throws IOException
    {
        saveWidgetPalette( output, p );

	return true;
    }
    
    
    boolean saveModel( ObjectOutput output, Model the_model ) throws IOException
    {
	try
	    {
		output.writeObject( the_model );
	    }
	catch( NotSerializableException e )
	    {
		the_gui.getEditorPanel().displayError("Model Save Error","Model not serializable. Contact Support at support@bionetgen.com");
	    }
            
        return true;
    }
    
    boolean saveCurrentID( ObjectOutput output ) throws IOException
    {
	try
	    {
		output.writeLong( (new IDGenerator()).getCurrentID() );
	    }
	catch( NotSerializableException e )
	    {
		the_gui.getEditorPanel().displayError("IDGenerator Save Error","IDGenerator not serializable. Contact Support at support@bionetgen.com");
	    }

        
	return true;
    }

    boolean saveJournal( ObjectOutput output, JTextArea jf ) throws IOException
    {
        
        output.writeObject( jf );
        
	return true;
    }
    
    void xmlSave( File save_file )
    {

	XMLEncoder e = null;
	try
	    {
		e = new XMLEncoder(new BufferedOutputStream( new FileOutputStream(save_file)));
		
                e.writeObject( the_gui.getEditorPanel().getAllContainers() );
            }
	catch(IOException ex)
	    {
		the_gui.getEditorPanel().displayError("XML State Save Error", ex.getMessage() );
		//fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
	    }

	e.close();
    }

    public void saveWidgetPalette( ObjectOutput output, WidgetPalette p ) throws IOException
    {
        saveWidgetPanel( output, p );
    }

    public State.ModelParametersState packageModelParams( State.ModelParametersState state, ModelParameters mp )
    {
        state.parameters = mp.getParameters();
        return state;
    }
    
    public State.SimulationConfigState packageSimConfig( State.SimulationConfigState state, SimulationConfig sc )
    {
	state.network_maxnummolecules = sc.getNetworkMaxnummolecules();
	state.network_maxnumruleapplications = sc.getNetworkMaxnumruleapplications();
	state.equilibration_timebetweenequilchecks = sc.getEquilibrationTimebetweenequilchecks();
	state.equilibration_maxnumequilchecks = sc.getEquilibrationMaxnumequilchecks();
	state.simulation_atol = sc.getSimulationATOL();
	state.simulation_rtol = sc.getSimulationRTOL();
	state.timecourse_nsteps = sc.getTimecourseNsteps();
	state.timecourse_tend = sc.getTimecourseTend();
	state.network_choosefromfiles = sc.getNetworkChooseFromFiles();
	state.network_generatenew = sc.getNetworkGenerateNew();
	state.equilibration_equilibrate = sc.getEquilibrationEquilibrate();
	state.equilibration_noequilibrate = sc.getEquilibrationNoEquilibrate();
	state.simulation_ODE = sc.getSimulationODE();
	state.simulation_SSA = sc.getSimulationSSA();
	state.timecourse_tendnsteps = sc.getTimecourseTendnsteps();
	state.timecourse_sampletimes = sc.getTimecourseSampletimes();
	state.network_isomorphism = sc.getNetworkIsomorphism();
	state.simulation_sparse = sc.getSimulationSparse();
	state.simulation_readnetfile = sc.getSimulationReadNetFile();
	state.options_verbose = sc.getOptionsVerbose();
	state.options_sbml = sc.getOptionsSBML();
	state.timecourse_sampletimes_values = sc.getTimecourseSampletimesvalues();
	state.network_maxstoichlimits_values = sc.getNetworkStoichLimitValues();
	state.equilibration_equilbooleans_values = sc.getEquilibrationBooleanValues();
        return state;
    }
    
    public State.WidgetPanelState packageWidgetPanel( State.WidgetPanelState state, WidgetPanel panel )
    {
       
        state.species = panel.getAllSpecies();
        state.containers = panel.getAllContainers();
        state.components = panel.getAllComponents();
        state.edges = panel.getAllEdges();
        state.operators = panel.getAllOperators();
        state.rules = panel.getAllReactionRules();
        state.patterns = panel.getAllPatterns();
        state.groups = panel.getAllGroups();
        state.flickr_labels = panel.getAllFlickrLabels();
        state.reactions = panel.getAllReactions();
        
        state.height = panel.getHeight();
        state.width = panel.getWidth();
        state.x = panel.getX();
        state.y = panel.getY();
        
        return state;
    }
    
    public State packageState() 
    {
            State state = new State();
            
            // Package Species Palette
            // Remove transients from last BNG run.
            the_gui.getSpeciesPalette().removeDerivedSpecies();
            the_gui.getSpeciesPalette().compressDisplay();
            the_gui.getReactionPalette().initialize();
            the_gui.getReactionPalette().repaint();
            
            state.species_panel_state = packageWidgetPanel(state.species_panel_state, the_gui.getSpeciesPalette());
            state.molecule_panel_state = packageWidgetPanel(state.molecule_panel_state, the_gui.getMoleculePalette());
            state.rule_panel_state = packageWidgetPanel(state.rule_panel_state, the_gui.getReactionRulePalette());
            state.editor_panel_state = packageWidgetPanel(state.editor_panel_state, the_gui.getEditorPanel());
            state.obser_panel_state = packageWidgetPanel(state.obser_panel_state, the_gui.getObservablesPalette());
            state.current_id = (new IDGenerator()).getCurrentID();
            state.simulation_config_state = packageSimConfig( state.simulation_config_state, the_gui.getSimulationConfig() );
            state.model_parameters_state = packageModelParams( state.model_parameters_state, the_gui.getModelParameters() );
            
            Model the_model = the_gui.getModel();

            state.model = the_model;

            return state;            
    }
    
    public boolean saveState(ObjectOutput output, State state) 
    {
        try
        {
            output.writeObject( state );
        }
        catch ( Exception e )
        {
            the_gui.getEditorPanel().displayError("Error Saving File", "The message was: " + e.getMessage() );
        }
            
        return true;
    }
    
}
