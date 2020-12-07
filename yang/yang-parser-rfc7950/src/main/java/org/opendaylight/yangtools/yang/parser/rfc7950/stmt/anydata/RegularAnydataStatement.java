/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.anydata;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementSourceReference;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithQNameArgument.WithSubstatements;

/**
 * YANG 1.1 AnyData declared statement implementation.
 */
final class RegularAnydataStatement extends WithSubstatements implements AnydataStatement {
    RegularAnydataStatement(final QName argument,
                            final ImmutableList<? extends DeclaredStatement<?>> substatements,
                            final StatementSourceReference sourceReference) {
        super(argument, substatements, sourceReference);
    }
}
