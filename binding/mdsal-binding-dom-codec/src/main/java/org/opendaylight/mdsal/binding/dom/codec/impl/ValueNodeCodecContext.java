/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.dom.codec.impl;

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;

/**
 * Abstract base class for atomic nodes. These are nodes which are not decomposed in the Binding Specification, such
 * as LeafNodes and LeafSetNodes.
 */
// FIXME: MDSAL-436: this class should be specialized for Leaf and LeafSet
final class ValueNodeCodecContext extends NodeCodecContext implements NodeContextSupplier {
    private final NodeIdentifier yangIdentifier;
    private final Codec<Object, Object> valueCodec;
    private final Method getter;
    private final TypedDataSchemaNode schema;
    private final Object defaultObject;

    ValueNodeCodecContext(final TypedDataSchemaNode schema, final Codec<Object, Object> codec,
            final Method getter, final SchemaContext schemaContext) {
        this.yangIdentifier = NodeIdentifier.create(schema.getQName());
        this.valueCodec = requireNonNull(codec);
        this.getter = getter;
        this.schema = requireNonNull(schema);

        this.defaultObject = createDefaultObject(schema, valueCodec, schemaContext);
    }

    private static Object createDefaultObject(final DataSchemaNode schema, final Codec<Object, Object> codec,
                                              final SchemaContext schemaContext) {
        if (schema instanceof LeafSchemaNode) {
            Optional<? extends Object> defaultValue = ((LeafSchemaNode) schema).getType().getDefaultValue();
            TypeDefinition<?> type = ((LeafSchemaNode) schema).getType();
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

    @Override
    protected NodeIdentifier getDomPathArgument() {
        return yangIdentifier;
    }

    protected Codec<Object, Object> getValueCodec() {
        return valueCodec;
    }

    @Override
    public NodeCodecContext get() {
        return this;
    }

    Method getGetter() {
        return getter;
    }

    @Override
    protected Object deserializeObject(final NormalizedNode<?, ?> normalizedNode) {
        if (normalizedNode instanceof LeafNode<?>) {
            return valueCodec.deserialize(normalizedNode.getValue());
        }
        if (normalizedNode instanceof LeafSetNode<?>) {
            @SuppressWarnings("unchecked")
            final Collection<LeafSetEntryNode<Object>> domValues = ((LeafSetNode<Object>) normalizedNode).getValue();
            final List<Object> result = new ArrayList<>(domValues.size());
            for (final LeafSetEntryNode<Object> valueNode : domValues) {
                result.add(valueCodec.deserialize(valueNode.getValue()));
            }
            return result;
        }
        return null;
    }

    @Override
    public TypedDataSchemaNode getSchema() {
        return schema;
    }

    @Override
    Object defaultObject() {
        return defaultObject;
    }
}
