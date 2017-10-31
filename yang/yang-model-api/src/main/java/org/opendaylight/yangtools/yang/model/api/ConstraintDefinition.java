/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

/**
 * Contains method which returns various data constraints for some YANG element
 * (e.g. min or max number of elements). Not all constraints are allowed for all
 * YANG element therefore if the constraint doesn't have sense for some element
 * then the method returns <code>null</code> value.
 */
public interface ConstraintDefinition extends MustConstraintAware, WhenConditionAware {
    /**
     * Expreses if the presence of the data element for which this constraint is
     * specified is|isn't required.
     *
     * <p>
     * Contains the value of the <b>mandatory</b> YANG substatement.
     * It is used with YANG statements leaf, choice, anyxml, deviate.
     *
     * @return boolean value:
     *         <ul>
     *         <li>true - if <code>mandatory</code> YANG keyword argument =
     *         <i>true</i></li>
     *         <li>false - if <code>mandatory</code> YANG keyword argument =
     *         <i>false</i></li>
     *         </ul>
     */
    boolean isMandatory();
}
