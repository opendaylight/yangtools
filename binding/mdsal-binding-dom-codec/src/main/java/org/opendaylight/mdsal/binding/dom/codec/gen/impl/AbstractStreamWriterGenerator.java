/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map.Entry;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import org.opendaylight.mdsal.binding.dom.codec.gen.spi.StaticConstantDefinition;
import org.opendaylight.mdsal.binding.dom.codec.util.AugmentableDispatchSerializer;
import org.opendaylight.mdsal.binding.generator.util.BindingRuntimeContext;
import org.opendaylight.mdsal.binding.generator.util.JavassistUtils;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.util.Types;
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
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractStreamWriterGenerator extends AbstractGenerator implements DataObjectSerializerGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractStreamWriterGenerator.class);

    protected static final String SERIALIZE_METHOD_NAME = "serialize";
    protected static final AugmentableDispatchSerializer AUGMENTABLE = new AugmentableDispatchSerializer();
    private static final Field FIELD_MODIFIERS;

    private final LoadingCache<Class<?>, DataObjectSerializerImplementation> implementations;
    private final CtClass[] serializeArguments;
    private final JavassistUtils javassist;
    private BindingRuntimeContext context;

    static {
        /*
         * Cache reflection access to field modifiers field. We need this to set
         * fix the static declared fields to final once we initialize them. If we
         * cannot get access, that's fine, too.
         */
        Field f = null;
        try {
            f = Field.class.getDeclaredField("modifiers");
            f.setAccessible(true);
        } catch (NoSuchFieldException | SecurityException e) {
            LOG.warn("Could not get Field modifiers field, serializers run at decreased efficiency", e);
        }

        FIELD_MODIFIERS = f;
    }

    protected AbstractStreamWriterGenerator(final JavassistUtils utils) {
        this.javassist = Preconditions.checkNotNull(utils,"JavassistUtils instance is required.");
        this.serializeArguments = new CtClass[] {
                javassist.asCtClass(DataObjectSerializerRegistry.class),
                javassist.asCtClass(DataObject.class),
                javassist.asCtClass(BindingStreamEventWriter.class),
        };
        javassist.appendClassLoaderIfMissing(DataObjectSerializerPrototype.class.getClassLoader());
        this.implementations = CacheBuilder.newBuilder().weakKeys().build(new SerializerImplementationLoader());
    }

    @Override
    public final DataObjectSerializerImplementation getSerializer(final Class<?> type) {
        return implementations.getUnchecked(type);
    }

    @Override
    public final void onBindingRuntimeContextUpdated(final BindingRuntimeContext runtime) {
        this.context = runtime;
    }

    @Override
    protected final String loadSerializerFor(final Class<?> cls) {
        return implementations.getUnchecked(cls).getClass().getName();
    }

    private final class SerializerImplementationLoader extends CacheLoader<Class<?>, DataObjectSerializerImplementation> {

        private static final String GETINSTANCE_METHOD_NAME = "getInstance";
        private static final String SERIALIZER_SUFFIX = "$StreamWriter";

        private String getSerializerName(final Class<?> type) {
            return type.getName() + SERIALIZER_SUFFIX;
        }

        @Override
        @SuppressWarnings("unchecked")
        public DataObjectSerializerImplementation load(final Class<?> type) throws Exception {
            Preconditions.checkArgument(BindingReflections.isBindingClass(type));
            Preconditions.checkArgument(DataContainer.class.isAssignableFrom(type),"DataContainer is not assingnable from %s from classloader %s.",type,type.getClassLoader());

            final String serializerName = getSerializerName(type);

            Class<? extends DataObjectSerializerImplementation> cls;
            try {
                cls = (Class<? extends DataObjectSerializerImplementation>) ClassLoaderUtils
                        .loadClass(type.getClassLoader(), serializerName);
            } catch (final ClassNotFoundException e) {
                cls = generateSerializer(type, serializerName);
            }

            final DataObjectSerializerImplementation obj =
                    (DataObjectSerializerImplementation) cls.getDeclaredMethod(GETINSTANCE_METHOD_NAME).invoke(null);
            LOG.debug("Loaded serializer {} for class {}", obj, type);
            return obj;
        }

        private Class<? extends DataObjectSerializerImplementation> generateSerializer(final Class<?> type,
                final String serializerName) throws CannotCompileException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, NoSuchFieldException {
            final DataObjectSerializerSource source = generateEmitterSource(type, serializerName);
            final CtClass poolClass = generateEmitter0(type, source, serializerName);
            @SuppressWarnings("unchecked")
            final Class<? extends DataObjectSerializerImplementation> cls = poolClass.toClass(type.getClassLoader(), type.getProtectionDomain());

            /*
             * Due to OSGi class loader rules we cannot initialize the fields during
             * construction, as the initializer expressions do not see our implementation
             * classes. This should be almost as good as that, as we are resetting the
             * fields to final before ever leaking the class.
             */
            for (final StaticConstantDefinition constant : source.getStaticConstants()) {
                final Field field = cls.getDeclaredField(constant.getName());
                field.setAccessible(true);
                field.set(null, constant.getValue());

                if (FIELD_MODIFIERS != null) {
                    FIELD_MODIFIERS.setInt(field, field.getModifiers() | Modifier.FINAL);
                }
            }

            return cls;
        }
    }

    private DataObjectSerializerSource generateEmitterSource(final Class<?> type, final String serializerName) {
        Types.typeForClass(type);
        javassist.appendClassLoaderIfMissing(type.getClassLoader());
        final Entry<GeneratedType, Object> typeWithSchema = context.getTypeWithSchema(type);
        final GeneratedType generatedType = typeWithSchema.getKey();
        final Object schema = typeWithSchema.getValue();

        final DataObjectSerializerSource source;
        if (schema instanceof ContainerSchemaNode) {
            source = generateContainerSerializer(generatedType, (ContainerSchemaNode) schema);
        } else if (schema instanceof ListSchemaNode){
            final ListSchemaNode casted = (ListSchemaNode) schema;
            if (casted.getKeyDefinition().isEmpty()) {
                source = generateUnkeyedListEntrySerializer(generatedType, casted);
            } else {
                source = generateMapEntrySerializer(generatedType, casted);
            }
        } else if(schema instanceof AugmentationSchema) {
            source = generateSerializer(generatedType,(AugmentationSchema) schema);
        } else if(schema instanceof ChoiceCaseNode) {
            source = generateCaseSerializer(generatedType,(ChoiceCaseNode) schema);
        } else if(schema instanceof NotificationDefinition) {
            source = generateNotificationSerializer(generatedType,(NotificationDefinition) schema);
        } else {
            throw new UnsupportedOperationException("Schema type " + schema.getClass() + " is not supported");
        }
        return source;
    }

    private CtClass generateEmitter0(final Class<?> type, final DataObjectSerializerSource source, final String serializerName) {
        final CtClass product;

        /*
         * getSerializerBody() has side effects, such as loading classes and codecs, it should be run in model class
         * loader in order to correctly reference load child classes.
         *
         * Furthermore the fact that getSerializedBody() can trigger other code generation to happen, we need to take
         * care of this before calling instantiatePrototype(), as that will call our customizer with the lock held,
         * hence any code generation will end up being blocked on the javassist lock.
         */
        final String body = ClassLoaderUtils.withClassLoader(type.getClassLoader(),
            (Supplier<String>) () -> source.getSerializerBody().toString());

        try {
            product = javassist.instantiatePrototype(DataObjectSerializerPrototype.class.getName(), serializerName, cls -> {
                // Generate any static fields
                for (final StaticConstantDefinition def : source.getStaticConstants()) {
                    final CtField field = new CtField(javassist.asCtClass(def.getType()), def.getName(), cls);
                    field.setModifiers(Modifier.PRIVATE + Modifier.STATIC);
                    cls.addField(field);
                }

                // Replace serialize() -- may reference static fields
                final CtMethod serializeTo = cls.getDeclaredMethod(SERIALIZE_METHOD_NAME, serializeArguments);
                serializeTo.setBody(body);

                // The prototype is not visible, so we need to take care of that
                cls.setModifiers(Modifier.setPublic(cls.getModifiers()));
            });
        } catch (final NotFoundException e) {
            LOG.error("Failed to instatiate serializer {}", source, e);
            throw new LinkageError("Unexpected instantation problem: serializer prototype not found", e);
        }
        return product;
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
     * @param schema Schema of augmentation
     * @return Source for augmentation node writer
     */
    protected abstract DataObjectSerializerSource generateSerializer(GeneratedType type, AugmentationSchema schema);

    /**
     * Generates serializer source for notification node,
     * which will read supplied binding type and invoke proper methods
     * on supplied {@link BindingStreamEventWriter}.
     * <p>
     * Implementation is required to recursively invoke events
     * for all reachable binding objects.
     *
     * @param type Binding type of notification
     * @param node Schema of notification
     * @return Source for notification node writer
     */
    protected abstract DataObjectSerializerSource generateNotificationSerializer(GeneratedType type, NotificationDefinition node);

}
