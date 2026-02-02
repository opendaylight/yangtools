/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationEffectiveStatement;
import org.opendaylight.yangtools.rfc7952.parser.dagger.Rfc7952Module;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.DescriptionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class AnnotationTest {
    private static final AnnotationName LAST_MODIFIED =
        new AnnotationName(QName.create("http://example.org/example-last-modified", "last-modified"));

    @Test
    void testAnnotationResolution() throws Exception {
        final var reactor = RFC7950Reactors.vanillaReactorBuilder()
            .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
                Rfc7952Module.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
            .build();
        final var context = reactor.newBuild(YangIRSourceModule.provideTextToIR())
            .addSource(new URLYangTextSource(AnnotationTest.class.getResource("/ietf-yang-metadata@2016-08-05.yang")))
            .addSource(new URLYangTextSource(AnnotationTest.class.getResource("/example-last-modified.yang")))
            .buildEffective();

        assertThat(context.getModuleStatements().values().stream()
            .flatMap(module -> module.streamEffectiveSubstatements(AnnotationEffectiveStatement.class))
            .toList()).hasSize(1).first().satisfies(annotation -> {
                assertEquals(LAST_MODIFIED, annotation.argument());
                assertEquals(BaseTypes.stringType(), annotation.getTypeDefinition());
                assertEquals(Optional.of("""
                    This annotation contains the date and time when the
                    annotated instance was last modified (or created)."""),
                    annotation.findFirstEffectiveSubstatementArgument(DescriptionEffectiveStatement.class));
            });
    }
}
