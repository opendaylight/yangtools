/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingDataObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingStreamEventWriter;
import org.opendaylight.mdsal.binding.dom.codec.api.IncorrectNestingException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingClassInLoadingStrategyException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaException;
import org.opendaylight.mdsal.binding.dom.codec.api.MissingSchemaForClassException;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeTypeContainer;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DataContainerCodecContext<D extends DataObject, T extends RuntimeTypeContainer> extends NodeCodecContext
        implements BindingDataObjectCodecTreeNode<D>  {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerCodecContext.class);
    private static final VarHandle EVENT_STREAM_SERIALIZER;

    static {
        try {
            EVENT_STREAM_SERIALIZER = MethodHandles.lookup().findVarHandle(DataContainerCodecContext.class,
                "eventStreamSerializer", DataObjectSerializer.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull DataContainerCodecPrototype<T> prototype;

    // Accessed via a VarHandle
    @SuppressWarnings("unused")
    private volatile DataObjectSerializer eventStreamSerializer;

    DataContainerCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this.prototype = requireNonNull(prototype);
    }

    public final @NonNull T getType() {
        return prototype.getType();
    }

    @Override
    public final ChildAddressabilitySummary getChildAddressabilitySummary() {
        return prototype.getChildAddressabilitySummary();
    }

    protected final QNameModule namespace() {
        return prototype.getNamespace();
    }

    protected final CodecContextFactory factory() {
        return prototype.getFactory();
    }

    @Override
    protected YangInstanceIdentifier.PathArgument getDomPathArgument() {
        return prototype.getYangArg();
    }

    /**
     * Returns nested node context using supplied YANG Instance Identifier.
     *
     * @param arg Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @Override
    public abstract NodeCodecContext yangPathArgumentChild(YangInstanceIdentifier.PathArgument arg);

    /**
     * Returns nested node context using supplied Binding Instance Identifier
     * and adds YANG instance identifiers to supplied list.
     *
     * @param arg Binding Instance Identifier Argument
     * @return Context of child or null if supplied {@code arg} does not represent valid child.
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @Override
    public DataContainerCodecContext<?, ?> bindingPathArgumentChild(final PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        final DataContainerCodecContext<?,?> child = streamChild(arg.getType());
        if (builder != null) {
            child.addYangPathArgument(arg,builder);
        }
        return child;
    }

    /**
     * Returns deserialized Binding Path Argument from YANG instance identifier.
     */
    protected PathArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        return bindingArg();
    }

    protected final PathArgument bindingArg() {
        return prototype.getBindingArg();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Class<D> getBindingClass() {
        return Class.class.cast(prototype.getBindingClass());
    }

    @Override
    public abstract <C extends DataObject> DataContainerCodecContext<C, ?> streamChild(Class<C> childClass);

    /**
     * Returns child context as if it was walked by {@link BindingStreamEventWriter}. This means that to enter case, one
     * must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass child class
     * @return Context of child or Optional.empty is supplied class is not applicable in context.
     */
    @Override
    public abstract <C extends DataObject> Optional<DataContainerCodecContext<C,?>> possibleStreamChild(
            Class<C> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + prototype.getBindingClass() + "]";
    }

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        if (cacheSpecifier.isEmpty()) {
            return new NonCachingCodec<>(this);
        }
        return new CachingNormalizedNodeCodec<>(this, ImmutableSet.copyOf(cacheSpecifier));
    }

    @NonNull BindingStreamEventWriter createWriter(final NormalizedNodeStreamWriter domWriter) {
        return BindingToNormalizedStreamWriter.create(this, domWriter);
    }

    protected final <V> @NonNull V childNonNull(final @Nullable V nullable,
            final YangInstanceIdentifier.PathArgument child, final String message, final Object... args) {
        if (nullable == null) {
            throw childNullException(extractName(child), message, args);
        }
        return nullable;
    }

    protected final <V> @NonNull V childNonNull(final @Nullable V nullable, final QName child, final String message,
            final Object... args) {
        if (nullable == null) {
            throw childNullException(child, message, args);
        }
        return nullable;
    }

    protected final <V> @NonNull V childNonNull(final @Nullable V nullable, final Class<?> childClass,
            final String message, final Object... args) {
        if (nullable == null) {
            throw childNullException(childClass, message, args);
        }
        return nullable;
    }

    private IllegalArgumentException childNullException(final QName child, final String message, final Object... args) {
        final QNameModule module = child.getModule();
        if (!factory().getRuntimeContext().getEffectiveModelContext().findModule(module).isPresent()) {
            throw new MissingSchemaException("Module " + module + " is not present in current schema context.");
        }
        throw IncorrectNestingException.create(message, args);
    }

    private IllegalArgumentException childNullException(final Class<?> childClass, final String message,
            final Object... args) {
        final BindingRuntimeContext runtimeContext = factory().getRuntimeContext();
        final CompositeRuntimeType schema;
        if (Augmentation.class.isAssignableFrom(childClass)) {
            schema = runtimeContext.getAugmentationDefinition(childClass.asSubclass(Augmentation.class));
        } else {
            schema = runtimeContext.getSchemaDefinition(childClass);
        }
        if (schema == null) {
            throw new MissingSchemaForClassException(childClass);
        }

        try {
            runtimeContext.loadClass(Type.of(childClass));
        } catch (final ClassNotFoundException e) {
            throw new MissingClassInLoadingStrategyException(
                "User supplied class " + childClass.getName() + " is not available in " + runtimeContext, e);
        }

        throw IncorrectNestingException.create(message, args);
    }

    private static QName extractName(final YangInstanceIdentifier.PathArgument child) {
        if (child instanceof AugmentationIdentifier) {
            final Set<QName> children = ((AugmentationIdentifier) child).getPossibleChildNames();
            checkArgument(!children.isEmpty(), "Augmentation without childs must not be used in data");
            return children.iterator().next();
        }
        return child.getNodeType();
    }

    final DataObjectSerializer eventStreamSerializer() {
        final DataObjectSerializer existing = (DataObjectSerializer) EVENT_STREAM_SERIALIZER.getAcquire(this);
        return existing != null ? existing : loadEventStreamSerializer();
    }

    // Split out to aid inlining
    private DataObjectSerializer loadEventStreamSerializer() {
        final DataObjectSerializer loaded = factory().getEventStreamSerializer(getBindingClass());
        final Object witness = EVENT_STREAM_SERIALIZER.compareAndExchangeRelease(this, null, loaded);
        return witness == null ? loaded : (DataObjectSerializer) witness;
    }

    @Override
    public NormalizedNode serialize(final D data) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        // We create DOM stream writer which produces normalized nodes
        final NormalizedNodeStreamWriter domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        writeAsNormalizedNode(data, domWriter);
        return result.getResult();
    }

    @Override
    public void writeAsNormalizedNode(final D data, final NormalizedNodeStreamWriter writer) {
        try {
            eventStreamSerializer().serialize(data, createWriter(writer));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to serialize Binding DTO",e);
        }
    }

    static final <T extends NormalizedNode> @NonNull T checkDataArgument(final @NonNull Class<T> expectedType,
            final NormalizedNode data) {
        try {
            return expectedType.cast(requireNonNull(data));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected " + expectedType.getSimpleName(), e);
        }
    }

    // FIXME: MDSAL-780 replace this method with BindingRuntimeTypes-driven logic
    static final Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method,
            final String prefix) {
        final String methodName = method.getName();
        if ("getClass".equals(methodName) || !methodName.startsWith(prefix) || method.getParameterCount() > 0) {
            return Optional.empty();
        }

        final Class<?> returnType = method.getReturnType();
        if (DataContainer.class.isAssignableFrom(returnType)) {
            return optionalDataContainer(returnType);
        } else if (List.class.isAssignableFrom(returnType)) {
            return getYangModeledReturnType(method, 0);
        } else if (Map.class.isAssignableFrom(returnType)) {
            return getYangModeledReturnType(method, 1);
        }
        return Optional.empty();
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    private static Optional<Class<? extends DataContainer>> getYangModeledReturnType(final Method method,
            final int parameterOffset) {
        try {
            return ClassLoaderUtils.callWithClassLoader(method.getDeclaringClass().getClassLoader(),
                () -> genericParameter(method.getGenericReturnType(), parameterOffset)
                    .flatMap(result -> result instanceof Class ? optionalCast((Class<?>) result) : Optional.empty()));
        } catch (Exception e) {
            /*
             * It is safe to log this this exception on debug, since this
             * method should not fail. Only failures are possible if the
             * runtime / backing.
             */
            LOG.debug("Unable to find YANG modeled return type for {}", method, e);
        }
        return Optional.empty();
    }

    private static Optional<java.lang.reflect.Type> genericParameter(final java.lang.reflect.Type type,
            final int offset) {
        if (type instanceof ParameterizedType parameterized) {
            final var parameters = parameterized.getActualTypeArguments();
            if (parameters.length > offset) {
                return Optional.of(parameters[offset]);
            }
        }
        return Optional.empty();
    }

    private static Optional<Class<? extends DataContainer>> optionalCast(final Class<?> type) {
        return DataContainer.class.isAssignableFrom(type) ? optionalDataContainer(type) : Optional.empty();
    }


    // FIXME: MDSAL-780: remove this method
    static final Optional<Class<? extends DataContainer>> optionalDataContainer(final Class<?> type) {
        return Optional.of(type.asSubclass(DataContainer.class));
    }
}
