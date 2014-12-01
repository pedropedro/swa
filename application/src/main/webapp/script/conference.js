"use strict";

var ConferenceApp = angular.module('ConferenceApp', [
'ngResource','ngAnimate','ngRoute',
'ui.grid','ui.grid.resizeColumns',
'ui.directives']);

ConferenceApp.config( function($provide){ $provide.decorator('GridOptions', function($delegate){ return function(){

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

/* R-SQL service
 * getWhere() - build a R-SQL conform filter / "where" string using current visible values in the grid filter
 * getOrderBy - build a R-SQL conform sort / "order by" string using current visible values in the grid column sorter
 */
ConferenceApp.factory('rsql', ['uiGridConstants', function(uiGridConstants) {
	return {
		getWhere : // Build a R-SQL "where" filter string
		function (aGrid) {

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

// TEST
ConferenceApp.constant('myDiConst', 'C');
ConferenceApp.value('myDiValue', 21);
ConferenceApp.factory('myDiFactory', ['myDiConst', function(c){ return { f1:function(){return c}, f2:function(n){return n*n} } }]);
ConferenceApp.service('myDiService', ['myDiValue', function(v){ this.a=v; this.f3 = function(b){return b+this.a}; } ]);
ConferenceApp.service('myService', ['myDiFactory','myDiService', function(f,s){
	this.factory = f;
	this.service = s;
	this.f4 = function(b){return this.factory.f1() + this.service.f3(b)};
	this.f5 = function(n){return this.factory.f2(n)};
}]);

// Create a default table in given scope
function createDefaultTable( $scope, rsql, rest ){

	$scope.table = {};
	$scope.table.data = {};
	$scope.table.errors = [];
	$scope.table.rowsPerPage = 5;
	$scope.table.getRowsParam = function(){ return $scope.table.rowsPerPage ? $scope.table.rowsPerPage : 5 };
	$scope.table.pageParam = 1;
	$scope.table.getPageParam = function(){ return $scope.table.pageParam ? $scope.table.pageParam : 1 };
	$scope.table.queryParam = '';
	$scope.table.getQueryParam = function(){ return $scope.table.queryParam === '' ? null : $scope.table.queryParam };
	$scope.table.sortParam = {};
	$scope.table.getSortParam = function(){
		var s = '';
		for(var key in $scope.table.sortParam) s += ( key + $scope.table.sortParam[key] );
		return s === '' ? null : s;
	};

	$scope.table.onRegisterApi = function( gridApi ){
		if(rsql) {
			gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ){
				$scope.table.sortParam = rsql.getOrderBy( sortColumns, $scope.table.sortParam );
			});

			gridApi.core.on.filterChanged( $scope, function() {
				$scope.table.queryParam = rsql.getWhere( this.grid );
			});
		};
	};

	$scope.table.queryRunning = false;
	$scope.table.isQueryRunning = function(){ return $scope.table.queryRunning };
	$scope.table.query = function(){
		if(rest) {
			$scope.table.queryRunning = true;

			rest.query({id:null, p:$scope.table.getPageParam(), r:$scope.table.getRowsParam(),
						s:$scope.table.getSortParam(), q:$scope.table.getQueryParam()}
						).$promise.then(
							function(data){
								$scope.table.data = data;
								$scope.table.queryRunning = false;
							},
							function( error ){
								$scope.table.errors.push(getErrorAsString(error));
								$scope.table.queryRunning = false;
							});
		}
    };

	$scope.getTable = function() { return $scope.table };
};

// RESTEasy implementation !
function getErrorAsString( error ){
	var s = "";
	for( var key in error.data ){
		if(Array.isArray(error.data[key]))
			for( var subkey in error.data[key] ){
				s += error.data[key][subkey]['constraintType'];
				s += " ";
				s += error.data[key][subkey]['value'];
				s += " : ";
				s += error.data[key][subkey]['message'];
				s += "\n";
			}
		else if(error.data[key]) s += ( error.data[key] + "\n" );
	}
	return s;
};

// Conferences UI ------------------------------------------------------------------------------------------------------
ConferenceApp.factory('ConferenceREST', ['$resource', function(r){ return r('rest/conferences/:id'); }]);

ConferenceApp.controller("ConferenceCtrl", ['$scope','uiGridConstants','rsql','ConferenceREST','$log',
function( $scope, uiGridConstants, rsql, ConferenceREST, $log ) {

	createDefaultTable( $scope, rsql, ConferenceREST );

	$scope.getTable().columnDefs = [
			{ name:'name', width:'25%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: 'starts with ...'} },
			{ name:'description', width:'49%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.CONTAINS, placeholder: 'contains ...'} },
			{ name:'from', width:'7%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=', term: '2010-12-31' },
					{ condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'to', width:'7%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=' },
                    { condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'location.name', width:'12%', displayName: 'Location', filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: '* is a wildcard'} }
		];

	$scope.$log = $log;

	$log.info('INFO logged');

//	$scope.getTable().query();

/*
	$scope.remove = function(id){
		ConferenceREST.remove({id:id});
	};

	$scope.save = function(){
		ConferenceREST.save($scope.data.currentConference);
		$scope.data.currentConference = {};
	};

	$scope.edit = function(id){
		ConferenceREST.get({id:id}, function(data) {
			$scope.data.currentConference = data;
			$scope.data.currentConference.from = new Date($scope.data.currentConference.from).format("YYYY-MM-DD");
			$scope.data.currentConference.to = new Date($scope.data.currentConference.to).format("YYYY-MM-DD");;
		});
	}
*/
}]);