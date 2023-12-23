/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNodes;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MandatoryLeafEnforcer implements Immutable {
    private static final Logger LOG = LoggerFactory.getLogger(MandatoryLeafEnforcer.class);

    // FIXME: Well, there is still room to optimize footprint and performance. This list of lists can have overlaps,
    //        i.e. in the case of:
    //
    //          container foo {
    //            presence "something";
    //            container bar {
    //              leaf baz { type string; mandatory true; }
    //              leaf xyzzy { type string; mandatory true; }
    //            }
    //          }
    //
    //        we will have populated:
    //          [ foo, bar ]
    //          [ foo, xyzzy ]
    //        and end up looking up 'foo' twice. A better alternative to the inner list would be a recursive tree
    //        structure with terminal and non-terminal nodes:
    //
    //          non-terminal(foo):
    //            terminal(bar)
    //            terminal(xyzzy)
    //
    //        And then we would have a List of (unique) mandatory immediate descendants -- and recurse through them.
    //
    //        For absolute best footprint this means keeping a PathArgument (for terminal) and perhaps an
    //        ImmutableList<Object> (for non-terminal) nodes -- since PathArgument and ImmutableList are disjunct, we
    //        can make a quick instanceof determination to guide us.
    //
    //        We then can short-circuit to using NormalizedNodes.getDirectChild(), or better yet, we can talk directly
    //        to DistinctNodeContainer.childByArg() -- which seems to be the context in which we are invoked.
    //        At any rate, we would perform lookups step by step -- and throw an exception when to find a child.
    //        Obviously in that case we need to reconstruct the full path -- but that is an error path and we can expend
    //        some time to get at that. A simple Deque<PathArgument> seems like a reasonable compromise to track what we
    //        went through.
    private final ImmutableList<ImmutableList<PathArgument>> mandatoryNodes;

    private MandatoryLeafEnforcer(final ImmutableList<ImmutableList<PathArgument>> mandatoryNodes) {
        this.mandatoryNodes = requireNonNull(mandatoryNodes);
    }

    public static @Nullable MandatoryLeafEnforcer forContainer(final DataNodeContainer schema,
            final boolean includeConfigFalse) {

        final var builder = ImmutableList.<ImmutableList<PathArgument>>builder();
        findMandatoryNodes(builder, YangInstanceIdentifier.of(), schema, includeConfigFalse);
        final var mandatoryNodes = builder.build();
        return mandatoryNodes.isEmpty() ? null : new MandatoryLeafEnforcer(mandatoryNodes);
    }

    public void enforceOnData(final NormalizedNode data) {
        for (var path : mandatoryNodes) {
            if (NormalizedNodes.findNode(data, path).isEmpty()) {
                throw new IllegalArgumentException("Node " + data.name() + " is missing mandatory descendant "
                    + YangInstanceIdentifier.of(path));
            }
        }
    }

    private static void findMandatoryNodes(final Builder<ImmutableList<PathArgument>> builder,
            final YangInstanceIdentifier id, final DataNodeContainer schema, final boolean includeConfigFalse) {
        for (var child : schema.getChildNodes()) {
            if (includeConfigFalse || child.effectiveConfig().orElse(Boolean.TRUE)) {
                if (child instanceof ContainerSchemaNode container) {
                    if (!container.isPresenceContainer()) {
                        // the container is either:
                        //    - not in an augmented subtree and not augmenting
                        //    - in an augmented subtree
                        // in both cases just append the NodeID to the ongoing ID and continue the search.
                        findMandatoryNodes(builder, id.node(NodeIdentifier.create(container.getQName())), container,
                            includeConfigFalse);
                    }
                } else {
                    boolean needEnforce = child instanceof MandatoryAware aware && aware.isMandatory();
                    if (!needEnforce && child instanceof ElementCountConstraintAware aware) {
                        needEnforce = aware.getElementCountConstraint()
                            .map(constraint -> {
                                final Integer min = constraint.getMinElements();
                                return min != null && min > 0;
                            })
                            .orElse(Boolean.FALSE);
                    }
                    if (needEnforce) {
                        final var desc = id.node(NodeIdentifier.create(child.getQName()));
                        LOG.debug("Adding mandatory child {}", desc);
                        builder.add(ImmutableList.copyOf(desc.getPathArguments()));
                    }
                }
            }
        }
    }
}
