(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('PlayerDetailController', PlayerDetailController);

    PlayerDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Player', 'User', 'Team'];

    function PlayerDetailController($scope, $rootScope, $stateParams, previousState, entity, Player, User, Team) {
        var vm = this;

        vm.player = entity;
        vm.previousState = previousState.name;
        vm.tournaments = Player.getAllTournaments({id: vm.player.id});

        var unsubscribe = $rootScope.$on('tournamentControlApp:playerUpdate', function(event, result) {
            vm.player = result;
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
