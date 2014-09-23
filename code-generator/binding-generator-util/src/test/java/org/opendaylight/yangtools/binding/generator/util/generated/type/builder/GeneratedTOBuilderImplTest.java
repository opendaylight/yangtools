/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.util.generated.type.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Restrictions;
import org.opendaylight.yangtools.sal.binding.model.api.type.builder.MethodSignatureBuilder;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LengthConstraint;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

public class GeneratedTOBuilderImplTest {

    @Test
    public void testCreateNewInstance() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        assertNotNull(genTOBuilder);
    }

    @Test
    public void testSetExtendsType() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedTOBuilderImpl extendedTypeBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "ExtendedType");
        final GeneratedTransferObject extendedType = extendedTypeBuilder.toInstance();
        genTOBuilder.setExtendsType(extendedType);
        final GeneratedTransferObject genTO = genTOBuilder.toInstance();

        assertEquals("ExtendedType", genTO.getSuperType().getName());
    }

    @Test
    public void testAddMethod() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final MethodSignatureBuilder methodSignatureBuilder = genTOBuilder.addMethod("testMethod");
        assertEquals(methodSignatureBuilder, genTOBuilder.getMethodDefinitions().get(0));
        assertEquals("testMethod", genTOBuilder.getMethodDefinitions().get(0).getName());
    }

    @Test
    public void testAddEqualsIdentity() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedPropertyBuilderImpl propertyBuilder = new GeneratedPropertyBuilderImpl("testProperty");
        genTOBuilder.addEqualsIdentity(propertyBuilder);

        final GeneratedTransferObject genTO = genTOBuilder.toInstance();
        assertEquals(1, genTO.getEqualsIdentifiers().size());
        assertEquals("testProperty", genTO.getEqualsIdentifiers().get(0).getName());
    }

    @Test
    public void testAddHashIdentity() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedPropertyBuilderImpl propertyBuilder = new GeneratedPropertyBuilderImpl("testProperty");
        genTOBuilder.addHashIdentity(propertyBuilder);

        final GeneratedTransferObject genTO = genTOBuilder.toInstance();
        assertEquals(1, genTO.getHashCodeIdentifiers().size());
        assertEquals("testProperty", genTO.getHashCodeIdentifiers().get(0).getName());
    }

    @Test
    public void testAddToStringProperty() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedPropertyBuilderImpl propertyBuilder = new GeneratedPropertyBuilderImpl("testProperty");
        genTOBuilder.addToStringProperty(propertyBuilder);

        final GeneratedTransferObject genTO = genTOBuilder.toInstance();
        assertEquals(1, genTO.getToStringIdentifiers().size());
        assertEquals("testProperty", genTO.getToStringIdentifiers().get(0).getName());
    }

    @Test
    public void testSetRestrictions() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final Restrictions restrictions = new Restrictions() {

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public List<RangeConstraint> getRangeConstraints() {
                return null;
            }

            @Override
            public List<PatternConstraint> getPatternConstraints() {
                return null;
            }

            @Override
            public List<LengthConstraint> getLengthConstraints() {
                return null;
            }
        };
        genTOBuilder.setRestrictions(restrictions);
        final GeneratedTransferObject genTO = genTOBuilder.toInstance();

        assertNotNull(genTO.getRestrictions());
    }

    @Test
    public void testSetSUID() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedPropertyBuilderImpl propertyBuilder = new GeneratedPropertyBuilderImpl("testProperty");
        genTOBuilder.setSUID(propertyBuilder);

        final GeneratedTransferObject genTO = genTOBuilder.toInstance();
        assertEquals("testProperty", genTO.getSUID().getName());
    }

    @Test
    public void testToStringMethod() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        assertNotNull(genTOBuilder.toString());
    }

    @Test
    public void testSetterMethods() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        genTOBuilder.setTypedef(true);
        genTOBuilder.setIsUnion(true);
        genTOBuilder.setIsUnionBuilder(true);
        genTOBuilder.setDescription("test description");
        genTOBuilder.setModuleName("test-module");
        genTOBuilder.setReference("http://tools.ietf.org/html/rfc6020");
        genTOBuilder.setSchemaPath(SchemaPath.ROOT.getPathFromRoot());

        final GeneratedTransferObject genTO = genTOBuilder.toInstance();

        assertTrue(genTO.isTypedef());
        assertTrue(genTO.isUnionType());
        assertTrue(genTO.isUnionTypeBuilder());
        assertEquals("test description", genTO.getDescription());
        assertEquals("test-module", genTO.getModuleName());
        assertEquals("http://tools.ietf.org/html/rfc6020", genTO.getReference());
        assertEquals(SchemaPath.ROOT.getPathFromRoot(), genTO.getSchemaPath());
    }

    @Test
    public void testMethodsOfGeneratedTransferObjectImpl() {
        final GeneratedTOBuilderImpl genTOBuilder = new GeneratedTOBuilderImpl("org.opendaylight.yangtools.test", "Test");
        final GeneratedTransferObject genTO = genTOBuilder.toInstance();

        assertNotNull(genTO.toString());
    }
}
