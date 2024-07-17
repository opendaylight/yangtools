/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.util.function.Supplier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.ClassGenerator;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.GeneratorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link ClassGenerator}s for binding interfaces.
 */
abstract sealed class CodecClassGenerator<T extends CodecDataObject<?>> implements ClassGenerator<T>
        permits CodecDataObjectGenerator, CodecEntryObjectGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CodecClassGenerator.class);

    private static final ByteBuddy BB = new ByteBuddy();

    static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    private final @NonNull TypeDescription superClass;
    private final @NonNull GetterGenerator getterGenerator;

    @NonNullByDefault
    CodecClassGenerator(final TypeDescription superClass, final GetterGenerator getterGenerator) {
        this.superClass = requireNonNull(superClass);
        this.getterGenerator = requireNonNull(getterGenerator);
    }

    @Override
    public final Class<T> customizeLoading(final Supplier<Class<T>> loader) {
        return ClassGeneratorBridge.loadWithProvider(getterGenerator, loader);
    }

    @Override
    public final GeneratorResult<T> generateClass(final BindingClassLoader loader, final String fqcn,
            final Class<?> bindingInterface) {
        LOG.trace("Generating class {}", fqcn);

        final var bindingDef = TypeDefinition.Sort.describe(bindingInterface);
        @SuppressWarnings("unchecked")
        var builder = (Builder<T>) BB.subclass(Generic.Builder.parameterizedType(superClass, bindingDef).build())
            .name(fqcn).implement(bindingDef);

        builder = customizeBuilder(getterGenerator.generateGetters(builder));

        return GeneratorResult.of(new UnloadedLoadableClass<>(builder.make()));
    }

    /**
     * Customize the layout of a {@link DynamicType.Builder}.
     *
     * @param builder the {@link DynamicType.Builder}
     * @return a possibly updated {@link DynamicType.Builder}
     */
    abstract DynamicType.Builder<T> customizeBuilder(DynamicType.Builder<T> builder);
}
