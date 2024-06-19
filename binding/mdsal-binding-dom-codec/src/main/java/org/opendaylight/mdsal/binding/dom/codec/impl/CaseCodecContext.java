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
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class CaseCodecContext<D extends DataObject> extends DataObjectCodecContext<D, CaseRuntimeType> {
    CaseCodecContext(final CaseCodecPrototype prototype) {
        super(prototype, CodecItemFactory.of(prototype.javaClass()));
    }

    @Override
    void addYangPathArgument(final List<YangInstanceIdentifier.PathArgument> builder, final PathArgument arg) {
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
