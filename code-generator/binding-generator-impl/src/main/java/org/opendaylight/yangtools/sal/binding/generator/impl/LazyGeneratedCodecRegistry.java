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
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

class LazyGeneratedCodecRegistry implements //
        CodecRegistry, //
        SchemaContextListener, //
        GeneratorListener {

    private static final Logger LOG = LoggerFactory.getLogger(LazyGeneratedCodecRegistry.class);

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

    private static final Map<Class<?>, LocationAwareDispatchCodec<?>> dispatchCodecs = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, LocationAwareDispatchCodec<?>>());

    private static final Map<Class<?>, QName> identityQNames = Collections
            .synchronizedMap(new WeakHashMap<Class<?>, QName>());
    private static final Map<QName, Type> qnamesToIdentityMap = new ConcurrentHashMap<>();
    /** Binding type to encountered classes mapping **/
    @SuppressWarnings("rawtypes")
    private static final Map<Type, WeakReference<Class>> typeToClass = new ConcurrentHashMap<>();

    private static final ConcurrentMap<Type, ChoiceCaseNode> caseTypeToCaseSchema = new ConcurrentHashMap<>();

    private static final Map<SchemaPath, Type> pathToType = new ConcurrentHashMap<>();
    private static final Map<List<QName>, Type> pathToInstantiatedType = new ConcurrentHashMap<>();
    private static final Map<Type, QName> typeToQname = new ConcurrentHashMap<>();
    private static final BiMap<Type, AugmentationSchema> typeToAugment = HashBiMap
            .create(new ConcurrentHashMap<Type, AugmentationSchema>());

    private static final Multimap<Type, Type> augmentableToAugmentations = Multimaps.synchronizedMultimap(HashMultimap
            .<Type, Type> create());
    private static final Multimap<Type, Type> choiceToCases = Multimaps.synchronizedMultimap(HashMultimap
            .<Type, Type> create());

    private final InstanceIdentifierCodec instanceIdentifierCodec = new InstanceIdentifierCodecImpl(this);
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
    public <T extends Augmentation<?>> AugmentationCodecWrapper<T> getCodecForAugmentation(final Class<T> augClass) {
        AugmentationCodecWrapper<T> codec = null;
        @SuppressWarnings("rawtypes")
        AugmentationCodecWrapper potentialCodec = augmentationCodecs.get(augClass);
        if (potentialCodec != null) {
            codec = potentialCodec;
        } else {
            lock.waitForSchema(augClass);
            Class<? extends BindingCodec<Map<QName, Object>, Object>> augmentRawCodec = generator
                    .augmentationTransformerFor(augClass);

            BindingCodec<Map<QName, Object>, Object> rawCodec = newInstanceOf(augmentRawCodec);
            codec = new AugmentationCodecWrapper<T>(rawCodec, augClass);
            augmentationCodecs.put(augClass, codec);
        }

        final Class<? extends Augmentable<?>> objectSupertype;
        try {
            objectSupertype = BindingReflections.findAugmentationTarget(augClass);
        } catch (Exception e) {
            LOG.warn("Failed to find target for augmentation {}, ignoring it", augClass, e);
            return codec;
        }

        if (objectSupertype == null) {
            LOG.warn("Augmentation target for {} not found, ignoring it", augClass);
            return codec;
        }

        getAugmentableCodec(objectSupertype).addImplementation(codec);
        return codec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QName getQNameForAugmentation(final Class<?> cls) {
        Preconditions.checkArgument(Augmentation.class.isAssignableFrom(cls));
        return getCodecForAugmentation((Class<? extends Augmentation<?>>) cls).getAugmentationQName();
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
        Preconditions.checkState(weakRef != null, "Could not find loaded class for path: %s and type: %s", path,
                type.getFullyQualifiedName());
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
            getCodecForDataObject((Class<? extends DataObject>) cls);
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
        DataNodeContainer previous = currentSchema.findModuleByNamespaceAndRevision(firstNode.getNamespace(),
                firstNode.getRevision());
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
            LOG.debug(
                    "Run-time consistency issue: constructor for {} is not available. This indicates either a code generation bug or a misconfiguration of JVM.",
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
        ChoiceCaseNode caseSchema = caseTypeToCaseSchema.get(typeref);

        Preconditions.checkState(caseSchema != null, "Case schema is not available for %s", caseClass.getName());
        Class<? extends BindingCodec> newCodec = generator.caseCodecFor(caseClass, caseSchema);
        BindingCodec newInstance = newInstanceOf(newCodec);
        @SuppressWarnings("unchecked")
        ChoiceCaseCodecImpl caseCodec = new ChoiceCaseCodecImpl(caseClass, caseSchema, newInstance);
        caseCodecs.put(caseClass, caseCodec);
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

        synchronized (augmentableToAugmentations) {
            augmentableToAugmentations.putAll(context.getAugmentableToAugmentations());
        }
        synchronized (choiceToCases) {
            choiceToCases.putAll(context.getChoiceToCases());
        }
        synchronized (caseTypeToCaseSchema) {
            caseTypeToCaseSchema.putAll(context.getCaseTypeToSchemas());
        }
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext context) {
        currentSchema = context;
        resetDispatchCodecsAdaptation();

    }

    /**
     * Resets / clears adaptation for all schema context sensitive codecs in
     * order for them to adapt to new schema context and maybe newly discovered
     * augmentations This ensure correct behaviour for augmentations and
     * augmented cases for preexisting codecs, which augmentations were
     * introduced at later point in time.
     *
     * This also makes removed augmentations unavailable.
     */
    private void resetDispatchCodecsAdaptation() {
        synchronized (dispatchCodecs) {
            for (LocationAwareDispatchCodec<?> codec : dispatchCodecs.values()) {
                codec.resetCodec(this);
            }
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void onChoiceCodecCreated(final Class<?> choiceClass,
            final Class<? extends BindingCodec<Map<QName, Object>, Object>> choiceCodec, final ChoiceNode schema) {
        ChoiceCodec<?> oldCodec = choiceCodecs.get(choiceClass);
        Preconditions.checkState(oldCodec == null);
        BindingCodec<Map<QName, Object>, Object> delegate = newInstanceOf(choiceCodec);
        PublicChoiceCodecImpl<?> newCodec = new PublicChoiceCodecImpl(delegate);
        DispatchChoiceCodecImpl dispatchCodec = new DispatchChoiceCodecImpl(choiceClass,this);
        choiceCodecs.put(choiceClass, newCodec);
        synchronized (dispatchCodecs) {
            dispatchCodecs.put(choiceClass, dispatchCodec);
        }
        CodecMapping.setDispatchCodec(choiceCodec, dispatchCodec);
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

    public synchronized AugmentableDispatchCodec getAugmentableCodec(final Class<?> dataClass) {
        AugmentableDispatchCodec ret = augmentableCodecs.get(dataClass);
        if (ret != null) {
            return ret;
        }
        ret = new AugmentableDispatchCodec(dataClass,this);
        augmentableCodecs.put(dataClass, ret);
        synchronized (dispatchCodecs) {
            dispatchCodecs.put(dataClass, ret);
        }
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

        boolean isApplicable(InstanceIdentifier<?> parentPath, CompositeNode data);

        public Class<?> getDataType();

    }

    @SuppressWarnings("rawtypes")
    private static abstract class LocationAwareDispatchCodec<T extends LocationAwareBindingCodec> implements BindingCodec {

        private final Map<Class, T> implementations = Collections.synchronizedMap(new WeakHashMap<Class, T>());
        private final Set<InstanceIdentifier<?>> adaptedForPaths = new HashSet<>();
        private LazyGeneratedCodecRegistry registry;


        protected LocationAwareDispatchCodec(final LazyGeneratedCodecRegistry registry) {
            this.registry = registry;
        }

        protected Map<Class, T> getImplementations() {
            return implementations;
        }

        /**
         * Resets codec adaptation based on location and schema context.
         *
         * This is required if new cases / augmentations were introduced or
         * removed and first use of codec is triggered by invocation from DOM to
         * Java, so the implementations may change and this may require loading
         * of new codecs and/or removal of existing ones.
         *
         */
        public synchronized void resetCodec(final LazyGeneratedCodecRegistry currentRegistry) {
            registry = currentRegistry;
            adaptedForPaths.clear();
            resetAdaptationImpl();
        }

        protected void resetAdaptationImpl() {
            // Intentionally NOOP, subclasses may specify their custom
            // behaviour.
        }

        protected final LazyGeneratedCodecRegistry getRegistry() {
            return registry;
        }
        protected void addImplementation(final T implementation) {
            implementations.put(implementation.getDataType(), implementation);
        }

        @Override
        public final Object deserialize(final Object input) {
            throw new UnsupportedOperationException("Invocation of deserialize without Tree location is unsupported");
        }

        @Override
        public final Object deserialize(final Object parent, final InstanceIdentifier parentPath) {
            adaptForPath(parentPath);
            Preconditions.checkArgument(parent instanceof CompositeNode, "node must be of CompositeNode type.");
            CompositeNode parentData = (CompositeNode) parent;
            ArrayList<T> applicable = new ArrayList<>(implementations.size());

            /*
             * Codecs are filtered to only ones, which
             * are applicable in supplied parent context.
             *
             */
            for (T impl : getImplementations().values()) {
                @SuppressWarnings("unchecked")
                boolean codecApplicable = impl.isApplicable(parentPath, parentData);
                if (codecApplicable) {
                    applicable.add(impl);
                }
            }
            LOG.trace("{}: Deserializing mixins from {}, Schema Location {}, Applicable Codecs: {}, All Codecs: {}",this,parent,parentPath,applicable,getImplementations().values());

            /* In case of none is applicable, we return
             * null. Since there is no mixin which
             * is applicable in this location.
            */
            if(applicable.isEmpty()) {
                return null;
            }
            return deserializeImpl(parentData, parentPath, applicable);
        }

        protected abstract Object deserializeImpl(final CompositeNode input, final InstanceIdentifier<?> parentPath,
                Iterable<T> applicableCodecs);

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

        protected final synchronized void adaptForPath(final InstanceIdentifier<?> path) {
            if (adaptedForPaths.contains(path)) {
                return;
            }
            LOG.info("Adapting mixin codec {} for path {}",this,path);
            /**
             * We search in schema context if the use of this location aware
             * codec (augmentable codec, case codec) makes sense on provided
             * location (path)
             *
             */
            Optional<DataNodeContainer> contextNode = BindingSchemaContextUtils.findDataNodeContainer(getRegistry().currentSchema,
                    path);
            /**
             * If context node is present, this codec makes sense on provided
             * location.
             *
             */
            if (contextNode.isPresent()) {
                synchronized (this) {
                    /**
                     *
                     * We adapt (turn on / off) possible implementations of
                     * child codecs (augmentations, cases) based on this
                     * location.
                     *
                     *
                     */

                    adaptForPathImpl(path, contextNode.get());
                    try {
                        /**
                         * We trigger serialization of instance identifier, to
                         * make sure instance identifier codec is aware of
                         * combination of this path / augmentation / case
                         */
                        getRegistry().getInstanceIdentifierCodec().serialize(path);
                    } catch (Exception e) {
                        LOG.warn("Exception during preparation of instance identifier codec for  path {}.", path, e);
                    }
                    adaptedForPaths.add(path);
                }
            } else {
                LOG.debug("Context node (parent node) not found for {}", path);
            }
        }

        protected abstract T tryToLoadImplementation(Class<? extends DataContainer> inputType);

        protected abstract void tryToLoadImplementations();

        protected abstract void adaptForPathImpl(InstanceIdentifier<?> path, DataNodeContainer ctx);
    }

    @SuppressWarnings("rawtypes")
    private static class ChoiceCaseCodecImpl<T extends DataContainer> implements ChoiceCaseCodec<T>, //
            Delegator<BindingCodec>, LocationAwareBindingCodec<Node<?>, ValueWithQName<T>> {
        private final BindingCodec delegate;
        private final ChoiceCaseNode schema;
        private final Map<InstanceIdentifier<?>, ChoiceCaseNode> instantiatedLocations;
        private final Class<?> dataType;

        public ChoiceCaseCodecImpl(final Class<?> caseClass, final ChoiceCaseNode caseNode,
                final BindingCodec newInstance) {
            this.delegate = newInstance;
            this.dataType = caseClass;
            this.schema = caseNode;
            instantiatedLocations = new HashMap<>();
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public ValueWithQName<T> deserialize(final Node<?> input, final InstanceIdentifier<?> bindingIdentifier) {
            if (input == null) {
                return null;
            }
            QName qname = input.getNodeType();
            synchronized (instantiatedLocations) {
                ChoiceCaseNode instantiation = instantiatedLocations.get(bindingIdentifier);
                if (instantiation != null) {
                    qname = instantiatedLocations.get(bindingIdentifier).getQName();
                }
            }
            @SuppressWarnings("unchecked")
            T value = (T) getDelegate().deserialize(new SimpleEntry(qname, input), bindingIdentifier);
            return new ValueWithQName<T>(qname, value);
        }

        @Override
        public CompositeNode serialize(final ValueWithQName<T> input) {
            throw new UnsupportedOperationException("Direct invocation of this codec is not allowed.");
        }

        @Override
        public BindingCodec getDelegate() {
            return delegate;
        }

        public ChoiceCaseNode getSchema() {
            return schema;
        }

        @Override
        @Deprecated
        public boolean isAcceptable(final Node<?> input) {
            return checkAgainstSchema(schema, input);
        }

        private static boolean checkAgainstSchema(final ChoiceCaseNode schema, final Node<?> node) {
            if (node instanceof CompositeNode) {
                CompositeNode input = (CompositeNode) node;
                for (Node<?> childNode : input.getValue()) {
                    QName child = childNode.getNodeType();
                    if (schema.getDataChildByName(child) != null) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public Class<?> getDataType() {
            return dataType;
        }

        public void adaptForPath(final InstanceIdentifier<?> augTarget, final ChoiceCaseNode choiceCaseNode) {
            synchronized (instantiatedLocations) {
                instantiatedLocations.put(augTarget, choiceCaseNode);
            }
        }

        @Override
        public boolean isApplicable(final InstanceIdentifier path, final CompositeNode input) {
            ChoiceCaseNode instantiatedSchema = null;
            synchronized (instantiatedLocations) {
                instantiatedSchema = instantiatedLocations.get(path);
            }
            if (instantiatedSchema == null) {
                return false;
            }
            return checkAgainstSchema(instantiatedSchema, input);
        }

        protected boolean isAugmenting(final QName choiceName, final QName proposedQName) {
            if (schema.isAugmenting()) {
                return true;
            }
            // Choice QName
            QName parentQName = Iterables.get(schema.getPath().getPathTowardsRoot(), 1);
            if (!parentQName.getNamespace().equals(schema.getQName().getNamespace())) {
                return true;
            }
            if (!parentQName.equals(choiceName)) {
                // This item is instantiation of choice via uses in other YANG
                // module
                if (choiceName.getNamespace().equals(schema.getQName())) {
                    // Original definition of grouping is in same namespace
                    // as original definition of case
                    // so for sure case is introduced via instantiation of
                    // grouping
                    return false;
                }
                // Since we are still here, that means case has same namespace
                // as its parent, which is instantiation of grouping
                // but case namespace is different from parent node
                // so it is augmentation.
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "ChoiceCaseCodec [case=" + dataType
                    + ", knownLocations=" + instantiatedLocations.keySet() + "]";
        }
    }

    private static class PublicChoiceCodecImpl<T> implements ChoiceCodec<T>,
            Delegator<BindingCodec<Map<QName, Object>, Object>> {

        private final BindingCodec<Map<QName, Object>, Object> delegate;

        public PublicChoiceCodecImpl(final BindingCodec<Map<QName, Object>, Object> delegate) {
            this.delegate = delegate;
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

        @Override
        public BindingCodec<Map<QName, Object>, Object> getDelegate() {
            return delegate;
        }

    }

    class DispatchChoiceCodecImpl extends LocationAwareDispatchCodec<ChoiceCaseCodecImpl<?>> {
        private final Class<?> choiceType;
        private final QName choiceName;

        private DispatchChoiceCodecImpl(final Class<?> type, final LazyGeneratedCodecRegistry registry) {
            super(registry);
            choiceType = type;
            choiceName = BindingReflections.findQName(type);
        }

        @Override
        public Object deserializeImpl(final CompositeNode input, final InstanceIdentifier<?> path,
                final Iterable<ChoiceCaseCodecImpl<?>> codecs) {
            ChoiceCaseCodecImpl<?> caseCodec = Iterables.getOnlyElement(codecs);
            ValueWithQName<?> value = caseCodec.deserialize(input, path);
            if (value != null) {
                return value.getValue();
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Object serialize(final Object input) {
            Preconditions.checkArgument(input instanceof Map.Entry<?, ?>, "Input must be QName, Value");
            @SuppressWarnings("rawtypes")
            QName derivedQName = (QName) ((Map.Entry) input).getKey();
            @SuppressWarnings("rawtypes")
            Object inputValue = ((Map.Entry) input).getValue();
            Preconditions.checkArgument(inputValue instanceof DataObject);
            Class<? extends DataContainer> inputType = ((DataObject) inputValue).getImplementedInterface();
            ChoiceCaseCodecImpl<?> codec = tryToLoadImplementation(inputType);
            Preconditions.checkState(codec != null, "Unable to get codec for %s", inputType);
            if (codec.isAugmenting(choiceName, derivedQName)) {
                // If choice is augmenting we use QName which defined this
                // augmentation
                return codec.getDelegate().serialize(new ValueWithQName<>(codec.getSchema().getQName(), inputValue));
            }
            return codec.getDelegate().serialize(input);
        }

        @SuppressWarnings("rawtypes")
        protected Optional<ChoiceCaseCodecImpl> tryToLoadImplementation(final Type potential) {
            try {
                @SuppressWarnings("unchecked")
                Class<? extends DataContainer> clazz = (Class<? extends DataContainer>) classLoadingStrategy
                        .loadClass(potential);
                ChoiceCaseCodecImpl codec = tryToLoadImplementation(clazz);
                addImplementation(codec);
                return Optional.of(codec);
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to find class for choice {}", potential, e);
            }
            return Optional.absent();
        }

        @Override
        protected ChoiceCaseCodecImpl<?> tryToLoadImplementation(final Class<? extends DataContainer> inputType) {
            ChoiceCaseCodecImpl<?> codec = getCaseCodecFor(inputType);
            addImplementation(codec);
            return codec;
        }

        @Override
        protected void tryToLoadImplementations() {
            Type type = referencedType(choiceType);
            Collection<Type> potentialCases;
            synchronized (choiceToCases) {
                potentialCases = choiceToCases.get(type);
            }
            for (Type potential : potentialCases) {
                try {
                    tryToLoadImplementation(potential);
                } catch (CodeGenerationException e) {
                    LOG.warn("Failed to proactively generate choice code for {}", type, e);
                }
            }
        }

        @Override
        protected void adaptForPathImpl(final InstanceIdentifier<?> augTarget, final DataNodeContainer ctxNode) {
            Optional<ChoiceNode> newChoice = BindingSchemaContextUtils.findInstantiatedChoice(ctxNode, choiceType);
            tryToLoadImplementations();
            Preconditions.checkState(newChoice.isPresent(), "BUG: Unable to find instantiated choice node in schema.");
            for (@SuppressWarnings("rawtypes")
            Entry<Class, ChoiceCaseCodecImpl<?>> codec : getImplementations().entrySet()) {
                ChoiceCaseCodecImpl<?> caseCodec = codec.getValue();
                Optional<ChoiceCaseNode> instantiatedSchema = BindingSchemaContextUtils.findInstantiatedCase(newChoice.get(),
                        caseCodec.getSchema());
                if (instantiatedSchema.isPresent()) {
                    caseCodec.adaptForPath(augTarget, instantiatedSchema.get());
                }
            }
        }



        @Override
        public String toString() {
            return "DispatchChoiceCodecImpl [choiceType=" + choiceType + "]";
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static class AugmentableDispatchCodec extends LocationAwareDispatchCodec<AugmentationCodecWrapper> {

        private final Class augmentableType;

        public AugmentableDispatchCodec(final Class type, final LazyGeneratedCodecRegistry registry) {
            super(registry);
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
                AugmentationCodec codec = getRegistry().getCodecForAugmentation(entry.getKey());
                CompositeNode node = codec.serialize(new ValueWithQName(null, entry.getValue()));
                ret.addAll(node.getChildren());
            }
            return ret;
        }

        @Override
        public Map<Class, Augmentation> deserializeImpl(final CompositeNode input, final InstanceIdentifier<?> path,
                final Iterable<AugmentationCodecWrapper> codecs) {
            LOG.trace("{}: Going to deserialize augmentations from {} in location {}. Available codecs {}",this,input,path,codecs);
            Map<Class, Augmentation> ret = new HashMap<>();
            for (AugmentationCodecWrapper codec : codecs) {
                    // We add Augmentation Identifier to path, in order to
                    // correctly identify children.
                    Class type = codec.getDataType();
                    final InstanceIdentifier augmentPath = path.augmentation(type);
                    ValueWithQName<?> value = codec.deserialize(input, augmentPath);
                    if (value != null && value.getValue() != null) {
                        ret.put(type, (Augmentation) value.getValue());
                    }
            }
            return ret;
        }

        protected Optional<AugmentationCodecWrapper> tryToLoadImplementation(final Type potential) {
            try {
                Class<? extends Augmentation<?>> clazz = (Class<? extends Augmentation<?>>) getRegistry().classLoadingStrategy
                        .loadClass(potential);
                return Optional.of(tryToLoadImplementation(clazz));
            } catch (ClassNotFoundException e) {
                LOG.warn("Failed to find class for augmentation of {}", potential, e);
            }
            return Optional.absent();
        }

        @Override
        protected AugmentationCodecWrapper tryToLoadImplementation(final Class inputType) {
            AugmentationCodecWrapper<? extends Augmentation<?>> potentialImpl = getRegistry().getCodecForAugmentation(inputType);
            addImplementation(potentialImpl);
            return potentialImpl;
        }

        @Override
        protected void tryToLoadImplementations() {
            Type type = referencedType(augmentableType);
            Collection<Type> potentialAugmentations;
            synchronized (augmentableToAugmentations) {
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
        protected void adaptForPathImpl(final InstanceIdentifier<?> augTarget, final DataNodeContainer ctxNode) {
            if (ctxNode instanceof AugmentationTarget) {
                Set<AugmentationSchema> availableAugmentations = ((AugmentationTarget) ctxNode)
                        .getAvailableAugmentations();
                if (!availableAugmentations.isEmpty()) {
                    updateAugmentationMapping(augTarget, availableAugmentations);
                }
            }
        }

        /**
         *
         * Adapts augmentation codec for specific provider location (target)
         *
         * Since augmentation are not forward-referencing and may be discovered
         * during runtime, we need to adapt {@link AugmentableDispatchCodec},
         * {@link AugmentationCodecWrapper} and {@link InstanceIdentifierCodec}
         * for this newly discovered location where augmentation may be used.
         *
         * Adaptation consists of:
         * <ol>
         * <li>scan of available (valid) augmentations for current location
         * <li>lookup for Java classes derived from this augmentations
         * <li>generation of missing codecs
         * <li>updating Augmentation codecs to work with new location
         * <li>updating Instance Identifier to work with new location
         *
         */
        private void updateAugmentationMapping(final InstanceIdentifier<?> augTarget,
                final Set<AugmentationSchema> availableAugmentations) {
            for (AugmentationSchema aug : availableAugmentations) {

                Type potentialType = getTypeForAugmentation(aug);
                if (potentialType != null) {
                    Optional<AugmentationCodecWrapper> potentialImpl = tryToLoadImplementation(potentialType);
                    if (potentialImpl.isPresent()) {
                        potentialImpl.get().addApplicableFor(augTarget, aug);
                        Class augType = potentialImpl.get().getDataType();
                        InstanceIdentifier augPath = augTarget.augmentation(augType);
                        try {

                            org.opendaylight.yangtools.yang.data.api.InstanceIdentifier domPath = getRegistry().getInstanceIdentifierCodec()
                                    .serialize(augPath);
                            if (domPath == null) {
                                LOG.error("Unable to serialize instance identifier for {}", augPath);
                            }
                        } catch (Exception e) {
                            LOG.error("Unable to serialize instance identifiers for {}", augPath, e);
                        }

                    }
                } else {
                    // Omits warning for empty augmentations since they are not
                    // represented in data
                    if (!aug.getChildNodes().isEmpty()) {
                        LOG.warn("Could not find generated type for augmentation {} with children {}", aug,
                                aug.getChildNodes());
                    }
                }
            }
        }

        private Type getTypeForAugmentation(final AugmentationSchema aug) {
            Optional<AugmentationSchema> currentAug = Optional.of(aug);
            while (currentAug.isPresent()) {
                Type potentialType = typeToAugment.inverse().get(currentAug.get());
                if (potentialType != null) {
                    return potentialType;
                }
                currentAug = currentAug.get().getOriginalDefinition();
            }
            return null;
        }

        @Override
        public String toString() {
            return "AugmentableDispatchCodec [augmentable=" + augmentableType + "]";
        }

    }

    @SuppressWarnings("rawtypes")
    private static class AugmentationCodecWrapper<T extends Augmentation<?>> implements AugmentationCodec<T>,
            Delegator<BindingCodec>, LocationAwareBindingCodec<Node<?>, ValueWithQName<T>> {

        private final BindingCodec delegate;
        private final QName augmentationQName;
        private final Multimap<InstanceIdentifier<?>, QName> validAugmentationTargets;
        private final Class<?> augmentationType;

        public AugmentationCodecWrapper(final BindingCodec<Map<QName, Object>, Object> rawCodec, final Class<?> dataType) {
            this.delegate = rawCodec;
            this.augmentationType = dataType;
            this.augmentationQName = BindingReflections.findQName(rawCodec.getClass());
            this.validAugmentationTargets = Multimaps.synchronizedSetMultimap(HashMultimap
                    .<InstanceIdentifier<?>, QName> create());
        }

        public void addApplicableFor(final InstanceIdentifier<?> path, final AugmentationSchema aug) {
            for (DataSchemaNode child : aug.getChildNodes()) {
                validAugmentationTargets.put(path, child.getQName());
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
        public boolean isApplicable(final InstanceIdentifier parentPath,final CompositeNode parentData) {
            return isAcceptable(parentPath);
        }

        @Override
        public Class<?> getDataType() {
            return augmentationType;
        }

        @Override
        public String toString() {
            return "AugmentationCodecWrapper [augmentation=" + augmentationType
                    + ", knownLocations=" + validAugmentationTargets.keySet() + "]";
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
        public Object deserialize(final Object input, final InstanceIdentifier bindingIdentifier) {
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
