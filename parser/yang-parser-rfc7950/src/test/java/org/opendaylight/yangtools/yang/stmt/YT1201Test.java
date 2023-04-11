/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
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
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

class YT1201Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    void testWhenPrefixes() {
        final var bar = assertEffectiveModelDir("/bugs/YT1201/").getDataChildByName(BAR);
        final var when = assertInstanceOf(ContainerSchemaNode.class, bar).getWhenCondition().orElseThrow()
            .getRootExpr();
        assertEquals(List.of(YangXPathAxis.CHILD.asStep(FOO)), assertInstanceOf(Relative.class, when).getSteps());
    }
}
