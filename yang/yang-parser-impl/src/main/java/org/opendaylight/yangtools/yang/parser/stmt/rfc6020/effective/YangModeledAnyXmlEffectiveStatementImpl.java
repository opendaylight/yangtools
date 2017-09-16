/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.base.Preconditions;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.AnyxmlStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class YangModeledAnyXmlEffectiveStatementImpl extends AnyXmlEffectiveStatementImpl
        implements YangModeledAnyXmlSchemaNode {

    private final ContainerSchemaNode schemaOfAnyXmlData;

    public YangModeledAnyXmlEffectiveStatementImpl(final StmtContext<QName, AnyxmlStatement,
            EffectiveStatement<QName, AnyxmlStatement>> ctx, final ContainerSchemaNode contentSchema) {
        super(ctx);
        schemaOfAnyXmlData = Preconditions.checkNotNull(contentSchema);
    }

    @Nonnull
    @Override
    public ContainerSchemaNode getSchemaOfAnyXmlData() {
        return schemaOfAnyXmlData;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(getQName());
        result = prime * result + Objects.hashCode(getPath());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        YangModeledAnyXmlEffectiveStatementImpl other = (YangModeledAnyXmlEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return YangModeledAnyXmlEffectiveStatementImpl.class.getSimpleName() + "["
               + "qname=" + getQName()
               + ", path=" + getPath()
               + "]";
    }
}
