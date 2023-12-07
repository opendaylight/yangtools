/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

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
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DataSchemaNodeMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MandatoryMixin;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.MustConstraintMixin;

public final class UndeclaredLeafEffectiveStatement extends AbstractUndeclaredEffectiveStatement<QName, LeafStatement>
        implements LeafEffectiveStatement, LeafSchemaNode, DataSchemaNodeMixin<LeafStatement>,
                   MandatoryMixin<QName, LeafStatement>, MustConstraintMixin<QName, LeafStatement> {
    private static final VarHandle TYPE;

    static {
        try {
            TYPE = MethodHandles.lookup().findVarHandle(UndeclaredLeafEffectiveStatement.class, "type",
                TypeDefinition.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull Object substatements;
    private final @NonNull QName argument;
    private final int flags;

    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile TypeDefinition<?> type;

    public UndeclaredLeafEffectiveStatement(final QName argument, final int flags,
            final ImmutableList<? extends @NonNull EffectiveStatement<?, ?>> substatements) {
        this.argument = requireNonNull(argument);
        this.flags = flags;
        this.substatements = maskList(substatements);
    }

    public UndeclaredLeafEffectiveStatement(final UndeclaredLeafEffectiveStatement original, final QName argument,
            final int flags) {
        this.argument = requireNonNull(argument);
        this.flags = flags;
        substatements = original.substatements;
    }

    @Override
    public QName argument() {
        return argument;
    }

    @Override
    public int flags() {
        return flags;
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        return unmaskList(substatements);
    }

    @Override
    public LeafEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public TypeDefinition<?> getType() {
        final var local = (TypeDefinition<?>) TYPE.getAcquire(this);
        return local != null ? local : loadType();
    }

    private TypeDefinition<?> loadType() {
        final var ret = ConcreteTypes.typeOf(this);
        final var witness = (TypeDefinition<?>) TYPE.compareAndExchangeRelease(this, null, ret);
        return witness != null ? witness : ret;
    }
}
