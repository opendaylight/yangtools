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
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A prototype for {@link ChoiceCodecContext}.
 */
final class ChoiceCodecPrototype<T extends ChoiceIn<?>>
        extends DataContainerPrototype<ChoiceCodecContext<T>, ChoiceRuntimeType> {
    private final @NonNull NodeIdentifier yangArg;
    private final @NonNull Class<T> javaClass;

    ChoiceCodecPrototype(final CodecContextFactory contextFactory, final ChoiceRuntimeType runtimeType,
            final Class<T> javaClass) {
        super(contextFactory, runtimeType);
        this.javaClass = requireNonNull(javaClass);
        yangArg = NodeIdentifier.create(runtimeType.statement().argument());
    }

    @Override
    Class<T> javaClass() {
        return javaClass;
    }

    @Override
    NodeIdentifier yangArg() {
        return yangArg;
    }

    @Override
    ChoiceCodecContext<T> createInstance() {
        return new ChoiceCodecContext<>(this);
    }
}
