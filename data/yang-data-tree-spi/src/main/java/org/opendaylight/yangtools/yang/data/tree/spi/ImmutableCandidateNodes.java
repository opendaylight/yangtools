/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Appeared;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Created;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Deleted;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Disappeared;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Modified;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Replaced;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.Unmodified;

/**
 * Immutable implementations of {@link CandidateNode} contracts.
 */
@NonNullByDefault
public final class ImmutableCandidateNodes {
    private ImmutableCandidateNodes() {
        // Hidden on purpose
    }

    public static CandidateNode.Appeared appeared(final NormalizedNode dataAfter,
            final Map<PathArgument, CandidateNode> children) {
        return new ImmutableAppeared(dataAfter, children);
    }

    public static CandidateNode.Created created(final NormalizedNode dataAfter) {
        return new ImmutableCreated(dataAfter);
    }

    public static CandidateNode.Deleted deleted(final NormalizedNode dataBefore) {
        return new ImmutableDeleted(dataBefore);
    }

    public static CandidateNode.Disappeared disappeared(final NormalizedNode dataBefore,
            final Map<PathArgument, CandidateNode> children) {
        return new ImmutableDisappeared(dataBefore, children);
    }

    public static CandidateNode.Modified modified(final NormalizedNode dataBefore, final NormalizedNode dataAfter,
            final Map<PathArgument, CandidateNode> children) {
        return new ImmutableModified(dataBefore, dataAfter, children);
    }

    public static CandidateNode.Replaced replaced(final NormalizedNode dataBefore, final NormalizedNode dataAfter) {
        return new ImmutableReplaced(dataBefore, dataAfter);
    }

    public static CandidateNode.Unmodified unmodified(final NormalizedNode dataAfter) {
        return new ImmutableUnmodified(dataAfter);
    }

    private record ImmutableAppeared(NormalizedNode dataAfter, Map<PathArgument, CandidateNode> map)
            implements Appeared {
        ImmutableAppeared {
            requireNonNull(dataAfter);
            requireNonNull(map);
        }

        @Override
        public Collection<CandidateNode> children() {
            return map.values();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument childName) {
            return map.get(childName);
        }
    }

    private record ImmutableCreated(NormalizedNode dataAfter) implements Created {
        ImmutableCreated {
            requireNonNull(dataAfter);
        }
    }

    private record ImmutableDeleted(NormalizedNode dataBefore) implements Deleted {
        ImmutableDeleted {
            requireNonNull(dataBefore);
        }
    }

    private record ImmutableDisappeared(NormalizedNode dataBefore, Map<PathArgument, CandidateNode> map)
            implements Disappeared {
        ImmutableDisappeared {
            requireNonNull(dataBefore);
            requireNonNull(map);
        }

        @Override
        public Collection<CandidateNode> children() {
            return map.values();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return map.get(arg);
        }
    }

    private record ImmutableModified(
            NormalizedNode dataBefore,
            NormalizedNode dataAfter,
            Map<PathArgument, CandidateNode> map) implements Modified {
        ImmutableModified {
            requireNonNull(dataBefore);
            requireNonNull(dataAfter);
            requireNonNull(map);
        }

        @Override
        public Collection<CandidateNode> children() {
            return map.values();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return map.get(arg);
        }
    }

    private record ImmutableReplaced(NormalizedNode dataBefore, NormalizedNode dataAfter) implements Replaced {
        ImmutableReplaced {
            requireNonNull(dataBefore);
            requireNonNull(dataAfter);
        }
    }

    private record ImmutableUnmodified(NormalizedNode dataAfter) implements Unmodified {
        ImmutableUnmodified {
            requireNonNull(dataAfter);
        }
    }
}
