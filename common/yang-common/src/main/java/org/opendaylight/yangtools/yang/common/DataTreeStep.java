/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jdt.annotation.NonNull;

@Beta
public abstract class DataTreeStep<T extends AbstractQName> extends AbstractDataTreeStep<T> {
    private final @NonNull ImmutableMap<T, Object> keyValues;

    DataTreeStep(final T qname, final ImmutableMap<T, Object> keyValues) {
        super(qname);
        this.keyValues = requireNonNull(keyValues);
    }

    public final @NonNull ImmutableMap<T, Object> keyValues() {
        return keyValues;
    }
}
