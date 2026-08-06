/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.CODEHELPERS;
import static org.opendaylight.yangtools.binding.codegen.TypeNames.OVERRIDE;
import static org.opendaylight.yangtools.binding.contract.Naming.BUILDER_SUFFIX;
import static org.opendaylight.yangtools.binding.contract.Naming.KEY_AWARE_KEY_NAME;
import static org.opendaylight.yangtools.binding.contract.Naming.NONNULL_PREFIX;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.lib.AbstractAugmentable;
import org.opendaylight.yangtools.binding.lib.AbstractDataContainer;
import org.opendaylight.yangtools.binding.lib.AbstractEntryObject;
import org.opendaylight.yangtools.binding.model.AugmentableArchetype;
import org.opendaylight.yangtools.binding.model.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;

/**
 * A template for the inner implementation class supported by a {@link BuilderTemplate}.
 */
// FIXME: consider refactoring as an inner class in BuilderTemplate, as we are never a standalone template, which
//        would allow proper specialization based on properties.isEmpty(), augmentType != null and keyType != null.
final class BuilderImplTemplate extends BaseTemplate {
    /**
     * {@link AbstractDataContainer} as a {@link TypeName}.
     */
    private static final @NonNull TypeName ABSTRACT_DATA_CONTAINER = TypeName.ofClass(AbstractDataContainer.class);
    /**
     * {@link AbstractAugmentable} as a {@link TypeName}.
     */
    private static final @NonNull TypeName ABSTRACT_AUGMENTABLE = TypeName.ofClass(AbstractAugmentable.class);
    /**
     * {@link AbstractEntryObject} as a {@link TypeName}.
     */
    private static final @NonNull TypeName ABSTRACT_ENTRY_OBJECT = TypeName.ofClass(AbstractEntryObject.class);

    private final @NonNull BuilderTemplate builder;

    @NonNullByDefault
    BuilderImplTemplate(final GeneratedClass.Nested javaType, final BuilderTemplate builder) {
        super(javaType);
        this.builder = requireNonNull(builder);
    }

    @Override
    BlockBuilder body() {
        // cache things from builder
        final var targetType = builder.targetType;
        final var props = builder.props;

        final var implIface = importedName(targetType);
        final var override = importedName(OVERRIDE);
        final var simpleName = typeName().simpleName();
        final var builderName = importedName(builder.typeName());

        final var bb = newBlockBuilder()
            .frg(DeprecatedAnnotation.of(javaType(), targetType.statement()))
            .str("private static final class ").str(simpleName).str(" extends ");
        if (props instanceof BuilderTemplate.WithKey with) {
            bb.gen(importedName(ABSTRACT_ENTRY_OBJECT), importedName(with.parentName()), implIface,
                importedName(with.key()));
        } else if (targetType instanceof AugmentableArchetype) {
            bb.gen(importedName(ABSTRACT_AUGMENTABLE), implIface);
        } else {
            bb.gen(importedName(ABSTRACT_DATA_CONTAINER), implIface);
        }
        bb.str(" implements ").str(implIface).oB();

        // generate instance fields
        for (var getter : props.allGetters()) {
            bb.str("private final ").str(importedName(getter.type())).sp().str(getter.fieldName()).eS();
        }

        bb
            .nl()
            .str(simpleName).str("(final ").str(builderName).str(" base)").oB();

        if (targetType instanceof AugmentableArchetype) {
            bb.str("super(base." + BuilderTemplate.AUGMENTATION_FIELD);
            if (props instanceof BuilderTemplate.WithKey) {
                bb.str(", extractKey(base)");
            }
            bb.eol(");");
        }

        switch (props) {
            case BuilderTemplate.WithKey with -> {
                bb.eol("final var key = key();");
                for (var getter : with.keyGetters()) {
                    bb.str("this.").str(getter.fieldName()).str(" = key.").str(getter.name()).eol("();");
                }
                appendCopyNonKeys(bb, with.implGetters());
                // TODO: this is generating a utility static method for use in the (only) constructor. We should be
                //       inlining this code into the constructor once JEP-482 Flexible Constructor Bodies available. We
                //       should construct the key into a 'key' local variable, so that generateCopyKeys() below can
                //       reference it
                bb
                    .cB()
                    .nl()
                    .str("private static ").str(importedNonNull(with.key())).str(" extractKey(").str(builderName)
                        .str(" base)").oB()
                        .str("final var key = base." + KEY_AWARE_KEY_NAME).eol("();")
                        .eol("return key != null ? key")
                        .str("    : new ").str(importedName(with.key())).str("(");

                // Note: keys have at least one component
                final var it = with.keyGetters().iterator();
                while (true) {
                    final var getter = it.next();
                    bb.str("base.").str(getter.name()).str("()");
                    if (!it.hasNext()) {
                        break;
                    }
                    bb.str(", ");
                }

                bb
                    .eol(");")
                    .cB();
            }
            case BuilderTemplate.WithoutKey without -> {
                appendCopyNonKeys(bb, without.allGetters());
                bb.cB();
            }
        }

        // generate getters
        final var getters = props.allGetters();
        if (!getters.isEmpty()) {
            bb.newLine();

            final var it = getters.iterator();
            while (true) {
                final var getter = it.next();

                // getFoo()
                bb
                    .at().eol(importedName(OVERRIDE))
                    .str("public ").str(importedName(getter.type())).sp().str(getter.name()).str("()").oB()
                        .str("return ");
                final var fieldName = getter.fieldName();
                if (getter.isBinary()) {
                    bb.str(importedName(CODEHELPERS)).str(".copyArray(").str(fieldName).eol(");");
                } else {
                    bb.str(fieldName).eS();
                }
                bb.cB();

                // nonnullFoo() for structural containers
                if (getter.type() instanceof ContainerObjectArchetype fieldType
                    && BuilderTemplate.isNonPresenceContainer(fieldType)) {
                    bb
                        .nl()
                        .at().eol(override)
                        .str("public ").str(importedName(fieldType)).str(" " + NONNULL_PREFIX).str(getter.suffix())
                            .str("()").oB()
                            .str("var tmp = ").str(getter.name()).eol("();")
                            .str("return tmp != null ? tmp : ")
                                // FIXME: better reference to FooBuilder.empty()
                                .str(fieldType.canonicalName()).eol(BUILDER_SUFFIX + ".empty();")
                        .cB();
                }

                if (!it.hasNext()) {
                    break;
                }
                bb.newLine();
            }
        }

        return bb.cB();
    }

    private void appendCopyNonKeys(final BlockBuilder bb, final List<GetterShape> getters) {
        for (var getter : getters) {
            bb.str("this.").str(getter.fieldName()).str(" = ");
            if (getter.method().statement() instanceof ListEffectiveStatement) {
                bb.str(importedName(CODEHELPERS)).str(".emptyToNull(base.").str(getter.name()).eol("());");
            } else {
                bb.str("base.").str(getter.name()).eol("();");
            }
        }
    }
}
