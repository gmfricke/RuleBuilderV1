

/*
 * State.java
 *
 * Created on September 28, 2005, 12:45 AM
 *
 * The State object holds all the savable data from RuleBuilder.
 * This should make saving and restoring state much easier since
 * it only involves one packaged object. Previouly the order objects
 * were saved to file in mattered and caused numerous problems.
 */

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author Matthew Fricke
 */
public class State implements Serializable 
{
    class WidgetPanelState implements Serializable
    {
        private static final long serialVersionUID = 1;
           
        //public SelectionBox the_selection_box;
        public Vector<BioComponent> components = new Vector<BioComponent>();
        public Vector<BioContainer> containers = new Vector<BioContainer>();
        public Vector<Edge> edges = new Vector<Edge>();
        public Vector<Operator> operators = new Vector<Operator>();
        public Vector<Species> species = new Vector<Species>();
        public Vector<ReactionRule> rules = new Vector<ReactionRule>();
        public Vector<Reaction> reactions = new Vector<Reaction>();
        public Vector<Pattern> patterns = new Vector<Pattern>();
        public Vector<Group> groups = new Vector<Group>();
        public Vector<FlickrLabel> flickr_labels = new Vector<FlickrLabel>();
        
        public int height;
        public int width;
        public int x;
        public int y;
    }
    
    
    class ModelParametersState implements Serializable
    {
        private static final long serialVersionUID = 1;
        public Vector<Parameter> parameters = new Vector<Parameter>();
    }
    
    class SimulationConfigState implements Serializable
    {
        private static final long serialVersionUID = 1;
        
	// textfields used in SimulationConfig
	public String network_maxnummolecules;
	public String network_maxnumruleapplications;
	public String equilibration_timebetweenequilchecks;
	public String equilibration_maxnumequilchecks;
	public String simulation_atol;
	public String simulation_rtol;
	public String timecourse_nsteps;
	public String timecourse_tend;
	
	// radiobuttons used in SimulationConfig
	public boolean network_choosefromfiles;
	public boolean network_generatenew;
	public boolean equilibration_equilibrate;
	public boolean equilibration_noequilibrate;
	public boolean simulation_ODE;
	public boolean simulation_SSA;
	public boolean timecourse_tendnsteps;
	public boolean timecourse_sampletimes;

	// checkboxes used in SimulationConfig
	public boolean network_isomorphism;
	public boolean simulation_sparse;
	public boolean simulation_readnetfile;
	public boolean options_verbose;
	public boolean options_sbml;

	// vectors used in SimulationConfig
	public Vector<String> timecourse_sampletimes_values = new Vector<String>();
	public Vector<Vector<Object>> network_maxstoichlimits_values = new Vector<Vector<Object>>();
	public Vector<Vector<Object>> equilibration_equilbooleans_values = new Vector<Vector<Object>>();

    }

    class SummaryPanelState implements Serializable
    {
        private static final long serialVersionUID = 1;
    }
    
    private static final long serialVersionUID = 1;
    
    public WidgetPanelState reaction_panel_state = new WidgetPanelState();
    public WidgetPanelState editor_panel_state = new WidgetPanelState();
    public WidgetPanelState rule_panel_state = new WidgetPanelState();
    public WidgetPanelState molecule_panel_state = new WidgetPanelState();
    public WidgetPanelState species_panel_state = new WidgetPanelState();
    public WidgetPanelState obser_panel_state = new WidgetPanelState();
    public SummaryPanelState summary_panel_state;
    public ModelParametersState model_parameters_state = new ModelParametersState();
    public Model model;
    public SimulationConfigState simulation_config_state = new SimulationConfigState();
    public long current_id;
    public long current_idmoleculetypes;
    public long current_idobservables;
    public long current_idparameters;
    public long current_idreactions;
    public long current_idrules;
    public long current_idspecies;
    
    /** Creates a new instance of State */
    public State() 
    {
    }
    
    
}
