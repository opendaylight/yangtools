/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

@Beta
public final class YangDataAsContainer extends AbstractAsContainer {
    private final @NonNull YangDataSchemaNode delegate;

    public YangDataAsContainer(final YangDataSchemaNode delegate) {
        this.delegate = requireNonNull(delegate);
    }

    @Override
    protected YangDataSchemaNode delegate() {
        return delegate;
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return delegate.getChildNodes();
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return delegate.getDataChildByName(name);
    }

    @Override
    public Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }
}
