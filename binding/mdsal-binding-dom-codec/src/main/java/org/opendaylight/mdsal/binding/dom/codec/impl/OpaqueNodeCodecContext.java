/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingOpaqueObjectCodecTreeNode;
import org.opendaylight.mdsal.binding.dom.codec.loader.CodecClassLoader;
import org.opendaylight.mdsal.binding.dom.codec.loader.StaticClassPool;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.OpaqueData;
import org.opendaylight.yangtools.yang.binding.OpaqueObject;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.api.schema.ForeignDataNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

abstract class OpaqueNodeCodecContext<T extends OpaqueObject<T>> extends ValueNodeCodecContext
        implements BindingOpaqueObjectCodecTreeNode<T> {
    static final class AnyXml<T extends OpaqueObject<T>> extends OpaqueNodeCodecContext<T> {
        AnyXml(final AnyXmlSchemaNode schema, final Method getter, final Class<T> bindingClass,
                final CodecClassLoader loader) {
            super(schema, getter, bindingClass, loader);
        }

        @Override
        ForeignDataNode<?, ?> serializedData(final OpaqueData<?> opaqueData) {
            final Class<?> model = opaqueData.getObjectModel();
            verify(DOMSource.class.isAssignableFrom(model), "Cannot just yet support object model %s", model);
            return Builders.anyXmlBuilder().withNodeIdentifier(getDomPathArgument())
                    .withValue((DOMSource) opaqueData.getData()).build();
        }
    }

    private static final CtClass SUPERCLASS = StaticClassPool.findClass(CodecOpaqueObject.class);
    private static final MethodType CONSTRUCTOR_TYPE = MethodType.methodType(OpaqueObject.class,
        OpaqueData.class);

    private final Codec<Object, Object> valueCodec = new Codec<Object, Object>() {
        @Override
        public Object serialize(final Object input) {
            checkArgument(bindingClass.isInstance(input), "Unexpected input %s", input);
            // FIXME: this works for DOMSource only, for generalization we need to pass down the object model, too
            final OpaqueData<?> opaqueData = bindingClass.cast(input).getValue();
            final Object data = opaqueData.getData();
            checkArgument(data instanceof DOMSource, "Unexpected data %s", data);
            return data;
        }

        @Override
        public Object deserialize(final Object input) {
            checkArgument(input instanceof NormalizedNode, "Unexpected input %s", input);
            return OpaqueNodeCodecContext.this.deserializeObject((NormalizedNode<?, ?>) input);
        }
    };

    private final MethodHandle proxyConstructor;
    private final @NonNull Class<T> bindingClass;

    OpaqueNodeCodecContext(final DataSchemaNode schema, final Method getter, final Class<T> bindingClass,
            final CodecClassLoader loader) {
        super(schema, getter, null);
        this.bindingClass = requireNonNull(bindingClass);
        proxyConstructor = createImpl(loader, bindingClass);
    }

    @Override
    public final Class<T> getBindingClass() {
        return bindingClass;
    }

    @Override
    public final T deserialize(final NormalizedNode<?, ?> data) {
        checkArgument(data instanceof ForeignDataNode, "Unexpected value %s", data);
        final ForeignDataNode<?, ?> foreignData = (ForeignDataNode<?, ?>) data;
        // Streaming cannot support anything but DOMSource-based AnyxmlNodes.
        verify(foreignData instanceof AnyXmlNode, "Variable node %s not supported yet", foreignData);

        return bindingClass.cast(createBindingProxy(new ForeignOpaqueData<>(foreignData)));
    }

    @Override
    public final ForeignDataNode<?, ?> serialize(final T data) {
        final OpaqueData<?> opaqueData = data.getValue();
        return opaqueData instanceof ForeignOpaqueData ? ((ForeignOpaqueData<?>) opaqueData).domData()
                : serializedData(opaqueData);
    }

    @Override
    protected final @NonNull Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return deserialize(normalizedNode);
    }

    @Override
    Codec<Object, Object> getValueCodec() {
        return valueCodec;
    }

    abstract @NonNull ForeignDataNode<?, ?> serializedData(OpaqueData<?> opaqueData);

    @SuppressWarnings("checkstyle:illegalCatch")
    private OpaqueObject<?> createBindingProxy(final OpaqueData<?> data) {
        try {
            return (OpaqueObject<?>) proxyConstructor.invokeExact(data);
        } catch (final Throwable e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException(e);
        }
    }

    private static MethodHandle createImpl(final CodecClassLoader rootLoader, final Class<?> bindingClass) {
        final Class<?> proxyClass;
        try {
            proxyClass = rootLoader.generateSubclass(SUPERCLASS, bindingClass, "codecImpl",
                (pool, binding, generated) -> {
                    generated.addInterface(binding);
                    generated.setModifiers(Modifier.PUBLIC | Modifier.FINAL);
                });
        } catch (CannotCompileException | IOException | NotFoundException e) {
            throw new LinkageError("Failed to instantiate prototype for " + bindingClass, e);
        }

        Constructor<?> ctor;
        try {
            ctor = proxyClass.getDeclaredConstructor(OpaqueData.class);
        } catch (NoSuchMethodException e) {
            throw new LinkageError("Failed to acquire constructor for prototype " + proxyClass, e);
        }
        try {
            return MethodHandles.publicLookup().unreflectConstructor(ctor).asType(CONSTRUCTOR_TYPE);
        } catch (IllegalAccessException e) {
            throw new LinkageError("Failed to access constructor for prototype " + proxyClass, e);
        }
    }
}
