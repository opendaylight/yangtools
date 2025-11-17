/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Effective statement representation of 'get-filter-element-attributes' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6241">RFC6241</a>.
 */
@NonNullByDefault
public interface GetFilterElementAttributesEffectiveStatement
        extends UnknownEffectiveStatement<Empty, GetFilterElementAttributesStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return GetFilterElementAttributesStatement.DEFINITION;
    }
}
