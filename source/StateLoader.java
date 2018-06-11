// This class knows how to load BioNetGens state from a file

import java.io.*; // For file manipulation and object writing
import java.util.*; // For vectors and arraylists
import java.util.logging.*; // For error logs
import javax.swing.*;
import java.awt.Rectangle;
import java.awt.Dimension;

import java.beans.XMLDecoder;

public class StateLoader
{
    transient protected boolean debug_statements = true;
    private GUI the_gui;
    private Model the_model;

    StateLoader( GUI the_gui, Model the_model )
    {
	this.the_gui = the_gui;
	this.the_model = the_model;
    }

    boolean load( File load_file )
    {
	//declared here only to ensure visibilty in finally clause
	
	    
	ObjectInput input = null;
	try{
	    //use buffering
            
            try
            {
                InputStream file = new FileInputStream( load_file );
                InputStream buffer = new BufferedInputStream( file );
                input = new ObjectInputStream ( buffer );
                State state = (State)input.readObject();
                unpackState( state );
                the_gui.refreshAll();
            }
            catch ( ClassCastException oldversion )
            {
         
                /*
                InputStream file = new FileInputStream( load_file );
                InputStream buffer = new BufferedInputStream( file );
                input = new ObjectInputStream ( buffer );
                
	    // Load the panels the old way
	    
            loadWidgetPanel(input, the_gui.getSpeciesPalette() );
	    loadWidgetPanel( input, the_gui.getMoleculePalette() );
            loadWidgetPanel( input, the_gui.getReactionRulePalette() );
            loadWidgetPanel( input, the_gui.getEditorPanel() );
            loadWidgetPanel( input, the_gui.getObservablesPalette() );
            loadCurrentID( input );
	    //loadJournal( input, the_gui.getJournalPane() );
            loadSimConfig( input, the_gui.getSimulationConfig() );
            loadModel( input, the_gui.getModel() );
                 **/
                 
            }
            
	}
	catch( FileNotFoundException e)
	    {
		the_gui.getEditorPanel().displayError("State Loading Error", "File not found");
                //e.printStackTrace();
            }
        catch (EOFException eof)
	  {
	      the_gui.getEditorPanel().displayError( "State Load Exception Caught",
						  "Premature end of file encountered. Contact support at support@bionetgen.com");
              eof.printStackTrace();
        } 
	catch( IOException e)
	    {
		the_gui.getEditorPanel().displayError("State Loading Error", e.getMessage());
                e.printStackTrace();
        }
	catch( ClassNotFoundException e)
	    {
		the_gui.getEditorPanel().displayError("State Loading Error", e.getMessage());
                e.printStackTrace();
        }
	try {
	    if ( input != null ) {
		//close "input" and its underlying streams
		input.close();
	    }
	}
	catch (IOException ex)
	  {
	      the_gui.getEditorPanel().displayError( "State Load Exception Caught",
						  "Could not close input stream");
	      //fLogger.log(Level.SEVERE, "Cannot close input stream.", ex);
	  }     
	  
        
	if (debug_statements) System.out.println("State Load Successful.");
	return true;
    }

    
    // Have to set the panel fields manually because we don't want to be forced
    // to reapply transfer handlers and scroll panes on each state load
    
    
    boolean loadMoleculePalette( ObjectInput input, MoleculePalette panel ) throws IOException
    {
        MoleculePalette temp = null;
        //JFrame temp_frame = null;
        boolean make_frame_visible = false;
        
        try 
        {
            temp = (MoleculePalette)input.readObject();
            //temp_frame = (JFrame) input.readObject();
            make_frame_visible = input.readBoolean();
        }
        catch (EOFException eof)
        {
            if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
        } 
        catch (OptionalDataException ode)
        {
            if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
        }
        catch (InvalidClassException ice)
        {                  
              if (debug_statements) System.out.println("Invalid Class Exception");
              if (debug_statements) System.out.println(ice.getMessage());
              if (debug_statements) System.out.println(ice.toString());
              the_gui.getEditorPanel().displayError( "Load Failure","The data file you attempted to load appears to be from an incompatible version of BioNetGen." );
        }
        catch (IOException ioe)
        {                  
              if (debug_statements) System.out.println("IOException on read object");
              if (debug_statements) System.out.println(ioe.getMessage());
              if (debug_statements) System.out.println(ioe.toString());
        }
        catch (ClassNotFoundException cnf)
        {
            if (debug_statements) System.out.println("ClassNotFoundException");
        }
        catch ( ClassCastException cce )
        {
        the_gui.getEditorPanel().displayError( "Error Loading SpeciesPanel",
                                                "Class Cast Exception:"
                                                + cce.getMessage() );
        }
        
        if ( temp == null ) return false;
        
        panel.setContainers( temp.getAllContainers() );
	panel.setComponents( temp.getAllComponents() );
	panel.setSpecies( temp.getAllSpecies() );
	panel.setEdges( temp.getAllEdges() );
        panel.setVerticalOffset( temp.getVerticalOffset() );
        panel.setArea( temp.getArea() );
        
        int new_height = temp.getHeight();
        int new_width = temp.getWidth();
        int new_x = temp.getX();
        int new_y = temp.getY();
        
        //the_gui.getMoleculePaletteFrame().setBounds( temp_frame.getBounds() );
        //panel.reshape( new_x, new_y, new_width, new_height ); // depricated
        
        // Update Scrollbar information
        panel.setPreferredSize( temp.getArea() );
        panel.revalidate();
        
        // Update visible/invisible information and "view" checkboxes
        // ** BROKEN - the condition is always false
        if ( make_frame_visible == true ) 
        {
            the_gui.getMoleculePaletteFrame().setVisible( true );
            the_gui.frameMadeVisible( the_gui.getMoleculePaletteFrame() );
        }
        else 
        {
            the_gui.getMoleculePaletteFrame().setVisible( false );
            the_gui.frameMadeInvisible( the_gui.getMoleculePaletteFrame() );
        }
        
        linkContainingPanel( panel );
        
        panel.repaint();
        
        return true;
    }
    
