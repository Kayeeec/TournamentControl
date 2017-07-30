(function() {
    'use strict';

    angular
        .module('tournamentControlApp')
        .controller('EliminationDeleteController',EliminationDeleteController);

    EliminationDeleteController.$inject = ['$uibModalInstance', 'entity', 'Elimination'];

    function EliminationDeleteController($uibModalInstance, entity, Elimination) {
        var vm = this;

        vm.elimination = entity;
        vm.clear = clear;
        vm.confirmDelete = confirmDelete;

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function confirmDelete (id) {
            Elimination.delete({id: id},
                function () {
                    $uibModalInstance.close(true);
                });
        }
    }
})();
