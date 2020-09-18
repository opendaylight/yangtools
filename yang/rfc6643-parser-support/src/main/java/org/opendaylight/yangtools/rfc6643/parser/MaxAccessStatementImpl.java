/*
 * Copyright (c) 2016, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.parser;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement;

final class MaxAccessStatementImpl extends AbstractDeclaredStatement<MaxAccess> implements MaxAccessStatement {
    private final @NonNull Object substatements;
    private final MaxAccess argument;

    MaxAccessStatementImpl(final MaxAccess argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        this.argument = requireNonNull(argument);
        this.substatements = maskList(substatements);
    }

    @Override
    public MaxAccess argument() {
        return argument;
    }

    @Override
    public String rawArgument() {
        return argument.stringLiteral();
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}
