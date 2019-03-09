package com.quollwriter.uistrings;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;
import java.nio.charset.*;
import java.util.stream.*;

import javafx.beans.property.*;
import javafx.beans.binding.*;

import com.gentlyweb.utils.*;

import com.quollwriter.*;
import com.quollwriter.ui.LanguageStringsEditor;
import com.quollwriter.ui.fx.*;
import com.quollwriter.ui.fx.components.*;
import com.quollwriter.ui.fx.viewers.*;

import static com.quollwriter.uistrings.UILanguageStringsManager.getUIString;
import static com.quollwriter.LanguageStrings.*;

public class UILanguageStringsManager
{

    private static UILanguageStrings uiLanguageStrings = null;
    private static UILanguageStrings defaultUILanguageStrings = null;
    private static StringProperty uilangProp = new SimpleStringProperty (uiLanguageStrings.ENGLISH_ID);

    private UILanguageStringsManager ()
    {

    }

    public static void init ()
               throws Exception
    {

        UILanguageStringsManager.defaultUILanguageStrings = new UILanguageStrings (Utils.getResourceFileAsString (Constants.DEFAULT_UI_LANGUAGE_STRINGS_FILE));

        UILanguageStringsManager.uiLanguageStrings = UILanguageStringsManager.defaultUILanguageStrings;

        // Load the user default, if appropriate.
        final String uilangid = UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME);

