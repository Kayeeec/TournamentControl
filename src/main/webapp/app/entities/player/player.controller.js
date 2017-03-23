(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('PlayerController', PlayerController);

    PlayerController.$inject = ['Player'];

    function PlayerController(Player) {

        var vm = this;

        vm.players = [];

        loadAll();

        function loadAll() {
            Player.query(function(result) {
                vm.players = result;
                vm.searchQuery = null;
            });
        }
    }
})();
