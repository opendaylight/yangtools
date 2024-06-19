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
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectStep;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

abstract sealed class DataObjectCodecPrototype<T extends CompositeRuntimeType> extends CommonDataObjectCodecPrototype<T>
        permits CaseCodecPrototype, ContainerLikeCodecPrototype, ListCodecPrototype,
                NotificationCodecContext.Prototype {
    private final @NonNull NodeIdentifier yangArg;

    DataObjectCodecPrototype(final Class<?> cls, final NodeIdentifier yangArg, final T type,
            final CodecContextFactory factory) {
        this(InstanceIdentifier.createStep(cls.asSubclass(DataObject.class)), yangArg, type, factory);
    }

    DataObjectCodecPrototype(final DataObjectStep<?> bindingArg, final NodeIdentifier yangArg, final T type,
            final CodecContextFactory factory) {
        super(bindingArg, type, factory);
        this.yangArg = requireNonNull(yangArg);
    }

    @Override
    final NodeIdentifier yangArg() {
        return yangArg;
    }
}
