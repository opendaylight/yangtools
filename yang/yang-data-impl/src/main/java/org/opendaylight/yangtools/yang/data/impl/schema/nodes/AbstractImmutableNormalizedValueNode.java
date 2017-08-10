/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractImmutableNormalizedValueNode<K extends PathArgument, V> extends
        AbstractImmutableNormalizedNode<K, V> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractImmutableNormalizedValueNode.class);
    @Nullable
    private final V value;

    protected AbstractImmutableNormalizedValueNode(final K nodeIdentifier, @Nullable final V value) {
        super(nodeIdentifier);

        /*
         * Null value is allowed for empty type definition so it should be debug,
         * but still we are logging it in case we need to debug missing values.
         */
        // FIXME: one we do not map YANG 'void' to java.lang.Void we should be enforcing non-null here
        if (value == null) {
            LOG.debug("The value of node {} is null", nodeIdentifier.getNodeType());
        }
        this.value = value;
    }

    @Nullable
    @Override
    public final V getValue() {
        return wrapValue(value);
    }

    @Nullable
    protected final V value() {
        return value;
    }

    protected V wrapValue(final V value) {
        return value;
    }
}
