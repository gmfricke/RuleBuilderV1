/*
 * SimulationConfig.java
 * @author mlf
 *
 */

//
// OUTLINE OF MAIN SECTIONS
//	Network
//	Equilibration
//	Simulation Method
//	Time Course
//	Additional Options
//
//  encoding of graphics objects for each of these sections is found in this order
//
//  the option to read in a network input file and use along with the other settings
//  is still encoded, but completely turned off and invisible to the user with the
//  the selection of 'generate new' set to true; at a later time if it is desired to
//  provide this option the relevant JPanels containing the buttons for choosing
//  between 'select from files' or 'generate new' may be reinstated and added
//  to parent panels for display

import java.awt.*; 			// For graphical windowing tools
import java.awt.event.*; 		// For mouse interactions
import java.beans.*;
import java.io.Serializable;
import java.io.*;		 	// For file manipulation and object writing
import javax.swing.filechooser.*; 	// for GUI file save and open
import java.text.*; 			// for adding attributes to strings such as color
import javax.swing.*; 			// For graphical interface tools
import javax.swing.border.*;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.FlowLayout;
import java.util.Arrays;
import java.util.List;
import javax.swing.JFileChooser;
import java.lang.*;

public class SimulationConfig extends Object implements Serializable 
{
	// ACTION HANDLING CLASSES

