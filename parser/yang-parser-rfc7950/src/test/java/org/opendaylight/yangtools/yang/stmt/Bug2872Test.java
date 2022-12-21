/*
 * Copyright (c) 2016 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;

class Bug2872Test extends AbstractYangTest {
    @Test
    void test() {
        final var schema = assertEffectiveModelDir("/bugs/bug2872");
        final var myLeaf = assertInstanceOf(LeafSchemaNode.class, schema.getDataChildByName(
            QName.create("bug2872", "2016-06-08", "bar")));
        final var myEnum = assertInstanceOf(EnumTypeDefinition.class, myLeaf.getType());

        assertEquals(List.of("value-one", "value-two"), myEnum.getValues().stream().map(EnumPair::getName).toList());
    }
}
