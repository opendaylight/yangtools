/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ContainerEffectiveStatementImpl;

public class Bug4610Test {

    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug4610");

        Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2015-12-12");
        QNameModule foo = QNameModule.create(new URI("foo"), revision);
        QNameModule bar = QNameModule.create(new URI("bar"), revision);

        QName g1 = QName.create(bar, "g1");
        QName g2 = QName.create(bar, "g2");
        QName c1Bar = QName.create(bar, "c1");

        QName c1Foo = QName.create(foo, "c1");
        QName g3 = QName.create(foo, "g3");
        QName root = QName.create(foo, "root");

        ContainerEffectiveStatementImpl effectiveContainerStatementG1 = findContainer(context, g1, c1Bar);
        ContainerEffectiveStatementImpl effectiveContainerStatementG2 = findContainer(context, g2, c1Bar);
        ContainerEffectiveStatementImpl effectiveContainerStatementG3 = findContainer(context, g3, c1Foo);
        ContainerEffectiveStatementImpl effectiveContainerStatementRoot = findContainer(context, root, c1Foo);

        // check arguments
        QName originalStatementArgument = effectiveContainerStatementG1.argument();
        assertTrue(originalStatementArgument.equals(effectiveContainerStatementG2.argument()));
        assertFalse(originalStatementArgument.equals(effectiveContainerStatementG3.argument()));
        assertFalse(originalStatementArgument.equals(effectiveContainerStatementRoot.argument()));

        ContainerStatement originalContainerStatement = effectiveContainerStatementG1.getDeclared();
        ContainerStatement inGrouping2ContainerStatement = effectiveContainerStatementG2.getDeclared();
        ContainerStatement inGrouping3ContainerStatement = effectiveContainerStatementG3.getDeclared();
        ContainerStatement inRootContainerStatement = effectiveContainerStatementRoot.getDeclared();

        // check declared instances
        assertTrue(originalContainerStatement == inGrouping2ContainerStatement);
        assertTrue(originalContainerStatement == inGrouping3ContainerStatement);
        assertTrue(originalContainerStatement == inRootContainerStatement);

    }

    private static ContainerEffectiveStatementImpl findContainer(final SchemaContext context, final QName... path) {
        SchemaNode node = SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(true, path));
        assertTrue(node instanceof ContainerEffectiveStatementImpl);
        ContainerEffectiveStatementImpl container = (ContainerEffectiveStatementImpl) node;
        return container;
    }
}