    boolean loadReactionRulePalette( ObjectInput input, ReactionRulePalette panel ) throws IOException
    {
        ReactionRulePalette temp = null;
        //JFrame temp_frame = null;
        boolean make_frame_visible = false;
        
        try 
        {
            temp = (ReactionRulePalette)input.readObject();
            //temp_frame = (JFrame)input.readObject();
            make_frame_visible = input.readBoolean();
        }
        catch (EOFException eof)
        {
            if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
            the_gui.getEditorPanel().displayError( "Error Loading Reaction Rule Palette",
                                                    eof.getMessage() );
        } 
        catch (OptionalDataException ode)
        {
            if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
            the_gui.getEditorPanel().displayError( "Error Loading Reaction Rule Palette",
                                                    ode.getMessage() );
        }
        catch (InvalidClassException ice)
        {                  
              if (debug_statements) System.out.println("Invalid Class Exception");
              if (debug_statements) System.out.println(ice.getMessage());
              if (debug_statements) System.out.println(ice.toString());
              the_gui.getEditorPanel().displayError( "Load Failure","The data file you attempted to load appears to be from an incompatible version of BioNetGen." );
        }
        catch (IOException ioe)
        {                  
              if (debug_statements) System.out.println("IOException on read object");
              if (debug_statements) System.out.println(ioe.getMessage());
              if (debug_statements) System.out.println(ioe.toString());
              the_gui.getEditorPanel().displayError( "Error Loading Reaction Rule Palette",
                                                    ioe.getMessage() );
        }
        catch (ClassNotFoundException cnf)
        {
            if (debug_statements) System.out.println("ClassNotFoundException");
            the_gui.getEditorPanel().displayError( "Error Loading Reaction Rule Palette",
                                                    cnf.getMessage() );
        }
        catch ( ClassCastException cce )
        {
        the_gui.getEditorPanel().displayError( "Error Loading Reaction Rule Palette",
                                                "Class Cast Exception:"
                                                + cce.getMessage() );
        }
        
        if ( temp == null ) return false;
        
        panel.setContainers( temp.getAllContainers() );
	panel.setComponents( temp.getAllComponents() );
	panel.setSpecies( temp.getAllSpecies() );
	panel.setEdges( temp.getAllEdges() );
        panel.setReactionRules( temp.getAllReactionRules() );
        panel.setVerticalOffset( temp.getVerticalOffset() );
        panel.setArea( temp.getArea() );
        
        int new_height = temp.getHeight();
        int new_width = temp.getWidth();
        int new_x = temp.getX();
        int new_y = temp.getY();
        
        //the_gui.getReactionRulePaletteFrame().setBounds( temp_frame.getBounds() );
        //panel.reshape( new_x, new_y, new_width, new_height ); // depricated
        panel.setPreferredSize( temp.getArea() );
        panel.revalidate();
        
        // Update visible/invisible information and "view" checkboxes
        if ( make_frame_visible == true ) 
        {
            the_gui.getReactionRulePaletteFrame().setVisible( true );
            the_gui.frameMadeVisible( the_gui.getReactionRulePaletteFrame() );
        }
        else 
        {
            the_gui.getReactionRulePaletteFrame().setVisible( false );
            the_gui.frameMadeInvisible( the_gui.getReactionRulePaletteFrame() );
        }
        
        
        linkContainingPanel( panel );
        
        panel.repaint();
        
        return true;
    }
    
