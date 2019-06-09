package com.quollwriter.ui.fx.viewers;

import java.util.*;
import java.util.function.*;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.Node;
import javafx.scene.control.*;

import com.quollwriter.data.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.*;
import com.quollwriter.ui.fx.panels.*;
import com.quollwriter.ui.fx.sidebars.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.charts.*;
import com.quollwriter.ui.fx.popups.*;

import static com.quollwriter.LanguageStrings.*;
import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;

public class ProjectViewer extends AbstractProjectViewer
{

    public static final String VIEWER_STATE_ID = "project.state";

    private ProjectSideBar sidebar = null;

    public interface CommandId extends AbstractProjectViewer.CommandId
    {

        String newscene = "newscene";
        String editscene = "editscene";
        String viewscene = "viewscene";
        String deletescene = "deletescene";
        String newoutlineitem = "newoutlineitem";
        String editoutlineitem = "editoutlineitem";
        String viewoutlineitem = "viewoutlineitem";
        String deleteoutlineitem = "deleteoutlineitem";
        String newasset = "newasset";
        String editasset = "editasset";
        String viewasset = "viewasset";
        String deleteasset = "deleteasset";
        String ideaboard = "ideaboard";
        String togglespellchecking = "togglespellchecking";
        String newchapter = "newchapter";
        String deletechapter = "deletechapter";
        String showwordcounts = "showwordcounts";
        String showchapterinfo = "showchapterinfo";
        String newnote = "newnote";
        String neweditneedednote = "neweditneedednote";
        String createbackup = "createbackup";
        String exportproject = "exportproject";
        String closeproject = "closeproject";
        String deleteproject = "deleteproject";
        String openproject = "openproject";
        String renameproject = "renameproject";

    }

    public ProjectViewer ()
    {

        this.sidebar = new ProjectSideBar (this);

        Environment.tagsProperty ().addListener ((SetChangeListener<Tag>) ev ->
        {

            Tag t = ev.getElementRemoved ();

            if (t != null)
            {

                this.removeTag (t);

            }

        });

        this.initActionMappings ();

    }

