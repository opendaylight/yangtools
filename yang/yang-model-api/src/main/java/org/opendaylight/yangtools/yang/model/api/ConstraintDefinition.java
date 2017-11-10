/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import javax.annotation.Nullable;

/**
 * Contains method which returns various data constraints for some YANG element
 * (e.g. min or max number of elements). Not all constraints are allowed for all
 * YANG element therefore if the constraint doesn't have sense for some element
 * then the method returns <code>null</code> value.
 */
public interface ConstraintDefinition {
    /**
     * Returns the minimum required number of data elements for node where this
     * constraint is specified.
     *
     * <p>
     * The returning value equals to value of the argument of the
     * <b>min-elements</b> YANG substatement.
     * It is used with YANG statements leaf-list, list, deviate.
     *
     * @return integer with minimal number of elements, or null if no minimum is defined
     */
    @Nullable Integer getMinElements();

    /**
     * Returns the maximum admissible number of data elements for node where
     * this constraint is specified.
     *
     * <p>
     * The returning value equals to value of the argument of the
     * <b>max-elements</b> YANG substatement.
     * It is used with YANG statements leaf-list, list, deviate.
     *
     * @return integer with maximum number of elements, or null if no maximum is defined
     */
    @Nullable Integer getMaxElements();
}
