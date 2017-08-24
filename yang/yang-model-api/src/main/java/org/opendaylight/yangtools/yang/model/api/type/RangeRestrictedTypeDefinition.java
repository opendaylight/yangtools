/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Interface for {@link TypeDefinition}s which can have their values restricted to a set of allowed values.
 *
 * @param <T> Concrete {@link TypeDefinition} subinterface
 */
public interface RangeRestrictedTypeDefinition<T extends TypeDefinition<T>> extends TypeDefinition<T> {
    /**
     * Returns range constraints for instance of this type. These are the effective constraints, e.g. they include
     * any range constraints imposed by base types.
     *
     * @return list of range constraints which are specified as the argument of the <code>range</code> which is
     *         a substatement of the <code>type</code> statement
     */
    @Nonnull List<RangeConstraint> getRangeConstraints();
}
