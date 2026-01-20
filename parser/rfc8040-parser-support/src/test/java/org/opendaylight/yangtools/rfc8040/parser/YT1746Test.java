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

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;

class YT1746Test extends AbstractYangDataTest {
    @Test
    void featureIndependent() throws Exception {
        final var module = QNameModule.of("yt1746");
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
            .findModuleStatement(module)
            .orElseThrow()
            .findFirstEffectiveSubstatement(YangDataEffectiveStatement.class)
            .orElseThrow();

        // there should not be any if-feature statements under bar
        assertNoIfFeature(bar);

        final var baz = bar.findFirstEffectiveSubstatement(ContainerEffectiveStatement.class).orElseThrow();
        assertEquals(QName.create(module, "baz"), baz.argument());

        // container baz should have two containers: inside and unconditional
        // container conditional's if-feature is evaluated before it is included in yang-data, hence it is omitted
        assertThat(baz.streamEffectiveSubstatements(ContainerEffectiveStatement.class))
            .hasSize(2)
            .map(ContainerEffectiveStatement::argument)
            .allSatisfy(name -> assertEquals(module, name.getModule()))
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
}
