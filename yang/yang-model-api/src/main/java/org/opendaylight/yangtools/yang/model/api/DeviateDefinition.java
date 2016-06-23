/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.annotations.Beta;
import java.util.Collection;
import java.util.Set;

/**
 * Interface describing YANG 'deviate' statement
 *
 * The 'deviate' statement defines how the device's implementation of
 * the target node deviates from its original definition.
 * The argument is one of the strings "not-supported", "add", "replace", or "delete".
 */
@Beta
public interface DeviateDefinition {

    /**
     *
     * @return enum which describes the type of this deviate statement
     */
    DeviateKind getDeviateType();

    /**
     *
     * @return value of the deviated config statement or null if it is not deviated
     */
    Boolean getDeviatedConfig();

    /**
     *
     * @return value of the deviated default statement or null if it is not deviated
     */
    String getDeviatedDefault();

    /**
     *
     * @return value of the deviated mandatory statement or null if it is not deviated
     */
    Boolean getDeviatedMandatory();

    /**
     *
     * @return value of the deviated max-elements statement or null if it is not deviated
     */
    Integer getDeviatedMaxElements();

    /**
     *
     * @return value of the deviated min-elements statement or null if it is not deviated
     */
    Integer getDeviatedMinElements();

    /**
     *
     * @return set of the deviated must statements
     */
    Set<MustDefinition> getDeviatedMusts();

    /**
     *
     * @return deviated type statement or null if it is not deviated
     */
    TypeDefinition<?> getDeviatedType();

    /**
     *
     * @return collection of the deviated unique statements
     */
    Collection<UniqueConstraint> getDeviatedUniques();

    /**
     *
     * @return value of the deviated units statement or null if it is not deviated
     */
    String getDeviatedUnits();
}
