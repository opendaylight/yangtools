/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RegularKeyStatement extends AbstractKeyStatement {
    private final @NonNull Object substatements;

    RegularKeyStatement(final StmtContext<Collection<SchemaNodeIdentifier>, ?, ?> context,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(context);
        this.substatements = maskSubstatements(substatements);
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskSubstatements(substatements);
    }
}
