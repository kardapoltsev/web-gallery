// 1. Install nodejs (it will automatically installs requirejs)
 
//- assuming the folder structure for your app is:
// app
// 		css
// 		img
// 		js
// 		main.js
// 		index.html
// build
// 		app.build.js
// 		r.js (downloaded from requirejs website)
 
// 2. the command line to run:
// $ node r.js -o app.build.js
// 
 
({
	//- paths are relative to this app.build.js file
	appDir: "app",
	baseUrl: "js",
  paths: {
    app: "app",
//    require: "",
    router: "router",
    jquery: "lib/jquery/dist/jquery",
    "jquery-ui": "lib/jquery-ui/jquery-ui",
    "magnific-popup": "lib/magnific-popup/dist/jquery.magnific-popup",
    backbone: "lib/backbone/backbone",
    "backbone-relational": "lib/backbone-relational/backbone-relational",
    underscore: "lib/underscore/underscore",
    "bootstrap-tagsinput": "lib/bootstrap-tagsinput/dist/bootstrap-tagsinput",
    "bootstrap": "lib/bootstrap/dist/js/bootstrap",
    "file-upload": "lib/jquery-file-upload/js/jquery.fileupload",
    "iframe-transport": "lib/jquery-file-upload/js/jquery.iframe-transport",
    "jquery.ui.widget": "lib/jquery-ui/ui/widget",
    "typeahead": "lib/typeahead.js/dist/typeahead.jquery"
  },
  shim: {
    "jquery-ui": {
      exports: "$",
      deps: [
        "jquery"
      ]
    },
    "magnific-popup": {
      deps: [
        "jquery"
      ]
    },
    "backbone": {
      deps: ["underscore", "jquery"],
      exports: "Backbone"
    },
    "backbone-relational": {
      deps: ["underscore", "backbone"]
    },
    "underscore": {
      exports: "_"
    },
    "bootstrap-tagsinput": {
      deps: ["bootstrap", "typeahead"]
    },
    "typeahead": {
      deps: ["jquery"]
    },
    "bootstrap": {
      deps: ["jquery"]
    },
    "file-upload": {
      deps: ["iframe-transport"]
    }
  },
	//- this is the directory that the new files will be. it will be created if it doesn't exist
	dir: "app-build",
	optimizeCss: "standard.keepLines",
//  optimize: 'uglify2',
  optimize: 'none',
	modules: [
		{
			name: "app"
		}
	],
	fileExclusionRegExp: /\.git/
})
