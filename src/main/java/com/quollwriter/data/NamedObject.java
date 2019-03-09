package com.quollwriter.data;

import java.io.*;
import java.util.*;

import com.gentlyweb.xml.*;

import javafx.beans.property.*;

import com.quollwriter.*;

import com.quollwriter.data.comparators.*;

import org.jdom.*;

public abstract class NamedObject extends DataObject
{

    public static final String NAME = "name";
    public static final String LAST_MODIFIED = "lastModified";
    public static final String DESCRIPTION = "description";
    public static final String ALIASES = "aliases";
    public static final String TAG = "tag";

    private StringProperty nameProp = null;
    private String   name = null;
    private Date     lastModified = null;
    private StringWithMarkup   description = null;
    private Set<Link>  links = new HashSet ();
    private Set<Note> notes = new TreeSet (new ChapterItemSorter ());
    private String     aliases = null;
    private Set<File> files = new LinkedHashSet ();
    private Set<Tag> tags = new LinkedHashSet ();

    public NamedObject(String objType,
                       String name)
    {

        super (objType);

        this.name = name;

        this.nameProp = new SimpleStringProperty (this.name);

    }

    public NamedObject(String objType)
    {

        super (objType);

    }

    public StringProperty nameProperty ()
    {

        return this.nameProp;

    }

    public <T extends NamedObject> void merge (T other)
    {

        String od = other.getDescriptionText ();

        String td = this.getDescriptionText ();

        if ((td == null)
            &&
            (od != null)
           )
        {

            this.setDescription (other.getDescription ());

        }

        if ((td != null)
            &&
            (od != null)
           )
        {

            td = td.trim ();
            od = od.trim ();

            if ((!td.equalsIgnoreCase (od))
                &&
                (!td.toLowerCase ().contains (od.toLowerCase ()))
               )
            {

                String nd = td + "\n\n" + od;

                this.setDescription (new StringWithMarkup (nd,
                                                           this.getDescription ().getMarkup ()));

            }

        }

        Set<String> taliases = new LinkedHashSet (this.getAliasesAsList ());

        taliases.addAll (other.getAliasesAsList ());

        if (taliases.size () > 0)
        {

            StringBuilder b = new StringBuilder ();

            int i = 0;

            for (String a : taliases)
            {

                b.append (a);

                if (i < taliases.size () - 1)
                {

                    b.append (",");

                }

                i++;

            }

            this.setAliases (b.toString ());

        }

    }

    @Override
    public void fillToStringProperties (Map<String, Object> props)
    {

        super.fillToStringProperties (props);

        this.addToStringProperties (props,
                                    "name",
                                    this.name);
        this.addToStringProperties (props,
                                    "lastModified",
                                    this.lastModified);
        this.addToStringProperties (props,
                                    "links",
                                    this.links.size ());
        this.addToStringProperties (props,
                                    "notes",
                                    this.notes.size ());
        this.addToStringProperties (props,
                                    "tags",
                                    this.tags);

    }

    public synchronized void reindex ()
    {

        TreeSet<Note> nnotes = new TreeSet (new ChapterItemSorter ());

        nnotes.addAll (this.notes);

        this.notes = nnotes;

        for (Note n : this.notes)
        {

            n.reindex ();

        }

    }

    public boolean contains (String s)
    {

        if (s == null)
        {

            return false;

        }

        s = s.trim ().toLowerCase ();

        if (s.length () == 0)
        {

            return false;

        }

        if (this.name.toLowerCase ().indexOf (s) != -1)
        {

            return true;

        }

        if ((this.description != null)
            &&
            (this.description.getText () != null)
           )
        {

            if (this.description.getText ().toLowerCase ().indexOf (s) != -1)
            {

                return true;

            }

        }

        if (this.aliases != null)
        {

            if (this.aliases.toLowerCase ().indexOf (s) != -1)
            {

                return true;

            }

        }

        return false;

    }

    public Note getNoteAt (int pos)
    {

        for (Note n : this.notes)
        {

            if (n.getPosition () == pos)
            {

                return n;

            }

        }

        return null;

    }

    public Set<Note> getNotesAt (int pos)
    {

        Set<Note> notes = new TreeSet (new ChapterItemSorter ());

        for (Note n : this.notes)
        {

            if (n.getPosition () == pos)
            {

                notes.add (n);

            }

        }

        return notes;

    }

    public DataObject getObjectForReference (ObjectReference r)
    {

        if (r.equals (this.getObjectReference ()))
        {

            return this;

        }

        DataObject d = null;

        for (Note n : this.notes)
        {

            d = n.getObjectForReference (r);

            if (d != null)
            {

                break;

            }

        }

        return d;

    }

