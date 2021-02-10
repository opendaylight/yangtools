/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class YT841Test {
    private static final QNameModule FOO = QNameModule.create(XMLNamespace.of("foo"), Revision.of("2018-01-02"));

    @Test
    public void testFindDataSchemaNode() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/YT841/");
        final Module foo = context.findModule(FOO).get();

        final SchemaNode target = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true,
                QName.create(FOO, "foo"),
                QName.create(FOO, "foo"),
                QName.create(FOO, "foo"),
                QName.create(FOO, "input")));
        assertNotNull(target);

    }
}
