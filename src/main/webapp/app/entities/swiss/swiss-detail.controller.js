(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('SwissDetailController', SwissDetailController);

    SwissDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Swiss'];

    function SwissDetailController($scope, $rootScope, $stateParams, previousState, entity, Swiss) {
        var vm = this;

        vm.swiss = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:swissUpdate', function(event, result) {
            vm.swiss = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
