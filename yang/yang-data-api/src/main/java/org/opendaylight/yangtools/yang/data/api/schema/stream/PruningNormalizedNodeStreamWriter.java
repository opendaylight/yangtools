/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.stream;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import java.io.IOException;
import javax.xml.transform.dom.DOMSource;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;

/**
 * A simple NormalizedNodeStreamWriter, which forwards events to a delegate. Unlike
 * {@link ForwardingNormalizedNodeStreamWriter}, which allows customization of individual events, this class works
 * towards pruning subtrees from a stream.
 */
@Beta
public final class PruningNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    /**
     * Filter consulted when a new node is entered or exited. The idea is that the filter can be stateful and track
     * progress through the stream and make decisions to prune subtrees.
     */
    @NonNullByDefault
    public interface Filter {
        /**
         * Make a decision on entering a specific node, if the subtree is not actively being filtered. If this method
         * returns {@code true} it will not be consulted until a sibling node is encountered, i.e. the entire subtree
         * identified by current state plus {@code name} will be pruned.
         *
         * @param name Node identifiers
         * @return {@code true} if the node and its associated events should be allowed, false otherwise.
         */
        boolean enterNode(PathArgument name);

        /**
         * Invoked when a previously-unfiltered subtree level is exited.
         */
        void exitNode();
    }

    private final NormalizedNodeStreamWriter delegate;
    private final Filter filter;

    private ClassToInstanceMap<NormalizedNodeStreamWriterExtension> extensions;
    private int depth;

    public PruningNormalizedNodeStreamWriter(final NormalizedNodeStreamWriter delegate, final Filter filter) {
        this.delegate = requireNonNull(delegate);
        this.filter = requireNonNull(filter);
    }

    @Override
    public ClassToInstanceMap<NormalizedNodeStreamWriterExtension> getExtensions() {
        if (extensions == null) {
            final ClassToInstanceMap<NormalizedNodeStreamWriterExtension> delegateExts = delegate.getExtensions();
            if (!delegateExts.isEmpty()) {
                throw new IllegalStateException("Extension filtering not implemented yet for " + delegateExts);
            } else {
                extensions = ImmutableClassToInstanceMap.of();
            }
        }

        return extensions;
    }

    @Override
    public void startLeafNode(final NodeIdentifier name) throws IOException {
        if (checkFilter(name)) {
            delegate.startLeafNode(name);
        }
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startLeafSet(name, childSizeHint);
        }
    }

    @Override
    public void startOrderedLeafSet(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startOrderedLeafSet(name, childSizeHint);
        }
    }

    @Override
    public void startLeafSetEntryNode(final NodeWithValue<?> name) throws IOException {
        if (checkFilter(name)) {
            delegate.startLeafSetEntryNode(name);
        }
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startContainerNode(name, childSizeHint);
        }
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startUnkeyedList(name, childSizeHint);
        }
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startUnkeyedListItem(name, childSizeHint);
        }
    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startMapNode(name, childSizeHint);
        }
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IOException {
        if (checkFilter(identifier)) {
            delegate.startMapEntryNode(identifier, childSizeHint);
        }
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startOrderedMapNode(name, childSizeHint);
        }
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startChoiceNode(name, childSizeHint);
        }
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IOException {
        if (checkFilter(identifier)) {
            delegate.startAugmentationNode(identifier);
        }
    }

    @Override
    public void startAnyxmlNode(final NodeIdentifier name) throws IOException {
        if (checkFilter(name)) {
            delegate.startAnyxmlNode(name);
        }
    }

    @Override
    public void domSourceValue(final DOMSource value) throws IOException {
        if (depth == 0) {
            delegate.domSourceValue(value);
        }
    }

    @Override
    public void startYangModeledAnyXmlNode(final NodeIdentifier name, final int childSizeHint) throws IOException {
        if (checkFilter(name)) {
            delegate.startYangModeledAnyXmlNode(name, childSizeHint);
        }
    }

    @Override
    public void endNode() throws IOException {
        if (depth == 0) {
            delegate.endNode();
            filter.exitNode();
        } else {
            depth--;
        }
    }

    @Override
    public void scalarValue(final @NonNull Object value) throws IOException {
        if (depth == 0) {
            delegate.scalarValue(value);
        }
    }

    @Override
    public void close() throws IOException {
        if (depth != 0) {
            throw new IOException("Attempted to close a writer with filter depth " + depth);
        }
        delegate.close();
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    private boolean checkFilter(final PathArgument name) {
        if (depth == 0 && filter.enterNode(name)) {
            return true;
        }

        depth++;
        return false;
    }
}
