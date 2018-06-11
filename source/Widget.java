/*
 * WidgetBean.java
 *
 * Created on December 3, 2004, 5:36 PM
 */  

import java.beans.*;
import java.io.Serializable;
import java.awt.*; // For graphical windowing tools
import java.awt.event.*; // For mouse interactions
import javax.swing.*; // For graphical interface tools
import java.net.*; //For URL image loading from Jar files
import java.util.*; //For vector data structure

import java.awt.datatransfer.*; // For drag 'n drop between windows
import java.awt.dnd.*; // For drag 'n drop between windows
import java.io.Serializable; // DropHandler needs to be Serializable
import java.io.*; // For file IO in write and read object methods

import java.awt.image.BufferedImage;

/**
 * @author matthew
 */

public class Widget extends Object implements Comparable, Serializable, ActionListener, Transferable
{
    

        
    // Serialization explicit version
    private static final long serialVersionUID = 1;
    
    transient protected boolean debug_statements = true;
    
    public boolean selected;
        transient Point drag_start = new Point(0,0);
        transient public Color color = Color.black;
	transient public Color selected_color = Color.blue;
        transient public Color unselected_color = Color.black;
	public int x;
	public int y;
	transient public WidgetPanel containing_panel;
	
        protected FlickrLabel label;
        //public String label;
	public boolean template;
	public int height;
	public int width;
	public String image_url;
        //transient public ImageIcon icon;
	//transient public Image image;
        public int x_offset;
	public int y_offset;
        protected long ID;
        private IDGenerator id_gen = new IDGenerator();
        transient protected Font font = new Font("Arial", Font.PLAIN, 12);
    
        private Vector peers = new Vector();
        
       // Called by subclasses
        public Widget()
        { 
            this.setID( id_gen.getNextID());
             
        }
        
