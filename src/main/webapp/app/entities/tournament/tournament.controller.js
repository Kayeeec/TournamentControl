(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentController', TournamentController);

    TournamentController.$inject = ['Tournament'];

    function TournamentController(Tournament) {

        var vm = this;

        vm.tournaments = [];

        loadAll();

        function loadAll() {
            Tournament.query(function(result) {
                vm.tournaments = result;
                vm.searchQuery = null;
            });
        }
    }
})();
