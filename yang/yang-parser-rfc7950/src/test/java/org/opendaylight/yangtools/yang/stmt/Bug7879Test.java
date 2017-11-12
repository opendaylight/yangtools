/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

public class Bug7879Test {
    private static final String NS = "my-model-ns";

    @Test
    public void test() throws Exception {
        final SchemaContext context = TestUtils.parseYangSources("/bugs/bug7879");
        assertNotNull(context);

        assertTrue(findNode(context, qN("my-alarm"), qN("my-content"), qN("my-event-container"))
            instanceof ContainerSchemaNode);
        final SchemaNode myEventValueLeaf = findNode(context, qN("my-alarm"), qN("my-content"), qN("my-event-value"));
        assertTrue(myEventValueLeaf instanceof LeafSchemaNode);
        assertEquals(Optional.of("new description"), myEventValueLeaf.getDescription());
    }

    private static SchemaNode findNode(final SchemaContext context, final QName... qnames) {
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, qnames));
    }

    private static QName qN(final String localName) {
        return QName.create(NS, localName);
    }
}
