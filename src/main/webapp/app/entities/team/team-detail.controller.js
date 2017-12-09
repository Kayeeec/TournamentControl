(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TeamDetailController', TeamDetailController);

    TeamDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Team', 'User', 'Player', 'My'];

    function TeamDetailController($scope, $rootScope, $stateParams, previousState, entity, Team, User, Player, My) {
        var vm = this;

        vm.team = entity;
        vm.previousState = previousState.name;
        vm.tournaments = Team.getAllTournaments({id: vm.team.id});

        var unsubscribe = $rootScope.$on('tournamentControlApp:teamUpdate', function(event, result) {
            vm.team = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        vm.getTournamentLink = My.getTournamentLink;
    }
})();
