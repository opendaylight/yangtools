/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javassist.ClassPool;

import javax.annotation.concurrent.GuardedBy;

import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.YangSchemaUtils;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.IdentifiableItem;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.Item;
import org.opendaylight.yangtools.yang.binding.RpcService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.CompositeNode;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.SimpleNodeTOImpl;
import org.opendaylight.yangtools.yang.data.impl.codec.AugmentationCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService;
import org.opendaylight.yangtools.yang.data.impl.codec.CodecRegistry;
import org.opendaylight.yangtools.yang.data.impl.codec.DataContainerCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException;
import org.opendaylight.yangtools.yang.data.impl.codec.InstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder;
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.SettableFuture;

public class RuntimeGeneratedMappingServiceImpl implements BindingIndependentMappingService, SchemaContextListener,
        SchemaLock, AutoCloseable, SchemaContextHolder, TypeResolver {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeGeneratedMappingServiceImpl.class);

    private final ConcurrentMap<Type, Set<QName>> serviceTypeToRpc = new ConcurrentHashMap<>();

    /**
     * This is map of types which users are waiting for.
     */
    @GuardedBy("this")
    private final Multimap<Type, SettableFuture<Type>> promisedTypes = HashMultimap.create();

    private final ClassLoadingStrategy classLoadingStrategy;

    // FIXME: will become final
    private ClassPool pool;
    private AbstractTransformerGenerator binding;
    private LazyGeneratedCodecRegistry registry;

    /*
     * FIXME: updated here, access from AbstractTransformer
     */
    private final Map<Type, AugmentationSchema> typeToAugmentation = new ConcurrentHashMap<>();
    private final ConcurrentMap<Type, GeneratedTypeBuilder> typeToDefinition = new ConcurrentHashMap<>();
    private final ConcurrentMap<Type, SchemaNode> typeToSchemaNode = new ConcurrentHashMap<>();
    private final Map<SchemaPath, GeneratedTypeBuilder> pathToType = new ConcurrentHashMap<>();

    // FIXME: need to figure these out
    private final ConcurrentMap<Type, Type> typeDefinitions = new ConcurrentHashMap<>();
    private SchemaContext schemaContext;

    @Deprecated
    public RuntimeGeneratedMappingServiceImpl() {
        this(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    @Deprecated
    public RuntimeGeneratedMappingServiceImpl(final ClassLoadingStrategy strat) {
        classLoadingStrategy = strat;
    }

    public RuntimeGeneratedMappingServiceImpl(final ClassPool pool) {
        this(pool, GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    public RuntimeGeneratedMappingServiceImpl(final ClassPool pool, final ClassLoadingStrategy strat) {
        this.pool = Preconditions.checkNotNull(pool);
        this.classLoadingStrategy = Preconditions.checkNotNull(strat);

        // FIXME: merge into constructor once legacy init() is removed
        doInit();
    }

    private void doInit() {
        binding = new TransformerGenerator(this, pool);
        registry = new LazyGeneratedCodecRegistry(this, binding, classLoadingStrategy);
        binding.setListener(registry);

        // if (ctx !== null) {
        // listenerRegistration = ctx.registerService(SchemaServiceListener,
        // this, new Hashtable<String, String>());
        // }
    }

    @Deprecated
    public void setPool(final ClassPool pool) {
        this.pool = pool;
    }

    @Override
    public synchronized SchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public synchronized void onGlobalContextUpdated(final SchemaContext context) {
        this.schemaContext = Preconditions.checkNotNull(context);
        this.recreateBindingContext(context);
        this.registry.onGlobalContextUpdated(context);
    }

    @GuardedBy("this")
    private void recreateBindingContext(final SchemaContext schemaContext) {
        BindingGeneratorImpl newBinding = new BindingGeneratorImpl();
        newBinding.generateTypes(schemaContext);

        for (Map.Entry<Module, ModuleContext> entry : newBinding.getModuleContexts().entrySet()) {

            registry.onModuleContextAdded(schemaContext, entry.getKey(), entry.getValue());
            pathToType.putAll(entry.getValue().getChildNodes());
            Module module = entry.getKey();
            ModuleContext context = entry.getValue();
            updateBindingFor(context.getChildNodes(), schemaContext);
            updateBindingFor(context.getCases(), schemaContext);
            String namespace = BindingGeneratorUtil.moduleNamespaceToPackageName(module);

            if (!module.getRpcs().isEmpty()) {
                Set<QName> rpcs = new HashSet<>();
                for (RpcDefinition rpc : module.getRpcs()) {
                    rpcs.add(rpc.getQName());
                }
                Type serviceClass = new ReferencedTypeImpl(namespace, BindingMapping.getClassName(module.getName())
                        + "Service");
                serviceTypeToRpc.put(serviceClass, rpcs);
            }

            Map<SchemaPath, Type> typedefs = context.getTypedefs();
            for (Map.Entry<SchemaPath, Type> typedef : typedefs.entrySet()) {
                Type value = typedef.getValue();
                Type typeRef = new ReferencedTypeImpl(value.getPackageName(), value.getName());
                typeDefinitions.put(typeRef, value);
                TypeDefinition<?> schemaNode = YangSchemaUtils.findTypeDefinition(schemaContext, typedef.getKey());
                if (schemaNode != null) {

                    typeToSchemaNode.put(typeRef, schemaNode);
                } else {
                    LOG.error("Type definition for {} is not available", value);
                }
            }
            List<GeneratedTypeBuilder> augmentations = context.getAugmentations();
            for (GeneratedTypeBuilder augmentation : augmentations) {
                typeToDefinition.put(augmentation, augmentation);
            }
            typeToAugmentation.putAll(context.getTypeToAugmentation());
            for (GeneratedTypeBuilder augmentation : augmentations) {
                updatePromisedSchemas(augmentation);
            }
        }
    }

    @Override
    public CompositeNode toDataDom(final DataObject data) {
        return toCompositeNodeImpl(data);
    }

    @Override
    public Entry<InstanceIdentifier, CompositeNode> toDataDom(
            final Entry<org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject>, DataObject> entry) {
        try {
            org.opendaylight.yangtools.yang.data.api.InstanceIdentifier key = toDataDom(entry.getKey());
            CompositeNode data;
            if (Augmentation.class.isAssignableFrom(entry.getKey().getTargetType())) {
                data = toCompositeNodeImplAugument(key, entry.getValue());
            } else {
                data = toCompositeNodeImpl(key, entry.getValue());
            }
            return new SimpleEntry<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier, CompositeNode>(key,
                    data);

        } catch (Exception e) {
            LOG.error("Error during serialization for {}.", entry.getKey(), e);
            throw e;
        }
    }

    private CompositeNode toCompositeNodeImpl(final DataObject object) {
        Class<? extends DataContainer> cls = object.getImplementedInterface();
        waitForSchema(cls);
        DataContainerCodec<DataObject> codec = (DataContainerCodec<DataObject>) registry.getCodecForDataObject(cls);
        return codec.serialize(new ValueWithQName<DataObject>(null, object));
    }

    private CompositeNode toCompositeNodeImpl(final org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier,
            final DataObject object) {
        PathArgument last = identifier.getPath().get(identifier.getPath().size() - 1);
        Class<? extends DataContainer> cls = object.getImplementedInterface();
        waitForSchema(cls);
        DataContainerCodec<DataObject> codec = (DataContainerCodec<DataObject>) registry.getCodecForDataObject(cls);
        return codec.serialize(new ValueWithQName<DataObject>(last.getNodeType(), object));
    }

    private CompositeNode toCompositeNodeImplAugument(
            final org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier, final DataObject object) {

        // val cls = object.implementedInterface;
        // waitForSchema(cls);
        org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument last = identifier.getPath().get(
                identifier.getPath().size() - 1);
        AugmentationCodec codec = registry.getCodecForAugmentation((Class) object.getImplementedInterface());
        CompositeNode ret = codec.serialize(new ValueWithQName<DataObject>(last.getNodeType(), object));
        if (last instanceof NodeIdentifierWithPredicates) {
            NodeIdentifierWithPredicates predicates = (NodeIdentifierWithPredicates) last;
            List<Node<?>> newNodes = new ArrayList<Node<?>>(predicates.getKeyValues().size());
            for (Map.Entry<QName, Object> predicate : predicates.getKeyValues().entrySet()) {
                newNodes.add(new SimpleNodeTOImpl<Object>(predicate.getKey(), null, predicate.getValue()));
            }
            newNodes.addAll(ret.getChildren());
            return new CompositeNodeTOImpl(last.getNodeType(), null, newNodes);
        }
        return ret;
    }

    @Override
    public void waitForSchema(final Class<?> cls) {
        if (!registry.isCodecAvailable(cls)) {
            final Type ref = Types.typeForClass(cls);
            try {
                getSchemaWithRetry(ref);
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Waiting for schema for class {} failed", cls, e);
                throw new IllegalStateException(String.format("Failed to get schema for %s", cls), e);
            }
        }
    }

    @Override
    public InstanceIdentifier toDataDom(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> path) {
        for (final org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument arg : path.getPathArguments()) {
            this.waitForSchema(arg.getType());
        }

        final InstanceIdentifierCodec c = registry.getInstanceIdentifierCodec();
        Preconditions.checkState(c != null, "InstanceIdentifierCodec not present");
        return c.serialize(path);
    }

    @Override
    public DataObject dataObjectFromDataDom(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> path,
            final CompositeNode domData) throws DeserializationException {
        if (domData == null) {
            return null;
        }

        try {
            final Class<? extends DataContainer> container = path.getTargetType();
            // FIXME: deprecate use without iid
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> wildcardedPath = createWildcarded(path);

            final DataContainerCodec<? extends DataContainer> transformer = registry.getCodecForDataObject(container);
            Preconditions.checkState(transformer != null, "Failed to find codec for type %s", container);

            final ValueWithQName<? extends DataContainer> deserialize = transformer.deserialize(domData, wildcardedPath);
            if (deserialize == null) {
                return null;
            }

            return (DataObject) deserialize.getValue();
        } catch (Exception e) {
            LOG.warn("Failed to deserialize path {} data {}", path, domData);
            throw new DeserializationException("Data deserialization failed", e);
        }
    }

    @Override
    public org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends Object> fromDataDom(final InstanceIdentifier entry) throws DeserializationException {
        try {
            final InstanceIdentifierCodec c = registry.getInstanceIdentifierCodec();
            Preconditions.checkState(c != null, "InstanceIdentifierCodec not present");
            return c.deserialize(entry);
        } catch (Exception e) {
            LOG.warn("Failed to deserialize entry {}", entry);
            throw new DeserializationException("Entry deserialization failed", e);
        }
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return this.registry;
    }

    private void updateBindingFor(final Map<SchemaPath, GeneratedTypeBuilder> map, final SchemaContext module) {
        for (Map.Entry<SchemaPath, GeneratedTypeBuilder> entry : map.entrySet()) {
            SchemaNode schemaNode = SchemaContextUtil.findDataSchemaNode(module, entry.getKey());

            // LOG.info("{} : {}",entry.key,entry.value.fullyQualifiedName)
            Type typeRef = new ReferencedTypeImpl(entry.getValue().getPackageName(), entry.getValue().getName());
            typeToDefinition.put(typeRef, entry.getValue());
            if (schemaNode != null) {
                typeToSchemaNode.put(typeRef, schemaNode);
                updatePromisedSchemas(entry.getValue());
            }
        }
    }

    @Deprecated
    public void init() {
        doInit();
    }

    @Override
    public Set<QName> getRpcQNamesFor(final Class<? extends RpcService> service) {
        Set<QName> serviceRef = serviceTypeToRpc.get(new ReferencedTypeImpl(service.getPackage().getName(), service
                .getSimpleName()));
        if (serviceRef == null) {
            serviceRef = Collections.emptySet();
        }
        return serviceRef;
    }

    private void getSchemaWithRetry(final Type type) throws InterruptedException, ExecutionException {
        final SettableFuture<Type> f;

        synchronized (this) {
            if (typeToDefinition.containsKey(type)) {
                return;
            }

            LOG.info("Thread blocked waiting for schema for: {}", type.getFullyQualifiedName());
            f = SettableFuture.create();
            promisedTypes.put(type, f);
        }

        f.get();
        LOG.info("Schema for {} became available, thread unblocked", type.getFullyQualifiedName());
    }

    @GuardedBy("this")
    private void updatePromisedSchemas(final Type builder) {
        final Type ref = new ReferencedTypeImpl(builder.getPackageName(), builder.getName());
        final Collection<SettableFuture<Type>> futures = promisedTypes.get(ref);

        if (futures != null) {
            for (SettableFuture<Type> future : futures) {
                future.set(builder);
            }
            promisedTypes.removeAll(builder);
        }
    }

    @Override
    public void close() {
        // Nothing to do
    }

    @Override
    public DataContainer dataObjectFromDataDom(final Class<? extends DataContainer> container,
            final CompositeNode domData) {
        // FIXME: Add check for valids inputs
        // which are Notification and Rpc Input / Rpc Output

        org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataContainer> id = org.opendaylight.yangtools.yang.binding.InstanceIdentifier
                .create((Class) container);
        Preconditions.checkNotNull(id, "Failed to create path for type %s", container);

        try {
            return dataObjectFromDataDom(id, domData);
        } catch (DeserializationException e) {
            LOG.warn("Conversion of class {} path {} data {} failed", container, id, domData, e);
            throw new IllegalStateException("Failed to create data object", e);
        }
    }

    @Override
    public synchronized Optional<Class<? extends RpcService>> getRpcServiceClassFor(final String namespace, final String revision) {
        Module module = null;
        if (schemaContext != null) {
            module = schemaContext.findModuleByName(namespace, QName.parseRevision(revision));
        }
        if (module == null) {
            return Optional.absent();
        }
        try {
            Optional<Type> rpcTypeName = getRpcServiceType(module);
            if (rpcTypeName.isPresent()) {
                Class<?> rpcClass = classLoadingStrategy.loadClass(rpcTypeName.get().getFullyQualifiedName());
                return Optional.<Class<? extends RpcService>> of((Class<? extends RpcService>) rpcClass);
            }
        } catch (Exception e) {
            LOG.debug("RPC class not present for {},{}", namespace, revision, e);
        }
        return Optional.absent();
    }

    public Optional<Type> getRpcServiceType(final Module module) {
        String namespace = BindingGeneratorUtil.moduleNamespaceToPackageName(module);
        if (module.getRpcs().isEmpty()) {
            return Optional.<Type> absent();
        }
        return Optional.<Type> of(new ReferencedTypeImpl(namespace, BindingMapping.getClassName(module.getName())
                + BindingMapping.RPC_SERVICE_SUFFIX));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> createWildcarded(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> path) {

        LinkedList<org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument> wildcardedArgs = new LinkedList<>();
        for(org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument pathArg : path.getPathArguments()) {
            if(pathArg instanceof IdentifiableItem<?,?>) {
                pathArg = new Item(pathArg.getType());
            }
            wildcardedArgs.add(pathArg);
        }
        return org.opendaylight.yangtools.yang.binding.InstanceIdentifier.create(wildcardedArgs);
    }

    @Override
    public final AugmentationSchema getAugmentation(final Type type) {
        return typeToAugmentation.get(type);
    }

    @Override
    public final GeneratedTypeBuilder getDefinition(final Type type) {
        return typeToDefinition.get(type);
    }

    @Override
    public final SchemaNode getSchemaNode(final Type type) {
        return typeToSchemaNode.get(type);
    }

    @Override
    public final GeneratedTypeBuilder getTypeBuilder(final SchemaPath path) {
        return pathToType.get(path);
    }
}
