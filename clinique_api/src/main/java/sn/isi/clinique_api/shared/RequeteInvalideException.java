package sn.isi.clinique_api.shared;

public class RequeteInvalideException extends RuntimeException {

    public RequeteInvalideException(String message) {
        super(message);
    }
}