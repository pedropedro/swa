var ConferenceApp = angular.module("ConferenceApp", ["ngResource","ngAnimate","ui.grid","ui.grid.resizeColumns"]);

ConferenceApp.config( function($provide){ $provide.decorator('GridOptions', function($delegate){ return function(){
	var defaultTable = new $delegate();

	defaultTable.data = {};

	defaultTable.rowIdentity    = function(row) { return row.id; };
	defaultTable.getRowIdentity = function(row) { return row.id; };

	defaultTable.enableScrollbars     = false;
	defaultTable.enableSorting        = true;
	defaultTable.useExternalSorting   = true;
	defaultTable.enableFiltering      = true;
	defaultTable.useExternalFiltering = true;
	defaultTable.minRowsToShow        = 1;

	return defaultTable;
};})});

ConferenceApp.factory('ConferenceFactory', function ($resource) { return $resource('rest/conferences/:id'); });

ConferenceApp.controller("ConferenceCtrl", function ($scope, ConferenceFactory, uiGridConstants) {

	$scope.table = {

		// common definitions - to be refactored into a separate module
		rowsPerPage : 5,
		queryParam : '',
		sortParam : '',  // TODO: associative array [name:DIR] + getSortString()

		onRegisterApi: function( gridApi ) {
			$scope.gridApi = gridApi;
			$scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ) {
				if( sortColumns.length === 0 ) {
					$scope.table.sortParam = '';
				}
				else {
					if( sortColumns.length === 1 )
						$scope.table.sortParam = '';

					for(var i=0; i < sortColumns.length; i++) {

						// skip already defined sort fields (from previous click)
						if( $scope.table.sortParam.indexOf(sortColumns[i].name) >= 0 )
							continue;

						switch( sortColumns[i].sort.direction ) {
							case uiGridConstants.ASC:
								$scope.table.sortParam += sortColumns[i].name;
								$scope.table.sortParam += '+';
								break;
							case uiGridConstants.DESC:
								$scope.table.sortParam += sortColumns[i].name;
								$scope.table.sortParam += '-';
								break;
							case undefined:
								break;
						}
					}
				}
			});
			$scope.gridApi.core.on.filterChanged( $scope, function() {
				$scope.table.queryParam = '';

				for(var i=0; i < this.grid.columns.length; i++)
					for(var j=0; j < this.grid.columns[i].filters.length; j++)
						if( this.grid.columns[i].filters[j].term ) {

							$scope.table.queryParam += this.grid.columns[i].name;

							switch( this.grid.columns[i].filters[j].condition ) {
								case uiGridConstants.filter.GREATER_THAN_OR_EQUAL:
									$scope.table.queryParam += ">='";
									break;
								case uiGridConstants.filter.GREATER_THAN:
									$scope.table.queryParam += ">'";
									break;
								case uiGridConstants.filter.LESS_THAN_OR_EQUAL:
									$scope.table.queryParam += "<='";
									break;
								case uiGridConstants.filter.LESS_THAN:
									$scope.table.queryParam += "<'";
									break;
								case uiGridConstants.filter.ENDS_WITH:
								case uiGridConstants.filter.CONTAINS:
									$scope.table.queryParam += "=='*";
									break;
								case uiGridConstants.filter.NOT_EQUAL:
									$scope.table.queryParam += "!='";
									break;
								case uiGridConstants.filter.STARTS_WITH:
								case uiGridConstants.filter.EXACT:
								case undefined:
									$scope.table.queryParam += "=='";
									break;
							}

							$scope.table.queryParam += this.grid.columns[i].filters[j].term;

							switch( this.grid.columns[i].filters[j].condition ) {
								case uiGridConstants.filter.CONTAINS:
								case uiGridConstants.filter.STARTS_WITH:
									$scope.table.queryParam += "*";
									break;
							}

							$scope.table.queryParam += "' and ";
						}
				if( $scope.table.queryParam.endsWith("' and ") )
					$scope.table.queryParam = $scope.table.queryParam.substring(0, $scope.table.queryParam.length - 5);
			});
		}

		// controller specific stuff
		, columnDefs: [
			{ name:'name', width:'20%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: 'starts with ...'} },
			{ name:'description', width:'40%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.CONTAINS, placeholder: 'contains ...'} },
			{ name:'from', width:'10%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=', term: '2000-01-01' },
					{ condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'to', width:'10%', type: 'date', cellFilter: 'date:"yyyy-MM-dd"', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=' },
                    { condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'location.name', width:'20%', displayName: 'Location', filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: '* is a wildcard'} }
		]
	};


	// old stuff
	$scope.data = {};

    $scope.getAllConferences = function(){
		ConferenceFactory.query(function(data) {
			$scope.data.conferences = data;
			$scope.table.data = data;
		});
    };

	$scope.getAllConferences();

    $scope.removeConference = function(id){
    	ConferenceFactory.remove({id:id});
    };

    $scope.saveConference = function(){
    	ConferenceFactory.save($scope.data.currentConference);
    	$scope.data.currentConference = {};
    };

    $scope.editConference = function(id){
    	ConferenceFactory.get({id:id}, function(data) {
			$scope.data.currentConference = data;
	    	$scope.data.currentConference.from = new Date($scope.data.currentConference.from).format("YYYY-MM-DD");
	    	$scope.data.currentConference.to = new Date($scope.data.currentConference.to).format("YYYY-MM-DD");;
		});
    }
});