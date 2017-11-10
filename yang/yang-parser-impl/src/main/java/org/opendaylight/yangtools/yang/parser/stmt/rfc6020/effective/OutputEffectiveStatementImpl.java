/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public final class OutputEffectiveStatementImpl extends AbstractEffectiveContainerSchemaNode<OutputStatement>
        implements OutputEffectiveStatement {

    public OutputEffectiveStatementImpl(
            final StmtContext<QName, OutputStatement, EffectiveStatement<QName, OutputStatement>> ctx) {
        super(ctx);
    }

    @Override
    public Set<ActionDefinition> getActions() {
        return ImmutableSet.of();
    }

    @Override
    public Set<NotificationDefinition> getNotifications() {
        return ImmutableSet.of();
    }

    @Override
    public boolean isPresenceContainer() {
        // FIXME: this should not really be here
        return false;
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
        OutputEffectiveStatementImpl other = (OutputEffectiveStatementImpl) obj;
        return Objects.equals(getQName(), other.getQName()) && Objects.equals(getPath(), other.getPath());
    }

    @Override
    public String toString() {
        return "RPC Output " + getQName().getLocalName();
    }
}
