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

import org.opendaylight.yangtools.binding.data.codec.gen.spi.StaticConstantDefinition;
import org.opendaylight.yangtools.binding.data.codec.util.AugmentableDispatchSerializer;
import org.opendaylight.yangtools.binding.generator.util.Types;
import org.opendaylight.yangtools.sal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.yangtools.sal.binding.generator.util.ClassCustomizer;
import org.opendaylight.yangtools.sal.binding.generator.util.JavassistUtils;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
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

abstract class AbstractStreamWriterGenerator extends AbstractGenerator implements DataObjectSerializerGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamWriterGenerator.class);

    protected static final String SERIALIZE_METHOD_NAME = "serialize";
    protected static final AugmentableDispatchSerializer AUGMENTABLE = new AugmentableDispatchSerializer();

    private final LoadingCache<Class<?>, Class<? extends DataObjectSerializerImplementation>> implementations;
    private final CtClass[] serializeArguments;
    private final JavassistUtils javassist;
    private BindingRuntimeContext context;

    protected AbstractStreamWriterGenerator(final JavassistUtils utils) {
        super();
        this.javassist = Preconditions.checkNotNull(utils,"JavassistUtils instance is required.");
        this.serializeArguments = new CtClass[] {
                javassist.asCtClass(DataObjectSerializerRegistry.class),
                javassist.asCtClass(DataObject.class),
                javassist.asCtClass(BindingStreamEventWriter.class),
        };

        this.implementations = CacheBuilder.newBuilder().weakKeys().build(new SerializerImplementationLoader());
    }

    @Override
    public final DataObjectSerializerImplementation getSerializer(final Class<?> type) {
        try {
            return implementations.getUnchecked(type).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public final void onBindingRuntimeContextUpdated(final BindingRuntimeContext runtime) {
        this.context = runtime;
    }

    @Override
    protected final String loadSerializerFor(final Class<?> cls) {
        return implementations.getUnchecked(cls).getName();
    }

    private final class SerializerImplementationLoader extends CacheLoader<Class<?>, Class<? extends DataObjectSerializerImplementation>> {
        private static final String SERIALIZER_SUFFIX = "$StreamWriter";

        private String getSerializerName(final Class<?> type) {
            return type.getName() + SERIALIZER_SUFFIX;
        }

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
            final DataObjectSerializerSource source = generateEmitterSource(type, serializerName);
            final CtClass poolClass = generateEmitter0(source, serializerName);
            @SuppressWarnings("unchecked")
            final Class<? extends DataObjectSerializerImplementation> cls = poolClass.toClass(type.getClassLoader(), type.getProtectionDomain());

            initStaticConstants(cls,source);
            return cls;
        }
    }

    private DataObjectSerializerSource generateEmitterSource(final Class<?> type, final String serializerName) {
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
        final CtClass product;
        try {
            product = javassist.instantiatePrototype(DataObjectSerializerPrototype.class.getName(), serializerName, new ClassCustomizer() {
                @Override
                public void customizeClass(final CtClass cls) throws CannotCompileException, NotFoundException {
                    // Generate any static fields
                    for (StaticConstantDefinition def : source.getStaticConstants()) {
                        CtField field = new CtField(javassist.asCtClass(def.getType()), def.getName(), cls);
                        field.setModifiers(Modifier.PUBLIC + Modifier.STATIC);
                        cls.addField(field);
                    }

                    // Replace serialize()
                    final String body = source.getSerializerBody().toString();
                    final CtMethod serializeTo = cls.getDeclaredMethod(SERIALIZE_METHOD_NAME, serializeArguments);
                    serializeTo.setBody(body);
                }
            });
        } catch (NotFoundException e) {
            LOG.error("Failed to instatiate serializer {}", source, e);
            throw new LinkageError("Unexpected instantation problem: prototype not found", e);
        }
        return product;
    }

    private static void initStaticConstants(final Class<? extends DataObjectSerializerImplementation> cls, final DataObjectSerializerSource source) {
        for(StaticConstantDefinition constant : source.getStaticConstants()) {
            try {
                Field field = cls.getDeclaredField(constant.getName());
                field.set(null, constant.getValue());
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
                throw new IllegalStateException("Could not initialize expected constant",e);
            }
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
    protected abstract DataObjectSerializerSource generateContainerSerializer(GeneratedType type, ContainerSchemaNode node);

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
    protected abstract DataObjectSerializerSource generateCaseSerializer(GeneratedType type, ChoiceCaseNode node);

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
    protected abstract DataObjectSerializerSource generateMapEntrySerializer(GeneratedType type, ListSchemaNode node);

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
    protected abstract DataObjectSerializerSource generateUnkeyedListEntrySerializer(GeneratedType type, ListSchemaNode node);

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
    protected abstract DataObjectSerializerSource generateSerializer(GeneratedType type, AugmentationSchema schema);

}
