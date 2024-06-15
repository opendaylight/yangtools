/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.SequencedMap;
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
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidate.CandidateNode.WithChildren;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeCandidateNode;

/**
 * Immutable implementations of {@link CandidateNode} contracts.
 */
@NonNullByDefault
public final class ImmutableCandidateNodes {
    private ImmutableCandidateNodes() {
        // Hidden on purpose
    }

    public static Appeared appeared(final NormalizedNode dataAfter, final WithChildrenImpl children) {
        return new AppearedImpl(dataAfter, children);
    }

    public static Appeared appeared(final NormalizedNode dataAfter, final Map<PathArgument, CandidateNode> children) {
        return appeared(dataAfter, MapWithChildrenImpl.of(children));
    }

    public static Created created(final NormalizedNode dataAfter) {
        return new CreatedImpl(dataAfter);
    }

    public static Deleted deleted(final NormalizedNode dataBefore) {
        return new DeletedImpl(dataBefore);
    }

    public static Disappeared disappeared(final NormalizedNode dataBefore, final WithChildrenImpl children) {
        return new DisappearedImpl(dataBefore, children);
    }

    public static Disappeared disappeared(final NormalizedNode dataBefore,
            final Map<PathArgument, CandidateNode> children) {
        return disappeared(dataBefore, WithChildrenImpl.of(children));
    }

    public static Modified modified(final NormalizedNode dataBefore, final NormalizedNode dataAfter,
            final WithChildrenImpl children) {
        return new ModifiedImpl(dataBefore, dataAfter, children);
    }

    public static Modified modified(final NormalizedNode dataBefore, final NormalizedNode dataAfter,
            final Map<PathArgument, CandidateNode> children) {
        return new ModifiedImpl(dataBefore, dataAfter, children);
    }

    public static Replaced replaced(final NormalizedNode dataBefore, final NormalizedNode dataAfter) {
        return new ReplacedImpl(dataBefore, dataAfter);
    }

    public static Unmodified unmodified(final NormalizedNode dataAfter) {
        return new UnmodifiedImpl(dataAfter);
    }

    /**
     * Provider interface mirroring {@link WithChildren}.
     */
    public interface WithChildrenImpl {
        /**
         * Implementation of {@link WithChildren#children()}.
         *
         * @return Unmodifiable collection of modified child nodes.
         */
        Collection<CandidateNode> childrenImpl();

        /**
         * Implementation of {@link WithChildren#modifiedChild(PathArgument)}.
         *
         * @param arg {@link PathArgument} of child node
         * @return Modified child or {@code null} if the specified child has not been modified
         * @throws NullPointerException if {@code childNamez} is {@code null}
         */
        @Nullable CandidateNode modifiedChild(PathArgument arg);

        static WithChildrenImpl of() {
            return MapWithChildrenImpl.EMPTY;
        }

        static WithChildrenImpl of(final CandidateNode child) {
            return new SingletonWithChildrenImpl(child);
        }

        static WithChildrenImpl of(final CandidateNode... children) {
            return of(Arrays.asList(children));
        }

        static WithChildrenImpl of(final Collection<CandidateNode> children) {
            return of(Maps.uniqueIndex(children, CandidateNode::name));
        }

        static WithChildrenImpl of(final Map<PathArgument, CandidateNode> map) {
            return switch (map.size()) {
                case 0 -> of();
                case 1 -> {
                    final var child = map instanceof SequencedMap<PathArgument, CandidateNode> seq
                        ? seq.sequencedValues().getFirst()
                        : map.values().iterator().next();
                    yield of(child);
                }
                default -> new MapWithChildrenImpl(map);
            };
        }
    }

    private record SingletonWithChildrenImpl(CandidateNode child) implements WithChildrenImpl {
        SingletonWithChildrenImpl {
            requireNonNull(child);
        }


    }

    private record MapWithChildrenImpl(Map<PathArgument, CandidateNode> map) implements WithChildrenImpl {
        static final MapWithChildrenImpl EMPTY = new MapWithChildrenImpl(Map.of());

        MapWithChildrenImpl {
            requireNonNull(map);
        }

        @Override
        public Collection<CandidateNode> childrenImpl() {
            return map.values();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return map.get(arg);
        }
    }

    @Deprecated
    private record DataTreeCandidateNodeCompat(
            @Nullable NormalizedNode dataBefore,
            @Nullable NormalizedNode dataAfter,
            @Nullable WithChildrenImpl childrenImpl) implements DataTreeCandidateNode {

    }


    private record AppearedImpl(NormalizedNode dataAfter, WithChildrenImpl childrenImpl)
            implements Appeared {
        AppearedImpl {
            requireNonNull(dataAfter);
            requireNonNull(childrenImpl);
        }

        @Override
        public Collection<CandidateNode> children() {
            return childrenImpl.childrenImpl();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return childrenImpl.modifiedChild(arg);
        }

        @Override
        @Deprecated
        public DataTreeCandidateNode toLegacy() {
            return new DataTreeCandidateNodeCompat(null, dataAfter, childrenImpl);
        }
    }

    private record CreatedImpl(NormalizedNode dataAfter) implements Created {
        CreatedImpl {
            requireNonNull(dataAfter);
        }
    }

    private record DeletedImpl(NormalizedNode dataBefore) implements Deleted {
        DeletedImpl {
            requireNonNull(dataBefore);
        }
    }

    private record DisappearedImpl(NormalizedNode dataBefore, WithChildrenImpl map)
            implements Disappeared {
        DisappearedImpl {
            requireNonNull(dataBefore);
            requireNonNull(map);
        }

        @Override
        public Collection<CandidateNode> children() {
            return map.childrenImpl();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return map.modifiedChild(arg);
        }
    }

    private record ModifiedImpl(
            NormalizedNode dataBefore,
            NormalizedNode dataAfter,
            WithChildrenImpl childrenImpl) implements Modified {
        ModifiedImpl {
            requireNonNull(dataBefore);
            requireNonNull(dataAfter);
            requireNonNull(childrenImpl);
        }

        @Override
        public Collection<CandidateNode> children() {
            return childrenImpl.childrenImpl();
        }

        @Override
        public @Nullable CandidateNode modifiedChild(final PathArgument arg) {
            return childrenImpl.modifiedChild(arg);
        }
    }

    private record ReplacedImpl(NormalizedNode dataBefore, NormalizedNode dataAfter) implements Replaced {
        ReplacedImpl {
            requireNonNull(dataBefore);
            requireNonNull(dataAfter);
        }
    }

    private record UnmodifiedImpl(NormalizedNode dataAfter) implements Unmodified {
        UnmodifiedImpl {
            requireNonNull(dataAfter);
        }
    }
}
