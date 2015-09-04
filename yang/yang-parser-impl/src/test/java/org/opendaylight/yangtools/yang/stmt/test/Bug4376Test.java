/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug4376Test {

    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException,
            ParseException {
        SchemaContext context = StmtTestUtils.parseYangSources("/stmt-test/bug-4376");

        assertNotNull(context);

        QNameModule foo = QNameModule.create(new URI("foo"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2015-09-02"));

        DataSchemaNode dataChildByName = context.getDataChildByName(QName.create(foo, "foo"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        ContainerSchemaNode containerFoo = (ContainerSchemaNode) dataChildByName;

        List<UnknownSchemaNode> unknownSchemaNodes = containerFoo.getUnknownSchemaNodes();
        assertEquals(1, unknownSchemaNodes.size());

        UnknownSchemaNode unknownNode = unknownSchemaNodes.iterator().next();
        String nodeParameter = unknownNode.getNodeParameter();

        String expectedParameter = "network-status (disable)|(software-interfaces)|(physical)|(logical) ";
        assertEquals(expectedParameter, nodeParameter);
    }

}
