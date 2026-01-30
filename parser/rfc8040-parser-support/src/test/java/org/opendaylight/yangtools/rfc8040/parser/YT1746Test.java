/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.spi.meta.SomeModifiersUnresolvedException;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class YT1746Test extends AbstractYangDataTest {
    private static final @NonNull QNameModule NS = QNameModule.of("yt1746");

    @Test
    void featureIndependent() throws Exception {
        final var bar = newBuild().addSource(sourceForYangText("""
            module yt1746 {
              namespace yt1746;
              prefix yt1746;

              import ietf-restconf { prefix rc; }

              feature foo;

              grouping outside-grp {
                container unconditional;
                container conditional {
                  if-feature foo;
                }
              }

              rc:yang-data bar {
                container baz {
                  grouping inside-grp {
                    container inside {
                      if-feature foo;
                    }
                  }

                  uses inside-grp;
                  uses outside-grp {
                    if-feature foo;
                  }
                }
              }
            }"""))
            // but 'foo' is not present in features
            .setSupportedFeatures(FeatureSet.of())
            .buildEffective()
            .findModuleStatement(NS)
            .orElseThrow()
            .findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .orElseThrow();

        // there should not be any if-feature statements under bar
        assertNoIfFeature(bar);

        final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create(NS, "baz"), baz.argument());

        // container baz should have two containers: inside and unconditional
        // container conditional's if-feature is evaluated before it is included in yang-data, hence it is omitted
        assertThat(baz.streamEffectiveSubstatements(ContainerEffectiveStatement.class))
            .hasSize(2)
            .map(ContainerEffectiveStatement::argument)
            .allSatisfy(name -> assertEquals(NS, name.getModule()))
            .allMatch(name -> switch (name.getLocalName()) {
                case "inside", "unconditional" -> true;
                default -> false;
            });
    }

    private static void assertNoIfFeature(final EffectiveStatement<?, ?> parent) {
        assertThat(parent.effectiveSubstatements()).allSatisfy(stmt -> {
            assertThat(stmt).isNotInstanceOf(IfFeatureEffectiveStatement.class);
            assertNoIfFeature(stmt);
        });
    }

    @Test
    void actionIsRejected() throws Exception {
        final var builder = newBuild().addSource(sourceForYangText("""
            module yt1746 {
              yang-version 1.1;
              namespace yt1746;
              prefix yt1746;

              import ietf-restconf { prefix rc; }

              rc:yang-data foo {
                container bar {
                  action baz;
                }
              }
            }"""));

        final var ex = assertThrows(SomeModifiersUnresolvedException.class, builder::buildEffective);
        assertEquals(ModelProcessingPhase.FULL_DECLARATION, ex.getPhase());
        final var cause = assertInstanceOf(SourceException.class, ex.getCause());
        assertEquals("Action (yt1746)baz is defined within another structure [at <UNKNOWN>:10:7]", cause.getMessage());
    }
}
