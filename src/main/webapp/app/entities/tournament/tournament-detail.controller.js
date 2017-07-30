(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDetailController', TournamentDetailController);

    TournamentDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Tournament', 'Game', 'User', 'Participant', 'SetSettings'];

    function TournamentDetailController($scope, $rootScope, $stateParams, previousState, entity, Tournament, Game, User, Participant, SetSettings) {
        var vm = this;

        vm.tournament = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('tournamentControlApp:tournamentUpdate', function(event, result) {
            vm.tournament = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
