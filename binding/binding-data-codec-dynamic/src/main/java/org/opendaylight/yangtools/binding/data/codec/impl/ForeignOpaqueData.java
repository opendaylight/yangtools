/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.OpaqueData;
import org.opendaylight.yangtools.binding.lib.AbstractOpaqueData;
import org.opendaylight.yangtools.yang.data.api.schema.ForeignDataNode;

/**
 * An {@link OpaqueData} implementation backed by {@link ForeignDataNode}.
 *
 * @param <T> Object model type
 */
final class ForeignOpaqueData<T> extends AbstractOpaqueData<T> {
    private final ForeignDataNode<T> domData;

    ForeignOpaqueData(final ForeignDataNode<T> domData) {
        this.domData = requireNonNull(domData);
    }

    @Override
    public Class<T> getObjectModel() {
        return domData.bodyObjectModel();
    }

    @Override
    public T getData() {
        return domData.body();
    }

    ForeignDataNode<T> domData() {
        return domData;
    }
}
