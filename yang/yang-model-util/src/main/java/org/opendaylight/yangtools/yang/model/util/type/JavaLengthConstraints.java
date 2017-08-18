/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

final class JavaLengthConstraints {
    private JavaLengthConstraints() {
        throw new UnsupportedOperationException();
    }

    static final List<LengthConstraint> INTEGER_SIZE_CONSTRAINTS = ImmutableList.of(
        BaseConstraints.newLengthConstraint(0, Integer.MAX_VALUE, Optional.empty(), Optional.empty()));
}