        /**
         *
         * @param x
         * @param y
         * @param label
         * @param icon_url
         * @param template
         * @param containing_panel
         */        
        public Widget(int x, int y, String label, WidgetPanel containing_panel)
    {
	
        //containing_panel.add( this.label );
	this.x = x;
	this.y = y;
	this.containing_panel = containing_panel;
        
        setLabel( label );
        setID( id_gen.getNextID() );
        
    }
    
 
    
public boolean contains(MouseEvent e)
	{
	    return contains( e.getX(), e.getY() );
	}

public boolean contains(int mouse_x, int mouse_y )
	{
	    return mouse_x > getX() && mouse_x < getX() + getWidth() && mouse_y > getY() && mouse_y < getY() + getHeight();
            //return mouse_x > getZoomedX() && mouse_x < getZoomedX() + getZoomedWidth() && mouse_y > getZoomedY() && mouse_y < getZoomedY() + getZoomedHeight();
        } 

// Does this widgets bounding_box entirely contain the bounding box of the target widget?
boolean contains( Widget w )
	{
	    return getBoundingBox().contains( w.getBoundingBox() );
	}


void updateLocation( int mouse_x, int mouse_y, boolean confine_to_boundaries )
	{
            if (debug_statements) System.out.println("Widget:UpdateLocation(): mouse_x = " + mouse_x + ", " + "mouse_y = " + mouse_y );
            
	    // if (debug_statements) System.out.println("+++++ UPDATE LOCATION CALLED +++++");

            WidgetPanel ct = getContainingPanel();
            
            int panel_width = (int)(ct.getBaseDimension().getWidth()*(1.0/ct.getZoom())-ct.getZoomedXTranslation()*(1.0/ct.getZoom()));
            int panel_height = (int)(ct.getBaseDimension().getHeight()*(1.0/ct.getZoom())-ct.getZoomedYTranslation()*(1.0/ct.getZoom()));
            int panel_x = 0; //(int)(-ct.getZoomedXTranslation()*(1.0/ct.getZoom()));
            int panel_y = 0; //(int)(-ct.getZoomedYTranslation()*(1.0/ct.getZoom()));
           
            
            if (debug_statements) System.out.println( "panel_width = " + panel_width + ", panel_height = " + panel_height );
            if (debug_statements) System.out.println( "panel_x = " + panel_x + ", panel_y = " + panel_y );
            if (debug_statements) System.out.println("Zoomed X,Y: " + ct.getZoomedXTranslation() + ", " + ct.getZoomedYTranslation() );
            
	    int new_x = ( mouse_x - x_offset );
	    int new_y = ( mouse_y - y_offset );
            
	    if ( confine_to_boundaries )
		{
		    
		    if((new_x+width+10)> 10000 )//panel_width)
			{
			    new_x = (int)10000-getWidth();
			}
		    if(new_x < 0)
			{
			    new_x = 0; //panel_x;  
			}
		    if((new_y+height)>10000) //panel_height)
                    {
			new_y = (int)10000-getHeight() - 10; //(int)panel_height-getHeight() - 10;
		    }
		    if(new_y < 0)
                    {
			new_y = 0;//panel_y;
		    }
		}
            
            if (debug_statements) System.out.println("new x = " + new_x + ", " + "new y = " + new_y );
            
            
            this.x = new_x; 
	    this.y = new_y;
	    
            // Skip widgets that don't use their labels
            
            if ( !( this instanceof FlickrLabel || this instanceof SelectionBox) )
            {
                label.setX( new_x+getWidth()-label.getLabelXOffset() );
                label.setY( new_y+getHeight()-label.getLabelYOffset() );
            }
            
            int pref_width = containing_panel.getPreferredSize().width;
            int pref_height = containing_panel.getPreferredSize().height;
            
            int new_left_x_loc = getZoomedX();
            int new_up_y_loc = getZoomedY();
            int new_right_x_loc = getZoomedX()+getZoomedWidth();
            int new_down_y_loc = getZoomedY()+getZoomedHeight();
            
            if (debug_statements) System.out.println( "new_left_x_loc: "+ new_left_x_loc );    
            if (debug_statements) System.out.println( "new_up_y_loc: "+ new_up_y_loc ); 
            if (debug_statements) System.out.println( "new_right_x_loc: "+ new_right_x_loc ); 
            if (debug_statements) System.out.println( "new_down_x_loc: "+ new_down_y_loc ); 
            if (debug_statements) System.out.println( "preferred width: " + pref_width ); 
            if (debug_statements) System.out.println( "preferred height: "+ pref_height ); 
            if (debug_statements) System.out.println("viewport x: " + containing_panel.getVisibleRect().x );
            if (debug_statements) System.out.println("viewport y: " + containing_panel.getVisibleRect().x );
            if (debug_statements) System.out.println("viewport width: " + containing_panel.getVisibleRect().width );
            if (debug_statements) System.out.println("viewport height: " + containing_panel.getVisibleRect().height );
            if (debug_statements) System.out.println("Zoomedx: " + getZoomedX() );
            if (debug_statements) System.out.println("Zoomedy: " + getZoomedY() );
            if (debug_statements) System.out.println("Zoomedwidth: " + getZoomedWidth() );
            if (debug_statements) System.out.println("Zoomedheight: " + getZoomedHeight() );
            if (debug_statements) System.out.println("x: " + getX() );
            if (debug_statements) System.out.println("y: " + getY() );
            if (debug_statements) System.out.println("width: " + getWidth() );
            if (debug_statements) System.out.println("height: " + getHeight() );
            
                if ( getZoomedX()+getZoomedWidth() > containing_panel.getVisibleRect().x + containing_panel.getVisibleRect().width ) 
                {
                    //pref_width = new_right_x_loc;
                    containing_panel.setPreferredSize( new Dimension( getZoomedX()+getZoomedWidth(), containing_panel.getPreferredSize().height ) );
                    Rectangle r = new Rectangle(getZoomedX(), getZoomedY(), getZoomedWidth(), getZoomedHeight() );
                    if (debug_statements) System.out.println("Scrolling to Visible: " + getZoomedX() +","+ getZoomedY() +","+ getZoomedWidth() +","+ getZoomedHeight() );
                    containing_panel.scrollRectToVisible(r);
                    containing_panel.revalidate();
                }
                if ( getZoomedY()+getZoomedHeight() > containing_panel.getVisibleRect().y + containing_panel.getVisibleRect().height ) 
                {
                    //pref_height = new_down_y_loc;
                    
                    containing_panel.setPreferredSize( new Dimension( containing_panel.getPreferredSize().width, getZoomedY()+getZoomedHeight() ) );
                    //containing_panel.setPreferredSize( new Dimension( pref_width, pref_height ) );
                    Rectangle r = new Rectangle(getZoomedX(), getZoomedY(), getZoomedWidth(), getZoomedHeight() );
                    if (debug_statements) System.out.println("Scrolling to Visible: " + getZoomedX() +","+ getZoomedY() +","+ getZoomedWidth() +","+ getZoomedHeight() );
                    
                    containing_panel.scrollRectToVisible(r);
                    containing_panel.revalidate();
                }
 
            
                    if ( getZoomedX() < containing_panel.getVisibleRect().x )
                    {
                        //containing_panel.setPreferredSize( new Dimension( , pref_height ) );
                        
                        Rectangle r = new Rectangle(getZoomedX(), getZoomedY(), getZoomedWidth(), getZoomedHeight() );
                    
                        if (debug_statements) System.out.println("Scrolling to Visible: " + getZoomedX() +","+ getZoomedY() +","+ getZoomedWidth() +","+ getZoomedHeight() );
                    
                        containing_panel.scrollRectToVisible(r);
                        containing_panel.revalidate();
                        //pref_width += -new_left_x_loc;
                        //moveAllRight(getSelectedWidget(),-new_left_x_loc);
                    }

                     if ( getZoomedY() < containing_panel.getVisibleRect().y )
                    {
                        //containing_panel.setPreferredSize( new Dimension( pref_width, pref_height ) );
                        Rectangle r = new Rectangle(getZoomedX(), getZoomedY(), getZoomedWidth(), getZoomedHeight() );
                    
                        if (debug_statements) System.out.println("Scrolling to Visible: " + getZoomedX() +","+ getZoomedY() +","+ getZoomedWidth() +","+ getZoomedHeight() );
                    
                        containing_panel.scrollRectToVisible(r);
                        containing_panel.revalidate();
                        
                        //pref_height += -new_up_y_loc;
                        //moveAllDown(getSelectedWidget(),-new_up_y_loc);
                    }
                
                        
            
            //containing_panel.setPreferredSize( containing_panel.getZoomedUsedArea() );       
            
            // Scroll to view the new location
            
                    //repaint();
	    //if (debug_statements) System.out.println("Containing Panel: " + containing_panel );
	    containing_panel.repaint();
	}
	


