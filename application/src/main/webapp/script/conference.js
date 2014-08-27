var ConferenceApp = angular.module("ConferenceApp", ["ngResource"]);

ConferenceApp.factory('ConferenceFactory', function ($resource) {
	return $resource('rest/conferences/:id');
});

ConferenceApp.controller("ConferenceCtrl", function ($scope, ConferenceFactory) {

	$scope.data = {};
	
    $scope.getAllConferences = function(){
		ConferenceFactory.query(function(data) {
			$scope.data.conferences = data;
		});
    };
    
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