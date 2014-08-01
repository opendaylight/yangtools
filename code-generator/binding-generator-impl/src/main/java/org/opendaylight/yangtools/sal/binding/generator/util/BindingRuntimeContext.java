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
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaNodeUtils;

public class BindingRuntimeContext implements Immutable {

    private final ClassLoadingStrategy strategy;
    private final SchemaContext schemaContext;

    private final Map<Type, AugmentationSchema> augmentationToSchema = new HashMap<>();
    private final BiMap<Type, Object> typeToDefiningSchema = HashBiMap.create();
    private final Multimap<Type, Type> augmentableToAugmentations = HashMultimap.create();
    private final Multimap<Type, Type> choiceToCases = HashMultimap.create();

    private BindingRuntimeContext(final ClassLoadingStrategy strategy, final SchemaContext schema) {
        this.strategy = strategy;
        this.schemaContext = schema;

        BindingGeneratorImpl generator = new BindingGeneratorImpl();
        generator.generateTypes(schema);
        Map<Module, ModuleContext> modules = generator.getModuleContexts();

        for (Entry<Module, ModuleContext> entry : modules.entrySet()) {
            ModuleContext ctx = entry.getValue();
            augmentationToSchema.putAll(ctx.getTypeToAugmentation());
            typeToDefiningSchema.putAll(ctx.getTypeToSchema());
            augmentableToAugmentations.putAll(ctx.getAugmentableToAugmentations());
            choiceToCases.putAll(ctx.getChoiceToCases());
        }
    }

    public static final BindingRuntimeContext create(final ClassLoadingStrategy strategy, final SchemaContext ctx) {

        return new BindingRuntimeContext(strategy, ctx);
    }

    public ClassLoadingStrategy getStrategy() {
        return strategy;
    }

    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public AugmentationSchema getAugmentationDefinition(final Class<?> childClass) {
        ReferencedTypeImpl ref = new ReferencedTypeImpl(childClass.getPackage().getName(), childClass.getSimpleName());
        return augmentationToSchema.get(ref);
    }

    public DataSchemaNode getSchemaDefinition(final Class<?> childClass) {
        ReferencedTypeImpl ref = new ReferencedTypeImpl(childClass.getPackage().getName(), childClass.getSimpleName());
        return (DataSchemaNode) typeToDefiningSchema.get(ref);
    }

    public Entry<AugmentationIdentifier, AugmentationSchema> getAugmentationSchema(final DataNodeContainer target,
            final Class<? extends Augmentation<?>> aug) {
        ReferencedTypeImpl reference = new ReferencedTypeImpl(aug.getPackage().getName(), aug.getSimpleName());
        AugmentationSchema origSchema = augmentationToSchema.get(reference);
        Preconditions.checkArgument(origSchema != null,
                "Supplied augmentation %s in not valid in current schema context", aug);
        // FIXME: Add real lookup to find schema

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

    public ChoiceCaseNode getCaseSchemaDefinition(final ChoiceNode schema, final Class<?> childClass) {
        DataSchemaNode origSchema = getSchemaDefinition(childClass);
        Preconditions.checkArgument(origSchema instanceof ChoiceCaseNode, "Supplied {} is not case.");
        // FIXME: Make sure that if there are multiple augmentations of samely
        // named case, with same structure we allow for its normalization
        // this is due proporty of Binding specification and copy builders
        // that user may be unaware that he is using incorect case
        // which was generated for choice inside grouping.
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
        for(ChoiceNode choice :  FluentIterable.from(schema.getChildNodes()).filter(ChoiceNode.class)) {
            ChoiceNode originalChoice = getOriginalSchema(choice);
            Type choiceType = referencedType(typeToDefiningSchema.inverse().get(originalChoice));
            Collection<Type> cases = choiceToCases.get(choiceType);

            for(Type caze : cases) {
                Entry<Type,Type> caseIdentifier = new SimpleEntry<>(choiceType,caze);
                HashSet<Type> caseChildren = new HashSet<>();
                if(caze instanceof GeneratedTypeBuilder) {
                    caze = ((GeneratedTypeBuilder) caze).toInstance();
                }
                collectAllContainerTypes((GeneratedType) caze, caseChildren);
                for(Type caseChild : caseChildren) {
                    childToCase.put(caseChild, caseIdentifier);
                }
            }
        }
        return ImmutableMap.copyOf(childToCase);

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
        T original = (T) SchemaNodeUtils.getRootOriginalIfPossible(choice);
        if(original != null) {
            return original;
        }
        return choice;
    }

}
