/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

abstract class ReflectionBasedCodec extends ValueTypeCodec {

    private final Class<?> typeClass;

    ReflectionBasedCodec(final Class<?> typeClass) {
        this.typeClass = requireNonNull(typeClass);
    }

    protected final Class<?> getTypeClass() {
        return typeClass;
    }
}