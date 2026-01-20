/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * A statement constraned by {@link MinElementsStatement} and {@link MaxElementsStatement}: either a
 * {@link LeafListStatement} or a {@link ListStatement}.
 */
@Beta
public sealed interface ElementDefinitionStatement
        extends DataDefinitionStatement,
                ConfigStatementAwareDeclaredStatement<QName>,
                MustStatementAwareDeclaredStatement<QName>
        permits LeafListStatement, ListStatement {

    default @NonNull Optional<MinElementsStatement> getMinElements() {
        return findFirstDeclaredSubstatement(MinElementsStatement.class);
    }

    default @NonNull Optional<MaxElementsStatement> getMaxElements() {
        return findFirstDeclaredSubstatement(MaxElementsStatement.class);
    }

    default @NonNull Optional<OrderedByStatement> getOrderedBy() {
        return findFirstDeclaredSubstatement(OrderedByStatement.class);
    }
}
