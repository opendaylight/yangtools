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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Serialization proxy for {@link ErrorMessage}.
 *
 */
@NonNullByDefault
record EMv1(String elementBody, @Nullable String xmlLang) implements Serializable {
    EMv1 {
        requireNonNull(elementBody);
    }

    @java.io.Serial
    private Object readResolve() {
        return new ErrorMessage(elementBody, xmlLang);
    }
}
