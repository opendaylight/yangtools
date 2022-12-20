/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

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
import org.opendaylight.yangtools.yang.model.api.stmt.AugmentStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.CaseStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ConfigStatementAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DataDefinitionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.DocumentedDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.GroupingStatement;
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
import org.opendaylight.yangtools.yang.model.api.stmt.RootDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RpcStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.StatusStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeAwareDeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UsesStatement;

public class YangOrganizationTreeIterator extends AbstractIterator<@NonNull String> {
    private final Deque<Iterator<? extends DeclaredStatement<?>>> stack = new ArrayDeque<>(16);
    private final Stack<DeclaredStatement<?>> statementStack = new Stack<>();
    private final Iterator<? extends RootDeclaredStatement> rootIterator;
    private final Iterator<? extends AugmentStatement> augmentIterator;
    private final Iterator<? extends RpcStatement> rpcIterator;
    private final Iterator<? extends NotificationStatement> notificationIterator;
    private final Iterator<? extends GroupingStatement> groupingIterator;
    private final Stack<String> indent = new Stack<>();

    private boolean printGap = false;
    private final int depth;

    private enum IndentType {
        SPACE,
        LINE,
        INITIAL
    }

    YangOrganizationTreeIterator(final DeclaredStatement<?> stmt, final StatementPrefixResolver resolver,
            final Set<StatementDefinition> ignoredStatements, final int depth) {
        if (stmt instanceof RootDeclaredStatement rootStatement) {
            rootIterator = List.of(rootStatement).iterator();
            augmentIterator = rootStatement.getAugments().iterator();
            rpcIterator = rootStatement.getRpcs().iterator();
            notificationIterator = rootStatement.getNotifications().iterator();
            groupingIterator = rootStatement.getGroupings().iterator();
        } else {
            throw new IllegalArgumentException();
        }
        this.depth = depth;
    }

    @Override
    protected @NonNull String computeNext() {

        while (true) {
            if (stack.isEmpty()) {
                if (rootIterator.hasNext()) {
                    pushStatement(rootIterator, null, IndentType.INITIAL);
                }
                else if (augmentIterator.hasNext()) {
                    indent.push(" ");
                    pushStatement(augmentIterator, null, IndentType.SPACE);
                    printGap = true;
                } else if (rpcIterator.hasNext()) {
                    indent.clear();
                    indent.push("    ");
                    pushStatement(rpcIterator, null, IndentType.SPACE);
                    return "\n  rpcs:\n";
                } else if (notificationIterator.hasNext()) {
                    indent.clear();
                    indent.push("    ");
                    pushStatement(notificationIterator, null, IndentType.SPACE);
                    return "\n  notifications:\n";
                } else if (groupingIterator.hasNext()) {
                    indent.clear();
                    indent.push(" ");
                    pushStatement(groupingIterator, null, IndentType.SPACE);
                    printGap = true;
                } else {
                    endOfData();
                    return "";
                }
            }

            final DeclaredStatement<?> parentStatement = statementStack.peek();
            final Iterator<? extends DeclaredStatement<?>> it = pollStatement();

            if (stack.size() > depth) {
                if (it.hasNext()) {
                    return String.join("", indent) + "...\n";
                }
                else {
                    return "";
                }
            }

            while (it.hasNext()) {
                final DeclaredStatement<?> currentStatement = it.next();

                final String currentIndent = String.join("", indent);

                if (currentStatement instanceof ModuleStatement) {
                    pushStatement(it, parentStatement, IndentType.INITIAL);
                    pushStatement(getTopLevelSubStatements(currentStatement), currentStatement, IndentType.LINE);
                    return "module: " + currentStatement.rawArgument() + "\n";
                } else if (currentStatement instanceof SubmoduleStatement) {
                    pushStatement(it, parentStatement, IndentType.INITIAL);
                    pushStatement(getTopLevelSubStatements(currentStatement), currentStatement, IndentType.LINE);
                    return "submodule: " + currentStatement.rawArgument() + "\n";
                } else if (currentStatement instanceof AugmentStatement augmentStatement) {
                    pushStatement(it, parentStatement, IndentType.SPACE);
                    pushStatement(getTrimmedSubStatements(augmentStatement), currentStatement, IndentType.LINE);
                    return printGap() + "  augment " + augmentStatement.rawArgument() + ":\n";
                } else if (currentStatement instanceof RpcStatement rpcStatement) {
                    pushStatement(it, parentStatement, IndentType.LINE);
                    final String result = composeLine(currentStatement, currentIndent);
                    pushStatement(getRpcSubStatements(rpcStatement), currentStatement, IndentType.LINE);
                    return result;
                } else if (currentStatement instanceof InputStatement inputStatement) {
                    pushStatement(it, parentStatement, IndentType.LINE);
                    final String result = composeLine(currentStatement, currentIndent);
                    pushStatement(getTrimmedSubStatements(inputStatement), currentStatement, IndentType.LINE);
                    return result;
                } else if (currentStatement instanceof OutputStatement outputStatement) {
                    pushStatement(it, parentStatement, IndentType.LINE);
                    final String result = composeLine(currentStatement, currentIndent);
                    pushStatement(getTrimmedSubStatements(outputStatement), currentStatement, IndentType.LINE);
                    return result;
                } else if (currentStatement instanceof GroupingStatement groupingStatement) {
                    pushStatement(it, parentStatement, IndentType.SPACE);
                    pushStatement(getTrimmedSubStatements(groupingStatement), currentStatement, IndentType.LINE);
                    return printGap() + "  grouping " + groupingStatement.rawArgument() + ":\n";
                } else {
                    pushStatement(it, parentStatement, IndentType.LINE);
                    final String result = composeLine(currentStatement, currentIndent);
                    pushStatement(getTrimmedSubStatements(currentStatement), currentStatement, IndentType.LINE);
                    return result;
                }
            }
        }
    }

