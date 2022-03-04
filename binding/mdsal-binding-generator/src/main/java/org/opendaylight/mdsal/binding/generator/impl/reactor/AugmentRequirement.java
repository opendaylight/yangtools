/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl.reactor;

import static com.google.common.base.Verify.verify;
import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Mutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;

/**
 * Class tracking state of resolution of an {@code augment} statement's target generator.
 *
 * <p>
 * This is not quite straightforward. 'path' works on top of schema tree, which is instantiated view. Since we
 * do not generate duplicate instantiations along 'uses' path, findSchemaTreeGenerator() would satisfy our
 * request by returning a child of the source 'grouping'.
 *
 * <p>
 * When that happens, our subsequent lookups need to adjust the namespace being looked up to the grouping's
 * namespace... except for the case when the step is actually an augmentation, in which case we must not make
 * that adjustment.
 *
 * <p>
 * Hence we deal with this lookup recursively, dropping namespace hints when we cross into groupings. Note we
 * take an initial hint -- which UsesAugmentGenerator provides, but ModuleAugmentGenerator does not -- and that
 * accounts for the difference.
 */
final class AugmentRequirement implements Mutable {
    private final @NonNull Set<QNameModule> squashNamespaces = new HashSet<>(4);
    private final @NonNull AbstractAugmentGenerator augment;
    private final @NonNull Iterator<QName> remaining;

    private @NonNull AbstractCompositeGenerator<?> target;
    private QNameModule localNamespace;
    private QName qname;

    private AugmentRequirement(final AbstractAugmentGenerator augment, final AbstractCompositeGenerator<?> target) {
        this.augment = requireNonNull(augment);
        this.target = requireNonNull(target);
        remaining = augment.statement().argument().getNodeIdentifiers().iterator();
        qname = remaining.next();
    }

    AugmentRequirement(final ModuleAugmentGenerator augment, final ModuleGenerator module) {
        this((AbstractAugmentGenerator) augment, module);
    }

    AugmentRequirement(final UsesAugmentGenerator augment, final GroupingGenerator grouping) {
        this((AbstractAugmentGenerator) augment, grouping);
        // Starting in a grouping: squash namespace references to the grouping's namespace
        localNamespace = grouping.getQName().getModule();
        squashNamespaces.add(qname.getModule());
    }

    @NonNull LinkageProgress resolve() {
        return qname == null ? resolveAsTarget() : resolveAsChild();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).omitNullValues()
            .add("augment", augment)
            .add("target", target)
            .add("qname", qname)
            .toString();
    }

    private @NonNull LinkageProgress resolveAsTarget() {
        // Resolved requirement, if we also have original we end resolution here and now
        final var original = target.tryOriginal();
        if (original != null) {
            augment.setTargetGenerator(original);
            original.addAugment(augment);
            return LinkageProgress.DONE;
        }
        return LinkageProgress.NONE;
    }

    private @NonNull LinkageProgress resolveAsChild() {
        // First try local statements without adjustment
        var gen = target.findLocalSchemaTreeGenerator(qname);
        if (gen != null) {
            return progressTo(gen);
        }

        // Second try local augments, as those are guaranteed to match namespace exactly
        final var aug = target.findAugmentForGenerator(qname);
        if (aug != null) {
            return moveTo(aug);
        }

        // Third try local groupings, as those perform their own adjustment
        final var grp = target.findGroupingForGenerator(qname);
        if (grp != null) {
            squashNamespaces.add(qname.getModule());
            localNamespace = grp.getQName().getModule();
            return moveTo(grp);
        }

        // Lastly try local statements adjusted with namespace, if applicable
        gen = target.findLocalSchemaTreeGenerator(squashNamespaces.contains(qname.getModule())
            ? qname.bindTo(verifyNotNull(localNamespace)) : qname);
        if (gen != null) {
            return progressTo(gen);
        }
        return LinkageProgress.NONE;
    }

    private @NonNull LinkageProgress moveTo(final @NonNull AbstractCompositeGenerator<?> newTarget) {
        target = newTarget;
        return tryProgress();
    }

    private @NonNull LinkageProgress progressTo(final @NonNull AbstractExplicitGenerator<?> newTarget) {
        verify(newTarget instanceof AbstractCompositeGenerator, "Unexpected generator %s", newTarget);
        target = (AbstractCompositeGenerator<?>) newTarget;
        qname = remaining.hasNext() ? remaining.next() : null;
        return tryProgress();
    }

    private @NonNull LinkageProgress tryProgress() {
        final var progress = resolve();
        return progress != LinkageProgress.NONE ? progress : LinkageProgress.SOME;
    }
}
