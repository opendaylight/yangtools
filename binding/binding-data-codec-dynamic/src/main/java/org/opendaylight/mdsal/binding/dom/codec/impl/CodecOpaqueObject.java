/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.AbstractOpaqueObject;
import org.opendaylight.yangtools.binding.OpaqueData;
import org.opendaylight.yangtools.binding.OpaqueObject;

/**
 * A base class for {@link OpaqueObject}s backed by {@link ForeignOpaqueData}. While this class is public, it not part
 * of API surface and is an implementation detail. The only reason for it being public is that it needs to be accessible
 * by code generated at runtime.
 *
 * @param <T> OpaqueObject type
 */
@Beta
public abstract class CodecOpaqueObject<T extends OpaqueObject<T>> extends AbstractOpaqueObject<T> {
    private final @NonNull OpaqueData<?> value;

    protected CodecOpaqueObject(final OpaqueData<?> value) {
        this.value = requireNonNull(value);
    }

    @Override
    public final OpaqueData<?> getValue() {
        return value;
    }
}
