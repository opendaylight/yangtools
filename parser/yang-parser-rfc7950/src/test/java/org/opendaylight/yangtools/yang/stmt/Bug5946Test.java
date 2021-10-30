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
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Descendant;
import org.opendaylight.yangtools.yang.model.api.stmt.UniqueEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

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
    private static final SchemaNodeIdentifier L1_ID = SchemaNodeIdentifier.Descendant.of(L1);
    private static final SchemaNodeIdentifier L2_ID = SchemaNodeIdentifier.Descendant.of(L2);
    private static final SchemaNodeIdentifier C_L3_ID = SchemaNodeIdentifier.Descendant.of(C, L3);

    @Test
    public void test() throws Exception {
        SchemaContext context = StmtTestUtils.parseYangSources(new File(getClass()
                .getResource("/bugs/bug5946/foo.yang").toURI()));
        assertNotNull(context);

        Collection<? extends UniqueEffectiveStatement> uniqueConstraints = getListConstraints(context, WITHOUT_UNIQUE);
        assertNotNull(uniqueConstraints);
        assertTrue(uniqueConstraints.isEmpty());

        Collection<? extends UniqueEffectiveStatement> simpleUniqueConstraints =
            getListConstraints(context, SIMPLE_UNIQUE);
        assertNotNull(simpleUniqueConstraints);
        assertEquals(1, simpleUniqueConstraints.size());
        Collection<Descendant> simpleUniqueConstraintTag = simpleUniqueConstraints.iterator().next().argument();
        assertTrue(simpleUniqueConstraintTag.contains(L1_ID));
        assertTrue(simpleUniqueConstraintTag.contains(C_L3_ID));

        Collection<? extends UniqueEffectiveStatement> multipleUniqueConstraints =
            getListConstraints(context, MULTIPLE_UNIQUE);
        assertNotNull(multipleUniqueConstraints);
        assertEquals(3, multipleUniqueConstraints.size());
        boolean l1l2 = false;
        boolean l1cl3 = false;
        boolean cl3l2 = false;
        for (UniqueEffectiveStatement uniqueConstraint : multipleUniqueConstraints) {
            Collection<Descendant> uniqueConstraintTag = uniqueConstraint.argument();
            if (uniqueConstraintTag.contains(L1_ID) && uniqueConstraintTag.contains(L2_ID)) {
                l1l2 = true;
            } else if (uniqueConstraintTag.contains(L1_ID) && uniqueConstraintTag.contains(C_L3_ID)) {
                l1cl3 = true;
            } else if (uniqueConstraintTag.contains(C_L3_ID) && uniqueConstraintTag.contains(L2_ID)) {
                cl3l2 = true;
            }
        }
        assertTrue(l1l2 && l1cl3 && cl3l2);
    }

    @Test
    public void testInvalid() throws Exception {
        try {
            StmtTestUtils.parseYangSources(new File(getClass().getResource("/bugs/bug5946/foo-invalid.yang").toURI()));
            fail("Should fail due to invalid argument of unique constraint");
        } catch (ReactorException e) {
            assertTrue(e.getCause().getMessage().startsWith(
                    "Unique statement argument '/simple-unique/l1' contains schema node identifier '/simple-unique/l1'"
                            + " which is not in the descendant node identifier form."));
        }
    }

    private static @NonNull Collection<? extends UniqueEffectiveStatement> getListConstraints(
            final SchemaContext context, final QName listQName) {
        DataSchemaNode dataChildByName = context.getDataChildByName(listQName);
        assertTrue(dataChildByName instanceof ListSchemaNode);
        return ((ListSchemaNode) dataChildByName).getUniqueConstraints();
    }
}
