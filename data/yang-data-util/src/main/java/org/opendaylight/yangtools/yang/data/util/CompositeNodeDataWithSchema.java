/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.annotations.Beta;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser.
 * This class is to be used only by respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>
 * Represents a node which is composed of multiple simpler nodes.
 */
public class CompositeNodeDataWithSchema<T extends DataSchemaNode> extends AbstractNodeDataWithSchema<T> {
    /**
     * Policy on how child nodes should be treated when an attempt is made to add them multiple times.
     */
    @Beta
    public enum ChildReusePolicy {
        /**
         * Do not consider any existing nodes at all, just perform a straight append. Multiple occurrences of a child
         * will result in multiple children being emitted. This is almost certainly the wrong policy unless the caller
         * prevents such a situation from arising via some different mechanism.
         */
        NOOP,
        /**
         * Do not allow duplicate definition of a child node. This would typically be used when a child cannot be
         * encountered multiple times, but the caller does not make any provision to detect such a conflict. If a child
         * node would end up being defined a second time, {@link DuplicateChildNodeRejectedException} is reported.
         */
        REJECT {
            @Override
            AbstractNodeDataWithSchema<?> appendChild(final Collection<AbstractNodeDataWithSchema<?>> view,
                    final AbstractNodeDataWithSchema<?> newChild) {
                final DataSchemaNode childSchema = newChild.getSchema();
                final AbstractNodeDataWithSchema<?> existing = findExistingChild(view, childSchema);
                if (existing != null) {
                    throw new DuplicateChildNodeRejectedException("Duplicate child " + childSchema.getQName());
                }
                return super.appendChild(view, newChild);
            }
        },
        /**
         * Reuse previously-defined child node. This is most appropriate when a child may be visited multiple times
         * and the intent is to append content of each visit. A typical usage is list elements with RFC7950 XML
         * encoding, where there is no encapsulating element and hence list entries may be interleaved with other
         * children.
         */
        REUSE {
            @Override
            AbstractNodeDataWithSchema<?> appendChild(final Collection<AbstractNodeDataWithSchema<?>> view,
                    final AbstractNodeDataWithSchema<?> newChild) {
                final AbstractNodeDataWithSchema<?> existing = findExistingChild(view, newChild.getSchema());
                return existing != null ? existing : super.appendChild(view, newChild);
            }
        };

        AbstractNodeDataWithSchema<?> appendChild(final Collection<AbstractNodeDataWithSchema<?>> view,
                final AbstractNodeDataWithSchema<?> newChild) {
            view.add(newChild);
            return newChild;
        }

        static @Nullable AbstractNodeDataWithSchema<?> findExistingChild(
                final Collection<AbstractNodeDataWithSchema<?>> view, final DataSchemaNode childSchema) {
            for (AbstractNodeDataWithSchema<?> existing : view) {
                if (childSchema.equals(existing.getSchema())) {
                    return existing;
                }
            }
            return null;
        }
    }

    /**
     * nodes which were added to schema via augmentation and are present in data input.
     */
    private final Multimap<AugmentationSchemaNode, AbstractNodeDataWithSchema<?>> augmentationsToChild =
        ArrayListMultimap.create();

    /**
     * remaining data nodes (which aren't added via augment). Every of one them should have the same QName.
     */
    private final List<AbstractNodeDataWithSchema<?>> children = new ArrayList<>();

    public CompositeNodeDataWithSchema(final T schema) {
        super(schema);
    }

    void addChild(final AbstractNodeDataWithSchema<?> newChild) {
        children.add(newChild);
    }

    public final AbstractNodeDataWithSchema<?> addChild(final Deque<DataSchemaNode> schemas,
            final ChildReusePolicy policy) {
        checkArgument(!schemas.isEmpty(), "Expecting at least one schema");

        // Pop the first node...
        final DataSchemaNode schema = schemas.pop();
        if (schemas.isEmpty()) {
            // Simple, direct node
            return addChild(schema, policy);
        }

        // The choice/case mess, reuse what we already popped
        final DataSchemaNode choiceCandidate = schema;
        checkArgument(choiceCandidate instanceof ChoiceSchemaNode, "Expected node of type ChoiceNode but was %s",
            choiceCandidate.getClass());
        final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) choiceCandidate;

        final DataSchemaNode caseCandidate = schemas.pop();
        checkArgument(caseCandidate instanceof CaseSchemaNode, "Expected node of type ChoiceCaseNode but was %s",
            caseCandidate.getClass());
        final CaseSchemaNode caseNode = (CaseSchemaNode) caseCandidate;

        final AugmentationSchemaNode augSchema;
        if (choiceCandidate.isAugmenting()) {
            augSchema = NormalizedNodeSchemaUtils.findCorrespondingAugment(getSchema(), choiceCandidate);
        } else {
            augSchema = null;
        }

