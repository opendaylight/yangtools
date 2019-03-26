/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug5518Test {
    @Test
    public void test() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5518");
        assertNotNull(context);

        final DataSchemaNode dataChildByName = context.getDataChildByName(QName.create("foo", "root"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        final ContainerSchemaNode root = (ContainerSchemaNode) dataChildByName;
        final Collection<MustDefinition> mustConstraints = root.getMustConstraints();
        assertEquals(1, mustConstraints.size());
        final MustDefinition must = mustConstraints.iterator().next();
        assertEquals("not(deref(.)/../same-pass)", must.getXpath().getOriginalString());
    }
}
