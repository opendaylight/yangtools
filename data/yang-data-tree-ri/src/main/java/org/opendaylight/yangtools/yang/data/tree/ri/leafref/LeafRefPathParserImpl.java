/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.ri.leafref;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.PathExpression.DerefSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.LocationPathSteps;
import org.opendaylight.yangtools.yang.model.api.PathExpression.Steps;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Relative;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.Step;
import org.opendaylight.yangtools.yang.xpath.api.YangPathExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangQNameExpr;

final class LeafRefPathParserImpl {
    private final QNameModule leafrefModule;
    private final QNameModule nodeModule;

    LeafRefPathParserImpl(final LeafrefTypeDefinition leafrefType, final TypedDataSchemaNode currentNode) {
        // FIXME: these two namespaces look not quite right:
        //        - leafrefModule is used for absolute paths, irrespective of where they occur
        //        - nodeModule is used for relative paths, irrespective of where they occur
        //
        // There is little in RFC7950 which would hint at such a distinction and if even if it were true, it would be
        // the job of YANG parser to ensure that absolute paths are bound during parsing.
        //
        // The only distinction is relative to where the leafref is defined, namely:
        //
        // 1) as per section 9.9.2:
        //     o  If the "path" statement is defined within a typedef, the context
        //        node is the leaf or leaf-list node in the data tree that
        //        references the typedef.
        //
        //     o  Otherwise, the context node is the node in the data tree for which
        //        the "path" statement is defined.
        //
        // 2) as per section 6.4.1:
        //     o  Names without a namespace prefix belong to the same namespace as
        //        the identifier of the current node.  Inside a grouping, that
        //        namespace is affected by where the grouping is used (see
        //        Section 7.13).  Inside a typedef, that namespace is affected by
        //        where the typedef is referenced.  If a typedef is defined and
        //        referenced within a grouping, the namespace is affected by where
        //        the grouping is used (see Section 7.13).
        leafrefModule = getBaseModule(leafrefType);
        nodeModule = currentNode.getQName().getModule();
    }

    LeafRefPath parseLeafRefPath(final PathExpression path) {
        final Steps steps = path.getSteps();
        if (steps instanceof LocationPathSteps) {
            return parseLocationPath(((LocationPathSteps) steps).getLocationPath());
        } else if (steps instanceof DerefSteps) {
            throw new UnsupportedOperationException("deref() leafrefs are not implemented yet");
        } else {
            throw new IllegalStateException("Unsupported steps " + steps);
        }
    }

    private LeafRefPath parseLocationPath(final YangLocationPath locationPath) {
        return LeafRefPath.create(
            createPathSteps(locationPath.isAbsolute() ? leafrefModule : nodeModule, locationPath.getSteps()),
            locationPath.isAbsolute());
    }

    private static Deque<QNameWithPredicate> createPathSteps(final QNameModule localModule,
            final ImmutableList<Step> steps) {
        final Deque<QNameWithPredicate> path = new ArrayDeque<>(steps.size());
        for (Step step : steps) {
            switch (step.getAxis()) {
                case CHILD:
                    checkState(step instanceof QNameStep, "Unsupported step %s", step);
                    path.add(adaptChildStep((QNameStep) step, localModule));
                    break;
                case PARENT:
                    path.add(QNameWithPredicate.UP_PARENT);
                    break;
                default:
                    throw new IllegalStateException("Unsupported axis in step " + step);
            }
        }
        return path;
    }

    private static QNameWithPredicate adaptChildStep(final QNameStep step, final QNameModule localModule) {
        final QName qname = resolve(step.getQName(), localModule);
        final Set<YangExpr> predicates = step.getPredicates();
        if (predicates.isEmpty()) {
            return new SimpleQNameWithPredicate(qname);
        }

        final QNameWithPredicateBuilder builder = new QNameWithPredicateBuilder(qname.getModule(),
            qname.getLocalName());

        for (YangExpr pred : predicates) {
            final QNamePredicateBuilder predBuilder = new QNamePredicateBuilder();

            if (pred instanceof YangBinaryExpr) {
                final YangBinaryExpr eqPred = (YangBinaryExpr) pred;
                checkState(eqPred.getOperator() == YangBinaryOperator.EQUALS);

                final YangExpr left = eqPred.getLeftExpr();
                checkState(left instanceof YangQNameExpr, "Unsupported left expression %s", left);
                predBuilder.setIdentifier(resolve(((YangQNameExpr) left).getQName(), localModule));

                final YangExpr right = eqPred.getRightExpr();
                if (right instanceof YangPathExpr) {
                    final YangPathExpr rightPath = (YangPathExpr) right;
                    final YangExpr filter = rightPath.getFilterExpr();
                    if (filter instanceof YangFunctionCallExpr) {
                        checkState(YangFunction.CURRENT.getIdentifier().equals(
                            ((YangFunctionCallExpr) filter).getName()));
                    } else {
                        throw new IllegalStateException("Unhandled filter " + filter);
                    }

                    final Relative location = rightPath.getLocationPath()
                            .orElseThrow(() -> new IllegalStateException("Missing locationPath in " + rightPath));
                    predBuilder.setPathKeyExpression(LeafRefPath.create(
                        createPathSteps(localModule, location.getSteps()), false));
                } else {
                    throw new UnsupportedOperationException("Not implemented for " + right);
                }
            }

            builder.addQNamePredicate(predBuilder.build());
        }

        return builder.build();
    }

    private static QName resolve(final AbstractQName qname, final QNameModule localModule) {
        if (qname instanceof QName) {
            return (QName) qname;
        } else if (qname instanceof Unqualified) {
            // Bind to namespace. Note we expect to perform frequent matching, hence we are interning the result
            return ((Unqualified) qname).bindTo(localModule).intern();
        } else {
            throw new IllegalStateException("Unhandled unresolved QName " + qname);
        }
    }

    /**
     * Find the first definition of supplied leafref type and return the module which contains this definition.
     */
    private static QNameModule getBaseModule(final LeafrefTypeDefinition leafrefType) {
        LeafrefTypeDefinition current = leafrefType;
        while (true) {
            final LeafrefTypeDefinition base = current.getBaseType();
            if (base == null) {
                return current.getQName().getModule();
            }
            current = base;
        }
    }
}
