/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;

class YT1443Test extends AbstractYangDataTest {
    @Test
    void buildEffectiveModelTest() throws Exception {
        final var module = REACTOR.newBuild()
            .addSources(IETF_RESTCONF_MODULE, sourceForYangText("""
                module yt1443 {
                  yang-version 1.1;
                  namespace "yt1443";
                  prefix "yt1443";

                  import ietf-restconf { prefix rc; }

                  rc:yang-data support-save-data {
                    anydata support-save-data;
                  }
                }"""))
            .buildEffective()
            .findModuleStatement(QName.create("yt1443", "yt1443"))
            .orElseThrow();
        assertNotNull(module);
    }
}
