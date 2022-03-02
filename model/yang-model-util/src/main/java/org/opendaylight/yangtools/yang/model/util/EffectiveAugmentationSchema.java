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
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent. This is needed to
 * correctly interpret constructs like this:
 * <pre>
 *   <code>
 *     container foo;
 *
 *     augment /foo {
 *       container bar;
 *     }
 *
 *     augment /foo/bar {
 *       container baz;
 *     }
 *   </code>
 * </pre>
 * The {@link AugmentationSchemaNode} returned for {@code augment /foo} contains bare {@code container bar}, e.g. it
 * does not show {@code augment /foo/bar} as an available augmentation -- this is only visible in {@code foo}'s schema
 * nodes.
 *
 * <p>
 * Note this class only handles {@link DataSchemaNode}s, not all {@code schema tree} statements, as it strictly should.
 */
// FIXME: YANGTOOLS-1407: this functionality should be integrated into EffectiveAugmentStatement/AugmentationSchemaNode
public final class EffectiveAugmentationSchema implements AugmentationSchemaNode {
    private final AugmentationSchemaNode delegate;
    private final ImmutableSet<DataSchemaNode> realChildSchemas;
    private final ImmutableMap<QName, DataSchemaNode> mappedChildSchemas;

    public EffectiveAugmentationSchema(final AugmentationSchemaNode augmentSchema,
            final Collection<? extends DataSchemaNode> realChildSchemas) {
        delegate = requireNonNull(augmentSchema);
        this.realChildSchemas = ImmutableSet.copyOf(realChildSchemas);

        final Map<QName, DataSchemaNode> m = new HashMap<>(realChildSchemas.size());
        for (DataSchemaNode realChildSchema : realChildSchemas) {
            m.put(realChildSchema.getQName(), realChildSchema);
        }

        mappedChildSchemas = ImmutableMap.copyOf(m);
    }

    /**
     * Returns an AugmentationSchemaNode as effective in a parent node.
     *
     * @param schema Augmentation schema
     * @param parent Parent schema
     * @return Adjusted Augmentation schema
     * @throws NullPointerException if any of the arguments is null
     */
    // FIXME: 8.0.0: integrate this method into the constructor
    public static AugmentationSchemaNode create(final AugmentationSchemaNode schema, final DataNodeContainer parent) {
        final Set<DataSchemaNode> children = new HashSet<>();
        for (DataSchemaNode augNode : schema.getChildNodes()) {
            // parent may have the corresponding child removed via 'deviate unsupported', i.e. the child is effectively
            // not present at the target site
            final DataSchemaNode child = parent.dataChildByName(augNode.getQName());
            if (child != null) {
                children.add(child);
            }
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
