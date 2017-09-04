/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import org.opendaylight.yangtools.binding.data.codec.gen.impl.DataObjectSerializerGenerator;

/**
 * @deprecated Use {@link org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry} instead.
 */
@Deprecated
public class BindingNormalizedNodeCodecRegistry
        extends org.opendaylight.mdsal.binding.dom.codec.impl.BindingNormalizedNodeCodecRegistry {

    public BindingNormalizedNodeCodecRegistry(final DataObjectSerializerGenerator generator) {
        super(generator);
    }
}
