#
#	registryFile.jai
#
# The S3-dedicated JAI registry initialization file
#
############################################################################
#
# Each line can be in one of the formats described below. Space or tab
# characters separate keywords in each line. The comment character is
# '#'; on each line all characters following the first comment character
# are ignored. The file must be encoded in UTF-8.
#
# 1. To register descriptors :
#
#	descriptor <descriptor-class-name>
#	odesc	   <descriptor-class-name>  <descriptor-name>
#
#    The second version above is deprecated and is retained for backward
#    compatibility with JAI 1.0.2. Descriptors are always registered
#    against <descriptor-class>.getName(). The <descriptor-name> in the
#    second version is always ignored.
#
# 2. To register factory objects under a product against a specific mode :
#
#	<registry-mode-name>	<factory-class-name>	<product-name>	<descriptor-name>   <local-name>
#	<registry-mode-name>	<factory-class-name>	<descriptor-name>
#
#    The first version above is used to register factory objects against
#    modes that support preferences. The second version is used for those
#    that do not support preferences. <local-name> is an arbitrary name that
#    is unique for a given mode. This is (only) used later on in this file
#    to set preferences between factory objects.
#
# 3. To set preferences between products for a descriptor under a
#    specific mode :
#
#	prefProduct <modeName>	<descriptor-name>   <preferred-product-name> <other-product-name>
#	pref	    product	<descriptor-name>   <preferred-product-name> <other-product-name>
#
#    The second version above is deprecated and is retained for backward
#    compatibility with JAI 1.0.2. This version is assumed to set
#    product preferences for the "rendered" mode.
#
# 4. To set preferences between factory objects for descriptor under a
#    a specific product and registry mode :
#
#	pref	<modeName>  <descriptor-name>	<product-name>	<preferred-factory-local-name>	<other-factory-local-name>
#
############################################################################
#
# Image operation descriptors :
#
descriptor	fr.gael.drb.cortex.topic.sentinel3.jai.operator.QuicklookOlciDescriptor
descriptor  fr.gael.drb.cortex.topic.sentinel3.jai.operator.QuicklookSlstrDescriptor
descriptor  fr.gael.drb.cortex.topic.sentinel3.jai.operator.S3HistogramEqualizerDescriptor


#
# "rendered" factory objects
#
rendered  fr.gael.drb.cortex.topic.sentinel3.jai.operator.QuicklookOlciRIF	fr.gael.drb.cortex.topic.sentinel3.jai.operator QuicklookOlci	QuicklookOlci
rendered  fr.gael.drb.cortex.topic.sentinel3.jai.operator.QuicklookSlstrRIF fr.gael.drb.cortex.topic.sentinel3.jai.operator QuicklookSlstr  QuicklookSlstr
rendered  fr.gael.drb.cortex.topic.sentinel3.jai.operator.S3HistogramEqualizerRIF  fr.gael.drb.cortex.topic.sentinel3.jai.operator S3HistogramEqualizer  S3HistogramEqualizer



#
# "rendered" factory object preferences
#
#pref	rendered	absolute	com.sun.media.jai	mlibabsoluterif	sunabsoluterif