    public abstract Set<NamedObject> getAllNamedChildObjects ();

    public Set<File> getFiles ()
    {

        return this.files;

    }

    public void setFiles (Set<File> files)
    {

        this.files = files;

    }

    public void addFile (File f)
    {

        if (this.files == null)
        {

            this.files = new LinkedHashSet ();

        }

        this.files.add (f);

    }

    public void addNote (Note n)
    {

        n.setObject (this);

        n.setParent (this);

        this.notes.add (n);

    }

    public void removeNote (Note n)
    {

        this.notes.remove (n);

    }

    public Set<Note> getNotes ()
    {

        return this.notes;

    }

    public Set<String> getAllNames ()
    {

        Set<String> l = new HashSet ();

        l.add (this.name);

        l.addAll (this.getAliasesAsList ());

        return l;

    }

    public List<String> getAliasesAsList ()
    {

        List l = new ArrayList<String> ();

        if (this.aliases != null)
        {

            StringTokenizer t = new StringTokenizer (this.aliases,
                                                     ",;" + String.valueOf ('\n'));

            while (t.hasMoreTokens ())
            {

                l.add (t.nextToken ().trim ());

            }

        }

        return l;

    }

    public String getAliases ()
    {

        return this.aliases;

    }

    public void setAliases (String a)
    {

        String oldAliases = this.aliases;

        this.aliases = a;

        this.firePropertyChangedEvent (NamedObject.ALIASES,
                                       oldAliases,
                                       this.aliases);

    }

    public void clearLinks ()
    {

        this.links.clear ();

    }

    public void removeLink (Link l)
    {

        this.links.remove (l);

    }

    public void addLink (Link l)
    {

        this.links.add (l);

    }

    public Set<NamedObject> getOtherObjectsInLinks ()
    {

        Set<NamedObject> s = new TreeSet (NamedObjectSorter.getInstance ());

        Iterator<Link> it = this.links.iterator ();

        while (it.hasNext ())
        {

            Link l = it.next ();

            s.add (l.getOtherObject (this));

        }

        return s;

    }

    public void addLinkTo (NamedObject o)
    {

        if (this == o)
        {

            return;

        }

        this.addLink (new Link (this,
                                o));

    }

    public Set<Link> getLinks ()
    {

        return this.links;

    }

    public void removeLinkFor (NamedObject n)
    {

        Iterator<Link> iter = this.links.iterator ();

        while (iter.hasNext ())
        {

            Link l = iter.next ();

            if (l.getObject1 () == n)
            {

                iter.remove ();

                return;

            }

        }

    }

    public void setLinks (Set<Link> l)
    {

        this.links.addAll (l);

    }

    public Date getLastModified ()
    {

        if (this.lastModified == null)
        {

            return this.getDateCreated ();

        }

        return this.lastModified;

    }

    public String getDescriptionText ()
    {

        if (this.description != null)
        {

            return this.description.getText ();

        }

        return null;

    }

    public StringWithMarkup getDescription ()
    {

        return this.description;

    }

    public void setDescription (StringWithMarkup d)
    {

        StringWithMarkup oldDesc = this.description;

        this.description = d;

        this.setLastModified (new Date ());

        this.firePropertyChangedEvent (NamedObject.DESCRIPTION,
                                       oldDesc,
                                       this.description);

    }

    public void setName (String n)
    {

        String oldName = this.name;

        this.name = n;

        this.setLastModified (new Date ());

        this.firePropertyChangedEvent (NamedObject.NAME,
                                       oldName,
                                       this.name);

    }

    public String getName ()
    {

        return this.name;

    }

    public void setLastModified (Date d)
    {

        Date oldDate = this.lastModified;

        this.lastModified = d;

        if (oldDate == null)
        {

            oldDate = new Date ();
            oldDate.setTime (0);

        }

        this.firePropertyChangedEvent (NamedObject.LAST_MODIFIED,
                                       oldDate,
                                       this.lastModified);

    }

    public abstract void getChanges (NamedObject old,
                                     Element     root);

    protected void addFieldChangeElement (Element changesEl,
                                          String  fieldName,
                                          String  oldValue,
                                          String  newValue)
    {

        if (NamedObject.areDifferent (oldValue,
                                      newValue))
        {

            Element fieldEl = new Element ("field");

            changesEl.addContent (fieldEl);

            fieldEl.setAttribute ("name",
                                  fieldName);

            Element oldEl = new Element ("old");
            Element newEl = new Element ("new");

            oldEl.addContent ((oldValue != null) ? (oldValue + "") : (null + ""));
            newEl.addContent ((newValue != null) ? (newValue + "") : (null + ""));

            fieldEl.addContent (oldEl);
            fieldEl.addContent (newEl);

        }

    }

