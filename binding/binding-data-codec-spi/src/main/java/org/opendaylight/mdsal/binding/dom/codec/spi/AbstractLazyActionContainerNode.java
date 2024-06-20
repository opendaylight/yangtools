/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.spi;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.Action;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@NonNullByDefault
abstract class AbstractLazyActionContainerNode<T extends DataObject>
        extends AbstractBindingLazyContainerNode<T, BindingNormalizedNodeSerializer> {
    final Class<? extends Action<?, ?, ?>> action;

    AbstractLazyActionContainerNode(final NodeIdentifier identifier, final T bindingData,
            final BindingNormalizedNodeSerializer codec, final Class<? extends Action<?, ?, ?>> action) {
        super(identifier, bindingData, codec);
        this.action = requireNonNull(action);
    }
}