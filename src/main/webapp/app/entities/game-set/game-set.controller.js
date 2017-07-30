(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('GameSetController', GameSetController);

    GameSetController.$inject = ['GameSet'];

    function GameSetController(GameSet) {

        var vm = this;

        vm.gameSets = [];

        loadAll();

        function loadAll() {
            GameSet.query(function(result) {
                vm.gameSets = result;
                vm.searchQuery = null;
            });
        }
    }
})();
