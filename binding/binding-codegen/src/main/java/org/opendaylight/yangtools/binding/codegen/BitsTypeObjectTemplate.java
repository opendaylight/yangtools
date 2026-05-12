/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.BitsTypeObject;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.binding.model.api.BitsTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

/**
 * A template for {@link BitsTypeObject} specializations.
 */
@NonNullByDefault
final class BitsTypeObjectTemplate extends ArchetypeTemplate<BitsTypeObjectArchetype> {
    record Builder(BitsTypeObjectArchetype type, DataRootArchetype root) implements Template.Builder {
        Builder {
            requireNonNull(type);
            requireNonNull(root);
        }

        @Override
        public BitsTypeObjectTemplate build() {
            return new BitsTypeObjectTemplate(type, root);
        }
    }

    private BitsTypeObjectTemplate(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        super(javaType, archetype, root);
    }

    private BitsTypeObjectTemplate(final BitsTypeObjectArchetype archetype, final DataRootArchetype root) {
        super(GeneratedClass.of(archetype), archetype, root);
    }

    static BlockBuilder generateInner(final GeneratedClass.Nested javaType, final BitsTypeObjectArchetype archetype,
            final DataRootArchetype root) {
        return new BitsTypeObjectTemplate(javaType, archetype, root).generateAsInnerClass();
    }

    @Override
    BlockBuilder body() {
        final var archetype = archetype();
        final var simpleName = archetype.simpleName();

        return null;
    }



//  final var builder = builderFactory.newBitsTypeObjectBuilder(typeName);
//  builder.setTypedef(isTypedef());
//  builder.addImplementsType(BindingTypes.BITS_TYPE_OBJECT);
//  builder.setBaseType(typedef);
//  YangSourceDefinition.of(module, definingStatement).ifPresent(builder::setYangSourceDefinition);
//
//  for (var bit : typedef.getBits()) {
//      final String name = bit.getName();
//      var genPropertyBuilder = builder.addProperty(Naming.getPropertyName(name));
//      genPropertyBuilder.setReadOnly(true);
//      genPropertyBuilder.setReturnType(Types.primitiveBooleanType());
//  }
//  builder.addConstant(Types.immutableSetTypeFor(Types.STRING), TypeConstants.VALID_NAMES_NAME, typedef);
//
//  builder.setModuleName(module.argument().getLocalName());
//  builderFactory.addCodegenInformation(typedef, builder);
//  AbstractTypeObjectGenerator.annotateDeprecatedIfNecessary(typedef, builder);
//  AbstractTypeObjectGenerator.makeSerializable(builder);
//  return builder.build();

    @Override
    void appendValidNames(final BlockBuilder bb) {
        for (var c : type().getConstantDefinitions()) {
            if (TypeConstants.VALID_NAMES_NAME.equals(c.getName())) {
                bb.nl().blk(validNamesAndValues((BitsTypeDefinition) c.getValue()));
            }
        }
    }

    private BlockBuilder validNamesAndValues(final BitsTypeDefinition typedef) {
        final var override = importedName(OVERRIDE);

        return newBlockBuilder()
            .nl()
            .at().eol(override)
            .str("public ").gen(importedName(IMMUTABLE_SET), importedName(Types.STRING)).str(" validNames()").oB()
                .eol("return " + TypeConstants.VALID_NAMES_NAME + ";")
            .cB()
            .nl()
            .at().eol(override)
            .str("public boolean[] values()").oB()
                .str("return new boolean[]").jBlock(bb -> {
                    final var bits = typedef.getBits();
                    if (bits.isEmpty()) {
                        bb.eol("// empty");
                        return;
                    }

                    final var it = bits.iterator();
                    while (true) {
                        final var bit = it.next();
                        bb.str(getterMethodName(Naming.getPropertyName(bit.getName()))).str("()");
                        if (!it.hasNext()) {
                            bb.nl();
                            break;
                        }
                        bb.eol(",");
                    }
                }).eS()
            .cB()
            .nl();
    }
}
