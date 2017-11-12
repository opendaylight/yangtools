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
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;

final class YangModeledAnyxmlEffectiveStatementImpl implements YangModeledAnyXmlSchemaNode, AnyxmlEffectiveStatement {
    private final AnyxmlEffectiveStatement delegate;
    private final ContainerSchemaNode contentSchema;

    YangModeledAnyxmlEffectiveStatementImpl(final AnyxmlEffectiveStatement delegate,
        final ContainerSchemaNode contentSchema) {
        this.delegate = requireNonNull(delegate);
        this.contentSchema = requireNonNull(contentSchema);
    }

    @Nonnull
    @Override
    public ContainerSchemaNode getSchemaOfAnyXmlData() {
        return contentSchema;
    }

    private AnyXmlSchemaNode delegateSchemaNode() {
        return (AnyXmlSchemaNode) delegate;
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

    @Override
    public boolean isAugmenting() {
        return delegateSchemaNode().isAugmenting();
    }

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
    public Collection<MustDefinition> getMustConstraints() {
        return delegateSchemaNode().getMustConstraints();
    }

    @Override
    public AnyxmlStatement getDeclared() {
        return delegate.getDeclared();
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        return delegate.get(namespace, identifier);
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        return delegate.getAll(namespace);
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return delegate.effectiveSubstatements();
    }

    @Override
    public StatementDefinition statementDefinition() {
        return delegate.statementDefinition();
    }

    @Override
    public QName argument() {
        return delegate.argument();
    }

    @Override
    public StatementSource getStatementSource() {
        return delegate.getStatementSource();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        YangModeledAnyxmlEffectiveStatementImpl other = (YangModeledAnyxmlEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return YangModeledAnyxmlEffectiveStatementImpl.class.getSimpleName() + "["
               + "qname=" + getQName()
               + ", path=" + getPath()
               + "]";
    }
}
