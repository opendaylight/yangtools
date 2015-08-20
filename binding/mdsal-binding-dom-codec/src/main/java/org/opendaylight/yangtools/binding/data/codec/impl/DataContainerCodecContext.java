/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeCachingCodec;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;

abstract class DataContainerCodecContext<D extends DataObject,T> extends NodeCodecContext<D>  {

    private final DataContainerCodecPrototype<T> prototype;
    private volatile DataObjectSerializer eventStreamSerializer;

    protected DataContainerCodecContext(final DataContainerCodecPrototype<T> prototype) {
        this.prototype = prototype;
    }

    protected final T schema() {
        return prototype.getSchema();
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
     * Returns nested node context using supplied YANG Instance Identifier
     *
     * @param arg Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @Override
    public abstract NodeCodecContext<?> yangPathArgumentChild(final YangInstanceIdentifier.PathArgument arg);

    /**
     * Returns nested node context using supplied Binding Instance Identifier
     * and adds YANG instance identifiers to supplied list.
     *
     * @param arg Binding Instance Identifier Argument
     * @return Context of child or null if supplied {@code arg} does not represent valid child.
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    @Override
    public @Nullable DataContainerCodecContext<?,?> bindingPathArgumentChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        final DataContainerCodecContext<?,?> child = streamChild(arg.getType());
        if(child != null) {
            if (builder != null) {
                child.addYangPathArgument(arg,builder);
            }
            return child;
        }
        throw new IllegalArgumentException("SUpplied argument is not valid child");
    }

    /**
     * Returns deserialized Binding Path Argument from YANG instance identifier.
     *
     * @param domArg
     * @return
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

    /**
     *
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one
     * must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass
     * @return Context of child node or null, if supplied class is not subtree child
     * @throws IllegalArgumentException If supplied child class is not valid in specified context.
     */
    @Override
    public abstract @Nullable <DV extends DataObject> DataContainerCodecContext<DV,?> streamChild(final Class<DV> childClass) throws IllegalArgumentException;

    /**
     *
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one
     * must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass
     * @return Context of child or Optional absent is supplied class is not applicable in context.
     */
    @Override
    public abstract <DV extends DataObject> Optional<DataContainerCodecContext<DV,?>> possibleStreamChild(final Class<DV> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + prototype.getBindingClass() + "]";
    }

    @Override
    public BindingNormalizedNodeCachingCodec<D> createCachingCodec(
            final ImmutableCollection<Class<? extends DataObject>> cacheSpecifier) {
        if(cacheSpecifier.isEmpty()) {
            return new NonCachingCodec<>(this);
        }
        return new CachingNormalizedNodeCodec<D>(this,ImmutableSet.copyOf(cacheSpecifier));
    }

    BindingStreamEventWriter createWriter(final NormalizedNodeStreamWriter domWriter) {
        return  new BindingToNormalizedStreamWriter(this, domWriter);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final YangInstanceIdentifier.PathArgument child,
            final String message, final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaException.checkModulePresent(factory().getRuntimeContext().getSchemaContext(), child);
        throw IncorrectNestingException.create(message, args);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final QName child, final String message,
            final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaException.checkModulePresent(factory().getRuntimeContext().getSchemaContext(), child);
        throw IncorrectNestingException.create(message, args);
    }

    @Nonnull
    protected final <V> V childNonNull(@Nullable final V nullable, final Class<?> childClass, final String message,
            final Object... args) {
        if (nullable != null) {
            return nullable;
        }
        MissingSchemaForClassException.check(factory().getRuntimeContext(), childClass);
        MissingClassInLoadingStrategyException.check(factory().getRuntimeContext().getStrategy(), childClass);
        throw IncorrectNestingException.create(message, args);
    }

    DataObjectSerializer eventStreamSerializer() {
        if(eventStreamSerializer == null) {
            eventStreamSerializer = factory().getEventStreamSerializer(getBindingClass());
        }
        return eventStreamSerializer;
    }

    @Override
    public NormalizedNode<?, ?> serialize(final D data) {
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

}