/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.leafref;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.ArrayDeque;
import org.opendaylight.yangtools.yang.common.AbstractQName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Qualified;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangBinaryOperator;
import org.opendaylight.yangtools.yang.xpath.api.YangFunction;
import org.opendaylight.yangtools.yang.xpath.api.YangFunctionCallExpr;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath;
import org.opendaylight.yangtools.yang.xpath.api.YangLocationPath.QNameStep;
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
        return switch (path) {
            case PathExpression.LocationPath location -> parseLocationPath(location.locationPath());
            case PathExpression.Deref deref ->
                throw new UnsupportedOperationException("deref() leafrefs are not implemented yet");
        };
    }

    private LeafRefPath parseLocationPath(final YangLocationPath locationPath) {
        return LeafRefPath.create(
            createPathSteps(locationPath.isAbsolute() ? leafrefModule : nodeModule, locationPath.getSteps()),
            locationPath.isAbsolute());
    }

    private static ArrayDeque<QNameWithPredicate> createPathSteps(final QNameModule localModule,
            final ImmutableList<Step> steps) {
        final var path = new ArrayDeque<QNameWithPredicate>(steps.size());
        for (var step : steps) {
            switch (step.getAxis()) {
                case CHILD -> {
                    if (step instanceof QNameStep qname) {
                        path.add(adaptChildStep(qname, localModule));
                    } else {
                        throw new IllegalStateException("Unsupported step " + step);
                    }
                }
                case PARENT -> path.add(QNameWithPredicate.UP_PARENT);
                default -> throw new IllegalStateException("Unsupported axis in step " + step);
            }
        }
        return path;
    }

    private static QNameWithPredicate adaptChildStep(final QNameStep step, final QNameModule localModule) {
        final var qname = resolve(step.getQName(), localModule);
        final var predicates = step.getPredicates();
        if (predicates.isEmpty()) {
            return new SimpleQNameWithPredicate(qname);
        }

        final var builder = new QNameWithPredicateBuilder(qname.getModule(), qname.getLocalName());

        for (var pred : predicates) {
            final var predBuilder = new QNamePredicateBuilder();

            if (pred instanceof YangBinaryExpr eqPred) {
                checkState(eqPred.getOperator() == YangBinaryOperator.EQUALS);

                final var left = eqPred.getLeftExpr();
                checkState(left instanceof YangQNameExpr, "Unsupported left expression %s", left);
                predBuilder.setIdentifier(resolve(((YangQNameExpr) left).getQName(), localModule));

                final var right = eqPred.getRightExpr();
                switch (right) {
                    case YangPathExpr rightPath -> {
                        final var filter = rightPath.getFilterExpr();
                        switch (filter) {
                            case YangFunctionCallExpr call -> {
                                checkState(YangFunction.CURRENT.getIdentifier().equals(call.getName()));
                                final var location = rightPath.getLocationPath().orElseThrow(
                                    () -> new IllegalStateException("Missing locationPath in " + rightPath));
                                predBuilder.setPathKeyExpression(LeafRefPath.create(
                                    createPathSteps(localModule, location.getSteps()), false));
                            }
                            default -> throw new IllegalStateException("Unhandled filter " + filter);
                        }
                    }
                    default -> throw new UnsupportedOperationException("Not implemented for " + right);
                }
            }

            builder.addQNamePredicate(predBuilder.build());
        }

        return builder.build();
    }

    private static QName resolve(final AbstractQName aqname, final QNameModule localModule) {
        return switch (aqname) {
            case QName qname -> qname;
            case Qualified qualified -> throw new IllegalStateException("Unhandled unresolved QName " + qualified);
            // Bind to namespace. Note we expect to perform frequent matching, hence we are interning the result
            case Unqualified unqualified -> unqualified.bindTo(localModule).intern();
        };
    }

    /**
     * Find the first definition of supplied leafref type and return the module which contains this definition.
     */
    private static QNameModule getBaseModule(final LeafrefTypeDefinition leafrefType) {
        var current = leafrefType;
        while (true) {
            final var base = current.getBaseType();
            if (base == null) {
                return current.getQName().getModule();
            }
            current = base;
        }
    }
}
