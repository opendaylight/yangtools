/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import com.google.common.annotations.Beta;

@Beta
public class DuplicateChildNodeRejectedException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public DuplicateChildNodeRejectedException(final String message) {
        super(message);
    }
}