	// this function calculates the pointer offset so that the container moves RELATIVE to the pointer's
	// (x,y) when dragged - not TO the pointers (x,y)
	void calculatePointerOffset( int mouse_x, int mouse_y )
	{
	    	//x_offset = Math.abs( mouse_x - getX() );
                //y_offset = Math.abs( mouse_y - getY() );
	    x_offset = mouse_x - getX();
	    y_offset = mouse_y - getY();
	    
	}

	private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException
	{
	    stream.defaultReadObject();
	    
            visible = true;
            
            if ( getFlickrLabel() != null )
            if (!( this instanceof FlickrLabel) ) getFlickrLabel().setOwner( this );
            
            if ( peers == null )
            {
                peers = new Vector();
            }
            
            index = 0;
            
	    // Images and Icons can't be serialized so recreate them when readObject called
	    // if the image_url is not null
	    //if ( image_url != null)
	//	{
//		    URL url = this.getClass().getResource(image_url);
//		    icon = new ImageIcon(url);
//		    width = icon.getIconWidth();
//		    height = icon.getIconHeight();
//		    image = icon.getImage();
//		}
	
            // Calling assignID here causes problems with subclasses
            color = Color.black;
            selected_color = Color.blue;
            unselected_color = Color.black;
            
            IDGenerator idgen = new IDGenerator();
            ID = idgen.getNextID();
            
            
	}
         
         // Queries
        public boolean isSelected()
	{
	    return selected;
	}

    
        // Gets and Sets
        
        public int getZoomedX()
	{
            WidgetPanel ct = getContainingPanel();
	    return (int)((getX()+ct.getZoomedXTranslation())*ct.getZoom());
	}

	public int getZoomedY()
	{
            WidgetPanel ct = getContainingPanel();
	    return (int)((getY()+ct.getZoomedYTranslation())*ct.getZoom());
        }
        
        public int getX()
	{
            return x;
	}

	public int getY()
	{
            return y;
	}
        
        public void setX(int new_x)
	{
            int old_x = this.x;
	    this.x = new_x; 
	}
	
	public void setY(int new_y)
	{
            int old_y = this.y;
	    this.y = new_y;
            
  	}

