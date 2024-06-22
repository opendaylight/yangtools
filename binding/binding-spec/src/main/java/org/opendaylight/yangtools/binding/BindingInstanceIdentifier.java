/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding;

import java.io.ObjectStreamException;
import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Binding representation of a {@code instance-identifier}.
 */
public abstract sealed class BindingInstanceIdentifier implements Serializable, Immutable
        // FIXME: PropertyReference as well
        permits DataObjectReference {
    @java.io.Serial
    private static final long serialVersionUID = 1L;

    @java.io.Serial
    abstract Object writeReplace() throws ObjectStreamException;
}
