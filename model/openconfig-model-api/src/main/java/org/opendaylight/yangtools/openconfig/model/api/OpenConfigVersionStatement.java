/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.ArgumentDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

public interface OpenConfigVersionStatement extends UnknownStatement<SemVer> {
    /**
     * The definition of {@code oc-ext:openconfig-version} statement.
     *
     * @since 15.0.0
     */
    @NonNull StatementDefinition<SemVer, @NonNull OpenConfigVersionStatement,
        @NonNull OpenConfigVersionEffectiveStatement> DEF = StatementDefinition.of(
            OpenConfigVersionStatement.class, OpenConfigVersionEffectiveStatement.class,
            QNameModule.of(OpenConfigConstants.MODULE_NAMESPACE), "openconfig-version",
            ArgumentDefinition.of(SemVer.class, QNameModule.of(OpenConfigConstants.MODULE_NAMESPACE), "semver"));

    @Override
    default StatementDefinition<SemVer, ?, ?> statementDefinition() {
        return DEF;
    }
}
