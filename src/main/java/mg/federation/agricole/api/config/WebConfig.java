package mg.federation.agricole.api.config;

import mg.federation.agricole.api.interceptor.ApiKeyInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ApiKeyInterceptor apiKeyInterceptor;

    public WebConfig(ApiKeyInterceptor apiKeyInterceptor) {
        this.apiKeyInterceptor = apiKeyInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Appliquer l'interceptor à tous les chemins
        registry.addInterceptor(apiKeyInterceptor)
                .addPathPatterns("/**");  // Toutes les requêtes sont protégées
    }
}