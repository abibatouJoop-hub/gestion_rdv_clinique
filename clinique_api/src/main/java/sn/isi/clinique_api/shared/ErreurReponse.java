package sn.isi.clinique_api.shared;

import java.time.Instant;
import java.util.List;

public record ErreurReponse(int code, String message, Instant horodatage, List<String> details) {
}
