/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.odlext.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.odlext.model.api.AugmentIdentifierEffectiveStatement;
import org.opendaylight.yangtools.odlext.parser.dagger.OdlCodegenModule;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor.BuildAction;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class AugmentIdentifierTest {
    private static final @NonNull QName AUGMENT = QName.create("urn:augment", null, "augment");
    private static final @NonNull CrossSourceStatementReactor REACTOR = RFC7950Reactors.vanillaReactorBuilder()
        .addAllSupports(ModelProcessingPhase.STATEMENT_DEFINITION,
            OdlCodegenModule.provideParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
        .build();

    @Test
    void testNotCopiedFromAugment() {
        final var modelContext = assertDoesNotThrow(() -> newBuild()
            .addSource(new StringYangTextSource(SourceIdentifier.ofQName(AUGMENT), """
                module augment {
                  namespace urn:augment;
                  prefix aug;

                  import odl-codegen-extensions { prefix oce; }

                  container foo;

                  augment /foo {
                    oce:augment-identifier augment-foo;

                    leaf augmented {
                      type string;
                    }
                  }
                }"""))
            .buildEffective());

        final var substatements = assertThat(modelContext
            .getModuleStatements()
            .get(AUGMENT.getModule())
            .effectiveSubstatements())
            .hasSize(5)
            .actual();

        final var augment = assertInstanceOf(AugmentEffectiveStatement.class, substatements.get(4));
        final var augmentSubstatements = assertThat(augment.effectiveSubstatements()).hasSize(2).actual();
        assertEquals(Unqualified.of("augment-foo"),
            assertInstanceOf(AugmentIdentifierEffectiveStatement.class, augmentSubstatements.getFirst()).argument());
        assertInstanceOf(LeafEffectiveStatement.class, augmentSubstatements.getLast());

        final var foo = assertInstanceOf(ContainerEffectiveStatement.class, substatements.get(3));
        final var fooSubstatements = assertThat(foo.effectiveSubstatements()).hasSize(2).actual();
        assertInstanceOf(LeafEffectiveStatement.class, fooSubstatements.getFirst());
        assertSame(augment, fooSubstatements.getLast());
    }

    @Test
    void testRejectDuplicate() {
        final var re = assertThrows(ReactorException.class, () -> newBuild()
            .addSource(new StringYangTextSource(SourceIdentifier.ofQName(AUGMENT), """
                module augment {
                  namespace urn:augment;
                  prefix aug;

                  import odl-codegen-extensions { prefix oce; }

                  container foo;

                  augment /foo {
                    oce:augment-identifier augment-foo;
                    oce:augment-identifier augment-foo;

                    leaf augmented {
                      type string;
                    }
                  }
                }"""))
            .buildEffective());

        final var cause = assertInstanceOf(SourceException.class, re.getCause());
        assertThat(cause.getMessage())
            .startsWith("""
                (urn:opendaylight:yang:extension:codegen?revision=2024-06-27)augment-identifier can be used at most \
                once, already declared at \
                """)
            .contains(":10:5 [at ")
            .endsWith(":11:5]");
    }

    @Test
    void testRejectInContainer() {
        final var re = assertThrows(ReactorException.class, () -> newBuild()
            .addSource(new StringYangTextSource(SourceIdentifier.ofQName(AUGMENT), """
                module augment {
                  namespace urn:augment;
                  prefix aug;

                  import odl-codegen-extensions { prefix oce; }

                  container foo {
                    oce:augment-identifier augment-foo;
                  }
                }"""))
            .buildEffective());
        final var cause = assertInstanceOf(SourceException.class, re.getCause());
        assertThat(cause.getMessage())
            .startsWith("""
                (urn:opendaylight:yang:extension:codegen?revision=2024-06-27)augment-identifier cannot be used in \
                container [at """)
            .endsWith(":8:5]");
    }

    @Test
    void vanillaIgnoresStatement() {
        final var modelContext = assertDoesNotThrow(() -> newBuild(RFC7950Reactors.vanillaReactor())
            .addSource(new StringYangTextSource(SourceIdentifier.ofQName(AUGMENT), """
                module augment {
                  namespace urn:augment;
                  prefix aug;

                  import odl-codegen-extensions { prefix oce; }

                  container foo {
                    oce:augment-identifier augment-foo;
                  }
                }"""))
            .buildEffective());

        final var substatements = assertThat(modelContext
            .getModuleStatements()
            .get(AUGMENT.getModule())
            .effectiveSubstatements())
            .hasSize(4)
            .actual();
        final var container = assertInstanceOf(ContainerEffectiveStatement.class, substatements.getLast());
        assertEquals(List.of(), container.effectiveSubstatements());
    }

    @NonNullByDefault
    private static BuildAction.WithYang<YangTextSource> newBuild() {
        return newBuild(REACTOR);
    }

    @NonNullByDefault
    private static BuildAction.WithYang<YangTextSource> newBuild(final CrossSourceStatementReactor reactor) {
        return reactor.newBuild(YangIRSourceModule.provideTextToIR())
            .addLibSource(
                new URLYangTextSource(ContextReferenceTest.class.getResource("/odl-codegen-extensions.yang")));
    }
}