        public int getZoomedHeight()
	{
            WidgetPanel ct = getContainingPanel();
	    return (int)((getHeight()+ct.getZoomedYTranslation())*ct.getZoom());
        }

	public int getZoomedWidth()
	{
            WidgetPanel ct = getContainingPanel();
	    return (int)((getWidth()+ct.getZoomedXTranslation())*ct.getZoom());
	}

        public int getHeight()
        {
            return height;
        }

        public int getWidth()
        {
            return width;
        }
        
        public int getHeightWithLabel()
	{
            int label_height = 0;
            if ( label != null ) label_height = label.getHeight()*2; //*2 to account for the label offset 
            return getHeight()+label_height;
	}
 
	public int getWidthWithLabel()
	{
            int label_width = 0;
            if ( label != null ) label_width = label.getWidth(); 
            return getHeight() > label_width ? width : label_width;
	}

        
	public String getLabel()
	{
            if ( this instanceof FlickrLabel ) return ((FlickrLabel)this).getString();
	    return label.getString();
	}

	public void setHeight( int new_height )
	{
            int old_height = this.height;
            this.height = new_height;
            
            
	}

	public void setWidth( int new_width )
	{
            int old_width = this.width;
            this.width = new_width;
            
            
	}

        public void setLabel( String new_label )
        {
            setLabel( new_label, false );
        }
        
	public void setLabel( String new_label, boolean peer )
        {
            //containing_panel.removeFlickrLabel( this.label );
            if ( this.label == null )
            {
                this.label = new FlickrLabel( new_label, this, getX(), getY()+getHeight()+12, containing_panel, false );
               
            }
            else
            {
                this.label.setString( new_label );  // = new FlickrLabel( new_label, getX(), getY()+getHeight()+12, containing_panel );
            }            

            if (containing_panel != null ) containing_panel.repaint();
            
            if ( !peer )
            {
                Iterator<Widget> w_itr = getPeers().iterator();
                while ( w_itr.hasNext() )
                {
                    w_itr.next().setLabel( new_label, true );
                }
            }
	}
        
        public void setSelected( boolean new_selected_state )
	{
	 //   if (debug_statements) System.out.println("SetSelected called...");

            if ( containing_panel == null ) if (debug_statements) System.out.println("(setSelected()) NULL CONTAINING PANEL IN WIDGET OF TYPE \"" + this.getClass().getName() + "\"");
           // if ( !containing_panel.isSelected() && new_selected_state == true )
           // {
           //     if (debug_statements) System.out.println("Attempt to select widget in unselected windows ignored.");
           //     new_selected_state = false;  
           // }
            
            
            Color old_color = this.color;
            boolean old_selected_state = this.selected;
            this.selected = new_selected_state;
            
	    if ( this.selected == true )
		{
		    //if (debug_statements) System.out.println("true");
		    setColor( getSelectedColor() );
		}
	    else
		{
		    //if (debug_statements) System.out.println("false");
		    setColor( getUnselectedColor() );
		}

            
            // Schedule canvas update if there was a color change
            if ( old_color != this.color )
            {
                if (containing_panel != null ) containing_panel.repaint();
            }
	}
	
	public void setYOffset( int new_y_offset )
	{
            int old_y_offset = this.y_offset;
            this.y_offset = new_y_offset;
	    
	}

	public void setXOffset( int new_x_offset )
	{
            int old_x_offset = this.x_offset;
            this.x_offset = new_x_offset;
	    
            
	}

    public int getYOffset()
	{
	    return y_offset;
	}

	public int getXOffset()
	{
	    return x_offset;
	}

	public Color getUnselectedColor()
	{
	    return unselected_color;
	}
        
        public Color getColor()
	{
	   return color;
	}
        
        public Color getSelectedColor()
	{
	    return selected_color;
	}
        
	public void setContainingPanel( WidgetPanel containing_panel )
	{
            if (debug_statements) System.out.println("Called Widget's setContainingPanel(...)");
	
            //this.containing_panel.removeFlickrLabel( this.label );
            this.containing_panel = containing_panel;
            if (label != null ) label.setContainingPanel( containing_panel );
            //this.containing_panel.addFlickrLabel( this.label );
	}
        
        public WidgetPanel getContainingPanel()
        {
            return containing_panel;
        }
        
	public void setColor( Color new_color )
	{
            color = new_color;  
	}
        
        public void setSelectedColor( Color new_color )
	{
            selected_color = new_color;  
	}
        
