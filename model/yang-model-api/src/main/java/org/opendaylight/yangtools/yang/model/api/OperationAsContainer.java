/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

final class OperationAsContainer extends ContainerLikeCompat {
    private final @NonNull OperationDefinition delegate;

    OperationAsContainer(final OperationDefinition parentNode) {
        delegate = requireNonNull(parentNode);
    }

    @Override
    protected OperationDefinition delegate() {
        return delegate;
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        final var input = delegate.getInput();
        if (name.equals(input.getQName())) {
            return input;
        }
        final var output = delegate.getOutput();
        if (name.equals(output.getQName())) {
            return output;
        }
        return null;
    }

    @Override
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return Set.of();
    }

    @Override
    public List<DataSchemaNode> getChildNodes() {
        return List.of(delegate.getInput(), delegate.getOutput());
    }
}
