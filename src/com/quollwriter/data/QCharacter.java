package com.quollwriter.data;

import java.util.*;

import org.jdom.*;


public class QCharacter extends Asset
{

    public static final String OBJECT_TYPE = "character";

    public QCharacter()
    {

        super (QCharacter.OBJECT_TYPE);

    }

    public String toString ()
    {

        return this.getObjectType () + "(name: " + this.name + ", id: " + this.getKey () + "), in: " + this.getProject ();

    }

    public void getChanges (NamedObject old,
                            Element     root)
    {

    }

}