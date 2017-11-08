/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nullable;

/**
 * Marker interface for statements which may contain a 'config' statement, as defined in RFC7950.
 */
public interface ConfigStatementContainer {
    /**
     * Return a {@link ConfigStatement} child, if present.
     *
     * @return A {@link ConfigStatement}, or null if none is present.
     */
    @Nullable ConfigStatement getConfig();
}
