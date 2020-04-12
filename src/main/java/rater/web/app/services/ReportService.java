package rater.web.app.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rater.web.app.session.UserSession;

@Component
public class ReportService {

    private final UserSession userSession;

    @Autowired
    public ReportService(UserSession userSession) {
        this.userSession = userSession;
    }
}
