/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;

/**
 * Reproducer: a leaf nested inside an {@code augment} statement, with an explicit
 * {@code config false;} of its own, reports the wrong {@code effectiveConfig()}/
 * {@code isConfiguration()} when reached via
 * {@code AugmentationSchemaNode.getChildNodes()} (the augmentation's own child tree),
 * even though the very same node reports the correct value when reached via
 * {@code EffectiveModelContext.findDataTreeChild()} (the tree grafted onto the
 * augment's target).
 *
 * <p>This mirrors the shape of RFC 8344 (ietf-ip), whose
 * {@code augment "/if:interfaces/if:interface"} augments a config=true target and
 * nests leaves such as {@code ip:origin}/{@code ip:is-router} a couple of levels
 * below the augmentation, each with an explicit {@code config false;}.
 */
class AugmentationChildConfigTest extends AbstractYangTest {
    private static final QName TOP = QName.create("foo", "top");
    private static final QName AUG_CON = QName.create("bar", "aug-con");
    private static final QName AUG_LIST = QName.create("bar", "aug-list");
    private static final QName STATE_LEAF = QName.create("bar", "state-leaf");

    @Test
    void configFalseIsHonoredViaSchemaTree() {
        final var context = assertEffectiveModel(
            "/bugs/AugmentationChildConfig/foo.yang", "/bugs/AugmentationChildConfig/bar.yang");

        final var viaTree = context.findDataTreeChild(TOP, AUG_CON, AUG_LIST, STATE_LEAF).orElseThrow();
        assertEquals(Optional.of(Boolean.FALSE), viaTree.effectiveConfig());
        assertEquals(Boolean.FALSE, viaTree.isConfiguration());
    }

    @Test
    void configFalseIsHonoredViaAugmentationChildNodes() {
        final var context = assertEffectiveModel(
            "/bugs/AugmentationChildConfig/foo.yang", "/bugs/AugmentationChildConfig/bar.yang");

        final var bar = context.findModule("bar").orElseThrow();
        final var augmentation = bar.getAugmentations().iterator().next();

        final var stateLeaf = findChild(augmentation.getChildNodes(), STATE_LEAF);

        // BUG: this leaf declares "config false;" directly on itself, yet the
        // AugmentationSchemaNode-sourced view reports it as config=true / unset,
        // disagreeing with the schema-tree view asserted in
        // configFalseIsHonoredViaSchemaTree() above.
        assertTrue(stateLeaf.effectiveConfig().isPresent(),
            "expected an explicit config statement to be visible on the augmentation's own child node");
        assertEquals(Optional.of(Boolean.FALSE), stateLeaf.effectiveConfig());
        assertEquals(Boolean.FALSE, stateLeaf.isConfiguration());
    }

    private static DataSchemaNode findChild(final Iterable<? extends DataSchemaNode> nodes, final QName qname) {
        for (final var node : nodes) {
            if (qname.equals(node.getQName())) {
                return node;
            }
            if (node instanceof DataNodeContainer container) {
                final var found = findChildOrNull(container.getChildNodes(), qname);
                if (found != null) {
                    return found;
                }
            }
        }
        throw new AssertionError("Did not find " + qname);
    }

    private static DataSchemaNode findChildOrNull(final Iterable<? extends DataSchemaNode> nodes,
            final QName qname) {
        try {
            return findChild(nodes, qname);
        } catch (final AssertionError e) {
            return null;
        }
    }
}
