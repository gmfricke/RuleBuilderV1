// BioNetGen - Biological Network Generation
// Copyright 2004 Los Alamos National Labs and Go Figure Software
// Author: Matthew Fricke
// Version 0.10alpha Prototype
// 10/09/2004

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure
import javax.swing.text.*; // For styled text documents

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
import java.io.*; // for file io
import javax.swing.filechooser.*; // for GUI file save and open
import javax.swing.Timer; // For splash screen delay
import ptolemy.plot.Plot; // For InternalFrameListener Plotter search
import javax.help.*; // for JavaHelp system
//import javax.help.CSH; // for HelpFromSource action listener

public class GUI implements Runnable
{
    protected boolean debug_statements = true;
    
    Model the_model;
    Thread the_model_thread;
    
    // Lets try using internal windows
    JDesktopPane desktop;
    
    private Configuration config;
    private SimulationConfig simulation_config;
    private ModelParameters model_parameters;
     
    // The singleton undo/redo manager
    EditsManager edits_manager = new EditsManager();
    
    private JTextPane notes_pane = new JTextPane();
    private JPanel notes_panel = new JPanel();
    
    // Windows needed for our program
    private JFrame main_frame;
    private JInternalFrame notes_frame;
    private JInternalFrame molecule_frame;
    private JInternalFrame search_frame;
    private JInternalFrame species_frame;
    private JInternalFrame reaction_rule_frame;
    private JInternalFrame reaction_frame;
    private JInternalFrame observables_frame;
    private JInternalFrame editor_frame;
    private Plotter simulation_frame;
    private JInternalFrame model_summary_frame;
    private JInternalFrame engine_output_log_frame;
    private JInternalFrame journal_frame;
    private JSlider zoom_slider;
    
    private JTextArea journal_pane;
    
    protected JToolBar edit_toolbar;
    protected JToolBar zoom_toolbar;
    
    private HelpSet helpset;
    private HelpBroker help_broker;
    
    private JLabel status_bar;
    
    private String notes = new String();
    
    private JMenuBar editor_menu_bar;
    private JMenu file_menu;
    
    private JScrollPane search_scroll_pane;
    private JScrollPane molecule_scroll_pane;
    private JScrollPane species_scroll_pane;
    private JScrollPane observables_scroll_pane;
    private JScrollPane reaction_rule_scroll_pane;
    private JScrollPane reaction_scroll_pane;
    private JScrollPane editor_scroll_pane;
    private JScrollPane notes_scroll_pane;
    
    private SearchPalette search_palette;
    private MoleculePalette molecule_palette;
    private SpeciesPalette species_palette;
    private ReactionRulePalette reaction_rule_palette;
    private ReactionPalette reaction_palette;
    private ObservablesPalette observables_palette;
    private WidgetPanel editor_panel;
    private Plotter simulation_panel;
    private WidgetPanel model_summary_panel;
    
    private Timer timer;
    private int splash_pause = 3000;
    
    private JWindow splash_frame; //JWindow is (like) an unadorned JFrame

    private NetworkPanel net_panel;
    private RulePanel rule_panel;
    //protected SpeciesDisplayPanel species_panel;
    //protected RulesDisplayPanel rules_display_panel;
    
    private JCheckBoxMenuItem search_frame_visibility;
    private JCheckBoxMenuItem molecule_frame_visibility;
    private JCheckBoxMenuItem species_frame_visibility;
    private JCheckBoxMenuItem reaction_rule_frame_visibility;
    private JCheckBoxMenuItem observables_frame_visibility;
    private JCheckBoxMenuItem simulation_frame_visibility;
    private JCheckBoxMenuItem model_summary_frame_visibility;
    private JCheckBoxMenuItem zoom_slider_visibility;
    private JCheckBoxMenuItem debug_frame_visibility;
    private JCheckBoxMenuItem reaction_frame_visibility;
    private JCheckBoxMenuItem editor_frame_visibility;
    private JCheckBoxMenuItem engine_output_log_frame_visibility;
    
    private String config_path;

    private String version = "1.50a Beta";

    private JFileChooser fc;
    private JFileChooser xmlfc;
    private StateLoader state_loader;
    private StateSaver state_saver;

    private WidgetTransferHandler widget_transfer_handler;
    private WidgetDragHandler widget_drag_handler;
    
    private JFileChooser open_fc;    
    private JFileChooser import_fc;    
    private JFileChooser export_fc;    
    
    private JFileChooser save_fc;    
    
    private File current_save_file;
    
    public Widget copied_widget;
   
    private boolean save_needed = false;
    
    // Scrollbar listening class
    class ScrollControl implements AdjustmentListener 
    {
    
        // This method is called whenever the value of a scrollbar is changed,
        // either by the user or programmatically.
        public void adjustmentValueChanged(AdjustmentEvent evt) 
        {
            if (debug_statements) System.out.println("Scroll adjustmentValueChanged");
            
            Adjustable source = evt.getAdjustable();
    
            // getValueIsAdjusting() returns true if the user is currently
            // dragging the scrollbar's knob and has not picked a final value
            if (evt.getValueIsAdjusting()) {
                // The user is dragging the knob
                return;
            }
    
            // Determine which scrollbar fired the event
            int orient = source.getOrientation();
            if (orient == Adjustable.HORIZONTAL) {
                // Event from horizontal scrollbar
            } else {
                // Event from vertical scrollbar
            }
    
            // Determine the type of event
            int type = evt.getAdjustmentType();
            switch (type) {
              case AdjustmentEvent.UNIT_INCREMENT:
                  // Scrollbar was increased by one unit
                  break;
              case AdjustmentEvent.UNIT_DECREMENT:
                  // Scrollbar was decreased by one unit
                  break;
              case AdjustmentEvent.BLOCK_INCREMENT:
                  // Scrollbar was increased by one block
                  break;
              case AdjustmentEvent.BLOCK_DECREMENT:
                  // Scrollbar was decreased by one block
                  break;
              case AdjustmentEvent.TRACK:
                  // The knob on the scrollbar was dragged
                  break;
            }
    
            // Update container locations
            if (debug_statements) System.out.println("Scroll value " + evt);
            //refreshAll();
            //Iterator i = containers.iterator();
            //while( i.hasNext() )
	    //{
		//BioContainer bc = ((BioContainer)i.next());
                //bc.updateLocation(bc.getX(), bc.getY()+evt.getValue(),false);
	    //} 
        }
    }
    
    class InternalFrameListener implements javax.swing.event.InternalFrameListener
    {
        
        public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) 
        {
            JInternalFrame frame = e.getInternalFrame();
            String frame_title = frame.getTitle();
            
            if (debug_statements) System.out.println( frame_title + " now the active frame. Changing the toolbar listener to " + frame_title );
            
            if ( frame instanceof Plotter )
            {
                Plotter plotter = (Plotter)frame;
                if (debug_statements) System.out.println("Found Engine Output Log Frame");
                setToolbarListener( zoom_toolbar, plotter );
                setToolbarListener( edit_toolbar, plotter );
                return;
            }
            
            // Find the first widget panel in the source frame
            Component[] components = e.getInternalFrame().getContentPane().getComponents();
            if (debug_statements) System.out.println("Searching the active InternalFrame's " + components.length + " components for a WidgetPanel");
                    
            for ( int i = 0; i < components.length; i++ )
            {
                if (debug_statements) System.out.println("Found a component of type " +  components[i].getClass().getName() );
            
                if ( components[i] instanceof JScrollPane )
                {
                    if (debug_statements) System.out.println("Found a ScrollPane in the active internal frame");
                    //Component[] sp_comps = ((JScrollPane)components[i]).getComponents();
                    
                    Component[] sp_comps = ((JScrollPane)components[i]).getViewport().getComponents();
                    
                    for ( int j = 0; j < sp_comps.length; j++ )
                    {
                        if (debug_statements) System.out.println("Found a component of type " +  sp_comps[j].getClass().getName() );
          
                        if ( sp_comps[j] instanceof WidgetPanel )
                        {   
                            if (debug_statements) System.out.println("Found a WidgetPanel in the active internal frame");
                    
                            WidgetPanel current = (WidgetPanel)sp_comps[j];
                
                            // set the toolbars to use the found widgetpanel
                            // as its listener
                            setToolbarListener( zoom_toolbar, current );
                            setToolbarListener( edit_toolbar, current );
                            
                            current.setSelected(true);
                            break;
                        }
                    }
                }
            }
           
        }
        
