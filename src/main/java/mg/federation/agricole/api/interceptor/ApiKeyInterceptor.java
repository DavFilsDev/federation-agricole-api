package mg.federation.agricole.api.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.federation.agricole.api.config.DataSource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private final DataSource dataSource;

    public ApiKeyInterceptor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // Récupérer la clé API de l'en-tête
        String apiKey = request.getHeader("x-api-key");

        // Récupérer la clé API valide depuis le DataSource
        String validApiKey = dataSource.getApiKey();

        // Vérifier si l'en-tête est présent
        if (apiKey == null || apiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Bad credentials\"}");
            return false;
        }

        // Vérifier si la clé API est correcte
        if (!apiKey.equals(validApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Bad credentials\"}");
            return false;
        }

        // Clé API valide, continuer le traitement
        return true;
    }
}