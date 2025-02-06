/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal554Test {
    @Test
    void builderTemplateGenerateListenerMethodsTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResource("/mdsal554.yang"));
        assertEquals(4, genTypes.size());

        // status deprecated
        final var deprecated = genTypes.get(1);
        assertEquals("DeprecatedNotification", deprecated.getName());
        final var deprecatedAnnotations = deprecated.getAnnotations();
        assertEquals(1, deprecatedAnnotations.size());

        var annotation = deprecatedAnnotations.get(0);
        assertEquals(JavaTypeName.create(Deprecated.class), annotation.getIdentifier());
        assertEquals(List.of(), annotation.getParameters());

        // status obsolete
        final var obsolete = genTypes.get(2);
        assertEquals("ObsoleteNotification", obsolete.getName());

        final var obsoleteAnnotations = obsolete.getAnnotations();
        assertEquals(1, obsoleteAnnotations.size());

        annotation = obsoleteAnnotations.get(0);
        assertEquals(JavaTypeName.create(Deprecated.class), annotation.getIdentifier());

        final var annotationParameters = annotation.getParameters();
        assertEquals(1, annotationParameters.size());

        assertEquals("forRemoval", annotationParameters.get(0).getName());
        assertEquals("true", annotationParameters.get(0).getValue());

        // status current
        final var current = genTypes.get(3);
        assertEquals("TestNotification", current.getName());
        assertEquals(List.of(), current.getAnnotations());
    }
}