    protected void addFieldChangeElement (Element          changesEl,
                                          String           fieldName,
                                          StringWithMarkup oldValue,
                                          StringWithMarkup newValue)
    {

        String ot = (oldValue != null ? oldValue.getText () : null);
        String nt = (newValue != null ? newValue.getText () : null);

        if (NamedObject.areDifferent (ot,
                                      nt))
        {

            Element fieldEl = new Element ("field");

            changesEl.addContent (fieldEl);

            fieldEl.setAttribute ("name",
                                  fieldName);

            Element oldEl = new Element ("old");
            Element newEl = new Element ("new");

            oldEl.addContent ((ot != null) ? (ot + "") : (null + ""));
            newEl.addContent ((nt != null) ? (nt + "") : (null + ""));

            fieldEl.addContent (oldEl);
            fieldEl.addContent (newEl);

        }

    }

    protected void addFieldChangeElement (Element changesEl,
                                          String  fieldName,
                                          Date    oldValue,
                                          Date    newValue)
    {

        if (NamedObject.areDifferent (oldValue,
                                      newValue))
        {

            Element fieldEl = new Element ("field");

            changesEl.addContent (fieldEl);

            fieldEl.setAttribute ("name",
                                  fieldName);

            Element oldEl = new Element ("old");
            Element newEl = new Element ("new");

            oldEl.addContent ((oldValue != null) ? (oldValue.getTime () + "") : (null + ""));
            newEl.addContent ((newValue != null) ? (newValue.getTime () + "") : (null + ""));

            fieldEl.addContent (oldEl);
            fieldEl.addContent (newEl);

        }

    }

    public Element getChanges (NamedObject old)
    {

        Element root = new Element ("changes");

        this.addFieldChangeElement (root,
                                    "name",
                                    ((old != null) ? old.getName () : null),
                                    this.name);

        this.addFieldChangeElement (root,
                                    "aliases",
                                    ((old != null) ? old.getAliases () : null),
                                    this.aliases);

        this.addFieldChangeElement (root,
                                    "description",
                                    ((old != null) ? old.getDescription () : null),
                                    this.description);

        this.getChanges (old,
                         root);

        if (root.getContent ().size () > 0)
        {

            return root;

        }

        return null;

    }

    public void addTag (Tag t)
    {

        if (t == null)
        {

            return;

        }

        this.tags.add (t);

        this.updateTags ();

        this.firePropertyChangedEvent (NamedObject.TAG,
                                       null,
                                       t);

    }

    public void removeTag (Tag t)
    {

        if (t == null)
        {

            return;

        }

        this.tags.remove (t);

        this.updateTags ();

        this.firePropertyChangedEvent (NamedObject.TAG,
                                       t,
                                       null);

    }

    public boolean hasTag (Tag t)
    {

        if (t == null)
        {

            return false;

        }

        return this.tags.contains (t);

    }

    private void updateTags ()
    {

        Set<String> tagKeys = new LinkedHashSet ();

        for (Tag t : this.tags)
        {

            tagKeys.add (t.getKey ().toString ());

        }

        try
        {

            this.setProperty (Constants.TAGS_PROPERTY_NAME,
                              Utils.joinStrings (tagKeys,
                                                 ";"));

        } catch (Exception e) {

            Environment.logError ("Unable to updated tags to: " +
                                  tagKeys +
                                  " for: " +
                                  this,
                                  e);

        }

    }

    @Override
    public void setPropertiesAsString (String s)
                                throws Exception
    {

        super.setPropertiesAsString (s);

        Set<String> tagKeys = new LinkedHashSet (Utils.splitString (this.getProperty (Constants.TAGS_PROPERTY_NAME),
                                                                    ";"));

        for (String k : tagKeys)
        {

            try
            {

                Tag t = Environment.getTagByKey (Long.parseLong (k));

                if (t != null)
                {

                    this.tags.add (t);

                }

            } catch (Exception e) {

                // Ignore.

            }

        }

    }

    public static boolean areDifferent (Comparable o,
                                        Comparable n)
    {

        if ((o == null) &&
            (n == null))
        {

            return false;

        }

        if ((o != null) &&
            (n == null))
        {

            return true;

        }

        if ((o == null) &&
            (n != null))
        {

            return true;

        }

        return o.compareTo (n) != 0;

    }

}
