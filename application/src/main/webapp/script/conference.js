"use strict";

var ConferenceApp = angular.module("ConferenceApp", ["ngResource","ngAnimate","ui.grid","ui.grid.resizeColumns"]);

ConferenceApp.config( function($provide){ $provide.decorator('GridOptions', function($delegate){ return function(){

	var defaultTable = new $delegate();

	defaultTable.rowIdentity    = function(row) { return row.id; };
	defaultTable.getRowIdentity = function(row) { return row.id; };

	defaultTable.enableScrollbars     = false;
	defaultTable.enableSorting        = true;
	defaultTable.useExternalSorting   = true;
	defaultTable.enableFiltering      = true;
	defaultTable.useExternalFiltering = true;
	defaultTable.minRowsToShow        = 1;
	defaultTable.virtualizationThreshold = 99;

	return defaultTable;
};})});

function DefaultTable($scope, uiGridConstants){

	this.data = {};
	this.rowsPerPage = 5;
	this.getRowsParam = function(){ return this.rowsPerPage ? this.rowsPerPage : 5 };
	this.pageParam = 1;
	this.getPageParam = function(){ return this.pageParam ? this.pageParam : 1 };
	this.queryParam = '';
	this.getQueryParam = function(){ return this.queryParam === '' ? null : this.queryParam };
	this.sortParam = {};
	this.getSortParam = function(){
		var s = '';
		for(var key in this.sortParam) s += ( key + this.sortParam[key] );
		return s === '' ? null : s;
	};

	$scope.currentTable$42__ = this;

	this.onRegisterApi = function( gridApi ){

		var table = $scope.currentTable$42__;

		$scope.gridApi = gridApi;

		$scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ){
			if( sortColumns.length === 0 ){
				table.sortParam = {};
			}
			else {
				if( sortColumns.length === 1 )
					table.sortParam = {};

				for(var i=0; i < sortColumns.length; i++){

					switch( sortColumns[i].sort.direction ){
						case uiGridConstants.ASC:
							table.sortParam[sortColumns[i].name] = '+';
							break;
						case uiGridConstants.DESC:
							table.sortParam[sortColumns[i].name] = '-';
							break;
						default:
							delete table.sortParam[sortColumns[i].name];
							break;
					}
				}

				if( sortColumns.length > 1 )
					for(var key in table.sortParam){
						var found = false;
						for(var i=0; i < sortColumns.length; i++){
							found = key === sortColumns[i].name;
							if(found) break;
						}
						if(! found) delete table.sortParam[key];
					}
			}
		});

		$scope.gridApi.core.on.filterChanged( $scope, function() {

			var table = $scope.currentTable$42__;

			table.queryParam = '';

			for(var i=0; i < this.grid.columns.length; i++)
				for(var j=0; j < this.grid.columns[i].filters.length; j++)
					if( this.grid.columns[i].filters[j].term ) {

						table.queryParam += this.grid.columns[i].name;

						switch( this.grid.columns[i].filters[j].condition ) {
							case uiGridConstants.filter.GREATER_THAN_OR_EQUAL:
								table.queryParam += ">='";
								break;
							case uiGridConstants.filter.GREATER_THAN:
								table.queryParam += ">'";
								break;
							case uiGridConstants.filter.LESS_THAN_OR_EQUAL:
								table.queryParam += "<='";
								break;
							case uiGridConstants.filter.LESS_THAN:
								table.queryParam += "<'";
								break;
							case uiGridConstants.filter.ENDS_WITH:
							case uiGridConstants.filter.CONTAINS:
								table.queryParam += "=='*";
								break;
							case uiGridConstants.filter.NOT_EQUAL:
								table.queryParam += "!='";
								break;
							case uiGridConstants.filter.STARTS_WITH:
							case uiGridConstants.filter.EXACT:
							case undefined:
								table.queryParam += "=='";
								break;
						}

						table.queryParam += this.grid.columns[i].filters[j].term;

						switch( this.grid.columns[i].filters[j].condition ) {
							case uiGridConstants.filter.CONTAINS:
							case uiGridConstants.filter.STARTS_WITH:
								table.queryParam += "*";
								break;
						}

						table.queryParam += "' and ";
					}
			if( table.queryParam.endsWith("' and ") )
				table.queryParam = table.queryParam.substring(0, table.queryParam.length - 5);
		});
	};
};


// Conferences UI ------------------------------------------------------------------------------------------------------
ConferenceApp.factory('ConferenceREST', function ($resource) { return $resource('rest/conferences/:id'); });

ConferenceApp.controller("ConferenceCtrl", function ($scope, ConferenceREST, uiGridConstants) {

	$scope.queryRunning = false;
	$scope.table = new DefaultTable($scope, uiGridConstants);
	$scope.table.columnDefs = [
			{ name:'name', width:'25%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: 'starts with ...'} },
			{ name:'description', width:'49%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.CONTAINS, placeholder: 'contains ...'} },
			{ name:'from', width:'7%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=', term: '1999-12-31' },
					{ condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'to', width:'7%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=' },
                    { condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'location.name', width:'12%', displayName: 'Location', filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: '* is a wildcard'} }
		];

	// old stuff
	$scope.data = {};

    $scope.query = function(){
    	$scope.queryRunning = true;
		document.querySelector('.spinner').style.visibility = 'visible';
		ConferenceREST.query( { id:null,
								p:$scope.table.getPageParam(),
								r:$scope.table.getRowsParam(),
								s:$scope.table.getSortParam(),
								q:$scope.table.getQueryParam()},
								function(data){	//$scope.data.conferences = data;
												$scope.table.data = data;
												document.querySelector('.spinner').style.visibility = 'hidden';
												$scope.queryRunning = false;
											});
    };

	$scope.query();

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
});