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
 * Marker interface for statements which may contain a 'default' statement, as defined in RFC7950.
 *
 * @deprecated Use {@link DefaultStatementAwareDeclaredStatement} instead.
 */
@Deprecated
public interface DefaultStatementContainer {
    /**
     * Return a {@link DefaultStatement} child, if present.
     *
     * @return A {@link DefaultStatement}, or null if none is present.
     */
    @Nullable DefaultStatement getDefault();
}
