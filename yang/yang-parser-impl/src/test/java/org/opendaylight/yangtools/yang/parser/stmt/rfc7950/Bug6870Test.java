/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.stmt.rfc7950;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.ModifierKind;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.stmt.StmtTestUtils;

public class Bug6870Test {
    private static final String FOO_NS = "foo";
    private static final String FOO_REV = "1970-01-01";

    @Test
    public void valid11Test() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSource("/rfc7950/bug6870/foo.yang");
        assertNotNull(schemaContext);

        assertModifier(schemaContext, ModifierKind.INVERT_MATCH, ImmutableList.of("root", "my-leaf"));
        assertModifier(schemaContext, null, ImmutableList.of("root", "my-leaf-2"));
    }

    private static void assertModifier(final SchemaContext schemaContext, final ModifierKind expectedModifierKind,
            final List<String> localNamePath) {
        final SchemaNode findNode = findNode(schemaContext, localNamePath);
        assertTrue(findNode instanceof LeafSchemaNode);
        final LeafSchemaNode myLeaf = (LeafSchemaNode) findNode;

        final TypeDefinition<? extends TypeDefinition<?>> type = myLeaf.getType();
        assertTrue(type instanceof StringTypeDefinition);
        final StringTypeDefinition stringType = (StringTypeDefinition) type;

        final List<PatternConstraint> patternConstraints = stringType.getPatternConstraints();
        assertEquals(1, patternConstraints.size());

        final PatternConstraint patternConstraint = patternConstraints.iterator().next();
        assertEquals(expectedModifierKind, patternConstraint.getModifier());
    }

    private static SchemaNode findNode(final SchemaContext context, final Iterable<String> localNamesPath) {
        final Iterable<QName> qNames = Iterables.transform(localNamesPath,
            localName -> QName.create(FOO_NS, FOO_REV, localName));
        return SchemaContextUtil.findDataSchemaNode(context, SchemaPath.create(qNames, true));
    }

    @Test
    public void invalid11Test() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6870/invalid11.yang");
            fail("Test should fail due to invalid Yang 1.1");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage()
                    .startsWith("'Invert-match' is not valid argument of modifier statement"));
        }
    }

    @Test
    public void invalid10Test() throws Exception {
        try {
            StmtTestUtils.parseYangSource("/rfc7950/bug6870/invalid10.yang");
            fail("Test should fail due to invalid Yang 1.0");
        } catch (final SomeModifiersUnresolvedException e) {
            assertTrue(e.getCause().getMessage().startsWith("modifier is not a YANG statement or use of extension"));
        }
    }
}