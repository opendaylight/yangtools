/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.Node;

public abstract class AbstractNodeBuilder<P extends Node<?>, T extends NodeBuilder<P, T>> implements NodeBuilder<P, T> {

    private final Map<QName, String> attributes;
    private QName qName;

    public AbstractNodeBuilder() {
        this.attributes = new ConcurrentHashMap<>();
    }

    public AbstractNodeBuilder(QName nodeType, Map<QName, String> attributes) {
        super();
        this.qName = nodeType;
        this.attributes = new ConcurrentHashMap<>(attributes);
    }

    public AbstractNodeBuilder(QName nodeType) {
        this.qName = nodeType;
        this.attributes = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    protected final T thisInstance() {
        return (T) this;
    }

    @Override
    public final T setQName(QName name) {
        this.qName = name;
        return thisInstance();
    }

    public QName getQName() {
        return qName;
    }

    @Override
    public final T setAttribute(QName attrName, String attrValue) {
        attributes.put(attrName, attrValue);
        return thisInstance();
    }

    public Map<QName, String> getAttributes() {
        return attributes;
    }
    
    @Override
    public T setAttribute(String attrName, String attrValue) {
        attributes.put(QName.create(qName, attrName), attrValue);
        return thisInstance();
    }

}
