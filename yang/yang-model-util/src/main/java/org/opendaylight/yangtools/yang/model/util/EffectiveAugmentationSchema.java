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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
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
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

/**
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent.
 */
// FIXME: 7.0.0: re-evaluate the need for this class and potentially its effective statement replacement
public final class EffectiveAugmentationSchema implements AugmentationSchemaNode {
    private final AugmentationSchemaNode delegate;
    private final ImmutableSet<DataSchemaNode> realChildSchemas;
    private final ImmutableMap<QName, DataSchemaNode> mappedChildSchemas;

    public EffectiveAugmentationSchema(final AugmentationSchemaNode augmentSchema,
            final Collection<? extends DataSchemaNode> realChildSchemas) {
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
    public Optional<? extends QualifiedBound> getWhenCondition() {
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
    public Collection<? extends UnknownSchemaNode> getUnknownSchemaNodes() {
        return delegate.getUnknownSchemaNodes();
    }

    @Override
    public Collection<? extends TypeDefinition<?>> getTypeDefinitions() {
        return delegate.getTypeDefinitions();
    }

    @Override
    public Collection<? extends DataSchemaNode> getChildNodes() {
        return realChildSchemas;
    }

    @Override
    public Collection<? extends GroupingDefinition> getGroupings() {
        return delegate.getGroupings();
    }

    @Override
    public DataSchemaNode dataChildByName(final QName name) {
        return mappedChildSchemas.get(requireNonNull(name));
    }

    @Override
    public Collection<? extends UsesNode> getUses() {
        return delegate.getUses();
    }

    @Override
    public Optional<AugmentationSchemaNode> getOriginalDefinition() {
        return delegate.getOriginalDefinition();
    }

    @Override
    public Collection<? extends ActionDefinition> getActions() {
        return delegate.getActions();
    }

    @Override
    public Optional<ActionDefinition> findAction(final QName qname) {
        return delegate.findAction(qname);
    }

    @Override
    public Collection<? extends NotificationDefinition> getNotifications() {
        return delegate.getNotifications();
    }

    @Override
    public Optional<NotificationDefinition> findNotification(final QName qname) {
        return delegate.findNotification(qname);
    }

    @Override
    public AugmentEffectiveStatement asEffectiveStatement() {
        return delegate.asEffectiveStatement();
    }
}
