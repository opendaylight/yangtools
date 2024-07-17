/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;
import static net.bytebuddy.implementation.bytecode.member.MethodVariableAccess.loadThis;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.Map;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class CodecEntryObjectGenerator<T extends CodecDataObject<?>> extends CodecClassGenerator<T> {
    private static final class KeyMethodImplementation extends CachedMethodImplementation {
        private static final StackManipulation CODEC_KEY =
            invokeMethod(CodecDataObject.class, "codecKey", VarHandle.class);

        @NonNullByDefault
        KeyMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            return new ByteCodeAppender.Simple(
                // return (FooType) codecKey(getFoo$$$V);
                loadThis(),
                getField(implementationTarget.getInstrumentedType(), handleName),
                CODEC_KEY,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectGenerator.class);
    private static final TypeDescription BB_CEO = ForLoadedType.of(CodecEntryObject.class);

    private final @NonNull Method keyMethod;

    private CodecEntryObjectGenerator(final @NonNull TypeDescription superClass, final Method keyMethod,
            final @NonNull GetterGenerator getterGenerator) {
        super(superClass, getterGenerator);
        this.keyMethod = requireNonNull(keyMethod);
    }

    static <T extends CodecDataObject<T>> @NonNull Class<T> generate(final BindingClassLoader loader,
            final Class<?> bindingInterface, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Method keyMethod) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new CodecEntryObjectGenerator<>(BB_CEO, keyMethod,
                new ReusableGetterGenerator(simpleProperties, daoProperties)));
    }

    @Override
    DynamicType.Builder<T> customizeBuilder(final DynamicType.Builder<T> builder) {
        LOG.trace("Generating for key {}", keyMethod);
        final var methodName = keyMethod.getName();
        final var retType = ForLoadedType.of(keyMethod.getReturnType());
        return builder.defineMethod(methodName, retType, PUB_FINAL)
            .intercept(new KeyMethodImplementation(methodName, retType));
    }
}
