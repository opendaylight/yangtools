/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.modifier;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.stmt.ModifierStatement;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Class providing necessary support for processing YANG 1.1 Modifier statement.
 */
@Beta
final class ModifierStatementImpl extends WithArgument<ModifierKind> implements ModifierStatement {
    ModifierStatementImpl(final StmtContext<ModifierKind, ModifierStatement, ?> context) {
        super(context);
    }
}
