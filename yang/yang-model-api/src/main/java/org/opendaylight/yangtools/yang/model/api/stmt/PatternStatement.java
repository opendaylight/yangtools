/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;

public interface PatternStatement extends DeclaredStatement<PatternConstraint>, DocumentedConstraintGroup {

    @Nonnull
    PatternConstraint getValue();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020)
     * implementation of PatternStatement which does not support modifier statement.
     * YANG pattern statement has been changed in YANG 1.1 (RFC7950) and now allows modifier statement.
     *
     * @return modifier statement
     */
     // FIXME: version 2.0.0: make this method non-default
    @Nullable default ModifierStatement getModifierStatement() {
        return null;
    }
}
