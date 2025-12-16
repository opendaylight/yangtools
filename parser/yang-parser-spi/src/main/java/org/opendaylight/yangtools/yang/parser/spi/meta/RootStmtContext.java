/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.parser.spi.ParserNamespaces;

/**
 * A marker interface for a {@link StmtContext}. Useful for operations which make assumption about the context's
 * hierarchy.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public interface RootStmtContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StmtContext<A, D, E> {
    /**
     * A marker interface for a {@link StmtContext.Mutable}. Useful for operations which make assumption about the
     * context's hierarchy.
     *
     * @param <A> Argument type
     * @param <D> Declared Statement representation
     * @param <E> Effective Statement representation
     */
    interface Mutable<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
            extends StmtContext.Mutable<A, D, E>, RootStmtContext<A, D, E> {
        @Override
        RootStmtContext.Mutable<?, ?, ?> getRoot();
    }

    // FIXME: 15.0.0: make this method non-default
    // FIXME: 15.0.0: throw VerifyException or similar this-is-a-coding-error exception
    @Override
    default QNameModule definingModule() {
        final var declaredRepr = publicDefinition().getDeclaredRepresentationClass();
        final StmtContext<?, ?, ?> module;
        if (ModuleStatement.class.isAssignableFrom(declaredRepr)) {
            module = this;
        } else if (SubmoduleStatement.class.isAssignableFrom(declaredRepr)) {
            final var belongsTo = namespace(ParserNamespaces.BELONGSTO_PREFIX_TO_MODULECTX);
            if (belongsTo == null || belongsTo.isEmpty()) {
                throw new IllegalArgumentException(this + " does not have belongs-to linkage resolved");
            }
            module = belongsTo.values().iterator().next();
        } else {
            throw new IllegalArgumentException("Unsupported root " + this);
        }

        final var ret = namespaceItem(ParserNamespaces.MODULECTX_TO_QNAME, module);
        if (ret == null) {
            throw new IllegalArgumentException("Failed to look up QNameModule for " + module + " in " + this);
        }
        return ret;
    }
}
