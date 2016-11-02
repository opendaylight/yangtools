/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.thirdparty.plugin;

import com.google.common.annotations.Beta;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnknownEffectiveStatementBase;

@Beta
public final class ThirdPartyExtensionEffectiveStatementImpl extends UnknownEffectiveStatementBase<String> {

    private final SchemaPath path;
    private final String valueFromNamespace;

    public ThirdPartyExtensionEffectiveStatementImpl(final StmtContext<String, UnknownStatement<String>, ?> ctx) {
        super(ctx);
        path = ctx.getParentContext().getSchemaPath().get().createChild(getNodeType());
        valueFromNamespace = ctx.getFromNamespace(ThirdPartyNamespace.class, ctx);
    }

    public String getValueFromNamespace() {
        return valueFromNamespace;
    }

    @Nonnull
    @Override
    public QName getQName() {
        return getNodeType();
    }

    @Nonnull
    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, getNodeType(), getNodeParameter());
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
        final ThirdPartyExtensionEffectiveStatementImpl other = (ThirdPartyExtensionEffectiveStatementImpl) obj;
        if (!Objects.equals(path, other.path)) {
            return false;
        }
        if (!Objects.equals(getNodeType(), other.getNodeType())) {
            return false;
        }
        if (!Objects.equals(getNodeParameter(), other.getNodeParameter())) {
            return false;
        }
        return true;
    }
}
