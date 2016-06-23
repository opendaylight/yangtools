/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.model.api;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Set;

public interface DeviateDefinition {

    /**
     * Enum describing YANG deviation 'deviate' statement. It defines how the
     * device's implementation of the target node deviates from its original
     * definition.
     */
    enum Deviate {
        NOT_SUPPORTED("not-supported"), ADD("add"), REPLACE("replace"), DELETE("delete");

        private final String keyword;

        Deviate(final String keyword) {
            this.keyword = Preconditions.checkNotNull(keyword);
        }

        /**
         * @return String that corresponds to the yang keyword.
         */
        public String getKeyword() {
            return keyword;
        }
    }

    /**
     *
     * @return enum which describes the type of this deviate statement
     */
    Deviate getDeviateType();

    /**
     *
     * @return value of the deviated config statement
     */
    boolean getDeviatedConfig();

    /**
     *
     * @return value of the deviated default statement
     */
    String getDeviatedDefault();

    /**
     *
     * @return value of the deviated mandatory statement
     */
    boolean getDeviatedMandatory();

    /**
     *
     * @return value of the deviated max-elements statement
     */
    Integer getDeviatedMaxElements();

    /**
     *
     * @return value of the deviated min-elements statement
     */
    Integer getDeviatedMinElements();

    /**
     *
     * @return set of the deviated must statements
     */
    Set<MustDefinition> getDeviatedMusts();

    /**
     *
     * @return deviated type statement
     */
    TypeDefinition<?> getDeviatedType();

    /**
     *
     * @return collection of the deviated unique statements
     */
    Collection<UniqueConstraint> getDeviatedUniques();

    /**
     *
     * @return value of the deviated units statement
     */
    String getDeviatedUnits();
}
