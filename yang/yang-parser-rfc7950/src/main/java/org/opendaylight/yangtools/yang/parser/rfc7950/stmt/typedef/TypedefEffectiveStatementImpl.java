/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSource;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveSchemaNode;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TypedefEffectiveStatementImpl extends AbstractEffectiveSchemaNode<TypedefStatement> implements
        TypedefEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(TypedefEffectiveStatementImpl.class);

    private final @NonNull TypeDefinition<?> typeDefinition;

    private volatile TypeEffectiveStatement<TypeStatement> typeStatement;

    TypedefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        final TypeEffectiveStatement<?> typeEffectiveStmt = firstSubstatementOfType(TypeEffectiveStatement.class);
        final DerivedTypeBuilder<?> builder = DerivedTypes.derivedTypeBuilder(typeEffectiveStmt.getTypeDefinition(),
            ctx.getSchemaPath().get());
        String dflt = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement) {
                dflt = ((DefaultEffectiveStatement) stmt).argument();
                builder.setDefaultValue(dflt);
            } else if (stmt instanceof DescriptionEffectiveStatement) {
                builder.setDescription(((DescriptionEffectiveStatement)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatement) {
                builder.setReference(((ReferenceEffectiveStatement)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatement) {
                builder.setStatus(((StatusEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatement) {
                builder.setUnits(((UnitsEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnknownSchemaNode) {
                // FIXME: should not directly implement, I think
                builder.addUnknownSchemaNode((UnknownSchemaNode)stmt);
            } else {
                if (!(stmt instanceof TypeEffectiveStatement)) {
                    LOG.debug("Ignoring statement {}", stmt);
                }
            }
        }

        SourceException.throwIf(
            EffectiveStmtUtils.hasDefaultValueMarkedWithIfFeature(ctx.getRootVersion(), typeEffectiveStmt, dflt),
            ctx.getStatementSourceReference(),
            "Typedef '%s' has default value '%s' marked with an if-feature statement.", ctx.getStatementArgument(),
            dflt);

        typeDefinition = builder.build();
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        return typeDefinition;
    }

    @Override
    public TypeEffectiveStatement<TypeStatement> asTypeEffectiveStatement() {
        TypeEffectiveStatement<TypeStatement> ret = typeStatement;
        if (ret == null) {
            synchronized (this) {
                ret = typeStatement;
                if (ret == null) {
                    typeStatement = ret = new ProxyTypeEffectiveStatement();
                }
            }
        }

        return ret;
    }

    private final class ProxyTypeEffectiveStatement implements TypeEffectiveStatement<TypeStatement> {
        @Override
        public TypeStatement getDeclared() {
            return null;
        }

        @Override
        public <K, V, N extends IdentifierNamespace<K, V>> V get(@Nonnull final Class<N> namespace,
                @Nonnull final K identifier) {
            return TypedefEffectiveStatementImpl.this.get(namespace, identifier);
        }

        @Override
        public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(@Nonnull final Class<N> namespace) {
            return TypedefEffectiveStatementImpl.this.getAll(namespace);
        }

        @Nonnull
        @Override
        public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
            return TypedefEffectiveStatementImpl.this.effectiveSubstatements();
        }

        @Override
        public StatementDefinition statementDefinition() {
            return YangStmtMapping.TYPE;
        }

        @Override
        public String argument() {
            return getQName().getLocalName();
        }

        @Override
        public StatementSource getStatementSource() {
            return StatementSource.CONTEXT;
        }

        @Override
        public TypeDefinition<?> getTypeDefinition() {
            return TypedefEffectiveStatementImpl.this.getTypeDefinition();
        }
    }
}
