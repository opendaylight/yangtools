/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement corresponding to config's "regexp-posix" .
 *
 * @author Martin Bobak
 */
@NonNullByDefault
public interface OpenConfigRegexpPosixStatement extends UnknownStatement<Empty> {
    /**
     * The definition of {@code oc-ext:regex-posix} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(
        OpenConfigRegexpPosixStatement.class, OpenConfigRegexpPosixEffectiveStatement.class,
        OpenConfigConstants.REGEXP_POSIX_MODULE, "regexp-posix");

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
