/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.binding.runtime.api;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.DefaultType;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.Action;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DocumentedNode.WithStatus;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextProvider;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runtime Context for Java YANG Binding classes. It provides information derived from the backing effective model,
 * which is not captured in generated classes (and hence cannot be obtained from {@code BindingReflections}.
 *
 * <p>Some of this information are for example list of all available children for cases
 * {@link #getChoiceCaseChildren(DataNodeContainer)}, since choices are augmentable and new choices may be introduced
 * by additional models. Same goes for all possible augmentations.
 */
@Beta
public final class BindingRuntimeContext implements SchemaContextProvider, Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContext.class);

    private final @NonNull BindingRuntimeTypes runtimeTypes;
    private final @NonNull ClassLoadingStrategy strategy;

    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final Optional<Type> identityType = runtimeTypes.findIdentity(key);
                checkArgument(identityType.isPresent(), "Supplied QName %s is not a valid identity", key);
                try {
                    return strategy.loadClass(identityType.get());
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + identityType + "was not found.", e);
                }
            }
        });

    private BindingRuntimeContext(final BindingRuntimeTypes runtimeTypes, final ClassLoadingStrategy strategy) {
        this.runtimeTypes = requireNonNull(runtimeTypes);
        this.strategy = requireNonNull(strategy);
    }

    /**
     * Creates Binding Runtime Context from supplied class loading strategy and schema context.
     *
     * @param strategy Class loading strategy to retrieve generated Binding classes
     * @param runtimeTypes Binding classes to YANG schema mapping
     * @return A new instance
     */
    public static @NonNull BindingRuntimeContext create(final BindingRuntimeTypes runtimeTypes,
            final ClassLoadingStrategy strategy) {
        return new BindingRuntimeContext(runtimeTypes, strategy);
    }

    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    public @NonNull ClassLoadingStrategy getStrategy() {
        return strategy;
    }

    public @NonNull BindingRuntimeTypes getTypes() {
        return runtimeTypes;
    }

    /**
     * Returns an stable immutable view of schema context associated with this Binding runtime context.
     *
     * @return stable view of schema context
     */
    @Override
    public SchemaContext getSchemaContext() {
        return runtimeTypes.getSchemaContext();
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
    public @Nullable AugmentationSchemaNode getAugmentationDefinition(final Class<?> augClass) {
        checkArgument(Augmentation.class.isAssignableFrom(augClass),
            "Class %s does not represent augmentation", augClass);
        return runtimeTypes.findAugmentation(DefaultType.of(augClass)).orElse(null);
    }

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
    public DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        checkArgument(!Augmentation.class.isAssignableFrom(cls), "Supplied class must not be an augmentation (%s is)",
            cls);
        checkArgument(!Action.class.isAssignableFrom(cls), "Supplied class must not be an action (%s is)", cls);
        return (DataSchemaNode) runtimeTypes.findSchema(DefaultType.of(cls)).orElse(null);
    }

    public ActionDefinition getActionDefinition(final Class<? extends Action<?, ?, ?>> cls) {
        return (ActionDefinition) runtimeTypes.findSchema(DefaultType.of(cls)).orElse(null);
    }

    public Entry<AugmentationIdentifier, AugmentationSchemaNode> getResolvedAugmentationSchema(
            final DataNodeContainer target, final Class<? extends Augmentation<?>> aug) {
        final AugmentationSchemaNode origSchema = getAugmentationDefinition(aug);
        checkArgument(origSchema != null, "Augmentation %s is not known in current schema context", aug);
        /*
         * FIXME: Validate augmentation schema lookup
         *
         * Currently this algorithm, does not verify if instantiated child nodes
         * are real one derived from augmentation schema. The problem with
         * full validation is, if user used copy builders, he may use
         * augmentation which was generated for different place.
         *
         * If this augmentations have same definition, we emit same identifier
         * with data and it is up to underlying user to validate data.
         *
         */
        final Set<QName> childNames = new HashSet<>();
        final Set<DataSchemaNode> realChilds = new HashSet<>();
        for (final DataSchemaNode child : origSchema.getChildNodes()) {
            final DataSchemaNode dataChildQNname = target.getDataChildByName(child.getQName());
            final String childLocalName = child.getQName().getLocalName();
            if (dataChildQNname == null) {
                for (DataSchemaNode dataSchemaNode : target.getChildNodes()) {
                    if (childLocalName.equals(dataSchemaNode.getQName().getLocalName())) {
                        realChilds.add(dataSchemaNode);
                        childNames.add(dataSchemaNode.getQName());
                    }
                }
            } else {
                realChilds.add(dataChildQNname);
                childNames.add(child.getQName());
            }
        }

        final AugmentationIdentifier identifier = AugmentationIdentifier.create(childNames);
        final AugmentationSchemaNode proxy = new EffectiveAugmentationSchema(origSchema, realChilds);
        return new SimpleEntry<>(identifier, proxy);
    }

    /**
     * Returns resolved case schema for supplied class.
     *
     * @param schema Resolved parent choice schema
     * @param childClass Class representing case.
     * @return Optionally a resolved case schema,.empty if the choice is not legal in
     *         the given context.
     * @throws IllegalArgumentException If supplied class does not represent case.
     */
    public Optional<CaseSchemaNode> getCaseSchemaDefinition(final ChoiceSchemaNode schema, final Class<?> childClass) {
        final DataSchemaNode origSchema = getSchemaDefinition(childClass);
        checkArgument(origSchema instanceof CaseSchemaNode, "Supplied schema %s is not case.", origSchema);

        /* FIXME: Make sure that if there are multiple augmentations of same
         * named case, with same structure we treat it as equals
         * this is due property of Binding specification and copy builders
         * that user may be unaware that he is using incorrect case
         * which was generated for choice inside grouping.
         */
        final Optional<CaseSchemaNode> found = findInstantiatedCase(schema, (CaseSchemaNode) origSchema);
        return found;
    }

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
    public Entry<GeneratedType, WithStatus> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(DefaultType.of(type));
    }

    private Entry<GeneratedType, WithStatus> getTypeWithSchema(final Type referencedType) {
        final WithStatus schema = runtimeTypes.findSchema(referencedType).orElseThrow(
            () -> new NullPointerException("Failed to find schema for type " + referencedType));
        final Type definedType = runtimeTypes.findType(schema).orElseThrow(
            () -> new NullPointerException("Failed to find defined type for " + referencedType + " schema " + schema));

        if (definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).build(), schema);
        }
        checkArgument(definedType instanceof GeneratedType, "Type %s is not a GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType, schema);
    }

    public ImmutableMap<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        final Map<Type, Entry<Type, Type>> childToCase = new HashMap<>();

        for (final ChoiceSchemaNode choice :  Iterables.filter(schema.getChildNodes(), ChoiceSchemaNode.class)) {
            final ChoiceSchemaNode originalChoice = getOriginalSchema(choice);
            final Optional<Type> optType = runtimeTypes.findType(originalChoice);
            checkState(optType.isPresent(), "Failed to find generated type for choice %s", originalChoice);
            final Type choiceType = optType.get();

            for (Type caze : runtimeTypes.findCases(choiceType)) {
                final Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType, caze);
                final HashSet<Type> caseChildren = new HashSet<>();
                if (caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).build();
                }
                collectAllContainerTypes((GeneratedType) caze, caseChildren);
                for (final Type caseChild : caseChildren) {
                    childToCase.put(caseChild, caseIdentifier);
                }
            }
        }
        return ImmutableMap.copyOf(childToCase);
    }

    public Set<Class<?>> getCases(final Class<?> choice) {
        final Collection<Type> cazes = runtimeTypes.findCases(DefaultType.of(choice));
        final Set<Class<?>> ret = new HashSet<>(cazes.size());
        for (final Type caze : cazes) {
            try {
                final Class<?> c = strategy.loadClass(caze);
                ret.add(c);
            } catch (final ClassNotFoundException e) {
                LOG.warn("Failed to load class for case {}, ignoring it", caze, e);
            }
        }
        return ret;
    }

    public Class<?> getClassForSchema(final SchemaNode childSchema) {
        final SchemaNode origSchema = getOriginalSchema(childSchema);
        final Optional<Type> clazzType = runtimeTypes.findType(origSchema);
        checkArgument(clazzType.isPresent(), "Failed to find binding type for %s (original %s)",
            childSchema, origSchema);

        try {
            return strategy.loadClass(clazzType.get());
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableMap<AugmentationIdentifier, Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        final Map<AugmentationIdentifier, Type> identifierToType = new HashMap<>();
        if (container instanceof AugmentationTarget) {
            for (final AugmentationSchemaNode augment : ((AugmentationTarget) container).getAvailableAugmentations()) {
                // Augmentation must have child nodes if is to be used with Binding classes
                AugmentationSchemaNode augOrig = augment;
                while (augOrig.getOriginalDefinition().isPresent()) {
                    augOrig = augOrig.getOriginalDefinition().get();
                }

                if (!augment.getChildNodes().isEmpty()) {
                    final Optional<Type> augType = runtimeTypes.findType(augOrig);
                    if (augType.isPresent()) {
                        identifierToType.put(getAugmentationIdentifier(augment), augType.get());
                    }
                }
            }
        }

        return ImmutableMap.copyOf(identifierToType);
    }

    public Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ClassLoadingStrategy", strategy)
                .add("runtimeTypes", runtimeTypes)
                .toString();
    }

    private static AugmentationIdentifier getAugmentationIdentifier(final AugmentationSchemaNode augment) {
        // FIXME: use DataSchemaContextNode.augmentationIdentifierFrom() once it does caching
        return AugmentationIdentifier.create(augment.getChildNodes().stream().map(DataSchemaNode::getQName)
            .collect(ImmutableSet.toImmutableSet()));
    }

    private static Set<Type> collectAllContainerTypes(final GeneratedType type, final Set<Type> collection) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            Type childType = definition.getReturnType();
            if (childType instanceof ParameterizedType) {
                childType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            }
            if (childType instanceof GeneratedType || childType instanceof GeneratedTypeBuilder) {
                collection.add(childType);
            }
        }
        for (final Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllContainerTypes((GeneratedType) parent, collection);
            }
        }
        return collection;
    }

    private static <T extends SchemaNode> T getOriginalSchema(final T choice) {
        @SuppressWarnings("unchecked")
        final T original = (T) SchemaNodeUtils.getRootOriginalIfPossible(choice);
        if (original != null) {
            return original;
        }
        return choice;
    }

    private static Optional<CaseSchemaNode> findInstantiatedCase(final ChoiceSchemaNode instantiatedChoice,
            final CaseSchemaNode originalDefinition) {
        CaseSchemaNode potential = instantiatedChoice.getCaseNodeByName(originalDefinition.getQName());
        if (originalDefinition.equals(potential)) {
            return Optional.of(potential);
        }
        if (potential != null) {
            SchemaNode potentialRoot = SchemaNodeUtils.getRootOriginalIfPossible(potential);
            if (originalDefinition.equals(potentialRoot)) {
                return Optional.of(potential);
            }
        }

        // We try to find case by name, then lookup its root definition
        // and compare it with original definition
        // This solves case, if choice was inside grouping
        // which was used in different module and thus namespaces are
        // different, but local names are still same.
        //
        // Still we need to check equality of definition, because local name is not
        // sufficient to uniquelly determine equality of cases
        //
        for (CaseSchemaNode caze : instantiatedChoice.findCaseNodes(originalDefinition.getQName().getLocalName())) {
            if (originalDefinition.equals(SchemaNodeUtils.getRootOriginalIfPossible(caze))) {
                return Optional.of(caze);
            }
        }
        return Optional.empty();
    }
}
