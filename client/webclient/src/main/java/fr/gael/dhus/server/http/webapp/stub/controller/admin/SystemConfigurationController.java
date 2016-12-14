/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 Serco (http://serco.com/) and Gael System (http://www.gael.fr) consortium
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.gael.dhus.server.http.webapp.stub.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fr.gael.dhus.database.object.config.Configuration;
import fr.gael.dhus.database.object.config.messaging.MailConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailFromConfiguration;
import fr.gael.dhus.database.object.config.messaging.MailServerConfiguration;
import fr.gael.dhus.database.object.config.messaging.MessagingConfiguration;
import fr.gael.dhus.service.exception.EmailNotSentException;
import fr.gael.dhus.spring.context.ApplicationContextProvider;


@RestController
public class SystemConfigurationController {

    private static Log logger = LogFactory.getLog(SystemConfigurationController.class);

    /**
     * List
     *
     * @return
     */
    @RequestMapping(value = "/admin/system/configurations", method= RequestMethod.GET)
    public ResponseEntity<?> configurations(){
        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);
        try
        {
            Configuration cfg = systemService.getCurrentConfiguration ();
            return new ResponseEntity<>(convertConfiguration (cfg), HttpStatus.OK);
        }
        catch (AccessDeniedException e){
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * UPDATE
     *
     * @param
     * @return
     */
    @RequestMapping (value = "/admin/system/configurations", method= RequestMethod.PUT)
    public ResponseEntity<?>  updateConfig (@RequestBody ConfigurationData configurationData){
        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);

        try
        {
            Configuration cfg =
                    systemService.saveSystemSettings(convertConfigurationData(configurationData));

            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (EmailNotSentException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"email_not_sent\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (AccessDeniedException e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * CREATE
     *
     * @param
     * @return
     */
    @RequestMapping (value = "/admin/system/defaultconfigurations", method= RequestMethod.POST)
    public ResponseEntity<?>  defaultConfigurations (@RequestParam(value="reset", defaultValue="") String reset){
        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);
        try
        {
            Configuration cfg = systemService.resetToDefaultConfiguration();
            return new ResponseEntity<>(convertConfiguration (cfg), HttpStatus.OK);
        }
        catch (EmailNotSentException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"email_not_sent\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (AccessDeniedException e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @RequestMapping (value = "/admin/system/archive", method= RequestMethod.POST)
    public ResponseEntity<?>  vitaloca (@RequestParam(value="reset", defaultValue="") String reset) {
        fr.gael.dhus.service.ArchiveService archiveService = ApplicationContextProvider
                .getBean(fr.gael.dhus.service.ArchiveService.class);
        try {
            return new ResponseEntity<>("{\"code\":\""+archiveService.synchronizeLocalArchive()+"\"}", HttpStatus.OK);
        }
        catch (EmailNotSentException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"email_not_sent\"}", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (AccessDeniedException e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * UPDATE
     *
     * @param
     * @return
     */
    @RequestMapping (value = "/admin/system/rootpassword", method= RequestMethod.PUT)
    public ResponseEntity<?>  updateRootPassword (@RequestBody RootPasswordModel body){
        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);

        try
        {
            systemService.changeRootPassword (body.getNewPassword(), body.getOldPassword());
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (EmailNotSentException e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\"email_not_sent\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
        catch (AccessDeniedException e) {
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * List
     *
     * @return
     */
    @RequestMapping(value = "/admin/system/dumpdatabases")
    public ResponseEntity<?> dumpDatabasesList () {

        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);

        try {
            List<Long> dates= new ArrayList<Long>();
            for(Date date : systemService.getDumpDatabaseList ()){
                dates.add(date.getTime());
            }

            return new ResponseEntity<>(dates, HttpStatus.OK);
        }
        catch (AccessDeniedException e){
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    /**
     * List
     *
     * @return
     */
    @RequestMapping(value = "/admin/system/database", method= RequestMethod.POST)
    public ResponseEntity<?> restoreDatabase (@RequestBody RestoreDatabaseRequestModel body) {

        fr.gael.dhus.service.SystemService systemService = ApplicationContextProvider
                .getBean (fr.gael.dhus.service.SystemService.class);

        try {
            systemService.restoreDumpDatabase (body.getDate());
            return new ResponseEntity<>("{\"code\":\"OK\"}", HttpStatus.OK);
        }
        catch (AccessDeniedException e){
            return new ResponseEntity<>("{\"code\":\"unauthorized\"}", HttpStatus.FORBIDDEN);
        }
        catch (Exception e)
        {
            e.printStackTrace ();
            return new ResponseEntity<>("{\"code\":\""+e.getMessage()+"\"}" , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private ConfigurationData convertConfiguration (Configuration cfg)
    {
        ConfigurationData configurationData = new ConfigurationData ();

        configurationData.setMailWhenCreate (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserCreate ());
        configurationData.setMailWhenUpdate (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserUpdate ());
        configurationData.setMailWhenDelete (cfg.getMessagingConfiguration ().getMailConfiguration ().isOnUserDelete ());

        configurationData.setMailServerSmtp (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getSmtp ());
        configurationData.setMailServerPassword (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getPassword ());
        configurationData.setMailServerTls (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().isTls ());
        configurationData.setMailServerPort (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getPort ());
        configurationData.setMailServerUser (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getUsername ());

        configurationData.setMailServerFromMail (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getAddress ());
        configurationData.setMailServerFromName (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getMailFromConfiguration ().getName ());
        configurationData.setMailServerReplyTo (cfg.getMessagingConfiguration ().getMailConfiguration ().getServerConfiguration ().getReplyTo ());

        configurationData.setRegistrationMail (cfg.getSystemConfiguration ().getSupportConfiguration ().getRegistrationMail ());

        configurationData.setSupportMail (cfg.getSystemConfiguration ().getSupportConfiguration ().getMail ());
        configurationData.setSupportName (cfg.getSystemConfiguration ().getSupportConfiguration ().getName ());

        return configurationData;
    }

    private Configuration convertConfigurationData (ConfigurationData configurationData)
    {
        Configuration cfg = new Configuration ();

        MessagingConfiguration msgCfg = new MessagingConfiguration ();
        MailConfiguration mailCfg = new MailConfiguration ();

        mailCfg.setOnUserCreate (configurationData.isMailWhenCreate ());
        mailCfg.setOnUserUpdate (configurationData.isMailWhenUpdate ());
        mailCfg.setOnUserDelete (configurationData.isMailWhenDelete ());

        MailServerConfiguration srvCfg = new MailServerConfiguration ();

        srvCfg.setSmtp (configurationData.getMailServerSmtp ());
        srvCfg.setPassword (configurationData.getMailServerPassword ());
        srvCfg.setTls (configurationData.isMailServerTls ());
        srvCfg.setPort (configurationData.getMailServerPort ());
        srvCfg.setUsername (configurationData.getMailServerUser ());

        MailFromConfiguration mSrvCfg = new MailFromConfiguration ();

        mSrvCfg.setAddress (configurationData.getMailServerFromMail ());
        mSrvCfg.setName (configurationData.getMailServerFromName ());

        srvCfg.setMailFromConfiguration (mSrvCfg);

        srvCfg.setReplyTo (configurationData.getMailServerReplyTo ());

        mailCfg.setServerConfiguration (srvCfg);

        msgCfg.setMailConfiguration (mailCfg);

        cfg.setMessagingConfiguration (msgCfg);

        fr.gael.dhus.database.object.config.system.SupportConfiguration supportCfg = new fr.gael.dhus.database.object.config.system.SupportConfiguration();

        supportCfg.setRegistrationMail (configurationData.getRegistrationMail ());
        supportCfg.setMail (configurationData.getSupportMail ());
        supportCfg.setName (configurationData.getSupportName ());

        fr.gael.dhus.database.object.config.system.SystemConfiguration sysCfg = new fr.gael.dhus.database.object.config.system.SystemConfiguration();
        sysCfg.setSupportConfiguration (supportCfg);

        cfg.setSystemConfiguration (sysCfg);

        return cfg;

}
}
