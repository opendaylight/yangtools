/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.leafref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.net.URI;
import java.util.Map;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Bug7844Test {
    private static final String FOO_NS = "foo";
    private static final String BAZ_NS = "baz";
    private static final String REV = "1970-01-01";

    @Test
    public void test() throws Exception {
        final SchemaContext context = YangParserTestUtils.parseYangSources("/bug7844");
        assertNotNull(context);

        final LeafRefContext leafRefContext = LeafRefContext.create(context);
        assertNotNull(leafRefContext);

        final Map<QName, LeafRefContext> referencingChilds = leafRefContext.getReferencingChilds();
        assertEquals(1, referencingChilds.size());

        final LeafRefContext myLeafCtx = referencingChilds.get(foo("my-leaf"));
        assertTrue(myLeafCtx.getReferencedByChilds().isEmpty());
        assertTrue(myLeafCtx.getReferencingChilds().isEmpty());
        assertFalse(myLeafCtx.isReferenced());
        assertTrue(myLeafCtx.isReferencing());
        final LeafRefPath absoluteLeafRefTargetPath = myLeafCtx.getAbsoluteLeafRefTargetPath();
        final QNameModule bazQNameModule = QNameModule.create(new URI(BAZ_NS),
                SimpleDateFormatUtil.getRevisionFormat().parse(REV));
        assertEquals(
                LeafRefPath.create(true, new QNameWithPredicateImpl(bazQNameModule, "root", ImmutableList.of()),
                        new QNameWithPredicateImpl(bazQNameModule, "target", ImmutableList.of())),
                absoluteLeafRefTargetPath);

        final Map<QName, LeafRefContext> referencedByChilds = leafRefContext.getReferencedByChilds();
        assertEquals(1, referencedByChilds.size());

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
        assertEquals(1, allReferencedByLeafRefCtxs.size());
        assertTrue(myLeafCtx == targetCtx.getReferencedByLeafRefCtxByName(foo("my-leaf")));
    }

    private static QName foo(final String localName) {
        return QName.create(FOO_NS, REV, localName);
    }

    private static QName baz(final String localName) {
        return QName.create(BAZ_NS, REV, localName);
    }
}
