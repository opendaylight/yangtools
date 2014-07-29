/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractImmutableNormalizedValueNode<K extends YangInstanceIdentifier.PathArgument, V> extends
        AbstractImmutableNormalizedNode<K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractImmutableNormalizedValueNode.class);
    private final V value;

    protected AbstractImmutableNormalizedValueNode(final K nodeIdentifier, final V value) {
        super(nodeIdentifier);
        if (value == null) {
            LOGGER.warn("The value of node " + nodeIdentifier.getNodeType() + " is null");
        }
        this.value = value;
    }

    @Override
    public final V getValue() {
        return value;
    }
}
