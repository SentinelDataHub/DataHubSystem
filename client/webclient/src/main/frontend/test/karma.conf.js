// Karma configuration
// http://karma-runner.github.io/0.12/config/configuration-file.html
// Generated on 2015-07-23 using
// generator-karma 1.0.0

module.exports = function(config) {
  'use strict';

  config.set({
    // enable / disable watching file and executing tests whenever any file changes
    autoWatch: true,

    // base path, that will be used to resolve files and exclude
    basePath: '../',

    // testing framework to use (jasmine/mocha/qunit/...)
    // as well as any additional frameworks (requirejs/chai/sinon/...)
    frameworks: [
      "jasmine"
    ],

    // list of files / patterns to load in the browser
    files: [
      // bower:js
      "libs/jquery/dist/jquery.js",
      "libs/angular/angular.js",
      "libs/angular-animate/angular-animate.js",
      "libs/angular-aria/angular-aria.js",
      "libs/angular-bootstrap/ui-bootstrap-tpls.js",
      "libs/angular-cookies/angular-cookies.js",
      "libs/angular-messages/angular-messages.js",
      "libs/angular-resource/angular-resource.js",
      "libs/angular-route/angular-route.js",
      "libs/angular-sanitize/angular-sanitize.js",
      "libs/angular-touch/angular-touch.js",
      "libs/bootstrap/dist/js/bootstrap.js",
      "libs/cesiumjs/Cesium/Cesium.js",
      "libs/jquery-ui/jquery-ui.js",
      "libs/moment/moment.js",
      "libs/ngToast/dist/ngToast.js",
      "libs/ol3-bower/ol.js",
      "libs/underscore/underscore.js",
      "libs/angular-mocks/angular-mocks.js",
      // endbower
      "app/scripts/**/*.js",
      "test/mock/**/*.js",
      "test/spec/**/*.js"
    ],

    // list of files / patterns to exclude
    exclude: [
    ],

    // web server port
    port: 8080,

    // Start these browsers, currently available:
    // - Chrome
    // - ChromeCanary
    // - Firefox
    // - Opera
    // - Safari (only Mac)
    // - PhantomJS
    // - IE (only Windows)
    browsers: [
      "PhantomJS"
    ],

    // Which plugins to enable
    plugins: [
      "karma-phantomjs-launcher",
      "karma-jasmine"
    ],

    // Continuous Integration mode
    // if true, it capture browsers, run tests and exit
    singleRun: false,

    colors: true,

    // level of logging
    // possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
    logLevel: config.LOG_INFO,

    // Uncomment the following lines if you are using grunt's server to run the tests
    // proxies: {
    //   '/': 'http://localhost:9000/'
    // },
    // URL root prevent conflicts with the site root
    // urlRoot: '_karma_'
  });
};
