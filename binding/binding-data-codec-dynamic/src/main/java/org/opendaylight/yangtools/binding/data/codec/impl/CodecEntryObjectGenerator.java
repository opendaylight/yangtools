/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.Method;
import java.util.Map;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CodecEntryObjectGenerator<T extends CodecDataObject<?>> extends CodecClassGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(CodecEntryObjectGenerator.class);
    private static final TypeDescription BB_CEO = ForLoadedType.of(CodecEntryObject.class);


    private final @NonNull Class<? extends DataContainer> parentClass;
    private final @NonNull Class<? extends Key<?>> keyClass;

    private CodecEntryObjectGenerator(final Class<? extends DataContainer> parentClass,
            final Class<? extends Key<?>> keyClass, final @NonNull GetterGenerator getterGenerator) {
        super(getterGenerator);
        this.keyClass = requireNonNull(keyClass);
        this.parentClass = requireNonNull(parentClass);
    }

    static <T extends CodecDataObject<?>> @NonNull Class<T> generate(final BindingClassLoader loader,
            final Class<?> bindingInterface, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Class<? extends DataContainer> parentClass,
            final Class<? extends Key<?>> key) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new CodecEntryObjectGenerator<>(parentClass, key,
                new ReusableGetterGenerator(simpleProperties, daoProperties)));
    }

    @Override
    DynamicType.Builder<?> newBuilder(final TypeDescription.Generic bindingDef) {
        LOG.trace("Generating for parent {} key {}", parentClass, keyClass);
        return BB.subclass(Generic.Builder.parameterizedType(BB_CEO, ForLoadedType.of(parentClass), bindingDef,
            ForLoadedType.of(keyClass)).build());
    }
}
