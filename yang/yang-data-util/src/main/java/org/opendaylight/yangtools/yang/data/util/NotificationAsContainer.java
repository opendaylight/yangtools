/*
 * Copyright (c) 2016 ZTE, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

public final class NotificationAsContainer implements ContainerSchemaNode {

    private final Map<QName, DataSchemaNode> mapNodes = new ConcurrentHashMap<>();
    private final NotificationDefinition delegate;

    public NotificationAsContainer(final NotificationDefinition notification) {
        delegate = notification;
        notification.getChildNodes().stream().forEach(node->mapNodes.put(node.getQName(),node));
    }

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

    @Override
    public Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public Collection<DataSchemaNode> getChildNodes() {
        return delegate.getChildNodes();
    }

    @Override
    public DataSchemaNode getDataChildByName(final QName qName) {
        return mapNodes.get(qName);
    }

    @Override
    public Set<UsesNode> getUses() {
        return Collections.emptySet();
    }

    @Override
    public boolean isPresenceContainer() {
        return false;
    }

    @Override
    public Set<AugmentationSchema> getAvailableAugmentations() {
        return delegate.getAvailableAugmentations();
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
        throw new UnsupportedOperationException();
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
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }
}
