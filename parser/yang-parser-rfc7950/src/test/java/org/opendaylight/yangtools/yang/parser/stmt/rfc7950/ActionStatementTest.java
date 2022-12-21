/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class ActionStatementTest extends AbstractYangTest {

    private static final String FOO_NS = "foo-namespace";
    private static final String FOO_REV = "2016-12-13";

    @Test
    void testActionStatementInDataContainers() throws Exception {
        final var context = assertEffectiveModel("/rfc7950/action-stmt/foo.yang");

        assertContainsActions(context, "root", "grp-action", "aug-action");
        assertContainsActions(context, "top-list", "top-list-action");
        assertContainsActions(context, "top", "top-action");

        final Collection<? extends GroupingDefinition> groupings = context.getGroupings();
        assertEquals(1, groupings.size());
        assertContainsActions(groupings.iterator().next(), "grp-action");

        final Collection<? extends Module> modules = context.getModules();
        assertEquals(1, modules.size());
        final Module foo = modules.iterator().next();
        final Collection<? extends AugmentationSchemaNode> augmentations = foo.getAugmentations();
        assertEquals(1, augmentations.size());
        assertContainsActions(augmentations.iterator().next(), "aug-action", "grp-action");
    }

    private static void assertContainsActions(final SchemaContext schemaContext, final String dataContainerName,
            final String... actionNames) {
        final DataSchemaNode dataChildByName = schemaContext.getDataChildByName(QName.create(FOO_NS, FOO_REV,
            dataContainerName));
        assertContainsActions(assertInstanceOf(ActionNodeContainer.class, dataChildByName), actionNames);
    }

    private static void assertContainsActions(final ActionNodeContainer actionContainer,
            final String... actionNames) {
        final Collection<? extends ActionDefinition> actions = actionContainer.getActions();
        assertEquals(actionNames.length, actions.size());

        final Set<QName> actionQNames = new HashSet<>();
        actions.forEach(n -> actionQNames.add(n.getQName()));

        for (final String actionName : actionNames) {
            assertTrue(actionQNames.contains(QName.create(FOO_NS, FOO_REV, actionName)));
        }
    }

    @Test
    void testActionUnsupportedInYang10() {
        assertSourceException(startsWith("action is not a YANG statement or use of extension"),
            "/rfc7950/action-stmt/foo10.yang");
    }

    @Test
    void testActionWithinIllegalAncestor() {
        assertSourceException(startsWith("Action (foo-namespace?revision=2016-12-13)action-in-grouping is defined"
            + " within a notification, rpc or another action"), "/rfc7950/action-stmt/foo-invalid.yang");
    }

    @Test
    void testActionWithinListWithoutKey() {
        assertSourceException(startsWith("Action (bar-namespace?revision=2016-12-13)my-action is defined within a list"
            + " that has no key statement"), "/rfc7950/action-stmt/bar-invalid.yang");
    }

    @Test
    void testActionInUsedGroupingWithinCase() {
        assertSourceException(startsWith("Action (baz-namespace?revision=2016-12-13)action-in-grouping"
            + " is defined within a case statement"), "/rfc7950/action-stmt/baz-invalid.yang");
    }

    @Test
    void testActionInUsedGroupingAtTopLevelOfModule() {
        assertSourceException(startsWith("Action (foobar-namespace?revision=2016-12-13)my-action is defined"
            + " at the top level of a module"), "/rfc7950/action-stmt/foobar-invalid.yang");
    }
}