    boolean loadSpeciesPalette( ObjectInput input, SpeciesPalette panel ) throws IOException
    {
        loadWidgetPanel( input, panel );
       
        return true;
    }
    
    
    boolean loadWidgetPanel( ObjectInput input, WidgetPanel panel ) throws IOException 
    {
   	    
	// Read WidgetPanel from stream
		try
		    {
			//WidgetPanel temp = (WidgetPanel)input.readObject();
                        //Rectangle frame_bounds = (Rectangle)input.readObject();
			//the_gui.setNetPanel( temp );
			
                            Vector<Operator> operators = new Vector<Operator>();
                            Vector<ReactionRule> rules = new Vector<ReactionRule>();
                            
                            panel.setSpecies( (Vector<Species>)input.readObject() );
                            panel.setContainers( (Vector<BioContainer>)input.readObject() );
                            panel.setComponents( (Vector<BioComponent>)input.readObject() );
                            panel.setEdges( (Vector<Edge>)input.readObject() );
                            operators.addAll( (Vector<Operator>)input.readObject() );
                            rules.addAll( (Vector<ReactionRule>)input.readObject() );
                            panel.setPatterns( (Vector<Pattern>)input.readObject() );

                            
                            panel.setOperators( convertOperators(operators, panel) );
                            panel.setReactionRules( convertRules( rules, panel ) );
                            
                                
                                
                            int new_height = (int)input.readInt();
                            int new_width = (int)input.readInt();
                            int new_x = (int)input.readInt();
                            int new_y = (int)input.readInt();
                            Dimension preferred_size = (Dimension)input.readObject();
                        
                        //the_gui.getEditorFrame().setBounds( frame_bounds );
                        
			// Update the containing panel for the new widgets
			linkContainingPanel( panel );
                        
			// Explict repaint in case the loaded file contained no widgets to
			// trigger a repaint
                        panel.setPreferredSize( preferred_size );
                   
                        //panel.reshape( new_x, new_y, new_width, new_height );
                        
                        //panel.setPreferredSize( dim );
                        panel.invalidate();
                        panel.revalidate();
                        panel.repaint();
                        
                        if ( panel instanceof WidgetPalette )
                        {
                            ((WidgetPalette)panel).compressDisplay();
                        }
                        
		    }
		catch (EOFException eof)
		    {
			if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
                        the_gui.getEditorPanel().displayError( "Error Loading Widget Panel",
                                                    eof.getMessage() );
		    } 
                catch (InvalidClassException ice)
                {                  
                    if (debug_statements) System.out.println("Invalid Class Exception");
                    if (debug_statements) System.out.println(ice.getMessage());
                    if (debug_statements) System.out.println(ice.toString());
                    the_gui.getEditorPanel().displayError( "Load Failure","The data file you attempted to load appears to be from an incompatible version of BioNetGen." );
                    ice.printStackTrace();
                }
		catch (OptionalDataException ode)
		    {
			if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
                        the_gui.getEditorPanel().displayError( "Error Loading Widget Panel",
                                                    ode.getMessage() );
		    }
		catch (IOException ioe)
		    {                  
	      if (debug_statements) System.out.println("IOException on read object");
	      if (debug_statements) System.out.println(ioe.getMessage());
	      if (debug_statements) System.out.println(ioe.toString());
              the_gui.getEditorPanel().displayError( "Error Loading Widget Panel",
                                                    ioe.getMessage() );
		    }
		catch (ClassNotFoundException cnf)
		    {
			if (debug_statements) System.out.println("ClassNotFoundException");
                        the_gui.getEditorPanel().displayError( "Error Loading Widget Panel",
                                                    cnf.getMessage() );
		    }
                catch ( ClassCastException cce )
                {
                    the_gui.getEditorPanel().displayError( "Error Loading WidgetPanel",
                                                            "Class Cast Exception:"
                                                            + cce.getMessage() );
                }
                
		return true;

    }

