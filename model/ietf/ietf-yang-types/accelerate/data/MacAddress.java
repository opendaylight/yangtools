/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Iterator;
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
    private static final Splitter splitter;
    private static final int MACSIZE = 6;

    static {
        final List<Pattern> l = new ArrayList<Pattern>();
        for (String regEx : PATTERN_CONSTANTS) {
            l.add(Pattern.compile(regEx));
        }
        splitter = Splitter.on(':');
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

        Iterator<String> digits =  splitter.split(_value).iterator();

        /* our real MAC presentation */

        byte [] temp_binary = new byte [MACSIZE];

        int i = 0;

        while (digits.hasNext()) {
            temp_binary[i] = (byte) Short.parseShort(digits.next(), 16);
            i++;
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
     * Creates a Mac object from a byte sequence, the argument is copied before
     * MacAddress instantiation 
     * Callers which do not need to worry about the _binary_form safety should use 
     * MacAddress(_binary_form, false) instead
     * The only check is for byte [] length
     *
     * @param _binary_form Mac represented as byte array - as seen on the network
     */

    public MacAddress(byte [] _binary_form) {
        this(_binary_form, true);
    }


    /**
     * Creates a Mac object from a byte sequence providing an optional clone flag
     * The only check is for byte [] length
     *
     * @param _binary_form Mac represented as byte array - as seen on the network
     * @param clone_me clone the argument before instantiating MacAddress
     */

    public MacAddress(byte [] _binary_form, boolean clone_me) {
        /* check onjava.lang.ly length */
        Preconditions.checkArgument(
            (_binary_form.length == MACSIZE),
            "Supplied binary value \"%s\" is not the correct length",
            "" + _binary_form.length
        );
        if (clone_me) {
            this._binary_form = Arrays.copyOf(_binary_form, 6);
        } else {
            this._binary_form = _binary_form;
        }
    }

    public static MacAddress getDefaultInstance(String defaultValue) {
        return new MacAddress(defaultValue);
    }

     /**
     * @return String form as expected from model
     */

    public java.lang.String getValue() {
        /*
         * Provide identical behaviour to the generated class by rebuilding the string out of
         * binary. We use format here for readability, this may be replaced by something faster
         * at a later date
         */
        return String.format(
            "%02x:%02x:%02x:%02x:%02x:%02x",
            _binary_form[0],
            _binary_form[1],
            _binary_form[2],
            _binary_form[3],
            _binary_form[4],
            _binary_form[5]
        );
    }

     /**
     * @return binary form as used on the network
     */

    public byte [] getBinaryForm() {
        /* Provide the binary form directly to ensure penalty-less serialization and bitops */
        return Arrays.copyOf(_binary_form, MACSIZE);
    }

     /**
     * @return binary form as used on the network
     */

    public byte [] getBinaryFormUnsafe() {
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
        if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }
        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress.class.getSimpleName()).append(" [");

        builder.append(this.getValue());

        return builder.append(']').toString();
    }

}
