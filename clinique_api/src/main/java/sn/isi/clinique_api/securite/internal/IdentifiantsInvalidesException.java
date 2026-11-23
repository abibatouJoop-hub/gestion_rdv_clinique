// IdentifiantsInvalidesException.java
package sn.isi.clinique_api.securite.internal;

class IdentifiantsInvalidesException extends RuntimeException {
    IdentifiantsInvalidesException() {
        super("Identifiants invalides");
    }
}