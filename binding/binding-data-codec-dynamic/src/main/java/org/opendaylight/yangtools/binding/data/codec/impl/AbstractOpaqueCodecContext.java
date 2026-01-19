/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import javax.xml.transform.dom.DOMSource;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.jar.asm.Opcodes;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.OpaqueData;
import org.opendaylight.yangtools.binding.OpaqueObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingOpaqueObjectCodecTreeNode;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader.GeneratorResult;
import org.opendaylight.yangtools.yang.data.api.schema.ForeignDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract sealed class AbstractOpaqueCodecContext<T extends OpaqueObject<T>> extends ValueNodeCodecContext
        implements BindingOpaqueObjectCodecTreeNode<T> permits AnydataCodecContext, AnyxmlCodecContext {
    private static final MethodType CTOR_LOOKUP_TYPE = MethodType.methodType(void.class, OpaqueData.class);
    private static final MethodType CTOR_INVOKE_TYPE = MethodType.methodType(OpaqueObject.class, OpaqueData.class);
    @SuppressWarnings("rawtypes")
    private static final Builder<CodecOpaqueObject> TEMPLATE = new ByteBuddy().subclass(CodecOpaqueObject.class)
            .modifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC);

    private final AbstractValueCodec<Object, Object> valueCodec = new AbstractValueCodec<>() {
        @Override
        protected Object serializeImpl(final Object input) {
            final T cast;
            try {
                cast = getBindingClass().cast(input);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Unexpected input " + input, e);
            }

            // FIXME: this works for DOMSource only, for generalization we need to pass down the object model, too
            final var opaqueData = requireNonNull(cast).getValue();
            final var data = opaqueData.getData();
            if (data instanceof DOMSource dom) {
                return dom;
            }
            throw new IllegalArgumentException("Unexpected data " + data);
        }

        @Override
        protected Object deserializeImpl(final Object input) {
            checkArgument(input instanceof NormalizedNode, "Unexpected input %s", input);
            return deserializeObject((NormalizedNode) input);
        }
    };

    private final MethodHandle proxyConstructor;

    AbstractOpaqueCodecContext(final DataSchemaNode schema, final String getterName, final Class<T> bindingClass,
            final BindingClassLoader loader) {
        super(schema, getterName, bindingClass, null);
        proxyConstructor = createImpl(loader, bindingClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final Class<T> getBindingClass() {
        return (Class<T>) valueType();
    }

    @Override
    public final T deserialize(final NormalizedNode data) {
        if (data instanceof ForeignDataNode<?> foreign) {
            return deserialize(foreign);
        }
        throw new IllegalArgumentException("Expected a ForeignDataNode, not " + data.contract().getSimpleName());
    }

    T deserialize(final ForeignDataNode<?> foreignData) {
        return getBindingClass().cast(createBindingProxy(new ForeignOpaqueData<>(foreignData)));
    }

    @Override
    public final ForeignDataNode<?> serialize(final T data) {
        final OpaqueData<?> opaqueData = data.getValue();
        return opaqueData instanceof ForeignOpaqueData<?> foreign ? foreign.domData() : serializedData(opaqueData);
    }

    @Override
    protected final @NonNull Object deserializeObject(final NormalizedNode normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    ValueCodec<Object, Object> getValueCodec() {
        return valueCodec;
    }

    abstract @NonNull ForeignDataNode<?> serializedData(OpaqueData<?> opaqueData);

    @SuppressWarnings("checkstyle:illegalCatch")
    private OpaqueObject<?> createBindingProxy(final OpaqueData<?> data) {
        try {
            return (OpaqueObject<?>) proxyConstructor.invokeExact(data);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    private static MethodHandle createImpl(final BindingClassLoader rootLoader, final Class<?> bindingClass) {
        final var proxyClass = CodecPackage.CODEC.generateClass(rootLoader, bindingClass,
            (loader, fqcn, bindingInterface) -> GeneratorResult.of(new UnloadedLoadableClass<>(TEMPLATE
                .name(fqcn)
                .implement(bindingInterface)
                .make())));

        try {
            return MethodHandles.publicLookup().findConstructor(proxyClass, CTOR_LOOKUP_TYPE).asType(CTOR_INVOKE_TYPE);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new LinkageError("Failed to access constructor for prototype " + proxyClass, e);
        }
    }
}
