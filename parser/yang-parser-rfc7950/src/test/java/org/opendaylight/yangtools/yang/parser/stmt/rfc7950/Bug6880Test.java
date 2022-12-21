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

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6880Test extends AbstractYangTest {
    @Test
    void valid10Test() {
        final var context = assertEffectiveModel("/rfc7950/bug6880/foo.yang");
        final var myLeafList = assertInstanceOf(LeafListSchemaNode.class,
            context.findDataTreeChild(QName.create("foo", "my-leaf-list")).orElseThrow());
        assertEquals(Set.of("my-default-value-1", "my-default-value-2"), myLeafList.getDefaults());
    }

    @Test
    void invalid10Test() {
        assertInvalidSubstatementException(startsWith("DEFAULT is not valid for LEAF_LIST"),
            "/rfc7950/bug6880/invalid10.yang");
    }
}