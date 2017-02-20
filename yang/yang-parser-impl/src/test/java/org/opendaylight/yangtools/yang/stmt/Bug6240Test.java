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

import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;

public class Bug6240Test {
    private static final String NS = "bar";
    private static final String REV = "2016-07-19";

    @Test
    public void testModels() throws Exception {
        final SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug6240/correct");
        assertNotNull(context);

        final Set<Module> modules = context.getModules();
        assertEquals(2, modules.size());

        Module bar = null;
        for (final Module module : modules) {
            if ("bar".equals(module.getName())) {
                bar = module;
                break;
            }
        }

        assertNotNull(bar);
        assertTrue(bar.getDataChildByName(QName.create(NS, REV, "foo-grp-con")) instanceof ContainerSchemaNode);
        assertTrue(bar.getDataChildByName(QName.create(NS, REV, "sub-foo-grp-con")) instanceof ContainerSchemaNode);

        assertEquals(1, bar.getSubmodules().size());

        final DataSchemaNode dataChildByName = bar.getDataChildByName(QName.create(NS, REV, "sub-bar-con"));
        assertTrue(dataChildByName instanceof ContainerSchemaNode);
        final ContainerSchemaNode subBarCon = (ContainerSchemaNode) dataChildByName;

        assertTrue(subBarCon.getDataChildByName(QName.create(NS, REV, "foo-grp-con")) instanceof ContainerSchemaNode);
        assertTrue(subBarCon.getDataChildByName(QName.create(NS, REV, "sub-foo-grp-con")) instanceof ContainerSchemaNode);
    }

    @Test
    public void testInvalidModels() throws Exception {
        try {
            StmtTestUtils.parseYangSources("/bugs/bug6240/incorrect");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getCause().getMessage().startsWith(
                "Grouping '(bar?revision=2016-07-19)foo-imp-grp' was not resolved."));
        }
    }
}
