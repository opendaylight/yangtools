/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractAnydataNode;

final class ImmutableAnydataNode<V> extends AbstractAnydataNode<V> {
    private final @NonNull NodeIdentifier name;
    private final @NonNull V body;
    private final @NonNull Class<V> objectModel;

    ImmutableAnydataNode(final NodeIdentifier name, final V body, final Class<V> objectModel) {
        this.name = requireNonNull(name);
        this.body = requireNonNull(body);
        this.objectModel = requireNonNull(objectModel);
    }

    @Override
    public NodeIdentifier name() {
        return name;
    }

    @Override
    public Class<V> bodyObjectModel() {
        return objectModel;
    }

    @Override
    protected V value() {
        return body;
    }

    @Override
    protected V wrappedValue() {
        return body;
    }
}