    private String composeLine(DeclaredStatement<?> stmt, final String currentIndent) {
        return currentIndent
                + getStatementStatus(stmt)
                + "--"
                + getStatementFlags(stmt)
                + getName(stmt)
                + getOpts(stmt)
                + " "
                + getType(stmt)
                + " "
                + getIfFeatures(stmt)
                + "\n";
    }

    private Iterator<? extends DeclaredStatement<?>> getTrimmedSubStatements(DeclaredStatement<?> stmt) {
        return stmt.declaredSubstatements().stream()
                .filter(statement ->
                                (statement instanceof DataDefinitionStatement)
                                || (statement instanceof OperationDeclaredStatement)
                                || (statement instanceof NotificationStatement))
                .iterator();
    }

    private Iterator<? extends DeclaredStatement<?>> getTopLevelSubStatements(DeclaredStatement<?> stmt) {
        return stmt.declaredSubstatements().stream()
                .filter(statement -> statement instanceof DataDefinitionStatement)
                .iterator();
    }

    private Iterator<? extends DeclaredStatement<?>> getRpcSubStatements(RpcStatement stmt) {
        return stmt.declaredSubstatements().stream()
                .filter(statement ->
                                (statement instanceof GroupingStatement)
                                        || (statement instanceof InputStatement)
                                        || (statement instanceof OutputStatement))
                .iterator();
    }

    private String getStatementStatus(DeclaredStatement<?> stmt) {
        if (stmt instanceof DocumentedDeclaredStatement.WithStatus<?> statement) {
            final Optional<StatusStatement> statusStatement = statement.getStatus();

            if (statusStatement.isEmpty()) {
                return getStatementStatusFromParentStatements();
            } else if (statusStatement.orElseThrow().rawArgument().equals("deprecated")) {
                return "x";
            } else if (statusStatement.orElseThrow().rawArgument().equals("obsolete")) {
                return "o";
            }
            return "+";
        }
        return "+";
    }

    private String getStatementStatusFromParentStatements() {
        for (DeclaredStatement<?> statement : statementStack) {
            if (statement instanceof DocumentedDeclaredStatement.WithStatus<?> statusAware) {
                final Optional<StatusStatement> statusStatement = statusAware.getStatus();

                if (statusStatement.isEmpty()) {
                    continue;
                } else if (statusStatement.orElseThrow().rawArgument().equals("deprecated")) {
                    return "x";
                } else if (statusStatement.orElseThrow().rawArgument().equals("obsolete")) {
                    return "o";
                }
                return "+";
            }
        }
        return "+";
    }

