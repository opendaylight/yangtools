/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.AddedByUsesAware;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

final class CaseNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D, CaseRuntimeType> {
    CaseNodeCodecContext(final DataContainerCodecPrototype<CaseRuntimeType> prototype) {
        super(prototype);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    Item<?> createBindingArg(final Class<?> childClass, final EffectiveStatement<?, ?> childSchema) {
        // FIXME: MDSAL-697: see overridden method for further guidance
        return childSchema instanceof AddedByUsesAware aware && aware.isAddedByUses()
            ? Item.of((Class)getBindingClass(), (Class)childClass)
                : super.createBindingArg(childClass, childSchema);
    }

    @Override
    void addYangPathArgument(final PathArgument arg, final List<YangInstanceIdentifier.PathArgument> builder) {
        // NOOP
    }

    @Override
    public D deserialize(final NormalizedNode data) {
        return createBindingProxy(checkDataArgument(ChoiceNode.class, data));
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    public YangInstanceIdentifier.PathArgument serializePathArgument(final PathArgument arg) {
        checkArgument(arg == null, "Unexpected argument %s", arg);
        return null;
    }

    @Override
    public PathArgument deserializePathArgument(final YangInstanceIdentifier.PathArgument arg) {
        checkArgument(arg == null, "Unexpected argument %s", arg);
        return null;
    }
}
