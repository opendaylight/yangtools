/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

public class Bug5946Test {
    private static final String NS = "foo";
    private static final String REV = "2016-05-26";
    private static final QName L1 = QName.create(NS, REV, "l1");
    private static final QName L2 = QName.create(NS, REV, "l2");
    private static final QName L3 = QName.create(NS, REV, "l3");
    private static final QName C = QName.create(NS, REV, "c");
    private static final QName WITHOUT_UNIQUE = QName.create(NS, REV, "without-unique");
    private static final QName SIMPLE_UNIQUE = QName.create(NS, REV, "simple-unique");
    private static final QName MULTIPLE_UNIQUE = QName.create(NS, REV, "multiple-unique");

    @Test
    public void test() throws SourceException, FileNotFoundException, ReactorException, URISyntaxException {
        SchemaContext context = StmtTestUtils.parseYangSources("/bugs/bug5946");
        assertNotNull(context);

        Collection<UniqueConstraint> uniqueConstraints = getListConstraints(context, WITHOUT_UNIQUE);
        assertNotNull(uniqueConstraints);
        assertTrue(uniqueConstraints.isEmpty());

        Collection<UniqueConstraint> simpleUniqueConstraints = getListConstraints(context, SIMPLE_UNIQUE);
        assertNotNull(simpleUniqueConstraints);
        assertEquals(1, simpleUniqueConstraints.size());
        Collection<Relative> simpleUniqueConstraintTag = simpleUniqueConstraints.iterator().next().getTag();
        assertTrue(simpleUniqueConstraintTag.contains(SchemaNodeIdentifier.create(false, L1)));
        assertTrue(simpleUniqueConstraintTag.contains(SchemaNodeIdentifier.create(false, C, L3)));

        Collection<UniqueConstraint> multipleUniqueConstraints = getListConstraints(context, MULTIPLE_UNIQUE);
        assertNotNull(multipleUniqueConstraints);
        assertEquals(3, multipleUniqueConstraints.size());
    }

    private static Collection<UniqueConstraint> getListConstraints(SchemaContext context, QName listQName) {
        DataSchemaNode dataChildByName = context.getDataChildByName(listQName);
        assertTrue(dataChildByName instanceof ListSchemaNode);
        return ((ListSchemaNode) dataChildByName).getUniqueConstraints();
    }
}