        public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) 
        {
           
            frameMadeInvisible( (JInternalFrame) e.getSource() );
            internalFrameDeSelected( e );
        }
        
        public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) 
        {   
            
            
            internalFrameDeSelected(e);
        }
        
        public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e) 
        {
            if (debug_statements) System.out.println("Internal Frame Deactivated");
            internalFrameDeSelected(e);
        }
        
        public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent e) 
        {
            frameMadeVisible( (JInternalFrame) e.getSource() );
            internalFrameDeSelected( e );
        }
        
        public void internalFrameIconified(javax.swing.event.InternalFrameEvent e) 
        {
            if (debug_statements) System.out.println("Frame iconified");
            frameMadeInvisible( (JInternalFrame) e.getSource() );
            internalFrameDeSelected(e);
        }
        
        public void internalFrameOpened(javax.swing.event.InternalFrameEvent e) 
        {
            frameMadeVisible( (JInternalFrame) e.getSource() );
            internalFrameDeSelected( e );
        }
        
        private void internalFrameDeSelected(javax.swing.event.InternalFrameEvent e)
        {
            // Find the first widget panel in the source frame
            Component[] components = e.getInternalFrame().getContentPane().getComponents();
            if (debug_statements) System.out.println("Searching the deselected InternalFrame's " + components.length + " components for a WidgetPanel");
                    
            for ( int i = 0; i < components.length; i++ )
            {
                if (debug_statements) System.out.println("Found a component of type " +  components[i].getClass().getName() );
            
                if ( components[i] instanceof JScrollPane )
                {
                    if (debug_statements) System.out.println("Found a ScrollPane in the active internal frame");
                    //Component[] sp_comps = ((JScrollPane)components[i]).getComponents();
                    
                    Component[] sp_comps = ((JScrollPane)components[i]).getViewport().getComponents();
                    
                    for ( int j = 0; j < sp_comps.length; j++ )
                    {
                        if (debug_statements) System.out.println("Found a component of type " +  sp_comps[j].getClass().getName() );
          
                        if ( sp_comps[j] instanceof WidgetPanel )
                        {   
                            if (debug_statements) System.out.println("Found a WidgetPanel in the active internal frame");
                    
                            WidgetPanel current = (WidgetPanel)sp_comps[j];
                
                            current.setSelected( false );

                            break;
                        }
                    }
                }
            }
        }
    }
    
    /**
* Class TextAreaPrintStream
* extends PrintStream.
* A custom made PrintStream which overrides methods println(String)
* and print(String).
* Thus, when the out stream is set as this PrintStream (with System.setOut
* method), all calls to if (debug_statements) System.out.println(String) or if (debug_statements) System.out.print(String)
* will result in an output stream of characters in the JTextArea given as an
* argument of the constructor of the class.
**/

    class TextAreaPrintStream extends PrintStream 
    {

//The JTextArea to wich the output stream will be redirected.
private JTextArea textArea;
private final static String newline = "\n";
/**
* Method TextAreaPrintStream
* The constructor of the class.
* @param the JTextArea to wich the output stream will be redirected.
* @param a standard output stream (needed by super method)
**/
public TextAreaPrintStream(JTextArea area, OutputStream out) {
super(out);
textArea = area;
}

/**
* Method println
* @param the String to be output in the JTextArea textArea (private
* attribute of the class).
* After having printed such a String, prints a new line.
**/
public void println(String string) {
textArea.append( string + newline);
}

/**
* Method print
* @param the String to be output in the JTextArea textArea (private
* attribute of the class).
**/
public void print(String string) {
textArea.append(string);
}
}


    
    class BNGWindowListener implements WindowListener, WindowStateListener, WindowFocusListener
    {
        
        public void checkWM() {
        Toolkit tk = editor_frame.getToolkit();
        if (!(tk.isFrameStateSupported(Frame.ICONIFIED))) {
            displayMessage(
               "Your window manager doesn't support ICONIFIED.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_VERT))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_VERT.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_HORIZ))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_HORIZ.");
        }
        if (!(tk.isFrameStateSupported(Frame.MAXIMIZED_BOTH))) {
            displayMessage(
               "Your window manager doesn't support MAXIMIZED_BOTH.");
        } else {
            displayMessage(
               "Your window manager supports MAXIMIZED_BOTH.");
        }
    }

    public void windowClosing(WindowEvent e) {
        displayMessage("WindowListener method called: windowClosing.");

        final JInternalFrame source = (JInternalFrame)e.getSource();
        frameMadeInvisible( source );
        
        /*
        //A pause so user can see the message before
        //the window actually closes.
        ActionListener task = new ActionListener() {
            boolean alreadyDisposed = false;
            public void actionPerformed(ActionEvent e) {
                if (!alreadyDisposed) {
                    alreadyDisposed = true;
                    source.dispose();
                } else { //make sure the program exits
                    System.exit(0);
                }
            }
        };
        javax.swing.Timer timer = new javax.swing.Timer(500, task); //fire every half second
        timer.setInitialDelay(2000);        //first delay 2 seconds
        timer.start();
         */
    }

    public void windowClosed(WindowEvent e) {
        //This will only be seen on standard output.
        displayMessage("WindowListener method called: windowClosed.");
        frameMadeInvisible( (JInternalFrame) e.getSource() );
    }

    public void windowOpened(WindowEvent e) 
    {   
        displayMessage("WindowListener method called: windowOpened.");
        frameMadeVisible( (JInternalFrame) e.getSource() );
    }

    public void windowIconified(WindowEvent e) 
    {
        displayMessage("WindowListener method called: windowIconified.");
        //frameMadeInvisible( (JFrame) e.getSource() );
    }

    public void windowDeiconified(WindowEvent e) {
        displayMessage("WindowListener method called: windowDeiconified.");
        //frameMadeVisible( (JFrame) e.getSource() );
    }

    public void windowActivated(WindowEvent e) {
        displayMessage("WindowListener method called: windowActivated.");
        //frameMadeVisible( (JFrame) e.getSource() );
    }

    public void windowDeactivated(WindowEvent e) {
        displayMessage("WindowListener method called: windowDeactivated.");
    }

    public void windowGainedFocus(WindowEvent e) {
        displayMessage("WindowFocusListener method called: windowGainedFocus.");
    }

    public void windowLostFocus(WindowEvent e) {
        displayMessage("WindowFocusListener method called: windowLostFocus.");
    }

    public void windowStateChanged(WindowEvent e) {
        displayStateMessage(
          "WindowStateListener method called: windowStateChanged.", e);
    }

    void displayMessage(String msg) {
        
        if (debug_statements) System.out.println(msg + "\n");
    }

    void displayStateMessage(String prefix, WindowEvent e) {
        int state = e.getNewState();
        int oldState = e.getOldState();
        String msg = prefix
                   + "\n" + " "
                   + "New state: "
                   + convertStateToString(state)
                   + "\n" + " "
                   + "Old state: "
                   + convertStateToString(oldState);
        
        if (debug_statements) System.out.println(msg+"\n");
    }

    String convertStateToString(int state) {
        if (state == Frame.NORMAL) {
            return "NORMAL";
        }
        if ((state & Frame.ICONIFIED) != 0) {
            return "ICONIFIED";
        }
        //MAXIMIZED_BOTH is a concatenation of two bits, so
        //we need to test for an exact match.
        if ((state & Frame.MAXIMIZED_BOTH) == Frame.MAXIMIZED_BOTH) {
            return "MAXIMIZED_BOTH";
        }
        if ((state & Frame.MAXIMIZED_VERT) != 0) {
            return "MAXIMIZED_VERT";
        }
        if ((state & Frame.MAXIMIZED_HORIZ) != 0) {
            return "MAXIMIZED_HORIZ";
        }
        return "UNKNOWN";
    }
    
    
    }
    
    class ToolbarAction extends AbstractAction {
    
    private WidgetPanel panel;
    
    public ToolbarAction(String text, Icon icon, String description, char accelerator, WidgetPanel panel) 
	{

	    super(text, icon);
	    this.panel = panel;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(accelerator, 
                     Toolkit.getDefaultToolkit(  ).getMenuShortcutKeyMask(  )));
            putValue(SHORT_DESCRIPTION, description);
            
        }
    
    public void actionPerformed(ActionEvent e) 
	{
            
        
            if (debug_statements) System.out.println("ActionEvent: " + getValue(NAME) + "," + getValue(SHORT_DESCRIPTION) );
            if ( getValue(SHORT_DESCRIPTION).equals("file history") )
            {
                String path = (String)getValue(NAME);
                
                if (debug_statements) System.out.println("Attempt to open \"" + path + "\"");
                
                File load_file = new File( path );
                               
                // Give the user a chance to save work before initializing
                int result = saveWorkOption("This action will replace your current work. Do you want to save first?");
                if ( result == JOptionPane.CANCEL_OPTION ) {
                    return;
                }
                
                initialize();
                
                if ( !state_loader.load( load_file ) ) {
                    editor_panel.displayError("State Load Error",
                    "Error loading state from file");
                }
                else {
                    current_save_file = load_file;
                    String project_name = current_save_file.getName().replaceAll(".bng$","");
                    main_frame.setTitle("RuleBuilder " + version + " - " + project_name );
                }
                return;
            }
            else if ( getValue(NAME).equals("notes") )
            {
                displayNotes();
            }
            else if ( getValue(NAME).equals("Reset Window Positions") )
            {
                positionWindows();
            }
            else if ( getValue(NAME).equals("plot") )
            {
                simulation_frame.setVisible(true);
                try
                {
                    simulation_frame.setIcon(false);
                    simulation_frame.setSelected(true);
                }
                catch (Exception veto)
                {
                    veto.printStackTrace();
                }
                simulation_frame_visibility.setSelected(true);
            }
            else if ( getValue(NAME).equals("Import") )
            {
                importFile();
            }
            else if ( getValue(NAME).equals("Export") )
            {
                exportFile();
            }
            else if ( getValue(NAME).equals("search") )
            {
                search_frame.setVisible(true);
                try
                {
                    search_frame.setIcon(false);
                    search_frame.setSelected(true);
                }
                catch (Exception veto)
                {
                    veto.printStackTrace();
                }
                search_frame_visibility.setSelected(true);
            }
            else if ( getValue(NAME).equals("clear window") )
            {
                String[] choices = {"Clear","Don't Clear"};
                int choice = getEditorPanel().displayConfirm("Clear Editor Window","Are you sure you want to delete everything in the editor window?",choices, choices[1]);
                
                if ( choice == 0 )
                {
                    getEditorPanel().initialize();
                }
                else
                {
                    return;
                }
            }
            else if ( getValue(NAME).equals("cdk model") )
            {
                // Since only the editor frame can take modes make the 
                // editor selected/active
                
                try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                
                panel.setMode("cdk_model");
                status_bar.setText("Add CDK Model Mode");
            }
            else if ( getValue(NAME).equals("atom map") )
            {
                try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                
                panel.setMode("atom_map");
                status_bar.setText("Define Atom Map Mode");
            }
            else if ( getValue(NAME).equals("Model Parameters") )
            {
                model_parameters.displayDialog();
            }
            else if ( getValue(NAME).equals("undo") )
            {
                try
                {
                    edits_manager.undo();
                }
                catch ( EmptyStackException ese )
                {
                    getEditorPanel().displayWarning( "Undo Warning", "No undoable edits were found." );
                }
            }
            else if ( getValue(NAME).equals("redo") )
            {
                try
                {
                    edits_manager.redo();
                }
                catch ( EmptyStackException ese )
                {
                    getEditorPanel().displayWarning( "Redo Warning", "No redoable edits were found." );
                }
            }
            else if ( getValue(NAME).equals("Reaction Network Generation and Simulation Settings") )
            {
                simulation_config.displaySimConfigWindow();
            }
            else if ( getValue(NAME).equals("settings") )
            {
                if (debug_statements) System.out.println( "Configuration Menu Item Selected" );
                //ConfigurationLoader loader = new ConfigurationLoader();
                //config = loader.load();
                
                //if ( config == null )
                //{
                //    config = new Configuration();
                //}
                
                config.displayConfigWindow();
                
                config.writeValues();
                
                //String path = "rulebuilder.cfg";
                ConfigurationSaver cs = new ConfigurationSaver( config_path, getTheGUI() );
                cs.save( config );
                
            }
            else if ( getValue(NAME).equals("new") )
		{
                    int result = saveWorkOption("This action will replace your current work. Do you want to save first?");
                    if ( result == JOptionPane.CANCEL_OPTION )
                    {
                        return;
                    }
                        
                    initialize();
        	}
	    else if ( getValue(NAME).equals("Run BioNetGen") )
		{
                            
                
                    File output_bngl = config.getBNGLOutputFile();
                    
                    if (output_bngl != null ) output_bngl.deleteOnExit();
                    
                    if ( output_bngl == null )
                                {
                                //getEditorPanel().displayError("Error Running Modeling Engine","A path to the output BNGL file has not been defined under settings.");
                                config.displayConfigWindow();
                                return;
                        
                                }
                    
                            the_model.setOutputBNGLFile( output_bngl );
                            
                            File engine_file = config.getEngineFile();
                            
                            if ( engine_file == null )
                             {
                                //getEditorPanel().displayError("Error Running Modeling Engine","A path to the modeling engine has not been defined under settings.");
                                config.displayConfigWindow();
                                return;
                        
                             }
                            
                            String engine_path = engine_file.getAbsolutePath();
                        
                            if ( engine_path == null )
                             {
                                //getEditorPanel().displayError("Error Running Modeling Engine","A path to the modeling engine has not been defined under settings.");
                                config.displayConfigWindow();
                                return;
                        
                             }
                            
                            the_model.setupEngine( engine_path, getSimulationConfig().getEngineArguments() );
                            
                            if ( the_model.getReactionRules().isEmpty() )
                            {
                                getEditorPanel().displayError("Error Running Modeling Engine","No Reaction Rules have been defined.");
                                return;
                            }
                            
                            if ( the_model.getSpecies().isEmpty() )
                            {
                                getEditorPanel().displayError("Error Running Modeling Engine","No Species have been defined.");
                                return;
                            }
                            
                            //getSimulationConfig().displaySimConfigWindow();
                            
                            try
                            {
                                the_model.update();
                            }
                            catch ( Exception exp )
                            {
                                getEditorPanel().displayError("Error Running Modeling Engine", exp.getMessage() );
                                exp.printStackTrace();
                            }
                }
            else if ( getValue(NAME).equals("open") )
		{
		    //Set up the file chooser.
		    if (open_fc == null) {
			open_fc = new JFileChooser();
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			//open_fc.addChoosableFileFilter(new XMLFilter());
                        //open_fc.addChoosableFileFilter(new NetFilter());
                        //open_fc.addChoosableFileFilter(new BNGLFilter());
                        //open_fc.addChoosableFileFilter(new BNGFilter());
                        
			open_fc.setAcceptAllFileFilterUsed(false);
			
                        open_fc.setFileFilter( new BNGFilter() );
                        
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		    }
		    
		    //Show it.
		    int returnVal = open_fc.showDialog(main_frame,"open");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            String path = open_fc.getSelectedFile().getAbsolutePath();
                            
                            
                            if ( open_fc.getFileFilter() instanceof BNGFilter )
                            {
                                //append .bng if not already the extension
                               if (!path.matches(".*\\.bng$"))
                               {
                                   path = path+".bng";
                               }
                                
                               File load_file = new File( path );
                               
                                // Give the user a chance to save work before initializing
                                int result = saveWorkOption("This action will replace your current work. Do you want to save first?");
                                if ( result == JOptionPane.CANCEL_OPTION )
                                {
                                    return;
                                }
                                
                                initialize();
			    
                                if ( !state_loader.load( load_file ) ) 
			        {
			    	   editor_panel.displayError("State Load Error",
						       "Error loading state from file");
			        }
                                else
                                {
                                     current_save_file = load_file;
                                     String project_name = current_save_file.getName().replaceAll(".bng$","");
                                     main_frame.setTitle("RuleBuilder " + version + " - " + project_name );
                                     
                  
                                     if ( getConfig().addToFileHistory( path ) )
                                     {
                                         // If first file add a separator
                                        if ( getConfig().getFileHistory().size() == 1 )
                                        {
                                            file_menu.addSeparator();
                                        }
                                        
                                        ToolbarAction file_history_entry = new ToolbarAction(path, new ImageIcon(""), "file history", 'o', editor_panel);
                                        file_menu.add( file_history_entry );
                                     }
                                }
                            }
                                
		    		    		    
		    }
                    
                   
                    
                    //Reset the file chooser for the next time it's shown.
		    //fc.setSelectedFile(null);
		}
            else if ( getValue(NAME).equals("load BNGL file") )
		{
		    //Set up the file chooser.
		    //if (fc == null) {
			JFileChooser fc = new JFileChooser();
			
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			fc.addChoosableFileFilter(new BNGLFilter());
			fc.setAcceptAllFileFilterUsed(false);
			
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		    //}
		    
		    //Show it.
		    int returnVal = fc.showDialog(main_frame,"Load");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            File load_file = fc.getSelectedFile();
                            String path = load_file.getAbsolutePath();
                            //append .bng if not already the extension
                            if (!path.matches(".*\\.bngl$"))
                            {
                                load_file = new File( path+".bngl");
                            }
                            
	                    initialize();
			    
                            if ( !getModel().readBNGL( load_file ) )
			    {
			    	editor_panel.displayError("BNGL Load Error",
						       "Error loading bngl file");
			    }  
			} 
		    		    		    
		    //Reset the file chooser for the next time it's shown.
		    fc.setSelectedFile(null);
		}
            else if ( getValue(NAME).equals("append") )
		{
		
			JFileChooser fc = new JFileChooser();
			
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			// fc.addChoosableFileFilter(new FPMFilter()); mlf
			fc.setAcceptAllFileFilterUsed(false);
                        
		    //Show it.
		    int returnVal = fc.showDialog(main_frame,"Load");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            File load_file = fc.getSelectedFile();
                            String path = load_file.getAbsolutePath();
                            //append .bng if not already the extension
                            if (!path.matches(".*\\.fpm$"))
                            {
                                load_file = new File( path+".fpm");
                            }
                            
	                    if ( getEditorPanel().displayConfirm("Are you sure you want to append\n" +
                            " the file named \"" + load_file.getName() + "\"?") )
                            {
                                if ( !mapFPMToReactionRules( load_file ) )
                                {
                                    editor_panel.displayError("File Load Error",
						       "Error loading .fpm file");
                                }  
                            }
			} 
		    		    		    
		    //Reset the file chooser for the next time it's shown.
		    fc.setSelectedFile(null);
		}
            else if ( getValue(NAME).equals("save") && current_save_file != null )
            {
                if ( getEditorPanel().displayConfirm("Are you sure you want to overwrite\n" +
                " the file named \"" + current_save_file.getName() + "\"?") )
                {
                    state_saver.save( current_save_file );
                }
                else
                {
                    return;
                }
            }
	    else if ( getValue(NAME).equals("save as") || (getValue(NAME).equals("save") && current_save_file == null) )
		{
		    //Set up the file chooser.
		    if (save_fc == null) {
			save_fc = new JFileChooser();
			
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			//save_fc.addChoosableFileFilter(new XMLFilter());
                        //save_fc.addChoosableFileFilter(new BNGLFilter());
                        //save_fc.addChoosableFileFilter(new BNGFilter());
                        save_fc.setAcceptAllFileFilterUsed(false);
                        save_fc.setFileFilter( new BNGFilter() );
			
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		    }
		    
		    //Show it.
		    int returnVal = save_fc.showDialog(main_frame,"Save");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            File save_file = save_fc.getSelectedFile();
                            String path = save_file.getAbsolutePath();
                            
                            
                            
                            if ( save_fc.getFileFilter() instanceof BNGFilter )
                            {
                                //append .bng if not already the extension
                                if (!path.endsWith(".bng"))
                                {
                                    path = path+".bng";
                                }
                            
                                    save_file = new File(path);
                            
                                
                                    if ( save_file.exists() )
                                {
                                    if ( !getEditorPanel().displayConfirm("Are you sure you want to overwrite\n" +
                                    " the file named \"" + save_file.getName() + "\"?") )
                                    {
                                        return;
                                    }
                                }
                            
                                
                                if (debug_statements) System.out.println("Path:" + path);
                            
                            
                                 state_saver.save( save_file );
                                 current_save_file = save_file;
                                
                                String project_name = current_save_file.getName().replaceAll(".bng$","");
                                main_frame.setTitle("Rule Builder " + version + " - " + project_name );
                            
                                if ( getConfig().addToFileHistory( path ) )
                                     {
                                         // If first file add a separator
                                        if ( getConfig().getFileHistory().size() == 1 )
                                        {
                                            file_menu.addSeparator();
                                        }
                                        
                                        ToolbarAction file_history_entry = new ToolbarAction(path, new ImageIcon(""), "file history", 'o', editor_panel);
                                        file_menu.add( file_history_entry );
                                     }
                            }
                            else if ( save_fc.getFileFilter() instanceof BNGLFilter )
                            {
                                //append .bngl if not already the extension
                                if (!path.matches(".*\\.bngl$"))
                                {
                                    path = path+".bngl";
                                }
                            
                                save_file = new File(path);
                            
                                if ( save_file.exists() )
                                {
                                    if ( !getEditorPanel().displayConfirm("Are you sure you want to overwrite\n" +
                                    " the file named \"" + save_file.getName() + "\"?") )
                                    {
                                        return;
                                    }
                                }
                            
                                
                                current_save_file = save_file;
                           
                                try
                                {
                                    getModel().writeBNGL( save_file );
                                }
                                catch( BNGLOutputMalformedException bom )
                                {
                                    getEditorPanel().displayError("Error Writing the BNGL File", bom.getMessage());
                                    return;
                                }
                            }
                            
                            else 
                            {
                                editor_panel.displayError("File Save Error","That save format is not yet supported");
                            }
			}
		    
		    //Reset the file chooser for the next time it's shown.
		    //fc.setSelectedFile(null);
		}
	else if ( getValue(NAME).equals("save as xml") )
		{
		    //Set up the file chooser.
		    if (xmlfc == null) {
			xmlfc = new JFileChooser();
			
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			xmlfc.addChoosableFileFilter(new XMLFilter());
			xmlfc.setAcceptAllFileFilterUsed(false);
			
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		    }
		    
		    //Show it.
		    int returnVal = xmlfc.showDialog(main_frame,"Save");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
			    state_saver.xmlSave( xmlfc.getSelectedFile() );
			}
		    
                    // Don't do this so the file saver remembers the last file name
		    //Reset the file chooser for the next time it's shown.
		    //xmlfc.setSelectedFile(null);
		}
	    else if ( getValue(NAME).equals("about") )
		{
		    splash_frame.setVisible(true);
		}
	    // else if ( getValue(NAME).equals("find rule") )
