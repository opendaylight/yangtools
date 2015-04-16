/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */



package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Arrays;
import java.beans.ConstructorProperties;
import com.google.common.primitives.Ints;
import java.net.Inet4Address;
import java.net.InetAddress;
import com.google.common.net.InetAddresses;


/**
 * The ipv4-address type represents an IPv4 address in
 *         dotted-quad notation.  The IPv4 address may include a zone
 *         index, separated by a % sign.
 *         The zone index is used to disambiguate identical address
 *         values.  For link-local addresses, the zone index will
 *         typically be the interface index number or the name of an
 *         interface.  If the zone index is not present, the default
 *         zone of the device will be used.
 *         The canonical format for the zone index is the numerical
 *         format
 */
public class Ipv4Address
 implements Serializable {
    private static final long serialVersionUID = -7811024098214962896L;
    private static final List<Pattern> patterns;
    public static final List<String> PATTERN_CONSTANTS = ImmutableList.of("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(%[\\p{N}\\p{L}]+)?$");

    static {
        final List<Pattern> l = new ArrayList<Pattern>();
        for (String regEx : PATTERN_CONSTANTS) {
            l.add(Pattern.compile(regEx));
        }

        patterns = ImmutableList.copyOf(l);
    }
    final private byte [] _binary_form;

    /**
     * The spec is broken here - it presumes we have to keep "zone" for network originated IPv4
     * There "zone" as in routing scope, vrf, etc is all out of band and not co-habiting the same
     * data source with the packet header
     */

    final private String _zone;

    @ConstructorProperties("value")
    public Ipv4Address(java.lang.String _value) {


        Preconditions.checkNotNull(_value, "Supplied value may not be null");

        boolean valid = false;
        for (Pattern p : patterns) {
            if (p.matcher(_value).matches()) {
                valid = true;
                break;
            }
        }

        Preconditions.checkArgument(valid, "Supplied value \"%s\" does not match any of the permitted patterns %s", _value, PATTERN_CONSTANTS);

        String [] address =  _value.split("%");

        /*
        * our real IPv4 presentation - we may consider replacing this with int at
        * a later date. Depends on how it is being (ab)used downstream.
        * getByAddress when given a numeric IP does not do any checking besides length
        * and that is verified by the regexp so it should always succeed
        */

        InetAddress _inet_form = InetAddresses.forString(address[0]);

        Preconditions.checkArgument(_inet_form instanceof Inet4Address);

        this._binary_form = _inet_form.getAddress();

        if (address.length == 2) {
            this._zone = address[1];
        } else {
            this._zone = null;
        }
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */

    public Ipv4Address(Ipv4Address source) {
        this._binary_form = source._binary_form;
        this._zone = source._zone;
    }

    /**
     * Creates an Ipv4 object from a byte sequence, the argument is copied before
     * Ipv4Address instantiation
     * Callers which do not need to worry about the _binary_form safety should use
     * Ipv4Address(_binary_form, false) instead
     * The only check is for byte [] length
     *
     * @param _binary_form Ipv4Address represented as byte array - as seen on the network
     */

    public Ipv4Address(byte [] _binary_form) {
        this(_binary_form, true);
    }


    /* Note - no support for zoning in this form, this is network originated IP only */

    /**
     * Creates an Ipv4Address from binary (packet) representation
     *
     * @param _binary_form ip address as byte array
     * @param clone_me - if true, will create a copy of the original
     *                   v4 address binary represenation first
     */

    public Ipv4Address(byte [] _binary_form, boolean clone_me) {
        /* check onjava.lang.ly length */
        Preconditions.checkArgument(
            (_binary_form.length == 4),
            "Supplied binary value \"%d\" is not the correct length",
            _binary_form.length
        );
        if (clone_me) {
            this._binary_form = Arrays.copyOf(_binary_form, 4);
            this._zone = null;
        } else {
            this._binary_form = _binary_form;
            this._zone = null;
        }
    }

    /**
     * Creates an Ipv4Address from integer representation
     *
     * @param _int_form - ip address as integer
     */

    public Ipv4Address(int _int_form) {
        /* check nothing - 4 bytes are 4 bytes at all times of the day */
        this._binary_form = Ints.toByteArray(_int_form);
        this._zone = null;
    }

    public static Ipv4Address getDefaultInstance(String defaultValue) {
        return new Ipv4Address(defaultValue);
    }

    /**
     * @return binary form as used in network
     */

    public byte [] getBinaryForm() {
        return this._binary_form;
    }

    /**
     * @return integer form - alternative network use
     */

    public int getIntegerForm() {
        return Ints.fromByteArray(_binary_form);
    }


    /* This warrants a comment on James Gosling opinion on why Java
     * should not have any signed types. Or maybe not
     */

    private int unsigned_byte(byte arg) {
        return (arg & 0xff);
    }

    public java.lang.String getValue() {
        /* fake the "expected" value by rebuilding string out of binary */
        StringBuilder sb = new java.lang.StringBuilder();
        sb.append(
            String.format(
                "%d.%d.%d.%d",
                unsigned_byte(_binary_form[0]),
                unsigned_byte(_binary_form[1]),
                unsigned_byte(_binary_form[2]),
                unsigned_byte(_binary_form[3])
            )
        );
        if (_zone != null) {
            sb.append("%");
            sb.append(_zone);
        }
        return sb.toString();

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((_binary_form == null) ? 0 : Arrays.hashCode(_binary_form))
            + ((_zone == null) ? 0 : _zone.hashCode());
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
        Ipv4Address other = (Ipv4Address) obj;
        if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }

        /* compare zones if present */

        if (_zone == null) {
            if (other._zone != null) {
                return false;
            }
        } else if (! _zone.equals(other._zone)) {
            return false;
        }

        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address.class.getSimpleName()).append(" [");

        builder.append(this.getValue());

        return builder.append(']').toString();
    }
}
