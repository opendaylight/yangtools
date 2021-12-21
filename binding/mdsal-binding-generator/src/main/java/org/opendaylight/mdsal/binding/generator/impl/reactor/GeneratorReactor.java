/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Stopwatch;
import com.google.common.base.VerifyException;
import com.google.common.collect.Maps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.binding.ChildOf;
import org.opendaylight.yangtools.yang.binding.ChoiceIn;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.PathExpression;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.ri.type.TypeBuilder;
import org.opendaylight.yangtools.yang.model.spi.ModuleDependencySort;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A multi-stage reactor for generating {@link GeneratedType} instances from an {@link EffectiveModelContext}.
 *
 * <p>
 * The reason for multi-stage processing is that the problem ahead of us involves:
 * <ul>
 *   <li>mapping {@code typedef} and restricted {@code type} statements onto Java classes</li>
 *   <li>mapping a number of schema tree nodes into Java interfaces with properties</li>
 *   <li>taking advantage of Java composition to provide {@code grouping} mobility</li>
 * </ul>
 */
public final class GeneratorReactor extends GeneratorContext implements Mutable {
    private enum State {
        INITIALIZED,
        EXECUTING,
        FINISHED
    }

    private static final Logger LOG = LoggerFactory.getLogger(GeneratorReactor.class);

    private final Deque<Iterable<? extends Generator>> stack = new ArrayDeque<>();
    private final @NonNull Map<QNameModule, ModuleGenerator> generators;
    private final @NonNull List<ModuleGenerator> children;
    private final @NonNull SchemaInferenceStack inferenceStack;

    private Map<?, AbstractTypeAwareGenerator<?>> leafGenerators;
    private State state = State.INITIALIZED;

    public GeneratorReactor(final EffectiveModelContext context) {
        inferenceStack = SchemaInferenceStack.of(context);

        // Construct modules and their subtrees. Dependency sort is very much needed here, as it establishes order of
        // module evaluation, and that (along with the sort in AbstractCompositeGenerator) ensures we visit
        // AugmentGenerators without having forward references.
        // FIXME: migrate to new ModuleDependencySort when it is available, which streamline things here
        children = ModuleDependencySort.sort(context.getModules()).stream()
            .map(module -> {
                verify(module instanceof ModuleEffectiveStatement, "Unexpected module %s", module);
                return new ModuleGenerator((ModuleEffectiveStatement) module);
            })
            .collect(Collectors.toUnmodifiableList());
        generators = Maps.uniqueIndex(children, gen -> gen.statement().localQNameModule());
    }

