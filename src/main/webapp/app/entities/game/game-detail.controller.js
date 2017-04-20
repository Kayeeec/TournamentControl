(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameDetailController', GameDetailController);

    GameDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Game', 'Tournament', 'Participant', 'GameSet'];

    function GameDetailController($scope, $rootScope, $stateParams, previousState, entity, Game, Tournament, Participant, GameSet) {
        var vm = this;

        vm.game = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:gameUpdate', function(event, result) {
            vm.game = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
    
    
        
        
})();
