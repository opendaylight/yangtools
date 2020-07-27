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
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.runtime.api.BindingRuntimeTypes;
import org.opendaylight.mdsal.binding.runtime.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

@Beta
public abstract class ForwardingBindingRuntimeContext extends ForwardingObject implements BindingRuntimeContext {
    @Override
    protected abstract BindingRuntimeContext delegate();

    @Override
    public ClassLoadingStrategy getStrategy() {
        return delegate().getStrategy();
    }

    @Override
    public BindingRuntimeTypes getTypes() {
        return delegate().getTypes();
    }

    @Override
    public AugmentationSchemaNode getAugmentationDefinition(final Class<?> augClass) {
        return delegate().getAugmentationDefinition(augClass);
    }

    @Override
    public DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        return delegate().getSchemaDefinition(cls);
    }

    @Override
    public ActionDefinition getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return delegate().getActionDefinition(cls);
    }

    @Override
    public Absolute getActionIdentifier(final Class<? extends Action<?, ?, ?>> cls) {
        return delegate().getActionIdentifier(cls);
    }

    @Override
    public Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
        return delegate().getResolvedAugmentationSchema(target, aug);
    }

    @Override
    public Optional<CaseSchemaNode> getCaseSchemaDefinition(final ChoiceSchemaNode schema, final Class<?> childClass) {
        return delegate().getCaseSchemaDefinition(schema, childClass);
    }

    @Override
    public Entry<GeneratedType, WithStatus> getTypeWithSchema(final Class<?> type) {
        return delegate().getTypeWithSchema(type);
    }

    @Override
    public Map<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        return delegate().getChoiceCaseChildren(schema);
    }

    @Override
    public Set<Class<?>> getCases(final Class<?> choice) {
        return delegate().getCases(choice);
    }

    @Override
    public Class<?> getClassForSchema(final SchemaNode childSchema) {
        return delegate().getClassForSchema(childSchema);
    }

    @Override
    public ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        return delegate().getAvailableAugmentationTypes(container);
    }

    @Override
    public Class<?> getIdentityClass(final QName input) {
        return delegate().getIdentityClass(input);
    }
}