    boolean loadCurrentID( ObjectInput input ) throws IOException 
    {
   	    
	// Read WidgetPanel from stream
		try
		    {
			long current_id = input.readLong();
			IDGenerator temp = new IDGenerator();
                        temp.setCurrentID( current_id );
                }
		catch (EOFException eof)
		    {
			if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
                        the_gui.getEditorPanel().displayError( "Error loading Current ID",
                                                    eof.getMessage() );
		    } 
		catch (OptionalDataException ode)
		    {
			if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
                        the_gui.getEditorPanel().displayError( "Error loading Current ID",
                                                    ode.getMessage() );
                }
		catch (IOException ioe)
		    {                
                        
                        if (debug_statements) System.out.println("IOException on read long integer (current id)");
                        if (debug_statements) System.out.println(ioe.getMessage());
                        if (debug_statements) System.out.println(ioe.toString());
                        the_gui.getEditorPanel().displayError( "Error loading Current ID",
                                                    ioe.getMessage() );
		    }
		
                
		return true;

    }
    
    boolean loadModel( ObjectInput input, Model the_model ) 
    {           
	// Read WidgetPanel from stream
		try
		    {
			Model temp = (Model)input.readObject();
			
                        Vector<ReactionRule> rules = temp.getReactionRules();
                        Vector<ReactionRule> new_rules = convertRules( rules, the_gui.getReactionRulePalette() );
                        temp.setReactionRules( new_rules );
                        
                        the_gui.setModel( temp );
			temp.setGUI(the_gui);
                        
                        Configuration config = the_gui.getConfig();
                        File engine_file = config.getEngineFile();
                      
                        String engine_path = "";
                        if ( engine_file != null )
                        {
                            engine_path = engine_file.getAbsolutePath();
                        }
                        
                        String engine_args = the_gui.getSimulationConfig().getEngineArguments();
                        temp.setupEngine( engine_path, engine_args );
                        temp.setOutputBNGLFile( config.getBNGLOutputFile() );
		    }
		catch (EOFException eof)
		    {
			if (debug_statements) System.out.println("eof encountered" + eof.getMessage());
                        the_gui.getEditorPanel().displayError( "Error loading Model",
                                                    eof.getMessage() );
		    } 
		catch (OptionalDataException ode)
		    {
			if (debug_statements) System.out.println("OptionalDataException" + ode.getMessage());
                        the_gui.getEditorPanel().displayError( "Error loading Model",
                                                    ode.getMessage() );
		    }
		catch (IOException ioe)
		    {                  
                        if (debug_statements) System.out.println("IOException on read object");
                        if (debug_statements) System.out.println(ioe.getMessage());
                        if (debug_statements) System.out.println(ioe.toString());
                        the_gui.getEditorPanel().displayError( "Error loading Model",
                                                    ioe.getMessage() );
		    }
		catch (ClassNotFoundException cnf)
		    {
			if (debug_statements) System.out.println("ClassNotFoundException");
                        the_gui.getEditorPanel().displayError( "Error loading Model",
                                                    cnf.getMessage() );
		    }
                catch ( ClassCastException cce )
                {
                    the_gui.getEditorPanel().displayError( "Error Loading Model",
                                                            "Class Cast Exception:"
                                                            + cce.getMessage() );
                }
		
		
		return true;
    }
    
