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
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.Map;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.data.codec.impl.ClassGeneratorBridge.LocalNameProvider;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecDataObjectGenerator.AbstractCachedMethodImplementation;
import org.opendaylight.yangtools.binding.data.codec.impl.CodecDataObjectGenerator.AbstractMethodImplementation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dynamic-resolution {@link GetterGenerator}. This is the safe default, dealing with the case when the target
 * construct is instantiated at multiple places. It deals with the resulting ambiguities by re-resolving the underlying
 * YANG local name against the local namespace.
 */
final class ReusableGetterGenerator extends GetterGenerator implements LocalNameProvider {
    private static final class NonnullMethodImplementation extends AbstractMethodImplementation {
        private static final StackManipulation NONNULL_MEMBER =
            invokeMethod(CodecDataObject.class, "codecMemberOrEmpty", Object.class, Class.class);

        private final @NonNull Class<?> bindingClass;
        private final @NonNull Method getterMethod;

        NonnullMethodImplementation(final String methodName, final TypeDescription retType,
                final Class<?> bindingClass, final Method getterMethod) {
            super(methodName, retType);
            this.bindingClass = requireNonNull(bindingClass);
            this.getterMethod = requireNonNull(getterMethod);
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            return new ByteCodeAppender.Simple(
                // return (FooType) codecMemberOrEmpty(getFoo(), FooType.class)
                loadThis(),
                loadThis(),
                invokeMethod(getterMethod),
                ClassConstant.of(TypeDefinition.Sort.describe(bindingClass).asErasure()),
                NONNULL_MEMBER,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            // No-op
            return instrumentedType;
        }
    }

    /**
     * A simple leaf method, which looks up child by a String constant. This is slightly more complicated because we
     * want to make sure we are using the same String instance as the one stored in associated DataObjectCodecContext,
     * so that during lookup we perform an identity check instead of comparing content -- speeding things up as well
     * as minimizing footprint. Since that string is not guaranteed to be interned in the String Pool, we cannot rely
     * on the constant pool entry to resolve to the same object.
     */
    private static final class SimpleGetterMethodImplementation extends AbstractCachedMethodImplementation {
        private static final StackManipulation CODEC_MEMBER =
            invokeMethod(CodecDataObject.class, "codecMember", VarHandle.class, String.class);
        private static final StackManipulation BRIDGE_RESOLVE =
            invokeMethod(ClassGeneratorBridge.class, "resolveLocalName", String.class);
        private static final Generic BB_STRING = TypeDefinition.Sort.describe(String.class);

        // getFoo$$$S
        private final String stringName;

        SimpleGetterMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
            stringName = methodName + "$$$S";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final var tmp = super.prepare(instrumentedType)
                // private static final String getFoo$$$S;
                .withField(new FieldDescription.Token(stringName, PRIV_CONST, BB_STRING));

            return tmp.withInitializer(new ByteCodeAppender.Simple(
                // getFoo$$$S = CodecDataObjectBridge.resolveString("getFoo");
                new TextConstant(methodName),
                BRIDGE_RESOLVE,
                putField(tmp, stringName)));
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final var instrumentedType = implementationTarget.getInstrumentedType();
            return new ByteCodeAppender.Simple(
                // return (FooType) codecMember(getFoo$$$V, getFoo$$$S);
                loadThis(),
                getField(instrumentedType, handleName),
                getField(instrumentedType, stringName),
                CODEC_MEMBER,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }
    }

    private static final class StructuredGetterMethodImplementation extends AbstractCachedMethodImplementation {
        private static final StackManipulation CODEC_MEMBER =
            invokeMethod(CodecDataObject.class, "codecMember", VarHandle.class, Class.class);

        private final Class<?> bindingClass;

        StructuredGetterMethodImplementation(final String methodName, final TypeDescription retType,
                final Class<?> bindingClass) {
            super(methodName, retType);
            this.bindingClass = requireNonNull(bindingClass);
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            return new ByteCodeAppender.Simple(
                // return (FooType) codecMember(getFoo$$$V, FooType.class);
                loadThis(),
                getField(implementationTarget.getInstrumentedType(), handleName),
                ClassConstant.of(TypeDefinition.Sort.describe(bindingClass).asErasure()),
                CODEC_MEMBER,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(ReusableGetterGenerator.class);

    private final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties;
    private final Map<Class<?>, PropertyInfo> daoProperties;

    ReusableGetterGenerator(final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties) {
        this.simpleProperties = requireNonNull(simpleProperties);
        this.daoProperties = requireNonNull(daoProperties);
    }

    @Override
    public String resolveLocalName(String methodName) {
        for (var entry : simpleProperties.entrySet()) {
            if (methodName.equals(entry.getKey().getName())) {
                return entry.getValue().getSchema().getQName().getLocalName();
            }
        }
        throw new VerifyException("Failed to find property for " + methodName + " in " + simpleProperties);
    }

    @Override
    <T> Builder<T> generateGetters(final Builder<T> builder) {
        var tmp = builder;
        for (var method : simpleProperties.keySet()) {
            LOG.trace("Generating for simple method {}", method);
            final var methodName = method.getName();
            final var retType = ForLoadedType.of(method.getReturnType());
            tmp = tmp.defineMethod(methodName, retType, CodecDataObjectGenerator.PUB_FINAL)
                .intercept(new SimpleGetterMethodImplementation(methodName, retType));
        }
        for (var entry : daoProperties.entrySet()) {
            final var info = entry.getValue();
            final var method = info.getterMethod();
            LOG.trace("Generating for structured method {}", method);
            final var methodName = method.getName();
            final var retType = ForLoadedType.of(method.getReturnType());
            tmp = tmp.defineMethod(methodName, retType, CodecDataObjectGenerator.PUB_FINAL)
                .intercept(new StructuredGetterMethodImplementation(methodName, retType, entry.getKey()));

            if (info instanceof PropertyInfo.GetterAndNonnull orEmpty) {
                final var nonnullName = orEmpty.nonnullMethod().getName();
                tmp = tmp.defineMethod(nonnullName, retType, CodecDataObjectGenerator.PUB_FINAL)
                    .intercept(new NonnullMethodImplementation(nonnullName, retType, entry.getKey(), method));
            }
        }

        return tmp;
    }
}
