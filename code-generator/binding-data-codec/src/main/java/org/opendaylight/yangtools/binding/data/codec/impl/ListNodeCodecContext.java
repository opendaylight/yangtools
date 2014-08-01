/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import java.util.List;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class ListNodeCodecContext extends DataObjectCodecContext<ListSchemaNode> {

    private final YangInstanceIdentifier.PathArgument yangIdentifier;
    private final Codec<NodeIdentifierWithPredicates, IdentifiableItem<?, ?>> codec;

    ListNodeCodecContext(final Class<?> cls, final ListSchemaNode nodeSchema, final CodecContextFactory loader) {
        super(cls, nodeSchema.getQName().getModule(), nodeSchema, loader);
        this.yangIdentifier = new YangInstanceIdentifier.NodeIdentifier(nodeSchema.getQName());
        if (Identifiable.class.isAssignableFrom(cls)) {
            this.codec = loader.getPathArgumentCodec(cls,nodeSchema);
        } else {
            this.codec = null;
        }
    }

    @Override
    public YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return yangIdentifier;
    }

    @Override
    public void addYangPathArgument(final InstanceIdentifier.PathArgument arg, final List<YangInstanceIdentifier.PathArgument> builder) {

        /*
         * DOM Instance Identifier for list is always represent by two
         * entries one for map and one for children. This is also true for
         * wildcarded instance identifiers
         */
        if (builder == null) {
            return;
        }
        super.addYangPathArgument(arg, builder);
        if (arg instanceof IdentifiableItem<?, ?>) {
            builder.add(codec.serialize((IdentifiableItem<?, ?>) arg));
        } else {
            // Adding wildcarded
            super.addYangPathArgument(arg, builder);
        }
    }

    @Override
    public InstanceIdentifier.PathArgument getBindingPathArgument(
            final org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument domArg) {
        if(domArg instanceof NodeIdentifierWithPredicates) {
            return codec.deserialize((NodeIdentifierWithPredicates) domArg);
        }
        return super.getBindingPathArgument(domArg);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public NodeIdentifierWithPredicates serialize(final Identifier<?> key) {
        return codec.serialize(new IdentifiableItem(bindingClass, key));
    }
}