    boolean loadSimConfig( ObjectInput input, SimulationConfig sc ) throws IOException, ClassNotFoundException
    {
 	// mlf 9 feb 06 commented out to allow for revision: START
        //sc.setSampleTimes( (String)input.readObject() );
        
        //sc.setNumberOfSteps((String)input.readObject() );
        
        //sc.setStepLength((String)input.readObject() );
        
        //sc.setMaxIterations((String)input.readObject() );
        
        //sc.setMaxAggregation((String)input.readObject() );
        
        //sc.setAbsoluteErrorTolerance((String)input.readObject() );
        
        //sc.setRelativeErrorTolerance((String)input.readObject() );
        
        //sc.setUpdateInterval((String)input.readObject() );
        
        //sc.setNumberOfRuns((String)input.readObject() );
        
        //sc.setFlyItrInit((String)input.readObject() );
        
        //sc.setOverwriteArg(input.readBoolean() );
  
        //sc.setGenerateNetworkArg(input.readBoolean() );
        
        //sc.setCompArg( input.readBoolean() );
 
        //sc.setODEArg(input.readBoolean() );

        //sc.setSSAArg(input.readBoolean() );
 
        //sc.setSMBLArg(input.readBoolean() );
        
        //sc.setMatlabArg(input.readBoolean() );
        
        //sc.setGraphArg(input.readBoolean() );
        
        //sc.setAllArg(input.readBoolean() );
        
        //sc.setDebugArg(input.readBoolean() );
 	// mlf 9 feb 06 commented out to allow for revision: END
        
        return true;
    }
    
