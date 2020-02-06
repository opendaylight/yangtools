/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * An {@link AbstractEffectiveDocumentedNode} which can optionally support {@link SchemaTreeAwareEffectiveStatement}.
 *
 * @param <A> Argument type ({@link Void} if statement does not have argument.)
 * @param <D> Class representing declared version of this statement.
 * @author Robert Varga
 */
@Beta
public abstract class AbstractSchemaEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends AbstractEffectiveDocumentedNode<A, D> {
    protected AbstractSchemaEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);
    }
}
