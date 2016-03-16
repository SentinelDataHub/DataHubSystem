package fr.gael.dhus.api.stub.stub_share;

import java.util.ArrayList;
import java.util.List;



public class CollectionData
{
   private Long id;
   private String name;
   private String description;
   private boolean hasChildren;
   private List<Long> productIds;
   private List<Long> addedIds;
   private List<Long> removedIds;
   private List<CollectionData> displayedChildren;
   private int deep;
   
   private CollectionData parent;

   public CollectionData ()
   {
   }

   public CollectionData (Long id, String name, String description,
      CollectionData parent, boolean hasChildren)
   {
      this.id = id;
      this.name = name;
      this.description = description;
      this.parent = parent;
      this.hasChildren = hasChildren;
   }

   public void setId (Long id)
   {
      this.id = id;
   }

   public Long getId ()
   {
      return id;
   }

   public String getName ()
   {
      return name;
   }

   public void setName (String name)
   {
      this.name = name;
   }

   public String getDescription ()
   {
      return description;
   }

   public void setDescription (String description)
   {
      this.description = description;
   }

   public CollectionData getParent ()
   {
      return parent;
   }

   public void setParent (CollectionData parent)
   {
      this.parent = parent;
   }

   public List<Long> getProductIds ()
   {
      return productIds;
   }

   public void setProductIds (List<Long> productIds)
   {
      this.productIds = productIds;
   }
   
   public void addProduct (Long pid)
   {
      checkLists();
      if (removedIds.contains (pid))
      {
         removedIds.remove (pid);
         return;
      }
      if (addedIds.contains (pid) || productIds.contains (pid))
      {
         return;
      }
      addedIds.add (pid);      
   }
   
   public void removeProduct (Long pid)
   {
      checkLists();
      if (addedIds.contains (pid))
      {
         addedIds.remove (pid);
         return;
      }
      if (removedIds.contains (pid) || !productIds.contains (pid))
      {
         return;
      }      
      removedIds.add (pid);
   }
   
   public void addProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         addProduct(id);
      }
   }
   
   public void removeProducts (Long[] pid)
   {
      for (Long id : pid)
      {
         removeProduct(id);
      }
   }
   
   private void checkLists()
   {
      if (productIds == null)
      {
         this.productIds = new ArrayList<Long> ();
      }
      if (addedIds == null)
      {
         this.addedIds = new ArrayList<Long> ();
      }
      if (removedIds == null)
      {
         this.removedIds = new ArrayList<Long> ();
      }
   }
   
   public List<Long> getAddedIds ()
   {
      return addedIds;
   }

   public void setAddedIds (List<Long> addedIds)
   {
      this.addedIds = addedIds;
   }

   public List<Long> getRemovedIds ()
   {
      return removedIds;
   }

   public void setRemovedIds (List<Long> removedIds)
   {
      this.removedIds = removedIds;
   }
   
   public boolean contains(Long pid)
   {
      boolean added = addedIds != null && addedIds.contains (pid);
      boolean removed = removedIds != null && removedIds.contains (pid);
      boolean base = productIds != null && productIds.contains (pid);
      
      return added || (base && !removed);
   }
   
   public List<CollectionData> getDisplayedChildren()
   {
      return displayedChildren;
   }
   
   public void setDisplayedChildren(List<CollectionData> children)
   {
      displayedChildren = new ArrayList<CollectionData>();
      displayedChildren.addAll(children);
   }
      
   public int getDeep ()
   {
      return deep;
   }

   public void setDeep (int deep)
   {
      this.deep = deep;
   }

   @Override
   public String toString ()
   {
      return name;
   }

   @Override
   public boolean equals (Object o)
   {
      return o instanceof CollectionData && ((CollectionData) o).id == this.id;
   }

   public CollectionData copy ()
   {
      CollectionData copy = new CollectionData(id, name, description, parent, hasChildren);
      copy.setProductIds (productIds);
      copy.setAddedIds (addedIds);
      copy.setRemovedIds (removedIds);
      return copy;
   }

   public boolean hasChildren ()
   {
      return hasChildren;
   }
}