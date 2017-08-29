/**
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

abstract class AbstractListConstraintEffectiveStatement<T, D extends DeclaredStatement<List<T>>>
        extends AbstractConstraintEffectiveStatement<List<T>, D> {

    AbstractListConstraintEffectiveStatement(final StmtContext<List<T>, D, ?> ctx) {
        super(ctx);
    }

    @Override
    final List<T> createConstraints(final List<T> argument) {
        return ImmutableList.copyOf(argument);
    }
}
