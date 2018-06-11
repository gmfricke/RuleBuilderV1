/*
 * CDKCapsule.java
 *
 * Created on December 14, 2005, 10:42 AM by Matthew Fricke
 *
 * This Widget encapsulates CDK (Chemistry Development Kit) Models
 * and allows them to be visualized inside RuleBuilder.
 */

/**
 *
 * @author  Matthew Fricke
 */

import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For action events
import javax.swing.*; // For graphical interface tools

import java.io.*;

import org.openscience.cdk.ChemModel;
import org.openscience.cdk.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.io.*;

import java.util.*; // For Vector

import javax.vecmath.Point2d;

public class CDKCapsule extends Widget 
{
    transient private JFileChooser open_fc;
    transient private ChemModel cdk_model = new ChemModel();
    private Molecule cdk_molecule = new Molecule();
    transient private AlphaRenderer2D renderer; 
    transient private Renderer2DModel renderer_model;
    private Point cdk_offset = new Point( 0, 0 );
    transient private Dimension cdk_display_size = new Dimension(10000,10000);
    private Vector<Point2d> original_atom_locations = new Vector<Point2d>();
    private static final long serialVersionUID = 1;
    
    private String MDL_path;
    
    /** Creates a new instance of CDKCapsule */
    public CDKCapsule( int x, int y, int width, int height, WidgetPanel panel ) 
    {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.containing_panel = panel;
     
        //renderer_model = new Renderer2DModel();
        renderer = new AlphaRenderer2D();// renderer_model );
        
        setLabel("CDK Capsule");
        
        }
    

        
        
    public void display( Component c, Graphics2D g2d )
    {
        if ( !isVisible() ) return;
        
        renderer.getRenderer2DModel().setBackgroundDimension( cdk_display_size );
        renderer.getRenderer2DModel().setForeColor( Color.BLACK );
        //renderer.getRenderer2DModel().setZoomFactor( 4 );
        
        //renderer.paintChemModel(cdk_model, g2d);
        if (debug_statements) System.out.println("Displaying molecule with " + cdk_molecule.getAtomCount() + " atoms and path ." + MDL_path);
        renderer.paintMolecule(cdk_molecule, g2d);
        
        
        g2d.setColor(Color.BLACK);
        //g2d.drawRect( getX(), getY(), getWidth(), getHeight() );
        if (debug_statements) System.out.println("Bounding Box:" + getX() + "," + getY() + "," + getWidth() + "," + getHeight() );
       
    }
    
    public Molecule getCDKMolecule()
    {
        return cdk_molecule;
    }
    
