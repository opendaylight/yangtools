/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

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
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public final class RpcAsContainer implements ContainerSchemaNode {
    private final RpcDefinition delegate;

    @Override
    public Optional<String> getDescription() {
        return delegate.getDescription();
    }

    @Override
    public Optional<String> getReference() {
        return delegate.getReference();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public Status getStatus() {
        return delegate.getStatus();
    }

    public ContainerSchemaNode getInput() {
        return delegate.getInput();
    }

    public ContainerSchemaNode getOutput() {
        return delegate.getOutput();
    }

    public RpcAsContainer(final RpcDefinition parentNode) {
        delegate = parentNode;
    }

    @Override
    public QName getQName() {
        return delegate.getQName();
    }

    @Override
    public SchemaPath getPath() {
        return delegate.getPath();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        // FIXME: check QNameModule
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
    public Set<AugmentationSchemaNode> getAvailableAugmentations() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isPresenceContainer() {
        return false;
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
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
    public boolean isAugmenting() {
        return false;
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public Set<ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public Collection<MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }
}
