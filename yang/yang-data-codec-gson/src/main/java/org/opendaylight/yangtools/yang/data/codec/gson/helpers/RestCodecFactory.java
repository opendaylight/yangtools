/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson.helpers;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;

import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * This class is implementation-internal and subject to change. Please do not use it.
 */
@Beta
public final class RestCodecFactory {
    private final SchemaContextUtils utils;

    private RestCodecFactory(final SchemaContextUtils utils) {
        this.utils = Preconditions.checkNotNull(utils);
    }

    public static RestCodecFactory create(final SchemaContextUtils utils) {
        return new RestCodecFactory(utils);
    }

    public final Codec<Object, Object> codecFor(final TypeDefinition<?> typeDefinition) {
        // FIXME: implement loadingcache
        return new ObjectCodec(utils, typeDefinition);
    }
}
