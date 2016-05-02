/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.xml;

import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
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
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.type.DerivedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final LoadingCache<DataSchemaNode, XmlCodec<?>> codecs =
            CacheBuilder.newBuilder().softValues().build(new CacheLoader<DataSchemaNode, XmlCodec<?>>() {
                @Override
                public XmlCodec<?> load(final DataSchemaNode key) throws Exception {
                    final TypeDefinition<?> type;
                    if (key instanceof LeafSchemaNode) {
                        type = ((LeafSchemaNode) key).getType();
                    } else if (key instanceof LeafListSchemaNode) {
                        type = ((LeafListSchemaNode) key).getType();
                    } else {
                        throw new IllegalArgumentException("Not supported node type " + key.getClass().getName());
                    }
                    return createCodec(key,type);
                }
            });

    private final SchemaContext schemaContext;
    private final XmlCodec<YangInstanceIdentifier> iidCodec;

    private XmlCodecFactory(final SchemaContext context) {
        this.schemaContext = Preconditions.checkNotNull(context);
        iidCodec = new XmlStringInstanceIdentifierCodec(context, this);
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

    private XmlCodec<?> createCodec(final DataSchemaNode key, final TypeDefinition<?> type) {
        final TypeDefinition<?> normalizedType = DerivedTypes.derivedTypeBuilder(type, type.getPath()).build();
        if (normalizedType instanceof LeafrefTypeDefinition) {
            return createReferencedTypeCodec(key, (LeafrefTypeDefinition) normalizedType);
        } else if (normalizedType instanceof IdentityrefTypeDefinition) {
            final XmlCodec<?> xmlStringIdentityrefCodec =
                    new XmlStringIdentityrefCodec(schemaContext, key.getQName().getModule());
            return xmlStringIdentityrefCodec;
        }
        return createFromSimpleType(normalizedType);
    }

    private XmlCodec<?> createReferencedTypeCodec(final DataSchemaNode schema, final LeafrefTypeDefinition type) {
        // FIXME: Verify if this does indeed support leafref of leafref
        final TypeDefinition<?> referencedType =
                SchemaContextUtil.getBaseTypeForLeafRef(type, getSchemaContext(), schema);
        Verify.verifyNotNull(referencedType, "Unable to find base type for leafref node '%s'.", schema.getPath());
        return createCodec(schema, referencedType);
    }

    private XmlCodec<?> createFromSimpleType(final TypeDefinition<?> type) {
        if (type instanceof InstanceIdentifierTypeDefinition) {
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

    XmlCodec<?> codecFor(final DataSchemaNode schema) {
        return codecs.getUnchecked(schema);
    }
}
