/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.ParentObject;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;

abstract sealed class ParentObjectCodecPrototype<
        O extends ParentObject<O>,
        C extends DataContainerCodecContext<O, R, P>,
        P extends DataContainerPrototype<C, R>,
        R extends CompositeRuntimeType>
        extends DataContainerPrototype<C, R> permits NotificationCodecContext.Prototype {
    ParentObjectCodecPrototype(final CodecContextFactory contextFactory, final R runtimeType) {
        super(contextFactory, runtimeType);
    }
}
