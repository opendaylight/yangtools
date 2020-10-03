/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DerivableSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class SchemaNodeUtilsTest {
    @Mock
    private DerivableSchemaNode derivableNode;

    @Test
    public void testHandleNullGetOriginalIfPossible() {
        Optional<SchemaNode> originalIfPossible = SchemaNodeUtils
                .getOriginalIfPossible(null);
        assertNotNull(originalIfPossible);
        assertThat(originalIfPossible, instanceOf(Optional.class));
    }

    @Test
    public void testHandleNodeGetOriginalIfPossible() {
        Optional<DerivableSchemaNode> of = Optional.of(derivableNode);
        doReturn(of).when(derivableNode).getOriginal();
        Optional<SchemaNode> originalIfPossible = SchemaNodeUtils
                .getOriginalIfPossible(derivableNode);
        assertNotNull(originalIfPossible);
        assertThat(originalIfPossible, instanceOf(Optional.class));
    }

    @Test
    public void testHandleNullGetRootOriginalIfPossible() {
        SchemaNode rootOriginalIfPossible = SchemaNodeUtils
                .getRootOriginalIfPossible(null);
        assertNull(rootOriginalIfPossible);
    }

    @Test
    public void testTraversal() {
        final Module mockedModule = mockDataNodeContainer(Module.class);

        final ContainerSchemaNode mockedAugmentingContainer = mockCopyableNode(true, mock(ContainerSchemaNode.class));
        final ContainerSchemaNode mockedContainer = mockCopyableNode(false, mockDataNodeContainer(
            ContainerSchemaNode.class));

        final ListSchemaNode mockedList = mockCopyableNode(false, mockDataNodeContainer(ListSchemaNode.class));

        final ChoiceSchemaNode mockedChoice = mockCopyableNode(false, mock(ChoiceSchemaNode.class));
        final CaseSchemaNode mockedCase1 = mockDataNodeContainer(CaseSchemaNode.class);
        final CaseSchemaNode mockedCase2 = mockDataNodeContainer(CaseSchemaNode.class);
        doReturn(ImmutableSet.of(mockedCase1, mockedCase2)).when(mockedChoice).getCases();

        final Set<DataSchemaNode> childNodes = ImmutableSet.of(mockedAugmentingContainer, mockedContainer, mockedList,
                mockedChoice);
        doReturn(childNodes).when(mockedModule).getChildNodes();

        final NotificationDefinition mockedNotification = mockDataNodeContainer(NotificationDefinition.class);
        final ContainerSchemaNode mockedContainerInNotification = mockCopyableNode(false,
            mockDataNodeContainer(ContainerSchemaNode.class));
        final Set<DataSchemaNode> notificationChildNodes = ImmutableSet.of(mockedContainerInNotification);
        doReturn(notificationChildNodes).when(mockedNotification).getChildNodes();
        final Set<NotificationDefinition> notifications = ImmutableSet.of(mockedNotification);

        doReturn(notifications).when(mockedModule).getNotifications();

        final RpcDefinition mockedRpc = mockOperationDefinition(mock(RpcDefinition.class));
        final ContainerSchemaNode mockedContainerInRpcInput = mockDataNodeContainer(ContainerSchemaNode.class);
        final ListSchemaNode mockedListInRpcInputContainer = mockCopyableNode(false,
            mockDataNodeContainer(ListSchemaNode.class));
        final Set<DataSchemaNode> rpcInputChildNodes = ImmutableSet.of(mockedListInRpcInputContainer);
        doReturn(rpcInputChildNodes).when(mockedContainerInRpcInput).getChildNodes();
        doReturn(mockedContainerInRpcInput).when(mockedRpc).getInput();
        final Set<RpcDefinition> rpcs = ImmutableSet.of(mockedRpc);

        doReturn(rpcs).when(mockedModule).getRpcs();

        final GroupingDefinition mockedGrouping = mockDataNodeContainer(GroupingDefinition.class);
        final Set<GroupingDefinition> groupings = ImmutableSet.of(mockedGrouping);

        doReturn(groupings).when(mockedModule).getGroupings();

        final Collection<? extends ContainerSchemaNode> containers = SchemaNodeUtils.getAllContainers(mockedModule);
        assertEquals(2, containers.size());
        assertFalse(containers.contains(mockedAugmentingContainer));
        assertTrue(containers.contains(mockedContainer));
        assertTrue(containers.contains(mockedContainerInNotification));
    }

    @Deprecated
    private static <T extends CopyableNode> T mockCopyableNode(final boolean augmenting, final T node) {
        doReturn(augmenting).when(node).isAugmenting();
        return node;
    }

    private static <T extends DataNodeContainer> T mockDataNodeContainer(final Class<T> clazz) {
        final T mock = mock(clazz);
        doReturn(Collections.emptyList()).when(mock).getChildNodes();
        doReturn(Collections.emptySet()).when(mock).getGroupings();
        doReturn(Collections.emptySet()).when(mock).getTypeDefinitions();
        return mock;
    }

    private static <T extends OperationDefinition> T mockOperationDefinition(final T mock) {
        doReturn(Collections.emptySet()).when(mock).getTypeDefinitions();
        doReturn(mockDataNodeContainer(ContainerSchemaNode.class)).when(mock).getOutput();
        return mock;
    }
}
