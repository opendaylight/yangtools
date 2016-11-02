/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public final class RpcAsContainer implements ContainerSchemaNode {

    private final RpcDefinition delegate;

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getReference() {
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

    @Nonnull
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

    @Nonnull
    @Override
    public QName getQName() {
        return delegate.getQName();
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return delegate.getPath();
    }

    @Nonnull
    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public DataSchemaNode getDataChildByName(final QName name) {
        // FIXME: check QNameModule
        switch (name.getLocalName()) {
            case "input":
                return delegate.getInput();
            case "output":
                return delegate.getOutput();
            default:
                return null;
        }
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return Collections.emptySet();
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

    @Override
    public boolean isAugmenting() {
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        return false;
    }

    @Override
    public boolean isConfiguration() {
        return false;
    }

    @Override
    public ConstraintDefinition getConstraints() {
        // TODO Auto-generated method stub
        return null;
    }

}
