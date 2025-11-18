/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.NodeStep;
import org.opendaylight.yangtools.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.yang.data.spi.node.ImmutableNodes;

/**
 * A {@link ContainerLikeCodecContext} specialized for {@code container}s which do not have a presence statement.
 */
final class StructuralContainerCodecContext<D extends DataObject> extends ContainerLikeCodecContext<D> {
    private static final VarHandle EMPTY_OBJECT;

    static {
        try {
            EMPTY_OBJECT = MethodHandles.lookup().findVarHandle(StructuralContainerCodecContext.class,
                "emptyObject", DataObject.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile @Nullable D emptyObject = null;

    StructuralContainerCodecContext(final Class<D> cls, final ContainerRuntimeType type,
            final CodecContextFactory factory) {
        this(new StructuralContainerCodecPrototype(new NodeStep<>(cls), type, factory));
    }

    StructuralContainerCodecContext(final StructuralContainerCodecPrototype prototype) {
        super(prototype);
    }

    @NonNull D emptyObject() {
        final var local = (D) EMPTY_OBJECT.getAcquire(this);
        return local != null ? local : loadEmptyObject();
    }

    private @NonNull D loadEmptyObject() {
        final var local = createBindingProxy(
            ImmutableNodes.newContainerBuilder().withNodeIdentifier(getDomPathArgument()).build());
        final var witness = (D) EMPTY_OBJECT.compareAndExchangeRelease(this, null, local);
        return witness != null ? witness : local;
    }
}
