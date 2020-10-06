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
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;

final class RegularKeyStatement extends AbstractKeyStatement {
    private final @NonNull Object substatements;

    RegularKeyStatement(final @NonNull String rawArgument, final @NonNull Set<QName> argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(rawArgument, argument);
        this.substatements = maskList(substatements);
    }

    @Override
    public Collection<? extends DeclaredStatement<?>> declaredSubstatements() {
        return unmaskList(substatements);
    }
}
