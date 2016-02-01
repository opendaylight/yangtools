/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.transform.dom.serializer;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlCodecProvider;
import org.opendaylight.yangtools.yang.data.impl.codec.xml.XmlDocumentUtils;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangInferencePipeline;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.YangStatementSourceImpl;
import org.w3c.dom.Document;

public class DomSerializerTest {
    private static final YangStatementSourceImpl source = new YangStatementSourceImpl("/dom-serializer-test/first.yang",
            false);
    private static final Document doc = XmlDocumentUtils.getDocument();
    private static final XmlCodecProvider codecProvider = new XmlCodecProvider() {
        @Override
        public TypeDefinitionAwareCodec<Object, ? extends TypeDefinition<?>> codecFor(TypeDefinition<?> baseType) {
            return TypeDefinitionAwareCodec.from(baseType);
        }
    };

    @Test
    public void LeafNodeDomSerializerTest() throws ReactorException, IOException, YangSyntaxErrorException {
        SchemaContext schemaContext = createTestContext();
        assertNotNull("Schema context must not be null.", schemaContext);

        LeafNodeDomSerializer temp = new LeafNodeDomSerializer(doc, codecProvider);
    }

    public static SchemaContext createTestContext() throws IOException, YangSyntaxErrorException, ReactorException {
        CrossSourceStatementReactor.BuildAction reactor = YangInferencePipeline.RFC6020_REACTOR.newBuild();
        reactor.addSources(source);
        return reactor.buildEffective();
    }
}