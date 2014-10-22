/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import static org.junit.Assert.assertNotNull;

import com.google.common.base.Optional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.model.util.StringType;

public class UnionDependencySortTest {

    @Rule
    public ExpectedException expException = ExpectedException.none();

    @Test
    public void testSortMethod() {

        final UnionDependencySort unionDependencySort = new UnionDependencySort();
        final Set<TypeDefinition<?>> typeDefs = new HashSet<>();

        final StringType stringType = StringType.getInstance();
        final ExtendedType extendedType = ExtendedType.builder(QName.create("ExtendedType1"), stringType, Optional.<String> absent(), Optional.<String> absent(), SchemaPath.create(false, QName.create("Cont1"), QName.create("List1"))).build();

        typeDefs.add(stringType);
        typeDefs.add(extendedType);

        final List<ExtendedType> sortedExtendedTypes = unionDependencySort.sort(typeDefs);
        assertNotNull(sortedExtendedTypes);

        expException.expect(IllegalArgumentException.class);
        expException.expectMessage("Set of Type Definitions cannot be NULL!");
        unionDependencySort.sort(null);
    }
}
