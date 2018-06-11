/*
 * Forward and Reverse.java
 *
 * Created on September 19, 2005, 11:20 PM
 */

import java.net.*; //For URL image loading from Jar files
import javax.swing.*; // For graphical interface tool
import java.awt.*; // For graphical windowing tools
import java.awt.event.*;

import java.util.*; // For vectors

import java.beans.*;
import java.io.Serializable;
import java.io.*;

/**
 * @author matthew
 */
public class ForwardAndReverse extends Forward implements Serializable {
    
    private class ParameterChooserListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e) 
        {
            JComboBox cb = (JComboBox)e.getSource();
            String key = (String)cb.getSelectedItem();
            
            
            String value = model_parameters.getValue( key );
            
            // If the key does not exist then do not replace the value
            if ( value != null )
            {
                rate_textfield.setText( value );
            }
        }
    }
    
    private class RateDialogDone extends WindowAdapter implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            handleClose();
        }
    
        public void windowClosing(WindowEvent e)
        {
            handleClose();
	}
    
        private void handleClose()
        {
        
            String key = (String)parameter_chooser.getSelectedItem();
            String value = rate_textfield.getText();
            
            if ( key.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate must have a name\n");
                        return;
            }
            
            if ( value.length() < 1 )
            {
                getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate field was left blank\n");
                        return;
            }
            
            Float rate;
            try {
                    rate = new Float( value );
                    
                    if ( rate.floatValue() < 0.0 ) 
                    {
                        getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate must be a positive number;\n" +
                        value + " is not in that range.");
                        return;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate",value + " is not a valid number" );
                    return;
                }
            
            if ( !model_parameters.setValue( key, value ) ) // false if key not found
            {
                model_parameters.addParameter( key, value );
            }
            
            setReverseRateParameter( model_parameters.getParameter(key) ); //rate_value.floatValue() );
            
        rate_dialog.setVisible( false );
        rate_dialog.dispose();
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
  private class RateDialogCancel extends WindowAdapter implements ActionListener 
    {
        public void actionPerformed(ActionEvent event) 
        {
            handleClose();
        }
    
        public void windowClosing(WindowEvent e)
        {
            handleClose();
	}
    
        private void handleClose()
        {
            
        rate_dialog.setVisible( false );
        rate_dialog.dispose();
        
        getContainingPanel().getTheGUI().getEditorPanel().repaint();
    }
  }
    
    
    private float reverse_rate = 0;
    private String reverse_rate_name = "";
    private Parameter reverse_rate_parameter = new Parameter("", "");
   
    private static final long serialVersionUID = 1;
    
    transient protected JComboBox parameter_chooser;
    transient protected Vector<Parameter> parameters;
    transient protected JTextField rate_textfield;
    transient protected JDialog rate_dialog;
    transient protected ModelParameters model_parameters;
    
    
    ForwardAndReverse( int x, int y,  WidgetPanel containing_panel ) 
    {
        //this.label = "forward_and_reverse";
		this.x = x;
		this.y = y;
		this.containing_panel = containing_panel;

		image_url = "images/forward_and_reverse_op.png";
                image = loadImage( image_url );
                setLabel(" ");
                
       }
     
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Set Reverse Rate") ) 
        {
            setReverseRateFromUser();
        }
        
        super.actionPerformed(action);
    }
    
    
    
    public void setReverseRate(String r) 
    {
        reverse_rate_parameter.setValue( r );
    }
    
    public String getReverseRate() 
    {
        return reverse_rate_parameter.getValue();
    }
    
    public void setReverseRateName(String r) 
    {
        reverse_rate_parameter.setKey( r );
    }
 
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        super.display( c, g2d );
        
        g2d.setColor(Color.black);
     
        if ( getReverseRateParameter() != null )
        g2d.drawString( getReverseRateParameter().getKey(), x+2, y+getHeight()+7 );
     
    }
    
    public void displayPopupMenu(int x, int y)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem srr = new JMenuItem("Set Reverse Rate");
        srr.addActionListener( this );
        popup.add( srr ); 
        super.displayPopupMenu(x, y);
    }

    public void setReverseRateFromUser() 
    {
        setReverseRateFromUser( true );
    }
    
    
    public void setReverseRateFromUser( boolean allow_cancel ) 
    {
        JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            rate_dialog = new JDialog( owner, true );
            rate_dialog.setTitle( "Reverse Reaction Rate" );
            rate_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
            rate_dialog.addWindowListener( new RateDialogCancel() );
            
            Container content = rate_dialog.getContentPane();
            
            JPanel button_panel = new JPanel();
            JPanel name_panel = new JPanel();
            JPanel type_panel = new JPanel();
            
            rate_textfield = new JTextField(10);
            model_parameters = getContainingPanel().getTheGUI().getModelParameters();
            Vector<Parameter> parameters = model_parameters.getParameters();
            
            Vector<String> parameter_names = model_parameters.getParameterNames();
            
            parameter_chooser = new JComboBox( parameter_names );
            parameter_chooser.setEditable( true );
            parameter_chooser.addActionListener( new ParameterChooserListener() );
            
            if ( !getReverseRateName().equals("") )
            {
                String default_selection = getReverseRateParameter().getKey();
                parameter_chooser.setSelectedItem( default_selection ); 
            }
            
            //if ( getReverseRateParameter() != null )
            //{
            //    String default_selection = getReverseRateParameter().getKey();
            //    parameter_chooser.setSelectedItem( default_selection ); 
            //}
            
            JButton done = new JButton("Done");
            JButton cancel = new JButton("Cancel");
            done.addActionListener( new RateDialogDone() );
            cancel.addActionListener( new RateDialogCancel() );
            
            name_panel.add( new JLabel("Parameter Name: "), BorderLayout.WEST );
            
            name_panel.add( parameter_chooser, BorderLayout.EAST );
            
            type_panel.add( new JLabel("Rate: "), BorderLayout.WEST );
            type_panel.add( rate_textfield, BorderLayout.EAST );
            
            if ( allow_cancel )
            {
                button_panel.add( done, BorderLayout.WEST );
                button_panel.add( cancel, BorderLayout.EAST );
            }
            else
            {
                button_panel.add( done, BorderLayout.CENTER );
            }
             
            content.add( name_panel, BorderLayout.NORTH );
            content.add( type_panel, BorderLayout.CENTER );
            content.add( button_panel, BorderLayout.SOUTH );
            
            
            rate_dialog.pack();
            
            rate_dialog.setLocation( 200, 100 );  
            rate_dialog.setSize( 300, 200 );  
            
            rate_dialog.setVisible(true);
        
        
        /*
            Float rate_value = null;
            boolean rate_set = false;
            
            ModelParameters params = getContainingPanel().getTheGUI().getModelParameters();
            params.displayDialog();
            Parameter param = params.getSelectedParameter();
            if ( param == null )
            {
                getContainingPanel().displayError("Error Setting Rate", "No parameter was selected from the parameter table.");
                return;
            }
            
          */
        
            /*
            while ( rate_set == false ) {
                String rate = (String)getContainingPanel().displayInputQuestion( "Reaction Rate", "Enter the forward reaction rate.");
                
                if ( rate == null ) {
                    return;
                }
                
                try {
                    rate_value = new Float( rate );
                    rate_set = true;
                    
                    if ( rate_value.floatValue() < 0.0 ) {
                        getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate must be positive;\n" +
                        rate + " is not in that range.");
                        rate_set = false;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate",rate + " is not a valid number" );
                    rate_set = false;
                }
            }
            */

            
            getContainingPanel().repaint();
    }
    
   /* 
public void setReverseRateFromUser() 
    {
        Float rate_value = null;
            boolean rate_set = false;
            
            ModelParameters params = getContainingPanel().getTheGUI().getModelParameters();
            params.displayDialog();
            Parameter param = params.getSelectedParameter();
            if ( param == null )
            {
                getContainingPanel().displayError("Error Setting Rate", "No parameter was selected from the parameter table.");
                return;
            }
            
            
            /*
            while ( rate_set == false ) {
                String rate = (String)getContainingPanel().displayInputQuestion( "Reaction Rate", "Enter the reverse reaction rate.");
                
                if ( rate == null ) {
                    return;
                }
                
                try {
                    rate_value = new Float( rate );
                    rate_set = true;
                    
                    if ( rate_value.floatValue() < 0.0 ) {
                        getContainingPanel().displayError("Error Setting the Reaction Rate",
                        "The reaction rate must be positive;\n" +
                        rate + " is not in that range.");
                        rate_set = false;
                    }
                    
                }
                catch( NumberFormatException exp ) {
                    getContainingPanel().displayError( "Error Setting the Reaction Rate",rate + " is not a valid number" );
                    rate_set = false;
                }
            }
            */

            //setReverseRate( rate_value.floatValue() );
            //setReverseRateParameter(param);
            //getContainingPanel().repaint();
    //}

    public void setReverseRateParameter(Parameter p) 
    {
        reverse_rate_parameter = p; 
    }
 
    public String getReverseRateName() 
    {
        return reverse_rate_parameter.getKey();
    }
    
    public Parameter getReverseRateParameter() 
    {
        return reverse_rate_parameter;
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
    
        // Convert legacy rate to rate parameter
        if ( reverse_rate_parameter == null )
        reverse_rate_parameter = new Parameter( "k"+getID()+"r", new Float( reverse_rate ).toString() );
    }
}
