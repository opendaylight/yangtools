/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.builder.api;

import java.util.Set;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;

public interface ConstraintsBuilder extends Builder<ConstraintDefinition> {

    /**
     * Returns module name in which constraint is defined.
     *
     * @return module name
     */
    String getModuleName();

    /**
     *
     * Return line on which constraints were defined.
     *
     * @return line
     */
    int getLine();

    /**
     *
     * Returns number of minimum required elements.
     *
     * This constraint has meaning only if associated node is list or leaf-list.
     *
     * @return number of minimum required elements.
     */
    Integer getMinElements();

    /**
     *
     * Sets number of minimum required elements.
     *
     * This constraint has meaning only if associated node is list or leaf-list.
     *
     * @param minElements
     *            number of minimum required elements.
     */
    void setMinElements(Integer minElements);

    /**
     *
     * Returns number of maximum required elements.
     *
     * This constraint has meaning only if associated node is list or leaf-list.
     *
     * @return number of maximum required elements.
     */
    Integer getMaxElements();

    /**
     *
     * Sets number of maximum required elements.
     *
     * This constraint has meaning only if associated node is list or leaf-list.
     *
     * @param maxElements
     *            number of maximum required elements.
     */
    void setMaxElements(Integer maxElements);

    /**
     * Returns <code>must</code> definition associated with this builder.
     *
     * @return <code>must</code> definition associated with this builder.
     */
    Set<MustDefinition> getMustDefinitions();

    /**
     * Adds must definition to product of this builder.
     *
     * @param must
     *            <code>must</code> definition which should be associated with
     *            parent node.
     */
    void addMustDefinition(MustDefinition must);

    /**
     * Returns when condition associated with this constraints.
     *
     * @return when condition associated with this constraints.
     */
    String getWhenCondition();

    /**
     * Sets when condition associated with this constraints.
     *
     * @param whenCondition
     *            when condition.
     */
    void addWhenCondition(String whenCondition);

    /**
     * Returns true if associated node is mandatory.
     *
     *
     * @return true if associated node is mandatory.
     */
    boolean isMandatory();

    /**
     * Sets mandatory status of parent node
     *
     * @param mandatory
     */
    void setMandatory(boolean mandatory);

    /**
     * Build constraint definition
     *
     * @return instance of ConstraintDefinition created from this builder
     */
    ConstraintDefinition toInstance();

}