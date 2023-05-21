/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

final class OperationAsContainer extends AbstractAsContainer {
    private final @NonNull OperationDefinition delegate;

    OperationAsContainer(final OperationDefinition parentNode) {
        delegate = requireNonNull(parentNode);
    }

    public static @NonNull OperationAsContainer of(final OperationDefinition delegate) {
        return new OperationAsContainer(delegate);
    }

    @Override
    protected @NonNull OperationDefinition delegate() {
        return delegate;
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
    public Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return ImmutableList.of(delegate.getInput(), delegate.getOutput());
    }
}
