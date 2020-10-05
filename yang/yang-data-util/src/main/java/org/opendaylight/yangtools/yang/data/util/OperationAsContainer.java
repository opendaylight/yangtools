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
import com.google.common.collect.ForwardingObject;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.InputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.OutputSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

@Beta
public class OperationAsContainer extends ForwardingObject implements ContainerLike, OperationDefinition {
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
    public final Optional<String> getDescription() {
        return delegate.getDescription();
    }

    @Override
    public final Optional<String> getReference() {
        return delegate.getReference();
    }

    @Override
    public final Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public final Collection<? extends GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public final Status getStatus() {
        return delegate.getStatus();
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
    public final QName getQName() {
        return delegate.getQName();
    }

    @Override
    @Deprecated
    public final SchemaPath getPath() {
        return delegate.getPath();
    }

    @Override
    public final Optional<DataSchemaNode> findDataChildByName(final QName name) {
        if (!name.getModule().equals(getQName().getModule())) {
            return Optional.empty();
        }

        switch (name.getLocalName()) {
            case "input":
                return Optional.of(delegate.getInput());
            case "output":
                return Optional.of(delegate.getOutput());
            default:
                return Optional.empty();
        }
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return ImmutableSet.of();
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

    @Deprecated
    @Override
    public final boolean isAugmenting() {
        return false;
    }

    @Deprecated
    @Override
    public final boolean isAddedByUses() {
        return false;
    }

    @Override
    public final boolean isConfiguration() {
        return false;
    }

    @Override
    public final Collection<? extends ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<ActionDefinition> findAction(final QName qname) {
        requireNonNull(qname);
        return Optional.empty();
    }

    @Override
    public final Collection<? extends NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<NotificationDefinition> findNotification(final QName qname) {
        requireNonNull(qname);
        return Optional.empty();
    }

    @Override
    public final Collection<? extends MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public final Optional<? extends QualifiedBound> getWhenCondition() {
        return Optional.empty();
    }
}
