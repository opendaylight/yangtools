/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import com.google.common.base.VerifyException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentableRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;

/**
 * Analysis of a {@link DataObject} specialization class. The primary point of this class is to separate out creation
 * indices needed for {@link #proxyConstructor}. Since we want to perform as much indexing as possible in a single pass,
 * we also end up indexing things that are not strictly required to arrive at that constructor.
 */
final class CodecDataObjectAnalysis<R extends CompositeRuntimeType> extends AbstractDataContainerAnalysis<R> {
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(void.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);
    private static final MethodType DATAOBJECT_TYPE = MethodType.methodType(DataObject.class,
        AbstractDataObjectCodecContext.class, DataContainerNode.class);

    final @NonNull Class<? extends CodecDataObject<?>> generatedClass;
    final @NonNull List<AugmentRuntimeType> possibleAugmentations;
    final @NonNull MethodHandle proxyConstructor;

    CodecDataObjectAnalysis(final CommonDataObjectCodecPrototype<R> prototype, final CodecItemFactory itemFactory,
            final Method keyMethod) {
        this(prototype.getBindingClass(), prototype.getType(), prototype.getFactory(), itemFactory, keyMethod);
    }

    CodecDataObjectAnalysis(final Class<?> bindingClass, final R runtimeType, final CodecContextFactory factory,
            final CodecItemFactory itemFactory, final Method keyMethod) {
        super(bindingClass, runtimeType, factory, itemFactory);

        // Final bits: generate the appropriate class, As a side effect we identify what Augmentations are possible
        if (Augmentable.class.isAssignableFrom(bindingClass)) {
            // Verify we have the appropriate backing runtimeType
            if (!(runtimeType instanceof AugmentableRuntimeType augmentableRuntimeType)) {
                throw new VerifyException(
                    "Unexpected type %s backing augmenable %s".formatted(runtimeType, bindingClass));
            }

            possibleAugmentations = augmentableRuntimeType.augments();
            generatedClass = CodecDataObjectGenerator.generateAugmentable(factory.getLoader(), bindingClass,
                leafContexts, daoProperties, keyMethod);
        } else {
            possibleAugmentations = List.of();
            generatedClass = CodecDataObjectGenerator.generate(factory.getLoader(), bindingClass, leafContexts,
                daoProperties, keyMethod);
        }

        // All done: acquire the constructor: it is supposed to be public
        final MethodHandle ctor;
        try {
            ctor = MethodHandles.publicLookup().findConstructor(generatedClass, CONSTRUCTOR_TYPE);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new LinkageError("Failed to find contructor for class " + generatedClass, e);
        }

        proxyConstructor = ctor.asType(DATAOBJECT_TYPE);
    }
}
