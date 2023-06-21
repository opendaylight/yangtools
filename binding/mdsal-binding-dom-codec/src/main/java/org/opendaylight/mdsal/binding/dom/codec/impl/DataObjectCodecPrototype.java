/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ChoiceRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerLikeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.KeyAware;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceEffectiveStatement;

// FIXME: abstract and sealed
non-sealed class DataObjectCodecPrototype<T extends RuntimeTypeContainer> extends DataContainerCodecPrototype<T> {
    private final @NonNull NodeIdentifier yangArg;

    @SuppressWarnings("unchecked")
    DataObjectCodecPrototype(final Class<?> cls, final NodeIdentifier yangArg, final T type,
            final CodecContextFactory factory) {
        this(Item.of((Class<? extends DataObject>) cls), yangArg, type, factory);
    }

    DataObjectCodecPrototype(final Item<?> bindingArg, final NodeIdentifier yangArg, final T type,
            final CodecContextFactory factory) {
        super(bindingArg, yangArg.getNodeType().getModule(), type, factory);
        this.yangArg = requireNonNull(yangArg);
    }

    @Override
    final NodeIdentifier getYangArg() {
        return yangArg;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    DataContainerCodecContext<?, T> createInstance() {
        final var type = getType();
        if (type instanceof ContainerLikeRuntimeType containerLike) {
            if (containerLike instanceof ContainerRuntimeType container
                && container.statement().findFirstEffectiveSubstatement(PresenceEffectiveStatement.class)
                    .isEmpty()) {
                return new NonPresenceContainerNodeCodecContext(this);
            }
            return new ContainerNodeCodecContext(this);
        } else if (type instanceof ListRuntimeType) {
            return KeyAware.class.isAssignableFrom(getBindingClass())
                    ? KeyedListNodeCodecContext.create((DataContainerCodecPrototype<ListRuntimeType>) this)
                            : new ListNodeCodecContext(this);
        } else if (type instanceof ChoiceRuntimeType) {
            return new ChoiceCodecContext(this);
        }
        throw new IllegalArgumentException("Unsupported type " + getBindingClass() + " " + type);
    }
}