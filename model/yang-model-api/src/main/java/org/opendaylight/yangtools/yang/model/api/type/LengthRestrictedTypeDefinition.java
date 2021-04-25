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
 * Interface for {@link TypeDefinition}s which can have their values restricted to a set of allowed lengths.
 *
 * @param <T> Concrete {@link TypeDefinition} subinterface
 */
public interface LengthRestrictedTypeDefinition<T extends TypeDefinition<T>> extends TypeDefinition<T> {
    /**
     * Returns length constraint of this type, if applicable. This is the effective constraint, e.g. it includes any
     * length constraints implied by base type hierarchy.
     *
     * @return length constraint which are specified in the <code>length</code> substatement of the <code>type</code>
     *         statement.
     */
    Optional<LengthConstraint> getLengthConstraint();
}
