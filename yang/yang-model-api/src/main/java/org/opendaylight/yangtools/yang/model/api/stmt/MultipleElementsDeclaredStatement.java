/*
 * Copyright (c) 2018 Pantheon Technoglogies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Optional;

public interface MultipleElementsDeclaredStatement extends DataDefinitionStatement, MultipleElementsGroup {
    @Override
    default MinElementsStatement getMinElements() {
        final Optional<MinElementsStatement> opt = findFirstDeclaredSubstatement(MinElementsStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default MaxElementsStatement getMaxElements() {
        final Optional<MaxElementsStatement> opt = findFirstDeclaredSubstatement(MaxElementsStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }

    @Override
    default OrderedByStatement getOrderedBy() {
        final Optional<OrderedByStatement> opt = findFirstDeclaredSubstatement(OrderedByStatement.class);
        return opt.isPresent() ? opt.get() : null;
    }
}
