/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

/**
 * Test suite for testing of UnionDependencySort
 *
 * @author Lukas Sedlak <lsedlak@cisco.com>
 */
public class UnionDependencySortTest {

    private SchemaContext schemaContext;

    @Before
    public void setUp() throws Exception {
        YangParserImpl parser = new YangParserImpl();
        InputStream stream = UnionDependencySortTest.class.getResourceAsStream("/union-definitions.yang");
            List<InputStream> inputStreams = Collections.singletonList(stream);
        final Set<Module> modules = parser.parseYangModelsFromStreams(inputStreams);
        schemaContext = parser.resolveSchemaContext(modules);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSortWithNullTypeDefinitions() throws Exception {
        UnionDependencySort unionSort = new UnionDependencySort();
        unionSort.sort(null);
    }

    @Test
    public void testSort() throws Exception {
        UnionDependencySort unionSort = new UnionDependencySort();

        final Set<TypeDefinition<?>> typedefs = schemaContext.getTypeDefinitions();
        final List<ExtendedType> sortedUnions = unionSort.sort(typedefs);

        assertNotNull(sortedUnions);
        assertTrue(!sortedUnions.isEmpty());

        ExtendedType simpleUnion=null;
        ExtendedType enumUnion=null;
        ExtendedType innerUnion=null;
        ExtendedType nestedUnion=null;
        ExtendedType complexUnion=null;
        ExtendedType stringUnion=null;
        for (final ExtendedType union : sortedUnions) {
            if (union.getQName().getLocalName().equals("simple-union")) {
                simpleUnion = union;
            } else if (union.getQName().getLocalName().equals("enum-union")) {
                enumUnion = union;
            } else if (union.getQName().getLocalName().equals("inner-union")) {
                innerUnion = union;
            } else if (union.getQName().getLocalName().equals("nested-union")) {
                nestedUnion = union;
            } else if (union.getQName().getLocalName().equals("complex-union")) {
                complexUnion = union;
            } else if (union.getQName().getLocalName().equals("string-union")) {
                stringUnion = union;
            }
        }

        assertNotNull(simpleUnion);
        assertNotNull(enumUnion);
        assertNotNull(innerUnion);
        assertNotNull(nestedUnion);
        assertNotNull(complexUnion);
        assertNotNull(stringUnion);
    }
}