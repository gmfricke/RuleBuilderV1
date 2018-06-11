import java.util.*; // For vector and parameter treemap
import java.io.Serializable; // So the model can be serialized
import java.io.*; // For file manipulation and string writing
import java.io.InputStreamReader;
import javax.swing.text.*; // For styled text documents

import java.awt.*; // For graphical windowing tools
//import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.util.regex.*;
import java.awt.event.*;

public class Model implements Serializable, Runnable, BNGMLWriter, BNGMLReader
{
    
    private class TermListener implements ActionListener {
            public void actionPerformed( ActionEvent e )
            {
                  terminaterunrequest = true;
                  proc.destroy();
            } 
    }
    transient private boolean terminaterunrequest = false;
    transient private Process proc;

    transient protected boolean debug_statements = false;   
    
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    transient private GUI the_gui; //Transient so StateLoader doesn't fail by trying to serialize the GUI
    
    private Vector<Species> species = new Vector<Species>();
    private Vector<Group> observables = new Vector<Group>();
    private Vector<ReactionRule> reaction_rules = new Vector<ReactionRule>();
    private Vector<MoleculeType> molecule_types = new Vector<MoleculeType>();
    
    private File bngl_output_file;    
    private File bngl_input_file;
    private String engine_command_path = new String();
    private String engine_arguments = new String();
   
    //private Vector component_types = new Vector();

    public IDMoleculeTypesGenerator idmoleculetypesgenerator;
    public IDObservablesGenerator idobservablesgenerator;
    public IDParametersGenerator idparametersgenerator;
    public IDReactionsGenerator idreactionsgenerator;
    public IDRulesGenerator idrulesgenerator;
    public IDSpeciesGenerator idspeciesgenerator;
    
    class BNGLReader implements Runnable
    {
        private BufferedReader buffered_reader; 
        private String file_name;
        private Thread thread;
        
        BNGLReader( BufferedReader br, String file_name )
        {
            this.buffered_reader = br;
        }
        
        public void setThread( Thread t )
        {
            thread = t;
        }
        
        public void run()
        {
           
            //readBNGL( buffered_reader, file_name );
            //thread.stop();
            
        }
    }
    
    private TreeMap<String,String> bngl_parameters = new TreeMap<String,String>();
     
    Model( GUI the_gui )
    {
	this.the_gui = the_gui;
	if ( the_gui == null )
	    {
		if (debug_statements) System.out.println("Fatal Error: GUI pointer null. Exiting...");
		System.exit(1);
	    }

	idmoleculetypesgenerator = new IDMoleculeTypesGenerator();
	idobservablesgenerator = new IDObservablesGenerator();
	idparametersgenerator = new IDParametersGenerator();
	idreactionsgenerator = new IDReactionsGenerator();
	idrulesgenerator = new IDRulesGenerator();
	idspeciesgenerator = new IDSpeciesGenerator();
    }

    void initialize()
    {
        //component_types.removeAllElements();
	species.removeAllElements();
        reaction_rules.removeAllElements();
        molecule_types.removeAllElements();
        observables.removeAllElements();

	long zeroreset = 0;
	idmoleculetypesgenerator.setCurrentIDMoleculeTypes(zeroreset);
	idobservablesgenerator.setCurrentIDObservables(zeroreset);
	idparametersgenerator.setCurrentIDParameters(zeroreset);
	idreactionsgenerator.setCurrentIDReactions(zeroreset);
	idrulesgenerator.setCurrentIDRules(zeroreset);
	idspeciesgenerator.setCurrentIDSpecies(zeroreset);
    }

    public long getIDMoleculeTypesGeneratorNext () {
	return idmoleculetypesgenerator.getNextIDMoleculeTypes();
    }

    public long getIDObervablesGeneratorNext () {
	return idobservablesgenerator.getNextIDObservables();
    }

    public long getIDParametersGeneratorNext () {
	return idparametersgenerator.getNextIDParameters();
    }

    public long getIDReactionsGeneratorNext () {
	return idreactionsgenerator.getNextIDReactions();
    }

    public long getIDRulesGeneratorNext () {
	return idrulesgenerator.getNextIDRules();
    }

    public long getIDSpeciesGeneratorNext () {
	return idspeciesgenerator.getNextIDSpecies();
    }

    public long getIDMoleculeTypesGeneratorCurrent() {
        return idmoleculetypesgenerator.getCurrentIDMoleculeTypes();
    }

    public long getIDObservablesGeneratorCurrent() {
        return idobservablesgenerator.getCurrentIDObservables();
    }

    public long getIDParametersGeneratorCurrent() {
        return idparametersgenerator.getCurrentIDParameters();
    }

    public long getIDReactionsGeneratorCurrent() {
        return idreactionsgenerator.getCurrentIDReactions();
    }

    public long getIDRulesGeneratorCurrent() {
        return idrulesgenerator.getCurrentIDRules();
    }

    public long getIDSpeciesGeneratorCurrent () {
        return idspeciesgenerator.getCurrentIDSpecies();
    }

    // Method for adding species to the model explicitly
    synchronized boolean addSpecies( Species s )
    {
        species.add( s );
        
        if (debug_statements) System.out.println( "There are now " + species.size() + " species in the model." );
        
        return true;
    }
    
    // Method for adding reaction rules to the model explicitly
    synchronized boolean addReactionRule( ReactionRule rr )
    {
        reaction_rules.add( rr );
        return true;
    }
    
    synchronized boolean removeReactionRule( ReactionRule rr )
    {
        reaction_rules.remove( rr );
        return true;
    }
    
    
    synchronized boolean removeSpecies( Species s )
    {
        species.remove( s );
        return true;
    }
    
    //synchronized boolean removeComponentType( ComponentType ct )
    //{
    //    component_types.remove( ct );
    //    return true;
    //}
    
    synchronized boolean removeMoleculeType( BioContainer mt )
    {
        molecule_types.remove( mt );
        return true;
    }
    
    
    synchronized void setGUI( GUI the_gui )
    {
	this.the_gui = the_gui;
    }

    synchronized public GUI getGUI()
    {
	return the_gui;
    }
    
    public void run() 
    {
        if (debug_statements) System.out.println("Model Thread Successfully Started: This thread does nothing at the moment.");
    }    
    
