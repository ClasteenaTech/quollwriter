package com.quollwriter.editors.ui;

import java.awt.Component;
import java.awt.Font;
import java.awt.AWTEvent;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.plaf.LayerUI;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import java.io.*;

import javax.swing.*;
import javax.swing.border.*;

import com.quollwriter.ui.*;
import com.quollwriter.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.events.*;
import com.quollwriter.editors.ui.*;
import com.quollwriter.editors.*;
import com.quollwriter.editors.ui.sidebars.*;
import com.quollwriter.editors.messages.*;
import com.quollwriter.ui.components.ActionAdapter;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

public class EditorInfoBox extends Box implements EditorChangedListener, EditorMessageListener
{

    private EditorEditor editor = null;
    private AbstractViewer viewer = null;
    private JLabel avatar = null;
    private JLabel mainName = null;
    private JLabel onlineStatus = null;
    private JLabel other = null;
    private Box details = null;
    private Box editorInfo = null;
    private JButton projectMessages = null;
    private JButton importantMessages = null;
    private JButton comments = null;
    private JButton chat = null;
    private boolean showProjectInfo = false;
    private ProjectEditor projEditor = null;
    private MessageBox pendingMessageBox = null;
    private Project proj = null;
    private boolean editorProject = false;

    public EditorInfoBox (EditorEditor   ed,
                          AbstractViewer viewer,
                          boolean        showProjectInfo)
                   throws GeneralException
    {

        super (BoxLayout.Y_AXIS);

        final EditorInfoBox _this = this;

        this.editor = ed;

        this.showProjectInfo = showProjectInfo;

        if ((this.showProjectInfo)
            &&
            (!(viewer instanceof AbstractProjectViewer))
           )
        {

           throw new IllegalArgumentException ("To show project information then a project viewer must be provided.");

        }

        if (viewer instanceof AbstractProjectViewer)
        {

            this.proj = ((AbstractProjectViewer) viewer).getProject ();

            this.editorProject = this.proj.isEditorProject ();

        }

        // Load the messages.
        EditorsEnvironment.loadMessagesForEditor (this.editor);

        // We add ourselves as a listener for editor change events however we don't ever
        // remove ourselves since, as a standard component, we don't have a fixed lifecycle.
        EditorsEnvironment.addEditorChangedListener (this);

        EditorsEnvironment.addEditorMessageListener (this);

        this.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.viewer = viewer;

        if (this.viewer instanceof AbstractProjectViewer)
        {

            this.projEditor = ((AbstractProjectViewer) this.viewer).getProject ().getProjectEditor (this.editor);

        }

        this.editorInfo = new Box (BoxLayout.X_AXIS);
        this.editorInfo.setAlignmentX (Component.LEFT_ALIGNMENT);

        JLayer infoWrapper = new JLayer<JComponent> (this.editorInfo, new LayerUI<JComponent> ()
        {

            @Override
            public void installUI(JComponent c) {
                super.installUI(c);
                // enable mouse motion events for the layer's subcomponents
                ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
            }

            @Override
            public void uninstallUI(JComponent c) {
                super.uninstallUI(c);
                // reset the layer event mask
                ((JLayer) c).setLayerEventMask(0);
            }

            @Override
            public void processMouseEvent (MouseEvent                   ev,
                                           JLayer<? extends JComponent> l)
            {

                // TODO: Check for multi-platform compatibility.
                if (ev.getID () != MouseEvent.MOUSE_RELEASED)
                {

                    return;

                }

                if (ev.getSource () instanceof JButton)
                {

                    return;

                }

                if (_this.editor.getEditorStatus () == EditorEditor.EditorStatus.pending)
                {

                    return;

                }

                if (ev.getClickCount () != 1)
                {

                    return;

                }

                if (ev.isPopupTrigger ())
                {

                    return;

                }

                // Show the editor.
                try
                {

                    _this.viewer.sendMessageToEditor (_this.editor);

                } catch (Exception e) {

                    Environment.logError ("Unable to show editor: " +
                                          _this.editor,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              getUIString (editors,LanguageStrings.editor,view,actionerror));
                                              //"Unable to show {editor}.");

                }

            }

        });

        infoWrapper.setAlignmentX (Component.LEFT_ALIGNMENT);

        this.add (infoWrapper);

        this.setOpaque (false);

        this.avatar = new JLabel ();

        this.avatar.setAlignmentY (Component.TOP_ALIGNMENT);

        this.editorInfo.add (this.avatar);
        this.avatar.setOpaque (false);

        this.avatar.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                   UIUtils.createLineBorder ()));

        final boolean pending = ed.isPending ();

        this.details = new Box (BoxLayout.Y_AXIS);
        details.setAlignmentY (Component.TOP_ALIGNMENT);

        JLabel l = new JLabel ("");
        l.setBorder (null);
        l.setVerticalAlignment (JLabel.TOP);
        l.setAlignmentX (Component.LEFT_ALIGNMENT);
        l.setFont (l.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (java.awt.Font.PLAIN));

        //l.setFont (l.getFont ().deriveFont ((float) 16).deriveFont (Font.PLAIN));
        l.setAlignmentY (Component.TOP_ALIGNMENT);
        l.setVerticalAlignment (SwingConstants.TOP);
        this.details.add (l);
        this.mainName = l;

        l = UIUtils.createInformationLabel (null);
        this.onlineStatus = l;

        UIUtils.setPadding (l, 0, 3, 0, 5);
        //this.details.add (this.onlineStatus);

        l.setVisible (false);
        //l.setAlignmentY (Component.TOP_ALIGNMENT);
        //l.setVerticalAlignment (SwingConstants.TOP);
        UIUtils.setPadding (l, 0, 3, 0, 5);

        l = UIUtils.createInformationLabel (null);
        l.setVisible (false);
        UIUtils.setPadding (l, 3, 3, 0, 5);
        this.details.add (l);

        this.other = l;

        this.projectMessages = UIUtils.createButton (Project.OBJECT_TYPE,
                                                     Constants.ICON_MENU,
                                                     "",
                                                     new ActionListener ()
                                                     {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                EditorsUIUtils.showProjectMessagesForEditor (_this.editor,
                                                                                                             (AbstractProjectViewer) _this.viewer,
                                                                                                             _this.projectMessages);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show project messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,messages,show,project,actionerror));
                                                                                          //"Unable to show {project} messages for {editor}.");

                                                            }

                                                        }

                                                     });

        this.projectMessages.setIconTextGap (2);
        this.projectMessages.setFont (this.projectMessages.getFont ().deriveFont (Font.BOLD,
                                                                                  14));

        this.importantMessages = UIUtils.createButton (Constants.ERROR_ICON_NAME,
                                                       Constants.ICON_MENU,
                                                       "",
                                                       new ActionListener ()
                                                       {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                EditorsUIUtils.showImportantMessagesForEditor (_this.editor,
                                                                                                               _this.viewer,
                                                                                                               _this.importantMessages);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show important messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,messages,show,important,actionerror));
                                                                                          //"Unable to show important messages for {editor}.");

                                                            }

                                                        }

                                                     });

        this.importantMessages.setIconTextGap (2);
        this.importantMessages.setFont (this.importantMessages.getFont ().deriveFont (Font.BOLD,
                                                                                      14));

        this.comments = UIUtils.createButton (Constants.COMMENT_ICON_NAME,
                                              Constants.ICON_MENU,
                                              "",
                                              new ActionListener ()
                                              {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {

                                                        EditorsUIUtils.showAllCommentsForEditor (_this.editor,
                                                                                                 (AbstractProjectViewer) _this.viewer,
                                                                                                 _this.comments);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to show comments for editor: " +
                                                                              _this.editor,
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  getUIString (editors,LanguageStrings.editor,view,actionerror));
                                                                                  //"Unable to show {comments} for {editor}.");

                                                    }

                                                }

                                             });

        this.comments.setIconTextGap (2);
        this.comments.setFont (this.comments.getFont ().deriveFont (Font.BOLD,
                                                                    14));

        this.chat = UIUtils.createButton (Constants.MESSAGE_ICON_NAME,
                                          Constants.ICON_MENU,
                                          "",
                                          new ActionListener ()
                                          {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    try
                                                    {

                                                        _this.viewer.sendMessageToEditor (_this.editor);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to show editor: " +
                                                                              _this.editor,
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  getUIString (editors,LanguageStrings.editor,view,actionerror));
                                                                                  //"Unable to show {editor}.");

                                                    }

                                                }

                                             });

        this.chat.setIconTextGap (2);
        this.chat.setFont (this.projectMessages.getFont ().deriveFont (Font.BOLD,
                                                                       14));

        Box statusBox = new Box (BoxLayout.X_AXIS);

        statusBox.setAlignmentX (Component.LEFT_ALIGNMENT);
        this.details.add (statusBox);

        java.util.List buts = new java.util.ArrayList ();
        buts.add (this.onlineStatus);
        buts.add (this.importantMessages);
        buts.add (this.comments);
        buts.add (this.projectMessages);
        buts.add (this.chat);

        statusBox.add (UIUtils.createButtonBar (buts));
        statusBox.add (Box.createHorizontalGlue ());

        this.editorInfo.add (this.details);

    }

    public void setShowProjectInfo (boolean v)
    {

        this.showProjectInfo = v;

        this.update ();

    }

    public boolean isShowProjectInfo ()
    {

        return this.showProjectInfo;

    }

    private boolean isShowAttentionBorder ()
    {

        final EditorInfoBox _this = this;

        // TODO: Investigate why this is needed, this is being called on closedown of QW.
        // Probably from close of link to message server.
        if (this.viewer instanceof AbstractProjectViewer)
        {

            if (((AbstractProjectViewer) this.viewer).getProject () == null)
            {

                return false;

            }

        }

        if (this.editor.isPrevious ())
        {

            return false;

        }

        return this.editor.getMessages (EditorsUIUtils.getImportantMessageFilter ()).size () > 0;

    }

    private Set<EditorMessage> getProjectComments ()
    {

        return this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                        ProjectCommentsMessage.MESSAGE_TYPE));

    }

    private Set<EditorMessage> getChatMessages ()
    {

        return this.editor.getMessages (new EditorMessageFilter ()
        {

            @Override
            public boolean accept (EditorMessage m)
            {

                if ((m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                    &&
                    (!m.isDealtWith ())
                   )
                {

                    return true;

                }

                return false;

            }

        });

    }

    private Set<EditorMessage> getProjectMessages ()
    {

        final EditorInfoBox _this = this;

        final String projId = this.proj.getId ();

        return this.editor.getMessages (new EditorMessageFilter ()
        {

            @Override
            public boolean accept (EditorMessage m)
            {

                if (!projId.equals (m.getForProjectId ()))
                {

                    return false;

                }

                if ((m.getMessageType ().equals (NewProjectMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (NewProjectResponseMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (UpdateProjectMessage.MESSAGE_TYPE))
                    ||
                    (m.getMessageType ().equals (ProjectEditStopMessage.MESSAGE_TYPE))
                   )
                {

                    return true;

                }

                return false;

            }

        });

    }

    private Set<EditorMessage> getImportantMessages ()
    {

        if (this.editor.isPrevious ())
        {

            return new HashSet ();

        }

        final EditorInfoBox _this = this;

        String _projId = "";

        if (this.proj != null)
        {

            _projId = this.proj.getId ();

        }

        final String projId = _projId;

        Set<EditorMessage> mess = this.editor.getMessages (new EditorMessageFilter ()
                                                           {

                                                                @Override
                                                                public boolean accept (EditorMessage m)
                                                                {

                                                                    if (!EditorsUIUtils.getImportantMessageFilter ().accept (m))
                                                                    {

                                                                        return false;

                                                                    }

                                                                    if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                                                                    {

                                                                        return false;

                                                                    }

                                                                    if (_this.showProjectInfo)
                                                                    {

                                                                        if (projId.equals (m.getForProjectId ()))
                                                                        {

                                                                            return false;

                                                                        }

                                                                    }

                                                                    return true;

                                                                 }

                                                            });

        return mess;

    }
    public ProjectEditor getProjectEditor ()
    {

        return this.projEditor;


    }
    public EditorEditor getEditor ()
    {

        return this.editor;

    }

    public void handleMessage (EditorMessageEvent ev)
    {

        if (ev.getEditor () == this.editor)
        {

            this.update ();

        }

    }

    public void editorChanged (EditorChangedEvent ev)
    {

        if (ev.getEditor () == this.editor)
        {

            this.update ();

        }

    }

    private void update ()
    {

        if (this.proj != null)
        {

            // TODO: Fix this.
            if (((AbstractProjectViewer) this.viewer).getProject () == null)
            {

                // We are closing down.
                return;

            }

        }

        this.onlineStatus.setVisible (false);
        this.other.setVisible (false);
        this.projectMessages.setVisible (false);
        this.importantMessages.setVisible (false);

        this.mainName.setText (this.editor.getMainName ());

        BufferedImage bi = null;

        if (this.editor.getMainAvatar () != null)
        {

            bi = UIUtils.getScaledImage (this.editor.getMainAvatar (),
                                         50);

        } else {

            bi = Environment.getNoEditorAvatarImage ();

        }

        this.avatar.setIcon (new ImageIcon (bi));

        if (this.editor.getOnlineStatus () != null)
        {

            String type = Constants.ONLINE_STATUS_ICON_NAME_PREFIX + this.editor.getOnlineStatus ().getType ();

            this.onlineStatus.setIcon (Environment.getIcon (type,
                                                            Constants.ICON_MENU_INNER));
            this.onlineStatus.setToolTipText (this.editor.getOnlineStatus ().getName ());
            //this.onlineStatus.setText (this.editor.getOnlineStatus ().getName ());
            this.onlineStatus.setText ("");
            this.onlineStatus.setVisible (true);
            this.onlineStatus.setMaximumSize (this.onlineStatus.getPreferredSize ());

        }

        if (this.pendingMessageBox != null)
        {

            this.pendingMessageBox.setVisible (false);
            this.remove (this.pendingMessageBox);

        }

        if (!this.editor.isPending ())
        {

            UIUtils.setAsButton (this.editorInfo);

            if (!this.editor.isPrevious ())
            {

                this.editorInfo.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,info,tooltip,currenteditor),
                                                               //"Click to send a message to %s, right click to see the menu",
                                                               this.editor.getMainName ()));

            } else {

                this.editorInfo.setToolTipText (getUIString (editors,LanguageStrings.editor,view,info,tooltip,previouseditor));
                //"Right click to see the menu");

            }

        } else {

            if (!this.editor.isInvitedByMe ())
            {

                this.other.setText (String.format (getUIString (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitereceived),
                                                   //"Received: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));

            } else {

                this.other.setText (String.format (getUIString (editors,LanguageStrings.editor,view,LanguageStrings.other,pendingeditor,invitesent),
                                                   //"Invited: %s",
                                                   Environment.formatDate (this.editor.getDateCreated ())));

            }

            this.other.setVisible (true);

        }

        //final String projId = this.projectViewer.getProject ().getId ();

        Set<EditorMessage> mess = this.getImportantMessages ();

        int ms = mess.size ();

        this.importantMessages.setForeground (java.awt.Color.black);

        if (ms > 0)
        {

            this.importantMessages.setForeground (java.awt.Color.red);

            this.importantMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,importantmessages,tooltip),
                                                                  //"%s new/important message%s requiring your attention, click to view them",
                                                                  Environment.formatNumber (ms)));
                                                                  //(ms == 1 ? "" : "s"),
                                                                  //(ms == 1 ? "s" : "")));

            this.importantMessages.setText (String.format ("%s",
                                                           Environment.formatNumber (ms)));

            this.importantMessages.setVisible (true);

        }
       /*
        if (this.editor.isPending ())
        {

            this.importantMessages.setVisible (false);

        }
         */

        if (this.editor.isPrevious ())
        {

            this.onlineStatus.setIcon (Environment.getIcon (Constants.ERROR_RED_ICON_NAME,
                                                            Constants.ICON_MENU_INNER));
            this.onlineStatus.setToolTipText (getUIString (editors,LanguageStrings.editor,view,previouseditor,onlinestatus,tooltip));
            //"This is a previous {contact}."));
            this.onlineStatus.setText ("");
            this.onlineStatus.setMaximumSize (this.onlineStatus.getPreferredSize ());

            this.onlineStatus.setVisible (true);

        }

        if ((this.showProjectInfo)
            &&
            ((this.projEditor != null)
             ||
             (this.editorProject)
            )
           )
        {

            if (this.projEditor != null)
            {

                this.other.setVisible (true);
                this.other.setText (Environment.replaceObjectNames (this.projEditor.getStatusMessage ()));

            }

            int undealtWithCount = 0;

            // Get undealt with messages that are not chat.
            // If there is just one then show it, otherwise show a link that will display a popup of them.
            Set<EditorMessage> projMess = this.getProjectMessages ();

            for (EditorMessage em : projMess)
            {

                if (!em.isDealtWith ())
                {

                    undealtWithCount++;

                }

            }

            int ps = projMess.size ();

            this.projectMessages.setForeground (java.awt.Color.black);

            if (undealtWithCount > 0)
            {

                this.projectMessages.setForeground (java.awt.Color.red);

                this.projectMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                    //"%s {project} message%s requiring your attention, click to view them",
                                                                    Environment.formatNumber (undealtWithCount)));
                                                                    //(undealtWithCount == 1 ? "" : "s"),
                                                                    //(undealtWithCount == 1 ? "s" : "")));

            } else {

                this.projectMessages.setToolTipText (String.format (getUIString (editors,LanguageStrings.editor,view,projecteditor,undealtwithmessagecount,tooltip),
                                                                    //"%s {project} message%s, click to view them",
                                                                    Environment.formatNumber (ps)));
                                                                    //(projMess.size () == 1 ? "" : "s"),
                                                                    //(projMess.size () == 1 ? "s" : "")));

            }

            this.projectMessages.setText (String.format ("%s",
                                                         Environment.formatNumber (ps)));

            this.projectMessages.setVisible (true);

        }

        this.comments.setVisible (false);

        if (this.showProjectInfo)
        {

            AbstractProjectViewer pv = (AbstractProjectViewer) this.viewer;

            int commCount = 0;

            if (!this.editor.isPending ())
            {

                this.comments.setVisible (true);
                this.comments.setForeground (java.awt.Color.black);

                // Get undealt with messages that are not chat.
                // If there is just one then show it, otherwise show a link that will display a popup of them.
                Set<EditorMessage> comments = this.editor.getMessages (new DefaultEditorMessageFilter (pv.getProject (),
                                                                                                       ProjectCommentsMessage.MESSAGE_TYPE));

                if (comments.size () > 0)
                {

                    int sets = comments.size ();
                    int undealtWithCount = 0;

                    for (EditorMessage m : comments)
                    {

                        if (!m.isDealtWith ())
                        {

                            undealtWithCount++;


                        }

                        ProjectCommentsMessage pcm = (ProjectCommentsMessage) m;

                        commCount += pcm.getComments ().size ();

                    }

                    if (undealtWithCount > 0)
                    {

                        this.comments.setForeground (java.awt.Color.red);

                    }

                    this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,projectcomments,(this.projEditor != null ? received : sent),tooltip),
                                                                                                //"%s {comment%s} %s %s",
                                                                                                 Environment.formatNumber (commCount),
                                                                                                 //(commCount == 1 ? "" : "s"),
                                                                                                 //(this.projEditor != null ? "from" : "sent to"),
                                                                                                 this.editor.getShortName ())));

                } else {

                    if (this.projEditor != null)
                    {

                        this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,noprojectcomments,received,tooltip),
                                                                                                    //"%s has not sent you any {comments} yet.",
                                                                                                     this.editor.getShortName ())));

                    } else {

                        this.comments.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,noprojectcomments,sent,tooltip),
                                                                                                    //"You have not sent any {comments} to %s yet.",
                                                                                                     this.editor.getShortName ())));

                    }

                }

                this.comments.setText (Environment.formatNumber (commCount));

                this.comments.setEnabled (commCount > 0);

            }

        }

        this.chat.setVisible (false);

        Set<EditorMessage> chatMessages = this.getChatMessages ();

        int chatMessagesSize = chatMessages.size ();

        if (chatMessagesSize > 0)
        {

            this.chat.setForeground (java.awt.Color.red);

            this.chat.setToolTipText (Environment.replaceObjectNames (String.format (getUIString (editors,LanguageStrings.editor,view,unreadchatmessages,tooltip),
                                                                                     //"%s unread chat message%s",
                                                                                     Environment.formatNumber (chatMessagesSize))));

            this.chat.setText (Environment.formatNumber (chatMessagesSize));

            this.chat.setVisible (true);

        }

        if (this.isShowAttentionBorder ())
        {

            this.editorInfo.setBorder (new CompoundBorder (new MatteBorder (0, 2, 0, 0, UIUtils.getColor ("#ff0000")),
                                                           UIUtils.createPadding (0, 5, 0, 0)));

        } else {

            this.editorInfo.setBorder (null);

        }

        this.validate ();
        this.repaint ();

    }

    public EditorInfoBox init ()
    {

        this.update ();

        return this;

    }

    public void addDeleteAllMessagesMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        JMenuItem mi = null;

        if (Environment.isDebugModeEnabled ())
        {

            if ((this.proj != null)
                &&
                (this.proj.getProjectEditor (_this.editor) != null)
               )
            {

                menu.add (UIUtils.createMenuItem ("Remove {project} editor [Debug option]",
                                                  Constants.DELETE_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                    @Override
                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        UIUtils.createTextInputPopup (_this.viewer,
                                                                                      "Remove {project} editor?",
                                                                                      Constants.DELETE_ICON_NAME,
                                                                                      String.format ("To remove <b>%s</b> as a {project} editor please enter <b>Yes</b> in the box below.  Note: this will also remove all {project} related message types for this {project} (project-new, project-new-response, project-update, project-edit-stop, project-comments)",
                                                                                                     _this.editor.getMainName ()),
                                                                                      "Yes, delete them",
                                                                                      Constants.CANCEL_BUTTON_LABEL_ID,
                                                                                      null,
                                                                                      UIUtils.getYesValueValidator (),
                                                                                      new ActionListener ()
                                                                                      {

                                                                                            @Override
                                                                                            public void actionPerformed (ActionEvent ev)
                                                                                            {

                                                                                                if (!_this.editor.messagesLoaded ())
                                                                                                {

                                                                                                    try
                                                                                                    {

                                                                                                        EditorsEnvironment.loadMessagesForEditor (_this.editor);

                                                                                                    } catch (Exception e) {

                                                                                                        Environment.logError ("Unable to load messages for editor: " +
                                                                                                                              _this.editor,
                                                                                                                              e);

                                                                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                                                                  "Unable to load messages for editor.");

                                                                                                        return;

                                                                                                    }

                                                                                                }

                                                                                                final Set<EditorMessage> messages = _this.editor.getMessages (new DefaultEditorMessageFilter (_this.proj,
                                                                                                                            NewProjectMessage.MESSAGE_TYPE,
                                                                                                                            NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                                                            UpdateProjectMessage.MESSAGE_TYPE,
                                                                                                                            ProjectEditStopMessage.MESSAGE_TYPE,
                                                                                                                            ProjectCommentsMessage.MESSAGE_TYPE));

                                                                                                try
                                                                                                {

                                                                                                    EditorsEnvironment.deleteMessages (messages);

                                                                                                    EditorsEnvironment.removeProjectEditor (_this.proj.getProjectEditor (_this.editor));

                                                                                                } catch (Exception e) {

                                                                                                    Environment.logError ("Unable to delete messages for editor: " +
                                                                                                                          _this.editor,
                                                                                                                          e);

                                                                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                                                                              "Unable to delete messages for editor.");

                                                                                                    return;

                                                                                                }

                                                                                                UIUtils.showMessage ((PopupsSupported) _this.viewer,
                                                                                                                     "{Project} editor removed",
                                                                                                                     "All associated {project} messages have been deleted.");

                                                                                  }

                                                                              },
                                                                              null,
                                                                              null);

                                            }

                                         }));

            }

            menu.add (UIUtils.createMenuItem ("Delete all messages for types [Debug option]",
                                              Constants.DELETE_ICON_NAME,
            new ActionListener ()
            {

                @Override
                public void actionPerformed (ActionEvent ev)
                {

                    Box b = new Box (BoxLayout.Y_AXIS);

                    Set<String> types = new LinkedHashSet ();

                    types.add (NewProjectMessage.MESSAGE_TYPE);
                    types.add (UpdateProjectMessage.MESSAGE_TYPE);
                    types.add (NewProjectResponseMessage.MESSAGE_TYPE);
                    types.add (ProjectEditStopMessage.MESSAGE_TYPE);
                    types.add (ProjectCommentsMessage.MESSAGE_TYPE);
                    types.add (InviteMessage.MESSAGE_TYPE);
                    types.add (InviteResponseMessage.MESSAGE_TYPE);
                    types.add (EditorChatMessage.MESSAGE_TYPE);
                    types.add (EditorInfoMessage.MESSAGE_TYPE);
                    types.add (EditorRemovedMessage.MESSAGE_TYPE);

                    final Map<String, JCheckBox> cbs = new HashMap ();

                    for (String t : types)
                    {

                        JCheckBox cb = UIUtils.createCheckBox (t);

                        cbs.put (t,
                                 cb);

                        b.add (cb);

                    }

                    UIUtils.showMessage (_this.viewer,
                                         "Delete types of message",
                                         b,
                                         "Delete",
                                         new ActionListener ()
                                         {

                                            @Override
                                            public void actionPerformed (ActionEvent ev)
                                            {

                                                if (!_this.editor.messagesLoaded ())
                                                {

                                                    try
                                                    {

                                                        EditorsEnvironment.loadMessagesForEditor (_this.editor);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to load messages for editor: " +
                                                                              _this.editor,
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  "Unable to load messages for editor.");

                                                        return;

                                                    }

                                                }

                                                Set<String> selTypes = new LinkedHashSet ();

                                                for (String t : cbs.keySet ())
                                                {

                                                    if (cbs.get (t).isSelected ())
                                                    {

                                                        selTypes.add (t);

                                                    }

                                                }

                                                Set<EditorMessage> toDel = _this.editor.getMessages (null,
                                                                                                     selTypes.toArray (new String[selTypes.size ()]));

                                                try
                                                {

                                                    EditorsEnvironment.deleteMessages (toDel);

                                                } catch (Exception e) {

                                                    Environment.logError ("Unable to delete messages for editor: " +
                                                                          _this.editor,
                                                                          e);

                                                    UIUtils.showErrorMessage (_this.viewer,
                                                                              "Unable to delete messages for editor.");

                                                    return;

                                                }

                                                for (EditorMessage m : toDel)
                                                {

                                                    _this.editor.removeMessage (m);

                                                }

                                                UIUtils.showMessage ((PopupsSupported) _this.viewer,
                                                                     "Selected message types deleted",
                                                                     "All message for selected types have been deleted.");

                                            }

                                         },
                                         null);

                }

            }));

        }

    }

    public void addSendMessageMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPrevious ())
        {

            return;

        }

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,sendmessage),
                                              //"Send message",
                                                Constants.MESSAGE_ICON_NAME,
                                                new ActionListener ()
                                                {

                                                   public void actionPerformed (ActionEvent ev)
                                                   {

                                                       try
                                                       {

                                                           _this.viewer.sendMessageToEditor (_this.editor);

                                                       } catch (Exception e) {

                                                           Environment.logError ("Unable to show editor: " +
                                                                                 _this.editor,
                                                                                 e);

                                                           UIUtils.showErrorMessage (_this,
                                                                                     getUIString (editors,LanguageStrings.editor,view,actionerror));
                                                                                     //"Unable to show {editor}.");

                                                       }

                                                   }

                                                }));

        }

    }

    public void addShowImportantMessagesMenuItem (JPopupMenu menu)
    {

        if (this.editor.isPrevious ())
        {

            return;

        }

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            final Set<EditorMessage> messages = this.editor.getMessages (new EditorMessageFilter ()
            {

                public boolean accept (EditorMessage m)
                {

                    if (!EditorsUIUtils.getDefaultViewableMessageFilter ().accept (m))
                    {

                        return false;

                    }

                    if (m.isDealtWith ())
                    {

                        return false;

                    }

                    if (m.getMessageType ().equals (EditorChatMessage.MESSAGE_TYPE))
                    {

                        return false;

                    }

                    return true;

                }

            });

            if (messages.size () > 0)
            {

                menu.add (UIUtils.createMenuItem (String.format (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,importantmessages),
                                                                 //"View new/important messages (%s)",
                                                                 Environment.formatNumber (messages.size ())),
                                                  Constants.ERROR_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                              EditorsUIUtils.showImportantMessagesForEditor (_this.editor,
                                                                                                             _this.viewer,
                                                                                                             null);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show project messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,LanguageStrings.messages,show,important,actionerror));
                                                                                          //"Unable to {project} messages for editor.");

                                                                return;

                                                            }

                                                        }

                                                  }));

            }

        }

    }

    public void addProjectSentAndUpdatesMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        //boolean isEditorProject = this.projectViewer.getProject ().isEditorProject ();

        if ((!pending)
            &&
            (this.showProjectInfo)
            &&
            (this.proj != null)
           )
        {

            final Set<EditorMessage> messages = this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                                                         NewProjectMessage.MESSAGE_TYPE,
                                                                                                         NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                                         UpdateProjectMessage.MESSAGE_TYPE,
                                                                                                         ProjectEditStopMessage.MESSAGE_TYPE));

            if (messages.size () > 0)
            {

                menu.add (UIUtils.createMenuItem (String.format (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,projectupdates),
                                                                //"View updates you have sent/received for this {project} (%s)",
                                                                 Environment.formatNumber (messages.size ())),
                                                  Project.OBJECT_TYPE,
                                                  new ActionListener ()
                                                  {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                EditorsUIUtils.showProjectMessagesForEditor (_this.editor,
                                                                                                             (AbstractProjectViewer) _this.viewer,
                                                                                                             null);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show project messages for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,LanguageStrings.messages,show,project,actionerror));
                                                                                          //"Unable to {project} messages for editor.")

                                                                return;

                                                            }

                                                        }

                                                  }));

            }

        }

    }

    public void addProjectsInvolvedWithMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            // Get all the projects.
            int projCount = 0;

            try
            {

                projCount = Environment.getAllProjectInfos (Project.EDITOR_PROJECT_TYPE).size ();

            } catch (Exception e) {

                Environment.logError ("Unable to get all projects",
                                      e);

            }

            if (projCount > 0)
            {

                menu.add (UIUtils.createMenuItem (String.format (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,projectsuserediting),
                                                                //"View {projects} I'm editing for %s (%s)",
                                                                 this.editor.getShortName (),
                                                                 Environment.formatNumber (projCount)),
                                                  Project.OBJECT_TYPE,
                                                  new ActionListener ()
                                                  {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                EditorsUIUtils.showProjectsUserIsEditingForEditor (_this.editor,
                                                                                                                   _this.viewer);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show projects user is editing for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,LanguageStrings.editor,showprojectscontactisediting,actionerror));
                                                                                          //String.format ("Unable to show {projects} you are editing for %s.",
                                                                                            //             _this.editor.getShortName ()));

                                                                return;

                                                            }

                                                        }

                                                  }));

            }

            final Set<EditorMessage> messages = this.editor.getMessages (new EditorMessageFilter ()
            {

                public boolean accept (EditorMessage m)
                {

                    if (m.isSentByMe ())
                    {

                        return false;

                    }

                    if (!m.getMessageType ().equals (NewProjectResponseMessage.MESSAGE_TYPE))
                    {

                        return false;

                    }

                    NewProjectResponseMessage nprm = (NewProjectResponseMessage) m;

                    if (!nprm.isAccepted ())
                    {

                        return false;

                    }

                    return true;

                }

            });

            if (messages.size () > 0)
            {

                menu.add (UIUtils.createMenuItem (String.format (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,projectscontactediting),
                                                                 //"View {projects} %s is editing for me (%s)",
                                                                 this.editor.getShortName (),
                                                                 Environment.formatNumber (messages.size ())),
                                                  Project.OBJECT_TYPE,
                                                  new ActionListener ()
                                                  {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            try
                                                            {

                                                                EditorsUIUtils.showProjectsEditorIsEditingForUser (_this.editor,
                                                                                                                   _this.viewer);

                                                            } catch (Exception e) {

                                                                Environment.logError ("Unable to show projects for editor: " +
                                                                                      _this.editor,
                                                                                      e);

                                                                UIUtils.showErrorMessage (_this.viewer,
                                                                                          getUIString (editors,LanguageStrings.editor,showprojectscontactisediting,actionerror));
                                                                                          //String.format ("Unable to show {projects} %s is editing for you.",
                                                                                            //             _this.editor.getShortName ()));

                                                                return;

                                                            }

                                                        }

                                                  }));

            }

        }

    }

    public void addShowCommentsMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPending ())
        {

            return;

        }

        if (this.proj == null)
        {

            return;

        }

        boolean isEditorProject = this.proj.isEditorProject ();

        final Set<EditorMessage> messages = this.editor.getMessages (new EditorMessageFilter ()
        {

            public boolean accept (EditorMessage m)
            {

                return ((m.getMessageType ().equals (ProjectCommentsMessage.MESSAGE_TYPE))
                        &&
                        (_this.proj.getId ().equals (m.getForProjectId ())));

            }

        });

        String suffix = (this.projEditor != null ? "received" : "sent");

        if ((isEditorProject)
            &&
            (messages.size () > 0)
           )
        {

            menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,commentssent),
                                                //String.format ("View all {comments} sent",
                                                //             suffix),
                                              Constants.COMMENT_ICON_NAME,
                                              new ActionListener ()
                                              {

                                                  public void actionPerformed (ActionEvent ev)
                                                  {

                                                    try
                                                    {

                                                        EditorsUIUtils.showAllCommentsForEditor (_this.editor,
                                                                                                 (AbstractProjectViewer) _this.viewer,
                                                                                                 null);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to show comments from editor: " +
                                                                              _this.editor,
                                                                              e);

                                                        UIUtils.showErrorMessage (_this.viewer,
                                                                                  getUIString (editors,viewcommentserror));
                                                                                  //"Unable to show {comments} from editor.");

                                                        return;

                                                    }

                                                  }

                                              }));

        } else {

            Iterator<EditorMessage> iter = messages.iterator ();

            if (messages.size () > 0)
            {

                final ProjectCommentsMessage message = (ProjectCommentsMessage) messages.iterator ().next ();

                menu.add (UIUtils.createMenuItem (String.format (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,(this.projEditor != null ? lastcommentsreceived : lastcommentssent)),
                                                                //"View last {comments} %s (%s)",
                                                                 message.getComments ().size ()),
                                                                 //suffix),
                                                  Constants.FIND_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            EditorsUIUtils.showProjectComments (message,
                                                                                                (AbstractProjectViewer) _this.viewer,
                                                                                                null);

                                                        }

                                                  }));

            }

            if (messages.size () > 1)
            {

                menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,(this.projEditor != null ? commentsreceived : commentssent)),
                                                                //"View all {comments} %s",
                                                                 //suffix),
                                                  Constants.COMMENT_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                      public void actionPerformed (ActionEvent ev)
                                                      {

                                                        try
                                                        {

                                                            EditorsUIUtils.showAllCommentsForEditor (_this.editor,
                                                                                                     (AbstractProjectViewer) _this.viewer,
                                                                                                     null);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to show comments from editor: " +
                                                                                  _this.editor,
                                                                                  e);

                                                            UIUtils.showErrorMessage (_this.viewer,
                                                                                      getUIString (editors,viewcommentserror));
                                                                                      //"Unable to show {comments} from editor.");

                                                            return;

                                                        }

                                                      }

                                                  }));

            }

        }

    }

    public void addSendOrUpdateProjectMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPrevious ())
        {

            return;

        }

        if (this.proj == null)
        {

            return;

        }

        final boolean pending = this.editor.isPending ();

        boolean isEditorProject = this.proj.isEditorProject ();

        if ((!pending)
            &&
            (!isEditorProject)
           )
        {

            if (!this.editor.messagesLoaded ())
            {

                try
                {

                    EditorsEnvironment.loadMessagesForEditor (_this.editor);

                } catch (Exception e) {

                    Environment.logError ("Unable to load messages for editor: " +
                                          _this.editor,
                                          e);

                    UIUtils.showErrorMessage (_this.viewer,
                                              getUIString (editors,LanguageStrings.editor,view,actionerror));
                                              //"Unable to load messages for editor.");

                    return;

                }

            }

            // Find out what was the last project message sent.
            Set<EditorMessage> messages = this.editor.getMessages (new DefaultEditorMessageFilter (this.proj,
                                                                                                   NewProjectMessage.MESSAGE_TYPE,
                                                                                                   NewProjectResponseMessage.MESSAGE_TYPE,
                                                                                                   ProjectEditStopMessage.MESSAGE_TYPE,
                                                                                                   UpdateProjectMessage.MESSAGE_TYPE));

            EditorMessage last = null;

            for (EditorMessage m : messages)
            {

                last = m;

            }

            boolean addSend = false;
            boolean addUpdate = false;

            if ((last == null)
                ||
                (last instanceof ProjectEditStopMessage)
               )
            {

                addSend = true;

            }

            if (last instanceof NewProjectMessage)
            {

                // Sent the project.  Do nothing since we have no response.
                addSend = true;
                //return;

            }

            if (last instanceof NewProjectResponseMessage)
            {

                NewProjectResponseMessage npr = (NewProjectResponseMessage) last;

                if (!npr.isAccepted ())
                {

                    addSend = true;

                } else {

                    addUpdate = true;

                }

            }

            if (last instanceof UpdateProjectMessage)
            {

                addUpdate = true;

            }

            if (addSend)
            {

                menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,sendproject),
                                                //"Send {project}/{chapters}",
                                                  Constants.SEND_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        EditorsUIUtils.showSendProject ((AbstractProjectViewer) _this.viewer,
                                                                                        _this.editor,
                                                                                        null);

                                                    }

                                                  }));

                return;

            }

            if (addUpdate)
            {

                menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,sendupdateproject),
                                                //"Update {project}/{chapters}",
                                                  Constants.SEND_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        EditorsUIUtils.showUpdateProject ((AbstractProjectViewer) _this.viewer,
                                                                                          _this.editor,
                                                                                          null);

                                                    }

                                                  }));

            } else {

                menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,sendproject),
                                                 //"Send {project}/{chapters}",
                                                  Constants.SEND_ICON_NAME,
                                                  new ActionListener ()
                                                  {

                                                    public void actionPerformed (ActionEvent ev)
                                                    {

                                                        EditorsUIUtils.showSendProject ((AbstractProjectViewer) _this.viewer,
                                                                                        _this.editor,
                                                                                        null);

                                                    }

                                                  }));

            }

        }

    }

    public void addUpdateEditorInfoMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,updatecontactinfo),
                                            //"Update the {contact} information",
                                              Constants.EDIT_ICON_NAME,
                                              new ActionListener ()
                                              {

                                                public void actionPerformed (ActionEvent ev)
                                                {

                                                    EditorsUIUtils.updateEditorInfo (_this.viewer,
                                                                                     _this.editor);

                                                }

                                              },
                                              null,
                                              null));

        }

    }

    /**
     * Add a mouse listener to the content, because the JLayer intercepts the mouse events we need to channel the
     * listener add to the actual content component.
     *
     * TODO: Make this nicer somehow, and add removeMouseListener.
     */
    @Override
    public void addMouseListener (MouseListener m)
    {

        this.editorInfo.addMouseListener (m);

    }

    public void addRemoveEditorMenuItem (JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        if (this.editor.isPrevious ())
        {

            return;

        }

        menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,removecontact),
                                        //"Remove {contact}",
                                            Constants.DELETE_ICON_NAME,
                                            new ActionListener ()
                                            {

                                               public void actionPerformed (ActionEvent ev)
                                               {

                                                   EditorsUIUtils.showRemoveEditor (_this.viewer,
                                                                                    _this.editor,
                                                                                    null);

                                               }

                                           }));

    }

    public void addShowAllMessagesMenuItem (final JPopupMenu menu)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        menu.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,allmessages),
                                        //"View ALL messages sent/received",
                                          Constants.FIND_ICON_NAME,
                                          new ActionListener ()
                                          {

                                              public void actionPerformed (ActionEvent ev)
                                              {

                                                   EditorsUIUtils.showAllMessagesForEditor (_this.editor,
                                                                                            _this.viewer,
                                                                                            null);

                                              }

                                          }));

    }

