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

        ExtendedType union = sortedUnions.get(0);
        assertEquals("simple-union", union.getQName().getLocalName());
        union = sortedUnions.get(1);
        assertEquals("enum-union", union.getQName().getLocalName());
        union = sortedUnions.get(2);
        assertEquals("inner-union", union.getQName().getLocalName());
        union = sortedUnions.get(3);
        assertEquals("nested-union", union.getQName().getLocalName());
        union = sortedUnions.get(4);
        assertEquals("complex-union", union.getQName().getLocalName());
        union = sortedUnions.get(5);
        assertEquals("string-union", union.getQName().getLocalName());
    }
}