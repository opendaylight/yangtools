/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.AnnotationType;
import org.opendaylight.mdsal.binding.model.api.AnnotationType.Parameter;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal554Test {
    @Test
    public void builderTemplateGenerateListenerMethodsTest() {
        final List<GeneratedType> genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal554.yang"));
        assertEquals(4, genTypes.size());

        final List<MethodSignature> methods = genTypes.get(3).getMethodDefinitions();
        assertEquals(3, methods.size());

        // status deprecated
        final MethodSignature deprecated = methods.get(0);
        assertEquals("onDeprecatedNotification", deprecated.getName());
        assertFalse(deprecated.isDefault());

        final List<AnnotationType> deprecatedAnnotations = deprecated.getAnnotations();
        assertEquals(1, deprecatedAnnotations.size());

        AnnotationType annotation = deprecatedAnnotations.get(0);
        assertEquals(JavaTypeName.create(Deprecated.class), annotation.getIdentifier());
        assertEquals(List.of(), annotation.getParameters());

        // status obsolete
        final MethodSignature obsolete = methods.get(1);
        assertEquals("onObsoleteNotification", obsolete.getName());
        assertTrue(obsolete.isDefault());

        final List<AnnotationType> obsoleteAnnotations = obsolete.getAnnotations();
        assertEquals(1, obsoleteAnnotations.size());

        annotation = obsoleteAnnotations.get(0);
        assertEquals(JavaTypeName.create(Deprecated.class), annotation.getIdentifier());

        final List<Parameter> annotationParameters = annotation.getParameters();
        assertEquals(1, annotationParameters.size());

        assertEquals("forRemoval", annotationParameters.get(0).getName());
        assertEquals("true", annotationParameters.get(0).getValue());

        // status current
        final MethodSignature current = methods.get(2);
        assertEquals("onTestNotification", current.getName());
        assertFalse(current.isDefault());
        assertEquals(List.of(), current.getAnnotations());
    }
}
