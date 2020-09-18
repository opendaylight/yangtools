/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8791.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.rfc8791.model.api.AugmentStructureEffectiveStatement;
import org.opendaylight.yangtools.rfc8791.model.api.StructureEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.source.StringYangTextSource;
import org.opendaylight.yangtools.yang.model.spi.source.URLYangTextSource;
import org.opendaylight.yangtools.yang.parser.api.YangParserConfiguration;
import org.opendaylight.yangtools.yang.parser.rfc7950.reactor.RFC7950Reactors;
import org.opendaylight.yangtools.yang.parser.spi.meta.ModelProcessingPhase;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.CrossSourceStatementReactor;
import org.opendaylight.yangtools.yang.source.ir.dagger.YangIRSourceModule;

class YangStructureExtensionTest {
    private static final QNameModule EXAMPLE_MODULE = QNameModule.of("urn:example:example-module");
    private static final QNameModule EXAMPLE_MODULE_FOO = QNameModule.of("foo");
    private static final QNameModule EXAMPLE_MODULE_BAR = QNameModule.of("bar");
    private static final QNameModule EXAMPLE_MODULE_BAZ = QNameModule.of("baz");
    private static final QName EXAMPLE_ADDRESS_BOOK = QName.create(EXAMPLE_MODULE, "address-book");
    private static final QName EXAMPLE_ADDRESS = QName.create(EXAMPLE_MODULE, "address");
    private static final QName EXAMPLE_FOO_DATA = QName.create(EXAMPLE_MODULE_FOO, "foo-data");
    private static final CrossSourceStatementReactor REACTOR = RFC7950Reactors.vanillaReactorBuilder()
        .addAllSupports(ModelProcessingPhase.FULL_DECLARATION,
            new Rfc8791ParserExtension().configureBundle(YangParserConfiguration.DEFAULT))
        .build();

    private static final @NonNull YangTextSource SX_SOURCE = new URLYangTextSource(
        YangStructureExtensionTest.class.getResource("/ietf-yang-structure-ext@2020-06-17.yang"));
    private static final @NonNull YangTextSource FOO_SOURCE = new StringYangTextSource(new SourceIdentifier("foo"), """
        module foo {
          yang-version 1.1;
          namespace foo;
          prefix foo;

          import ietf-yang-structure-ext { prefix sx; }

          sx:structure foo-data {
            container foo-con {
              config false;
            }
          }
        }""");
    private static final @NonNull YangTextSource BAR_SOURCE = new StringYangTextSource(new SourceIdentifier("bar"), """
        module bar {
          yang-version 1.1;
          namespace bar;
          prefix bar;

          import ietf-yang-structure-ext { prefix sx; }
          import foo { prefix foo; }

          sx:augment-structure /foo:foo-data/foo:foo-con {
            leaf add-leaf1 { type int32; }
            leaf add-leaf2 { type string; }
          }
        }""");
    private static final @NonNull YangTextSource BAZ_SOURCE = new StringYangTextSource(new SourceIdentifier("baz"), """
        module baz {
          namespace baz;
          prefix baz;

          import ietf-yang-structure-ext { prefix sx; }
          import foo { prefix foo; }

          container cont {
            sx:structure boo-data {
              container boo-con;
            }
            sx:augment-structure /foo:foo-data/foo:foo-con {
              leaf add-leaf1 { type int32; }
              leaf add-leaf2 { type string; }
            }
          }
        }""");

