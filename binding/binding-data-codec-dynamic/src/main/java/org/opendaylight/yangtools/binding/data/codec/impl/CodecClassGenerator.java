/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;
import static net.bytebuddy.implementation.bytecode.member.MethodVariableAccess.loadThis;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;

import java.util.function.Supplier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.ClassGenerator;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.GeneratorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link ClassGenerator}s for binding interfaces.
 */
abstract sealed class CodecClassGenerator<T extends CodecDataContainer<?, ?>> implements ClassGenerator<T>
        permits CodecDataObjectGenerator, CodecYangDataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(CodecClassGenerator.class);
    private static final Generic BB_BOOLEAN = TypeDefinition.Sort.describe(boolean.class);
    private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
    private static final Generic BB_INT = TypeDefinition.Sort.describe(int.class);
    private static final Generic BB_STRING = TypeDefinition.Sort.describe(String.class);
    private static final StackManipulation FIRST_ARG_REF = MethodVariableAccess.REFERENCE.loadFrom(1);
    private static final ByteBuddy BB = new ByteBuddy();

    private static final int PROT_FINAL = Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    private final @NonNull TypeDescription superClass;
    private final @NonNull GetterGenerator getterGenerator;

    @NonNullByDefault
    CodecClassGenerator(final TypeDescription superClass, GetterGenerator getterGenerator) {
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

        // Final bits:
        return GeneratorResult.of(builder
            // codecHashCode() ...
            .defineMethod("codecHashCode", BB_INT, PROT_FINAL)
            .intercept(codecHashCode(bindingInterface))
            // ... equals(Object) ...
            .defineMethod("codecEquals", BB_BOOLEAN, PROT_FINAL).withParameter(BB_OBJECT)
            .intercept(codecEquals(bindingInterface))
            // ... toString() ...
            .defineMethod("toString", BB_STRING, PUB_FINAL)
            .intercept(toString(bindingInterface))
            // ... and build it
            .make());
    }

    /**
     * Customize the layout of a {@link DynamicType.Builder}.
     *
     * @param builder the {@link DynamicType.Builder}
     * @return a possibly updated {@link DynamicType.Builder}
     */
    abstract DynamicType.Builder<T> customizeBuilder(DynamicType.Builder<T> builder);

    private static Implementation codecHashCode(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingHashCode(this);
            loadThis(),
            invokeMethod(bindingInterface, Naming.BINDING_HASHCODE_NAME, bindingInterface),
            MethodReturn.INTEGER);
    }

    private static Implementation codecEquals(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingEquals(this, obj);
            loadThis(),
            FIRST_ARG_REF,
            invokeMethod(bindingInterface, Naming.BINDING_EQUALS_NAME, bindingInterface, Object.class),
            MethodReturn.INTEGER);
    }

    private static Implementation toString(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingToString(this);
            loadThis(),
            invokeMethod(bindingInterface, Naming.BINDING_TO_STRING_NAME, bindingInterface),
            MethodReturn.REFERENCE);
    }
}
