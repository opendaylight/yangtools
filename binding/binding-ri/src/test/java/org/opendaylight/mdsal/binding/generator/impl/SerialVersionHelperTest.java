/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.Serializable;
import org.junit.Test;
import org.opendaylight.mdsal.binding.generator.impl.reactor.SerialVersionHelper;
import org.opendaylight.mdsal.binding.model.ri.Types;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;
import org.opendaylight.mdsal.binding.model.ri.generated.type.builder.CodegenGeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.AccessModifier;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.MethodSignatureBuilder;

public class SerialVersionHelperTest {
    @Test
    public void computeDefaultSUIDTest() {
        CodegenGeneratedTypeBuilder generatedTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("my.package", "MyName"));

        MethodSignatureBuilder method = generatedTypeBuilder.addMethod("myMethodName");
        method.setAccessModifier(AccessModifier.PUBLIC);
        generatedTypeBuilder.addProperty("myProperty");
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));

        assertEquals(6788238694991761868L, SerialVersionHelper.computeDefaultSUID(generatedTypeBuilder));
    }

    @Test
    public void computeDefaultSUIDStabilityTest() {
        // test method computeDefaultSUID
        GeneratedTypeBuilder genTypeBuilder = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"));
        genTypeBuilder.addMethod("testMethod");
        genTypeBuilder.addAnnotation("org.opendaylight.yangtools.test.annotation", "AnnotationTest");
        genTypeBuilder.addEnclosingTransferObject(new CodegenGeneratedTOBuilder(genTypeBuilder.getIdentifier()
            .createEnclosed("testObject")).build());
        genTypeBuilder.addProperty("newProp");
        GeneratedTypeBuilder genType = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test", "Type2"));
        genTypeBuilder.addImplementsType(genType);
        long computedSUID = SerialVersionHelper.computeDefaultSUID(genTypeBuilder);

        GeneratedTypeBuilder genTypeBuilder2 = new CodegenGeneratedTypeBuilder(
            JavaTypeName.create("org.opendaylight.yangtools.test2", "TestType2"));
        long computedSUID2 = SerialVersionHelper.computeDefaultSUID(genTypeBuilder2);
        assertNotEquals(computedSUID, computedSUID2);
    }
}
