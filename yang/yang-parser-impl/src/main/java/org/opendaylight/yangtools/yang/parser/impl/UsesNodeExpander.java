package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.fillAugmentTarget;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNode;
import static org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils.findSchemaNodeInModule;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationTargetBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.SchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.BuilderUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.GroupingUtils;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * FIXME: Consider signature definition DataNodeContainerBuilder ?
 */
class UsesNodeExpander implements StatementExpander<UsesNodeBuilder, DataNodeContainerBuilder >{

    private static final Logger LOG = LoggerFactory.getLogger(UsesNodeExpander.class);
    private final Map<URI, TreeMap<Date, ModuleBuilder>> modules;

    public UsesNodeExpander(Map<URI, TreeMap<Date, ModuleBuilder>> modules) {
        this.modules = Preconditions.checkNotNull(modules, "Module context must not be null");
    }

    @Override
    public void expand(UsesNodeBuilder definition, DataNodeContainerBuilder parent) throws YangParseException {
        if (!definition.isResolved()) {
            ModuleBuilder module = BuilderUtils.getParentModule(parent);
            GroupingBuilder target = GroupingUtils.getTargetGroupingFromModules(definition, modules, module);

            int index = nodeAfterUsesIndex(definition);
            List<DataSchemaNodeBuilder> targetNodes = target.instantiateChildNodes(parent);
            for (DataSchemaNodeBuilder targetNode : targetNodes) {
                parent.addChildNode(index++, targetNode);
            }
            parent.getTypeDefinitionBuilders().addAll(target.instantiateTypedefs(parent));
            parent.getGroupingBuilders().addAll(target.instantiateGroupings(parent));
            parent.getUnknownNodes().addAll(target.instantiateUnknownNodes(parent));
            definition.setResolved(true);
            for (AugmentationSchemaBuilder augment : definition.getAugmentations()) {
                resolveUsesAugment(augment, module, modules);
            }

            GroupingUtils.performRefine(definition);
        }
    }

    private int nodeAfterUsesIndex(final UsesNodeBuilder usesNode) {
        DataNodeContainerBuilder parent = usesNode.getParent();
        int usesLine = usesNode.getLine();

        List<DataSchemaNodeBuilder> childNodes = parent.getChildNodeBuilders();
        if (childNodes.isEmpty()) {
            return 0;
        }

        DataSchemaNodeBuilder nextNodeAfterUses = null;
        for (DataSchemaNodeBuilder childNode : childNodes) {
            if (!(childNode.isAddedByUses()) && !(childNode.isAugmenting())) {
                if (childNode.getLine() > usesLine) {
                    nextNodeAfterUses = childNode;
                    break;
                }
            }
        }

        // uses is declared after child nodes
        if (nextNodeAfterUses == null) {
            return childNodes.size();
        }

        return parent.getChildNodeBuilders().indexOf(nextNodeAfterUses);
    }

    /**
     * Perform augmentation defined under uses statement.
     *
     * @param augment
     *            augment to resolve
     * @param module
     *            current module
     * @param modules
     *            all loaded modules
     * @return true if augment process succeed
     */
    private boolean resolveUsesAugment(final AugmentationSchemaBuilder augment, final ModuleBuilder module,
            final Map<URI, TreeMap<Date, ModuleBuilder>> modules) {
        if (augment.isResolved()) {
            return true;
        }

        UsesNodeBuilder usesNode = (UsesNodeBuilder) augment.getParent();
        DataNodeContainerBuilder parentNode = usesNode.getParent();
        Optional<SchemaNodeBuilder> potentialTargetNode;
        SchemaPath resolvedTargetPath = YangParserImpl.findUsesAugmentTargetNodePath(parentNode, augment);
        if (parentNode instanceof ModuleBuilder && resolvedTargetPath.isAbsolute()) {
            // Uses is directly used in module body, we lookup
            // We lookup in data namespace to find correct augmentation target
            potentialTargetNode = findSchemaNodeInModule(resolvedTargetPath, (ModuleBuilder) parentNode);
        } else {
            // Uses is used in local context (be it data namespace or grouping
            // namespace,
            // since all nodes via uses are imported to localName, it is safe to
            // to proceed only with local names.
            //
            // Conflicting elements in other namespaces are still not present
            // since resolveUsesAugment occurs before augmenting from external
            // modules.
            potentialTargetNode = Optional.<SchemaNodeBuilder> fromNullable(findSchemaNode(augment.getTargetPath()
                    .getPathFromRoot(), (SchemaNodeBuilder) parentNode));
        }

        if (potentialTargetNode.isPresent()) {
            SchemaNodeBuilder targetNode = potentialTargetNode.get();
            if (targetNode instanceof AugmentationTargetBuilder) {
                fillAugmentTarget(augment, targetNode);
                ((AugmentationTargetBuilder) targetNode).addAugmentation(augment);
                augment.setResolved(true);
                return true;
            } else {
                LOG.warn(
                        "Error in module {} at line {}: Unsupported augment target: {}. Augmentation process skipped.",
                        module.getName(), augment.getLine(), potentialTargetNode);
                augment.setResolved(true);
                augment.setUnsupportedTarget(true);
                return true;
            }
        } else {
            throw new YangParseException(module.getName(), augment.getLine(), String.format(
                    "Failed to resolve augment in uses. Invalid augment target path: %s", augment.getTargetPath()));
        }

    }


}
