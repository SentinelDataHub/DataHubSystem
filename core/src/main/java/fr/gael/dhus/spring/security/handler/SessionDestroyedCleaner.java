package fr.gael.dhus.spring.security.handler;

import javax.servlet.http.HttpSessionEvent;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

import fr.gael.dhus.spring.context.SecurityContextProvider;

@Component
public class SessionDestroyedCleaner extends HttpSessionEventPublisher
{
   @Override
   public void sessionDestroyed (final HttpSessionEvent event)
   {
      SecurityContextProvider.removeSecurityContext ((String) event
         .getSession ().getAttribute ("integrity"));
      super.sessionDestroyed (event);
   }
}