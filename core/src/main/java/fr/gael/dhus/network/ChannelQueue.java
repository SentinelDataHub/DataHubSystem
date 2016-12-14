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
package fr.gael.dhus.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import fr.gael.dhus.service.NetworkUsageService;
import fr.gael.dhus.database.object.User;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

class ChannelQueue extends AbstractChannel
                   implements Iterable<Channel>
{
   /**
    * The collection children channels. The collection may be empty but
    * never null.
    */
   private final Collection<Channel> subChannels;

   /**
    * Channel classifier
    */
   private ChannelClassifier classifier = null;

   /**
    * Build a default channel queue.
    */
   ChannelQueue(final String name) throws IllegalArgumentException
   {
      super(name);
      this.subChannels = Collections.synchronizedList (new LinkedList<Channel> ());

      new Thread(new RoundRobin(), "" + name + " - Round-Robin Thread").start();
   }

   @Override
   public Iterator<Channel> iterator()
   {
      return this.subChannels.iterator();
   }

   /**
    * Add a channel to the queue.
    *
    * @param channel is the channel to be added. This parameter shall not
    *    be null.
    * @throws IllegalArgumentException if the channel parameter is null.
    */
   public void addChannel(Channel channel) throws IllegalArgumentException
   {
      // Check parameter
      if (channel == null)
      {
         throw new IllegalArgumentException("Cannot add null channel.");
      }

      // Add channel to the sub-channels
      this.subChannels.add(channel);

      // Register this channel as parent of the added one
      channel.setParent(this);

   } // End addChannel(Channel)

   public boolean removeChannel(Channel channel)
   {
      return this.subChannels.remove(channel);
   }

   ChannelClassifier getClassifier()
   {
      return classifier;
   }

   void setClassifier(ChannelClassifier classifier)
   {
      this.classifier = classifier;
      // TODO Shall reject flows that no longer comply with the classifier
   }

   @Override
   public synchronized Channel getChannel(ConnectionParameters parameters) throws
      IllegalArgumentException, RegulationException
   {
      // Check parameter
      if (parameters == null)
      {
         throw new IllegalArgumentException(
               "Cannot build a channel flow from a null " +
               "set of parameters.");
      }

      // Reject request if the parameters do not comply with the classifier
      if ((this.classifier != null) &&
          (!this.classifier.complyWith(parameters)))
      {
         return null;
      }

      // Performs quotas control
      checkQuotas (parameters, this.getUserQuotas()); 

      // Try to get the flow from sub-channels
      for (Channel sub_channel : this)
      {
         Channel channel = sub_channel.getChannel(parameters);
         
         if (channel != null)
         {
            return channel;
         }
      }
      
      // Compute flow name
      String flow_name = "--anonymous--";

      if ((parameters.getUser() != null) &&
            (parameters.getUser().getUsername() != null))
      {
         flow_name = parameters.getUser().getUsername();
      }
      
      flow_name += "@" + this.getName() + "{" + parameters.getDirection()
            .getLabel() + "}";
      
      // Otherwise creates the channel
      Channel channel = new ChannelFlow(flow_name, parameters);
      
      this.addChannel(channel);
      
      return channel;
   }

   private void checkQuotas (ConnectionParameters parameters, UserQuotas quotas)
      throws RegulationException
   {
      // Raise an exception if connection count exceeded
      // Connection count is computed from this channel and includes all
      // sub-channels
      if ((quotas != null) &&
          (quotas.getMaxConcurrent() != null))
      {
         int max_concurrent = quotas.getMaxConcurrent();
         int connection_count = this.countUserChannels(parameters.getUser());

         if (connection_count >= max_concurrent)
         {
            // Get user name
            String user_name = "--anonymous--";
            if (parameters.getUser() != null)
            {
               user_name = parameters.getUser().getUsername();
            }

            // Throw regulation exception
            throw new RegulationException("Maximum number of " +
               max_concurrent + " concurrent flows achieved by the user \"" +
               user_name + "\"");
         }
      }
      NetworkUsageService network_service = ApplicationContextProvider.
               getBean (NetworkUsageService.class);
      // Raise an exception if maxCount reached
      if ((quotas !=null) &&
          (quotas.getMaxCount () != null) &&
          (parameters.getUser()  != null))
      {
         User user = parameters.getUser();
         long period = quotas.getMaxCountPeriod ();

         int total_counted = network_service.countDownloadsByUserSince (user,
            period);
         // Checks the retrieved count
         if (total_counted>=quotas.getMaxCount ())
         {
            // Throw regulation exception
            throw new RegulationException("Maximum number of download (" +
               total_counted + ") exceeded by the user \"" +
               user.getUsername () + "\""); 
         }
      }

      // Raise an exception if maxCumulativeSize reached
      if ((quotas !=null) &&
          (quotas.getMaxCumulativeSize () != null) &&
          (parameters.getUser()  != null))
      {
         User user = parameters.getUser();
         long period = quotas.getMaxCountPeriod ();
         long expected_size = parameters.getStreamSize ();
         
         long total_sized = network_service.getDownloadedSizeByUserSince (user,
            period) + expected_size;
         // Checks the retrieved count
         if (total_sized>=quotas.getMaxCumulativeSize ())
         {
            // Throw regulation exception
            throw new RegulationException("Maximum size of download (" +
                     total_sized + ") exceeded by the user \"" +
               user.getUsername () + "\"");
         }
      }
   }

   @Override
   public String toString()
   {
      return toString(0);
   }

   public String toString(final int indent_level)
   {
      String indent = "   ";
      String left_margin = "";
      for (int iindent = 0; iindent < indent_level; iindent++)
      {
         left_margin += indent;
      }
      String message =
         left_margin + "Channel Queue (" +
         ((this.getName() != null) ? this.getName() : "--anonymous--") + " x "
         + this.getWeight() + ") - Classifier: " +
         ((this.classifier != null) ? this.classifier : "None");

      UserQuotas user_quotas = this.getUserQuotas();

      message += "\n" + left_margin + indent + (user_quotas != null ?
            user_quotas.toString() : "User Quotas: none");

      for (Channel sub_channel : this)
      {
         if (sub_channel instanceof ChannelQueue)
         {
            message += "\n" +
               ((ChannelQueue) sub_channel).toString(indent_level + 1);
         }
         else
         {
            message += "\n" + left_margin + indent + sub_channel.toString();
         }
      }

      return message;
   }

   @Override
   public int countUserChannels(User user)
   {
      // Prepare counter
      int counter = 0;

      // Loop among sub-channels
      for (Channel channel : this)
      {
         counter += channel.countUserChannels(user);
      }

      // Return counter
      return counter;
   }

   private class RoundRobin implements Runnable
   {

      @Override
      public void run()
      {
         List<Channel> non_empty_queues = new ArrayList<Channel>();

         // Infinite Round-Robin loop
         while (true)
         {
             int weights_sum = 0;
             int quantum = 0;
             int permits = 0;

            // Infinite loop broken when a sub-channel has something to send
            while (true)
            {
               non_empty_queues.clear();
               weights_sum = 0;
               quantum = 0;
               permits = 0;

               for (Object channel_object : subChannels.toArray())
               {
                  Channel channel = (Channel)channel_object;

                  if (channel == null)
                     continue;

                  int awaiting_permits = channel.getAwaitingPermits();

                  if (awaiting_permits > 0)
                  {
                     non_empty_queues.add(channel);
                     weights_sum += channel.getWeight();
                     quantum = Math.max(awaiting_permits, quantum);
                     permits += awaiting_permits;
                  }
               }

                if (non_empty_queues.size() > 0)
                {
                    break;
                }
                

                try
               {
                  waitPoke();
               }
               catch (InterruptedException e)
               {
                  e.printStackTrace();
               }
            }

            try
            {
               acquire(permits);
            }
            catch (IllegalArgumentException e)
            {
               e.printStackTrace();
            }
            catch (RegulationException e)
            {
               e.printStackTrace();
            }
            catch (InterruptedException e)
            {
               e.printStackTrace();
            }

            for (Channel channel : non_empty_queues)
            {
               channel.release(permits * channel.getWeight() / weights_sum);
            }
         }

      }

   }
} // End ChannelQueue class
