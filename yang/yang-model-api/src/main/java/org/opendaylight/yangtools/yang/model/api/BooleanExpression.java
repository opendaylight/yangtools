/*
 * Copyright (c) 2016 Inocybe Technologies and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import java.util.Collection;

import org.opendaylight.yangtools.yang.common.QName;

/**
 * Contains methods for evaluating a boolean expression result and
 * for getting boolean typed variable names
 *
 */
public interface BooleanExpression {

    /**
     * The result of evaluating the boolean expression from the statements raw argument.
     * @return <code>true</code> is evaluates to true else
     *         <code>false</code>
     */
    boolean eval();

    /**
     * Get all or specific subset of boolean typed variable names that map to feature names.
     * @return a list of variable names
     */
    Collection<? extends QName> getNames();

    /**
     * @return the string equivalent of boolean expression
     */
    String toString();
}
