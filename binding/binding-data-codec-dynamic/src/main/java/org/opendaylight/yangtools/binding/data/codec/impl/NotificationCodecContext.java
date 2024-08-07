/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static net.bytebuddy.implementation.bytecode.member.MethodVariableAccess.loadThis;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Instant;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InGenericShape;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.BaseNotification;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.EventInstantAware;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.GeneratorResult;
import org.opendaylight.yangtools.binding.runtime.api.NotificationRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

final class NotificationCodecContext<D extends DataObject & BaseNotification>
        extends DataObjectCodecContext<D, NotificationRuntimeType> {
    private static final Generic EVENT_INSTANT_AWARE = TypeDefinition.Sort.describe(EventInstantAware.class);

    private static final String EVENT_INSTANT_NAME;
    private static final Generic EVENT_INSTANT_RETTYPE;

    static {
        final MethodDescription eventInstance = EVENT_INSTANT_AWARE.getDeclaredMethods().getOnly();
        EVENT_INSTANT_NAME = eventInstance.getName();
        EVENT_INSTANT_RETTYPE = eventInstance.getReturnType();
    }

    private static final Generic BB_DOCC = TypeDefinition.Sort.describe(DataObjectCodecContext.class);
    private static final Generic BB_DCN = TypeDefinition.Sort.describe(DataContainerNode.class);
    private static final Generic BB_I = TypeDefinition.Sort.describe(Instant.class);

    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class, DataObjectCodecContext.class,
        DataContainerNode.class, Instant.class);
    private static final MethodType NOTIFICATION_TYPE = MethodType.methodType(BaseNotification.class,
        NotificationCodecContext.class, ContainerNode.class, Instant.class);
    private static final String INSTANT_FIELD = "instant";

    private final MethodHandle eventProxy;

    NotificationCodecContext(final Class<?> notificationClass, final NotificationRuntimeType type,
            final CodecContextFactory factory) {
        super(new Prototype<>(notificationClass, type, factory));
        final var bindingClass = getBindingClass();

        final var eventAwareClass = CodecPackage.EVENT_AWARE.generateClass(prototype().contextFactory().getLoader(),
            bindingClass, (loader, fqcn, bindingInterface) -> {
                final var codecImpl = CodecPackage.CODEC.getGeneratedClass(loader, bindingClass);

                return GeneratorResult.of(new ByteBuddy()
                    .subclass(codecImpl, ConstructorStrategy.Default.NO_CONSTRUCTORS)
                    .implement(EVENT_INSTANT_AWARE)
                    .name(fqcn)
                    .defineField(INSTANT_FIELD, BB_I, Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC)
                    .defineConstructor(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC)
                        .withParameters(BB_DOCC, BB_DCN, BB_I)
                        .intercept(ConstructorImplementation.INSTANCE)
                    .defineMethod(EVENT_INSTANT_NAME, EVENT_INSTANT_RETTYPE, Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC)
                        .intercept(EventInstantImplementation.INSTANCE)
                    .make());
            });

        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(eventAwareClass, CONSTRUCTOR_TYPE);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new LinkageError("Failed to acquire constructor", e);
        }
        eventProxy = ctor.asType(NOTIFICATION_TYPE);
    }

    @Override
    public D deserialize(final NormalizedNode data) {
        return createBindingProxy(checkDataArgument(ContainerNode.class, data));
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    @NonNull BaseNotification deserialize(final @NonNull ContainerNode data, final @NonNull Instant eventInstant) {
        final BaseNotification ret;
        try {
            ret = (BaseNotification) eventProxy.invokeExact(this, data, eventInstant);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new LinkageError("Failed to instantiate notification", e);
        }
        return verifyNotNull(ret);
    }

    /**
     * Prototype for a {@code notification}. This class only exists because DataContainerCodecContext requires a
     * prototype.
     */
    static final class Prototype<D extends DataObject & BaseNotification>
            extends DataObjectCodecPrototype<NotificationRuntimeType> {
        private Prototype(final Class<?> cls, final NotificationRuntimeType type, final CodecContextFactory factory) {
            super(cls, NodeIdentifier.create(type.statement().argument()), type, factory);
        }

        @Override
        NotificationCodecContext<?> createInstance() {
            throw new UnsupportedOperationException("Should never be invoked");
        }
    }

    private enum ConstructorImplementation implements Implementation {
        INSTANCE;

        private static final StackManipulation LOAD_INSTANT_ARG = MethodVariableAccess.REFERENCE.loadFrom(3);
        private static final StackManipulation LOAD_CTOR_ARGS;

        static {
            try {
                LOAD_CTOR_ARGS = MethodVariableAccess.allArgumentsOf(new MethodDescription.ForLoadedConstructor(
                    AugmentableCodecDataObject.class.getDeclaredConstructor(CommonDataObjectCodecContext.class,
                        DataContainerNode.class)));
            } catch (NoSuchMethodException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final TypeDescription instrumentedType = implementationTarget.getInstrumentedType();
            final InGenericShape superCtor = verifyNotNull(instrumentedType.getSuperClass()).getDeclaredMethods()
                    .filter(ElementMatchers.isConstructor()).getOnly();

            return new ByteCodeAppender.Simple(
                loadThis(),
                LOAD_CTOR_ARGS,
                MethodInvocation.invoke(superCtor),
                loadThis(),
                LOAD_INSTANT_ARG,
                putField(instrumentedType, INSTANT_FIELD),
                MethodReturn.VOID);
        }
    }

    private enum EventInstantImplementation implements Implementation {
        INSTANCE;

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType;
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            return new ByteCodeAppender.Simple(
              loadThis(),
              getField(implementationTarget.getInstrumentedType(), INSTANT_FIELD),
              MethodReturn.REFERENCE);
        }
    }
}
