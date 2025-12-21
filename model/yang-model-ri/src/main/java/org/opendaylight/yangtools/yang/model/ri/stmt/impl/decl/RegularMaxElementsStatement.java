/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MaxElementsValue;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.WithArgument.WithSubstatements;

public final class RegularMaxElementsStatement extends WithSubstatements<MaxElementsValue>
        implements MaxElementsStatement {
    public RegularMaxElementsStatement(final String rawArgument, final MaxElementsValue argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(rawArgument, argument, substatements);
    }
}
