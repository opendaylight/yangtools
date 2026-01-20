/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredMultiElementStatement;

/**
 * A statement constraned by {@link MinElementsStatement} and {@link MaxElementsStatement}: either a
 * {@link LeafListStatement} or a {@link ListStatement}.
 */
public sealed interface MultiElementDefinitionStatement
    extends DataDefinitionStatement, DeclaredMultiElementStatement<QName>, ConfigStatementAwareDeclaredStatement<QName>,
            MustStatementAwareDeclaredStatement<QName>
    permits LeafListStatement, ListStatement {
    // Nothing else
}