    /**
     * Execute the reactor. Execution follows the following steps:
     * <ol>
     *   <li>link the statement inheritance graph along {@code uses}/{@code grouping} statements</li>
     *   <li>link the {@code typedef} inheritance hierarchy by visiting all {@link TypedefGenerator}s and memoizing the
     *       {@code type} lookup</li>
     *   <li>link the {@code identity} inheritance hierarchy by visiting all {@link IdentityGenerator}s and memoizing
     *       the {@code base} lookup</li>
     *   <li>link the {@code type} statements and resolve type restriction hierarchy, determining the set of Java
             classes required for Java equivalent of effective YANG type definitions</li>
     *   <li>bind {@code leafref} and {@code identityref} references to their Java class roots</li>
     *   <li>resolve {@link ChoiceIn}/{@link ChildOf} hierarchy</li>
     *   <li>assign Java package names and {@link JavaTypeName}s to all generated classes</li>
     *   <li>create {@link Type} instances</li>
     * </ol>
     *
     * @param builderFactory factory for creating {@link TypeBuilder}s for resulting types
     * @return Resolved generators
     * @throws IllegalStateException if the reactor has failed execution
     * @throws NullPointerException if {@code builderFactory} is {@code null}
     */
    public @NonNull Map<QNameModule, ModuleGenerator> execute(final TypeBuilderFactory builderFactory) {
        switch (state) {
            case INITIALIZED:
                state = State.EXECUTING;
                break;
            case FINISHED:
                return generators;
            case EXECUTING:
                throw new IllegalStateException("Cannot resume partial execution");
            default:
                throw new IllegalStateException("Unhandled state" + state);
        }

        // Step 1a: walk all composite generators and resolve 'uses' statements to the corresponding grouping node,
        //          establishing implied inheritance ...
        linkUsesDependencies(children);

        // Step 1b: ... and also link augments and their targets in a separate pass, as we need groupings fully resolved
        //          before we attempt augmentation lookups ...
        for (ModuleGenerator module : children) {
            for (Generator child : module) {
                if (child instanceof ModuleAugmentGenerator) {
                    ((ModuleAugmentGenerator) child).linkAugmentationTarget(this);
                }
            }
        }

        // Step 1c: ... finally establish linkage along the reverse uses/augment axis. This is needed to route generated
        //          type manifestations (isAddedByUses/isAugmenting) to their type generation sites.
        linkOriginalGenerator(children);

        /*
         * Step 2: link typedef statements, so that typedef's 'type' axis is fully established
         * Step 3: link all identity statements, so that identity's 'base' axis is fully established
         * Step 4: link all type statements, so that leafs and leaf-lists have restrictions established
         *
         * Since our implementation class hierarchy captures all four statements involved in a common superclass, we can
         * perform this in a single pass.
         */
        final Stopwatch sw = Stopwatch.createStarted();
        linkDependencies(children);

        // Step five: resolve all 'type leafref' and 'type identityref' statements, so they point to their
        //            corresponding Java type representation.
        bindTypeDefinition(children);

        // Step six: walk all composite generators and link ChildOf/ChoiceIn relationships with parents. We have taken
        //           care of this step during tree construction, hence this now a no-op.

        /*
         * Step seven: assign java packages and JavaTypeNames
         *
         * This is a really tricky part, as we have large number of factors to consider:
         * - we are mapping grouping, typedef, identity and schema tree namespaces into Fully Qualified Class Names,
         *   i.e. four namespaces into one
         * - our source of class naming are YANG identifiers, which allow characters not allowed by Java
         * - we generate class names as well as nested package hierarchy
         * - we want to generate names which look like Java as much as possible
         * - we need to always have an (arbitrarily-ugly) fail-safe name
         *
         * To deal with all that, we split this problem into multiple manageable chunks.
         *
         * The first chunk is here: we walk all generators and ask them to do two things:
         * - instantiate their CollisionMembers and link them to appropriate CollisionDomains
         * - return their collision domain
         *
         * Then we process we ask collision domains until all domains are resolved, driving the second chunk of the
         * algorithm in CollisionDomain. Note that we may need to walk the domains multiple times, as the process of
         * solving a domain may cause another domain's solution to be invalidated.
         */
        final List<CollisionDomain> domains = new ArrayList<>();
        collectCollisionDomains(domains, children);
        boolean haveUnresolved;
        do {
            haveUnresolved = false;
            for (CollisionDomain domain : domains) {
                if (domain.findSolution()) {
                    haveUnresolved = true;
                }
            }
        } while (haveUnresolved);

        // Step eight: generate actual Types
        //
        // We have now properly cross-linked all generators and have assigned their naming roots, so from this point
        // it looks as though we are performing a simple recursive execution. In reality, though, the actual path taken
        // through generators is dictated by us as well as generator linkage.
        for (ModuleGenerator module : children) {
            module.ensureType(builderFactory);
        }

        LOG.debug("Processed {} modules in {}", generators.size(), sw);
        state = State.FINISHED;
        return generators;
    }

    private void collectCollisionDomains(final List<CollisionDomain> result,
            final Iterable<? extends Generator> parent) {
        for (Generator gen : parent) {
            gen.ensureMember();
            collectCollisionDomains(result, gen);
            if (gen instanceof AbstractCompositeGenerator) {
                result.add(((AbstractCompositeGenerator<?>) gen).domain());
            }
        }
    }

    @Override
    <E extends EffectiveStatement<QName, ?>, G extends AbstractExplicitGenerator<E>> G resolveTreeScoped(
            final Class<G> type, final QName argument) {
        LOG.trace("Searching for tree-scoped argument {} at {}", argument, stack);

        // Check if the requested QName matches current module, if it does search the stack
        final Iterable<? extends Generator> last = stack.getLast();
        verify(last instanceof ModuleGenerator, "Unexpected last stack item %s", last);

        if (argument.getModule().equals(((ModuleGenerator) last).statement().localQNameModule())) {
            for (Iterable<? extends Generator> ancestor : stack) {
                for (Generator child : ancestor) {
                    if (type.isInstance(child)) {
                        final G cast = type.cast(child);
                        if (argument.equals(cast.statement().argument())) {
                            LOG.trace("Found matching {}", child);
                            return cast;
                        }
                    }
                }
            }
        } else {
            final ModuleGenerator module = generators.get(argument.getModule());
            if (module != null) {
                for (Generator child : module) {
                    if (type.isInstance(child)) {
                        final G cast = type.cast(child);
                        if (argument.equals(cast.statement().argument())) {
                            LOG.trace("Found matching {}", child);
                            return cast;
                        }
                    }
                }
            }
        }

        throw new IllegalStateException("Could not find " + type + " argument " + argument + " in " + stack);
    }

