/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import org.opendaylight.yangtools.yang.model.api.stmt.WhenStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathExpression.QualifiedBound;

final class EmptyWhenStatement extends WithArgument<QualifiedBound> implements WhenStatement {
    EmptyWhenStatement(final String rawArgument, final QualifiedBound argument) {
        super(rawArgument, argument);
    }
}
