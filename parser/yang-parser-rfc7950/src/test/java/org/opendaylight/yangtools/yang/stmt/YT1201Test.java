/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangXPathAxis;

class YT1201Test extends AbstractYangTest {
    private static final QName FOO = QName.create("foo", "foo");
    private static final QName BAR = QName.create("foo", "bar");

    @Test
    void testWhenPrefixes() {
        final DataSchemaNode bar = assertEffectiveModelDir("/bugs/YT1201/").getDataChildByName(BAR);
        assertThat(bar, instanceOf(ContainerSchemaNode.class));
        final YangExpr when = ((ContainerSchemaNode) bar).getWhenCondition().get().getRootExpr();
        assertThat(when, instanceOf(Relative.class));
        assertEquals(List.of(YangXPathAxis.CHILD.asStep(FOO)), ((Relative) when).getSteps());
    }
}
