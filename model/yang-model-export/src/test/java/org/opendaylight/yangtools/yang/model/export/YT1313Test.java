/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1313Test {
    @Test
    public void testSubmoduleImportPrefixes() {
        final var bar = YangParserTestUtils.parseYangResourceDirectory("/bugs/yt1313")
            .getModuleStatement(QNameModule.create(XMLNamespace.of("bar")));

        final StatementPrefixResolver resolver = StatementPrefixResolver.forModule(bar);
        assertNotNull(resolver);
    }
}
