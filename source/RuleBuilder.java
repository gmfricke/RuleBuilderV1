import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.awt.*; // For older windowing
import java.io.*; // For file access

public class RuleBuilder
{
    
    RuleBuilder( String bng_path )
    { 
	
            GUI gui_reference = new GUI( bng_path ); 
            Model model_reference = new Model( gui_reference );
            gui_reference.addModel( model_reference );
            
            Thread model_thread = new Thread( model_reference );
            Thread gui_thread = new Thread( gui_reference );
	   
            gui_reference.setModelThread( model_thread );
            
            model_thread.start();
            gui_thread.start();
            
           
    }

    /**
     *
     * @param args
     */    
    public static void main(String[] args) 
    {      
        System.out.print("Args:");
        for ( int i = 0; i < args.length; i++ )
        {
            System.out.print(" " + args[i]);
        }
        System.out.println();
        
	// mlf; handle NullPointerException error
        //String bng_path = null;
        String bng_path = new String("no_path");

        if ( args.length > 0 ) bng_path = args[0];
        
        // Have to do inline communication because invoke in Launcher.java
        // fails if passed a null value for args
        if ( bng_path.equals("no_path") )
        {
            bng_path = null;
        }
        
        if ( bng_path != null )
        {
            File check = new File( bng_path );
            if ( !check.exists() )
            {
                System.out.println(bng_path + " does not exist. Exiting.");
                System.exit(1);
            }
        }
          
        System.out.println("Starting with file " + bng_path);
	Runtime.getRuntime().traceMethodCalls(true);
	new RuleBuilder( bng_path );
       
    }

}
