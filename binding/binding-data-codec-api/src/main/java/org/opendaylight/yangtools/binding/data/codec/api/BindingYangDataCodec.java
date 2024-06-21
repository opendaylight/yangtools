/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.api;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

/**
 * A codec capable of translating RFC8040 {@code yang-data} values between their {@link NormalizedYangData} and
 * {@link YangData} representation.
 */
public interface BindingYangDataCodec<T extends YangData<T>> extends Immutable {

    @NonNull T toBinding(@NonNull NormalizedYangData dom);

    @NonNull NormalizedYangData fromBinding(@NonNull T binding);
}
