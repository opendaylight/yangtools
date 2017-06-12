/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

/**
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent.
 *
 */
public final class EffectiveAugmentationSchema implements AugmentationSchema {
    private final AugmentationSchema delegate;
    private final Set<DataSchemaNode> realChildSchemas;
    private final Map<QName, DataSchemaNode> mappedChildSchemas;

    public EffectiveAugmentationSchema(final AugmentationSchema augmentSchema,
            final Set<DataSchemaNode> realChildSchemas) {
        this.delegate = Preconditions.checkNotNull(augmentSchema);
        this.realChildSchemas = ImmutableSet.copyOf(realChildSchemas);

        final Map<QName, DataSchemaNode> m = new HashMap<>(realChildSchemas.size());
        for (DataSchemaNode realChildSchema : realChildSchemas) {
            m.put(realChildSchema.getQName(), realChildSchema);
        }

        this.mappedChildSchemas = ImmutableMap.copyOf(m);
    }

    @Override
    public RevisionAwareXPath getWhenCondition() {
        return delegate.getWhenCondition();
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
    public DataSchemaNode getDataChildByName(final QName name) {
        return mappedChildSchemas.get(name);
    }

    @Override
    public Set<UsesNode> getUses() {
        return delegate.getUses();
    }

    @Override
    public Optional<AugmentationSchema> getOriginalDefinition() {
        return delegate.getOriginalDefinition();
    }
}
