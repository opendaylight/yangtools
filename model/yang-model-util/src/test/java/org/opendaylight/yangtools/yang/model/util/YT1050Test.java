/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT1050Test {
    private static final QName SECONDARY = QName.create("yt1050", "secondary");
    private static final QName TYPE = QName.create(SECONDARY, "type");
    private static final QName GRP_USES = QName.create(SECONDARY, "grp-uses");

    private EffectiveModelContext context;
    private LeafSchemaNode secondaryType;
    private LeafSchemaNode primaryType;
    private Module module;

    @BeforeEach
    void before() {
        context = YangParserTestUtils.parseYang("""
            module yt1050 {
              yang-version 1.1;
              namespace "yt1050";
              prefix "yt1050";
              identity target-base;
              typedef target-type {
                type identityref {
                  base target-base;
                }
              }
              grouping grp {
                leaf id {
                  type string;
                }
                leaf type {
                  type target-type;
                }
                list secondary {
                  key "id type";
                  leaf id {
                    type leafref {
                      path "/grp-uses/id";
                    }
                  }
                  leaf type {
                    type leafref {
                      path "deref(../id)/../type";
                    }
                  }
                }
              }
              list grp-uses {
                uses grp;
                key "id type";
              }
            }""");
        module = context.getModules().iterator().next();

        final var grpUses = assertInstanceOf(ListSchemaNode.class, module.getDataChildByName(GRP_USES));
        primaryType = assertInstanceOf(LeafSchemaNode.class, grpUses.getDataChildByName(TYPE));

        final var grp = module.getGroupings().iterator().next();
        secondaryType = assertInstanceOf(LeafSchemaNode.class,
            assertInstanceOf(ListSchemaNode.class, grp.getDataChildByName(SECONDARY)).getDataChildByName(TYPE));
    }

    @Test
    void testFindDataSchemaNodeForRelativeXPathWithDeref() {
        final var typeNodeType = assertInstanceOf(LeafrefTypeDefinition.class, secondaryType.getType());
        final var stack = SchemaInferenceStack.of(context);
        stack.enterGrouping(QName.create(module.getQNameModule(), "grp"));
        stack.enterSchemaTree(QName.create(module.getQNameModule(), "secondary"));
        stack.enterSchemaTree(secondaryType.getQName());
        assertSame(primaryType, stack.resolvePathExpression(typeNodeType.getPathStatement()));
    }
}