    synchronized public boolean update() throws BNGLOutputMalformedException
    {
        writeBNGL( bngl_output_file );
        
        if (debug_statements) System.out.println("Wrote output to " + bngl_output_file.getPath() );
        
        //String command_line = "/bin/ls /bin";
        
        // Clean up
        getGUI().getReactionPalette().initialize();
        getGUI().getSpeciesPalette().removeDerivedSpecies();
        getGUI().getSpeciesPalette().compressDisplay();
        getGUI().refreshAll();
        
        String bngl_output_path = bngl_output_file.getAbsolutePath();
        
        // Only quote paths under Windows since the OS X and Linux execs are unstable if "" are included
        String os_name = System.getProperty("os.name");
        if ( os_name.contains("Win") || os_name.contains("win") )
        {
            System.out.println("Running on " + os_name + " so don't quote");
            engine_command_path = "\""+engine_command_path+"\"";
            bngl_output_path = "\""+bngl_output_path+"\"";
        }
       
        String[] command = new String[2];
        command[0] = "perl"; 
        command[1] = engine_command_path + " " + bngl_output_path;

        String command_line = command[0] + " " + command[1];
        
        try 
        {
            
            // Setup a place in the GUI to display the output
            // might be better to place this in the GUI class and
            // call it in the other GUI thread later..
           
            JInternalFrame log_frame = the_gui.getEngineOutputLogFrame();
            log_frame.setSelected(true);
            log_frame.getContentPane().removeAll(); // Clear previous contents
            
            log_frame.setVisible(true);
            try
            {
                log_frame.setIcon(false);
            }
            catch (Exception veto)
            {
                veto.printStackTrace();
            }
            
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            final JTextPane log_pane = new JTextPane();
            log_pane.setEditable( false );
            log_pane.setBackground(Color.WHITE);
            log_pane.setForeground(Color.BLACK);
            log_pane.setAutoscrolls( true );
            
            JScrollPane log_scrollpane = new JScrollPane(log_pane);
            log_scrollpane.setVerticalScrollBarPolicy(
	    	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            log_scrollpane.setPreferredSize(new Dimension(800, 475));
    
            JButton term = new JButton("Terminate Run");
            
            panel.add( log_scrollpane, BorderLayout.CENTER );
            panel.add( term, BorderLayout.SOUTH );
            log_frame.getContentPane().add(panel);//(log_scrollpane, BorderLayout.CENTER);
       
            
            log_frame.setSize( 800, 560 );
            log_frame.setLocation( 75, 25 );
            log_frame.pack();
            log_frame.setVisible(true);
            
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setFontFamily(sas, "SansSerif");
            StyleConstants.setFontSize(sas, 13);
                   
            StyleConstants.setForeground(sas, Color.blue);
            StyledDocument styled_doc = log_pane.getStyledDocument();
            Position position = styled_doc.getEndPosition();
            int offset = position.getOffset();
            String first_line = "Please Wait. Calling " + command_line + "...\n\n";
            styled_doc.insertString(offset, first_line, sas );
           
            // any error messages?

            terminaterunrequest = false;
                      
            Runtime rt = Runtime.getRuntime();
          
            
            proc = rt.exec(command_line);
             
            term.addActionListener( new TermListener()
                                    );
            
            //proc.wait();
            
            StreamGobbler error_gobbler = new StreamGobbler(proc.getErrorStream(), "ERROR", log_pane);            
            
            // any output?
            StreamGobbler output_gobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT", log_pane);
            
            //SwingUtilities.invokeLater(output_gobbler);
            //SwingUtilities.invokeLater(error_gobbler);
            output_gobbler.start();
            error_gobbler.start();
              
            
            //int exitVal = proc.waitFor();
            //if (debug_statements) System.out.println("Process exit value: " + exitVal);
        
            // Read in the results
	    // determine the cdat and gdat file names
	    
            // Spawn a new thread so we don't block the swing event thread
            Runnable waiter = new Runnable() 
            {
                public void run() 
                {
                    try
                    {
                        int exitVal = proc.waitFor();
			if ( exitVal != 0) {
				if ( terminaterunrequest ) {
                            		//getGUI().getEditorPanel().displayInformation("Run Terminated","Run termination at user request.");
				        SimpleAttributeSet normal_sas = new SimpleAttributeSet();
            				StyleConstants.setFontFamily(normal_sas, "SansSerif");
            				StyleConstants.setFontSize(normal_sas, 13);
            				StyleConstants.setForeground(normal_sas, Color.blue);
            				String text = "\n\nRun termination at user request.";
					StyledDocument styled_doc = log_pane.getStyledDocument();
					Position position = styled_doc.getEndPosition();
            				int offset = position.getOffset();
            				styled_doc.insertString(offset, text, normal_sas );
				}
                        	else {
                            		getGUI().getEditorPanel().displayError("BNG2 Returned an Error ("+exitVal+")","Review the BNG Log for more information.");
                        	}
			}
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                    
                    String path = bngl_output_file.getAbsolutePath();
                    path = path.replaceAll("\\.bngl$","");
                    if (debug_statements) System.out.println( "cdat+gdat path:" + path );
                    
                    the_gui.getPlotter().readDATFiles(path);
                    
                    Runnable plotter_thread = new Runnable() 
                    {
                        public void run() 
                        {
                            the_gui.getPlotter().plot();
			    if (the_gui.getSimulationConfig().getSimulationReadNetFile()) { 
	                        if (debug_statements) System.out.println("Model: user elected to read net file back in");
                                readNetwork();
                            }
			    else {
				if (debug_statements) System.out.println("Model: user elected NOT to read net file back in");
			    }
                        }
                    };
                    SwingUtilities.invokeLater(plotter_thread);
                    
                    
                }
            };
            Thread waiting_thread = new Thread( waiter  );
            waiting_thread.start();
        }
        catch ( IOException io )
        {
            getGUI().getEditorPanel().displayError("Unable to Create BNG2 Process", "There was an IO exception. Check that perl is installed and that you are able to run it from the command line.");
        }
        catch ( Throwable t )
        {
            the_gui.getEditorPanel().displayError("Exception caught while trying to execute " + command_line, "Exception message was " + t.getMessage() );
            t.printStackTrace();
            return false;
        }
	
        return true;
    }
    
    synchronized public boolean addMolecule(BioContainer molecule) 
    {
        return false;
    }
    
    public synchronized void setOutputBNGLFile(File output_file) 
    {
        bngl_output_file = output_file;
    }  
    
    public synchronized void setInputBNGLFile(File input_file) 
    {
        bngl_input_file = input_file;
    }  
    
    public synchronized void setupEngine(String command_path, String args ) 
    {
        engine_command_path = command_path;
        engine_arguments = args;
    }    
    
    public synchronized void setEngineArguments(String s) 
    {
        this.engine_arguments = s;
    }    
    
    
    public synchronized Vector<Species> getSpecies() 
    {
        return species;
    }
    
    public Species getSpecies(String label) 
    {
        Iterator<Species> itr = getSpecies().iterator();
        while( itr.hasNext() )
        {
            Species current = itr.next();
            if ( label.equals( current.getLabel() ) )
            {
                return current;
            }
        }
        
        return null;
    }
    
    public ReactionRule getRule(String label) 
    {
        Iterator<ReactionRule> itr = getRules().iterator();
        while( itr.hasNext() )
        {
            ReactionRule current = itr.next();
            if ( label.equals( current.getLabel() ) )
            {
                return current;
            }
        }
        
        return null;
    }
    
    public synchronized Vector<ReactionRule> getReactionRules() 
    {
        return reaction_rules;
    }
    
    public synchronized Vector<ReactionRule> getRules() 
    {
        return reaction_rules;
    }
    
    public synchronized Map getBNGLParameters() 
    {
        return bngl_parameters;
    }
        
    
    public boolean writeBNGL( File save_file ) throws BNGLOutputMalformedException
    {
        
        OutputStreamWriter output = null;
	try{
	    //use buffering
	    OutputStream output_stream = new FileOutputStream( save_file );
	    output = new OutputStreamWriter( output_stream );
            
            try
	    {
		output.write("begin parameters\n");
                
                ModelParameters model_params = the_gui.getModelParameters();
                
                Iterator param_itr = model_params.getParameters().iterator(); 
                while ( param_itr.hasNext() )
                {
                    Parameter param = (Parameter)param_itr.next();
                    output.write( param.getKey() + " " + param.getValue() + "\n" );
                }
                
                // Legacy support for previous species that had a hard number instead of
                // a variable name.
                Iterator s_itr = species.iterator();
                while ( s_itr.hasNext() )
                {
                    Species current_species = (Species) s_itr.next();
                    if ( current_species.getConcentrationName() == null )
                    {
                        output.write( current_species.getConcentrationName()+" "+ current_species.getConcentration() +"\n" ); 
                    }
                }
                   
                
                Iterator rrule_itr = reaction_rules.iterator();
                while ( rrule_itr.hasNext() )
                {
                    ReactionRule current_rule = (ReactionRule) rrule_itr.next();
                    
                    
                    if ( current_rule.isReversable() )
                    {
                        if ( current_rule.getForwardRateName() == null || current_rule.getReverseRateName() == null )
                        {
                            output.write( current_rule.getForwardRateName() +" "+ current_rule.getForwardRate() +"\n" );
                            output.write( current_rule.getReverseRateName() +" "+ current_rule.getReverseRate() +"\n" );
                        }
                    }  
                    else
                    {
                        if ( current_rule.getForwardRateName() == null )
                        {
                            output.write( current_rule.getForwardRateName() +" "+ current_rule.getForwardRate() +"\n" );       
                        }
                    }
                }
                
                output.write("end parameters\n\n");
                
                output.write("begin molecule types\n");
                Iterator<MoleculeType> types_itr = getMoleculeTypes().iterator();
                Integer index = 0;
                while( types_itr.hasNext() )
                {
                    index++;
                    output.write(index.toString());
                    MoleculeType mt = types_itr.next();
                    output.write( " " + mt.getLabel() );
                    output.write( "(" );
                    
                    Iterator<BioComponent> component_itr = mt.getComponents().iterator();
                    
                    if( component_itr.hasNext() )
                    {
                        ComponentType ct = component_itr.next().getType();
                        output.write( ct.getLabel() );
                        writeAllowedStatesToBNGL( ct, output );
                    }
                    
                    while( component_itr.hasNext() )
                    {
                        ComponentType ct = component_itr.next().getType();
                        output.write( "," + ct.getLabel() );
                        writeAllowedStatesToBNGL( ct, output );
                    }
                    
                    output.write(")\n");
                }
                
                output.write("end molecule types\n\n");
                 
                output.write("begin seed species\n");
                
                if (debug_statements) System.out.println("There are " + species.size() + " species to write." );
                
                Iterator species_itr = species.iterator();
                while ( species_itr.hasNext() )
                {
                    Species current_species = (Species) species_itr.next();
                 
                    output.write( current_species.getLabel() + ": " );
                    
                    writeSpeciesToBNGL( current_species, output );
                    
                        output.write( " "+current_species.getConcentrationName() );
                    
                    output.write("\n");
                }
                
                output.write("end seed species\n\n");
                
               
                if ( getGUI().getConfig().getEngineVersion().equals("1.x") )
                {
                    output.write("begin reaction_rules\n");
                }
                else
                {
                    output.write("begin reaction rules\n");
                }
                
                Iterator reaction_rule_itr = reaction_rules.iterator();
                
                
                
                while ( reaction_rule_itr.hasNext() )
                {
                    ReactionRule current_rule = (ReactionRule) reaction_rule_itr.next();
                    
                    output.write( current_rule.getLabel() + ": " );
                    
                    Iterator reactants_itr = current_rule.getReactants().iterator();
                    if (reactants_itr.hasNext() ) 
                    {
                        writePatternToBNGL( (Pattern)reactants_itr.next(), output );
                    }
                    
                    while( reactants_itr.hasNext() )
                    {
                        output.write( "+" );
                        writePatternToBNGL( (Pattern)reactants_itr.next(), output );  
                    }
                    
                    if ( current_rule.isReversable() )
                    {
                        output.write("<->");
                    }
                    else
                    {
                        output.write("->");
                    }
                    
                    Iterator products_itr = current_rule.getProducts().iterator();
                    if (products_itr.hasNext() ) 
                    {
                        writePatternToBNGL( (Pattern)products_itr.next(), output );
                    }
                    
                    while( products_itr.hasNext() )
                    {
                        output.write( "+" );
                        writePatternToBNGL( (Pattern)products_itr.next(), output );  
                    }
                    
                    if ( current_rule.isReversable() )
                    {
                            output.write( " "+current_rule.getForwardRateName() );
                            output.write( ","+current_rule.getReverseRateName() );             
                    }
                    else
                    {
                        output.write( " "+current_rule.getForwardRateName() );
                    }
                    
                    if ( current_rule.getAnnotation() != null )
                    {
                        output.write(" " + current_rule.getAnnotation() );
                    }
                    
                    output.write("\n");
                }
                
                 if ( getGUI().getConfig().getEngineVersion().equals("1.x") )
                {
                    output.write("end reaction_rules\n\n");
                }
                else
                {
                    output.write("end reaction rules\n\n");
                }
                
                // Write observables
                output.write("begin observables\n");
                
                Iterator ob_itr = getObservables().iterator();
                while ( ob_itr.hasNext() )
                {
                    Group ob = (Group)ob_itr.next();
                    output.write( ob.getType() + " ");
                    output.write( ob.getLabel() + " ");
                    
                    Iterator<Pattern> pattern_itr = ob.getPatterns().iterator();
                    while ( pattern_itr.hasNext() )
                    {
                        Pattern pattern = (Pattern)pattern_itr.next();
                        writePatternToBNGL( pattern, output );
                        output.write(" ");
                    }
                    
                    output.write( "\n" );
                   
                }
                
                output.write("end observables\n\n");
                
                SimulationConfig sc = getGUI().getSimulationConfig();
                    
                if ( getGUI().getConfig().getEngineVersion().equals("1.x") )
                {
                    output.write("begin control_sim\n");
                    //if ( sc.getSampleTimes().length() != 0 ) output.write( "@sample_times=("+sc.getSampleTimes()+")\n" );
                    //if ( sc.getNumberOfSteps().length() != 0 ) output.write( "$n_steps="+sc.getNumberOfSteps() +"\n" );
                    //if ( sc.getStepLength().length() != 0 ) output.write( "$step_length="+sc.getStepLength() +"\n" );
                    //if ( sc.getMaxIterations().length() != 0 ) output.write( "$MAX_ITER="+sc.getMaxIterations() +"\n" );
                    //if ( sc.getMaxAggregation().length() != 0 ) output.write( "$MAX_AGG="+sc.getMaxAggregation() +"\n" );
                    //if ( sc.getAbsoluteErrorTolerance().length() != 0 ) output.write( "$atol_integ="+sc.getAbsoluteErrorTolerance() +"\n" );
                    //if ( sc.getRelativeErrorTolerance().length() != 0 ) output.write( "$atol_integ="+sc.getRelativeErrorTolerance() +"\n" );
                    //if ( sc.getUpdateInterval().length() != 0 ) output.write( "$SSA_UPDATE_INTERVAL="+sc.getUpdateInterval() +"\n" );
                    //if ( sc.getNumberOfRuns().length() != 0 ) output.write( "$N_RUNS="+sc.getNumberOfRuns() +"\n" );
                    //if ( sc.getFlyItrInit().length() != 0 ) output.write( "$FLY_ITER_INIT="+sc.getFlyItrInit() +"\n" );
                    
                    output.write("end control_sim\n"); 
                }
                else {

		    	// tie printiter, overwrite and verbose to option debug on, write all if option debug
                    	// is selected, else default to off for each of these

                    	if ( sc.getNetworkChooseFromFiles())  {
                    	}
                    	else {
                        	Vector<Vector<Object>> maxstoichlimits = sc.getNetworkStoichLimitValues();
				String maxstoich = "";
				if (maxstoichlimits.size() > 0) {
					String maxstoich_start = "max_stoich=>{";
					String maxstoich_end = "}";
					StringBuffer maxstoich_values = new StringBuffer("");
                        		Iterator<Vector<Object>> maxstoichrowiter = maxstoichlimits.iterator();
                        		Vector<Object> maxstoichthisrow;
                        		Iterator<Object> maxstoichcoliter;
					int hack = 0;
                        		while (maxstoichrowiter.hasNext()) {
                                		maxstoichthisrow = maxstoichrowiter.next();
                                		maxstoichcoliter = maxstoichthisrow.iterator();
                                		while (maxstoichcoliter.hasNext()) {
                                        		Object current = maxstoichcoliter.next();
                                        		String currentstring = current.toString();
							String newcurrentstring = new String();
							if (currentstring == "unlimited") {
								newcurrentstring = "1e10";
							}
							else {
								newcurrentstring = currentstring;
							}
                                        		if (debug_statements) System.out.println("Model max stoich value " + currentstring);
							if (hack % 2 == 0) {
								maxstoich_values.append(currentstring + "=>");
							}
							else {
								maxstoich_values.append(newcurrentstring + ",");
							}
							++hack;
                                		}       
                        		}
					int maxstoichvalueslength = maxstoich_values.length();
					maxstoich_values.deleteCharAt(maxstoichvalueslength - 1);
					String revisedmaxstoichvalues = maxstoich_values.toString();
					maxstoich = maxstoich_start + revisedmaxstoichvalues + maxstoich_end;
					if (debug_statements) System.out.println("Model build up of max stoich string = " + maxstoich);
				}
				else {
					if (debug_statements) System.out.println("sim config error:  gen new network selected, no maxstoich string available");
				}   

				String netiso;
				if (sc.getNetworkIsomorphism()) 
					netiso = "check_iso=>1,";
				else
					netiso = "check_iso=>0,";
				String maxagg = "max_agg=>"+sc.getNetworkMaxnummolecules()+",";
				String maxiter = "max_iter=>"+sc.getNetworkMaxnumruleapplications()+",";
				String printiter = "print_iter=>0,";
				String overwrite = "overwrite=>1,";
				String verbose = "verbose=>0";
                                
                                // BNG uses "on-the-fly" when max_iter is set to a low number"
                                // omiting the generate_network command to indicate on-the-fly is broken
                                // Jim Faeder is working on a fix
                                if ( !sc.getPreGenerate() )
                                {
                                    maxiter = "max_iter=>1,";
                                }
                                
                                output.write("generate_network({overwrite=>1,"+netiso+maxiter+maxstoich+"});\n\n");
                        }

			if (sc.getEquilibrationEquilibrate()) { // user asked for equilibration to be performed

				Species cs;
                        	Vector<Vector<Object>> equilbooleans = sc.getEquilibrationBooleanValues();
                        	Iterator<Vector<Object>> equilbooleansrowiter1 = equilbooleans.iterator();
                        	Iterator<Vector<Object>> equilbooleansrowiter2 = equilbooleans.iterator();
                        	Vector<Object> equilbooleansthisrow;
                        	Iterator<Object> equilbooleanscoliter;

				// zero out species concentrations
				int equilindex = 0;
                        	while (equilbooleansrowiter1.hasNext()) {
                                	equilbooleansthisrow = equilbooleansrowiter1.next();
					String specieslabelstring = equilbooleansthisrow.elementAt(0).toString();
					String speciesboolstring = equilbooleansthisrow.elementAt(1).toString();
					if (debug_statements) System.out.println("model notes equilboolean of " + specieslabelstring + " val is " + speciesboolstring);
					Integer equilindexinteger = new Integer(equilindex);
					String equilindexstring = equilindexinteger.toString();
					if (debug_statements) System.out.println("model notes equilindex of " + equilindexstring);
					if (speciesboolstring.equals("false")) {
						cs = species.elementAt(equilindex);
						output.write("setConcentration(\"");
						writeBioGraphToBNGL( (BioGraph)cs, output );
						output.write("\",0);\n");
					}
					++equilindex;
				}

				String suffix = "suffix=>\"equil\"";
				String maxtime = "t_end=>"+sc.getEquilibrationTimebetweenequilchecks();
				String steps = "n_steps=>"+sc.getEquilibrationMaxnumequilchecks();
                        	String atol = "atol=>"+sc.getSimulationATOL();
                        	String rtol = "rtol=>"+sc.getSimulationRTOL();
				String sparse = "sparse=>1";
				String steadystate = "steady_state=>1";
                    	    	output.write( "simulate_ode({"+suffix+","+maxtime+","+steps+","+atol+","+rtol+","+sparse+","+steadystate+"});\n\n");

				// restore species concentrations 
				//    quick and dirty way; better: revise writeBioGraphToBNGL to 
				//    allow write to a stringbuffer, not just the outputstream, can
				//    then fill a stringbuffer in the above loop and avoid this 
				//    repeated code
				equilindex = 0;
                        	while (equilbooleansrowiter2.hasNext()) {
                                	equilbooleansthisrow = equilbooleansrowiter2.next();
					String speciesboolstring = equilbooleansthisrow.elementAt(1).toString();
					if (speciesboolstring.equals("false")) {
						cs = species.elementAt(equilindex);
						output.write("setConcentration(\"");
						writeBioGraphToBNGL((BioGraph)cs,output);	
						output.write("\",\""+cs.getConcentrationName()+"\");\n");	
					}
					++equilindex;
				}

			}
	
		    	if (sc.getTimecourseTendnsteps()) {

		        	// sim time settings apply to both ODE and SSA

                        	String t_end = "t_end=>" + sc.getTimecourseTend();
                        	String n_steps = "n_steps=>" + sc.getTimecourseNsteps();
                        	String atol = "atol=>"+sc.getSimulationATOL();
                        	String rtol = "rtol=>"+sc.getSimulationRTOL();
				String sparse;
				if (sc.getSimulationSparse()) {
					sparse = "sparse=>1";
				}
				else {
					sparse = "sparse=>0";
				}
                        	if ( sc.getSimulationODE() ) 
                    	    		output.write( "\nsimulate_ode({"+t_end+","+n_steps+","+atol+","+rtol+","+sparse+"});");
		        	else 
                    	    		output.write( "\nsimulate_ssa({"+t_end+","+n_steps+"});");
				output.write("\n\n");
                    	}
                    	else {  // user asked for sample times instead
                 
				String sample_start = "sample_times=>[";
				String sample_end = "]";
				StringBuffer sample_times = new StringBuffer("");
                        	Vector<String> stimes = sc.getTimecourseSampletimesvalues();
				String sampletimes;
				// protect ourselves from accessing a null or empty vector in case we've screwed up somehow
				if (stimes.size() > 0) {
                       	 		Enumeration e = stimes.elements();
					while (e.hasMoreElements()) 
			    			sample_times.append((String)e.nextElement() + ",");
					String numvalues = sample_times.toString();
					int numvalueslength = numvalues.length();
					sample_times.deleteCharAt(numvalueslength - 1);
					String revisednumvalues = sample_times.toString();
	                		sampletimes = sample_start + revisednumvalues + sample_end;
                        		if (debug_statements) System.out.println(sampletimes);
				}
				else {
					sampletimes = "";
				}
                        	String atol = "atol=>"+sc.getSimulationATOL();
                        	String rtol = "rtol=>"+sc.getSimulationRTOL();
				String sparse;
				if (sc.getSimulationSparse()) {
					sparse = "sparse=>1";
				}
				else {
					sparse = "sparse=>0";
				}
                        	if ( sc.getSimulationODE() ) 
                    	    		output.write( "\nsimulate_ode({"+sampletimes+","+atol+","+rtol+","+sparse+"});");
		        	else 
                    	    		output.write( "\nsimulate_ssa({"+sampletimes+"});");
				output.write("\n\n");
		        }
                             
		    	if ( sc.getOptionsSBML() ) 
	                	output.write("\nwriteSBML({suffix=>\"sbml\"});");
                    }

                return true;
            }
            catch(IOException ex)
	    {
		the_gui.getEditorPanel().displayError("BNGL Output Error",
						   "Cannot output file. IO Failure. Check the BNGL Output Path in Settings");
	    //fLogger.log(Level.SEVERE, "Cannot perform output.", ex);
	    }
            catch ( ClassCastException cce )
            {
                cce.printStackTrace();
                the_gui.getEditorPanel().displayError("BNGL Output Exception Caught", "Attempt to cast an Object into the wrong class.\n" +
                "Please contact support at support@bionetgen.com" );
            }
            catch ( BNGLOutputMalformedException bom )
            {
                throw bom;
            }
            catch ( Exception e )
            {
                e.printStackTrace();
                the_gui.getEditorPanel().displayError("BNGL Output Exception Caught", "Exception message was " + e.getMessage()
                + "Contact support at support@bionetgen.com");
            }

	}
	 catch ( FileNotFoundException fnfe )
            {
                the_gui.getEditorPanel().displayError("BNGL Output Error",
						   "Path not found. Check the BNGL Output Path in Settings.");
	    //fL
            }
        catch ( Exception e )
            {
                e.printStackTrace();
                the_gui.getEditorPanel().displayError("BNGL Output Exception Caught", "Exception message was " + e.getMessage()
                + "Contact support at support@bionetgen.com");
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
		    the_gui.getEditorPanel().displayError("BNGL Output Error","Cannot close output stream.");
		    //	fLogger.log(Level.SEVERE, "Cannot close output stream.", ex);
	    }
	    
	}
	
	return false;
    }

    private void writePatternToBNGL( Pattern p, OutputStreamWriter output ) throws IOException, BNGLOutputMalformedException
    {
        writeBioGraphToBNGL( (BioGraph)p, output );
    }
    
    private void writeSpeciesToBNGL( Species s, OutputStreamWriter output ) throws IOException, BNGLOutputMalformedException
    {
        if (debug_statements) System.out.println("Writing species to BNGL from the " + species.size() + " species in the model.");
        
        writeBioGraphToBNGL( (BioGraph)s, output );
    }
    
    private void writeBioGraphToBNGL( BioGraph s, OutputStreamWriter output ) throws IOException, BNGLOutputMalformedException
    {
        
        Iterator container_itr = s.getContainers().iterator();
        if ( container_itr.hasNext() )
        {
            BioContainer current_container = (BioContainer) container_itr.next();
            
            // Sort the containers components so that containers of the same type
            // have their components printed in a consistent order.
            current_container.sortComponents();
            
            String l = current_container.getLabel();
            l = l.replaceAll("\\s","_");
            if (debug_statements) System.out.println("Replaced Label: \"" + current_container.getLabel() + "\" with \"" + l + "\"");
            output.write( l );
            
            //Write map label
            if ( current_container.getAtomMap() != null )
            {
                output.write("%"+current_container.getAtomMap().getLabel());
            }
            
                        output.write("(");
                    
                        Iterator component_itr = current_container.getComponents().iterator();
                        if ( component_itr.hasNext() )
                        {
                            BioComponent current_component = (BioComponent)component_itr.next();
                            writeComponentToBNGL( current_component, s, output );
                            
                            
                         }
                        while( component_itr.hasNext() )
                        {
                            output.write(",");
                            BioComponent current_component = (BioComponent)component_itr.next();
                            writeComponentToBNGL( current_component, s, output );
                        }          
                        
                        output.write(")");
        }
        
        while ( container_itr.hasNext() )
        {
            output.write(".");
            BioContainer current_container = (BioContainer) container_itr.next();
            output.write( current_container.getLabel() );
                        output.write("(");
                    
                        Iterator component_itr = current_container.getComponents().iterator();
                        if ( component_itr.hasNext() )
                        {
                            BioComponent current_component = (BioComponent)component_itr.next();
                            
                            writeComponentToBNGL( current_component, s, output );
                        }
                        while( component_itr.hasNext() )
                        {
                            output.write(",");
                            BioComponent current_component = (BioComponent)component_itr.next();
                            writeComponentToBNGL( current_component, s, output );
                        }
                        
                        
                        output.write(")");
        }
    }
    
    /**
     * 
     * @param input_file 
     * @return 
     */
    synchronized public boolean readBNGL( File input_file )
    {
        final File the_file = input_file;
        final String file_name = input_file.getName();
            try
            {
                if (debug_statements) System.out.println("Reading file " + file_name );
       
            // set up stream and reader filter sequence    
      FileInputStream fileIn = new FileInputStream ( input_file );
      final ProgressMonitorInputStream in
         = new ProgressMonitorInputStream ( the_gui.getEditorPanel(),
            "Reading " + input_file.getName (), fileIn);
      //final BufferedReader in = new BufferedReader (inReader);
            in.getProgressMonitor().setMillisToPopup(0);
            
            
        Thread ParseBNGLThread = new Thread() 
        {
                public void run() 
                {
                    try
                    {
                        //InputStreamReader insr = new InputStreamReader (in);
                        //BufferedReader inbr = new BufferedReader (in);
                        
                        parseBNGL( in );
                        
                        /*
                        String line;
                        
                        while ( (line = readLine( in ) ) != null )
                        {
                            System.out.println("Read \""+line+"\". Sleeping for 1/100 of a second");
                            Thread.currentThread().sleep(10);
                        }
                         */
                        in.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
        };
        ParseBNGLThread.setDaemon(true); // So the thread wont stop RuleBuilder from closing.
        ParseBNGLThread.start();
       
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return true;
    }
    
    // This is the method the thread calls
    synchronized public boolean parseBNGL( ProgressMonitorInputStream in ) 
    {
        bngl_parameters = new TreeMap<String,String>();
       
        try
        {
            if (debug_statements) System.out.println("Parsing BNGL");
            
            String line = null;
            
            in.getProgressMonitor().setNote( "Loading Parameters" );
                    
            // Parameters
            boolean in_params_block = false;
            while ( (line = readLine( in ) ) != null )
            {
             if (debug_statements) System.out.println("Read: "+line);
             
             
             
             if ( line.matches("(?i)^\\s*$") )
             {
                 if (debug_statements) System.out.println( "Skipping blank line.");
                 continue;
             }
             else if ( line.matches("(?i).*#.*") ) 
             {
                 if (debug_statements) System.out.println( "Skipping comment line: " + line );
                 continue;
             }
             else if ( line.matches( "begin parameters" ) ) 
             {
                 if (debug_statements) System.out.println( "Found start of Parameter block.");
              
                 in_params_block = true;
                 
                 continue;
             }
             else if ( line.matches( "end parameters" ) ) 
             {
                 if (debug_statements) System.out.println( "Found end of Parameter block.");
                 in_params_block = false;
                 break;
             }
             
             
                if (debug_statements) System.out.println( "Processing Parameter: " + line );
             
                if ( in_params_block )
                {
                    
                    java.util.regex.Pattern param_pattern = java.util.regex.Pattern.compile("(\\w+)\\s+([\\d\\.e\\-\\+]+)");
                    Matcher name_value_fit = param_pattern.matcher( line );
                
                    // Handle "\" line splits
                    line = concatLines( line, in );
                  
                    
                    if ( name_value_fit.find() )
                    {
                        String name = name_value_fit.group(1);
                        String value = name_value_fit.group(2);
                        if (debug_statements) System.out.println("Broke the parameter up into a name ("+name+") and a value (" + value + ")");
                        bngl_parameters.put( name, value );
                    }
                    else
                    {
                    // Malformed parameter expression
                    }
                }
            }
            
            // Print the parameters
            if (debug_statements) 
            {
                    Iterator param_itr = bngl_parameters.values().iterator();
            
                    while ( param_itr.hasNext() )
                    {
                        System.out.println("Value: " + (String) param_itr.next() );
                    }
            }
            
            // species
            in.getProgressMonitor().setNote( "Loading Species" );
            boolean in_species_block = false;
            while ( (line = readLine( in ) ) != null )
            {
             
            
                
             if ( line.matches("(?i)^\\s*$") ) 
             {
                 if (debug_statements) System.out.println( "Skipping blank line.");
                 continue;
             }
             else if ( line.matches("(?i).*#.*") ) 
             {
                 if (debug_statements) System.out.println( "Skipping comment line: " + line );
                 continue;
             }
             else if ( line.matches( "begin species" ) | line.matches ( "begin seed species") )
             {
                 if (debug_statements) System.out.println( "Found start of Species block.");
                 in_species_block = true;
                 continue;
             }
             else if ( line.matches( "end species" ) | line.matches ("end seed species") )
             {
                 if (debug_statements) System.out.println( "Found end of Species block.");
                 break;
             } 
             
                if (debug_statements) System.out.println( "Processing Species: " + line );
                
             // handle lines split with "\"
             line = concatLines( line, in );
             
             
             if ( in_species_block )
             {
                Species s = readSpeciesFromBNGL( line );
                s.setVisible(false);
                getGUI().getSpeciesPalette().getAllSpecies().add(s);
                addSpecies(s);
             }
            }
            
            // Make species visible
            in.getProgressMonitor().setNote( "Displaying " + getGUI().getSpeciesPalette().getAllSpecies().size() + " Species" );
            
            getGUI().getSpeciesPalette().compressDisplay();
            Iterator<Species> species_itr = getGUI().getSpeciesPalette().getAllSpecies().iterator();
            while ( species_itr.hasNext() )
            {    
                species_itr.next().setVisible(true);
            }
            
            // rules
            in.getProgressMonitor().setNote( "Loading Rules" );
            boolean in_rules_block = false;
            while ( (line = readLine( in ) ) != null )
            {
                
             if ( line.matches("(?i)^\\S*$") ) 
             {
                 if (debug_statements) System.out.println( "Skipping blank line.");
                 continue;
             }
             else if ( line.matches("(?i).*#.*") ) 
             {
                 if (debug_statements) System.out.println( "Skipping comment line: " + line );
                 continue;
             }
             else if ( line.matches( "begin reaction_rules" ) || line.matches( "begin reaction rules" ) )
             {
                 if (debug_statements) System.out.println( "Found start of Reaction Rule block.");
                 in_rules_block = true;
                 continue;
             }
             else if ( line.matches( "end reaction_rules" ) || line.matches( "end reaction rules" ) ) 
             {
                 if (debug_statements) System.out.println( "Found end of Reaction Rule block.");
                 break;
             }
             else if ( line.matches("^\\s*[\n\r]") ) 
             {
                 if (debug_statements) System.out.println("Skipping blank line.");
                 continue; // File may contain blacnk lines
             }
             
             // handle lines split with "\"
             line = concatLines( line, in );
             
             
             /*
             while ( line.matches(".*\\\\.*") )
             {
                 System.out.println("Concat \\ split lines. Before: " + line );
                 String next = br.readLine();
                 System.out.println("next line: " + next + " has length " + next.length() );
                 line = line.replaceAll("\\\\",next);
                 System.out.println("After: " + line );
             }
             */
             
             if (debug_statements) System.out.println( "Processing Reaction Rule: " + line );
                
             if ( in_rules_block )
             {
                ReactionRule rr = readReactionRuleFromBNGL( line );
                rr.setVisible(false);
                //getGUI().getReactionRulePalette().addReactionRule( rr ); // dont use this for speed's sake
                getGUI().getReactionRulePalette().getAllReactionRules().add(rr);  
                addReactionRule(rr);
             }
            
            }
            
            
            in.getProgressMonitor().setNote( "Displaying " + getGUI().getReactionRulePalette().getAllReactionRules().size() + " Rules" );
            
            getGUI().getReactionRulePalette().compressDisplay();
            Iterator<ReactionRule> itr = getGUI().getReactionRulePalette().getAllReactionRules().iterator();
            while ( itr.hasNext() )
            {
                itr.next().setVisible(true);
            }
            
            
            // Read observables
            in.getProgressMonitor().setNote( "Loading Observables");
            boolean in_observables_block = false;
            while ( (line = readLine( in ) ) != null )
            {
             
                
             if ( line.matches("(?i)^\\s*$") ) 
             {
                 if (debug_statements) System.out.println( "Skipping blank line.");
                 continue;
             }
             else if ( line.matches("(?i).*#.*") ) 
             {
                 if (debug_statements) System.out.println( "Skipping comment line: " + line );
                 continue;
             }
             else if ( line.matches( "begin observables" ) )
             {
                 if (debug_statements) System.out.println( "Found start of observables block.");
                 in_observables_block = true;
                 continue;
             }
             else if ( line.matches( "end observables" ) ) 
             {
                 if (debug_statements) System.out.println( "Found end of observables block.");
                 break;
             }
             
                if (debug_statements) System.out.println( "Processing Observable: " + line );
                
             // handle lines split with "\"
             line = concatLines( line, in );
             
             
             if ( in_observables_block )
             {
                Group g = readGroupFromBNGL( line );
                g.setVisible( false );
                getGUI().getObservablesPalette().getAllGroups().add( g );
                addObservable(g);
             }
            }
            
            // Make groups visible
            in.getProgressMonitor().setNote( "Displaying " + getGUI().getObservablesPalette().getAllGroups().size() + " Observables" );
            
            getGUI().getObservablesPalette().compressDisplay();
            Iterator<Group> group_itr = getGUI().getObservablesPalette().getAllGroups().iterator();
            while ( group_itr.hasNext() )
            {
                group_itr.next().setVisible(true);
            }
        }
        catch (InterruptedIOException iioe )
        {
            the_gui.initialize();
            return false;
        }
        catch ( IllegalStateException ise )
        {
            getGUI().getEditorPanel().displayError("Error Loading BNGL File", "Could not parse as a BNGL file.");
            if (debug_statements) System.out.println("Error: Unable to parse as a BNGL file.");
            ise.printStackTrace();
            return false;
        }
        catch ( BNGLInputMalformedException bim )
        {
            getGUI().getEditorPanel().displayError("Error Loading BNGL File", bim.getMessage() );
            return false;
        }
        catch ( Exception e )
        {
            getGUI().getEditorPanel().displayError("Error Loading BNGL File", "Unhandled exception caught. The message was " + e.getMessage() );
            e.printStackTrace();
            return false;
        }
        
        
        return true;
    }
    
    synchronized public BioComponent readComponentFromBNGL( String bngl_component ) throws BNGLInputMalformedException
    {
        if (debug_statements) System.out.println("Processing component " + bngl_component );
        java.util.regex.Pattern component_pattern = java.util.regex.Pattern.compile("(\\w+)~?(\\w+)?");
        Matcher fit = component_pattern.matcher(bngl_component);
        
        
        if ( !fit.find() )
        {
            throw new BNGLInputMalformedException("Component "+bngl_component+" has invalid syntax.");
        }
   
        String label = null; 
        String state = null;
        boolean stateless = false;
       
            label = fit.group(1);
            state = fit.group(2);
        
        
        /*
        if ( fit.group(1) == null )
        {
            label = fit.group(3);
            state = null;
        }
        else
        {
            c
        }
        */
         
        if (debug_statements) System.out.println("Component Label: " + label);
        if (debug_statements) System.out.println("Component State: " + state);
        
        
        
        WidgetPanel containing_panel = new WidgetPanel( getGUI() );
        int x = 0;
        int y = 0;
        boolean template = false;
        BioComponent new_component = new BioComponent( x, y, label, state, template, containing_panel ); 
        
        // Remember edge if present
        java.util.regex.Pattern edge_pattern = java.util.regex.Pattern.compile("\\!(\\d+|\\+|\\?)");
        Matcher edge_fit = edge_pattern.matcher(bngl_component);
        
        Vector<Edge> edges = new Vector<Edge>();
        
        
        new_component.setBindingState("No Additional Bonds");
        
        while ( edge_fit.find() )
        {
            String edge_id = edge_fit.group(1);
            
            if (debug_statements) System.out.println("Found edge " + edge_id + " attached to component " + label ); 
            
            if ( edge_id.equals("?") )
            {
                 new_component.setBindingState("Don't Care");
            }
            else if ( edge_id.equals("+") )
            {
                 new_component.setBindingState("Additional Bonds");
            }
            else
            {
                new_component.setBindingState("No Additional Bonds");
                new_component.addBNGLEdge( edge_id );
            }
        
            
            
        }
        
        // Remember map if present
        java.util.regex.Pattern map_pattern = java.util.regex.Pattern.compile("\\%(\\d+)");
        Matcher map_fit = map_pattern.matcher(bngl_component);
        if ( map_fit.find() )
        {
            String map_id = map_fit.group(1);
            
            if (debug_statements) System.out.println("Found map " + map_id + " attached to component " + label ); 
            
            new_component.addBNGLMap( map_id );
        }
        
        
        return new_component;
    }
   
    synchronized public BioGraph readBioGraphFromBNGL( String bngl_biograph ) throws BNGLInputMalformedException
    {
        if (debug_statements) System.out.println("Processing BNGL BioGraph: " + bngl_biograph);
        
            BioGraph new_graph = new BioGraph();

            String containers[] = bngl_biograph.split("\\.");
            
            // Process the containers
            for ( int i = 0; i < containers.length; i++ )
            {
                BioContainer new_container = readContainerFromBNGL( containers[i] );
                new_graph.addContainer( new_container );
            }
       
        return new_graph;
    }
   
    synchronized public BioContainer readContainerFromBNGL( String bngl_container ) throws BNGLInputMalformedException
    {            
            if (debug_statements) System.out.println("Processing BNGL Container: " + bngl_container);
            java.util.regex.Pattern container_label_pattern = java.util.regex.Pattern.compile("^\\s*(\\S+)\\(\\S*\\)\\s*$");
            Matcher container_label_fit = container_label_pattern.matcher( bngl_container );
            
            WidgetPanel containing_panel = new WidgetPanel( getGUI() );
            
            if ( !container_label_fit.find() )
            {
                    // This might be an empty container with no parentheses
                    container_label_pattern = java.util.regex.Pattern.compile("^\\s*(\\w+)\\s*$");
                    container_label_fit = container_label_pattern.matcher( bngl_container );
                    if ( container_label_fit.find() )
                    {
                        String label = container_label_fit.group( 1 );
                        return new BioContainer(0, 0, label, containing_panel );
                    }
                    else
                    {
                        throw new BNGLInputMalformedException("BNGL Container \""+bngl_container+"\" is malformed.");
                    }
            }
                
            String label = container_label_fit.group( 1 );
            
            if (debug_statements) System.out.println("Container Label: " + label);
     
            int x = 10;
            int y = 10;
            
            
            BioContainer new_container = new BioContainer(0, 0, label, containing_panel );
            
            java.util.regex.Pattern components_pattern = java.util.regex.Pattern.compile("\\((\\S*)\\)"); //|\\w+[!\\d]*)[\\(\\,]");
            Matcher components_fit = components_pattern.matcher( bngl_container );
            
            components_fit.find();
            String components_str = components_fit.group(1);
            
            String[] components = components_str.split("[\\(\\,\\)]");
            for ( int i = 0; i < components.length; i++ )
            { 
                String component = components[i];
                if ( component.matches( "\\s*" ) )
                {
                    continue;
                }
                    
                if (debug_statements) System.out.println( "Model: Processing component: " + component + " (size="+component.length()+")");
                
                BioComponent new_component = readComponentFromBNGL( component );
                new_component.calculatePointerOffset( 0, 0 );
                new_component.updateLocation( x, y, false );
                
                y+=50;
                new_container.setHeight( y );
                new_container.addComponent( new_component ); 
            }
            
             
             
            
            
            //java.util.regex.Pattern component_pattern = java.util.regex.Pattern.compile("[\\(\\,](\\w+~?[\\S]*!?[\\d\\+\\?]*)"); //|\\w+[!\\d]*)[\\(\\,]");
            //Matcher component_fit = component_pattern.matcher( bngl_container );
            /*
            while ( component_fit.find() )
            {
                String component = component_fit.group(1);
                if (debug_statements) System.out.println( "Processing component: " + component );
                BioComponent new_component = readComponentFromBNGL( component );
                new_component.calculatePointerOffset( 0, 0 );
                new_component.updateLocation( x, y, false );
                
                y+=50;
                new_container.setHeight( y );
                new_container.addComponent( new_component );
                
                
            }
	      */       

            // If the molecule type exists update it with the state 
            // components
            
            MoleculeType mt = getMoleculeType( new_container.getLabel() );
            if ( mt == null )
            {
               mt = new MoleculeType( new_container );
               the_gui.getMoleculePalette().addMoleculeType( mt );
            }
            
            Iterator comp_itr = new_container.getComponents().iterator();
            while( comp_itr.hasNext() )
            {
                BioComponent current_comp = (BioComponent)comp_itr.next();
            
                String state = current_comp.getState();
                String clabel = current_comp.getLabel();
             
                if ( debug_statements ) System.out.println("Model: Creating ComponentType from component " + clabel);
                
                
                // Create an appropriate component type if a matching one does not exist already
                if ( mt.getComponentType( clabel ) == null )
                {
                    ComponentType ct = new ComponentType();
                    if ( current_comp.getState() != null && !current_comp.getState().equals("*") )
                    {
                        if (debug_statements) System.out.println("Model: Creating new component type " + clabel + " and adding state " + state );
                        Vector<String> allowed_states = new Vector<String>();
                        allowed_states.add( state );
                        ct.setLabel( clabel );
                        ct.setAllowedStates( allowed_states );
                        ct.setDefaultState( state );
                        ct.setStateless( false );
                        ct.setContainingPanel( the_gui.getMoleculePalette() );
                    }
                    else
                    {
                        ct.setLabel( clabel );
                        //ct.setStateless( true );
                    }
            
                    ct.setContainingPanel( new WidgetPanel( getGUI() ) );
                    mt.addComponentType( ct );
                }
                else // make sure the observed state of the current component is in the
                // allowed state list of the corresponding type
                {
                    // addAllowedStates checks for and eliminates duplicates
               
                        
                    ComponentType type = mt.getComponentType( clabel );
                    if (debug_statements) System.out.println("Model: Found an existing ComponentType labeled \"" + type.getLabel() + "\"" );
                    
                    if ( type.isStateless() && state != null )
                    {   
                        throw new BNGLInputMalformedException("Model: Component \""+clabel+"\" was used with an explicit state \""+state+"\" even though this component was used elsewhere as if it were stateless.");
                    }
                   else
                    {
                        if ( state != null )
                        {
                            if ( !state.equals("*") )
                            {
                                if (debug_statements) System.out.println("Model: Adding allowed state " + state + " to " + type.getLabel() );
                                type.addAllowedState( state );
                            }
                            else
                            {
                                if (debug_statements) System.out.println("Model: state is \"*\" not adding to allowed states" );
                            }
                        }
			}
                    }
                }
            
             
            new_container.calculatePointerOffset(0,0);
        return new_container;
    }
   
    
    
    synchronized Species readSpeciesFromBNGL( String bngl_species ) throws BNGLInputMalformedException
    {
            // We always use the editor panel on creation because it is guarenteed to be
            // displayed - and have valid dimensions.
                 
            if (debug_statements) System.out.println("Model:readSpeciesFromBNGL() processing " + bngl_species );
            
            /*
            // divide species into concentration and containers
            //java.util.regex.Pattern param_pattern = java.util.regex.Pattern.compile("(?m)\\s+(\\w+)\\:\\s+([\\w\\d\\.e_]+)\\s*$");
            java.util.regex.Pattern param_pattern = java.util.regex.Pattern.compile("(?m)\\s+(\\w+)\\:\\s+([\\w\\.+-]+)\\s*$");
            Matcher param_fit = param_pattern.matcher( bngl_species );
            
            if ( !param_fit.find() )
            {
                throw new BNGLInputMalformedException("Could not divide the species \""+bngl_species+"\" into a concentration portion and a structural portion");
            }
             */
             
            
           
            String species_label = null;
            String species_structure = null;
            String param_name = null;
                 
            java.util.regex.Pattern species_pattern = java.util.regex.Pattern.compile("(?m)\\s*([\\s\\w]+)\\:\\s+(\\S+)\\s+([\\w\\.\\+\\-]+)\\s*$");
            Matcher species_fit = species_pattern.matcher( bngl_species );
            
            if ( species_fit.find() ) // has label
            {
		//idspeciesgenerator.getNextIDSpecies();
                species_label = species_fit.group(1);
                species_structure = species_fit.group(2);
                param_name = species_fit.group(3);
                //throw new BNGLInputMalformedException("Could not divide the reaction rule\n\"" + bngl_reaction_rule + "\"\ninto label, structure and rate portions");
            }
            else // unlabeled
            {
                species_pattern = java.util.regex.Pattern.compile("(?m)(\\S+)\\s+([\\w\\.\\+\\-]+)\\s*$");
                species_fit = species_pattern.matcher( bngl_species );
                
                if ( species_fit.find() )
                {
                    //species_label = "Species" + (new IDGenerator().getCurrentID()+1);
                    species_label = "Species" + idspeciesgenerator.getNextIDSpecies();
                    species_structure = species_fit.group(1);
                    param_name = species_fit.group(2);
                }
                else
                {
                    throw new BNGLInputMalformedException("Could not divide the species\n\"" + bngl_species + "\"\ninto label (optional), structure and concentration portions");
                }
            }
        
            
            Species new_species = new Species(species_label, 0, 0, getGUI().getSpeciesPalette() );//new WidgetPanel( getGUI() ) );
            
        //String param_name = param_fit.group(1);
        
        //String species_structure = bngl_species.replaceAll( "(?m)\\s+([\\w\\d\\.e_]+)\\s*$", "" );
        
        if (debug_statements) System.out.println( "Divided the species \"" + bngl_species + "\" into a label (optional)\"" + species_label + "\", concentration \"" + param_name + "\" portion and a structural \"" + species_structure + "\" portion" );
        
        String concentration = null;
        
        if ( param_name.matches("[\\d\\.\\+\\-e]+") )
        {
            concentration = param_name;
            param_name = "AutoConc"+param_name.hashCode();
            //param_name = "AutoGenConc"+getGUI().getModelParameters().getParameters().size();
        }
        else
        {
            concentration = (String)bngl_parameters.get( param_name );
        }
        
        if ( concentration == null )
        {
            throw new BNGLInputMalformedException("The species parameter \"" + param_name + "\" cound not be found in the parameters section or is not a valid number.");
        }
       
        //Float concentration_f = new Float( concentration );
         
            Parameter conc = new Parameter( param_name, concentration );
            getGUI().getModelParameters().addParameter( conc );
            new_species.setConcentrationParameter( conc );
            
            
            
            String containers[] = species_structure.split("\\.");
            
            int x = 0;
            int y = 0;
            // Process the containers
            for ( int i = 0; i < containers.length; i++ )
            {
                if (debug_statements) System.out.println("Processing Container:" + containers[i] );
                BioContainer new_container = readContainerFromBNGL( containers[i] );
               
                new_container.updateLocation( x, y, false );
                x += new_container.getWidth()+35;
                new_species.addContainer( new_container );
              
             // this might not be the right place to side effect additions to the molecule type list 
            // - trying to keep functional :(
                
                // Check that a type hasnt already been added for this molecule
                // we have to assume that duplicates in the bngl file are
                // really the same molecule - otherwise the bngl file 
                // would make no sense
            if ( null == getMoleculeType( new_container.getLabel() ) )
            {
                if (debug_statements) System.out.println("Adding new MoleculeType matching a container discovered in the BNGL file.");
                
                MoleculeType mt = new MoleculeType( new_container );
             
                getGUI().getMoleculePalette().addMoleculeType( mt );
            }
            else if ( null == getGUI().getMoleculePalette().getMoleculeType( new_container ) )
            {
                // A molecule type exists matching the new_containers name but has a different structure
                // this is an error. Molecules in species must be fully defined and have the same
                // structure if they have the same name. (unlike patterns).
                throw new BNGLInputMalformedException(new_container.getLabel() + " was defined elsewhere with a different structure.");
            }
            
            }
     
            // Process edges
            Iterator components = new_species.getComponents().iterator();
            while ( components.hasNext() )
            {
                BioComponent current_component = (BioComponent)components.next();
                Iterator bngl_edges = current_component.getBNGLEdges().iterator();
                while ( bngl_edges.hasNext() )
                {
                    String current_bngl_edge = (String)bngl_edges.next();
                    
                    // Find the component with a bngl_edge to match this one and create
                    // a new edge linking those components
                    Iterator comps= new_species.getComponents().iterator();
                    while ( comps.hasNext() )
                    {
                        BioComponent curr_comp = (BioComponent)comps.next();
                        
                        if ( curr_comp != current_component )
                        {
                            Iterator bngl_edge_itr = curr_comp.getBNGLEdges().iterator();
                            while ( bngl_edge_itr.hasNext() )
                            {
                                String curr_bngl_edge = (String) bngl_edge_itr.next();
                                
                                if ( curr_bngl_edge.equals( current_bngl_edge ) )
                                {
                                    WidgetPanel cp = new WidgetPanel( getGUI() );
                                    
                                    // the edge automatically adds itself to the
                                    // endpoints edge lists
                                    Edge e = new Edge( curr_comp, current_component, cp );
                                    //e.setLabel(curr_bngl_edge);
                                    if (debug_statements) System.out.println("Created new edge (" + curr_bngl_edge + ") between " + curr_comp.getLabel() + " and " + current_component.getLabel() );
                                }
                            }
                        }
                    }
                }
            }
            
            // add the label here so the species knows how big it is before positioning the label.
            new_species.setLabel( species_label );
       
        return new_species;
    }
    
    synchronized Map readParametersFromBNGL( BufferedReader br ) 
    {
        Map params = null;
        
        return params;
    }
    
    synchronized public ReactionRule readReactionRuleFromBNGL( String bngl_reaction_rule ) throws BNGLInputMalformedException
    {
        // remove extra spaces around operators
        if (debug_statements) System.out.println("bngl_reaction_string: " + bngl_reaction_rule );
        bngl_reaction_rule = bngl_reaction_rule.replaceAll("\\s*\\+\\s*", "+");
        bngl_reaction_rule = bngl_reaction_rule.replaceAll("\\s*\\-\\>\\s*", "->");
        bngl_reaction_rule = bngl_reaction_rule.replaceAll("\\s*\\<\\-\\>\\s*", "<->");
        if (debug_statements) System.out.println("Removed operator spaces: " + bngl_reaction_rule );
        
        Vector<Operator> operators_vect = new Vector<Operator>();
        String op_name = null;
        String icon_path = null;
        double forward_rate = 0.0;
        double reverse_rate = 0.0;
        boolean reversable = false;
        
        // Match on the rest of the expression
        
        //String reaction_and_rates[] = bngl_reaction_rule.split("\\s+");
            
           String rule_label = null;
           String reaction_rule = null;
           String rates = null;
           String annotation = null;
                
            java.util.regex.Pattern reaction_rule_pattern = java.util.regex.Pattern.compile("(?m)\\s*([\\s\\w]+)\\:\\s*(\\S+)\\s+([\\w\\.\\+\\-]+(?:\\,\\s*[\\w\\.\\+\\-]+)?)\\s*(.*)$");
            Matcher reaction_rule_fit = reaction_rule_pattern.matcher( bngl_reaction_rule );
            
            if ( reaction_rule_fit.find() )
            {
		idrulesgenerator.getNextIDRules();
                rule_label = reaction_rule_fit.group(1);
                reaction_rule = reaction_rule_fit.group(2);
                rates = reaction_rule_fit.group(3);
                annotation = reaction_rule_fit.group(4);
                 }
            else
            {
                reaction_rule_pattern = java.util.regex.Pattern.compile("(?m)\\s*(\\S+)\\s+([\\w\\.\\+\\-]+(?:\\,\\s*[\\w\\.\\+\\-]+)?)\\s*(.*)$");
                reaction_rule_fit = reaction_rule_pattern.matcher( bngl_reaction_rule );
                
                if ( reaction_rule_fit.find() )
                {
                    //rule_label = "Rule"+new IDGenerator().getCurrentID()+1;
                    rule_label = "Rule"+idrulesgenerator.getNextIDRules();
                    reaction_rule = reaction_rule_fit.group(1);
                    rates = reaction_rule_fit.group(2);
                    annotation = reaction_rule_fit.group(3);
                }
                else
                {
                    throw new BNGLInputMalformedException("Could not divide the reaction rule\n\"" + bngl_reaction_rule + "\"\ninto label (optional), structure, rate portions and annotation (optional)");
                }
            }
        
       //if (debug_statements) System.out.println("Read rule with label= \"" + rule_label + "\", structure=\"" + reaction_rule + "\", rates=\"" + rates +"\"");
            
            
        //String reaction_rule = bngl_reaction_rule.replaceAll( "(?m)\\s+([\\w\\.e_\\,\\s]+)\\s*$", "" );
        
        if (debug_statements) System.out.println( "Divided the reaction rule \"" + bngl_reaction_rule + "\" into a label \"" + rule_label + "\" portion, a rule \"" + reaction_rule + "\" portion, a rates \"" + rates + "\" portion, and an annotation \""+annotation+"\" portion.");
        
        //String rates = reaction_and_rates[1];
        Operator arrow = null;
        
        // Determine whether this is a two way reaction or a forward only reaction
        if ( reaction_rule.matches( ".*\\<\\-\\>.*" ) )
        {
            reversable = true;
            op_name = "forward_and_reverse";
            icon_path = "images/forward_and_reverse_op.png";
        
            
             // Find and process the forward and reverse rates
            java.util.regex.Pattern rates_pattern = java.util.regex.Pattern.compile("\\s*([\\w\\.\\+\\-]+)\\,\\s*([\\w\\.\\+\\-]+)\\s*");
            Matcher rates_fit = rates_pattern.matcher( rates );
           
            // Only try to process rates if they were found
            if ( rates_fit.find() )
            {
                String for_param_name = rates_fit.group( 1 );
                String rev_param_name = rates_fit.group( 2 );
                
                String rev_str = null; 
                String for_str = null; 
                
                // Check if we have the rates themselves or need to do a lookup
                if ( rev_param_name.matches("[\\d\\.\\e]+") )
                {
                    rev_str = rev_param_name;
                    //rev_param_name = "AutoGenRate"+rev_param_name.hashCode();
                    rev_param_name = "AutoGenRate"+getGUI().getModelParameters().getParameters().size();
                }
                else
                {
                    rev_str = (String)bngl_parameters.get( rev_param_name );
                }
                
                if ( for_param_name.matches("[\\d\\.\\e]+") )
                {
                    for_str = for_param_name;
                    //for_param_name = "AutoGenRate"+for_param_name.hashCode();
                    for_param_name = "AutoGenRate"+getGUI().getModelParameters().getParameters().size();
                }
                else
                {
                    for_str = (String)bngl_parameters.get( for_param_name );
                }
        
                Parameter for_rate = new Parameter( for_param_name, for_str );
                Parameter rev_rate = new Parameter( rev_param_name, for_str );
              
                if ( for_str == null )
                {
                    throw new BNGLInputMalformedException("The ReactionRule parameter \"" + for_param_name + "\" cound not be found in the parameters section.");
                }
                
                if ( rev_str == null )
                {
                    throw new BNGLInputMalformedException("The ReactionRule parameter \"" + rev_param_name + "\" cound not be found in the parameters section.");
                }
                  
                /*
                try
                {
                    Double ford = new Double( for_str );
                    forward_rate = ford.doubleValue();
                }
                catch ( NumberFormatException nfe  )
                {
                    throw new BNGLInputMalformedException("The Rate Parameter " + for_str + " was malformed.");
                }
                catch ( ClassCastException cce )
                {
                    cce.printStackTrace();
                }
                
                try
                {
                    Double revd = new Double( rev_str );
                    reverse_rate = revd.doubleValue();
                }
                catch ( NumberFormatException nfe  )
                {
                    throw new BNGLInputMalformedException("The Rate Parameter " + rev_str + " was malformed.");
                }
                catch ( ClassCastException cce )
                {
                    cce.printStackTrace();
                }
                */
                 
                arrow = new ForwardAndReverse(0, 0, getGUI().getReactionRulePalette() );
                //((ForwardAndReverse)arrow).setForwardRate( for_str );
                //((ForwardAndReverse)arrow).setReverseRate( rev_str );
        
                ((ForwardAndReverse)arrow).setReverseRateParameter( rev_rate );
                ((ForwardAndReverse)arrow).setForwardRateParameter( for_rate );
                getGUI().getModelParameters().addParameter( for_rate );
                getGUI().getModelParameters().addParameter( rev_rate );
                
            }
            else
            {
                throw new BNGLInputMalformedException("BNGL Reaction Rule \"" + bngl_reaction_rule + "\" is Malformed. The rate parameters were not found.");
            }
        }
        else if ( reaction_rule.matches( ".*\\-\\>.*" ) )
        {
            //op_name = "forward";
            //icon_path = "images/forward_op.gif";
            
            // Find and process the forward rate 
            java.util.regex.Pattern rate_pattern = java.util.regex.Pattern.compile("\\s*([\\w\\.\\+\\-]+)\\s*");
            Matcher rate_fit = rate_pattern.matcher( rates );
           
            String for_str = null;
            
            // Only try to process rates if they were found
            if ( rate_fit.find() )
            {
                String for_param_name = rate_fit.group( 1 );
                
                if ( for_param_name.matches("[\\d\\.\\e]+") )
                {
                    for_str = for_param_name;
                    for_param_name = "AutoGenRate"+for_param_name.hashCode();
                }
                else
                {
                    for_str = (String)bngl_parameters.get( for_param_name );
                }
        
                Parameter for_rate = new Parameter( for_param_name, for_str );
                
                /*
                try
                {
                    String fr_str = (String)bngl_parameters.get( param_name );
                    Double frd = new Double( fr_str );
                    forward_rate = frd.doubleValue();
                }
                catch ( ClassCastException cce )
                {
                    cce.printStackTrace();
                }
                catch ( NumberFormatException nfe )
                {
                    nfe.printStackTrace();
                }
                
                 */
                
                if ( for_str == null ) throw new BNGLInputMalformedException("BNGL Reaction Rule \"" + bngl_reaction_rule + "\" is Malformed. The rate parameter " + for_param_name + " was not found.");
                
                if (debug_statements) System.out.println("Forward rate parameter set to " + for_str );
                
                arrow = new Forward(0, 0, getGUI().getReactionRulePalette() );
                
                //((Forward)arrow).setForwardRateName( param_name );
                //((Forward)arrow).setForwardRate( fr_str );
               
                ((Forward)arrow).setForwardRateParameter( for_rate );
                getGUI().getModelParameters().addParameter( for_rate );
        
            }
            else
            {
                throw new BNGLInputMalformedException("BNGL Reaction Rule \"" + bngl_reaction_rule + "\" is Malformed. The rate parameter was not found.");
            }
            
        }
        else
        {
            throw new BNGLInputMalformedException("BNGL Reaction Rule \"" + bngl_reaction_rule + "\" is Malformed. Could not find \"->\" or \"<->\"");
        }
        
        //[^!] so we don't split on !+ edge patterns
        String sides[] = reaction_rule.split("\\<?\\-\\>");
        if (debug_statements) System.out.println( "Side0: " + sides[0] );
        if (debug_statements) System.out.println( "Side1: " + sides[1] );
        
        
        
        String reactants[] = sides[0].split("(?<!\\!)\\+");
        String products[] = sides[1].split("(?<!\\!)\\+");
       
       //String reactants[] = sides[0].split("[^\\!]\\+");
       //String products[] = sides[1].split("[^\\!]\\+");
       
        
        if (debug_statements) System.out.print( "Identified the reactant string: " );
        for ( int i = 0; i < reactants.length; i++ )
        {
            if (debug_statements) System.out.print(reactants[i]+" ");
        }
        if (debug_statements) System.out.println();    
        
        if (debug_statements) System.out.print( "Identified the product string: ");
        for ( int i = 0; i < products.length; i++ )
        {
            if (debug_statements) System.out.print(products[i]+" ");
        }
        if (debug_statements) System.out.println(); 
        
        
        int x = 0;
        int y = 0;
        Vector<Pattern> reactants_vect = new Vector<Pattern>();
        for ( int i = 0; i < reactants.length; i++ )
        {
            Pattern new_pattern = readPatternFromBNGL( reactants[i] );
            new_pattern.calculatePointerOffset(0,0);
            new_pattern.updateLocation( x, y, false );
            x += new_pattern.getWidth()+20;
            reactants_vect.add( new_pattern );
            if (debug_statements) System.out.println("Adding reactant " + reactants[i] + " to reaction rule");
            if (debug_statements) System.out.println( i + " " + reactants.length );
            if ( i < reactants.length - 1 )
            {
                      Plus op = new Plus(x, y+((Pattern)reactants_vect.lastElement()).getHeight()/2, getGUI().getReactionRulePalette() );
                      
                      operators_vect.add( op );
                      x+=50;
            }
        }

            arrow.setX( x );
            arrow.setY( y+((Pattern)reactants_vect.lastElement()).getHeight()/2 );
            operators_vect.add( arrow );
            
        x+=50;
        
        Vector<Pattern> products_vect = new Vector<Pattern>();
        for ( int i = 0; i < products.length; i++ )
        {
            Pattern new_pattern = readPatternFromBNGL( products[i] );
            new_pattern.calculatePointerOffset(0,0);
            new_pattern.updateLocation( x, y, false );
            x += new_pattern.getWidth()+20;
            products_vect.add( new_pattern );
            if (debug_statements) System.out.println("Adding product " + products[i] + " to reaction rule");
            if ( i < products.length - 1 )
            {
                      Plus op = new Plus(x, y+((Pattern)products_vect.lastElement()).getHeight()/2, getGUI().getReactionRulePalette() );
                      
                      operators_vect.add( op );
                      x+=50;
            }
        }
        
        
        Vector<Pattern> operands = new Vector<Pattern>();
        operands.addAll( reactants_vect );
        operands.addAll( products_vect );
        
        WidgetPalette reaction_rule_palette = getGUI().getReactionRulePalette();
        ReactionRule rr = new ReactionRule(reactants_vect, operators_vect, products_vect, "", "", reversable, reaction_rule_palette );
        rr.setLabel(rule_label);
        Vector<String> invalid_maps = new Vector<String>();
        
        // Process AtomMaps
         Iterator components = rr.getComponents().iterator();
         if (debug_statements) System.out.println("readReactionRuleFromBNGL(): Found "+rr.getComponents().size()+" components in this reaction rule while processing maps");
            while ( components.hasNext() )
            {
                BioComponent current_component = (BioComponent)components.next();
                String bngl_map = current_component.getBNGLMap();
             
                if (debug_statements) System.out.println("readReactionRuleFromBNGL(): found start bngl map " + bngl_map );
                
                if ( bngl_map == null )
                {
                    continue;
                }
                
                boolean map_end_points_found = false;
                
                    // Find the component with a bngl_map to match this one and create
                    // a new map linking those components
                    Iterator comps= rr.getComponents().iterator();
                    
                    while ( comps.hasNext() )
                    {
                        BioComponent curr_comp = (BioComponent)comps.next();
                        
                        if ( curr_comp != current_component )
                        {
                            String curr_bngl_map = curr_comp.getBNGLMap();
        
                            if (debug_statements) System.out.println("readReactionRuleFromBNGL(): found end bngl map " + curr_bngl_map );
                            
                            if ( curr_bngl_map == null )
                            {
                                continue;
                            }
                            
                            
                            
                                if ( curr_bngl_map.equals( bngl_map ) )
                                {
                                    WidgetPanel cp = new WidgetPanel( getGUI() );
                                    
                                    // the edge automatically adds itself to the
                                    // endpoints edge lists
                                    AtomMap am = new AtomMap( curr_comp, current_component, cp );
                                    am.setLabel(curr_bngl_map);
                                    if (debug_statements) System.out.println("readReactionRuleFromBNGL(): Created new map (" + curr_bngl_map + ") between " + curr_comp.getLabel() + " and " + current_component.getLabel() );
                                    map_end_points_found = true;
                                }   
                        }
                    }
                    
                    if ( !map_end_points_found )
                    {
                        invalid_maps.add( bngl_map );
                    }
            }   
                    
         if ( !invalid_maps.isEmpty() )
         {
             if ( invalid_maps.size() == 1 )
             {
                 getGUI().getEditorPanel().displayWarning("BNGL Malformed Warning", "In rule \""+rule_label+"\" "+invalid_maps.size() +" map had only one end point." );
             }
             else
             {
                getGUI().getEditorPanel().displayWarning("BNGL Malformed Warning", "In rule \""+rule_label+"\" "+invalid_maps.size() +" maps had only one end point." );
             }
         }
         
        rr.setAnnotation( annotation );
        return rr;
    }
 
    Group readGroupFromBNGL( String bngl_group ) throws BNGLInputMalformedException
    {
        if (debug_statements) System.out.println("readGroupFromBNGL(): " + bngl_group);
        
        Vector<Pattern> patterns = new Vector();
        String patterns_string = new String();
        Vector<Operator> operators = new Vector();
        String label = new String();
        String type = new String();
        
        java.util.regex.Pattern group_pattern = java.util.regex.Pattern.compile("\\s*(\\w+)\\s+(\\w+)\\s+([\\S\\s]+)");
            Matcher group_fit = group_pattern.matcher( bngl_group );
            
            if ( group_fit.find() ) // has label
            {
                type = group_fit.group(1);
                label = group_fit.group(2);
                patterns_string = group_fit.group(3);
                
            }
            else
            {
                throw new BNGLInputMalformedException("Could not divide the group\n\"" + bngl_group + "\"\ninto type, label, and a sequance of patterns");
            }
        
            int current_x = 0;
            String[] patterns_array = patterns_string.split("\\s+");
            for ( int i = 0; i < patterns_array.length; i++ )
            {
                if (debug_statements) System.out.println("Adding pattern " + patterns_array[i]);
                Pattern p = readPatternFromBNGL(patterns_array[i]);
                patterns.add( p );
                
                p.calculatePointerOffset(0,0);
                p.updateLocation( current_x, p.getY(), false );
                
                current_x += p.getWidth()+20;
                
                // for each pattern except the last one add a union operator after
                if ( i != patterns_array.length-1 )   
                {
                    int y = p.getY() + p.getHeight()/2;
                    int x = current_x;
                    Union u = new Union( x, y, getGUI().getObservablesPalette() );
                    operators.add( u );
                    current_x += u.getWidth() + 20;
                }
            }
           
            
        Group g = new Group( label, type, patterns, operators, getGUI().getObservablesPalette() );
        
        return g;
    }
            
    synchronized Pattern readPatternFromBNGL( String bngl_pattern ) throws BNGLInputMalformedException
    {
            Pattern new_pattern = new Pattern("Pattern",0,0, new WidgetPanel( getGUI() ) );
            
            String containers[] = bngl_pattern.split("\\.");
            
            int x = 0;
            int y = 0;
            // Process the containers
            for ( int i = 0; i < containers.length; i++ )
            {
                BioContainer new_container = readContainerFromBNGL( containers[i] );
                new_pattern.addContainer( new_container );
                
                new_container.updateLocation( x, y, false );
                x += new_container.getWidth()+35;
            }
     
            // Process edges
            Iterator components = new_pattern.getComponents().iterator();
            while ( components.hasNext() )
            {
                BioComponent current_component = (BioComponent)components.next();
                Iterator bngl_edges = current_component.getBNGLEdges().iterator();
                while ( bngl_edges.hasNext() )
                {
                    String current_bngl_edge = (String)bngl_edges.next();
                    
                    // Find the component with a bngl_edge to match this one and create
                    // a new edge linking those components
                    Iterator comps= new_pattern.getComponents().iterator();
                    while ( comps.hasNext() )
                    {
                        BioComponent curr_comp = (BioComponent)comps.next();
                        
                        if ( curr_comp != current_component )
                        {
                            Iterator bngl_edge_itr = curr_comp.getBNGLEdges().iterator();
                            while ( bngl_edge_itr.hasNext() )
                            {
                                String curr_bngl_edge = (String) bngl_edge_itr.next();
                                
                                if ( curr_bngl_edge.equals( current_bngl_edge ) )
                                {
                                    WidgetPanel cp = getGUI().getReactionRulePalette();
                                    
                                    // the edge automatically adds itself to the
                                    // endpoints edge lists
                                    Edge e = new Edge( curr_comp, current_component, cp );
                                    e.setLabel(curr_bngl_edge);
                                    if (debug_statements) System.out.println("Created new edge (" + curr_bngl_edge + ") between " + curr_comp.getLabel() + " and " + current_component.getLabel() );
                                }
                            }
                        }
                    }
                }
            }
            
            
        return new_pattern;
}
    
    /*
    synchronized Pattern readPatternFromBNGL( String bngl_pattern ) 
    {
            Pattern new_pattern = new Pattern();
            new_pattern.setContainingPanel( getGUI().getReactionRulePalette() );
       
            String containers[] = bngl_pattern.split("\\.");
            
            
            
            int x = 0;
            int y = 0;
            // Process the containers
            for ( int i = 0; i < containers.length; i++ )
            {
                BioContainer new_container = readContainerFromBNGL( containers[i] );
                
                new_container.updateLocation( x, y, false );
                x += new_container.getWidth()+35;
                new_pattern.addContainer( new_container );
     
                if (debug_statements) System.out.println("X coord for new container in pattern: " + x);
            }
     
            
            // Process edges
            Iterator components = new_pattern.getComponents().iterator();
            while ( components.hasNext() )
            {
                BioComponent current_component = (BioComponent)components.next();
                Iterator bngl_edges = current_component.getBNGLEdges().iterator();
                while ( bngl_edges.hasNext() )
                {
                    String current_bngl_edge = (String)bngl_edges.next();
                    
                    // Find the component with a bngl_edge to match this one and create
                    // a new edge linking those components
                    Iterator comps= new_pattern.getComponents().iterator();
                    while ( comps.hasNext() )
                    {
                        BioComponent curr_comp = (BioComponent)comps.next();
                        
                        if ( curr_comp != current_component )
                        {
                            Iterator bngl_edge_itr = curr_comp.getBNGLEdges().iterator();
                            while ( bngl_edge_itr.hasNext() )
                            {
                                String curr_bngl_edge = (String) bngl_edge_itr.next();
                                
                                if ( curr_bngl_edge.equals( current_bngl_edge ) )
                                {
                                    WidgetPanel cp = getGUI().getSpeciesPalette();
                                    
                                    // the edge automatically adds itself to the
                                    // endpoints edge lists
                                    Edge e = new Edge( curr_comp, current_component, cp );
                                    e.setLabel(curr_bngl_edge);
                                    if (debug_statements) System.out.println("Created new edge (" + curr_bngl_edge + ") between " + curr_comp.getLabel() + " and " + current_component.getLabel() );
                                }
                            }
                        }
                    }
                }
            }
            
        return new_pattern;
    }
  */
       
    synchronized public void addMoleculeType(MoleculeType mol_type) 
    {
        molecule_types.add( mol_type );
    }
    
    //synchronized public void addComponentType(ComponentType type) 
    //{
    //    component_types.add( type );
    //}
    
    public Vector<MoleculeType> getMoleculeTypes() 
    {
        return molecule_types;
    }
 
    // Returns the set of MoleculeTypes that are partial match to this pattern
    public Vector<MoleculeType> getMoleculeTypes( BioContainer pattern ) 
    {
        Vector<MoleculeType> matches = new Vector<MoleculeType>();
        
        Iterator mt_itr = getMoleculeTypes().iterator();
        while ( mt_itr.hasNext() )
        {
            MoleculeType mt = (MoleculeType)mt_itr.next();
            
            if ( mt.isMatchingPattern( pattern ) )
            {
                matches.add( mt );
            }
        }
        
        return matches;
    }
    
    public Vector<ComponentType> getComponentTypes( String label ) 
    {
        Vector<ComponentType> matches = new Vector<ComponentType>();
        
        Iterator mt_itr = getMoleculeTypes().iterator();
        while ( mt_itr.hasNext() )
        {
            MoleculeType mt = (MoleculeType)mt_itr.next();
            
            if ( label.equals("*") )
            {
                matches.addAll( mt.getComponentTypes() ); 
            }
            else
            {
                ComponentType ct = mt.getComponentType(label);
                if ( ct != null ) matches.add( ct );
            }
        }
        
        return matches;
    }

    // Returns the MoleculeType that is a complete match to this pattern
    public MoleculeType getMoleculeType( BioContainer pattern ) 
    {
        //return getMoleculeType( pattern.getLabel() );
        
        Iterator mt_itr = getMoleculeTypes().iterator();
        while ( mt_itr.hasNext() )
        {
            MoleculeType mt = (MoleculeType)mt_itr.next();
            
            if ( mt.isType( pattern ) )
            {
                return mt;
            }
        }
        
        return null;
    }
    
    public Vector<Group> getObservables() 
    {
        return observables;
    }
    
    public Group getObservable(String label) 
    {
        Iterator<Group> itr = getObservables().iterator();
        while( itr.hasNext() )
        {
            Group current = itr.next();
            if ( label.equals( current.getLabel() ) )
            {
                return current;
            }
        }
        
        return null;
    }
    
    synchronized public MoleculeType getMoleculeType(String type_name) 
    {
        Iterator type_itr = molecule_types.iterator();
        while( type_itr.hasNext() )
        {
            MoleculeType type = (MoleculeType)type_itr.next();
            
            if ( type.getLabel().equals( type_name ) )
            {
               return type; 
            }
        }
        
       // if (debug_statements) System.out.println("Could not find Molecule Type \"" + type_name +"\"" );
        return null;
    }
    
    /*
    synchronized public ComponentType getComponentType( String type_name ) 
    {
        Iterator type_itr = component_types.iterator();
        while( type_itr.hasNext() )
        {
            ComponentType type = (ComponentType)type_itr.next();
            
            if ( type.getLabel().equals( type_name ) )
            {
               return type; 
            }
        }
        
        return null;
    }
    */
    
    synchronized public void writeComponentToBNGL( BioComponent component, BioGraph bg, OutputStreamWriter output ) throws IOException, BNGLOutputMalformedException
    {
        
            String l = component.getLabel();
            l = l.replaceAll("\\s", "_");
            output.write( l );
            
            //Write map label
            if ( component.getAtomMap() != null )
            {
                output.write("%"+component.getAtomMap().getLabel());
            }
            
        if ( component.isStateless() )
        {
            //output.write( "*" );
        }
        else
        {
            output.write("~");
            
            if ( component.getState() == null )
            {
                output.write("*"); // For backwards compatibility
            }
            else
            {
                output.write( component.getState() );
            }
        }
        
         
        
	// write edges only if this component is part of a biograph
	//(only components in biographs can have edges by definition)
        if ( bg != null ) // write edges
        {
            // For indexing
            Vector<Edge> bg_edges = bg.getEdges();
            
            Iterator edge_itr = component.getEdges().iterator();
            while( edge_itr.hasNext() )
            {
                 Edge current_edge = (Edge)edge_itr.next();
                 //BioComponent other_end = current_edge.getOtherComponent( component );
            
                 output.write("!");
                 output.write( new Integer( bg_edges.indexOf( current_edge ) ).toString() );
            }
        
           if ( component.getBindingState().equals("Additional Bonds") )
           {
              output.write("!+");
           }
          else if ( component.getBindingState().equals("Don't Care") )
          {
             output.write("!?");
          }
            
            
        }
        
        
        //else // Version 1.x only supported states not names and edges
        //{
        //    if ( component.isStateless() )
        //    {
        //        throw new BNGLOutputMalformedException("Attempt to write a stateless component in BNGL 1.x format.");
        //    }
        //    output.write( component.getState() );
        //}
        
        
        
        
    }
    
    public synchronized boolean readNetwork() 
    {
        if (debug_statements) System.out.println( "Read Network()" );
        
        String file_name = bngl_output_file.getName();
        String path = bngl_output_file.getAbsolutePath();
        String os_dir_separator = bngl_output_file.separator;
        String name_prefix = file_name.replaceAll( "\\.bngl$", "" );

	if (debug_statements) System.out.println ("bngl output name is " + file_name);
        
        String net_path = null;
        if ( getGUI().getConfig().getEngineVersion().equals("1.x") )
        {
            net_path = bngl_output_file.getParent() + os_dir_separator+"NET"+os_dir_separator + name_prefix + ".net";
        }
        else
        {
            net_path = bngl_output_file.getParent() + os_dir_separator + name_prefix + ".net";
        }
        
        File net_file = new File( net_path );
        if ( !net_file.exists() )
        {
            getGUI().getEditorPanel().displayError("Error Reading Network","The network file generated by the BNGL Engine\ncould not be found at " + net_path);
            return false;
        }
        
        return readNetwork( net_file );
    }
   
    
    
    // This function read the .NET file generated by the BNGL Engine after a successful run
    public synchronized boolean readNetwork( File input_file )
    { 
        final File the_file = input_file;
        final String file_name = input_file.getName();
            try
            {
                if (debug_statements) System.out.println("Reading file " + file_name );
       
            // set up stream and reader filter sequence    
      FileInputStream fileIn = new FileInputStream ( input_file );
      final ProgressMonitorInputStream in
         = new ProgressMonitorInputStream ( the_gui.getEditorPanel(),
            "Reading " + input_file.getName (), fileIn);
      //final BufferedReader in = new BufferedReader (inReader);
            in.getProgressMonitor().setMillisToPopup(0);
            
        
            
        Thread ParseNetworkThread = new Thread() 
        {
                public void run() 
                {
                    try
                    {
                        //InputStreamReader insr = new InputStreamReader (in);
                        //BufferedReader inbr = new BufferedReader (in);
                        
                        parseNetwork( in );
                        
                        /*
                        String line;
                        
                        while ( (line = readLine( in ) ) != null )
                        {
                            System.out.println("Read \""+line+"\". Sleeping for 1/100 of a second");
                            Thread.currentThread().sleep(10);
                        }
                         */
                        in.close();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
        };
        ParseNetworkThread.setDaemon(true); // So the thread wont stop RuleBuilder from closing.
        ParseNetworkThread.start();
       
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return true;
    }
    
    synchronized public String readBNGLBlock( String bngl_input, String block_name ) 
    {
        
        
            // Find and process the block
            java.util.regex.Pattern block_pattern = java.util.regex.Pattern.compile("begin " + block_name + "\\s+([\\S\\s]*)\\s+end " + block_name );
            Matcher block_fit = block_pattern.matcher( bngl_input );
           
            // Only try to process parameters if that section was found
            if ( block_fit.find() )
            {
                return block_fit.group( 1 );
            }
            
            return null;
    }
    
    synchronized public void addObservable(Group g) 
    {
        observables.add(g);
    }
    
    synchronized public void removeObservable(Group g) 
    {
         observables.remove(g);
    }
    
    
    
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException
    {
            stream.writeObject( molecule_types );
            stream.writeObject( observables );
            stream.writeObject( reaction_rules );
            stream.writeObject( species );
         
            stream.writeObject( engine_arguments );
         
    }
    
    public void setReactionRules( Vector<ReactionRule> rules )
    {
        reaction_rules = rules;
    }
    
    public void setSpecies( Vector<Species> s )
    {
        species = s;
    }
    
    public boolean isValidType( BioContainer bc ) 
    {
        Iterator<BioComponent> comp_itr = bc.getComponents().iterator();
        while ( comp_itr.hasNext() )
        {
            if ( !comp_itr.next().getBindingState().equals( "No Additional Bonds" ) ) return false;
        }
        
        if ( getMoleculeType( bc ) != null )
        {
            return true;
        }
        
        return false;
    }
    
    public boolean isValidPattern( BioContainer pattern ) 
    {
        if ( getMoleculeTypes( pattern ).isEmpty() )
        {
            return false;
        }
        
        return true;
    }
   
    
    // The following methods allow the model to read and write BNGML
    // (Biological Network Generator Markup Language)
    
    public BioGraph readBioGraphFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
       return null;
    }
    
    public BioComponent readComponentFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
       return null;
    }
    
    public BioContainer readContainerFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public Edge readEdgeFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public boolean readModelFromBNGML(File file) 
    {
        return false;
    }
    
    public Group readObservableFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public Map readParametersFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public Pattern readPatternFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public ReactionRule readRuleFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public Species readSpeciesFromBNGML(InputStream xml_stream) throws BNGMLFormatException
    {
        return null;
    }
    
    public void writeBioGraphToBNGML(BioGraph graph, OutputStream xml_stream) {
    }
    
    public void writeComponentToBNGML(BioComponent component, OutputStream xml_stream) {
    }
    
    public void writeContainerToBNGML(BioContainer container, OutputStream xml_stream) {
    }
    
    public void writeEdgeToBNGML(Edge edge, OutputStream xml_stream) {
    }
    
    public void writeModelToBNGML(File file) {
    }
    
    public void writeObservableToBNGML(Group group, OutputStream xml_stream) {
    }
    
    public void writeParametersToBNGML(Map parameters, OutputStream xml_stream) {
    }
    
    public void writePatternToBNGML(Pattern pattern, OutputStream xml_stream) {
    }
    
    public void writeRuleToBNGML(ReactionRule rule, OutputStream xml_stream) {
    }
    
    public void writeSpeciesToBNGML(Species species, OutputStream xml_stream) {
    }

    private String bngl_input_string;

    public String getBnglnputString() {
        return getBNGLInputString();
    }

    public void setBNGLInputString(String bngl_input_string) {
        this.setBngl_input_string(bngl_input_string);
    }

    public String getBNGLInputString() {
        return bngl_input_string;
    }

    public void setBngl_input_string(String bngl_input_string) {
        this.bngl_input_string = bngl_input_string;
    }

    public String concatLines(String line, ProgressMonitorInputStream in ) 
    {
        String next_line = null;
        try
        {
            if ( line.matches(".*\\\\.*") ) 
            {
             next_line = concatLines( readLine( in ), in );
         
             line = line.replaceFirst( "\\\\", next_line );
            }      
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
      
        return line;
    }

    public void writeAllowedStatesToBNGL(ComponentType ct, OutputStreamWriter output) throws IOException
    {
        
                    if ( ct.getAllowedStates().isEmpty() ) return;
                    
                    String default_state = ct.getDefaultState();
                    output.write( "~" + default_state );
                    
                    Iterator<String> states_itr = ct.getAllowedStates().iterator();
                    while( states_itr.hasNext() )
                    {
                        String state = states_itr.next();
                    
                        if ( !state.equals(default_state) ) output.write("~"+state);
                    }
    
    }

    // Have to write this because Java's ProgressMonitorInputStream with InputStreamReader is broken 
    public String readLine(ProgressMonitorInputStream in) throws IOException
    {
  
 	StringBuffer buffer = new StringBuffer();
	int nread = 0;
 
	while (true) 
        {
		final int data = in.read();
		final char ch = (char) (data & 0xff);
 
		if (data == -1)
 			break;
		nread++;
 
		if (ch == '\n')
 			break;
		if (ch == '\r') {	// Check for '\r\n'
			in.mark(1);
			final int data2 = in.read();
			final char ch2 = (char) (data & 0xff);

                        if (data2 != -1 && ch2 != '\r')
				in.reset();        // Jump back to mark
 			break;
 		}
                
		buffer.append(ch);
        }
        
	return (nread == 0) ? null : buffer.toString();
 }

    public boolean parseNetwork(ProgressMonitorInputStream in) 
    {
        try
        {
            bngl_parameters = new TreeMap<String,String>();
            /*
        boolean modal = false;
        JFrame owner = getGUI().getEditorFrame();
        final JDialog progress_frame = new JDialog(owner, modal);
        
        final NetworkLoader task = new NetworkLoader();
         
        final JProgressBar progressBar = new JProgressBar(0, task.getLengthOfTask());
        progressBar.setValue(0);
        progressBar.setStringPainted(true);

        final JTextArea taskOutput = new JTextArea(5, 15);
        taskOutput.setMargin(new Insets(5,5,5,5));
        taskOutput.setEditable(false);
        taskOutput.setCursor(null); //inherit the panel's cursor
                                    //see bug 4851758

        JPanel panel = new JPanel( );
        
        panel.add(progressBar, BorderLayout.NORTH );

        panel.add(new JScrollPane(taskOutput), BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        progress_frame.getContentPane().add(panel);
        progress_frame.pack();
        progress_frame.setVisible( true );
        
        //Create a timer.
        int ONE_SECOND = 1000;
        final javax.swing.Timer timer = new javax.swing.Timer(ONE_SECOND, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                progressBar.setValue(task.getProgress());
                String s = task.getMessage();
                if (s != null) {
                    taskOutput.append(s + "\n");
                    taskOutput.setCaretPosition(
                            taskOutput.getDocument().getLength());
                }
                if (task.isDone()) {
                    Toolkit.getDefaultToolkit().beep();
                    //timer.stop();
                    progress_frame.setCursor(null); //turn off the wait cursor
                    progressBar.setValue(progressBar.getMinimum());
                }
            }
        });
        
       task.load( filename );
       timer.start();
        */
       
       boolean modal = false;
       JFrame owner = getGUI().getMainFrame();
       JDialog progress_frame = new JDialog(owner, modal);
        
            
            
            //NetworkLoader task = new NetworkLoader();
            //task.load( br, this );
            
            // Read buffer into string
            String net_input_string = new String();
            String line = null;
            int ln_count = 0;
            while ( (line = readLine(in)) != null )
            {
                if (debug_statements) System.out.println( "Read Network(File) line " + ln_count++ );
                net_input_string += line+"\n";
                ln_count++;
                //progress_bar.setValue( ln_count );
            }
      
            int progress_value = 0;
            
            if (debug_statements) System.out.println( "Read Network(File) 1" );
            
            // Remove all comments and blank lines from the input
            net_input_string = net_input_string.replaceAll("#.*", "" );
            
            // Read net_params
            //TreeMap net_params = new TreeMap();
            String params_block = readBNGLBlock( net_input_string, "parameters" );
            
            // discard index numbers- they arnt used for anything
            // params_block = params_block.replaceAll("(m?)$\\s*\\d+", "" );
            
            if (debug_statements) System.out.println( "Read Network(File) 2" );
            
            // Read parameters into a map
            java.util.regex.Pattern param_pattern = java.util.regex.Pattern.compile("\\s*\\d+\\s*(\\w+)\\s+([\\w+\\.e-]+)");
            Matcher name_value_fit = param_pattern.matcher( params_block );
                
            while ( name_value_fit.find() )
            {
                String name = name_value_fit.group(1);
                String value = name_value_fit.group(2);
                if (debug_statements) System.out.println("Adding <" + name + "," + value + "> to the net params map." );
                getBNGLParameters().put( name, value );
                //progress_bar.setValue( progress_value++ );
            }
                
            if (debug_statements) System.out.println( "Read Network(File) 3" );
            
            // Read species
            TreeMap net_species = new TreeMap();
            String species_block = readBNGLBlock( net_input_string, "species" );
            
            // Record the index for the map
            java.util.regex.Pattern indx_species_pattern = java.util.regex.Pattern.compile("\\s*(\\d+)\\s+(.*)");
            Matcher indx_species_fit = indx_species_pattern.matcher( species_block );
               
            if (debug_statements) System.out.println(species_block);
            
            if (debug_statements) System.out.println( "Read Network(File) 4" );
            
            int existing_species_count = species.size();
            int net_species_count = 0;
            
            while ( indx_species_fit.find() )
            {
                String index = indx_species_fit.group(1);
                String species_str = indx_species_fit.group(2);
                Species species = readSpeciesFromBNGL( species_str );
                if (debug_statements) System.out.println("Adding species \"" + species_str + "\"to index " +index+ " in the net species map." );
                net_species.put( index, species );
                net_species_count++;
                

                                
                // Only add species from the net file if they are not already in
                // the model. BNG writes new species after seed species so
                // a simple count should do the trick.
                if ( net_species_count > existing_species_count )
                {
                    species.setDerived(true);
                    the_gui.getSpeciesPalette( ).addSpecies( species );
                    // Don't add to model in this version
                    this.species.remove( this.species.lastElement() );
                }
                
                //progress_bar.setValue( progress_value++ );
                
                // Add Species to the Species window (make transparent to show they were generated)
                //getGUI().getSpeciesPalette().addSpecies( species );
            }

            if (debug_statements) System.out.println( "Read Network(File) 5" );
            
            // Read Reactions
            TreeMap reactions = new TreeMap();
            String reactions_block = readBNGLBlock(net_input_string, "reactions");
            if (debug_statements) System.out.println("Reaction Block\n"+reactions_block);
            
            if (reactions_block == null )
            {
                the_gui.getEditorPanel().displayError("Error Reading Net File","No reaction block found");
                return false;
            }
            
            ReactionPalette reaction_palette = the_gui.getReactionPalette();
           
            java.util.regex.Pattern reaction_pattern = java.util.regex.Pattern.compile("\\s*\\d+\\s+([\\s\\d]+)\\s*\\,\\s*([\\s\\d]+)\\s+(\\w+)");

            Matcher reaction_fit = reaction_pattern.matcher( reactions_block );
           
            if ( reactions_block != null )
            while ( reaction_fit.find() )
            {
                Vector<Operator> operators = new Vector();
                Vector<Species> reactants = new Vector();
                Vector<Species> products = new Vector();
                
                    if (debug_statements) System.out.println("Processing reaction line: " + reaction_fit.group(0));

                    String reactants_str = reaction_fit.group(1);
                    String products_str = reaction_fit.group(2);
                    String parameter_str = reaction_fit.group(3);
                
                if (debug_statements) System.out.println("Reaction: " + reaction_fit.group(1) + " -> " + reaction_fit.group(2) + ", " + reaction_fit.group(3));
                
                java.util.regex.Pattern reactant_pattern = java.util.regex.Pattern.compile("(\\d+)");
                Matcher reactant_fit = reactant_pattern.matcher( reactants_str );
                
                int new_x = 0;
                int padding = 25;
                
                while ( reactant_fit.find() )
                {
                    String reactant_str = reactant_fit.group(1);
                    
                    if (debug_statements) System.out.print( reactant_str + " ");
                    Species s = (Species)WidgetCloner.clone( (Species)net_species.get( reactant_str ) );
                
                    reactants.add(s);    
                }
                    
                for ( int i = 0; i < reactants.size(); i++ )
                {
                    Species s = (Species)reactants.get(i);
                    // The new species object needs a frame of reference from which 
                    // to calculate its offset.
                    s.calculatePointerOffset(0,0);
                    
                    s.updateLocation(new_x,0, false);
                    if (debug_statements) System.out.println("X position: " + new_x);
                    new_x = new_x + s.getWidth()+padding;
                    
                    if ( i < reactants.size() - 1 )
                    {
                      Plus op = new Plus(new_x, ((Species)reactants.lastElement()).getHeight()/2, reaction_palette );
                      
                      operators.add( op );
                      if (debug_statements) System.out.println("X position: " + new_x);
                      new_x+=padding;
                    }
                }
                 
            
                
                if (debug_statements) System.out.print( " -> ");
                
                // insert operator here
                Forward arrow = new Forward(new_x, ((Species)reactants.lastElement()).getHeight()/2, reaction_palette);
                operators.add( arrow );
                if (debug_statements) System.out.println("X position: " + new_x);
                new_x = new_x + arrow.getWidth() + padding;
                
                java.util.regex.Pattern product_pattern = java.util.regex.Pattern.compile("(\\d+)");
                Matcher product_fit = product_pattern.matcher( products_str );
        
                while ( product_fit.find() )
                {
                    String product_str = product_fit.group(1);
                    
                    if (debug_statements) System.out.print( product_str + " ");
                    Species s = (Species)WidgetCloner.clone( (Species)net_species.get( product_str ) );
                   
                    products.add(s);
                    
                }
                
                for ( int i = 0; i < products.size(); i++ )
                {
                    Species s = (Species)products.get(i);
                    
                    // The new species object needs a frame of reference from which 
                    // to calculate its offset.
                    s.calculatePointerOffset(0,0);
                    
                    s.updateLocation(new_x,0, false);
                    if (debug_statements) System.out.println("X position: " + new_x);
                    new_x = new_x + s.getWidth()+padding;
                    
                    if ( i < products.size() - 1 )
                    {
                      Plus op = new Plus(new_x, ((Species)products.lastElement()).getHeight()/2, getGUI().getReactionRulePalette() );
                      
                      operators.add( op );
                      if (debug_statements) System.out.println("X position: " + new_x);
                      new_x+=padding;
                    }
                }
                
                
                float reverse_rate = 0;
                float forward_rate = 0;
                
                boolean reversable = false;
                
                if (debug_statements) System.out.print( "," + getBNGLParameters().get( parameter_str ) );
                String for_str = null;
                
                if ( parameter_str.matches("[\\d\\.\\e]+") )
                {
                    for_str = parameter_str;
                    parameter_str = "AutoGenRate"+parameter_str.hashCode();
                }
                else
                {
                    for_str = (String)bngl_parameters.get( parameter_str );
                }
        
                Parameter for_rate = new Parameter( parameter_str, for_str );
                //the_gui.getModelParameters().addParameter(for_rate);
                
                // Add the reaction to the reaction palette
                Reaction reaction = new Reaction( reactants, operators, products, forward_rate, reverse_rate, reversable, reaction_palette);
                
                
                //((Forward)reaction.getProductionOperator()).setForwardRateParameter( for_rate );
                ((Forward)reaction.getProductionOperator()).setForwardRateParameter( for_rate );
                
                
                // The new reaction object needs a frame of reference from which 
                // to calculate its offset.
                //reaction.calculatePointerOffset(0,0);
                //reaction.updateLocation( 0, 0, false );
                
                if (debug_statements) System.out.println("Reached reaction_palette.addReaction( reaction );");
                
                
                reaction.setLabel("Reaction{#}");
                reaction_palette.addReaction( reaction );
            } 
            
            
            if (debug_statements) System.out.println();
            
            if (debug_statements) System.out.println( "Read Network(File) 6" );
                
            return true;
        }
        catch ( Exception e )
        {
            getGUI().getEditorPanel().displayError("Error Reading Network File", e.getMessage() );
            e.printStackTrace();
        }
                    
        return false;
    }

    
    
    
    
    
    
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
         molecule_types = (Vector<MoleculeType>)stream.readObject();
         observables = (Vector<Group>)stream.readObject();
         reaction_rules = (Vector<ReactionRule>)stream.readObject();
         species = (Vector<Species>)stream.readObject();
         
         engine_arguments = (String)stream.readObject();

	 idmoleculetypesgenerator = new IDMoleculeTypesGenerator();
	 idobservablesgenerator = new IDObservablesGenerator();
	 idparametersgenerator = new IDParametersGenerator();
	 idreactionsgenerator = new IDReactionsGenerator();
	 idrulesgenerator = new IDRulesGenerator();
	 idrulesgenerator.setCurrentIDRules(reaction_rules.size());
	 idspeciesgenerator = new IDSpeciesGenerator();
	 idspeciesgenerator.setCurrentIDSpecies(species.size());

         
    }
    
    
    
    
}
