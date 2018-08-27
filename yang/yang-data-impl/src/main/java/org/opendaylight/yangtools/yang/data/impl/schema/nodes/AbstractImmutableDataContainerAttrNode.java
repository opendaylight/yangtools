/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.AttributesContainer;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

// FIXME: are attributes part of hashCode/equals?
public abstract class AbstractImmutableDataContainerAttrNode<K extends PathArgument>
        extends AbstractImmutableDataContainerNode<K>
    implements AttributesContainer {

    private final Map<QName, String> attributes;

    public AbstractImmutableDataContainerAttrNode(
            final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children,
            final K nodeIdentifier, final Map<QName, String> attributes) {
        super(children, nodeIdentifier);
        this.attributes = attributes;
    }

    @Override
    public final Map<QName, String> getAttributes() {
        return attributes;
    }

    @Override
    public final Object getAttributeValue(final QName value) {
        return attributes.get(value);
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper toStringHelper) {
        super.addToStringAttributes(toStringHelper);
        if (!attributes.isEmpty()) {
            toStringHelper.add("attributes", attributes);
        }
        return toStringHelper;
    }

}
