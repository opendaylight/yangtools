/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Serialization proxy for {@link DefaultYangNamespaceContext}.
 */
@NonNullByDefault
record YNSv1(Map<String, QNameModule> prefixToModule) implements Serializable {
    YNSv1 {
        requireNonNull(prefixToModule);
    }

    @java.io.Serial
    private Object readResolve() {
        return YangNamespaceContext.of(prefixToModule);
    }
}
