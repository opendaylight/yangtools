/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.List;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

abstract class DataContainerCodecContext<T> extends NodeCodecContext {

    protected final T schema;
    protected final QNameModule namespace;
    protected final CodecContextFactory factory;
    protected final Class<?> bindingClass;
    protected final InstanceIdentifier.Item<?> bindingArg;

    protected final LoadingCache<Class<?>, DataContainerCodecContext<?>> containerChild;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected DataContainerCodecContext(final Class<?> cls, final QNameModule namespace, final T nodeSchema,
            final CodecContextFactory factory) {
        super();
        this.schema = nodeSchema;
        this.factory = factory;
        this.namespace = namespace;
        this.bindingClass = cls;
        this.bindingArg = new InstanceIdentifier.Item(bindingClass);

        this.containerChild = CacheBuilder.newBuilder().build(new CacheLoader<Class<?>, DataContainerCodecContext<?>>() {
            @Override
            public DataContainerCodecContext<?> load(final Class<?> key) throws Exception {
                return loadChild(key);
            }
        });
    }

    static DataContainerCodecContext<?> from(final Class<?> cls, final DataSchemaNode schema,
            final CodecContextFactory loader) {
        if (schema instanceof ContainerSchemaNode) {
            return new ContainerNodeCodecContext(cls, (ContainerSchemaNode) schema, loader);
        } else if (schema instanceof ListSchemaNode) {
            return new ListNodeCodecContext(cls, (ListSchemaNode) schema, loader);
        } else if (schema instanceof ChoiceNode) {
            return new ChoiceNodeCodecContext(cls, (ChoiceNode) schema, loader);
        }
        throw new IllegalArgumentException("Not supported type " + cls + " " + schema);
    }

    protected  T getSchema() {
        return schema;
    }

    /**
     * Returns nested node context using supplied YANG Instance Identifier
     *
     * @param arg Yang Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    protected abstract NodeCodecContext getYangIdentifierChild(final YangInstanceIdentifier.PathArgument arg);

    /**
     * Returns nested node context using supplied Binding Instance Identifier
     * and adds YANG instance identifiers to supplied list.
     *
     * @param arg Binding Instance Identifier Argument
     * @return Context of child
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    protected  DataContainerCodecContext<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        final DataContainerCodecContext<?> child = getStreamChild(arg.getType());
        if (builder != null) {
            child.addYangPathArgument(arg,builder);
        }
        return child;
    }

    /**
     *
     * Returns deserialized Binding Path Argument from YANG instance identifier.
     *
     * @param domArg
     * @return
     */
    protected  PathArgument getBindingPathArgument(final YangInstanceIdentifier.PathArgument domArg) {
        return bindingArg;
    }

    /**
     *
     * Returns child context as if it was walked by
     * {@link BindingStreamEventWriter}. This means that to enter case, one
     * must issue getChild(ChoiceClass).getChild(CaseClass).
     *
     * @param childClass
     * @return Context of child
     */
    protected  DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) {
        return containerChild.getUnchecked(childClass);
    }

    /**
     * Loads children identified by supplied class. If children is not
     * valid, throws {@link IllegalArgumentException}.
     *
     * @param childClass
     * @return Context of child
     */
    protected abstract DataContainerCodecContext<?> loadChild(final Class<?> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + bindingClass + "]";
    }

}