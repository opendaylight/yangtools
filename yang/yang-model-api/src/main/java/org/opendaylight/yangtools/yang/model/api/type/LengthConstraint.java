/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import com.google.common.collect.RangeSet;
import org.opendaylight.yangtools.yang.model.api.ConstraintMetaDefinition;

/**
 * The Length Constraint value consists of an explicit value, or a lower bound
 * returned by {@link #getMin()} and an upper bound returned by
 * {@link #getMax()}. <br>
 * <br>
 * Length-restricting values MUST NOT be negative. A length value is a
 * non-negative integer, or one of the special values <code>min</code> or
 * <code>max</code>. The defined <code>min</code> and <code>max</code> mean the
 * minimum and maximum length accepted for the type being restricted,
 * respectively. <br>
 * An implementation is not required to support a length value larger than
 * {@link Long#MAX_VALUE} <br>
 * <br>
 * The interface extends definitions from {@link ConstraintMetaDefinition} <br>
 * <br>
 * This interface was modeled according to definition in <a
 * href="https://tools.ietf.org/html/rfc6020#section-9.4.4">[RFC-6020] The
 * length Statement</a>.
 *
 * @see ConstraintMetaDefinition
 */
public interface LengthConstraint extends ConstraintMetaDefinition {
    /**
     * Return allowed length ranges. Returned RangeSet must not be empty.
     *
     * @return Set of allowed lengths.
     */
    RangeSet<Integer> getAllowedRanges();
}
