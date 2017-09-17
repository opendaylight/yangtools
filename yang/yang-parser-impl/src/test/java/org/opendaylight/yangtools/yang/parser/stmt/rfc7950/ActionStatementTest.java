/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class ActionStatementTest {

    private static final String FOO_NS = "foo-namespace";
    private static final String FOO_REV = "2016-12-13";

    @Test
    public void testActionStatementInDataContainers() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/action-stmt/foo.yang");
        assertNotNull(schemaContext);

        assertContainsActions(schemaContext, "root", "grp-action", "aug-action");
        assertContainsActions(schemaContext, "top-list", "top-list-action");
        assertContainsActions(schemaContext, "top", "top-action");

        final Set<GroupingDefinition> groupings = schemaContext.getGroupings();
        assertEquals(1, groupings.size());
        assertContainsActions(groupings.iterator().next(), "grp-action");

        final Set<Module> modules = schemaContext.getModules();
        assertEquals(1, modules.size());
        final Module foo = modules.iterator().next();
        final Set<AugmentationSchema> augmentations = foo.getAugmentations();
        assertEquals(1, augmentations.size());
        assertContainsActions(augmentations.iterator().next(), "aug-action", "grp-action");
    }

    private static void assertContainsActions(final SchemaContext schemaContext, final String dataContainerName,
            final String... actionNames) {
        final DataSchemaNode dataChildByName = schemaContext.getDataChildByName(QName.create(FOO_NS, FOO_REV,
                dataContainerName));
        assertTrue(dataChildByName instanceof ActionNodeContainer);
        assertContainsActions((ActionNodeContainer) dataChildByName, actionNames);
    }

    private static void assertContainsActions(final ActionNodeContainer actionContainer,
            final String... actionNames) {
        final Set<ActionDefinition> actions = actionContainer.getActions();
        assertEquals(actionNames.length, actions.size());

        final Set<QName> actionQNames = new HashSet<>();
        actions.forEach(n -> actionQNames.add(n.getQName()));

        for (final String actionName : actionNames) {
            assertTrue(actionQNames.contains(QName.create(FOO_NS, FOO_REV, actionName)));
        }
    }

    @Test
    public void testActionUnsupportedInYang10() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/action-stmt/foo10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("action is not a YANG statement or use of extension"));
        }
    }

    @Test
    public void testActionWithinIllegalAncestor() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/action-stmt/foo-invalid.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith("Action (foo-namespace?revision=2016-12-13)"
                    + "action-in-grouping is defined within a notification, rpc or another action"));
        }
    }

    @Test
    public void testActionWithinListWithoutKey() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/action-stmt/bar-invalid.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Action (bar-namespace?revision=2016-12-13)my-action is defined within a list"
                            + " that has no key statement"));
        }
    }

    @Test
    public void testActionInUsedGroupingWithinCase() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/action-stmt/baz-invalid.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Action (baz-namespace?revision=2016-12-13)action-in-grouping is defined within a "
                            + "case statement"));
        }
    }

    @Test
    public void testActionInUsedGroupingAtTopLevelOfModule() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/action-stmt/foobar-invalid.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Action (foobar-namespace?revision=2016-12-13)my-action is defined at the top level of a module"));
        }
    }
}
