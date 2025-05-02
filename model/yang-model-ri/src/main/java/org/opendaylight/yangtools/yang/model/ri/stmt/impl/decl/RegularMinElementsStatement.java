/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.decl;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredStatement.ArgumentToString.WithSubstatements;

public final class RegularMinElementsStatement extends WithSubstatements<MinElementsArgument>
        implements MinElementsStatement {
    public RegularMinElementsStatement(final MinElementsArgument argument,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(argument, substatements);
    }
}
