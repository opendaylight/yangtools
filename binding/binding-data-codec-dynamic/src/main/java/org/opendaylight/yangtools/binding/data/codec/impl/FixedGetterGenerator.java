/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static net.bytebuddy.implementation.bytecode.member.MethodVariableAccess.loadThis;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableMap;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
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
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.data.codec.impl.ClassGeneratorBridge.CodecContextSupplierProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A fixed-resolution {@link GetterGenerator}. This is useful for when the target construct is known to have exactly one
 * instantiation. We can inline NodeContextSuppliers without an issue.
 */
// FIXME: MDSAL-443: wire this implementation, which requires that BindingRuntimeTypes provides information about types
//                   being generated from within a grouping
final class FixedGetterGenerator extends GetterGenerator implements CodecContextSupplierProvider {
    private static final class SupplierGetterMethodImplementation extends CachedMethodImplementation {
        private static final StackManipulation CODEC_MEMBER =
            invokeMethod(CodecDataObject.class, "codecMember", VarHandle.class, CodecContextSupplier.class);
        private static final StackManipulation BRIDGE_RESOLVE =
            invokeMethod(ClassGeneratorBridge.class, "resolveCodecContextSupplier", String.class);
        private static final Generic BB_NCS = TypeDefinition.Sort.describe(CodecContextSupplier.class);

        // getFoo$$$C
        private final @NonNull String contextName;

        @NonNullByDefault
        SupplierGetterMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
            contextName = methodName + "$$$C";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final var tmp = super.prepare(instrumentedType)
                // private static final NodeContextSupplier getFoo$$$C;
                .withField(new FieldDescription.Token(contextName, PRIV_CONST, BB_NCS));

            return tmp.withInitializer(new ByteCodeAppender.Simple(
                // getFoo$$$C = CodecDataObjectBridge.resolve("getFoo");
                new TextConstant(methodName),
                BRIDGE_RESOLVE,
                putField(tmp, contextName)));
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final var instrumentedType = implementationTarget.getInstrumentedType();
            return new ByteCodeAppender.Simple(
                // return (FooType) codecMember(getFoo$$$V, getFoo$$$C);
                loadThis(),
                getField(instrumentedType, handleName),
                getField(instrumentedType, contextName),
                CODEC_MEMBER,
                TypeCasting.to(retType),
                MethodReturn.REFERENCE);
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(FixedGetterGenerator.class);

    private final @NonNull ImmutableMap<Method, CodecContextSupplier> properties;

    FixedGetterGenerator(final ImmutableMap<Method, CodecContextSupplier> properties) {
        this.properties = requireNonNull(properties);
    }

    @Override
    public CodecContextSupplier resolveCodecContextSupplier(final String methodName) {
        for (var entry : properties.entrySet()) {
            if (methodName.equals(entry.getKey().getName())) {
                return verifyNotNull(entry.getValue());
            }
        }
        throw new VerifyException("Failed to find property for " + methodName + " in " + properties);
    }

    @Override
    <T> Builder<T> generateGetters(final Builder<T> builder) {
        var tmp = builder;
        for (var method : properties.keySet()) {
            LOG.trace("Generating for fixed method {}", method);
            final var methodName = method.getName();
            final var retType = ForLoadedType.of(method.getReturnType());
            tmp = tmp.defineMethod(methodName, retType, CodecDataObjectGenerator.PUB_FINAL)
                .intercept(new SupplierGetterMethodImplementation(methodName, retType));
        }
        return tmp;
    }
}
