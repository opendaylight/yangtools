/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.ri.stmt.impl.eff;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.model.spi.meta.EffectiveStatementMixins.DocumentedNodeMixin;

public final class ExtensionEffectiveStatementImpl extends DefaultArgument<QName, ExtensionStatement>
        implements ExtensionDefinition, ExtensionEffectiveStatement, DocumentedNodeMixin<QName, ExtensionStatement> {
    private static final class RecursionDetector extends ThreadLocal<Deque<ExtensionEffectiveStatementImpl>> {
        boolean check(final ExtensionEffectiveStatementImpl current) {
            final Deque<ExtensionEffectiveStatementImpl> stack = get();
            if (stack != null) {
                for (ExtensionEffectiveStatementImpl s : stack) {
                    if (s == current) {
                        return true;
                    }
                }
            }
            return false;
        }

        void push(final ExtensionEffectiveStatementImpl current) {
            Deque<ExtensionEffectiveStatementImpl> stack = get();
            if (stack == null) {
                stack = new ArrayDeque<>(1);
                set(stack);
            }

            stack.push(current);
        }

        void pop() {
            Deque<ExtensionEffectiveStatementImpl> stack = get();
            stack.pop();
            if (stack.isEmpty()) {
                remove();
            }
        }
    }

    private static final RecursionDetector TOSTRING_DETECTOR = new RecursionDetector();

    private final Object substatements;

    public ExtensionEffectiveStatementImpl(final ExtensionStatement declared,
            final ImmutableList<? extends EffectiveStatement<?, ?>> substatements) {
        super(declared);
        this.substatements = maskList(substatements);
    }

    @Override
    public QName getQName() {
        return argument();
    }

    @Override
    public String getArgument() {
        return findFirstEffectiveSubstatementArgument(ArgumentEffectiveStatement.class)
                .map(QName::getLocalName)
                .orElse(null);
    }

    @Override
    public boolean isYinElement() {
        return findFirstEffectiveSubstatement(ArgumentEffectiveStatement.class)
                .flatMap(arg -> arg.findFirstEffectiveSubstatementArgument(YinElementEffectiveStatement.class))
                .orElse(Boolean.FALSE);
    }

    @Override
    public ImmutableList<? extends EffectiveStatement<?, ?>> effectiveSubstatements() {
        final Object local = verifyNotNull(substatements, "Substatements are not yet initialized");
        return unmaskList(local);
    }

    @Override
    public Status getStatus() {
        return findFirstEffectiveSubstatementArgument(StatusEffectiveStatement.class).orElse(Status.CURRENT);
    }

    @Override
    public ExtensionEffectiveStatement asEffectiveStatement() {
        return this;
    }

    @Override
    public String toString() {
        if (TOSTRING_DETECTOR.check(this)) {
            return recursedToString();
        }

        TOSTRING_DETECTOR.push(this);
        try {
            return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                    + "argument=" + getArgument()
                    + ", qname=" + getQName()
                    + ", yin=" + isYinElement()
                    + ", extensionSchemaNodes=" + getUnknownSchemaNodes()
                    + "]";
        } finally {
            TOSTRING_DETECTOR.pop();
        }
    }

    private String recursedToString() {
        return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                + "argument=" + getArgument()
                + ", qname=" + getQName()
                + ", yin=" + isYinElement()
                + " <RECURSIVE> ]";
    }
}
