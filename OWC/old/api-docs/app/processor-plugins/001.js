onmessage = function(e) {
  console.warn("===== INSIDE WEB WORKER ====");
  console.log("e: ", e);
  var input = JSON.parse(e.data);
  // for(var i = 0; i < input.length; i++){
  //   console.log("input[i]: ", input[i]);
  // }
  var sum = 0;
  var length = input.length;
  for(var i = 0; i < input.length; i++){
    var sizeString =  input[i].attributes[2].Size;
    var value = parseInt(sizeString.split(" ")[0]);
    var unity = sizeString.split(" ")[1];
    var multiplicator = 1;
    console.log("-----> unity: ", unity);
    switch(unity){
      case "MB":
        multiplicator = 1024;
        break;
      case "GB":
        multiplicator = 1024*1024;
        break;
      case "B":
          multiplicator = 1;
          break;
    }
    sum += (value * multiplicator);
  }

   postMessage((length > 0)?sum/length:-1);

}
