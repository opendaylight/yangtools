/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationSchemaNode;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.YangStatementStreamSource;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;

class AnnotationTest {
    private static final AnnotationName LAST_MODIFIED =
        new AnnotationName(QName.create("http://example.org/example-last-modified", "last-modified"));

    @Test
    void testAnnotationResolution() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addStatementSupport(ModelProcessingPhase.FULL_DECLARATION,
                new AnnotationStatementSupport(YangParserConfiguration.DEFAULT))
            .build();
        final var context = reactor.newBuild()
            .addSources(
                YangStatementStreamSource.create(
                    new URLYangTextSource(AnnotationTest.class.getResource("/ietf-yang-metadata@2016-08-05.yang"))),
                YangStatementStreamSource.create(
                    new URLYangTextSource(AnnotationTest.class.getResource("/example-last-modified.yang"))))
            .buildEffective();

        final var annotations = AnnotationSchemaNode.findAll(context);
        assertEquals(1, annotations.size());
        final var annotation = annotations.get(LAST_MODIFIED);
        assertNotNull(annotation);

        final var findAnnotation = AnnotationSchemaNode.find(context, LAST_MODIFIED);
        assertTrue(findAnnotation.isPresent());
        assertSame(annotation, findAnnotation.orElseThrow());

        assertEquals(BaseTypes.stringType(), annotation.getType());
        assertEquals(Optional.empty(), annotation.getReference());
        assertEquals(Optional.of("This annotation contains the date and time when the\n"
                + "annotated instance was last modified (or created)."), annotation.getDescription());
    }
}
