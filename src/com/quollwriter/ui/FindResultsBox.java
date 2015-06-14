package com.quollwriter.ui;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

import com.quollwriter.data.*;

public abstract class FindResultsBox extends ProjectObjectsAccordionItem
{

    protected int count = 0;
    
    public FindResultsBox (String                title,
                           String                iconType,
                           String                forObjType,
                           AbstractProjectViewer viewer)
    {
                                            
        super (title,
               iconType,
               forObjType,
               viewer);
                                            
    }
        
    public boolean showItemCountOnHeader ()
    {
        
        return true;
        
    }
    
    public int getItemCount ()
    {
        
        return this.count;
        
    }

    public void reloadTree (JTree tree)
    {
        
    }        
                
    public void fillTreePopupMenu (JPopupMenu m,
                                   MouseEvent ev)
    {
        
    }
    
    public TreeCellEditor getTreeCellEditor (AbstractProjectViewer pv,
                                             JTree                 tree)
    {
        
        return null;
        
    }
    
    public int getViewObjectClickCount (Object d)
    {
        
        return 1;
        
    }
    
    public boolean isTreeEditable ()
    {
        
        return false;
        
    }
    
    public boolean isDragEnabled ()
    {
        
        return false;
        
    }
    
    public DragActionHandler getTreeDragActionHandler (AbstractProjectViewer pv,
                                                       JTree                 tree)
    {
        
        return null;
        
    }
    
    public void clearResults ()
    {
        
        ((DefaultTreeModel) this.tree.getModel ()).setRoot (null);
        
        this.setVisible (false);
        
    }

}