// 		{
// 		    //rule_panel.findAllSpecies( rule_panel.getAllComponents());
// 		    //Rule r = rule_panel.findRule( );
		    
// 		    // Rule creation failed reset rule window contents
// 		    if ( r == null )
// 			{
// 			    rule_panel.species.removeAllElements();
// 			    for ( int i = 0; i < rule_panel.components.size(); i++ )
// 				{
// 				    ((BioComponent)rule_panel.components.get(i)).setSpecies(null);
// 				}
			    
// 			    return;
// 			}

// 		    rule_panel.components.removeAllElements();
// 		    rule_panel.containers.removeAllElements();
// 		    rule_panel.edges.removeAllElements();
// 		    rule_panel.species.removeAllElements();
// 		    rule_panel.operators.removeAllElements();
// 		    the_model.addRule( r );
// 		}
            else if ( getValue(NAME).equals("manipulate") )
            {
                panel.setMode("manipulate");
                status_bar.setText("Object Manipulation Mode");
            }
	    else if ( getValue(NAME).equals("erase") )
		{
		    
		    if (debug_statements) System.out.println("Erasing...");
                    panel.removeSelectedWidget();

		    
		}

	    else if ( getValue(NAME).equals("add edge") )
		{
                    try
                    {
                        if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                        editor_frame.setSelected( true );
                    }
                    catch ( Exception exp )
                    {
                        exp.printStackTrace();
                    }
                    
                    panel.setMode("add_edges");
                    status_bar.setText("Add Edge Mode");
		    
                    /*
		    if ( panel.the_selection_box.getContents().size() == 2 )
			{
			    if ( panel.the_selection_box.getContents().get(0) instanceof BioComponent
				 && panel.the_selection_box.getContents().get(1) instanceof BioComponent )
				{
				    panel.linkComponents((BioComponent)panel.the_selection_box.getContents().get(0), 
						   (BioComponent)panel.the_selection_box.getContents().get(1));
				}
			    else
				{
				    if (debug_statements) System.out.println("inappropriate selection for adding edges");
				}
			}
		    else
			{
			    if (debug_statements) System.out.println("inappropriate selection for adding edges");
			}
                     */
		}

	    else if ( getValue(NAME).equals("add unspecified component") )
		{
                    
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
		    panel.setMode("add_unspecified_components");
                    status_bar.setText("Add Component Mode");
		    //panel.addSelectedComponent( new BioComponent(0, 0, "C", "?", false, panel ) );
		}
            else if ( getValue(NAME).equals("add bound component") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
		    panel.setMode("add_bound_components");
                    status_bar.setText("Add Component Mode");
		    //panel.addSelectedComponent( new BioComponent(0, 0, "C", "?", false, panel ) );
		}
	    else if ( getValue(NAME).equals("add unbound component") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
		    panel.setMode("add_unbound_components");
                    status_bar.setText("Add Component Mode");
		    //panel.addSelectedComponent( new BioComponent(0, 0, "C", "?", false, panel ) );
                }
            else if ( getValue(NAME).equals("add container") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_containers");
                    status_bar.setText("Add Container Mode");
		    //panel.addSelectedContainer( new BioContainer(0, 0, "Container", panel ) );
		}
	    else if ( getValue(NAME).equals("add plus") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_plus_operators");
                    status_bar.setText("Add \'Plus\' Operator Mode");
		    //panel.addSelectedOperator( new Operator(0, 0, "plus", "images/plus.gif", false, panel ) );
		}
	    else if ( getValue(NAME).equals("add forward and reverse") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_forward_and_reverse_operators");
                    status_bar.setText("Add Forward and Reverse Production Operator Mode");
		    
		    //panel.addSelectedOperator( new Operator(0, 0, "n&s", "images/forward_and_reverse.gif", false, panel ) );
		}
	    else if ( getValue(NAME).equals("add forward") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_forward_operators");
                    status_bar.setText("Add Forward Production Operator Mode");
		    
		    //panel.addSelectedOperator( new Operator(0, 0, "forward", "images/forward.gif", false, panel ) );
		}
            else if ( getValue(NAME).equals("add and") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_and_operators");
                    status_bar.setText("Add Logical \"And\" Operator Mode");
		    
		    //panel.addSelectedOperator( new Operator(0, 0, "forward", "images/forward.gif", false, panel ) );
		}
            else if ( getValue(NAME).equals("add or") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_or_operators");
                    status_bar.setText("Add Logical \"Or\" Operator Mode");
		    
		    //panel.addSelectedOperator( new Operator(0, 0, "forward", "images/forward.gif", false, panel ) );
		}
	    else if ( getValue(NAME).equals("add union") )
		{
                    try
                {
                    if ( editor_frame.isIcon() ) editor_frame.setIcon( false );
                    editor_frame.setSelected( true );
                }
                catch ( Exception exp )
                {
                    exp.printStackTrace();
                }
                    
                    panel.setMode("add_union_operators");
                    status_bar.setText("Add Set \"Union\" Operator Mode");
		    
		    //panel.addSelectedOperator( new Operator(0, 0, "forward", "images/forward.gif", false, panel ) );
		}
	    
	    // End selection
	    panel.the_selection_box.inUse(false);
	    
	}

        public void setMnemonic(int key) 
        {
            putValue(MNEMONIC_KEY, key);
        }
	
    }	
        
    class CheckBoxListener implements ItemListener {
    public void itemStateChanged(ItemEvent e) 
	{

            try
            {
	Object source = e.getItemSelectable();
        if (debug_statements) System.out.println( e + "\n");

        if (source == search_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the search window be displayed");
			//molecule_frame.setVisible(true);
                        search_frame.setIcon( false );
                        //deiconify( molecule_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the search window be hidden");
			search_frame.setIcon(true);
		    }
	    } 
        else if (source == molecule_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that molecule window be displayed");
			//molecule_frame.setVisible(true);
                        molecule_frame.setIcon( false );
                        //deiconify( molecule_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that molecule window be hidden");
			molecule_frame.setIcon(true);
		    }
	    } 
        else if (source == species_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that species window be displayed");
			//species_frame.setVisible(true);
                        //deiconify( species_frame );
                        species_frame.setIcon( false );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that species window be hidden");
			species_frame.setIcon(true);
		    }
	    }
            else if (source == reaction_rule_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that reaction rule window be displayed");
			reaction_rule_frame.setIcon(false);
                        //deiconify( reaction_rule_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that reaction rule window be hidden");
			reaction_rule_frame.setIcon(true);
		    }
	    } 
        else if (source == observables_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that reaction rule window be displayed");
			observables_frame.setIcon(false);
                        //deiconify( observables_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that reaction rule window be hidden");
			observables_frame.setIcon(true);
		    }
	    }
        else if (source == simulation_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that simulation window be displayed");
			simulation_frame.setIcon(false);
                        //deiconify( simulation_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that simulation window be hidden");
			simulation_frame.setIcon(true);
		    }
	    }
         else if (source == model_summary_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that model summary window be displayed");
			model_summary_frame.setIcon(false);
                        //deiconify( model_summary_frame );
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that model summary window be hidden");
			model_summary_frame.setIcon(true);
		    }
	    }
        else if (source == debug_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the debug frame be displayed");
			journal_frame.setIcon(false);
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the debug frame be hidden");
			journal_frame.setIcon(true);
		    }
	    }
        else if (source == reaction_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the reaction frame be displayed");
			reaction_frame.setIcon(false);
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the reaction frame be hidden");
			reaction_frame.setIcon(true);
		    }
	    }
        else if (source == engine_output_log_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the Engine Output Log frame be displayed");
			engine_output_log_frame.setIcon(false);
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the Engine Output Log frame be hidden");
			engine_output_log_frame.setIcon(true);
		    }
	    }
        else if (source == editor_frame_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the editor frame be displayed");
			editor_frame.setIcon(false);
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the editor frame be hidden");
			editor_frame.setIcon(true);
		    }
	    }
        else if (source == zoom_slider_visibility ) 
	    {
		if (e.getStateChange() == ItemEvent.SELECTED)
		    {
                        if (debug_statements) System.out.println("User requested that the zoom slider be displayed");
			zoom_slider.setVisible(true);
		    }
		else
		    {
                        if (debug_statements) System.out.println("User requested that the zoom slider be hidden");
			zoom_slider.setVisible(false);
		    }
	    }
            }
     catch (Exception exp)
    {
        exp.printStackTrace();
    }
     
        }

    }

	/*
    public void actionPerformed(ActionEvent e)
    {
	if ( e.getActionCommand().equals("Container Palette") )
	    {
		if ( e.getStateChange() == ActionEvent.SELECTED )
		    {
			container_frame.setVisible(true);	
		    } 
		if ( e.getStateChange() == ActionEvent.DESELECTED )
		    {
			container_frame.setVisible(false);	
		    }
		
		
		//rule_frame.setVisible(true);
		//species_frame.setVisible(true);
		
	    }
	else if ( e.getActionCommand().equals("Component Palette") )
	    {

	    }
	else if ( e.getActionCommand().equals("Rules Window") )
	    {

	    }
    } 

	*/

    


   
    /**
     * Construct the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    GUI( String bng_path ) 
    {
        // Display the splash screen
        displaySplashScreen();
        
        this.bng_startup_path = bng_path;
        
        // Load configuration
        // Determine the location of RuleBuilder - current path not good enough when RuleBuilder is called by an
        // associated file type
        
        
        String class_path = getClass().getResource("RuleBuilder.class").toString(); //adds a sep character to the front 
        //sometimes and seems to work like toString when called from a jar so using toString for consistency
        
        // strip internal jar path to class - leaving the filesystem path to the jar or class
        String file_path = class_path.replaceFirst("\\!.*","");
        
        // replace %20 with " "
        file_path = file_path.replaceAll("%20"," ");
        
        file_path = file_path.replaceFirst("^.*?file:",""); // strip first character + java adds a / at the front giving
        
        // Java appends a / to the start of the path in Windows. That's ok if RuleBuilder is called from the
        // same drive it is installed on but breaks if I try to start RuleBuilder on c: from a dir on z:
        if ( System.getProperty("os.name").contains("win") || System.getProperty("os.name").contains("Win") )
        {
            file_path = file_path.replaceFirst("^.","");
        }

        // /c:/etc for example, this breaks
        
        // wrap path in " " for paths with spaces
        //file_path = "\"" + file_path + "\"";
        
        System.out.println("Class Path: " + class_path);
        System.out.println("File Path: " + file_path);
        
        File temp = new File( file_path );
        config_path = temp.getParent() + temp.separatorChar + "rulebuilder.cfg";
        
        
        //config_path = System.getProperty("user.home") +File.separator+"rulebuilder.cfg";
        
        System.out.println("Configuration path: " + config_path);
        
        ConfigurationLoader cl = new ConfigurationLoader( config_path, this );
        config = cl.load();
        
        if ( config == null )
        {
           config = new Configuration();
        }
        config.setGUI( this );
        

        String look_and_feel = null;
        if ( config.isUseNativeLookAndFeel() )
        {
        
        // Specify look and feel to use
        //String look_and_feel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        look_and_feel = UIManager.getSystemLookAndFeelClassName();
       //String look_and_feel = "javax.swing.plaf.metal.MetalLookAndFeel";
        }
        else
        {
            look_and_feel = "javax.swing.plaf.metal.MetalLookAndFeel";
        }
            
        try
	{
                
                //String look_and_feel = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
                //
                if (debug_statements) System.out.println("Using: " + look_and_feel);
		UIManager.setLookAndFeel(look_and_feel);
                //UIManager.getCrossPlatformLookAndFeelClassName());
                //UIManager.getSystemLookAndFeelClassName());
            
  
            }
	catch (Exception exc)
	    {
		System.err.println("Error loading \""+look_and_feel+"\"\n" + exc);
	    }
        
        // Run does everything
    }
    
    void createAndDisplayGUI()
    {
        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
            
        
        // BNG has multiple windows so we need an object to keep track of their states
        BNGWindowListener window_listener = new BNGWindowListener();
        
	model_parameters = new ModelParameters( this, getModel() );
	//simulation_config = new SimulationConfig( this, getModel() ); // mlf
	

        // WidgetTransferHandler
        widget_transfer_handler = new WidgetTransferHandler();
        
        // The handler monitors the drag action in DnD and modifies the cursor
        // depending on whether the pointer is over a valid drop site or not.
        widget_drag_handler = new WidgetDragHandler();
        
	// Create the state saver and loader objects
	state_saver = new StateSaver( this, the_model );
	state_loader = new StateLoader( this, the_model );

        // Create the JavaHelp System
        
        URL helpset_url = this.getClass().getResource("documentation/BNGHelp/jhelpset.hs");
           
        try
        {
            helpset = new HelpSet(null, helpset_url);
        }
        catch (Exception exp)
        {
            //editor_panel.displayError("Error Loading Help System",
            //                          exp.getMessage() );
        }
            
        help_broker = helpset.createHelpBroker();
        
        if ( help_broker == null )
        {
            //editor_panel.displayError("Error Loading Help System",
            //                          "Help broker creation failed. Contact support\n" +
            //                          "at support@bionetgen.com" );
        }
        
        
        // Create the windows
        
        //Make the big window be indented 50 pixels from each edge
        //of the screen.
        
        //Set up the GUI.
        desktop = new JDesktopPane(); //a specialized layered pane
        
        
        
        main_frame = new JFrame();
        main_frame.getContentPane().add(desktop, BorderLayout.CENTER );
        main_frame.addWindowListener( new AppCloser() );
        
        //Create a status bar
        status_bar = new JLabel();
        status_bar.setText( "Object Manipulation Mode" );
        
        // Calculate the dimensions for the new window
	//Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();

        // Build the Editor Window
        
	editor_frame = new JInternalFrame();
	main_frame.setTitle("Rule Builder " + version );
        editor_frame.setTitle("Drawing Board");
        
        main_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        editor_panel = new WidgetPanel( this );
	
	// Put everything together inside the frame we created
	editor_frame.getContentPane().add(status_bar, BorderLayout.SOUTH);
        editor_frame.getContentPane().add(editor_panel, BorderLayout.CENTER);
        
        JToolBar editor_toolbar = createToolBar();
        
        editor_menu_bar = createMenuBar( editor_panel );
        // Can't do this until the view menu has been created      
        displayJournal();
        
        zoom_toolbar = createZoomToolbar( editor_panel );
        
        edit_toolbar = createEditToolbar( editor_panel );
        editor_toolbar.add(  zoom_toolbar );
        editor_toolbar.add( edit_toolbar );
        
        
        main_frame.setJMenuBar( editor_menu_bar );
	editor_scroll_pane = new JScrollPane(editor_panel);
        editor_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //editor_scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
        //editor_scroll_pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        editor_scroll_pane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
        editor_frame.getContentPane().add(editor_scroll_pane, BorderLayout.CENTER );
        
        editor_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        editor_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        editor_frame.setIconifiable( true );
        editor_frame.setMaximizable( true );
        editor_frame.setResizable( true );
        
        editor_frame.addInternalFrameListener( new InternalFrameListener() );
        
        zoom_slider = createZoomSlider( editor_panel );
        editor_frame.add( zoom_slider , BorderLayout.EAST );
        zoom_slider.setVisible( false );
        
        main_frame.getContentPane().add( editor_toolbar, BorderLayout.NORTH);
        editor_toolbar.setVisible( true );
        
        editor_frame.setVisible( true );
	desktop.add(editor_frame);
        
       
        
        
        //editor_panel.setPreferredSize( new Dimension( screen_size.width, screen_size.height ) );
	
        
        // Build the Molecule Window
        //molecule
        molecule_frame = new JInternalFrame();
        molecule_frame.setTitle("Molecule Templates Palette");
        molecule_palette = new MoleculePalette( this );
        //molecule_frame.getContentPane().add(molecule_panel, BorderLayout.CENTER);
        //molecule_frame.addWindowListener(window_listener);
        //molecule_frame.addWindowFocusListener(window_listener);
        //molecule_frame.addWindowStateListener(window_listener);
        molecule_scroll_pane = new JScrollPane(molecule_palette);
        molecule_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //molecule_scroll_pane.setPreferredSize(new Dimension(200,200));
        molecule_scroll_pane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        molecule_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        molecule_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        molecule_frame.putClientProperty("JInternalFrame.isPalette",Boolean.TRUE);
        
        molecule_frame.getContentPane().add(molecule_scroll_pane);
        //molecule_frame.getContentPane().add(createZoomSlider( molecule_palette ), BorderLayout.EAST);
        
        //JToolBar molecule_toolbar = new JToolBar();
        //molecule_toolbar.add( createZoomToolbar( molecule_palette ) );
        //molecule_toolbar.add( createEditToolbar( molecule_palette ) );
        
        //molecule_frame.getContentPane().add( molecule_toolbar, BorderLayout.NORTH);
        
        molecule_frame.addInternalFrameListener( new InternalFrameListener() );
        
        molecule_frame.setIconifiable( true );
        molecule_frame.setMaximizable( true );
        molecule_frame.setResizable( true );
        
        molecule_frame.setVisible(true);
        
        desktop.add( molecule_frame );
        
        //search frame
        search_frame = new JInternalFrame();
        search_frame.setTitle("Search");
        search_palette = new SearchPalette( this );
        //molecule_frame.getContentPane().add(molecule_panel, BorderLayout.CENTER);
        //molecule_frame.addWindowListener(window_listener);
        //molecule_frame.addWindowFocusListener(window_listener);
        //molecule_frame.addWindowStateListener(window_listener);
        search_scroll_pane = new JScrollPane(search_palette);
        search_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //search_scroll_pane.setPreferredSize(new Dimension(200,200));
        search_scroll_pane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

        search_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        search_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        search_frame.getContentPane().add(search_scroll_pane);
        //molecule_frame.getContentPane().add(createZoomSlider( molecule_palette ), BorderLayout.EAST);
        
        //JToolBar molecule_toolbar = new JToolBar();
        //molecule_toolbar.add( createZoomToolbar( molecule_palette ) );
        //molecule_toolbar.add( createEditToolbar( molecule_palette ) );
        
        //molecule_frame.getContentPane().add( molecule_toolbar, BorderLayout.NORTH);
        
        search_frame.addInternalFrameListener( new InternalFrameListener() );
        
        search_frame.setIconifiable( true );
        search_frame.setMaximizable( true );
        search_frame.setResizable( true );
        
        search_frame.setVisible(true);
        
        desktop.add( search_frame );
        
        // BioNetGen output log frame
        engine_output_log_frame = new JInternalFrame();
        engine_output_log_frame.setTitle("BioNetGen Output Log");
        
        engine_output_log_frame.addInternalFrameListener( new InternalFrameListener() );
        
        engine_output_log_frame.setIconifiable( true );
        engine_output_log_frame.setMaximizable( true );
        engine_output_log_frame.setResizable( true );
        
        desktop.add( engine_output_log_frame );
        
        // create species panel
        species_frame = new JInternalFrame();
        species_frame.setTitle("Seed Species");
        species_palette = new SpeciesPalette( this );
        //species_frame.addWindowListener(window_listener);
        //species_frame.addWindowFocusListener(window_listener);
        //species_frame.addWindowStateListener(window_listener);
        //species_frame.setSize( width/3, height/2 );
        //species_frame.setLocation( x, y+height/2 );
        species_scroll_pane = new JScrollPane(species_palette);
        species_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //species_scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
        //species_scroll_pane.setPreferredSize(new Dimension(200,200));
        
        species_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        species_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        species_frame.getContentPane().add(species_scroll_pane, BorderLayout.CENTER);
        //species_frame.getContentPane().add(createZoomSlider( species_palette ), BorderLayout.EAST);
        
        //JToolBar species_toolbar = new JToolBar();
        //species_toolbar.add( createZoomToolbar( species_palette ) );
        //species_toolbar.add( createEditToolbar( species_palette ) );
        
        //species_frame.getContentPane().add( species_toolbar, BorderLayout.NORTH);
        
        species_frame.addInternalFrameListener( new InternalFrameListener() );
        
        species_frame.setIconifiable( true );
        species_frame.setMaximizable( true );
        species_frame.setResizable( true );
        
        species_frame.setVisible(true);
        

        
        desktop.add( species_frame );
        
        // create species panel
        observables_frame = new JInternalFrame();
        observables_frame.setTitle("Observables");
        observables_palette = new ObservablesPalette( this );
        //observables_frame.addWindowListener(window_listener);
        //observables_frame.addWindowFocusListener(window_listener);
        //observables_frame.addWindowStateListener(window_listener);
        //observables_frame.setSize( width/3, height/2 );
        //observables_frame.setLocation( x, y+height/2 );
        observables_scroll_pane = new JScrollPane(observables_palette);
        observables_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //observable_scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
        //observables_scroll_pane.setPreferredSize(new Dimension(200,200));
        
        observables_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        observables_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        observables_frame.getContentPane().add(observables_scroll_pane, BorderLayout.CENTER);
        //observable_frame.getContentPane().add(createZoomSlider( observable_palette ), BorderLayout.EAST);
        
        //JToolBar observables_toolbar = new JToolBar();
        //observables_toolbar.add( createZoomToolbar( observables_palette ) );
        //observables_toolbar.add( createEditToolbar( observables_palette ) );
        
        //observables_frame.getContentPane().add( observables_toolbar, BorderLayout.NORTH);
        
        observables_frame.addInternalFrameListener( new InternalFrameListener() );
        
        observables_frame.setIconifiable( true );
        observables_frame.setMaximizable( true );
        observables_frame.setResizable( true );
        
        observables_frame.setVisible(true);
        
        

        
        desktop.add( observables_frame );
        
        //reaction_rule
        reaction_rule_frame = new JInternalFrame();
        reaction_rule_frame.setTitle("Reaction Rules");
        reaction_rule_palette = new ReactionRulePalette( this );
        //reaction_rule_frame.addWindowListener(window_listener);
        //reaction_rule_frame.addWindowFocusListener(window_listener);
        //reaction_rule_frame.addWindowStateListener(window_listener);
        //reaction_rule_frame.setLocation( editor_frame.getX(), editor_frame.getY() + editor_frame.getHeight()+padding);
        //reaction_rule_frame.setSize( editor_frame.getWidth()+padding+molecule_frame.getWidth(), (int)(0.51*(main_frame.getHeight() - reaction_rule_frame.getY())) - padding );
        reaction_rule_scroll_pane = new JScrollPane(reaction_rule_palette);
        reaction_rule_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //reaction_rule_scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
        //reaction_rule_scroll_pane.setPreferredSize(new Dimension(200,200));
        
        reaction_rule_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        reaction_rule_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        reaction_rule_frame.getContentPane().add(reaction_rule_scroll_pane, BorderLayout.CENTER);
        //reaction_rule_frame.getContentPane().add(createZoomSlider( reaction_rule_palette ), BorderLayout.EAST);
        //JToolBar rr_toolbar = new JToolBar();
        //rr_toolbar.add( createZoomToolbar( reaction_rule_palette ) );
        //rr_toolbar.add( createEditToolbar( reaction_rule_palette ) );
        
        //reaction_rule_frame.getContentPane().add( rr_toolbar, BorderLayout.NORTH);
        reaction_rule_frame.setIconifiable( true );
        reaction_rule_frame.setMaximizable( true );
        reaction_rule_frame.setResizable( true );
        
        reaction_rule_frame.addInternalFrameListener( new InternalFrameListener() );
        
        reaction_rule_frame.setVisible(true);
        
        desktop.add( reaction_rule_frame );
        
        //reactions
        reaction_frame = new JInternalFrame();
        reaction_frame.setTitle("Reactions");
        reaction_palette = new ReactionPalette( this );
        //reaction_frame.addWindowListener(window_listener);
        //reaction_frame.addWindowFocusListener(window_listener);
        //reaction_frame.addWindowStateListener(window_listener);
        //reaction_frame.setSize( 400, 300 );
        //reaction_frame.setLocation( editor_frame.getX(), editor_frame.getY() + editor_frame.getHeight()+20);
        reaction_scroll_pane = new JScrollPane(reaction_palette);
        reaction_scroll_pane.getVerticalScrollBar().addAdjustmentListener(new ScrollControl());
        //reaction_rule_scroll_pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );
        //reaction_scroll_pane.setPreferredSize(new Dimension(200,200));
        
        reaction_scroll_pane.getHorizontalScrollBar().setUnitIncrement(20);
        reaction_scroll_pane.getVerticalScrollBar().setUnitIncrement(20);
        
        reaction_frame.getContentPane().add(reaction_scroll_pane, BorderLayout.CENTER);
        //reaction_rule_frame.getContentPane().add(createZoomSlider( reaction_rule_palette ), BorderLayout.EAST);
        //JToolBar r_toolbar = new JToolBar();
        //r_toolbar.add( createZoomToolbar( reaction_palette ) );
        //r_toolbar.add( createEditToolbar( reaction_palette ) );
        
        //reaction_frame.getContentPane().add( r_toolbar, BorderLayout.NORTH);
        
        reaction_frame.setIconifiable( true );
        reaction_frame.setMaximizable( true );
        reaction_frame.setResizable( true );
        
        desktop.add( reaction_frame );
        
        reaction_frame.addInternalFrameListener( new InternalFrameListener() );
        
        reaction_frame.setVisible(true);
                    
        //simulation
        simulation_frame = new Plotter();
        simulation_frame.setVisible(false);
        
        simulation_frame.setIconifiable( true );
        simulation_frame.setMaximizable( true );
        simulation_frame.setResizable( true );
                
        simulation_frame.addInternalFrameListener( new InternalFrameListener() );
        
        desktop.add( simulation_frame );
        
        //model_summary
        model_summary_frame = new JInternalFrame();
        model_summary_frame.setTitle("Model Summary");
        model_summary_panel = new WidgetPanel( this );
        model_summary_frame.getContentPane().add(model_summary_panel, BorderLayout.CENTER);
        //model_summary_frame.addWindowListener(window_listener);
        //model_summary_frame.addWindowFocusListener(window_listener);
        //model_summary_frame.addWindowStateListener(window_listener);
        //model_summary_frame.setSize( width/3, height );
        //model_summary_frame.setLocation( x, y );
        
        model_summary_frame.setIconifiable( true );
        model_summary_frame.setMaximizable( true );
        model_summary_frame.setResizable( true );
        
        model_summary_frame.setVisible( false );
        
        //desktop.add( model_summary_frame );
        
        
        // Set the Transfer handlers so that widgets panels understand Drag 'n Drop
        /*
        molecule_palette.setTransferHandler( widget_transfer_handler );
        species_palette.setTransferHandler( widget_transfer_handler );
        observables_palette.setTransferHandler( widget_transfer_handler );
        reaction_rule_palette.setTransferHandler( widget_transfer_handler );
        editor_panel.setTransferHandler( widget_transfer_handler );
        
        
        editor_panel.setDropTarget(new DropTarget( editor_panel , widget_transfer_handler.getDropHandler()));
        species_palette.setDropTarget(new DropTarget(species_palette, widget_transfer_handler.getDropHandler()));
        reaction_rule_palette.setDropTarget(new DropTarget(reaction_rule_palette, widget_transfer_handler.getDropHandler()));
        observables_palette.setDropTarget(new DropTarget(observables_palette, widget_transfer_handler.getDropHandler()));
        molecule_palette.setDropTarget(new DropTarget(molecule_palette, widget_transfer_handler.getDropHandler()));
        */
        
        notes_frame = new JInternalFrame("Notes");
        
        notes_frame.addInternalFrameListener( new InternalFrameListener() );
        

                notes_frame.setClosable( true );
                notes_frame.setResizable(true);
                
                try
                {
                    notes_frame.setIcon(true);
                }
                catch ( Exception e )
                {
                    e.printStackTrace();
                }

        
            desktop.add( notes_frame );
        
            
            int width = (int)screen_size.getWidth()/2;
          
            notes_panel.setSize(width, 400);
            notes_pane.setEditable( true );
            notes_pane.setBackground(Color.WHITE);
            notes_pane.setForeground(Color.BLACK);
            notes_pane.setAutoscrolls( true );
            
            SimpleAttributeSet heading_sas = new SimpleAttributeSet();
            StyleConstants.setFontFamily(heading_sas, "SansSerif");
            StyleConstants.setFontSize(heading_sas, 24);
                   
            StyleConstants.setForeground(heading_sas, Color.blue);
            StyledDocument styled_doc = notes_pane.getStyledDocument();
            Position position = styled_doc.getEndPosition();
            
            SimpleAttributeSet normal_sas = new SimpleAttributeSet();
            StyleConstants.setFontFamily(normal_sas, "SansSerif");
            StyleConstants.setFontSize(normal_sas, 15);
                   
            StyleConstants.setForeground(normal_sas, Color.blue);
            
            int offset = position.getOffset();
            
            String heading = "Notes: ";
            
            try
            {
                styled_doc.insertString(offset, notes, normal_sas );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
      
            notes_scroll_pane = new JScrollPane(notes_pane);
            notes_scroll_pane.setVerticalScrollBarPolicy(
	    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            notes_scroll_pane.setPreferredSize(new Dimension(width, 400));
 
          
            notes_panel.add( notes_scroll_pane );
            notes_frame.getContentPane().add(notes_panel);//(log_scrollpane, BorderLayout.CENTER);
       
            notes_frame.setSize( width, 400 );
           
            notes_frame.setLocation( (screen_size.width-width)/2, 50 );
            
            notes_frame.pack();

            notes_frame.setVisible( false );
        
        positionWindows();

	// mlf
	if ( simulation_config == null )
	{
		simulation_config = new SimulationConfig();
	}
        simulation_config.setGUI( this );
        simulation_config.setModel( getModel() );
        
        config.writeValues();
        
        if ( !getConfig().getFileHistory().isEmpty() ) file_menu.addSeparator();
        Iterator path_itr = getConfig().getFileHistory().iterator();
        while ( path_itr.hasNext() )
        {
            String path = (String)path_itr.next();
            ToolbarAction file_history_entry = new ToolbarAction(path, new ImageIcon(""), "file history", 'o', editor_panel);
            file_menu.add( file_history_entry );    
        }
        
        //recreateFileMenu();
        
        
        
        
        main_frame.setVisible( true );
        
        if (debug_statements) System.out.println("Processing BNG Startup File...");
        if (bng_startup_path != null)
        {
            loadBNGFile( bng_startup_path );
        }
        else
        {
            if (debug_statements) System.out.println("No startup BNG file was specified (null)");
        }
        
        try 
        {
            observables_frame.setIcon( true );
            reaction_frame.setIcon( true );
            model_summary_frame.setIcon( true );
            simulation_frame.setIcon( true ); 
        } 
        catch (java.beans.PropertyVetoException e) 
        {
            //!
        }
        
        try 
        {
            editor_frame.setSelected(true);
        } 
        catch (java.beans.PropertyVetoException e) 
        {
            //!
        }
              
        // Display beta notice
	
    } 

    // This is very error prone - depends on the_model being created
    // before addModel is called and that nothing using the model occurs
    // in the GUI before this method is called
    boolean addModel( Model the_model )
    {
	if ( this.the_model != null )
	    {
		return false;
	    }
	
	this.the_model = the_model;
	
	
	//editor_panel.addModel(the_model);
    
	
	return true;
    }

    protected final class AppCloser extends WindowAdapter implements ActionListener 
    {
        /**
         *
         * @param e
         */        
        public void windowClosing(WindowEvent e)
	{
	    closeHandler();
        }
        
        public void actionPerformed(ActionEvent e) 
        {
            closeHandler();
        }
        
        private void closeHandler()
        {
            // Allow user to save work before closing
	    //net_panel.displayQuestion("Save Work?", "Do you want to save your work before exiting?");
            
            if ( isSaveNeeded() )
            {
                int result = saveWorkOption("Do you want to save your work before exiting?");
                
                if ( result == JOptionPane.CANCEL_OPTION )
                {
                    return;
                }
            }   
                // Save settings
                new ConfigurationSaver(config_path, getTheGUI() ).save( getConfig() );
                
                if (debug_statements) System.out.println("Exiting.");
                System.exit(0);
         
        }
        
    }
            
    public JDesktopPane getDesktop()
    {
        return desktop;
    }

    WidgetPanel getEditorPanel()
    {
	return (WidgetPanel)editor_panel;
    }

    public Model getModel()
    {
	return the_model;
    }
    
    public Thread getModelThread()
    {
	return the_model_thread;
    }

    void setModel( Model the_model )
    {
	this.the_model = the_model;
    }

    void setModelThread( Thread the_model_thread )
    {
	this.the_model_thread = the_model_thread;
    }
    
    JToolBar createToolBar()
    {

       	JMenuBar rule_menubar = new JMenuBar(  );

	// Icon image paths
	// use urls so images can be stored in JAR file
        URL manip_jpg_url = this.getClass().getResource("images/manip.gif");
	//URL erase_gif_url = this.getClass().getResource("images/erase.gif");
	URL add_edge_gif_url = this.getClass().getResource("images/add_edge.gif");
	URL unspecified_component_gif_url = this.getClass().getResource("images/unspecified_binding_component.gif");
        URL bound_component_gif_url = this.getClass().getResource("images/bound_component.gif");
        URL unbound_component_gif_url = this.getClass().getResource("images/unbound_component.gif");
        URL dropdown_arrow_gif_url = this.getClass().getResource("images/dropdown_arrow.gif");

        URL atom_map_gif_url = this.getClass().getResource("images/map.gif");
        URL add_cdk_model_gif_url = this.getClass().getResource("images/cdk.gif");
        
        
        URL undo_gif_url = this.getClass().getResource("images/undo.gif");
        URL redo_gif_url = this.getClass().getResource("images/redo.gif");

        
	URL forward_gif_url = this.getClass().getResource("images/forward.gif");
	URL forward_and_reverse_gif_url = this.getClass().getResource("images/forward_and_reverse.gif");
	URL plus_gif_url = this.getClass().getResource("images/plus.gif");
	URL container_gif_url = this.getClass().getResource("images/container.gif");
	URL model_gif_url = this.getClass().getResource("images/model.gif");
        URL species_gif_url = this.getClass().getResource("images/species.gif");
        URL sim_config_gif_url = this.getClass().getResource("images/settings.gif");
 
        URL or_gif_url = this.getClass().getResource("images/or.gif");
        URL and_gif_url = this.getClass().getResource("images/and.gif");
        URL union_gif_url = this.getClass().getResource("images/union.gif");
        URL plot_gif_url = this.getClass().getResource("images/plot.gif");
 
        URL search_gif_url = this.getClass().getResource("images/search.gif");
        
        URL model_params_gif_url = this.getClass().getResource("images/model_params.gif");
 
        URL notes_gif_url = this.getClass().getResource("images/notes.gif");
        
        // Create a set of actions to use in both the menu and toolbar.
        ImageIcon manip_icon = new ImageIcon(manip_jpg_url);
        ToolbarAction rule_manip_action = new ToolbarAction("manipulate", manip_icon, "manipulate objects", 'M', editor_panel);
        //ToolbarAction rule_erase_action = new ToolbarAction("erase", new ImageIcon(erase_gif_url), "erase selection", 'E', editor_panel);
	 
        ImageIcon add_edge_icon = new ImageIcon(add_edge_gif_url);
        ToolbarAction rule_add_edge_action = new ToolbarAction("add edge", add_edge_icon, "Edge", 'A', editor_panel);
	
        ImageIcon add_container_icon = new ImageIcon(container_gif_url);
        ToolbarAction rule_add_container_action = new ToolbarAction("add container", add_container_icon, "container", 'N', editor_panel);
	
        ToolbarAction run_model_action = new ToolbarAction("Run BioNetGen", new ImageIcon(model_gif_url), "Run BioNetGen", 'N', editor_panel);
	
        ToolbarAction plot_action = new ToolbarAction("plot", new ImageIcon(plot_gif_url), "plot", 'N', editor_panel);
	
        ToolbarAction search_action = new ToolbarAction("search", new ImageIcon(search_gif_url), "search", 'N', editor_panel);
	
        ToolbarAction notes_action = new ToolbarAction("notes", new ImageIcon(notes_gif_url), "notes", 'N', editor_panel);
	
        ImageIcon add_forward_icon = new ImageIcon(forward_gif_url);
	ToolbarAction rule_add_forward_action = new ToolbarAction("add forward", add_forward_icon, "forward production", 'C', editor_panel);
	
        ImageIcon add_forward_and_reverse_icon = new ImageIcon(forward_and_reverse_gif_url);
        ToolbarAction rule_add_forward_and_reverse_action = new ToolbarAction("add forward and reverse", add_forward_and_reverse_icon, "forward and reverse production", 'C', editor_panel);
	
        ImageIcon add_plus_icon = new ImageIcon(plus_gif_url);
        ToolbarAction rule_add_plus_action = new ToolbarAction("add plus", add_plus_icon, "plus", 'A', editor_panel);
	
        ToolbarAction rule_find_action = new ToolbarAction("find rule", new ImageIcon(species_gif_url), "find rule", 'f', editor_panel);
        
        ToolbarAction add_and_action = new ToolbarAction("add or", new ImageIcon(or_gif_url), "logical or", 'C', editor_panel);
	ToolbarAction add_or_action = new ToolbarAction("add and", new ImageIcon(and_gif_url), "logical and", 'A', editor_panel);
	
        ImageIcon add_union_icon = new ImageIcon(union_gif_url);
        ToolbarAction add_union_action = new ToolbarAction("add union", add_union_icon, "set union", 'A', editor_panel);
	
        
        ToolbarAction sim_config_action = new ToolbarAction("Reaction Network Generation and Simulation Settings", new ImageIcon(sim_config_gif_url), "Reaction Network Generation and Simulation Settings", 's', editor_panel);
        ToolbarAction model_params_action = new ToolbarAction("Model Parameters", new ImageIcon(model_params_gif_url), "Model Parameters", 's', editor_panel);
        
        ImageIcon add_unspecified_binding_component_icon = new ImageIcon(unspecified_component_gif_url);
        ToolbarAction rule_add_unspecified_component_action = new ToolbarAction("add unspecified component", add_unspecified_binding_component_icon, "Unspecified Binding Component", 'C', editor_panel);
	ToolbarAction rule_add_bound_component_action = new ToolbarAction("add bound component", new ImageIcon(bound_component_gif_url), "bound component", 'C', editor_panel);
	
        ImageIcon add_unbound_component_icon = new ImageIcon(unbound_component_gif_url);
        ToolbarAction rule_add_unbound_component_action = new ToolbarAction("add unbound component", add_unbound_component_icon, "Unbound Component", 'C', editor_panel);
        
        ToolbarAction undo_action = new ToolbarAction("undo", new ImageIcon(undo_gif_url), "undo", 'C', editor_panel);
	ToolbarAction redo_action = new ToolbarAction("redo", new ImageIcon(redo_gif_url), "redo", 'C', editor_panel);
        
        ImageIcon add_atom_map_icon = new ImageIcon(atom_map_gif_url);
        ToolbarAction atom_map_action = new ToolbarAction("atom map", add_atom_map_icon , "atom map", 'C', editor_panel);
        
        ImageIcon add_cdk_model_icon = new ImageIcon(add_cdk_model_gif_url);
        ToolbarAction add_cdk_model_action = new ToolbarAction("cdk model", add_cdk_model_icon , "cdk model", 'C', editor_panel);
         
        JToggleButton manip_button = new JToggleButton(manip_icon, true );
        manip_button.setAction( rule_manip_action );
        manip_button.addActionListener( atom_map_action );
        manip_button.setText(null);
        
        JToggleButton add_component_button = new JToggleButton(add_unbound_component_icon, false );
        add_component_button.setAction( rule_add_unbound_component_action );
        add_component_button.addActionListener( rule_add_unbound_component_action );
        add_component_button.setText(null);
        
        JToggleButton add_container_button = new JToggleButton(add_container_icon, false );
        add_container_button.setAction( rule_add_container_action );
        add_container_button.addActionListener( rule_add_container_action );
        add_container_button.setText(null);
        
        JToggleButton add_edge_button = new JToggleButton(add_edge_icon, false );
        add_edge_button.setAction( rule_add_edge_action );
        add_edge_button.addActionListener( rule_add_edge_action );
        add_edge_button.setText(null);
        
        JToggleButton add_forward_button  = new JToggleButton(add_forward_icon, false );
        add_forward_button.setAction( rule_add_forward_action );
        add_forward_button.addActionListener( rule_add_forward_action );
        add_forward_button.setText(null);
        
        JToggleButton add_forward_and_reverse_button  = new JToggleButton(add_forward_and_reverse_icon, false );
        add_forward_and_reverse_button.setAction( rule_add_forward_and_reverse_action );
        add_forward_and_reverse_button.addActionListener( rule_add_forward_and_reverse_action );
        add_forward_and_reverse_button.setText(null);
        
        JToggleButton add_plus_button = new JToggleButton(add_plus_icon, false );
        add_plus_button.setAction( rule_add_plus_action );
        add_plus_button.addActionListener( rule_add_plus_action );
        add_plus_button.setText(null);
        
        JToggleButton add_union_button = new JToggleButton(add_union_icon, false );
        add_union_button.setAction( add_union_action );
        add_union_button.addActionListener( add_union_action );
        add_union_button.setText(null);
        
        JToggleButton add_map_button = new JToggleButton(add_atom_map_icon, false );
        add_map_button.setAction( atom_map_action );
        add_map_button.addActionListener( atom_map_action );
        add_map_button.setText(null);
        
        JToggleButton add_cdk_model_button = new JToggleButton(add_cdk_model_icon, false );
        add_cdk_model_button.setAction( add_cdk_model_action );
        add_cdk_model_button.addActionListener( add_cdk_model_action );
        add_cdk_model_button.setText(null);
        
        
        ButtonGroup modes = new ButtonGroup();
        modes.add( manip_button );
        modes.add( add_component_button );
        modes.add( add_container_button );
        modes.add( add_plus_button );
        modes.add( add_forward_button );
        modes.add( add_forward_and_reverse_button );
        modes.add( add_union_button );
        modes.add( add_edge_button );
        modes.add( add_map_button );
        //modes.add( add_cdk_model_button );
        
        JToolBar rule_toolbar = new JToolBar("Rule Tools");
        
	//rule_toolbar.add(rule_manip_action);
        rule_toolbar.add(manip_button);
        
        
        rule_toolbar.addSeparator();
        rule_toolbar.add(add_component_button);
        
        
        //rule_toolbar.add(rule_add_unbound_component_action );
        //rule_toolbar.add(rule_add_unspecified_component_action);
	//rule_toolbar.add(rule_add_bound_component_action);
        //rule_toolbar.add(rule_add_container_action);
	rule_toolbar.add(add_container_button);
        
        //rule_toolbar.add(rule_add_edge_action);
        rule_toolbar.add(add_edge_button);
	//rule_toolbar.add(rule_erase_action);
	rule_toolbar.addSeparator();

	//rule_toolbar.add(rule_add_forward_action);
        rule_toolbar.add(add_forward_button);
        
	//rule_toolbar.add(rule_add_forward_and_reverse_action);
	rule_toolbar.add(add_forward_and_reverse_button);
        
        //rule_toolbar.add(rule_add_plus_action);
	rule_toolbar.add(add_plus_button);
        
        rule_toolbar.addSeparator();
        rule_toolbar.add( add_map_button );
        //rule_toolbar.add(add_cdk_model_button);
        rule_toolbar.addSeparator();
        //rule_toolbar.add(add_and_action);
	//rule_toolbar.add(add_or_action );
        //rule_toolbar.add(add_union_action );
        rule_toolbar.add(add_union_button);
        rule_toolbar.addSeparator();
        rule_toolbar.add( sim_config_action );
        rule_toolbar.add( model_params_action );
        rule_toolbar.add( run_model_action );
        rule_toolbar.add( plot_action );
        rule_toolbar.add( search_action );
	rule_toolbar.addSeparator();
        rule_toolbar.add( undo_action );
        rule_toolbar.add( redo_action );
        rule_toolbar.addSeparator();
        //rule_toolbar.add( notes_action );
	
        //rule_toolbar.add(rule_find_action);
       
	rule_toolbar.setMaximumSize(rule_toolbar.getSize());
        
	return rule_toolbar;
    }

    JMenuBar createMenuBar( WidgetPanel editor_panel )
    {
	JMenuBar menubar = new JMenuBar();
	file_menu = new JMenu("File");

	ToolbarAction about_action         = new ToolbarAction("about",        new ImageIcon(""), "about", 'a', editor_panel);
        ToolbarAction file_open_action     = new ToolbarAction("open",        new ImageIcon(""), "open", 'o', editor_panel);
        ToolbarAction file_append_action   = new ToolbarAction("append",        new ImageIcon(""), "append", 'a', editor_panel);
        ToolbarAction file_save_action     = new ToolbarAction("save",        new ImageIcon(""), "save", 's', editor_panel);
	ToolbarAction file_saveas_action   = new ToolbarAction("save as",        new ImageIcon(""), "save as", 'd', editor_panel);
        ToolbarAction file_new_action      = new ToolbarAction("new",        new ImageIcon(""), "new", 'n', editor_panel);
	ToolbarAction load_BNGL_action     = new ToolbarAction("Load BNGL File", new ImageIcon(""), "open", 'o', editor_panel);
        ToolbarAction file_xmlsave_action  = new ToolbarAction("save as xml",    new ImageIcon(""), "save as xml", 'x', editor_panel);
        ToolbarAction configuration_action = new ToolbarAction("settings",       new ImageIcon(""), "settings", 'c', editor_panel);
        ToolbarAction export_action = new ToolbarAction("Export",       new ImageIcon(""), "Export", 'c', editor_panel);
        ToolbarAction import_action = new ToolbarAction("Import",       new ImageIcon(""), "Import", 'c', editor_panel);
        ToolbarAction reset_windows_action = new ToolbarAction("Reset Window Positions",       new ImageIcon(""), "Reset Window Positions", 'c', editor_panel);
        
        
        JMenuItem exit_action              = new JMenuItem("exit");
        exit_action.addActionListener( new AppCloser() );
        
        //editor_menu_bar.remove( file_menu );
	file_menu.add(file_new_action);
        file_menu.add(file_open_action);
        file_menu.add(file_append_action);
        file_menu.add(file_save_action);
        file_menu.add(file_saveas_action);
        file_menu.add(import_action);
        file_menu.add(export_action);
        
        file_menu.setMnemonic( java.awt.event.KeyEvent.VK_F );
        file_new_action.setMnemonic( java.awt.event.KeyEvent.VK_N );
        
        //net_file_menu.add(net_load_BNGL_action );
        //net_file_menu.add(net_file_xmlsave_action);
        file_menu.add(configuration_action);
        file_menu.add(exit_action);
        
        menubar.add(file_menu);
        
        ToolbarAction edit_clear_action = new ToolbarAction("clear window", new ImageIcon(""), "clear window", 'n', editor_panel);
        JMenu edit_menu = new JMenu("Edit");
        edit_menu.add( edit_clear_action );
        menubar.add(edit_menu);

        
	//JMenu net_pointer_menu = new JMenu("Pointer Tools");
	//net_pointer_menu.add(net_erase_action);
	//net_pointer_menu.add(net_add_edge_action);
	//net_pointer_menu.add(net_add_container_action);
	
	//net_menubar.add(net_pointer_menu);

        // Visibility checkboxes
	CheckBoxListener view_listener = new CheckBoxListener();
        
        editor_frame_visibility = new JCheckBoxMenuItem( "Drawing Board", true );
        editor_frame_visibility.addItemListener( view_listener );
        
        molecule_frame_visibility = new JCheckBoxMenuItem( "Molecule Palette", true );
        molecule_frame_visibility.addItemListener( view_listener );
        
        search_frame_visibility = new JCheckBoxMenuItem( "Search Palette", true );
        search_frame_visibility.addItemListener( view_listener );
        
        
        species_frame_visibility = new JCheckBoxMenuItem( "Species Palette", true );
        species_frame_visibility.addItemListener( view_listener );
  
        reaction_rule_frame_visibility = new JCheckBoxMenuItem( "Reaction Rule Palette", true );
        reaction_rule_frame_visibility.addItemListener( view_listener );
  
        observables_frame_visibility = new JCheckBoxMenuItem( "Observables Palette", false );
        observables_frame_visibility.addItemListener( view_listener );
        
        reaction_frame_visibility = new JCheckBoxMenuItem( "Reaction Window", false );
        reaction_frame_visibility.addItemListener( view_listener );
        
        
        simulation_frame_visibility = new JCheckBoxMenuItem( "Simulation Results Plot", false );
        simulation_frame_visibility.addItemListener( view_listener );
  
        model_summary_frame_visibility = new JCheckBoxMenuItem( "Model Summary", false );
        model_summary_frame_visibility.addItemListener( view_listener );
  
        debug_frame_visibility = new JCheckBoxMenuItem( "Debug Output Window", false );
        debug_frame_visibility.addItemListener( view_listener );
        
        zoom_slider_visibility = new JCheckBoxMenuItem( "Zoom Slider", false );
        zoom_slider_visibility.addItemListener( view_listener );
  
        engine_output_log_frame_visibility = new JCheckBoxMenuItem( "Engine Output Log", false );
        engine_output_log_frame_visibility.addItemListener( view_listener );
        
	JMenu view_menu = new JMenu("View");
	view_menu.add( editor_frame_visibility );
        view_menu.add( molecule_frame_visibility );
        view_menu.add( species_frame_visibility );
	view_menu.add( reaction_rule_frame_visibility );
        view_menu.add( observables_frame_visibility );
	view_menu.add( reaction_frame_visibility );
        view_menu.add( simulation_frame_visibility );
        //view_menu.add( model_summary_frame_visibility );
        view_menu.add( debug_frame_visibility );
        view_menu.add( zoom_slider_visibility );
        view_menu.add( engine_output_log_frame_visibility );
        view_menu.add( reset_windows_action );
        
        
	menubar.add(view_menu);
	
	JMenu help_menu = new JMenu("Help");
	help_menu.add(about_action);
        
        JMenuItem documentation_menu_item = new JMenuItem("contents");
        ActionListener helper = new CSH.DisplayHelpFromSource(help_broker);
        documentation_menu_item.addActionListener( helper );
        help_menu.add( documentation_menu_item );
        
	menubar.add( help_menu );


        view_menu.setMnemonic( java.awt.event.KeyEvent.VK_W );
        edit_menu.setMnemonic( java.awt.event.KeyEvent.VK_E );
        help_menu.setMnemonic( java.awt.event.KeyEvent.VK_H );
        
	return menubar;
    }

    void hideSpashScreen()
    {
         splash_frame.setVisible(false);
         splash_frame.dispose();
    }
    
    void displaySplashScreen()
    {
	// Display the splash screen
	splash_frame = new JWindow();
        splash_frame.setAlwaysOnTop( true );
	//splash_frame.addWindowListener( new SplashEventHandler() );
	URL splash_url = this.getClass().getResource("images/splash.png");
	
	 // Milliseconds
	//new SplashScreen( splash_url, splash_frame, pause );
	
	JLabel l = new JLabel(new ImageIcon(splash_url));
        splash_frame.getContentPane().add(l, BorderLayout.CENTER);
        splash_frame.pack();
        Dimension screenSize =
          Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();
        splash_frame.setLocation(screenSize.width/2 - (labelSize.width/2),
                    screenSize.height/2 - (labelSize.height/2));
        
        
	splash_frame.addMouseListener(new MouseAdapter()
            {
                public void mousePressed(MouseEvent e)
                {
                    if (debug_statements) System.out.println("Splash Screen Hidden by Mouse Click");
                    hideSpashScreen();   
                }
            });
        
        
	    splash_frame.setVisible(true);
	   	
    }
    
    /**
     *
     * @param source
     */    
    protected void frameMadeVisible( JInternalFrame source )
    {
        if ( source == search_frame )
        {
            search_frame_visibility.setState( true );
        }
        else if ( source == molecule_frame )
        {
            molecule_frame_visibility.setState( true );
        }
        else if ( source == species_frame )
        {
            species_frame_visibility.setState( true );
        }
        else if ( source == reaction_rule_frame )
        {
            reaction_rule_frame_visibility.setState( true );
        }
        else if ( source == reaction_rule_frame )
        {
            observables_frame_visibility.setState( true );
        }
        else if ( source == simulation_frame )
        {
            simulation_frame_visibility.setState( true );
        }
        else if ( source == model_summary_frame )
        {
            model_summary_frame_visibility.setState( true );
        }
        else if ( source == reaction_frame )
        {
            reaction_frame_visibility.setState( true );
        }
        else if ( source == observables_frame )
        {
            observables_frame_visibility.setState( true );
        }
        else if ( source == editor_frame )
        {
            editor_frame_visibility.setState( true );
        }
        else if ( source == engine_output_log_frame )
        {
            engine_output_log_frame_visibility.setState( true );
        }
        else if ( source == this.reaction_frame )
        {
            reaction_frame_visibility.setState( true );
        }
        else if ( source == journal_frame )
        {
            debug_frame_visibility.setState( true );
        }
    }

    /**
     *
     * @param source
     */    
    protected void frameMadeInvisible( JInternalFrame source )
    { 
        if ( source == search_frame )
        {
            search_frame_visibility.setState( false );
        }
        else if ( source == molecule_frame )
        {
            molecule_frame_visibility.setState( false );
        }
        else if ( source == species_frame )
        {
            species_frame_visibility.setState( false );
        }
        else if ( source == reaction_rule_frame )
        {
            reaction_rule_frame_visibility.setState( false );
        }
        else if ( source == simulation_frame )
        {
            simulation_frame_visibility.setState( false );
        }
        else if ( source == model_summary_frame )
        {
            model_summary_frame_visibility.setState( false );
        }
        else if ( source == reaction_frame )
        {
            reaction_frame_visibility.setState( false );
        }
        else if ( source == observables_frame )
        {
            observables_frame_visibility.setState( false );
        }
        else if ( source == editor_frame )
        {
            editor_frame_visibility.setState( false );
        }
        else if ( source == engine_output_log_frame )
        {
            engine_output_log_frame_visibility.setState( false );
        }
        else if ( source == this.reaction_frame )
        {
            reaction_frame_visibility.setState( false );
        }
        else if ( source == journal_frame )
        {
            this.debug_frame_visibility.setState( false );
        }
    }
    
    /**
     *
     * @param frame
     */    
    public void deiconify(JFrame frame) {
        int state = frame.getExtendedState();
    
        // Clear the iconified bit
        state &= ~Frame.ICONIFIED;
    
        // Deiconify the frame
        frame.setExtendedState(state);
    }
    
    /**
     *
     * @return
     */    
    public MoleculePalette getMoleculePalette() 
    {
        return molecule_palette;
    }
    
    public ReactionRulePalette getReactionRulePalette() 
    {
        return reaction_rule_palette;
    }
    
    public ReactionPalette getReactionPalette() 
    {
        return reaction_palette;
    }
    
    public ObservablesPalette getObservablesPalette() 
    {
        return observables_palette;
    }
    
    /**
     *
     * @return
     */    
    public JScrollPane getMoleculeScrollPane() 
    {
        return molecule_scroll_pane;
    }
    
    /**
     *
     * @return
     */    
    public SpeciesPalette getSpeciesPalette() 
    {
        return species_palette;
    }
    
    public JInternalFrame getSpeciesPaletteFrame() 
    {
        return species_frame;
    }
    
    public ModelParameters getModelParameters()
    {
        return model_parameters;
    }
    
    public WidgetDragHandler getWidgetDragHandler()
    {
        return widget_drag_handler;
    }
    
    public void initialize() 
    {
                    editor_panel.initialize();
                    molecule_palette.initialize();
                    species_palette.initialize();
                    reaction_rule_palette.initialize();
                    observables_palette.initialize();
                    simulation_frame.initialize(); 
                    the_model.initialize();
                    model_summary_panel.initialize();
                    reaction_palette.initialize();
                    IDGenerator idgen = new IDGenerator();
                    idgen.setCurrentID( 0 );
                    simulation_config.initialize();
                    model_parameters.initialize();
                    
    
    }
    
    public JInternalFrame getSearchPaletteFrame() 
    {
        return search_frame;
    }
    
    public JInternalFrame getMoleculePaletteFrame() 
    {
        return molecule_frame;
    }
    
    public JInternalFrame getReactionRulePaletteFrame() 
    {
        return reaction_rule_frame;
    }
   
    public JInternalFrame getEditorFrame() 
    {
        return editor_frame;
    }
    
    public JFrame getMainFrame() 
    {
        return main_frame;
    }
    
     public boolean isSearchPaletteFrameVisible() 
    {
        return search_frame_visibility.getState();
    }
    
    public boolean isMoleculePaletteFrameVisible() 
    {
        return molecule_frame_visibility.getState();
    }
    
    public boolean isSpeciesPaletteFrameVisible() 
    {
        return species_frame_visibility.getState();
    }
    
    public boolean isReactionRulePaletteFrameVisible() 
    {
        return reaction_rule_frame_visibility.getState();
    
    }
    
    public boolean isGroupRulePaletteFrameVisible() 
    {
        return observables_frame_visibility.getState();
    
    }
    
    public boolean isSimulationFrameVisible() 
    {
        return simulation_frame_visibility.getState();
    }   
        
    public boolean isModelSummaryFrameVisible() 
    {
        return model_summary_frame_visibility.getState();
    }   
    
    public boolean isReactionFrameVisible() 
    {
        return reaction_frame_visibility.getState();
    }  
    
    public void run() 
    {
        if ( the_model == null )
	    {
		if (debug_statements) System.out.println("Fatal Error: Model pointer null. Exiting...");
		System.exit(1);
	    }
        

        createAndDisplayGUI();
        
        displayStartupNotice();
        
        //Runnable sc = new Runnable()
        //{
        //    public void run()
        //    {
        //        
        //    }
        //};
        //SwingUtilities.invokeLater( sc );
       
        // Splash screen on a timer
        timer = new Timer( splash_pause, new ActionListener() 
            {
             public void actionPerformed(ActionEvent evt) 
             {
                 // Time's up get rid of the splash screen unless
                 // the user did it manually from within displaySplashScreen()
                 
                 if (splash_frame.isVisible() == true )
                {
                        hideSpashScreen();
                    
                    
                    timer.stop();
                }
             }
            }); 
            
        timer.start();
        
    }
    
    public void refreshAll() 
    {
        try
        {
            molecule_palette.repaint();
            species_palette.repaint();
            reaction_rule_palette.repaint();
            observables_palette.repaint();
            editor_panel.repaint();
            simulation_panel.repaint();
            model_summary_panel.repaint();
        }
        catch ( NullPointerException e )
        {
            
        }
    }
    
    public GUI getTheGUI() 
    {
        return this;
    }    
    
    public void displayJournal() 
    {
        
        journal_frame = new JInternalFrame("Debugging Journal");
            
            journal_pane = new JTextArea();
            journal_pane.setAutoscrolls( true );
            
            JScrollPane editorScrollPane = new JScrollPane(journal_pane);
            editorScrollPane.setVerticalScrollBarPolicy(
	    	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            editorScrollPane.setPreferredSize(new Dimension(800, 500));
    
            journal_frame.getContentPane().add(editorScrollPane, BorderLayout.CENTER);
       
            desktop.add(journal_frame);
            
            //journal_frame.pack();
            //JInternalFrame edpanel = getEditorFrame();
            journal_frame.setSize( 800, 500 );
            journal_frame.setLocation( 50, 50 );
            journal_frame.setIconifiable( true );
            journal_frame.setMaximizable( true );
            journal_frame.setResizable( true );
            
            journal_frame.addInternalFrameListener( new InternalFrameListener() );
        
            journal_frame.setVisible( true );
            
            try
            {
                journal_frame.setIcon(true);
            }
            catch (Exception e )
            {
                e.printStackTrace();
            }
            // any error messages?
            //StreamGobbler error_gobbler = new StreamGobbler( pin, "ERROR");            
            
            // any output?
            //StreamGobbler output_gobbler = new StreamGobbler(piOut, "OUTPUT", journal_pane);

            //output_gobbler.start();
            //error_gobbler.start();
        
            /*
        PipedInputStream piOut = null;
        PipedInputStream piErr = null;
        PipedOutputStream poOut = null;
        PipedOutputStream poErr = null;
        
        try
        {
            piOut = new PipedInputStream();
            poOut = new PipedOutputStream(piOut);
            System.setOut(new PrintStream(poOut, true));
            
            // Set up System.err
            piErr = new PipedInputStream();
            poErr = new PipedOutputStream(piErr);
            System.setErr(new PrintStream(poErr, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        
        //StreamGobbler output_gobbler = new StreamGobbler(piOut, "OUTPUT", journal_pane);            
            
            // any output?
        //StreamGobbler error_gobbler = new StreamGobbler(piErr, "ERROR", journal_pane);
            
            //output_gobbler.start();
            //error_gobbler.start();
              
            
        new ReaderThread(piOut, journal_pane).start();
        new ReaderThread(piErr, journal_pane).start();           
            
        //out.close();
            */
        
        //TextAreaPrintStream output_redirect = new TextAreaPrintStream(journal_pane, System.out);
        //TextAreaPrintStream error_redirect = new TextAreaPrintStream(journal_pane, System.err);
        
        //PrintStream out
        //System.setOut(output_redirect);
        //System.setErr(output_redirect);
        
        return;
    }    
    
    public JTextArea getJournalPane() 
    {
        return journal_pane;
    }
    
    public JInternalFrame getJournalFrame() 
    {
        return journal_frame;
    }
    
    
    public int saveWorkOption(String message) 
    {
        int result = 0;
        boolean save_resolved = false;
        String[] button_labels = {"Save","Don't Save","Cancel"};
            while ( !save_resolved )
            {
            
            
            result = editor_panel.displayConfirm("Save Work?", message, button_labels, button_labels[2] );
            
            if ( result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION )
            {
                return JOptionPane.CANCEL_OPTION;
            }
            else if ( result == JOptionPane.YES_OPTION )
            {
            
            //Set up the file chooser.
		    if (fc == null) {
			fc = new JFileChooser();
			
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			fc.addChoosableFileFilter(new BNGFilter());
			fc.setAcceptAllFileFilterUsed(false);
			
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		    }
		    
		    //Show it.
		    int returnVal = fc.showDialog(main_frame,"Save");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            File save_file = fc.getSelectedFile();
                            
                            String path = save_file.getAbsolutePath();
                            
                            if (!path.matches(".*\\.bng$"))
                               {
                                   path = path+".bng";
                               }
                                
                               save_file = new File( path );
                            
                            if ( save_file.exists() )
                                {
                                    if ( getEditorPanel().displayConfirm("Are you sure you want to overwrite\n" +
                                    " the file named \"" + save_file.getName() + "\"?") )
                                    {
                                        state_saver.save( save_file );
                                    }
                                    else
                                    {
                                        // Save not resolved so begin again
                                        continue;
                                    }
                                }
			    state_saver.save( save_file );
                            save_resolved = true;
			}
                    
		    //Reset the file chooser for the next time it's shown.
		    //fc.setSelectedFile(null);
            }
            else if ( result == JOptionPane.NO_OPTION )
            {
                save_resolved = true;
                
            }
    
           
    }
        
        return result;
    }
    
    public JToolBar createZoomToolbar(WidgetPanel listener)
    {
        URL zoom_out_url = this.getClass().getResource("images/zoom_out.gif");
	URL zoom_in_url = this.getClass().getResource("images/zoom_in.gif");
        URL autozoom_url = this.getClass().getResource("images/autozoom.gif");
       
        
	JToolBar zoom_toolbar = new JToolBar("Zoom Tools");
        // Add a button to the toolbar; remove the label and margin before adding
        JButton b1 = new JButton("Zoom In", new ImageIcon( zoom_in_url ) );
        JButton b2 = new JButton("Zoom Out", new ImageIcon( zoom_out_url ) );
        JButton b3 = new JButton("Autozoom", new ImageIcon( autozoom_url ) );
        
        b1.setActionCommand("Zoom In");
        b1.setMnemonic( java.awt.event.KeyEvent.VK_EQUALS );
        b2.setActionCommand("Zoom Out");
        b2.setMnemonic( java.awt.event.KeyEvent.VK_MINUS );
        b3.setActionCommand("Autozoom");
        
        
        b1.setText(null);
        b2.setText(null);
        b3.setText(null);
        b1.setToolTipText("Zoom In (alt-=)");
        b2.setToolTipText("Zoom Out (alt--)");
        b3.setToolTipText("Zoom to Show All");
        //b1.setMargin(new Insets(0, 0, 0, 0));
        //b2.setMargin(new Insets(0, 0, 0, 0));
        b1.addActionListener( listener );
        b2.addActionListener( listener );
        b3.addActionListener( listener );
        zoom_toolbar.add(b1);
        zoom_toolbar.add(b2);
        zoom_toolbar.add(b3);
        
        return zoom_toolbar;
    }
   
    public void setToolbarListener( JToolBar toolbar, ActionListener listener )
    {
        Component[] components = toolbar.getComponents();
        for ( int i = 0; i < components.length; i++ )
        {
            
            if ( components[i] instanceof JButton )
            {
                JButton current = (JButton)components[i];
                
                // Remove the old listeners
                ActionListener[] old_listeners = current.getActionListeners();
                for ( int j = 0; j < old_listeners.length; j++ )
                {
                    if (debug_statements) System.out.println("Removed old listener from button \"" + current.getActionCommand() +"\"");
                    current.removeActionListener( old_listeners[j] );
                }
                
                // Add the new listener
                if (debug_statements) System.out.println("Added new listener to button \""  + current.getActionCommand() + "\"");
                current.addActionListener( listener );
            }
        }
        
    }
    
    public JToolBar createEditToolbar(WidgetPanel listener)
    {
        URL cut_url = this.getClass().getResource("images/cut.gif");
	URL copy_url = this.getClass().getResource("images/copy.gif");
        URL paste_url = this.getClass().getResource("images/paste.gif");
        URL erase_url = this.getClass().getResource("images/erase.gif");
	URL print_url = this.getClass().getResource("images/print.gif");
 
        
	JToolBar toolbar = new JToolBar("Edit Tools");
        // Add a button to the toolbar; remove the label and margin before adding
        JButton cut = new JButton("cut", new ImageIcon( cut_url ) );
        JButton copy = new JButton("copy", new ImageIcon( copy_url ) );
        JButton paste = new JButton("paste", new ImageIcon( paste_url ) );
        JButton delete = new JButton("delete", new ImageIcon( erase_url ) );
        JButton print = new JButton("print", new ImageIcon( print_url ) );
        
        cut.setMnemonic( java.awt.event.KeyEvent.VK_X );
        copy.setMnemonic( java.awt.event.KeyEvent.VK_C );
        paste.setMnemonic( java.awt.event.KeyEvent.VK_V );
        delete.setMnemonic( java.awt.event.KeyEvent.VK_DELETE );
        print.setMnemonic( java.awt.event.KeyEvent.VK_P );
        
        cut.setActionCommand("cut");
        copy.setActionCommand("copy");
        paste.setActionCommand("paste");
        delete.setActionCommand("Delete");
        print.setActionCommand("print");
        //cut.setText("cut");
        //copy.setText("copy");
        //paste.setText("paste");
        //delete.setText("delete");
        
        cut.setText(null);
        copy.setText(null);
        paste.setText(null);
        delete.setText(null);
        print.setText(null);
       
        cut.setToolTipText( "Cut (alt-x)");
        copy.setToolTipText( "Copy (alt-c)");
        paste.setToolTipText("Paste (alt-v)");
        delete.setToolTipText( "Delete (alt-del)");
        print.setToolTipText( "Print (alt-p)");
        
        
        //b1.setMargin(new Insets(0, 0, 0, 0));
        //b2.setMargin(new Insets(0, 0, 0, 0));
        cut.addActionListener( listener );
        copy.addActionListener( listener );
        paste.addActionListener( listener );
        delete.addActionListener( listener );
        
        toolbar.add(cut);
        toolbar.add(copy);
        toolbar.add(paste);
        toolbar.add(delete);
        toolbar.add(print);
        
        return toolbar;
    }
    
    public JToolBar createCopyToolbar(WidgetPanel listener)
    {
        URL copy_url = this.getClass().getResource("images/copy.gif");
        
	JToolBar toolbar = new JToolBar("Edit Tools");
        JButton copy = new JButton("copy", new ImageIcon( copy_url ) );
        
        copy.setActionCommand("copy");
        
        copy.setText(null);
        
        copy.addActionListener( listener );
        
        toolbar.add(copy);
        return toolbar;
    }
    
    
    public JSlider createZoomSlider(WidgetPanel listener) 
    {
        JSlider zoom_slider = new JSlider(JSlider.VERTICAL, 1, 12, 10);
        zoom_slider.setMinorTickSpacing(1);
        zoom_slider.setMajorTickSpacing(3);
        zoom_slider.setPaintTicks(true);
        zoom_slider.setPaintLabels(true);
        //Create the label table for slider
        Hashtable labelTable = new Hashtable();
        labelTable.put( new Integer( 1 ), new JLabel("-10x") );
        labelTable.put( new Integer( 2 ), new JLabel("-5x") );
        labelTable.put( new Integer( 3 ), new JLabel("") );
        labelTable.put( new Integer( 4 ), new JLabel("") );
        labelTable.put( new Integer( 5 ), new JLabel("-2x") );
        labelTable.put( new Integer( 6 ), new JLabel("") );
        labelTable.put( new Integer( 7 ), new JLabel("") );
        labelTable.put( new Integer( 8 ), new JLabel("") );
        labelTable.put( new Integer( 9 ), new JLabel("") );
        labelTable.put( new Integer( 10 ), new JLabel("1x") );
        labelTable.put( new Integer( 11 ), new JLabel("") );
        labelTable.put( new Integer( 12 ), new JLabel("1.2x") );
        zoom_slider.setLabelTable( labelTable );
        zoom_slider.addChangeListener( listener );
        return zoom_slider;
    }
    
    synchronized public Configuration getConfig() 
    {
        return config;
    }

    synchronized public void setSimulationConfig( SimulationConfig sc ) 
    {
        simulation_config = sc;
    }
    
    synchronized public void setModelParameters( ModelParameters m ) 
    {
        model_parameters = m;
    }
    
    synchronized public SimulationConfig getSimulationConfig() 
    {
        return simulation_config;
    }
    
    public void displayModelParameterDialog() 
    {
        
    }
    
    public void recreateFileMenu() 
    {
        ToolbarAction file_open_action     = new ToolbarAction("open", new ImageIcon(""), "open     ", 'o', editor_panel);
        ToolbarAction file_save_action     = new ToolbarAction("save", new ImageIcon(""), "save     ", 's', editor_panel);
	ToolbarAction file_saveas_action   = new ToolbarAction("save as", new ImageIcon(""), "save as  ", 'd', editor_panel);

        ToolbarAction file_new_action      = new ToolbarAction("new", new ImageIcon(""), "new      ", 'n', editor_panel);
        ToolbarAction file_xmlsave_action  = new ToolbarAction("save as xml", new ImageIcon(""), "save as xml", 'x', editor_panel);
        ToolbarAction configuration_action = new ToolbarAction("settings", new ImageIcon(""), "settings", 'c', editor_panel);
        JMenuItem exit_action              = new JMenuItem("exit");
        exit_action.addActionListener( new AppCloser() );
        
        editor_menu_bar.remove( file_menu );
        file_menu = new JMenu("File");
        
	file_menu.add(file_new_action);
        file_menu.add(file_open_action);
        file_menu.add(file_save_action);
        file_menu.add(file_saveas_action);
	//net_file_menu.add(net_load_BNGL_action );
        //net_file_menu.add(net_file_xmlsave_action);
        file_menu.add(configuration_action);
        file_menu.add(exit_action);
                
        file_menu.addSeparator();
        
        if ( getConfig() != null )
        if ( getConfig().getFileHistory() != null )
        {
            Iterator history_itr = getConfig().getFileHistory().iterator();
            while ( history_itr.hasNext() )
            {
                String path = (String)history_itr.next();
                if (debug_statements) System.out.println("Added " + path + " to file menu.");
            
                file_menu.add( new JMenuItem( path ) );
            }
        }
        
        editor_menu_bar.add( file_menu, 0 );
        
    }
    
    public EditsManager getEditsManager() 
    {
        return edits_manager;
    }
    
    public JInternalFrame getSelectedInternalFrame() 
    {
        Component[] desktop_components = desktop.getComponents();
        for ( int i = 0; i < desktop.getComponentCount(); i++ )
        {
            if ( desktop_components[i] instanceof JInternalFrame )
            {
                if ( ((JInternalFrame)desktop_components[i]).isSelected() )
                {
                    return (JInternalFrame)desktop_components[i];
                }
            }
        }
    
        // No internal frames selected
        return null;
    }
    
    public Widget getCopiedWidget() 
    {
        return copied_widget;
    }

    public boolean mapFPMToReactionRules( File file )
    {
        try
        {
            FileReader fr = new FileReader( file );
            
            Vector<CDKCapsule> mols = new Vector<CDKCapsule>();
            
            LineNumberReader lnr = new LineNumberReader(fr);
            
            //for ( int i = 0; i < 2; i++ )
            
            CDKCapsule current_mol = null;
            while ( (current_mol = getMoleculeFromFPM( lnr ))!=null )
            {
                if (debug_statements) System.out.println("Read molecule named " + current_mol.getLabel() + " with " + current_mol.getCDKMolecule().getAtomCount() + " atoms." );
                mols.add( current_mol );
            }
       
            fr.close();
 
        
        mapFPMToReactionRules(mols, getReactionRulePalette() );
        mapFPMToSpeciesRules(mols,  getSpeciesPalette() );
        mapFPMToContainers(mols,  getMoleculePalette() );
        
        getReactionRulePalette().compressDisplay();
        getSpeciesPalette().compressDisplay();
        getMoleculePalette().compressDisplay();
        
        mapFPMToReactionRules(mols, getEditorPanel() );
        mapFPMToSpeciesRules(mols,  getEditorPanel() );
        mapFPMToContainers(mols,  getEditorPanel() );
        
        
        getEditorPanel().displayInformation("Finished Appending FPM Data","Read " + mols.size() + " molecules from " + file.getName() );
        
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean mapFPMToContainers(Vector<CDKCapsule>mols, WidgetPanel containing_panel ) 
    {
        try
        {
            
            
            Iterator container_itr = containing_panel.getAllContainers().iterator();
                while ( container_itr.hasNext() )
                {
                    BioContainer container = (BioContainer)container_itr.next();
                    
                    CDKCapsule mol = null;

                    // Find the matching molecule
                    Iterator mol_itr = mols.iterator();
                    while ( mol_itr.hasNext() )
                    {
                        mol = (CDKCapsule)mol_itr.next();
                        
                        if ( container.getLabel().equals( mol.getLabel() ) )
                        {
                            CDKCapsule copy = new CDKCapsule( 0,0,0,0, containing_panel );
                            copy.setLabel( mol.getLabel() );
                            copy.setCDKMolecule( (org.openscience.cdk.Molecule)mol.getCDKMolecule().clone() );
                            
                            container.setCDKCapsule( copy );
                            break; // We only want to match one molecule to each container
                            //container.setShowCDK( true );
                        }
                    }
                }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean mapFPMToSpeciesRules(Vector<CDKCapsule>mols, WidgetPanel containing_panel ) 
    {
        try
        {
          
            Vector<Species> species = containing_panel.getAllSpecies();
           
            Iterator species_itr = species.iterator();
            while ( species_itr.hasNext() )
            {
                Species s = (Species)species_itr.next();
                
                Iterator container_itr = s.getContainers().iterator();
                while ( container_itr.hasNext() )
                {
                    BioContainer container = (BioContainer)container_itr.next();
                    
                    CDKCapsule mol = null;

                    // Find the matching molecule
                    Iterator mol_itr = mols.iterator();
                    while ( mol_itr.hasNext() )
                    {
                        mol = (CDKCapsule)mol_itr.next();
                        
                        if ( container.getLabel().equals( mol.getLabel() ) )
                        {
                            CDKCapsule copy = new CDKCapsule( 0,0,0,0, containing_panel );
                            copy.setLabel( mol.getLabel() );
                            copy.setCDKMolecule( (org.openscience.cdk.Molecule)mol.getCDKMolecule().clone() );
                            
                            container.setCDKCapsule( copy );
                            break; // We only want to match one molecule to each container
                            //container.setShowCDK( true );
                        }
                    }
                }
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return true;
    }
    
    public boolean mapFPMToReactionRules(Vector<CDKCapsule>mols, WidgetPanel containing_panel ) 
    {
        try
        {
            Iterator<ReactionRule> rule_itr = containing_panel.getAllReactionRules().iterator();
            while ( rule_itr.hasNext() )
            {
                ReactionRule rule = rule_itr.next();
                Vector<BioContainer> containers = rule.getContainers();
           
            Iterator container_itr = containers.iterator();
            while ( container_itr.hasNext() )
            {
                BioContainer container = (BioContainer)container_itr.next();
                
                CDKCapsule mol = null;

                    // Find the matching molecule
                    Iterator mol_itr = mols.iterator();
                    while ( mol_itr.hasNext() )
                    {
                        mol = (CDKCapsule)mol_itr.next();
                        
                        if ( container.getLabel().equals( mol.getLabel() ) )
                        {
                            CDKCapsule copy = new CDKCapsule( 0,0,0,0, containing_panel );
                            copy.setLabel( mol.getLabel() );
                            copy.setCDKMolecule( (org.openscience.cdk.Molecule)mol.getCDKMolecule().clone() );
                            
                            container.setCDKCapsule( copy );
                            break; // We only want to match one molecule to each container
                            //container.setShowCDK( true );
                        }
                    }
                }
            
            rule.layout();
            }
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
        return true;
    }
    
    private CDKCapsule getMoleculeFromFPM( LineNumberReader lnr ) throws Exception
    {
        org.openscience.cdk.Molecule mol = new org.openscience.cdk.Molecule();
        
        String CompoundName = lnr.readLine(); //read the compound name
        
        if ( CompoundName == null ) return null;
        
        if (debug_statements) System.out.println( "Molecule Name: " + CompoundName );
        
        int NoOfAtoms = Integer.parseInt(lnr.readLine()); //get the number of atoms
        
        if (debug_statements) System.out.println( "Number of Atoms: " + NoOfAtoms );
        
        //read the atom information
        String str;
        for(int i = 0; i < NoOfAtoms;i++)
        { 
            str = lnr.readLine();
            if (debug_statements) System.out.println( "Atom: " + str );
            StringTokenizer st = new StringTokenizer(str," \t\n\r\f:");            
            String AtomElementSymbol = st.nextToken();
            int AtomIndex = Integer.parseInt(st.nextToken());
            
            org.openscience.cdk.Atom a = new org.openscience.cdk.Atom(AtomElementSymbol);
                
            int CarbonIndex = 0;
            if ( AtomElementSymbol.equals("C") )
            {
                try
                {
                    CarbonIndex = Integer.parseInt(st.nextToken());
                    String id = AtomElementSymbol += CarbonIndex;
                    a.setID( id );
                }
                catch ( NoSuchElementException nse )
                {
                    // It is a "context" atom?
                }
            }
                
                
                mol.addAtom(a);
        }
        
        //read the number of bonds
        int NoOfBonds = Integer.parseInt(lnr.readLine());
        
        if (debug_statements) System.out.println( "Number of Bonds: " + NoOfBonds );
        
        //parse the bond information
        for(int i = 0; i < NoOfBonds;i++)
        {
            str = lnr.readLine();
            StringTokenizer st = new StringTokenizer(str," \t\n\r\f:");
            
            int BondIndex = Integer.parseInt(st.nextToken()); 
            int Atom1Index = Integer.parseInt(st.nextToken()); 
            int Atom2Index = Integer.parseInt(st.nextToken()); 
            int BondType = Integer.parseInt(st.nextToken());
            
            
            org.openscience.cdk.Bond b = new org.openscience.cdk.Bond(mol.getAtomAt(Atom1Index - 1),mol.getAtomAt(Atom2Index - 1),BondType);
            mol.addBond(b);               
            
        }
        
        lnr.readLine(); //read the /// line
        
        CDKCapsule cdk_capsule = new CDKCapsule( 0, 0, 0, 0, new WidgetPanel(this) );
        cdk_capsule.setLabel( CompoundName );
        cdk_capsule.setCDKMolecule( mol );
        
        return cdk_capsule;
    }

    public void loadBNGFile(String path) 
    {
        if (debug_statements) System.out.println("Loading " + path);
        
        File load_file = new File( path );
       
        if ( !state_loader.load( load_file ) ) 
			        {
			    	   getEditorPanel().displayError("State Load Error",
						       "Error loading state from file");
			        }
                                else
                                {
                                     current_save_file = load_file;
                                     String project_name = current_save_file.getName().replaceAll(".bng$","");
                                     main_frame.setTitle("Rule Builder " + version + " - " + project_name );
                                     
                  
                                     if ( getConfig().addToFileHistory( path ) )
                                     {
                                         // If first file add a separator
                                        if ( getConfig().getFileHistory().size() == 1 )
                                        {
                                            file_menu.addSeparator();
                                        }
                                        
                                        ToolbarAction file_history_entry = new ToolbarAction(path, new ImageIcon(""), "file history", 'o', editor_panel);
                                        file_menu.add( file_history_entry );
                                     }
                                }
    }

    private String bng_startup_path;

    public boolean print( JComponent jc )
    {
        PrintUtilities.printComponent( jc );
        return true;
    }

    public JInternalFrame getEngineOutputLogFrame() 
    {
        return engine_output_log_frame;
    }

    public Plotter getPlotter() 
    {
            return simulation_frame;
    }

    public boolean isSaveNeeded() {
        return save_needed;
    }

    public void setSaveNeeded(boolean save_needed) {
        this.save_needed = save_needed;
    }

    public void exportFile() 
    {
        if (export_fc == null) {
			export_fc = new JFileChooser();
			
                        export_fc.setAcceptAllFileFilterUsed(false);
                        export_fc.setFileFilter( new BNGLFilter() );
		    }
                
              //Show it.
		    int returnVal = export_fc.showDialog(main_frame,"Export");
                
                //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            File export_file = export_fc.getSelectedFile();
                            String path = export_file.getAbsolutePath();
                            
                            
                     
                
                if ( export_fc.getFileFilter() instanceof BNGLFilter )
                            {
                                //append .bngl if not already the extension
                                if (!path.matches(".*\\.bngl$"))
                                {
                                    path = path+".bngl";
                                }
                            
                                export_file = new File(path);
                            
                                if ( export_file.exists() )
                                {
                                    if ( !getEditorPanel().displayConfirm("Are you sure you want to overwrite\n" +
                                    " the file named \"" + export_file.getName() + "\"?") )
                                    {
                                        return;
                                    }
                                }
                            
                                
                                try
                                {
                                    getModel().writeBNGL( export_file );
                                }
                                catch( BNGLOutputMalformedException bom )
                                {
                                    getEditorPanel().displayError("Error Writing the BNGL File", bom.getMessage());
                                    return;
                                }
                            }
                    }
                            
    }

    public void importFile() 
    {
        if (import_fc == null) 
                {
			import_fc = new JFileChooser();
			//Add a custom file filter and disable the default
			//(Accept All) file filter.
			//open_fc.addChoosableFileFilter(new XMLFilter());
                        import_fc.addChoosableFileFilter(new NetFilter());
                        //import_fc.addChoosableFileFilter(new BNGLFilter());
                        //open_fc.addChoosableFileFilter(new BNGFilter());
                        
			import_fc.setAcceptAllFileFilterUsed(false);
			
                        import_fc.setFileFilter( new BNGLFilter() );
                        
			//Add custom icons for file types.
			//fc.setFileView(new ImageFileView());
			
			//Add the preview pane.
			//fc.setAccessory(new ImagePreview(fc));
		  }
		    
		    //Show it.
		    int returnVal = import_fc.showDialog(main_frame,"Import");
		    
		    //Process the results.
		    if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
                            String path = import_fc.getSelectedFile().getAbsolutePath();
                            
                         if ( import_fc.getFileFilter() instanceof BNGLFilter )
                         {
                            if (!path.matches(".*\\.bngl$"))
                               {
                                   path = path+".bngl";
                               }
                               
                               File load_file = new File( path );
                               
                                int result = saveWorkOption("This action will replace your current work. Do you want to save first?");
                                if ( result == JOptionPane.CANCEL_OPTION )
                                {
                                    return;
                                }
                                
                                
                                initialize();
			    
                                boolean failed = false;
                                
                                failed = !getModel().readBNGL( load_file );
                                
                                if ( failed )
                                {
                                    editor_panel.displayError("BNGL Load Error",
						       "Error loading bngl file");
                                } 
                         }
                    else if ( import_fc.getFileFilter() instanceof NetFilter )
                    {
                               if (!path.matches(".*\\.net$"))
                               {
                                   path = path+".net";
                               }
                               
                               File load_file = new File( path );
                               
                                int result = saveWorkOption("This action will replace your current work. Do you want to save first?");
                                if ( result == JOptionPane.CANCEL_OPTION )
                                {
                                    return;
                                }
                                
                                
                                initialize();
                                //set the cursor to wait
                                //getEditorPanel().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                                
                                
				if (simulation_config.getSimulationReadNetFile()) 
                                {
					if (debug_statements) System.out.println(" GUI finds readnetflag on ");
                                	if ( !getModel().readNetwork( load_file ) ) {
						if (debug_statements) System.out.println (" GUI can't exec readNetwork on file ");
                                    		editor_panel.displayError("Network Error",
						"Error loading network file");
                                	}
                                	else {
						if (debug_statements) System.out.println (" GUI found readNetwork file ");
                                    		current_save_file = load_file;
                                	}
                                    }
				else 
                                {
					if (debug_statements) System.out.println (" GUI finds readnetflag is off");
                                }
                        } 
                    }
    }

    public void positionWindows() 
    {
        int inset = 20;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        main_frame.setBounds(inset, inset,
                  screenSize.width  - inset*2,
                  screenSize.height - inset*2);

       
        // Build the Editor Window
        int height = (int)(main_frame.getHeight()*0.6 );
	int width = (int)(main_frame.getWidth()*0.72 );
        int x = (int)(10);
        int y = (int)(10);
        
        editor_frame.setSize( width, height );
	editor_frame.setLocation( x, y );
        editor_scroll_pane.setPreferredSize(new Dimension(width,height));
      
        
        int padding = 5;
        x = x + width + padding;
        molecule_frame.setSize( width/3, height/2 );
        molecule_frame.setLocation( x, y );
        
        search_frame.setSize( width/3, height/2 );
        search_frame.setLocation( x, y );
        
        engine_output_log_frame.setSize( 3*width/4, 4*height/5 );
        engine_output_log_frame.setLocation( x, y );
   
        species_frame.setSize( width/3, height/2 );
        species_frame.setLocation( x, y+height/2 );
        
        observables_frame.setSize( width/3, height/2 );
        observables_frame.setLocation( x, y+height/2 );
        
        reaction_rule_frame.setLocation( editor_frame.getX(), editor_frame.getY() + editor_frame.getHeight()+padding);
        reaction_rule_frame.setSize( editor_frame.getWidth()+padding+molecule_frame.getWidth(), (int)(0.51*(main_frame.getHeight() - reaction_rule_frame.getY())) - padding );
      
        reaction_frame.setSize( 400, 300 );
        reaction_frame.setLocation( editor_frame.getX(), editor_frame.getY() + editor_frame.getHeight()+20);
        
        model_summary_frame.setSize( width/3, height );
        model_summary_frame.setLocation( x, y );
        
    }

    

    public void displayNotes() 
    {    
       notes_frame.setVisible(true);
         
       try 
       {  
        notes_frame.setIcon(false);
       }
       catch (Exception veto)
       {
        veto.printStackTrace();
       } 
    }

    public void displayStartupNotice() 
    {
        try
        {
            JInternalFrame notes_frame = new JInternalFrame();
            desktop.add(notes_frame);
            notes_frame.setSelected(true);
            notes_frame.getContentPane().removeAll(); // Clear previous contents
          
            notes_frame.setClosable( true );
            
            try
            {
                notes_frame.setIcon(false);
            }
            catch (Exception veto)
            {
                veto.printStackTrace();
            }
            
            JPanel panel = new JPanel();
            
            Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
            
            int width = (int)screen_size.getWidth()/2;
            
            panel.setSize(width, 400);
            
            notes_pane.setEditable( false );
            notes_pane.setBackground(Color.WHITE);
            notes_pane.setForeground(Color.BLACK);
            notes_pane.setAutoscrolls( true );
            
            JScrollPane notes_scrollpane = new JScrollPane(notes_pane);
            notes_scrollpane.setVerticalScrollBarPolicy(
	    	JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            notes_scrollpane.setPreferredSize(new Dimension(width, 400));
    
            
            panel.add( notes_scrollpane );
            notes_frame.getContentPane().add(panel);//(log_scrollpane, BorderLayout.CENTER);
       
            notes_frame.setSize( width, 400 );
            
            
            
            notes_frame.setLocation( (screen_size.width-width)/2, 50 );
            
            notes_frame.pack();
            
            SimpleAttributeSet heading_sas = new SimpleAttributeSet();
            StyleConstants.setFontFamily(heading_sas, "SansSerif");
            StyleConstants.setFontSize(heading_sas, 24);
                   
            StyleConstants.setForeground(heading_sas, Color.blue);
            StyledDocument styled_doc = notes_pane.getStyledDocument();
            Position position = styled_doc.getEndPosition();
            
            SimpleAttributeSet normal_sas = new SimpleAttributeSet();
            StyleConstants.setFontFamily(normal_sas, "SansSerif");
            StyleConstants.setFontSize(normal_sas, 15);
                   
            StyleConstants.setForeground(normal_sas, Color.blue);
            
            int offset = position.getOffset();
            
            String heading = "Welcome to RuleBuilder " + version;
            String text = "\n\nYour feedback on this software will help us resolve bugs and improve workflow. Please subscribe to the BioNetGen list server at www.bionetgen.com for new release information. Your observations and suggestions regarding RuleBuilder and BioNetGen should be sent to the mailing list at bionetgen@bionetgen.com. \n\nWe take your feedback very seriously and thank you for participating.";
       
            styled_doc.insertString(offset, text, normal_sas );
            styled_doc.insertString(offset, heading, heading_sas ); 
             
            
            
                notes_frame.setVisible(true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        
        
    }

    

}
    
