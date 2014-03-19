/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base;

import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.*;

/**
 * Proxy for AugmentationSchema. Child node schemas are replaced with actual schemas from parent.
 */
public final class AugmentationSchemaProxy implements AugmentationSchema {
    private final AugmentationSchema delegate;
    private final Set<DataSchemaNode> realChildSchemas;

    public AugmentationSchemaProxy(AugmentationSchema augmentSchema, Set<DataSchemaNode> realChildSchemas) {
        this.delegate = augmentSchema;
        this.realChildSchemas = realChildSchemas;
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
    public DataSchemaNode getDataChildByName(QName name) {
        // FIXME, retrieve from map
        for (DataSchemaNode realChildSchema : realChildSchemas) {
            if(realChildSchema.getQName().equals(name))
                return realChildSchema;
        }

        throw new IllegalArgumentException("Unknown child: " + name + " in: " + delegate);
    }

    @Override
    public DataSchemaNode getDataChildByName(String name) {
        // FIXME implement
        throw new UnsupportedOperationException("Unable to retrieve child node by name");
    }

    @Override
    public Set<UsesNode> getUses() {
        return delegate.getUses();
    }
}
