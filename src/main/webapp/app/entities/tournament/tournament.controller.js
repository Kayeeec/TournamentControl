(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentController', TournamentController);

    TournamentController.$inject = ['Tournament', 'AllVersusAll', 'Elimination', 'ParseLinks', 'AlertService', 'paginationConstants'];

    function TournamentController(Tournament, AllVersusAll, Elimination, ParseLinks, AlertService, paginationConstants) {

        var vm = this;

        //vm.tournaments = [];
        vm.allVersusAlls = [];
        
// BEGIN elimination ============================================
        vm.eliminations = [];
        vm.loadPage = loadPage;
        vm.itemsPerPage = paginationConstants.itemsPerPage;
        vm.page = 0;
        vm.links = {
            last: 0
        };
        vm.predicate = 'id';
        vm.reset = reset;
        vm.reverse = true;
// END elimination ============================================

        loadAll();

        function loadAll() {
//            Tournament.query(function(result) {
//                vm.tournaments = result;
//                vm.searchQuery = null;
//            });
            AllVersusAll.query(function(result) {
                vm.allVersusAlls = result;
                vm.searchQuery = null;
            });
            
// BEGIN elimination ============================================
            Elimination.query({
                page: vm.page,
                size: vm.itemsPerPage,
                sort: sort()
            }, onSuccess, onError);
            function sort() {
                var result = [vm.predicate + ',' + (vm.reverse ? 'asc' : 'desc')];
                if (vm.predicate !== 'id') {
                    result.push('id');
                }
                return result;
            }

            function onSuccess(data, headers) {
                vm.links = ParseLinks.parse(headers('link'));
                vm.totalItems = headers('X-Total-Count');
                for (var i = 0; i < data.length; i++) {
                    vm.eliminations.push(data[i]);
                }
            }

            function onError(error) {
                AlertService.error(error.data.message);
            }
// END elimination ============================================
        }
        
        function reset () {
            vm.page = 0;
            vm.eliminations = [];
            loadAll();
        }

        function loadPage(page) {
            vm.page = page;
            loadAll();
        }
        
        $('#myTab a[href="#allversusall"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        $('#myTab a[href="#elimination"]').click(function (e) {
            e.preventDefault();
            $(this).tab('show');
        });
        
        $("#allversusallTable").stupidtable();
        $("#eliminationTable").stupidtable();
    }
})();
