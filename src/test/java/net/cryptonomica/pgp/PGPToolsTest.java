package net.cryptonomica.pgp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.DatatypeConverter;

import static org.junit.Assert.*;

/**
 *
 */
public class PGPToolsTest {
    static private final String armoredPublicPGPkeyBlockWRONG = "a;sdlfkjasdfasdfkieadf;lkj;lkj";

    static private final String armoredPublicPGPkeyBlockRSA = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
            "Version: GnuPG v2.0.22 (GNU/Linux)\n" +
            "\n" +
            "mQINBFZpf0wBEADo7NZ4Oymw2pVJMfrNEhGwYOT4CGMOTZHQo3rYFrN3yxCsd/xX\n" +
            "r8kWLvkKvEFSD0XfQlq6slA9fnOtsRZl4JlmabKC33ZkB1zhV2c78AhhVMrfFi2a\n" +
            "114xHhOHkR4LOv8mAyyRKd5mpuyYpPKcF2670jXxANeqNQCoKKM/dPS1uxGapE9Z\n" +
            "GG0GFKvIUqKWJUAv7JqOAxtXbAS7JFMwiH16jL/TeuvKy+0JKvlBM4qxB9S18hi4\n" +
            "GYg1SEATqmeu9E+6alz0L25REYYZZOMja1quF8HywsRY0fSZqCbD+9diP0keAg6z\n" +
            "PSDSlgDF4WBUi+c8PoXcRRZq7+XYgky4l8wfGoGnOZKA4GH2SMlNAX8jCIrC7Cpf\n" +
            "KPCu6NMY533X735Md6fnWOeuyxz4owPRb6OTt4rYiA3/9vCu/waZ1zZx/wATYORS\n" +
            "1wmOx8ZjogeAPOz6a2PW8hrK0tWbT3ILucC7dxjYfdKHZX2+v3eMGvXFRKss8J5i\n" +
            "E5rlJiDnAMoERmIJunh/EJHz4te+RdMy2jWeDQWGzaFyZC92SUo9tAUJk7xez0KX\n" +
            "5sYzhXjFJX/17DwFBTXIOYpFjWUyPaQaP7ByWOjZzhCmQxYRBUJSoxOty1ngkZ+B\n" +
            "7zAZSTXh2mukHukRERG1//2FsiBZeapRhAaFWdWnPDN95bg7L7/qQ5szjQARAQAB\n" +
            "tDdWaWt0b3IgQWdleWV2IDxhZ2V5ZXZAaW50ZXJuYXRpb25hbC1hcmJpdHJhdGlv\n" +
            "bi5vcmcudWs+iQI/BBMBAgApBQJWaX9MAhsvBQkB4lLUBwsJCAcDAgEGFQgCCQoL\n" +
            "BBYCAwECHgECF4AACgkQzjaf2edxc+UywQ/+MfYX5BRzl7iSndhr/vVOEycxZntu\n" +
            "9efMhwQYO87EXT/cNpRMX/u2Wzhx70brzFIenvaYTPpVkYKinKv3iEAauJ1wr5/a\n" +
            "PrUxRcfVnBrV8ecWTO6SbNRDePqayst8bBq9rdqnKnpDEv911zk2hjJtCeFDlUkO\n" +
            "eiTLgYCVOQ7n0fbN4pjrBGAqDGs2SFjJXAKYJpZqY77P0crHxXCMyukrdj2WxnB7\n" +
            "JyMdmBqViJSo/MCi7SFXqVQFTvJZVBhTQEUO5KNJA6oAYztYKXBE3AybE528m571\n" +
            "p+HhJ2mOnlNaRu5y12RRQJ7qmKvN9uzHz0+mt8rRk3oYWCYImTMwxDpc1mT39QKs\n" +
            "0WS/zp5NTIaa+Hvt7V8GH5hmq67YL+JcxEINZGZ+MZmiSKuvGVuqC8ZJpswumVw5\n" +
            "PHtMEL+0zBxZXN41YyfTudssXbvMrNiGIdlQA7CW/7yMviVR4nyQtFQJwYSL4mva\n" +
            "uTQSnihm4Bj6FeYpgGpk1TyIzgtIyaQNSvnlDYFezmN/JsAs+25EP/oxyFLaK+ij\n" +
            "WdjVmnsDGS4l5BWH/a8BYPbSnm7YrlYkYCXjTjK1/EeWA/tggApISFptc8YQw0Wp\n" +
            "Qw0IwhJVPWDVW8nDrv/1IPhTJgYfIfdmxr32RLhNePEZGZ02zfCP9BrlNKut9/Hn\n" +
            "x1yKWjVaWZO8VtU=\n" +
            "=XyhJ\n" +
            "-----END PGP PUBLIC KEY BLOCK-----";


    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void readPublicKey() throws Exception {

    }

    @Test
    public void readPublicKeyFromString() throws Exception {
        // 1
        try {
            PGPTools.readPublicKeyFromString(armoredPublicPGPkeyBlockWRONG);
            fail("wrong input accepted");
        } catch (Exception e) {
            //
        }
        // 2
        assertEquals("57A5FEE5A34D563B4B85ADF3CE369FD9E77173E5",
                DatatypeConverter.printHexBinary(
                        PGPTools.readPublicKeyFromString(armoredPublicPGPkeyBlockRSA).getFingerprint()
                )
        );
        // 3
        assertEquals("Viktor Ageyev <ageyev@international-arbitration.org.uk>",
                PGPTools.readPublicKeyFromString(armoredPublicPGPkeyBlockRSA).getUserIDs().next()
        );

    }

}