/*
 * Copyright (c) 2014, 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.gen.impl;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import org.opendaylight.mdsal.binding.dom.codec.util.BindingSchemaMapping;
import org.opendaylight.mdsal.binding.dom.codec.util.ChoiceDispatchSerializer;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.ParameterizedType;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.BindingSerializer;
import org.opendaylight.yangtools.yang.binding.BindingStreamEventWriter;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerImplementation;
import org.opendaylight.yangtools.yang.binding.DataObjectSerializerRegistry;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class DataNodeContainerSerializerSource extends DataObjectSerializerSource {

    private static final Logger LOG = LoggerFactory.getLogger(DataNodeContainerSerializerSource.class);

    protected static final String INPUT = "_input";
    private static final String CHOICE_PREFIX = "CHOICE_";

    protected final DataNodeContainer schemaNode;
    private final GeneratedType dtoType;

    DataNodeContainerSerializerSource(final AbstractGenerator generator, final GeneratedType type,
            final DataNodeContainer node) {
        super(generator);
        this.dtoType = requireNonNull(type);
        this.schemaNode = requireNonNull(node);
    }

    /**
     * Return the character sequence which should be used for start event.
     *
     * @return Start event character sequence
     */
    protected abstract CharSequence emitStartEvent();

    @Override
    protected CharSequence getSerializerBody() {
        final StringBuilder sb = new StringBuilder()
                .append("{\n")
                .append(statement(assign(DataObjectSerializerRegistry.class, REGISTRY, "$1")))
                .append(statement(assign(dtoType, INPUT, cast(dtoType, "$2"))))
                .append(statement(assign(BindingStreamEventWriter.class, STREAM,
                    cast(BindingStreamEventWriter.class, "$3"))))
                .append(statement(assign(BindingSerializer.class, SERIALIZER, null)))
                .append("if (")
                .append(STREAM)
                .append(" instanceof ")
                .append(BindingSerializer.class.getName())
                .append(") {")
                .append(statement(assign(SERIALIZER, cast(BindingSerializer.class, STREAM))))
                .append('}')
                .append(statement(emitStartEvent()));

        emitBody(sb);
        emitAfterBody(sb);

        return sb.append(statement(endNode()))
                .append(statement("return null"))
                .append('}');
    }

    /**
     * Allows for customization of emitting code, which is processed after
     * normal DataNodeContainer body. Ideal for augmentations or others.
     */
    protected void emitAfterBody(final StringBuilder sb) {
        // No-op
    }

    private static Map<String, Type> collectAllProperties(final GeneratedType type, final Map<String, Type> hashMap) {
        for (final MethodSignature definition : type.getMethodDefinitions()) {
            hashMap.put(definition.getName(), definition.getReturnType());
        }
        for (final Type parent : type.getImplements()) {
            if (parent instanceof GeneratedType) {
                collectAllProperties((GeneratedType) parent, hashMap);
            }
        }
        return hashMap;
    }

    private void emitBody(final StringBuilder sb) {
        final Map<String, Type> getterToType = collectAllProperties(dtoType, new HashMap<String, Type>());
        for (final DataSchemaNode schemaChild : schemaNode.getChildNodes()) {
            if (!schemaChild.isAugmenting()) {
                final String getter = BindingSchemaMapping.getGetterMethodName(schemaChild);
                final Type childType = getterToType.get(getter);
                checkState(childType != null, "Unable to find type for child node %s. Expected child nodes: %s",
                        schemaChild.getPath(), getterToType);
                emitChild(sb, getter, childType, schemaChild);
            }
        }
    }

    private void emitChild(final StringBuilder sb, final String getterName, final Type childType,
            final DataSchemaNode schemaChild) {
        sb.append(statement(assign(childType, getterName, cast(childType, invoke(INPUT, getterName)))));

        sb.append("if (").append(getterName).append(" != null) {\n");
        emitChildInner(sb, getterName, childType, schemaChild);
        sb.append("}\n");
    }

    private void emitChildInner(final StringBuilder sb, final String getterName, final Type childType,
            final DataSchemaNode child) {
        if (child instanceof LeafSchemaNode) {
            sb.append(statement(leafNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof AnyXmlSchemaNode) {
            sb.append(statement(anyxmlNode(child.getQName().getLocalName(), getterName)));
        } else if (child instanceof LeafListSchemaNode) {
            final CharSequence startEvent;
            if (((LeafListSchemaNode) child).isUserOrdered()) {
                startEvent = startOrderedLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            } else {
                startEvent = startLeafSet(child.getQName().getLocalName(),invoke(getterName, "size"));
            }
            sb.append(statement(startEvent));
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            sb.append(forEach(getterName, valueType, statement(leafSetEntryNode(CURRENT))));
            sb.append(statement(endNode()));
        } else if (child instanceof ListSchemaNode) {
            final Type valueType = ((ParameterizedType) childType).getActualTypeArguments()[0];
            final ListSchemaNode casted = (ListSchemaNode) child;
            emitList(sb, getterName, valueType, casted);
        } else if (child instanceof ContainerSchemaNode) {
            sb.append(tryToUseCacheElse(getterName, statement(staticInvokeEmitter(childType, getterName))));
        } else if (child instanceof ChoiceSchemaNode) {
            final String propertyName = CHOICE_PREFIX + childType.getName();
            staticConstant(propertyName, DataObjectSerializerImplementation.class,
                ChoiceDispatchSerializer.from(loadClass(childType)));
            sb.append(tryToUseCacheElse(getterName, statement(invoke(propertyName,
                StreamWriterGenerator.SERIALIZE_METHOD_NAME, REGISTRY, cast(DataObject.class, getterName), STREAM))));
        }
    }

    private static StringBuilder tryToUseCacheElse(final String getterName, final CharSequence statement) {
        return new StringBuilder()
                .append("if (").append(SERIALIZER).append(" == null || ")
                .append(invoke(SERIALIZER, "serialize", getterName)).append(" == null) {\n")
                .append(statement)
                .append('}');
    }

    private void emitList(final StringBuilder sb, final String getterName, final Type valueType,
            final ListSchemaNode child) {
        final CharSequence startEvent;

        sb.append(statement(assign("int", "_count", invoke(getterName, "size"))));
        if (child.getKeyDefinition().isEmpty()) {
            startEvent = startUnkeyedList(classReference(valueType), "_count");
        } else if (child.isUserOrdered()) {
            startEvent = startOrderedMapNode(classReference(valueType), "_count");
        } else {
            startEvent = startMapNode(classReference(valueType), "_count");
        }
        sb.append(statement(startEvent));
        sb.append(forEach(getterName, valueType, tryToUseCacheElse(CURRENT, statement(staticInvokeEmitter(valueType,
            CURRENT)))));
        sb.append(statement(endNode()));
    }
}
