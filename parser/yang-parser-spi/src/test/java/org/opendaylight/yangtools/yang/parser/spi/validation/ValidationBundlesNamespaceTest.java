/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.validation;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.opendaylight.yangtools.yang.parser.spi.validation.ValidationBundlesNamespace.ValidationBundleType.SUPPORTED_DATA_NODES;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.StorageNodeType;

public class ValidationBundlesNamespaceTest {
    @Test
    public void testBehaviour() {
        final NamespaceStorageNode node = mock(NamespaceStorageNode.class);
        doReturn(StorageNodeType.GLOBAL).when(node).getStorageNodeType();

        final Collection<?> result = mock(Collection.class);
        doReturn(result).when(node).getFromLocalStorage(ValidationBundlesNamespace.class, SUPPORTED_DATA_NODES);
        assertSame(result, ValidationBundlesNamespace.BEHAVIOUR.getFrom(node, SUPPORTED_DATA_NODES));
    }
}
