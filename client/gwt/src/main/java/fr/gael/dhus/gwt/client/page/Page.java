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
package fr.gael.dhus.gwt.client.page;

public enum Page
{  
   OVERVIEW (new OverviewPage()),
   FORGOT (new ForgotPage()),
   MANAGEMENT (new ManagementPage()),
   PROFILE (new ProfilePage()),
   CART (new CartPage()),
   REGISTER (new RegisterPage()),
   SEARCH (new SearchPage()),
   SEARCHVIEW (new SearchViewPage()),
   STATISTICS (new StatisticsPage()),
   UPLOAD (new UploadPage()),
   ABOUT (new AboutPage()),
   TERMS (new TermsPage()),
   RESETPASSWORD (new ResetPasswordPage());   
   
   private Page (AbstractPage page)
   {
      this.page = page;
   }
   
   private AbstractPage page;
   
   public AbstractPage getPage ()
   {
      return page;
   }
   
   public void load ()
   {
      page.load ();
   }
   
   public void unload ()
   {
      page.unload ();
   }
}
