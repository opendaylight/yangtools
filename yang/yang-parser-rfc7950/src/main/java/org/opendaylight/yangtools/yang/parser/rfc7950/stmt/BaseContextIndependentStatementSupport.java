/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

@Beta
public abstract class BaseContextIndependentStatementSupport<A, D extends DeclaredStatement<A>,
        E extends EffectiveStatement<A, D>> extends BaseStatementSupport<A, D, E> {

    protected BaseContextIndependentStatementSupport(final StatementDefinition publicDefinition) {
        super(publicDefinition);
    }

    @Override
    protected final boolean isContextIndependent() {
        return true;
    }
}
