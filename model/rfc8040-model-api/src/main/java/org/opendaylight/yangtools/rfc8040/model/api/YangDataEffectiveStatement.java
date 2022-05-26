/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.DataTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownEffectiveStatement;

/**
 * Effective statement representation of 'yang-data' extension defined in
 * <a href="https://tools.ietf.org/html/rfc8040#section-8">RFC 8040</a>.
 */
@Beta
public interface YangDataEffectiveStatement extends UnknownEffectiveStatement<String, YangDataStatement>,
        DataTreeAwareEffectiveStatement<String, YangDataStatement> {
    @Override
    default StatementDefinition statementDefinition() {
        return YangDataStatements.YANG_DATA;
    }
}
