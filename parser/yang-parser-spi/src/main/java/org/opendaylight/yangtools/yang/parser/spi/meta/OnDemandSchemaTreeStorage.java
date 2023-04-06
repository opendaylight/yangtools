/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.NamespaceBehaviours;

/**
 * Interface implemented by {@link NamespaceStorage}s which support dynamic addition of child elements as they
 * are requested. This means that such a node can, defer creation of child namespace storage nodes, in effect lazily
 * expanding this namespace on an if-needed basis.
 */
@Beta
public interface OnDemandSchemaTreeStorage extends NamespaceStorage {
    /**
     * Request that a new member of this node's schema tree statement be added. Implementations are required to
     * perform lookup in their internal structure and create a child if tractable. Resulting node is expected to
     * have been registered with local storage, so that it is accessible through
     * {@link #getFromLocalStorage(ParserNamespace, Object)}.
     *
     * <p>
     * This method must not change its mind about a child's presence -- once it returns non-present, it has to be
     * always returning non-present.
     *
     * <p>
     * The results produced by this method are expected to be consistent with
     * {@link SchemaTreeAwareEffectiveStatement#findSchemaTreeNode(QName)} and
     * {@link NamespaceBehaviours#SCHEMA_TREE}'s {@code getFrom(NamespaceStorage, QName)}.
     *
     * @param qname node identifier of the child being requested
     * @return Requested child, if it is present.
     * @throws NullPointerException in {@code qname} is null
     */
    <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
        @Nullable StmtContext<QName, D, E> requestSchemaTreeChild(QName qname);
}