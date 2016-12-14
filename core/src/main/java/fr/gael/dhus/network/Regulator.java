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

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fr.gael.dhus.database.object.User;
import fr.gael.dhus.database.object.config.network.ChannelType;
import fr.gael.dhus.database.object.config.network.ClassifierCriteriaType;
import fr.gael.dhus.database.object.config.network.ClassifierType;
import fr.gael.dhus.database.object.config.network.NetworkConfiguration;
import fr.gael.dhus.database.object.config.network.PeriodicalPositiveInt;
import fr.gael.dhus.database.object.config.network.PeriodicalPositiveLong;
import fr.gael.dhus.database.object.config.network.UserQuotasType;
import fr.gael.dhus.spring.context.ApplicationContextProvider;
import fr.gael.dhus.system.config.ConfigurationManager;

/**
 * TODO Documentation
 */
class Regulator
{
   /**
    * A logger for this class.
    */
   private static final Logger LOGGER = LogManager.getLogger(Regulator.class);

   /**
    * The a single instance default network regulator.
    */
   private static Regulator defaultRegulator = null;

   private final ChannelQueue outboundChannel;

   private final ChannelQueue inboundChannel;

   Regulator()
   {
      this.outboundChannel = new ChannelQueue("OutboundChannel");
      this.inboundChannel = new ChannelQueue("InboundChannel");
   }

   public Channel getChannel(ConnectionParameters parameters)
         throws IllegalArgumentException, RegulationException
   {
      // Check parameter
      if (parameters == null)
      {
         throw new IllegalArgumentException(
               "Cannot get a channel without connection parameters.");
      }

      // Return channel according to the traffic direction
      if (parameters.getDirection() == TrafficDirection.OUTBOUND)
      {
         return this.outboundChannel.getChannel(parameters);
      }
      else
      {
         return this.inboundChannel.getChannel(parameters);
      }
   }
   
   public void releaseChannel(final Channel channel)
   {
      // For the moment just detach the channel from its parent
      // TODO This method may be moved to the flow itself
      if (channel.getParent() != null)
      {
         ((ChannelQueue) channel.getParent()).removeChannel(channel);
      }
   }

   /**
    * TODO Documentation
    *
    * @return
    * @throws IllegalStateException
    */
   public static synchronized Regulator getDefaultRegulator()
         throws IllegalStateException
   {
      // Creates a new default regulator if not already done
      if (defaultRegulator == null)
      {
         // Create a new regulator
         Regulator regulator = new Regulator();

         try
         {
            regulator.configure(((ConfigurationManager)
               ApplicationContextProvider.getBean (ConfigurationManager.class))
                  .getNetworkConfiguration ());
         }
         catch (Exception exception)
         {
            throw new IllegalStateException(
               "Cannot configure default regulator.", exception);
         }

         // Assign new regulator as the default one
         defaultRegulator = regulator;
      }

      LOGGER.debug(defaultRegulator);

      // Return the default regulator
      return defaultRegulator;

   } // End getDefaultRegulator()

   /**
    * TODO Documentation
    *
    * @param config_stream
    * @throws IllegalArgumentException
    */
   private void configure(NetworkConfiguration network)
         throws IllegalArgumentException, IllegalStateException
   {
      // Check parameter
      if (network == null)
      {
         throw new IllegalArgumentException(
               "Cannot configure regulator with empty network configuration.");
      }

      // Configure outbound channels
      if (network.getOutbound() != null)
      {
         for (ChannelType channel_type : network.getOutbound().getChannel())
         {
            this.outboundChannel.addChannel(configureChannel(channel_type));
         }
      }

      // Configure inbound channels
      if (network.getInbound() != null)
      {
         for (ChannelType channel_type : network.getInbound().getChannel())
         {
            this.inboundChannel.addChannel(configureChannel(channel_type));
         }
      }

   } // End configure(InputStream)

