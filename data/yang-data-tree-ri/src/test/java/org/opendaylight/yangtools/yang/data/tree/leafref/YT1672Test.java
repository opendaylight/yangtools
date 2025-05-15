/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1672Test {
    @Test
    void leafRefCanPointToNonExisting() {
        final var context = YangParserTestUtils.parseYang(
            """
            module leafref-source {
              yang-version 1.1;
              namespace "leafref:source";
              prefix src;
              description "Test model that contains source of leafref for testing openapi";

              grouping leafref-grouping {
                container conts {
                  list cont {
                    key "name";
                    leaf name {
                      type string;
                    }
                  }
                }
              }

              uses leafref-grouping;
            }""",
            """
            module leafref-usage {
              yang-version 1.1;
              namespace "leafref:usage";
              prefix usg;

              import leafref-source {
                prefix src;
              }

              container leafref-cont {
                leaf refleaf {
                  type leafref {
                    path "/src:conts/src:cont/src:name";
                  }
                  description "Leafref to the leaf in different model. Used for testing leafref resolvig of OpenApi.";
                }
              }
            }""",
            """
            module leafref-deviation {
              yang-version 1.1;
              namespace "leafref:deviation";
              prefix dev;

              import leafref-source {
                prefix src;
              }

              deviation "/src:conts" {
                deviate not-supported;
                description
                  "Deviate container that used in leafref other model.";
              }
            }""");

        final var leafrefModule = context.findModule("leafref-usage").orElseThrow();
        final var leafrefQNameModule = leafrefModule.getQNameModule();
        final var cont = QName.create(leafrefQNameModule, "leafref-cont");
        final var leaf = QName.create(leafrefQNameModule, "refleaf");
        final var leafRefPath = Absolute.of(cont, leaf);
        final var leafRefContext = LeafRefContext.create(context);

        assertTrue(leafRefContext.isLeafRef(leafRefPath));
        assertFalse(leafRefContext.hasReferencedChild());

        final var sourceModule = context.findModule("leafref-source").orElseThrow();
        final var conts = QName.create(sourceModule.getQNameModule(), "conts");

        assertEquals(Optional.empty(), sourceModule.findDataChildByName(conts));
    }
}
