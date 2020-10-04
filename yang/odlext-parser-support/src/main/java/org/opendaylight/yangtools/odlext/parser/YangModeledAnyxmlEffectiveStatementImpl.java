/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.ForwardingEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;

final class YangModeledAnyxmlEffectiveStatementImpl
        extends ForwardingEffectiveStatement<QName, AnyxmlStatement, AnyxmlEffectiveStatement>
        implements YangModeledAnyxmlSchemaNode, AnyxmlEffectiveStatement {
    private final @NonNull AnyxmlEffectiveStatement delegate;
    private final @NonNull ContainerSchemaNode contentSchema;

    YangModeledAnyxmlEffectiveStatementImpl(final AnyxmlEffectiveStatement delegate,
        final ContainerSchemaNode contentSchema) {
        this.delegate = requireNonNull(delegate);
        this.contentSchema = requireNonNull(contentSchema);
    }

    @Override
    protected @NonNull AnyxmlEffectiveStatement delegate() {
        return delegate;
    }

    @Override
    public @NonNull ContainerSchemaNode getSchemaOfAnyXmlData() {
        return contentSchema;
    }

    private AnyxmlSchemaNode delegateSchemaNode() {
        return (AnyxmlSchemaNode) delegate;
    }

    @Override
    public boolean isConfiguration() {
        return delegateSchemaNode().isConfiguration();
    }

    @Override
    public QName getQName() {
        return delegateSchemaNode().getQName();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return delegateSchemaNode().getPath();
    }

    @Override
    public Status getStatus() {
        return delegateSchemaNode().getStatus();
    }

    @Override
    public Optional<String> getDescription() {
        return delegateSchemaNode().getDescription();
    }

    @Override
    public Optional<String> getReference() {
        return delegateSchemaNode().getReference();
    }

    @Deprecated
    @Override
    public boolean isAugmenting() {
        return delegateSchemaNode().isAugmenting();
    }

    @Deprecated
    @Override
    public boolean isAddedByUses() {
        return delegateSchemaNode().isAddedByUses();
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return delegateSchemaNode().getWhenCondition();
    }

    @Override
    public boolean isMandatory() {
        return delegateSchemaNode().isConfiguration();
    }

    @Override
    public Collection<? extends MustDefinition> getMustConstraints() {
        return delegateSchemaNode().getMustConstraints();
    }

    @Override
    public Collection<? extends UnknownSchemaNode> getUnknownSchemaNodes() {
        return delegateSchemaNode().getUnknownSchemaNodes();
    }

    @Override
    public AnyxmlEffectiveStatement asEffectiveStatement() {
        return delegateSchemaNode().asEffectiveStatement();
    }

    @Override
    public String toString() {
        return YangModeledAnyxmlEffectiveStatementImpl.class.getSimpleName() + "["
               + "qname=" + getQName()
               + ", path=" + getPath()
               + "]";
    }
}
