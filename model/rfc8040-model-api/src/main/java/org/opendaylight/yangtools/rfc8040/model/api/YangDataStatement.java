/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'yang-data' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8040#section-8">RFC 8040</a>.
 */
@NonNullByDefault
public interface YangDataStatement extends UnknownStatement<YangDataName> {
    /**
     * The definition of {@code rc:yang-data} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(YangDataConstants.RFC8040_MODULE, "yang-data", "name",
        false, YangDataStatement.class, YangDataEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
