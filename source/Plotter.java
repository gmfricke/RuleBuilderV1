/*
 * Plotter.java
 *
 * Created on February 10, 2006, 2:38 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

/**
 *
 * @author matthew
 */

import javax.swing.*;
import java.io.*;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.awt.event.*; // For checkbox events

import java.util.*;

public class Plotter extends JInternalFrame implements ActionListener
{         
    transient protected boolean debug_statements = true;
    private PlotterPanel gdat_plot = new PlotterPanel();
    private PlotterPanel cdat_plot = new PlotterPanel();
    
    /** Creates a new instance of Plotter */
    public Plotter() 
    {
        
        
        setTitle("Concentration Plot");
        setIconifiable( true );
        setMaximizable( true );
        setResizable( false );
        
        // Set the size of the toplevel window.
        setSize(950, 500);

        try
        {
            setSelected(true);
        }
        catch ( java.beans.PropertyVetoException pve )
        {
            if (debug_statements) System.out.println("Plotter Frame Selection Vetoed");
        }
        
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Observables", gdat_plot);
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);

        tabbedPane.addTab("Species",cdat_plot);
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2);
        
        getContentPane().add( tabbedPane );
    }
    
    public void actionPerformed( ActionEvent e ) 
    {
        if ( e.getActionCommand().equals("print") ) 
        {
               print();
        }
    }
        
    public void initialize()
    {
        gdat_plot.initialize();
        cdat_plot.initialize();
    }

    public void print() 
    {
        if ( gdat_plot.isVisible() ) gdat_plot.print();
        if ( cdat_plot.isVisible() ) cdat_plot.print();
    }

    public void readDATFiles(String path) 
    {
        try
        {
        File gdat_file = new File(path + ".gdat");
        File cdat_file = new File(path + ".cdat");
        cdat_file.deleteOnExit();
        gdat_file.deleteOnExit();
            gdat_plot.readDATFile( gdat_file );
            cdat_plot.readDATFile( cdat_file );
    
            if (debug_statements) System.out.println("Plotter Read " + path + ".cdat");
            if (debug_statements) System.out.println("Plotter Read " + path + ".gdat");
        }
        catch (Exception e )
        {
            e.printStackTrace();
        }
    }
    
    public void plot()
    {
        gdat_plot.plot();
        cdat_plot.plot();
    }
}
