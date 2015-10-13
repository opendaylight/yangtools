/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type;

import java.util.Collections;
import java.util.List;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.EnumStatement;
import org.opendaylight.yangtools.yang.model.api.type.EnumTypeDefinition.EnumPair;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.Utils;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.DescriptionEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.EffectiveStatementBase;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ReferenceEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.StatusEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ValueEffectiveStatementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnumEffectiveStatementImpl extends EffectiveStatementBase<String, EnumStatement> implements EnumPair {

    private static final Logger LOG = LoggerFactory.getLogger(EnumEffectiveStatementImpl.class);

    private final SchemaPath path;
    private String description;
    private String reference;
    private Status status;
    private Integer value;
    private final QName maybeQNameArgument;

    public EnumEffectiveStatementImpl(final StmtContext<String, EnumStatement, ?> ctx) {
        super(ctx);

        SchemaPath pathInit = Utils.getSchemaPath(ctx.getParentContext());
        QNameModule moduleQName = pathInit.getLastComponent().getModule();
        QName maybeQNameArgumentInit = null;
        try {
            maybeQNameArgumentInit = QName.create(moduleQName, argument());
        } catch (IllegalArgumentException e) {
            String localName = Utils.replaceIllegalCharsForQName(argument());
            LOG.warn("{}. Enum argument '{}' has been replaced by '{}'.", e.getMessage(), argument(), localName, e);
            maybeQNameArgumentInit = QName.create(moduleQName, localName);
        }
        this.maybeQNameArgument = maybeQNameArgumentInit;
        this.path = pathInit.createChild(this.maybeQNameArgument);

        for (final EffectiveStatement<?, ?> effectiveStatement : effectiveSubstatements()) {
            if (effectiveStatement instanceof DescriptionEffectiveStatementImpl) {
                description = ((DescriptionEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ReferenceEffectiveStatementImpl) {
                reference = ((ReferenceEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof StatusEffectiveStatementImpl) {
                status = ((StatusEffectiveStatementImpl) effectiveStatement).argument();
            }
            if (effectiveStatement instanceof ValueEffectiveStatementImpl) {
                value = ((ValueEffectiveStatementImpl) effectiveStatement).argument();
            }
        }
    }

    @Override
    public String getName() {
        return argument();
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public QName getQName() {
        return maybeQNameArgument;
    }

    @Override
    public SchemaPath getPath() {
        return path;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return Collections.emptyList();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public Status getStatus() {
        return status;
    }
}