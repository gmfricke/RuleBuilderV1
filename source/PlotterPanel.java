/*
 * PlotterPanel.java
 *
 * Created on March 3, 2006, 10:06 AM
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
import javax.swing.event.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import ptolemy.plot.Plot;

public class PlotterPanel extends JPanel
{
    class CheckBoxListener implements ItemListener 
    {
        public void itemStateChanged(ItemEvent e) 
	{
            try
            {
                Object source = e.getItemSelectable();
                if (debug_statements) System.out.println( e + "\n");

                // There was a change in the course selection panel so redisplay
                plot();
            }
            catch ( Exception exp )
            {
                exp.printStackTrace();
            }
        }
    }
    
    class ListSelectionHandler implements ListSelectionListener 
    {
     public void valueChanged(ListSelectionEvent e) 
     {
        plot();
        
        /*
        int firstIndex = e.getFirstIndex();
        int lastIndex = e.getLastIndex();
        boolean isAdjusting = e.getValueIsAdjusting();
        if (debug_statements) System.out.println("Event for indexes "
                      + firstIndex + " - " + lastIndex
                      + "; isAdjusting is " + isAdjusting
                      + "; selected indexes:");

        if (lsm.isSelectionEmpty()) {
            if (debug_statements) System.out.println(" <none>");
        } else {
            // Find out which indexes are selected.
            int minIndex = lsm.getMinSelectionIndex();
            int maxIndex = lsm.getMaxSelectionIndex();
            for (int i = minIndex; i <= maxIndex; i++) {
                if (lsm.isSelectedIndex(i)) 
                {
                    if (debug_statements) System.out.println(" " + i);
                    
                }
            }
        }
        if (debug_statements) System.out.println();
         **/
    }
}
    transient protected boolean debug_statements = true;
    transient private Plot plot = new Plot();
    private Vector<Vector<Double>> data_courses = new Vector<Vector<Double>>();
    private Vector<Double> time_course = new Vector<Double>();
    transient private JPanel course_selection = new JPanel();
    transient private Vector<JCheckBox> course_check_boxes = new Vector<JCheckBox>();
    transient private String headings[];
    transient private String project_name;
    //private JPanel course_labels = new JPanel();
    transient JList list; 
    transient ListSelectionModel list_selection_model;
    
    /** Creates a new instance of PlotterPanel */
    public PlotterPanel()  
    {            
        plot.setSize(800, 500);
        //plot.setButtons(true);
        //plot.setYRange(0, 4);
        //plot.setXRange(0, 1);
        //plot.addYTick("-PI", -Math.PI);
        //plot.addYTick("-PI/2", -Math.PI / 2);
        plot.addYTick("0", 0);
        plot.addXTick("0", 0);
        //plot.addYTick("PI/2", Math.PI / 2);
        //plot.addYTick("PI", Math.PI);
        plot.setMarksStyle("points");
        plot.setYRange(0, 1.0);
        
        // Layout the two plots
        //GridBagLayout gridbag = new GridBagLayout();
        //GridBagConstraints c = new GridBagConstraints();
        setLayout(new BorderLayout());
                            
        list = new JList();
        list_selection_model = list.getSelectionModel();
        list_selection_model.addListSelectionListener(new ListSelectionHandler());
        
        JScrollPane listScroller = new JScrollPane(list);
        listScroller.setPreferredSize(new Dimension(150, 500));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        
        list_selection_model.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        //Create a container so that we can add a title around
        //the scroll pane.  Can't add a title directly to the
        //scroll pane because its background would be white.
        //Lay out the label and scroll pane from top to bottom.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("Concentrations");
        label.setLabelFor(list);
        //listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0,5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        
        //course_selection_panel.setLayout();
        
        //course_selection_panel.add( course_labels );
        //course_selection_panel.add( course_selection );
        
        //course_labels.setLayout( new BoxLayout(course_labels,BoxLayout.PAGE_AXIS) );
        listPane.setLayout( new BoxLayout(listPane,BoxLayout.PAGE_AXIS) );
        
        // Handle the course selection panel
        //c.gridx = 0;
        //c.gridy = 0;
        //c.gridwidth = 1;
        //gridbag.setConstraints(course_selection, c);
        //gridbag.setConstraints(listPane, c);
        
        listPane.setSize(150,500);
        listPane.setPreferredSize(new Dimension(160,500));
        
        add(listPane, BorderLayout.WEST);
        
        // Handle the rightPlot
        //c.gridx = 1;
        //c.gridy = 0;
        //c.gridwidth = 1;
        //c.fill = GridBagConstraints.BOTH;
        //c.weightx = 1.0;
        //c.weighty = 1.0;
        //gridbag.setConstraints(plot, c);
        
        add(plot, BorderLayout.CENTER);
        
        // Call setConnected before reading in data.
        plot.setConnected(true);
        plot.setImpulses(false);
        
        plot.setYRange(0,1.0);
        plot.setXRange(0,1.0);
        
        //readDATFile( new File("/Users/matthew/test.gdat") );
        //plot();
    }
    
    public void setProjectName( String name )
    {
        project_name = name;
    }
    
    public void actionPerformed(ActionEvent e) 
    {
            if ( e.getActionCommand().equals("gdat") )
            {
                readDATFile( new File(project_name + ".gdat") );
            }
            else if ( e.getActionCommand().equals("cdat") )
            {
                readDATFile( new File(project_name + ".cdat") );
            }
    }
              
    
    public boolean plot()
    {
        //plot.clear(false); // needed to clear the ticks
        plot.clear(true);
        int counter = 0;
        
        plot.setYRange(0,1.0);
        plot.setXRange(0,1.0);
        
        plot.setXLabel("time");
        plot.setYLabel("concentration");
       
        
        Iterator<Vector<Double>> i = data_courses.iterator();
        Iterator<Double> time_itr = time_course.iterator();
        
        double max_time = 0;
        double max_y = 0;
        
        int data_course_id = 0;
        while ( i.hasNext() )
        {
            Vector<Double> course = i.next();
            Iterator<Double> course_itr = course.iterator();

            // remove the data course and then add it back in if selected
            plot.clear(data_course_id);
            plot.removeLegend(data_course_id);
            
          
            double max_value = 0; // max of each trace
            
            //if ( course_check_boxes.get( data_course_id ).isSelected() )
            if ( list_selection_model.isSelectedIndex(data_course_id) )
            {
                boolean first = true;
                while( course_itr.hasNext() )
                {
                    double time = time_itr.next().doubleValue();
                    double value = course_itr.next().doubleValue();
                    
                    // find max time
                    if ( max_time < time ) max_time = time;
                    
                    // find max y from all data courses
                    if ( max_y < value ) max_y = value;
                    
                    // find max value for this course
                    if ( max_value < value ) max_value = value;
                    
                    if (debug_statements) System.out.println("Added data point at: " + value );
                    plot.addPoint(data_course_id, time, value, !first );
                    first = false;
                }
                
                plot.addLegend(data_course_id, headings[data_course_id]);
            }
            
            //plot.addYTick(new Double(max_value).toString(), max_value );
            
            time_itr = time_course.iterator();
            data_course_id++;
            
        }
        
        if (debug_statements) System.out.println("max_y="+max_y);
        if ( max_y == 0 )
        {
            max_y = 1; // set to a valid value
        }
        else
        {
            // I want to display y axis values that are relevent to the data being
            // plotted. To do this I find out how many zeros are after the decimal point
            // and display values rounded to that number of zeros plus two decimal places
            // round max y and add a little padding up to 2 decimal places
            
            double val = 0;
            double factor = 0;
            for ( int j = 0; Math.round(val) == 0; j++ )
            {
                factor = Math.pow( 10, j );
                val = max_y*factor;
            }
            
            max_y = Math.ceil(val*10)/(factor*10);
   
        }
        
        // Add regularly spaced y ticks
        
        if ( max_y == 0) max_y = 1; // so we dont go to infinity in the for loop
        
        //plot.addYTick(new Double(1.0).toString(), 1.0 );
        plot.addYTick(new Double(0.0).toString(), 0.0 );
        
        double y_tick = 0;
        for ( ; y_tick <= max_y; y_tick+=max_y/10 )
        {
            //y_tick = (Math.ceil(y_tick*1000))/1000d;
            
            if ( y_tick != 0 )
            {
            double val = 0;
            double factor = 0;
            for ( int j = 0; Math.round(val) == 0; j++ )
            {
                factor = Math.pow( 10, j );
                val = y_tick*factor;
            }
            
            y_tick = Math.round(val*100)/(factor*100);
            }
            
            //if ( y_tick > 1.0 ) y_tick = 1.0;
            if (debug_statements) System.out.println(":::y_tick="+y_tick);
            plot.addYTick(new Double(y_tick).toString(), y_tick );
        }
        
        // one more time to make sure we have a tick above max_y
        if ( y_tick != 0 )
            {
            double val = 0;
            double factor = 0;
            for ( int j = 0; Math.round(val) == 0; j++ )
            {
                factor = Math.pow( 10, j );
                val = y_tick*factor;
            }
            
            y_tick = Math.round(val*100)/(factor*100);
            }
        
        //if ( y_tick > 1.0 ) y_tick = 1.0;
        plot.addYTick(new Double(y_tick).toString(), y_tick );
        if (debug_statements) System.out.println("Added y tick at " + (y_tick));
        
        double y_top = y_tick;
        //if ( y_top > 1.0 ) y_top = 1.0;
        plot.setYRange(0,y_top);
        
        
        // Add regularly spaces x ticks
        plot.addXTick(new Double(0.0).toString(), 0.0 );
        
        if ( max_time == 0) max_time = 1; // so we dont go to infinity in the for loop
        
        double x_tick = 0;
        double max_x = max_time;
        for ( ; x_tick <= max_x; x_tick+=max_x/10 )
        {
            if ( x_tick != 0 )
            {
            double val = 0;
            double factor = 0;
            for ( int j = 0; Math.round(val) == 0; j++ )
            {
                factor = Math.pow( 10, j );
                val = x_tick*factor;
            }
            
            x_tick = Math.round(val*100)/(factor*100);
            }
            
            //if ( y_tick > 1.0 ) y_tick = 1.0;
            if (debug_statements) System.out.println(":::x_tick="+x_tick);
            plot.addXTick(new Double(x_tick).toString(), x_tick );
        }
        
        // one more time to make sure we have a tick above max_y
        if ( x_tick != 0 )
            {
            double val = 0;
            double factor = 0;
            for ( int j = 0; Math.round(val) == 0; j++ )
            {
                factor = Math.pow( 10, j );
                val = x_tick*factor;
            }
            
            x_tick = Math.round(val*100)/(factor*100);
            }
        
        //if ( y_tick > 1.0 ) y_tick = 1.0;
        plot.addXTick(new Double(x_tick).toString(), x_tick );
        if (debug_statements) System.out.println("Added x tick at " + (x_tick));
        plot.setXRange(0,max_x);
        
        //for ( double t_tick = 0; t_tick < max_time; t_tick+=max_time/10 )
        //{
        //    plot.addXTick(new Double(t_tick).toString(), t_tick );
        //}
        
        //plot.fillPlot(); //resize plot to fit
        this.revalidate();
        plot.repaint();
        
        return true;
    }
    
    public boolean readDATFile( File file )
    {
        data_courses.removeAllElements();
        time_course.removeAllElements();
        
        
        plot.setTitle( file.getName() );
        
        try
        {
        FileInputStream fileIn = new FileInputStream ( file );
        ProgressMonitorInputStream progressIn = new ProgressMonitorInputStream ( null,
                                    "Reading " + file.getName (), fileIn);
        InputStreamReader inReader = new InputStreamReader (progressIn);
        final BufferedReader in = new BufferedReader (inReader);
        progressIn.getProgressMonitor().setMillisToPopup(0);
         
            boolean title = true;
            String line = null;
            while ( (line = in.readLine() ) != null )
            {
                if (debug_statements) System.out.println("Read: "+line);
                               
                int num_data_courses = 0;   
                
                if ( title )
                {
                    line = line.replaceAll("^\\s*","");
                    
                    title = false;
                    String line_headings[] = line.split("\\s+");
                    headings = new String[line_headings.length-2];
                    for ( int i = 2; i < line_headings.length; i++ ) // start at 2 because of leading # in headings and leading " " in data lines.
                    {
                        headings[i-2] = line_headings[i];
                        //plot.addLegend(i, headings[i]);
                        data_courses.add( new Vector<Double>() );
                        JPanel jp = new JPanel( new BorderLayout() );
                        JCheckBox check_box = new JCheckBox();
                        check_box.addItemListener( new CheckBoxListener() );
                        jp.add( new JLabel(headings[i-2]), BorderLayout.WEST );
                        jp.add( check_box, BorderLayout.EAST );
                        course_selection.add( jp );
                        course_check_boxes.add( check_box );
                    }
                    
                    num_data_courses = headings.length; 
                    
                    continue;
                }
                
                String line_values[] = line.split("\\s+"); //includes leading space
                
                for ( int i = 1; i < line_values.length; i++ )
                {
                    if (i == 1) time_course.add( new Double(line_values[1]) );
                    else data_courses.get(i-2).add( new Double(line_values[i]) ); // -2 skips the blank space at position 0 and the time course
                }
                   
            }
            
            list.setListData(headings);
        }
        catch ( java.io.FileNotFoundException file_not_found )
        {
            // Do nothing
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
            
        
        return true;
    }
    
    public void initialize()
    {
        plot.clear(false);
        headings = new String[0];
        list.setListData(headings);
        data_courses.removeAllElements();
        time_course.removeAllElements();
        course_selection.removeAll();
        repaint();
    }
    
    
    public boolean print() 
    {
        if (debug_statements) System.out.println("PlotterPanel::print() called.");
        PrintUtilities.printComponent( plot );
        return true;
    }


}
