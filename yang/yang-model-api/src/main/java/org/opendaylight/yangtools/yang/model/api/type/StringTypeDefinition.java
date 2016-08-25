/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.util.List;
import org.immutables.value.Value;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *
 * Contains method for getting data from the <code>string</code> YANG built-in
 * type.
 */
@Value.Immutable
public interface StringTypeDefinition extends TypeDefinition<StringTypeDefinition> {

    /**
     * Returns length constraint specified in the string. These are the effective constraints, e.g. they include
     * any length constraints implied by base types.
     *
     * @return list of length constraint which are specified in the
     *         <code>length</code> substatement of the <code>type</code>
     *         statement
     */
    List<LengthConstraint> getLengthConstraints();

    /**
     * Returns patterns specified in the string.
     *
     * @return list of pattern constraints which are specified in the
     *         <code>pattern</code> substatement of the <code>type</code>
     *         statement
     */
    List<PatternConstraint> getPatternConstraints();
}
