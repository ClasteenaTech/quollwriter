package com.quollwriter.editors.ui;

import java.util.List;
import java.util.ArrayList;

import java.awt.event.*;
import java.awt.Component;
import java.awt.Dimension;
import javax.swing.*;
import javax.swing.border.*;

import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.factories.*;
import com.jgoodies.forms.layout.*;

import com.quollwriter.*;
import com.quollwriter.ui.*;
import com.quollwriter.data.*;
import com.quollwriter.data.editors.*;
import com.quollwriter.ui.components.ImagePanel;
import com.quollwriter.ui.components.ActionAdapter;
import com.quollwriter.editors.messages.*;
import com.quollwriter.editors.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.Environment.getUIString;

// Use an annotation?
//@MessageBox(class=NewProjectResponseMessage)
public class NewProjectResponseMessageBox extends MessageBox<NewProjectResponseMessage>
{

    private Box responseBox = null;

    public NewProjectResponseMessageBox (NewProjectResponseMessage mess,
                                         AbstractViewer            viewer)
    {

        super (mess,
               viewer);

    }

    public boolean isAutoDealtWith ()
    {

        return false;

    }

    public void doUpdate ()
    {

        if (this.message.isDealtWith ())
        {

            if (this.responseBox != null)
            {

                this.responseBox.setVisible (false);

            }

        }

    }

