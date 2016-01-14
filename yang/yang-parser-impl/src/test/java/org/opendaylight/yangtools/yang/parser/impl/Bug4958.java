/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug4958 {
    private static final String NS = "foo";
    private static final String REV = "2016-02-11";

    @Test
    public void test() throws URISyntaxException {
        URL resourceDir = Bug4958.class.getResource("/bugs/bug4958");
        File testSourcesDir = new File(resourceDir.toURI());
        SchemaContext context = YangParserImpl.getInstance().parseFiles(Arrays.asList(testSourcesDir.listFiles()));
        assertNotNull(context);

        SchemaPath targetPath = SchemaPath.create(true,
                QName.create(NS, REV, "root-grp"),
                QName.create(NS, REV, "sub-grp"),
                QName.create(NS, REV, "con-in-sub-grp"),
                QName.create(NS, REV, "con-in-ext-grp-one"),
                QName.create(NS, REV, "con-in-ext-grp-two"));

        SchemaNode findDataSchemaNode = SchemaContextUtil.findDataSchemaNode(context, targetPath);
        assertTrue(findDataSchemaNode instanceof ContainerSchemaNode);
    }
}
