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
//@MessageBox(messageClass=InviteResponseMessage)
public class InviteResponseMessageBox extends MessageBox<InviteResponseMessage>
{

    private Box responseBox = null;

    public InviteResponseMessageBox (InviteResponseMessage mess,
                                     AbstractViewer        viewer)
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

    }

    public void doInit ()
    {

        final InviteResponseMessageBox _this = this;

        if (!this.message.isDealtWith ())
        {

            // Show the response.
            this.responseBox = new Box (BoxLayout.Y_AXIS);

            this.add (this.responseBox);

            JComponent l = UIUtils.createBoldSubHeader (getUIString (editors,messages,inviteresponse,undealtwith,(this.message.isAccepted () ? accepted : rejected),title),
                                                        //String.format ("%s the invitation",
                                                        //               (this.message.isAccepted () ? "Accepted" : "Rejected")),
                                                        (this.message.isAccepted () ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME));

            this.responseBox.add (l);
            this.responseBox.setBorder (UIUtils.createPadding (5, 5, 0, 5));

            if (this.message.isAccepted ())
            {

                if ((this.message.getEditorName () != null)
                    ||
                    (this.message.getEditorAvatar () != null)
                   )
                {

                    JTextPane desc = UIUtils.createHelpTextPane (getUIString (editors,messages,inviteresponse,undealtwith,accepted,text),
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

            final EditorEditor ed = this.message.getEditor ();

            JButton ok = UIUtils.createButton (getUIString (editors,messages,inviteresponse,undealtwith,buttons,confirm));
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

                            // Is this response for an invite message or just out of the blue from a web service invite?
                            if (!EditorsEnvironment.hasSentMessageOfTypeToEditor (ed,
                                                                                  InviteMessage.MESSAGE_TYPE))
                            {

                                EditorsEnvironment.sendUserInformationToEditor (ed,
                                                                                null,
                                                                                null,
                                                                                null);

                            }

                        } else {

                            ed.setEditorStatus (EditorEditor.EditorStatus.rejected);

                            EditorsEnvironment.updateEditor (ed);

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
        String iconName = (accepted ? Constants.ACCEPTED_ICON_NAME : Constants.REJECTED_ICON_NAME);

        String message = getUIString (editors,messages,inviteresponse,dealtwith,LanguageStrings.accepted,title);
        //"Accepted invitation to be {an editor}";

        if (!accepted)
        {

            message = getUIString (editors,messages,inviteresponse,dealtwith,rejected,title);
            //"Rejected invitation to be {an editor}";

        }

        JComponent h = UIUtils.createBoldSubHeader (message,
                                                    iconName);

        this.add (h);

    }

}
