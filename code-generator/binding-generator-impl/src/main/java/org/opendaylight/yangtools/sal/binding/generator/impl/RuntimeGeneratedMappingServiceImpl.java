/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.util.concurrent.SettableFuture;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import javassist.ClassPool;

import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.BindingGeneratorImpl;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.LazyGeneratedCodecRegistry;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.sal.binding.generator.impl.SchemaLock;
import org.opendaylight.yangtools.sal.binding.generator.impl.TransformerGenerator;
import org.opendaylight.yangtools.sal.binding.generator.util.YangSchemaUtils;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.BindingMapping;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
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
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName;
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

public class RuntimeGeneratedMappingServiceImpl implements BindingIndependentMappingService, SchemaContextListener,
        SchemaLock, AutoCloseable, SchemaContextHolder {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeGeneratedMappingServiceImpl.class);

    private ClassPool pool;

    @Extension
    private TransformerGenerator binding;

    @Extension
    private LazyGeneratedCodecRegistry registry;

    private final ConcurrentMap<Type, Type> typeDefinitions = new ConcurrentHashMap<>();
    private final ConcurrentMap<Type, GeneratedTypeBuilder> typeToDefinition = new ConcurrentHashMap<>();
    private final ConcurrentMap<Type, SchemaNode> typeToSchemaNode = new ConcurrentHashMap<>();
    private final ConcurrentMap<Type, Set<QName>> serviceTypeToRpc = new ConcurrentHashMap<>();
    private final HashMultimap<Type, SettableFuture<Type>> promisedTypes = HashMultimap.create();
    private final ClassLoadingStrategy classLoadingStrategy;
    private SchemaContext schemaContext;

    public RuntimeGeneratedMappingServiceImpl() {
        this(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy());
    }

    public RuntimeGeneratedMappingServiceImpl(ClassLoadingStrategy strat) {
        classLoadingStrategy = strat;
    }

    public ClassPool getPool() {
        return this.pool;
    }

    public void setPool(final ClassPool pool) {
        this.pool = pool;
    }

    @Override
    public SchemaContext getSchemaContext() {
        return schemaContext;
    }

    public void setSchemaContext(final SchemaContext schemaContext) {
        this.schemaContext = schemaContext;
    }

    public TransformerGenerator getBinding() {
        return this.binding;
    }

    public void setBinding(final TransformerGenerator binding) {
        this.binding = binding;
    }

    public LazyGeneratedCodecRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(final LazyGeneratedCodecRegistry registry) {
        this.registry = registry;
    }

    public ConcurrentMap<Type, GeneratedTypeBuilder> getTypeToDefinition() {
        return typeToDefinition;
    }

    public ConcurrentMap<Type, Type> getTypeDefinitions() {
        return typeDefinitions;
    }

    public ConcurrentMap<Type, SchemaNode> getTypeToSchemaNode() {
        return typeToSchemaNode;
    }

    public ConcurrentMap<Type, Set<QName>> getServiceTypeToRpc() {
        return serviceTypeToRpc;
    }

    @Override
    public void onGlobalContextUpdated(final SchemaContext arg0) {
        this.setSchemaContext(arg0);
        this.recreateBindingContext(arg0);
        LazyGeneratedCodecRegistry _registry = this.getRegistry();
        _registry.onGlobalContextUpdated(arg0);
    }

    private void recreateBindingContext(SchemaContext schemaContext) {
        BindingGeneratorImpl newBinding = new BindingGeneratorImpl();
        newBinding.generateTypes(schemaContext);

        for (Map.Entry<Module, ModuleContext> entry : newBinding.getModuleContexts().entrySet()) {

            registry.onModuleContextAdded(schemaContext, entry.getKey(), entry.getValue());
            binding.getPathToType().putAll(entry.getValue().getChildNodes());
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
                binding.getTypeDefinitions().put(typeRef, value);
                TypeDefinition<?> schemaNode = YangSchemaUtils.findTypeDefinition(schemaContext, typedef.getKey());
                if (schemaNode != null) {

                    binding.getTypeToSchemaNode().put(typeRef, schemaNode);
                } else {
                    LOG.error("Type definition for {} is not available", value);
                }
            }
            List<GeneratedTypeBuilder> augmentations = context.getAugmentations();
            for (GeneratedTypeBuilder augmentation : augmentations) {
                binding.getTypeToDefinition().put(augmentation, augmentation);
            }
            binding.getTypeToAugmentation().putAll(context.getTypeToAugmentation());
            for (GeneratedTypeBuilder augmentation : augmentations) {
                updatePromisedSchemas(augmentation);
            }
        }
    }

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

    private CompositeNode toCompositeNodeImpl(DataObject object) {
        Class<? extends DataContainer> cls = object.getImplementedInterface();
        waitForSchema(cls);
        DataContainerCodec<DataObject> codec = (DataContainerCodec<DataObject>) registry.getCodecForDataObject(cls);
        return codec.serialize(new ValueWithQName<DataObject>(null, object));
    }

    private CompositeNode toCompositeNodeImpl(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier,
            DataObject object) {
        PathArgument last = identifier.getPath().get(identifier.getPath().size() - 1);
        Class<? extends DataContainer> cls = object.getImplementedInterface();
        waitForSchema(cls);
        DataContainerCodec<DataObject> codec = (DataContainerCodec<DataObject>) registry.getCodecForDataObject(cls);
        return codec.serialize(new ValueWithQName<DataObject>(last.getNodeType(), object));
    }

    private CompositeNode toCompositeNodeImplAugument(
            org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier, DataObject object) {

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
    public void waitForSchema(Class class1) {
        if (registry.isCodecAvailable(class1)) {
            return;
        }
        Type ref = Types.typeForClass(class1);
        getSchemaWithRetry(ref);
    }

    public InstanceIdentifier toDataDom(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> path) {
        for (final org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument arg : path.getPath()) {
            this.waitForSchema(arg.getType());
        }
        return registry.getInstanceIdentifierCodec().serialize(path);
    }

    @Override
    public DataObject dataObjectFromDataDom(
            final org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends DataObject> path,
            final CompositeNode node) {
        return (DataObject) dataObjectFromDataDom(path.getTargetType(), node);
    }

    @Override
    public org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends Object> fromDataDom(
            final InstanceIdentifier entry) {
        try {
            return tryDeserialization(new Callable<org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends Object>>() {
                @Override
                public org.opendaylight.yangtools.yang.binding.InstanceIdentifier<? extends Object> call() {
                    return getRegistry().getInstanceIdentifierCodec().deserialize(entry);
                }
            });
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return this.getRegistry();
    }

    private static <T> T tryDeserialization(Callable<T> deserializationBlock) throws DeserializationException {
        try {
            return deserializationBlock.call();
        } catch (Exception e) {
            // FIXME: Make this block providing more information.
            throw new DeserializationException(e);
        }
    }

    private void updateBindingFor(Map<SchemaPath, GeneratedTypeBuilder> map, SchemaContext module) {
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

    public void init() {
        binding = new TransformerGenerator(pool);
        registry = new LazyGeneratedCodecRegistry(this, classLoadingStrategy);

        registry.setGenerator(binding);
        // binding.staticFieldsInitializer = registry
        binding.setListener(registry);
        binding.setTypeToDefinition(typeToDefinition);
        binding.setTypeToSchemaNode(typeToSchemaNode);
        binding.setTypeDefinitions(typeDefinitions);

        // if (ctx !== null) {
        // listenerRegistration = ctx.registerService(SchemaServiceListener,
        // this, new Hashtable<String, String>());
        // }
    }

    @Override
    public Set<QName> getRpcQNamesFor(Class<? extends RpcService> service) {
        Set<QName> serviceRef = serviceTypeToRpc.get(new ReferencedTypeImpl(service.getPackage().getName(), service
                .getSimpleName()));
        if (serviceRef == null) {
            serviceRef = Collections.emptySet();
        }
        return serviceRef;
    }

    private void getSchemaWithRetry(Type type) {
        try {
            if (typeToDefinition.containsKey(type)) {
                return;
            }
            LOG.info("Thread blocked waiting for schema for: {}", type.getFullyQualifiedName());
            waitForTypeDefinition(type).get();
            LOG.info("Schema for {} became available, thread unblocked", type.getFullyQualifiedName());
        } catch (Throwable t) {
            Exceptions.sneakyThrow(t);
        }
    }

    private Future<Type> waitForTypeDefinition(final Type type) {
        final SettableFuture<Type> future = SettableFuture.<Type> create();
        promisedTypes.put(type, future);
        return future;
    }

    private void updatePromisedSchemas(Type builder) {
        Type ref = new ReferencedTypeImpl(builder.getPackageName(), builder.getName());
        Set<SettableFuture<Type>> futures = promisedTypes.get(ref);
        if (futures == null || futures.isEmpty()) {
            return;
        }
        for (SettableFuture<Type> future : futures) {
            future.set(builder);
        }
        promisedTypes.removeAll(builder);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public DataContainer dataObjectFromDataDom(final Class<? extends DataContainer> container,
            final CompositeNode domData) {
        try {
            return tryDeserialization(new Callable<DataObject>() {
                @Override
                public DataObject call() throws Exception {
                    if (Objects.equal(domData, null)) {
                        return null;
                    }
                    final DataContainerCodec<? extends DataContainer> transformer = getRegistry()
                            .getCodecForDataObject(container);
                    ValueWithQName<? extends DataContainer> deserialize = transformer.deserialize(domData);
                    DataContainer value = null;
                    if (deserialize != null) {
                        value = deserialize.getValue();
                    }
                    return ((DataObject) value);
                }
            });
        } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
        }
    }

    @Override
    public Optional<Class<? extends RpcService>> getRpcServiceClassFor(String namespace, String revision) {
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
                Class<?> rpcClass = binding.getClassLoadingStrategy().loadClass(
                        rpcTypeName.get().getFullyQualifiedName());
                return Optional.<Class<? extends RpcService>> of((Class<? extends RpcService>) rpcClass);
            }
        } catch (Exception e) {
            LOG.debug("RPC class not present for {},{}", namespace, revision, e);
        }
        return Optional.absent();
    }

    public Optional<Type> getRpcServiceType(Module module) {
        String namespace = BindingGeneratorUtil.moduleNamespaceToPackageName(module);
        if (module.getRpcs().isEmpty()) {
            return Optional.<Type> absent();
        }
        return Optional.<Type> of(new ReferencedTypeImpl(namespace, BindingMapping.getClassName(module.getName())
                + BindingMapping.RPC_SERVICE_SUFFIX));
    }

}
