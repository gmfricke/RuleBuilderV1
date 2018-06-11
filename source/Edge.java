import java.beans.*; // For java bean object support methods
import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
//import java.awt.geom.AffineTransform; // For resizing containers
import java.awt.geom.*;
import java.awt.*; 
 
// Widgets are straight lines drawn from (x,y) to (x+width,y+height)
    public class Edge extends Widget implements Serializable
    {
            
        // Serialization explicit version
        private static final long serialVersionUID = 1;
        
	protected Connectable start;
	protected Connectable end;
	transient Line2D line = new Line2D.Float();
        long edge_id;

        // For subclasses
        protected Edge(){}
        
        /**
         *
         * @param start
         * @param end
         * @param containing_panel
         */        
	public Edge( Connectable start, Connectable end, WidgetPanel containing_panel )
	{
            if ( debug_statements ) System.out.println( this + ": constructing with Components " + start + " " + end );
            
	    setContainingPanel( containing_panel );
	    selected = false;
            
            link(start, end);
            
            // Not decided whether edges should show labels
            setLabel( "e");
            
            updateLocation();
	}

	public void display( Component c, Graphics2D g2d )
	{
            if ( !isVisible() ) return;
            
            //label.updateLocation( getX() + getWidth()/2, getY() + getHeight()/2, false );
	    	    
	    //g.setColor(Color.black);
	    
	    // Draw a line from one Component to another, the
	    // getWidth/Height business is so the line will
	    // be drawn from the center of the components
	    
	    //Arrow arrow = new Arrow();
	    //g.drawLine( start.getX() + start.getWidth()/2, 
	    //		start.getY() + start.getHeight()/2,
	    //		end.getX() + end.getWidth()/2,
	    //		end.getY() + end.getHeight()/2);
	    //

	    //arrow.drawArrow(g,start.getX() + start.getWidth()/2, 
	    //		    start.getY() + start.getHeight()/2,
	    //		    end.getX()+ start.getWidth()/2,
	    //		    end.getY()+ start.getHeight()/2 );

	    //Arrow arrow=new Arrow();
	    //arrow.drawArrow(g,100,100,Math.PI/4,100,Arrow.SIDE_LEAD);
	    
	    // Draw box at line center
	    //g.drawRect( getX()-5, getY()-5, 10, 10 );

            if (debug_statements) System.out.println("Edge " + this + ": start=" + getStart() + " end=" + getEnd() );
            
	    g2d.setColor(color);
            Point start_point = start.getEdgeAttachPoint();
            Point end_point = end.getEdgeAttachPoint();
            
            int start_x = (int)start_point.getX();
            int start_y = (int)start_point.getY();
            int end_x = (int)end_point.getX();
            int end_y = (int)end_point.getY();
            
	    line = new Line2D.Float( start_x, start_y, end_x, end_y );
	    //g2d.setStroke(new BasicStroke(8));
	    //Graphics2D g2d = (Graphics2D)g;
	    ((Graphics2D)g2d).draw(line);
            //g.drawString( getStart().getID()+"-("+getID()+")-"+ getEnd().getID(), getX()+(getWidth()/2), getY()+getHeight()/2 );
            
            //if (label != null) label.updateLocation( getX()+getWidth()/2, getY()+getHeight()/2, false );
            
            //if ( getLabel() != null )
            //    g.drawString( getLabel(), getX()+(getWidth()/2), getY()+getHeight()/2 );
            
	    g2d.setColor(c.getBackground());
            }
            


	// Awkward method needed because one of the adjacent Components initiated the
	// edge deletion. We have to wait for the calling component to handle the other
	// end because it is using a for loop over a Vector of edges (the vector can't)
	// be messed with until the loop is complete. Find another way. 
	public void disconnectOtherEnd( Connectable caller )
	{
	    if ( caller == start )
		{
		    getEnd().getEdges().remove( this );
		}
	    else if ( caller == end )
		{
		    getStart().getEdges().remove( this );
		}
	    else 
		{
		    getContainingPanel().displayError("Serious Error in Edge:DisconnectOtherEnd()",
                    "A non-adjcent node (BioComponet) has called \"disconnectOtherEnd\"."
                    +"Contact support at support@bionetgen.com");
		}

	}

        /**
         *
         * @param s
         */        
       public boolean setStart( Connectable s )
       {   
            start = s;
            if ( start.addEdge( this ) )
            {
                return false;
            }
            return true;
        }
        
       /**
        *
        * @return
        */       
	public Connectable getStart()
	{
	    return start;
	}

        /**
         *
         * @param e
         */        
        public boolean setEnd( Connectable e )
        {
            end = e;
            if ( !end.addEdge( this ) )
            {
                return false;
            }
            
            return true;
        }
        
        /**
         *
         * @return
         */        
	public Connectable getEnd()
	{
	    return end;
	}

	public Connectable getOtherEnd( Connectable this_connectable )
	{
	    if ( start == this_connectable )
		{
		    return end;
		}
	    else if ( end == this_connectable )
		{
		    return start;
		}
	
            if (debug_statements) System.out.println("Given component that is not adjacent to this edge! Failing.");
	    return null;
	}

	public boolean contains(int mouse_x, int mouse_y)
	{
	    if (debug_statements) System.out.println( line.ptLineDist(mouse_x, mouse_y) );
	    return line.ptLineDist(mouse_x, mouse_y) < 2;
	}

        public int calculateX() 
        {
          
            if ( end.getEdgeAttachPoint().getX() > start.getEdgeAttachPoint().getX() )
            {
                return (int)start.getEdgeAttachPoint().getX();
            }
                
            return (int)end.getEdgeAttachPoint().getX();
            
        } 
        
        public int calculateY() 
        {
            if ( end.getEdgeAttachPoint().getY() > start.getEdgeAttachPoint().getY() )
            {
                return (int)start.getEdgeAttachPoint().getY();
            }
                
            return (int)end.getEdgeAttachPoint().getY();
            
        } 
        
        public int calculateWidth()
        {
            return (int)Math.abs( end.getEdgeAttachPoint().getX() 
                            - start.getEdgeAttachPoint().getX() );
        }
        
        public int calculateHeight()
        {
            return (int)Math.abs( end.getEdgeAttachPoint().getY() 
                            - start.getEdgeAttachPoint().getY() );
        
        }
        
        public int getX()
        {
            x = calculateX();
            return x;
        }
        
        public int getY()
        {
            y = calculateY();
            return y;
        }
        
        public int getHeight()
        {
            height = calculateHeight();
            return height;
        }
        
        public int getWidth()
        {
            width = calculateWidth();
            return width;
        }
        
        public void updateLocation()
        {
            x = calculateX();
            y = calculateY();
            width = calculateWidth();
            height = calculateHeight();
            
            label.setX( x + width/2 );
            label.setY( y + height/2 );
        }
        
        public void paint(int x_origin, int y_origin, Graphics2D g2d) 
        {
            // First get start and end positions relative to the container or biograph
            // they are inside
            int s_x = (int)getStart().getEdgeAttachPoint().getX()-x_origin;
            int s_y = (int)getStart().getEdgeAttachPoint().getY()-y_origin;
            
            int e_x = (int)getEnd().getEdgeAttachPoint().getX()-x_origin;
            int e_y = (int)getEnd().getEdgeAttachPoint().getY()-y_origin;            
            
            g2d.setColor(Color.BLACK);
	    line = new Line2D.Float(s_x + start.getWidth()/2,
					   s_y + start.getHeight()/2, 
					   e_x + end.getWidth()/2, 
					   e_y + start.getHeight()/2);
	   
	    g2d.draw(line);
            
	    g2d.setColor(Color.WHITE);
        }
        
	/*
	int getX()
	{
	    return (start.getX() + end.getX())/2;
	}

	int getY()
	{
	    return (start.getY() + end.getY())/2;
	}
	*/

    public void link(Connectable new_start, Connectable new_end) 
    {
        if ( debug_statements ) System.out.println( this + ": Linking Components " + start + " " + end );
        // Check that this edge isnt a duplicate
            Vector<Edge> duplicates = new Vector();
            Iterator<Edge> edge_itr = new_start.getEdges().iterator();
            while ( edge_itr.hasNext() )
            {
                Edge edge = edge_itr.next();
                if ( edge.getOtherEnd( new_start ) == new_end )
                {
                    duplicates.add( edge );
                 
                  
                    
                    //getContainingPanel().displayError("Edge Creation Error", "Attempt to create an edge that already exists.");
                    //return;
                }
            }
            
            Iterator<Edge> duplicate_itr = duplicates.iterator();
            while ( duplicate_itr.hasNext() )
            {
                Edge e = duplicate_itr.next();
                new_start.removeEdge(e);
                if (debug_statements) System.out.println( "Duplicate edge \""+e.getLabel()+"\" replaced which this edge");
            }
            
            start = new_start;
            end = new_end;
            
            // Only set start and end to be this.start and this.end if adding this edge
            // to the start and end components succeeds
	    if ( !start.addEdge(this) || !end.addEdge(this) )
            {
                if (debug_statements) System.out.println( this+": Could not link components.");
                start = null;
                end = null;
            }
           
            x = calculateX();
            y = calculateY();
            height = calculateHeight();
            width = calculateWidth();
            
    }
     }
