package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Iterator;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

import com.google.common.base.Optional;

public class TreeUtils {


    public static Optional<NormalizedNode<?, ?>> findNode(InstanceIdentifier rootPath, NormalizedNode<?, ?> rootNode, InstanceIdentifier childPath) {
        if(rootPath.contains(childPath)) {
            int common = rootPath.getPath().size();
            InstanceIdentifier relativePath = new InstanceIdentifier(childPath.getPath().subList(common, childPath.getPath().size()));
            return findNode(rootNode, relativePath);
        }
        return Optional.absent();
    }


    public static Optional<NormalizedNode<?, ?>> findNode(NormalizedNode<?, ?> tree, InstanceIdentifier path) {
        checkNotNull(tree, "Tree must not be null");
        checkNotNull(path, "Path must not be null");

        Optional<NormalizedNode<?, ?>> currentNode = Optional.<NormalizedNode<?, ?>> of(tree);
        Iterator<PathArgument> pathIterator = path.getPath().iterator();
        while (currentNode.isPresent() && pathIterator.hasNext()) {
            currentNode = getDirectChild(currentNode.get(), pathIterator.next());
        }
        return currentNode;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static Optional<NormalizedNode<?, ?>> getDirectChild(NormalizedNode<?, ?> node, PathArgument pathArg) {
        if (node instanceof LeafNode<?> || node instanceof LeafSetEntryNode<?>) {
            return Optional.absent();
        } else if (node instanceof DataContainerNode<?>) {
            return (Optional) ((DataContainerNode<?>) node).getChild(pathArg);
        } else if (node instanceof MapNode && pathArg instanceof NodeIdentifierWithPredicates) {
            return (Optional) ((MapNode) node).getChild((NodeIdentifierWithPredicates) pathArg);
        } else if (node instanceof LeafSetNode<?>) {
            return (Optional) ((LeafSetNode<?>) node).getChild((NodeWithValue) pathArg);
        }
        return Optional.absent();
    }

}
