/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.DynamicType.Builder;

/**
 * An {@link CodecClassGenerator} generating {@link CodecYangData} subclasses.
 */
final class CodecYangDataGenerator<T extends CodecYangData<T>> extends CodecClassGenerator<T> {
    private static final TypeDescription BB_CYD = ForLoadedType.of(CodecYangData.class);

    CodecYangDataGenerator(final ImmutableMap<Method, CodecContextSupplier> properties) {
        super(BB_CYD, new FixedGetterGenerator(properties));
    }

    @Override
    Builder<T> customizeBuilder(final Builder<T> builder) {
        return builder;
    }
}
