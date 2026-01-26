/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement corresponding to config's "posix-pattern" .
 *
 * @author Martin Bobak
 */
public interface OpenConfigPosixPatternStatement extends UnknownStatement<String> {
    /**
     * The definition of {@code oc-ext:posix-pattern} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<String, @NonNull OpenConfigPosixPatternStatement,
        @NonNull OpenConfigPosixPatternEffectiveStatement> DEF = StatementDefinition.of(
            OpenConfigPosixPatternStatement.class, OpenConfigPosixPatternEffectiveStatement.class,
            OpenConfigConstants.REGEXP_POSIX_MODULE, "posix-pattern",
            ArgumentDefinition.of(String.class, OpenConfigConstants.REGEXP_POSIX_MODULE, "pattern"));

    @Override
    default StatementDefinition<String, ?, ?> statementDefinition() {
        return DEF;
    }
}
