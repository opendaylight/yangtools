/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.odlext.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;

public class Bug3874ExtensionTest {
    private static CrossSourceStatementReactor reactor;

    @BeforeClass
    public static void createReactor() {
        reactor = RFC7950Reactors.vanillaReactorBuilder()
                .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlSchemaLocationStatementSupport.getInstance())
                .addNamespaceSupport(ModelProcessingPhase.FULL_DECLARATION, AnyxmlSchemaLocationNamespace.BEHAVIOUR)
                .overrideStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                    AnyxmlStatementSupportOverride.getInstance())
                .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void test() throws Exception {
        SchemaContext context = reactor.newBuild()
                .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/bugs/bug3874/foo.yang")))
                .addSource(YangStatementStreamSource.create(
                    YangTextSchemaSource.forResource("/bugs/bug3874/yang-ext.yang")))
                .buildEffective();

        QNameModule foo = QNameModule.create(URI.create("foo"));
        QName myContainer2QName = QName.create(foo, "my-container-2");
        QName myAnyXmlDataQName = QName.create(foo, "my-anyxml-data");

        DataSchemaNode dataChildByName = context.findDataChildByName(myAnyXmlDataQName).get();
        assertTrue(dataChildByName instanceof YangModeledAnyXmlSchemaNode);
        YangModeledAnyXmlSchemaNode yangModeledAnyXml = (YangModeledAnyXmlSchemaNode) dataChildByName;

        SchemaNode myContainer2 = SchemaContextUtil.findDataSchemaNode(context,
            SchemaPath.create(true, myContainer2QName));
        assertTrue(myContainer2 instanceof ContainerSchemaNode);
        assertEquals(myContainer2, yangModeledAnyXml.getSchemaOfAnyXmlData());

        List<UnknownSchemaNode> unknownSchemaNodes = yangModeledAnyXml.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        UnknownSchemaNode next = unknownSchemaNodes.iterator().next();
        assertTrue(next instanceof AnyxmlSchemaLocationEffectiveStatementImpl);
        AnyxmlSchemaLocationEffectiveStatementImpl anyxmlSchemaLocationUnknownNode =
                (AnyxmlSchemaLocationEffectiveStatementImpl) next;
        assertEquals(OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION.getStatementName(),
            anyxmlSchemaLocationUnknownNode.getNodeType());
        assertEquals(OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION.getStatementName(),
            anyxmlSchemaLocationUnknownNode.getQName());
    }
}
