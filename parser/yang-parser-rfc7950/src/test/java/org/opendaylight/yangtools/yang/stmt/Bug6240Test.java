/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug6240Test extends AbstractYangTest {
    private static final String NS = "bar";
    private static final String REV = "2016-07-19";

    @Test
    public void testModels() throws Exception {
        final var context = assertEffectiveModelDir("/bugs/bug6240/correct");

        final var modules = context.getModules();
        assertEquals(2, modules.size());

        Module bar = null;
        for (final Module module : modules) {
            if ("bar".equals(module.getName())) {
                bar = module;
                break;
            }
        }

        assertNotNull(bar);
        assertThat(bar.getDataChildByName(QName.create(NS, REV, "foo-grp-con")), instanceOf(ContainerSchemaNode.class));
        assertThat(bar.getDataChildByName(QName.create(NS, REV, "sub-foo-grp-con")),
            instanceOf(ContainerSchemaNode.class));

        assertEquals(1, bar.getSubmodules().size());

        final DataSchemaNode dataChildByName = bar.getDataChildByName(QName.create(NS, REV, "sub-bar-con"));
        assertThat(dataChildByName, instanceOf(ContainerSchemaNode.class));
        final ContainerSchemaNode subBarCon = (ContainerSchemaNode) dataChildByName;

        assertThat(subBarCon.getDataChildByName(QName.create(NS, REV, "foo-grp-con")),
            instanceOf(ContainerSchemaNode.class));
        assertThat(subBarCon.getDataChildByName(QName.create(NS, REV, "sub-foo-grp-con")),
            instanceOf(ContainerSchemaNode.class));
    }

    @Test
    public void testInvalidModels() {
        final var ex = assertThrows(SomeModifiersUnresolvedException.class,
            () -> StmtTestUtils.parseYangSources("/bugs/bug6240/incorrect"));
        final var cause = ex.getCause();
        assertThat(cause, instanceOf(SourceException.class));
        assertThat(cause.getMessage(), startsWith("Grouping '(bar?revision=2016-07-19)foo-imp-grp' was not resolved."));
    }
}
