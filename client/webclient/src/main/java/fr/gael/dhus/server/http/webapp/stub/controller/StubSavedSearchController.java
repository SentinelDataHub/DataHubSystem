package fr.gael.dhus.server.http.webapp.stub.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.SchedulerException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.Search;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.service.exception.UserNotExistingException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

@RestController
public class StubSavedSearchController
{

   private static Log logger = LogFactory
         .getLog(StubSavedSearchController.class);

   @RequestMapping(value = "/users/{userid}/searches", method = RequestMethod.POST)
   public void storeUserSearch(final Principal principal,
         @PathVariable(value = "userid")
         final String userid,
         @RequestParam(value = "complete", defaultValue = "")
         final String complete)
   {
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      final User u =
         (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (u == null)
      {
         throw new UserNotExistingException();
      }

      logger.debug("complete search " + complete);
      userService.storeUserSearch(u.getUUID(), complete, "",
            new HashMap<String, String>(), complete);

   }

   @RequestMapping(value = "/users/{userid}/searches", method = RequestMethod.GET)
   public List<Search> getUserSearches(final Principal principal,
         @PathVariable(value = "userid")
         final String userid, @RequestParam(value = "offset", defaultValue = "")
         final int offset, @RequestParam(value = "count", defaultValue = "")
         final int count)
   {
      List<Search> searches = null;
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      final User u =
         (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (u == null)
      {
         throw new UserNotExistingException();
      }
      logger.debug(" ***** parameters ***** ");
      logger.debug(" ***** offset ***** " + offset);
      logger.debug(" ***** count ***** " + count);
      searches = userService.scrollSearchesOfUser(u.getUUID(), offset, count);
      try
      {
         userService.getNextScheduleSearch();
      }
      catch (final SchedulerException e)
      {
         logger.error("Error scheduling next search" + e.getMessage());
         e.printStackTrace();
      }
      return searches;

   }

   @RequestMapping(value = "/users/{userid}/searches/count", method = RequestMethod.GET)
   public int getUserSearchesCount(final Principal principal,
         @PathVariable(value = "userid")
         final String userid)
   {
      int count = 0;
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      final User u =
         (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (u == null)
      {
         throw new UserNotExistingException();
      }
      count = userService.countUserSearches(u.getUUID());

      return count;

   }

   @RequestMapping(value = "/users/{userid}/searches", method = RequestMethod.DELETE)
   public void clearSavedSearches(final Principal principal,
         @PathVariable(value = "userid")
         final String userid)
   {
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      final User u =
         (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (u == null)
      {
         throw new UserNotExistingException();
      }
      userService.clearSavedSearches(u.getUUID());
   }

   @RequestMapping(value = "/users/{userid}/searches/{searchid}", method = RequestMethod.DELETE)
   public void removeUserSearch(final Principal principal,
         @PathVariable(value = "userid")
         final String userid, @PathVariable(value = "searchid")
         final String searchid, @RequestParam(value = "id", defaultValue = "")
         final String id)
   {
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      final User u =
         (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
      if (u == null)
      {
         throw new UserNotExistingException();
      }
      userService.removeUserSearch(u.getUUID(), id);
   }

   @RequestMapping(value = "/users/{userid}/searches/{searchid}", method = RequestMethod.POST)
   public void updateUserSearchNotification(@PathVariable(value = "userid")
   final String userid, @PathVariable(value = "searchid")
   final String searchid, @RequestParam(value = "id", defaultValue = "")
   final String id, @RequestParam(value = "notify", defaultValue = "")
   final boolean notify)
   {
      final fr.gael.dhus.service.UserService userService =
         ApplicationContextProvider
               .getBean(fr.gael.dhus.service.UserService.class);

      userService.activateUserSearchNotification(id, notify);
   }

}
