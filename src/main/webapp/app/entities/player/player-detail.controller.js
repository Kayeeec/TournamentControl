(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('PlayerDetailController', PlayerDetailController);

    PlayerDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Player', 'User', 'Team', 'My'];

    function PlayerDetailController($scope, $rootScope, $stateParams, previousState, entity, Player, User, Team, My) {
        var vm = this;

        vm.player = entity;
        
        vm.tournaments = Player.getAllTournaments({id: vm.player.id});
        vm.combinedTournaments = Player.getAllCombinedTournaments({id: vm.player.id});
        
        vm.teams = Player.getTeams({id: vm.player.id});
        var unsubscribe = $rootScope.$on('tournamentControlApp:playerUpdate', function(event, result) {
            vm.player = result;
        });
        $scope.$on('$destroy', unsubscribe);

        vm.getTournamentLink = My.getTournamentLink;
        
        vm.getCombinedTournamentLink = My.getCombinedTournamentLink;
        
        vm.backLink = function () {
            return My.backLink(previousState);
        };
        My.savePreviousUrl(previousState);
    }
})();
