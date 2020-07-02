/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.unique;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;

class EmptyUniqueEffectiveStatement extends DefaultArgument<Set<Descendant>, UniqueStatement>
        implements UniqueEffectiveStatement, UniqueConstraint {
    EmptyUniqueEffectiveStatement(final UniqueStatement declared) {
        super(declared);
    }

    @Override
    public Set<Descendant> getTag() {
        // FIXME: YANGTOOLS-908: verifyNotNull() should not be needed here
        return verifyNotNull(argument());
    }
}
