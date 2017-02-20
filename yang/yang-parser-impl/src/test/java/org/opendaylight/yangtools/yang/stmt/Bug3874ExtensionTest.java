/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.SupportedExtensionsMapping;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.AnyxmlSchemaLocationEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.YangModeledAnyXmlEffectiveStatementImpl;

public class Bug3874ExtensionTest {

    @Test
    public void test() throws Exception {
            SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug3874");

            Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("1970-01-01");
            QNameModule foo = QNameModule.create(new URI("foo"), revision);
            QName myContainer2QName = QName.create(foo, "my-container-2");
            QName myAnyXmlDataQName = QName.create(foo, "my-anyxml-data");

            DataSchemaNode dataChildByName = context.getDataChildByName(myAnyXmlDataQName);
            assertTrue(dataChildByName instanceof YangModeledAnyXmlEffectiveStatementImpl);
            YangModeledAnyXmlEffectiveStatementImpl yangModeledAnyXml = (YangModeledAnyXmlEffectiveStatementImpl) dataChildByName;

            SchemaNode myContainer2 = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, myContainer2QName));
            assertTrue(myContainer2 instanceof ContainerSchemaNode);
            assertEquals(myContainer2, yangModeledAnyXml.getSchemaOfAnyXmlData());

            List<UnknownSchemaNode> unknownSchemaNodes = yangModeledAnyXml.getUnknownSchemaNodes();
            assertEquals(1, unknownSchemaNodes.size());

            UnknownSchemaNode next = unknownSchemaNodes.iterator().next();
            assertTrue(next instanceof AnyxmlSchemaLocationEffectiveStatementImpl);
            AnyxmlSchemaLocationEffectiveStatementImpl anyxmlSchemaLocationUnknownNode= (AnyxmlSchemaLocationEffectiveStatementImpl) next;
            assertEquals(SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION.getStatementName(), anyxmlSchemaLocationUnknownNode.getNodeType());
            assertEquals(SupportedExtensionsMapping.ANYXML_SCHEMA_LOCATION.getStatementName(), anyxmlSchemaLocationUnknownNode.getQName());
    }
}
