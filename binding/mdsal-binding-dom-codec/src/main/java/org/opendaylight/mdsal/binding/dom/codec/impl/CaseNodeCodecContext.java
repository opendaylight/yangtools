/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

final class CaseNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D, CaseSchemaNode> {
    CaseNodeCodecContext(final DataContainerCodecPrototype<CaseSchemaNode> prototype) {
        super(prototype);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    Item<?> createBindingArg(final Class<?> childClass, final DataSchemaNode childSchema) {
        return childSchema.isAddedByUses() ? Item.of((Class)getBindingClass(), (Class)childClass)
                : Item.of((Class<? extends DataObject>) childClass);
    }

    @Override
    protected void addYangPathArgument(final PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        // NOOP
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        Preconditions.checkState(normalizedNode instanceof ChoiceNode);
        return createBindingProxy((ChoiceNode) normalizedNode);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @Override
    public PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }
}