    @Override
    ModuleGenerator resolveModule(final QNameModule namespace) {
        final ModuleGenerator module = generators.get(requireNonNull(namespace));
        checkState(module != null, "Failed to find module for %s", namespace);
        return module;
    }

    @Override
    AbstractTypeObjectGenerator<?> resolveLeafref(final PathExpression path) {
        LOG.trace("Resolving path {}", path);
        verify(inferenceStack.isEmpty(), "Unexpected data tree state %s", inferenceStack);
        try {
            // Populate inferenceStack with a grouping + data tree equivalent of current stack's state.
            final Iterator<Iterable<? extends Generator>> it = stack.descendingIterator();
            // Skip first item, as it points to our children
            verify(it.hasNext(), "Unexpected empty stack");
            it.next();

            while (it.hasNext()) {
                final Iterable<? extends Generator> item = it.next();
                verify(item instanceof Generator, "Unexpected stack item %s", item);
                ((Generator) item).pushToInference(inferenceStack);
            }

            return inferenceStack.inGrouping() ? lenientResolveLeafref(path) : strictResolvePath(path);
        } finally {
            inferenceStack.clear();
        }
    }

    private @NonNull AbstractTypeAwareGenerator<?> strictResolvePath(final @NonNull PathExpression path) {
        try {
            inferenceStack.resolvePathExpression(path);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to find leafref target " + path.getOriginalString(), e);
        }
        return mapToGenerator();
    }

    private @Nullable AbstractTypeAwareGenerator<?> lenientResolveLeafref(final @NonNull PathExpression path) {
        try {
            inferenceStack.resolvePathExpression(path);
        } catch (IllegalArgumentException e) {
            LOG.debug("Ignoring unresolved path {}", path, e);
            return null;
        }
        return mapToGenerator();
    }

    // Map a statement to the corresponding generator
    private @NonNull AbstractTypeAwareGenerator<?> mapToGenerator() {
        // Some preliminaries first: we need to be in the correct module to walk the path
        final ModuleEffectiveStatement module = inferenceStack.currentModule();
        final ModuleGenerator gen = verifyNotNull(generators.get(module.localQNameModule()),
            "Cannot find generator for %s", module);

        // Now kick of the search
        final List<EffectiveStatement<?, ?>> stmtPath = inferenceStack.toInference().statementPath();
        final AbstractExplicitGenerator<?> found = gen.findGenerator(stmtPath);
        if (found instanceof AbstractTypeAwareGenerator) {
            return (AbstractTypeAwareGenerator<?>) found;
        }
        throw new VerifyException("Statements " + stmtPath + " resulted in unexpected " + found);
    }

    // Note: unlike other methods, this method pushes matching child to the stack
    private void linkUsesDependencies(final Iterable<? extends Generator> parent) {
        for (Generator child : parent) {
            if (child instanceof AbstractCompositeGenerator) {
                LOG.trace("Visiting composite {}", child);
                final AbstractCompositeGenerator<?> composite = (AbstractCompositeGenerator<?>) child;
                stack.push(composite);
                composite.linkUsesDependencies(this);
                linkUsesDependencies(composite);
                stack.pop();
            }
        }
    }

    private void linkDependencies(final Iterable<? extends Generator> parent) {
        for (Generator child : parent) {
            if (child instanceof AbstractDependentGenerator) {
                ((AbstractDependentGenerator<?>) child).linkDependencies(this);
            } else if (child instanceof AbstractCompositeGenerator) {
                stack.push(child);
                linkDependencies(child);
                stack.pop();
            }
        }
    }

    private void linkOriginalGenerator(final Iterable<? extends Generator> parent) {
        for (Generator child : parent) {
            if (child instanceof AbstractExplicitGenerator) {
                ((AbstractExplicitGenerator<?>) child).linkOriginalGenerator(this);
            }
            if (child instanceof AbstractCompositeGenerator) {
                stack.push(child);
                linkOriginalGenerator(child);
                stack.pop();
            }
        }
    }

    private void bindTypeDefinition(final Iterable<? extends Generator> parent) {
        for (Generator child : parent) {
            stack.push(child);
            if (child instanceof AbstractTypeObjectGenerator) {
                ((AbstractTypeObjectGenerator<?>) child).bindTypeDefinition(this);
            } else if (child instanceof AbstractCompositeGenerator) {
                bindTypeDefinition(child);
            }
            stack.pop();
        }
    }
}
