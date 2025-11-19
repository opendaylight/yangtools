/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

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
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating {@link CodecDataObject} and {@link AugmentableCodecDataObject} specializations.
 *
 * <p>Code generation here is probably more involved than usual mainly due to the fact we *really* want to express the
 * strong connection between a generated class to the extent possible. In most cases (grouping-generated types) this
 * involves one level of indirection, which is a safe approach. If we are dealing with a type generated outside of a
 * grouping statement, though, we are guaranteed instantiation-invariance and hence can hard-wire to a runtime-constant
 * {@link CodecContextSupplier} -- which  provides significant boost to JITs ability to optimize code -- especially with
 * inlining and constant propagation.
 *
 * <p>The accessor mapping performance is critical due to users typically not taking care of storing the results
 * acquired by an invocation, assuming the accessors are backed by a normal field -- which of course is not true, as
 * the results are lazily computed.
 *
 * <p>The design is such that for a particular structure like:
 * <pre>
 *     container foo {
 *         leaf bar {
 *             type string;
 *         }
 *     }
 * </pre>
 * we end up generating a class with the following layout:
 * <pre>
 *     public final class Foo$$$codecImpl extends CodecDataObject implements Foo {
 *         private static final VarHandle getBar$$$V;
 *         private volatile Object getBar;
 *
 *         public Foo$$$codecImpl(DistinctNodeContainer data) {
 *             super(data);
 *         }
 *
 *         public Bar getBar() {
 *             return (Bar) codecMember(getBar$$$V, "bar");
 *         }
 *     }
 * </pre>
 *
 * <p>This strategy minimizes the bytecode footprint and follows the generally good idea of keeping common logic in a
 * single place in a maintainable form. The glue code is extremely light (~6 instructions), which is beneficial on both
 * sides of invocation:
 * <ul>
 *   <li>generated method can readily be inlined into the caller</li>
 *   <li>it forms a call site into which codeMember() can be inlined with VarHandle being constant</li>
 * </ul>
 *
 * <p>The second point is important here, as it allows the invocation logic around VarHandle to completely disappear,
 * becoming synonymous with operations on a field. Even though the field itself is declared as volatile, it is only ever
 * accessed through helper method using VarHandles -- and those helpers are using relaxed field ordering
 * of {@code getAcquire()}/{@code setRelease()} memory semantics.
 *
 * <p>Furthermore there are distinct {@code codecMember} methods, each of which supports a different invocation style:
 * <ul>
 *   <li>with {@code String}, which ends up looking up a {@link ValueNodeCodecContext}</li>
 *   <li>with {@code Class}, which ends up looking up a {@link DataContainerCodecContext}</li>
 *   <li>with {@code NodeContextSupplier}, which performs a direct load</li>
 * </ul>
 * The third mode of operation requires that the object being implemented is not defined in a {@code grouping}, because
 * it welds the object to a particular namespace -- hence it trades namespace mobility for access speed.
 *
 * <p>The sticky point here is the NodeContextSupplier, as it is a heap object which cannot normally be looked up from
 * the static context in which the static class initializer operates -- so we need perform some sort of a trick here.
 * Even though ByteBuddy provides facilities for bridging references to type fields, those facilities operate on
 * volatile fields -- hence they do not quite work for us.
 *
 * <p>Another alternative, which we used in Javassist-generated DataObjectSerializers, is to muck with the static field
 * using reflection -- which works, but requires redefinition of Field.modifiers, which is something Java 9+ complains
 * about quite noisily.
 *
 * <p>We take a different approach here, which takes advantage of the fact we are in control of both code generation
 * (here) and class loading (in {@link BindingClassLoader}). The process is performed in four steps:
 * <ul>
 * <li>During code generation, the context fields are pointed towards
 *     {@link ClassGeneratorBridge#resolveCodecContextSupplier(String)} and
 *     {@link ClassGeneratorBridge#resolveKey(String)} methods, which are public and static, hence perfectly usable
 *     in the context of a class initializer.</li>
 * <li>During class loading of generated byte code, the original instance of the generator is called to wrap the actual
 *     class loading operation. At this point the generator installs itself as the current generator for this thread via
 *     {@link ClassGeneratorBridge#setup(CodecDataObjectGenerator)} and allows the class to be loaded.
 * <li>After the class has been loaded, but before the call returns, we will force the class to initialize, at which
 *     point the static invocations will be redirected to {@link #resolveCodecContextSupplier(String)} and
 *     {@link #resolveKey(String)} methods, thus initializing the fields to the intended constants.</li>
 * <li>Before returning from the class loading call, the generator will detach itself via
 *     {@link ClassGeneratorBridge#tearDown(CodecDataObjectGenerator)}.</li>
 * </ul>
 *
 * <p>This strategy works due to close cooperation with the target ClassLoader, as the entire code generation and
 * loading block runs with the class loading lock for this FQCN and the reference is not leaked until the process
 * completes.
 */
final class CodecDataObjectGenerator<T extends CodecDataObject<?>> extends CodecClassGenerator<T> {
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
    private static final TypeDescription BB_CDO = ForLoadedType.of(CodecDataObject.class);
    private static final TypeDescription BB_ACDO = ForLoadedType.of(AugmentableCodecDataObject.class);

    private final Method keyMethod;

    private CodecDataObjectGenerator(final @NonNull TypeDescription superClass,
            final @NonNull GetterGenerator getterGenerator, final @Nullable Method keyMethod) {
        super(superClass, getterGenerator);
        this.keyMethod = keyMethod;
    }

    static <T extends CodecDataObject<T>> @NonNull Class<T> generate(final BindingClassLoader loader,
            final Class<?> bindingInterface, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Method keyMethod) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new CodecDataObjectGenerator<>(BB_CDO, new ReusableGetterGenerator(simpleProperties, daoProperties),
                keyMethod));
    }

    static <T extends CodecDataObject<T>> @NonNull Class<T> generateAugmentable(final BindingClassLoader loader,
            final Class<?> bindingInterface, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Method keyMethod) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new CodecDataObjectGenerator<>(BB_ACDO, new ReusableGetterGenerator(simpleProperties, daoProperties),
                keyMethod));
    }

    @Override
    DynamicType.Builder<T> customizeBuilder(final DynamicType.Builder<T> builder) {
        if (keyMethod == null) {
            return builder;
        }

        LOG.trace("Generating for key {}", keyMethod);
        final var methodName = keyMethod.getName();
        final var retType = ForLoadedType.of(keyMethod.getReturnType());
        return builder.defineMethod(methodName, retType, PUB_FINAL)
            .intercept(new KeyMethodImplementation(methodName, retType));
    }
}
