/*
 * Copyright (c) 2018, 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6643.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

@NonNullByDefault
public interface DisplayHintStatement extends UnknownStatement<String> {
    /**
     * The definition of {@code smiv2:max-access} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(
        DisplayHintStatement.class, DisplayHintEffectiveStatement.class,
        IetfYangSmiv2Constants.RFC6643_MODULE, "display-hint", "format");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
