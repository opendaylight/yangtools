/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import java.util.List;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.GroupingUtils;
import org.opendaylight.yangtools.yang.common.QNameModule;
import java.util.Collection;
import org.opendaylight.yangtools.yang.common.QName;
import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.NamespaceStorageNode;
import org.opendaylight.yangtools.yang.parser.spi.meta.NamespaceBehaviour.Registry;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;

class SubstatementContext<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementContextBase<A, D, E> {

    private final StatementContextBase<?, ?, ?> parent;
    private final A argument;

    SubstatementContext(StatementContextBase<?, ?, ?> parent,
            ContextBuilder<A, D, E> builder) throws SourceException {
        super(builder);
        this.parent = Preconditions.checkNotNull(parent,
                "Parent must not be null");
        this.argument = builder.getDefinition().parseArgumentValue(this,
                builder.getRawArgument());
    }

    @SuppressWarnings("unchecked")
    SubstatementContext(SubstatementContext<A, D, E> original,
            QNameModule newQNameModule,
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException {
        super(original);
        this.parent = newParent;

        if (newQNameModule != null) {
            if (original.argument instanceof QName) {
                QName originalQName = (QName) original.argument;
                this.argument = (A) QName.create(newQNameModule,
                        originalQName.getLocalName());
            } else if (StmtContextUtils.producesDeclared(original,
                    KeyStatement.class)) {
                this.argument = (A) StmtContextUtils
                        .replaceModuleQNameForKey(
                                (StmtContext<Collection<SchemaNodeIdentifier>, KeyStatement, ?>) original,
                                newQNameModule);
            } else {
                this.argument = original.argument;
            }
        } else {
            this.argument = original.argument;
        }

        copyDeclaredStmts(original, newQNameModule, typeOfCopy);

        copyEffectiveStmts(original, newQNameModule, typeOfCopy);
    }

    private void copyDeclaredStmts(SubstatementContext<A, D, E> original,
            QNameModule newQNameModule, TypeOfCopy typeOfCopy)
            throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> originalDeclaredSubstatements = original
                .declaredSubstatements();
        for (StatementContextBase<?, ?, ?> stmtContext : originalDeclaredSubstatements) {
            if (GroupingUtils.needToCopyByUses(stmtContext)) {
                StatementContextBase<?, ?, ?> copy = stmtContext.createCopy(
                        newQNameModule, this, typeOfCopy);
                this.addEffectiveSubstatement(copy);
            } else if (GroupingUtils.isReusedByUses(stmtContext)) {
                this.addEffectiveSubstatement(stmtContext);
            }
        }
    }

    private void copyEffectiveStmts(SubstatementContext<A, D, E> original,
            QNameModule newQNameModule, TypeOfCopy typeOfCopy)
            throws SourceException {
        Collection<? extends StatementContextBase<?, ?, ?>> originalEffectiveSubstatements = original
                .effectiveSubstatements();
        for (StatementContextBase<?, ?, ?> stmtContext : originalEffectiveSubstatements) {
            if (GroupingUtils.needToCopyByUses(stmtContext)) {
                StatementContextBase<?, ?, ?> copy = stmtContext.createCopy(
                        newQNameModule, this, typeOfCopy);
                this.addEffectiveSubstatement(copy);
            } else if (GroupingUtils.isReusedByUses(stmtContext)) {
                this.addEffectiveSubstatement(stmtContext);
            }
        }
    }

    @Override
    public StatementContextBase<?, ?, ?> getParentContext() {
        return parent;
    }

    @Override
    public NamespaceStorageNode getParentNamespaceStorage() {
        return parent;
    }

    @Override
    public Registry getBehaviourRegistry() {
        return parent.getBehaviourRegistry();
    }

    @Override
    public RootStatementContext<?, ?, ?> getRoot() {
        return parent.getRoot();
    }

    @Override
    public A getStatementArgument() {
        return argument;
    }

    @Override
    public StatementContextBase<?, ?, ?> createCopy(
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException {
        return createCopy(null, newParent, typeOfCopy);
    }

    @Override
    public StatementContextBase<A, D, E> createCopy(QNameModule newQNameModule,
            StatementContextBase<?, ?, ?> newParent, TypeOfCopy typeOfCopy)
            throws SourceException {
        SubstatementContext<A, D, E> copy = new SubstatementContext<>(this,
                newQNameModule, newParent, typeOfCopy);
        copy.setTypeOfCopy(typeOfCopy);
        copy.setOriginalCtx(this);
        return copy;
    }

    @Override
    public List<Object> getArgumentsFromRoot() {
        List<Object> argumentsFromRoot = parent.getArgumentsFromRoot();
        argumentsFromRoot.add(argument);
        return argumentsFromRoot;
    }

    @Override
    public List<StmtContext<?, ?, ?>> getStmtContextsFromRoot() {
        List<StmtContext<?, ?, ?>> stmtContextsList = parent
                .getStmtContextsFromRoot();
        stmtContextsList.add(this);
        return stmtContextsList;
    }

    @Override
    public boolean isRootContext() {
        return false;
    }
}
