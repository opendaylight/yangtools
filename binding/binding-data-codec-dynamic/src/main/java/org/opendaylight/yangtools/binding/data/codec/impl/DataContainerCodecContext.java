/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.Augmentable;
import org.opendaylight.yangtools.binding.Augmentation;
import org.opendaylight.yangtools.binding.BindingObject;
import org.opendaylight.yangtools.binding.DataContainer;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.DataObjectStep;
import org.opendaylight.yangtools.binding.data.codec.api.BindingDataContainerCodecTreeNode;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCodec;
import org.opendaylight.yangtools.binding.data.codec.api.IncorrectNestingException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingClassInLoadingStrategyException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingSchemaException;
import org.opendaylight.yangtools.binding.data.codec.api.MissingSchemaForClassException;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.yangtools.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizationResultHolder;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract sealed class DataContainerCodecContext<D extends DataContainer, R extends CompositeRuntimeType,
        P extends DataContainerPrototype<?, R>>
        extends CodecContext implements BindingDataContainerCodecTreeNode<D>
        permits ChoiceCodecContext, CommonDataObjectCodecContext, YangDataCodecContext {
    private static final Logger LOG = LoggerFactory.getLogger(DataContainerCodecContext.class);
    private static final VarHandle EVENT_STREAM_SERIALIZER;

    static {
        try {
            EVENT_STREAM_SERIALIZER = MethodHandles.lookup().findVarHandle(DataContainerCodecContext.class,
                "eventStreamSerializer", DataContainerSerializer.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final @NonNull P prototype;
    private final @NonNull ChildAddressabilitySummary childAddressabilitySummary;

    // Accessed via a VarHandle
    @SuppressWarnings("unused")
    @SuppressFBWarnings(value = "UUF_UNUSED_FIELD", justification = "https://github.com/spotbugs/spotbugs/issues/2749")
    private volatile DataContainerSerializer eventStreamSerializer;

    DataContainerCodecContext(final P prototype) {
        this.prototype = requireNonNull(prototype);
        childAddressabilitySummary = computeChildAddressabilitySummary(prototype.runtimeType().statement());
    }

    final @NonNull P prototype() {
        return prototype;
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Class<D> getBindingClass() {
        return (Class<D>) prototype().javaClass();
    }

    // overridden in AugmentationCodecContext
    @Override
    NodeIdentifier getDomPathArgument() {
        return prototype.yangArg();
    }

    @Override
    public final ChildAddressabilitySummary getChildAddressabilitySummary() {
        return childAddressabilitySummary;
    }

    // Non-final for ChoiceCodecContext
    @Override
    public CodecContext yangPathArgumentChild(final PathArgument arg) {
        CodecContextSupplier supplier;
        if (arg instanceof NodeIdentifier nodeId) {
            supplier = yangChildSupplier(nodeId);
        } else if (arg instanceof NodeIdentifierWithPredicates nip) {
            supplier = yangChildSupplier(new NodeIdentifier(nip.getNodeType()));
        } else {
            supplier = null;
        }
        return childNonNull(supplier, arg, "Argument %s is not valid child of %s", arg, getSchema()).getCodecContext();
    }

    abstract @Nullable CodecContextSupplier yangChildSupplier(@NonNull NodeIdentifier arg);

    @Override
    public abstract CommonDataObjectCodecContext<?, ?> bindingPathArgumentChild(DataObjectStep<?> step,
        List<PathArgument> builder);

    /**
     * Serializes supplied Binding Path Argument and adds all necessary YANG instance identifiers to supplied list.
     *
     * @param step Binding Path Argument
     * @param builder DOM Path argument.
     */
    final void addYangPathArgument(final DataObjectStep<?> step, final List<PathArgument> builder) {
        if (builder != null) {
            addYangPathArgument(builder, step);
        }
    }

    void addYangPathArgument(final @NonNull List<PathArgument> builder, final DataObjectStep<?> step) {
        final var yangArg = getDomPathArgument();
        if (yangArg != null) {
            builder.add(yangArg);
        }
    }

    @Override
    public final <C extends DataObject> DataContainerCodecContext<C, ?, ?> getStreamChild(final Class<C> childClass) {
        return childNonNull(streamChild(childClass), childClass,
            "Child %s is not valid child of %s", getBindingClass(), childClass);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <C extends DataObject> DataContainerCodecContext<C, ?, ?> streamChild(final Class<C> childClass) {
        final var childProto = streamChildPrototype(requireNonNull(childClass));
        return childProto == null ? null : (DataContainerCodecContext<C, ?, ?>) childProto.getCodecContext();
    }

    abstract @Nullable DataContainerPrototype<?, ?> streamChildPrototype(@NonNull Class<?> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + getBindingClass() + "]";
    }

    static final <T extends DataObject, C extends DataContainerCodecContext<T, ?, ?> & BindingNormalizedNodeCodec<T>>
            @NonNull BindingNormalizedNodeCachingCodec<T> createCachingCodec(final C context,
                final ImmutableCollection<Class<? extends BindingObject>> cacheSpecifier) {
        return cacheSpecifier.isEmpty() ? new NonCachingCodec<>(context)
            : new CachingNormalizedNodeCodec<>(context, ImmutableSet.copyOf(cacheSpecifier));
    }

    protected final <V> @NonNull V childNonNull(final @Nullable V nullable, final PathArgument child,
            final String message, final Object... args) {
        if (nullable == null) {
            throw childNullException(child.getNodeType(), message, args);
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

    @CheckReturnValue
    private IllegalArgumentException childNullException(final QName child, final String message, final Object... args) {
        final var module = child.getModule();
        if (!prototype().contextFactory().runtimeContext().modelContext().findModule(module).isPresent()) {
            return new MissingSchemaException("Module " + module + " is not present in current schema context.");
        }
        return new IncorrectNestingException(message, args);
    }

    @CheckReturnValue
    private @NonNull IllegalArgumentException childNullException(final Class<?> childClass, final String message,
            final Object... args) {
        return childNullException(prototype().contextFactory().runtimeContext(), childClass, message, args);
    }

    @CheckReturnValue
    static @NonNull IllegalArgumentException childNullException(final BindingRuntimeContext runtimeContext,
            final Class<?> childClass, final String message, final Object... args) {
        final CompositeRuntimeType schema;
        if (Augmentation.class.isAssignableFrom(childClass)) {
            schema = runtimeContext.getAugmentationDefinition(childClass.asSubclass(Augmentation.class));
        } else {
            schema = runtimeContext.getSchemaDefinition(childClass);
        }
        if (schema == null) {
            return new MissingSchemaForClassException(childClass);
        }

        try {
            runtimeContext.loadClass(Type.of(childClass));
        } catch (final ClassNotFoundException e) {
            return new MissingClassInLoadingStrategyException(
                "User supplied class " + childClass.getName() + " is not available in " + runtimeContext, e);
        }

        return new IncorrectNestingException(message, args);
    }

    final DataContainerSerializer eventStreamSerializer() {
        final DataContainerSerializer existing = (DataContainerSerializer) EVENT_STREAM_SERIALIZER.getAcquire(this);
        return existing != null ? existing : loadEventStreamSerializer();
    }

    // Split out to aid inlining
    private DataContainerSerializer loadEventStreamSerializer() {
        final DataContainerSerializer loaded = prototype().contextFactory().getEventStreamSerializer(getBindingClass());
        final Object witness = EVENT_STREAM_SERIALIZER.compareAndExchangeRelease(this, null, loaded);
        return witness == null ? loaded : (DataContainerSerializer) witness;
    }

    final @NonNull NormalizedNode serializeImpl(final @NonNull D data) {
        final var result = new NormalizationResultHolder();
        // We create DOM stream writer which produces normalized nodes
        final var domWriter = ImmutableNormalizedNodeStreamWriter.from(result);
        try {
            eventStreamSerializer().serialize(data, new BindingToNormalizedStreamWriter(this, domWriter));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to serialize Binding DTO",e);
        }
        return result.getResult().data();
    }

    static final <T extends NormalizedNode> @NonNull T checkDataArgument(final @NonNull Class<T> expectedType,
            final NormalizedNode data) {
        try {
            return expectedType.cast(requireNonNull(data));
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Expected " + expectedType.getSimpleName(), e);
        }
    }

    /**
     * Determines if two augmentation classes or case classes represents same data.
     *
     * <p>Two augmentations or cases could be substituted only if and if:
     * <ul>
     *   <li>Both implements same interfaces</li>
     *   <li>Both have same children</li>
     *   <li>If augmentations: Both have same augmentation target class. Target class was generated for data node in a
     *       grouping.</li>
     *   <li>If cases: Both are from same choice. Choice class was generated for data node in grouping.</li>
     * </ul>
     *
     * <p><b>Explanation:</b>
     * Binding Specification reuses classes generated for groupings as part of normal data tree, this classes from
     * grouping could be used at various locations and user may not be aware of it and may use incorrect case or
     * augmentation in particular subtree (via copy constructors, etc).
     *
     * @param potential Class which is potential substitution
     * @param target Class which should be used at particular subtree
     * @return true if and only if classes represents same data.
     * @throws NullPointerException if any argument is {@code null}
     */
    // FIXME: MDSAL-785: this really should live in BindingRuntimeTypes and should not be based on reflection. The only
    //                   user is binding-dom-codec and the logic could easily be performed on GeneratedType instead. For
    //                   a particular world this boils down to a matrix, which can be calculated either on-demand or
    //                   when we create BindingRuntimeTypes. Achieving that will bring us one step closer to being able
    //                   to have a pre-compiled Binding Runtime.
    @SuppressWarnings({ "rawtypes", "unchecked" })
    static final boolean isSubstitutionFor(final Class potential, final Class target) {
        Set<Class> subImplemented = new HashSet<>(Arrays.asList(potential.getInterfaces()));
        Set<Class> targetImplemented = new HashSet<>(Arrays.asList(target.getInterfaces()));
        if (!subImplemented.equals(targetImplemented)) {
            return false;
        }
        if (Augmentation.class.isAssignableFrom(potential)
            && !findAugmentationTarget(potential).equals(findAugmentationTarget(target))) {
            return false;
        }
        for (Method potentialMethod : potential.getMethods()) {
            if (Modifier.isStatic(potentialMethod.getModifiers())) {
                // Skip any static methods, as we are not interested in those
                continue;
            }

            try {
                Method targetMethod = target.getMethod(potentialMethod.getName(), potentialMethod.getParameterTypes());
                if (!potentialMethod.getReturnType().equals(targetMethod.getReturnType())) {
                    return false;
                }
            } catch (NoSuchMethodException e) {
                // Counterpart method is missing, so classes could not be substituted.
                return false;
            } catch (SecurityException e) {
                throw new IllegalStateException("Could not compare methods", e);
            }
        }
        return true;
    }

    /**
     * Find augmentation target class from concrete Augmentation class. This method uses first generic argument of
     * implemented {@link Augmentation} interface.
     *
     * @param augmentation {@link Augmentation} subclass for which we want to determine augmentation target.
     * @return Augmentation target - class which augmentation provides additional extensions.
     */
    static final Class<? extends Augmentable<?>> findAugmentationTarget(
            final Class<? extends Augmentation<?>> augmentation) {
        final Optional<Class<Augmentable<?>>> opt = ClassLoaderUtils.findFirstGenericArgument(augmentation,
            Augmentation.class);
        return opt.orElse(null);
    }

    private static @NonNull ChildAddressabilitySummary computeChildAddressabilitySummary(final Object nodeSchema) {
        // FIXME: rework this to work on EffectiveStatements
        if (nodeSchema instanceof DataNodeContainer contaner) {
            boolean haveAddressable = false;
            boolean haveUnaddressable = false;
            for (DataSchemaNode child : contaner.getChildNodes()) {
                if (child instanceof ContainerSchemaNode || child instanceof AugmentationSchemaNode) {
                    haveAddressable = true;
                } else if (child instanceof ListSchemaNode list) {
                    if (list.getKeyDefinition().isEmpty()) {
                        haveUnaddressable = true;
                    } else {
                        haveAddressable = true;
                    }
                } else if (child instanceof AnydataSchemaNode || child instanceof AnyxmlSchemaNode
                        || child instanceof TypedDataSchemaNode) {
                    haveUnaddressable = true;
                } else if (child instanceof ChoiceSchemaNode choice) {
                    switch (computeChildAddressabilitySummary(choice)) {
                        case ADDRESSABLE -> haveAddressable = true;
                        case UNADDRESSABLE -> haveUnaddressable = true;
                        case MIXED -> {
                            haveAddressable = true;
                            haveUnaddressable = true;
                        }
                        default -> throw new IllegalStateException("Unhandled accessibility summary for " + child);
                    }
                } else {
                    LOG.warn("Unhandled child node {}", child);
                }
            }

            if (!haveAddressable) {
                // Empty or all are unaddressable
                return ChildAddressabilitySummary.UNADDRESSABLE;
            }

            return haveUnaddressable ? ChildAddressabilitySummary.MIXED : ChildAddressabilitySummary.ADDRESSABLE;
        } else if (nodeSchema instanceof ChoiceSchemaNode choice) {
            return computeChildAddressabilitySummary(choice);
        }

        // No child nodes possible: return unaddressable
        return ChildAddressabilitySummary.UNADDRESSABLE;
    }

    private static @NonNull ChildAddressabilitySummary computeChildAddressabilitySummary(
            final ChoiceSchemaNode choice) {
        boolean haveAddressable = false;
        boolean haveUnaddressable = false;
        for (CaseSchemaNode child : choice.getCases()) {
            switch (computeChildAddressabilitySummary(child)) {
                case ADDRESSABLE:
                    haveAddressable = true;
                    break;
                case UNADDRESSABLE:
                    haveUnaddressable = true;
                    break;
                case MIXED:
                    // A child is mixed, which means we are mixed, too
                    return ChildAddressabilitySummary.MIXED;
                default:
                    throw new IllegalStateException("Unhandled accessibility summary for " + child);
            }
        }

        if (!haveAddressable) {
            // Empty or all are unaddressable
            return ChildAddressabilitySummary.UNADDRESSABLE;
        }

        return haveUnaddressable ? ChildAddressabilitySummary.MIXED : ChildAddressabilitySummary.ADDRESSABLE;
    }
}