        if (uilangid != null)
        {

            if (!UILanguageStrings.isEnglish (uilangid))
            {

                UILanguageStrings ls = UILanguageStringsManager.getUILanguageStrings (uilangid);

                if ((ls == null)
                    ||
                    // Have we updated QW and need to get newer versions?
                    ((ls != null)
                     &&
                     (ls.getQuollWriterVersion ().isNewer (Environment.getQuollWriterVersion ()))
                    )
                   )
                {

                    // Something has gone wrong, try and download again.
                    UILanguageStringsManager.downloadUILanguageFile (uilangid,
                                                 // On complete.
                                                 () ->
                                                 {

                                                    try
                                                    {

                                                        UILanguageStringsManager.setUILanguage (uilangid);

                                                    } catch (Exception e) {

                                                        Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                              e);

                                                        ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                         UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,errors,download));
                                                                                         //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                    }

                                                    ComponentUtils.showMessage (Environment.getFocusedViewer (),
                                                                                UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,redownload,confirmpopup,title),
                                                                                //"Language strings re-downloaded",
                                                                                UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,redownload,confirmpopup,text));
                                                                                //"Quoll Writer has re-downloaded the User Interface language strings you are using because they were missing from your local system.  In the interim the User Interface has fallen back to using English.<br /><br />To return to using your selected language Quoll Writer must be restarted.",

                                                 },
                                                 // On error.
                                                 () -> {

                                                    ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                     UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,redownload,actionerror));
                                                                                     //"Warning!  Quoll Writer has been unable to re-download the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                 });

                } else {

                    UILanguageStringsManager.setUILanguage (uilangid);

                    if (!ls.isUser ())
                    {

                        // See if there is an update to the strings.
                        UILanguageStringsManager.downloadUILanguageFile (uilangid,
                                                     // On complete
                                                     () ->
                                                     {

                                                        try
                                                        {

                                                            UILanguageStringsManager.setUILanguage (uilangid);

                                                        } catch (Exception e) {

                                                            Environment.logError ("Unable to set ui language to: " + uilangid,
                                                                                  e);

                                                            ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                                                             UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,update,actionerror));
                                                                                             //"Warning!  Quoll Writer has been unable to update the User Interface strings for your selected language.  There may be multiple reasons for this, such as a connection error to the internet or that the Quoll Writer server is unavailable.<br /><br />It is recommended that you either restart Quoll Writer to try again or try downloading the strings from the Options panel.<br /><br />In the interim Quoll Writer has fallen back to using <b>English</b>.");

                                                        }

                                                        ComponentUtils.showMessage (Environment.getFocusedViewer (),
                                                                                    UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,update,confirmpopup,title),
                                                                                    //"Language strings updated",
                                                                                    UILanguageStringsManager.getUILanguageStringProperty (uilanguage,set,downloading,update,confirmpopup,text));
                                                                                    //"Quoll Writer has updated the User Interface language strings you are using because a new version was available.<br /><br />To make full use of the updated strings Quoll Writer must be restarted.",

                                                    },
                                                    // On error.
                                                    null);

                    }

                }

            }

        }

    }

    public static StringProperty uilangProperty ()
    {

        return UILanguageStringsManager.uilangProp;

    }

    public static void downloadUILanguageFile (final String   id,
                                               final Runnable onComplete,
                                               final Runnable onError)
    {

        Environment.schedule (() ->
        {

            String lastMod = "";

            UILanguageStrings ls = null;

            try
            {

                ls = UILanguageStringsManager.getUILanguageStrings (id);

            } catch (Exception e) {

                Environment.logError ("Unable to get language strings: " + id,
                                      e);

                UIUtils.runLater (onError);

                return;

            }

            if (ls != null)
            {

                Date d = ls.getLastModified ();

                if (d == null)
                {

                    d = ls.getDateCreated ();

                }

                lastMod = d.getTime () + "";

            }

            String url = UserProperties.get (Constants.QUOLL_WRITER_GET_UI_LANGUAGE_STRINGS_URL_PROPERTY_NAME);

            url = StringUtils.replaceString (url,
                                             Constants.VERSION_TAG,
                                             Environment.getQuollWriterVersion ().toString ());

            url = StringUtils.replaceString (url,
                                             Constants.ID_TAG,
                                             id);

            url = StringUtils.replaceString (url,
                                             Constants.LAST_MOD_TAG,
                                             lastMod);

            try
            {

                String data = Utils.getUrlFileAsString (new URL (Environment.getQuollWriterWebsite () + "/" + url));

                if (data.startsWith (Constants.JSON_RETURN_PREFIX))
                {

                    data = data.substring (Constants.JSON_RETURN_PREFIX.length ());

                }

                if (data.trim ().length () == 0)
                {

                    Environment.logError ("No language strings data available for: " + id + ", " + Environment.getQuollWriterVersion ());

                    UIUtils.runLater (onError);

                    return;

                }

                // Will be a collection.
                Collection col = null;

                try
                {

                    col = (Collection) JSONDecoder.decode (data);

                } catch (Exception e) {

                    Environment.logError ("Unable to decode language strings data for id: " + id + ", " + Environment.getQuollWriterVersion (),
                                          e);

                    UIUtils.runLater (onError);

                    return;

                }

                Iterator iter = col.iterator ();

                int updated = 0;

                while (iter.hasNext ())
                {

                    Map m = (Map) iter.next ();

                    String nid = (String) m.get (":id");

                    if (id == null)
                    {

                        throw new GeneralException ("No id found.");

                    }

                    updated++;

                    Path f = UILanguageStringsManager.getUILanguageStringsFilePath (nid);

                    Files.write (f, JSONEncoder.encode (m).getBytes (StandardCharsets.UTF_8));

                }

                UIUtils.runLater (onComplete);

            } catch (Exception e) {

                Environment.logError ("Unable to get user interface files for: " + id + ", " + Environment.getQuollWriterVersion (),
                                      e);

                UIUtils.runLater (onError);

            }

        },
        1 * Constants.SEC_IN_MILLIS,
        -1);

    }

    public static void setUILanguage (String id)
                               throws Exception
    {

        UILanguageStrings ls = null;

        ls = UILanguageStringsManager.getUILanguageStrings (id);

        if (ls == null)
        {

            throw new GeneralException ("No language strings found for id: " +
                                        id);

        }

        UILanguageStringsManager.uiLanguageStrings = ls;

        UserProperties.set (Constants.USER_UI_LANGUAGE_PROPERTY_NAME, id);

        UILanguageStringsManager.uilangProp.setValue (id);

    }

    public static Set<UILanguageStrings> getAllUILanguageStrings ()
                                                           throws Exception
    {

        return UILanguageStringsManager.getAllUILanguageStrings (null);

    }

    public static Set<UILanguageStrings> getAllUILanguageStrings (Version ver)
                                                           throws Exception
    {

        Path d = UILanguageStringsManager.getUILanguageStringsDirPath ();

        // Cycle down all subdirs.
        return Files.walk (d, 0)
        // Map the file to a UILanguageStrings instance.
        .map (f ->
        {

            try
            {

                UILanguageStrings ls = new UILanguageStrings (f.toFile ());

                if (ver != null)
                {

                    if (!ls.getQuollWriterVersion ().equals (ver))
                    {

                        ls = null;

                    }

                }

                return ls;

            } catch (Exception e) {

                throw new RuntimeException ("Unable to create ui language strings from: " +
                                            f,
                                            e);

            }

        })
        // Remove nulls.
        .filter (f ->
        {

            return f != null;

        })
        // Collect to a set.
        .collect (Collectors.toSet ());

/*
        File[] files = this.getUILanguageStringsDir ().listFiles ();

        if (files == null)
        {

            return ret;

        }

        for (int i = 0; i < files.length; i++)
        {

            File f = files[i];

            if (f.isFile ())
            {

                try
                {

                    UILanguageStrings ls = new UILanguageStrings (f);

                    if (ver != null)
                    {

                        if (!ls.getQuollWriterVersion ().equals (ver))
                        {

                            continue;

                        }

                    }

                    ret.add (ls);

                } catch (Exception e) {

                    Environment.logError ("Unable to create strings from: " + f,
                                          e);

                    // Delete the file.
                    f.delete ();

                }

            }

        }

        return ret;
*/
    }

    private static Path getUILanguageStringsDirPath ()
                                       throws IOException
    {

        Path d = Environment.getUserPath (Constants.UI_LANGUAGES_DIR_NAME);

        Files.createDirectories (d);

        return d;

    }

    public static Path getUILanguageStringsFilePath (String id)
                                              throws IOException
    {

        return UILanguageStringsManager.getUILanguageStringsDirPath ().resolve (id);

    }

    public static Path getUserUILanguageStringsFilePath (Version qwVersion,
                                                         String  id)
                                                  throws IOException
    {

        if (id.equals (UILanguageStrings.ENGLISH_ID))
        {

            id = id.substring (1);

        }

        return UILanguageStringsManager.getUserUILanguageStringsDirPath (qwVersion).resolve (id);

    }

    private static Path getUserUILanguageStringsDirPath (Version v)
                                                  throws IOException
    {

        Path p = Environment.getUserPath (Constants.USER_UI_LANGUAGES_DIR_NAME).resolve (v.toString ());

        Files.createDirectories (p);

        return p;

    }

    public static Path getUserUILanguageStringsFilePath (UILanguageStrings ls)
                                                  throws IOException
    {

        return UILanguageStringsManager.getUserUILanguageStringsFilePath (ls.getQuollWriterVersion (),
                                                                          ls.getId ());

    }

    public static UILanguageStrings getUILanguageStrings (String  id,
                                                          Version ver)
                                                   throws Exception
    {

        if (ver == null)
        {

            ver = Environment.getQuollWriterVersion ();

        }

        if (id.startsWith ("user-"))
        {

            id = id.substring ("user-".length ());

            return UILanguageStringsManager.getUserUILanguageStrings (ver,
                                                                      id);

        }

        if (id.equals (UILanguageStrings.ENGLISH_ID))
        {

            return UILanguageStringsManager.getDefaultUILanguageStrings ();

        }

        Path f = UILanguageStringsManager.getUILanguageStringsFilePath (id);

        if (Files.notExists (f))
        {

            return null;

        }

        String data = new String (Files.readAllBytes (f),
                                  StandardCharsets.UTF_8);

        UILanguageStrings s = new UILanguageStrings (data);

        return s;

    }

    public static UILanguageStrings getUILanguageStrings (String id)
                                                   throws Exception
    {

        return UILanguageStringsManager.getUILanguageStrings (id,
                                                              Environment.getQuollWriterVersion ());

    }

    public static Set<UILanguageStrings> getAllUserUILanguageStrings (Version qwVer)
                                                               throws Exception
    {

        Set<UILanguageStrings> ret = new LinkedHashSet<> ();

        for (UILanguageStrings ls : UILanguageStringsManager.getAllUserUILanguageStrings ())
        {

            if (ls.getQuollWriterVersion ().equals (qwVer))
            {

                ret.add (ls);

            }

        }

        return ret;

    }

    public static Set<UILanguageStrings> getAllUserUILanguageStrings ()
                                                        throws Exception
    {

        Set<UILanguageStrings> s = new TreeSet<> ();

        Path d = Environment.getUserPath (Constants.USER_UI_LANGUAGES_DIR_NAME);

        // Cycle down all subdirs.
        return Files.walk (d)
        // Map the file to a UILanguageStrings instance.
        .map (f ->
        {

            // Grr...
            try
            {

                UILanguageStrings ls = new UILanguageStrings (f.toFile ());

                if (ls.isEnglish ())
                {

                    ls = null;

                }

                return ls;

            } catch (Exception e) {

                throw new RuntimeException ("Unable to create ui language strings from: " +
                                            f,
                                            e);

            }

        })
        // Remove nulls.
        .filter (f ->
        {

            return f != null;

        })
        // Collect to a set.
        .collect (Collectors.toSet ());

    }

    private static void deleteUserUILanguageStrings (final UILanguageStrings ls)
    {

        // TODO Change to use a runnable.
        java.awt.event.ActionListener remFile = new java.awt.event.ActionListener ()
        {

            @Override
            public void actionPerformed (java.awt.event.ActionEvent ev)
            {

                try
                {

                    Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (ls.getQuollWriterVersion (),
                                                                                        ls.getId ());

                    Files.deleteIfExists (f);

                } catch (Exception e) {

                    Environment.logError (String.format ("Unable to delete user ui language strings: %s/%s",
                                                         ls.getQuollWriterVersion (),
                                                         ls.getId ()),
                                          e);

                }

            }

        };

        // TODO Change to Environment.doForOpenViewers(LanguageStringsEditor.class) ->

/*
TODO
        for (AbstractViewer v : Environment.openViewersProperty ())
        {

            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if ((lse.getUserLanguageStrings ().getId ().equals (ls.getId ()))
                    &&
                    (lse.getUserLanguageStrings ().getQuollWriterVersion ().equals (ls.getQuollWriterVersion ()))
                   )
                {

                    lse.close (false,
                               remFile);

                    return;

                }

            }

        }
*/
        remFile.actionPerformed (new java.awt.event.ActionEvent (ls, 0, "do"));

        if (UserProperties.get (Constants.USER_UI_LANGUAGE_PROPERTY_NAME).equals ("user-" + ls.getId ()))
        {

            try
            {

                // Need to set the language back to English.
                UILanguageStringsManager.setUILanguage (UILanguageStrings.ENGLISH_ID);

            } catch (Exception e) {

                Environment.logError ("Unable to set UI strings.",
                                      e);

                // TODO Check this...
                ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                                 new SimpleStringProperty ("Unable to reset user interface language to " + Constants.ENGLISH));

                return;

            }

            // TODO Check this...
            ComponentUtils.showMessage (Environment.getFocusedViewer (),
                                        new SimpleStringProperty ("Restart recommended"),
                                        new SimpleStringProperty ("The user interface language has been reset to " + Constants.ENGLISH + ", a restart is recommended."));

        }

    }

    public static void deleteUserUILanguageStrings (UILanguageStrings ls,
                                                    boolean           allVersions)
                                             throws Exception
    {

        if (!ls.isUser ())
        {

            throw new IllegalArgumentException ("Can only delete user language strings.");

        }

        if (allVersions)
        {

            Set<UILanguageStrings> allLs = UILanguageStringsManager.getAllUserUILanguageStrings ();

            for (UILanguageStrings _ls : allLs)
            {

                if (_ls.getId ().equals (ls.getId ()))
                {

                    UILanguageStringsManager.deleteUserUILanguageStrings (_ls);

                }

            }

        } else {

            UILanguageStringsManager.deleteUserUILanguageStrings (ls);

        }

    }

    public static UILanguageStrings getUserUIEnglishLanguageStrings (Version v)
                                                              throws Exception
    {

        // If the version is the same as the QW version the user is running then
        if (v.equals (Environment.getQuollWriterVersion ()))
        {

            UILanguageStrings def = UILanguageStringsManager.getDefaultUILanguageStrings ();

            UILanguageStringsManager.saveUserUILanguageStrings (def);

            return def;

            //return Environment.getUserUIEnglishLanguageStrings (v);

        }

        // See if there is a user strings file.
        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (v,
                                                                            UILanguageStrings.ENGLISH_ID);

        if (Files.exists (f))
        {

            return new UILanguageStrings (f.toFile ());

        }

        return null;

    }

    public static void saveUserUILanguageStrings (UILanguageStrings ls)
                                           throws Exception
    {

        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (ls);

        Files.createDirectories (f.getParent ());

        String json = JSONEncoder.encode (ls.getAsJSON ());

        Files.write (f,
                     json.getBytes (StandardCharsets.UTF_8));

    }

    public static UILanguageStrings getUserUILanguageStrings (Version v,
                                                              String  id)
                                                       throws Exception
    {

        Path f = UILanguageStringsManager.getUserUILanguageStringsFilePath (v,
                                                                            id);

        if (Files.exists (f))
        {

            UILanguageStrings ls = new UILanguageStrings (f.toFile ());
            ls.setUser (true);

            return ls;

        }

        return null;

    }

    public static UILanguageStrings getDefaultUILanguageStrings ()
    {

        return UILanguageStringsManager.defaultUILanguageStrings;

    }

    public static UILanguageStrings getCurrentUILanguageStrings ()
    {

        return UILanguageStringsManager.uiLanguageStrings;

    }

    public static String getUIString (String... ids)
    {

        return UILanguageStringsManager.getUIString (Arrays.asList (ids));

    }

    public static String getUIString (List<String> prefix,
                                      String...    ids)
    {

        List<String> _ids = new ArrayList (prefix);

        for (String s : ids)
        {

            _ids.add (s);

        }

        String s = UILanguageStringsManager.uiLanguageStrings.getString (_ids);

        if (s == null)
        {

            s = BaseStrings.toId (_ids);

        }

        return s;

    }

    public static StringProperty getUILanguageStringProperty (List<String> ids)
    {

        SimpleStringProperty prop =  new SimpleStringProperty ();
        prop.bind (getUILanguageBinding (ids));
        //prop.setValue (getUIString (ids));

        return prop;

    }

    /**
     * Creates a string property that is bound to the uilang property.  A binding function is created that uses the <b>ids</b> parm to get
     * a ui language string, the <b>reps</b> parm are then used as replacement values in a call to String.format
     * on the ui language string, i.e.
     *
     * String.format (getUIString (ids), reps)
     *
     * If a rep is a StringProperty object then getValue is called on it to get the replacement value.  Otherwise .toString is called on the
     * rep value.  Nulls can be passed in the list of reps, they are ignored if present.
     *
     * @param ids The ids for the ui string.
     * @param reps The replacements to perform on the ui string identified by <b>ids</b>.
     * @return The string property.
     */
    public static StringProperty getUILanguageStringProperty (List<String> ids,
                                                              Object...    reps)
    {

        List<String> _reps = new ArrayList<> ();

        for (Object o : reps)
        {

            if (o == null)
            {

                continue;

            }

            if (o instanceof Integer)
            {

                _reps.add (Environment.formatNumber ((Integer) o));

            }

            if (o instanceof Double)
            {

                _reps.add (Environment.formatNumber ((Double) o));

            }

            if (o instanceof Float)
            {

                _reps.add (Environment.formatNumber ((Float) o));

            }

            if (o instanceof StringProperty)
            {

                _reps.add (((StringProperty) o).getValue ());

                continue;

            }

            _reps.add (o.toString ());

        }

        SimpleStringProperty prop =  new SimpleStringProperty ();
        prop.bind (Bindings.createStringBinding (() ->
        {

            return String.format (UILanguageStringsManager.getUIString (ids),
                                  _reps.toArray ());
        },
        UILanguageStringsManager.uilangProp));

        return prop;

    }

    public static StringProperty getUILanguageStringProperty (String... prefix)
    {

        return getUILanguageStringProperty (Arrays.asList (prefix));

    }
