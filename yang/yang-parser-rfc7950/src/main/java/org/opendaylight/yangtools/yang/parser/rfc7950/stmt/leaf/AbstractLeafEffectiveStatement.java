/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.leaf;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DefaultEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.WhenEffectiveStatement;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypeBuilder;
import org.opendaylight.yangtools.yang.model.util.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement;

abstract class AbstractLeafEffectiveStatement extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DerivableSchemaNode,
            EffectiveStatementMandatoryMixin, EffectiveStatementConfigurationMixin, EffectiveStatementStatusMixin {
    private static final VarHandle TYPE;

    static {
        try {
            TYPE = MethodHandles.lookup().findVarHandle(AbstractLeafEffectiveStatement.class, "type",
                TypeDefinition.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // Variable: either a single substatement or an ImmutableList
    private final @NonNull Object substatements;
    private final @NonNull SchemaPath path;
    private final int flags;

    // Accessed through TYPE VarHandle
    @SuppressWarnings("unused")
    private volatile TypeDefinition<?> type;

    AbstractLeafEffectiveStatement(final LeafStatement declared, final SchemaPath path, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = substatements.size() == 1 ? substatements.get(0) : substatements;
        this.path = requireNonNull(path);
        this.flags = flags;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        if (substatements instanceof ImmutableList) {
            return (ImmutableList<? extends EffectiveStatement<?, ?>>) substatements;
        }
        verify(substatements instanceof EffectiveStatement, "Unexpected substatement %s", substatements);
        return ImmutableList.of((EffectiveStatement<?, ?>) substatements);
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final Optional<String> getDescription() {
        return findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class);
    }

    @Override
    public final Optional<String> getReference() {
        return findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class);
    }

    @Override
    public final Optional<RevisionAwareXPath> getWhenCondition() {
        return findFirstEffectiveSubstatementArgument(WhenEffectiveStatement.class);
    }

    @Override
    // FIXME: move to AbstractEffectiveDocumentedNode?
    public final ImmutableList<UnknownSchemaNode> getUnknownSchemaNodes() {
        return effectiveSubstatements().stream()
                .filter(UnknownSchemaNode.class::isInstance)
                .map(UnknownSchemaNode.class::cast)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public final @NonNull QName argument() {
        return getQName();
    }

    @Override
    public final @NonNull QName getQName() {
        return path.getLastComponent();
    }

    @Override
    public final @NonNull SchemaPath getPath() {
        return path;
    }

    @Override
    public final TypeDefinition<?> getType() {
        final TypeDefinition<?> existing = (TypeDefinition<?>) TYPE.getAcquire(this);
        return existing != null ? existing : loadType();
    }

    private TypeDefinition<?> loadType() {
        // Checked during instantiation
        final TypeEffectiveStatement<?> typeStmt = verifyNotNull(firstSubstatementOfType(TypeEffectiveStatement.class));

        final ConcreteTypeBuilder<?> builder = ConcreteTypes.concreteTypeBuilder(typeStmt.getTypeDefinition(),
            getPath());
        for (final EffectiveStatement<?, ?> stmt : effectiveSubstatements()) {
            if (stmt instanceof DefaultEffectiveStatement) {
                builder.setDefaultValue(((DefaultEffectiveStatement)stmt).argument());
            } else if (stmt instanceof DescriptionEffectiveStatement) {
                builder.setDescription(((DescriptionEffectiveStatement)stmt).argument());
            } else if (stmt instanceof ReferenceEffectiveStatement) {
                builder.setReference(((ReferenceEffectiveStatement)stmt).argument());
            } else if (stmt instanceof StatusEffectiveStatement) {
                builder.setStatus(((StatusEffectiveStatement)stmt).argument());
            } else if (stmt instanceof UnitsEffectiveStatement) {
                builder.setUnits(((UnitsEffectiveStatement)stmt).argument());
            }
        }
        final TypeDefinition<?> loaded = builder.build();

        final Object witness = TYPE.compareAndExchangeRelease(this, null, loaded);
        return witness == null ? loaded : (TypeDefinition<?>) witness;
    }
}
