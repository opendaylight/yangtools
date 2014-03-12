/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl

import com.google.common.base.Optional
import com.google.common.collect.FluentIterable
import com.google.common.collect.HashMultimap
import com.google.common.util.concurrent.SettableFuture
import java.util.AbstractMap.SimpleEntry
import java.util.ArrayList
import java.util.Collections
import java.util.Map
import java.util.Map.Entry
import java.util.Set
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.Future
import javassist.ClassPool
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl
import org.opendaylight.yangtools.binding.generator.util.Types
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy
import org.opendaylight.yangtools.sal.binding.generator.util.YangSchemaUtils
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType
import org.opendaylight.yangtools.sal.binding.model.api.Type
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder
import org.opendaylight.yangtools.yang.binding.Augmentation
import org.opendaylight.yangtools.yang.binding.BindingMapping
import org.opendaylight.yangtools.yang.binding.DataContainer
import org.opendaylight.yangtools.yang.binding.DataObject
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier
import org.opendaylight.yangtools.yang.binding.RpcService
import org.opendaylight.yangtools.yang.common.QName
import org.opendaylight.yangtools.yang.data.api.CompositeNode
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates
import org.opendaylight.yangtools.yang.data.api.Node
import org.opendaylight.yangtools.yang.data.impl.CompositeNodeTOImpl
import org.opendaylight.yangtools.yang.data.impl.SimpleNodeTOImpl
import org.opendaylight.yangtools.yang.data.impl.codec.AugmentationCodec
import org.opendaylight.yangtools.yang.data.impl.codec.BindingIndependentMappingService
import org.opendaylight.yangtools.yang.data.impl.codec.DataContainerCodec
import org.opendaylight.yangtools.yang.data.impl.codec.DeserializationException
import org.opendaylight.yangtools.yang.data.impl.codec.ValueWithQName
import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.yang.model.api.SchemaContext
import org.opendaylight.yangtools.yang.model.api.SchemaContextHolder
import org.opendaylight.yangtools.yang.model.api.SchemaContextListener
import org.opendaylight.yangtools.yang.model.api.SchemaNode
import org.opendaylight.yangtools.yang.model.api.SchemaPath
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil
import org.slf4j.LoggerFactory