/*
    public static StringProperty getUILanguageStringProperty (List<String> prefix,
                                                              String...    ids)
    {

        checkInstance ();

        List<String> _ids = new ArrayList<> ();

        if (prefix != null)
        {

            _ids.addAll (prefix);

        }

        if (ids != null)
        {

            _ids.addAll (Arrays.asList (ids));

        }

        return getUILanguageStringProperty (_ids);

    }
*/
    public static Binding<String> getUILanguageBinding (List<String> ids)
    {

        return Bindings.createStringBinding (() -> UILanguageStringsManager.getUIString (ids), UILanguageStringsManager.uilangProp);

    }

    public static Binding<String> getUILanguageBinding (String... ids)
    {

        return Bindings.createStringBinding (() -> UILanguageStringsManager.getUIString (ids), UILanguageStringsManager.uilangProp);

    }

    public static boolean isLanguageEnglish (String language)
    {

        if (language == null)
        {

            return false;

        }

        return Constants.ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.BRITISH_ENGLISH.equalsIgnoreCase (language)
               ||
               Constants.US_ENGLISH.equalsIgnoreCase (language);

    }

    public static LanguageStringsEditor editUILanguageStrings (UILanguageStrings userStrings,
                                                               Version           baseQWVersion)
    {

        LanguageStringsEditor lse = UILanguageStringsManager.getUILanguageStringsEditor (userStrings);

        if (lse != null)
        {

            lse.toFront ();

            return lse;

        }

         try
         {

             LanguageStringsEditor _ls = new LanguageStringsEditor (userStrings,
                                                                    baseQWVersion);
             _ls.init ();

             return _ls;

         } catch (Exception e) {

             Environment.logError ("Unable to create language strings editor",
                                   e);

             ComponentUtils.showErrorMessage (Environment.getFocusedViewer (),
                                              getUILanguageStringProperty (uilanguage,edit,actionerror));

            return null;

         }

    }

    public static LanguageStringsEditor getUILanguageStringsEditor (UILanguageStrings ls)
    {

        for (AbstractViewer v : Environment.openViewersProperty ())
        {
/*
TODO
            if (v instanceof LanguageStringsEditor)
            {

                LanguageStringsEditor lse = (LanguageStringsEditor) v;

                if (lse.getUserLanguageStrings ().equals (ls))
                {

                    return lse;

                }

            }
*/
        }

        return null;

    }

}
