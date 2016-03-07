/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import com.google.common.base.Preconditions;
import java.util.List;

/**
 * Interface describing YANG 'deviation' statement.
 * <p>
 * The 'deviation' statement defines a hierarchy of a module that the device
 * does not implement faithfully. Deviations define the way a device deviate
 * from a standard.
 * </p>
 */
public interface Deviation {

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
     * @return SchemaPath that identifies the node in the schema tree where a
     *         deviation from the module occurs.
     */
    SchemaPath getTargetPath();

    /**
     * @return deviate statement of this deviation
     */
    Deviate getDeviate();

    /**
     * @return textual cross-reference to an external document that provides
     *         additional information relevant to this node.
     */
    String getReference();

    /**
     * @return collection of all unknown nodes defined under this schema node.
     */
    List<UnknownSchemaNode> getUnknownSchemaNodes();

}
