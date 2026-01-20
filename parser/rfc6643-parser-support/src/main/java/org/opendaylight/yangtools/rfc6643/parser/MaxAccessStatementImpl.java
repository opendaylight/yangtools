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
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccess;
import org.opendaylight.yangtools.rfc6643.model.api.MaxAccessStatement;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement;

final class MaxAccessStatementImpl extends AbstractDeclaredStatement<MaxAccess> implements MaxAccessStatement {
    private final @NonNull Object substatements;
    private final @NonNull MaxAccess argument;

    MaxAccessStatementImpl(final @NonNull MaxAccess argument,
            final @NonNull ImmutableList<? extends DeclaredStatement<?>> substatements) {
        this.argument = requireNonNull(argument);
        this.substatements = maskList(substatements);
    }

    @Override
    public MaxAccess argument() {
        return argument;
    }

    @Override
    public @NonNull String rawArgument() {
        return argument.stringLiteral();
    }

    @Override
    public ImmutableList<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}
