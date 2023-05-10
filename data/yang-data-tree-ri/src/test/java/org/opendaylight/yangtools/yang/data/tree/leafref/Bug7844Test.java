/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug7844Test {
    private static final String FOO_NS = "foo";
    private static final String BAR_NS = "bar";
    private static final String BAZ_NS = "baz";
    private static final String BAR_YANG = """
        module bar {
            namespace bar;
            prefix bar-mod;
            import baz { prefix baz-imp; }
            typedef bar-leafref {
                type baz-imp:my-leafref;
                description "bar-leafref";
            }
            typedef bar-base-leafref {
                type leafref {
                    path "/baz-imp:root/baz-imp:target";
                }
            }
            leaf my-leafref-in-bar {
                type bar-base-leafref;
            }
            leaf my-leafref-in-bar-2 {
                type bar-base-leafref;
                description "bar-base-leafref-2";
            }
            leaf bar-target {
                type string;
            }
        }""";
    private static final String BAZ_YANG = """
        module baz {
            namespace baz;
            prefix baz-mod;
            typedef my-leafref {
                type leafref {
                    path "/baz-mod:root/baz-mod:target";
                }
                description "baz-leafref";
            }
            container root {
                leaf target {
                    type string;
                }
            }
        }""";
    private static final String FOO_YANG = """
        module foo {
            namespace foo;
            prefix foo-mod;
            import bar { prefix bar-imp; }
            leaf my-leaf {
                type foo-leafref;
            }
            typedef foo-leafref {
                type bar-imp:bar-leafref;
                description "foo-leafref";
            }
            leaf my-leaf-2 {
                type foo-leafref-2;
            }
            typedef foo-leafref-2 {
                type bar-imp:bar-base-leafref;
                description "foo-leaf-ref-2";
            }
            leaf bar-base-leafref {
                type bar-imp:bar-base-leafref;
            }
            leaf bar-base-leafref-2 {
                type bar-imp:bar-base-leafref;
                description "bar-base-leafref-2";
            }
            leaf direct-leafref {
                type leafref {
                    path "/bar-imp:bar-target";
                }
            }
        }""";

    @Test
    public void test() {
        final EffectiveModelContext context = YangParserTestUtils.parseYang(BAR_YANG, BAZ_YANG, FOO_YANG);
        assertNotNull(context);

        final LeafRefContext leafRefContext = LeafRefContext.create(context);
        assertNotNull(leafRefContext);

        final Map<QName, LeafRefContext> referencingChilds = leafRefContext.getReferencingChilds();
        assertEquals(7, referencingChilds.size());

        final QNameModule bazQNameModule = QNameModule.create(XMLNamespace.of(BAZ_NS));
        final LeafRefPath expectedPathToBazTarget = LeafRefPath.create(true,
                new QNameWithPredicateImpl(bazQNameModule, "root", ImmutableList.of()),
                new QNameWithPredicateImpl(bazQNameModule, "target", ImmutableList.of()));
        final LeafRefContext myLeafCtx = referencingChilds.get(foo("my-leaf"));
        assertLeafRef(myLeafCtx, expectedPathToBazTarget);
        assertLeafRef(referencingChilds.get(foo("my-leaf-2")), expectedPathToBazTarget);
        assertLeafRef(referencingChilds.get(foo("bar-base-leafref")), expectedPathToBazTarget);
        assertLeafRef(referencingChilds.get(foo("bar-base-leafref-2")), expectedPathToBazTarget);
        assertLeafRef(referencingChilds.get(bar("my-leafref-in-bar")), expectedPathToBazTarget);
        assertLeafRef(referencingChilds.get(bar("my-leafref-in-bar-2")), expectedPathToBazTarget);

        final QNameModule barQNameModule = QNameModule.create(XMLNamespace.of(BAR_NS));
        final LeafRefPath expectedPathToBarTarget = LeafRefPath.create(true,
                new QNameWithPredicateImpl(barQNameModule, "bar-target", ImmutableList.of()));
        assertLeafRef(referencingChilds.get(foo("direct-leafref")), expectedPathToBarTarget);

        final Map<QName, LeafRefContext> referencedByChilds = leafRefContext.getReferencedByChilds();
        assertEquals(2, referencedByChilds.size());

        final LeafRefContext rootCtx = referencedByChilds.get(baz("root"));
        assertEquals(1, rootCtx.getReferencedByChilds().size());
        assertTrue(rootCtx.getReferencingChilds().isEmpty());
        assertFalse(rootCtx.isReferencing());
        assertFalse(rootCtx.isReferenced());

        final LeafRefContext targetCtx = rootCtx.getReferencedChildByName(baz("target"));
        assertTrue(targetCtx.getReferencedByChilds().isEmpty());
        assertTrue(targetCtx.getReferencingChilds().isEmpty());
        assertTrue(targetCtx.isReferenced());
        assertFalse(targetCtx.isReferencing());

        final Map<QName, LeafRefContext> allReferencedByLeafRefCtxs = targetCtx.getAllReferencedByLeafRefCtxs();
        assertEquals(6, allReferencedByLeafRefCtxs.size());
        assertTrue(myLeafCtx == targetCtx.getReferencedByLeafRefCtxByName(foo("my-leaf")));
    }

    private static void assertLeafRef(final LeafRefContext leafRefToTest, final LeafRefPath expectedLeafRefPath) {
        assertNotNull(leafRefToTest);
        assertNotNull(expectedLeafRefPath);
        assertTrue(leafRefToTest.getReferencedByChilds().isEmpty());
        assertTrue(leafRefToTest.getReferencingChilds().isEmpty());
        assertFalse(leafRefToTest.isReferenced());
        assertTrue(leafRefToTest.isReferencing());
        assertEquals(expectedLeafRefPath, leafRefToTest.getAbsoluteLeafRefTargetPath());
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, localName);
    }

    private static QName bar(final String localName) {
        return QName.create(BAR_NS, localName);
    }

    private static QName baz(final String localName) {
        return QName.create(BAZ_NS, localName);
    }
}
