(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameController', GameController);

    GameController.$inject = ['Game'];

    function GameController(Game) {

        var vm = this;

        vm.games = [];

        loadAll();

        function loadAll() {
            Game.query(function(result) {
                vm.games = result;
                vm.searchQuery = null;
            });
        }
    }
})();