	private class NetworkChooseFromFiles implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (choosefromfilesButton.isSelected()) {
				max_num_molecules_textfield.setEnabled(false);
				max_num_ruleapplications_textfield.setEnabled(false);
				isomorphismBox.setEnabled(false);
				network_generatenew_table_subpanel.TableDisable();
				network_choosefromfilesvalue = true;
				network_generatenewvalue = false;
				the_gui.getEditorPanel().displayError("Network File Error","This option is not operational for this version.");
				// uncomment below once we know from JF how to handle preexisting network files and bngl files in a single call to BNG
				//File ef = getFileFromUser();
				//String filename = ef.getName();
				//if (ef.exists()) {
			        //	 if (debug_statements) System.out.println("I have a network file to go get: " + filename);
				//}
				//else {
				//	if (debug_statements) System.out.println("I couldn't find the requested network file to go get: " + filename);
				//	the_gui.getEditorPanel().displayError("Network File Error",filename + " is not usable");
				//}
			}
			else {
				max_num_molecules_textfield.setEnabled(true);
				max_num_ruleapplications_textfield.setEnabled(true);
				network_choosefromfilesvalue = false;
				network_generatenewvalue = true;
				isomorphismBox.setEnabled(true);
			}
		}
	}

	private class NetworkGenerateNew implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (generatenewButton.isSelected()) {
				max_num_molecules_textfield.setEnabled(true);
				max_num_ruleapplications_textfield.setEnabled(true);
				network_choosefromfilesvalue = false;
				network_generatenewvalue = true;
				isomorphismBox.setEnabled(true);
			}
			else {
				max_num_molecules_textfield.setEnabled(false);
				max_num_ruleapplications_textfield.setEnabled(false);
				network_choosefromfilesvalue = true;
				network_generatenewvalue = false;
				isomorphismBox.setEnabled(false);
			}
		}
	}

	private class EquilibrationEquilibrate implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (performequilibrationButton.isSelected()) {
				time_between_equil_checks_textfield.setEnabled(true);
				max_num_equil_checks_textfield.setEnabled(true);
				equilibration_equilibratevalue = true;
				equilibration_noequilibratevalue = false;
			}
			else {
				time_between_equil_checks_textfield.setEnabled(false);
				max_num_equil_checks_textfield.setEnabled(false);
				equilibration_equilibratevalue = false;
				equilibration_noequilibratevalue = true;
			}
		}
	}

	private class EquilibrationNoEquilibrate implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (noequilibrationButton.isSelected()) {
				time_between_equil_checks_textfield.setEnabled(false);
				max_num_equil_checks_textfield.setEnabled(false);
				equilibration_equilibratevalue = false;
				equilibration_noequilibratevalue = true;
			}
			else {
				time_between_equil_checks_textfield.setEnabled(true);
				max_num_equil_checks_textfield.setEnabled(true);
				equilibration_equilibratevalue = true;
				equilibration_noequilibratevalue = false;
			}
		}
	}

	private class SimulationODE implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			abs_tol_textfield.setEnabled(true);
			rel_tol_textfield.setEnabled(true);
			sparseBox.setEnabled(true);
			simulation_ODEvalue = true;
			simulation_SSAvalue = false;
		}
	}

	private class SimulationSSA implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			abs_tol_textfield.setEnabled(false);
			rel_tol_textfield.setEnabled(false);
			sparseBox.setEnabled(false);
			simulation_ODEvalue = false;
			simulation_SSAvalue = true;
		}
	}

	private class TimecourseTendnsteps implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (tendnstepsButton.isSelected()) {
				n_steps_textfield.setEnabled(true);
				t_end_textfield.setEnabled(true);
				timecourse_tendnstepsvalue = true;
				timecourse_sampletimesvalue = false;
			}
			else {	
				n_steps_textfield.setEnabled(false);
				t_end_textfield.setEnabled(false);
				timecourse_tendnstepsvalue = false;
				timecourse_sampletimesvalue = true;
			}
		}
	}

	private class TimecourseSampletimes implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (sampletimesButton.isSelected()) {
				n_steps_textfield.setEnabled(false);
				t_end_textfield.setEnabled(false);
				timecourse_tendnstepsvalue = false;
				timecourse_sampletimesvalue = true;
			}
			else {
				n_steps_textfield.setEnabled(true);
				t_end_textfield.setEnabled(true);
				timecourse_tendnstepsvalue = true;
				timecourse_sampletimesvalue = false;
			}
		}
	}

	private class SimulationSparse implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (sparseBox.isSelected()) {
				simulation_sparsevalue = true;
			}
			else {
				simulation_sparsevalue = false;
			}
		}
	}

	private class SimulationReadNetFile implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (readnetfileBox.isSelected()) {
				simulation_readnetfilevalue = true;
			}
			else {
				simulation_readnetfilevalue = false;
			}
		}
	}

	private class NetworkIsomorphism implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (isomorphismBox.isSelected()) {
				network_isomorphismvalue = true;
			}
			else {
				network_isomorphismvalue = false;
			}
		}
	}

        private class NetworkPreGenerate implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (pregenerateBox.isSelected()) 
                        {
				network_pregeneratevalue = true;        
			}
			else 
                        {
				network_pregeneratevalue = false;
                                abs_tol_textfield.setEnabled(false);
                                rel_tol_textfield.setEnabled(false);
                                sparseBox.setEnabled(false);
                                simulation_ODEvalue = false;
                                simulation_SSAvalue = true;
                                setSimulationSSA(true);
			}
		}
	}
        
	private class OptionsVerbose implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (verboseBox.isSelected()) {
				options_verbosevalue = true;
			}
			else {
				options_verbosevalue = false;
			}
		}
	}

	private class OptionsSBML implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			handleSelected();
		}

		private void handleSelected() {
			if (sbmlBox.isSelected()) {
				options_sbmlvalue = true;
			}
			else {
				options_sbmlvalue = false;
			}
		}
	}

    	private class SimCancel implements ActionListener {
         	public void actionPerformed(ActionEvent event) { 
        		handleCancel(); 
		}
         
         	private void handleCancel() {
             		if ( !the_gui.getEditorPanel().displayQuestion("Cancel Changes","Are you sure you want to cancel changes to settings?") ) {
             			return;
			}
             		else {
                		config_dialog.setVisible( false );
                		config_dialog.dispose();
             		}
         	}
   	}
   
    	private class SimCloser extends WindowAdapter implements ActionListener {
    		public void actionPerformed(ActionEvent event) {
        		handleClose();
    		}
    
    		public void windowClosing(WindowEvent e) {
			handleClose();
    		}
    
    		private void handleClose() {        

			// ensure that the user has input at least three sample times, if this option has been chosen
			// save user sample times data; and verify receipt within the debug window
			if (sampletimesButton.isSelected()) {
				temptable = timecourse_sampletimes_table_subpanel.getJTable();
				CellEditor current_timecourse_sampletimes_table_editor = temptable.getCellEditor();
				if (current_timecourse_sampletimes_table_editor != null) { current_timecourse_sampletimes_table_editor.stopCellEditing(); }	
				usersampletimes = new Vector<String>();
				usersampletimes = timecourse_sampletimes_table_subpanel.getNewSampleTimes();
				if (usersampletimes.size() > 2) {
					Enumeration e = usersampletimes.elements();
					while (e.hasMoreElements()) {
						if (debug_statements) System.out.println("I have a sample time: " + (String)e.nextElement());
					}
				}
				else {
 					the_gui.getEditorPanel().displayError( "Output Capture:  Sample Times Option Error","At least three sample time must be provided.");
                                	return; 
				}
			}

			// save max stoich table data; and verify receipt within the debug window
			temptable = network_generatenew_table_subpanel.getJTable();
			CellEditor current_network_generatenew_table_editor = temptable.getCellEditor();
			if (current_network_generatenew_table_editor != null) { current_network_generatenew_table_editor.stopCellEditing(); }
			maxstoichlimits = network_generatenew_table_subpanel.getNewTableData();
			Iterator<Vector<Object>> maxstoichrowiter = maxstoichlimits.iterator();
			Vector<Object> maxstoichthisrow;
			Iterator<Object> maxstoichcoliter;
                	while (maxstoichrowiter.hasNext()) {
				maxstoichthisrow = maxstoichrowiter.next();
				maxstoichcoliter = maxstoichthisrow.iterator();
				while (maxstoichcoliter.hasNext()) {
					Object current = maxstoichcoliter.next();
					String currentstring = current.toString();
					if (debug_statements) System.out.println("I have a max stoich value " + currentstring);
				}
			}

			// save equilibration table data; and verify receipt within the debug window
			if (equilibration_equilibratevalue) {
				int falsecount = 0;
				int truecount = 0;
				temptable = equilibration_equilibrate_table_subpanel.getJTable();
				CellEditor current_equilibration_equilibrate_table_editor = temptable.getCellEditor();
				if (current_equilibration_equilibrate_table_editor != null) { current_equilibration_equilibrate_table_editor.stopCellEditing(); }
				equilbooleans = equilibration_equilibrate_table_subpanel.getNewTableData();
				Integer equilbooleanssize = new Integer(equilbooleans.size());
				Iterator<Vector<Object>> equilbooleansrowiter = equilbooleans.iterator();
				Vector<Object> equilbooleansthisrow;
				Iterator<Object> equilbooleanscoliter;
                		while (equilbooleansrowiter.hasNext()) {
					equilbooleansthisrow = equilbooleansrowiter.next();
					String specieslabelstring = equilbooleansthisrow.elementAt(0).toString();
                                	String speciesboolstring = equilbooleansthisrow.elementAt(1).toString();
					if (debug_statements) System.out.println("I have an equilibration table species name of " + specieslabelstring);
					if (debug_statements) System.out.println("I have an equilitration table boolean value of " + speciesboolstring); 
					if (speciesboolstring.equals("false")) {
						++falsecount;
					}
					else {
						++truecount;
					}
				}
				if (!(falsecount > 0)) {
					if (debug_statements) System.out.println("force the user to check off at least one checkbox");
 					the_gui.getEditorPanel().displayError( "Equilibration:  Species Participation Error","At least one species must be checked off.");
                                	return; 
				}
				if (!(truecount > 0)) {
					if (debug_statements) System.out.println("force the user to check on at least one checkbox");
 					the_gui.getEditorPanel().displayError( "Equilibration:  Species Participation Error","At least one species must be checked on.");
                                	return; 
				}
			}

                        the_gui.setSaveNeeded( true );
                        
        		config_dialog.setVisible( false );
        		config_dialog.dispose();
    		}
  	}
  
	public SimulationConfig(GUI gui, Model model) {
		this.the_gui = gui;
		this.the_model = model;
	}
	 
    	public SimulationConfig() {
    	}
    
    	public void setGUI( GUI the_gui ) {
        	this.the_gui = the_gui;
    	}

	public void setModel (Model the_model ) {
		this.the_model = the_model;
	}
    
    	// Serialization explicit version
    	private static final long serialVersionUID = 1;

        transient protected boolean debug_statements = true;
        
	// DEFAULT VALUES

	// Network
	private String max_num_molecules = "500";
	private String max_num_ruleapplications = "25";
	private String[] maxstoich_headings_stringarray = new String[] {"Molecule","Count"};

	// Equilibration
	private String time_between_equilibration_checks = "10000";
	private String max_number_equilibration_checks = "100";
	private String[] equil_headings_stringarray = new String[] {"Species","Include"};

	// Simulation Method
	private String abs_integ_error = "1e-12";
    	private String rel_integ_error = "1e-12";
	private String sampletimes_headings_stringarray[] = new String[] {"Time Values"};

	// Time Course
	private String n_steps = "10";
	private String t_end = "1000";

	// Table Headings
	private String network_table_title = "Stoichiometry Max Counts ";
	private String equilibration_table_title = "Species Participation    ";
	private String timecourse_table_title = "Sample Times";
    
    	transient JDialog config_dialog;
    	transient GUI the_gui;
	transient Model the_model;
	transient State the_state;
	transient TablePanelPreload.TableModelPreload temptablemodel;
	transient JTable temptable;

	// SCREEN COMPONENTS

	private	JTextField max_num_molecules_textfield = new JTextField(max_num_molecules,4);
	private	JTextField max_num_ruleapplications_textfield = new JTextField(max_num_ruleapplications,4);
	private JTextField time_between_equil_checks_textfield= new JTextField(time_between_equilibration_checks,4);
	private JTextField max_num_equil_checks_textfield = new JTextField(max_number_equilibration_checks,4);
    	private	JTextField abs_tol_textfield = new JTextField(abs_integ_error,4);
    	private JTextField rel_tol_textfield = new JTextField(rel_integ_error,4); 
    	private JTextField n_steps_textfield = new JTextField(n_steps,4);   
    	private JTextField t_end_textfield = new JTextField(t_end,4); 

	private	JRadioButton choosefromfilesButton = new JRadioButton("select from files");
	private JRadioButton generatenewButton = new JRadioButton("generate new");
	private JRadioButton performequilibrationButton = new JRadioButton("run equilibrate");
	private JRadioButton noequilibrationButton = new JRadioButton("skip equilibrate");
	private JRadioButton ordinarydifferentialequationButton = new JRadioButton("ODE");
	private	JRadioButton stochasticsimulationalgorithmButton = new JRadioButton("SSA");
	private	JRadioButton tendnstepsButton = new JRadioButton("t end, t steps");
	private	JRadioButton sampletimesButton = new JRadioButton("sample times");

	private JCheckBox readnetfileBox = new JCheckBox("read net file into engine");
	private	JCheckBox isomorphismBox = new JCheckBox("isomorphism check");
	private	JCheckBox pregenerateBox = new JCheckBox("pre-generate network");
        private JCheckBox sparseBox = new JCheckBox("sparse");
	private	JCheckBox verboseBox = new JCheckBox("debug output");
	private	JCheckBox sbmlBox = new JCheckBox("SBML output");

	// BOOLEANS FOR SAVING STATE - DEFAULTS - USE THESE IN ABSENCE OF RESTORABLE SETTINGS FILE

	public boolean network_choosefromfilesvalue = false;
	public boolean network_generatenewvalue = true;
	public boolean equilibration_equilibratevalue = false;
	public boolean equilibration_noequilibratevalue = true;
	public boolean simulation_ODEvalue = true;
	public boolean simulation_SSAvalue = false;
	public boolean timecourse_tendnstepsvalue = true;
	public boolean timecourse_sampletimesvalue = false;
	public boolean simulation_sparsevalue = false;
	public boolean simulation_readnetfilevalue = false;
	public boolean network_isomorphismvalue = true;
        public boolean network_pregeneratevalue = true; 
	public boolean options_verbosevalue = false;
	public boolean options_sbmlvalue = false;

	public TablePanelPreload network_generatenew_table_subpanel;
	public TablePanelPreload equilibration_equilibrate_table_subpanel;
	public TablePanelUserInput timecourse_sampletimes_table_subpanel;

	// VECTORS
	Vector<String> usersampletimes;
	Vector<Vector<Object>> maxstoichlimits;
	Vector<Vector<Object>> equilbooleans;

    	public void displaySimConfigWindow() {
        
        	boolean is_modal = true;
		this.setModel(the_gui.getModel());

		// FRAMES
        
        	JFrame ef = the_gui.getMainFrame();

		// TABLE PANELS

		// rowbdata was originally used as a source of boolean data for the equil data; it is
		// tied to molecules not species, and is therefore currently not in use
		Vector<Vector<Object>> rowbdata = new Vector<Vector<Object>>();
		Vector<Vector<Object>> localmaxstoichlimits = new Vector<Vector<Object>>();
        	Vector<MoleculeType> mtdata = the_model.getMoleculeTypes();
        	Iterator<MoleculeType> itr = mtdata.iterator();
		Object[] idata = new Object[2];
		Object[] bdata = new Object[2];
		int ii = 0;
        	while (itr.hasNext()) {
        		MoleculeType mt = itr.next();
                	String mtlabel = mt.getLabel();
                	if (debug_statements) System.out.println("SimConfig.displaySimConfigWindow found component of type MoleculeType " + mt.getLabel());
			idata[0] = mtlabel; idata[1] = "unlimited";
			bdata[0] = mtlabel; bdata[1] = true;
			Vector bentry = new Vector(Arrays.asList(bdata));
			Vector ientry = new Vector(Arrays.asList(idata));
			rowbdata.addElement(bentry);
                	localmaxstoichlimits.addElement(ientry);
			++ii;
        	}       
		Vector<Vector<Object>> localequilbooleans = new Vector<Vector<Object>>();
		Vector<Species> spdata = the_model.getSpecies();
		Iterator<Species> spitr = spdata.iterator();
		Object[] sdata = new Object[2];
		int jj = 0;
		while (spitr.hasNext()) {
			Species s = spitr.next();
			String slabel = s.getLabel();
			if (debug_statements) System.out.println("SimConfig.displaySimConfigWindow found species " + s.getLabel());
			sdata[0] = slabel; sdata[1] = true;
			Vector sbentry = new Vector(Arrays.asList(sdata));
			localequilbooleans.addElement(sbentry);
			++jj;
		}

		// set up coherence between what might be found in an existing input file and what is now on the screen for maxstoich table
		maxstoichlimits = getNetworkStoichLimitValues();
		int maxstoichlimitssize = maxstoichlimits.size();
		if (maxstoichlimitssize > 0) {  // we have data from file that should amend what we have locally
			Iterator<Vector<Object>> localmaxstoichlimitsrowiter = localmaxstoichlimits.iterator();
			Iterator<Vector<Object>> maxstoichlimitsrowiter = maxstoichlimits.iterator();
			Vector<Object> localmaxstoichlimitsthisrow;
			Vector<Object> maxstoichlimitsthisrow;
			Boolean namenotyetfound;
			while (localmaxstoichlimitsrowiter.hasNext()) {
				localmaxstoichlimitsthisrow = localmaxstoichlimitsrowiter.next();
				String thisrowitemname = localmaxstoichlimitsthisrow.elementAt(0).toString();
				if (debug_statements) System.out.println("localmaxstoichlimitsthisrow itemname is " + thisrowitemname);
				namenotyetfound = true;
				while (maxstoichlimitsrowiter.hasNext() & namenotyetfound) {
					maxstoichlimitsthisrow = maxstoichlimitsrowiter.next();
					String maxstoichlimitsthisrowitemname = maxstoichlimitsthisrow.elementAt(0).toString();
					if (debug_statements) System.out.println("maxstoichlimitsthisrow itemname is " + thisrowitemname);
					if (thisrowitemname.equals(maxstoichlimitsthisrowitemname)) {
						if (debug_statements) System.out.println("item names match found");
						localmaxstoichlimitsthisrow.removeElementAt(1);
						localmaxstoichlimitsthisrow.insertElementAt(maxstoichlimitsthisrow.elementAt(1),1);
						namenotyetfound = false;
					}
				}
			}
		}
		maxstoichlimits = localmaxstoichlimits;
		setNetworkMaxStoichLimitValues(localmaxstoichlimits);
		network_generatenew_table_subpanel = new TablePanelPreload("yes",the_gui,this,maxstoich_headings_stringarray,maxstoichlimits,250,160,network_table_title);

		// set up coherence between what might be found in an existing input file and what is now on the screen for equil table
		equilbooleans = getEquilibrationBooleanValues();
		int equilbooleanssize = equilbooleans.size();
		if (equilbooleanssize > 0) {  // we have data from file that should amend what we have locally
			Iterator<Vector<Object>> localequilbooleansrowiter = localequilbooleans.iterator();
			Iterator<Vector<Object>> equilbooleansrowiter = equilbooleans.iterator();
			Vector<Object> localequilbooleansthisrow;
			Vector<Object> equilbooleansthisrow;
			Boolean equilnamenotyetfound;
			while (localequilbooleansrowiter.hasNext()) {
				localequilbooleansthisrow = localequilbooleansrowiter.next();
				String localequilthisrowitemname = localequilbooleansthisrow.elementAt(0).toString();
				if (debug_statements) System.out.println("localequilbooleansthisrow itemname is " + localequilthisrowitemname);
				equilnamenotyetfound = true;
				while (equilbooleansrowiter.hasNext() & equilnamenotyetfound) {
					equilbooleansthisrow = equilbooleansrowiter.next();
					String equilbooleansthisrowitemname = equilbooleansthisrow.elementAt(0).toString();
					if (debug_statements) System.out.println("equilbooleansthisrow itemname is " + equilbooleansthisrowitemname);
					if (localequilthisrowitemname.equals(equilbooleansthisrowitemname)) {
						if (debug_statements) System.out.println("item names match found");
						localequilbooleansthisrow.removeElementAt(1);
						localequilbooleansthisrow.insertElementAt(equilbooleansthisrow.elementAt(1),1);
						equilnamenotyetfound = false;
					}
				}
			}
		}
		equilbooleans = localequilbooleans;
		setEquilibrationBooleanValues(equilbooleans);
		equilibration_equilibrate_table_subpanel = new TablePanelPreload(true,the_gui,this,equil_headings_stringarray,equilbooleans,230,160,equilibration_table_title);

		timecourse_sampletimes_table_subpanel = new TablePanelUserInput(the_gui,the_model,the_state,this,sampletimes_headings_stringarray,200,195,timecourse_table_title);
		usersampletimes = timecourse_sampletimes_table_subpanel.getNewSampleTimes();

		// DIMENSIONS

		Dimension size0 = new Dimension(80,25); 	// foo
		Dimension size1 = new Dimension(80,45); 	// textfields
		Dimension size2 = new Dimension(150,45);	// options panel - one option
		Dimension size3 = new Dimension(150,30);	// radio button
		Dimension size4 = new Dimension(300,200);	// table panels
		Dimension size5 = new Dimension(450,180);	// subpanels
		Dimension size6 = new Dimension(150,120);	// options panel - two options
		Dimension size7 = new Dimension(450,195);	// subpanels
		Dimension size8 = new Dimension(450,240);	// timecourse panel - previous
		Dimension size9 = new Dimension(150,70);	// two radio buttons
		Dimension size10 = new Dimension(300,240);	// timecourse panel - new
		Dimension size11 = new Dimension(150,240);	// writeoptions panel - new
		Dimension size12 = new Dimension(1000,300);

		// DIALOGS
        
        	config_dialog = new JDialog(ef, "Reaction Network Generation and Simulation Settings", is_modal);
        	config_dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        	config_dialog.addWindowListener(new SimCloser());
		config_dialog.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
	
		// FONTS

        	Font big_heading_font = new Font("Dialog", Font.BOLD, 16);
        	Font heading_font = new Font("Dialog", Font.BOLD, 12);
        	Font plain_font = new Font("Dialog", Font.PLAIN, 9);
		Font small_font = new Font("Dialog", Font.PLAIN, 8);

		// TEXTFIELDS

		// Network
		max_num_molecules_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		max_num_molecules_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		max_num_molecules_textfield.setEditable(true);
		max_num_molecules_textfield.setFocusable(true);
		max_num_molecules_textfield.setForeground(Color.black);
		max_num_ruleapplications_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		max_num_ruleapplications_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		max_num_ruleapplications_textfield.setEditable(true);
		max_num_ruleapplications_textfield.setFocusable(true);
		max_num_ruleapplications_textfield.setForeground(Color.black);
		max_num_molecules_textfield.setEnabled(network_generatenewvalue);
		max_num_ruleapplications_textfield.setEnabled(network_generatenewvalue);

		// Equilibration 
		time_between_equil_checks_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		time_between_equil_checks_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		time_between_equil_checks_textfield.setEditable(true);
		time_between_equil_checks_textfield.setFocusable(true);
		time_between_equil_checks_textfield.setForeground(Color.black);
		max_num_equil_checks_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		max_num_equil_checks_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		max_num_equil_checks_textfield.setEditable(true);
		max_num_equil_checks_textfield.setFocusable(true);
		max_num_equil_checks_textfield.setForeground(Color.black);
		time_between_equil_checks_textfield.setEnabled(equilibration_equilibratevalue);
		max_num_equil_checks_textfield.setEnabled(equilibration_equilibratevalue);

		// Simulation Method
		abs_tol_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		abs_tol_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		abs_tol_textfield.setEditable(true);
		abs_tol_textfield.setFocusable(true);
       		abs_tol_textfield.setForeground(Color.black);
		rel_tol_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		rel_tol_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		rel_tol_textfield.setEditable(true);
		rel_tol_textfield.setFocusable(true);
       		rel_tol_textfield.setForeground(Color.black);
		abs_tol_textfield.setEnabled(simulation_ODEvalue);
		rel_tol_textfield.setEnabled(simulation_ODEvalue);
	
		// Time Course
		n_steps_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		n_steps_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		n_steps_textfield.setEditable(true);
		n_steps_textfield.setFocusable(true);
		n_steps_textfield.setForeground(Color.black);
		t_end_textfield.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		t_end_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		t_end_textfield.setEditable(true);
		t_end_textfield.setFocusable(true);
		t_end_textfield.setForeground(Color.black);
		n_steps_textfield.setEnabled(timecourse_tendnstepsvalue);
		t_end_textfield.setEnabled(timecourse_tendnstepsvalue);

		// RADIO BUTTONS, RADIO GROUPS, BUTTONS

		// Network
		choosefromfilesButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		choosefromfilesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		choosefromfilesButton.setSelected(network_choosefromfilesvalue);
		choosefromfilesButton.addActionListener(new NetworkChooseFromFiles());
		generatenewButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		generatenewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		generatenewButton.setSelected(network_generatenewvalue);
		generatenewButton.addActionListener(new NetworkGenerateNew());
		ButtonGroup networkgeneration = new ButtonGroup();
		networkgeneration.add(choosefromfilesButton);
		networkgeneration.add(generatenewButton);

		// Equilibration
		performequilibrationButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		performequilibrationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		performequilibrationButton.setSelected(equilibration_equilibratevalue);
		performequilibrationButton.addActionListener(new EquilibrationEquilibrate());
		noequilibrationButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		noequilibrationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		noequilibrationButton.setSelected(equilibration_noequilibratevalue);
		noequilibrationButton.addActionListener(new EquilibrationNoEquilibrate());
		ButtonGroup runequilibration = new ButtonGroup();
		runequilibration.add(performequilibrationButton);
		runequilibration.add(noequilibrationButton);

		// Simulation Method
		ordinarydifferentialequationButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		ordinarydifferentialequationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		ordinarydifferentialequationButton.setSelected(simulation_ODEvalue);
		ordinarydifferentialequationButton.addActionListener(new SimulationODE());
		stochasticsimulationalgorithmButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		stochasticsimulationalgorithmButton.setSelected(simulation_SSAvalue);
		stochasticsimulationalgorithmButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		stochasticsimulationalgorithmButton.addActionListener(new SimulationSSA());
		ButtonGroup simmethodgroup = new ButtonGroup();
		simmethodgroup.add(ordinarydifferentialequationButton);
		simmethodgroup.add(stochasticsimulationalgorithmButton);

		// Time Course
		tendnstepsButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		tendnstepsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		tendnstepsButton.setSelected(timecourse_tendnstepsvalue);
		tendnstepsButton.addActionListener(new TimecourseTendnsteps());
		sampletimesButton.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		sampletimesButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		sampletimesButton.setSelected(timecourse_sampletimesvalue);
		sampletimesButton.addActionListener(new TimecourseSampletimes());
		ButtonGroup timecoursegroup = new ButtonGroup();
		timecoursegroup.add(tendnstepsButton);
		timecoursegroup.add(sampletimesButton);

		// Window Save/Cancel Operations
        	JButton cancel_button = new JButton("Cancel");
        	cancel_button.addActionListener(new SimCancel());
        	JButton save_button = new JButton("Save");
		save_button.addActionListener(new SimCloser());

		// CHECK BOXES

		// Salt
		JCheckBox fooBox = new JCheckBox("");
		fooBox.setVisible(false);
		fooBox.setEnabled(false);
		JCheckBox fooBox2 = new JCheckBox("                 ");
		fooBox2.setVisible(false);
		fooBox2.setEnabled(false);
		JPanel fooPanel = new JPanel();
		fooPanel.setMaximumSize(size0);
		fooPanel.setPreferredSize(size0);
		fooPanel.setMinimumSize(size0);
		fooPanel.add(fooBox);

		// Network
		isomorphismBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		isomorphismBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		isomorphismBox.setSelected(network_isomorphismvalue);
		isomorphismBox.setEnabled(network_generatenewvalue);
        	isomorphismBox.addActionListener(new NetworkIsomorphism());

                // pre-generate
                pregenerateBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		pregenerateBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		pregenerateBox.setSelected(network_pregeneratevalue);
        	pregenerateBox.addActionListener(new NetworkPreGenerate());
                
		// Simulation Method
		sparseBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		sparseBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		sparseBox.setSelected(simulation_sparsevalue);
		sparseBox.setEnabled(simulation_ODEvalue);
        	sparseBox.addActionListener(new SimulationSparse());
		readnetfileBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		readnetfileBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		readnetfileBox.setSelected(simulation_readnetfilevalue);
		readnetfileBox.setEnabled(true);
        	readnetfileBox.addActionListener(new SimulationReadNetFile());

		// Additional Options
		verboseBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		verboseBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		verboseBox.setSelected(options_verbosevalue);
		verboseBox.addActionListener(new OptionsVerbose());
		sbmlBox.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		sbmlBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		sbmlBox.setSelected(options_sbmlvalue);
		sbmlBox.addActionListener(new OptionsSBML());

		// PANELS

		// Network
		// panel not currently in use; however we may change layout later and need this
		JPanel network_radiochoices_subpanel = new JPanel();
		network_radiochoices_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_radiochoices_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_radiochoices_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_radiochoices_subpanel.setLayout(new BoxLayout(network_radiochoices_subpanel, BoxLayout.PAGE_AXIS));
		network_radiochoices_subpanel.setMaximumSize(size9);
		network_radiochoices_subpanel.setPreferredSize(size9);
		network_radiochoices_subpanel.setMinimumSize(size9);
		network_radiochoices_subpanel.add(choosefromfilesButton);
		network_radiochoices_subpanel.add(generatenewButton);

		// panel not currently in use; however we may change layout later and need this
		JPanel network_selectfromfiles_radio_subpanel = new JPanel();
		network_selectfromfiles_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_selectfromfiles_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_selectfromfiles_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_selectfromfiles_radio_subpanel.setLayout(new BoxLayout(network_selectfromfiles_radio_subpanel, BoxLayout.PAGE_AXIS));
		network_selectfromfiles_radio_subpanel.setMaximumSize(size3);
		network_selectfromfiles_radio_subpanel.setPreferredSize(size3);
		network_selectfromfiles_radio_subpanel.setMinimumSize(size3);

		// panel not currently in use; however we may change layout later and need this
		JPanel network_generatenew_radio_subpanel = new JPanel();
		network_generatenew_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_generatenew_radio_subpanel.setLayout(new BoxLayout(network_generatenew_radio_subpanel, BoxLayout.PAGE_AXIS));
		network_generatenew_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_generatenew_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_generatenew_radio_subpanel.setMaximumSize(size3);
		network_generatenew_radio_subpanel.setPreferredSize(size3);
		network_generatenew_radio_subpanel.setMinimumSize(size3);

		JPanel network_generatenew_data1_subpanel = new JPanel();
		network_generatenew_data1_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	network_generatenew_data1_subpanel.setLayout(new BoxLayout(network_generatenew_data1_subpanel,BoxLayout.PAGE_AXIS));
		network_generatenew_data1_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_generatenew_data1_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_generatenew_data1_subpanel.setMaximumSize(size1);
		network_generatenew_data1_subpanel.setPreferredSize(size1);
		network_generatenew_data1_subpanel.setMinimumSize(size1);
		// unhooked per Issue #125; may be reinstated at any time
		//network_generatenew_data1_subpanel.add(max_num_molecules_textfield);
		//network_generatenew_data1_subpanel.add(makeLabel("Max Molecules",plain_font));
		//network_generatenew_data1_subpanel.add(makeLabel("per Species",plain_font));

		JPanel network_generatenew_data2_subpanel = new JPanel();
		network_generatenew_data2_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	network_generatenew_data2_subpanel.setLayout(new BoxLayout(network_generatenew_data2_subpanel,BoxLayout.PAGE_AXIS));
		network_generatenew_data2_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_generatenew_data2_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_generatenew_data2_subpanel.setMaximumSize(size1);
		network_generatenew_data2_subpanel.setPreferredSize(size1);
		network_generatenew_data2_subpanel.setMinimumSize(size1);
		network_generatenew_data2_subpanel.add(max_num_ruleapplications_textfield);
		network_generatenew_data2_subpanel.add(makeLabel("Max Rule",plain_font)); 
		network_generatenew_data2_subpanel.add(makeLabel("Applications",plain_font)); 

		JPanel network_generatenew_options_subpanel = new JPanel();
		network_generatenew_options_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_generatenew_options_subpanel.setLayout(new BoxLayout(network_generatenew_options_subpanel, BoxLayout.PAGE_AXIS));
		network_generatenew_options_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_generatenew_options_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		network_generatenew_options_subpanel.setMaximumSize(size2);
		network_generatenew_options_subpanel.setPreferredSize(size2);
		network_generatenew_options_subpanel.setMinimumSize(size2);
		network_generatenew_options_subpanel.add(isomorphismBox);
                network_generatenew_options_subpanel.add(pregenerateBox);
                
		JPanel network_generatenew_data_subpanel = new JPanel();
		network_generatenew_data_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	network_generatenew_data_subpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		network_generatenew_data_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		// unhooked per Issue #125; may be reinstated at any time
		//network_generatenew_data_subpanel.add(network_generatenew_data1_subpanel,BorderLayout.WEST);
		network_generatenew_data_subpanel.add(network_generatenew_data2_subpanel,BorderLayout.CENTER);

		JPanel network_generatenew_nontable_subpanel = new JPanel();
		network_generatenew_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	network_generatenew_nontable_subpanel.setLayout(new BoxLayout(network_generatenew_nontable_subpanel, BoxLayout.PAGE_AXIS));
		network_generatenew_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//network_generatenew_nontable_subpanel.add(network_radiochoices_subpanel,BorderLayout.WEST); // disabled pending decision on read from file issue
		network_generatenew_nontable_subpanel.add(network_generatenew_data_subpanel,BorderLayout.WEST);
		network_generatenew_nontable_subpanel.add(network_generatenew_options_subpanel,BorderLayout.WEST);

		// panel not currently in use; however we may change layout later and need this
		JPanel network_selectfromfiles_panel = new JPanel();
		network_selectfromfiles_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_selectfromfiles_panel.setLayout(new BoxLayout(network_selectfromfiles_panel, BoxLayout.PAGE_AXIS));
		network_selectfromfiles_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_selectfromfiles_panel.setMaximumSize(size5);
		network_selectfromfiles_panel.setPreferredSize(size5);
		network_selectfromfiles_panel.setMinimumSize(size5);
		network_selectfromfiles_panel.setBorder(BorderFactory.createTitledBorder("Use Existing"));
		network_selectfromfiles_panel.add(network_selectfromfiles_radio_subpanel,BorderLayout.WEST);

		// panel not currently in use; however we may change layout later and need this
		JPanel network_generatenew_panel = new JPanel();
		network_generatenew_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		network_generatenew_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		network_generatenew_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_generatenew_panel.setMaximumSize(size5);
		network_generatenew_panel.setPreferredSize(size5);
		network_generatenew_panel.setMinimumSize(size5);
		network_generatenew_panel.setBorder(BorderFactory.createTitledBorder("New"));
		network_generatenew_panel.add(network_generatenew_nontable_subpanel,BorderLayout.WEST);
		network_generatenew_panel.add(network_generatenew_table_subpanel,BorderLayout.WEST);

        	JPanel network_panel = new JPanel();
		network_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	network_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		network_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		network_panel.setMaximumSize(size8);
		network_panel.setPreferredSize(size8);
		network_panel.setMinimumSize(size8);
		network_panel.setBorder(BorderFactory.createTitledBorder("Network Generation"));
		network_panel.add(network_generatenew_nontable_subpanel,BorderLayout.WEST);
		network_panel.add(network_generatenew_table_subpanel,BorderLayout.CENTER);

		// Equilibration
		JPanel equilibration_radiochoices_subpanel = new JPanel();
		equilibration_radiochoices_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_radiochoices_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_radiochoices_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_radiochoices_subpanel.setLayout(new BoxLayout(equilibration_radiochoices_subpanel, BoxLayout.PAGE_AXIS));
		equilibration_radiochoices_subpanel.setMaximumSize(size9);
		equilibration_radiochoices_subpanel.setPreferredSize(size9);
		equilibration_radiochoices_subpanel.setMinimumSize(size9);
		equilibration_radiochoices_subpanel.add(noequilibrationButton);
		equilibration_radiochoices_subpanel.add(performequilibrationButton);

		JPanel equilibration_equilibrate_radio_subpanel = new JPanel();
		equilibration_equilibrate_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_equilibrate_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_equilibrate_radio_subpanel.setLayout(new BoxLayout(equilibration_equilibrate_radio_subpanel, BoxLayout.PAGE_AXIS));
		equilibration_equilibrate_radio_subpanel.setMaximumSize(size3);
		equilibration_equilibrate_radio_subpanel.setPreferredSize(size3);
		equilibration_equilibrate_radio_subpanel.setMinimumSize(size3);

		JPanel equilibration_noequilibrate_radio_subpanel = new JPanel();
		equilibration_noequilibrate_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_noequilibrate_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_noequilibrate_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_noequilibrate_radio_subpanel.setLayout(new BoxLayout(equilibration_noequilibrate_radio_subpanel, BoxLayout.PAGE_AXIS));
		equilibration_noequilibrate_radio_subpanel.setMaximumSize(size3);
		equilibration_noequilibrate_radio_subpanel.setPreferredSize(size3);
		equilibration_noequilibrate_radio_subpanel.setMinimumSize(size3);

		JPanel equilibration_equilibrate_data1_subpanel = new JPanel();
		equilibration_equilibrate_data1_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	equilibration_equilibrate_data1_subpanel.setLayout(new BoxLayout(equilibration_equilibrate_data1_subpanel,BoxLayout.PAGE_AXIS));
		equilibration_equilibrate_data1_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_data1_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_equilibrate_data1_subpanel.setMaximumSize(size1);
		equilibration_equilibrate_data1_subpanel.setPreferredSize(size1);
		equilibration_equilibrate_data1_subpanel.setMinimumSize(size1);
		equilibration_equilibrate_data1_subpanel.add(time_between_equil_checks_textfield);
		equilibration_equilibrate_data1_subpanel.add(makeLabel("Max Time",plain_font));
		equilibration_equilibrate_data1_subpanel.add(makeLabel(" ",plain_font));

		JPanel equilibration_equilibrate_data2_subpanel = new JPanel();
		equilibration_equilibrate_data2_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	equilibration_equilibrate_data2_subpanel.setLayout(new BoxLayout(equilibration_equilibrate_data2_subpanel,BoxLayout.PAGE_AXIS));
		equilibration_equilibrate_data2_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_data2_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_equilibrate_data2_subpanel.setMaximumSize(size1);
		equilibration_equilibrate_data2_subpanel.setPreferredSize(size1);
		equilibration_equilibrate_data2_subpanel.setMinimumSize(size1);
		equilibration_equilibrate_data2_subpanel.add(max_num_equil_checks_textfield);
		equilibration_equilibrate_data2_subpanel.add(makeLabel("Num of Steps    ",plain_font));
		equilibration_equilibrate_data2_subpanel.add(makeLabel(" ",plain_font));

		JPanel equilibration_equilibrate_options_subpanel = new JPanel();
		equilibration_equilibrate_options_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_equilibrate_options_subpanel.setLayout(new BoxLayout(equilibration_equilibrate_options_subpanel, BoxLayout.PAGE_AXIS));
		equilibration_equilibrate_options_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_options_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		equilibration_equilibrate_options_subpanel.setMaximumSize(size2);
		equilibration_equilibrate_options_subpanel.setPreferredSize(size2);
		equilibration_equilibrate_options_subpanel.setMinimumSize(size2);
		equilibration_equilibrate_options_subpanel.add(fooBox2);

		JPanel equilibration_equilibrate_data_subpanel = new JPanel();
		equilibration_equilibrate_data_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	equilibration_equilibrate_data_subpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		equilibration_equilibrate_data_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_data_subpanel.add(equilibration_equilibrate_data1_subpanel,BorderLayout.WEST);
		equilibration_equilibrate_data_subpanel.add(equilibration_equilibrate_data2_subpanel,BorderLayout.CENTER);

		JPanel equilibration_equilibrate_nontable_subpanel = new JPanel();
		equilibration_equilibrate_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	equilibration_equilibrate_nontable_subpanel.setLayout(new BoxLayout(equilibration_equilibrate_nontable_subpanel, BoxLayout.PAGE_AXIS));
		equilibration_equilibrate_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_nontable_subpanel.add(equilibration_radiochoices_subpanel,BorderLayout.WEST);
		equilibration_equilibrate_nontable_subpanel.add(equilibration_equilibrate_data_subpanel,BorderLayout.WEST);
		equilibration_equilibrate_nontable_subpanel.add(equilibration_equilibrate_options_subpanel,BorderLayout.WEST);

		JPanel equilibration_equilibrate_panel = new JPanel();
		equilibration_equilibrate_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_equilibrate_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		equilibration_equilibrate_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_equilibrate_panel.setMaximumSize(size5);
		equilibration_equilibrate_panel.setPreferredSize(size5);
		equilibration_equilibrate_panel.setMinimumSize(size5);
		equilibration_equilibrate_panel.setBorder(BorderFactory.createTitledBorder("New"));
		equilibration_equilibrate_panel.add(equilibration_equilibrate_nontable_subpanel,BorderLayout.WEST);
		equilibration_equilibrate_panel.add(equilibration_equilibrate_table_subpanel,BorderLayout.WEST);

		JPanel equilibration_noequilibrate_panel = new JPanel();
		equilibration_noequilibrate_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		equilibration_noequilibrate_panel.setLayout(new BoxLayout(equilibration_noequilibrate_panel, BoxLayout.PAGE_AXIS));
		equilibration_noequilibrate_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_noequilibrate_panel.setMaximumSize(size5);
		equilibration_noequilibrate_panel.setPreferredSize(size5);
		equilibration_noequilibrate_panel.setMinimumSize(size5);
		equilibration_noequilibrate_panel.setBorder(BorderFactory.createTitledBorder("No Equilibration"));
		equilibration_noequilibrate_panel.add(equilibration_noequilibrate_radio_subpanel,BorderLayout.WEST);

        	JPanel equilibration_panel = new JPanel();
		equilibration_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	equilibration_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		equilibration_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		equilibration_panel.setMaximumSize(size8);
		equilibration_panel.setPreferredSize(size8);
		equilibration_panel.setMinimumSize(size8);
		equilibration_panel.setBorder(BorderFactory.createTitledBorder("Equilibration"));
		equilibration_panel.add(equilibration_equilibrate_nontable_subpanel,BorderLayout.WEST);
		equilibration_panel.add(equilibration_equilibrate_table_subpanel,BorderLayout.WEST);

		// Simulation Method
		JPanel simulation_radiochoices_subpanel = new JPanel();
		simulation_radiochoices_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_radiochoices_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_radiochoices_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		simulation_radiochoices_subpanel.setLayout(new BoxLayout(simulation_radiochoices_subpanel, BoxLayout.PAGE_AXIS));
		simulation_radiochoices_subpanel.setMaximumSize(size9);
		simulation_radiochoices_subpanel.setPreferredSize(size9);
		simulation_radiochoices_subpanel.setMinimumSize(size9);
		simulation_radiochoices_subpanel.add(stochasticsimulationalgorithmButton);
		simulation_radiochoices_subpanel.add(ordinarydifferentialequationButton);

		JPanel simulation_ode_radio_subpanel = new JPanel();
		simulation_ode_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_ode_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		simulation_ode_radio_subpanel.setLayout(new BoxLayout(simulation_ode_radio_subpanel, BoxLayout.PAGE_AXIS));
		simulation_ode_radio_subpanel.setMaximumSize(size3);
		simulation_ode_radio_subpanel.setPreferredSize(size3);
		simulation_ode_radio_subpanel.setMinimumSize(size3);

		JPanel simulation_ssa_radio_subpanel = new JPanel();
		simulation_ssa_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_ssa_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ssa_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		simulation_ssa_radio_subpanel.setLayout(new BoxLayout(simulation_ssa_radio_subpanel, BoxLayout.PAGE_AXIS));
		simulation_ssa_radio_subpanel.setMaximumSize(size3);
		simulation_ssa_radio_subpanel.setPreferredSize(size3);
		simulation_ssa_radio_subpanel.setMinimumSize(size3);

		JPanel simulation_ode_data1_subpanel = new JPanel();
		simulation_ode_data1_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_ode_data1_subpanel.setLayout(new BoxLayout(simulation_ode_data1_subpanel,BoxLayout.PAGE_AXIS));
		simulation_ode_data1_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_data1_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		simulation_ode_data1_subpanel.setMaximumSize(size1);
		simulation_ode_data1_subpanel.setPreferredSize(size1);
		simulation_ode_data1_subpanel.setMinimumSize(size1);
		simulation_ode_data1_subpanel.add(abs_tol_textfield);
		simulation_ode_data1_subpanel.add(makeLabel("Abs Integration",plain_font));
		simulation_ode_data1_subpanel.add(makeLabel("Error Tolerance ",plain_font));

		JPanel simulation_ode_data2_subpanel = new JPanel();
		simulation_ode_data2_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_ode_data2_subpanel.setLayout(new BoxLayout(simulation_ode_data2_subpanel,BoxLayout.PAGE_AXIS));
		simulation_ode_data2_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_data2_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		simulation_ode_data2_subpanel.setMaximumSize(size1);
		simulation_ode_data2_subpanel.setPreferredSize(size1);
		simulation_ode_data2_subpanel.setMinimumSize(size1);
		simulation_ode_data2_subpanel.add(rel_tol_textfield);
		simulation_ode_data2_subpanel.add(makeLabel("Rel Integration",plain_font));
		simulation_ode_data2_subpanel.add(makeLabel("Error Tolerance ",plain_font));

		JPanel simulation_ode_data_subpanel = new JPanel();
		simulation_ode_data_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_ode_data_subpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		simulation_ode_data_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_data_subpanel.add(simulation_ode_data1_subpanel,BorderLayout.WEST);
		simulation_ode_data_subpanel.add(simulation_ode_data2_subpanel,BorderLayout.CENTER);

		JPanel simulation_ode_options_subpanel = new JPanel();
		simulation_ode_options_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_ode_options_subpanel.setLayout(new BoxLayout(simulation_ode_options_subpanel, BoxLayout.PAGE_AXIS));
		simulation_ode_options_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_options_subpanel.add(sparseBox);
		simulation_ode_options_subpanel.add(readnetfileBox);

		JPanel simulation_ode_nontable_subpanel = new JPanel();
		simulation_ode_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_ode_nontable_subpanel.setLayout(new BoxLayout(simulation_ode_nontable_subpanel, BoxLayout.PAGE_AXIS));
		simulation_ode_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_nontable_subpanel.add(simulation_radiochoices_subpanel,BorderLayout.WEST);
		simulation_ode_nontable_subpanel.add(simulation_ode_data_subpanel,BorderLayout.WEST);
		simulation_ode_nontable_subpanel.add(simulation_ode_options_subpanel,BorderLayout.WEST);

		JPanel simulation_ssa_nontable_subpanel = new JPanel();
		simulation_ssa_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_ssa_nontable_subpanel.setLayout(new BoxLayout(simulation_ssa_nontable_subpanel, BoxLayout.PAGE_AXIS));
		simulation_ssa_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ssa_nontable_subpanel.add(simulation_ssa_radio_subpanel,BorderLayout.WEST);

		JPanel simulation_ode_panel = new JPanel();
		simulation_ode_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_ode_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		simulation_ode_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ode_panel.setMaximumSize(size5);
		simulation_ode_panel.setPreferredSize(size5);
		simulation_ode_panel.setMinimumSize(size5);
		simulation_ode_panel.setBorder(BorderFactory.createTitledBorder("ODE"));

		JPanel simulation_ssa_panel = new JPanel();
		simulation_ssa_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		simulation_ssa_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		simulation_ssa_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_ssa_panel.setMaximumSize(size5);
		simulation_ssa_panel.setPreferredSize(size5);
		simulation_ssa_panel.setMinimumSize(size5);
		simulation_ssa_panel.setBorder(BorderFactory.createTitledBorder("SSA"));
		simulation_ssa_panel.add(simulation_ssa_nontable_subpanel,BorderLayout.WEST);

        	JPanel simulation_panel = new JPanel();
		simulation_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	simulation_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		simulation_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		simulation_panel.setMaximumSize(size10);	// was size8 prior to inclusion of write options panel
		simulation_panel.setPreferredSize(size10);
		simulation_panel.setMinimumSize(size10);
		simulation_panel.setBorder(BorderFactory.createTitledBorder("Simulation Method"));
		simulation_panel.add(simulation_ode_nontable_subpanel,BorderLayout.WEST);

		// Time Course
		JPanel timecourse_radiochoices_subpanel = new JPanel();
		timecourse_radiochoices_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		timecourse_radiochoices_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_radiochoices_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		timecourse_radiochoices_subpanel.setLayout(new BoxLayout(timecourse_radiochoices_subpanel, BoxLayout.PAGE_AXIS));
		timecourse_radiochoices_subpanel.setMaximumSize(size9);
		timecourse_radiochoices_subpanel.setPreferredSize(size9);
		timecourse_radiochoices_subpanel.setMinimumSize(size9);
		timecourse_radiochoices_subpanel.add(tendnstepsButton);
		timecourse_radiochoices_subpanel.add(sampletimesButton);

		JPanel timecourse_tendnsteps_radio_subpanel = new JPanel();
		timecourse_tendnsteps_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		timecourse_tendnsteps_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_tendnsteps_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		timecourse_tendnsteps_radio_subpanel.setLayout(new BoxLayout(timecourse_tendnsteps_radio_subpanel, BoxLayout.PAGE_AXIS));
		timecourse_tendnsteps_radio_subpanel.setMaximumSize(size3);
		timecourse_tendnsteps_radio_subpanel.setPreferredSize(size3);
		timecourse_tendnsteps_radio_subpanel.setMinimumSize(size3);

		JPanel timecourse_sampletimes_radio_subpanel = new JPanel();
		timecourse_sampletimes_radio_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		timecourse_sampletimes_radio_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_sampletimes_radio_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		timecourse_sampletimes_radio_subpanel.setLayout(new BoxLayout(timecourse_sampletimes_radio_subpanel, BoxLayout.PAGE_AXIS));
		timecourse_sampletimes_radio_subpanel.setMaximumSize(size3);
		timecourse_sampletimes_radio_subpanel.setPreferredSize(size3);
		timecourse_sampletimes_radio_subpanel.setMinimumSize(size3);

		JPanel timecourse_tendnsteps_data1_subpanel = new JPanel();
		timecourse_tendnsteps_data1_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	timecourse_tendnsteps_data1_subpanel.setLayout(new BoxLayout(timecourse_tendnsteps_data1_subpanel,BoxLayout.PAGE_AXIS));
		timecourse_tendnsteps_data1_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_tendnsteps_data1_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		timecourse_tendnsteps_data1_subpanel.setMaximumSize(size1);
		timecourse_tendnsteps_data1_subpanel.setPreferredSize(size1);
		timecourse_tendnsteps_data1_subpanel.setMinimumSize(size1);
		timecourse_tendnsteps_data1_subpanel.add(t_end_textfield);
		timecourse_tendnsteps_data1_subpanel.add(makeLabel("Max Time        ",plain_font));
		timecourse_tendnsteps_data1_subpanel.add(makeLabel("                ",plain_font));

		JPanel timecourse_tendnsteps_data2_subpanel = new JPanel();
		timecourse_tendnsteps_data2_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	timecourse_tendnsteps_data2_subpanel.setLayout(new BoxLayout(timecourse_tendnsteps_data2_subpanel,BoxLayout.PAGE_AXIS));
		timecourse_tendnsteps_data2_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_tendnsteps_data2_subpanel.setAlignmentY(Component.TOP_ALIGNMENT);
		timecourse_tendnsteps_data2_subpanel.setMaximumSize(size1);
		timecourse_tendnsteps_data2_subpanel.setPreferredSize(size1);
		timecourse_tendnsteps_data2_subpanel.setMinimumSize(size1);
		timecourse_tendnsteps_data2_subpanel.add(n_steps_textfield);
		timecourse_tendnsteps_data2_subpanel.add(makeLabel("Num of Steps   ",plain_font));
		timecourse_tendnsteps_data2_subpanel.add(makeLabel("                ",plain_font));

		JPanel timecourse_tendnsteps_data_subpanel = new JPanel();
		timecourse_tendnsteps_data_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	timecourse_tendnsteps_data_subpanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		timecourse_tendnsteps_data_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_tendnsteps_data_subpanel.add(timecourse_tendnsteps_data1_subpanel,BorderLayout.WEST);
		timecourse_tendnsteps_data_subpanel.add(timecourse_tendnsteps_data2_subpanel,BorderLayout.CENTER);

		JPanel timecourse_tendnsteps_nontable_subpanel = new JPanel();
		timecourse_tendnsteps_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	timecourse_tendnsteps_nontable_subpanel.setLayout(new BoxLayout(timecourse_tendnsteps_nontable_subpanel, BoxLayout.PAGE_AXIS));
		timecourse_tendnsteps_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_tendnsteps_nontable_subpanel.add(timecourse_radiochoices_subpanel,BorderLayout.WEST);
		timecourse_tendnsteps_nontable_subpanel.add(timecourse_tendnsteps_data_subpanel,BorderLayout.WEST);

        	JPanel timecourse_panel = new JPanel();
		timecourse_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	timecourse_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		timecourse_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		timecourse_panel.setMaximumSize(size8);	
		timecourse_panel.setPreferredSize(size8);
		timecourse_panel.setMinimumSize(size8);
		timecourse_panel.setBorder(BorderFactory.createTitledBorder("Output Capture"));
		timecourse_panel.add(timecourse_tendnsteps_nontable_subpanel,BorderLayout.WEST);
		timecourse_panel.add(timecourse_sampletimes_table_subpanel,BorderLayout.WEST);

		JPanel writeoptions_nontable_subpanel = new JPanel();
		writeoptions_nontable_subpanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		writeoptions_nontable_subpanel.setLayout(new BoxLayout(writeoptions_nontable_subpanel, BoxLayout.PAGE_AXIS));
		writeoptions_nontable_subpanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		writeoptions_nontable_subpanel.add(sbmlBox);
		// unhooked per issue #128; may be reinstated at any time
		//writeoptions_nontable_subpanel.add(verboseBox);

		JPanel writeoptions_panel = new JPanel();
		writeoptions_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		writeoptions_panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		writeoptions_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
		writeoptions_panel.setMaximumSize(size11);
		writeoptions_panel.setPreferredSize(size11);
		writeoptions_panel.setMinimumSize(size11);
		writeoptions_panel.setBorder(BorderFactory.createTitledBorder("Options"));
		writeoptions_panel.add(writeoptions_nontable_subpanel,BorderLayout.WEST);

		// Window Save/Cancel Operations
        	JPanel button_panel = new JPanel();
		button_panel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		button_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        	button_panel.add(save_button);
        	button_panel.add(cancel_button);

		// Arrange all the panels
		JPanel mounttoppanels = new JPanel();
		mounttoppanels.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	mounttoppanels.setLayout(new FlowLayout(FlowLayout.LEFT));
		mounttoppanels.add(network_panel,BorderLayout.WEST);
		mounttoppanels.add(equilibration_panel,BorderLayout.EAST);

		JPanel mountbottompanels = new JPanel();
		mountbottompanels.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        	mountbottompanels.setLayout(new FlowLayout(FlowLayout.LEFT));
		mountbottompanels.add(timecourse_panel,BorderLayout.EAST);
		mountbottompanels.add(simulation_panel,BorderLayout.CENTER);
		mountbottompanels.add(writeoptions_panel,BorderLayout.WEST);

		JPanel mountpanels = new JPanel();
		mountpanels.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		mountpanels.setLayout(new BoxLayout(mountpanels, BoxLayout.PAGE_AXIS));
		mountpanels.add(mounttoppanels,BorderLayout.WEST);
		mountpanels.add(mountbottompanels,BorderLayout.EAST);
	
		JPanel allpanels = new JPanel();
		allpanels.setLayout(new BorderLayout());
       		allpanels.add(mountpanels, BorderLayout.CENTER);
       		allpanels.add(button_panel, BorderLayout.SOUTH);

		// CONTAINERS
        
        	Container content = config_dialog.getContentPane();
		content.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		content.setLayout(new FlowLayout(FlowLayout.LEFT));
		content.add(allpanels,BorderLayout.WEST);

		// the pack and setVisible must be called after all the add methods
		// in order to interact with the event queue properly
       		config_dialog.pack();
      	  	config_dialog.setVisible(true);

    	}

	// ACCESSOR FUNCTIONS TO CAPTURE USER CHOICES

	public String getNetworkMaxnummolecules() {
		return max_num_molecules_textfield.getText();
	}

	public String getNetworkMaxnumruleapplications() {
		return max_num_ruleapplications_textfield.getText();
	}

	public String getEquilibrationTimebetweenequilchecks() {
		return time_between_equil_checks_textfield.getText();
	}

	public String getEquilibrationMaxnumequilchecks() {
		return max_num_equil_checks_textfield.getText();
	}

	public String getSimulationATOL() {
		return abs_tol_textfield.getText();
	}

	public String getSimulationRTOL() {
		return rel_tol_textfield.getText();
	}

	public String getTimecourseNsteps() {
		return n_steps_textfield.getText();
	}

	public String getTimecourseTend() {
		return t_end_textfield.getText();
	}

	public boolean getNetworkChooseFromFiles() {
		return network_choosefromfilesvalue;
	}

	public boolean getNetworkGenerateNew() {
		return network_generatenewvalue;
	}

	public boolean getEquilibrationEquilibrate() {
		return equilibration_equilibratevalue;
	}

	public boolean getEquilibrationNoEquilibrate() {
		return equilibration_noequilibratevalue;
	}
	
	public boolean getSimulationODE() {
		return simulation_ODEvalue;
	}

	public boolean getSimulationSSA() {
		return simulation_SSAvalue;
	}
	
	public boolean getTimecourseTendnsteps() {
		return timecourse_tendnstepsvalue;
	}

	public boolean getTimecourseSampletimes() {
		return timecourse_sampletimesvalue;
	}

	public boolean getNetworkIsomorphism() {
		return network_isomorphismvalue;
	}

        public boolean getPreGenerate() {
		return network_pregeneratevalue;
	}
        
	public boolean getSimulationSparse() {
		return simulation_sparsevalue;
	}

	public boolean getSimulationReadNetFile() {
		return simulation_readnetfilevalue;
	}

	public boolean getOptionsVerbose() {
		return options_verbosevalue;
	}

	public boolean getOptionsSBML() {
		return options_sbmlvalue;
	}

	public Vector<String> getTimecourseSampletimesvalues() {
		if (usersampletimes == null) {
			return new Vector<String>();
		}

		Iterator <String> usersampletimesiter = usersampletimes.iterator();
                while (usersampletimesiter.hasNext()) {
			if (debug_statements) System.out.println("getTimecourseSampletimesvalues going to file: " + usersampletimesiter.next());
		}
		return usersampletimes;
	}

	public Vector<Vector<Object>> getNetworkStoichLimitValues() {
		if (maxstoichlimits == null) {
			return new Vector<Vector<Object>>();
		}

		Iterator<Vector<Object>> maxstoichrowiter = maxstoichlimits.iterator();
		Vector<Object> maxstoichthisrow;
		Iterator<Object> maxstoichcoliter;
                while (maxstoichrowiter.hasNext()) {
			maxstoichthisrow = maxstoichrowiter.next();
			maxstoichcoliter = maxstoichthisrow.iterator();
			while (maxstoichcoliter.hasNext()) {
				Object current = maxstoichcoliter.next();
				String currentstring = current.toString();
				if (debug_statements) System.out.println("getNetworkMaxStoichLimitValues going to file: " + currentstring);
			}
		}
		return maxstoichlimits;
	}

	public Vector<Vector<Object>> getEquilibrationBooleanValues() {
		if (equilbooleans == null) {
			return new Vector<Vector<Object>>();
		}

		Iterator<Vector<Object>> equilbooleansrowiter = equilbooleans.iterator();
		Vector<Object> equilbooleansthisrow;
		Iterator<Object> equilbooleanscoliter;
                while (equilbooleansrowiter.hasNext()) {
			equilbooleansthisrow = equilbooleansrowiter.next();
			equilbooleanscoliter = equilbooleansthisrow.iterator();
			while (equilbooleanscoliter.hasNext()) {
				Object current = equilbooleanscoliter.next();
				String currentstring = current.toString();
				if (debug_statements) System.out.println("getEquilibrationBooleanValues going to file: " + currentstring);
			}
		}
		return equilbooleans;
	}

	// FUNCTIONS TO INSTANTIATE USER CHOICES

	public boolean setNetworkMaxnummolecules(String maxnummoleculesarg) {
		max_num_molecules_textfield = new JTextField(maxnummoleculesarg,4);
		return true;
	}

	public boolean setNetworkMaxnumruleapplications(String maxnumruleapplicationsarg) {
		max_num_ruleapplications_textfield = new JTextField(maxnumruleapplicationsarg,4);
		return true;
	}

	public boolean setEquilibrationTimebetweenequilchecks(String timebetweenequilibrationchecksarg) {
		time_between_equil_checks_textfield= new JTextField(timebetweenequilibrationchecksarg,4);
		return true;
	}

	public boolean setEquilibrationMaxnumequilchecks(String maxnumberequilibrationchecksarg) {
		max_num_equil_checks_textfield = new JTextField(maxnumberequilibrationchecksarg,4);
		return true;
	}

	public boolean setSimulationATOL(String absintegerrorarg) {
    		abs_tol_textfield = new JTextField(absintegerrorarg,4);
		return true;
	}

	public boolean setSimulationRTOL(String relintegerrorarg) {
    		rel_tol_textfield = new JTextField(relintegerrorarg,4); 
		return true;
	}

	public boolean setTimecourseNsteps(String nstepsarg) {
    		n_steps_textfield = new JTextField(nstepsarg,4);   
		return true;
	}

	public boolean setTimecourseTend(String tendarg) {
    		t_end_textfield = new JTextField(tendarg,4); 
		return true;
	}

	public boolean setNetworkChoosefromfiles(boolean choosefromfilesarg) {
		choosefromfilesButton.setSelected(choosefromfilesarg);
		network_choosefromfilesvalue = choosefromfilesarg;
		return true;
	}

	public boolean setNetworkGenerateNew(boolean generatenewarg) {
		generatenewButton.setSelected(generatenewarg);
		isomorphismBox.setEnabled(true);
		network_generatenewvalue = generatenewarg;
		return true;
	}

	public boolean setEquilibrationEquilibrate(boolean runequilibrationarg) {
		performequilibrationButton.setSelected(runequilibrationarg);
		equilibration_equilibratevalue = runequilibrationarg;
		return true;
	}

	public boolean setEquilibrationNoEquilibrate(boolean noequilibrationarg) {
		noequilibrationButton.setSelected(noequilibrationarg);
		equilibration_noequilibratevalue = noequilibrationarg;
		return true;
	}
	
	public boolean setSimulationODE(boolean ODEarg) {
		ordinarydifferentialequationButton.setSelected(ODEarg);
		simulation_ODEvalue = ODEarg;
		return true;
	}

	public boolean setSimulationSSA(boolean SSAarg) {
		stochasticsimulationalgorithmButton.setSelected(SSAarg);
		simulation_SSAvalue = SSAarg;
		return true;
	}
	
	public boolean setTimecourseTendnsteps(boolean tendnstepsarg) {
		tendnstepsButton.setSelected(tendnstepsarg);
		timecourse_tendnstepsvalue = tendnstepsarg;
		return true;
	}

	public boolean setTimecourseSampletimes(boolean sampletimesarg) {
		sampletimesButton.setSelected(sampletimesarg);
		timecourse_sampletimesvalue = sampletimesarg;
		return true;
	}

	public boolean setNetworkIsomorphism(boolean isomorphismarg) {
		isomorphismBox.setSelected(isomorphismarg);
		network_isomorphismvalue = isomorphismarg;
		return true;
	}
        
        public boolean setNetworkPreGenerate(boolean pregeneratearg) {
		pregenerateBox.setSelected(pregeneratearg);
		network_pregeneratevalue = pregeneratearg;
		return true;
	}

	public boolean setSimulationSparse(boolean sparsearg) {
		sparseBox.setSelected(sparsearg);
		simulation_sparsevalue = sparsearg;
		return true;
	}

	public boolean setSimulationReadNetFile(boolean readnetfilearg) {
		readnetfileBox.setSelected(readnetfilearg);
		simulation_readnetfilevalue = readnetfilearg;
		return true;
	}

	public boolean setOptionsVerbose(boolean verbosearg) {
		verboseBox.setSelected(verbosearg);
		options_verbosevalue = verbosearg;
		return true;
	}

	public boolean setOptionsSBML(boolean sbmlarg) {
		sbmlBox.setSelected(sbmlarg);
		options_sbmlvalue = sbmlarg;
		return true;
	}

	public boolean setTimecourseSampletimesvalues(Vector<String> stvalues) {
		usersampletimes = stvalues;
		Iterator <String> usersampletimesiter = usersampletimes.iterator();
		while (usersampletimesiter.hasNext()) {
			if (debug_statements) System.out.println("setTimecourseSampletimesvalues restored from file: " + usersampletimesiter.next());
		}
		return true;
	}

	public boolean setNetworkMaxStoichLimitValues(Vector<Vector<Object>> maxstvalues) {
		maxstoichlimits = maxstvalues;
		Iterator<Vector<Object>> maxstoichrowiter = maxstoichlimits.iterator();
		Vector<Object> maxstoichthisrow;
		Iterator<Object> maxstoichcoliter;
                while (maxstoichrowiter.hasNext()) {
			maxstoichthisrow = maxstoichrowiter.next();
			maxstoichcoliter = maxstoichthisrow.iterator();
			while (maxstoichcoliter.hasNext()) {
				Object current = maxstoichcoliter.next();
				String currentstring = current.toString();
				if (debug_statements) System.out.println("setNetworkMaxStoichLimitValues restored from file: " + currentstring);
			}
		}
		return true;
	}

	public boolean setEquilibrationBooleanValues(Vector<Vector<Object>> equilvalues) {
		equilbooleans = equilvalues;
		return true;
	}

    	public File getFileFromUser() {
        	File file = null;
    
        	JFileChooser fc = new JFileChooser();
        	int returnVal = fc.showDialog( config_dialog,"OK");
                        
        	//Process the results.
       		if (returnVal == JFileChooser.APPROVE_OPTION) {
            		file =  fc.getSelectedFile();
        	}
        
        	return file;
    	}
    
    	public void writeValues() {
        }
    
    	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
	    	stream.defaultReadObject();
    	}
    
    	private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        	stream.defaultWriteObject();
    	}
    
    	public String getEngineArguments() {
        	String args = new String();
        	return args;
    	}
    
    	public JLabel makeLabel(String text, Font font) {
       	 	JLabel label = new JLabel(text,JLabel.LEFT);
		label.setAlignmentX(Component.LEFT_ALIGNMENT);
       	 	label.setFont(font);
        	return label;
    	}
    
    	public void initialize() {
        	if (debug_statements) System.out.println("Initializing Simulation Configuration");
    }
}
