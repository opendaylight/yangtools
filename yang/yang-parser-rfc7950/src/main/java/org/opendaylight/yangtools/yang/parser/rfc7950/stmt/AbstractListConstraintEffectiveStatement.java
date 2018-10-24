/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement.WithArgument;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@Beta
public abstract class AbstractListConstraintEffectiveStatement<T, D extends WithArgument<List<T>>>
        extends AbstractConstraintEffectiveStatement<List<T>, D> {

    protected AbstractListConstraintEffectiveStatement(final StmtContext<List<T>, D, ?> ctx) {
        super(ctx);
    }

    @Override
    protected final List<T> createConstraints(final List<T> argument) {
        return ImmutableList.copyOf(argument);
    }
}
