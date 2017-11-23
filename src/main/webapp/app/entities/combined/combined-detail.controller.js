(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('CombinedDetailController', CombinedDetailController);

    CombinedDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Combined', 'Participant', 'Tournament'];

    function CombinedDetailController($scope, $rootScope, $stateParams, previousState, entity, Combined, Participant, Tournament) {
        var vm = this;

        vm.combined = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:combinedUpdate', function(event, result) {
            vm.combined = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
