/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.rfc7950.stmt.extension;

import static com.google.common.base.Verify.verifyNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.util.RecursiveObjectLeaker;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ArgumentEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ExtensionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.YinElementEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.rfc7950.stmt.AbstractEffectiveDocumentedNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

final class ExtensionEffectiveStatementImpl extends AbstractEffectiveDocumentedNode<QName, ExtensionStatement>
        implements ExtensionDefinition, ExtensionEffectiveStatement {
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

    private final @NonNull QName qname;
    private final @Nullable String argument;
    private final @NonNull SchemaPath schemaPath;

    private final @NonNull List<UnknownSchemaNode> unknownNodes;
    private final boolean yin;

    private ExtensionEffectiveStatementImpl(
            final StmtContext<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> ctx) {
        super(ctx);
        this.qname = verifyNotNull(ctx.getStatementArgument());
        this.schemaPath = ctx.getSchemaPath().get();

        final List<UnknownSchemaNode> unknownNodesInit = new ArrayList<>();
        for (EffectiveStatement<?, ?> unknownNode : effectiveSubstatements()) {
            if (unknownNode instanceof UnknownSchemaNode) {
                unknownNodesInit.add((UnknownSchemaNode) unknownNode);
            }
        }
        this.unknownNodes = ImmutableList.copyOf(unknownNodesInit);

        // initFields
        final Optional<ArgumentEffectiveStatement> optArgumentSubstatement = findFirstEffectiveSubstatement(
            ArgumentEffectiveStatement.class);
        if (optArgumentSubstatement.isPresent()) {
            final ArgumentEffectiveStatement argumentStatement = optArgumentSubstatement.get();
            this.argument = argumentStatement.argument().getLocalName();
            this.yin = argumentStatement.findFirstEffectiveSubstatement(YinElementEffectiveStatement.class)
                    .map(YinElementEffectiveStatement::argument).orElse(Boolean.FALSE).booleanValue();
        } else {
            this.argument = null;
            this.yin = false;
        }
    }

    /**
     * Create a new ExtensionEffectiveStatement, dealing with potential recursion.
     *
     * @param ctx Statement context
     * @return A potentially under-initialized instance
     */
    static EffectiveStatement<QName, ExtensionStatement> create(
            final StmtContext<QName, ExtensionStatement, EffectiveStatement<QName, ExtensionStatement>> ctx) {
        // Look at the thread-local leak in case we are invoked recursively
        final ExtensionEffectiveStatementImpl existing = RecursiveObjectLeaker.lookup(ctx,
            ExtensionEffectiveStatementImpl.class);
        if (existing != null) {
            // Careful! this object is not fully initialized!
            return existing;
        }

        RecursiveObjectLeaker.beforeConstructor(ctx);
        try {
            // This result is fine, we know it has been completely initialized
            return new ExtensionEffectiveStatementImpl(ctx);
        } finally {
            RecursiveObjectLeaker.afterConstructor(ctx);
        }
    }

    @Override
    protected Collection<? extends EffectiveStatement<?, ?>> initSubstatements(
            final Collection<? extends StmtContext<?, ?, ?>> substatementsInit) {
        // WARNING: this leaks an incompletely-initialized object
        RecursiveObjectLeaker.inConstructor(this);

        return super.initSubstatements(substatementsInit);
    }

    @Override
    public QName getQName() {
        return qname;
    }

    @Override
    public SchemaPath getPath() {
        return schemaPath;
    }

    @Override
    public List<UnknownSchemaNode> getUnknownSchemaNodes() {
        return unknownNodes;
    }

    @Override
    public String getArgument() {
        return argument;
    }

    @Override
    public boolean isYinElement() {
        return yin;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(qname);
        result = prime * result + Objects.hashCode(schemaPath);
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
        ExtensionEffectiveStatementImpl other = (ExtensionEffectiveStatementImpl) obj;
        return Objects.equals(qname, other.qname) && Objects.equals(schemaPath, other.schemaPath);
    }

    @Override
    public String toString() {
        if (TOSTRING_DETECTOR.check(this)) {
            return recursedToString();
        }

        TOSTRING_DETECTOR.push(this);
        try {
            return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                    + "argument=" + argument
                    + ", qname=" + qname
                    + ", schemaPath=" + schemaPath
                    + ", extensionSchemaNodes=" + unknownNodes
                    + ", yin=" + yin
                    + "]";
        } finally {
            TOSTRING_DETECTOR.pop();
        }
    }

    private String recursedToString() {
        return ExtensionEffectiveStatementImpl.class.getSimpleName() + "["
                + "argument=" + argument
                + ", qname=" + qname
                + ", schemaPath=" + schemaPath
                + ", yin=" + yin
                + " <RECURSIVE> ]";
    }
}