/*
    public void addSearchMessagesMenuItem (final JPopupMenu  menu,
                                           final EditorPanel panel)
    {

        final EditorInfoBox _this = this;

        final boolean pending = this.editor.isPending ();

        if (!pending)
        {

            menu.add (UIUtils.createMenuItem ("Search messages",
                                                Constants.FIND_ICON_NAME,
                                                new ActionListener ()
                                                {

                                                   public void actionPerformed (ActionEvent ev)
                                                   {

                                                       panel.showSearch ();

                                                   }

                                                }));

        }

    }
*/
    public void addFullPopupListener ()
    {

        final EditorInfoBox _this = this;

        this.editorInfo.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                _this.addSendMessageMenuItem (m);

                _this.addSendOrUpdateProjectMenuItem (m);

                _this.addShowImportantMessagesMenuItem (m);

                //_this.addShowCommentsMenuItem (m);

                //_this.addProjectsInvolvedWithMenuItem (m);

                //_this.addProjectSentAndUpdatesMenuItem (m);

/*
                infBox.addSearchMessagesMenuItem (m,
                                                  _this);
  */

                if (_this.editor.isPending ())
                {

                    m.add (UIUtils.createMenuItem (getUIString (editors,LanguageStrings.editor,view,popupmenu,items,resendinvite),
                                                    //"Resend Invite",
                                                   Constants.NOTIFY_ICON_NAME,
                                                   new ActionListener ()
                                                   {

                                                        public void actionPerformed (ActionEvent ev)
                                                        {

                                                            EditorsEnvironment.sendInvite (_this.editor.getEmail ());

                                                        }

                                                    }));

                }

                _this.addShowAllMessagesMenuItem (m);

                _this.addUpdateEditorInfoMenuItem (m);

                _this.addRemoveEditorMenuItem (m);

                _this.addDeleteAllMessagesMenuItem (m);

            }

        });

    }

    public void addBasicPopupListener ()
    {

        final EditorInfoBox _this = this;

        this.editorInfo.addMouseListener (new MouseEventHandler ()
        {

            @Override
            public void fillPopup (JPopupMenu m,
                                   MouseEvent ev)
            {

                _this.addDeleteAllMessagesMenuItem (m);

                _this.addSendMessageMenuItem (m);

                _this.addUpdateEditorInfoMenuItem (m);

            }

        });

    }

}
