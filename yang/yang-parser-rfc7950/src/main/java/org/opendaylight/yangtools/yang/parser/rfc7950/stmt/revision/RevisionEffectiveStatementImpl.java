/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.revision;

import java.util.Optional;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ReferenceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.DeclaredEffectiveStatementBase.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RevisionEffectiveStatementImpl extends WithArgument<Revision, RevisionStatement>
        implements DocumentedNode, RevisionEffectiveStatement {

    private final String reference;
    private final String description;

    RevisionEffectiveStatementImpl(final StmtContext<Revision, RevisionStatement, ?> ctx) {
        super(ctx);
        description = findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class).orElse(null);
        reference = findFirstEffectiveSubstatementArgument(ReferenceEffectiveStatement.class).orElse(null);
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    @Override
    public Optional<String> getReference() {
        return Optional.ofNullable(reference);
    }
}
