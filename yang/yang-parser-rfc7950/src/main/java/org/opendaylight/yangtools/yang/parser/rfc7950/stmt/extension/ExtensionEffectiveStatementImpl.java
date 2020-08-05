/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractDeclaredEffectiveStatement.DefaultArgument;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.EffectiveStatementMixins.DocumentedNodeMixin;

final class ExtensionEffectiveStatementImpl extends DefaultArgument<QName, ExtensionStatement>
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

    private final @NonNull SchemaPath path;

    private volatile Object substatements;

    ExtensionEffectiveStatementImpl(final ExtensionStatement declared, final SchemaPath path) {
        super(declared);
        this.path = requireNonNull(path);
    }

    @Override
    public QName getQName() {
        return argument();
    }

    @Override
    @Deprecated
    public SchemaPath getPath() {
        return path;
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
    public String toString() {
        if (TOSTRING_DETECTOR.check(this)) {
            return recursedToString();
        }

        TOSTRING_DETECTOR.push(this);
        try {
            return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                    + "argument=" + getArgument()
                    + ", qname=" + getQName()
                    + ", schemaPath=" + path
                    + ", yin=" + isYinElement()
                    + ", extensionSchemaNodes=" + getUnknownSchemaNodes()
                    + "]";
        } finally {
            TOSTRING_DETECTOR.pop();
        }
    }

    void setSubstatements(final ImmutableList<? extends EffectiveStatement<?, ?>> newSubstatements) {
        verify(substatements == null, "Substatements already initialized");
        substatements = maskList(newSubstatements);
    }

    private String recursedToString() {
        return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                + "argument=" + getArgument()
                + ", qname=" + getQName()
                + ", schemaPath=" + path
                + ", yin=" + isYinElement()
                + " <RECURSIVE> ]";
    }
}
