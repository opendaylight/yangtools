/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
@ThreadSafe
public final class XmlCodecFactory {

    private static final Logger LOG = LoggerFactory.getLogger(XmlCodecFactory.class);
    private static final XmlCodec<Object> NULL_CODEC = new XmlCodec<Object>() {
        @Override
        public Object deserialize(final String input) {
            return null;
        }

        @Override
        public String serialize(final Object input) {
            return null;
        }

        @Override
        public void serializeToWriter(final XMLStreamWriter writer, final Object value) throws XMLStreamException {
            // NOOP since codec is unkwown.
            LOG.warn("Call of the serializeToWriter method on XmlCodecFactory.NULL_CODEC object. No operation " +
                    "performed.");
        }
    };

    private final LoadingCache<Entry<DataSchemaNode, NamespaceContext>, XmlCodec<?>> codecs =
            CacheBuilder.newBuilder().softValues().build(
                    new CacheLoader<Entry<DataSchemaNode, NamespaceContext>, XmlCodec<?>>() {
                @Override
                public XmlCodec<?> load(@Nonnull final Entry<DataSchemaNode, NamespaceContext> schemaNodeAndNamespaceCtxPair)
                        throws Exception {
                    final DataSchemaNode schemaNode = schemaNodeAndNamespaceCtxPair.getKey();
                    final TypeDefinition<?> type;
                    if (schemaNode instanceof LeafSchemaNode) {
                        type = ((LeafSchemaNode) schemaNode).getType();
                    } else if (schemaNode instanceof LeafListSchemaNode) {
                        type = ((LeafListSchemaNode) schemaNode).getType();
                    } else {
                        throw new IllegalArgumentException("Not supported node type " + schemaNode.getClass().getName());
                    }
                    return createCodec(schemaNode,type, schemaNodeAndNamespaceCtxPair.getValue());
                }
            });

    private final SchemaContext schemaContext;

    private XmlCodecFactory(final SchemaContext context) {
        this.schemaContext = Preconditions.checkNotNull(context);
    }

    /**
     * Instantiate a new codec factory attached to a particular context.
     *
     * @param context SchemaContext to which the factory should be bound
     * @return A codec factory instance.
     */
    public static XmlCodecFactory create(final SchemaContext context) {
        return new XmlCodecFactory(context);
    }

    private XmlCodec<?> createCodec(final DataSchemaNode key, final TypeDefinition<?> type,
                                    final NamespaceContext namespaceContext) {
        if (type instanceof LeafrefTypeDefinition) {
            return createReferencedTypeCodec(key, (LeafrefTypeDefinition) type, namespaceContext);
        } else if (type instanceof IdentityrefTypeDefinition) {
            return createIdentityrefTypeCodec(key, namespaceContext);
        } else if (type instanceof UnionTypeDefinition) {
            return createUnionTypeCodec(key, (UnionTypeDefinition)type, namespaceContext);
        }
        return createFromSimpleType(key, type, namespaceContext);
    }

    private XmlCodec<?> createReferencedTypeCodec(final DataSchemaNode schema, final LeafrefTypeDefinition type,
                                                  final NamespaceContext namespaceContext) {
        // FIXME: Verify if this does indeed support leafref of leafref
        final TypeDefinition<?> referencedType =
                SchemaContextUtil.getBaseTypeForLeafRef(type, getSchemaContext(), schema);
        Verify.verifyNotNull(referencedType, "Unable to find base type for leafref node '%s'.", schema.getPath());
        return createCodec(schema, referencedType, namespaceContext);
    }

    public XmlCodec<QName> createIdentityrefTypeCodec(final DataSchemaNode schema,
                                                      final NamespaceContext namespaceContext) {
        final XmlCodec<QName> xmlStringIdentityrefCodec =
                new XmlStringIdentityrefCodec(getSchemaContext(), schema.getQName().getModule(), namespaceContext);
        return xmlStringIdentityrefCodec;
    }

    private XmlCodec<Object> createUnionTypeCodec(final DataSchemaNode schema, final UnionTypeDefinition type,
                                                  final NamespaceContext namespaceContext) {
        final XmlCodec<Object> xmlStringUnionCodec = new XmlStringUnionCodec(schema, type, this, namespaceContext);
        return xmlStringUnionCodec;
    }

    private XmlCodec<?> createFromSimpleType(
        final DataSchemaNode schema, final TypeDefinition<?> type,
        final NamespaceContext namespaceContext) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
            final XmlCodec<YangInstanceIdentifier> iidCodec = new XmlStringInstanceIdentifierCodec(schemaContext, this,
                    namespaceContext);
            return iidCodec;
        }
        if (type instanceof EmptyTypeDefinition) {
            return XmlEmptyCodec.INSTANCE;
        }

        final TypeDefinitionAwareCodec<Object, ?> codec = TypeDefinitionAwareCodec.from(type);
        if (codec == null) {
            LOG.debug("Codec for type \"{}\" is not implemented yet.", type.getQName().getLocalName());
            return NULL_CODEC;
        }
        return AbstractXmlCodec.create(codec);
    }

    SchemaContext getSchemaContext() {
        return schemaContext;
    }

    XmlCodec<?> codecFor(final DataSchemaNode schema, final NamespaceContext namespaceContext) {
        return codecs.getUnchecked(new SimpleImmutableEntry<>(schema, namespaceContext));
    }

    XmlCodec<?> codecFor(final DataSchemaNode schema, final TypeDefinition<?> unionSubType,
                         final NamespaceContext namespaceContext) {
        return createCodec(schema, unionSubType, namespaceContext);
    }
}
