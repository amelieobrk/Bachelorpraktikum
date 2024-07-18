package de.kreuzenonline.kreuzen.i18n;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ResourceBundle;

@Configuration
public class I18NConfig {

    private final String appLocale;

    public I18NConfig(@Value("${app.locale}") String appLocale) {
        this.appLocale = appLocale;
    }

    @Bean
    public ResourceBundle getResourceBundle() {

        return ResourceBundle.getBundle("messages/" + appLocale);
    }
}
