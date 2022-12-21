/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class Bug6880Test extends AbstractYangTest {
    @Test
    void valid10Test() {
        final var context = assertEffectiveModel("/rfc7950/bug6880/foo.yang");

        final DataSchemaNode findDataSchemaNode = context.findDataTreeChild(QName.create("foo", "my-leaf-list"))
            .orElse(null);
        assertThat(findDataSchemaNode, instanceOf(LeafListSchemaNode.class));
        final LeafListSchemaNode myLeafList = (LeafListSchemaNode) findDataSchemaNode;

        final Collection<? extends Object> defaults = myLeafList.getDefaults();
        assertEquals(2, defaults.size());
        assertTrue(defaults.contains("my-default-value-1") && defaults.contains("my-default-value-2"));
    }

    @Test
    void invalid10Test() {
        assertInvalidSubstatementException(startsWith("DEFAULT is not valid for LEAF_LIST"),
            "/rfc7950/bug6880/invalid10.yang");
    }
}