        public void setUnselectedColor( Color new_color )
	{
            unselected_color = new_color;  
	}
        
    
    
   
    public Rectangle getBoundingBox() 
    {
        return new Rectangle( getX(), getY(), getWidth(), getHeight() );
    }
    
    public void actionPerformed(ActionEvent e) 
    {
        if ( e.getActionCommand().equals("Set Label") )
        {
            if ( e.getSource() instanceof JTextField )
            {
                JTextField tf = (JTextField)e.getSource();
                setLabel( tf.getText() );
            }
        }
        
    }
    
    public long getID() 
    {
        return ID;
    }
    
    public void setID( long ID ) 
    {
        this.ID = ID;
    }
    
    public void displayPopupMenu( int x, int y )
    {
        getContainingPanel().getTheGUI().setSaveNeeded( true );
    }
    
    public void assignID() 
    {
        IDGenerator idgen = new IDGenerator();
        ID = idgen.getNextID();
    }
    
    public void resetOffsets() 
    {
        setXOffset(0);
        setYOffset(0);
    }
    
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException 
    {
        return this;
    }    
    
    public DataFlavor[] getTransferDataFlavors() 
    {
        return null;
    }
    
    public boolean isDataFlavorSupported(DataFlavor flavor) 
    {
        return true;
    }
    
    public BufferedImage createImage()
    {
        BufferedImage img = new BufferedImage
        (
         getWidth()+5, 
         getHeight()+5, 
         BufferedImage.TYPE_INT_ARGB_PRE
        );
        
        Graphics2D g2 = img.createGraphics();
 
     // Make the image ghostlike
     g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));
 
     // Draw the Container onto the img
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
     paint( getX(), getY(), g2 );
     //g2.setColor( Color.BLACK );
     //g2.drawOval( 5, 5, 10, 10 );
     //g2.dispose();
     
     return img;
    }
    
    // Should be abstract but need to instantiate widgets for WidgetPanel:drop()
    public void paint(int origin_x, int origin_y, Graphics2D g2d)
    {
        
    }
    
    public void setDragStart(Point start) 
    {
        drag_start = start;
    }
    
    public Point getDragStart() 
    {
        return drag_start;
    }
    
    public FlickrLabel getFlickrLabel() 
    {
        return label;
    }
    
    public Font getFont() 
    {
        return this.font;
    }
    
    public void setFont( Font f ) 
    {
        this.font = f;
    }
    
    public void display( Component c, Graphics2D g2d ){}

    public void refreshLocation() 
    {
        resetOffsets();
        updateLocation(getX(), getY(), true);
    }
    
    public int compareTo(Object o) 
   {
        /*
       Widget w = (Widget)o;
       String this_label = getLabel();
       String other_label = w.getLabel();
       
       int result = this_label.compareTo( other_label );
       if (debug_statements) System.out.println("{Sort} Comparing " + this_label + " and " + other_label + " = " + result );
        */
        
       Widget w = (Widget)o;
       Integer this_index = getIndex();
       Integer other_index = w.getIndex();
       
       // Assumes indicies are positive
       int result = this_index.compareTo( other_index );
        
       return result;
   }
    
    public boolean isVisible() 
    {
        return this.visible;
    }
    
    public void setVisible(boolean visible) 
    {
        this.visible = visible;
        
        if ( visible == true )
        {
            if ( getContainingPanel() != null ) getContainingPanel().repaint();
        }
    }

    transient private boolean visible = true;

    private Integer index = 0;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
    
        public Vector getPeers() 
    {
        return peers;
    }

    public void setPeers(Vector peers) 
    {
        this.peers = peers;
    }

    public void removePeer(Widget w) 
    {
        w.getPeers().remove(this);
        peers.remove( w );
    }
    
    public void emptyPeers() 
    {
        // copy peers so we dont have concurrent access
        Vector peer_copy = new Vector();
        
        Iterator<Widget> w_itr = peers.iterator();
        while( w_itr.hasNext() )
        {
            peer_copy.add( w_itr.next() );
        }
        
        Iterator<Widget> copy_itr = peer_copy.iterator();
        while( copy_itr.hasNext() )
        {
            copy_itr.next().removePeer( this );
        }
    }

    public void addPeer(Widget w) 
    {
        peers.add( w );
        w.getPeers().add(this);
    }

}
    
                            
    
    
