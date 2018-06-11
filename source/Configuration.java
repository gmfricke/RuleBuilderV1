/*
 * Configuration.java
 *
 * Created on June 2, 2005, 1:58 PM by Matthew Fricke
 */

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools

import java.beans.*;
import java.io.Serializable;

import java.io.*; // For file manipulation and object writing
import javax.swing.filechooser.*; // for GUI file save and open

import java.util.*; // For vectors

/**
 * @author matthew
 */
public class Configuration extends Object implements Serializable 
{
    
    //action handling classes
    private class ConfigCancel implements ActionListener
    {
         public void actionPerformed(ActionEvent event) 
        {
            handleCancel();
        }
         
         private void handleCancel()
         {
             if ( !the_gui.getEditorPanel().displayQuestion("Cancel Changes","Are you sure you want to cancel changes to settings?") )
             {
                 return;
             }
             else
             {
                config_dialog.setVisible( false );
                config_dialog.dispose();
             }
         }
   
    }
   
    
    private class ConfigCloser extends WindowAdapter implements ActionListener
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
        String engine_path = engine_file_text.getText();
        String bngl_path = bngl_output_file_text.getText();
        
        File engine_file = null;
        try
        {
            engine_file = new File( engine_path );
        
        }
        catch( NullPointerException npe )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "No such path exisits.");
            return;
        }
        
        File bngl_file = null;
        try
        {
            bngl_file = new File( bngl_path );
        }
        catch( NullPointerException npe )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "Invalid path.");
            return;
        }
            
        if ( engine_path.length() == 0 )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "No path to the BNGL Engine was specified.");
            return;
        }
            
        if ( bngl_path.length() == 0 )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "No path was specified for the BNGL Output File.");
            return;
        }
        
        // Moronic way to set the parent directory (File has the information
        //about the absolute path but doesnt use it except through the constructor
        engine_path = engine_file.getAbsolutePath();
        bngl_path = bngl_file.getAbsolutePath();
        
        //append .bngl if not already the extension
        if (!bngl_path.matches(".*\\.bngl$"))
        {
           bngl_path += ".bngl";
       }
        
        bngl_file = new File( bngl_path );
        
        try
        {
            bngl_file.delete();
            bngl_file.createNewFile();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
            
        if ( bngl_file.getParent() == null )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "The path " + bngl_file.getAbsolutePath()
            + "\n is a relative path. Please enter an absolute path. Output path changes have not been saved.");
            return;
        }
        else if ( !engine_file.canRead() )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "Cannot read " + engine_file.getAbsolutePath()
            + ". Check the specified path is valid.");
            return;  
        }
        else if ( !bngl_file.canWrite() )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "Cannot write to " + bngl_file.getAbsolutePath()
            + ". Check the specified path is valid.");
            return;  
        }
        else if ( bngl_file.isDirectory() )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "Cannot write to " + bngl_file.getAbsolutePath()
            + "\nsince it is a directory not a file. Output path changes have not been saved.");
            return;  
        }
        // this should insure that if the file doesnt exist the error wont be triggered
        // since the canWrite() method is basically an "anding" of exists and "can write"
        else if ( !bngl_file.canWrite() && bngl_file.exists() )
        {
            the_gui.getEditorPanel().displayError( "Error in BNGL Output Path", "Cannot write to " + bngl_file.getAbsolutePath()
            + "\nYou do not appear to have permission to write to that file. BNGL Output path changes have not been saved.");
            return;    
        }
        else
        {
            setBNGLOutputPath( bngl_file.getAbsolutePath() );
            setBNGLOutputFile( bngl_file );
        }
        
       
        
        if ( engine_file.getParent() == null )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "The path " + engine_file.getAbsolutePath()
            + "\n is a relative path. Please enter an absolute path. Output path changes have not been saved.");
            return;
        }
        else if ( engine_file.isDirectory() )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "Cannot use the path " + engine_file.getAbsolutePath()
            + "\n since it is to a directory not a file. Engine path changes will not be saved.");
            return;
        }
        if ( !engine_file.canRead() )
        {
            the_gui.getEditorPanel().displayError( "Error in Engine Path", "Cannot read from " + engine_file.getAbsolutePath()
            + "\nEngine path changes will not be saved. Check that the path for the BioNetGen Engine on this page is valid.");
            return;
        }
        else
        {
            setEnginePath( engine_file.getAbsolutePath() );
            setEngineFile( engine_file );
        }
        
        /*
        if ( version_1_rb.isSelected() )
        {
            engine_version = "1.x";
        }
        else if ( version_2_rb.isSelected() )
        {
            engine_version = "2.x";
        }
        else
        {
            if (debug_statements) System.out.println( "Error: neither version was selected at configuration save and exit.");
        }
          
         */
        
        if ( look_and_feel_cb.isSelected() != isUseNativeLookAndFeel() )
        {
            the_gui.getEditorPanel().displayInformation("Look & Feel Changed","Look & Feel changes will take effect when RuleBuilder is restarted.");
            setUseNativeLookAndFeel( look_and_feel_cb.isSelected() );
        }
        
        engine_file = new File(engine_path);
        bngl_file = new File(bngl_path);
        
        config_dialog.setVisible( false );
        config_dialog.dispose();
    }
  }
  
    
    private class ConfigEngineBrowser implements ActionListener 
    {
    
        public void actionPerformed(ActionEvent event) 
        {
            
            File ef = getEngineFileFromUser();
            
            if ( ef != null )
            {
                engine_file_text.setText( ef.getAbsolutePath() );
                config_dialog.repaint();
            }
            
        }
    
    }
    
    private class ConfigBNGLOutputBrowser implements ActionListener 
    {
    
        public void actionPerformed(ActionEvent event) 
        {
            File file = getBNGLFileFromUser();
            
            if ( file != null )
            {
                String bngl_path = file.getAbsolutePath();
                
                //append .bngl if not already the extension
                if (!bngl_path.matches(".*\\.bngl$"))
                {
                    bngl_path = bngl_path+".bngl";
                }
                
                bngl_output_file_text.setText( bngl_path );
                config_dialog.repaint();
            }
            
        }
    
    }
    
    private class ConfigVersionListener implements ActionListener 
    {
          
        public void actionPerformed(ActionEvent event) 
        {
            
        }
     
    }
    
        
    // Serialization explicit version
    private static final long serialVersionUID = 3;
    transient protected boolean debug_statements = true;
    
    transient JDialog config_dialog;
    transient GUI the_gui;
    
    transient private JTextField engine_file_text;
    transient private File engine_file;
    private String engine_path;
    
    transient private JTextField bngl_output_file_text;
    transient private File bngl_output_file;
    private String bngl_output_path;
    
     //transient private JRadioButton version_1_rb = new JRadioButton();
     //transient private JRadioButton version_2_rb = new JRadioButton();
     
     private String engine_version = new String("2.x");
     
     private Vector<String> file_history = new Vector<String>();
     
    public Configuration() 
    {
            use_native_look_and_feel = true;
    }
    
    public Vector<String> getFileHistory()
    {   
        // For backwards compatibility
        if ( file_history == null )
        {
            file_history = new Vector<String>();
        }
        

        
        return file_history;
    }
    
    public boolean addToFileHistory( String path )
    {
        
        // Check that the path is unique
        Iterator path_itr = file_history.iterator();
        while ( path_itr.hasNext() )
        {
            String existing_path = (String)path_itr.next();
            if ( path.equals( existing_path ) )
            {
                return false;
            }
        }
        
       
        if (debug_statements) System.out.println("Config: adding " + path + " to file history.");
        file_history.add( path );
       
        // Limit the list to six on program startup
        if ( file_history.size() >= 6 )
        {
            file_history.remove(0);
        }
        
        return true;
    }
    
    public void setGUI( GUI the_gui )
    {
        this.the_gui = the_gui;
    }
    
    public File getEngineFile() 
    {
        return engine_file;
    }
    
    public void setEngineFile(File value) {
 
        engine_file = value;
   }
    
    public void setEnginePath(String value) 
    {
 
        engine_path = value;
    }
    
    public void setBNGLOutputPath(String value) 
    {
 
        bngl_output_path = value;
   }
    
    public void setBNGLOutputFile(File value) {
 
        bngl_output_file = value;
   }
    
      public File getBNGLOutputFile() {
 
        return bngl_output_file;
   }
      
   public String getEngineVersion() 
   {
        return engine_version;
   }
    
    public void displayConfigWindow() 
    {
        
        boolean is_modal = true;
        config_dialog = new JDialog( the_gui.getMainFrame(), "RuleBuilder Settings", is_modal);
        config_dialog.setLocation( 100, 100 );
        config_dialog.setSize( 300, 250 );
        config_dialog.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
        config_dialog.addWindowListener( new ConfigCloser() );
        
        Container content = config_dialog.getContentPane();
        
        JPanel button_panel = new JPanel();
        JPanel engine_path_panel = new JPanel();
        JPanel output_path_panel = new JPanel();
        JPanel version_panel = new JPanel(new FlowLayout( FlowLayout.LEFT ));
       
        JPanel look_and_feel_panel = new JPanel();
        look_and_feel_cb = new JCheckBox();
        look_and_feel_cb.setSelected( use_native_look_and_feel );
        
        JButton save_button = new JButton("Save");
        button_panel.add( save_button );
        save_button.addActionListener( new ConfigCloser() );
        
        JButton cancel_button = new JButton("Cancel");
        button_panel.add( cancel_button );
        cancel_button.addActionListener( new ConfigCancel() );
        
        // Engine File Input
        JButton engine_file_button = new JButton("Browse");
        
        String current_engine_path = "";
        File current_engine_file = getEngineFile();
        if ( current_engine_file != null )
        {
            current_engine_path = current_engine_file.getAbsolutePath();
        }
        
        
        JLabel engine_file_label = new JLabel("BioNetGen Perl Engine: ");
        engine_file_text = new JTextField(current_engine_path, 50 );
        engine_path_panel.add( engine_file_label, BorderLayout.WEST );
        engine_path_panel.add( engine_file_text, BorderLayout.CENTER );
        engine_path_panel.add( engine_file_button, BorderLayout.EAST );
        
        engine_file_button.addActionListener( new ConfigEngineBrowser() );
        
        // BNGL Output File Input
        JButton bngl_output_file_button = new JButton("Browse");
        
        String current_bngl_output_path = "";
        File current_bngl_output_file = getBNGLOutputFile();
        if ( current_bngl_output_file != null )
        {
            current_bngl_output_path = current_bngl_output_file.getAbsolutePath();
        }
        
        JLabel bngl_output_file_label = new JLabel("Temporary Work File:  ");
        bngl_output_file_text = new JTextField(current_bngl_output_path, 50 );
        output_path_panel.add( bngl_output_file_label, BorderLayout.WEST );
        output_path_panel.add( bngl_output_file_text, BorderLayout.CENTER );
        output_path_panel.add( bngl_output_file_button, BorderLayout.EAST );
        
        bngl_output_file_button.addActionListener( new ConfigBNGLOutputBrowser() );
       
        //ButtonGroup bg = new ButtonGroup();
        //bg.add( version_1_rb );
        //bg.add( version_2_rb );
        //version_panel.add( new JLabel("Engine Version "));
        //version_panel.add( version_1_rb );
        //version_panel.add( new JLabel("1.x"));
        //version_panel.add( version_2_rb );
        //version_panel.add( new JLabel("2.x"));
       
        look_and_feel_panel.add( new JLabel("Use Native OS Look & Feel: ") );
        look_and_feel_panel.add( look_and_feel_cb );
        
        
        BoxLayout settings_layout = new BoxLayout( content, BoxLayout.Y_AXIS );
        //BorderLayout settings_layout = new BorderLayout();
        content.setLayout( settings_layout );
        
        content.add(engine_path_panel);
        //content.add(version_panel);
        content.add(output_path_panel);
        content.add(look_and_feel_panel);
        content.add(button_panel);
       
        engine_path_panel.setVisible(true);
        
        config_dialog.pack();
        
        config_dialog.setVisible( true );
    }
    
    public File getEngineFileFromUser() 
    {
        File file = null;
    
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new PerlFilter());
        int returnVal = fc.showDialog( config_dialog,"OK");
        File output_file = null;
                        
        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            file =  fc.getSelectedFile();
            
        }
        
        return file;
    }
    
    public File getBNGLFileFromUser() 
    {
        File file = null;
    
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new BNGLFilter());
        int returnVal = fc.showDialog( config_dialog,"OK");
        File output_file = null;
                        
        //Process the results.
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            file =  fc.getSelectedFile();
        }
        
        return file;
    }
    
    public void writeValues()
    {
        String engine_path = "null";
        String bngl_path = "null";
        
        if ( getEngineFile() != null ) 
        {
            engine_path = getEngineFile().getAbsolutePath();
        }
        
        if ( getBNGLOutputFile() != null ) 
        {
            bngl_path = getBNGLOutputFile().getAbsolutePath();
        }
        
        
        if (debug_statements) System.out.println("Engine File Path: " + engine_path );
        if (debug_statements) System.out.println("BNGL Output File Path: " + bngl_path );
        
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
	    stream.defaultReadObject();
            
            engine_path = (String) stream.readObject();
            bngl_output_path = (String) stream.readObject();
            file_history = (Vector<String>) stream.readObject();
            
            engine_file = new File( engine_path );
            bngl_output_file = new File( bngl_output_path );
    }
    
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException
    {
        stream.defaultWriteObject();
        
	stream.writeObject( (String)engine_path );
        stream.writeObject( (String)bngl_output_path );
        stream.writeObject( (Vector<String>)file_history );
    }

    private boolean use_native_look_and_feel;

    public boolean isUseNativeLookAndFeel() 
    {
        return use_native_look_and_feel;
    }

    public void setUseNativeLookAndFeel(boolean use_native_look_and_feel ) 
    {
    
        this.use_native_look_and_feel = use_native_look_and_feel;
    }

    transient private JCheckBox look_and_feel_cb;
    
    
    
}
