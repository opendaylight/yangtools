/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.concepts.Delegator;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.CodeGenerationException;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTOBuilder;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentable;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;
import org.opendaylight.yangtools.yang.binding.BindingCodec;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.Identifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.codec.AugmentationCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ChoiceCaseCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ChoiceCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.codec.DataContainerCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DomCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.IdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.IdentityCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

class LazyGeneratedCodecRegistry implements //
        CodecRegistry, //
        SchemaContextListener, //
        GeneratorListener {

    private static final Logger LOG = LoggerFactory.getLogger(LazyGeneratedCodecRegistry.class);
    private static final LateMixinCodec NOT_READY_CODEC = new LateMixinCodec();

    // Concrete class to codecs
    private static final Map<Class<?>, DataContainerCodec<?>> containerCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, DataContainerCodec<?>>());
    private static final Map<Class<?>, IdentifierCodec<?>> identifierCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, IdentifierCodec<?>>());
    private static final Map<Class<?>, PublicChoiceCodecImpl<?>> choiceCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, PublicChoiceCodecImpl<?>>());
    private static final Map<Class<?>, ChoiceCaseCodecImpl<?>> caseCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, ChoiceCaseCodecImpl<?>>());
    private static final Map<Class<?>, AugmentableDispatchCodec> augmentableCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, AugmentableDispatchCodec>());
    private static final Map<Class<?>, AugmentationCodecWrapper<?>> augmentationCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, AugmentationCodecWrapper<?>>());
    private static final Map<Class<?>, QName> identityQNames = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, QName>());
    private static final Map<QName, Type> qnamesToIdentityMap = new ConcurrentHashMap<>();
    /** Binding type to encountered classes mapping **/
    @SuppressWarnings("rawtypes")
    private static final Map<Type, WeakReference<Class>> typeToClass = new ConcurrentHashMap<>();

    @SuppressWarnings("rawtypes")
    private static final ConcurrentMap<Type, ChoiceCaseCodecImpl> typeToCaseCodecs = new ConcurrentHashMap<>();

    private static final Map<SchemaPath, GeneratedTypeBuilder> pathToType = new ConcurrentHashMap<>();
    private static final Map<List<QName>, Type> pathToInstantiatedType = new ConcurrentHashMap<>();
    private static final Map<Type, QName> typeToQname = new ConcurrentHashMap<>();
    private static final BiMap<Type, AugmentationSchema> typeToAugment = HashBiMap
            .create(new ConcurrentHashMap<Type, AugmentationSchema>());

    private static final Multimap<Type, Type> augmentableToAugmentations = Multimaps.synchronizedMultimap(HashMultimap
            .<Type, Type> create());
    private static final Multimap<Type, Type> choiceToCases = Multimaps.synchronizedMultimap(HashMultimap
            .<Type, Type> create());

    private final InstanceIdentifierCodec instanceIdentifierCodec = new InstanceIdentifierCodecImpl(this);
    private final CaseClassMapFacade classToCaseRawCodec = new CaseClassMapFacade();
    private final IdentityCompositeCodec identityRefCodec = new IdentityCompositeCodec();
    private final ClassLoadingStrategy classLoadingStrategy;
    private final AbstractTransformerGenerator generator;
    private final SchemaLock lock;

    // FIXME: how is this protected?
    private SchemaContext currentSchema;

    LazyGeneratedCodecRegistry(final SchemaLock lock, final AbstractTransformerGenerator generator,
            final ClassLoadingStrategy classLoadingStrategy) {
        this.lock = Preconditions.checkNotNull(lock);
        this.classLoadingStrategy = Preconditions.checkNotNull(classLoadingStrategy);
        this.generator = Preconditions.checkNotNull(generator);
    }

    public SchemaLock getLock() {
        return lock;
    }

    @Override
    public InstanceIdentifierCodec getInstanceIdentifierCodec() {
        return instanceIdentifierCodec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Augmentation<?>> AugmentationCodecWrapper<T> getCodecForAugmentation(final Class<T> object) {
        AugmentationCodecWrapper<T> codec = null;
        @SuppressWarnings("rawtypes")
        AugmentationCodecWrapper potentialCodec = augmentationCodecs.get(object);
        if (potentialCodec != null) {
            codec = potentialCodec;
        } else {
            lock.waitForSchema(object);
            Class<? extends BindingCodec<Map<QName, Object>, Object>> augmentRawCodec = generator
                    .augmentationTransformerFor(object);

            BindingCodec<Map<QName, Object>, Object> rawCodec = newInstanceOf(augmentRawCodec);
            codec = new AugmentationCodecWrapper<T>(rawCodec, null, object);
            augmentationCodecs.put(object, codec);
        }
        Class<? extends Augmentable<?>> objectSupertype = getAugmentableArgumentFrom(object);
        if (objectSupertype != null) {
            getAugmentableCodec(objectSupertype).addImplementation(codec);
        } else {
            LOG.warn("Could not find augmentation target for augmentation {}", object);
        }
        return codec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QName getQNameForAugmentation(final Class<?> cls) {
        Preconditions.checkArgument(Augmentation.class.isAssignableFrom(cls));
        return getCodecForAugmentation((Class<? extends Augmentation<?>>) cls).getAugmentationQName();
    }

    private static Class<? extends Augmentable<?>> getAugmentableArgumentFrom(
            final Class<? extends Augmentation<?>> augmentation) {
        try {
            Class<? extends Augmentable<?>> ret = BindingReflections.findAugmentationTarget(augmentation);
            return ret;

        } catch (Exception e) {
            LOG.debug("Could not find augmentable for {} using {}", augmentation, augmentation.getClassLoader(), e);
            return null;
        }
    }

    @Override
    public Class<?> getClassForPath(final List<QName> names) {
        final DataSchemaNode node = getSchemaNode(names);
        final SchemaPath path = node.getPath();
        final Type t = pathToType.get(path);

        final Type type;
        if (t != null) {
            type = new ReferencedTypeImpl(t.getPackageName(), t.getName());
        } else {
            type = pathToInstantiatedType.get(names);
            Preconditions.checkState(type != null, "Failed to lookup instantiated type for path %s", path);
        }

        @SuppressWarnings("rawtypes")
        final WeakReference<Class> weakRef = typeToClass.get(type);
        Preconditions.checkState(weakRef != null, "Could not find loaded class for path: %s and type: %s", path, type.getFullyQualifiedName());
        return weakRef.get();
    }

    @Override
    public void putPathToClass(final List<QName> names, final Class<?> cls) {
        final Type reference = Types.typeForClass(cls);
        pathToInstantiatedType.put(names, reference);
        LOG.trace("Path {} attached to class {} reference {}", names, cls, reference);
        bindingClassEncountered(cls);
    }

    @Override
    public IdentifierCodec<?> getKeyCodecForPath(final List<QName> names) {
        @SuppressWarnings("unchecked")
        Class<? extends Identifiable<?>> cls = (Class<? extends Identifiable<?>>) getClassForPath(names);
        return getIdentifierCodecForIdentifiable(cls);
    }

    @Override
    public <T extends DataContainer> DataContainerCodec<T> getCodecForDataObject(final Class<T> type) {
        @SuppressWarnings("unchecked")
        DataContainerCodec<T> ret = (DataContainerCodec<T>) containerCodecs.get(type);
        if (ret != null) {
            return ret;
        }
        Class<? extends BindingCodec<Map<QName, Object>, Object>> newType = generator.transformerFor(type);
        BindingCodec<Map<QName, Object>, Object> rawCodec = newInstanceOf(newType);
        DataContainerCodecImpl<T> newWrapper = new DataContainerCodecImpl<>(rawCodec);
        containerCodecs.put(type, newWrapper);
        return newWrapper;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void bindingClassEncountered(final Class cls) {

        ConcreteType typeRef = Types.typeForClass(cls);
        if (typeToClass.containsKey(typeRef)) {
            return;
        }
        LOG.trace("Binding Class {} encountered.", cls);
        WeakReference<Class> weakRef = new WeakReference<>(cls);
        typeToClass.put(typeRef, weakRef);
        if (Augmentation.class.isAssignableFrom(cls)) {

        } else if (DataObject.class.isAssignableFrom(cls)) {
            @SuppressWarnings({ "unchecked", "unused" })
            Object cdc = getCodecForDataObject((Class<? extends DataObject>) cls);
        }
    }

    @Override
    public void onClassProcessed(final Class<?> cls) {
        ConcreteType typeRef = Types.typeForClass(cls);
        if (typeToClass.containsKey(typeRef)) {
            return;
        }
        LOG.trace("Binding Class {} encountered.", cls);
        @SuppressWarnings("rawtypes")
        WeakReference<Class> weakRef = new WeakReference<Class>(cls);
        typeToClass.put(typeRef, weakRef);
    }

    private DataSchemaNode getSchemaNode(final List<QName> path) {
        QName firstNode = path.get(0);
        DataNodeContainer previous = currentSchema.findModuleByNamespaceAndRevision(firstNode.getNamespace(), firstNode.getRevision());
        Preconditions.checkArgument(previous != null, "Failed to find module %s for path %s", firstNode, path);

        Iterator<QName> iterator = path.iterator();
        while (iterator.hasNext()) {
            QName arg = iterator.next();
            DataSchemaNode currentNode = previous.getDataChildByName(arg);
            if (currentNode == null && previous instanceof DataNodeContainer) {
                currentNode = searchInChoices(previous, arg);
            }
            if (currentNode instanceof DataNodeContainer) {
                previous = (DataNodeContainer) currentNode;
            } else if (currentNode instanceof LeafSchemaNode || currentNode instanceof LeafListSchemaNode) {
                Preconditions.checkState(!iterator.hasNext(), "Path tries to nest inside leaf node.");
                return currentNode;
            }
        }
        return (DataSchemaNode) previous;
    }

    private DataSchemaNode searchInChoices(final DataNodeContainer node, final QName arg) {
        Set<DataSchemaNode> children = node.getChildNodes();
        for (DataSchemaNode child : children) {
            if (child instanceof ChoiceNode) {
                ChoiceNode choiceNode = (ChoiceNode) child;
                DataSchemaNode potential = searchInCases(choiceNode, arg);
                if (potential != null) {
                    return potential;
                }
            }
        }
        return null;
    }

    private DataSchemaNode searchInCases(final ChoiceNode choiceNode, final QName arg) {
        Set<ChoiceCaseNode> cases = choiceNode.getCases();
        for (ChoiceCaseNode caseNode : cases) {
            DataSchemaNode node = caseNode.getDataChildByName(arg);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    private static <T> T newInstanceOf(final Class<?> cls) {
        try {
            @SuppressWarnings("unchecked")
            T ret = (T) cls.newInstance();
            return ret;
        } catch (InstantiationException e) {
            LOG.error("Failed to instantiate codec {}", cls.getSimpleName(), e);
            throw new IllegalStateException(String.format("Failed to instantiate codec %s", cls), e);
        } catch (IllegalAccessException e) {
            LOG.debug("Run-time consistency issue: constructor for {} is not available. This indicates either a code generation bug or a misconfiguration of JVM.",
                    cls.getSimpleName(), e);
            throw new IllegalStateException(String.format("Cannot access contructor of %s", cls), e);
        }
    }

    @Override
    public <T extends Identifiable<?>> IdentifierCodec<?> getIdentifierCodecForIdentifiable(final Class<T> type) {
        IdentifierCodec<?> obj = identifierCodecs.get(type);
        if (obj != null) {
            return obj;
        }
        Class<? extends BindingCodec<Map<QName, Object>, Object>> newCodec = generator
                .keyTransformerForIdentifiable(type);
        BindingCodec<Map<QName, Object>, Object> newInstance;
        newInstance = newInstanceOf(newCodec);
        IdentifierCodecImpl<?> newWrapper = new IdentifierCodecImpl<>(newInstance);
        identifierCodecs.put(type, newWrapper);
        return newWrapper;
    }

    @Override
    public IdentityCodec<?> getIdentityCodec() {
        return identityRefCodec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BaseIdentity> IdentityCodec<T> getCodecForIdentity(final Class<T> codec) {
        bindingClassEncountered(codec);
        return identityRefCodec;
    }

    @Override
    public void onCodecCreated(final Class<?> cls) {
        CodecMapping.setIdentifierCodec(cls, instanceIdentifierCodec);
        CodecMapping.setIdentityRefCodec(cls, identityRefCodec);
    }

    @Override
    public <T extends Identifier<?>> IdentifierCodec<T> getCodecForIdentifier(final Class<T> object) {
        @SuppressWarnings("unchecked")
        IdentifierCodec<T> obj = (IdentifierCodec<T>) identifierCodecs.get(object);
        if (obj != null) {
            return obj;
        }
        Class<? extends BindingCodec<Map<QName, Object>, Object>> newCodec = generator
                .keyTransformerForIdentifier(object);
        BindingCodec<Map<QName, Object>, Object> newInstance;
        newInstance = newInstanceOf(newCodec);
        IdentifierCodecImpl<T> newWrapper = new IdentifierCodecImpl<>(newInstance);
        identifierCodecs.put(object, newWrapper);
        return newWrapper;
    }

    @SuppressWarnings("rawtypes")
    public ChoiceCaseCodecImpl getCaseCodecFor(final Class caseClass) {
        ChoiceCaseCodecImpl<?> potential = caseCodecs.get(caseClass);
        if (potential != null) {
            return potential;
        }
        ConcreteType typeref = Types.typeForClass(caseClass);
        ChoiceCaseCodecImpl caseCodec = typeToCaseCodecs.get(typeref);

        Preconditions.checkState(caseCodec != null, "Case Codec was not created proactivelly for %s",
                caseClass.getName());
        Preconditions.checkState(caseCodec.getSchema() != null, "Case schema is not available for %s",
                caseClass.getName());
        Class<? extends BindingCodec> newCodec = generator.caseCodecFor(caseClass, caseCodec.getSchema());
        BindingCodec newInstance = newInstanceOf(newCodec);
        caseCodec.setDelegate(newInstance);
        caseCodecs.put(caseClass, caseCodec);

        for (Entry<Class<?>, PublicChoiceCodecImpl<?>> choice : choiceCodecs.entrySet()) {
            if (choice.getKey().isAssignableFrom(caseClass)) {
                choice.getValue().cases.put(caseClass, caseCodec);
            }
        }
        return caseCodec;
    }

    public void onModuleContextAdded(final SchemaContext schemaContext, final Module module, final ModuleContext context) {
        pathToType.putAll(context.getChildNodes());

        BiMap<Type, AugmentationSchema> bimap = context.getTypeToAugmentation();
        for (Map.Entry<Type, AugmentationSchema> entry : bimap.entrySet()) {
            Type key = entry.getKey();
            AugmentationSchema value = entry.getValue();
            Set<DataSchemaNode> augmentedNodes = value.getChildNodes();
            if (augmentedNodes != null && !(augmentedNodes.isEmpty())) {
                typeToAugment.put(key, value);
            }
        }

        qnamesToIdentityMap.putAll(context.getIdentities());
        for (Entry<QName, GeneratedTOBuilder> identity : context.getIdentities().entrySet()) {
            typeToQname.put(
                    new ReferencedTypeImpl(identity.getValue().getPackageName(), identity.getValue().getName()),
                    identity.getKey());
        }

        synchronized(augmentableToAugmentations) {
            augmentableToAugmentations.putAll(context.getAugmentableToAugmentations());
        }
        synchronized(choiceToCases)  {
            choiceToCases.putAll(context.getChoiceToCases());
        }
        captureCases(context.getCases(), schemaContext);
    }

    private void captureCases(final Map<SchemaPath, GeneratedTypeBuilder> cases, final SchemaContext module) {
        for (Entry<SchemaPath, GeneratedTypeBuilder> caseNode : cases.entrySet()) {
            ReferencedTypeImpl typeref = new ReferencedTypeImpl(caseNode.getValue().getPackageName(), caseNode
                    .getValue().getName());

            pathToType.put(caseNode.getKey(), caseNode.getValue());

            ChoiceCaseNode node = (ChoiceCaseNode) SchemaContextUtil.findDataSchemaNode(module, caseNode.getKey());

            if (node == null) {
                LOG.warn("Failed to find YANG SchemaNode for {}, with path {} was not found in context.",
                        typeref.getFullyQualifiedName(), caseNode.getKey());
                @SuppressWarnings("rawtypes")
                ChoiceCaseCodecImpl value = new ChoiceCaseCodecImpl();
                typeToCaseCodecs.putIfAbsent(typeref, value);
                continue;
            }
            @SuppressWarnings("rawtypes")
            ChoiceCaseCodecImpl value = new ChoiceCaseCodecImpl(node);
            typeToCaseCodecs.putIfAbsent(typeref, value);
        }
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        currentSchema = context;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onChoiceCodecCreated(final Class<?> choiceClass,
            final Class<? extends BindingCodec<Map<QName, Object>, Object>> choiceCodec, final ChoiceNode schema) {
        ChoiceCodec<?> oldCodec = choiceCodecs.get(choiceClass);
        Preconditions.checkState(oldCodec == null);
        BindingCodec<Map<QName, Object>, Object> delegate = newInstanceOf(choiceCodec);
        PublicChoiceCodecImpl<?> newCodec = new PublicChoiceCodecImpl(delegate);
        choiceCodecs.put(choiceClass, newCodec);
        CodecMapping.setClassToCaseMap(choiceCodec, classToCaseRawCodec);
        CodecMapping.setCompositeNodeToCaseMap(choiceCodec, newCodec.getCompositeToCase());

        tryToCreateCasesCodecs(schema);

    }

    @Deprecated
    private void tryToCreateCasesCodecs(final ChoiceNode schema) {
        for (ChoiceCaseNode choiceCase : schema.getCases()) {
            ChoiceCaseNode caseNode = choiceCase;
            if (caseNode.isAddedByUses()) {
                DataSchemaNode origCaseNode = SchemaContextUtil.findOriginal(caseNode, currentSchema);
                if (origCaseNode instanceof ChoiceCaseNode) {
                    caseNode = (ChoiceCaseNode) origCaseNode;
                }
            }
            SchemaPath path = caseNode.getPath();

            GeneratedTypeBuilder type;
            if (path != null && (type = pathToType.get(path)) != null) {
                ReferencedTypeImpl typeref = new ReferencedTypeImpl(type.getPackageName(), type.getName());
                @SuppressWarnings("rawtypes")
                ChoiceCaseCodecImpl partialCodec = typeToCaseCodecs.get(typeref);
                if (partialCodec.getSchema() == null) {
                    partialCodec.setSchema(caseNode);
                }
                try {
                    Class<?> caseClass = classLoadingStrategy.loadClass(type.getFullyQualifiedName());
                    getCaseCodecFor(caseClass);
                } catch (ClassNotFoundException e) {
                    LOG.trace("Could not proactivelly create case codec for {}", type, e);
                }
            }
        }

    }

    @Override
    public void onValueCodecCreated(final Class<?> valueClass, final Class<?> valueCodec) {
    }

    @Override
    public void onCaseCodecCreated(final Class<?> choiceClass,
            final Class<? extends BindingCodec<Map<QName, Object>, Object>> choiceCodec) {
    }

    @Override
    public void onDataContainerCodecCreated(final Class<?> dataClass,
            final Class<? extends BindingCodec<?, ?>> dataCodec) {
        if (Augmentable.class.isAssignableFrom(dataClass)) {
            AugmentableDispatchCodec augmentableCodec = getAugmentableCodec(dataClass);
            CodecMapping.setAugmentationCodec(dataCodec, augmentableCodec);
        }
    }

    public AugmentableDispatchCodec getAugmentableCodec(final Class<?> dataClass) {
        AugmentableDispatchCodec ret = augmentableCodecs.get(dataClass);
        if (ret != null) {
            return ret;
        }
        ret = new AugmentableDispatchCodec(dataClass);
        augmentableCodecs.put(dataClass, ret);
        ret.tryToLoadImplementations();
        return ret;
    }

    private static abstract class IntermediateCodec<T> implements //
            DomCodec<T>, Delegator<BindingCodec<Map<QName, Object>, Object>> {

        private final BindingCodec<Map<QName, Object>, Object> delegate;

        @Override
        public BindingCodec<Map<QName, Object>, Object> getDelegate() {
            return delegate;
        }

        public IntermediateCodec(final BindingCodec<Map<QName, Object>, Object> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Node<?> serialize(final ValueWithQName<T> input) {
            Map<QName, Object> intermediateOutput = delegate.serialize(input);
            return IntermediateMapping.toNode(intermediateOutput);
        }

    }

    private static class IdentifierCodecImpl<T extends Identifier<?>> //
            extends IntermediateCodec<T> //
            implements IdentifierCodec<T> {

        public IdentifierCodecImpl(final BindingCodec<Map<QName, Object>, Object> delegate) {
            super(delegate);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input) {
            QName qname = input.getNodeType();
            @SuppressWarnings("unchecked")
            T value = (T) getDelegate().deserialize((Map<QName, Object>) input);
            return new ValueWithQName<T>(qname, value);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            QName qname = input.getNodeType();
            @SuppressWarnings("unchecked")
            T value = (T) getDelegate().deserialize((Map<QName, Object>) input, bindingIdentifier);
            return new ValueWithQName<T>(qname, value);
        }

        @Override
        public CompositeNode serialize(final ValueWithQName<T> input) {
            return (CompositeNode) super.serialize(input);
        }
    }

    private static class DataContainerCodecImpl<T extends DataContainer> //
            extends IntermediateCodec<T> //
            implements DataContainerCodec<T> {

        public DataContainerCodecImpl(final BindingCodec<Map<QName, Object>, Object> delegate) {
            super(delegate);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input) {
            if (input == null) {
                return null;
            }
            QName qname = input.getNodeType();
            @SuppressWarnings("unchecked")
            T value = (T) getDelegate().deserialize((Map<QName, Object>) input);
            return new ValueWithQName<T>(qname, value);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            if (input == null) {
                return null;
            }
            QName qname = input.getNodeType();
            @SuppressWarnings("unchecked")
            T value = (T) getDelegate().deserialize((Map<QName, Object>) input, bindingIdentifier);
            return new ValueWithQName<T>(qname, value);
        }

        @Override
        public CompositeNode serialize(final ValueWithQName<T> input) {
            return (CompositeNode) super.serialize(input);
        }
    }

    private interface LocationAwareBindingCodec<P, I> extends BindingCodec<P, I> {

        boolean isApplicable(InstanceIdentifier<?> location);

        public Class<?> getDataType();

    }

    @SuppressWarnings("rawtypes")
    private abstract class LocationAwareDispatchCodec<T extends LocationAwareBindingCodec> implements BindingCodec {

        private final Map<Class, T> implementations = Collections.synchronizedMap(new WeakHashMap<Class, T>());
        private final Set<InstanceIdentifier<?>> adaptedForPaths = new HashSet<>();

        protected Map<Class, T> getImplementations() {
            return implementations;
        }

        protected void addImplementation(final T implementation) {
            implementations.put(implementation.getDataType(), implementation);
        }

        @Override
        public final Object deserialize(final Object input) {
            throw new UnsupportedOperationException("Invocation of deserialize without Tree location is unsupported");
        }

        @Override
        public Object serialize(final Object input) {
            Preconditions.checkArgument(input instanceof DataContainer);
            Class<? extends DataContainer> inputType = ((DataContainer) input).getImplementedInterface();
            T implementation = implementations.get(inputType);
            if (implementation == null) {
                implementation = tryToLoadImplementationImpl(inputType);
            }

            return null;
        }

        private T tryToLoadImplementationImpl(final Class<? extends DataContainer> inputType) {
            T implementation = tryToLoadImplementation(inputType);
            Preconditions.checkArgument(implementation != null, "Data type %s is not supported.", inputType);
            addImplementation(implementation);
            return implementation;
        }

        protected final void adaptForPath(final InstanceIdentifier<?> path) {
            if (adaptedForPaths.contains(path)) {
                return;
            }
            /**
             * We search in schema context if the use of this location aware codec (augmentable codec, case codec)
             * makes sense on provided location (path)
             *
             */
            Optional<DataNodeContainer> contextNode = BindingSchemaContextUtils.findDataNodeContainer(currentSchema, path);
            /**
             * If context node is present, this codec makes sense on provided location.
             *
             */
            if (contextNode.isPresent()) {
                synchronized (this) {
                    /**
                     *
                     * We adapt (turn on / off) possible implementations of child codecs (augmentations, cases)
                     * based on this location.
                     *
                     *
                     */
                    adaptForPathImpl(path, contextNode.get());
                    try  {
                        /**
                         * We trigger serialization of instance identifier, to make sure instance identifier
                         * codec is aware of combination of this path / augmentation / case
                         */
                        instanceIdentifierCodec.serialize(path);
                    } catch (Exception e) {
                        LOG.warn("Exception during preparation of instance identifier codec for  path {}.",path,e);
                    }
                    adaptedForPaths.add(path);
                }
            } else {
                LOG.debug("Context node (parent node) not found for {}",path);
            }
        }

        protected abstract T tryToLoadImplementation(Class<? extends DataContainer> inputType);

        protected abstract void tryToLoadImplementations();

        protected abstract void adaptForPathImpl(InstanceIdentifier<?> path, DataNodeContainer ctx);
    }

    @SuppressWarnings("rawtypes")
    private static class ChoiceCaseCodecImpl<T extends DataContainer> implements ChoiceCaseCodec<T>, //
            Delegator<BindingCodec>, LocationAwareBindingCodec<Node<?>, ValueWithQName<T>> {
        private boolean augmenting;
        private boolean uses;
        private BindingCodec delegate;

        private Set<String> validNames;
        private Set<QName> validQNames;
        private ChoiceCaseNode schema;
        private Set<InstanceIdentifier<?>> applicableLocations;

        @Override
        public boolean isApplicable(final InstanceIdentifier location) {
            return applicableLocations.contains(location);
        }

        public void setSchema(final ChoiceCaseNode caseNode) {
            this.schema = caseNode;
            validNames = new HashSet<>();
            validQNames = new HashSet<>();
            for (DataSchemaNode node : caseNode.getChildNodes()) {
                QName qname = node.getQName();
                validQNames.add(qname);
                validNames.add(qname.getLocalName());
            }
            augmenting = caseNode.isAugmenting();
            uses = caseNode.isAddedByUses();
            applicableLocations = new HashSet<>();
        }

        public ChoiceCaseCodecImpl() {
            this.delegate = NOT_READY_CODEC;
        }

        public ChoiceCaseCodecImpl(final ChoiceCaseNode caseNode) {
            this.delegate = NOT_READY_CODEC;
            setSchema(caseNode);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public CompositeNode serialize(final ValueWithQName<T> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public BindingCodec getDelegate() {
            return delegate;
        }

        public void setDelegate(final BindingCodec delegate) {
            this.delegate = delegate;
        }

        public ChoiceCaseNode getSchema() {
            return schema;
        }

        @Override
        public boolean isAcceptable(final Node<?> input) {
            if (input instanceof CompositeNode) {
                if (augmenting && !uses) {
                    return checkAugmenting((CompositeNode) input);
                } else {
                    return checkLocal((CompositeNode) input);
                }
            }
            return false;
        }

        @SuppressWarnings("deprecation")
        private boolean checkLocal(final CompositeNode input) {
            QName parent = input.getNodeType();
            for (Node<?> childNode : input.getChildren()) {
                QName child = childNode.getNodeType();
                if (!Objects.equals(parent.getNamespace(), child.getNamespace())
                        || !Objects.equals(parent.getRevision(), child.getRevision())) {
                    continue;
                }
                if (validNames.contains(child.getLocalName())) {
                    return true;
                }
            }
            return false;
        }

        @SuppressWarnings("deprecation")
        private boolean checkAugmenting(final CompositeNode input) {
            for (Node<?> child : input.getChildren()) {
                if (validQNames.contains(child.getNodeType())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Class<?> getDataType() {
            // TODO Auto-generated method stub
            throw new UnsupportedOperationException("Not implemented Yet.");
        }
    }

    private static class PublicChoiceCodecImpl<T> implements ChoiceCodec<T>, Delegator<BindingCodec<Map<QName, Object>, Object>> {

        private final BindingCodec<Map<QName, Object>, Object> delegate;

        @SuppressWarnings("rawtypes")
        private final Map<Class, ChoiceCaseCodecImpl<?>> cases = Collections
                .synchronizedMap(new WeakHashMap<Class, ChoiceCaseCodecImpl<?>>());

        private final CaseCompositeNodeMapFacade CompositeToCase;

        public PublicChoiceCodecImpl(final BindingCodec<Map<QName, Object>, Object> delegate) {
            this.delegate = delegate;
            this.CompositeToCase = new CaseCompositeNodeMapFacade(cases);
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public Node<?> serialize(final ValueWithQName<T> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        public CaseCompositeNodeMapFacade getCompositeToCase() {
            return CompositeToCase;
        }

        @Override
        public BindingCodec<Map<QName, Object>, Object> getDelegate() {
            return delegate;
        }

    }

    @SuppressWarnings("unused")
    private class DispatchChoiceCodecImpl extends LocationAwareDispatchCodec<ChoiceCaseCodecImpl<?>> {

        @Override
        public Object deserialize(final Object input, @SuppressWarnings("rawtypes") final InstanceIdentifier bindingIdentifier) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object serialize(final Object input) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        protected ChoiceCaseCodecImpl<?> tryToLoadImplementation(final Class<? extends DataContainer> inputType) {
            return getCaseCodecFor(inputType);
        }

        @Override
        protected void tryToLoadImplementations() {
            // TODO Auto-generated method stub

        }

        @Override
        protected void adaptForPathImpl(final InstanceIdentifier<?> path, final DataNodeContainer ctx) {
            // TODO Auto-generated method stub

        }
    }

    @SuppressWarnings("rawtypes")
    private class CaseClassMapFacade extends MapFacadeBase {

        @Override
        public Set<Entry<Class, BindingCodec<Object, Object>>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public BindingCodec get(final Object key) {
            if (key instanceof Class) {
                Class cls = (Class) key;
                // bindingClassEncountered(cls);
                ChoiceCaseCodecImpl caseCodec = getCaseCodecFor(cls);
                return caseCodec.getDelegate();
            }
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    private static class CaseCompositeNodeMapFacade extends MapFacadeBase<CompositeNode> {

        final Map<Class, ChoiceCaseCodecImpl<?>> choiceCases;

        public CaseCompositeNodeMapFacade(final Map<Class, ChoiceCaseCodecImpl<?>> choiceCases) {
            this.choiceCases = choiceCases;
        }

        @Override
        public BindingCodec get(final Object key) {
            if (!(key instanceof CompositeNode)) {
                return null;
            }
            for (Entry<Class, ChoiceCaseCodecImpl<?>> entry : choiceCases.entrySet()) {
                ChoiceCaseCodecImpl<?> codec = entry.getValue();
                if (codec.isAcceptable((CompositeNode) key)) {
                    return codec.getDelegate();
                }
            }
            return null;
        }

    }

    /**
     * This map is used as only facade for
     * {@link org.opendaylight.yangtools.yang.binding.BindingCodec} in different
     * classloaders to retrieve codec dynamicly based on provided key.
     *
     * @param <T>
     *            Key type
     */
    @SuppressWarnings("rawtypes")
    private abstract static class MapFacadeBase<T> implements Map<T, BindingCodec<?, ?>> {

        @Override
        public boolean containsKey(final Object key) {
            return get(key) != null;
        }

        @Override
        public void clear() {
            throw notModifiable();
        }

        @Override
        public boolean equals(final Object obj) {
            return super.equals(obj);
        }

        @Override
        public BindingCodec remove(final Object key) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public Collection<BindingCodec<?, ?>> values() {
            return Collections.emptySet();
        }

        private UnsupportedOperationException notModifiable() {
            return new UnsupportedOperationException("Not externally modifiable.");
        }

        @Override
        public BindingCodec<Map<QName, Object>, Object> put(final T key, final BindingCodec<?, ?> value) {
            throw notModifiable();
        }

        @Override
        public void putAll(final Map<? extends T, ? extends BindingCodec<?, ?>> m) {
            throw notModifiable();
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Set<T> keySet() {
            return Collections.emptySet();
        }

        @Override
        public Set<Entry<T, BindingCodec<?, ?>>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public boolean containsValue(final Object value) {
            return false;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private class AugmentableDispatchCodec extends LocationAwareDispatchCodec<AugmentationCodecWrapper> {

        private final Class augmentableType;

        public AugmentableDispatchCodec(final Class type) {
            Preconditions.checkArgument(Augmentable.class.isAssignableFrom(type));
            augmentableType = type;
        }

        @Override
        // TODO deprecate use without iid
        public Object serialize(final Object input) {
            if (input instanceof Augmentable<?>) {
                Map<Class, Augmentation> augmentations = getAugmentations(input);
                return serializeImpl(augmentations);
            }
            return null;
        }

        private Map<Class, Augmentation> getAugmentations(final Object input) {
            Field augmentationField;
            try {
                augmentationField = input.getClass().getDeclaredField("augmentation");
                augmentationField.setAccessible(true);
                Map<Class, Augmentation> augMap = (Map<Class, Augmentation>) augmentationField.get(input);
                return augMap;
            } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
                LOG.debug("Could not read augmentations for {}", input, e);
            }
            return Collections.emptyMap();
        }

        @SuppressWarnings("deprecation")
        private List serializeImpl(final Map<Class, Augmentation> input) {
            List ret = new ArrayList<>();
            for (Entry<Class, Augmentation> entry : input.entrySet()) {
                AugmentationCodec codec = getCodecForAugmentation(entry.getKey());
                CompositeNode node = codec.serialize(new ValueWithQName(null, entry.getValue()));
                ret.addAll(node.getChildren());
            }
            return ret;
        }

        @Override
        public Map<Class, Augmentation> deserialize(final Object input, final InstanceIdentifier path) {
            adaptForPath(path);
            Map<Class, Augmentation> ret = new HashMap<>();

            if (input instanceof CompositeNode) {
                List<Entry<Class, AugmentationCodecWrapper>> codecs = new ArrayList<>(getImplementations().entrySet());
                for (Entry<Class, AugmentationCodecWrapper> codec : codecs) {
                    AugmentationCodec<?> ac = codec.getValue();
                    if (ac.isAcceptable(path)) {
                        // We add Augmentation Identifier to path, in order to
                        // correctly identify children.
                        InstanceIdentifier augmentPath = path.builder().augmentation(codec.getKey()).build();
                        ValueWithQName<?> value = codec.getValue().deserialize((CompositeNode) input, augmentPath);
                        if (value != null && value.getValue() != null) {
                            ret.put(codec.getKey(), (Augmentation) value.getValue());
                        }
                    }
                }
            }
            return ret;
        }

        protected Optional<AugmentationCodecWrapper> tryToLoadImplementation(final Type potential) {
            try {
                Class<? extends Augmentation<?>> clazz = (Class<? extends Augmentation<?>>) classLoadingStrategy
                        .loadClass(potential);
                return Optional.of(tryToLoadImplementation(clazz));
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to find class for augmentation of {}", potential, e);
            }
            return Optional.absent();
        }

        @Override
        protected AugmentationCodecWrapper tryToLoadImplementation(final Class inputType) {
            AugmentationCodecWrapper<? extends Augmentation<?>> potentialImpl = getCodecForAugmentation(inputType);
            addImplementation(potentialImpl);
            return potentialImpl;
        }

        @Override
        protected void tryToLoadImplementations() {
            Type type = referencedType(augmentableType);
            Collection<Type> potentialAugmentations;
            synchronized(augmentableToAugmentations) {
                potentialAugmentations = new ArrayList(augmentableToAugmentations.get(type));
            }
            for (Type potential : potentialAugmentations) {
                try {
                    tryToLoadImplementation(potential);
                } catch (CodeGenerationException e) {
                    LOG.warn("Failed to proactively generate augment code for {}", type, e);
                }
            }
        }

        @Override
        protected void adaptForPathImpl(final InstanceIdentifier<?> path, final DataNodeContainer ctxNode) {
            if (ctxNode instanceof AugmentationTarget) {
                Set<AugmentationSchema> availableAugmentations = ((AugmentationTarget) ctxNode)
                        .getAvailableAugmentations();
                if (!availableAugmentations.isEmpty()) {
                    updateAugmentationMapping(path,availableAugmentations);
                }
            }
        }

        private void updateAugmentationMapping(final InstanceIdentifier<?> path, final Set<AugmentationSchema> availableAugmentations) {
            for (AugmentationSchema aug : availableAugmentations) {

                Type potentialType = getTypeForAugmentation(aug);
                if (potentialType != null) {
                    Optional<AugmentationCodecWrapper> potentialImpl = tryToLoadImplementation(potentialType);
                    if (potentialImpl.isPresent()) {
                        potentialImpl.get().addApplicableFor(path,aug);
                    }
                } else {
                    LOG.warn("Could not find generated type for augmentation {} with children {}", aug, aug.getChildNodes());
                }
            }
            availableAugmentations.toString();
        }

        private Type getTypeForAugmentation(final AugmentationSchema aug) {
            Optional<AugmentationSchema> currentAug = Optional.of(aug);
            while(currentAug.isPresent()) {
                Type potentialType = typeToAugment.inverse().get(currentAug.get());
                if(potentialType != null) {
                    return potentialType;
                }
                currentAug = currentAug.get().getOriginalDefinition();
            }
            return null;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static class LateMixinCodec implements BindingCodec, Delegator<BindingCodec> {

        private BindingCodec delegate;

        @Override
        public BindingCodec getDelegate() {
            if (delegate == null) {
                throw new IllegalStateException("Codec not initialized yet.");
            }
            return delegate;
        }

        @Override
        public Object deserialize(final Object input) {
            return getDelegate().deserialize(input);
        }

        @Override
        public Object deserialize(final Object input, final InstanceIdentifier bindingIdentifier) {
            return getDelegate().deserialize(input, bindingIdentifier);
        }

        @Override
        public Object serialize(final Object input) {
            return getDelegate().serialize(input);
        }

    }

    @SuppressWarnings("rawtypes")
    private static class AugmentationCodecWrapper<T extends Augmentation<?>> implements AugmentationCodec<T>,
            Delegator<BindingCodec>, LocationAwareBindingCodec<Node<?>, ValueWithQName<T>> {

        private final BindingCodec delegate;
        private final QName augmentationQName;
        private final Multimap<InstanceIdentifier<?>,QName> validAugmentationTargets;
        private final Class<?> augmentationType;

        public AugmentationCodecWrapper(final BindingCodec<Map<QName, Object>, Object> rawCodec,
                final InstanceIdentifier<?> targetId, final Class<?> dataType) {
            this.delegate = rawCodec;
            this.augmentationType = dataType;
            this.augmentationQName = BindingReflections.findQName(rawCodec.getClass());
            this.validAugmentationTargets = Multimaps.synchronizedSetMultimap(HashMultimap.<InstanceIdentifier<?>,QName>create());
        }

        public void addApplicableFor(final InstanceIdentifier<?> path, final AugmentationSchema aug) {
            for(DataSchemaNode child : aug.getChildNodes()) {
                validAugmentationTargets.put(path,child.getQName());
            }
        }

        @Override
        public BindingCodec getDelegate() {
            return delegate;
        }

        @Override
        public CompositeNode serialize(final ValueWithQName<T> input) {
            @SuppressWarnings("unchecked")
            List<Map<QName, Object>> rawValues = (List<Map<QName, Object>>) getDelegate().serialize(input);
            List<Node<?>> serialized = new ArrayList<>(rawValues.size());
            for (Map<QName, Object> val : rawValues) {
                serialized.add(IntermediateMapping.toNode(val));
            }
            return new CompositeNodeTOImpl(input.getQname(), null, serialized);
        }

        @Override
        @SuppressWarnings("unchecked")
        public ValueWithQName<T> deserialize(final Node<?> input) {
            Object rawCodecValue = getDelegate().deserialize(input);
            return new ValueWithQName<T>(input.getNodeType(), (T) rawCodecValue);
        }

        @Override
        @SuppressWarnings("unchecked")
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            // if (!isAcceptable(bindingIdentifier)) {
            // return null;
            // }
            Object rawCodecValue = getDelegate().deserialize(input, bindingIdentifier);
            return new ValueWithQName<T>(input.getNodeType(), (T) rawCodecValue);
        }

        @Override
        public QName getAugmentationQName() {
            return augmentationQName;
        }

        @Override
        public boolean isAcceptable(final InstanceIdentifier<?> path) {
            if (path == null) {
                return false;
            }
            return validAugmentationTargets.containsKey(path);
        }

        @Override
        public boolean isApplicable(final InstanceIdentifier location) {
            return isAcceptable(location);
        }

        @Override
        public Class<?> getDataType() {
            return augmentationType;
        }
    }

    @SuppressWarnings("rawtypes")
    private class IdentityCompositeCodec implements IdentityCodec {

        @Override
        public Object deserialize(final Object input) {
            Preconditions.checkArgument(input instanceof QName);
            return deserialize((QName) input);
        }

        @Override
        public Class<?> deserialize(final QName input) {
            Type type = qnamesToIdentityMap.get(input);
            if (type == null) {
                return null;
            }
            ReferencedTypeImpl typeref = new ReferencedTypeImpl(type.getPackageName(), type.getName());
            WeakReference<Class> softref = typeToClass.get(typeref);
            if (softref == null) {

                try {
                    Class<?> cls = classLoadingStrategy.loadClass(typeref.getFullyQualifiedName());
                    if (cls != null) {
                        serialize(cls);
                        return cls;
                    }
                } catch (Exception e) {
                    LOG.warn("Identity {} was not deserialized, because of missing class {}", input,
                            typeref.getFullyQualifiedName());
                }
                return null;
            }
            return softref.get();
        }

        @Override
        public Object deserialize(final Object input,final InstanceIdentifier bindingIdentifier) {
            Type type = qnamesToIdentityMap.get(input);
            if (type == null) {
                return null;
            }
            ReferencedTypeImpl typeref = new ReferencedTypeImpl(type.getPackageName(), type.getName());
            WeakReference<Class> softref = typeToClass.get(typeref);
            if (softref == null) {

                try {
                    Class<?> cls = classLoadingStrategy.loadClass(typeref.getFullyQualifiedName());
                    if (cls != null) {
                        serialize(cls);
                        return cls;
                    }
                } catch (Exception e) {
                    LOG.warn("Identity {} was not deserialized, because of missing class {}", input,
                            typeref.getFullyQualifiedName());
                }
                return null;
            }
            return softref.get();
        }

        @Override
        public QName serialize(final Class input) {
            Preconditions.checkArgument(BaseIdentity.class.isAssignableFrom(input));
            bindingClassEncountered(input);
            QName qname = identityQNames.get(input);
            if (qname != null) {
                return qname;
            }
            ConcreteType typeref = Types.typeForClass(input);
            qname = typeToQname.get(typeref);
            if (qname != null) {
                identityQNames.put(input, qname);
            }
            return qname;
        }

        @Override
        public Object serialize(final Object input) {
            Preconditions.checkArgument(input instanceof Class);
            return serialize((Class) input);
        }

    }

    private static final Type referencedType(final Class<?> augmentableType) {
        return new ReferencedTypeImpl(augmentableType.getPackage().getName(), augmentableType.getSimpleName());
    }
}
