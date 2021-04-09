/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.odlext.model.api.OpenDaylightExtensionsStatements;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
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
            .build();
    }

    @AfterClass
    public static void freeReactor() {
        reactor = null;
    }

    @Test
    public void test() throws Exception {
        SchemaContext context = reactor.newBuild()
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/yang-ext.yang")))
            .addSource(YangStatementStreamSource.create(YangTextSchemaSource.forResource("/bug3874.yang")))
            .buildEffective();

        QNameModule foo = QNameModule.create(XMLNamespace.of("foo"));
        QName myContainer2QName = QName.create(foo, "my-container-2");
        QName myAnyXmlDataQName = QName.create(foo, "my-anyxml-data");

        DataSchemaNode dataChildByName = context.findDataChildByName(myAnyXmlDataQName).get();
        assertThat(dataChildByName, instanceOf(AnyxmlSchemaNode.class));
        AnyxmlSchemaNode anyxml = (AnyxmlSchemaNode) dataChildByName;

        SchemaNode myContainer2 = context.findDataTreeChild(myContainer2QName).orElse(null);
        assertThat(myContainer2, instanceOf(ContainerSchemaNode.class));

        Collection<? extends UnknownSchemaNode> unknownSchemaNodes = anyxml.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        UnknownSchemaNode next = unknownSchemaNodes.iterator().next();
        assertThat(next, instanceOf(AnyxmlSchemaLocationEffectiveStatementImpl.class));
        AnyxmlSchemaLocationEffectiveStatementImpl anyxmlSchemaLocationUnknownNode =
            (AnyxmlSchemaLocationEffectiveStatementImpl) next;
        assertEquals(Absolute.of(myContainer2QName), anyxmlSchemaLocationUnknownNode.argument());
        assertEquals(OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION.getStatementName(),
            anyxmlSchemaLocationUnknownNode.getNodeType());
        assertEquals(OpenDaylightExtensionsStatements.ANYXML_SCHEMA_LOCATION.getStatementName(),
            anyxmlSchemaLocationUnknownNode.getQName());
    }
}
