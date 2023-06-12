/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

final class NotificationAsContainer extends ContainerLikeCompat {
    private final @NonNull NotificationDefinition delegate;

    NotificationAsContainer(final NotificationDefinition delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    public NotificationDefinition delegate() {
        return delegate;
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return delegate.getChildNodes();
    }

    @Override
    public Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
        return delegate.getAvailableAugmentations();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return delegate.dataChildByName(name);
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }
}
