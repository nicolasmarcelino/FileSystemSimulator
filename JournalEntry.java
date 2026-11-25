import java.time.LocalDateTime;

public record JournalEntry(
        int id,
        LocalDateTime timestamp,
        String status, // TODO: possivel enum: COMMIT, BEGIN, ABORT
        // TODO: possivel enum: CREATE_DIR, CREATE_FILE, RENAME_FILE, RENAME_DIR, DELETE_DIR, DELETE_FILE...
        String operation,
        String[] params
) { }
