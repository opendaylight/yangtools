/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.dom.codec.api.BindingTypeObjectCodecTreeNode;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.binding.TypeObject;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

class LeafNodeCodecContext extends ValueNodeCodecContext {
    static final class OfTypeObject<T extends TypeObject> extends LeafNodeCodecContext
            implements BindingTypeObjectCodecTreeNode<T> {
        private final @NonNull Class<T> bindingClass;

        OfTypeObject(final LeafSchemaNode schema, final Codec<Object, Object> codec, final Method getter,
                final SchemaContext schemaContext, final Class<T> bindingClass) {
            super(schema, codec, getter, schemaContext);
            this.bindingClass = requireNonNull(bindingClass);
        }

        @Override
        public Class<T> getBindingClass() {
            return bindingClass;
        }

        @Override
        public T deserialize(final NormalizedNode<?, ?> data) {
            return bindingClass.cast(deserializeObject(data));
        }

        @Override
        public NormalizedNode<?, ?> serialize(final T data) {
            return ImmutableNodes.leafNode(getDomPathArgument(), getValueCodec().serialize(data));
        }
    }

    LeafNodeCodecContext(final LeafSchemaNode schema, final Codec<Object, Object> codec,
            final Method getter, final SchemaContext schemaContext) {
        super(schema, codec, getter, createDefaultObject(schema, codec, schemaContext));
    }

    static LeafNodeCodecContext of(final LeafSchemaNode schema, final Codec<Object, Object> codec,
            final Method getter, final Class<?> valueType, final SchemaContext schemaContext) {
        return TypeObject.class.isAssignableFrom(valueType)
                ? new OfTypeObject<>(schema, codec, getter, schemaContext, valueType.asSubclass(TypeObject.class))
                        : new LeafNodeCodecContext(schema, codec, getter, schemaContext);
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        return normalizedNode != null ? getValueCodec().deserialize(normalizedNode.getValue()) : null;
    }

    private static Object createDefaultObject(final LeafSchemaNode schema, final Codec<Object, Object> codec,
                                              final SchemaContext schemaContext) {
        Optional<? extends Object> defaultValue = schema.getType().getDefaultValue();
        TypeDefinition<?> type = schema.getType();
        if (defaultValue.isPresent()) {
            if (type instanceof IdentityrefTypeDefinition) {
                return qnameDomValueFromString(codec, schema, (String) defaultValue.get(), schemaContext);
            }
            return domValueFromString(codec, type, defaultValue.get());
        }

        while (type.getBaseType() != null && !type.getDefaultValue().isPresent()) {
            type = type.getBaseType();
        }

        defaultValue = type.getDefaultValue();
        if (defaultValue.isPresent()) {
            if (type instanceof IdentityrefTypeDefinition) {
                return qnameDomValueFromString(codec, schema, (String) defaultValue.get(), schemaContext);
            }
            return domValueFromString(codec, type, defaultValue);
        }
        return null;
    }

    private static Object qnameDomValueFromString(final Codec<Object, Object> codec, final DataSchemaNode schema,
                                                  final String defaultValue, final SchemaContext schemaContext) {
        int prefixEndIndex = defaultValue.indexOf(':');
        QName qname;
        if (prefixEndIndex != -1) {
            String defaultValuePrefix = defaultValue.substring(0, prefixEndIndex);

            Module module = schemaContext.findModule(schema.getQName().getModule()).get();
            if (module.getPrefix().equals(defaultValuePrefix)) {
                qname = QName.create(module.getQNameModule(), defaultValue.substring(prefixEndIndex + 1));
                return codec.deserialize(qname);
            }

            Set<ModuleImport> imports = module.getImports();
            for (ModuleImport moduleImport : imports) {
                if (moduleImport.getPrefix().equals(defaultValuePrefix)) {
                    Module importedModule = schemaContext.findModule(moduleImport.getModuleName(),
                        moduleImport.getRevision()).get();
                    qname = QName.create(importedModule.getQNameModule(), defaultValue.substring(prefixEndIndex + 1));
                    return codec.deserialize(qname);
                }
            }
            return null;
        }

        qname = QName.create(schema.getQName(), defaultValue);
        return codec.deserialize(qname);
    }

    private static Object domValueFromString(final Codec<Object, Object> codec, final TypeDefinition<?> type,
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
