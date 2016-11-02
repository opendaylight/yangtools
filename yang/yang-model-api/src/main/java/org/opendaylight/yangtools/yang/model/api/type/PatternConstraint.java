/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * Contains the method for getting the data from the YANG <code>pattern</code>
 * which is substatement of <code>type</code> statement.
 *
 */
public interface PatternConstraint extends ConstraintMetaDefinition {

    /**
     * Returns a regular expression (pattern).
     *
     * @return string with regular expression which is equal to the argument of
     *         the YANG <code>pattern</code> substatement
     */
    String getRegularExpression();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020)
     * implementations of PatternConstraint which do not support modifier statement.
     * YANG pattern statement has been changed in YANG 1.1 (RFC7950) and now allows modifier statement.
     *
     * @return enum constant which represents the value of modifier statement
     */
     // FIXME: version 2.0.0: make this method non-default
    @Nullable default ModifierKind getModifier() {
        return null;
    }
}
