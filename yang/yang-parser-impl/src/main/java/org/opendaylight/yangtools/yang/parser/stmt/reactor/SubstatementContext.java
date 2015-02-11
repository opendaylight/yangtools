/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class SubstatementContext<A,D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
    extends StatementContextBase<A,D,E> {

    private final StatementContextBase<?,?,?> parent;

    SubstatementContext(StatementContextBase<?,?,?> parent,ContextBuilder<A,D,E> builder) throws SourceException {
        super(builder);
        this.parent = Preconditions.checkNotNull(parent, "Parent must not be null");
    }

    @Override
    public StatementContextBase<?,?,?> getParentContext() {
        return parent;
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public Registry getBehaviourRegistry() {
        return parent.getBehaviourRegistry();
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

}
