/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.binding.YangData;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;

/**
 * A base class for {@link YangData}s backed by {@link YangDataCodecContext}. While this class is public, it is not part
 * of API surface and is an implementation detail. The only reason for it being public is that it needs to be accessible
 * by code generated at runtime.
 *
 * @param <T> YangData type
 */
@Beta
public abstract non-sealed class CodecYangData<T extends YangData<T>> extends CodecDataContainer<NormalizedYangData>
        implements YangData<T> {
    CodecYangData(final NormalizedYangData data) {
        super(data);
    }
}
