/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api;

import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Base representation of node in the data tree, defines basic parameters of
 * node such as a QName.
 *
 *
 * @param <T>
 */
public interface Node<T> extends Entry<QName, T> {

    /**
     * Returns the name of the Node
     *
     * @return qName of node
     */
    QName getNodeType();

    /**
     * Returns parent node
     *
     * @return parent node
     */
    CompositeNode getParent();

    /**
     * Returns the value that holds current node, if no value is defined method
     * can return <code>null</code>
     *
     * @return Returns the value that holds current node.
     */
    T getValue();
}
