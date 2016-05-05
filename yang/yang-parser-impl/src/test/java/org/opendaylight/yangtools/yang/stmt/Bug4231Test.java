/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug4231Test {

    @Test
    public void test() throws IOException, URISyntaxException, ParseException, ReactorException {
        SchemaContext context = TestUtils.parseYangSources("/bugs/bug4231");

        assertNotNull(context);

        QNameModule foo = QNameModule.create(new URI("foo"),
                SimpleDateFormatUtil.getRevisionFormat().parse("2015-09-02"));

        SchemaPath targetPath = SchemaPath
                .create(true, QName.create(foo, "augment-target"))
                .createChild(QName.create(foo, "my-container-in-grouping"))
                .createChild(QName.create(foo, "l2"));

        SchemaNode targetNode = SchemaContextUtil.findNodeInSchemaContext(
                context, targetPath.getPathFromRoot());
        assertNotNull(targetNode);
    }

}
