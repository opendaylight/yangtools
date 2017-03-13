/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * Contains the method for getting the data from the YANG <code>pattern</code>
 * which is substatement of <code>type</code> statement.
 *
 */
public interface PatternConstraint extends ConstraintMetaDefinition {

    /**
     * Returns a java regular expression (pattern).
     *
     * @return string with java regular expression which is equal to the argument of
     *         the YANG <code>pattern</code> substatement
     */
    String getRegularExpression();

    /**
     * Returns a raw regular expression as it was declared in a source.
     *
     * @return argument of pattern statement as it was declared in a source.
     */
    // FIXME: version 2.0.0: make this method non-default
    default String getRawRegularExpression() {
        return getRegularExpression();
    }
}
