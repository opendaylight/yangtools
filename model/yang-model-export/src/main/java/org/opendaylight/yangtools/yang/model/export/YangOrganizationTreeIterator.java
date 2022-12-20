/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.AbstractIterator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatementAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.IfFeatureAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.InputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.MandatoryStatementAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.NotificationStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OperationDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.OutputStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PresenceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;

public class YangOrganizationTreeIterator extends AbstractIterator<@NonNull String> {

    private final Deque<Iterator<? extends DeclaredStatement<?>>> stack = new ArrayDeque<>(16);
    private final Stack<String> indent = new Stack<>();

    YangOrganizationTreeIterator(final DeclaredStatement<?> stmt, final StatementPrefixResolver resolver,
            final Set<StatementDefinition> ignoredStatements, final boolean omitDefaultStatements) {
        pushStatement(List.of(requireNonNull(stmt)).iterator());
    }

    @Override
    protected @NonNull String computeNext() {

        while (!stack.isEmpty()) {
            final Iterator<? extends DeclaredStatement<?>> it = pollStatement();

            while (it.hasNext()) {
                final DeclaredStatement<?> currentStatement = it.next();

                if (currentStatement instanceof ModuleStatement) {
                    pushStatement(it);
                    pushStatement(getTrimmedSubstatements(currentStatement));
                    return "module: " + currentStatement.rawArgument() + "\n";
                } else if (currentStatement instanceof SubmoduleStatement) {
                    pushStatement(it);
                    pushStatement(getTrimmedSubstatements(currentStatement));
                    return "submodule: " + currentStatement.rawArgument() + "\n";
                } else if (currentStatement instanceof DocumentedDeclaredStatement.WithStatus<?>) {
                    final DocumentedDeclaredStatement.WithStatus<?> documentedDeclaredStatement =
                            (DocumentedDeclaredStatement.WithStatus<?>) currentStatement;
                    final String result = String.join("", indent)
                            + getStatementStatus(documentedDeclaredStatement)
                            + "--"
                            + getStatementFlags(documentedDeclaredStatement)
                            + getName(documentedDeclaredStatement)
                            + getOpts(documentedDeclaredStatement)
                            + " "
                            + getType(documentedDeclaredStatement)
                            + " "
                            + getIfFeatures(documentedDeclaredStatement)
                            + "\n";
                    pushStatement(it);
                    pushStatement(getTrimmedSubstatements(documentedDeclaredStatement));
                    return result;
                }
            }
        }
        endOfData();
        return "";
    }

    private Iterator<? extends DeclaredStatement<?>> getTrimmedSubstatements(DeclaredStatement<?> stmt) {
        return stmt.declaredSubstatements().stream()
                .filter(statement -> statement instanceof DocumentedDeclaredStatement.WithStatus<?>)
                .iterator();
    }

    private String getStatementStatus(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        final Optional<StatusStatement> statusStatement = stmt.getStatus();

        if (statusStatement.isEmpty()) {
            return "+";
        }
        else if (statusStatement.get().rawArgument().equals("deprecated")) {
            return "x";
        }
        else if (statusStatement.get().rawArgument().equals("obsolete")) {
            return "o";
        }
        else {
            return "+";
        }
    }

    private String getStatementFlags(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        if (stmt instanceof DataDefinitionStatement) {
            if (stmt instanceof ConfigStatementAwareDeclaredStatement<?>) {
                final ConfigStatementAwareDeclaredStatement configAware = (ConfigStatementAwareDeclaredStatement) stmt;
                final Optional<ConfigStatement> optConfig = configAware.getConfig();

                if (optConfig.isPresent()) {
                    if (optConfig.get().toString().equals("false")) {
                        return "ro";
                    }
                    else {
                        return "rw";
                    }
                } else {
                    return "rw";
                }

            } else if (stmt instanceof UsesStatement) {
                return "-u";
            } else {
                return "";
            }

        } else if (stmt instanceof OperationDeclaredStatement) {
            return "-x";
        } else if (stmt instanceof InputStatement) {
            return "-w";
        } else if (stmt instanceof OutputStatement) {
            return "ro";
        } else if (stmt instanceof NotificationStatement) {
            return "-n";
        } else {
            return "UU";
        }
    }

    private String getName(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        if (stmt instanceof ChoiceStatement) {
            return " (" + stmt.rawArgument() + ")";
        }
        else if (stmt instanceof CaseStatement) {
            return ":(" + stmt.rawArgument() + ")";
        }
        else {
            return " " + stmt.rawArgument();
        }
    }

    private String getOpts(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        if (stmt instanceof MandatoryStatementAwareDeclaredStatement<?>) {
            final MandatoryStatementAwareDeclaredStatement<?> mandatoryAwareStatement =
                    (MandatoryStatementAwareDeclaredStatement<?>) stmt;
            final var optionalMandatoryStatement = mandatoryAwareStatement.getMandatory();
            if (optionalMandatoryStatement.isPresent()) {
                if (optionalMandatoryStatement.get().toString().equals("false")) {
                    return "?";
                }
                else {
                    return "";
                }
            } else {
                return "";
            }
        }
        else if (stmt instanceof ContainerStatement) {
            final ContainerStatement containerStatement = (ContainerStatement) stmt;
            final PresenceStatement presenceStatement = containerStatement.getPresence();
            if (presenceStatement == null) {
                return "!";
            }
            else if (presenceStatement.toString().equals("true")) {
                return "!";
            }
            else {
                return "";
            }
        }
        else if (stmt instanceof LeafListStatement) {
            return "*";
        }
        else if (stmt instanceof ListStatement) {
            final ListStatement listStatement = (ListStatement) stmt;
            final KeyStatement keyStatement = listStatement.getKey();
            return "* [" + keyStatement.rawArgument() + "]";
        }
        else {
            return "";
        }
    }

    private String getType(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        if (stmt instanceof TypeAwareDeclaredStatement) {
            final TypeAwareDeclaredStatement typeAwareStatement = (TypeAwareDeclaredStatement) stmt;
            return typeAwareStatement.getType().rawArgument();
        }
        else {
            return "";
        }
    }

    private String getIfFeatures(DocumentedDeclaredStatement.WithStatus<?> stmt) {
        if (stmt instanceof IfFeatureAwareDeclaredStatement<?>) {
            final IfFeatureAwareDeclaredStatement<?> ifFeatureAwareStatement =
                    (IfFeatureAwareDeclaredStatement<?>) stmt;
            final var ifFeatures = ifFeatureAwareStatement.getIfFeatures()
                    .stream().map((statement) ->
                            statement.rawArgument() == null ? "" : statement.rawArgument())
                    .toList();
            if (!ifFeatures.isEmpty()) {
                return "{" + String.join(" ", ifFeatures) + "}?";
            }
            else {
                return "";
            }
        }
        else {
            return "";
        }
    }

    private void pushStatement(Iterator<? extends DeclaredStatement<?>> stmt) {
        if (stmt.hasNext()) {
            indent.push("|  ");
        }
        else {
            indent.push("   ");
        }
        stack.push(stmt);
    }

    private Iterator<? extends DeclaredStatement<?>> pollStatement() {
        indent.pop();
        return  stack.poll();
    }
}
