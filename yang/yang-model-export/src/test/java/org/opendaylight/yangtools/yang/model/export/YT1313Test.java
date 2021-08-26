/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertNotNull;

import java.net.URI;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1313Test {
    @Test
    public void testSubmoduleImportPrefixes() {
        final ModuleEffectiveStatement bar = YangParserTestUtils.parseYangResourceDirectory("/bugs/yt1313")
            .getModuleStatement(QNameModule.create(URI.create("bar")));

        final StatementPrefixResolver resolver = StatementPrefixResolver.forModule(bar);
        assertNotNull(resolver);
    }
}
