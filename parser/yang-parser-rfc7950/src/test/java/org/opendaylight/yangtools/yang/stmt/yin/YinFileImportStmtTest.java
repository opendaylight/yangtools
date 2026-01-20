/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt.yin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class YinFileImportStmtTest extends AbstractYinModulesTest {
    @Test
    void testImport() {
        final var testModule = context.findModules("ietf-netconf-monitoring").iterator().next();
        assertNotNull(testModule);

        assertThat(testModule.getImports())
            .hasSize(2)
            .allMatch(moduleImport -> switch (moduleImport.getModuleName().getLocalName()) {
                case "ietf-inet-types" -> {
                    assertEquals("inet", moduleImport.getPrefix());
                    yield true;
                }
                case "ietf-yang-types" -> {
                    assertEquals("yang", moduleImport.getPrefix());
                    yield true;
                }
                default -> false;
            });
    }
}
