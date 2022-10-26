/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypedefEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractUndeclaredEffectiveStatement.DefaultWithDataTree;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.OperationContainerMixin;

abstract class AbstractUndeclaredOperationContainer<D extends DeclaredStatement<QName>>
        extends DefaultWithDataTree<QName, D>
        implements TypedefAwareEffectiveStatement<QName, D>, OperationContainerMixin<D> {
    private final @NonNull QName argument;
    private final int flags;

    AbstractUndeclaredOperationContainer(final ImmutableList<? extends EffectiveStatement<?, ?>> substatements,
            final QName argument, final int flags) {
        super(substatements);
        this.argument = requireNonNull(argument);
        this.flags = flags;
    }

    AbstractUndeclaredOperationContainer(final AbstractUndeclaredOperationContainer<D> original, final QName argument,
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
    public final DataSchemaNode dataChildByName(final QName name) {
        return dataSchemaNode(name);
    }

    @Override
    public final Map<QName, TypedefEffectiveStatement> typedefNamespace() {
        return Map.of();
    }
}