class RuntimeGeneratedMappingServiceImpl implements BindingIndependentMappingService, SchemaContextListener,
SchemaLock, AutoCloseable, SchemaContextHolder {

    @Property
    ClassPool pool;

    private static val LOG = LoggerFactory.getLogger(RuntimeGeneratedMappingServiceImpl);

    @Property
    extension TransformerGenerator binding;

    @Property
    extension LazyGeneratedCodecRegistry registry;

    @Property
    val ConcurrentMap<Type, Type> typeDefinitions = new ConcurrentHashMap();

    @Property
    val ConcurrentMap<Type, GeneratedTypeBuilder> typeToDefinition = new ConcurrentHashMap();

    @Property
    val ConcurrentMap<Type, SchemaNode> typeToSchemaNode = new ConcurrentHashMap();

    @Property
    val ConcurrentMap<Type, Set<QName>> serviceTypeToRpc = new ConcurrentHashMap();

    val promisedTypes = HashMultimap.<Type, SettableFuture<Type>>create;

    val ClassLoadingStrategy classLoadingStrategy;

    @Property
    var SchemaContext schemaContext;

    new() {
        this(GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy())
    }

    new(ClassLoadingStrategy strat){
        classLoadingStrategy = strat
    }

    //ServiceRegistration<SchemaServiceListener> listenerRegistration
    override onGlobalContextUpdated(SchemaContext arg0) {
        schemaContext = arg0
        recreateBindingContext(arg0);
        registry.onGlobalContextUpdated(arg0);
    }

    def recreateBindingContext(SchemaContext schemaContext) {
        val newBinding = new BindingGeneratorImpl();
        newBinding.generateTypes(schemaContext);

        for (entry : newBinding.moduleContexts.entrySet) {

            registry.onModuleContextAdded(schemaContext, entry.key, entry.value);
            binding.pathToType.putAll(entry.value.childNodes)
            val module = entry.key;
            val context = entry.value;
            updateBindingFor(context.childNodes, schemaContext);
            updateBindingFor(context.cases, schemaContext);
            val namespace = BindingGeneratorUtil.moduleNamespaceToPackageName(module);

            if (!module.rpcs.empty) {
                val rpcs = FluentIterable.from(module.rpcs).transform[QName].toSet
                val serviceClass = new ReferencedTypeImpl(namespace,
                    BindingGeneratorUtil.parseToClassName(module.name) + "Service");
                serviceTypeToRpc.put(serviceClass, rpcs);
            }

            val typedefs = context.typedefs;
            for (typedef : typedefs.entrySet) {
                val typeRef = new ReferencedTypeImpl(typedef.value.packageName, typedef.value.name)
                binding.typeDefinitions.put(typeRef, typedef.value as GeneratedType);
                val schemaNode = YangSchemaUtils.findTypeDefinition(schemaContext, typedef.key);
                if (schemaNode != null) {

                    binding.typeToSchemaNode.put(typeRef, schemaNode);
                } else {
                    LOG.error("Type definition for {} is not available", typedef.value);
                }

            }
            val augmentations = context.augmentations;
            for (augmentation : augmentations) {
                binding.typeToDefinition.put(augmentation, augmentation);
            }
            binding.typeToAugmentation.putAll(context.typeToAugmentation);
            for (augmentation : augmentations) {
                updatePromisedSchemas(augmentation);
            }
        }
    }

    override CompositeNode toDataDom(DataObject data) {
        toCompositeNodeImpl(data);
    }

    override Entry<org.opendaylight.yangtools.yang.data.api.InstanceIdentifier, CompositeNode> toDataDom(
        Entry<InstanceIdentifier<? extends DataObject>, DataObject> entry) {

        try {
            val key = toDataDom(entry.key)
            var CompositeNode data;
            if (Augmentation.isAssignableFrom(entry.key.targetType)) {
                data = toCompositeNodeImplAugument(key, entry.value);
            } else {
                data = toCompositeNodeImpl(key, entry.value);
            }
            return new SimpleEntry(key, data);

        } catch (Exception e) {
            LOG.error("Error during serialization for {}.", entry.key, e);
            throw e;
        }
    }

    private def CompositeNode toCompositeNodeImpl(DataObject object) {
        val cls = object.implementedInterface;
        waitForSchema(cls);
        val codec = registry.getCodecForDataObject(cls) as DataContainerCodec<DataObject>;
        val ret = codec.serialize(new ValueWithQName(null, object));
        return ret as CompositeNode;
    }

    private def CompositeNode toCompositeNodeImpl(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier,
        DataObject object) {
        val last = identifier.path.last;
        val cls = object.implementedInterface;
        waitForSchema(cls);
        val codec = registry.getCodecForDataObject(cls) as DataContainerCodec<DataObject>;
        val ret = codec.serialize(new ValueWithQName(last.nodeType, object));
        return ret as CompositeNode;
    }

    private def CompositeNode toCompositeNodeImplAugument(
        org.opendaylight.yangtools.yang.data.api.InstanceIdentifier identifier, DataObject object) {

        //val cls = object.implementedInterface;
        //waitForSchema(cls);
        val last = identifier.path.last;
        val codec = registry.getCodecForAugmentation(object.implementedInterface as Class) as AugmentationCodec;
        val ret = codec.serialize(new ValueWithQName(last.nodeType, object));
        if (last instanceof NodeIdentifierWithPredicates) {
            val predicates = last as NodeIdentifierWithPredicates;
            val newNodes = new ArrayList<Node<?>>(predicates.keyValues.size);
            for (predicate : predicates.keyValues.entrySet) {
                newNodes.add(new SimpleNodeTOImpl(predicate.key, null, predicate.value));
            }
            newNodes.addAll(ret.children);
            return new CompositeNodeTOImpl(last.nodeType, null, newNodes);
        }
        return ret as CompositeNode;
    }

    override waitForSchema(Class class1) {

        if (registry.isCodecAvailable(class1)) {
            return;
        }
        val ref = Types.typeForClass(class1);
        getSchemaWithRetry(ref);
    }

    override org.opendaylight.yangtools.yang.data.api.InstanceIdentifier toDataDom(
        InstanceIdentifier<? extends DataObject> path) {
        for (arg : path.path) {
            waitForSchema(arg.type);
        }
        return registry.instanceIdentifierCodec.serialize(path);
    }

    override dataObjectFromDataDom(InstanceIdentifier<? extends DataObject> path, CompositeNode node) {
        dataObjectFromDataDom(path.targetType, node) as DataObject;
    }

    override fromDataDom(org.opendaylight.yangtools.yang.data.api.InstanceIdentifier entry) {
        return tryDeserialization[ |
            registry.instanceIdentifierCodec.deserialize(entry);
        ]
    }

    override getCodecRegistry() {
        return getRegistry();
    }

    private static def <T> T tryDeserialization(Callable<T> deserializationBlock) throws DeserializationException {
        try {
            deserializationBlock.call()
        } catch (Exception e) {

            // FIXME: Make this block providing more information.
            throw new DeserializationException(e);
        }
    }

    private def void updateBindingFor(Map<SchemaPath, GeneratedTypeBuilder> map, SchemaContext module) {

        for (entry : map.entrySet) {
            val schemaNode = SchemaContextUtil.findDataSchemaNode(module, entry.key);

            //LOG.info("{} : {}",entry.key,entry.value.fullyQualifiedName)
            val typeRef = new ReferencedTypeImpl(entry.value.packageName, entry.value.name)
            typeToDefinition.put(typeRef, entry.value);
            if (schemaNode != null) {
                typeToSchemaNode.put(typeRef, schemaNode);
                updatePromisedSchemas(entry.value);
            }

        }
    }

    public def void init() {
        binding = new TransformerGenerator(pool);
        registry = new LazyGeneratedCodecRegistry(this, classLoadingStrategy)

        registry.generator = binding

        //binding.staticFieldsInitializer = registry
        binding.listener = registry
        binding.typeToDefinition = typeToDefinition
        binding.typeToSchemaNode = typeToSchemaNode
        binding.typeDefinitions = typeDefinitions

    //        if (ctx !== null) {
    //            listenerRegistration = ctx.registerService(SchemaServiceListener, this, new Hashtable<String, String>());
    //        }
    }

    override getRpcQNamesFor(Class<? extends RpcService> service) {
        var serviceRef = serviceTypeToRpc.get(new ReferencedTypeImpl(service.package.name, service.simpleName))
        if (serviceRef == null) {
            serviceRef = Collections.emptySet()
        }
        return serviceRef
    }

    private def void getSchemaWithRetry(Type type) {
        if (typeToDefinition.containsKey(type)) {
            return;
        }
        LOG.info("Thread blocked waiting for schema for: {}", type.fullyQualifiedName)
        type.waitForTypeDefinition.get();
        LOG.info("Schema for {} became available, thread unblocked", type.fullyQualifiedName)
    }

    private def Future<Type> waitForTypeDefinition(Type type) {
        val future = SettableFuture.<Type>create()
        promisedTypes.put(type, future);
        return future;
    }

    private def void updatePromisedSchemas(Type builder) {
        val ref = new ReferencedTypeImpl(builder.packageName, builder.name);
        val futures = promisedTypes.get(ref);
        if (futures === null || futures.empty) {
            return;
        }
        for (future : futures) {
            future.set(builder);
        }
        promisedTypes.removeAll(builder);
    }

    override close() throws Exception {
        //listenerRegistration?.unregister();
    }

    override dataObjectFromDataDom(Class<? extends DataContainer> container, CompositeNode domData) {
        return tryDeserialization[ |
            if (domData == null) {
                return null;
            }
            val transformer = registry.getCodecForDataObject(container);
            val ret = transformer.deserialize(domData)?.value as DataObject;
            return ret;
        ]
    }

    override getRpcServiceClassFor(String namespace, String revision) {
        val module = schemaContext?.findModuleByName(namespace.toString, QName.parseRevision(revision));
        if (module == null) {
            return Optional.absent();
        }
        try {
            val rpcTypeName = module.rpcServiceType;
            if (rpcTypeName.present) {
                val rpcClass = binding.classLoadingStrategy.loadClass(rpcTypeName.get.fullyQualifiedName);
                return Optional.of(rpcClass as Class<? extends RpcService>);
            }
        } catch (Exception e) {
            LOG.debug("RPC class not present for {},{}", namespace, revision, e);
        }
        return Optional.absent()
    }

    def Optional<Type> getRpcServiceType(Module module) {
        val namespace = BindingGeneratorUtil.moduleNamespaceToPackageName(module);
        if (module.rpcs.empty) {
            return Optional.<Type>absent();
        }
        return Optional.<Type>of(
            new ReferencedTypeImpl(namespace,
                BindingMapping.getClassName(module.name) + BindingMapping.RPC_SERVICE_SUFFIX));
    }
}
