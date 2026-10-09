/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.QName;
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
     * A child added by {@link #addChild(DataSchemaContext.Composite, QName, ChildReusePolicy)}.
     *
     * @param data the added child
     * @param context the context describing the child, from which its own children can be looked up
     */
    public record Child(@NonNull AbstractNodeDataWithSchema<?> data, @NonNull DataSchemaContext context) {
        public Child {
            requireNonNull(data);
            requireNonNull(context);
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

    /**
     * Add the child with the specified name. If the child sits inside a choice, the choice and the case are added as
     * well, or reused if an earlier child already added them.
     *
     * @param context the context describing this node
     * @param qname the child's name
     * @param policy what to do if such a child already exists
     * @return the added child, or {@code null} if this node has no child with that name
     * @throws NullPointerException if any argument is {@code null}
     * @throws IllegalArgumentException if an earlier child came from a different case of the same choice
     */
    public final @Nullable Child addChild(final DataSchemaContext.Composite context, final QName qname,
            final ChildReusePolicy policy) {
        var childContext = context.childByQName(qname);
        if (childContext == null) {
            return null;
        }

        // A choice and its case have no element of their own: the child's element sits directly inside ours. Step
        // through each choice on the way, adding its data node and the right case's data node.
        CompositeNodeDataWithSchema<?> parent = this;
        while (childContext instanceof DataSchemaContext.Choice choice) {
            final var inner = choice.childByQName(qname);
            if (inner == null) {
                // The name is the choice's own name. A choice never has an element of its own, so there is no such
                // child. This always happens at the first choice, before anything was added.
                return null;
            }
            parent = parent.enterCase((ChoiceSchemaNode) choice.dataSchemaNode(), choice.caseOf(inner));
            childContext = inner;
        }

        return new Child(parent.addChild(childContext.dataSchemaNode(), policy), childContext);
    }

    /**
     * Add a child, going through any choice and case on the way.
     *
     * @param schemas the path to the child: just the child itself, or for each choice on the way the choice and then
     *                the case, followed by the child. This method empties it.
     * @param policy what to do if such a child already exists
     * @return the added child
     */
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

        return enterCase(choiceNode, caseNode).addChild(schemas, policy);
    }

    private AbstractNodeDataWithSchema<?> addChild(final DataSchemaNode schema, final ChildReusePolicy policy) {
        AbstractNodeDataWithSchema<?> newChild = addSimpleChild(schema, policy);
        return newChild == null ? addCompositeChild(schema, policy) : newChild;
    }

    // Returns the data node of a case, adding it and its choice if they are not here yet
    private CompositeNodeDataWithSchema<?> enterCase(final ChoiceSchemaNode choice, final CaseSchemaNode caze) {
        final var existing = findChoice(children, choice, caze);
        if (existing != null) {
            return existing;
        }

        final var choiceData = new ChoiceNodeDataWithSchema(choice);
        children.add(choiceData);
        return choiceData.addCompositeChild(caze, ChildReusePolicy.NOOP);
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
     *
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
