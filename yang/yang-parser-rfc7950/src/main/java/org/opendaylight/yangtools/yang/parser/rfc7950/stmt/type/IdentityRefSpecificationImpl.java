/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.type;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.IdentityRefSpecification;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawStringArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class IdentityRefSpecificationImpl extends WithSubstatements implements IdentityRefSpecification {

    /**
     * Deprecated.
     *
     * @deprecated Use {@link IdentityRefSpecificationImpl#IdentityRefSpecificationImpl(String, ImmutableList)} instead
     */
    @Deprecated(forRemoval = true)
    IdentityRefSpecificationImpl(final StmtContext<String, ?, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context.coerceRawStatementArgument(), substatements);
    }

    IdentityRefSpecificationImpl(final String rawArgument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(rawArgument, substatements);
    }
}
