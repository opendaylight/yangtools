package org.opendaylight.yangtools.sal.binding.generator.util;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.base.AugmentationSchemaProxy;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
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
    private final ClassLoadingStrategy strategy;
    private final SchemaContext schemaContext;

    private final Map<Type, AugmentationSchema> augmentationToSchema = new HashMap<>();
    private final BiMap<Type, Object> typeToDefiningSchema = HashBiMap.create();
    private final Multimap<Type, Type> augmentableToAugmentations = HashMultimap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();
    private final Map<QName, Type> identities = new HashMap<>();

    private BindingRuntimeContext(final ClassLoadingStrategy strategy, final SchemaContext schema) {
        this.strategy = strategy;
        this.schemaContext = schema;

        BindingGeneratorImpl generator = new BindingGeneratorImpl();
        generator.generateTypes(schema);
        Map<Module, ModuleContext> modules = generator.getModuleContexts();

        for (ModuleContext ctx : modules.values()) {
            augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            typeToDefiningSchema.putAll(ctx.getTypeToSchema());
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
     * @return Schema of augmentation
     * @throws IllegalArgumentException If supplied class is not an augmentation or current context does not contain schema for augmentation.
     */
    public AugmentationSchema getAugmentationDefinition(final Class<?> augClass) throws IllegalArgumentException {
        Preconditions.checkArgument(Augmentation.class.isAssignableFrom(augClass), "Class {} does not represent augmentation", augClass);
        final AugmentationSchema ret = augmentationToSchema.get(referencedType(augClass));
        Preconditions.checkArgument(ret != null, "Supplied augmentation {} is not valid in current context", augClass);
        return ret;
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
        Preconditions.checkArgument(!Augmentation.class.isAssignableFrom(cls),"Supplied class must not be augmentation");
        return (DataSchemaNode) typeToDefiningSchema.get(referencedType(cls));
    }

    public Entry<AugmentationIdentifier, AugmentationSchema> getResolvedAugmentationSchema(final DataNodeContainer target,
            final Class<? extends Augmentation<?>> aug) {
        AugmentationSchema origSchema = getAugmentationDefinition(aug);
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
        Set<QName> childNames = new HashSet<>();
        Set<DataSchemaNode> realChilds = new HashSet<>();
        for (DataSchemaNode child : origSchema.getChildNodes()) {
            realChilds.add(target.getDataChildByName(child.getQName()));
            childNames.add(child.getQName());
        }

        AugmentationIdentifier identifier = new AugmentationIdentifier(childNames);
        AugmentationSchema proxy = new AugmentationSchemaProxy(origSchema, realChilds);
        return new AbstractMap.SimpleEntry<>(identifier, proxy);
    }

    /**
     *
     * Returns resolved case schema for supplied class
     *
     * @param schema Resolved parent choice schema
     * @param childClass Class representing case.
     * @return Resolved case schema.
     * @throws IllegalArgumentException If supplied class does not represent case or supplied case class is not
     * valid in the context of parent choice schema.
     */
    public ChoiceCaseNode getCaseSchemaDefinition(final ChoiceNode schema, final Class<?> childClass) throws IllegalArgumentException {
        DataSchemaNode origSchema = getSchemaDefinition(childClass);
        Preconditions.checkArgument(origSchema instanceof ChoiceCaseNode, "Supplied {} is not case.");
        /* FIXME: Make sure that if there are multiple augmentations of same
         * named case, with same structure we treat it as equals
         * this is due property of Binding specification and copy builders
         * that user may be unaware that he is using incorrect case
         * which was generated for choice inside grouping.
         */
        Optional<ChoiceCaseNode> found = BindingSchemaContextUtils.findInstantiatedCase(schema,
                (ChoiceCaseNode) origSchema);
        Preconditions.checkArgument(found.isPresent(), "Supplied {} is not valid case in schema", schema);
        return found.get();
    }

    private static Type referencedType(final Class<?> type) {
        return new ReferencedTypeImpl(type.getPackage().getName(), type.getSimpleName());
    }

    public Entry<GeneratedType, Object> getTypeWithSchema(final Class<?> type) {
        Object schema = typeToDefiningSchema.get(referencedType(type));
        Type definedType = typeToDefiningSchema.inverse().get(schema);
        Preconditions.checkNotNull(schema);
        Preconditions.checkNotNull(definedType);

        return new SimpleEntry<>(((GeneratedTypeBuilder) definedType).toInstance(), schema);
    }

    public ImmutableMap<Type, Entry<Type, Type>> getChoiceCaseChildren(final DataNodeContainer schema) {
        Map<Type,Entry<Type,Type>> childToCase = new HashMap<>();;
        for (ChoiceNode choice :  FluentIterable.from(schema.getChildNodes()).filter(ChoiceNode.class)) {
            ChoiceNode originalChoice = getOriginalSchema(choice);
            Type choiceType = referencedType(typeToDefiningSchema.inverse().get(originalChoice));
            Collection<Type> cases = choiceToCases.get(choiceType);

            for (Type caze : cases) {
                Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType,caze);
                HashSet<Type> caseChildren = new HashSet<>();
                if (caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).toInstance();
                }
                collectAllContainerTypes((GeneratedType) caze, caseChildren);
                for (Type caseChild : caseChildren) {
                    childToCase.put(caseChild, caseIdentifier);
                }
            }
        }
        return ImmutableMap.copyOf(childToCase);
    }

    public Set<Class<?>> getCases(final Class<?> choice) {
        Collection<Type> cazes = choiceToCases.get(referencedType(choice));
        Set<Class<?>> ret = new HashSet<>(cazes.size());
        for(Type caze : cazes) {
            try {
                final Class<?> c = strategy.loadClass(caze);
                ret.add(c);
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to load class for case {}, ignoring it", caze, e);
            }
        }
        return ret;
    }

    public Class<?> getClassForSchema(final DataSchemaNode childSchema) {
        DataSchemaNode origSchema = getOriginalSchema(childSchema);
        Type clazzType = typeToDefiningSchema.inverse().get(origSchema);
        try {
            return strategy.loadClass(clazzType);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    public ImmutableMap<AugmentationIdentifier,Type> getAvailableAugmentationTypes(final DataNodeContainer container) {
        final Map<AugmentationIdentifier,Type> identifierToType = new HashMap<>();
        if (container instanceof AugmentationTarget) {
            Set<AugmentationSchema> augments = ((AugmentationTarget) container).getAvailableAugmentations();
            for (AugmentationSchema augment : augments) {
                // Augmentation must have child nodes if is to be used with Binding classes
                AugmentationSchema augOrig = augment;
                while (augOrig.getOriginalDefinition().isPresent()) {
                    augOrig = augOrig.getOriginalDefinition().get();
                }

                if (!augment.getChildNodes().isEmpty()) {
                    Type augType = typeToDefiningSchema.inverse().get(augOrig);
                    if (augType != null) {
                        identifierToType.put(getAugmentationIdentifier(augment),augType);
                    }
                }
            }
        }

        return ImmutableMap.copyOf(identifierToType);
    }

    private AugmentationIdentifier getAugmentationIdentifier(final AugmentationSchema augment) {
        Set<QName> childNames = new HashSet<>();
        for (DataSchemaNode child : augment.getChildNodes()) {
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
        for (MethodSignature definition : type.getMethodDefinitions()) {
            Type childType = definition.getReturnType();
            if(childType instanceof ParameterizedType) {
                childType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            }
            if(childType instanceof GeneratedType || childType instanceof GeneratedTypeBuilder) {
                collection.add(referencedType(childType));
            }
        }
        for (Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllContainerTypes((GeneratedType) parent, collection);
            }
        }
        return collection;
    }

    private static final <T extends SchemaNode> T getOriginalSchema(final T choice) {
        @SuppressWarnings("unchecked")
        T original = (T) SchemaNodeUtils.getRootOriginalIfPossible(choice);
        if (original != null) {
            return original;
        }
        return choice;
    }

    public Class<?> getIdentityClass(final QName input) {
        Type identityType = identities.get(input);
        Preconditions.checkArgument(identityType != null, "Supplied QName is not valid identity");
        try {
            return strategy.loadClass(identityType);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Required class " + identityType + "was not found.",e);
        }
    }

}
