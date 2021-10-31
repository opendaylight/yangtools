/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;

import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLiteralExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Absolute;

public class YT1361Test extends AbstractParserTest {
    @Test
    public void testQualifiedInstanceIdentifier() {
        final var iid = assertInstanceIdentifier("/foo:foo");
        assertNotNull(iid);
    }

    private Absolute assertInstanceIdentifier(final String str) {
        final var xpath = assertXPath(". = \"" + str + "\"");
        final var expr = xpath.getRootExpr();
        assertThat(expr, instanceOf(YangBinaryExpr.class));
        final var right = ((YangBinaryExpr) expr).getRightExpr();
        assertThat(right, instanceOf(YangLiteralExpr.class));
        try {
            return xpath.interpretAsInstanceIdentifier((YangLiteralExpr) right);
        } catch (XPathExpressionException e) {
            throw new AssertionError(e);
        }
    }
}
