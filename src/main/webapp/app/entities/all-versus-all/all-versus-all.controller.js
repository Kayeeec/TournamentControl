(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('AllVersusAllController', AllVersusAllController);

    AllVersusAllController.$inject = ['AllVersusAll'];

    function AllVersusAllController(AllVersusAll) {

        var vm = this;

        vm.allVersusAlls = [];

        loadAll();

        function loadAll() {
            AllVersusAll.query(function(result) {
                vm.allVersusAlls = result;
                vm.searchQuery = null;
            });
        }
    }
})();
