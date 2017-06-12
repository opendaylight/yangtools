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

import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;

public class DataNodeIteratorTest {

    private DataNodeIterator dataNodeIterator;

    @Before
    public void setUp() {
        DataNodeContainer dataNodeContainer = mock(DataNodeContainer.class);
        this.dataNodeIterator = new DataNodeIterator(dataNodeContainer);
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
        final Module mockedModule = mock(Module.class);

        final ContainerSchemaNode mockedAugmentingContainer = mock(ContainerSchemaNode.class);
        doReturn(true).when(mockedAugmentingContainer).isAugmenting();

        final ContainerSchemaNode mockedContainer = mock(ContainerSchemaNode.class);

        final ListSchemaNode mockedList = mock(ListSchemaNode.class);

        final ChoiceSchemaNode mockedChoice = mock(ChoiceSchemaNode.class);
        final ChoiceCaseNode mockedCase1 = mock(ChoiceCaseNode.class);
        final ChoiceCaseNode mockedCase2 = mock(ChoiceCaseNode.class);
        final Set<ChoiceCaseNode> cases = Sets.newHashSet(mockedCase1, mockedCase2);
        doReturn(cases).when(mockedChoice).getCases();

        final Set<DataSchemaNode> childNodes = Sets.newHashSet(mockedAugmentingContainer, mockedContainer, mockedList,
                mockedChoice);
        doReturn(childNodes).when(mockedModule).getChildNodes();

        final NotificationDefinition mockedNotification = mock(NotificationDefinition.class);
        final ContainerSchemaNode mockedContainerInNotification = mock(ContainerSchemaNode.class);
        final Set<DataSchemaNode> notificationChildNodes = Sets.newHashSet(mockedContainerInNotification);
        doReturn(notificationChildNodes).when(mockedNotification).getChildNodes();
        final Set<NotificationDefinition> notifications = Sets.newHashSet(mockedNotification);

        doReturn(notifications).when(mockedModule).getNotifications();

        final RpcDefinition mockedRpc = mock(RpcDefinition.class);
        final ContainerSchemaNode mockedContainerInRpcInput = mock(ContainerSchemaNode.class);
        final ListSchemaNode mockedListInRpcInputContainer = mock(ListSchemaNode.class);
        final Set<DataSchemaNode> rpcInputChildNodes = Sets.newHashSet(mockedListInRpcInputContainer);
        doReturn(rpcInputChildNodes).when(mockedContainerInRpcInput).getChildNodes();
        doReturn(mockedContainerInRpcInput).when(mockedRpc).getInput();
        final Set<RpcDefinition> rpcs = Sets.newHashSet(mockedRpc);

        doReturn(rpcs).when(mockedModule).getRpcs();

        final GroupingDefinition mockedGrouping = mock(GroupingDefinition.class);
        final Set<GroupingDefinition> groupings = Sets.newHashSet(mockedGrouping);

        doReturn(groupings).when(mockedModule).getGroupings();

        final DataNodeIterator dataNodeIterator = new DataNodeIterator(mockedModule);
        assertFalse(dataNodeIterator.allContainers().contains(mockedAugmentingContainer));
        assertTrue(dataNodeIterator.allContainers().contains(mockedContainer));
        assertTrue(dataNodeIterator.allLists().contains(mockedList));
        assertTrue(dataNodeIterator.allChoices().contains(mockedChoice));
        assertTrue(dataNodeIterator.allChoices().get(0).getCases().contains(mockedCase1));
        assertTrue(dataNodeIterator.allChoices().get(0).getCases().contains(mockedCase2));
        assertTrue(dataNodeIterator.allContainers().contains(mockedContainerInNotification));
        assertTrue(dataNodeIterator.allLists().contains(mockedListInRpcInputContainer));
        assertTrue(dataNodeIterator.allGroupings().contains(mockedGrouping));
    }
}
