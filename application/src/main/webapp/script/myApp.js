/* Demo AngularJS app for JUnit tests */
"use strict";

// Declare a new module with dependencies to be injected
var myApp = angular.module('myApp', ['ngResource','ngAnimate','ngRoute','ui.grid','ui.grid.resizeColumns']);

// Define different flavours of Angular injectable services
myApp.constant('myDiConst', 'C');

myApp.value('myDiValue', 21);

myApp.factory('myDiFactory', ['myDiConst', function(c){
	return {
		f1:function(){return c},
		f2:function(n){return n*n}
	}
}]);

myApp.service('myDiService', ['myDiValue', function(v){
	this.a = v;
	this.f3 = function(b){return b+this.a};
}]);

myApp.service('myService', ['myDiFactory','myDiService', function(f,s){
	this.factory = f;
	this.service = s;
	this.f4 = function(b){return this.factory.f1() + this.service.f3(b)};
	this.f5 = function(n){return this.factory.f2(n)};
}]);

/* R-SQL service
 * getWhere() - build a R-SQL conform filter / "where" string using current visible values in the grid filter
 * getOrderBy - build a R-SQL conform sort / "order by" string using current visible values in the grid column sorter
 */
myApp.factory('rsql', ['uiGridConstants', function(uiGridConstants) {
	return {
		getWhere : // Build a R-SQL "where" filter string
		function (aGrid) {
			// a grid from ui.grid module

			var where = '';

			for(var i=0; i < aGrid.columns.length; i++)
				for(var j=0; j < aGrid.columns[i].filters.length; j++)
					if( aGrid.columns[i].filters[j].term ) {

						where += aGrid.columns[i].name;

						switch( aGrid.columns[i].filters[j].condition ) {
							case uiGridConstants.filter.GREATER_THAN_OR_EQUAL:
								where += ">='";
								break;
							case uiGridConstants.filter.GREATER_THAN:
								where += ">'";
								break;
							case uiGridConstants.filter.LESS_THAN_OR_EQUAL:
								where += "<='";
								break;
							case uiGridConstants.filter.LESS_THAN:
								where += "<'";
								break;
							case uiGridConstants.filter.ENDS_WITH:
							case uiGridConstants.filter.CONTAINS:
								where += "=='*";
								break;
							case uiGridConstants.filter.NOT_EQUAL:
								where += "!='";
								break;
							case uiGridConstants.filter.STARTS_WITH:
							case uiGridConstants.filter.EXACT:
							case undefined:
								where += "=='^";
								break;
						}

						where += aGrid.columns[i].filters[j].term;

						switch( aGrid.columns[i].filters[j].condition ) {
							case uiGridConstants.filter.CONTAINS:
							case uiGridConstants.filter.STARTS_WITH:
								where += "*";
								break;
						}

						where += "' and ";
					}

			if( where.length ) where = where.substring(0, where.length - 5);

			return where;
		},

		getOrderBy : // Build a R-SQL "order by" sorting string
		function (sortColumns, aSorting) {

			if( sortColumns.length <= 1 ) aSorting = {};

			for(var i=0; i < sortColumns.length; i++){

				switch( sortColumns[i].sort.direction ){
					case uiGridConstants.ASC:
						aSorting[sortColumns[i].name] = '+';
						break;
					case uiGridConstants.DESC:
						aSorting[sortColumns[i].name] = '-';
						break;
					default:
						delete aSorting[sortColumns[i].name];
						break;
				}
			}

			if( sortColumns.length > 1 )
				for(var key in aSorting){
					var found = false;
					for(var i=0; i < sortColumns.length; i++){
						found = key === sortColumns[i].name;
						if(found) break;
					}
					if(! found) delete aSorting[key];
				}

			return aSorting;
		}
	};
}]);

// interacting with browser window object
myApp.factory('myAlert', ['$window', function(win){
	return function(msg){
		win.alert(msg);
		return "42";
	}
}]);

// directive with template
myApp.directive('templateDir', function() {
	return {
		restrict: 'E',
		replace: true,
		template: '<p>ABC{{x + y}}</p>'
	};
});

// directive with link function
myApp.directive('linkDir', function() {
	return {
		restrict: 'A',
		link: function(s,e,a) { s.zzz = e.element + a.attribute; }
	};
});

// filter
myApp.filter('reverse', function() {
	return function(input, uppercase) {
		input = input || '';
		var out = '';
		for (var i = 0; i < input.length; i++) {
			out = input.charAt(i) + out;
		}
		// conditional based on optional argument
		if (uppercase) {
			out = out.toUpperCase();
		}
		return out;
	};
});


// controller
myApp.controller("MainCtrl", ['$scope','$resource','$log', function( $scope, $resource, $log ) {

	var rest = $resource('rest/conferences/:id');

	$scope.table = {};
	$scope.table.data = {};
	$scope.table.errors = [];

	$scope.table.query = function(){
		if(rest) {
			$scope.table.queryRunning = true;

			rest.query({id:null, p:1, r:5, s:null, q:null}).$promise.then(
							function(data){
								$scope.table.data = data;
								$scope.table.queryRunning = false;
							},
							function( error ){
								$scope.table.errors.push(error.data.msg);
								$scope.table.queryRunning = false;
							});
		}
    };
	$scope.getTable = function() { return $scope.table };

	$scope.$log = $log;

	$scope.$log.info('INFO logged');

//	$scope.count = 0;
//	$scope.$on('MyEvent', function() { $scope.count++; });
}]);

// scope inheritance
myApp.controller("ChildCtrl", ['$scope', function(childScope){
/*
beforeEach(inject(function($rootScope, $controller) {
    mainScope = $rootScope.$new();
    $controller('MainController', {$scope: mainScope});
    childScope = mainScope.$new();
    $controller('ChildController', {$scope: childScope});
    grandChildScope = childScope.$new();
    $controller('GrandChildController', {$scope: grandChildScope});
}));
*/
}]);

// decorator

// config
myApp.config( function($provide){ $provide.decorator('GridOptions', function($delegate){ return function(){

	var defaultTable = new $delegate();

	defaultTable.getRowIdentity   = function(row) { return row.id; };
	defaultTable.enableRowHashing = false;

	defaultTable.enableScrollbars     = false;
	defaultTable.enableSorting        = true;
	defaultTable.useExternalSorting   = true;
	defaultTable.enableFiltering      = true;
	defaultTable.useExternalFiltering = true;
	defaultTable.minRowsToShow        = 1;
	defaultTable.virtualizationThreshold = 99;

	return defaultTable;
};})});
