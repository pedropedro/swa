var ConferenceApp = angular.module("ConferenceApp", ["ngResource","ngAnimate","ui.grid","ui.grid.resizeColumns"]);

ConferenceApp.factory('ConferenceFactory', function ($resource) {
	return $resource('rest/conferences/:id');
});

ConferenceApp.controller("ConferenceCtrl", function ($scope, ConferenceFactory, uiGridConstants) {

	$scope.rowsPerPage = 5;
	$scope.queryParam = '';
	$scope.sortParam = '';

	$scope.table = {
		data : {},
		enableSorting: true,
		useExternalSorting: true,
		enableFiltering: true,
		useExternalFiltering: true,
		columnDefs: [
			{ name:'name', width:'20%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: 'starts with ...'} },
			{ name:'description', width:'40%', enableSorting: false, filter:
					{ condition: uiGridConstants.filter.CONTAINS, placeholder: 'contains ...'} },
			{ name:'from', width:'10%', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=', term: 959817600000 },
					{ condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'to', width:'10%', filters: [
					{ condition: uiGridConstants.filter.GREATER_THAN_OR_EQUAL, placeholder: '>=' },
                    { condition: uiGridConstants.filter.LESS_THAN_OR_EQUAL,	placeholder: '=<' }	] },
			{ name:'location.name', width:'20%', displayName: 'Location', filter:
					{ condition: uiGridConstants.filter.STARTS_WITH, placeholder: '* is a wildcard'} }
		],
		onRegisterApi: function( gridApi ) {
			$scope.gridApi = gridApi;
			$scope.gridApi.core.on.sortChanged( $scope, function( grid, sortColumns ) {
				if( sortColumns.length === 0 ) {
					$scope.getAllConferences();
					$scope.sortParam = '';
				}
				else {
					$scope.sortParam = '';
					for(var i=0; i < sortColumns.length; i++) {
						switch( sortColumns[i].sort.direction ) {
							case uiGridConstants.ASC:
								$scope.sortParam += sortColumns[i].name;
								$scope.sortParam += '+';
								break;
							case uiGridConstants.DESC:
								$scope.sortParam += sortColumns[i].name;
								$scope.sortParam += '-';
								break;
							case undefined:
								break;
						}
					}
				}
			});
			$scope.gridApi.core.on.filterChanged( $scope, function() {
				$scope.queryParam = '';

				for(var i=0; i < this.grid.columns.length; i++)
					for(var j=0; j < this.grid.columns[i].filters.length; j++)
						if( this.grid.columns[i].filters[j].term ) {

							$scope.queryParam += this.grid.columns[i].name;

							switch( this.grid.columns[i].filters[j].condition ) {
								case uiGridConstants.filter.GREATER_THAN_OR_EQUAL:
									$scope.queryParam += ">='";
									break;
								case uiGridConstants.filter.GREATER_THAN:
									$scope.queryParam += ">'";
									break;
								case uiGridConstants.filter.LESS_THAN_OR_EQUAL:
									$scope.queryParam += "<='";
									break;
								case uiGridConstants.filter.LESS_THAN:
									$scope.queryParam += "<'";
									break;
								case uiGridConstants.filter.ENDS_WITH:
								case uiGridConstants.filter.CONTAINS:
									$scope.queryParam += "=='*";
									break;
								case uiGridConstants.filter.NOT_EQUAL:
									$scope.queryParam += "!='";
									break;
								case uiGridConstants.filter.STARTS_WITH:
								case uiGridConstants.filter.EXACT:
								case undefined:
									$scope.queryParam += "=='";
									break;
							}

							$scope.queryParam += this.grid.columns[i].filters[j].term;

							switch( this.grid.columns[i].filters[j].condition ) {
								case uiGridConstants.filter.CONTAINS:
								case uiGridConstants.filter.STARTS_WITH:
									$scope.queryParam += "*";
									break;
							}

							$scope.queryParam += "' and ";
						}
				if( $scope.queryParam.endsWith("' and ") )
					$scope.queryParam = $scope.queryParam.substring(0, $scope.queryParam.length - 5);
			});
		}
	};

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