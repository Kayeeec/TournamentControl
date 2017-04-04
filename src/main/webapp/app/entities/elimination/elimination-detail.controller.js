(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDetailController', EliminationDetailController);

    EliminationDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Elimination'];

    function EliminationDetailController($scope, $rootScope, $stateParams, previousState, entity, Elimination) {
        var vm = this;

        vm.elimination = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:eliminationUpdate', function(event, result) {
            vm.elimination = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
