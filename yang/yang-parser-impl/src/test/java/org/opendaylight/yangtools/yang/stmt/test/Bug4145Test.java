/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import java.text.ParseException;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import java.net.URI;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.junit.Test;

public class Bug4145Test {
    @Test
    public void bug4145Test() throws SourceException, FileNotFoundException,
            ReactorException, URISyntaxException, ParseException {
        SchemaContext context = StmtTestUtils
                .parseYangSources("/stmt-test/bug-4145");

        assertNotNull(context);

        QNameModule foo = QNameModule.create(new URI("foo"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2015-09-02"));

        SchemaPath targetPath = SchemaPath
                .create(true, QName.create(foo, "root-container"))
                .createChild(QName.create(foo, "node"))
                .createChild(QName.create(foo, "choice-in-grouping"))
                .createChild(QName.create(foo, "two"))
                .createChild(QName.create(foo, "two"));

        SchemaNode targetNode = SchemaContextUtil.findNodeInSchemaContext(
                context, targetPath.getPathFromRoot());
        assertNotNull(targetNode);
        assertTrue(targetNode instanceof LeafSchemaNode);
    }
}
