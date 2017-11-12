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

    @Override
    public boolean isConfiguration() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public QName getQName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SchemaPath getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getDescription() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<String> getReference() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAugmenting() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAddedByUses() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isMandatory() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Collection<MustDefinition> getMustConstraints() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AnyxmlStatement getDeclared() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> V get(final Class<N> namespace, final K identifier) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        // TODO Auto-generated method stub
        return null;
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
}