    private void initActionMappings ()
    {

        this.addActionMapping (() ->
        {

            Environment.showAllProjectsViewer ();

        },
        CommandId.openproject);

        this.addActionMapping (() ->
        {

            QuollPopup qp = this.getPopupById (RenameProjectPopup.POPUP_ID);

            if (qp != null)
            {

                qp.toFront ();
                return;

            }

            new RenameProjectPopup (this,
                                    this.getProject ()).show ();

        },
        CommandId.renameproject);

        this.addActionMapping (() ->
        {

            this.toggleSpellChecking ();

        },
        CommandId.togglespellchecking);

        this.addActionMapping (new ProjectViewerCommand<NamedObject> (this,
                                                                      (viewer, objs) ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No object provided.");

            }

            NamedObject o = (NamedObject) objs[0];

            // TODO
            if (o instanceof Chapter)
            {

                this.printChapter ((Chapter) o);

            }

        },
        CommandId.print));

        this.addActionMapping (new ProjectViewerCommand (this,
                                                         (viewer, objs) ->
        {

            if ((objs == null)
                ||
                (objs.length == 0)
               )
            {

                throw new IllegalArgumentException ("No chapter provided.");

            }

            Chapter c = (Chapter) objs[0];

            SideBar sb = viewer.getSideBarById (ChapterInformationSideBar.getSideBarIdForChapter (c));

            if (sb != null)
            {

                viewer.showSideBar (sb);

                return;

            }

            ChapterInformationSideBar csb = new ChapterInformationSideBar (viewer,
                                                                           c);

            viewer.addSideBar (csb);

            viewer.showSideBar (csb.getSideBar ());

        },
        CommandId.showchapterinfo));

        ProjectViewerCommand<ChapterItem> f = new ProjectViewerCommand<> (this,
                                                                          (viewer, objs) ->
                                                                          {

                                                                                if ((objs == null)
                                                                                    ||
                                                                                    (objs.length == 0)
                                                                                   )
                                                                                {

                                                                                    throw new IllegalArgumentException ("No chapter item provided.");

                                                                                }

                                                                                ChapterItem ci = (ChapterItem) objs[0];

                                                                                viewer.editChapter (ci.getChapter (),
                                                                                                    () ->
                                                                                                    {

                                                                                                        viewer.getEditorForChapter (ci.getChapter ()).editItem (ci);

                                                                                                   });

                                                                          },
                                                                          CommandId.editscene,
                                                                          CommandId.editoutlineitem);

        this.addActionMapping (f);

    }

    public void runCommand (String        id,
                            DataObject... context)
    {

        Command c = this.getActionMapping (id);

        if (c == null)
        {

            throw new IllegalArgumentException ("Unable to find command with id: " + id);

        }

        if (c instanceof ProjectViewerCommand)
        {

            ProjectViewerCommand pvc = (ProjectViewerCommand) c;

            pvc.run (context);

        }

    }

    @Override
    public void init (State s)
               throws GeneralException
    {

        this.setMainSideBar (this.sidebar);

        // We do this last because the sidebars will be restored by the super.
        super.init (s);

    }

    @Override
    public void openPanelForId (String id)
                         throws GeneralException    
    {

        super.openPanelForId (id);

    }

    @Override
    public void handleNewProject ()
                           throws Exception
    {

        Book b = this.project.getBooks ().get (0);

        Chapter c = b.getFirstChapter ();

        // Create a new chapter for the book.
        if (c == null)
        {

            c = new Chapter (b,
                             Environment.getDefaultChapterName ());

            b.addChapter (c);

        }

        this.saveObject (c,
                         true);

        // Refresh the chapter tree.
        // TODO Needed? this.reloadTreeForObjectType (c.getObjectType ());

        this.handleOpenProject ();

        this.editChapter (c);

    }

    @Override
    public void handleOpenProject ()
    {

        //this.initProjectItemBoxes ();

		final ProjectViewer _this = this;

		// Called whenever a note type is changed.
        /*
         TODO
		this.noteTypePropChangedListener = new PropertyChangedListener ()
		{

			@Override
			public void propertyChanged (PropertyChangedEvent ev)
			{

				if (ev.getChangeType ().equals (UserPropertyHandler.VALUE_CHANGED))
				{

					java.util.List<Note> toSave = new ArrayList ();

					Set<Note> objs = _this.getAllNotes ();

					for (Note o : objs)
					{

						if (o.getType ().equals ((String) ev.getOldValue ()))
						{

							o.setType ((String) ev.getNewValue ());

							toSave.add (o);

						}

						if (toSave.size () > 0)
						{

							try
							{

								_this.saveObjects (toSave,
												   true);

							} catch (Exception e)
							{

								Environment.logError ("Unable to save notes: " +
													  toSave +
													  " with new type: " +
													  ev.getNewValue (),
													  e);
// TODO: Language string
								UIUtils.showErrorMessage (_this,
														  "Unable to change type");

							}

						}

					}

					_this.reloadTreeForObjectType (Note.OBJECT_TYPE);

				}

			}

		};
*/
		// TODO Environment.getUserPropertyHandler (Constants.NOTE_TYPES_PROPERTY_NAME).addPropertyChangedListener (this.noteTypePropChangedListener);

		// TODO this.scheduleUpdateAppearsInChaptersTree ();

    }

    @Override
    public Supplier<Set<MenuItem>> getSettingsMenuSupplier ()
    {

        final ProjectViewer _this = this;

        return new Supplier<> ()
        {

            @Override
            public Set<MenuItem> get ()
            {

                Set<MenuItem> items = new LinkedHashSet<> ();

                java.util.List<String> prefix = Arrays.asList (LanguageStrings.project,settingsmenu,LanguageStrings.items);

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,openproject))
                    .styleClassName (StyleClassNames.OPEN)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.openproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,newproject))
                    .styleClassName (StyleClassNames.NEW)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.newproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,renameproject))
                    .styleClassName (StyleClassNames.RENAME)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.renameproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,statistics))
                    .styleClassName (StyleClassNames.STATISTICS)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.statistics);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,targets))
                    .styleClassName (StyleClassNames.TARGETS)
                    .onAction (ev ->
                    {

                        _this.runCommand (AllProjectsViewer.CommandId.targets);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,createbackup))
                    .styleClassName (StyleClassNames.CREATEBACKUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.createbackup);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,closeproject))
                    .styleClassName (StyleClassNames.CLOSE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.closeproject);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,deleteproject))
                    .styleClassName (StyleClassNames.DELETE)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.deleteproject);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,ideaboard))
                    .styleClassName (StyleClassNames.IDEABOARD)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.ideaboard);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,dowarmup))
                    .styleClassName (StyleClassNames.WARMUP)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.warmup);

                    })
                    .build ());

                items.add (new SeparatorMenuItem ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,importfileorproject))
                    .styleClassName (StyleClassNames.IMPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.importfile);

                    })
                    .build ());

                items.add (QuollMenuItem.builder ()
                    .label (Utils.newList (prefix,exportproject))
                    .styleClassName (StyleClassNames.EXPORT)
                    .onAction (ev ->
                    {

                        _this.runCommand (CommandId.exportproject);

                    })
                    .build ());

                return items;

            }

        };

    }

    @Override
    public Supplier<Set<Node>> getTitleHeaderControlsSupplier ()
    {

        Set<Node> pcons = super.getTitleHeaderControlsSupplier ().get ();

        return () ->
        {

            List<String> prefix = Arrays.asList (LanguageStrings.project, LanguageStrings.title,toolbar,buttons);

            Set<Node> controls = new LinkedHashSet<> ();

            controls.add (QuollButton.builder ()
                .tooltip (prefix,ideaboard,tooltip)
                .styleClassName (StyleClassNames.IDEABOARD)
                .onAction (ev ->
                {

                    this.runCommand (CommandId.ideaboard);

                })
                .build ());

            controls.addAll (pcons);

            return controls;

        };

    }

    public void viewObject (DataObject d)
    {

        if (d == null)
        {

            return;

        }

        this.viewObject (d,
                         null);

    }

    // TODO
    public void editObject (DataObject d)
    {

        // TODO

    }

    public void renameChapter (Chapter c)
    {

        // TODO

    }

    public void addNewChapterBelow (Chapter addBelow)
    {

        // TODO

    }

    public boolean isEditing (Chapter c)
    {

        return this.getEditorForChapter (c) != null;

    }

    @Override
    public ProjectChapterEditorPanelContent getEditorForChapter (Chapter c)
    {

        NamedObjectPanelContent p = this.getPanelForObject (c);

        if (p instanceof ProjectChapterEditorPanelContent)
        {

            return (ProjectChapterEditorPanelContent) p;

        }

        return null;

    }

    public void viewObject (final DataObject d,
                            final Runnable   doAfterView)
    {

        final ProjectViewer _this = this;

        if (d instanceof ChapterItem)
        {

            final ChapterItem ci = (ChapterItem) d;

            this.viewObject (ci.getChapter (),
                             () -> _this.getEditorForChapter (ci.getChapter ()).showItem (ci));

            return;

        }

        if (d instanceof Chapter)
        {

            Chapter c = (Chapter) d;

            if (d.getObjectType ().equals (Chapter.INFORMATION_OBJECT_TYPE))
            {

                try
                {

                    this.viewChapterInformation (c,
                                                 doAfterView);

                } catch (Exception e) {

                    Environment.logError ("Unable to view chapter information for chapter: " +
                                          c,
                                          e);

                    ComponentUtils.showErrorMessage (_this,
                                                     getUILanguageStringProperty (LanguageStrings.project,actions,viewchapterinformation,actionerror));
                                              //"Unable to show chapter information.");

                }

            } else
            {

                this.editChapter (c,
                                  doAfterView);

            }

            return;

        }

        if (d instanceof Asset)
        {

            this.viewAsset ((Asset) d,
                            doAfterView);

        }
/*
        if (d instanceof Note)
        {

            this.viewNote ((Note) d);

            return true;

        }
        */
/*
        if (d instanceof OutlineItem)
        {

            this.viewOutlineItem ((OutlineItem) d);

            return true;

        }
*/
        // Record the error, then ignore.
        // TODO throw new GeneralException ("Unable to open object");

    }

    public void editChapter (final Chapter  c)
    {

        this.editChapter (c,
                          null);

    }

    public void editChapter (final Chapter  c,
                             final Runnable doAfterView)
    {

        String pid = ProjectChapterEditorPanelContent.getPanelIdForChapter (c);

        if (this.showPanel (pid))
        {

            UIUtils.runLater (doAfterView);

            return;

        }

        try
        {

            ProjectChapterEditorPanelContent p = new ProjectChapterEditorPanelContent (this,
                                                                                       c);

            // TODO
            p.init (null);

            this.addPanel (p);

            this.editChapter (c,
                              doAfterView);

        } catch (Exception e) {

            Environment.logError ("Unable to edit chapter: " +
                                  c,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,editchapter,actionerror),
                                                                          c.getName ()));

        }

    }

    /**
     * This is a top-level action so it can handle showing the user a message, it returns a boolean to indicate
     * whether the chapter information is viewed.
     */
    public void viewChapterInformation (final Chapter c,
                                        final Runnable doAfterView)
                                 throws GeneralException
    {

        String sbid = ChapterInformationSideBar.getSideBarIdForChapter (c);
        SideBar sb = this.getSideBarById (sbid);

        if (sb == null)
        {

            ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                          c);

            this.addSideBar (cb);

        }

        this.showSideBar (sbid,
                          doAfterView);

    }

    public void viewAsset (final Asset    a,
                           final Runnable doAfterView)
    {

        NamedObjectPanelContent p = this.getPanelForObject (a);

        if (p != null)
        {

            this.setPanelVisible (p);

            if (doAfterView != null)
            {

                UIUtils.runLater (doAfterView);

            }

            return;

        }

        final ProjectViewer _this = this;

        AssetViewPanel avp = null;

        try
        {

            avp = new AssetViewPanel (this,
                                      a);

            if (doAfterView != null)
            {

                avp.readyForUseProperty ().addListener ((pv, oldv, newv) -> UIUtils.runLater (doAfterView));

            }

            // TODO Add state handling...
            avp.init (new State ());

            this.addPanel (avp.getPanel ());

        } catch (Exception e)
        {

            Environment.logError ("Unable to view asset: " +
                                  a,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,view,actionerror),
                                                                          a.getObjectTypeName (),
                                                                          a.getName ()));

            return;

        }

        // Open the tab :)
        this.viewAsset (a,
                        null);

    }

    @Override
    public SideBar getMainSideBar ()
    {

        return this.sidebar.getSideBar ();

    }

    @Override
    public String getStyleClassName ()
    {

        return StyleClassNames.PROJECT;

    }

    public void addChapterToTreeAfter (Chapter newChapter,
                                       Chapter addAfter)
    {
/*
TODO
        DefaultTreeModel model = (DefaultTreeModel) this.getChapterTree ().getModel ();

        DefaultMutableTreeNode cNode = new DefaultMutableTreeNode (newChapter);

        if (addAfter == null)
        {

            // Get the book node.
            TreePath tp = UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                            newChapter.getBook ());

            if (tp != null)
            {

                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent ();

                model.insertNodeInto (cNode,
                                      (MutableTreeNode) node,
                                      0);

            } else
            {

                DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot ();

                model.insertNodeInto (cNode,
                                      root,
                                      root.getChildCount ());

            }

        } else
        {

            // Get the "addAfter" node.
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) UIUtils.getTreePathForUserObject ((DefaultMutableTreeNode) model.getRoot (),
                                                                                                     addAfter).getLastPathComponent ();

            model.insertNodeInto (cNode,
                                  (MutableTreeNode) node.getParent (),
                                  node.getParent ().getIndex (node) + 1);

        }

        this.getChapterTree ().setSelectionPath (new TreePath (cNode.getPath ()));
*/
    }

    public void openObjectSection (Asset a)
    {

        // TODO this.sideBar.setObjectsOpen (a.getUserConfigurableObjectType ().getObjectTypeId ());

    }

    public void openObjectSection (String objType)
    {

        // TODO this.sideBar.setObjectsOpen (objType);

    }

    @Override
    public void showOptions (String sect)
                      throws GeneralException
    {

    }

    /**
     * Remove the specified tag from all objects in this project.
     *
     * @param tag The tag.
     */
    public void removeTag (Tag tag)
    {

        try
        {

            // Get all objects with the tag, remove the tag.
            Set<NamedObject> objs = this.project.getAllObjectsWithTag (tag);

            for (NamedObject o : objs)
            {

                o.removeTag (tag);

            }

            this.saveObjects (new ArrayList (objs),
                              true);

        } catch (Exception e) {

            Environment.logError ("Unable to remove tag: " +
                                  tag,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,actions,removetag,actionerror));
                                      //"Unable to remove tag.");

        }

    }

    public void removeTagFromObject (NamedObject n,
                                     Tag         t)
                              throws GeneralException
    {

        n.removeTag (t);

        this.saveObject (n,
                         true);

    }

    public void deleteObject (NamedObject o,
                              boolean     deleteChildObjects)
                       throws GeneralException
    {

        if (o instanceof ChapterItem)
        {

            this.deleteChapterItem ((ChapterItem) o,
                                    deleteChildObjects,
                                    true);

            return;

        }

        this.deleteObject (o);

    }

    public void deleteObject (NamedObject o)
                       throws GeneralException
    {

        if (o instanceof Asset)
        {

            this.deleteAsset ((Asset) o);

        }

        if (o instanceof Chapter)
        {

            this.deleteChapter ((Chapter) o);

        }

        if (o instanceof ChapterItem)
        {

            this.deleteChapterItem ((ChapterItem) o,
                                    true,
                                    true);

        }

    }

    public void deleteChapterItem (ChapterItem ci,
                                   boolean     deleteChildObjects,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        if (ci.getObjectType ().equals (Scene.OBJECT_TYPE))
        {

            this.deleteScene ((Scene) ci,
                              deleteChildObjects,
                              doInTransaction);

        }

        if (ci.getObjectType ().equals (OutlineItem.OBJECT_TYPE))
        {

            this.deleteOutlineItem ((OutlineItem) ci,
                                    doInTransaction);

        }

        if (ci.getObjectType ().equals (Note.OBJECT_TYPE))
        {

            this.deleteNote ((Note) ci,
                             doInTransaction);

        }

    }

    public void deleteNote (Note    n,
                            boolean doInTransaction)
                     throws GeneralException
    {

        Set<NamedObject> otherObjects = n.getOtherObjectsInLinks ();

        NamedObject obj = n.getObject ();

        // Need to get the links, they may not be setup.
        this.setLinks (n);

        this.dBMan.deleteObject (n,
                                 false,
                                 null);

        obj.removeNote (n);

        this.fireProjectEvent (ProjectEvent.Type.note,
                               ProjectEvent.Action.delete,
                               n);

        // TODO this.refreshObjectPanels (otherObjects);

        if (obj instanceof Chapter)
        {

            ProjectChapterEditorPanelContent qep = this.getEditorForChapter ((Chapter) obj);

            if (qep != null)
            {

                // TODO qep.removeItem (n);

            }

        }

        // TODO this.reloadNoteTree ();

        // TODO this.reloadChapterTree ();

    }

    public void deleteScene (Scene   s,
                             boolean deleteOutlineItems,
                             boolean doInTransaction)
                      throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = s.getOtherObjectsInLinks ();

        java.util.List<OutlineItem> outlineItems = new ArrayList<> (s.getOutlineItems ());

        // Get the editor panel for the item.
        Chapter c = s.getChapter ();

        this.dBMan.deleteObject (s,
                                 deleteOutlineItems,
                                 null);

        c.removeScene (s);

        this.fireProjectEvent (ProjectEvent.Type.scene,
                               ProjectEvent.Action.delete,
                               s);

        // TODO this.refreshObjectPanels (otherObjects);

        ProjectChapterEditorPanelContent qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            for (OutlineItem oi : outlineItems)
            {

                if (deleteOutlineItems)
                {

                    // TODO qep.removeItem (oi);

                } else {

                    // Add the item back into the chapter.
                    c.addChapterItem (oi);

                }

            }

            // TODO qep.removeItem (s);

        }

        // TODO ? this.reloadChapterTree ();

    }

    public void deleteOutlineItem (OutlineItem it,
                                   boolean     doInTransaction)
                            throws GeneralException
    {

        java.util.Set<NamedObject> otherObjects = it.getOtherObjectsInLinks ();

        // Get the editor panel for the item.
        Chapter c = it.getChapter ();

        this.dBMan.deleteObject (it,
                                 false,
                                 null);

        c.removeOutlineItem (it);

        this.fireProjectEvent (ProjectEvent.Type.outlineitem,
                               ProjectEvent.Action.delete,
                               it);

        if (it.getScene () != null)
        {

            it.getScene ().removeOutlineItem (it);

        }

        // TODO this.refreshObjectPanels (otherObjects);

        ProjectChapterEditorPanelContent qep = this.getEditorForChapter (c);

        if (qep != null)
        {

            // TODO qep.removeItem (it);

        }

        // TODO ? this.reloadChapterTree ();

    }

    public void deleteAsset (Asset a)
    {

        // Remove the links.
        try
        {

            // Capture a list of all the object objects in the links, we then need to message
            // the linked to panel of any of those.
            Set<NamedObject> otherObjects = a.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (a,
                                     false,
                                     null);

            this.project.removeObject (a);

            this.removeWordFromDictionary (a.getName ());
                                           //"project");
            //this.removeWordFromDictionary (a.getName () + "'s",
            //                               "project");

            // TODO this.refreshObjectPanels (otherObjects);

            this.fireProjectEvent (ProjectEvent.Type.asset,
                                   ProjectEvent.Action.delete,
                                   a);

        } catch (Exception e)
        {

            Environment.logError ("Unable to remove links: " +
                                  a,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (assets,delete,actionerror),
                                                                          a.getObjectTypeName (),
                                                                          a.getName ()));
                                      //"Unable to remove " + Environment.getObjectTypeName (a));

            return;

        }

        // TODO this.reloadTreeForObjectType (a.getObjectType ());

		this.removeAllSideBarsForObject (a);

        this.removePanel (a);

    }

    public void deleteChapter (Chapter c)
    {

        try
        {

            // Remove the chapter from the book.
            java.util.Set<NamedObject> otherObjects = c.getOtherObjectsInLinks ();

            this.dBMan.deleteObject (c,
                                     false,
                                     null);

            Book b = c.getBook ();

            b.removeChapter (c);

            // TODO this.refreshObjectPanels (otherObjects);

            // See if there is a chapter information sidebar.
            this.removeSideBar ("chapterinfo-" + c.getKey ());

            this.fireProjectEvent (ProjectEvent.Type.chapter,
                                   ProjectEvent.Action.delete,
                                   c);

        } catch (Exception e)
        {

            Environment.logError ("Unable to delete chapter: " + c,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (Arrays.asList (LanguageStrings.project,actions,deletechapter,actionerror),
                                                                          c.getName ()));
                                      //"Unable to delete " +
                                      //Environment.getObjectTypeName (c));

            return;

        }

        // Inform the chapter tree about the change.
        // TODO this.reloadTreeForObjectType (c.getObjectType ());

		this.removeAllSideBarsForObject (c);

        // Remove the tab (if present).
        this.removeAllPanelsForObject (c);

        // Notify the note tree about the change.
        // We get a copy of the notes here to allow iteration.
        Set<Note> _notes = new LinkedHashSet (c.getNotes ());
        for (Note n : _notes)
        {

            try
            {

                this.deleteNote (n,
                                 false);

            } catch (Exception e)
            {

                Environment.logError ("Unable to delete note: " + n,
                                      e);

            }

        }

    }

    public void addNewChapter ()
    {

        // TODO

    }

    public void setChapterEditComplete (Chapter chapter,
                                        boolean editComplete)
    {

        try
        {

            chapter.setEditComplete (editComplete);
/*
TODO
            AbstractEditorPanel p = (AbstractEditorPanel) this.getEditorForChapter (chapter);

            int pos = 0;

            if (p != null)
            {

                pos = Utils.stripEnd (p.getEditor ().getText ()).length ();

            } else {

                String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

                pos = Utils.stripEnd (t).length ();

            }

            chapter.setEditPosition (pos);
*/
            this.saveObject (chapter,
                             false);

        } catch (Exception e) {

            Environment.logError ("Unable to set chapter edit complete: " +
                                  chapter,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,seteditcomplete,actionerror));

        }

    }

    public void setChapterEditPosition (Chapter chapter,
                                        int     textPos)
                                 throws Exception
    {
/*
TODO
        AbstractEditorPanel p = (AbstractEditorPanel) this.getEditorForChapter (chapter);

        int l = 0;

        if (p != null)
        {

            l = Utils.stripEnd (p.getEditor ().getText ()).length ();

            textPos = Math.min (textPos, l);

            // See if we are on the last line (it may be that the user is in the icon
            // column).
            Rectangle pp = p.getEditor ().modelToView (textPos);

            if (UserProperties.getAsBoolean (Constants.SET_CHAPTER_AS_EDIT_COMPLETE_WHEN_EDIT_POSITION_IS_AT_END_OF_CHAPTER_PROPERTY_NAME))
            {

                if (textPos <= l)
                {

                    Rectangle ep = p.getEditor ().modelToView (l);

                    chapter.setEditComplete ((ep.y == pp.y));

                }

            }

        } else {

            String t = (chapter.getText () != null ? chapter.getText ().getText () : "");

            l = Utils.stripEnd (t).length ();

        }

        textPos = Math.min (textPos, l);

        chapter.setEditPosition (textPos);
*/
        this.saveObject (chapter,
                         false);

    }

    public void removeChapterEditPosition (Chapter chapter)
    {

        try
        {

            chapter.setEditComplete (false);
            chapter.setEditPosition (-1);

            this.saveObject (chapter,
                             false);

        } catch (Exception e) {

            Environment.logError ("Unable to remove edit position for chapter: " +
                                  chapter,
                                  e);

            ComponentUtils.showErrorMessage (this,
                                             getUILanguageStringProperty (LanguageStrings.project,editorpanel,actions,removeeditposition,actionerror));

        }

    }

    public void viewChapterInformation (Chapter c)
    {

/*
TODO
        ChapterInformationSideBar cb = new ChapterInformationSideBar (this,
                                                                      c);

        this.addSideBar (cb);

        this.showSideBar (cb.getId ());
*/

    }

    @Override
    public State getState ()
    {

        return super.getState ();

    }

}
