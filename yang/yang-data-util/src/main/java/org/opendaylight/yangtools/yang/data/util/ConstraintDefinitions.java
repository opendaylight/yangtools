/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;

/**
 * Utility methods for ConstraintDefinition implementations.
 */
public final class ConstraintDefinitions {
    private ConstraintDefinitions() {
        throw new UnsupportedOperationException();
    }

    public static int hashCode(final ConstraintDefinition def) {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(def.getWhenCondition());
        result = prime * result + Objects.hashCode(def.getMustConstraints());
        result = prime * result + Objects.hashCode(def.getMinElements());
        result = prime * result + Objects.hashCode(def.getMaxElements());
        result = prime * result + Objects.hashCode(def.isMandatory());
        return result;
    }

    public static boolean equals(final ConstraintDefinition def, final Object obj) {
        if (def == obj) {
            return true;
        }
        if (!(obj instanceof ConstraintDefinition)) {
            return false;
        }
        final ConstraintDefinition other = (ConstraintDefinition) obj;
        if (def.isMandatory() != other.isMandatory()) {
            return false;
        }
        if (!Objects.equals(def.getWhenCondition(), other.getWhenCondition())) {
            return false;
        }
        if (!Objects.equals(def.getMustConstraints(), other.getMustConstraints())) {
            return false;
        }
        if (!Objects.equals(def.getMinElements(), other.getMinElements())) {
            return false;
        }
        if (!Objects.equals(def.getMaxElements(), other.getMaxElements())) {
            return false;
        }
        return true;
    }

    public static String toString(final ConstraintDefinition def) {
        return MoreObjects.toStringHelper(def).omitNullValues()
                .add("whenCondition", def.getWhenCondition())
                .add("mustConstraints", def.getMustConstraints())
                .add("mandatory", def.isMandatory())
                .add("minElements", def.getMinElements())
                .add("maxElements", def.getMaxElements()).toString();
    }
}
