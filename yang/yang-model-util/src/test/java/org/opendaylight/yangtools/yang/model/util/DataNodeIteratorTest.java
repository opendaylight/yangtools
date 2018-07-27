/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.OperationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

@RunWith(MockitoJUnitRunner.class)
public class DataNodeIteratorTest {
    private DataNodeIterator dataNodeIterator;

    @Before
    public void before() {
        this.dataNodeIterator = new DataNodeIterator(mockDataNodeContainer(DataNodeContainer.class));
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

    @Deprecated
    private static <T extends CopyableNode> T mockCopyableNode(final boolean augmenting, final T node) {
        doReturn(augmenting).when(node).isAugmenting();
        return node;
    }

    @Test(expected = IllegalArgumentException.class)
    public void createDataNodeIteratorWithNullArgument() {
        new DataNodeIterator(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void removeFromEmptyDataNodeContainer() {
        dataNodeIterator.remove();
    }

    @Test(expected = NoSuchElementException.class)
    public void tryNextOnEmptyDataContainer() {
        dataNodeIterator.next();
    }

    @Test
    public void createDataNodeIteratorWith() {
        assertFalse("Has no next", dataNodeIterator.hasNext());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allChoices());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allContainers());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allTypedefs());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allGroupings());
        assertEquals("Should be empty list", Collections.EMPTY_LIST, dataNodeIterator.allLists());
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
        final QName mockedCase1QName = QName.create("", "case1");
        final CaseSchemaNode mockedCase2 = mockDataNodeContainer(CaseSchemaNode.class);
        final QName mockedCase2QName = QName.create("", "case2");
        final SortedMap<QName, CaseSchemaNode> cases = ImmutableSortedMap.of(mockedCase1QName, mockedCase1,
            mockedCase2QName, mockedCase2);
        doReturn(cases).when(mockedChoice).getCases();

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

        final DataNodeIterator it = new DataNodeIterator(mockedModule);
        assertFalse(it.allContainers().contains(mockedAugmentingContainer));
        assertTrue(it.allContainers().contains(mockedContainer));
        assertTrue(it.allLists().contains(mockedList));
        assertTrue(it.allChoices().contains(mockedChoice));
        assertTrue(it.allChoices().get(0).getCases().values().contains(mockedCase1));
        assertTrue(it.allChoices().get(0).getCases().values().contains(mockedCase2));
        assertTrue(it.allContainers().contains(mockedContainerInNotification));
        assertTrue(it.allLists().contains(mockedListInRpcInputContainer));
        assertTrue(it.allGroupings().contains(mockedGrouping));
    }
}
