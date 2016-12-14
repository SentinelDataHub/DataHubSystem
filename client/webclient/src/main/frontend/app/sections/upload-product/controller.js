/* 
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
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
'use strict';

/**
 * @ngdoc function
 * @name DHuS-webclient.controller:AboutCtrl
 * @description
 * # AboutCtrlf
 * Controller of the DHuS-webclient
 */
angular.module('DHuS-webclient')
.directive('fileModel', ['$parse', function ($parse) {
    return {
        restrict: 'A',
        link: function(scope, element, attrs) {
            var model = $parse(attrs.fileModel);
            var modelSetter = model.assign;
           
            element.bind('change', function(){
                scope.$apply(function(){
                    modelSetter(scope, element[0].files[0]);
                });
            });
        }
    };
}]);
angular.module('DHuS-webclient')
  .controller('UploadCtrl', function ($scope,AdminCollections,AdminUploadService,AdminCollectionManager ) {

     $scope.setIfSelectedFile = function () {
       var namefile = document.querySelector( '#upload-file-info' ).innerText;
       if(namefile != "No file choosen") {
          $scope.isSelectedFile = true;
       } else {
          $scope.isSelectedFile = false;
       }
     };


     $scope.getFileScanners = function() {




        AdminUploadService.getNextFileScannerDate()
          .then(function(response){
            $scope.nextFileScannerDate = response.data;
          });

              AdminUploadService.getFileScanners()
                  .then(function(response){            
                    $scope.names = response.data;
                    for (var i=0; i<$scope.names.length; i++) {
                     $scope.names[i].toggle = false;
                     $scope.names[i].toggleHover = false;
                     $scope.x_state = -1;
                     if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                     } else {
                        $scope.names[i].styleColor = "color: darkgray;";
                     }
                   }
                    for (var i=0; i<$scope.names.length; i++) {
                      if ($scope.names[i].status == 'ok') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                      } else if ($scope.names[i].status == 'error') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                      } else if ($scope.names[i].status == 'added') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      } else if ($scope.names[i].status == 'running') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                      } else {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      }
                       
                    }
                  });
             };

     $scope.getFileScanners();
     $scope.scanninginfo = "";
     $scope.urltoscan = "";
     $scope.username = "";
     $scope.password = "";
     $scope.pattern = "";
     $scope.collections = {};
     $scope.x_state = -1;
     $scope.toggle = false;
     $scope.isSelectedFile = false;

     $scope.addFileScanner = function() {
        var idCollections = AdminCollectionManager.getSelectedCollectionsIds();
        var model=
        {
          url: $scope.urltoscan,
          username: $scope.username,
          password: $scope.password,
          pattern: $scope.pattern,
          collections: idCollections,
          active: false
        };
      $scope.getScanningInfo(model);
      AdminUploadService.createFileScanner(model)
      .then(function(response) {
          var responseStatus = parseInt(response.status);
          if(responseStatus >= 200 && responseStatus < 300)
          {
             AdminUploadService.getFileScanners()
                  .then(function(response){            
                    $scope.names = response.data;
                    for (var i=0; i<$scope.names.length; i++) {
                     $scope.names[i].toggle = false;
                     $scope.names[i].toggleHover = false;
                     $scope.x_state = -1;
                     if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                     } else {
                        $scope.names[i].styleColor = "color: darkgray;";
                     }
                   }
                    for (var i=0; i<$scope.names.length; i++) {
                      if ($scope.names[i].status == 'ok') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                      } else if ($scope.names[i].status == 'error') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                      } else if ($scope.names[i].status == 'added') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      } else if ($scope.names[i].status == 'running') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                      } else {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      }
                    }
                    ToastManager.success("file scanner added");
                  });
          }
          else
          {
            ToastManager.error("error in add file scanner");
          }       
        },
        function(data) 
        {
          ToastManager.error("error in add file scanner");
        }
        );
     };

     $scope.saveFileScanner = function() {
       var idCollections = AdminCollectionManager.getSelectedCollectionsIds();

       var model=
        {
          id: $scope.names[$scope.x_state].id,
          url: $scope.urltoscan,
          username: $scope.username,
          password: $scope.password,
          pattern: $scope.pattern,
          collections: idCollections
        };
      AdminUploadService.updateFileScanner(model)
      .then(function(response) {
          var responseStatus = parseInt(response.status);
          if(responseStatus >= 200 && responseStatus < 300)
          {


            AdminUploadService.getFileScanners()
                  .then(function(response){            
                    $scope.names = response.data;
                    for (var i=0; i<$scope.names.length; i++) {
                     $scope.names[$scope.x_state].toggle = true;
                     $scope.names[i].toggleHover = false;
                     if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                     } else {
                        $scope.names[i].styleColor = "color: darkgray;";
                     }
                   }
                    for (var i=0; i<$scope.names.length; i++) {
                      if ($scope.names[i].status == 'ok') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                      } else if ($scope.names[i].status == 'error') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                      } else if ($scope.names[i].status == 'added') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      } else if ($scope.names[i].status == 'running') {
                        $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                      } else {
                        $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      }
                    }
                    ToastManager.success("file scanner updated");
                  });
          }
          else
          {
            ToastManager.error("error in update file scanner");
          }       
        },
        function(data) 
        {
          ToastManager.error("error in update file scanner");
        }
        );
     };

     $scope.refreshAllFilescanners = function() {
       $scope.scanninginfo = "";
       $scope.urltoscan = "";
       $scope.username = "";
       $scope.password = "";
       $scope.pattern = "";
       $scope.getFileScanners();
       $scope.toggle = false;
       $scope.collections = {};
     };

     $scope.getScanningInfo = function(x) {
      $scope.toggle = false;
      for (var i=0; i<$scope.names.length; i++) {
        if ($scope.names[i] == x) {
            if ($scope.x_state != i && $scope.x_state != -1) {
              $scope.names[$scope.x_state].toggle = false;
            }
            if ($scope.names[i].toggle) {
              $scope.scanninginfo = "";
              $scope.collections = {};
              $scope.urltoscan = "";
              $scope.username = "";
              $scope.password = "";
              $scope.pattern = "";
              $scope.names[i].toggle = false;
            } else {
              $scope.scanninginfo = x.statusMessage;
              $scope.collections = x.collections;
              $scope.urltoscan = x.url;
              $scope.username = x.username;
              $scope.password = "";
              $scope.pattern = x.pattern;
              $scope.names[i].toggle = true;
            }
            if ($scope.names[i].toggle) {
              $scope.x_state = i;
            } else {
              $scope.x_state = -1;
            }
        }
        $scope.toggle = $scope.toggle || $scope.names[i].toggle;
      }
     };

     $scope.setIdFileScannerHovered = function(x) {
        for (var i=0; i<$scope.names.length; i++) {
            if ($scope.names[i] == x) {
              if ($scope.x_state == -1) {
                $scope.names[i].toggleHover = true;
              }
            } else {
              if ($scope.x_state == -1) {
                $scope.names[i].toggleHover = false;
              }
            }
        }

     };

     $scope.resetIdFileScannerHovered = function() {
        for (var i=0; i<$scope.names.length; i++) {
          $scope.names[i].toggleHover = false;
        }
     };

     $scope.playFileScanner = function(x) {
        $scope.getScanningInfo(x);
        AdminUploadService.startFileScanner(x.id)
        .then(function(response) {
            var responseStatus = parseInt(response.status);
            if(responseStatus >= 200 && responseStatus < 300)
            {
               AdminUploadService.getFileScanners()
                    .then(function(response){            
                      $scope.names = response.data;
                      for (var i=0; i<$scope.names.length; i++) {
                       $scope.names[i].toggle = false;
                       $scope.names[i].toggleHover = false;
                       $scope.x_state = -1;
                       if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                       } else {
                          $scope.names[i].styleColor = "color: darkgray;";
                       }
                     }
                      for (var i=0; i<$scope.names.length; i++) {
                        if ($scope.names[i].status == 'ok') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                        } else if ($scope.names[i].status == 'error') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                        } else if ($scope.names[i].status == 'added') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        } else if ($scope.names[i].status == 'running') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                        } else {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        }
                      }
                      ToastManager.success("file scanner played");
                    });
            }
            else
            {
              ToastManager.error("error in play file scanner");
            }       
          },
          function(data) 
          {
            ToastManager.error("error in play file scanner");
          }
          );
      };


      $scope.stopFileScanner = function(x) {
        $scope.getScanningInfo(x);
        AdminUploadService.stopFileScanner(x.id)
        .then(function(response) {
            var responseStatus = parseInt(response.status);
            if(responseStatus >= 200 && responseStatus < 300)
            {
               AdminUploadService.getFileScanners()
                    .then(function(response){            
                      $scope.names = response.data;
                      for (var i=0; i<$scope.names.length; i++) {
                       $scope.names[i].toggle = false;
                       $scope.names[i].toggleHover = false;
                       $scope.x_state = -1;
                       if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                       } else {
                          $scope.names[i].styleColor = "color: darkgray;";
                       }
                     }
                      for (var i=0; i<$scope.names.length; i++) {
                        if ($scope.names[i].status == 'ok') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                        } else if ($scope.names[i].status == 'error') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                        } else if ($scope.names[i].status == 'added') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        } else if ($scope.names[i].status == 'running') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                        } else {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        }
                      }
                      ToastManager.success("file scanner stopped");
                    });
            }
            else
            {
              ToastManager.error("error in stop file scanner");
            }       
          },
          function(data) 
          {
            ToastManager.error("error in stop file scanner");
          }
          );
      };


      $scope.scheduleFileScanner = function(x) {
        $scope.getScanningInfo(x);
        var status = !x.active;
        if (status) {
          x.styleColor = "color: blue;";
        } else {
          x.styleColor = "color: darkgray;";
        }
        AdminUploadService.activateDeactivateFileScanner(x.id,status)
        .then(function(response) {
            var responseStatus = parseInt(response.status);
            if(responseStatus >= 200 && responseStatus < 300)
            {
               AdminUploadService.getFileScanners()
                    .then(function(response){            
                      $scope.names = response.data;
                      for (var i=0; i<$scope.names.length; i++) {
                       $scope.names[i].toggle = false;
                       $scope.names[i].toggleHover = false;
                       $scope.x_state = -1;
                       if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                       } else {
                          $scope.names[i].styleColor = "color: darkgray;";
                       }
                     }
                      for (var i=0; i<$scope.names.length; i++) {
                        if ($scope.names[i].status == 'ok') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                        } else if ($scope.names[i].status == 'error') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                        } else if ($scope.names[i].status == 'added') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        } else if ($scope.names[i].status == 'running') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                        } else {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        }
                      }
                      if (status) {
                        ToastManager.success("file scanner is activated");
                      } else {
                        ToastManager.success("file scanner is disactivated");
                      }
                    });
            }
            else
            {
              if (status) {
                ToastManager.success("error in activation file scanner");
              } else {
                ToastManager.success("error in disactivation file scanner");
              }
            }       
          },
          function(data) 
          {
            if (status) {
              ToastManager.success("error in activation file scanner");
            } else {
              ToastManager.success("error in disactivation file scanner");
            }
          }
          );
      };

      $scope.removeFileScanner = function(x) {
        $scope.getScanningInfo(x);
        $scope.scanninginfo = "";
        $scope.urltoscan = "";
        $scope.username = "";
        $scope.password = "";
        $scope.pattern = "";
        AdminUploadService.removeFileScanner(x.id)
        .then(function(response) {
            var responseStatus = parseInt(response.status);
            if(responseStatus >= 200 && responseStatus < 300)
            {
               AdminUploadService.getFileScanners()
                    .then(function(response){            
                      $scope.names = response.data;
                      for (var i=0; i<$scope.names.length; i++) {
                       $scope.names[i].toggle = false;
                       $scope.names[i].toggleHover = false;
                       $scope.x_state = -1;
                       if ($scope.names[i].active == true) {
                        $scope.names[i].styleColor = "color: blue;";
                       } else {
                          $scope.names[i].styleColor = "color: darkgray;";
                       }
                     }
                      for (var i=0; i<$scope.names.length; i++) {
                        if ($scope.names[i].status == 'ok') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-ok';
                        } else if ($scope.names[i].status == 'error') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-remove';
                        } else if ($scope.names[i].status == 'added') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                        } else if ($scope.names[i].status == 'running') {
                          $scope.names[i].classicon = 'glyphicon glyphicon-upload';
                        } else {
                          $scope.names[i].classicon = 'glyphicon glyphicon-question-sign';
                      }
                      }
                      ToastManager.success("file scanner removed");
                      $scope.scanninginfo = "";
                      $scope.urltoscan = "";
                      $scope.username = "";
                      $scope.password = "";
                      $scope.pattern = "";
                      $scope.collections = {};
                      $scope.x_state = -1;
                      $scope.toggle = false;
                      $scope.isSelectedFile = false;
                    });
            }
            else
            {
              ToastManager.error("error in remove file scanner");
            }       
          },
          function(data) 
          {
            ToastManager.error("error in remove file scanner");
          }
          );
      };

      
     $scope.uploadProduct = function(){
        if($scope.isSelectedFile == false) return;
        var file = $scope.fileToUpload;
        //console.log('file is ' );
       // console.dir(file);
        var uploadUrl = ApplicationConfig.baseUrl + "/api/upload";
        var selected = AdminCollectionManager.getSelectedCollectionsIds();
        //console.log('get selected ids!!!!  ',selected);
        AdminUploadService.uploadProduct(file, selected)
        .then( function(response){
            //console.log(response) ;
            var d = $('<div>').html(response.data);
           
            if(response.status == 200 || response.status == 201)
              AlertManager.success('Product Upload Succeeded',response.data);
            else
              AlertManager.error('Product Upload Error',response.data.replace('<style>','').replace('</style>',''));
           
                                     
          }, function(response){
            AlertManager.error('Product Upload Error',response.data.replace('<style>','').replace('</style>',''));
                   
          });
      };


  });
 