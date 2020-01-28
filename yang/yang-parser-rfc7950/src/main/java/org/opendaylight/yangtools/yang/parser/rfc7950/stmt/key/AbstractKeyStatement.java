/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.key;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithRawArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractKeyStatement extends WithRawArgument<Collection<SchemaNodeIdentifier>> implements KeyStatement {
    final @NonNull Object argument;

    AbstractKeyStatement(final StmtContext<Collection<SchemaNodeIdentifier>, ?, ?> context) {
        super(context);
        this.argument = KeyStatementSupport.maskCollection(context.coerceStatementArgument());
    }

    @Override
    public final @NonNull Collection<SchemaNodeIdentifier> argument() {
        return KeyStatementSupport.unmaskCollection(argument);
    }
}
