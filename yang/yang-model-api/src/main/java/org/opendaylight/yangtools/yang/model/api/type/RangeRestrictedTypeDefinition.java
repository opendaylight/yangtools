/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Marker interface for {@link TypeDefinition}s which can have their values restricted to a set of allowed values.
 *
 * @param <T> Concrete {@link TypeDefinition} subinterface
 */
public interface RangeRestrictedTypeDefinition<T extends TypeDefinition<T>> extends TypeDefinition<T> {
    /**
     * Returns range constraint of this type, if applicable. This is the effective constraint, e.g. it includes any
     * range constraints implied by base type hierarchy.
     *
     * @return range constraint which are specified in the <code>range</code> substatement of the <code>type</code>
     *         statement.
     */
    Optional<RangeConstraint<?>> getRangeConstraint();
}
