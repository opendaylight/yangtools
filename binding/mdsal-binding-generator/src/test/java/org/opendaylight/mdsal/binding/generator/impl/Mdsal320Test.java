/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal320Test {
    @Test
    public void mdsal320Test() {
        final var generateTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal320.yang"));
        assertEquals(2, generateTypes.size());

        final GeneratedType foo = generateTypes.stream().filter(type -> type.getFullyQualifiedName()
            .equals("org.opendaylight.yang.gen.v1.urn.odl.yt320.norev.Foo")).findFirst().get();

        final List<GeneratedType> fooTypes = foo.getEnclosedTypes();
        assertEquals(1, fooTypes.size());

        final GeneratedType bar = fooTypes.get(0);
        assertEquals("Bar", bar.getName());
        assertThat(bar, instanceOf(GeneratedTransferObject.class));
        assertTrue(((GeneratedTransferObject) bar).isUnionType());

        final List<GeneratedType> barTypes = bar.getEnclosedTypes();
        assertEquals(1, barTypes.size());

        final GeneratedType bar1 = barTypes.get(0);
        assertEquals("Bar$1", bar1.getName());
        assertThat(bar1, instanceOf(GeneratedTransferObject.class));
        assertTrue(((GeneratedTransferObject) bar1).isUnionType());

        final Iterator<MethodSignature> it = foo.getMethodDefinitions().iterator();
        assertTrue(it.hasNext());
        final MethodSignature getImplIface = it.next();
        assertEquals("implementedInterface", getImplIface.getName());
        assertTrue(getImplIface.isDefault());
        assertTrue(it.hasNext());

        final MethodSignature bindingHashCode = it.next();
        assertEquals(Naming.BINDING_HASHCODE_NAME, bindingHashCode.getName());
        final MethodSignature bindingEquals = it.next();
        assertEquals(Naming.BINDING_EQUALS_NAME, bindingEquals.getName());
        final MethodSignature bindingToString = it.next();
        assertEquals(Naming.BINDING_TO_STRING_NAME, bindingToString.getName());
        final MethodSignature getBar = it.next();
        final Type getBarType = getBar.getReturnType();
        assertTrue(getBarType instanceof GeneratedTransferObject);
        final GeneratedTransferObject getBarTO = (GeneratedTransferObject) getBarType;
        assertTrue(getBarTO.isUnionType());
        assertEquals(bar, getBarTO);
        final MethodSignature requireBar = it.next();
        assertThat(requireBar.getName(), startsWith(Naming.REQUIRE_PREFIX));
        assertFalse(it.hasNext());

        final GeneratedProperty bar1Prop = bar.getProperties().stream().filter(prop -> "bar$1".equals(prop.getName()))
                .findFirst().get();
        final Type bar1PropRet = bar1Prop.getReturnType();
        assertEquals(bar1, bar1PropRet);
    }
}