    public void doInit ()
    {

        final NewProjectResponseMessageBox _this = this;

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project: " +
                                  this.message.getForProjectId (),
                                  e);

        }

        final EditorEditor ed = this.message.getEditor ();

        ProjectEditor pe = null;

        if (proj != null)
        {

            try
            {

                pe = EditorsEnvironment.getProjectEditor (proj,
                                                          ed);

            } catch (Exception e) {

                Environment.logError ("Unable to get project editor for project: " +
                                      proj +
                                      ", editor: " +
                                      ed,
                                      e);

            }

        }

        final ProjectEditor fpe = pe;

        // Only do this if the editor is still pending.
        if ((!this.message.isDealtWith ())
            &&
            (this.message.getEditor ().isPending ())
           )
        {

            // Show the response.
            this.responseBox = new Box (BoxLayout.Y_AXIS);

            this.add (this.responseBox);

            JComponent l = UIUtils.createBoldSubHeader (getUIString (editors,messages,newprojectresponse,received,(this.message.isAccepted () ? accepted : rejected),title),
                                                        //String.format ("%s the {project}",
                                                        //               (this.message.isAccepted () ? "Accepted" : "Rejected")),
                                                        (this.message.isAccepted () ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME));

            this.responseBox.add (l);
            this.responseBox.setBorder (UIUtils.createPadding (5, 5, 0, 5));

            this.responseBox.add (this.getResponseDetails ());

            if (this.message.isAccepted ())
            {

                if ((this.message.getEditorName () != null)
                    ||
                    (this.message.getEditorAvatar () != null)
                   )
                {

                    JTextPane desc = UIUtils.createHelpTextPane (getUIString (editors,messages,newprojectresponse,labels,extra),
                                                                //"Additionally they provided the following name/avatar.",
                                                                 this.viewer);

                    this.responseBox.add (Box.createVerticalStrut (5));

                    this.responseBox.add (desc);
                    desc.setBorder (null);
                    desc.setSize (new Dimension (UIUtils.getPopupWidth () - 20,
                                                 desc.getPreferredSize ().height));

                    Box editorInfo = new Box (BoxLayout.X_AXIS);
                    editorInfo.setAlignmentX (Component.LEFT_ALIGNMENT);
                    editorInfo.setBorder (UIUtils.createPadding (5, 5, 5, 5));

                    this.responseBox.add (editorInfo);

                    if (this.message.getEditorAvatar () != null)
                    {

                        JLabel avatar = new JLabel ();

                        avatar.setAlignmentY (Component.TOP_ALIGNMENT);
                        avatar.setVerticalAlignment (SwingConstants.TOP);

                        editorInfo.add (avatar);
                        avatar.setOpaque (false);

                        avatar.setIcon (new ImageIcon (UIUtils.getScaledImage (_this.message.getEditorAvatar (),
                                                                               50)));

                        avatar.setBorder (new CompoundBorder (UIUtils.createPadding (0, 0, 0, 5),
                                                              UIUtils.createLineBorder ()));

                    }

                    if (this.message.getEditorName () != null)
                    {

                        JLabel name = new JLabel (this.message.getEditorName ());
                        editorInfo.add (name);

                        name.setBorder (null);
                        name.setAlignmentY (Component.TOP_ALIGNMENT);
                        name.setVerticalAlignment (JLabel.TOP);
                        name.setAlignmentX (Component.LEFT_ALIGNMENT);
                        name.setFont (name.getFont ().deriveFont ((float) UIUtils.getScaledFontSize (14)).deriveFont (java.awt.Font.PLAIN));

                    }

                }

            }

            JButton ok = UIUtils.createButton (getUIString (editors,messages,newprojectresponse,received,undealtwith,buttons,confirm));
            //"Ok, got it");

            ok.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        if (_this.message.isAccepted ())
                        {

                            ed.setEditorStatus (EditorEditor.EditorStatus.current);

                            if (_this.message.getEditorName () != null)
                            {

                                ed.setName (_this.message.getEditorName ());

                            }

                            if (_this.message.getEditorAvatar () != null)
                            {

                                ed.setAvatar (_this.message.getEditorAvatar ());

                            }

                            EditorsEnvironment.updateEditor (ed);

                            fpe.setStatusMessage (String.format (getUIString (editors,messages,newprojectresponse,received,editorstatus,accepted),
                                                                //"Accepted {project}: %s",
                                                                 Environment.formatDate (_this.message.getWhen ())));
                            fpe.setEditorFrom (_this.message.getWhen ());
                            fpe.setCurrent (true);
                            fpe.setStatus (ProjectEditor.Status.accepted);

                            EditorsEnvironment.updateProjectEditor (fpe);

                        } else {

                            ed.setEditorStatus (EditorEditor.EditorStatus.rejected);

                            EditorsEnvironment.updateEditor (ed);

                            if (fpe != null)
                            {

                                try
                                {

                                    EditorsEnvironment.removeProjectEditor (fpe);

                                } catch (Exception e) {

                                    Environment.logError ("Unable to remove project editor: " +
                                                          fpe,
                                                          e);

                                }

                            }

                        }

                        _this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (_this.message);

                        _this.responseBox.setVisible (false);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update editor: " +
                                              ed,
                                              e);

                        UIUtils.showErrorMessage (_this.viewer,
                                                  getUIString (editors,editor,edit,actionerror));
                                                  //"Unable to update {editor}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                }

            });

            JButton[] buts = new JButton[] { ok };

            JPanel bb = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT);
            bb.setOpaque (false);
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            bb.setBorder (UIUtils.createPadding (5, 0, 0, 0));

            this.responseBox.add (bb);

            return;

        }

        boolean accepted = this.message.isAccepted ();
        //String resMessage = this.message.getResponseMessage ();

        String iconName = (accepted ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME);

        String t = "";

        if (this.message.isSentByMe ())
        {

            //String text = "Accepted";

            if (!accepted)
            {

                t = getUIString (editors,messages,newprojectresponse,sent,rejected,title);

                //text = "Rejected";

            } else {

                t = getUIString (editors,messages,newprojectresponse,sent,LanguageStrings.accepted,title);

            }

            //message = text + " {project}";

        } else {

            //message = "{Project} accepted";

            if (!accepted)
            {

                t = getUIString (editors,messages,newprojectresponse,received,rejected,title);

                //message = "{Project} rejected";

            } else {

                t = getUIString (editors,messages,newprojectresponse,received,LanguageStrings.accepted,title);

            }

        }

        JComponent h = UIUtils.createBoldSubHeader (t,
                                                    iconName);

        this.add (h);

        this.add (this.getResponseDetails ());

        if ((this.message.isSentByMe ())
            &&
            (proj != null)
           )
        {

            JLabel viewProj = UIUtils.createClickableLabel (getUIString (editors,messages,newprojectresponse,sent,labels,clicktoview),
                                                            //"Click to view the {project}",
                                                            Environment.getIcon (Constants.VIEW_ICON_NAME,
                                                                                 Constants.ICON_CLICKABLE_LABEL),
                                                            new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    ProjectInfo proj = null;

                    try
                    {

                        proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                           Project.EDITOR_PROJECT_TYPE);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
                                              e);

                        UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                  String.format (getUIString (project,actions,openproject,openerrors,general),
                                                                 _this.message.getForProjectId (),
                                                                 getUIString (project,actions,openproject,openerrors,unspecified)));
                                                  //"Unable to open {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                    try
                    {

                        Environment.openProject (proj);

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
                                              e);

                        UIUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                  String.format (getUIString (project,actions,openproject,openerrors,general),
                                                                 _this.message.getForProjectId (),
                                                                 getUIString (project,actions,openproject,openerrors,unspecified)));
                                                  //"Unable to open {project}, please contact Quoll Writer support for assistance.");

                        return;

                    }

                }

            });

            viewProj.setBorder (UIUtils.createPadding (5, 10, 5, 5));

            this.add (viewProj);

        }

        if ((!this.message.isSentByMe ())
            &&
            (!this.message.isDealtWith ())
           )
        {

            final Box b = new Box (BoxLayout.Y_AXIS);
            b.setAlignmentX (Component.LEFT_ALIGNMENT);

            JButton ok = UIUtils.createButton (getUIString (editors,messages,newprojectresponse,received,undealtwith,buttons,confirm));
            //"Ok, got it");

            ok.addActionListener (new ActionAdapter ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    try
                    {

                        if (_this.message.isAccepted ())
                        {

                            fpe.setStatus (ProjectEditor.Status.accepted);

                            fpe.setEditorFrom (_this.message.getWhen ());
                            fpe.setCurrent (true);
                            fpe.setStatusMessage (String.format (getUIString (editors,messages,newprojectresponse,received,editorstatus,LanguageStrings.accepted),
                                                                //"Accepted {project}: %s",
                                                                 Environment.formatDate (_this.message.getWhen ())));

                            EditorsEnvironment.updateProjectEditor (fpe);

                        } else {

                            fpe.setCurrent (false);
                            fpe.setStatusMessage (String.format (getUIString (editors,messages,newprojectresponse,received,editorstatus,rejected),
                                                                //"Rejected {project}: %s",
                                                                 Environment.formatDate (_this.message.getWhen ())));

                            EditorsEnvironment.removeProjectEditor (fpe);

                        }

                        _this.message.setDealtWith (true);

                        EditorsEnvironment.updateMessage (_this.message);

                        b.setVisible (false);

                        UIUtils.resizeParent (b);

                        b.getParent ().remove (b);

                    } catch (Exception e) {

                        Environment.logError ("Unable to update message: " +
                                              _this.message,
                                              e);

                        UIUtils.showErrorMessage (_this.viewer,
                                                  getUIString (editors,messages,update,actionerror));
                                                  //"Unable to update message, please contact Quoll Writer support for assistance.");

                        return;

                    }

                }

            });

            JButton[] buts = new JButton[] { ok };

            JPanel bb = UIUtils.createButtonBar2 (buts,
                                                  Component.LEFT_ALIGNMENT);
            bb.setOpaque (false);
            bb.setAlignmentX (Component.LEFT_ALIGNMENT);
            bb.setBorder (UIUtils.createPadding (5, 10, 0, 0));

            b.add (bb);

            this.add (b);

        }

    }

    private JComponent getResponseDetails ()
    {

        final NewProjectResponseMessageBox _this = this;

        String rows = "top:p";

        String resMessage = this.message.getResponseMessage ();

        if (resMessage != null)
        {

            rows += ", 6px, top:p";

        }

        FormLayout fl = new FormLayout ("right:p, 6px, fill:100px:grow",
                                        rows);

        fl.setHonorsVisibility (true);
        PanelBuilder builder = new PanelBuilder (fl);

        CellConstraints cc = new CellConstraints ();

        int row = 1;

        builder.addLabel ("<html>" + getUIString (editors,messages,newprojectresponse,labels,project) + "</html>",
        //Environment.replaceObjectNames ("<html><i>{Project}</i></html>"),
                          cc.xy (1,
                                 row));

        ProjectInfo proj = null;

        try
        {

            proj = Environment.getProjectById (this.message.getForProjectId (),
                                               (this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

        } catch (Exception e) {

            Environment.logError ("Unable to get project: " +
                                  this.message.getForProjectId (),
                                  e);

        }

        if (proj != null)
        {

            JLabel openProj = UIUtils.createClickableLabel (proj.getName (),
                                                            null,
                                                            new ActionListener ()
            {

                public void actionPerformed (ActionEvent ev)
                {

                    ProjectInfo proj = null;

                    try
                    {

                        proj = Environment.getProjectById (_this.message.getForProjectId (),
                                                           (_this.message.isSentByMe () ? Project.EDITOR_PROJECT_TYPE : Project.NORMAL_PROJECT_TYPE));

                    } catch (Exception e) {

                        Environment.logError ("Unable to get project: " +
                                              _this.message.getForProjectId (),
                                              e);

                    }

                    if (proj != null)
                    {

                        try
                        {

                            Environment.openProject (proj);

                        } catch (Exception e) {

                            Environment.logError ("Unable to open project: " +
                                                  proj,
                                                  e);

                        }

                    }

                }

            });

            openProj.setToolTipText (getUIString (project,actions,openproject,tooltips,general));
            //Environment.replaceObjectNames ("Click to open the {project}"));

            builder.add (openProj,
                         cc.xy (3,
                                row));

        } else {

            NewProjectMessage m = (NewProjectMessage) this.message.getEditor ().getMessage (NewProjectMessage.MESSAGE_TYPE,
                                                                                            this.message.getForProjectId ());

            if (m != null)
            {

                builder.addLabel (m.getForProjectName (),
                                  cc.xy (3,
                                         row));

            }

        }

        row += 2;

        if (resMessage != null)
        {

            builder.addLabel ("<html>" + getUIString (editors,messages,newprojectresponse,labels,LanguageStrings.message) + "</html>",
                                //Environment.replaceObjectNames ("<html><i>{Message}</i></html>"),
                              cc.xy (1,
                                     row));

            JComponent nc = UIUtils.createHelpTextPane (resMessage,
                                                        this.viewer);
            nc.setBorder (null);

            builder.add (nc,
                         cc.xy (3,
                                row));

        }

        JPanel bp = builder.getPanel ();
        bp.setOpaque (false);
        bp.setAlignmentX (JComponent.LEFT_ALIGNMENT);
        bp.setAlignmentY (JComponent.TOP_ALIGNMENT);
        bp.setBorder (UIUtils.createPadding (0, 5, 0, 5));

        return bp;

    }

}
