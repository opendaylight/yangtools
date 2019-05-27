/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * Utility DataNodeContainer containing only the specified node.
 */
@Beta
public final class SingleChildDataNodeContainer implements DataNodeContainer {
    private final DataSchemaNode child;

    public SingleChildDataNodeContainer(final DataSchemaNode child) {
        this.child = requireNonNull(child);
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return child.getQName().equals(name) ? Optional.of(child) : Optional.empty();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return ImmutableSet.of(child);
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return ImmutableSet.of();
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return ImmutableSet.of();
    }

    @Override
    public Set<UsesNode> getUses() {
        return ImmutableSet.of();
    }
}
