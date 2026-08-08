/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

/**
 * A prototype for {@link ListCodecContext}.
 */
// FIXME: split into an abstract class and specialization
sealed class ListCodecPrototype<R extends ListRuntimeType> extends DataObjectCodecPrototype<R>
        permits MapCodecPrototype {
    ListCodecPrototype(final DataObjectStep<?> step, final R type, final CodecContextFactory factory) {
        super(step, NodeIdentifier.create(type.statement().argument()), type, factory);
    }

    @Override
    ListCodecContext<?, R> createInstance() {
        return new ListCodecContext<>(this);
    }

    @Override
    <T extends CodecDataObject<T>> GenClass<T> generateClass(final DataContainerAnalysis<R> analysis) {
        return generateAugmentable(analysis, runtimeType());
    }
}