    private String getStatementFlags(DeclaredStatement<?> stmt) {
        if (stmt instanceof CaseStatement) {
            return "";
        } else if (stmt instanceof DataDefinitionStatement) {
            if (stmt instanceof ConfigStatementAwareDeclaredStatement<?> configAware) {
                final Optional<ConfigStatement> optConfig = configAware.getConfig();

                if (optConfig.isPresent()) {
                    if (optConfig.orElseThrow().argument()) {
                        return "ro";
                    }
                    return "rw";
                }
                return getStatementFlagsFromParentStatements();

            } else if (stmt instanceof UsesStatement) {
                return "-u";
            }
            return "";
        } else if (stmt instanceof OperationDeclaredStatement) {
            return "-x";
        } else if (stmt instanceof InputStatement) {
            return "-w";
        } else if (stmt instanceof OutputStatement) {
            return "ro";
        } else if (stmt instanceof NotificationStatement) {
            return "-n";
        }
        return "UU";
    }

    private String getStatementFlagsFromParentStatements() {
        for (DeclaredStatement<?> parentStatement : statementStack) {
            if (parentStatement instanceof ConfigStatementAwareDeclaredStatement<?> configAware) {

                final Optional<ConfigStatement> optConfig = configAware.getConfig();
                if (optConfig.isPresent()) {
                    if (optConfig.orElseThrow().argument()) {
                        return "ro";
                    }
                    return "rw";
                }
            }
            else if (parentStatement instanceof InputStatement) {
                return "-w";
            }
            else if (parentStatement instanceof OutputStatement || parentStatement instanceof NotificationStatement) {
                return "ro";
            }
        }
        return "rw";
    }

    private String getName(DeclaredStatement<?> stmt) {
        if (stmt instanceof ChoiceStatement) {
            return " (" + stmt.rawArgument() + ")";
        } else if (stmt instanceof CaseStatement) {
            return ":(" + stmt.rawArgument() + ")";
        }
        return " " + stmt.rawArgument();
    }

    private String getOpts(DeclaredStatement<?> stmt) {
        if (stmt instanceof MandatoryStatementAwareDeclaredStatement<?> mandatoryAwareStatement) {
            final var optionalMandatoryStatement = mandatoryAwareStatement.getMandatory();
            if (optionalMandatoryStatement.isPresent()) {
                if (!optionalMandatoryStatement.orElseThrow().argument()) {
                    return "?";
                }
                return "";
            }
            else if (statementStack.peek() instanceof ListStatement listStatement) {
                final var keys = List.of(listStatement.getKey().rawArgument().split(" "));
                if (keys.contains(stmt.rawArgument())) {
                    return "";
                }
                return "?";
            }
            return "?";
        }
        else if (stmt instanceof ContainerStatement containerStatement) {
            final PresenceStatement presenceStatement = containerStatement.getPresence();
            if (presenceStatement == null) {
                return "";
            } else if (presenceStatement.argument().equals("true")) {
                return "!";
            }
            return "";
        } else if (stmt instanceof LeafListStatement) {
            return "*";
        } else if (stmt instanceof ListStatement listStatement) {
            final KeyStatement keyStatement = listStatement.getKey();
            return "* [" + keyStatement.rawArgument() + "]";
        }
        return "";
    }

    private String getType(DeclaredStatement<?> stmt) {
        if (stmt instanceof TypeAwareDeclaredStatement typeAwareStatement) {
            return typeAwareStatement.getType().rawArgument();
        }
        return "";
    }

    private String getIfFeatures(DeclaredStatement<?> stmt) {
        if (stmt instanceof IfFeatureAwareDeclaredStatement<?> ifFeatureAwareStatement) {
            final var ifFeatures = ifFeatureAwareStatement.getIfFeatures()
                    .stream().map((statement) ->
                            statement.rawArgument() == null ? "" : statement.rawArgument())
                    .toList();
            if (!ifFeatures.isEmpty()) {
                return "{" + String.join(" ", ifFeatures) + "}?";
            }
            return "";
        }
        return "";
    }

    private String printGap() {
        if (printGap) {
            printGap = false;
            return "\n";
        }
        return "";
    }

    private void pushStatement(Iterator<? extends DeclaredStatement<?>> stmtIterator,
            DeclaredStatement<?> stmt, IndentType indentType) {
        if (indentType == IndentType.SPACE) {
            indent.push("   ");
        }
        else if (indentType == IndentType.INITIAL) {
            indent.push("  ");
        } else if (stmtIterator.hasNext()) {
            indent.push("|  ");
        }
        else {
            indent.push("   ");
        }

        stack.push(stmtIterator);
        statementStack.push(stmt);
    }

    private Iterator<? extends DeclaredStatement<?>> pollStatement() {
        indent.pop();
        statementStack.pop();
        return  stack.poll();
    }
}
