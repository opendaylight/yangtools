package org.opendaylight.yangtools.ietf.stringtypes;

import static com.google.common.base.Preconditions.checkArgument;
import static org.opendaylight.yangtools.ietf.stringtypes.StringTypeUtils.hexValue;

import com.google.common.annotations.Beta;
import javax.annotation.concurrent.ThreadSafe;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.AbstractDerivedStringSupport;

@Beta
@NonNullByDefault
@ThreadSafe
public final class MacAddressStringSupport extends AbstractDerivedStringSupport<MacAddressString> {
    private static final MacAddressStringSupport INSTANCE = new MacAddressStringSupport();

    private MacAddressStringSupport() {
        super(MacAddressString.class);
    }

    public static MacAddressStringSupport getInstance() {
        return INSTANCE;
    }

    @Override
    public MacAddressString fromCanonicalString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        final int top = hexValue(str.charAt(0)) << 28 | hexValue(str.charAt(1)) << 24
                | hexValue(str.charAt(3)) << 20 | hexValue(str.charAt(4)) << 16
                | hexValue(str.charAt(6)) << 12 | hexValue(str.charAt(7)) << 8
                | hexValue(str.charAt(9)) << 4 | hexValue(str.charAt(10));
        final short bottom = (short) (hexValue(str.charAt(12)) << 12 | hexValue(str.charAt(13)) << 8
                | hexValue(str.charAt(15)) << 4 | hexValue(str.charAt(16)));
        return new MacAddressString(top, bottom);
    }

    @Override
    public MacAddressString fromString(final String str) {
        checkArgument(str.length() == 17, "Malformed string \"%s\"", str);
        for (int i = 2; i < 17; i += 3) {
            checkColon(str, i);
        }
        return fromCanonicalString(str);
    }

    private static void checkColon(final String str, final int offset) {
        final char ch = str.charAt(offset);
        checkArgument(ch == ':', "Invalid character '%s' at offset %s", ch, offset);
    }
}