        // looking for existing choice
        final Collection<AbstractNodeDataWithSchema<?>> childNodes;
        if (augSchema != null) {
            childNodes = augmentationsToChild.get(augSchema);
        } else {
            childNodes = children;
        }

        CompositeNodeDataWithSchema<?> caseNodeDataWithSchema = findChoice(childNodes, choiceCandidate, caseCandidate);
        if (caseNodeDataWithSchema == null) {
            ChoiceNodeDataWithSchema choiceNodeDataWithSchema = new ChoiceNodeDataWithSchema(choiceNode);
            childNodes.add(choiceNodeDataWithSchema);
            caseNodeDataWithSchema = choiceNodeDataWithSchema.addCompositeChild(caseNode, ChildReusePolicy.NOOP);
        }

        return caseNodeDataWithSchema.addChild(schemas, policy);
    }

    private AbstractNodeDataWithSchema<?> addChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        AbstractNodeDataWithSchema<?> newChild = addSimpleChild(schema, policy);
        return newChild == null ? addCompositeChild(schema, policy) : newChild;
    }

    private AbstractNodeDataWithSchema<?> addSimpleChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        final SimpleNodeDataWithSchema<?> newChild;
        if (schema instanceof LeafSchemaNode leaf) {
            newChild = new LeafNodeDataWithSchema(leaf);
        } else if (schema instanceof AnyxmlSchemaNode anyxml) {
            newChild = new AnyXmlNodeDataWithSchema(anyxml);
        } else if (schema instanceof AnydataSchemaNode anydata) {
            newChild = new AnydataNodeDataWithSchema(anydata);
        } else {
            return null;
        }

        final AugmentationSchemaNode augSchema;
        if (schema.isAugmenting()) {
            augSchema = NormalizedNodeSchemaUtils.findCorrespondingAugment(getSchema(), schema);
        } else {
            augSchema = null;
        }

        // FIXME: 7.0.0: use policy to determine if we should reuse or replace the child

        if (augSchema != null) {
            augmentationsToChild.put(augSchema, newChild);
        } else {
            addChild(newChild);
        }
        return newChild;
    }

    private static CaseNodeDataWithSchema findChoice(final Collection<AbstractNodeDataWithSchema<?>> childNodes,
            final DataSchemaNode choiceCandidate, final DataSchemaNode caseCandidate) {
        if (childNodes != null) {
            for (AbstractNodeDataWithSchema<?> nodeDataWithSchema : childNodes) {
                if (nodeDataWithSchema instanceof ChoiceNodeDataWithSchema childChoice
                        && nodeDataWithSchema.getSchema().getQName().equals(choiceCandidate.getQName())) {
                    CaseNodeDataWithSchema casePrevious = childChoice.getCase();

                    checkArgument(casePrevious.getSchema().getQName().equals(caseCandidate.getQName()),
                        "Data from case %s are specified but other data from case %s were specified earlier."
                        + " Data aren't from the same case.", caseCandidate.getQName(),
                        casePrevious.getSchema().getQName());

                    return casePrevious;
                }
            }
        }
        return null;
    }

    AbstractNodeDataWithSchema<?> addCompositeChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        final CompositeNodeDataWithSchema<?> newChild;

        if (schema instanceof ListSchemaNode list) {
            newChild = new ListNodeDataWithSchema(list);
        } else if (schema instanceof LeafListSchemaNode leafList) {
            newChild = new LeafListNodeDataWithSchema(leafList);
        } else if (schema instanceof ContainerLike containerLike) {
            newChild = new ContainerNodeDataWithSchema(containerLike);
        } else {
            newChild = new CompositeNodeDataWithSchema<>(schema);
        }

        return addCompositeChild(newChild, policy);
    }

    final AbstractNodeDataWithSchema<?> addCompositeChild(final CompositeNodeDataWithSchema<?> newChild,
            final ChildReusePolicy policy) {
        final var augSchema = NormalizedNodeSchemaUtils.findCorrespondingAugment(getSchema(), newChild.getSchema());
        final var view = augSchema == null ? children : augmentationsToChild.get(augSchema);

        return policy.appendChild(view, newChild);
    }

    /**
     * Return a hint about how may children we are going to generate.
     * @return Size of currently-present node list.
     */
    protected final int childSizeHint() {
        return children.size();
    }

    @Override
    public void write(final NormalizedNodeStreamWriter writer, final MetadataExtension metaWriter) throws IOException {
        for (AbstractNodeDataWithSchema<?> child : children) {
            child.write(writer, metaWriter);
        }
        for (var childsFromAgumentation : augmentationsToChild.asMap().values()) {
            for (var nodeDataWithSchema : childsFromAgumentation) {
                nodeDataWithSchema.write(writer, metaWriter);
            }
        }
    }
}
