/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingSchemaContextUtils;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.sal.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.EffectiveAugmentationSchema;
import org.opendaylight.yangtools.yang.model.util.EnumerationType;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 *
 * Runtime Context for Java YANG Binding classes
 *
 *<p>
 * Runtime Context provides additional insight in Java YANG Binding,
 * binding classes and underlying YANG schema, it contains
 * runtime information, which could not be derived from generated
 * classes alone using {@link org.opendaylight.yangtools.yang.binding.util.BindingReflections}.
 * <p>
 * Some of this information are for example list of all available
 * children for cases {@link #getChoiceCaseChildren(DataNodeContainer)}, since
 * choices are augmentable and new choices may be introduced by additional models.
 * <p>
 * Same goes for all possible augmentations.
 *
 */
public class BindingRuntimeContext implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(BindingRuntimeContext.class);
    private static final char DOT = '.';
    private final ClassLoadingStrategy strategy;
    private final SchemaContext schemaContext;

    private final Map<Type, AugmentationSchema> augmentationToSchema = new HashMap<>();
    private final BiMap<Type, Object> typeToDefiningSchema = HashBiMap.create();
    private final Multimap<Type, Type> augmentableToAugmentations = HashMultimap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Map<QName, Type> identities = new HashMap<>();

    private final LoadingCache<QName, Class<?>> identityClasses = CacheBuilder.newBuilder().weakValues().build(
        new CacheLoader<QName, Class<?>>() {
            @Override
            public Class<?> load(final QName key) {
                final Type identityType = identities.get(key);
                Preconditions.checkArgument(identityType != null, "Supplied QName %s is not a valid identity", key);
                try {
                    return strategy.loadClass(identityType);
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException("Required class " + identityType + "was not found.", e);
                }
            }
        });

    private BindingRuntimeContext(final ClassLoadingStrategy strategy, final SchemaContext schema) {
        this.strategy = strategy;
        this.schemaContext = schema;

        final BindingGeneratorImpl generator = new BindingGeneratorImpl(false);
        generator.generateTypes(schema);
        final Map<Module, ModuleContext> modules = generator.getModuleContexts();

        for (final ModuleContext ctx : modules.values()) {
            augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            typeToDefiningSchema.putAll(ctx.getTypeToSchema());

            ctx.getTypedefs();
            augmentableToAugmentations.putAll(ctx.getAugmentableToAugmentations());
            choiceToCases.putAll(ctx.getChoiceToCases());
            identities.putAll(ctx.getIdentities());
        }
    }

    /**
     *
     * Creates Binding Runtime Context from supplied class loading strategy and schema context.
     *
     * @param strategy Class loading strategy to retrieve generated Binding classes
     * @param ctx Schema Context which describes YANG model and to which Binding classes should be mapped
     * @return Instance of BindingRuntimeContext for supplied schema context.
     */
    public static final BindingRuntimeContext create(final ClassLoadingStrategy strategy, final SchemaContext ctx) {
        return new BindingRuntimeContext(strategy, ctx);
    }

    /**
     * Returns a class loading strategy associated with this binding runtime context
     * which is used to load classes.
     *
     * @return Class loading strategy.
     */
    public ClassLoadingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Returns an stable immutable view of schema context associated with this Binding runtime context.
     *
     * @return stable view of schema context
     */
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    /**
     * Returns schema of augmentation
     * <p>
     * Returned schema is schema definition from which augmentation class was generated.
     * This schema is isolated from other augmentations. This means it contains
     * augmentation definition as was present in original YANG module.
     * <p>
     * Children of returned schema does not contain any additional augmentations,
     * which may be present in runtime for them, thus returned schema is unsuitable
     * for use for validation of data.
     * <p>
     * For retrieving {@link AugmentationSchema}, which will contains
     * full model for child nodes, you should use method {@link #getResolvedAugmentationSchema(DataNodeContainer, Class)}
     * which will return augmentation schema derived from supplied augmentation target
     * schema.
     *
     * @param augClass Augmentation class
     * @return Schema of augmentation or null if augmentaiton is not known in this context
     * @throws IllegalArgumentException If supplied class is not an augmentation
     */
    public @Nullable AugmentationSchema getAugmentationDefinition(final Class<?> augClass) throws IllegalArgumentException {
        Preconditions.checkArgument(Augmentation.class.isAssignableFrom(augClass), "Class %s does not represent augmentation", augClass);
        return augmentationToSchema.get(referencedType(augClass));
    }

    /**
     * Returns defining {@link DataSchemaNode} for supplied class.
     *
     * <p>
     * Returned schema is schema definition from which class was generated.
     * This schema may be isolated from augmentations, if supplied class
     * represent node, which was child of grouping or augmentation.
     * <p>
     * For getting augmentation schema from augmentation class use
     * {@link #getAugmentationDefinition(Class)} instead.
     *
     * @param cls Class which represents list, container, choice or case.
     * @return Schema node, from which class was generated.
     */
    public DataSchemaNode getSchemaDefinition(final Class<?> cls) {
        Preconditions.checkArgument(!Augmentation.class.isAssignableFrom(cls),"Supplied class must not be augmentation (%s is)", cls);
        return (DataSchemaNode) typeToDefiningSchema.get(referencedType(cls));
    }

    public Entry<AugmentationIdentifier, AugmentationSchema> getResolvedAugmentationSchema(final DataNodeContainer target,
            final Class<? extends Augmentation<?>> aug) {
        final AugmentationSchema origSchema = getAugmentationDefinition(aug);
        Preconditions.checkArgument(origSchema != null, "Augmentation %s is not known in current schema context",aug);
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
            realChilds.add(target.getDataChildByName(child.getQName()));
            childNames.add(child.getQName());
        }

        final AugmentationIdentifier identifier = new AugmentationIdentifier(childNames);
        final AugmentationSchema proxy = new EffectiveAugmentationSchema(origSchema, realChilds);
        return new AbstractMap.SimpleEntry<>(identifier, proxy);
    }

    /**
     *
     * Returns resolved case schema for supplied class
     *
     * @param schema Resolved parent choice schema
     * @param childClass Class representing case.
     * @return Optionally a resolved case schema, absent if the choice is not legal in
     *         the given context.
     * @throws IllegalArgumentException If supplied class does not represent case.
     */
    public Optional<ChoiceCaseNode> getCaseSchemaDefinition(final ChoiceSchemaNode schema, final Class<?> childClass) throws IllegalArgumentException {
        final DataSchemaNode origSchema = getSchemaDefinition(childClass);
        Preconditions.checkArgument(origSchema instanceof ChoiceCaseNode, "Supplied schema %s is not case.", origSchema);

        /* FIXME: Make sure that if there are multiple augmentations of same
         * named case, with same structure we treat it as equals
         * this is due property of Binding specification and copy builders
         * that user may be unaware that he is using incorrect case
         * which was generated for choice inside grouping.
         */
        final Optional<ChoiceCaseNode> found = BindingSchemaContextUtils.findInstantiatedCase(schema,
                (ChoiceCaseNode) origSchema);
        return found;
    }

    private static Type referencedType(final Class<?> type) {
        return new ReferencedTypeImpl(type.getPackage().getName(), type.getSimpleName());
    }

    static Type referencedType(final String type) {
        final int packageClassSeparator = type.lastIndexOf(DOT);
        return new ReferencedTypeImpl(type.substring(0, packageClassSeparator), type.substring(packageClassSeparator + 1));
    }

    /**
     * Returns schema ({@link DataSchemaNode}, {@link AugmentationSchema} or {@link TypeDefinition})
     * from which supplied class was generated. Returned schema may be augmented with
     * additional information, which was not available at compile type
     * (e.g. third party augmentations).
     *
     * @param type Binding Class for which schema should be retrieved.
     * @return Instance of generated type (definition of Java API), along with
     *     {@link DataSchemaNode}, {@link AugmentationSchema} or {@link TypeDefinition}
     *     which was used to generate supplied class.
     */
    public Entry<GeneratedType, Object> getTypeWithSchema(final Class<?> type) {
        return getTypeWithSchema(referencedType(type));
    }

    public Entry<GeneratedType, Object> getTypeWithSchema(final String type) {
        return getTypeWithSchema(referencedType(type));
    }

    private Entry<GeneratedType, Object> getTypeWithSchema(final Type referencedType) {
        final Object schema = typeToDefiningSchema.get(referencedType);
        final Type definedType = typeToDefiningSchema.inverse().get(schema);
        Preconditions.checkNotNull(schema);
        Preconditions.checkNotNull(definedType);
        if(definedType instanceof GeneratedTypeBuilder) {
            return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).toInstance(), schema);
        }
        Preconditions.checkArgument(definedType instanceof GeneratedType,"Type {} is not GeneratedType", referencedType);
        return new SimpleEntry<>((GeneratedType) definedType,schema);
    }

    public ImmutableMap<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        final Map<Type,Entry<Type,Type>> childToCase = new HashMap<>();
        for (final ChoiceSchemaNode choice :  FluentIterable.from(schema.getChildNodes()).filter(ChoiceSchemaNode.class)) {
            final ChoiceSchemaNode originalChoice = getOriginalSchema(choice);
            final Type choiceType = referencedType(typeToDefiningSchema.inverse().get(originalChoice));
            final Collection<Type> cases = choiceToCases.get(choiceType);

            for (Type caze : cases) {
                final Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType,caze);
                final HashSet<Type> caseChildren = new HashSet<>();
                if (caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).toInstance();
                }
                collectAllContainerTypes((GeneratedType) caze, caseChildren);
                for (final Type caseChild : caseChildren) {
                    childToCase.put(caseChild, caseIdentifier);
                }
            }
        }
        return ImmutableMap.copyOf(childToCase);
    }

    /**
     * Map enum constants: yang - java
     *
     * @param enumClass enum generated class
     * @return mapped enum constants from yang with their corresponding values in generated binding classes
     */
    public BiMap<String, String> getEnumMapping(final Class<?> enumClass) {
        final Map.Entry<GeneratedType, Object> typeWithSchema = getTypeWithSchema(enumClass);
        return getEnumMapping(typeWithSchema);
    }

    /**
     * See {@link #getEnumMapping(Class)}}
     */
    public BiMap<String, String> getEnumMapping(final String enumClass) {
        final Map.Entry<GeneratedType, Object> typeWithSchema = getTypeWithSchema(enumClass);
        return getEnumMapping(typeWithSchema);
    }

    private static BiMap<String, String> getEnumMapping(final Entry<GeneratedType, Object> typeWithSchema) {
        final TypeDefinition<?> typeDef = (TypeDefinition<?>) typeWithSchema.getValue();

        final EnumerationType enumType;
        if(typeDef instanceof ExtendedType) {
            enumType = (EnumerationType) ((ExtendedType) typeDef).getBaseType();
        } else {
            Preconditions.checkArgument(typeDef instanceof EnumerationType);
            enumType = (EnumerationType) typeDef;
        }

        final HashBiMap<String, String> mappedEnums = HashBiMap.create();

        for (final EnumTypeDefinition.EnumPair enumPair : enumType.getValues()) {
            mappedEnums.put(enumPair.getName(), BindingMapping.getClassName(enumPair.getName()));
        }

        // TODO cache these maps for future use
        return mappedEnums;
    }

    public Set<Class<?>> getCases(final Class<?> choice) {
        final Collection<Type> cazes = choiceToCases.get(referencedType(choice));
        final Set<Class<?>> ret = new HashSet<>(cazes.size());
        for(final Type caze : cazes) {
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
        final Type clazzType = typeToDefiningSchema.inverse().get(origSchema);
        try {
            return strategy.loadClass(clazzType);
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableMap<AugmentationIdentifier,Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        final Map<AugmentationIdentifier,Type> identifierToType = new HashMap<>();
        if (container instanceof AugmentationTarget) {
            final Set<AugmentationSchema> augments = ((AugmentationTarget) container).getAvailableAugmentations();
            for (final AugmentationSchema augment : augments) {
                // Augmentation must have child nodes if is to be used with Binding classes
                AugmentationSchema augOrig = augment;
                while (augOrig.getOriginalDefinition().isPresent()) {
                    augOrig = augOrig.getOriginalDefinition().get();
                }

                if (!augment.getChildNodes().isEmpty()) {
                    final Type augType = typeToDefiningSchema.inverse().get(augOrig);
                    if (augType != null) {
                        identifierToType.put(getAugmentationIdentifier(augment),augType);
                    }
                }
            }
        }

        return ImmutableMap.copyOf(identifierToType);
    }

    private static AugmentationIdentifier getAugmentationIdentifier(final AugmentationSchema augment) {
        final Set<QName> childNames = new HashSet<>();
        for (final DataSchemaNode child : augment.getChildNodes()) {
            childNames.add(child.getQName());
        }
        return new AugmentationIdentifier(childNames);
    }

    private static Type referencedType(final Type type) {
        if(type instanceof ReferencedTypeImpl) {
            return type;
        }
        return new ReferencedTypeImpl(type.getPackageName(), type.getName());
    }

    private static Set<Type> collectAllContainerTypes(final GeneratedType type, final Set<Type> collection) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            Type childType = definition.getReturnType();
            if(childType instanceof ParameterizedType) {
                childType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            }
            if(childType instanceof GeneratedType || childType instanceof GeneratedTypeBuilder) {
                collection.add(referencedType(childType));
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

    public Class<?> getIdentityClass(final QName input) {
        return identityClasses.getUnchecked(input);
    }
}
