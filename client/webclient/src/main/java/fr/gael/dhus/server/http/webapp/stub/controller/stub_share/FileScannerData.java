package fr.gael.dhus.server.http.webapp.stub.controller.stub_share;

import java.util.List;

public class FileScannerData
{

   private Long id;
   private String url;
   private String username;
   private String password;
   private String status;
   private String statusMessage;
   private String pattern;
   private List<String> collections;
   private Boolean active;


   public FileScannerData(){
      this.id = null;
      this.url = null;
      this.username = null;
      this.password = null;
      this.status = null;
      this.statusMessage = null;
      this.pattern = null;
      this.collections = null;
      this.active = false;
   }
   
   public FileScannerData(Long id, String url, String username, String password, String pattern, List<String> collections, 
      String status, String statusMessage, boolean active)
   {
      this.url = url;
      this.id = id;
      this.username = username;
      this.password = password;
      this.pattern = pattern;
      this.collections = collections;
      this.status = status;
      this.statusMessage = statusMessage;
      this.active = active;

   }

   public String getUrl ()
   {
      return url;
   }

   public void setUrl (String url)
   {
      this.url = url;
   }

   public Long getId ()
   {
      return id;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   public String getUsername ()
   {
      return username;
   }

   public void setUsername (String username)
   {
      this.username = username;
   }

   public List<String> getCollections ()
   {
      return collections;
   }

   public void setCollections (List<String> collections)
   {
      this.collections = collections;
   }

   public String getStatus ()
   {
      return status;
   }

   public void setStatus (String status)
   {
      this.status = status;
   }

   public String getPassword ()
   {
      return password;
   }

   public void setPassword (String password)
   {
      this.password = password;
   }

   public String getStatusMessage ()
   {
      return statusMessage;
   }

   public void setStatusMessage (String statusMessage)
   {
      this.statusMessage = statusMessage;
   }

   public Boolean getActive() {
      return active;
   }

   public void setActive(Boolean active) {
      this.active = active;
   }

   public String getPattern()
   {
      return this.pattern;
   }
   
   public void setPattern(String pattern)
   {
      this.pattern = pattern;
   }
}
