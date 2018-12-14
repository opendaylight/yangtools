/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent.
 */
public final class EffectiveAugmentationSchema implements AugmentationSchemaNode {
    private final AugmentationSchemaNode delegate;
    private final ImmutableSet<DataSchemaNode> realChildSchemas;
    private final ImmutableMap<QName, DataSchemaNode> mappedChildSchemas;

    public EffectiveAugmentationSchema(final AugmentationSchemaNode augmentSchema,
            final Set<DataSchemaNode> realChildSchemas) {
        this.delegate = requireNonNull(augmentSchema);
        this.realChildSchemas = ImmutableSet.copyOf(realChildSchemas);

        final Map<QName, DataSchemaNode> m = new HashMap<>(realChildSchemas.size());
        for (DataSchemaNode realChildSchema : realChildSchemas) {
            m.put(realChildSchema.getQName(), realChildSchema);
        }

        this.mappedChildSchemas = ImmutableMap.copyOf(m);
    }

    /**
     * Returns an AugmentationSchemaNode as effective in a parent node.
     *
     * @param schema Augmentation schema
     * @param parent Parent schema
     * @return Adjusted Augmentation schema
     * @throws NullPointerException if any of the arguments is null
     */
    public static AugmentationSchemaNode create(final AugmentationSchemaNode schema, final DataNodeContainer parent) {
        Set<DataSchemaNode> children = new HashSet<>();
        for (DataSchemaNode augNode : schema.getChildNodes()) {
            children.add(parent.getDataChildByName(augNode.getQName()));
        }
        return new EffectiveAugmentationSchema(schema, children);
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return delegate.getWhenCondition();
    }

    @Override
    public Optional<String> getDescription() {
        return delegate.getDescription();
    }

    @Override
    public Optional<String> getReference() {
        return delegate.getReference();
    }

    @Override
    public Status getStatus() {
        return delegate.getStatus();
    }

    @Override
    public SchemaPath getTargetPath() {
        return delegate.getTargetPath();
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return delegate.getUnknownSchemaNodes();
    }

    @Override
    public Set<TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Set<DataSchemaNode> getChildNodes() {
        return realChildSchemas;
    }

    @Override
    public Set<GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public Optional<DataSchemaNode> findDataChildByName(final QName name) {
        return Optional.ofNullable(mappedChildSchemas.get(requireNonNull(name)));
    }

    @Override
    public Set<UsesNode> getUses() {
        return delegate.getUses();
    }

    @Override
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return delegate.getOriginalDefinition();
    }

    @Override
    public Set<ActionDefinition> getActions() {
        return delegate.getActions();
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return delegate.getNotifications();
    }
}
