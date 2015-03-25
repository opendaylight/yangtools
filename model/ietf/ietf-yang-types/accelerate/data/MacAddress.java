/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.beans.ConstructorProperties;
import java.util.Arrays;


 /**
 * The mac-address type represents an IEEE 802 MAC address.
 *
 * The canonical representation uses lowercase characters - any value is
 * normalized to canonical representation which is _LOWER_ case
 *
 *        In the value set and its semantics, this type is equivalent
 *        to the MacAddress textual convention of the SMIv2.
 *
 */

public class MacAddress
 implements Serializable {
    private static final long serialVersionUID = -8041141443619966138L;
    private static final List<Pattern> patterns;
    public static final List<String> PATTERN_CONSTANTS = ImmutableList.of("^[0-9a-fA-F]{2}(:[0-9a-fA-F]{2}){5}$");

    static {
        final List<Pattern> l = new ArrayList<Pattern>();
        for (String regEx : PATTERN_CONSTANTS) {
            l.add(Pattern.compile(regEx));
        }

        patterns = ImmutableList.copyOf(l);
    }

    /* Normalized Storage - network format */

    final private byte [] _binary_form;

    /**
     * Backwards compatible constructor - the signature is identical to the result of yangtools
     * generation
     */

    @ConstructorProperties("value")
    public MacAddress(java.lang.String _value) {

        Preconditions.checkNotNull(_value, "Supplied value may not be null");

        /* Test using the same patterns as in generated code */

        boolean valid = false;
        for (Pattern p : patterns) {
            if (p.matcher(_value).matches()) {
                valid = true;
                break;
            }
        }

        Preconditions.checkArgument(
            valid,
            "Supplied value \"%s\" does not match any of the permitted patterns %s",
            _value,
            PATTERN_CONSTANTS
        );

        /**
         * It has passed all checks, we now can make a proper "internal" MAC out of it
         * We can safely split here because it is guaranteed to be in the correct format
         */

        String [] digits =  _value.split(":");

        /* our real MAC presentation */

        byte [] temp_binary = new byte [6];

        for (int i=0; i < 6; i++) {
            temp_binary[i] = (byte) Short.parseShort(digits[i], 16);
        }

        this._binary_form = temp_binary;

    }

    /**
     * Creates a copy from Source Object - identical to yangtools behaviour
     *
     * @param source Source object
     */
    public MacAddress(MacAddress source) {
        this._binary_form = source._binary_form;
    }

    /**
     * Creates a Mac object from a byte sequence
     * Usage notes: it is the caller's obligation to clone() the sequence if needed!!!
     * The only check is for byte [] length
     *
     * @param _binary_form Mac represented as byte array - as seen on the network
     */
    public MacAddress(byte [] _binary_form) {
        /* check onjava.lang.ly length */
        Preconditions.checkArgument(
            (_binary_form.length == 6),
            "Supplied binary value \"%s\" is not the correct length",
            "" + _binary_form.length
        );
        this._binary_form = _binary_form;
    }

    public static MacAddress getDefaultInstance(String defaultValue) {
        return new MacAddress(defaultValue);
    }

    public java.lang.String getValue() {
        /* Provide identical behaviour to the generated class by rebuilding the string out of binary */
        StringBuilder sb = new java.lang.StringBuilder();
        sb.append(String.format("%02x", _binary_form[0]));
        for (int i = 1; i < 6; i++) {
            sb.append(String.format(":%02x", _binary_form[i]));
        }
        return sb.toString();
    }

     /**
     * @return binary form as used on the network
     */

    public byte [] getBinaryForm() {
        /* Provide the binary form directly to ensure penalty-less serialization and bitops */
        return _binary_form;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_binary_form == null) ? 0 : Arrays.hashCode(_binary_form));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MacAddress other = (MacAddress) obj;
        if (_binary_form == null) {
            if (other._binary_form != null) {
                return false;
            }
        } else if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress.class.getSimpleName()).append(" [");
        boolean first = true;

        if (_binary_form != null) {
            if (first) {
                first = false;
            } else {
                builder.append(", ");
            }
            builder.append(String.format("_value=%02x",_binary_form[0]));
            for (int i=1; i < 6 ; i++) {
                builder.append(String.format(":%02x", _binary_form[i]));
            }
         }
        return builder.append(']').toString();
    }

}

