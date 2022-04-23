/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

@Beta
public class OperationAsContainer extends AbstractAsContainer implements OperationDefinition {
    private final @NonNull OperationDefinition delegate;

    OperationAsContainer(final OperationDefinition parentNode) {
        delegate = requireNonNull(parentNode);
    }

    public static @NonNull OperationAsContainer of(final OperationDefinition delegate) {
        return new OperationAsContainer(delegate);
    }

    @Override
    protected final @NonNull OperationDefinition delegate() {
        return delegate;
    }

    @Override
    public final Collection<? extends @NonNull TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public final Collection<? extends @NonNull GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public final InputSchemaNode getInput() {
        return delegate.getInput();
    }

    @Override
    public final OutputSchemaNode getOutput() {
        return delegate.getOutput();
    }

    @Override
    public final EffectiveStatement<?, ?> asEffectiveStatement() {
        return delegate.asEffectiveStatement();
    }

    @Override
    public final DataSchemaNode dataChildByName(final QName name) {
        if (name.getModule().equals(getQName().getModule())) {
            return switch (name.getLocalName()) {
                case "input" -> delegate.getInput();
                case "output" -> delegate.getOutput();
                default -> null;
            };
        }
        return null;
    }

    @Override
    public final Collection<? extends AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<? extends DataSchemaNode> getChildNodes() {
        final List<DataSchemaNode> ret = new ArrayList<>();
        final InputSchemaNode input = getInput();
        if (input != null) {
            ret.add(input);
        }
        final OutputSchemaNode output = getOutput();
        if (output != null) {
            ret.add(output);
        }
        return ret;
    }
}
