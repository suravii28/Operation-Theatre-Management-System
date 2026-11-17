
package com.otms.service;

import com.otms.model.AuditEvent;
import com.otms.repository.AuditEventRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    public void log(String actorEmail, String action,
                    String entityType, Long entityId, String details) {

        AuditEvent event = new AuditEvent();
        event.setActorEmail(actorEmail);
        event.setAction(action);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setDetails(details);

        auditEventRepository.save(event);
    }

    public List<AuditEvent> search(String actor, String action,
                                   LocalDateTime from, LocalDateTime to) {
        return auditEventRepository.search(
                actor == null || actor.isBlank() ? null : actor,
                action == null || action.isBlank() ? null : action,
                from, to);
    }

    public void logCurrentUser(String action, String entityType,
                               Long entityId, String details) {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String actorEmail = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            actorEmail = authentication.getName();
        }

        log(actorEmail, action, entityType, entityId, details);
    }
}
