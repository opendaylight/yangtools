/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Marker interface for statements which may contain a 'mandatory' statement, as defined in RFC7950.
 *
 * @deprecated Use {@link MandatoryStatementAwareDeclaredStatement} instead.
 */
@Deprecated
public interface MandatoryStatementContainer {
    /**
     * Return a {@link MandatoryStatement} child, if present.
     *
     * @return A {@link MandatoryStatement}, or null if none is present.
     */
    @Nullable MandatoryStatement getMandatory();
}
