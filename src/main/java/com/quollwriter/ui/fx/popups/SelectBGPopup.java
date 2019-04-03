package com.quollwriter.ui.fx.popups;

import java.nio.file.*;
import java.nio.charset.*;
import java.util.*;
import java.util.concurrent.*;

import javafx.beans.property.*;
import javafx.stage.*;
import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.geometry.*;

import com.quollwriter.*;
import com.quollwriter.db.*;
import com.quollwriter.text.*;
import com.quollwriter.data.*;
import com.quollwriter.editors.*;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUILanguageStringProperty;
import static com.quollwriter.LanguageStrings.*;

public class SelectBGPopup extends PopupContent
{

    public static final String POPUP_ID = "selectbg";
    public static final double SWATCH_WIDTH = 75;
    public static final double SWATCH_HEIGHT = 75;

    private SimpleObjectProperty selectedProp = null;
    private Object origBG = null;
    private Object origSwatchSel = null;
    private FlowPane colorsPane = null;
    private FlowPane imagesPane = null;
    private BackgroundObject bgObj = null;

    public SelectBGPopup (AbstractViewer viewer,
                          Object         selected)
                   throws GeneralException
    {

        super (viewer);

        this.selectedProp = new SimpleObjectProperty ();

        final SelectBGPopup _this = this;

        this.imagesPane = new FlowPane ();

        Set<Path> userImages = UserProperties.userBGImagePathsProperty ().getValue ();

        for (Path s : userImages)
        {

            this.createUserBGImageSwatch (s,
                                          -1,
                                          this.imagesPane);

        }

        Set<String> bgImages = null;

        try
        {

            bgImages = Utils.getResourceListing (Constants.BACKGROUND_THUMB_IMGS_DIR);

        } catch (Exception e) {

            throw new GeneralException ("Unable to get bg image dir listing: " + Constants.BACKGROUND_THUMB_IMGS_DIR,
                                        e);

        }

        List<String> _bgImages = new ArrayList<> (bgImages);

        Collections.sort (_bgImages);

        bgImages = new LinkedHashSet (_bgImages);

        for (String s : bgImages)
        {

            Background bg = this.createBackground (s);

            if (bg == null)
            {

                continue;

            }

            Label swatch = this.getSwatch (bg);
            swatch.setUserData (s);

            UIUtils.setTooltip (swatch,
                                getUILanguageStringProperty (selectbackground,types,image,tooltip));
            Group g = new Group ();
            g.setUserData (s);
            g.getChildren ().add (swatch);
            this.imagesPane.getChildren ().add (g);

        }

        Set<Node> imgsHeaderCons = new LinkedHashSet<> ();

        imgsHeaderCons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.ADD)
            .tooltip (getUILanguageStringProperty (selectbackground,types,image,add,tooltip))
            .onAction (ev ->
            {

                FileChooser f = new FileChooser ();
                f.titleProperty ().bind (getUILanguageStringProperty (filechooser,image,LanguageStrings.popup,title));
                f.getExtensionFilters ().add (new FileChooser.ExtensionFilter (getUILanguageStringProperty (filechooser,image,LanguageStrings.popup,extensionfilter).getValue (), "*.jpg", "*.jpeg", "*.gif", "*.png"));

                java.io.File _f = f.showOpenDialog (this.viewer.getViewer ());

                if (_f == null)
                {

                    return;

                }

                Path fp = _f.toPath ();
                UserProperties.addUserBGImagePath (fp);

                _this.createUserBGImageSwatch (fp,
                                               0,
                                               _this.imagesPane);

                _this.selectedProp.setValue (fp);

            })
            .build ());

        AccordionItem imgsai = AccordionItem.builder ()
            .title (selectbackground,types,image,title)
            .styleClassName (StyleClassNames.IMAGES)
            .headerControls (imgsHeaderCons)
            .content (this.imagesPane)
            .build ();

        this.colorsPane = new FlowPane ();
        Set<Color> colors = new LinkedHashSet<> ();
        colors.add (Color.WHITE);
        colors.addAll (UserProperties.userColorsProperty ());
        colors.add (Color.BLACK);
        colors.stream ()
            .forEach (col ->
            {

                Region swatch = _this.getColorSwatch (col);

                if (swatch == null)
                {

                    return;

                }

                Group g = new Group ();
                g.setUserData (col);
                g.getChildren ().add (swatch);
                _this.colorsPane.getChildren ().add (g);

            });

        Set<Node> colsHeaderCons = new LinkedHashSet<> ();

        colsHeaderCons.add (QuollButton.builder ()
            .styleClassName (StyleClassNames.ADD)
            .tooltip (getUILanguageStringProperty (selectbackground,types,color,add,tooltip))
            .onAction (ev ->
            {

                Color initCol = null;

                Object sel = _this.selectedProp.getValue ();

                if (sel != null)
                {

                    if (sel instanceof Color)
                    {

                        initCol = (Color) sel;

                    } else {

                        String ss = sel.toString ();

                        if (ss.startsWith ("#"))
                        {

                            initCol = Color.web (ss);

                        }

                    }

                }

                if (initCol == null)
                {

                    initCol = Color.WHITE;

                }

                ColorChooserPopup colp = new ColorChooserPopup (_this.viewer,
                                                                initCol);
                colp.show ();
                colp.getChooser ().colorProperty ().addListener ((_p, oldv, newv) ->
                {

                    _this.selectedProp.setValue (newv);//UIUtils.colorToHex (newv));

                });
                colp.getChooser ().setOnColorSelected (eev ->
                {

                    // We use this one.
                    // Add it to the color list.
                    _this.addUserColor (colp.getChooser ().colorProperty ().getValue ());
                    colp.getPopup ().close ();

                });

                colp.getChooser ().setOnCancel (eev ->
                {

                    if (_this.origSwatchSel != null)
                    {

                        _this.selectedProp.setValue (_this.origSwatchSel);

                    } else {

                        _this.resetToOriginalBG ();

                    }

                });

                _this.addChildPopup (colp);

            })
            .build ());

        AccordionItem colsai = AccordionItem.builder ()
            .title (selectbackground,types,color,title)
            .styleClassName (StyleClassNames.COLORS)
            .headerControls (colsHeaderCons)
            .content (this.colorsPane)
            .build ();

        VBox v = new VBox ();
        v.setFillWidth (true);

        VBox.setVgrow (imgsai, Priority.ALWAYS);
        VBox.setVgrow (colsai, Priority.ALWAYS);

        v.getChildren ().addAll (imgsai, colsai);

        QScrollPane sp = new QScrollPane ();
        sp.setOutsideViewportVerticalPositionPolicy (VPos.CENTER);
        //sp.setPartiallyVisiblePolicy (QScrollPane.PartiallyVisiblePolicy.USE_POSITION_POLICY);
        sp.setContent (v);
        sp.setHbarPolicy (ScrollPane.ScrollBarPolicy.NEVER);

        this.getChildren ().addAll (sp);

        this.selectedProp.addListener ((pr, oldv, newv) ->
        {

            UIUtils.setSelected (_this.imagesPane,
                                 newv);
            UIUtils.setSelected (_this.colorsPane,
                                 newv);

            List<Node> ns = UIUtils.getSelected (_this.imagesPane);

            if (ns.size () == 0)
            {

                ns = UIUtils.getSelected (_this.colorsPane);

            }

            if (ns.size () > 0)
            {

                Node n = ns.iterator ().next ();

                sp.scrollIntoView (n);

            }

        });

        if (selected != null)
        {

            this.origBG = selected;

        }

    }

    private void createUserBGImageSwatch (Path     p,
                                          int      addAt,
                                          FlowPane parent)
    {

        final SelectBGPopup _this = this;

        Background bg = this.createBackground (p);

        if (bg == null)
        {

            return;

        }

        Group g = new Group ();
        Label swatch = this.getSwatch (bg);
        swatch.setUserData (p);
        UIUtils.setTooltip (swatch,
                            getUILanguageStringProperty (selectbackground,types,image,tooltip));

        ContextMenu cm = new ContextMenu ();

        Set<MenuItem> items = new LinkedHashSet<> ();

        items.add (QuollMenuItem.builder ()
            .label (selectbackground,popupmenu,LanguageStrings.items,remove)
            .styleClassName (StyleClassNames.DELETE)
            .onAction (eev ->
            {

                UserProperties.removeUserBGImagePath (p);

                parent.getChildren ().remove (g);

                if (p.equals (_this.selectedProp.getValue ()))
                {

                    _this.selectedProp.setValue (null);

                }

            })
            .build ());

        cm.getItems ().addAll (items);

        swatch.setContextMenu (cm);

        g.setUserData (p);
        g.getChildren ().add (swatch);

        if (addAt > -1)
        {

            parent.getChildren ().add (addAt,
                                       g);

        } else {

            parent.getChildren ().add (g);

        }

    }

    private void addUserColor (Color col)
    {

        UserProperties.addUserColor (col);

        this.colorsPane.getChildren ().add (1,
                                            this.getColorSwatch (col));

        this.selectedProp.setValue (col);

    }

    private Region getColorSwatch (Color col)
    {

        final SelectBGPopup _this = this;

        Background bg = this.createBackground (col);

        if (bg == null)
        {

            return null;

        }

        Label swatch = this.getSwatch (bg);
        swatch.setUserData (col);

        if ((col != Color.WHITE)
            &&
            (col != Color.BLACK)
           )
        {

            ContextMenu cm = new ContextMenu ();

            Set<MenuItem> items = new LinkedHashSet<> ();

            items.add (QuollMenuItem.builder ()
                .label (colorchooser,LanguageStrings.swatch,popupmenu,LanguageStrings.items,remove)
                .styleClassName (StyleClassNames.DELETE)
                .onAction (eev ->
                {

                    //UserProperties.removeUserImagePath (s);
                    UserProperties.removeUserColor (col);

                    _this.colorsPane.getChildren ().remove (swatch);

                    if (col.equals (_this.selectedProp.getValue ()))
                    {

                        _this.selectedProp.setValue (null);

                    }

                })
                .build ());

            cm.getItems ().addAll (items);

            swatch.setContextMenu (cm);

        }

        UIUtils.setTooltip (swatch,
                            getUILanguageStringProperty (selectbackground,types,color,tooltip));

        return swatch;

    }

    private void resetToOriginalBG ()
    {

        if (this.origBG instanceof Path)
        {

            if (!UserProperties.userBGImagePathsProperty ().getValue ().contains (this.origBG))
            {

                this.createUserBGImageSwatch ((Path) this.origBG,
                                              0,
                                              this.imagesPane);

            }

        }

        this.selectedProp.setValue (this.origBG);

    }

    public SimpleObjectProperty selectedProperty ()
    {

        return this.selectedProp;

    }

    private Label getSwatch (Background bg)
    {

        final SelectBGPopup _this = this;

        Label b = new Label ();
        b.getStyleClass ().add (StyleClassNames.ITEM);
        b.setBackground (bg);

        b.setOnMousePressed (ev ->
        {

            if (ev.isPrimaryButtonDown ())
            {

                _this.origSwatchSel = b.getUserData ();

                _this.selectedProp.setValue (b.getUserData ());

            }

        });

        return b;

    }

    @Override
    public QuollPopup createPopup ()
    {

        final SelectBGPopup _this = this;

        Set<Node> headerCons = new LinkedHashSet<> ();

        QuollButton b = QuollButton.builder ()
            .styleClassName (StyleClassNames.RESET)
            .tooltip (getUILanguageStringProperty (selectbackground,types,reset,tooltip))
            .onAction (ev ->
            {

                _this.resetToOriginalBG ();

            })
            .build ();

        Tooltip t = b.getTooltip ();

        Region r = new Region ();

        r.setBackground (_this.createBackground (_this.origBG));
        t.setGraphic (r);

        headerCons.add (b);

        QuollPopup p = QuollPopup.builder ()
            .title (selectbackground,title)
            .styleClassName (StyleClassNames.SELECTBG)
            .hideOnEscape (true)
            .withClose (true)
            .content (this)
            .popupId (POPUP_ID)
            .withViewer (this.viewer)
            .controls (headerCons)
            .removeOnClose (true)
            .build ();

        p.requestFocus ();

        if (this.origBG != null)
        {

            UIUtils.runLater (() ->
            {

                _this.selectedProp.setValue (_this.origBG);

            });

        }

        return p;

    }

    private Background createBackground (Object obj)
    {

        if (obj == null)
        {

            return null;

        }

        if (obj instanceof Color)
        {

            return this.createBackground ((Color) obj);

        }

        if (obj instanceof String)
        {

            return this.createBackground ((String) obj);

        }

        if (obj instanceof Path)
        {

            return this.createBackground ((Path) obj);

        }

        throw new IllegalArgumentException ("Object type: " + obj.getClass ().getName () + ", not supported.");

    }

    private Background createBackground (Color col)
    {

        return new Background (new BackgroundFill (col, null, null));

    }

    private Background createBackground (String id)
    {

        if (id.startsWith ("#"))
        {

            return new Background (new BackgroundFill (UIUtils.hexToColor (id), null, null));

        }

        Image im = Environment.getBackgroundImage (id, SWATCH_WIDTH, SWATCH_HEIGHT);

        if (im == null)
        {

            return null;

        }

        return new Background (new BackgroundImage (im,
                                                    BackgroundRepeat.NO_REPEAT,
                                                    BackgroundRepeat.NO_REPEAT,
                                                    null,
                                                    null));

    }

    private Background createBackground (Path path)
    {

        if (path == null)
        {

            return null;

        }

        if (Files.notExists (path))
        {

            return null;

        }

        if (Files.isDirectory (path))
        {

            return null;

        }

        try
        {

            Image im = new Image (Files.newInputStream (path),
                                  SWATCH_WIDTH,
                                  SWATCH_HEIGHT,
                                  false,
                                  true);

            return new Background (new BackgroundImage (im,
                                                        BackgroundRepeat.NO_REPEAT,
                                                        BackgroundRepeat.NO_REPEAT,
                                                        null,
                                                        null));

        } catch (Exception e) {

            Environment.logError ("Unable to get image for path: " + path,
                                  e);

            return null;

        }

    }

}
