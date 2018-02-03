/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YangTextSnippetTest {
    @Test
    public void testNotification() {
        final SchemaContext schema = YangParserTestUtils.parseYangResource("/bugs/bug2444/yang/notification.yang");

        for (Module module : schema.getModules()) {
            assertTrue(module instanceof ModuleEffectiveStatement);
            final ModuleEffectiveStatement stmt = (ModuleEffectiveStatement) module;

            final String str = YangTextSnippet.of(stmt, stmt.getDeclared()).toString();
            assertNotNull(str);
        }
    }
}
