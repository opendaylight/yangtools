/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.Rpc;
import org.opendaylight.yangtools.yang.binding.RpcInput;
import org.opendaylight.yangtools.yang.binding.RpcOutput;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 */
@Beta
// FIXME: refactor return to follow foo()/getFoo()/findFoo() naming
public interface BindingRuntimeContext extends EffectiveModelContextProvider, Immutable {
    @NonNull BindingRuntimeTypes getTypes();

    @NonNull <T> Class<T> loadClass(JavaTypeName type) throws ClassNotFoundException;

    default @NonNull <T> Class<T> loadClass(final Type type) throws ClassNotFoundException {
        return loadClass(type.getIdentifier());
    }

    @Override
    default EffectiveModelContext getEffectiveModelContext() {
        return getTypes().getEffectiveModelContext();
    }

    /**
     * Returns schema of augmentation.
     *
     * <p>Returned schema is schema definition from which augmentation class was generated.
     * This schema is isolated from other augmentations. This means it contains
     * augmentation definition as was present in original YANG module.
     *
     * <p>Children of returned schema does not contain any additional augmentations,
     * which may be present in runtime for them, thus returned schema is unsuitable
     * for use for validation of data.
     *
     * @param <T> Augmentation class type
     * @param augClass Augmentation class
     * @return Schema of augmentation or null if augmentation is not known in this context
     * @throws NullPointerException if {@code augClass} is null
     */
    <T extends Augmentation<?>> @Nullable AugmentRuntimeType getAugmentationDefinition(Class<T> augClass);

    /**
     * Returns defining {@link DataSchemaNode} for supplied class.
     *
     * <p>Returned schema is schema definition from which class was generated.
     * This schema may be isolated from augmentations, if supplied class
     * represent node, which was child of grouping or augmentation.
     *
     * <p>For getting augmentation schema from augmentation class use
     * {@link #getAugmentationDefinition(Class)} instead.
     *
     * @param cls Class which represents list, container, choice or case.
     * @return Schema node, from which class was generated.
     */
    @Nullable CompositeRuntimeType getSchemaDefinition(Class<?> cls);

    @Nullable ActionRuntimeType getActionDefinition(Class<? extends Action<?, ?, ?>> cls);

    @Nullable RpcRuntimeType getRpcDefinition(Class<? extends Rpc<?, ?>> cls);

    /**
     * Returns schema ({@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition})
     * from which supplied class was generated. Returned schema may be augmented with
     * additional information, which was not available at compile type
     * (e.g. third party augmentations).
     *
     * @param type Binding Class for which schema should be retrieved.
     * @return Instance of generated type (definition of Java API), along with
     *     {@link DataSchemaNode}, {@link AugmentationSchemaNode} or {@link TypeDefinition}
     *     which was used to generate supplied class.
     */
    @NonNull RuntimeType getTypeWithSchema(Class<?> type);

    @NonNull Class<? extends RpcInput> getRpcInput(QName rpcName);

    @NonNull Class<? extends RpcOutput> getRpcOutput(QName rpcName);

    // FIXME: 9.0.0: this needs to accept an EffectiveStatementInference
    @NonNull Class<?> getClassForSchema(Absolute schema);

    @NonNull Class<? extends BaseIdentity> getIdentityClass(QName input);
}
