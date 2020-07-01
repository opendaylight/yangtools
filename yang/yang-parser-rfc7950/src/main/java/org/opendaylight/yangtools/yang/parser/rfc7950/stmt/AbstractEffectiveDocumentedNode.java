/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
// FIXME: 6.0.0: fold this into AbstractEffectiveDocumentedNodeWithStatus
public abstract class AbstractEffectiveDocumentedNode<A, D extends DeclaredStatement<A>>
        extends DeclaredEffectiveStatementBase<A, D> implements DocumentedNode {
    private final @Nullable String description;
    private final @Nullable String reference;

    protected AbstractEffectiveDocumentedNode(final StmtContext<A, D, ?> ctx) {
        super(ctx);
        description = findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null);
        reference = findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class).orElse(null);
    }

    @Override
    public final Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public final Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }

    protected final @Nullable String nullableDescription() {
        return description;
    }

    protected final @Nullable String nullableReference() {
        return reference;
    }
}
