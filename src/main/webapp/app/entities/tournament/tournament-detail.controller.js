(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDetailController', TournamentDetailController);

    TournamentDetailController.$inject = ['My','$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Tournament', 'Game', 'User', 'Participant', 'SetSettings'];

    function TournamentDetailController(My, $scope, $rootScope, $stateParams, previousState, entity, Tournament, Game, User, Participant, SetSettings) {
        var vm = this;

        vm.tournament = entity;
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        My.savePreviousUrl(previousState);

        var unsubscribe = $rootScope.$on('tournamentControlApp:tournamentUpdate', function(event, result) {
            vm.tournament = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
