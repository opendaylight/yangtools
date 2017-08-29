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
 * A single value length restriction, as expressed by a length statement, as specified by
 * <a href="https://tools.ietf.org/html/rfc6020#section-9.4.4">[RFC-6020] The length Statement</a>.
 */
public interface LengthConstraint extends ConstraintMetaDefinition {
    /**
     * Return allowed length ranges. Returned RangeSet must not be empty.
     *
     * @return Set of allowed lengths.
     */
    RangeSet<Integer> getAllowedRanges();
}
