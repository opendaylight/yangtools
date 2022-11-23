/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;
import static net.bytebuddy.implementation.bytecode.member.MethodVariableAccess.loadThis;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.mdsal.binding.dom.codec.impl.ByteBuddyUtils.putField;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.ForLoadedType;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.impl.ClassGeneratorBridge.LocalNameProvider;
import org.opendaylight.mdsal.binding.dom.codec.impl.ClassGeneratorBridge.NodeContextSupplierProvider;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader.ClassGenerator;
import org.opendaylight.mdsal.binding.loader.BindingClassLoader.GeneratorResult;
import org.opendaylight.mdsal.binding.spec.naming.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Private support for generating {@link CodecDataObject} and {@link AugmentableCodecDataObject} specializations.
 *
 * <p>
 * Code generation here is probably more involved than usual mainly due to the fact we *really* want to express the
 * strong connection between a generated class to the extent possible. In most cases (grouping-generated types) this
 * involves one level of indirection, which is a safe approach. If we are dealing with a type generated outside of a
 * grouping statement, though, we are guaranteed instantiation-invariance and hence can hard-wire to a runtime-constant
 * {@link NodeContextSupplier} -- which  provides significant boost to JITs ability to optimize code -- especially with
 * inlining and constant propagation.
 *
 * <p>
 * The accessor mapping performance is critical due to users typically not taking care of storing the results acquired
 * by an invocation, assuming the accessors are backed by a normal field -- which of course is not true, as the results
 * are lazily computed.
 *
 * <p>
 * The design is such that for a particular structure like:
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
 * <p>
 * This strategy minimizes the bytecode footprint and follows the generally good idea of keeping common logic in a
 * single place in a maintainable form. The glue code is extremely light (~6 instructions), which is beneficial on both
 * sides of invocation:
 * <ul>
 *   <li>generated method can readily be inlined into the caller</li>
 *   <li>it forms a call site into which codeMember() can be inlined with VarHandle being constant</li>
 * </ul>
 *
 * <p>
 * The second point is important here, as it allows the invocation logic around VarHandle to completely disappear,
 * becoming synonymous with operations on a field. Even though the field itself is declared as volatile, it is only ever
 * accessed through helper method using VarHandles -- and those helpers are using relaxed field ordering
 * of {@code getAcquire()}/{@code setRelease()} memory semantics.
 *
 * <p>
 * Furthermore there are distinct {@code codecMember} methods, each of which supports a different invocation style:
 * <ul>
 *   <li>with {@code String}, which ends up looking up a {@link ValueNodeCodecContext}</li>
 *   <li>with {@code Class}, which ends up looking up a {@link DataContainerCodecContext}</li>
 *   <li>with {@code NodeContextSupplier}, which performs a direct load</li>
 * </ul>
 * The third mode of operation requires that the object being implemented is not defined in a {@code grouping}, because
 * it welds the object to a particular namespace -- hence it trades namespace mobility for access speed.
 *
 * <p>
 * The sticky point here is the NodeContextSupplier, as it is a heap object which cannot normally be looked up from the
 * static context in which the static class initializer operates -- so we need perform some sort of a trick here.
 * Even though ByteBuddy provides facilities for bridging references to type fields, those facilities operate on
 * volatile fields -- hence they do not quite work for us.
 *
 * <p>
 * Another alternative, which we used in Javassist-generated DataObjectSerializers, is to muck with the static field
 * using reflection -- which works, but requires redefinition of Field.modifiers, which is something Java 9+ complains
 * about quite noisily.
 *
 * <p>
 * We take a different approach here, which takes advantage of the fact we are in control of both code generation (here)
 * and class loading (in {@link BindingClassLoader}). The process is performed in four steps:
 * <ul>
 * <li>During code generation, the context fields are pointed towards
 *     {@link ClassGeneratorBridge#resolveNodeContextSupplier(String)} and
 *     {@link ClassGeneratorBridge#resolveKey(String)} methods, which are public and static, hence perfectly usable
 *     in the context of a class initializer.</li>
 * <li>During class loading of generated byte code, the original instance of the generator is called to wrap the actual
 *     class loading operation. At this point the generator installs itself as the current generator for this thread via
 *     {@link ClassGeneratorBridge#setup(CodecDataObjectGenerator)} and allows the class to be loaded.
 * <li>After the class has been loaded, but before the call returns, we will force the class to initialize, at which
 *     point the static invocations will be redirected to {@link #resolveNodeContextSupplier(String)} and
 *     {@link #resolveKey(String)} methods, thus initializing the fields to the intended constants.</li>
 * <li>Before returning from the class loading call, the generator will detach itself via
 *     {@link ClassGeneratorBridge#tearDown(CodecDataObjectGenerator)}.</li>
 * </ul>
 *
 * <p>
 * This strategy works due to close cooperation with the target ClassLoader, as the entire code generation and loading
 * block runs with the class loading lock for this FQCN and the reference is not leaked until the process completes.
 */
abstract class CodecDataObjectGenerator<T extends CodecDataObject<?>> implements ClassGenerator<T> {
    // Not reusable definition: we can inline NodeContextSuppliers without a problem
    // FIXME: MDSAL-443: wire this implementation, which requires that BindingRuntimeTypes provides information about
    //                   types being generated from within a grouping
    private static final class Fixed<T extends CodecDataObject<?>> extends CodecDataObjectGenerator<T>
            implements NodeContextSupplierProvider<T> {
        private final ImmutableMap<Method, NodeContextSupplier> properties;

        Fixed(final TypeDescription superClass, final ImmutableMap<Method, NodeContextSupplier> properties,
                final @Nullable Method keyMethod) {
            super(superClass, keyMethod);
            this.properties = requireNonNull(properties);
        }

        @Override
        Builder<T> generateGetters(final Builder<T> builder) {
            Builder<T> tmp = builder;
            for (Method method : properties.keySet()) {
                LOG.trace("Generating for fixed method {}", method);
                final String methodName = method.getName();
                final TypeDescription retType = TypeDescription.ForLoadedType.of(method.getReturnType());
                tmp = tmp.defineMethod(methodName, retType, PUB_FINAL).intercept(
                    new SupplierGetterMethodImplementation(methodName, retType));
            }
            return tmp;
        }

        @Override
        public NodeContextSupplier resolveNodeContextSupplier(final String methodName) {
            final Optional<Entry<Method, NodeContextSupplier>> found = properties.entrySet().stream()
                    .filter(entry -> methodName.equals(entry.getKey().getName())).findAny();
            verify(found.isPresent(), "Failed to find property for %s in %s", methodName, this);
            return verifyNotNull(found.orElseThrow().getValue());
        }
    }

    // Reusable definition: we have to rely on context lookups
    private static final class Reusable<T extends CodecDataObject<?>> extends CodecDataObjectGenerator<T>
            implements LocalNameProvider<T> {
        private final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties;
        private final Map<Class<?>, PropertyInfo> daoProperties;

        Reusable(final TypeDescription superClass, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
                final Map<Class<?>, PropertyInfo> daoProperties, final @Nullable Method keyMethod) {
            super(superClass, keyMethod);
            this.simpleProperties = requireNonNull(simpleProperties);
            this.daoProperties = requireNonNull(daoProperties);
        }

        @Override
        Builder<T> generateGetters(final Builder<T> builder) {
            Builder<T> tmp = builder;
            for (Method method : simpleProperties.keySet()) {
                LOG.trace("Generating for simple method {}", method);
                final String methodName = method.getName();
                final TypeDescription retType = TypeDescription.ForLoadedType.of(method.getReturnType());
                tmp = tmp.defineMethod(methodName, retType, PUB_FINAL).intercept(
                    new SimpleGetterMethodImplementation(methodName, retType));
            }
            for (Entry<Class<?>, PropertyInfo> entry : daoProperties.entrySet()) {
                final PropertyInfo info = entry.getValue();
                final Method method = info.getterMethod();
                LOG.trace("Generating for structured method {}", method);
                final String methodName = method.getName();
                final TypeDescription retType = TypeDescription.ForLoadedType.of(method.getReturnType());
                tmp = tmp.defineMethod(methodName, retType, PUB_FINAL).intercept(
                        new StructuredGetterMethodImplementation(methodName, retType, entry.getKey()));

                if (info instanceof PropertyInfo.GetterAndNonnull orEmpty) {
                    final String nonnullName = orEmpty.nonnullMethod().getName();
                    tmp = tmp.defineMethod(nonnullName, retType, PUB_FINAL).intercept(
                        new NonnullMethodImplementation(nonnullName, retType, entry.getKey(), method));
                }
            }

            return tmp;
        }

        @Override
        public String resolveLocalName(final String methodName) {
            final Optional<Entry<Method, ValueNodeCodecContext>> found = simpleProperties.entrySet().stream()
                    .filter(entry -> methodName.equals(entry.getKey().getName())).findAny();
            verify(found.isPresent(), "Failed to find property for %s in %s", methodName, this);
            return found.orElseThrow().getValue().getSchema().getQName().getLocalName();
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(CodecDataObjectGenerator.class);
    private static final Generic BB_BOOLEAN = TypeDefinition.Sort.describe(boolean.class);
    private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
    private static final Generic BB_INT = TypeDefinition.Sort.describe(int.class);
    private static final Generic BB_STRING = TypeDefinition.Sort.describe(String.class);
    private static final TypeDescription BB_CDO = ForLoadedType.of(CodecDataObject.class);
    private static final TypeDescription BB_ACDO = ForLoadedType.of(AugmentableCodecDataObject.class);

    private static final StackManipulation FIRST_ARG_REF = MethodVariableAccess.REFERENCE.loadFrom(1);

    private static final int PROT_FINAL = Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;
    private static final int PUB_FINAL = Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC;

    private static final ByteBuddy BB = new ByteBuddy();

    private final TypeDescription superClass;
    private final Method keyMethod;

    CodecDataObjectGenerator(final TypeDescription superClass, final @Nullable Method keyMethod) {
        this.superClass = requireNonNull(superClass);
        this.keyMethod = keyMethod;
    }

    static <D extends DataObject, T extends CodecDataObject<T>> Class<T> generate(final BindingClassLoader loader,
            final Class<D> bindingInterface, final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Method keyMethod) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new Reusable<>(BB_CDO, simpleProperties, daoProperties, keyMethod));
    }

    static <D extends DataObject, T extends CodecDataObject<T>> Class<T> generateAugmentable(
            final BindingClassLoader loader, final Class<D> bindingInterface,
            final ImmutableMap<Method, ValueNodeCodecContext> simpleProperties,
            final Map<Class<?>, PropertyInfo> daoProperties, final Method keyMethod) {
        return CodecPackage.CODEC.generateClass(loader, bindingInterface,
            new Reusable<>(BB_ACDO, simpleProperties, daoProperties, keyMethod));
    }

    @Override
    public final GeneratorResult<T> generateClass(final BindingClassLoader loader, final String fqcn,
            final Class<?> bindingInterface) {
        LOG.trace("Generating class {}", fqcn);

        final Generic bindingDef = TypeDefinition.Sort.describe(bindingInterface);
        @SuppressWarnings("unchecked")
        Builder<T> builder = (Builder<T>) BB.subclass(Generic.Builder.parameterizedType(superClass, bindingDef).build())
            .name(fqcn).implement(bindingDef);

        builder = generateGetters(builder);

        if (keyMethod != null) {
            LOG.trace("Generating for key {}", keyMethod);
            final String methodName = keyMethod.getName();
            final TypeDescription retType = TypeDescription.ForLoadedType.of(keyMethod.getReturnType());
            builder = builder.defineMethod(methodName, retType, PUB_FINAL).intercept(
                new KeyMethodImplementation(methodName, retType));
        }

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

    abstract Builder<T> generateGetters(Builder<T> builder);

    private static Implementation codecHashCode(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingHashCode(this);
            loadThis(),
            invokeMethod(bindingInterface, BindingMapping.BINDING_HASHCODE_NAME, bindingInterface),
            MethodReturn.INTEGER);
    }

    private static Implementation codecEquals(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingEquals(this, obj);
            loadThis(),
            FIRST_ARG_REF,
            invokeMethod(bindingInterface, BindingMapping.BINDING_EQUALS_NAME, bindingInterface, Object.class),
            MethodReturn.INTEGER);
    }

    private static Implementation toString(final Class<?> bindingInterface) {
        return new Implementation.Simple(
            // return Foo.bindingToString(this);
            loadThis(),
            invokeMethod(bindingInterface, BindingMapping.BINDING_TO_STRING_NAME, bindingInterface),
            MethodReturn.REFERENCE);
    }

    private abstract static class AbstractMethodImplementation implements Implementation {
        final TypeDescription retType;
        // getFoo, usually
        final String methodName;

        AbstractMethodImplementation(final String methodName, final TypeDescription retType) {
            this.methodName = requireNonNull(methodName);
            this.retType = requireNonNull(retType);
        }
    }

    private abstract static class AbstractCachedMethodImplementation extends AbstractMethodImplementation {
        private static final Generic BB_HANDLE = TypeDefinition.Sort.describe(VarHandle.class);
        private static final Generic BB_OBJECT = TypeDefinition.Sort.describe(Object.class);
        private static final StackManipulation OBJECT_CLASS = ClassConstant.of(TypeDescription.OBJECT);
        private static final StackManipulation LOOKUP = invokeMethod(MethodHandles.class, "lookup");
        private static final StackManipulation FIND_VAR_HANDLE = invokeMethod(Lookup.class,
            "findVarHandle", Class.class, String.class, Class.class);

        static final int PRIV_CONST = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL
                | Opcodes.ACC_SYNTHETIC;
        private static final int PRIV_VOLATILE = Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE | Opcodes.ACC_SYNTHETIC;

        // getFoo$$$V
        final String handleName;

        AbstractCachedMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
            handleName = methodName + "$$$V";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final InstrumentedType tmp = instrumentedType
                    // private static final VarHandle getFoo$$$V;
                    .withField(new FieldDescription.Token(handleName, PRIV_CONST, BB_HANDLE))
                    // private volatile Object getFoo;
                    .withField(new FieldDescription.Token(methodName, PRIV_VOLATILE, BB_OBJECT));

            return tmp.withInitializer(new ByteCodeAppender.Simple(
                // TODO: acquiring lookup is expensive, we should share it across all initialization
                // getFoo$$$V = MethodHandles.lookup().findVarHandle(This.class, "getFoo", Object.class);
                LOOKUP,
                ClassConstant.of(tmp),
                new TextConstant(methodName),
                OBJECT_CLASS,
                FIND_VAR_HANDLE,
                putField(tmp, handleName)));
        }
    }

    private static final class KeyMethodImplementation extends AbstractCachedMethodImplementation {
        private static final StackManipulation CODEC_KEY = invokeMethod(CodecDataObject.class,
            "codecKey", VarHandle.class);

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

    private static final class NonnullMethodImplementation extends AbstractMethodImplementation {
        private static final StackManipulation NONNULL_MEMBER = invokeMethod(CodecDataObject.class,
                "codecMemberOrEmpty", Object.class, Class.class);

        private final Class<?> bindingClass;
        private final Method getterMethod;

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

    /*
     * A simple leaf method, which looks up child by a String constant. This is slightly more complicated because we
     * want to make sure we are using the same String instance as the one stored in associated DataObjectCodecContext,
     * so that during lookup we perform an identity check instead of comparing content -- speeding things up as well
     * as minimizing footprint. Since that string is not guaranteed to be interned in the String Pool, we cannot rely
     * on the constant pool entry to resolve to the same object.
     */
    private static final class SimpleGetterMethodImplementation extends AbstractCachedMethodImplementation {
        private static final StackManipulation CODEC_MEMBER = invokeMethod(CodecDataObject.class,
            "codecMember", VarHandle.class, String.class);
        private static final StackManipulation BRIDGE_RESOLVE = invokeMethod(ClassGeneratorBridge.class,
            "resolveLocalName", String.class);
        private static final Generic BB_STRING = TypeDefinition.Sort.describe(String.class);

        // getFoo$$$S
        private final String stringName;

        SimpleGetterMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
            stringName = methodName + "$$$S";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final InstrumentedType tmp = super.prepare(instrumentedType)
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
            final TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
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
        private static final StackManipulation CODEC_MEMBER = invokeMethod(CodecDataObject.class,
            "codecMember", VarHandle.class, Class.class);

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

    private static final class SupplierGetterMethodImplementation extends AbstractCachedMethodImplementation {
        private static final StackManipulation CODEC_MEMBER = invokeMethod(CodecDataObject.class,
            "codecMember", VarHandle.class, NodeContextSupplier.class);
        private static final StackManipulation BRIDGE_RESOLVE = invokeMethod(ClassGeneratorBridge.class,
            "resolveNodeContextSupplier", String.class);
        private static final Generic BB_NCS = TypeDefinition.Sort.describe(NodeContextSupplier.class);

        // getFoo$$$C
        private final String contextName;

        SupplierGetterMethodImplementation(final String methodName, final TypeDescription retType) {
            super(methodName, retType);
            contextName = methodName + "$$$C";
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            final InstrumentedType tmp = super.prepare(instrumentedType)
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
            final TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
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
}
