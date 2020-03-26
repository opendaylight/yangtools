/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.typedef;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;
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
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.SchemaNodeMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class TypedefEffectiveStatementImpl extends Default<QName, TypedefStatement>
        implements TypedefEffectiveStatement, SchemaNodeMixin<QName, TypedefStatement> {
    private static final Logger LOG = LoggerFactory.getLogger(TypedefEffectiveStatementImpl.class);

    private static final VarHandle TYPE_DEFINITION;
    private static final VarHandle TYPE_STATEMENT;

    static {
        final Lookup lookup = MethodHandles.lookup();
        try {
            TYPE_DEFINITION = lookup.findVarHandle(TypedefEffectiveStatementImpl.class, "typeDefinition",
                TypeDefinition.class);
            TYPE_STATEMENT = lookup.findVarHandle(TypedefEffectiveStatementImpl.class, "typeStatement",
                ProxyTypeEffectiveStatement.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    private final int flags;

    // Accessed via TYPE_DEFINITION
    @SuppressWarnings("unused")
    private volatile TypeDefinition<?> typeDefinition;
    // Accessed via TYPE_STATEMENT
    @SuppressWarnings("unused")
    private volatile ProxyTypeEffectiveStatement typeStatement;

    TypedefEffectiveStatementImpl(final TypedefStatement declared, final SchemaPath path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.path = requireNonNull(path);
        this.flags = flags;
        this.substatements = maskList(substatements);
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public @NonNull QName argument() {
        return getQName();
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public TypeDefinition<?> getTypeDefinition() {
        final TypeDefinition<?> existing = (TypeDefinition<?>) TYPE_DEFINITION.getAcquire(this);
        return existing != null ? existing : loadTypeDefinition();
    }

    @Override
    public TypeEffectiveStatement<TypeStatement> asTypeEffectiveStatement() {
        final ProxyTypeEffectiveStatement local = (ProxyTypeEffectiveStatement) TYPE_STATEMENT.getAcquire(this);
        return local != null ? local : loadTypeStatement();
    }

    private @NonNull TypeDefinition<?> loadTypeDefinition() {
        final TypeEffectiveStatement<?> type = findFirstEffectiveSubstatement(TypeEffectiveStatement.class).get();
        final DerivedTypeBuilder<?> builder = DerivedTypes.derivedTypeBuilder(type.getTypeDefinition(), path);

        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement) {
                builder.setDefaultValue(((DefaultEffectiveStatement) stmt).argument());
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
            } else if (!(stmt instanceof TypeEffectiveStatement)) {
                LOG.debug("Ignoring statement {}", stmt);
            }
        }

        final TypeDefinition<?> created = builder.build();
        final Object witness = TYPE_DEFINITION.compareAndExchangeRelease(this, null, created);
        return witness == null ? created : (TypeDefinition<?>) witness;
    }

    private @NonNull ProxyTypeEffectiveStatement loadTypeStatement() {
        final ProxyTypeEffectiveStatement created = new ProxyTypeEffectiveStatement();
        final Object witness = TYPE_STATEMENT.compareAndExchangeRelease(this, null, created);
        return witness == null ? created : (ProxyTypeEffectiveStatement) witness;
    }

    private final class ProxyTypeEffectiveStatement implements TypeEffectiveStatement<TypeStatement> {
        @Override
        public TypeStatement getDeclared() {
            return null;
        }

        @Override
        public <K, V, N extends IdentifierNamespace<K, V>> Optional<? extends V> get(final Class<N> namespace,
                final K identifier) {
            return TypedefEffectiveStatementImpl.this.get(namespace, identifier);
        }

        @Override
        public <K, V, N extends IdentifierNamespace<K, V>> Map<K, V> getAll(final Class<N> namespace) {
            return TypedefEffectiveStatementImpl.this.getAll(namespace);
        }

        @Override
        public Collection<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
            return TypedefEffectiveStatementImpl.this.effectiveSubstatements();
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
