/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.getField;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.invokeMethod;
import static org.opendaylight.yangtools.binding.data.codec.impl.ByteBuddyUtils.putField;

import com.google.common.base.VerifyException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDefinition.Sort;
import net.bytebuddy.description.type.TypeDescription.Generic;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.scaffold.InstrumentedType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.Implementation.Context;
import net.bytebuddy.implementation.bytecode.ByteCodeAppender;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.TypeCreation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.KeyAware;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.data.codec.api.BindingStreamEventWriter;
import org.opendaylight.yangtools.binding.data.codec.spi.BindingSchemaMapping;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.ClassGenerator;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.GeneratorResult;
import org.opendaylight.yangtools.binding.model.api.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.api.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.api.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.ItemObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.NotificationBodyArchetype;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveStatementEquivalent;
import org.opendaylight.yangtools.yang.model.api.meta.DataContainerCompat;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnydataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class DataContainerStreamerGenerator<T extends DataContainerStreamer<?>> implements ClassGenerator<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerStreamerGenerator.class);
    private static final Generic BB_VOID = TypeDefinition.Sort.describe(void.class);
    private static final Generic BB_DATA_CONTAINER = TypeDefinition.Sort.describe(DataContainer.class);
    private static final Generic BB_DOSR = TypeDefinition.Sort.describe(DataContainerSerializerRegistry.class);
    private static final Generic BB_BESV = TypeDefinition.Sort.describe(BindingStreamEventWriter.class);
    private static final Generic BB_IOX = TypeDefinition.Sort.describe(IOException.class);

    private static final Builder<?> TEMPLATE = new ByteBuddy().subclass(DataContainerStreamer.class)
            .modifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC);

    private static final StackManipulation REG = MethodVariableAccess.REFERENCE.loadFrom(1);
    private static final StackManipulation OBJ = MethodVariableAccess.REFERENCE.loadFrom(2);
    private static final StackManipulation STREAM = MethodVariableAccess.REFERENCE.loadFrom(3);
    private static final StackManipulation UNKNOWN_SIZE = IntegerConstant.forValue(
        BindingStreamEventWriter.UNKNOWN_SIZE);

    private static final @NonNull StackManipulation START_AUGMENTATION_NODE =
        invokeMethod(BindingStreamEventWriter.class, "startAugmentationNode", Class.class);
    private static final @NonNull StackManipulation START_CASE =
        invokeMethod(BindingStreamEventWriter.class, "startCase", Class.class, int.class);
    private static final @NonNull StackManipulation START_CONTAINER_NODE =
        invokeMethod(BindingStreamEventWriter.class, "startContainerNode", Class.class, int.class);
    private static final @NonNull StackManipulation END_NODE =
        invokeMethod(BindingStreamEventWriter.class, "endNode");

    // startMapEntryNode(obj.key(), UNKNOWN_SIZE)
    private static final @NonNull StackManipulation START_MAP_ENTRY_NODE = new StackManipulation.Compound(
        OBJ,
        invokeMethod(KeyAware.class, Naming.KEY_AWARE_KEY_NAME),
        UNKNOWN_SIZE,
        invokeMethod(BindingStreamEventWriter.class, "startMapEntryNode", Key.class, int.class));

    // startUnkeyedListItem(UNKNOWN_SIZE)
    private static final @NonNull StackManipulation START_UNKEYED_LIST_ITEM = new StackManipulation.Compound(
        UNKNOWN_SIZE,
        invokeMethod(BindingStreamEventWriter.class, "startUnkeyedListItem", int.class));

    private static final StackManipulation STREAM_ANYDATA = invokeMethod(DataContainerStreamer.class,
        "streamAnydata", BindingStreamEventWriter.class, String.class, Object.class);
    private static final StackManipulation STREAM_ANYXML = invokeMethod(DataContainerStreamer.class,
        "streamAnyxml", BindingStreamEventWriter.class, String.class, Object.class);
    private static final StackManipulation STREAM_CHOICE = invokeMethod(DataContainerStreamer.class,
        "streamChoice", Class.class, DataContainerSerializerRegistry.class, BindingStreamEventWriter.class,
        DataContainer.class);
    private static final StackManipulation STREAM_CONTAINER = invokeMethod(DataContainerStreamer.class,
        "streamContainer", DataContainerStreamer.class, DataContainerSerializerRegistry.class,
        BindingStreamEventWriter.class, DataObject.class);
    private static final StackManipulation STREAM_LEAF = invokeMethod(DataContainerStreamer.class,
        "streamLeaf", BindingStreamEventWriter.class, String.class, Object.class);
    private static final StackManipulation STREAM_LEAF_LIST = invokeMethod(DataContainerStreamer.class,
        "streamLeafList",
        BindingStreamEventWriter.class, String.class, Set.class);
    private static final StackManipulation STREAM_ORDERED_LEAF_LIST = invokeMethod(DataContainerStreamer.class,
        "streamOrderedLeafList", BindingStreamEventWriter.class, String.class, List.class);
    private static final StackManipulation STREAM_LIST = invokeMethod(DataContainerStreamer.class,
        "streamList", Class.class, DataContainerStreamer.class, DataContainerSerializerRegistry.class,
        BindingStreamEventWriter.class, List.class);
    private static final StackManipulation STREAM_MAP = invokeMethod(DataContainerStreamer.class,
        "streamMap", Class.class, DataContainerStreamer.class, DataContainerSerializerRegistry.class,
        BindingStreamEventWriter.class, Map.class);
    private static final StackManipulation STREAM_ORDERED_MAP = invokeMethod(DataContainerStreamer.class,
        "streamOrderedMap", Class.class, DataContainerStreamer.class, DataContainerSerializerRegistry.class,
        BindingStreamEventWriter.class, List.class);

    // streamAugmentations(reg, stream, obj)
    private static final StackManipulation STREAM_AUGMENTATIONS = new StackManipulation.Compound(
        REG,
        STREAM,
        OBJ,
        invokeMethod(DataContainerStreamer.class, "streamAugmentations", DataContainerSerializerRegistry.class,
            BindingStreamEventWriter.class, Augmentable.class));

    private static final String INSTANCE_FIELD = "INSTANCE";

    private final @NonNull DataContainerCompat<?, ?> statement;
    private final @NonNull DataContainerArchetype archetype;
    private final @NonNull CodecContextFactory registry;
    private final @NonNull StackManipulation startEvent;
    private final @NonNull Class<?> type;

    private DataContainerStreamerGenerator(final CodecContextFactory registry, final DataContainerArchetype archetype,
            final Class<?> type) {
        this.registry = requireNonNull(registry);
        this.archetype = requireNonNull(archetype);
        this.type = requireNonNull(type);
        startEvent = switch (archetype) {
            case AugmentationArchetype unused ->
                // startAugmentationNode(Foo.class)
                new StackManipulation.Compound(ClassConstant.of(Sort.describe(type).asErasure()),
                    START_AUGMENTATION_NODE);
            case CaseObjectArchetype unused -> classUnknownSizeMethod(START_CASE, type);
            case EntryObjectArchetype unused -> START_MAP_ENTRY_NODE;
            case ItemObjectArchetype unused -> START_UNKEYED_LIST_ITEM;
            case AugmentableArchetype.OfNotification unused -> classUnknownSizeMethod(START_CONTAINER_NODE, type);
            case ContainerObjectArchetype unused -> classUnknownSizeMethod(START_CONTAINER_NODE, type);
            case NotificationBodyArchetype unused -> classUnknownSizeMethod(START_CONTAINER_NODE, type);
            case RpcInputArchetype unused -> classUnknownSizeMethod(START_CONTAINER_NODE, type);
            case RpcOutputArchetype unused -> classUnknownSizeMethod(START_CONTAINER_NODE, type);
            default -> throw new UnsupportedOperationException("Unsupported type " + archetype);
        };
        final var stmt = archetype.statement();
        if (!(stmt instanceof DataContainerCompat<?, ?> compat)) {
            throw new UnsupportedOperationException("Unsupported statement " + stmt);
        }
        statement = compat;
    }

    @NonNullByDefault
    static DataContainerStreamer<?> generateStreamer(final BindingClassLoader loader,
            final CodecContextFactory registry, final Class<?> type) {
        final var archetype = registry.runtimeContext().getTypeWithSchema(type).javaType();
        if (!(archetype instanceof DataContainerArchetype dataContainer)) {
            throw new IllegalArgumentException(type + " is not a DataContainer");
        }

        final var clazz = CodecPackage.STREAMER.generateClass(loader, type,
            new DataContainerStreamerGenerator<>(registry, dataContainer, type));
        final Object instance;
        try {
            instance = clazz.getField(INSTANCE_FIELD).get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new VerifyException("Inaccessible streamer instance", e);
        }
        if (!(instance instanceof DataContainerStreamer<?> streamer)) {
            throw new VerifyException("Not a DataContainerSteamer: " + instance);
        }
        return streamer;
    }

    @Override
    public GeneratorResult<T> generateClass(final BindingClassLoader loader, final String fqcn,
            final Class<?> bindingInterface) {
        LOG.trace("Definining streamer {}", fqcn);

        @SuppressWarnings("unchecked")
        final var builder = (Builder<T>) TEMPLATE.name(fqcn);
        final var childStreams = new ArrayList<ChildStream>();

        // FIXME: we are using DataSchemaNode for three things:
        //        - the String in QName.getLocalName
        //        - expected method return type and value type (for lists)
        //        - whether or not to use ordered on unordered (for lists and leaf-lists)
        //        all of that should be available from DataContainerArchetype
        for (var schemaChild : statement.toDataNodeContainer().getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final var getterName = BindingSchemaMapping.getGetterMethodName(schemaChild);
                final Method getter;
                try {
                    getter = type.getMethod(getterName);
                } catch (NoSuchMethodException e) {
                    throw new IllegalStateException("Failed to find getter " + getterName, e);
                }

                if (schemaChild instanceof EffectiveStatementEquivalent<?> equiv) {
                    final var childStream = createStream(loader, getter, equiv.asEffectiveStatement());
                    if (childStream != null) {
                        childStreams.add(childStream);
                    }
                } else {
                    LOG.warn("Ignoring {} due to incompatible schema {}", getter, schemaChild);
                }
            }
        }

        final var dependencies = new ArrayList<Class<?>>();
        for (var childStream : childStreams) {
            final var dependency = childStream.getDependency();
            if (dependency != null) {
                dependencies.add(dependency);
            }
        }

        final var result = GeneratorResult.<T>of(
            new UnloadedLoadableClass<>(builder
                .defineMethod("serialize", BB_VOID, Opcodes.ACC_PROTECTED | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC)
                .withParameters(BB_DOSR, BB_DATA_CONTAINER, BB_BESV)
                .throwing(BB_IOX)
                .intercept(new SerializeImplementation(bindingInterface, startEvent, childStreams))
                .make()),
            dependencies);

        LOG.trace("Definition of {} done", fqcn);
        return result;
    }

    private ChildStream createStream(final BindingClassLoader loader, final Method getter,
            final EffectiveStatement<?, ?> schema) {
        return switch (schema) {
            case AnydataEffectiveStatement stmt -> qnameChildStream(STREAM_ANYDATA, getter, stmt.argument());
            case AnyxmlEffectiveStatement stmt -> qnameChildStream(STREAM_ANYXML, getter, stmt.argument());
            case ChoiceEffectiveStatement unused -> choiceChildStream(getter);
            case ContainerEffectiveStatement stmt -> containerChildStream(getter);
            case LeafEffectiveStatement stmt -> qnameChildStream(STREAM_LEAF, getter, stmt.argument());
            case LeafListEffectiveStatement stmt ->
                qnameChildStream(switch (stmt.effectiveOrdering()) {
                    case SYSTEM -> STREAM_LEAF_LIST;
                    case USER -> STREAM_ORDERED_LEAF_LIST;
                }, getter, stmt.argument());
            case ListEffectiveStatement stmt -> {
                // FIXME: Reflection over encoding of actual method return type. There are two possibilities here:
                //        - we have generated an EntryObject, in which case we see Map<FooKey, Foo>
                //        - we have an ItemObject, in which case we see List<Foo>
                final var signature =  getMethod(getter.getName());
                final var returnType = signature.returnType();
                if (!(returnType instanceof ParameterizedType paramType)) {
                    throw new VerifyException("Unexpected method " + signature);
                }

                final var params = paramType.getActualTypeArguments();
                final StackManipulation method;
                final Class<?> valueClass;
                switch (params.size()) {
                    case 1 -> {
                        valueClass = loadTypeClass(loader, params.getFirst());
                        method = stmt.keyStatement() == null ? STREAM_LIST : STREAM_ORDERED_MAP;
                    }
                    case 2 -> {
                        // make sure the key is loaded
                        loadTypeClass(loader, params.getFirst());
                        valueClass = loadTypeClass(loader, params.getLast());
                        method = STREAM_MAP;
                    }
                    default -> throw new VerifyException("Unexpected type " + paramType + " for " + stmt);
                }

                yield listChildStream(getter, valueClass.asSubclass(DataObject.class), method);
            }
            default -> {
                LOG.debug("Ignoring {} due to unhandled schema {}", getter, schema);
                yield null;
            }
        };
    }

    private @NonNull MethodSignature getMethod(final String methodName) {
        final var method = lookupMethod(archetype, methodName);
        if (method == null) {
            throw new VerifyException("No method for " + methodName + " in " + archetype);
        }
        return method;
    }

    private static @Nullable MethodSignature lookupMethod(final DataContainerArchetype archetype,
            final String methodName) {
        for (var method : archetype.getMethodDefinitions()) {
            if (methodName.equals(method.name())) {
                return method;
            }
        }
        for (var type : archetype.getImplements()) {
            // FIXME: narrow down?
            if (type instanceof DataContainerArchetype dataContainer) {
                final var found = lookupMethod(dataContainer, methodName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    // streamChoice(Foo.class, reg, stream, obj.getFoo())
    private static @NonNull ChildStream choiceChildStream(final Method getter) {
        return new ChildStream(ClassConstant.of(Sort.describe(getter.getReturnType()).asErasure()),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            STREAM_CHOICE);
    }

    // streamContainer(FooStreamer.INSTANCE, reg, stream, obj.getFoo())
    private @NonNull ChildStream containerChildStream(final Method getter) {
        final var streamer = registry.getDataContainerStreamer(getter.getReturnType().asSubclass(DataObject.class));
        return new ChildStream(streamer,
            streamerInstance(streamer),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            STREAM_CONTAINER);
    }

    // <METHOD>(Foo.class, FooStreamer.INSTACE, reg, stream, obj.getFoo())
    private @NonNull ChildStream listChildStream(final Method getter, final Class<? extends DataObject> itemClass,
            final StackManipulation method) {
        final var streamer = registry.getDataContainerStreamer(itemClass);
        return new ChildStream(streamer,
            ClassConstant.of(Sort.describe(itemClass).asErasure()),
            streamerInstance(streamer),
            REG,
            STREAM,
            OBJ,
            invokeMethod(getter),
            method);
    }

    // <METHOD>(stream, "foo", obj.getFoo())
    private static @NonNull ChildStream qnameChildStream(final StackManipulation method, final Method getter,
            final QName qname) {
        return new ChildStream(STREAM,
            new TextConstant(qname.getLocalName()),
            OBJ,
            invokeMethod(getter),
            method);
    }

    private static StackManipulation streamerInstance(final DataContainerStreamer<?> streamer) {
        try {
            return getField(streamer.getClass().getDeclaredField(INSTANCE_FIELD));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    // <METHOD>(Foo.class, UNKNOWN_SIZE)
    @NonNullByDefault
    private static StackManipulation classUnknownSizeMethod(final StackManipulation method, final Class<?> type) {
        return new StackManipulation.Compound(
            ClassConstant.of(Sort.describe(type).asErasure()),
            UNKNOWN_SIZE,
            method);
    }

    private static Class<?> loadTypeClass(final BindingClassLoader loader, final Type type) {
        try {
            return loader.loadClass(type.canonicalName());
        } catch (ClassNotFoundException e) {
            throw new LinkageError("Failed to load " + type, e);
        }
    }

    private record SerializeImplementation(
            Class<?> bindingInterface,
            StackManipulation startEvent,
            List<ChildStream> children) implements Implementation {
        SerializeImplementation {
            requireNonNull(bindingInterface);
            requireNonNull(startEvent);
            requireNonNull(children);
        }

        @Override
        public InstrumentedType prepare(final InstrumentedType instrumentedType) {
            return instrumentedType
                // private static final This INSTANCE = new This()
                .withField(new FieldDescription.Token(INSTANCE_FIELD,
                    Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC,
                    instrumentedType.asGenericType()))
                .withInitializer(InitializeInstanceField.INSTANCE);
        }

        @Override
        public ByteCodeAppender appender(final Target implementationTarget) {
            final var manipulations = new ArrayList<StackManipulation>(children.size() + 6);

            // stream.<START_EVENT>(...)
            manipulations.add(STREAM);
            manipulations.add(startEvent);

            // ... emit children ...
            manipulations.addAll(children);

            if (Augmentable.class.isAssignableFrom(bindingInterface)) {
                // streamAugmentations(reg, stream, obj)
                manipulations.add(STREAM_AUGMENTATIONS);
            }

            // stream.endNode()
            manipulations.add(STREAM);
            manipulations.add(END_NODE);
            // return
            manipulations.add(MethodReturn.VOID);

            return new ByteCodeAppender.Simple(manipulations);
        }
    }

    private static final class ChildStream extends StackManipulation.Compound {
        private final @Nullable Class<?> dependency;

        ChildStream(final StackManipulation... stackManipulation) {
            super(stackManipulation);
            dependency = null;
        }

        ChildStream(final DataContainerStreamer<?> streamer, final StackManipulation... stackManipulation) {
            super(stackManipulation);
            dependency = streamer.getClass();
        }

        @Nullable Class<?> getDependency() {
            return dependency;
        }
    }

    private enum InitializeInstanceField implements ByteCodeAppender {
        INSTANCE;

        // TODO: eliminate this constant when ElementMatchers.isDefaultConstructor() returns a singleton
        private static final ElementMatcher<MethodDescription> IS_DEFAULT_CONSTRUCTOR =
            ElementMatchers.isDefaultConstructor();

        @Override
        public Size apply(final MethodVisitor methodVisitor, final Context implementationContext,
                final MethodDescription instrumentedMethod) {
            final var instrumentedType = implementationContext.getInstrumentedType();
            return new Size(
                new StackManipulation.Compound(
                    TypeCreation.of(instrumentedType),
                    Duplication.SINGLE,
                    MethodInvocation.invoke(instrumentedType.getDeclaredMethods()
                        .filter(IS_DEFAULT_CONSTRUCTOR)
                        .getOnly()
                        .asDefined()),
                    putField(instrumentedType, INSTANCE_FIELD))
                    .apply(methodVisitor, implementationContext)
                    .getMaximalSize(),
                instrumentedMethod.getStackSize());
        }
    }
}
