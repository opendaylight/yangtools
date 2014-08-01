/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.gen.impl;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.lang.reflect.Field;
import java.util.Map.Entry;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

import org.opendaylight.yangtools.binding.data.codec.gen.spi.AbstractSource;
import org.opendaylight.yangtools.binding.data.codec.gen.spi.StaticConstantDefinition;
import org.opendaylight.yangtools.binding.data.codec.util.AugmentableDispatchSerializer;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.api.ClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.impl.GeneratedClassLoadingStrategy;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.ClassGenerator;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.util.ClassLoaderUtils;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataContainer;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.binding.util.BindingReflections;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractStreamWriterGenerator {

    protected static final String SERIALIZER_SUFFIX = "$StreamWriter";
    protected static final String STATIC_SERIALIZE_METHOD_NAME = "staticSerialize";
    protected static final String SERIALIZE_METHOD_NAME = "serialize";

    protected static final AugmentableDispatchSerializer AUGMENTABLE = new AugmentableDispatchSerializer();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamWriterGenerator.class);

    private final JavassistUtils javassist;
    private final CtClass serializerCt;
    private final CtClass dataObjectCt;
    private final CtClass writerCt;
    private final CtClass voidCt;
    private final CtClass registryCt;

    private final CtClass[] serializeArguments;
    private final CtMethod serializeToMethod;

    private final LoadingCache<Class<?>, Class<? extends DataObjectSerializerImplementation>> implementations;
    private final ClassLoadingStrategy strategy;

    private BindingRuntimeContext context;

    protected AbstractStreamWriterGenerator(final JavassistUtils utils) {
        super();
        this.javassist = Preconditions.checkNotNull(utils,"JavassistUtils instance is required.");
        this.serializerCt = javassist.asCtClass(DataObjectSerializerImplementation.class);
        this.registryCt = javassist.asCtClass(DataObjectSerializerRegistry.class);
        this.writerCt = javassist.asCtClass(BindingStreamEventWriter.class);
        this.dataObjectCt = javassist.asCtClass(DataObject.class);
        this.voidCt = javassist.asCtClass(Void.class);
        this.serializeArguments =  new CtClass[] { registryCt,dataObjectCt, writerCt };

        try {
            this.serializeToMethod = serializerCt.getDeclaredMethod(SERIALIZE_METHOD_NAME, serializeArguments);
        } catch (NotFoundException e) {
            throw new IllegalStateException("Required method " + SERIALIZE_METHOD_NAME + "was not found in class " + BindingStreamEventWriter.class ,e);
        }
        this.implementations = CacheBuilder.newBuilder().weakKeys().build(new SerializerImplementationLoader());
        strategy = GeneratedClassLoadingStrategy.getTCCLClassLoadingStrategy();
    }

    private final class SerializerImplementationLoader extends
            CacheLoader<Class<?>, Class<? extends DataObjectSerializerImplementation>> {

        @Override
        public Class<? extends DataObjectSerializerImplementation> load(final Class<?> type) throws Exception {
            Preconditions.checkArgument(BindingReflections.isBindingClass(type));
            Preconditions.checkArgument(DataContainer.class.isAssignableFrom(type));

            String serializerName = getSerializerName(type);
            try {
                @SuppressWarnings("unchecked")
                final Class<? extends DataObjectSerializerImplementation> preexisting = (Class<? extends DataObjectSerializerImplementation>) ClassLoaderUtils
                        .loadClass(type.getClassLoader(), serializerName);
                return preexisting;
            } catch (ClassNotFoundException e) {
                return loadFromClassPoolOrGenerate(type, serializerName);
            }
        }

        private Class<? extends DataObjectSerializerImplementation> loadFromClassPoolOrGenerate(final Class<?> type,
                final String serializerName) throws CannotCompileException {
            CtClass poolClass;
            DataObjectSerializerSource source = generateEmitterSource(type, serializerName);
            poolClass = generateEmitter0(source, serializerName);
            @SuppressWarnings("unchecked")
            Class<? extends DataObjectSerializerImplementation> cls = poolClass.toClass(type.getClassLoader(), type.getProtectionDomain());
            initStaticConstants(cls,source);
            return cls;
        }

        private void initStaticConstants(final Class<? extends DataObjectSerializerImplementation> cls, final DataObjectSerializerSource source) {
            for(StaticConstantDefinition constant : source.getStaticConstants()) {
                try {
                    Field field = cls.getDeclaredField(constant.getName());
                    field.set(null, constant.getValue());
                } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    throw new IllegalStateException("Could not initialize expected constant",e);
                }
            }
        }
    }



    protected DataObjectSerializerSource generateEmitterSource(final Class<?> type, final String serializerName) {
        Types.typeForClass(type);
        Entry<GeneratedType, Object> typeWithSchema = context.getTypeWithSchema(type);
        GeneratedType generatedType = typeWithSchema.getKey();
        Object schema = typeWithSchema.getValue();

        final DataObjectSerializerSource source;
        if (schema instanceof ContainerSchemaNode) {
            source = generateContainerSerializer(generatedType, (ContainerSchemaNode) schema);
        } else if (schema instanceof ListSchemaNode){
            ListSchemaNode casted = (ListSchemaNode) schema;
            if (casted.getKeyDefinition().isEmpty()) {
                source = generateUnkeyedListEntrySerializer(generatedType, casted);
            } else {
                source = generateMapEntrySerializer(generatedType, casted);
            }
        } else if(schema instanceof AugmentationSchema) {
            source = generateSerializer(generatedType,(AugmentationSchema) schema);
        } else if(schema instanceof ChoiceCaseNode) {
            source = generateCaseSerializer(generatedType,(ChoiceCaseNode) schema);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }
        return source;
    }

    private CtClass generateEmitter0(final DataObjectSerializerSource source, final String serializerName) {
        CtClass product = javassist.createClass(serializerName, serializerCt, new ClassGenerator() {

            @Override
            public void process(final CtClass cls) {
                final String staticBody = source.getStaticSerializeBody().toString();
                try {

                    for(StaticConstantDefinition def : source.getStaticConstants()) {
                        CtField field = new CtField(javassist.asCtClass(def.getType()), def.getName(), cls);
                        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
                        cls.addField(field);
                    }

                    CtMethod staticSerializeTo = new CtMethod(voidCt, STATIC_SERIALIZE_METHOD_NAME, serializeArguments, cls);
                    staticSerializeTo.setModifiers(Modifier.PUBLIC + Modifier.FINAL + Modifier.STATIC);
                    staticSerializeTo.setBody(staticBody);
                    cls.addMethod(staticSerializeTo);


                    CtMethod serializeTo = new CtMethod(serializeToMethod,cls,null);
                    serializeTo.setModifiers(Modifier.PUBLIC + Modifier.FINAL);
                    serializeTo.setBody(
                            new StringBuilder().append('{')
                            .append(STATIC_SERIALIZE_METHOD_NAME).append("($$);\n")
                            .append("return null;")
                            .append('}')
                            .toString()
                            );
                    cls.addMethod(serializeTo);
                } catch (CannotCompileException e) {
                    LOG.error("Can not compile body of codec for {}.",serializerName,e);
                    throw new IllegalStateException(e);
                }

            }
        });
        return product;
    }

    @SuppressWarnings("unchecked")
    protected Class<? extends DataContainer> loadClass(final Type childType) {
        try {
            return (Class<? extends DataContainer>) strategy.loadClass(childType);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load referenced class ",e);
        }
    }

    public DataObjectSerializerImplementation getSerializer(final Class<?> type) {
        try {
            return implementations.getUnchecked(type).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates serializer source code for supplied container node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of container
     * @param node Schema of container
     * @return Source for container node writer
     */
    abstract DataObjectSerializerSource generateContainerSerializer(GeneratedType type, ContainerSchemaNode node);

    /**
     * Generates serializer source for supplied case node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of case
     * @param node Schema of case
     * @return Source for case node writer
     */
    abstract DataObjectSerializerSource generateCaseSerializer(GeneratedType type, ChoiceCaseNode node);

    /**
     * Generates serializer source for supplied list node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of list
     * @param node Schema of list
     * @return Source for list node writer
     */
    abstract DataObjectSerializerSource generateMapEntrySerializer(GeneratedType type, ListSchemaNode node);

    /**
     * Generates serializer source for supplied list node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of list
     * @param node Schema of list
     * @return Source for list node writer
     */
    abstract DataObjectSerializerSource generateUnkeyedListEntrySerializer(GeneratedType type, ListSchemaNode node);

    /**
     * Generates serializer source for supplied augmentation node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of augmentation
     * @param node Schema of augmentation
     * @return Source for augmentation node writer
     */
    abstract DataObjectSerializerSource generateSerializer(GeneratedType type, AugmentationSchema schema);

    protected static String getSerializerName(final Class<?> type) {
        return type.getName() + SERIALIZER_SUFFIX;
    }

    protected abstract class DataObjectSerializerSource extends AbstractSource {

        protected static final String STREAM = "_stream";
        protected static final String ITERATOR = "_iterator";
        protected static final String CURRENT = "_current";
        protected static final String REGISTRY = "_registry";

        /**
         * Returns body of static serialize method.
         *
         * <ul>
         * <li> {@link DataObjectSerializerRegistry} - registry of serializers
         * <li> {@link DataObject} - object to be serialized
         * <li> {@link BindingStreamEventWriter} - writer to which events should be serialized.
         * </ul>
         *
         * @return Valid javassist code describing static serialization body.
         */
        protected abstract CharSequence getStaticSerializeBody();

        protected final CharSequence leafNode(final String localName, final CharSequence value) {
            return invoke(STREAM, "leafNode", escape(localName), value);
        }

        protected final CharSequence startLeafSet(final String localName,final CharSequence expected) {
            return invoke(STREAM, "startLeafSet", escape(localName),expected);
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

        protected final CharSequence startUnkeyedListItem(final CharSequence expected) {
            return invoke(STREAM, "startUnkeyedListItem",expected);
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
                String className = implementations.getUnchecked(cls).getName();
                return invoke(className, STATIC_SERIALIZE_METHOD_NAME,REGISTRY, name,STREAM);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void onBindingRuntimeContextUpdated(final BindingRuntimeContext runtime) {
        this.context = runtime;
    }

}
