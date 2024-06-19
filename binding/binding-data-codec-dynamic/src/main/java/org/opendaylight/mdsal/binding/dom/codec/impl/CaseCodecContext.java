/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import java.util.List;
import org.opendaylight.mdsal.binding.runtime.api.CaseRuntimeType;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class CaseCodecContext<C extends ChoiceIn<?> & DataObject> extends DataObjectCodecContext<C, CaseRuntimeType> {
    CaseCodecContext(final CaseCodecPrototype<C> prototype) {
        super(prototype, prototype.javaClass());
    }

    @Override
    void addYangPathArgument(final List<PathArgument> builder, final DataObjectStep<?> step) {
        // NOOP
    }

    @Override
    public C deserialize(final NormalizedNode data) {
        return createBindingProxy(checkDataArgument(ChoiceNode.class, data));
    }

    @Override
    public PathArgument serializePathArgument(final DataObjectStep<?> step) {
        if (step != null) {
            throw new IllegalArgumentException("Unexpected argument " + step);
        }
        return null;
    }

    @Override
    public DataObjectStep<?> deserializePathArgument(final PathArgument arg) {
        if (arg != null) {
            throw new IllegalArgumentException("Unexpected argument " + arg);
        }
        return null;
    }
}
