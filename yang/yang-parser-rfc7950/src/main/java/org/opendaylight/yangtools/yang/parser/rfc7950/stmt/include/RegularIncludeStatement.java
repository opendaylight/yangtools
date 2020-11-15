/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.include;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IncludeStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;

final class RegularIncludeStatement extends WithSubstatements<String> implements IncludeStatement {
    RegularIncludeStatement(final String rawArgument, final String argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(rawArgument, argument, substatements);
    }
}
