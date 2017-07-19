/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec.xml;

import com.google.common.base.Preconditions;
import java.net.URI;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.util.AbstractStringInstanceIdentifierCodec;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.w3c.dom.Element;

@Deprecated
final class ElementInstanceIdentifierParser extends AbstractStringInstanceIdentifierCodec {
    private final SchemaContext schema;
    private final Element element;
    private final DataSchemaContextTree dataContextTree;

    ElementInstanceIdentifierParser(final SchemaContext schema, final Element element) {
        this.element = Preconditions.checkNotNull(element);
        this.schema = Preconditions.checkNotNull(schema);
        this.dataContextTree = DataSchemaContextTree.from(schema);
    }

    @Override
    protected Object deserializeKeyValue(final DataSchemaNode schemaNode, final String value) {
        Preconditions.checkNotNull(schemaNode, "schemaNode cannot be null");
        Preconditions.checkArgument(schemaNode instanceof LeafSchemaNode, "schemaNode must be of type LeafSchemaNode");
        TypeDefinition<?> originalType = XmlUtils.resolveBaseTypeFrom(((LeafSchemaNode) schemaNode).getType());
        if (originalType instanceof IdentityrefTypeDefinition) {
            return new ElementIdentityrefParser(schema, element).deserialize(value);
        } else if (originalType instanceof LeafrefTypeDefinition) {
            originalType = SchemaContextUtil.getBaseTypeForLeafRef((LeafrefTypeDefinition) originalType, schema,
                    schemaNode);
        }
        final TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> keyCodec =
                XmlUtils.DEFAULT_XML_CODEC_PROVIDER.codecFor(originalType);
        Preconditions.checkState(keyCodec != null, String.format("Cannot find codec for type '%s'.", originalType));
        return keyCodec.deserialize(value);
    }

    @Override
    protected String prefixForNamespace(@Nonnull final URI namespace) {
        return element.lookupPrefix(namespace.toString());
    }

    @Override
    protected QName createQName(@Nonnull final String prefix, @Nonnull final String localName) {
        final String namespace = element.lookupNamespaceURI(prefix);
        Preconditions.checkArgument(namespace != null, "Failed to lookup prefix %s", prefix);

        final URI ns = URI.create(namespace);
        final Module module = schema.findModuleByNamespaceAndRevision(ns, null);
        Preconditions.checkArgument(module != null, "Namespace %s is not owned by a module", ns);
        return QName.create(module.getQNameModule(), localName);
    }

    @Nonnull
    @Override
    protected DataSchemaContextTree getDataContextTree() {
        return dataContextTree;
    }

}
