/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'default-deny-write' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6536">RFC6536</a>.
 */
@NonNullByDefault
public interface DefaultDenyWriteStatement extends UnknownStatement<Empty> {
    /**
     * The definition of {@code nacm:default-deny-all} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.noArg(NACMConstants.RFC6536_MODULE, "default-deny-write",
        DefaultDenyWriteStatement.class, DefaultDenyWriteEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
