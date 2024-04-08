/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

import com.google.common.base.VerifyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;

@ExtendWith(MockitoExtension.class)
class ContainerNodeTest {
    @Mock
    private ContainerNode container;

    @Test
    void getChildByArgHasIdentifier() {
        final var key = new NodeIdentifier(QName.create("foo", "foo"));
        doCallRealMethod().when(container).getChildByArg(key);
        doReturn(null).when(container).childByArg(key);

        final var ex = assertThrows(VerifyException.class, () -> container.getChildByArg(key));
        assertEquals("No child matching (foo)foo", ex.getMessage());
    }
}
