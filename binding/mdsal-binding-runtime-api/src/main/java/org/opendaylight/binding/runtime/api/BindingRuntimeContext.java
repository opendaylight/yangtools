/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.api;

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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 *
 * <p>Some of this information are for example list of all available children for cases
 * {@link #getChoiceCaseChildren(DataNodeContainer)}, since choices are augmentable and new choices may be introduced
 * by additional models. Same goes for all possible augmentations.
 */
@Beta
public interface BindingRuntimeContext extends SchemaContextProvider, Immutable {
    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    @NonNull ClassLoadingStrategy getStrategy();

    @NonNull BindingRuntimeTypes getTypes();

    @Override
    default SchemaContext getSchemaContext() {
        return getTypes().getSchemaContext();
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
     * @return Schema of augmentation or null if augmentaiton is not known in this context
     * @throws IllegalArgumentException If supplied class is not an augmentation
     */
    @Nullable AugmentationSchemaNode getAugmentationDefinition(Class<?> augClass);

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

    @Nullable ActionDefinition getActionDefinition(Class<? extends Action<?, ?, ?>> cls);

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

    @NonNull ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(DataNodeContainer container);

    @NonNull Class<?> getIdentityClass(QName input);
}