    boolean loadJournal( ObjectInput input, JTextArea jf ) throws IOException
    {
        try
        {
                        JTextArea panel = new JTextArea();
                        JTextArea temp = (JTextArea)input.readObject();
                        Rectangle frame_bounds = (Rectangle)input.readObject();
			//the_gui.setNetPanel( temp );
			
                        int new_height = temp.getHeight();
                        int new_width = temp.getWidth();
                        int new_x = temp.getX();
                        int new_y = temp.getY();
        
                        
                        the_gui.getJournalFrame().setBounds( frame_bounds );
                        
			// Explict repaint in case the loaded file contained no widgets to
			// trigger a repaint
                        panel.setPreferredSize( temp.getPreferredSize() );
                        panel.revalidate();
			//panel.reshape( new_x, new_y, new_width, new_height ); // depricated
                        the_gui.getJournalFrame().setVisible( false );
                        panel.repaint();
                        the_gui.getJournalFrame().setVisible( true );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
        
	return true;
    }
    
    void xmlLoad()
    {

	XMLDecoder d = null;
	try 
	    {
		d = new XMLDecoder(new BufferedInputStream(new FileInputStream("Test.xml")));
		
                
		MoleculePalette molecule_palette = (MoleculePalette)d.readObject();
		WidgetPanel editor_panel = (WidgetPanel)d.readObject();
	
                // fill in as other panels are completed
        }
	catch( IOException e )
	    {
		the_gui.getEditorPanel().displayError("XML State Load Error", e.getMessage() );
	    }
	
	d.close();
    }
    
    public void linkContainingPanel(WidgetPanel panel)
    {
        // Update the containing panel for the new widgets
			for (int i = 0; i < panel.getAllContainers().size(); i++ )
			    ((BioContainer)panel.getAllContainers().get(i)).setContainingPanel( panel );

			for (int i = 0; i < panel.getAllComponents().size(); i++ )
			    ((BioComponent)panel.getAllComponents().get(i)).setContainingPanel( panel );
			
			for (int i = 0; i < panel.getAllSpecies().size(); i++ )
			    ((Species)panel.getAllSpecies().get(i)).setContainingPanel( panel );
			
			for (int i = 0; i < panel.getAllEdges().size(); i++ )
			    ((Edge)panel.getAllEdges().get(i)).setContainingPanel( panel );

			
			for (int i = 0; i < panel.getAllOperators().size(); i++ )
			    {
				((Operator)panel.getAllOperators().get(i)).setContainingPanel( panel );
			    }
                        
                        for (int i = 0; i < panel.getAllReactionRules().size(); i++ )
			    {
				((ReactionRule)panel.getAllReactionRules().get(i)).setContainingPanel( panel );
			    }
                         for (int i = 0; i < panel.getAllReactions().size(); i++ )
			    {
				((Reaction)panel.getAllReactions().get(i)).setContainingPanel( panel );
			    }
                        for (int i = 0; i < panel.getAllPatterns().size(); i++ )
			    {
				((Pattern)panel.getAllPatterns().get(i)).setContainingPanel( panel );
			    }
                         for (int i = 0; i < panel.getAllGroups().size(); i++ )
			    {
				((Group)panel.getAllGroups().get(i)).setContainingPanel( panel );
			    }
                            for (int i = 0; i < panel.getAllFlickrLabels().size(); i++ )
			    {
				((FlickrLabel)panel.getAllFlickrLabels().get(i)).setContainingPanel( panel );
			    }
                        
    }
 
    
    public Vector<ReactionRule> convertRules(Vector<ReactionRule> rules, WidgetPanel panel) 
    {
        Iterator rule_itr = rules.iterator();
        while( rule_itr.hasNext() )
        {
            ReactionRule rr = (ReactionRule)rule_itr.next();
            Vector<Operator> ops = rr.getOperators();
            Vector<Operator> new_ops = convertOperators( ops, panel );
            rr.setOperators( new_ops );     
        }
        
        return rules;
    }

    public Vector<Operator> convertOperators(Vector<Operator> operators, WidgetPanel panel) 
    {
                Vector<Operator> new_operators = new Vector<Operator>();
                    
        // Convert operators to the new version
                            Iterator op_itr = operators.iterator();
                            while( op_itr.hasNext() )
                            {
                                Operator op = (Operator)op_itr.next();
                           
                                if ( op instanceof Forward )
                                {
                                    new_operators.add( op );
                                }
                                else if ( op instanceof ForwardAndReverse )
                                {
                                    new_operators.add( op );
                                }
                                else if ( op instanceof Plus )
                                {
                                    new_operators.add( op );
                                }
                                else // old version so convert
                                {
                                    String label = op.getLabel();
                                    int x = op.getX();
                                    int y = op.getY();
                                    String icon_url = op.image_url;
                                    boolean template = op.template;
                                    WidgetPanel containing_panel = panel;
                                
                                    if ( label.equals("plus") )
                                    {
                                        Plus new_op = new Plus( x, y, containing_panel );
                                        panel.addOperator( new_op );
                                    }
                                    else if (label.equals("forward"))
                                    {
                                        Forward new_op = new Forward( x, y, containing_panel );
                                        panel.addOperator( new_op );
                                    }
                                    else if (label.equals("forward_and_reverse"))
                                    {
                                        ForwardAndReverse new_op = new ForwardAndReverse( x, y, containing_panel );
                                        panel.addOperator( new_op );
                                    }
                            }
                            }
                                
                                return new_operators;
                            }
    
    public boolean unpackWidgetPanel( State.WidgetPanelState state, WidgetPanel panel )
    {
         panel.setSpecies(state.species);
         panel.setContainers(state.containers);
         panel.setComponents(state.components);
         panel.setEdges(state.edges);
         panel.setOperators(state.operators);
         panel.setReactionRules(state.rules);
         panel.setPatterns(state.patterns);
         panel.setGroups(state.groups);
         panel.setFlickrLabels(state.flickr_labels);
         panel.setReactions(state.reactions);
         
         //panel.setHeight(state.height);
         //panel.setWidth(state.width);
         //panel.setX(state.x);
         //panel.setY(state.y);
        
         linkContainingPanel( panel );
         
         // Load legacy rates and concentrations into ModelParameters
         Iterator species_itr = panel.getAllSpecies().iterator();
                            while ( species_itr.hasNext() )
                            {
                                Species s = ((Species)species_itr.next());
                                
                                // add legacy parameters
                                the_gui.getModelParameters().addParameter( s.getConcentrationParameter() );
                            }
         
                             Iterator reaction_rules_itr = panel.getAllReactionRules().iterator();
                            while ( reaction_rules_itr.hasNext() )
                            {
                                ReactionRule r = ((ReactionRule)reaction_rules_itr.next());
                                
                                // add legacy parameters
                                the_gui.getModelParameters().addParameter( r.getForwardRateParameter() );
                                
                                if ( r.isReversable() )
                                {
                                    the_gui.getModelParameters().addParameter( r.getReverseRateParameter() );
                                }
                            }
         
        if ( panel instanceof WidgetPalette )
        {
            ((WidgetPalette)panel).compressDisplay();
        }
                             
        panel.autoZoom(true);
         
        return true;
    }
    
    public boolean unpackState( State state ) 
    {
            // Package Species Palette
            unpackWidgetPanel(state.species_panel_state, the_gui.getSpeciesPalette());
            unpackWidgetPanel(state.molecule_panel_state, the_gui.getMoleculePalette());
            unpackWidgetPanel(state.rule_panel_state, the_gui.getReactionRulePalette());
            unpackWidgetPanel(state.editor_panel_state, the_gui.getEditorPanel());
            unpackWidgetPanel(state.obser_panel_state, the_gui.getObservablesPalette());
            //unpackWidgetPanel(state.reaction_panel_state, the_gui.getReactionPalette());
            unpackSimConfig( state.simulation_config_state, the_gui.getSimulationConfig() );
            unpackModelParams( state.model_parameters_state, the_gui.getModelParameters() );
            (new IDGenerator()).setCurrentID( state.current_id ); 
                        
                // Restore the model object
                        the_gui.setModel( state.model );
                        Model model = the_gui.getModel();
                        model.setGUI( the_gui );
                        
                        //state.model_parameters_state.setGUI( the_gui );
                        //state.model_parameters_state.setModel( model );
                        
                        //the_gui.setModelParameters( state.model_parameters );
                        
                        
                        Configuration config = the_gui.getConfig();
                        File engine_file = config.getEngineFile();
                      
                        String engine_path = "";
                        if ( engine_file != null )
                        {
                            engine_path = engine_file.getAbsolutePath();
                        }
                        
                        String engine_args = the_gui.getSimulationConfig().getEngineArguments();
                        model.setupEngine( engine_path, engine_args );
                        model.setOutputBNGLFile( config.getBNGLOutputFile() );
                        
                        SpeciesPalette sp = the_gui.getSpeciesPalette();
                        ReactionRulePalette rrp = the_gui.getReactionRulePalette();
                        MoleculePalette mp = the_gui.getMoleculePalette();
                        ObservablesPalette op = the_gui.getObservablesPalette();
                        
                        
                        // relink all the widgets in the model to a widget panel
                            Iterator species_itr = model.getSpecies().iterator();
                            while ( species_itr.hasNext() )
                            {
                                Species s = ((Species)species_itr.next());
                                s.setContainingPanel(sp);
                            }
                            
                            Iterator observables_itr = model.getObservables().iterator();
                            while ( observables_itr.hasNext() )
                            {
                                ((Group)observables_itr.next()).setContainingPanel(op);
                            }
                            
                            Iterator reaction_rules_itr = model.getReactionRules().iterator();
                            while ( reaction_rules_itr.hasNext() )
                            {
                                ReactionRule r = ((ReactionRule)reaction_rules_itr.next());
                                r.setContainingPanel(rrp);
                                
                                // add legacy parameters
                                the_gui.getModelParameters().addParameter( r.getForwardRateParameter() );
                                
                                if ( r.isReversable() )
                                {
                                    the_gui.getModelParameters().addParameter( r.getReverseRateParameter() );
                                }
                            }
                            
                            Iterator molecule_types_itr = model.getMoleculeTypes().iterator();
                            while ( molecule_types_itr.hasNext() )
                            {
                                ((MoleculeType)molecule_types_itr.next()).setContainingPanel(mp);
                            }
                            
            return true;            
    }
    
    public State loadState(ObjectInput input ) 
    {
        State state = null;
        
        try
        {
            state = (State)input.readObject();
        }
        catch ( Exception e )
        {
            the_gui.getEditorPanel().displayError( " Error Loading File",  "The message was: " + e.getMessage() );
        }
                
            
        return state;
    }
    
    public void unpackModelParams( State.ModelParametersState state, ModelParameters mp ) 
    {
        if ( state != null ) mp.setParameters( state.parameters );
    }
    
    public void unpackSimConfig( State.SimulationConfigState state, SimulationConfig sc ) 
    {
	sc.setNetworkMaxnummolecules(state.network_maxnummolecules);
	sc.setNetworkMaxnumruleapplications(state.network_maxnumruleapplications);
	sc.setEquilibrationTimebetweenequilchecks(state.equilibration_timebetweenequilchecks);
	sc.setEquilibrationMaxnumequilchecks(state.equilibration_maxnumequilchecks);
	sc.setSimulationATOL(state.simulation_atol);
	sc.setSimulationRTOL(state.simulation_rtol);
	sc.setTimecourseNsteps(state.timecourse_nsteps);
	sc.setTimecourseTend(state.timecourse_tend);
	sc.setNetworkChoosefromfiles(state.network_choosefromfiles);
	sc.setNetworkGenerateNew(state.network_generatenew);
	sc.setEquilibrationEquilibrate(state.equilibration_equilibrate);
	sc.setEquilibrationNoEquilibrate(state.equilibration_noequilibrate);
	sc.setSimulationODE(state.simulation_ODE);
	sc.setSimulationSSA(state.simulation_SSA);
	sc.setTimecourseTendnsteps(state.timecourse_tendnsteps);
	sc.setTimecourseSampletimes(state.timecourse_sampletimes);
	sc.setNetworkIsomorphism(state.network_isomorphism);
	sc.setSimulationSparse(state.simulation_sparse);
	sc.setSimulationReadNetFile(state.simulation_readnetfile);
	sc.setOptionsVerbose(state.options_verbose);
	sc.setOptionsSBML(state.options_sbml);	
	sc.setTimecourseSampletimesvalues(state.timecourse_sampletimes_values);
	sc.setNetworkMaxStoichLimitValues(state.network_maxstoichlimits_values);
	sc.setEquilibrationBooleanValues(state.equilibration_equilbooleans_values);
    }
    
}

