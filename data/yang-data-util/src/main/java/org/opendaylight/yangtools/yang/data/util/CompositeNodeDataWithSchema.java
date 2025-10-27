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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter.MetadataExtension;
import org.opendaylight.yangtools.yang.model.api.AnydataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AnyxmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerLike;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Utility class used for tracking parser state as needed by a StAX-like parser. This class is to be used only by
 * respective XML and JSON parsers in yang-data-codec-xml and yang-data-codec-gson.
 *
 * <p>Represents a node which is composed of multiple simpler nodes.
 */
public sealed class CompositeNodeDataWithSchema<T extends DataSchemaNode> extends AbstractNodeDataWithSchema<T>
        permits AbstractMountPointDataWithSchema, CaseNodeDataWithSchema, ChoiceNodeDataWithSchema,
                LeafListNodeDataWithSchema, ListNodeDataWithSchema {
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
     * remaining data nodes (which aren't added via augment). Every of one them should have the same QName.
     */
    private final List<AbstractNodeDataWithSchema<?>> children = new ArrayList<>();

    // FIXME: hide this when JSON codec is sane
    public CompositeNodeDataWithSchema(final T schema) {
        super(schema);
    }

    public static @NonNull CompositeNodeDataWithSchema<?> of(final DataSchemaNode schema) {
        return switch (schema) {
            case ContainerLike containerLike -> new ContainerNodeDataWithSchema(containerLike);
            case LeafListSchemaNode leafList -> new LeafListNodeDataWithSchema(leafList);
            case ListSchemaNode list -> new ListNodeDataWithSchema(list);
            default -> new CompositeNodeDataWithSchema<>(schema);
        };
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

        CompositeNodeDataWithSchema<?> caseNodeDataWithSchema = findChoice(children, choiceCandidate, caseCandidate);
        if (caseNodeDataWithSchema == null) {
            ChoiceNodeDataWithSchema choiceNodeDataWithSchema = new ChoiceNodeDataWithSchema(choiceNode);
            children.add(choiceNodeDataWithSchema);
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
        switch (schema) {
            case LeafSchemaNode leaf -> newChild = new LeafNodeDataWithSchema(leaf);
            case AnyxmlSchemaNode anyxml -> newChild = new AnyXmlNodeDataWithSchema(anyxml);
            case AnydataSchemaNode anydata -> newChild = new AnydataNodeDataWithSchema(anydata);
            default -> {
                return null;
            }
        }

        // FIXME: 7.0.0: use policy to determine if we should reuse or replace the child
        addChild(newChild);
        return newChild;
    }

    private static CaseNodeDataWithSchema findChoice(final Collection<AbstractNodeDataWithSchema<?>> childNodes,
            final DataSchemaNode choiceCandidate, final DataSchemaNode caseCandidate) {
        if (childNodes != null) {
            for (var nodeDataWithSchema : childNodes) {
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
        return addCompositeChild(of(schema), policy);
    }

    final AbstractNodeDataWithSchema<?> addCompositeChild(final CompositeNodeDataWithSchema<?> newChild,
            final ChildReusePolicy policy) {
        return policy.appendChild(children, newChild);
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
        // FIXME: we probably want to emit children with the same namespace first
        for (var child : children) {
            child.write(writer, metaWriter);
        }
    }
}
