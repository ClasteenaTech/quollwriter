package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.dnd.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.LinkedHashSet;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.*;
import com.quollwriter.ui.actionHandlers.*;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.ui.components.Header;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.renderers.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class MessageAccordionItem<E extends EditorMessage> extends AccordionItem
{

    protected AbstractViewer viewer = null;
    protected Box content = null;
    protected Set<MessageBox> messageBoxes = new LinkedHashSet ();
    protected Date date = null;

    public MessageAccordionItem (AbstractViewer viewer,
                                 Date           d,
                                 Set<E>         messages)
    {

        super ("");

        this.date = d;

        if (messages == null)
        {

            messages = new LinkedHashSet ();

        }

        int c = messages.size ();
              /*
        String dateName = null;

        if (Utils.isToday (d))
        {

            dateName = "Today";

        }

        if (Utils.isYesterday (d))
        {

            dateName = "Yesterday";

        }

        if (dateName == null)
        {

            dateName = Environment.formatDate (d);

        }

        this.setTitle (String.format ("%s (%s)",
                                      dateName,
                                      c));
                                      */
        this.setIconType (null);

        this.viewer = viewer;

        final MessageAccordionItem _this = this;

        this.content = new Box (BoxLayout.Y_AXIS);

        Header h = this.getHeader ();

        // TODO: Make a configurable value.
        h.setTitleColor (UIUtils.getColor ("#aaaaaa"));
        h.setFontSize (14);

        // TODO: Tidy this up.

        h.setBorder (new CompoundBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getColor ("#dddddd")),
                                                             new EmptyBorder (0, 0, 3, 0)),
                                         h.getBorder ()));

        this.content.setBorder (UIUtils.createPadding (5, 0, 10, 0));

        for (E m : messages)
        {

            this.addMessage (m);

        }

        this.content.add (Box.createVerticalGlue ());

        this.updateHeaderTitle ();

    }

    public void updateHeaderTitle ()
    {

        String dateName = null;

        if (Utils.isToday (this.date))
        {

            dateName = getUIString (times,today);
            //"Today";

        }

        if (Utils.isYesterday (this.date))
        {

            dateName = getUIString (times,yesterday);
            //"Yesterday";

        }

        if (dateName == null)
        {

            dateName = Environment.formatDate (this.date);

        }

        int c = this.getContent ().getComponentCount () - 1;

        this.setTitle (String.format (getUIString (editors,editor,view,chatmessages,title),
                                    //"%s (%s)",
                                      dateName,
                                      c));

    }

    private JLabel createLabel (String m)
    {

        JLabel l = UIUtils.createInformationLabel (m);

        l.setForeground (UIUtils.getColor ("#aaaaaa"));

        return l;

    }

    public void addMessage (E m)
    {

        JComponent mb = this.getMessageBox (m);

        if (mb == null)
        {

            return;

        }

        this.content.add (mb);

        int c = this.messageBoxes.size ();

        this.updateHeaderTitle ();

        this.validate ();
        this.repaint ();

    }

    public JComponent getMessageBox (E m)
    {

        MessageBox mb = null;

        try
        {

            mb = MessageBoxFactory.getMessageBoxInstance (m,
                                                          this.viewer);

        } catch (Exception e) {

            Environment.logError ("Unable to get message box for message: " +
                                  m,
                                  e);

            return null;

        }

        if (mb != null)
        {

            try
            {

                mb.init ();

            } catch (Exception e) {

                Environment.logError ("Unable to init message box for message: " +
                                      m,
                                      e);

                return null;

            }

        } else {

            return null;

        }

        this.messageBoxes.add (mb);

        Box b = new Box (BoxLayout.Y_AXIS);
        b.setAlignmentX (Component.LEFT_ALIGNMENT);

        Box details = new Box (BoxLayout.X_AXIS);
        details.setAlignmentX (Component.LEFT_ALIGNMENT);

        String name = m.getEditor ().getMainName ();

        if (m.isSentByMe ())
        {

            name = "Me";

        }

        details.add (this.createLabel (name));
        details.add (Box.createHorizontalGlue ());
        details.add (this.createLabel (Environment.formatTime (m.getWhen ())));

        b.add (details);

        b.add (Box.createVerticalStrut (5));

        mb.setAlignmentX (Component.LEFT_ALIGNMENT);

        b.add (mb);

        b.setBorder (new EmptyBorder (0, 0, 3, 0));

        details.setBorder (new CompoundBorder (new MatteBorder (0, 0, 1, 0, UIUtils.getColor ("#dddddd")),
                                                             new EmptyBorder (5, 0, 5, 0)));


        return b;

    }

    public JComponent getContent ()
    {

        return this.content;

    }

    public void init ()
    {

        super.init ();

    }

}
