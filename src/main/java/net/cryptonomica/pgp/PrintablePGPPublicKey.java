package net.cryptonomica.pgp;

import org.bouncycastle.openpgp.PGPPublicKey;

import java.util.Iterator;

public class PrintablePGPPublicKey {

    PGPPublicKey base;

    public PrintablePGPPublicKey(PGPPublicKey iBase) {
        base = iBase;
    }

    public PGPPublicKey getPublicKey() {
        return base;
    }

    @Override
    public String toString() {
        StringBuilder outStr = new StringBuilder();
        Iterator iter = base.getUserIDs();

        outStr.append("[0x");
        outStr.append(Integer.toHexString((int) base.getKeyID()).toUpperCase());
        outStr.append("] ");

        while (iter.hasNext()) {
            outStr.append(iter.next().toString());
            outStr.append("; ");
        }

        return outStr.toString();
    }


}