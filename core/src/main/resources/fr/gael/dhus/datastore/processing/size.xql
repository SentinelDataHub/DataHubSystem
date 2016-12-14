(:
Testing support:
Command lines on uncompressed archives:
---------------------------------------
$> du -bs S2A_OPER_PRD_MSIL1C_PDMC_20150818T101204_R022_V20150813T102406_20150813T102406.SAFE/
429276933	S2A_OPER_PRD_MSIL1C_PDMC_20150818T101204_R022_V20150813T102406_20150813T102406.SAFE/

$> find S2A_OPER_PRD_MSIL1C_PDMC_20150818T101204_R022_V20150813T102406_20150813T102406.SAFE -type f  -exec ls -l {} \; | \
   cut -d" " -f 5 | \
   sed  -e ':a' -e 'N' -e '$!ba' -e 's/\n/ /g' -e 's/ /+/g' | bc
429219589

Drb Query inside zip archive:
----------------------------
drb.sh -f size.xql -variable product_path /data_1/data/sentinel-2/S2A_OPER_PRD_MSIL1C_PDMC_20150818T101204_R022_V20150813T102406_20150813T102406.zip
429219588

drb.sh -f size.xql -variable product_path /data_1/data/sentinel-2/S2A_OPER_PRD_MSIL1C_PDMC_20150818T101204_R022_V20150813T102406_20150813T102406.SAFE
429219589

Missing 1 byte in Drb process inside zip archive (?)...
:)
declare variable $product_path external;

declare function local:computeSizes ($path, $base as item()*) as item()*
{
   for $child in $path/*
   return
      if ($child/@directory)
      then
         local:computeSizes ($child, base)
      else
         if($child/@size)
         then
            ( data($child/@size), $base )
         else
            $base
};

let $product:=fn:doc($product_path)
return
   xs:unsignedLong(fn:sum(local:computeSizes($product, ())))