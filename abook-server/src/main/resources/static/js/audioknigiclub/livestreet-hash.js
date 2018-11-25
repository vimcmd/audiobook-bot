// jp31.js:formatted

// var security_ls_key = "d2f83253e7bd5bf60c6fbeb97b99cda1"; // LIVESTREET_SECURITY_KEY, in page head
// var crypto_js_pass = "EKxtcg46V"; // jp31.js:3707 (function k("0x2"))

var format = { // jp31.js:3689
    stringify : function(msg) {
        var data = {
            ct : msg.ciphertext.toString(CryptoJS.enc.Base64)
        };
        return msg.iv && (data.iv = msg.iv.toString()), msg.salt && (data.s = msg.salt.toString()), JSON.stringify(data);
    },
    parse : function(t) {
        /** @type {*} */
        var e = JSON.parse(t);
        var decryptOptions = CryptoJS.lib.CipherParams.create({
            ciphertext : CryptoJS.enc.Base64.parse(e.ct)
        });
        return e.iv && (decryptOptions.iv = CryptoJS.enc.Hex.parse(e.iv)), e.s && (decryptOptions.salt = CryptoJS.enc.Hex.parse(e.s)), decryptOptions;
    }
};

var hash = function (message, key) {
    return CryptoJS.AES.encrypt(JSON.stringify(message), key, {format: format}).toString();
};

