/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.ConstraintDefinition;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;

/**
 * Utility holder for constraint definitions which do not really constrain anything.
 */
public final class EmptyConstraintDefinition implements ConstraintDefinition {
    private static final EmptyConstraintDefinition INSTANCE = new EmptyConstraintDefinition();

    private EmptyConstraintDefinition() {
        // Hidden on purpose
    }

    public static EmptyConstraintDefinition getInstance() {
        return INSTANCE;
    }

    @Override
    public Optional<RevisionAwareXPath> getWhenCondition() {
        return Optional.empty();
    }

    @Override
    public Set<MustDefinition> getMustConstraints() {
        return ImmutableSet.of();
    }

    @Override
    public Integer getMinElements() {
        return null;
    }

    @Override
    public Integer getMaxElements() {
        return null;
    }

    @Override
    public int hashCode() {
        return ConstraintDefinitions.hashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return ConstraintDefinitions.equals(this, obj);
    }

    @Override
    public String toString() {
        return ConstraintDefinitions.toString(this);
    }
}
