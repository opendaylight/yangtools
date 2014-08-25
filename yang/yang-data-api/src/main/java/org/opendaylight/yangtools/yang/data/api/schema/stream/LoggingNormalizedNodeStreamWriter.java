package org.opendaylight.yangtools.yang.data.api.schema.stream;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.IOException;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Beta
public class LoggingNormalizedNodeStreamWriter implements NormalizedNodeStreamWriter {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingNormalizedNodeStreamWriter.class);
    private static final int DEFAULT_INDENT_SIZE = 2;
    private final int indentSize = DEFAULT_INDENT_SIZE;
    private int currentIndent = 0;

    private String ind() {
        return Strings.repeat(" ", currentIndent);
    }

    private void decIndent() {
        Preconditions.checkState(currentIndent >= 0, "Unexpected indentation %s", currentIndent);
        currentIndent -= indentSize;
    }

    private void incIndent() {
        currentIndent += indentSize;
    }

    @Override
    public void startUnkeyedListItem(final NodeIdentifier name, final int childSizeHint) throws IllegalStateException {
        LOG.debug("{}{}[](no key)", ind(), name);
        incIndent();
    }

    @Override
    public void startUnkeyedList(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        LOG.debug("{}{}(no key)", ind(), name);
        incIndent();
    }

    @Override
    public void startOrderedMapNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {

    }

    @Override
    public void startMapNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        LOG.debug("{}{}(key)", ind(), name);
        incIndent();
    }

    @Override
    public void startMapEntryNode(final NodeIdentifierWithPredicates identifier, final int childSizeHint)
            throws IllegalArgumentException {
        LOG.debug("{}{}[](key)", ind(), identifier);
        incIndent();
    }

    @Override
    public void startLeafSet(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        LOG.debug("{}{}(leaf-list)", ind(), name);
        incIndent();
    }

    @Override
    public void startContainerNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        LOG.debug("{}{}(container)", ind(), name);
        incIndent();
    }

    @Override
    public void startChoiceNode(final NodeIdentifier name, final int childSizeHint) throws IllegalArgumentException {
        LOG.debug("{}{}(choice)", ind(), name);
        incIndent();
    }

    @Override
    public void startAugmentationNode(final AugmentationIdentifier identifier) throws IllegalArgumentException {
        LOG.debug("{}{}(augmentation)", ind(), identifier);
        incIndent();
    }

    @Override
    public void leafSetEntryNode(final Object value) throws IllegalArgumentException {
        LOG.debug("{}{}({}) ", ind(), value, value.getClass().getSimpleName());
    }

    @Override
    public void leafNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
        LOG.debug("{}{}(leaf({}))=", ind(), name, value.getClass().getSimpleName(), value);
    }

    @Override
    public void endNode() throws IllegalStateException {
        decIndent();
        LOG.debug("{}(end)", ind());
    }

    @Override
    public void anyxmlNode(final NodeIdentifier name, final Object value) throws IllegalArgumentException {
        LOG.debug("{}{}(anyxml)=", ind(), name, value);
    }

    @Override
    public void flush() throws IOException {
        LOG.trace("<<FLUSH>>");
    }

    @Override
    public void close() throws IOException {
        LOG.debug("<<END-OF-STREAM>>");
    }
}