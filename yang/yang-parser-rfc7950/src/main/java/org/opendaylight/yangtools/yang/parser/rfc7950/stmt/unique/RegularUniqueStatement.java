/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;

import com.google.common.collect.ImmutableList;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RegularUniqueStatement extends WithSubstatements<Set<Descendant>> implements UniqueStatement {
    RegularUniqueStatement(final StmtContext<Set<Descendant>, ?, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context, substatements);
    }
}
