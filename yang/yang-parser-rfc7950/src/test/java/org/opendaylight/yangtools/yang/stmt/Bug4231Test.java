/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;

public class Bug4231Test {

    @Test
    public void test() throws Exception {
        EffectiveModelContext context = TestUtils.parseYangSources("/bugs/bug4231");

        assertNotNull(context);

        QNameModule foo = QNameModule.create(new URI("foo"), Revision.of("2015-09-02"));

        final SchemaInferenceStack stack = new SchemaInferenceStack(context);
        stack.enterSchemaTree(QName.create(foo, "augment-target"),
                QName.create(foo, "my-container-in-grouping"),
                QName.create(foo, "l2"));
        SchemaNode targetNode = SchemaContextUtil.findNodeInSchemaContext(
                context, stack.toSchemaNodeIdentifier().getNodeIdentifiers());
        assertNotNull(targetNode);
    }

}
