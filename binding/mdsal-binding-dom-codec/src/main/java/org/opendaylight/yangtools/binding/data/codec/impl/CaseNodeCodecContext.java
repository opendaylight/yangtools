/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;

final class CaseNodeCodecContext<D extends DataObject> extends DataObjectCodecContext<D,ChoiceCaseNode> {
    public CaseNodeCodecContext(final DataContainerCodecPrototype<ChoiceCaseNode> prototype) {
        super(prototype);
    }

    @Override
    protected void addYangPathArgument(final PathArgument arg,
            final List<org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument> builder) {
        // NOOP
    }

    @Override
    public D deserialize(final NormalizedNode<?, ?> normalizedNode) {
        Preconditions.checkState(normalizedNode instanceof ChoiceNode);
        return createBindingProxy((ChoiceNode) normalizedNode);
    }

    @Override
    protected Object deserializeObject(NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    public org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument serializePathArgument(
            PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }

    @Override
    public PathArgument deserializePathArgument(
            org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument arg) {
        Preconditions.checkArgument(arg == null);
        return null;
    }
}