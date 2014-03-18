/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.MutableSimpleNode;
import org.opendaylight.yangtools.yang.data.api.SimpleNode;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;

/**
 * @author michal.rehak
 * @param <T> type of simple node value
 *
 */
public class SimpleNodeTOImpl<T> extends AbstractNodeTO<T> implements
        SimpleNode<T>, Serializable {

    private static final long serialVersionUID = 100L;

    /**
     * @param qname
     * @param parent
     * @param value
     */
    public SimpleNodeTOImpl(QName qname, CompositeNode parent, T value) {
        super(qname, parent, value);
    }

    /**
     * @param qname
     * @param parent
     * @param value
     * @param modifyAction
     */
    public SimpleNodeTOImpl(QName qname, CompositeNode parent, T value, ModifyAction modifyAction) {
        super(qname, parent, value, modifyAction);
    }

    @Override
    public MutableSimpleNode<T> asMutable() {
        throw new IllegalAccessError("cast to mutable is not supported - "+getClass().getSimpleName());
    }

    @Override
    public String toString() {

        return super.toString() + ", value = "+getValue();
    }

  // Serialization related

    private void readObject(ObjectInputStream aStream) throws IOException, ClassNotFoundException {
        aStream.defaultReadObject();
        QName qName = (QName)aStream.readObject();
        CompositeNode parent = (CompositeNode) aStream.readObject();
        T value = (T) aStream.readObject();
        ModifyAction modifyAction = (ModifyAction) aStream.readObject();

        init(qName, parent, value, modifyAction);
    }

    private void writeObject(ObjectOutputStream aStream) throws IOException {
        aStream.defaultWriteObject();
        //manually serialize superclass
        aStream.writeObject(getQName());
        aStream.writeObject(getParent());
        aStream.writeObject(getValue());
        aStream.writeObject(getModificationAction());
    }

}
