/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.augment;

import com.google.common.collect.ImmutableList;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredStatement.WithArgument.WithSubstatements;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class RegularAugmentStatement extends WithSubstatements<SchemaNodeIdentifier> implements AugmentStatement {
    RegularAugmentStatement(final StmtContext<SchemaNodeIdentifier, ?, ?> ctx,
            final ImmutableList<? extends DeclaredStatement<?>> substatements) {
        super(ctx, substatements);
    }
}
