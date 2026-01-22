/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.Default;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OpaqueDataSchemaNodeMixin;

public class EmptyAnyxmlEffectiveStatement extends Default<QName, @NonNull AnyxmlStatement>
        implements AnyxmlEffectiveStatement, AnyxmlSchemaNode, OpaqueDataSchemaNodeMixin<@NonNull AnyxmlStatement> {
    private final @NonNull QName argument;
    private final int flags;

    public EmptyAnyxmlEffectiveStatement(final @NonNull AnyxmlStatement declared, final QName argument,
            final int flags) {
        super(declared);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    public EmptyAnyxmlEffectiveStatement(final EmptyAnyxmlEffectiveStatement original, final QName argument,
            final int flags) {
        super(original);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    @Override
    public final QName argument() {
        return argument;
    }

    @Override
    public final int flags() {
        return flags;
    }

    @Override
    public final AnyxmlEffectiveStatement asEffectiveStatement() {
        return this;
    }
}
