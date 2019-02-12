/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Preconditions;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.common.QName;

final class IdentityCodec implements Codec<QName, Class<?>> {
    private final BindingRuntimeContext context;

    IdentityCodec(final BindingRuntimeContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public Class<?> deserialize(final QName input) {
        Preconditions.checkArgument(input != null, "Input must not be null.");
        return context.getIdentityClass(input);
    }

    @Override
    public QName serialize(final Class<?> input) {
        Preconditions.checkArgument(BaseIdentity.class.isAssignableFrom(input));
        return BindingReflections.findQName(input);
    }
}
