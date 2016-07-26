/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 *
 * Node which is normalized according to the YANG schema
 * is identifiable by a {@link org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier}.
 *
 * See subinterfaces of this interface for concretization
 * of node.
 *
 * @param <K> Local identifier of node
 * @param <V> Value of node
 */
public interface NormalizedNode<K extends PathArgument, V> extends Identifiable<K> {
    /**
     * QName of the node as defined in YANG schema.
     *
     * @return QName of this node, non-null.
     */
    QName getNodeType();

    /**
     * Locally unique identifier of the node.
     *
     * @return Node identifier, non-null.
     */
    @Override
    K getIdentifier();

    /**
     * Value of node.
     *
     * @return Value of the node, may be null.
     */
    V getValue();
}
