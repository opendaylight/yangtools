/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A prototype for {@link YangDataCodecContext}.
 */
final class YangDataCodecPrototype<T extends YangData<T>>
        extends DataContainerPrototype<YangDataCodecContext<T>, YangDataRuntimeType> {
    private final @NonNull Class<T> javaClass;

    YangDataCodecPrototype(final CodecContextFactory contextFactory, final YangDataRuntimeType runtimeType,
            final Class<T> javaClass) {
        super(contextFactory, runtimeType);
        this.javaClass = requireNonNull(javaClass);
   }

    @Override
    Class<T> javaClass() {
        return javaClass;
    }

    @Override
    NodeIdentifier yangArg() {
        // FIXME: do we need this?
        throw new UnsupportedOperationException();
    }

    @Override
    YangDataCodecContext<T> createInstance() {
        // TODO Auto-generated method stub
        return null;
    }
}
