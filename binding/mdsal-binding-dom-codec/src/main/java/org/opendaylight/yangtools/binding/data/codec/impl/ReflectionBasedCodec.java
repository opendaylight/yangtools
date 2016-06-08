/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Preconditions;

abstract class ReflectionBasedCodec extends ValueTypeCodec {

    private final Class<?> typeClass;

    ReflectionBasedCodec(final Class<?> typeClass) {
        this.typeClass = Preconditions.checkNotNull(typeClass);
    }

    protected final Class<?> getTypeClass() {
        return typeClass;
    }
}