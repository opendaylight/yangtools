/*
 * Copyright (c) 2019 PATHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement representation of 'get-filter-element-attributes' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6241">RFC6241</a>.
 */
@NonNullByDefault
public interface GetFilterElementAttributesStatement extends UnknownStatement<Empty> {
    /**
     * The definition of {@code nc:get-filter-element-attributes} statement.
     *
     * @since 15.0.0
     */
    StatementDefinition DEFINITION = StatementDefinition.of(NetconfConstants.RFC6241_MODULE,
        "get-filter-element-attributes", GetFilterElementAttributesStatement.class,
        GetFilterElementAttributesEffectiveStatement.class);

    @Override
    default StatementDefinition statementDefinition() {
        return DEFINITION;
    }
}
