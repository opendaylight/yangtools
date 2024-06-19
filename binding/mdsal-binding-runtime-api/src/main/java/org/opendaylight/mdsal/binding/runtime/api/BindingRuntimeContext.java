/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 *
 * <p>Some of this information are for example list of all available children for cases
 * {@link #getChoiceCaseChildren(DataNodeContainer)}, since choices are augmentable and new choices may be introduced
 * by additional models. Same goes for all possible augmentations.
 */
@Beta
// FIXME: refactor return to follow foo()/getFoo()/findFoo() naming
public interface BindingRuntimeContext extends EffectiveModelContextProvider, Immutable {
    @NonNull BindingRuntimeTypes getTypes();

    @NonNull <T> Class<T> loadClass(Type type) throws ClassNotFoundException;

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
     * <p>For retrieving {@link AugmentationSchemaNode}, which will contains
     * full model for child nodes, you should use method
     * {@link #getResolvedAugmentationSchema(DataNodeContainer, Class)}
     * which will return augmentation schema derived from supplied augmentation target
     * schema.
     *
     * @param augClass Augmentation class
     * @return Schema of augmentation or null if augmentation is not known in this context
     */
    <T extends Augmentation<?>> @Nullable AugmentationSchemaNode getAugmentationDefinition(Class<T> augClass);

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
    @Nullable DataSchemaNode getSchemaDefinition(Class<?> cls);

    // FIXME: document this thing and perhaps move it to BindingRuntimeTypes?
    @Nullable DataSchemaNode findChildSchemaDefinition(DataNodeContainer parentSchema, QNameModule parentNamespace,
        Class<?> childClass);

    @Nullable ActionDefinition getActionDefinition(Class<? extends Action<?, ?, ?>> cls);

    @Deprecated(forRemoval = true)
    @Nullable Absolute getActionIdentifier(Class<? extends Action<?, ?, ?>> cls);

    @NonNull Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            DataNodeContainer target, Class<? extends Augmentation<?>> aug);

    /**
     * Returns resolved case schema for supplied class.
     *
     * @param schema Resolved parent choice schema
     * @param childClass Class representing case.
     * @return Optionally a resolved case schema,.empty if the choice is not legal in
     *         the given context.
     * @throws IllegalArgumentException If supplied class does not represent case.
     */
    @NonNull Optional<CaseSchemaNode> getCaseSchemaDefinition(ChoiceSchemaNode schema, Class<?> childClass);

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
    @NonNull Entry<GeneratedType, WithStatus> getTypeWithSchema(Class<?> type);

    @NonNull Map<Type, Entry<Type, Type>> getChoiceCaseChildren(DataNodeContainer schema);

    @NonNull Set<Class<?>> getCases(Class<?> choice);

    @NonNull Class<?> getClassForSchema(SchemaNode childSchema);

    /**
     * Return the mapping of a particular {@link DataNodeContainer}'s available augmentations. This method deals with
     * resolving {@code uses foo { augment bar { ... } } } scenarios by returning the augmentation created for
     * {@code grouping foo}'s Binding representation.
     *
     * @param container {@link DataNodeContainer} to examine
     * @return a mapping from local {@link AugmentationIdentifier}s to their corresponding Binding augmentations
     */
    @NonNull ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(DataNodeContainer container);

    @NonNull Class<?> getIdentityClass(QName input);
}
