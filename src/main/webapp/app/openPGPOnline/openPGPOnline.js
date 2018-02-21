"use strict";

/*
* OpenPGP.js
* http://openpgpjs.org
* https://github.com/openpgpjs/openpgpjs
* API: http://openpgpjs.org/openpgpjs/doc/
* */

$(function () {

    console.log("app.js is loaded");

    /* Web Storage API , https://developer.mozilla.org/en-US/docs/Web/API/Web_Storage_API/Using_the_Web_Storage_API */
    var storageType = 'localStorage'; // or 'sessionStorage'
    var storageAvailable =
        function () {
            try {
                var storage = window[storageType],
                    x = '__storage_test__';
                storage.setItem(x, x);
                storage.removeItem(x);
                return true;
            }
            catch (e) {
                return e instanceof DOMException && (
                        // everything except Firefox
                    e.code === 22 ||
                    // Firefox
                    e.code === 1014 ||
                    // test name field too, because code might not be present
                    // everything except Firefox
                    e.name === 'QuotaExceededError' ||
                    // Firefox
                    e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
                    // acknowledge QuotaExceededError only if there's something already stored
                    storage.length !== 0;
            }
        }();
    console.log("storageAvailable: " + storageAvailable);

    var myPublicKey;
    var myPrivateKey;

    var loadPublicKeyFromLocalStorage = function () {
        if (storageAvailable) {
            myPublicKey = localStorage.getItem('myPublicKey');
            if (myPublicKey) {
                $('#pubkeyShow').val(myPublicKey);
                $('#publicKeyLocalStorageMessage').text('Public key loaded from your browser local storage');
            }
        }
    };

    var loadPrivateKeyFromLocalStorage = function () {
        myPrivateKey = localStorage.getItem('myPrivateKey');
        if (myPrivateKey) {
            $("#privkeyShow").val(myPrivateKey);
            $('#privateKeyLocalStorageMessage').text('Private key loaded from your browser local storage');
        }
    };

    loadPrivateKeyFromLocalStorage();
    loadPublicKeyFromLocalStorage();

    if (!window.crypto.getRandomValues) {
        window.alert("This browser isn't supported!");
    }

    // Generate Mockup Data
    $("#useMockupData").click(function (event) {
        $("#passphrase").val("very strong password");
        $("#firstName").val("John");
        $("#lastName").val("Doe");
        $("#userEmail").val("john.doe@gmail.com");
        $("#expire_in").val(1);
        $("#messageText").val("this is a test message we want to encrypt or sign");
        // $("#numBits").val(1024);
        $("#numBits").val(2048);
    });

    // Encrypt
    $("#encrypt").click(function (event) {
        console.log("button clicked");
        var armoredKey = $('#pubkeyShow').val();
        console.log("armoredKey: " + armoredKey);
        var publicKey = openpgp.key.readArmored(armoredKey);

        var message = $("#messageText").val();
        console.log("message: " + message);

        var options, encryptedRaw, encryptedASCIIarmored;

        options = {
            data: message, // input as Uint8Array (or String)
            // passwords: ['secret stuff'],// multiple passwords possible
            publicKeys: publicKey.keys, // <-- !!!!
            // armor: false // don't ASCII armor (for Uint8Array output)
        };

        openpgp.encrypt(options).then(function (ciphertext) {
            console.log(ciphertext);
            console.log(JSON.stringify(ciphertext));
            encryptedASCIIarmored = ciphertext.data; // '-----BEGIN PGP MESSAGE ... END PGP MESSAGE-----'
            console.log("ciphertext.data: " + ciphertext.data);
            $("#encryptedText").val(ciphertext.data);
        });
    });

    // Decrypt
    $("#decryptButton").click(function (event) {

        var armoredMessage = $("#encryptedText").val();
        var armoredPubKey = $("#pubkeyShow").val();
        var armoredPrivKey = $("#privkeyShow").val();
        console.log("armoredPrivKey: ");
        console.log(armoredPrivKey);

        // var privateKeyEncrypted = openpgp.key.readArmored(armoredPrivKey).keys[0];
        var privateKeyEncrypted = openpgp.key.readArmored(armoredPrivKey).keys[0];
        console.log("privateKeyEncrypted: ");
        console.log(privateKeyEncrypted);
        var passphrase = document.getElementById("passphrase").value;
        var decrypted = privateKeyEncrypted.decrypt(passphrase); // boolean
        var privateKeyDecrypted = privateKeyEncrypted;

        var options = {
            message: openpgp.message.readArmored(armoredMessage), // parse armored message
            publicKeys: openpgp.key.readArmored(armoredPubKey).keys, // for verification (optional)
            privateKey: privateKeyDecrypted // after privateKeyEncrypted.decrypt(passphrase)
        };

        openpgp.decrypt(options).then(function (plaintext) {

            console.log("plaintext.data: " + plaintext.data);

            $("#decryptedText").val(plaintext.data);
        });
    });

    // Check Signature
    $("#checkSignatureButton").click(function (event) {
        $('#checkSignatureResult').text("verifying the signature...");
        var armoredMessage = $("#signedMessage").val();
        var armoredPubKey = $("#pubkeyShow").val();
        console.log("armoredPubKey: ");
        console.log(armoredPubKey);

        var message;
        var publicKeys;
        var options = {};

        try {
            message = openpgp.cleartext.readArmored(armoredMessage);
            publicKeys = openpgp.key.readArmored(armoredPubKey).keys;

        } catch (error) {
            console.log(error.message);
            $('#checkSignatureResult').text("This is not valid OpenPGP signed text").css({'color': 'red'});
            return;
        }

        if (message && publicKeys) {
            options.message = message;
            options.publicKeys = publicKeys;
        } else {
            $('#checkSignatureResult').text("This is not valid OpenPGP signed text").css({'color': 'red'});
            return;
        }

        openpgp.verify(options).then(function (verified) {
            var validity = verified.signatures[0].valid; // true
            console.log("verified.signatures:");
            console.log(verified.signatures);
            var result;
            var css = {};
            if (validity) {
                result = 'Signed by key id: [' + verified.signatures[0].keyid.toHex().toUpperCase() + ']';
                css.color = "green";
            } else {
                result = "Signature can not be verified";
                css.color = "red";
            }
            console.log(result);
            $('#checkSignatureResult').text(result).css(css);
        });

    });

    function makeGenerateKeysOptions() {
        var opts = {};
        opts.passphrase = document.getElementById("passphrase").value;
        opts.firstName = document.getElementById("firstName").value;
        opts.lastName = document.getElementById("lastName").value;
        opts.name = opts.firstName + " " + opts.lastName;
        opts.userEmail = document.getElementById("userEmail").value;
        opts.userId = opts.name + " <" + opts.userEmail + ">";
        opts.numBits = document.getElementById("numBits").value;
        var years = document.getElementById("expire_in").value;
        opts.expire_in = (60 * 60 * 24) * 365 * years; // expires in ... years
        console.log("makeGenerateKeysOptions() :");
        console.log(JSON.stringify(opts));
        return opts;
    }

    function checkGenerateKeysOptions(opts) {
        for (var objProperty in opts) {
            if (opts.hasOwnProperty(objProperty)) {
                console.log(objProperty + " : " + opts[objProperty]);
                if (typeof opts[objProperty] === 'undefined' || opts[objProperty] === null || opts[objProperty].toString().length === 0) {
                    $('#generateKeyOptionsMessage').text(objProperty + " is missing").css('color', 'red');
                    return false;
                }
            }
        }
        $('#generateKeyOptionsMessage').text("");
        return true;
    }

    function emptyKeyData() {
        $("#keyId").text("");
        $("#fingerprint").text("");
        $("#userId").text("");
        $("#created").text("");
        $("#exp").text("");
        $("#bitsSize").text("");
    }

    $("#generateKeysOpenPGPjs").click(function (event) {

        var genOpts = makeGenerateKeysOptions();
        if (!checkGenerateKeysOptions(genOpts)) {
            return;
        }

        emptyKeyData();
        $('#pubkeyShow').val("");
        $("#privkeyShow").val("");
        $('#publicKeyLocalStorageMessage').text("");
        $('#privateKeyLocalStorageMessage').text("");
        $("#statusMessage").text("Generating key, please wait ...");

        //
        var options = {
            userIds: [
                {
                    name: genOpts.name,
                    email: genOpts.userEmail
                }
            ], // multiple user IDs
            numBits: genOpts.numBits, // RSA key size
            // protects the private key
            passphrase: genOpts.passphrase,
            // {Number} keyExpirationTime (optional) The number of seconds after the key creation time that the key expires
            keyExpirationTime: genOpts.expire_in
        };

        // (static) generateKey(userIds, passphrase, numBits, unlocked, keyExpirationTime) → {Promise.<Object>}
        openpgp
            .generateKey(options)
            .then(function (key) {
                var privkey = key.privateKeyArmored; // '-----BEGIN PGP PRIVATE KEY BLOCK ... '
                console.log("privkey:");
                console.log(privkey);
                myPrivateKey = privkey;
                $("#privkeyShow").val(myPrivateKey);

                var pubkey = key.publicKeyArmored; // '-----BEGIN PGP PUBLIC KEY BLOCK ... '

                myPublicKey = pubkey;
                $("#statusMessage").text("");
                $('#pubkeyShow').val(myPublicKey);
                console.log("myPublicKey:");
                console.log(myPublicKey);

            });

    }); // end #generateKeysOpenPGPjs

    $("#saveSignedMessageAsFile").click(function () {
        var signedMessage = $("#signedMessage").val();
        var blob = new Blob([signedMessage], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "signedMessage.txt");
    });

    $("#saveEncryptedTextAsFile").click(function () {
        var encryptedText = $("#encryptedText").val();
        var blob = new Blob([encryptedText], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "encryptedText.txt");
    });

    $("#saveDecryptedTextAsFile").click(function () {
        var decryptedText = $("#decryptedText").val();
        var blob = new Blob([decryptedText], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "decryptedText.txt");
    });

    $("#savePublicKeyAsFile").click(function () {
        var blob = new Blob([myPublicKey], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "publicKey.txt");
    });

    $("#savePrivateKeyAsFile").click(function () {
        var blob = new Blob([myPrivateKey], {type: "text/plain;charset=utf-8"});
        saveAs(blob, "privateKey.txt");
    });

    $("#savePublicKeyToLocalStorageButton").click(function () {
        var message;
        if (storageAvailable) {
            localStorage.setItem('myPublicKey', myPublicKey);
            message = 'Public key stored to browser local storage';
        } else {
            message = 'Browser local storage not available'
        }
        $('#publicKeyLocalStorageMessage').text(message);
    });

    $("#removePublicKeyFromLocalStorageButton").click(function () {
        var message;
        if (storageAvailable) {
            localStorage.removeItem('myPublicKey');
            message = 'Public key removed from browser local storage';
        } else {
            message = 'Browser local storage not available'
        }
        $('#publicKeyLocalStorageMessage').text(message);
    });

    $("#savePrivateKeyToLocalStorageButton").click(function () {
        var message;
        if (storageAvailable) {
            localStorage.setItem('myPrivateKey', myPrivateKey);
            message = 'Private key stored to browser local storage';
        } else {
            message = 'Browser local storage not available'
        }
        $('#privateKeyLocalStorageMessage').text(message);
    });

    $("#removePrivateKeyFromLocalStorageButton").click(function () {
        var message;
        if (storageAvailable) {
            localStorage.removeItem('myPrivateKey');
            message = 'Private key removed from browser local storage';
        } else {
            message = 'Browser local storage not available'
        }
        $('#privateKeyLocalStorageMessage').text(message);
    });

    // ----- READ PRIVATE KEY - OPENPGP.JS
    $("#readPrivateKeyDataOpenPGPjs").click(function () {

        var privateKey = openpgp.key.readArmored(
            $('#privkeyShow').val()
        );
        console.log(privateKey);

        var keyId = '[' + privateKey.keys[0].primaryKey.keyid.toHex().toUpperCase() + ']';
        var fingerprint = privateKey.keys[0].primaryKey.fingerprint.toUpperCase();
        var userId = privateKey.keys[0].users[0].userId.userid;
        var created = privateKey.keys[0].primaryKey.created;
        var exp = privateKey.keys[0].getExpirationTime();
        var bitsSize = privateKey.keys[0].primaryKey.getBitSize();

        console.log(keyId);
        console.log(fingerprint);
        console.log(userId);
        console.log("created: " + created);
        console.log("exp: " + exp);
        console.log("bits size: " + bitsSize);

        $("#keyIdPrivate").text(keyId);
        $("#fingerprintPrivate").text(fingerprint);
        $("#userIdPrivate").text(userId);
        $("#createdPrivate").text(created);
        $("#expPrivate").text(exp);
        $("#bitsSizePrivate").text(bitsSize);

    });

    // ----- READ PUBLIC KEY - OPENPGP.JS:
    $("#readPublicKeyDataOpenPGPjs").click(function () {

        var publicKey = openpgp.key.readArmored(
            $('#pubkeyShow').val()
        );
        var keyId = '[' + publicKey.keys[0].primaryKey.keyid.toHex().toUpperCase() + ']';
        var fingerprint = publicKey.keys[0].primaryKey.fingerprint.toUpperCase();
        var userId = publicKey.keys[0].users[0].userId.userid;
        var created = publicKey.keys[0].primaryKey.created;

        console.log("publicKey.keys[0].primaryKey.created :");
        console.log(publicKey.keys[0].primaryKey.created);
        // Wed Jan 11 2017 17:53:20 GMT+0200 (IST)

        console.log("publicKey.keys[0].primaryKey.created.getTime() : ");
        console.log(publicKey.keys[0].primaryKey.created.getTime());
        // 1484150000000
        // 1,484,150,000,000 (vs. 1,484,150,000 in Kbpgp)

        console.log("publicKey.keys[0].getExpirationTime() :");
        console.log(publicKey.keys[0].getExpirationTime());

        /* https://github.com/openpgpjs/openpgpjs/blob/master/src/key.js#L442*/
        console.log("publicKey.keys[0].primaryKey.expirationTimeV3 : ")
        console.log(publicKey.keys[0].primaryKey.expirationTimeV3);
        // 0

        /* https://github.com/openpgpjs/openpgpjs/blob/master/src/key.js#L463 */
        console.log("publicKey.keys[0].getPrimaryUser().selfCertificate.keyExpirationTime:");
        console.log(publicKey.keys[0].getPrimaryUser().selfCertificate.keyExpirationTime);
        // 31536000

        console.log("publicKey.keys[0].getPrimaryUser().selfCertificate.keyNeverExpires :");
        console.log(publicKey.keys[0].getPrimaryUser().selfCertificate.keyNeverExpires);
        //
        var exp = new Date(
            publicKey.keys[0].primaryKey.created.getTime()
            + (publicKey.keys[0].getPrimaryUser().selfCertificate.keyExpirationTime * 1000)
        );

        //
        /**
         * see: https://github.com/openpgpjs/openpgpjs/blob/master/src/key.js#L472
         * .getExpirationTime() returns the expiration time of the primary key or null if key does not expire
         * @return {Date|null}
         */
        exp = publicKey.keys[0].getExpirationTime(); // <<< ---- use this
        //
        //

        var bitsSize = publicKey.keys[0].primaryKey.getBitSize();

        console.log(fingerprint);
        console.log(keyId);
        console.log(publicKey);
        console.log(userId);
        console.log("created: " + created);
        console.log("exp: " + exp);
        console.log("bits size: " + bitsSize);

        $("#keyId").text(keyId);
        $("#fingerprint").text(fingerprint);
        $("#userId").text(userId);
        $("#created").text(created);
        $("#exp").text(exp);
        $("#bitsSize").text(bitsSize);
    });

    // Read Public key data with Kbpgp
    // $("#readPublicKeyDataKbpgp").click(function () {
    //
    //     // var alice_pgp_key = "-----BEGIN PGP PUBLIC ... etc.";
    //     //
    //     // kbpgp.KeyManager.import_from_armored_pgp({
    //     //     armored: alice_pgp_key
    //     // }, function(err, alice) {
    //     //     if (!err) {
    //     //         console.log("alice is loaded");
    //     //     }
    //     // });
    //
    //     var publicKeyArmored = $('#pubkeyShow').val();
    //     var key;
    //     kbpgp.KeyManager.import_from_armored_pgp({
    //         armored: publicKeyArmored
    //     }, function (err, keyImported) {
    //         if (!err) {
    //             console.log("key is loaded:");
    //             console.log(keyImported);
    //             key = keyImported;
    //         }
    //     });
    //
    //     var fingerprint = key.get_pgp_fingerprint().toString('hex').toUpperCase();
    //     var userEmail = key.pgp.userids[0].components.email;
    //     var userName = key.pgp.userids[0].components.username;
    //     var userId = userName + " <" + userEmail + ">";
    //     //
    //     var created = new Date(key.primary.lifespan.generated * 1000);
    //
    //     //
    //     /*
    //      // OpenPGP.js:
    //      var exp = new Date(
    //      publicKey.keys[0].primaryKey.created.getTime()
    //      + (publicKey.keys[0].getPrimaryUser().selfCertificate.keyExpirationTime * 1000)
    //      );
    //      */
    //
    //     /*
    //     var exp = new Date(
    //         key.primary.lifespan.generated * 1000
    //         + key.primary.lifespan.expire_in * 1000
    //     ); // possible bug, see: https://github.com/keybase/kbpgp/issues/75*/
    //
    //     console.log("key.primary.lifespan: ");
    //     console.log(key.primary.lifespan);
    //     var exp = new Date(
    //         (key.primary.lifespan.generated + key.primary.lifespan.expire_in) * 1000
    //     ); // possible bug, see: https://github.com/keybase/kbpgp/issues/75
    //
    //     console.log(fingerprint);
    //     console.log(userId);
    //     console.log(created);
    //     console.log(exp);
    //
    //     $("#fingerprint").text(fingerprint);
    //     $("#userId").text(userId);
    //     $("#created").text(created);
    //     $("#exp").text(exp);
    // });

    $('#signMessage').click(function (event) {

        $("#signMessageError").text("");

        var messageToSign = $("#messageText").val();
        var privateKeyArmored = $("#privkeyShow").val();
        var passphrase = $("#passphrase").val();

        if (messageToSign.toString().length === 0) {
            $("#signMessageError").text("Message to sign is empty").css('color', 'red');
        }
        if (privateKeyArmored.toString().length === 0) {
            $("#signMessageError").text("Private key is empty").css('color', 'red');
        }
        if (passphrase.toString().length === 0) {
            $("#signMessageError").text("Password for private key is empty").css('color', 'red');
        }
        var privateKeyEncrypted;
        try {
            privateKeyEncrypted = openpgp.key.readArmored(privateKeyArmored).keys[0];
            if (typeof privateKeyEncrypted === 'undefined' || privateKeyEncrypted === null) {
                $("#signMessageError").text("Private key is invalid").css('color', 'red');
                return;
            }
            privateKeyEncrypted.decrypt(passphrase); // boolean
        } catch (error) {
            console.log(error);
            $("#signMessageError").text(error).css('color', 'red');
        }

        var privateKeyDecrypted = privateKeyEncrypted;

        var signObj = {
            data: messageToSign, // cleartext input to be signed
            privateKeys: privateKeyDecrypted, // array of keys or single key with decrypted secret key data to sign cleartext
            armor: true // (optional) if the return value should be ascii armored or the message object
        };
        var signedMessageObj = {};
        // see: https://openpgpjs.org/openpgpjs/doc/openpgp.js.html#line285
        // https://openpgpjs.org/openpgpjs/doc/module-openpgp.html
        openpgp.sign(signObj).then(function (res) { //
            // @return {Promise<String|CleartextMessage>} ASCII armored message or the message of type CleartextMessage
            signedMessageObj = res;
            console.log(JSON.stringify(signedMessageObj));
            console.log(signedMessageObj.data);
            $("#signedMessage").val(signedMessageObj.data);
            // document.getElementById("signedMessage").value = signedMessageObj.data;
        });
    });

    /* https://codepen.io/shaikmaqsood/pen/XmydxJ/ */
    function copyToClipboard(element) {
        // var $temp = $("<input>");
        var $temp = $("<textarea></textarea>");
        $("body").append($temp);
        console.log('copied to clipboard:');
        console.log($(element).val());
        $temp.val(
            // $(element).text()
            $(element).val()
        ).select();
        document.execCommand("copy");
        $temp.remove();
    }

    $("#copyPublicKeyToClipboard").click(function () {
        var element = "#pubkeyShow";
        copyToClipboard(element);
    });

    $("#copyPrivateKeyToClipboard").click(function () {
        var element = "#privkeyShow";
        copyToClipboard(element);
    });

    $("#copyEncryptedTextToClipboard").click(function () {
        var element = "#encryptedText";
        copyToClipboard(element);
    });

    $("#copySignedMessageToClipboard").click(function () {
        var element = "#signedMessage";
        copyToClipboard(element);
    });

    $("#copyDecryptedTextToClipboard").click(function () {
        var element = "#decryptedText";
        copyToClipboard(element);
    });

});
