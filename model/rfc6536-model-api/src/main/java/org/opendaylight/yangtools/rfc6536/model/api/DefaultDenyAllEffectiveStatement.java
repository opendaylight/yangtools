/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6536.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Effective statement representation of 'default-deny-all' extension defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc6536">RFC6536</a>.
 */
public interface DefaultDenyAllEffectiveStatement extends EffectiveStatement<Empty, @NonNull DefaultDenyAllStatement> {
    @Override
    default StatementDefinition<Empty, @NonNull DefaultDenyAllStatement, ?> statementDefinition() {
        return DefaultDenyAllStatement.DEF;
    }
}
