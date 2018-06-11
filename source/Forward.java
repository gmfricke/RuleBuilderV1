/*
 * Forward.java
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
public class Forward extends Operator implements Serializable {
    
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
            
            setForwardRateParameter( model_parameters.getParameter(key) ); //rate_value.floatValue() );
            
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
    
    
   
    private float forward_rate = 0;
    private String forward_rate_name = "";
    private Parameter forward_rate_parameter = new Parameter("","");
 
   
    private static final long serialVersionUID = 1;
    
    transient protected JComboBox parameter_chooser;
    transient protected Vector<Parameter> parameters;
    transient protected JTextField rate_textfield;
    transient protected JDialog rate_dialog;
    transient protected ModelParameters model_parameters;
    
    // For subclass
    public Forward()
    {}
    
    Forward( int x, int y, WidgetPanel containing_panel ) 
    {
        
                //this.label = "forward";
		this.x = x;
		this.y = y;
		this.containing_panel = containing_panel;
                
                
		image_url = "images/forward_op.png";
                image = loadImage( image_url );
                setLabel(" ");
		   
        }
    
     
    public void actionPerformed( ActionEvent action )
    {  
        if ( action.getActionCommand().equals("Set Forward Rate") ) 
        {
            setForwardRateFromUser();
        }
    }
    
    public void setForwardRate(String r) 
    {
        forward_rate_parameter.setValue( r );
    }
   
    public void setForwardRateName(String r) 
    {
        forward_rate_parameter.setKey( r ); 
    }
   
    public void setForwardRateParameter(Parameter p) 
    {
        forward_rate_parameter = p; 
    }
    
    public String getForwardRate() 
    {
        if ( forward_rate_parameter == null ) forward_rate_parameter = new Parameter("","");
        return forward_rate_parameter.getValue();
    }
    
    public String getForwardRateName() 
    {
        if ( forward_rate_parameter == null ) return null;
        return forward_rate_parameter.getKey();
    }
    
    public Parameter getForwardRateParameter() 
    {
        return forward_rate_parameter;
    }
    
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        super.display( c, g2d );
        
        g2d.setColor(Color.black);
        
        if ( getForwardRateParameter() != null )
        g2d.drawString( getForwardRateParameter().getKey(), x+2, y+3 );
    }
    

    public void displayPopupMenu(int x, int y)
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JMenuItem sfr = new JMenuItem("Set Forward Rate");
        sfr.addActionListener( this );
        popup.insert( sfr, 0 );
        //popup.add( sfr ); 
        super.displayPopupMenu(x, y);
        getContainingPanel().getTheGUI().refreshAll();
    }
 
    public void setForwardRateFromUser() 
    {
        setForwardRateFromUser( true );
    }
    
    public void setForwardRateFromUser( boolean allow_cancel ) 
    {
        JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
            rate_dialog = new JDialog( owner, true );
            rate_dialog.setTitle( "Forward Reaction Rate" );
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
            
            if ( !getForwardRateName().equals("") )
            {
                String default_selection = getForwardRateParameter().getKey();
                parameter_chooser.setSelectedItem( default_selection ); 
            }
            
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
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
    
        // Convert legacy rate to rate parameter
        if ( forward_rate_parameter == null )
        forward_rate_parameter = new Parameter( "k"+getID()+"f", new Float( forward_rate ).toString() );
        
        image = loadImage( image_url );
    }

    public void relinkParameter() 
    {
          String fname = getForwardRateName();
          if ( fname == null ) return;
          Parameter fparam = getContainingPanel().getTheGUI().getModelParameters().getParameter(fname);
          setForwardRateParameter( fparam );
    }
}
