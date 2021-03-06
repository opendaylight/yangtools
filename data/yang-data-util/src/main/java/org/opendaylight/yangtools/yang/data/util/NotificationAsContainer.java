/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationEffectiveStatement;

@Beta
public final class NotificationAsContainer extends AbstractAsContainer implements NotificationDefinition {
    private final @NonNull NotificationDefinition delegate;

    NotificationAsContainer(final NotificationDefinition delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public static @NonNull NotificationAsContainer of(final NotificationDefinition delegate) {
        return new NotificationAsContainer(delegate);
    }

    @Override
    protected @NonNull NotificationDefinition delegate() {
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
    public Collection<? extends @NonNull TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Collection<? extends @NonNull GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public NotificationEffectiveStatement asEffectiveStatement() {
        return delegate.asEffectiveStatement();
    }
}
