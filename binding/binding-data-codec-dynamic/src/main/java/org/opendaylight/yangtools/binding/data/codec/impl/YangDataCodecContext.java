/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.data.codec.api.BindingYangDataCodecTreeNode;
import org.opendaylight.yangtools.binding.runtime.api.YangDataRuntimeType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedYangData;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;

/**
 * A {@link YangData} codec context. While this class is public, it not part of API surface and is an implementation
 * detail. The only reason for it being public is that it needs to be accessible by code generated at runtime.
 *
 * @param <T> YangData type
 */
public final class YangDataCodecContext<T extends YangData<T>>
        extends AnalyzedDataContainerCodecContext<T, YangDataRuntimeType, YangDataCodecPrototype<T>>
        implements BindingYangDataCodecTreeNode<T> {
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        YangDataCodecContext.class, NormalizedYangData.class);
    private static final MethodType YANGDATA_TYPE = MethodType.methodType(YangData.class,
        YangDataCodecContext.class, NormalizedYangData.class);

    private final MethodHandle proxyConstructor;

    private YangDataCodecContext(final YangDataCodecPrototype<T> prototype, final DataContainerAnalysis<?> analysis) {
        super(prototype, new DataContainerAnalysis<>(prototype));

        final var generatedClass = CodecYangDataGenerator.generate(prototype().contextFactory().getLoader(),
            getBindingClass(), analysis.leafContexts, analysis.daoProperties);

        // All done: acquire the constructor: it is supposed to be public
        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(YANGDATA_TYPE);
    }

    YangDataCodecContext(final YangDataCodecPrototype<T> prototype) {
        this(prototype, new DataContainerAnalysis<>(prototype));
    }

    YangDataCodecContext(final Class<T> javaClass, final YangDataRuntimeType runtimeType,
            final CodecContextFactory contextFactory) {
        this(new YangDataCodecPrototype<>(contextFactory, runtimeType, javaClass));
    }

    @Override
    @Deprecated(since = "13.0.0", forRemoval = true)
    public WithStatus getSchema() {
        // FIXME: Bad cast, we should be returning an EffectiveStatement perhaps?
        return (WithStatus) prototype().runtimeType().statement();
    }

    @Override
    protected NodeIdentifier getDomPathArgument() {
        return null;
    }

    @Override
    @SuppressWarnings("checkstyle:illegalCatch")
    public T toBinding(final NormalizedYangData dom) {
        // Pre-flight checks: the names have to match
        final var expectedName = prototype().runtimeType().statement().argument();
        final var actualName = dom.name();
        if (!expectedName.equals(actualName)) {
            throw new IllegalArgumentException("Can only convert " + expectedName + ", not " + actualName);
        }

        try {
            return (T) proxyConstructor.invokeExact(this, dom);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public NormalizedYangData fromBinding(final T binding) {
        // Defend against generic misuse
        final var casted = getBindingClass().cast(requireNonNull(binding));

        // Fast path: this is our codec object, just extract the underlying data
        if (casted instanceof CodecYangData<?> codec && codec.codecContext() == this) {
            return codec.codecData();
        }

        // FIXME: implement this by streaming contents
        throw new UnsupportedOperationException();
    }

    @Override
    T deserializeObject(final NormalizedNode normalizedNode) {
        throw new UnsupportedOperationException("This method should never be called");
    }
}
