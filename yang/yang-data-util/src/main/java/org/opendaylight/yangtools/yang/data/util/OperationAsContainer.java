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
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

@Beta
public class OperationAsContainer extends ForwardingObject implements ContainerSchemaNode, OperationDefinition {
    private final OperationDefinition delegate;

    OperationAsContainer(final OperationDefinition parentNode) {
        delegate = requireNonNull(parentNode);
    }

    public static OperationAsContainer of(final OperationDefinition delegate) {
        return new OperationAsContainer(delegate);
    }

    @Override
    protected final OperationDefinition delegate() {
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
    public final Set<TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public final Set<GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public final Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public final ContainerSchemaNode getInput() {
        return delegate.getInput();
    }

    @Override
    public final ContainerSchemaNode getOutput() {
        return delegate.getOutput();
    }

    @Override
    public final QName getQName() {
        return delegate.getQName();
    }

    @Override
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
    public Set<UsesNode> getUses() {
        return ImmutableSet.of();
    }

    @Override
    public final Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public final boolean isPresenceContainer() {
        return false;
    }

    @Override
    public final Collection<DataSchemaNode> getChildNodes() {
        final List<DataSchemaNode> ret = new ArrayList<>();
        final ContainerSchemaNode input = getInput();
        final ContainerSchemaNode output = getOutput();
        if (input != null) {
            ret.add(input);
        }
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
    public final Set<ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public final Set<NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public final Collection<MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public final Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }
}
