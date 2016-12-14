/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6880Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void valid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6880/foo.yang");
        assertNotNull(schemaContext);

        final SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(schemaContext,
                SchemaPath.create(true, QName.create(FOO_NS, FOO_REV, "my-leaf-list")));
        assertTrue(findDataSchemaNode instanceof LeafListSchemaNode);
        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) findDataSchemaNode;

        final Collection<String> defaults = myLeafList.getDefaults();
        assertEquals(2, defaults.size());
        assertTrue(defaults.contains("my-default-value-1") && defaults.contains("my-default-value-2"));
    }

    @Test
    public void invalid10Test() throws ReactorException, SourceException, FileNotFoundException, URISyntaxException {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6880/invalid10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("DEFAULT is not valid for LEAF_LIST"));
        }
    }
}