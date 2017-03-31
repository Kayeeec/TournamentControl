(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentController', TournamentController);

    TournamentController.$inject = ['Tournament', 'AllVersusAll'];

    function TournamentController(Tournament, AllVersusAll) {

        var vm = this;

        vm.tournaments = [];
        vm.allVersusAlls = [];

        loadAll();

        function loadAll() {
            Tournament.query(function(result) {
                vm.tournaments = result;
                vm.searchQuery = null;
            });
            AllVersusAll.query(function(result) {
                vm.allVersusAlls = result;
                vm.searchQuery = null;
            });
        }
        
        $('#myTab a[href="#allversusall"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#playoff"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        
        $("#allversusallTable").stupidtable();
    }
})();
