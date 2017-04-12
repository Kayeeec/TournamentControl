(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameSetDetailController', GameSetDetailController);

    GameSetDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'GameSet', 'Game'];

    function GameSetDetailController($scope, $rootScope, $stateParams, previousState, entity, GameSet, Game) {
        var vm = this;

        vm.gameSet = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:gameSetUpdate', function(event, result) {
            vm.gameSet = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
