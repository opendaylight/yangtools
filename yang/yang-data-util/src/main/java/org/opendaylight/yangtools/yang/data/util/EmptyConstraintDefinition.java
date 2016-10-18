/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

/**
 * Utility holder for constraint definitions which do not really constrain anything.
 */
public abstract class EmptyConstraintDefinition implements ConstraintDefinition {
    private static final EmptyConstraintDefinition MANDATORY = new EmptyConstraintDefinition() {
        @Override
        public boolean isMandatory() {
            return true;
        }
    };
    private static final EmptyConstraintDefinition OPTIONAL = new EmptyConstraintDefinition() {
        @Override
        public boolean isMandatory() {
            return false;
        }
    };

    private EmptyConstraintDefinition() {
        // Hidden on purpose
    }

    public static EmptyConstraintDefinition create(final boolean mandatory) {
        return mandatory ? MANDATORY : OPTIONAL;
    }

    @Override
    public final RevisionAwareXPath getWhenCondition() {
        return null;
    }

    @Override
    public final Set<MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public final Integer getMinElements() {
        return null;
    }

    @Override
    public final Integer getMaxElements() {
        return null;
    }

    @Override
    public final int hashCode() {
        return ConstraintDefinitions.hashCode(this);
    }

    @Override
    public final boolean equals(final Object obj) {
        return ConstraintDefinitions.equals(this, obj);
    }

    @Override
    public final String toString() {
        return ConstraintDefinitions.toString(this);
    }
}
