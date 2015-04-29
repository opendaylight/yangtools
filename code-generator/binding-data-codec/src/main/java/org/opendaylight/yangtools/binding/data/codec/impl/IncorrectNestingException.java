package org.opendaylight.yangtools.binding.data.codec.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IncorrectNestingException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    protected IncorrectNestingException(final String message) {
        super(message);
    }

    public static IncorrectNestingException create(final String message, final Object... args) {
        return new IncorrectNestingException(String.format(message, args));
    }

    public static void check(final boolean check, final String message, final Object... args) {
        if(!check) {
            throw IncorrectNestingException.create(message, args);
        }
    }

    @Nonnull
    public static <V> V checkNonNull(@Nullable final V nullable, final String message, final Object... args) {
        if(nullable != null) {
            return nullable;
        }
        throw IncorrectNestingException.create(message, args);
    }
}
