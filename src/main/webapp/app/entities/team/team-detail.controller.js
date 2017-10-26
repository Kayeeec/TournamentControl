(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TeamDetailController', TeamDetailController);

    TeamDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Team', 'User', 'Player'];

    function TeamDetailController($scope, $rootScope, $stateParams, previousState, entity, Team, User, Player) {
        var vm = this;

        vm.team = entity;
        vm.previousState = previousState.name;
        vm.tournaments = Team.getAllTournaments({id: vm.team.id});

        var unsubscribe = $rootScope.$on('tournamentControlApp:teamUpdate', function(event, result) {
            vm.team = result;
        });
        $scope.$on('$destroy', unsubscribe);
        
        vm.getTournamentLink =function(tournament){
            if(tournament.tournamentType === "swiss"){
                return "swiss-detail({id: "+tournament.id+"})";
            }
            if(tournament.tournamentType === "elimination"){
                return "elimination-detail({id: "+tournament.id+"})";
            }
            if(tournament.tournamentType === "allVersusAll"){
                return "all-versus-all-detail({id:"+tournament.id+"})";
            }
            return "tournament-detail({id:"+tournament.id+"})";
        };
    }
})();
