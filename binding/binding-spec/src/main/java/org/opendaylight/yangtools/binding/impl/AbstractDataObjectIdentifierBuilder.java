/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.impl;

import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectIdentifier;
import org.opendaylight.yangtools.binding.ExactDataObjectStep;
import org.opendaylight.yangtools.binding.InexactDataObjectStep;

public abstract sealed class AbstractDataObjectIdentifierBuilder<T extends DataObject>
        extends AbstractDataObjectReferenceBuilder<T>
        implements DataObjectIdentifier.Builder<T>
        permits DataObjectIdentifierBuilder, DataObjectIdentifierBuilderWithKey {
    AbstractDataObjectIdentifierBuilder(final AbstractDataObjectIdentifierBuilder<?> prev) {
        super(prev);
    }

    AbstractDataObjectIdentifierBuilder(final DataObjectIdentifier<T> base) {
        super(base);
    }

    AbstractDataObjectIdentifierBuilder(final ExactDataObjectStep<?> item) {
        super(item);
    }

    @Override
    protected final void appendItem(final InexactDataObjectStep<?> item) {
        throw new IllegalArgumentException("Cannot make inexact step " + item);
    }
}