    @Test
    void testStructure() throws Exception {
        final var modelContext = newBuild()
            .addSource(new StringYangTextSource(new SourceIdentifier("example-module"), """
                module example-module {
                  yang-version 1.1;
                  namespace "urn:example:example-module";
                  prefix exm;

                  import ietf-yang-structure-ext { prefix sx; }

                  sx:structure address-book {
                    list address {
                    key "last first";
                      leaf last {
                        type string;
                        description "Last name";
                      }
                      leaf first {
                        type string;
                        description "First name";
                      }
                      leaf street {
                        type string;
                        description "Street name";
                      }
                      leaf city {
                        type string;
                        description "City name";
                      }
                      leaf state {
                        type string;
                        description "State name";
                      }
                    }
                  }
                }"""))
            .addSource(new StringYangTextSource(new SourceIdentifier("example-module-aug"), """
                module example-module-aug {
                  yang-version 1.1;
                  namespace "urn:example:example-module-aug";
                  prefix exma;

                  import ietf-yang-structure-ext { prefix sx; }
                  import example-module { prefix exm; }

                  sx:augment-structure "/exm:address-book/exm:address" {
                    leaf county {
                      type string;
                      description "County name";
                    }
                    leaf zipcode {
                      type string;
                      description "Postal zipcode";
                    }
                  }
                }"""))
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE).orElseThrow();
        final var addressBook = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
        assertEquals(EXAMPLE_ADDRESS_BOOK, addressBook.argument());
        var address = assertInstanceOf(ListSchemaNode.class,
            addressBook.toDataNodeContainer().dataChildByName(EXAMPLE_ADDRESS));
        assertNotNull(address);
        assertEquals(5, address.getChildNodes().size());
        var last = address.dataChildByName(QName.create(EXAMPLE_MODULE,"last"));
        assertNotNull(last);
    }

    @Test
    void testAugmentResolving() throws Exception {
        // test inspired by AugmentTest.testAugmentResolving
        // according to RFC8791
        final var modelContext = newBuild()
            .addSource(FOO_SOURCE)
            .addSource(BAR_SOURCE)
            .buildEffective();
        assertNotNull(modelContext);

        final Collection<? extends ExtensionDefinition> extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE_FOO).orElseThrow();
        final var structureNode = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
        assertEquals(EXAMPLE_FOO_DATA, structureNode.argument());

        final var fooCont = assertInstanceOf(ContainerSchemaNode.class, structureNode.toDataNodeContainer()
                .dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con")));
        assertNotNull(fooCont);
        final var leaf1 = fooCont
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf1"));
        final var leaf2 = fooCont
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf2"));
        // test fails here
        // leafs from augmentation in bar.yang are not present in foo:foo-con
        assertNotNull(leaf1);
        assertNotNull(leaf2);
    }

    @Test
    void testAugmentParsing() throws Exception {
        final var modelContext = newBuild()
            .addSource(FOO_SOURCE)
            .addSource(BAR_SOURCE)
            .buildEffective();
        assertNotNull(modelContext);

        final var extensions = modelContext.getExtensions();
        assertEquals(2, extensions.size());

        final var bar = modelContext.findModuleStatement(EXAMPLE_MODULE_BAR).orElseThrow();
        final var augmentStructureNode = bar.findFirstEffectiveSubstatement(AugmentStructureEffectiveStatement.class)
            .orElseThrow().toDataNodeContainer();

        final var leaf1 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf1"));
        assertNotNull(leaf1);
        final var leaf2 = augmentStructureNode
                .dataChildByName(QName.create(EXAMPLE_MODULE_BAR,"add-leaf2"));
        assertNotNull(leaf2);
    }

    @Test
    void testConfigStatementBeingIgnoredInStructure() throws Exception {
        final var modelContext = newBuild()
            .addSource(BAR_SOURCE)
            .addSource(FOO_SOURCE)
            .buildEffective();
        assertNotNull(modelContext);

        final var foo = modelContext.findModuleStatement(EXAMPLE_MODULE_FOO).orElseThrow();
        final var structureNode = foo.findFirstEffectiveSubstatement(StructureEffectiveStatement.class).orElseThrow();
        assertEquals(EXAMPLE_FOO_DATA, structureNode.argument());

        final var fooCont = assertInstanceOf(ContainerSchemaNode.class,
            structureNode.toDataNodeContainer().dataChildByName(QName.create(EXAMPLE_MODULE_FOO,"foo-con")));
        assertNotNull(fooCont);
        assertEquals(Optional.empty(), fooCont.effectiveConfig());
    }

    @Test
    // FIXME: "ignore" should be tested with a vanilla reactor
    void testIgnoreStructure() throws Exception {
        // TODO: in AugmentStructure argument needs to be valid path even when ignored, is it correct behavior?
        //   if not foo.yang don't need to be added as resource or imported in baz.yang
        final var modelContext = newBuild()
            .addSource(FOO_SOURCE)
            .addSource(BAZ_SOURCE)
            .buildEffective();
        assertNotNull(modelContext);

        final var baz = modelContext.findModuleStatement(EXAMPLE_MODULE_BAZ).orElseThrow();
        final var cont = assertInstanceOf(ContainerEffectiveStatement.class,
            baz.findDataTreeNode(QName.create(baz.localQNameModule(), "cont")));
        assertEquals(List.of(), cont.effectiveSubstatements());
    }

    @NonNullByDefault
    private static CrossSourceStatementReactor.BuildAction.WithYang<YangTextSource> newBuild() {
        return REACTOR.newBuild(YangIRSourceModule.provideTextToIR()).addLibSource(SX_SOURCE);
    }
}