    public void displayPopupMenu(int mouse_x, int mouse_y) 
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
        JPopupMenu popup = new JPopupMenu();
        JMenuItem read_mdl = new JMenuItem("Read MDL");
        read_mdl.addActionListener( this );
        popup.add( read_mdl ); 
        popup.show( getContainingPanel(), mouse_x, mouse_y );
    }
    
    public void readCDKMoleculeFromMDLFile( File file ) 
    {
        MDL_path = file.getAbsolutePath();
        
        try
        {
         
       boolean modal = false;
       //JFrame owner = getContainingPanel().getTheGUI().getMainFrame();
       //JDialog progress_frame = new JDialog(owner, modal);
        
       
       
        //InputStream in = new BufferedInputStream(
        //                  new ProgressMonitorInputStream(
        //                          progress_frame,
        //                          "Reading " + file.getName(),
        //                          new FileInputStream(file)));
        
       InputStream in = new BufferedInputStream( new FileInputStream(file) );
        
            
        MDLReader reader = new MDLReader( in );
        
        //cdk_model = new ChemModel();
       
        Molecule mol = (Molecule)reader.read( new Molecule() );
        
        setCDKMolecule( mol );
        
        in.close();
        
        }
        catch (Exception e)
        {
            if ( getContainingPanel() != null )
            {
                getContainingPanel().displayError("Error Reading MDL File","Exception when trying to open\n" + file.getAbsolutePath() + ":\n"+e.getMessage() );
            }
            
            e.printStackTrace();
        }
    }

    void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
    {
            setX( mouse_x );
            setY( mouse_y ); // cdk reverses the y axis
        
            label.setX( mouse_x );
            label.setY( mouse_y );
            
            // Move the atoms
            int mol_height = getHeight();
            int y_start = (int)cdk_display_size.getHeight() - (y+mol_height+cdk_offset.y); 
   
            if (debug_statements) System.out.println("Molecule Height: " + mol_height );
            //y_start += mol_height;
            
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Point2d p2d = (Point2d)this.original_atom_locations.get(i);
                double relative_x = p2d.x + x + cdk_offset.x;
                double relative_y = p2d.y + y_start;
                Point2d relative_location = new Point2d( relative_x, relative_y );
                cdk_molecule.getAtomAt(i).setPoint2d( relative_location );
            }
    }
    
    public int getX() 
    {
        // Find min x 
            double min = 10000000;
            
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                double current = a.getPoint2d().x;
                
                
                if ( current < min ) min = current;
                
            }
            
            return (int)(min);
    }
    
    public int getY() 
    {
        // Remember that CDK inverts the y axis
            double m = 0;
            
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                double current = a.getPoint2d().y;
                
                if ( current > m ) m = current;
                
            }
            
            return (int)(this.cdk_display_size.getHeight()-m);
    }
    
    public int getWidth() 
    {
            double max = 0;
            
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                double current = a.getPoint2d().x;
                
                if ( current > max ) max = current;
                
            }
            
            return (int)(max-getX());
    }
    
    public int getHeight() 
    {
        // Use the original locations to avoid recursion problems
        // when calculating current atom positions
        double max = 0;
            
            for ( int i = 0; i < this.original_atom_locations.size(); i++ )
            {
                Point2d p2d = (Point2d)this.original_atom_locations.get(i);
                double current = p2d.y;
                
                if ( current > max ) max = current;
                
            }
            
            return (int)max;
    }

    public void setCDKMolecule(Molecule molecule) 
    {
        try
        {
        // Generate a nice layout for the molecule and attach coords to atoms
            // the molecule wont display at all otherwise
            //The StructureDiagramGenerator generate the layout of molecule from its structure
            org.openscience.cdk.layout.StructureDiagramGenerator sdg = new org.openscience.cdk.layout.StructureDiagramGenerator();
            sdg.setMolecule((Molecule)molecule.clone()); //set the molecule to StructureDiagramGenerator
            sdg.setBondLength(35);
            
            sdg.generateCoordinates(); //generate the coordinates from its structure, the coordinates is stored in the internal representation of molecule
            cdk_molecule = sdg.getMolecule(); //get the molecule which includes the coordinates for each atom
        
            if (debug_statements) System.out.println("Read in a molecule with: ");
            if (debug_statements) System.out.println(cdk_molecule.getAtomCount() + " atoms.");
            if (debug_statements) System.out.println( "Assigned the following coordinates: " );
            
            // Make all coordinates positive
            
            // find smallest x & y
            double min_x = 100000000;
            double min_y = 100000000;
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                if ( min_x > a.getPoint2d().x ) min_x = a.getPoint2d().x;
                if ( min_y > a.getPoint2d().y ) min_y = a.getPoint2d().y;
            }
            
            //shift all atoms by that amount so they all have positive coords
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                a.getPoint2d().x += Math.abs( min_x );
                a.getPoint2d().y += Math.abs( min_y );
            }
            
            
            // Remember the original atom coords
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                this.original_atom_locations.add ( i, a.getPoint2d() );
            }
             
            int cdk_x_offset = 20;//(int)getX();
            int cdk_y_offset = 20;//-75;
            
            if (debug_statements) System.out.println( "cdk offsets " + cdk_x_offset + ", " + cdk_y_offset );
            
            
            cdk_offset = new Point( cdk_x_offset, cdk_y_offset ); 
            
            for ( int i = 0; i < cdk_molecule.getAtomCount(); i++ )
            {
                Atom a = cdk_molecule.getAtomAt(i);
                if (debug_statements) System.out.println( a.getSymbol()
                + " " + a.getPoint2d().x
                + " " + a.getPoint2d().y );
            }
            
            updateLocation( x-cdk_x_offset, y-cdk_y_offset, false );
        
        } 
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        
    }

    // CDK reverses the y axis. This function is needed to convert the CDK coords to 
    // RuleBuilder coords
    // Yet to be implemented
    public Point2d convertCDKCoords(Point2d cdk_point) 
    {
        
            int mol_height = getHeight();
            //int y_start = (int);//+cdk_offset.y); 
   
            if (debug_statements) System.out.println("Molecule Height: " + mol_height );
                
                double relative_x = cdk_point.x;// + cdk_offset.x;
                double relative_y = cdk_point.y;
                Point2d relative_location = new Point2d( relative_x, relative_y );
                
                return relative_location;
    }

    public Dimension getCDKDisplaySize() {
        return cdk_display_size;
    }

    public void setCDKDisplaySize(Dimension cdk_display_size) {
        this.cdk_display_size = cdk_display_size;
    }
    
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
    {
        stream.defaultReadObject();
        
        cdk_display_size = new Dimension(10000,10000);
        renderer = new AlphaRenderer2D();
        //this.reReadCDKMoleculeFromMDLFile();
    }

    public Vector<Point2d> getRelativeAtomLocations() 
    {
        Vector<Point2d> reverse_y_axis = new Vector<Point2d>();
        
        Iterator oal_itr = original_atom_locations.iterator();
        while ( oal_itr.hasNext() )
        {
            Point2d point = (Point2d)oal_itr.next();
            
            point = convertCDKCoords( point );
            
            int x = (int)point.x;
            int y = (int)point.y;
            
            reverse_y_axis.add( new Point2d(x, y) );
        }
        
        return reverse_y_axis;
    }

    public void setOriginal_atom_locations(Vector<Point2d> original_atom_locations) {
        this.original_atom_locations = original_atom_locations;
    }

    public void reReadCDKMoleculeFromMDLFile() 
    {
        try
        {
            if ( MDL_path == null ) return;
            
                File f = new File( MDL_path );
                readCDKMoleculeFromMDLFile( f );
        }
        catch (Exception e)
        {
            if (debug_statements) System.out.println("Exception when trying to open\n" + MDL_path + ":\n"+e.getMessage() );
            e.printStackTrace();
        }
    }

}
