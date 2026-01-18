/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8040.parser;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureSet;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureEffectiveStatement;

class YT1746Test extends AbstractYangDataTest {
    @Test
    void featureIndependent() throws Exception {
        final var bar = REACTOR.newBuild().addSources(IETF_RESTCONF_MODULE, sourceForYangText("""
            module yt1746 {
              namespace yt1746;
              prefix yt1746;

              import ietf-restconf { prefix rc; }

              feature foo;

              grouping outside-grp {
                container outside {
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

                  uses outside-grp;
                  uses inside-grp;
                }
              }
            }"""))
            // but 'foo' is not present in features
            .setSupportedFeatures(FeatureSet.of())
            .buildEffective()
            .findModuleStatements("yt1746")
            .iterator().next()
            .streamEffectiveSubstatements(YangDataEffectiveStatement.class)
            .findFirst().orElseThrow();

        // traverse all substatements of 'bar' and assert there is no if-feature present
        assertNoIfFeature(bar);
    }

    private static void assertNoIfFeature(final EffectiveStatement<?, ?> parent) {
        assertThat(parent.effectiveSubstatements()).allSatisfy(stmt -> {
            assertThat(stmt).isNotInstanceOf(IfFeatureEffectiveStatement.class);
            assertNoIfFeature(stmt);
        });
    }
}
