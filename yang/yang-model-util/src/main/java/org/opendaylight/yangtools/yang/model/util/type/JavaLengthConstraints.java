/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.util.BaseConstraints;

final class JavaLengthConstraints {
    private JavaLengthConstraints() {
        throw new UnsupportedOperationException();
    }

    private static final List<LengthConstraint> INTEGER_SIZE_CONSTRAINTS = ImmutableList.of(
        BaseConstraints.newLengthConstraint(0, Integer.MAX_VALUE, Optional.<String>absent(), Optional.<String>absent()));

    /**
     * Length constraint imposed on YANG binary type by our implementation. byte[].length is an integer, capping our
     * ability to support arbitrary binary data.
     */
    static final List<LengthConstraint> BINARY_LENGTH_CONSTRAINTS = INTEGER_SIZE_CONSTRAINTS;

    /**
     * Length constraint imposed on YANG string type by our implementation. {@link String#length()} is an integer,
     * capping our ability to support strings up to 18446744073709551615 as defined in
     * http://tools.ietf.org/html/rfc6020#section-9.4.4.
     *
     * FIXME: We could bump this number up to allow such models, but that could lead to unexpected run-time errors.
     *        In order to do that, the parser would need another pass on the effective statements, which would cap
     *        the constraints to the run-time environment.
     */
    static final List<LengthConstraint> STRING_LENGTH_CONSTRAINTS = INTEGER_SIZE_CONSTRAINTS;
}
