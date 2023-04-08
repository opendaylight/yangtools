/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public class Bug5550Test extends AbstractYangTest {
    @Test
    public void test() {
        final var context = assertEffectiveModelDir("/bugs/bug5550");

        final var root = QName.create("foo", "2016-03-18", "root");
        final var containerInGrouping = QName.create(root, "container-in-grouping");
        final var leaf1 = QName.create(root, "leaf-1");

        var findDataSchemaNode = context.findDataTreeChild(root, containerInGrouping, leaf1).orElseThrow();
        assertThat(findDataSchemaNode, instanceOf(LeafSchemaNode.class));
    }
}
