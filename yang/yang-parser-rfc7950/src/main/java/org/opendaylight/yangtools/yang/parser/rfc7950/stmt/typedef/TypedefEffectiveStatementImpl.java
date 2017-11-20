/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import com.google.common.base.Splitter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
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
import org.opendaylight.yangtools.yang.model.api.type.IdentityTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveSchemaNode;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStmtUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.InferenceException;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TypedefEffectiveStatementImpl extends AbstractEffectiveSchemaNode<TypedefStatement> implements
        TypedefEffectiveStatement {
    private static final Logger LOG = LoggerFactory.getLogger(TypedefEffectiveStatementImpl.class);
    private static final Splitter COLON_SPLITTER = Splitter.on(':');

    private final TypeDefinition<?> typeDefinition;

    private TypeEffectiveStatement<TypeStatement> typeStatement;

    TypedefEffectiveStatementImpl(final StmtContext<QName, TypedefStatement, ?> ctx) {
        super(ctx);

        final TypeEffectiveStatement<?> typeEffectiveStmt = firstSubstatementOfType(TypeEffectiveStatement.class);
        final TypeDefinition<?> typedef = typeEffectiveStmt.getTypeDefinition();

        // FIXME: this should live somewhere else, I think
        final BiFunction<StmtContext<QName, TypedefStatement, ?>, String, Object> valueMapper;
        if (typedef instanceof IdentityrefTypeDefinition) {
            valueMapper = this::resolveIdentity;
        } else {
            valueMapper = (ctx, value) -> value;
        }

        final DerivedTypeBuilder<?, Object> builder = (DerivedTypeBuilder<?, Object>) DerivedTypes.derivedTypeBuilder(
            typedef, ctx.getSchemaPath().get());
        String dflt = null;
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement) {
                dflt = ((DefaultEffectiveStatement) stmt).argument();
                builder.setDefaultValue(valueMapper.apply(dflt));
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

    private IdentityTypeDefinition resolveIdentity(final StmtContext<QName, TypedefStatement, ?> ctx,
            final String value) {
        final Iterator<String> it = COLON_SPLITTER.split(value).iterator();
        SourceException.throwIf(!it.hasNext(), ctx.getStatementSourceReference(), "Default value must be non-empty");

        final String first = it.next();
        final QNameModule mod;
        final String localName;
        if (it.hasNext()) {
            mod = InferenceException.throwIfNull(StmtContextUtils.getModuleQNameByPrefix(ctx, first),
                ctx.getStatementSourceReference(), "Prefix %s could not be resolved in default value %s", first, value);
            localName = it.next();
        } else {
            mod = getQName().getModule();
            localName = first;
        }

        final QName identityName = QName.create(mod, localName);

        // FIXME: resolve identity type definition -- I think this needs to live in TypedefStatementSupport, as we need
        //        to hook into IdentityNamespace

        throw new UnsupportedOperationException();
    }

    @Nonnull
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
                    ret = new ProxyTypeEffectiveStatement();
                    typeStatement = ret;
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

        @Nonnull
        @Override
        public StatementDefinition statementDefinition() {
            return YangStmtMapping.TYPE;
        }

        @Override
        public String argument() {
            return getQName().getLocalName();
        }

        @Nonnull
        @Override
        public StatementSource getStatementSource() {
            return StatementSource.CONTEXT;
        }

        @Nonnull
        @Override
        public TypeDefinition<?> getTypeDefinition() {
            return TypedefEffectiveStatementImpl.this.getTypeDefinition();
        }
    }
}
