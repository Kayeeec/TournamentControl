(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('TournamentDeleteController',TournamentDeleteController);

    TournamentDeleteController.$inject = ['$uibModalInstance', 'entity', 'Tournament'];

    function TournamentDeleteController($uibModalInstance, entity, Tournament) {
        var vm = this;

        vm.tournament = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Tournament.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
