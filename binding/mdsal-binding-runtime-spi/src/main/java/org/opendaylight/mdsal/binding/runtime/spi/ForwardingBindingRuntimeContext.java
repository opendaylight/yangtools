/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.AugmentRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.CompositeRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RpcRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.RuntimeType;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
public abstract class ForwardingBindingRuntimeContext extends ForwardingObject implements BindingRuntimeContext {
    @Override
    protected abstract BindingRuntimeContext delegate();

    @Override
    public BindingRuntimeTypes getTypes() {
        return delegate().getTypes();
    }

    @Override
    public <T extends Augmentation<?>> AugmentRuntimeType getAugmentationDefinition(final Class<T> augClass) {
        return delegate().getAugmentationDefinition(augClass);
    }

    @Override
    public CompositeRuntimeType getSchemaDefinition(final Class<?> cls) {
        return delegate().getSchemaDefinition(cls);
    }

    @Override
    public ActionRuntimeType getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return delegate().getActionDefinition(cls);
    }

    @Override
    public RpcRuntimeType getRpcDefinition(final Class<? extends Rpc<?, ?>> cls) {
        return delegate().getRpcDefinition(cls);
    }

    @Override
    public RuntimeType getTypeWithSchema(final Class<?> type) {
        return delegate().getTypeWithSchema(type);
    }

    @Override
    public Class<?> getClassForSchema(final Absolute schema) {
        return delegate().getClassForSchema(schema);
    }

    @Override
    public Class<? extends BaseIdentity> getIdentityClass(final QName input) {
        return delegate().getIdentityClass(input);
    }

    @Override
    public <T> Class<T> loadClass(final JavaTypeName typeName) throws ClassNotFoundException {
        return delegate().loadClass(typeName);
    }

    @Override
    public Class<? extends RpcInput> getRpcInput(final QName rpcName) {
        return delegate().getRpcInput(rpcName);
    }

    @Override
    public Class<? extends RpcOutput> getRpcOutput(final QName rpcName) {
        return delegate().getRpcOutput(rpcName);
    }
}
