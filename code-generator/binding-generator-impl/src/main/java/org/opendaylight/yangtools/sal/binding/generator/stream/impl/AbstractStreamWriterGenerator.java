package org.opendaylight.yangtools.sal.binding.generator.stream.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.lang.reflect.Field;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.opendaylight.yangtools.binding.generator.util.ReferencedTypeImpl;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.ModuleContext;
import org.opendaylight.yangtools.sal.binding.generator.stream.api.StaticCodecBinder;
import org.opendaylight.yangtools.sal.binding.generator.stream.api.StaticCodecBinder.NoopCodec;
import org.opendaylight.yangtools.sal.binding.generator.stream.api.StaticCodecBinder.StaticCodec;
import org.opendaylight.yangtools.sal.binding.generator.stream.api.StaticCodecBinder.StaticMethodCodec;
import org.opendaylight.yangtools.sal.binding.generator.util.ClassGenerator;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.binding.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStreamWriterGenerator {

    protected static final String EMITTER_SUFFIX = "$StreamWriter";
    protected static final String STATIC_EMIT_METHOD_NAME = "staticSerialize";
    protected static final String EMIT_METHOD_NAME = "serialize";

    protected static final AugmentableDispatchSerializer AUGMENTABLE = new AugmentableDispatchSerializer();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamWriterGenerator.class);

    private final JavassistUtils javassist;
    private final CtClass emitterCt;
    private final CtClass dataObjectCt;
    private final CtClass writerCt;
    private final CtClass voidCt;
    private final CtClass registryCt;

    private final CtClass[] serializeArguments;
    private final CtMethod emitToMethod;

    private final BiMap<Type, Object> typeToSchema = HashBiMap.create();
    private final LoadingCache<Class<?>, Class<? extends DataObjectSerializerImplementation>> emitterImplementations;
    private final ClassLoadingStrategy strategy;
    private final StaticCodecBinder binder;


    public AbstractStreamWriterGenerator(final ClassPool pool,final StaticCodecBinder binder) {
        super();
        this.javassist = JavassistUtils.forClassPool(pool);
        this.emitterCt = javassist.asCtClass(DataObjectSerializerImplementation.class);
        this.registryCt = javassist.asCtClass(DataObjectSerializerRegistry.class);
        this.writerCt = javassist.asCtClass(BindingStreamEventWriter.class);
        this.dataObjectCt = javassist.asCtClass(DataObject.class);
        this.voidCt = javassist.asCtClass(Void.class);
        this.serializeArguments =  new CtClass[] { registryCt,dataObjectCt, writerCt };
        this.binder = binder;

        try {
            this.emitToMethod = emitterCt.getDeclaredMethod(EMIT_METHOD_NAME, serializeArguments);
        } catch (NotFoundException e) {
            throw new IllegalStateException(e);
        }
        this.emitterImplementations = CacheBuilder.newBuilder().weakKeys().build(new EmitterImplementationLoader());
        strategy = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
    }

    private final class EmitterImplementationLoader extends
            CacheLoader<Class<?>, Class<? extends DataObjectSerializerImplementation>> {

        @Override
        public Class<? extends DataObjectSerializerImplementation> load(final Class<?> type) throws Exception {
            Preconditions.checkArgument(BindingReflections.isBindingClass(type));
            Preconditions.checkArgument(DataContainer.class.isAssignableFrom(type));

            String emitterName = getEmitterName(type);
            try {
                @SuppressWarnings("unchecked")
                final Class<? extends DataObjectSerializerImplementation> preexisting = (Class<? extends DataObjectSerializerImplementation>) ClassLoaderUtils
                        .loadClass(type.getClassLoader(), emitterName);
                return preexisting;
            } catch (ClassNotFoundException e) {
                return loadFromClassPoolOrGenerate(type, emitterName);
            }
        }

        private Class<? extends DataObjectSerializerImplementation> loadFromClassPoolOrGenerate(final Class<?> type,
                final String emitterName) throws CannotCompileException {
            CtClass poolClass;

            DataObjectSerializerSource source = generateEmitterSource(type, emitterName);
            poolClass = generateEmitter0(source, emitterName);
            @SuppressWarnings("unchecked")
            Class<? extends DataObjectSerializerImplementation> cls = poolClass.toClass(type.getClassLoader(), type.getProtectionDomain());
            initStaticConstants(cls,source);
            return cls;
        }

        private void initStaticConstants(final Class<? extends DataObjectSerializerImplementation> cls, final DataObjectSerializerSource source) {
            for(StaticConstantDefinition constant :  source.getStaticConstants()) {
                try {
                    Field field = cls.getDeclaredField(constant.name);
                    field.set(null, constant.value);
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException("Could not initialize expected constant",e);
                }

            }
        }
    }

    public void onModuleContextAdded(final Module module, final ModuleContext ctx) {
        typeToSchema.putAll(ctx.getTypeToSchema());
        ctx.getTypeToAugmentation();
    }

    protected DataObjectSerializerSource generateEmitterSource(final Class<?> type, final String emitterName) {
        Types.typeForClass(type);
        Entry<GeneratedType, Object> typeWithSchema = getTypeWithSchema(type);
        GeneratedType generatedType = typeWithSchema.getKey();
        Object schema = typeWithSchema.getValue();

        final DataObjectSerializerSource source;
        if (schema instanceof ContainerSchemaNode) {
            source = generateContainerSerializer(generatedType, (ContainerSchemaNode) schema);
        } else if (schema instanceof ListSchemaNode){
            ListSchemaNode casted = (ListSchemaNode) schema;
            if(casted.getKeyDefinition().isEmpty()) {
                source = generateUnkeyedListEntrySerializer(generatedType, casted);
            } else {
                source = generateMapEntrySerializer(generatedType, casted);
            }
        } else if(schema instanceof AugmentationSchema) {
            source = generateSerializer(generatedType,(AugmentationSchema) schema);
        } else if(schema instanceof ChoiceCaseNode){

            source = generateCaseSerializer(generatedType,(ChoiceCaseNode) schema);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + "is not supported");
        }
        return source;
    }

    private CtClass generateEmitter0(final DataObjectSerializerSource source, final String emitterName) {
        CtClass product = javassist.createClass(emitterName, emitterCt, new ClassGenerator() {

            @Override
            public void process(final CtClass cls) {


                String staticBody = source.getStaticEmmitBody().toString();
                try {

                    for(StaticConstantDefinition def : source.getStaticConstants()) {
                        CtField field = new CtField(javassist.asCtClass(def.type), def.name, cls);
                        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
                        cls.addField(field);
                    }

                    CtMethod staticEmitTo = new CtMethod(voidCt, STATIC_EMIT_METHOD_NAME, serializeArguments, cls);
                    staticEmitTo.setModifiers(Modifier.PUBLIC + Modifier.FINAL + Modifier.STATIC);
                    staticEmitTo.setBody(staticBody);
                    cls.addMethod(staticEmitTo);


                    CtMethod emitTo = new CtMethod(emitToMethod,cls,null);
                    emitTo.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
                    emitTo.setBody(
                            new StringBuilder().append("{")
                            .append(STATIC_EMIT_METHOD_NAME).append("($$);\n")
                            .append("return null;")
                            .append("}")
                            .toString()
                            );
                    cls.addMethod(emitTo);
                } catch (CannotCompileException e) {
                    LOG.error("Can not compile body of codec for {}.",emitterName,e);
                    throw new IllegalStateException(e);
                }

            }
        });
        return product;
    }

    public DataObjectSerializerImplementation getSerializer(final Class<?> type) {
        try {
            return emitterImplementations.getUnchecked(type).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    public Entry<GeneratedType, Object> getTypeWithSchema(final Class<?> type) {
        Object schema = typeToSchema.get(new ReferencedTypeImpl(type.getPackage().getName(), type.getSimpleName()));
        Type definedType = typeToSchema.inverse().get(schema);
        Preconditions.checkNotNull(schema);
        Preconditions.checkNotNull(definedType);

        return new SimpleEntry(((GeneratedTypeBuilder) definedType).toInstance(), schema);
    }

    abstract DataObjectSerializerSource generateContainerSerializer(GeneratedType type, ContainerSchemaNode node);

    abstract DataObjectSerializerSource generateCaseSerializer(GeneratedType type, ChoiceCaseNode node);

    abstract DataObjectSerializerSource generateMapEntrySerializer(GeneratedType type, ListSchemaNode node);

    abstract DataObjectSerializerSource generateUnkeyedListEntrySerializer(GeneratedType type, ListSchemaNode node);

    abstract DataObjectSerializerSource generateSerializer(GeneratedType type, AugmentationSchema schema);



    protected static String getEmitterName(final Class<?> type) {
        return type.getName() + EMITTER_SUFFIX;
    }

    protected abstract class DataObjectSerializerSource extends AbstractSource {

        protected static final String STREAM = "_stream";
        protected static final String ITERATOR = "_iterator";
        protected static final String CURRENT = "_current";
        protected static final String REGISTRY = "_registry";


        private CharSequence getEmitBody() {
            return getStaticEmmitBody();
        }



        protected abstract CharSequence getStaticEmmitBody();



        protected final CharSequence serialize(final Type type, final CharSequence value) {
            StaticCodec s = binder.getStaticSerializer(type);
            if(NoopCodec.INSTANCE.equals(s)) {
                return value;
            } else if(s instanceof StaticMethodCodec) {
                StaticMethodCodec sb = (StaticMethodCodec) s;
                return invoke(sb.getType().getFullyQualifiedName(), sb.getMethodName(), value);
            }
            throw new UnsupportedOperationException("Unsupported static codec type " + s);
        }

        protected final CharSequence leafNode(final String localName, final CharSequence value) {
            return invoke(STREAM, "leafNode", escape(localName), value);
        }

        protected final CharSequence startLeafSet(final String localName) {
            return invoke(STREAM, "startLeafSet", escape(localName));
        }

        protected final CharSequence leafSetEntryNode(final CharSequence value) {
            return invoke(STREAM, "leafSetEntryNode", value);

        }

        protected final CharSequence startContainerNode(final CharSequence type, final CharSequence expected) {
            return invoke(STREAM, "startContainerNode", (type),expected);
        }

        protected final  CharSequence escape(final String localName) {
            return '"'+localName+'"';
        }

        protected final CharSequence startUnkeyedList(final CharSequence type, final CharSequence expected) {
            return invoke(STREAM, "startUnkeyedList", (type),expected);
        }

        protected final CharSequence startUnkeyedListItem() {
            return invoke(STREAM, "startUnkeyedListItem");
        }

        protected final CharSequence startMapNode(final CharSequence type,final CharSequence expected) {
            return invoke(STREAM, "startMapNode", (type),expected);
        }

        protected final CharSequence startOrderedMapNode(final CharSequence type,final CharSequence expected) {
            return invoke(STREAM, "startOrderedMapNode", (type),expected);
        }

        protected final CharSequence startMapEntryNode(final CharSequence key, final CharSequence expected) {
            return invoke(STREAM,"startMapEntryNode",key,expected);

        }

        protected final CharSequence startAugmentationNode(final CharSequence key) {
            return invoke(STREAM,"startAugmentationNode",key);

        }

        protected final CharSequence startChoiceNode(final CharSequence localName,final CharSequence expected) {
            return invoke(STREAM, "startChoiceNode", (localName),expected);
        }

        protected final CharSequence startCaseNode(final CharSequence localName,final CharSequence expected) {
            return invoke(STREAM, "startCase", (localName),expected);
        }


        protected final CharSequence anyxmlNode(final String name, final String value) throws IllegalArgumentException {
            return invoke(STREAM,"anyxmlNode",escape(name),name);
        }

        protected final CharSequence endNode() {
            return invoke(STREAM, "endNode");
        }

        protected final CharSequence forEach(final String iterable,final Type valueType,final CharSequence body) {
            return forEach(iterable,ITERATOR,valueType.getFullyQualifiedName(),CURRENT,body);
        }

        protected final CharSequence classReference(final Type type) {
            return new StringBuilder().append(type.getFullyQualifiedName()).append(".class");
        }

        protected final CharSequence staticInvokeEmitter(final Type childType, final String name) {
            Class<?> cls;
            try {
                cls = strategy.loadClass(childType);
                String className = emitterImplementations.getUnchecked(cls).getName();
                return invoke(className, STATIC_EMIT_METHOD_NAME,REGISTRY, name,STREAM);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
