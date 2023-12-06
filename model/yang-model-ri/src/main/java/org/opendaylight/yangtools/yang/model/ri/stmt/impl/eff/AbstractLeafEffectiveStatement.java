/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafStatement;
import org.opendaylight.yangtools.yang.model.ri.type.ConcreteTypes;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MandatoryMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;

public abstract class AbstractLeafEffectiveStatement
        extends AbstractDeclaredEffectiveStatement.Default<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DataSchemaNodeMixin<LeafStatement>,
            MandatoryMixin<QName, LeafStatement>, MustConstraintMixin<QName, LeafStatement> {
    private static final VarHandle TYPE;

    static {
        try {
            TYPE = MethodHandles.lookup().findVarHandle(AbstractLeafEffectiveStatement.class, "type",
                TypeDefinition.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Object substatements;
    private final int flags;

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile TypeDefinition<?> type;

    AbstractLeafEffectiveStatement(final LeafStatement declared, final int flags,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.flags = flags;
        this.substatements = maskList(substatements);
    }

    AbstractLeafEffectiveStatement(final AbstractLeafEffectiveStatement original, final int flags) {
        super(original);
        this.flags = flags;
        substatements = original.substatements;
    }

    @Override
    public final ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final LeafEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public final TypeDefinition<?> getType() {
        final var local = (TypeDefinition<?>) TYPE.getAcquire(this);
        return local != null ? local : loadType();
    }

    private TypeDefinition<?> loadType() {
        final var ret = ConcreteTypes.typeOf(this);
        final var witness = (TypeDefinition<?>) TYPE.compareAndExchangeRelease(this, null, ret);
        return witness != null ? witness : ret;
    }
}