   /**
    * TODO Documentation
    *
    * @param channel_type
    * @return
    * @throws IllegalArgumentException
    */
   private static Channel configureChannel(ChannelType channel_type)
         throws IllegalArgumentException
   {
      // Check parameter
      if (channel_type == null)
      {
         throw new IllegalArgumentException(
               "Cannot configure a network channel from a null type.");
      }

      // Create a new channel queue
      ChannelQueue channel = new ChannelQueue(channel_type.getName());

      // Set channel weight with respect to its siblings
      channel.setWeight(channel_type.getWeight());

      // Configure channel classifier
      ClassifierType classifier_type = channel_type.getClassifier();

      if (classifier_type != null)
      {
         ChannelClassifier classifier = new ChannelClassifier();

         // Parse include rules
         if (classifier_type.getIncludes() != null)
         {
            for (ClassifierCriteriaType include_type : classifier_type
                  .getIncludes().getInclude())
            {
               ChannelClassifierRules rules = new ChannelClassifierRules();

               rules.setEmailPattern(include_type.getUserEmailPattern());
               rules.setServiceName(include_type.getService());

               classifier.addIncludeRules(rules);
            }
         }

         // Parse exclude rules
         if (classifier_type.getExcludes() != null)
         {
            for (ClassifierCriteriaType exclude_type : classifier_type
                  .getExcludes().getExclude())
            {
               ChannelClassifierRules rules = new ChannelClassifierRules();

               rules.setEmailPattern(exclude_type.getUserEmailPattern());
               rules.setServiceName(exclude_type.getService());

               classifier.addExcludeRules(rules);
            }
         }

         // Assign classifier
         channel.setClassifier(classifier);
      }

      // Configure user default quotas
      UserQuotasType user_quota_type = channel_type.getDefaultUserQuotas();

      if (user_quota_type != null)
      {
         // Initialize a UserQuota builder
         UserQuotas.Builder quota_builder = new UserQuotas.Builder();

         // Set maxConcurrent if provided
         if (user_quota_type.getMaxConcurrent() != null)
         {
            quota_builder.maxConcurrent(user_quota_type.getMaxConcurrent()
                  .intValue());
         }

         // Set maxCount if provided
         if (user_quota_type.getMaxCount() != null)
         {
            PeriodicalPositiveInt max_count = user_quota_type.getMaxCount();
            quota_builder.maxCount(max_count.getValue(), max_count.getPeriod(),
                  TimeUnit.valueOf(max_count.getPeriodUnit().value()));
         }

         // Set maxSize if provided
         if (user_quota_type.getMaxSize() != null)
         {
            quota_builder.maxSize(user_quota_type.getMaxSize().longValue());
         }

         // Set maxCumulativeSize if provided
         if (user_quota_type.getMaxCumulativeSize() != null)
         {
            PeriodicalPositiveLong max_cumul_size =
               user_quota_type.getMaxCumulativeSize();
            quota_builder.maxCumulativeSize(max_cumul_size.getValue(),
                  max_cumul_size.getPeriod(),
                  TimeUnit.valueOf(max_cumul_size.getPeriodUnit().value()));
         }

         // Set maxBandwidth if provided
         if (user_quota_type.getMaxBandwidth() != null)
         {
            quota_builder.maxBandwidth(user_quota_type.getMaxBandwidth()
                  .intValue());
         }

         // Assign user quotas to the channel
         channel.setDefaultUserQuotas(quota_builder.build());
      }

      // Configure sub-channels
      for (ChannelType sub_channel_type : channel_type.getChannel())
      {
         channel.addChannel(configureChannel(sub_channel_type));
      }

      // Return channel
      return channel;

   } // End configureChannel(ChannelType)

   /**
    * @param user
    * @param direction
    * @return
    */
   public int countUserChannels(final User user,
         final TrafficDirection direction)
   {
      if (direction == null)
      {
         throw new IllegalArgumentException("Invalid null direction.");
      }

      if (direction == TrafficDirection.OUTBOUND)
      {
         return this.outboundChannel.countUserChannels(user);
      }

      return this.inboundChannel.countUserChannels(user);
   }

   @Override
   public String toString()
   {
      return "Newtork Regulator Configuration:\n"
            + this.outboundChannel.toString() + "\n"
            + this.inboundChannel.toString();
   }

} // End Regulator class
