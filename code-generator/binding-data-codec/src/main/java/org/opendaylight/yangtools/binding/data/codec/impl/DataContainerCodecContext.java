/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import com.google.common.base.Optional;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;

abstract class DataContainerCodecContext<T> extends NodeCodecContext {

    private final DataContainerCodecPrototype<T> prototype;

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

    protected final Class<?> getBindingClass() {
        return Class.class.cast(prototype.getBindingClass());
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
     * @return Context of child or null if supplied {@code arg} does not represent valid child.
     * @throws IllegalArgumentException If supplied argument does not represent valid child.
     */
    protected @Nullable DataContainerCodecContext<?> getIdentifierChild(final InstanceIdentifier.PathArgument arg,
            final List<YangInstanceIdentifier.PathArgument> builder) {
        final DataContainerCodecContext<?> child = getStreamChild(arg.getType());
        if(child != null) {
            if (builder != null) {
                child.addYangPathArgument(arg,builder);
            }
            return child;
        }
        return null;
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

    protected final Class<?> bindingClass() {
        return prototype.getBindingClass();
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
    protected abstract @Nullable DataContainerCodecContext<?> getStreamChild(final Class<?> childClass) throws IllegalArgumentException;

    /**
    *
    * Returns child context as if it was walked by
    * {@link BindingStreamEventWriter}. This means that to enter case, one
    * must issue getChild(ChoiceClass).getChild(CaseClass).
    *
    * @param childClass
    * @return Context of child or Optional absent is supplied class is not applicable in context.
    */
   protected abstract Optional<DataContainerCodecContext<?>> getPossibleStreamChild(final Class<?> childClass);

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + prototype.getBindingClass() + "]";
    }

}