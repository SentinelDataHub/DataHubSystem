/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
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




import java.io.Serializable;

public class ConfigurationData implements Serializable
{
    private String mailServerSmtp;
    private int mailServerPort;
    private boolean mailServerTls;
    private String mailServerUser;
    private String mailServerPassword;

    private String mailServerFromName;
    private String mailServerFromMail;
    private String mailServerReplyTo;

    private boolean mailWhenCreate;
    private boolean mailWhenDelete;
    private boolean mailWhenUpdate;

    private String registrationMail;

    private String supportMail;
    private String supportName;

    public String getMailServerSmtp ()
    {
        return mailServerSmtp;
    }
    public void setMailServerSmtp (String mailServerSmtp)
    {
        this.mailServerSmtp = mailServerSmtp;
    }
    public int getMailServerPort ()
    {
        return mailServerPort;
    }
    public void setMailServerPort (int mailServerPort)
    {
        this.mailServerPort = mailServerPort;
    }
    public boolean isMailServerTls ()
    {
        return mailServerTls;
    }
    public void setMailServerTls (boolean mailServerTls)
    {
        this.mailServerTls = mailServerTls;
    }
    public String getMailServerUser ()
    {
        return mailServerUser;
    }
    public void setMailServerUser (String mailServerUser)
    {
        this.mailServerUser = mailServerUser;
    }
    public String getMailServerPassword ()
    {
        return mailServerPassword;
    }
    public void setMailServerPassword (String mailServerPassword)
    {
        this.mailServerPassword = mailServerPassword;
    }
    public String getMailServerFromName ()
    {
        return mailServerFromName;
    }
    public void setMailServerFromName (String mailServerFromName)
    {
        this.mailServerFromName = mailServerFromName;
    }
    public String getMailServerFromMail ()
    {
        return mailServerFromMail;
    }
    public void setMailServerFromMail (String mailServerFromMail)
    {
        this.mailServerFromMail = mailServerFromMail;
    }
    public String getMailServerReplyTo ()
    {
        return mailServerReplyTo;
    }
    public void setMailServerReplyTo (String mailServerReplyTo)
    {
        this.mailServerReplyTo = mailServerReplyTo;
    }
    public boolean isMailWhenCreate ()
    {
        return mailWhenCreate;
    }
    public void setMailWhenCreate (boolean mailWhenCreate)
    {
        this.mailWhenCreate = mailWhenCreate;
    }
    public boolean isMailWhenDelete ()
    {
        return mailWhenDelete;
    }
    public void setMailWhenDelete (boolean mailWhenDelete)
    {
        this.mailWhenDelete = mailWhenDelete;
    }
    public boolean isMailWhenUpdate ()
    {
        return mailWhenUpdate;
    }
    public void setMailWhenUpdate (boolean mailWhenUpdate)
    {
        this.mailWhenUpdate = mailWhenUpdate;
    }
    public String getSupportMail ()
    {
        return supportMail;
    }
    public void setSupportMail (String supportMail)
    {
        this.supportMail = supportMail;
    }
    public String getSupportName ()
    {
        return supportName;
    }
    public void setSupportName (String supportName)
    {
        this.supportName = supportName;
    }
    public String getRegistrationMail ()
    {
        return registrationMail;
    }
    public void setRegistrationMail (String registrationMail)
    {
        this.registrationMail = registrationMail;
    }
}
