/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.path;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.ArgumentUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

@NonNullByDefault
final class SimpleXPathExpression implements PathExpression {
    private final String originalString;
    private final boolean absolute;

    private SimpleXPathExpression(final RevisionAwareXPath xpath) {
        this.originalString = xpath.toString();
        this.absolute = xpath.isAbsolute();
    }

    static SimpleXPathExpression create(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: YANGTOOLS-969: this is not completely accurate
        return new SimpleXPathExpression(ArgumentUtils.parseXPath(ctx, path));
    }

    @Override
    public String getOriginalString() {
        return originalString;
    }

    @Override
    public boolean isAbsolute() {
        return absolute;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("absolute", absolute).add("originalString", originalString)
                .toString();
    }
}
