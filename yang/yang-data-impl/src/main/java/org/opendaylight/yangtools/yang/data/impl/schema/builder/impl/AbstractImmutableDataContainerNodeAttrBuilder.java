/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;

abstract class AbstractImmutableDataContainerNodeAttrBuilder<I extends PathArgument, R extends DataContainerNode<I>>
        extends AbstractImmutableDataContainerNodeBuilder<I, R> implements DataContainerNodeAttrBuilder<I, R> {
    private Map<QName, String> attributes;

    AbstractImmutableDataContainerNodeAttrBuilder() {
        this.attributes = Collections.emptyMap();
    }

    AbstractImmutableDataContainerNodeAttrBuilder(final int sizeHint) {
        super(sizeHint);
        this.attributes = Collections.emptyMap();
    }

    AbstractImmutableDataContainerNodeAttrBuilder(final AbstractImmutableDataContainerAttrNode<I> node) {
        super(node);
        this.attributes = node.getAttributes();
    }

    protected final Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withAttributes(final Map<QName, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withValue(
            final Collection<DataContainerChild<? extends PathArgument, ?>> value) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withValue(value);
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withChild(final DataContainerChild<?, ?> child) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withChild(child);
    }

    @Override
    public DataContainerNodeAttrBuilder<I, R> withNodeIdentifier(final I nodeIdentifier) {
        return (DataContainerNodeAttrBuilder<I, R>) super.withNodeIdentifier(nodeIdentifier);
    }
}
