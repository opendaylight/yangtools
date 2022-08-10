/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import java.io.Serial;
import java.io.Serializable;

/**
 * Serialization proxy for {@link SingletonSet}.
 */
final class SSv1 implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;

    private Object element;

    @SuppressWarnings("checkstyle:RedundantModifier")
    public SSv1() {
        // For Externalizable
    }

    SSv1(final Object element) {
        this.element = element;
    }

    @Serial
    private Object readResolve() {
        return SingletonSet.of(element);
    }
}
