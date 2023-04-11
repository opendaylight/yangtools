/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingTypeObjectCodecTreeNode;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

class LeafNodeCodecContext extends ValueNodeCodecContext.WithCodec {
    static final class OfTypeObject<T extends TypeObject> extends LeafNodeCodecContext
            implements BindingTypeObjectCodecTreeNode<T> {
        private final @NonNull Class<T> bindingClass;

        OfTypeObject(final LeafSchemaNode schema, final ValueCodec<Object, Object> codec, final String getterName,
                final EffectiveModelContext schemaContext, final Class<T> bindingClass) {
            super(schema, codec, getterName, schemaContext);
            this.bindingClass = requireNonNull(bindingClass);
        }

        @Override
        public Class<T> getBindingClass() {
            return bindingClass;
        }

        @Override
        public T deserialize(final NormalizedNode data) {
            return bindingClass.cast(deserializeObject(data));
        }

        @Override
        public NormalizedNode serialize(final T data) {
            return ImmutableNodes.leafNode(getDomPathArgument(), getValueCodec().serialize(data));
        }
    }

    LeafNodeCodecContext(final LeafSchemaNode schema, final ValueCodec<Object, Object> codec, final String getterName,
            final EffectiveModelContext schemaContext) {
        super(schema, codec, getterName, createDefaultObject(schema, codec, schemaContext));
    }

    static LeafNodeCodecContext of(final LeafSchemaNode schema, final ValueCodec<Object, Object> codec,
            final String getterName, final Class<?> valueType, final EffectiveModelContext schemaContext) {
        return TypeObject.class.isAssignableFrom(valueType)
                ? new OfTypeObject<>(schema, codec, getterName, schemaContext, valueType.asSubclass(TypeObject.class))
                        : new LeafNodeCodecContext(schema, codec, getterName, schemaContext);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode normalizedNode) {
        return normalizedNode != null ? getValueCodec().deserialize(normalizedNode.body()) : null;
    }

    private static Object createDefaultObject(final LeafSchemaNode schema, final ValueCodec<Object, Object> codec,
            final EffectiveModelContext schemaContext) {
        var optDefaultValue = schema.getType().getDefaultValue();
        TypeDefinition<?> type = schema.getType();
        if (optDefaultValue.isPresent()) {
            final var defaultValue = optDefaultValue.orElseThrow();
            if (type instanceof IdentityrefTypeDefinition) {
                return qnameDomValueFromString(codec, schema, (String) defaultValue, schemaContext);
            }
            return domValueFromString(codec, type, defaultValue);
        }

        while (type.getBaseType() != null && type.getDefaultValue().isEmpty()) {
            type = type.getBaseType();
        }

        optDefaultValue = type.getDefaultValue();
        if (optDefaultValue.isPresent()) {
            final var defaultValue = optDefaultValue.orElseThrow();
            if (type instanceof IdentityrefTypeDefinition) {
                return qnameDomValueFromString(codec, schema, (String) defaultValue, schemaContext);
            }
            return domValueFromString(codec, type, defaultValue);
        }
        return null;
    }

    private static Object qnameDomValueFromString(final ValueCodec<Object, Object> codec, final DataSchemaNode schema,
            final String defaultValue, final EffectiveModelContext schemaContext) {
        int prefixEndIndex = defaultValue.indexOf(':');
        QName qname;
        if (prefixEndIndex != -1) {
            String defaultValuePrefix = defaultValue.substring(0, prefixEndIndex);

            Module module = schemaContext.findModule(schema.getQName().getModule()).orElseThrow();
            if (module.getPrefix().equals(defaultValuePrefix)) {
                qname = QName.create(module.getQNameModule(), defaultValue.substring(prefixEndIndex + 1));
                return codec.deserialize(qname);
            }

            for (ModuleImport moduleImport : module.getImports()) {
                if (moduleImport.getPrefix().equals(defaultValuePrefix)) {
                    Module importedModule = schemaContext.findModule(moduleImport.getModuleName().getLocalName(),
                        moduleImport.getRevision()).orElseThrow();
                    qname = QName.create(importedModule.getQNameModule(), defaultValue.substring(prefixEndIndex + 1));
                    return codec.deserialize(qname);
                }
            }
            return null;
        }

        qname = QName.create(schema.getQName(), defaultValue);
        return codec.deserialize(qname);
    }

    private static Object domValueFromString(final ValueCodec<Object, Object> codec, final TypeDefinition<?> type,
            final Object defaultValue) {
        TypeDefinitionAwareCodec<?, ?> typeDefAwareCodec = TypeDefinitionAwareCodec.from(type);
        if (typeDefAwareCodec != null) {
            Object castedDefaultValue = typeDefAwareCodec.deserialize((String) defaultValue);
            return codec.deserialize(castedDefaultValue);
        }
        // FIXME: BUG-4647 Refactor / redesign this to throw hard error, once BUG-4638 is fixed and will provide proper
        //                 getDefaultValue() implementation.
        return null;
    }
}
